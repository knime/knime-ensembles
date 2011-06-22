package org.knime.ensembles.pmmlpredict;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.knime.base.node.mine.cluster.assign.ClusterAssignerNodeModel;
import org.knime.base.node.mine.decisiontree2.predictor.DecTreePredictorNodeModel;
import org.knime.base.node.mine.neural.mlp.MLPPredictorNodeModel;
import org.knime.base.node.mine.regression.logistic.predict.GeneralRegressionPredictorNodeModel;
import org.knime.base.node.mine.regression.predict.RegressionPredictorNodeModel;
import org.knime.base.node.mine.svm.predictor.SVMPredictorNodeModel;
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
import org.knime.core.pmml.PMMLModelType;
import org.w3c.dom.Node;

/**
*
* @author Iris Adae, University of Konstanz, Germany
*/
public class PMMLPredictorNodeModel extends NodeModel {



	   private static final int PMML_PORT = 0;
//	   private static final int DATA_PORT = 1;

    /**
     * Creates a new model with a PMML input and a data output.
     */
	protected PMMLPredictorNodeModel() {
		super(new PortType[] {
                PMMLPortObject.TYPE,
                BufferedDataTable.TYPE},
                new PortType[] {BufferedDataTable.TYPE});
	}

	/** {@inheritDoc} */
	@Override
	protected PortObject[] execute(final PortObject[] inObjects,
			final ExecutionContext exec)
			throws Exception {
		    PMMLPortObject port = (PMMLPortObject) inObjects[PMML_PORT];

		   Set<PMMLModelType> types =  port.getPMMLValue().getModelTypes();
		   if(types.size() < 1){
			   String msg = "No PMML Model found.";
	            throw new RuntimeException(msg);
		   }

		   PMMLModelType type = types.iterator().next();

		   if(types.size() > 1){
			   setWarningMessage("More models are found, the first one is used " +
			   		" : " + type.toString());
		   }

	        List<Node> models = port.getPMMLValue().getModels(type);
	        if(models.isEmpty()){
	        	  String msg = "No PMML Model found.";
		            throw new RuntimeException(msg);
	        }

	        switch (type) {
			case ClusteringModel :
			{
				ClusterAssignerNodeModel model = new ClusterAssignerNodeModel();
	        	return model.execute(inObjects, exec);
			}
			case GeneralRegressionModel:
			{
				GeneralRegressionPredictorNodeModel model
				= new GeneralRegressionPredictorNodeModel();
					return model.execute(inObjects, exec);
			}
			case RegressionModel:
			{
	        	RegressionPredictorNodeModel model
	        				= new RegressionPredictorNodeModel();
	        	return model.execute(inObjects, exec);
	        }
			case TreeModel :
			{
	        	DecTreePredictorNodeModel model
	        				= new DecTreePredictorNodeModel();
	        	return model.execute(inObjects, exec);
	        }
			case SupportVectorMachineModel :
			{
				SVMPredictorNodeModel model
	        				= new SVMPredictorNodeModel();
	        	return model.execute(inObjects, exec);
	        }
			case NeuralNetwork :
			{
				MLPPredictorNodeModel model
							= new MLPPredictorNodeModel();
	        	return model.execute(inObjects, exec);
			}

			default:
				// this should never happen.
				String msg = "No suitable predictor found for these model types.";
	            throw new RuntimeException(msg);
			}
	}



	/** {@inheritDoc} */
	@Override
	protected PortObjectSpec[] configure(final PortObjectSpec[] inSpecs)
			throws InvalidSettingsException {
		return new PortObjectSpec[]{null};
	}

	/** {@inheritDoc} */
	@Override
	protected void loadInternals(final File nodeInternDir, final ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
		// nothing to load

	}

	/** {@inheritDoc} */
	@Override
	protected void saveInternals(final File nodeInternDir, final ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
	    // no op
	}

	/** {@inheritDoc} */
	@Override
	protected void saveSettingsTo(final NodeSettingsWO settings) {
	}

	/** {@inheritDoc} */
	@Override
	protected void validateSettings(final NodeSettingsRO settings)
			throws InvalidSettingsException {
	}

	/** {@inheritDoc} */
	@Override
	protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
			throws InvalidSettingsException {
	}

	/** {@inheritDoc} */
	@Override
	protected void reset() {
	    // no op
	}

}
