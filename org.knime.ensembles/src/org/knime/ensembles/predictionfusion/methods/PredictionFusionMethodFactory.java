package org.knime.ensembles.predictionfusion.methods;

import org.knime.ensembles.predictionfusion.methods.impl.Maximum;
import org.knime.ensembles.predictionfusion.methods.impl.Mean;
import org.knime.ensembles.predictionfusion.methods.impl.Median;
import org.knime.ensembles.predictionfusion.methods.impl.Minimum;

/**
 * Factory that creates the PredictionFusionMethod object for the given name.
 * 
 * @author Patrick Winter, University of Konstanz
 */
public class PredictionFusionMethodFactory {

	/**
	 * @return The available fusion methods
	 */
	public static String[] getAvailablePredictionFusionMethods() {
		return new String[] { Maximum.NAME, Mean.NAME, Median.NAME, Minimum.NAME };
	}

	/**
	 * Creates the fusion method object to the given name.
	 * 
	 * @param methodName
	 *            Name of the selected fusion method
	 * @return The fusion method object
	 */
	public static PredictionFusionMethod getPredictionFusionMethod(final String methodName) {
		if (Maximum.NAME.equals(methodName)) {
			return new Maximum();
		}
		if (Mean.NAME.equals(methodName)) {
			return new Mean();
		}
		if (Median.NAME.equals(methodName)) {
			return new Median();
		}
		if (Minimum.NAME.equals(methodName)) {
			return new Minimum();
		}
		return null;
	}

}
