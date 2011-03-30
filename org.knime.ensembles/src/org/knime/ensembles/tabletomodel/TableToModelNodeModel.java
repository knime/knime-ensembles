package org.knime.ensembles.tabletomodel;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.RowKey;
import org.knime.core.data.model.PortObjectCell;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;

/**
 * This is the model implementation of a node model that wraps arbitrary
 * models into a table with one column
 *
 * @author Sebastian Peter, University of Konstanz, Germany
 */
public class TableToModelNodeModel extends NodeModel {

    private final static DataColumnSpec SPEC = new DataColumnSpecCreator(
            "PortObject", PortObjectCell.TYPE).createSpec();

    /** Default row key, if no key is entered. */
    static final RowKey DEFAULT_ROWKEY = RowKey.createRowKey(0);

    /** Constructor for the node model. */
    protected TableToModelNodeModel() {
        super(new PortType[]{BufferedDataTable.TYPE},
                new PortType[]{new PortType(PortObject.class)});
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected PortObject[] execute(final PortObject[] inObjects,
            final ExecutionContext exec) throws Exception {
        BufferedDataTable table = (BufferedDataTable) inObjects[0];
        Iterator<DataRow> it = table.iterator();
        PortObjectCell model = (PortObjectCell) it.next().getCell(0);
        return new PortObject[] {model.getPortObject()};
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
        return new PortObjectSpec[]{null};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        // no op
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        // no op
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        // no op
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
