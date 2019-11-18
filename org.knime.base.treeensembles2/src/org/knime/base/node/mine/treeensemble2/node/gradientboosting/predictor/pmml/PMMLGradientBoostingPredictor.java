/*
 * ------------------------------------------------------------------------
 *
 *  Copyright by KNIME AG, Zurich, Switzerland
 *  Website: http://www.knime.com; Email: contact@knime.com
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License, Version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>.
 *
 *  Additional permission under GNU GPL version 3 section 7:
 *
 *  KNIME interoperates with ECLIPSE solely via ECLIPSE's plug-in APIs.
 *  Hence, KNIME and ECLIPSE are both independent programs and are not
 *  derived from each other. Should, however, the interpretation of the
 *  GNU GPL Version 3 ("License") under any applicable laws result in
 *  KNIME and ECLIPSE being a combined program, KNIME AG herewith grants
 *  you the additional permission to use and propagate KNIME together with
 *  ECLIPSE with only the license terms in place for ECLIPSE applying to
 *  ECLIPSE and the GNU GPL Version 3 applying for KNIME, provided the
 *  license terms of ECLIPSE themselves allow for the respective use and
 *  propagation of ECLIPSE together with KNIME.
 *
 *  Additional permission relating to nodes for KNIME that extend the Node
 *  Extension (and in particular that are based on subclasses of NodeModel,
 *  NodeDialog, and NodeView) and that only interoperate with KNIME through
 *  standard APIs ("Nodes"):
 *  Nodes are deemed to be separate and independent programs and to not be
 *  covered works.  Notwithstanding anything to the contrary in the
 *  License, the License does not apply to Nodes, you are not required to
 *  license Nodes under the License, and you are granted a license to
 *  prepare and propagate Nodes, in each case even if such Nodes are
 *  propagated with or for interoperation with KNIME.  The owner of a Node
 *  may freely choose the license terms applicable to such Node, including
 *  when such Node is propagated with or for interoperation with KNIME.
 * ---------------------------------------------------------------------
 *
 * History
 *   15 Nov 2019 (Alexander): created
 */
package org.knime.base.node.mine.treeensemble2.node.gradientboosting.predictor.pmml;

import java.util.List;
import java.util.Optional;

import org.knime.base.node.mine.treeensemble2.model.AbstractGradientBoostingModel;
import org.knime.base.node.mine.treeensemble2.model.GradientBoostedTreesModel;
import org.knime.base.node.mine.treeensemble2.model.GradientBoostingModelPortObject;
import org.knime.base.node.mine.treeensemble2.model.MultiClassGradientBoostedTreesModel;
import org.knime.base.node.mine.treeensemble2.model.TreeEnsembleModelPortObjectSpec;
import org.knime.base.node.mine.treeensemble2.model.pmml.AbstractGBTModelPMMLTranslator;
import org.knime.base.node.mine.treeensemble2.model.pmml.AbstractTreeModelPMMLTranslator;
import org.knime.base.node.mine.treeensemble2.model.pmml.ClassificationGBTModelPMMLTranslator;
import org.knime.base.node.mine.treeensemble2.model.pmml.RegressionGBTModelPMMLTranslator;
import org.knime.base.node.mine.treeensemble2.node.gradientboosting.predictor.GBTRegressionPredictor;
import org.knime.base.node.mine.treeensemble2.node.gradientboosting.predictor.LKGradientBoostedTreesPredictor;
import org.knime.base.node.mine.treeensemble2.node.predictor.PredictionRearrangerCreator;
import org.knime.base.node.mine.treeensemble2.node.predictor.TreeEnsemblePredictionUtil;
import org.knime.base.node.mine.treeensemble2.node.predictor.TreeEnsemblePredictorConfiguration;
import org.knime.base.predict.PMMLClassificationPredictorOptions;
import org.knime.base.predict.PMMLTablePredictor;
import org.knime.base.predict.PredictorContext;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.DoubleValue;
import org.knime.core.data.StringValue;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.port.pmml.PMMLPortObject;
import org.knime.core.node.port.pmml.PMMLPortObjectSpec;
import org.knime.core.node.util.CheckUtils;

/**
 * A predictor for gradient boosting models.
 * @author Alexander Fillbrunn, KNIME GmbH, Konstanz, Germany
 * @param <M> the type of model handled by this predictor
 */
