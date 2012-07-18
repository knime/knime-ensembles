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

       String predictColumn = m_winner.getStringValue();
        
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
                cr3.keepOnly(m_winner.getStringValue());
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
          container.close();
          return container.getTable();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected DataTableSpec[] configure(final DataTableSpec[] inSpecs)
            throws InvalidSettingsException {
        final String winner = m_winner.getStringValue();
        if (!inSpecs[0].containsName(winner)) {
            throw new InvalidSettingsException(
                    "Winner column not selected");
        }
        // if all individual winner columns are removed, the final table
        // structure contains only the single winner column
        if (m_removedWinners.getBooleanValue()) {
            DataColumnSpec cspec = inSpecs[0].getColumnSpec(winner);
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

