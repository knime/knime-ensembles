package org.knime.ensembles.delegating;

import java.io.File;
import java.io.IOException;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.RowKey;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.workflow.LoopEndNode;

/**
 * This is the model implementation of Delegating.
 *
 *
 * @author Iris Adae, University of Konstanz, Germany
 */
public class DelegatingLoopEndNodeModel extends NodeModel
            implements LoopEndNode {

    private BufferedDataContainer m_outcontainer;

    /**
     * this variable contains a mapping from columns that are same
     * (Same datatype and columnname)
     * if columns, of the input data table are not found in the input of the
     * loop, it tries to find a compatible column (
     * Matching at least, the data type).
     */

    private BufferedDataTable m_inData;
    private int m_iterationnr = 0;
    
    private SettingsModelIntegerBounded m_maxIterations
            = DelegatingLoopEndNodeDialog.createIterationsModel();
    private SettingsModelIntegerBounded m_minNumberOfRows
            = DelegatingLoopEndNodeDialog.createNumOfRowsModel();
    

    /**
     * Constructor for the node model.
     */
    protected DelegatingLoopEndNodeModel() {
        super(2, 1);
    }

    private static int collectingIn = 0;
    private static int resultingIn = 1;

    /**
     * {@inheritDoc}
     */
    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
            final ExecutionContext exec) throws Exception {
        // outport 0 wird gesammelt und hinten ausgegeben
        // outport 1 wird an loop start zur�ck gegeben

        if (m_outcontainer == null) {
            m_outcontainer = exec.createDataContainer(
                    inData[collectingIn].getDataTableSpec());
        }

        BufferedDataContainer loopData = exec.createDataContainer(
                inData[resultingIn].getDataTableSpec());


        for (DataRow row : inData[resultingIn]) {
            loopData.addRowToTable(createNewRow(row, row.getKey()));
        }
        loopData.close();
        m_inData  = loopData.getTable();

        for (DataRow row : inData[collectingIn]) {
            RowKey newKey = new RowKey(row.getKey() + "#" + m_iterationnr);
            m_outcontainer.addRowToTable(createNewRow(row, newKey));
        }

        m_iterationnr++;
        // stop loop if there are less rows than needed.
        // or the max number of iterations is reached
        if (m_inData.getRowCount() < m_minNumberOfRows.getIntValue()
                   || m_iterationnr >= m_maxIterations.getIntValue()) {
            m_outcontainer.close();
            return new BufferedDataTable[]{m_outcontainer.getTable()};
        }
        // else go on with loop
        super.continueLoop();
        return new BufferedDataTable[]{null};
    }

    private DataRow createNewRow(final DataRow row, final RowKey newKey) {
        DataCell[] cells = new DataCell[row.getNumCells()];
        for (int i = 0; i < row.getNumCells(); i++) {
            cells[i] = row.getCell(i);
        }
        return new DefaultRow(newKey, cells);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void reset() {
        m_iterationnr = 0;
        m_outcontainer = null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected DataTableSpec[] configure(final DataTableSpec[] inSpecs)
            throws InvalidSettingsException {
        return new DataTableSpec[]{inSpecs[0]};
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        m_maxIterations.saveSettingsTo(settings);
        m_minNumberOfRows.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        m_maxIterations.loadSettingsFrom(settings);
        m_minNumberOfRows.loadSettingsFrom(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        m_maxIterations.validateSettings(settings);
        m_minNumberOfRows.validateSettings(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadInternals(final File internDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
        // nothing to load
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveInternals(final File internDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
        // nothing to save
    }

    /**Call to get the in data table of the last iteration.
     *
     * @return the indata table of the last iteration.
     */
    public BufferedDataTable getInData() {
        return m_inData;
    }

}