public class PMMLGradientBoostingPredictor<M extends AbstractGradientBoostingModel> implements PMMLTablePredictor {

    private PMMLGradientBoostingPredictorOptions m_options;
    private Version m_version;
    private boolean m_isRegression;

    /**
     * Creates a new instance of {@code PMMLGradientBoostingPredictor}.
     * @param version the KNIME Analytics Platform version the predictor is compatible with
     * @param isRegression whether the predictor is used for regression or classification
     * @param options the predictor options determining its output
     */
    public PMMLGradientBoostingPredictor(final Version version, final boolean isRegression,
        final PMMLGradientBoostingPredictorOptions options) {
        m_options = options;
        m_version = version;
        m_isRegression = isRegression;
    }

    /**
     * Versions in which changes were made to this class.
     *
     * @author Adrian Nembach, KNIME GmbH, Konstanz, Germany
     */
    public enum Version {
        /**
         * Nodes created with KNIME Analytics Platform prior to version 3.6.0
         */
        PRE360,
        /**
         * Nodes created with KNIME Analytics Platform of version 3.6.0 to 4.0.0
         */
        V360,
        /**
         * Nodes created with KNIME Analytics Platform of version 4.0.1 and later
         */
        V401;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BufferedDataTable predict(final BufferedDataTable input, final PMMLPortObject model, final PredictorContext ctx)
        throws Exception {
        GradientBoostingModelPortObject gbModel = importModel(model, ctx);
        DataTableSpec dataSpec = input.getDataTableSpec();
        TreeEnsemblePredictorConfiguration config = m_options.createTreeEnsembleConfiguration(m_isRegression);
        String targetColName = model.getSpec().getTargetCols().get(0).getName();
        if (!config.isChangePredictionColumnName()) {
            config.setPredictionColumnName(TreeEnsemblePredictorConfiguration.getPredictColumnName(targetColName));
        } else {
            config.setPredictionColumnName(m_options.getPredictionColumnName());
        }
        ColumnRearranger rearranger = createExecutionRearranger(dataSpec, gbModel.getSpec(), (M)gbModel.getEnsembleModel(), config);
        BufferedDataTable outTable = ctx.getExecutionContext().createColumnRearrangeTable(input, rearranger, ctx.getExecutionContext());
        return outTable;
    }

    private static TreeEnsembleModelPortObjectSpec translateSpec(final PMMLPortObjectSpec pmmlSpec) {
        DataTableSpec pmmlDataSpec = pmmlSpec.getDataTableSpec();
        ColumnRearranger cr = new ColumnRearranger(pmmlDataSpec);
        List<DataColumnSpec> targets = pmmlSpec.getTargetCols();
        CheckUtils.checkArgument(!targets.isEmpty(), "The provided PMML does not declare a target field.");
        CheckUtils.checkArgument(targets.size() == 1, "The provided PMML declares multiple target. "
            + "This behavior is currently not supported.");
        cr.move(targets.get(0).getName(), pmmlDataSpec.getNumColumns());
        return new TreeEnsembleModelPortObjectSpec(cr.createSpec());
    }

    private static DataType extractTargetType(final PMMLPortObjectSpec pmmlSpec) {
        return pmmlSpec.getTargetCols().get(0).getType();
    }

    @SuppressWarnings("unchecked")
    private GradientBoostingModelPortObject importModel(final PMMLPortObject pmmlPO, final PredictorContext ctx) {
        AbstractGBTModelPMMLTranslator<M> pmmlTranslator;
        DataType targetType = extractTargetType(pmmlPO.getSpec());
        if (targetType.isCompatible(DoubleValue.class)) {
            pmmlTranslator = (AbstractGBTModelPMMLTranslator<M>)new RegressionGBTModelPMMLTranslator();
        } else if (targetType.isCompatible(StringValue.class)) {
            pmmlTranslator = (AbstractGBTModelPMMLTranslator<M>)new ClassificationGBTModelPMMLTranslator();
        } else {
            throw new IllegalArgumentException("Currently only regression models are supported.");
        }
        pmmlPO.initializeModelTranslator(pmmlTranslator);
        if (pmmlTranslator.hasWarning()) {
            ctx.setWarningMessage(pmmlTranslator.getWarning());
        }
        return new GradientBoostingModelPortObject(new TreeEnsembleModelPortObjectSpec(pmmlTranslator.getLearnSpec()),
            pmmlTranslator.getGBTModel());
    }

