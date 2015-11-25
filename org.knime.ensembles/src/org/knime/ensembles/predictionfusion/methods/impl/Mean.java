package org.knime.ensembles.predictionfusion.methods.impl;

import org.knime.ensembles.predictionfusion.methods.AbstractPredictionFusionMethod;

/**
 * Fusion method that calculates the mean of the confidence values.
 * 
 * @author Patrick Winter, University of Konstanz
 */
public class Mean extends AbstractPredictionFusionMethod {

	/**
	 * Name of this fusion method.
	 */
	public static final String NAME = "Mean";

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected boolean influencedByWeight() {
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected double fuseConfidences(Double[] confidences) {
		double sum = 0;
		for (double confidence : confidences) {
			sum += confidence;
		}
		return sum / confidences.length;
	}

}
