package org.knime.ensembles.predictionfusion.methods.impl;

import org.knime.ensembles.predictionfusion.methods.AbstractPredictionFusionMethod;

/**
 * Fusion method that picks the highest confidence value.
 * 
 * @author Patrick Winter, University of Konstanz
 */
public class Maximum extends AbstractPredictionFusionMethod {

	/**
	 * Name of this fusion method.
	 */
	public static final String NAME = "Maximum";

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected boolean influencedByWeight() {
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected double fuseConfidences(Double[] confidences) {
		double max = Double.MIN_VALUE;
		for (double confidence : confidences) {
			if (max < confidence) {
				max = confidence;
			}
		}
		return max;
	}

}