    private PredictionRearrangerCreator createRearrangerCreator(final DataTableSpec predictSpec,
        final TreeEnsembleModelPortObjectSpec modelSpec, final M model,
        final TreeEnsemblePredictorConfiguration cfg) throws InvalidSettingsException {
        PredictionRearrangerCreator prc;
        if (m_isRegression) {
            prc = new PredictionRearrangerCreator(predictSpec,
                new GBTRegressionPredictor((GradientBoostedTreesModel)model,
                TreeEnsemblePredictionUtil.createRowConverter(modelSpec, model, predictSpec)));
            prc.addRegressionPrediction(cfg.getPredictionColumnName());
        } else {
            MultiClassGradientBoostedTreesModel gbt = (MultiClassGradientBoostedTreesModel)model;
            prc = new PredictionRearrangerCreator(predictSpec,
                new LKGradientBoostedTreesPredictor(gbt,
                    cfg.isAppendClassConfidences() || cfg.isAppendPredictionConfidence(),
                TreeEnsemblePredictionUtil.createRowConverter(modelSpec, model, predictSpec),
                m_version == Version.V401));
            TreeEnsemblePredictionUtil.setupRearrangerCreatorGBT(
                m_version == Version.PRE360, prc, modelSpec, gbt, cfg);
        }
        return prc;
    }

