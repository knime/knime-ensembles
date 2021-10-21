/*
 * ------------------------------------------------------------------------
 *  Copyright by KNIME AG, Zurich, Switzerland
 *  Website: http://www.knime.com; Email: contact@knime.com
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License, Version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>.
 *
 *  Additional permission under GNU GPL version 3 section 7:
 *
 *  KNIME interoperates with ECLIPSE solely via ECLIPSE's plug-in APIs.
 *  Hence, KNIME and ECLIPSE are both independent programs and are not
 *  derived from each other. Should, however, the interpretation of the
 *  GNU GPL Version 3 ("License") under any applicable laws result in
 *  KNIME and ECLIPSE being a combined program, KNIME AG herewith grants
 *  you the additional permission to use and propagate KNIME together with
 *  ECLIPSE with only the license terms in place for ECLIPSE applying to
 *  ECLIPSE and the GNU GPL Version 3 applying for KNIME, provided the
 *  license terms of ECLIPSE themselves allow for the respective use and
 *  propagation of ECLIPSE together with KNIME.
 *
 *  Additional permission relating to nodes for KNIME that extend the Node
 *  Extension (and in particular that are based on subclasses of NodeModel,
 *  NodeDialog, and NodeView) and that only interoperate with KNIME through
 *  standard APIs ("Nodes"):
 *  Nodes are deemed to be separate and independent programs and to not be
 *  covered works.  Notwithstanding anything to the contrary in the
 *  License, the License does not apply to Nodes, you are not required to
 *  license Nodes under the License, and you are granted a license to
 *  prepare and propagate Nodes, in each case even if such Nodes are
 *  propagated with or for interoperation with KNIME.  The owner of a Node
 *  may freely choose the license terms applicable to such Node, including
 *  when such Node is propagated with or for interoperation with KNIME.
 * ------------------------------------------------------------------------
 */
package org.knime.ensembles.voting;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.knime.base.node.preproc.joiner.Joiner;
import org.knime.base.node.preproc.joiner.Joiner2Settings;
import org.knime.base.node.preproc.joiner.Joiner2Settings.CompositionMode;
import org.knime.base.node.preproc.joiner.Joiner2Settings.DuplicateHandling;
import org.knime.base.node.preproc.joiner.Joiner2Settings.JoinMode;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.StringValue;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.data.container.SingleCellFactory;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.workflow.LoopEndNode;
import org.knime.core.node.workflow.LoopStartNode;
import org.knime.core.node.workflow.LoopStartNodeTerminator;

/**
 * This is the model implementation of VotingLoopEndNodeModel.
 * End node for the voting loop end.
 * 
 *  This node combines multiple predictions (e.g. predicted in each loop
 *  run) and detects the most frequent prediction.
 *
 * @author Thomas Gabriel, KNIME.com AG, Zurich
 */
public class VotingLoopEndNodeModel extends NodeModel implements LoopEndNode {

    private BufferedDataTable m_currentOutTable;

    private int m_iteration = 0;

    private final SettingsModelString m_winner
        = VotingLoopEndNodeDialog.createColumnModel();

    private final SettingsModelBoolean m_removedWinners
        = VotingLoopEndNodeDialog.createRemoveWinnersModel();

    // the selected columnIndex is needed for the autoguessing.
    private int m_columnIndex;

