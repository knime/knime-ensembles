package org.knime.ensembles.predictionfusion.methods.impl;

import java.util.Arrays;

import org.knime.ensembles.predictionfusion.methods.AbstractPredictionFusionMethod;

/**
 * Fusion method that calculates the median of the confidence values.
 * 
 * @author Patrick Winter, University of Konstanz
 */
public class Median extends AbstractPredictionFusionMethod {

	/**
	 * Name of this fusion method.
	 */
	public static final String NAME = "Median";

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
		Arrays.sort(confidences);
		if (confidences.length % 2 == 1) {
			// for an odd number of values return middle value
			int middle = (confidences.length - 1) / 2;
			return confidences[middle];
		} else {
			// for an equal number of values return mean of the two middle values
			int middle = confidences.length / 2;
			return (confidences[middle] + confidences[middle - 1]) / 2;
		}
	}

}
