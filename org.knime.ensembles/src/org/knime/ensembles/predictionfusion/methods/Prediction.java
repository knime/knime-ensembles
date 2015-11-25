package org.knime.ensembles.predictionfusion.methods;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Represents a prediction with (known) confidences and the resulting class.
 *
 * @author Patrick Winter, University of Konstanz
 */
public class Prediction {

	private Map<String, Double> m_confidences = new HashMap<String, Double>();
	private Double m_normalizationFactor = null;
	private Boolean m_sumIsZero = null;

	/**
	 * Sets the confidence for the given class to the given value. If confidence
	 * is null the existing confidence is removed.
	 *
	 * @param cls
	 *            The class
	 * @param confidence
	 *            The confidence value
	 * @return true if a confidence value for the given class was already
	 *         present, false otherwise
	 */
	public boolean setClassConfidence(final String cls, final Double confidence) {
		boolean wasPresent = m_confidences.containsKey(cls);
		if (confidence == null) {
			// confidence == null means we want to remove the existing
			// confidence
			m_confidences.remove(cls);
		} else {
			m_confidences.put(cls, confidence);
		}
		// normalization factor has potentially changed and has to be
		// recalculated on next use
		m_normalizationFactor = null;
		return wasPresent;
	}

	/**
	 * Returns the normalized confidence for the given class.
	 *
	 * @param cls
	 *            The class
	 * @param normalized if this is false the original value is returned
	 * @return The confidence or null if no confidence is present
	 */
	public Double getClassConfidence(final String cls, final boolean normalized) {
		Double confidence = m_confidences.get(cls);
		if (normalized && confidence != null) {
			// we have to normalize the confidence by calculating and using the
			// factor
			calculateNormalizationFactor();
			if (m_sumIsZero) {
				// if all confidences are zero we would end up with a sum of 0,
				// but we always want a sum of 1 so we divide it equally
				confidence = 1 / (double) m_confidences.size();
			} else {
				confidence *= m_normalizationFactor;
			}
		}
		return confidence;
	}

	/**
	 * Returns all classes that have a confidence value.
	 *
	 * @return The classes
	 */
	public String[] getClasses() {
		Set<String> classes = m_confidences.keySet();
		return classes.toArray(new String[classes.size()]);
	}

	/**
	 * Returns the winning class based on the set confidences. In case of a tie
	 * the first tying class in the priority array is selected.
	 *
	 * @param classPriority
	 *            Array determening the priority of classes in case of a tie
	 *            (with the first having the highest priority)
	 * @return The predicted class
	 */
	public String getPredictedClass(String[] classPriority) {
		if (classPriority == null) {
			// no priority preference we just use the classes in the order we
			// find them in the hash map
			classPriority = m_confidences.keySet().toArray(new String[m_confidences.size()]);
		}
		String cls = null;
		double maxConfidence = -1;
		for (String cls2 : classPriority) {
			Double confidence = m_confidences.get(cls2);
			if (confidence != null && maxConfidence < confidence) {
				// we only overwrite if confidence is truly bigger, this way the
				// first occurrence has priority
				maxConfidence = confidence;
				cls = cls2;
			}
		}
		return cls;
	}

	/**
	 * Calculates the normalization factor (if not already buffered). The
	 * normalization factor is used to calculate the normalized confidence so
	 * that the sum of all confidences is 1.
	 */
	private void calculateNormalizationFactor() {
		if (m_normalizationFactor == null) {
			// we only need to calculate if the confidences have changed
			// otherwise the cached value is still up to date
			double sum = 0;
			for (Entry<String, Double> confidence : m_confidences.entrySet()) {
				sum += confidence.getValue();
			}
			// if all confidences are 0 we have a special case
			m_sumIsZero = sum == 0;
			m_normalizationFactor = m_sumIsZero ? 1 : 1 / sum;
		}
	}

}
