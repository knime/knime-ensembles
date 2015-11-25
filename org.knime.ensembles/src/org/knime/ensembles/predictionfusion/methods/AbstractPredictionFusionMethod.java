package org.knime.ensembles.predictionfusion.methods;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract implementation of a prediction fusion method that handles the
 * prediction objects and breaks the problem down to combining double values.
 * 
 * @author Patrick Winter, University of Konstanz
 */
public abstract class AbstractPredictionFusionMethod implements PredictionFusionMethod {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Prediction fusePredictions(final Prediction[] predictions, final int[] predictionWeights) {
		Prediction fusedPrediction = new Prediction();
		if (predictions.length > 0) {
			String[] classes = predictions[0].getClasses();
			// iterate over classes
			for (String cls : classes) {
				List<Double> confidences = new ArrayList<Double>();
				// iterate over predictions for this class
				for (int i = 0; i < predictions.length; i++) {
					Double confidence = predictions[i].getClassConfidence(cls, true);
					if (confidence != null) {
						// oversample confidence
						int weight = influencedByWeight() ? predictionWeights[i] : 1;
						for (int j = 0; j < weight; j++) {
							confidences.add(confidence);
						}
					}
				}
				if (!confidences.isEmpty()) {
					// fuse confidences and put result into fused prediction
					fusedPrediction.setClassConfidence(cls,
							fuseConfidences(confidences.toArray(new Double[confidences.size()])));
				}
			}
		}
		return fusedPrediction;
	}

	/**
	 * States if this fusion method is influenced by weighting the prediction.
	 * If not than the confidences don't need to be oversampled.
	 * 
	 * @return true if this fusion method is influenced by weighting the
	 *         prediction, false otherwise
	 */
	protected abstract boolean influencedByWeight();

	/**
	 * Fuses the confidences for a single class into one confidence.
	 * 
	 * @param confidences
	 *            The confidences for a single class to fuse
	 * @return The fused confidence
	 */
	protected abstract double fuseConfidences(final Double[] confidences);

}
