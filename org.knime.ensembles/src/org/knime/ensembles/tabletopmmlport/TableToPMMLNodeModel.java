
package org.knime.ensembles.tabletopmmlport;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import org.knime.base.node.io.pmml.read.PMMLImport;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.xml.PMMLValue;
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
import org.w3c.dom.Document;
/**
 *
 * @author Iris Adae, University of Konstanz, Germany
 *
 */
public class TableToPMMLNodeModel extends NodeModel {

	 private final SettingsModelString m_column =
	        TableToPMMLNodeDialog.createColumnModel();

	/**
	 * 
	 */
	protected TableToPMMLNodeModel() {
		super(new PortType[] {new PortType(BufferedDataTable.class)},
				new PortType[] {new PortType(PMMLPortObject.class)});
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected PortObject[] execute(final PortObject[] inObjects, 
	        final ExecutionContext exec) throws Exception {
		 BufferedDataTable table = (BufferedDataTable) inObjects[0];
	     Iterator<DataRow> it = table.iterator();
	     int index = table.getSpec().findColumnIndex(m_column.getStringValue());

	     PMMLValue model = (PMMLValue) it.next().getCell(index);
	     Document doc = model.getDocument();

	     PMMLImport pmmlImport = new PMMLImport(doc);
	     return new PortObject[] {pmmlImport.getPortObject()};
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
	protected void loadInternals(final File nodeInternDir, 
	        final ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
		// nothing to load

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveInternals(final File nodeInternDir, 
	        final ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
		// TODO Auto-generated method stub

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
	protected void validateSettings(final NodeSettingsRO settings)
			throws InvalidSettingsException {
		m_column.validateSettings(settings);

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
	protected void reset() {
		// TODO Auto-generated method stub

	}

}