    private ColumnRearranger createExecutionRearranger(final DataTableSpec predictSpec,
        final TreeEnsembleModelPortObjectSpec modelSpec, final M model, final TreeEnsemblePredictorConfiguration cfg)
                throws InvalidSettingsException {
        PredictionRearrangerCreator prc = createRearrangerCreator(predictSpec, modelSpec, model, cfg);
        return prc.createExecutionRearranger();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DataTableSpec getOutputSpec(final DataTableSpec inputSpec, final PMMLPortObjectSpec pmmlSpec, final PredictorContext ctx)
        throws InvalidSettingsException {
        TreeEnsemblePredictorConfiguration config = m_options.createTreeEnsembleConfiguration(m_isRegression);
        DataType targetType = extractTargetType(pmmlSpec);
        if (m_isRegression && !targetType.isCompatible(DoubleValue.class)) {
            throw new InvalidSettingsException("This node expects a regression model.");
        } else if (!m_isRegression && !targetType.isCompatible(StringValue.class)) {
            throw new InvalidSettingsException("This node expectes a classification model.");
        }
        try {
            AbstractTreeModelPMMLTranslator.checkPMMLSpec(pmmlSpec);
        } catch (IllegalArgumentException e) {
            throw new InvalidSettingsException(e.getMessage());
        }
        TreeEnsembleModelPortObjectSpec modelSpec = translateSpec(pmmlSpec);
        String targetColName = pmmlSpec.getTargetCols().get(0).getName();
        if (!config.isChangePredictionColumnName()) {
            config.setPredictionColumnName(TreeEnsemblePredictorConfiguration.getPredictColumnName(targetColName));
        } else {
            config.setPredictionColumnName(m_options.getPredictionColumnName());
        }
        modelSpec.assertTargetTypeMatches(m_isRegression);
        PredictionRearrangerCreator prc = createRearrangerCreator(inputSpec, modelSpec, null, config);
        Optional<DataTableSpec> spec = prc.createSpec();
        return spec.isPresent() ? spec.get() : null;
    }

    /**
     * Options for the {@link PMMLGradientBoostingPredictor}.
     *
     * @author Alexander Fillbrunn, KNIME GmbH, Konstanz, Germany
     */
    public static class PMMLGradientBoostingPredictorOptions extends PMMLClassificationPredictorOptions {
        private boolean m_appendModelCount;
        private boolean m_useSoftVoting;
        private boolean m_appendPredConfidence;

        /**
         * Creates a new instance of {@code PMMLGradientBoostingPredictorOptions}.
         * @param appendModelCount whether the output table should contain the number of models in the ensemble.
         * @param useSoftVoting whether the predictor should use soft voting
         * @param appendPredConfidence whether the output table should contain the prediction confidence
         * @param customPredictionName the name of the column containing the prediction or null if it should be inferred from the PMML.
         * @param includeProbabilities whether the output table should include class probabilities if available
         * @param propColumnSuffix the suffix for class probability columns
         */
        public PMMLGradientBoostingPredictorOptions(final boolean appendModelCount,
            final boolean useSoftVoting, final boolean appendPredConfidence,
            final String customPredictionName, final boolean includeProbabilities,
            final String propColumnSuffix) {
            super(customPredictionName, includeProbabilities, propColumnSuffix);
            m_appendModelCount = appendModelCount;
            m_useSoftVoting = useSoftVoting;
            m_appendPredConfidence = appendPredConfidence;
        }

        /**
         * Creates a new instance of {@code PMMLGradientBoostingPredictorOptions} where the prediction column
         * name is inferred from the PMML and no class probabilities are output.
         * @param appendModelCount whether the output table should contain the number of models in the ensemble.
         * @param useSoftVoting whether the predictor should use soft voting
         * @param appendPredConfidence whether the output table should contain the prediction confidence
         */
        public PMMLGradientBoostingPredictorOptions(final boolean appendModelCount,
            final boolean useSoftVoting, final boolean appendPredConfidence) {
            this(appendModelCount, useSoftVoting, appendPredConfidence, null, false, "");
        }

        /**
         * Creates a new instance of {@code PMMLGradientBoostingPredictorOptions} with no appended model count,
         * no soft voting and no appended prediction confidence, where the prediction column
         * name is inferred from the PMML and no class probabilities are output.
         */
        public PMMLGradientBoostingPredictorOptions() {
            this(false, false, false, null, false, "");
        }

        /**
         * Creates a new instance of {@code PMMLGradientBoostingPredictorOptions} with no appended model count,
         * no soft voting and no appended prediction confidence, where the prediction column is inferred from the PMML.
         * @param includeProbabilities whether the output table should include class probabilities if available
         * @param propColumnSuffix the suffix for class probability columns
         */
        public PMMLGradientBoostingPredictorOptions(final boolean includeProbabilities, final String propColumnSuffix) {
            this(false, false, false, null, includeProbabilities, propColumnSuffix);
        }

        /**
         * Creates a new instance of {@code PMMLGradientBoostingPredictorOptions} with no appended model count,
         * no soft voting and no appended prediction confidence.
         * @param customPredictionName the name of the column containing the prediction
         * @param includeProbabilities whether the output table should include class probabilities if available
         * @param propColumnSuffix the suffix for class probability columns
         */
        public PMMLGradientBoostingPredictorOptions(final String customPredictionName, final boolean includeProbabilities,
            final String propColumnSuffix) {
            this(false, false, false, customPredictionName, includeProbabilities, propColumnSuffix);
        }

        /**
         * @return whether the predictor should use soft voting
         */
        public boolean useSoftVoting() {
            return m_useSoftVoting;
        }

        /**
         * @return whether the output table should contain the prediction confidence
         */
        public boolean appendPredConfidence() {
            return m_appendPredConfidence;
        }

        /**
         * @return whether the output table should contain the number of models in the ensemble.
         */
        public boolean appendModelCount() {
            return m_appendModelCount;
        }

        /**
         * Creates a tree ensemble configuration for the prediction algorithm.
         * @param isRegression whether the algorithm is a regression algorithm. If set to false,
         * the algorithm will perform a classification.
         * @return a {@link TreeEnsemblePredictorConfiguration} initialized from the current option instance.
         */
        public TreeEnsemblePredictorConfiguration createTreeEnsembleConfiguration(final boolean isRegression) {
            TreeEnsemblePredictorConfiguration config = new TreeEnsemblePredictorConfiguration(isRegression, getPredictionColumnName());
            config.setAppendClassConfidences(includeClassProbabilities());
            config.setAppendModelCount(m_appendModelCount);
            config.setAppendPredictionConfidence(m_appendPredConfidence);
            config.setChangePredictionColumnName(hasCustomPredictionColumnName());
            config.setSuffixForClassConfidences(getPropColumnSuffix());
            config.setUseSoftVoting(m_useSoftVoting);
            return config;
        }
    }
}
