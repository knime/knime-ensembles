package org.knime.ensembles.tabletomodel;

import java.io.File;
import java.io.IOException;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.model.PortObjectCell;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;

/**
 * This is the model implementation of a node model that wraps arbitrary
 * models into a table with one column.
 *
 * @author Sebastian Peter, University of Konstanz, Germany
 */
public class TableToModelNodeModel extends NodeModel {

    private final SettingsModelString m_column =
        TableToModelNodeDialog.createColumnModel();

    /** Constructor for the node model. */
    protected TableToModelNodeModel() {
        super(new PortType[]{BufferedDataTable.TYPE},
                new PortType[]{new PortType(PortObject.class)});
    }

    /** {@inheritDoc} */
    @Override
    protected PortObject[] execute(final PortObject[] inObjects,
            final ExecutionContext exec) throws Exception {
        BufferedDataTable table = (BufferedDataTable) inObjects[0];
        if (table.getRowCount() == 0) {
            throw new Exception("Empty input table; can't provide any model.");
        }
        int index = table.getSpec().findColumnIndex(m_column.getStringValue());
        int rowCount = 0;
        for (DataRow row : table) {
            exec.checkCanceled();
            final DataCell dc = row.getCell(index);
            if (!dc.isMissing()) {
                if (rowCount > 0) {
                    setWarningMessage("Found missing cell(s); skipping them and"
                            + " use row \"" + row.getKey() + "\".");
                } else {
                    if (table.getRowCount() > 1) {
                        setWarningMessage("Table has more than one row; "
                                + "taking first row \"" + row.getKey() + "\".");
                    }
                }
                final PortObjectCell model = (PortObjectCell) dc;
                return new PortObject[] {model.getPortObject()};
            }
            rowCount++;
        }
        throw new Exception("Found only missing cells in input table, "
                + "column \"" + m_column.getStringValue() + "\".");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void reset() {
        // no op
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected PortObjectSpec[] configure(final PortObjectSpec[] inSpecs)
            throws InvalidSettingsException {
        DataTableSpec spec = (DataTableSpec) inSpecs[0];
        if (!spec.containsName(m_column.getStringValue())) {
            throw new InvalidSettingsException("Selected column '"
                    + m_column.getStringValue() + "' not in input spec.");
        }
        return new PortObjectSpec[]{null};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        m_column.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        m_column.loadSettingsFrom(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        m_column.validateSettings(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadInternals(final File internDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
        // no internals to load
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveInternals(final File internDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
        // no internals to save
    }

}
