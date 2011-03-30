package org.knime.ensembles.pmmlporttocell;

import java.io.File;
import java.io.IOException;

import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.xml.XMLCell;
import org.knime.core.node.BufferedDataContainer;
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
import org.knime.core.node.port.pmml.PMMLPortObject;

public class PMMLPortToCellNodeModel extends NodeModel {

	protected PMMLPortToCellNodeModel() {
		super(	new PortType[] {new PortType(PMMLPortObject.class)},
				new PortType[] {new PortType(BufferedDataTable.class)});
	}
	
	@Override
	protected PortObject[] execute(PortObject[] inObjects, ExecutionContext exec)
			throws Exception {
		PMMLPortObject in = (PMMLPortObject) inObjects[0];
//		in.
		
//		System.out.println(in.getDocument());
		  DataColumnSpecCreator colSpecCreator =
              new DataColumnSpecCreator("PMML/XML", XMLCell.TYPE);
		  DataTableSpec spec = new DataTableSpec(colSpecCreator.createSpec());
		  
		BufferedDataContainer out = exec.createDataContainer(createSpec());
		out.close();
		return new PortObject[]{out.getTable()};
	}
	
	private DataTableSpec createSpec(){
		DataColumnSpecCreator colSpecCreator =
            new DataColumnSpecCreator("PMML/XML", XMLCell.TYPE);
		  DataTableSpec spec = new DataTableSpec(colSpecCreator.createSpec());
		  return spec;
	}
	
	@Override
	protected PortObjectSpec[] configure(PortObjectSpec[] inSpecs)
			throws InvalidSettingsException {
		return new PortObjectSpec[]{createSpec()};
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
		// TODO Auto-generated method stub

	}

	@Override
	protected void validateSettings(NodeSettingsRO settings)
			throws InvalidSettingsException {
		// TODO Auto-generated method stub

	}

	@Override
	protected void loadValidatedSettingsFrom(NodeSettingsRO settings)
			throws InvalidSettingsException {
		// TODO Auto-generated method stub

	}

	@Override
	protected void reset() {
		// TODO Auto-generated method stub

	}

}
