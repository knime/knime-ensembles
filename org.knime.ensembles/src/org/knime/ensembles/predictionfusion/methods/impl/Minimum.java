package org.knime.ensembles.predictionfusion.methods.impl;

import org.knime.ensembles.predictionfusion.methods.AbstractPredictionFusionMethod;

/**
 * Fusion method that picks the lowest confidence value.
 * 
 * @author Patrick Winter, University of Konstanz
 */
public class Minimum extends AbstractPredictionFusionMethod {

	/**
	 * Name of this fusion method.
	 */
	public static final String NAME = "Minimum";

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
		double min = Double.MAX_VALUE;
		for (double confidence : confidences) {
			if (min > confidence) {
				min = confidence;
			}
		}
		return min;
	}

}