    /** Constructor for the node model. */
    protected VotingLoopEndNodeModel() {
        super(1, 1);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
            final ExecutionContext exec) throws Exception {
        boolean isToContinueLoop;
        LoopStartNode startNode = getLoopStartNode();
        if (startNode instanceof LoopStartNodeTerminator) {
            isToContinueLoop = !((LoopStartNodeTerminator)startNode)
                        .terminateLoop();
        } else {
             throw new IllegalStateException("Loop end is not connected"
                     + " to matching/corresponding loop start node. You"
                     + " are trying to create an infinite loop!");
        }

        // we use the autoguessed value contained in m_columnIndes
       String predictColumn = inData[0].getSpec()
                                       .getColumnSpec(m_columnIndex).getName();
        
        // first step, if there is no outtable we create one using the
        // selected prediction column only
        if (m_currentOutTable == null) {
            // copy the filtered data table 
            m_currentOutTable = filterAndCopy(inData[0], 
                    exec.createSubExecutionContext(2 / 3.0),
                    predictColumn);
        } else {
            Joiner2Settings settings = getJoinerSettings();
            BufferedDataTable left = m_currentOutTable;
            BufferedDataTable right = filterAndCopy(inData[0], 
                    exec.createSubExecutionContext(0.1),
                    predictColumn);
            Joiner joiner = new Joiner(left.getDataTableSpec(),
                    right.getDataTableSpec(), settings);
            m_currentOutTable = joiner.computeJoinTable(left, right,
                    exec.createSubExecutionContext(0.9));
        }   
        
        if (isToContinueLoop) {
            super.continueLoop();
            return new BufferedDataTable[]{null};
        } else {
            ColumnRearranger cr2 = new ColumnRearranger(
                    m_currentOutTable.getSpec());
            cr2.append(new SingleCellFactory(inData[0].getDataTableSpec()
                                        .getColumnSpec(predictColumn)) {
                /** {@inheritDoc} */
                @Override
                public DataCell getCell(final DataRow row) {
                    final Map<DataCell, AtomicInteger> map =
                        new LinkedHashMap<DataCell, AtomicInteger>();
                    for (int r = 0; r < row.getNumCells(); r++) {
                        final DataCell cell = row.getCell(r);
                        if (map.containsKey(cell)) {
                            map.get(cell).incrementAndGet();
                        } else {
                            map.put(cell, new AtomicInteger(1));
                        }
                    }
                    if (map.isEmpty()) {
                        return DataType.getMissingCell();
                    } else {
                        DataCell maxWinner = null;
                        int maxOccurrence = 0;
                        for (Map.Entry<DataCell, AtomicInteger> entry
                                : map.entrySet()) {
                            int occurrence = entry.getValue().get();
                            if (occurrence > maxOccurrence) {
                                maxOccurrence = occurrence;
                                maxWinner = entry.getKey();
                            }
                        }
                        assert maxWinner != null : "Map can't be empty";
                        return maxWinner;
                    }
                }
            });
            BufferedDataTable out = exec.createColumnRearrangeTable(
                    m_currentOutTable, cr2, exec);
            // remove individual winner columns
            if (m_removedWinners.getBooleanValue()) {
                ColumnRearranger cr3 = new ColumnRearranger(out.getSpec());
                cr3.keepOnly(predictColumn);
                out = exec.createColumnRearrangeTable(out, cr3, exec);
            }
            return new BufferedDataTable[]{out};
        }
    }
    
