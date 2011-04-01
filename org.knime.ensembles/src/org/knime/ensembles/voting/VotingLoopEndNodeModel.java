package org.knime.ensembles.voting;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;
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

    /**
     * Constructor for the node model.
     */
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
            isToContinueLoop = !((LoopStartNodeTerminator)startNode).terminateLoop();
        } else {
            throw new IllegalStateException("Unexpected loop start node implementation");
        }
    	// first: filter input data
        ColumnRearranger cr = new ColumnRearranger(inData[0].getSpec());
        String winner = m_winner.getStringValue();
        cr.keepOnly(winner);
        BufferedDataTable data = exec.createColumnRearrangeTable(
                inData[0], cr, exec);
    	DataTableSpec spec = data.getDataTableSpec();
    	// one input column needs to be handled some other time
    	DataColumnSpec winnerSpec = spec.getColumnSpec(0);
    	DataColumnSpecCreator firstColCreator = new DataColumnSpecCreator(
    	        winnerSpec);
    	String columnName = winner + "#" + (m_iteration++);
    	firstColCreator.setName(columnName);
    	DataColumnSpec copyColSpecs = firstColCreator.createSpec();
    	BufferedDataContainer cont = exec.createDataContainer(new DataTableSpec(copyColSpecs));
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
                    final HashMap<DataCell, AtomicInteger> map =
                        new HashMap<DataCell, AtomicInteger>();
                    for (DataCell cell : row) {
                        if (map.containsKey(cell)) {
                            map.get(cell).incrementAndGet();
                        } else {
                            map.put(cell, new AtomicInteger(1));
                        }
                    }
                    if (map.isEmpty()) {
                        return DataType.getMissingCell();
                    } else {
                        TreeSet<Map.Entry<DataCell, AtomicInteger>> set
                            = new TreeSet<Map.Entry<DataCell, AtomicInteger>>(
                                  new Comparator<Map.Entry<DataCell, AtomicInteger>>() {
                                  /** {@inheritDoc} */
                                  @Override
                                  public int compare(final Map.Entry<DataCell, AtomicInteger> o1, final Map.Entry<DataCell, AtomicInteger> o2) {
                                      return o2.getValue().get() - o1.getValue().get();
                                  }
                      });
                      set.addAll(map.entrySet());
                      return set.first().getKey();
                    }
                }
            });
    	    BufferedDataTable out = exec.createColumnRearrangeTable(
    	            m_currentOutTable, cr2, exec);
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
        if (!inSpecs[0].containsName(m_winner.getStringValue())) {
            throw new InvalidSettingsException(
            		"Winner column not selected");
        }
        return new DataTableSpec[]{null};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        m_winner.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        m_winner.loadSettingsFrom(settings);
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

