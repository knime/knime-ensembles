package org.knime.ensembles.pmmlporttocell;

import java.io.File;
import java.io.IOException;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.xml.PMMLCell;
import org.knime.core.data.xml.PMMLCellFactory;
import org.knime.core.data.xml.PMMLValue;
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
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.pmml.PMMLPortObject;

/**
*
* @author Iris Adae, University of Konstanz, Germany
*/
public class PMMLPortToCellNodeModel extends NodeModel {

    private final SettingsModelString m_rowKeyModel =
        PMMLPortToCellNodeDialog.createStringModel();

    /**
     * Creates a new model with a PMML input and a data output.
     */
    protected PMMLPortToCellNodeModel() {
        super(new PortType[] {new PortType(PMMLPortObject.class)},
              new PortType[] {new PortType(BufferedDataTable.class)});
    }

    /** {@inheritDoc} */
    @Override
    protected PortObject[] execute(final PortObject[] inObjects,
            final ExecutionContext exec)
            throws Exception {
        PMMLPortObject in = (PMMLPortObject) inObjects[0];
        PMMLValue value = in.getPMMLValue();

        DataCell cell = PMMLCellFactory.create(value.getDocument());

        BufferedDataContainer out
                        = exec.createDataContainer(createSpec());
        out.addRowToTable(new DefaultRow(
                            m_rowKeyModel.getStringValue(), cell));
        out.close();
        return new PortObject[]{out.getTable()};
    }

    private DataTableSpec createSpec() {
        DataColumnSpecCreator colSpecCreator = new DataColumnSpecCreator("PMML", PMMLCell.TYPE);
        return new DataTableSpec(colSpecCreator.createSpec());
    }

    /** {@inheritDoc} */
    @Override
    protected PortObjectSpec[] configure(final PortObjectSpec[] inSpecs)
            throws InvalidSettingsException {
        return new PortObjectSpec[]{createSpec()};
    }

    /** {@inheritDoc} */
    @Override
    protected void loadInternals(final File nodeInternDir,
            final ExecutionMonitor exec)
            throws IOException, CanceledExecutionException {
        // nothing to load

    }

    /** {@inheritDoc} */
    @Override
    protected void saveInternals(final File nodeInternDir,
            final ExecutionMonitor exec)
            throws IOException, CanceledExecutionException {
        // no op
    }

    /** {@inheritDoc} */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        m_rowKeyModel.saveSettingsTo(settings);
    }

    /** {@inheritDoc} */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        m_rowKeyModel.validateSettings(settings);
    }

    /** {@inheritDoc} */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        m_rowKeyModel.loadSettingsFrom(settings);
    }

    /** {@inheritDoc} */
    @Override
    protected void reset() {
        // no op
    }

}