    private Joiner2Settings getJoinerSettings() {
         Joiner2Settings settings = new Joiner2Settings();
         settings.setCompositionMode(CompositionMode.MatchAll);
         settings.setDuplicateColumnSuffix(" (Iter #" + m_iteration + ")");
         settings.setDuplicateHandling(DuplicateHandling.AppendSuffix);
         settings.setEnableHiLite(false);
         // joining on RowIDs, this should not generate new row IDs but
         // only fill missing rows in either table
         settings.setJoinMode(JoinMode.FullOuterJoin);
         settings.setLeftIncludeAll(true);
         settings.setRightIncludeAll(true);
         // TODO to be replaced by Joiner2Settings.ROW_KEY_IDENTIFIER
         // once that is public
         settings.setLeftJoinColumns(new String[] {"$RowID$"});
         settings.setRightJoinColumns(new String[] {"$RowID$"});
         return settings;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void reset() {
        m_currentOutTable = null;
        m_iteration = 0;
    }
    
    // This methods takes the data table
    // removes all column without the one given as prediction Column
    // and copies the data into a new data table
    // in which the prediction column is renamed using the original
    // column name + a suffix containing the iteration number.
    private BufferedDataTable filterAndCopy(final BufferedDataTable table,
            final ExecutionContext exec, final String predictColumn) 
    throws CanceledExecutionException {
        
        
          ColumnRearranger cr = new ColumnRearranger(table.getDataTableSpec());
          cr.keepOnly(predictColumn);
          // data contains only the selected column
          BufferedDataTable data = exec.createColumnRearrangeTable(
                                      table, cr, exec);
          DataTableSpec spec = data.getDataTableSpec();
          
          String columnSuffix =  "#" + (m_iteration++);
          // change name of column
          DataColumnSpecCreator firstColCreator = new DataColumnSpecCreator(
                  spec.getColumnSpec(predictColumn));          
          firstColCreator.setName(predictColumn + columnSuffix);

          final DataTableSpec newSpec = new DataTableSpec(
                  firstColCreator.createSpec());
          BufferedDataContainer container
                  = exec.createDataContainer(newSpec);

          int i = 0;
          final int rowCount = table.getRowCount();
          for (DataRow r : data) {
              container.addRowToTable(r);
              exec.setProgress((i++) / (double)rowCount, 
                      "Process row " + i + "/"
                      + rowCount + " (\"" + r.getKey() + "\")");
              exec.checkCanceled();
          }
          exec.clearTable(data);
          container.close();
          return container.getTable();
    }
    
    private String autoguessing(final DataTableSpec inSpecs) {
        if (inSpecs.getNumColumns() == 0) {
            m_columnIndex = -1;
            return "Input table has no columns";
        }
        int i = 0;

        //warning user that we decided to select the column
        String warning = "";
        
        m_columnIndex = inSpecs.findColumnIndex(m_winner.getStringValue());
        
        while (i < inSpecs.getNumColumns() && m_columnIndex < 0 && warning.isEmpty()) {
            // we can only proceed, if our input table contains 
            // at least one column of type Double
             
             if (inSpecs.getColumnSpec(i).getType().isCompatible(StringValue.class)) {
                 //assign internal variable
                 m_columnIndex = i;
             }
             i++;   
        }
        if (warning.isEmpty() && m_columnIndex < 0) {
                // special case, we just use the first column
            m_columnIndex = 0;
        }
        
        if (m_columnIndex != inSpecs.findColumnIndex(m_winner.getStringValue())) {
                
            if (m_winner.getStringValue() == null || m_winner.getStringValue().length() <= 0) {
                  warning += "Autoguessing: ";
                  m_winner.setStringValue(inSpecs.getColumnSpec(m_columnIndex).getName());
            } else {
                  warning += "column '" + m_winner.getStringValue() + "' not found, instead ";
            }
                
            warning += " column '" + inSpecs.getColumnSpec(m_columnIndex).getName() + "' is used."; 
        }
        return warning;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected DataTableSpec[] configure(final DataTableSpec[] inSpecs)
            throws InvalidSettingsException {
        DataColumnSpec colSpec = inSpecs[0].getColumnSpec(m_winner.getStringValue());
        if (colSpec == null) {
            //column specified in settings is not valid. 
            String warning = autoguessing(inSpecs[0]);
            if (m_columnIndex < 0) {
                throw new InvalidSettingsException(warning);
            }
            if (warning != null && !warning.trim().isEmpty()) {
                setWarningMessage(warning);
            }
        } else {
            m_columnIndex = inSpecs[0].findColumnIndex(m_winner.getStringValue());
        }

        // if all individual winner columns are removed, the final table
        // structure contains only the single winner column
        if (m_removedWinners.getBooleanValue()) {
            DataColumnSpec cspec = inSpecs[0].getColumnSpec(m_columnIndex);
            return new DataTableSpec[] {new DataTableSpec(cspec)};
        }
        return new DataTableSpec[]{null};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        m_winner.saveSettingsTo(settings);
        m_removedWinners.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        m_winner.loadSettingsFrom(settings);
        try {
            m_removedWinners.loadSettingsFrom(settings);
        } catch (InvalidSettingsException ise) {
            // ignored: new with v2.4.1
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        m_winner.validateSettings(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadInternals(final File internDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
        // no op
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveInternals(final File internDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
        // no op
    }

}

