package org.knime.ensembles.predictionfusion.methods;

/**
 * Method that fuses weighted predictions.
 * 
 * @author Patrick Winter, University of Konstanz
 */
public interface PredictionFusionMethod {

	/**
	 * Fuse the given predictions into one.
	 * 
	 * @param predictions
	 *            The predictions to fuse
	 * @param predictionWeights
	 *            The weight of each prediction. Having a weight of 2 for
	 *            instance means that the prediction with the same index is
	 *            sampled two times
	 * @return The fused prediction
	 */
	Prediction fusePredictions(Prediction[] predictions, int[] predictionWeights);

}
