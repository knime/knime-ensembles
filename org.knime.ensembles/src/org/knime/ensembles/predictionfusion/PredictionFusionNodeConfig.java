package org.knime.ensembles.predictionfusion;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.config.Config;

/**
 * Prediction fusion node configuration.
 *
 * @author Patrick Winter, University of Konstanz
 */
class PredictionFusionNodeConfig {

	static final String METHOD_CFG = "method";
	static final String CLASSES_CFG = "classes";
	static final String PREDICTIONS_CFG = "predictions";
	static final String NR_PREDICTIONS_CFG = "nrPredictions";
	static final String PREDICTION_CFG = "prediction";

	private static final String METHOD_DEFAULT = "";
	private static final String[] CLASSES_DEFAULT = new String[0];
	private static final PredictionConfig[] PREDICTIONS_DEFAULT = new PredictionConfig[0];

	private String m_method = METHOD_DEFAULT;
	private String[] m_classes = CLASSES_DEFAULT;
	private PredictionConfig[] m_predictions = PREDICTIONS_DEFAULT;

	/**
	 * @return The fusion method
	 */
	public String getMethod() {
		return m_method;
	}

	/**
	 * @param method
	 *            The fusion method to set
	 */
	public void setMethod(final String method) {
		m_method = method;
	}

	/**
	 * @return Prioritized array of the classes
	 */
	public String[] getClasses() {
		return m_classes;
	}

	/**
	 * @param classes
	 *            Prioritized array of classes to set.
	 */
	public void setClasses(final String[] classes) {
		m_classes = classes;
	}

	/**
	 * @return Array of predictions
	 */
	public PredictionConfig[] getPredictions() {
		return m_predictions;
	}

	/**
	 * @param predictions
	 *            Array of predictions to set
	 */
	public void setPredictions(final PredictionConfig[] predictions) {
		m_predictions = predictions;
	}

	/**
	 * Load the config.
	 *
	 * @param settings
	 *            Settings object
	 * @throws InvalidSettingsException
	 *             If the config could not correctly be loaded
	 */
	public void load(final NodeSettingsRO settings) throws InvalidSettingsException {
		m_method = settings.getString(METHOD_CFG);
		m_classes = settings.getStringArray(CLASSES_CFG);
		Config predictionsConfig = settings.getConfig(PREDICTIONS_CFG);
		int nrPredictions = predictionsConfig.getInt(NR_PREDICTIONS_CFG);
		m_predictions = new PredictionConfig[nrPredictions];
		for (int i = 0; i < nrPredictions; i++) {
			Config predictionConfig = predictionsConfig.getConfig(PREDICTION_CFG + i);
			m_predictions[i] = new PredictionConfig(predictionConfig);
		}
	}

	/**
	 * Load the config (with default values for settings that are not
	 * available).
	 *
	 * @param settings
	 *            Settings object
	 */
	public void loadWithDefaults(final NodeSettingsRO settings) {
		m_method = settings.getString(METHOD_CFG, METHOD_DEFAULT);
		m_classes = settings.getStringArray(CLASSES_CFG, CLASSES_DEFAULT);
		try {
			// load subconfig for predictions
			Config predictionsConfig = settings.getConfig(PREDICTIONS_CFG);
			int nrPredictions = predictionsConfig.getInt(NR_PREDICTIONS_CFG);
			m_predictions = new PredictionConfig[nrPredictions];
			for (int i = 0; i < nrPredictions; i++) {
				// load subsubconfig for each prediction
				Config predictionConfig = predictionsConfig.getConfig(PREDICTION_CFG + i);
				m_predictions[i] = new PredictionConfig(predictionConfig);
			}
		} catch (InvalidSettingsException e) {
			m_predictions = PREDICTIONS_DEFAULT;
		}
	}

	/**
	 * Save the config.
	 *
	 * @param settings
	 *            Settings object
	 */
	public void save(final NodeSettingsWO settings) {
		settings.addString(METHOD_CFG, m_method);
		settings.addStringArray(CLASSES_CFG, m_classes);
		// create subconfig for predictions
		Config predictionsConfig = settings.addConfig(PREDICTIONS_CFG);
		predictionsConfig.addInt(NR_PREDICTIONS_CFG, m_predictions.length);
		for (int i = 0; i < m_predictions.length; i++) {
			// create subsubconfig for each prediction
			Config predictionConfig = predictionsConfig.addConfig(PREDICTION_CFG + i);
			m_predictions[i].save(predictionConfig);
		}
	}

	/**
	 * Configuration for one prediction including the weight and the confidence
	 * columns.
	 *
	 * @author Patrick Winter, University of Konstanz
	 */
	public static class PredictionConfig {

		static final String WEIGHT_CFG = "weight";
		static final String COLUMNS_CFG = "columns";

		private int m_weight;
		private String[] m_columns;

		/**
		 * Create a PredictionConfig.
		 *
		 * @param weight
		 *            Weight for the entire prediction
		 * @param columns
		 *            Confidence columns (in the same order as the classes in
		 *            the PredictionFusionNodeConfig)
		 */
		public PredictionConfig(final int weight, final String[] columns) {
			m_weight = weight;
			m_columns = columns;
		}

		/**
		 * Load the PredictionConfig from the given config object.
		 *
		 * @param config
		 *            The config
		 * @throws InvalidSettingsException
		 *             If the PredictionConfig could not correctly be loaded
		 */
		public PredictionConfig(final Config config) throws InvalidSettingsException {
			m_weight = config.getInt(WEIGHT_CFG);
			m_columns = config.getStringArray(COLUMNS_CFG);
		}

		/**
		 * @return Weight for the entire prediction
		 */
		public int getWeight() {
			return m_weight;
		}

		/**
		 * @return Confidence columns (in the same order as the classes in the
		 * PredictionFusionNodeConfig)
		 */
		public String[] getColumns() {
			return m_columns;
		}

		/**
		 * Save this prediction config into the given config object.
		 *
		 * @param config
		 *            The config
		 */
		public void save(final Config config) {
			config.addInt(WEIGHT_CFG, m_weight);
			config.addStringArray(COLUMNS_CFG, m_columns);
		}

	}

}
