
package org.knime.ensembles.tabletopmmlport;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

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
import org.knime.core.node.port.pmml.PMMLPortObjectSpec;
import org.w3c.dom.Document;
/**
 * 
 * @author Iris Adae, University of Konstanz, Germany
 *
 */
public class TableToPMMLNodeModel extends NodeModel {
	
	 private final SettingsModelString m_column =
	        TableToPMMLNodeDialog.createColumnModel();

	protected TableToPMMLNodeModel() {
		super(new PortType[] {new PortType(BufferedDataTable.class)},
				new PortType[] {new PortType(PMMLPortObject.class)});
	}
	
	@Override
	protected PortObject[] execute(PortObject[] inObjects, ExecutionContext exec)
			throws Exception {
		 BufferedDataTable table = (BufferedDataTable) inObjects[0];
	     Iterator<DataRow> it = table.iterator();
	     int index = table.getSpec().findColumnIndex(m_column.getStringValue());
	     
	     PMMLValue model = (PMMLValue) it.next().getCell(index);
	     Document doc = model.getDocument();

	     PMMLPortObjectSpec spec = PMMLPortObject.parseSpec(doc);
	     
	     PMMLPortObject obj = new PMMLPortObject(spec, doc);

	     return new PortObject[] {obj};
	}
	
	
	
	@Override
	protected PortObjectSpec[] configure(PortObjectSpec[] inSpecs)
			throws InvalidSettingsException {
		 DataTableSpec spec = (DataTableSpec) inSpecs[0];
	        if (!spec.containsName(m_column.getStringValue())) {
	            throw new InvalidSettingsException("Selected column '"
	                    + m_column.getStringValue() + "' not in input spec.");
	        }
	        return new PortObjectSpec[]{null};
	}

	@Override
	protected void loadInternals(File nodeInternDir, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
		// nothing to load

	}

	@Override
	protected void saveInternals(File nodeInternDir, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
		// TODO Auto-generated method stub

	}

	@Override
	protected void saveSettingsTo(NodeSettingsWO settings) {
		m_column.saveSettingsTo(settings);

	}

	@Override
	protected void validateSettings(NodeSettingsRO settings)
			throws InvalidSettingsException {
		m_column.validateSettings(settings);

	}

	@Override
	protected void loadValidatedSettingsFrom(NodeSettingsRO settings)
			throws InvalidSettingsException {
		m_column.loadSettingsFrom(settings);

	}

	@Override
	protected void reset() {
		// TODO Auto-generated method stub

	}

}
