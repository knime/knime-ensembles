package org.knime.ensembles.voting;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

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
 * This is the model implementation of LoopEndColumnCollector.
 * End node for the column collector loop example
 *
 * @author Thomas Gabriel, KNIME.com GmbH, Zurich
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
            throw new IllegalStateException(
                    "Unexpected loop start node implementation");
        }

        // first: filter input data
        DataTableSpec inSpec = inData[0].getSpec();
        ColumnRearranger cr = new ColumnRearranger(inSpec);
        String winner = m_winner.getStringValue();
        cr.keepOnly(winner);
        BufferedDataTable data = exec.createColumnRearrangeTable(
                inData[0], cr, exec);
        DataTableSpec spec = data.getDataTableSpec();
        // one input column needs to be handled some other time
        DataColumnSpec winnerSpec = spec.getColumnSpec(winner);
        DataColumnSpecCreator firstColCreator = new DataColumnSpecCreator(
                winnerSpec);
        String columnName = winner + "#" + (m_iteration++);
        firstColCreator.setName(columnName);
        DataColumnSpec copyColSpecs = firstColCreator.createSpec();
        final DataTableSpec newSpec = new DataTableSpec(copyColSpecs);
        BufferedDataContainer cont
                = exec.createDataContainer(newSpec);
        ExecutionMonitor sub = exec.createSubProgress(2 / 3.0);
        int count = 0;
        exec.setMessage("Copying input data");
        for (DataRow r : data) {
            cont.addRowToTable(r);
            sub.checkCanceled();
            sub.setProgress(count / (double) data.getRowCount(),
                    "Row " + count);
            count++;
        }
        cont.close();
        BufferedDataTable inCopy = cont.getTable();
        if (m_currentOutTable == null) {
            m_currentOutTable = inCopy;
        } else {
            exec.setMessage("Generating output table");
            sub = exec.createSubProgress(1 / 3.0);
            m_currentOutTable = exec.createJoinedTable(
                    m_currentOutTable, inCopy, sub);
        }
        if (isToContinueLoop) {
            super.continueLoop();
            return new BufferedDataTable[]{null};
        } else {
            ColumnRearranger cr2 = new ColumnRearranger(
                    m_currentOutTable.getSpec());
            cr2.append(new SingleCellFactory(winnerSpec) {
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
                        int maxOccurrence = Integer.MIN_VALUE;
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

    /**
     * {@inheritDoc}
     */
    @Override
    protected void reset() {
        m_currentOutTable = null;
        m_iteration = 0;
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

