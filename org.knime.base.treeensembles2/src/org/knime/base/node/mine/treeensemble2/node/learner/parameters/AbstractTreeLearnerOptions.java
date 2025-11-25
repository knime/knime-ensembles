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
 * ------------------------------------------------------------------------
 */
package org.knime.base.node.mine.treeensemble2.node.learner.parameters;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import org.knime.base.node.mine.treeensemble2.node.learner.TreeEnsembleLearnerConfiguration;
import org.knime.base.node.mine.treeensemble2.node.learner.TreeEnsembleLearnerConfiguration.ColumnSamplingMode;
import org.knime.base.node.mine.treeensemble2.node.learner.TreeEnsembleLearnerConfiguration.MissingValueHandling;
import org.knime.base.node.mine.treeensemble2.node.learner.TreeEnsembleLearnerConfiguration.SplitCriterion;
import org.knime.base.node.mine.treeensemble2.sample.row.RowSamplerFactory.RowSamplingMode;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DoubleValue;
import org.knime.core.data.NominalValue;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.webui.node.dialog.defaultdialog.widget.Modification;
import org.knime.core.webui.node.dialog.defaultdialog.widget.validation.custom.CustomValidation;
import org.knime.core.webui.node.dialog.defaultdialog.widget.validation.custom.CustomValidationProvider;
import org.knime.core.webui.node.dialog.defaultdialog.widget.validation.custom.ValidationCallback;
import org.knime.node.parameters.Advanced;
import org.knime.node.parameters.NodeParameters;
import org.knime.node.parameters.NodeParametersInput;
import org.knime.node.parameters.Widget;
import org.knime.node.parameters.layout.After;
import org.knime.node.parameters.layout.Layout;
import org.knime.node.parameters.layout.Section;
import org.knime.node.parameters.migration.LoadDefaultsForAbsentFields;
import org.knime.node.parameters.persistence.NodeParametersPersistor;
import org.knime.node.parameters.persistence.Persist;
import org.knime.node.parameters.persistence.Persistor;
import org.knime.node.parameters.persistence.legacy.LegacyColumnFilterPersistor;
import org.knime.node.parameters.updates.Effect;
import org.knime.node.parameters.updates.Effect.EffectType;
import org.knime.node.parameters.updates.EffectPredicate;
import org.knime.node.parameters.updates.EffectPredicateProvider;
import org.knime.node.parameters.updates.ParameterReference;
import org.knime.node.parameters.updates.ValueReference;
import org.knime.node.parameters.widget.OptionalWidget;
import org.knime.node.parameters.widget.choices.ChoicesProvider;
import org.knime.node.parameters.widget.choices.ColumnChoicesProvider;
import org.knime.node.parameters.widget.choices.EnumChoice;
import org.knime.node.parameters.widget.choices.EnumChoicesProvider;
import org.knime.node.parameters.widget.choices.Label;
import org.knime.node.parameters.widget.choices.RadioButtonsWidget;
import org.knime.node.parameters.widget.choices.filter.ColumnFilter;
import org.knime.node.parameters.widget.choices.filter.ColumnFilterWidget;
import org.knime.node.parameters.widget.choices.util.CompatibleColumnsProvider;
import org.knime.node.parameters.widget.number.NumberInputWidget;
import org.knime.node.parameters.widget.number.NumberInputWidgetValidation.MaxValidation;
import org.knime.node.parameters.widget.number.NumberInputWidgetValidation.MinValidation.IsPositiveDoubleValidation;
import org.knime.node.parameters.widget.number.NumberInputWidgetValidation.MinValidation.IsPositiveIntegerValidation;

/**
 * Shared parameter definition for all tree-based learners.
 * <p>
 * This abstract class contains all possible fields that may be used by any tree learner variant. Subclasses use the
 * {@link Modification} framework to selectively configure which widgets are shown in their dialogs.
 */
@LoadDefaultsForAbsentFields
@SuppressWarnings({"java:S104"})
public abstract class AbstractTreeLearnerOptions implements NodeParameters {

    AbstractTreeLearnerOptions() {
    }

    AbstractTreeLearnerOptions(final NodeParametersInput input) {
        if (input == null) {
            return;
        }
        input.getInTableSpec(0).ifPresent(tableSpec -> {
            for (var idx = tableSpec.getNumColumns() - 1; idx >= 0; idx--) {
                var col = tableSpec.getColumnSpec(idx);
                if (isValidTargetColumn(col)) {
                    m_targetColumn = col.getName();
                    break;
                }
            }
        });
    }

    abstract boolean isValidTargetColumn(final DataColumnSpec col);

    @Section(title = "Attribute Selection")
    interface AttributeSelectionSection {
    }

    @Section(title = "Tree Options")
    @After(AttributeSelectionSection.class)
    interface TreeOptionsSection {
    }

    @Section(title = "Data Sampling")
    @After(TreeOptionsSection.class)
    private interface DataSamplingSection {
    }

    @Section(title = "Ensemble Configuration")
    @After(DataSamplingSection.class)
    private interface EnsembleConfigurationSection {
    }

    @Section(title = "Advanced")
    @After(EnsembleConfigurationSection.class)
    @Advanced
    private interface AdvancedSection {
    }

    private static final class SeedDefaultProvider implements OptionalWidget.DefaultValueProvider<Long> {
        @Override
        public Long computeState(final NodeParametersInput context) {
            return System.currentTimeMillis();
        }
    }

    private static final class HiliteCountDefaultProvider implements OptionalWidget.DefaultValueProvider<Integer> {
        @Override
        public Integer computeState(final NodeParametersInput context) {
            return 2000;
        }
    }

    private static final class MaxTreeDepthDefaultProvider implements OptionalWidget.DefaultValueProvider<Integer> {
        @Override
        public Integer computeState(final NodeParametersInput context) {
            return 3;
        }
    }

    private static final class RowSamplingFractionDefaultProvider
        implements OptionalWidget.DefaultValueProvider<Double> {
        @Override
        public Double computeState(final NodeParametersInput context) {
            return 0.7;
        }
    }

    private static final class ColumnFilterPersistor extends LegacyColumnFilterPersistor {
        ColumnFilterPersistor() {
            super(TreeEnsembleLearnerConfiguration.KEY_COLUMN_FILTER_CONFIG);
        }
    }

    private static final class AttributeColumnsProvider extends CompatibleColumnsProvider {

        private Supplier<String> m_targetSupplier;

        AttributeColumnsProvider() {
            super(List.of(NominalValue.class, DoubleValue.class));
        }

        @Override
        public void init(final StateProviderInitializer initializer) {
            m_targetSupplier = initializer.computeFromValueSupplier(TargetColumnValueRef.class);
            super.init(initializer);
        }

        @Override
        public boolean isIncluded(final DataColumnSpec col) {
            if (!super.isIncluded(col)) {
                return false;
            }
            var target = m_targetSupplier == null ? null : m_targetSupplier.get();
            return target == null || !target.equals(col.getName());
        }
    }

    private static final class RootColumnProvider extends CompatibleColumnsProvider {
        RootColumnProvider() {
            super(List.of(NominalValue.class, DoubleValue.class));
        }
    }

    protected interface TargetColumnWidgetRef extends Modification.Reference {
    }

    interface TargetColumnValueRef extends ParameterReference<String> {
    }

    static abstract class TargetColumnChoicesProvider implements ColumnChoicesProvider {

        abstract boolean isValidTargetColumn(final DataColumnSpec col);

        @Override
        public List<DataColumnSpec> columnChoices(final NodeParametersInput context) {
            return context.getInTableSpec(0).stream().flatMap(DataTableSpec::stream).filter(this::isValidTargetColumn)
                .toList();
        }

    }

    private static final class TargetColumnPersistor implements NodeParametersPersistor<String> {
        @Override
        public String load(final NodeSettingsRO settings) throws InvalidSettingsException {
            return settings.getString(TreeEnsembleLearnerConfiguration.KEY_TARGET_COLUMN, null);
        }

        @Override
        public void save(final String value, final NodeSettingsWO settings) {
            settings.addString(TreeEnsembleLearnerConfiguration.KEY_TARGET_COLUMN, value);
        }

        @Override
        public String[][] getConfigPaths() {
            return new String[][]{{TreeEnsembleLearnerConfiguration.KEY_TARGET_COLUMN}};
        }
    }

    @Layout(AttributeSelectionSection.class)
    @Modification.WidgetReference(TargetColumnWidgetRef.class)
    @ValueReference(TargetColumnValueRef.class)
    @Persistor(TargetColumnPersistor.class)
    String m_targetColumn;

    private static final class ColumnAttributesSelectedPredicate implements EffectPredicateProvider {
        @Override
        public EffectPredicate init(final PredicateInitializer initializer) {
            return initializer.getEnum(TrainingAttributesParameters.TrainingAttributesModeRef.class)
                .isOneOf(TrainingAttributesParameters.TrainingAttributesModeOption.COLUMNS);
        }
    }

    @Persistor(TrainingAttributesParameters.TrainingAttributesParametersPersistor.class)
    TrainingAttributesParameters m_trainingAttributes = new TrainingAttributesParameters();

    protected interface AttributeColumnsWidgetRef extends Modification.Reference {
    }

    @Layout(AttributeSelectionSection.class)
    @ColumnFilterWidget(choicesProvider = AttributeColumnsProvider.class)
    @Effect(predicate = ColumnAttributesSelectedPredicate.class, type = EffectType.SHOW)
    @Persistor(ColumnFilterPersistor.class)
    @Modification.WidgetReference(AttributeColumnsWidgetRef.class)
    ColumnFilter m_attributeColumns = new ColumnFilter().withExcludeUnknownColumns();

    protected interface IgnoreColumnsWithoutDomainWidgetRef extends Modification.Reference {
    }

    @Layout(AttributeSelectionSection.class)
    @Effect(predicate = ColumnAttributesSelectedPredicate.class, type = EffectType.SHOW)
    @Persist(configKey = TreeEnsembleLearnerConfiguration.KEY_IGNORE_COLUMNS_WITHOUT_DOMAIN)
    @Modification.WidgetReference(IgnoreColumnsWithoutDomainWidgetRef.class)
    boolean m_ignoreColumnsWithoutDomain;

    private static final class HiliteCountPersistor implements NodeParametersPersistor<Optional<Integer>> {
        @Override
        public Optional<Integer> load(final NodeSettingsRO settings) throws InvalidSettingsException {
            var value = settings.getInt(TreeEnsembleLearnerConfiguration.KEY_NR_HILITE_PATTERNS, -1);
            return value > 0 ? Optional.of(value) : Optional.empty();
        }

        @Override
        public void save(final Optional<Integer> value, final NodeSettingsWO settings) {
            settings.addInt(TreeEnsembleLearnerConfiguration.KEY_NR_HILITE_PATTERNS, value.orElse(-1));
        }

        @Override
        public String[][] getConfigPaths() {
            return new String[][]{{TreeEnsembleLearnerConfiguration.KEY_NR_HILITE_PATTERNS}};
        }
    }

    protected interface HiliteCountRef extends Modification.Reference {
    }

    @Layout(AdvancedSection.class)
    @Modification.WidgetReference(HiliteCountRef.class)
    @OptionalWidget(defaultProvider = HiliteCountDefaultProvider.class)
    @NumberInputWidget(minValidation = IsPositiveIntegerValidation.class)
    @Persistor(HiliteCountPersistor.class)
    Optional<Integer> m_hiliteCount = Optional.empty();

    enum MissingValueHandlingOption {
            @Label(value = "Use surrogate splits", description = """
                    Learn surrogate splits to route rows with missing values. Comparable to the strategy used by \
                    single decision-tree learners.
                    """)
            SURROGATE(MissingValueHandling.Surrogate),
            @Label(value = "Learn direction during training (XGBoost)", description = """
                    Evaluate both branches during training and store the best direction for missing values. \
                    Matches the XGBoost-style behaviour used by recent tree ensemble nodes.
                    """)
            XGBOOST(MissingValueHandling.XGBoost);

        MissingValueHandlingOption(final MissingValueHandling delegate) {
            m_delegate = delegate;
        }

        MissingValueHandling toLegacy() {
            return m_delegate;
        }

        static MissingValueHandlingOption fromLegacy(final MissingValueHandling legacy) {
            return legacy == MissingValueHandling.Surrogate ? SURROGATE : XGBOOST;
        }

        private final MissingValueHandling m_delegate;
    }

    private static final class MissingValueHandlingPersistor
        implements NodeParametersPersistor<MissingValueHandlingOption> {
        @Override
        public MissingValueHandlingOption load(final NodeSettingsRO settings) throws InvalidSettingsException {
            var legacy = MissingValueHandling.valueOf(settings.getString(
                TreeEnsembleLearnerConfiguration.KEY_MISSING_VALUE_HANDLING, MissingValueHandling.XGBoost.name()));
            return MissingValueHandlingOption.fromLegacy(legacy);
        }

        @Override
        public void save(final MissingValueHandlingOption value, final NodeSettingsWO settings) {
            settings.addString(TreeEnsembleLearnerConfiguration.KEY_MISSING_VALUE_HANDLING, value.toLegacy().name());
        }

        @Override
        public String[][] getConfigPaths() {
            return new String[][]{{TreeEnsembleLearnerConfiguration.KEY_MISSING_VALUE_HANDLING}};
        }
    }

    private static final class MissingValueHandlingChoices implements EnumChoicesProvider<MissingValueHandlingOption> {
        @Override
        public List<EnumChoice<MissingValueHandlingOption>> computeState(final NodeParametersInput context) {
            return List.of(EnumChoice.fromEnumConst(MissingValueHandlingOption.XGBOOST),
                EnumChoice.fromEnumConst(MissingValueHandlingOption.SURROGATE));
        }
    }

    protected interface MissingValueHandlingWidgetRef extends Modification.Reference {
    }

    @Layout(TreeOptionsSection.class)
    @ChoicesProvider(MissingValueHandlingChoices.class)
    @Persistor(MissingValueHandlingPersistor.class)
    @Modification.WidgetReference(MissingValueHandlingWidgetRef.class)
    MissingValueHandlingOption m_missingValueHandling = MissingValueHandlingOption.XGBOOST;

    protected interface SaveTargetDistributionRef extends Modification.Reference {
    }

    @Layout(AdvancedSection.class)
    @Modification.WidgetReference(SaveTargetDistributionRef.class)
    @Persist(configKey = TreeEnsembleLearnerConfiguration.KEY_SAVE_TARGET_DISTRIBUTION_IN_NODES)
    boolean m_saveTargetDistributionInNodes;

    public static void targetColumn(final Modification.WidgetGroupModifier groupModifier) {
        groupModifier.find(TargetColumnWidgetRef.class) //
            .addAnnotation(Widget.class) //
            .withProperty("title", "Target column") //
            .withProperty("description", """
                    Select the column containing the value to be learned. Rows with missing values in this column are \
                    ignored during the learning process.
                    """).modify();//

    }

    protected static void trainingAttributes(final Modification.WidgetGroupModifier groupModifier) {
        groupModifier.find(TrainingAttributesParameters.TrainingAttributesModeWidgetRef.class) //
            .addAnnotation(Widget.class) //
            .withProperty("title", "Training attributes") //
            .withProperty("description", """
                    Choose whether to derive attributes from a fingerprint vector column or from ordinary table \
                    columns.
                    """) //
            .modify();
    }

    protected static void useFingerprintAttribute(final Modification.WidgetGroupModifier groupModifier) {
        groupModifier.find(TrainingAttributesParameters.FingerprintAttributeWidgetRef.class) //
            .addAnnotation(Widget.class) //
            .withProperty("title", "Fingerprint attribute") //
            .withProperty("description", """
                    Use a fingerprint (bit, byte, or double vector) column to learn the model. Each entry of the \
                    vector is treated as a separate attribute. All vectors must share the same length.
                    """) //
            .modify();
    }

    protected static void attributeColumns(final Modification.WidgetGroupModifier groupModifier) {
        groupModifier.find(AttributeColumnsWidgetRef.class) //
            .addAnnotation(Widget.class) //
            .withProperty("title", "Attribute selection") //
            .withProperty("description", """
                    Select the ordinary columns that should be used as learning attributes. Use the include/exclude \
                    lists or pattern matching to manage the selection.
                    """) //
            .modify();
    }

    protected static void ignoreColumnsWithoutDomainInfo(final Modification.WidgetGroupModifier groupModifier) {
        groupModifier.find(IgnoreColumnsWithoutDomainWidgetRef.class) //
            .addAnnotation(Widget.class) //
            .withProperty("title", "Ignore columns without domain information") //
            .withProperty("description", """
                    Ignore nominal columns that do not contain domain information, for example when the number of \
                    nominal values is very large.
                    """) //
            .modify();
    }

    protected static void useMissingValueHandlingWidget(final Modification.WidgetGroupModifier groupModifier) {
        // currently unused but kept for consistency since all nodes share the same configuration object
        groupModifier.find(MissingValueHandlingWidgetRef.class) //
            .addAnnotation(Widget.class) //
            .withProperty("title", "Missing value handling") //
            .withProperty("description",
                """
                        Choose how missing attribute values are processed. XGBoost-style handling learns the best direction \
                        for missing values during training; surrogates fall back to alternative splits.
                        """) //
            .modify();
    }

    protected static void useMidpointSplits(final Modification.WidgetGroupModifier groupModifier) {
        groupModifier.find(AverageSplitPointsWidgetRef.class) //
            .addAnnotation(Widget.class) //
            .withProperty("title", "Use mid-point splits") //
            .withProperty("description", """
                    For numerical splits, use the mid-point between two class boundaries. Otherwise the split value \
                    corresponds to the lower boundary with a ≤ comparison.
                    """) //
            .modify();
    }

    protected static void useBinarySplitsForNominal(final Modification.WidgetGroupModifier groupModifier) {
        groupModifier.find(BinaryNominalSplitsWidgetRef.class) //
            .addAnnotation(Widget.class) //
            .withProperty("title", "Use binary splits for nominal columns") //
            .withProperty("description", """
                    Allow binary splits for nominal attributes. Disabling keeps the original multi-way splits.
                    """) //
            .modify();
    }

    protected static void limitNumberOfLevels(final Modification.WidgetGroupModifier groupModifier) {
        groupModifier.find(MaxTreeDepthWidgetRef.class) //
            .addAnnotation(Widget.class) //
            .withProperty("title", "Limit number of levels (tree depth)") //
            .withProperty("description", """
                    Limit the maximal number of tree levels. When disabled the tree depth is unbounded.
                    """) //
            .modify();
    }

    protected static void minSplitNodeSize(final Modification.WidgetGroupModifier groupModifier) {
        groupModifier.find(MinNodeSizesParameters.MinNodeSizeWidgetRef.class) //
            .addAnnotation(Widget.class) //
            .withProperty("title", "Minimum split node size") //
            .withProperty("description", """
                    Minimum number of records in a node required to attempt another split.
                    """) //
            .modify();
    }

    protected static void fixedRootAttribute(final Modification.WidgetGroupModifier groupModifier) {
        groupModifier.find(FixedRootAttributeWidgetRef.class) //
            .addAnnotation(Widget.class) //
            .withProperty("title", "Use fixed root attribute") //
            .withProperty("description", """
                    Force the selected column to be used as the root split attribute in all trees.
                    """) //
            .modify();
    }

    protected static void rowSamplingFraction(final Modification.WidgetGroupModifier groupModifier) {
        groupModifier.find(RowSamplingFractionWidgetRef.class) //
            .addAnnotation(Widget.class) //
            .withProperty("title", "Sample training data (rows)") //
            .withProperty("description", """
                    Sample the training rows for each tree. Disable to use the full dataset for every model. Sampling \
                    with replacement corresponds to bootstrap sampling.
                    """) //
            .modify();
    }

    protected static void rowSamplingWithReplacement(final Modification.WidgetGroupModifier groupModifier) {
        groupModifier.find(RowSamplingReplacementWidgetRef.class) //
            .addAnnotation(Widget.class) //
            .withProperty("title", "Sample with replacement") //
            .withProperty("description", """
                    Draw sampled rows with replacement (bootstrap sampling). When disabled, rows are sampled without \
                    replacement.
                    """) //
            .modify();
    }

    protected static void rowSamplingMode(final Modification.WidgetGroupModifier groupModifier) {
        groupModifier.find(RowSamplingModeWidgetRef.class) //
            .addAnnotation(Widget.class) //
            .withProperty("title", "Data sampling mode") //
            .withProperty("description", """
                    Choose how rows are sampled when data sampling is enabled.
                    """) //
            .modify();
    }

    protected static void attributeSampling(final Modification.WidgetGroupModifier groupModifier) {
        groupModifier.find(ColumnSamplingModeWidgetRef.class) //
            .addAnnotation(Widget.class) //
            .withProperty("title", "Attribute sampling (columns)") //
            .withProperty("description", """
                    Control how many attributes are available when learning individual trees.
                    """) //
            .modify();
    }

    protected static void attributeSamplingLinearFraction(final Modification.WidgetGroupModifier groupModifier) {
        groupModifier.find(ColumnFractionWidgetRef.class) //
            .addAnnotation(Widget.class) //
            .withProperty("title", "Linear fraction") //
            .withProperty("description", """
                    Fraction of attributes to sample for each tree.
                    """) //
            .modify();
    }

    protected static void attributeSamplingAbsolute(final Modification.WidgetGroupModifier groupModifier) {
        groupModifier.find(ColumnAbsoluteWidgetRef.class) //
            .addAnnotation(Widget.class) //
            .withProperty("title", "Absolute number") //
            .withProperty("description", """
                    Number of attributes to sample for each tree.
                    """) //
            .modify();
    }

    protected static void attributeSelectionReuse(final Modification.WidgetGroupModifier groupModifier) {
        groupModifier.find(AttributeReuseWidgetRef.class) //
            .addAnnotation(Widget.class) //
            .withProperty("title", "Attribute selection") //
            .withProperty("description", """
                    Choose whether to use the same set of sampled attributes for all nodes in a tree, or sample a new \
                    set for each node.
                    """) //
            .modify();
    }

    protected static void saveTargetDistribution(final Modification.WidgetGroupModifier groupModifier) {
        groupModifier.find(SaveTargetDistributionRef.class) //
            .addAnnotation(Widget.class) //
            .withProperty("title", "Save target distribution in tree nodes") //
            .withProperty("description",
                """
                                Store the distribution of the target category values in each tree node. This increases the memory \
                            footprint but is required for some downstream views and model exports.
                        """) //
            .modify();
    }

    protected static void splitCriterion(final Modification.WidgetGroupModifier groupModifier) {
        groupModifier.find(SplitCriterionWidgetRef.class) //
            .addAnnotation(Widget.class) //
            .withProperty("title", "Split criterion") //
            .withProperty("description", """
                    Select the impurity measure used to evaluate candidate splits. Gini is the common default.
                    """) //
            .modify();
    }

    protected static void hilighting(final Modification.WidgetGroupModifier groupModifier) {
        groupModifier.find(HiliteCountRef.class).addAnnotation(Widget.class)
            .withProperty("title", "Enable highlighting (number of patterns to store)")
            .withProperty("description",
                "If selected, the node stores the selected number of rows and allows highlighting them in the node view.")
            .modify();
    }

    protected static void minChildNodeSize(final Modification.WidgetGroupModifier groupModifier) {
        groupModifier.find(MinNodeSizesParameters.MinChildNodeSizeRef.class).addAnnotation(Widget.class)
            .withProperty("title", "Minimum child node size")
            .withProperty("description",
                """
                        Minimum number of records allowed in the child nodes after a split. Must not exceed half the minimum split \
                        node size.
                        """)
            .modify();
    }

    protected static void numberOfModels(final Modification.WidgetGroupModifier groupModifier) {
        groupModifier.find(NrModelsRef.class) //
            .addAnnotation(Widget.class) //
            .withProperty("title", "Number of models") //
            .withProperty("description",
                """
                        Number of decision trees to learn. Larger ensembles generally provide more stable results but increase \
                        runtime.
                        """) //
            .modify();
    }

    protected static void randomSeed(final Modification.WidgetGroupModifier groupModifier) {
        groupModifier.find(SeedRef.class) //
            .addAnnotation(Widget.class) //
            .withProperty("title", "Use static random seed") //
            .withProperty("description",
                "Provide a seed to obtain deterministic results. Leave disabled to use a time-dependent seed.") //
            .modify();
    }

    enum SplitCriterionOption {
            @Label(value = "Gini", description = """
                    Measures the impurity of a split based on the Gini index. Common default for classification tasks.
                    """)
            GINI(SplitCriterion.Gini), @Label(value = "Information Gain", description = """
                    Maximizes the information gain when creating splits. Equivalent to entropy reduction.
                    """)
            INFORMATION_GAIN(SplitCriterion.InformationGain), @Label(value = "Information Gain Ratio", description = """
                    Uses the information gain ratio to compensate for the bias towards attributes with many distinct \
                    values.
                    """)
            INFORMATION_GAIN_RATIO(SplitCriterion.InformationGainRatio);

        SplitCriterionOption(final SplitCriterion delegate) {
            m_delegate = delegate;
        }

        SplitCriterion toLegacy() {
            return m_delegate;
        }

        static SplitCriterionOption fromLegacy(final SplitCriterion legacy) {
            return switch (legacy) {
                case InformationGain -> INFORMATION_GAIN;
                case InformationGainRatio -> INFORMATION_GAIN_RATIO;
                default -> GINI;
            };
        }

        private final SplitCriterion m_delegate;
    }

    private static final class SplitCriterionPersistor implements NodeParametersPersistor<SplitCriterionOption> {
        @Override
        public SplitCriterionOption load(final NodeSettingsRO settings) throws InvalidSettingsException {
            var legacy = SplitCriterion.valueOf(settings.getString(TreeEnsembleLearnerConfiguration.KEY_SPLIT_CRITERION,
                TreeEnsembleLearnerConfiguration.SplitCriterion.Gini.name()));
            return SplitCriterionOption.fromLegacy(legacy);
        }

        @Override
        public void save(final SplitCriterionOption value, final NodeSettingsWO settings) {
            settings.addString(TreeEnsembleLearnerConfiguration.KEY_SPLIT_CRITERION, value.toLegacy().name());
        }

        @Override
        public String[][] getConfigPaths() {
            return new String[][]{{TreeEnsembleLearnerConfiguration.KEY_SPLIT_CRITERION}};
        }
    }

    private static final class SplitCriterionChoices implements EnumChoicesProvider<SplitCriterionOption> {
        @Override
        public List<EnumChoice<SplitCriterionOption>> computeState(final NodeParametersInput context) {
            return List.of(EnumChoice.fromEnumConst(SplitCriterionOption.GINI),
                EnumChoice.fromEnumConst(SplitCriterionOption.INFORMATION_GAIN),
                EnumChoice.fromEnumConst(SplitCriterionOption.INFORMATION_GAIN_RATIO));
        }
    }

    protected interface SplitCriterionWidgetRef extends Modification.Reference {
    }

    @Layout(TreeOptionsSection.class)
    @ChoicesProvider(SplitCriterionChoices.class)
    @Persistor(SplitCriterionPersistor.class)
    @Modification.WidgetReference(SplitCriterionWidgetRef.class)
    SplitCriterionOption m_splitCriterion = SplitCriterionOption.GINI;

    protected interface AverageSplitPointsWidgetRef extends Modification.Reference {
    }

    @Layout(TreeOptionsSection.class)
    @Persist(configKey = TreeEnsembleLearnerConfiguration.KEY_USE_AVERAGE_SPLIT_POINTS)
    @Modification.WidgetReference(AverageSplitPointsWidgetRef.class)
    boolean m_useAverageSplitPoints = TreeEnsembleLearnerConfiguration.DEF_AVERAGE_SPLIT_POINTS;

    protected interface BinaryNominalSplitsWidgetRef extends Modification.Reference {
    }

    @Layout(TreeOptionsSection.class)
    @Persist(configKey = TreeEnsembleLearnerConfiguration.KEY_USE_BINARY_NOMINAL_SPLITS)
    @Modification.WidgetReference(BinaryNominalSplitsWidgetRef.class)
    boolean m_useBinaryNominalSplits = TreeEnsembleLearnerConfiguration.DEF_BINARY_NOMINAL_SPLITS;

    private static final class MaxDepthPersistor implements NodeParametersPersistor<Optional<Integer>> {
        @Override
        public Optional<Integer> load(final NodeSettingsRO settings) throws InvalidSettingsException {
            var value = settings.getInt(TreeEnsembleLearnerConfiguration.KEY_MAX_LEVELS,
                TreeEnsembleLearnerConfiguration.MAX_LEVEL_INFINITE);
            return value == TreeEnsembleLearnerConfiguration.MAX_LEVEL_INFINITE ? Optional.empty() : Optional.of(value);
        }

        @Override
        public void save(final Optional<Integer> value, final NodeSettingsWO settings) {
            settings.addInt(TreeEnsembleLearnerConfiguration.KEY_MAX_LEVELS,
                value.orElse(TreeEnsembleLearnerConfiguration.MAX_LEVEL_INFINITE));
        }

        @Override
        public String[][] getConfigPaths() {
            return new String[][]{{TreeEnsembleLearnerConfiguration.KEY_MAX_LEVELS}};
        }
    }

    protected interface MaxTreeDepthWidgetRef extends Modification.Reference {
    }

    @Layout(TreeOptionsSection.class)
    @OptionalWidget(defaultProvider = MaxTreeDepthDefaultProvider.class)
    @NumberInputWidget(minValidation = IsPositiveIntegerValidation.class)
    @Persistor(MaxDepthPersistor.class)
    @Modification.WidgetReference(MaxTreeDepthWidgetRef.class)
    Optional<Integer> m_maxTreeDepth = Optional.empty();

    @Persistor(MinNodeSizesParameters.MinNodeSizesPersistor.class)
    final MinNodeSizesParameters m_minNodeSizes = new MinNodeSizesParameters();

    private static final class HardCodedRootPersistor implements NodeParametersPersistor<Optional<String>> {
        @Override
        public Optional<String> load(final NodeSettingsRO settings) throws InvalidSettingsException {
            return Optional.ofNullable(settings.getString(TreeEnsembleLearnerConfiguration.KEY_ROOT_COLUMN, null));
        }

        @Override
        public void save(final Optional<String> value, final NodeSettingsWO settings) {
            settings.addString(TreeEnsembleLearnerConfiguration.KEY_ROOT_COLUMN, value.orElse(null));
        }

        @Override
        public String[][] getConfigPaths() {
            return new String[][]{{TreeEnsembleLearnerConfiguration.KEY_ROOT_COLUMN}};
        }
    }

    private static final class HardCodedRootValidationProvider implements CustomValidationProvider<Optional<String>> {

        @Override
        public ValidationCallback<Optional<String>>
            computeValidationCallback(final NodeParametersInput parametersInput) {
            return value -> {
                if (value.isPresent() && value.map(String::isBlank).orElse(false)) {
                    throw new InvalidSettingsException("Select a fixed root attribute or disable the option.");
                }
            };
        }

        @Override
        public void init(final StateProviderInitializer stateProviderInitializer) {
            stateProviderInitializer.computeAfterOpenDialog();
        }
    }

    protected interface FixedRootAttributeWidgetRef extends Modification.Reference {
    }

    @Layout(TreeOptionsSection.class)
    @ChoicesProvider(RootColumnProvider.class)
    @Persistor(HardCodedRootPersistor.class)
    @CustomValidation(HardCodedRootValidationProvider.class)
    @Modification.WidgetReference(FixedRootAttributeWidgetRef.class)
    Optional<String> m_hardCodedRootColumn = Optional.empty();

    protected interface NrModelsRef extends Modification.Reference {
    }

    @Layout(EnsembleConfigurationSection.class)
    @Modification.WidgetReference(NrModelsRef.class)
    @NumberInputWidget(minValidation = IsPositiveIntegerValidation.class)
    @Persist(configKey = TreeEnsembleLearnerConfiguration.KEY_NR_MODELS)
    int m_numberOfModels = TreeEnsembleLearnerConfiguration.DEF_NR_MODELS;

    private static final class RowSamplingFractionPersistor implements NodeParametersPersistor<Optional<Double>> {
        @Override
        public Optional<Double> load(final NodeSettingsRO settings) throws InvalidSettingsException {
            var fraction = settings.getDouble(TreeEnsembleLearnerConfiguration.KEY_DATA_FRACTION,
                TreeEnsembleLearnerConfiguration.DEF_DATA_FRACTION);
            return fraction < 1.0 ? Optional.of(fraction) : Optional.empty();
        }

        @Override
        public void save(final Optional<Double> value, final NodeSettingsWO settings) {
            settings.addDouble(TreeEnsembleLearnerConfiguration.KEY_DATA_FRACTION, value.orElse(1.0));
        }

        @Override
        public String[][] getConfigPaths() {
            return new String[][]{{TreeEnsembleLearnerConfiguration.KEY_DATA_FRACTION}};
        }
    }

    private static final class RowSamplingFractionMaxValidation extends MaxValidation {
        @Override
        protected double getMax() {
            return 1.0;
        }
    }

    protected interface RowSamplingFractionWidgetRef extends Modification.Reference {
    }

    @Layout(DataSamplingSection.class)
    @OptionalWidget(defaultProvider = RowSamplingFractionDefaultProvider.class)
    @NumberInputWidget(minValidation = IsPositiveDoubleValidation.class,
        maxValidation = RowSamplingFractionMaxValidation.class)
    @Persistor(RowSamplingFractionPersistor.class)
    @Modification.WidgetReference(RowSamplingFractionWidgetRef.class)
    Optional<Double> m_rowSamplingFraction = Optional.empty();

    private static final class RowSamplingReplacementPersistor implements NodeParametersPersistor<Boolean> {
        @Override
        public Boolean load(final NodeSettingsRO settings) throws InvalidSettingsException {
            return settings.getBoolean(TreeEnsembleLearnerConfiguration.KEY_IS_DATA_SELECTION_WITH_REPLACEMENT, true);
        }

        @Override
        public void save(final Boolean value, final NodeSettingsWO settings) {
            settings.addBoolean(TreeEnsembleLearnerConfiguration.KEY_IS_DATA_SELECTION_WITH_REPLACEMENT,
                Boolean.TRUE.equals(value));
        }

        @Override
        public String[][] getConfigPaths() {
            return new String[][]{{TreeEnsembleLearnerConfiguration.KEY_IS_DATA_SELECTION_WITH_REPLACEMENT}};
        }
    }

    protected interface RowSamplingReplacementWidgetRef extends Modification.Reference {
    }

    @Layout(DataSamplingSection.class)
    @Persistor(RowSamplingReplacementPersistor.class)
    @Modification.WidgetReference(RowSamplingReplacementWidgetRef.class)
    boolean m_rowSamplingWithReplacement = true;

    enum RowSamplingModeOption {
            @Label(value = "Random", description = """
                    Sample rows uniformly at random.
                    """)
            RANDOM(RowSamplingMode.Random), @Label(value = "Stratified", description = """
                    Sample rows stratified by the target class distribution.
                    """)
            STRATIFIED(RowSamplingMode.Stratified), @Label(value = "Equal size", description = """
                    Sample the same number of rows for each class. Available for classification tasks.
                    """)
            EQUAL_SIZE(RowSamplingMode.EqualSize);

        RowSamplingModeOption(final RowSamplingMode delegate) {
            m_delegate = delegate;
        }

        RowSamplingMode toLegacy() {
            return m_delegate;
        }

        static RowSamplingModeOption fromLegacy(final RowSamplingMode legacy) {
            for (var option : values()) {
                if (option.m_delegate == legacy) {
                    return option;
                }
            }
            return RANDOM;
        }

        private final RowSamplingMode m_delegate;
    }

    private static final class RowSamplingModePersistor implements NodeParametersPersistor<RowSamplingModeOption> {
        @Override
        public RowSamplingModeOption load(final NodeSettingsRO settings) throws InvalidSettingsException {
            var legacy = RowSamplingMode.valueOf(settings
                .getString(TreeEnsembleLearnerConfiguration.KEY_ROW_SAMPLING_MODE, RowSamplingMode.Random.name()));
            return RowSamplingModeOption.fromLegacy(legacy);
        }

        @Override
        public void save(final RowSamplingModeOption value, final NodeSettingsWO settings) {
            settings.addString(TreeEnsembleLearnerConfiguration.KEY_ROW_SAMPLING_MODE, value.toLegacy().name());
        }

        @Override
        public String[][] getConfigPaths() {
            return new String[][]{{TreeEnsembleLearnerConfiguration.KEY_ROW_SAMPLING_MODE}};
        }
    }

    protected interface RowSamplingModeWidgetRef extends Modification.Reference {
    }

    @Layout(DataSamplingSection.class)
    @Persistor(RowSamplingModePersistor.class)
    @Modification.WidgetReference(RowSamplingModeWidgetRef.class)
    RowSamplingModeOption m_rowSamplingMode = RowSamplingModeOption.RANDOM;

    private static final class ColumnSamplingModeChoices implements EnumChoicesProvider<ColumnSamplingModeOption> {
        @Override
        public List<EnumChoice<ColumnSamplingModeOption>> computeState(final NodeParametersInput context) {
            return List.of(EnumChoice.fromEnumConst(ColumnSamplingModeOption.NONE),
                EnumChoice.fromEnumConst(ColumnSamplingModeOption.SQUARE_ROOT),
                EnumChoice.fromEnumConst(ColumnSamplingModeOption.LINEAR),
                EnumChoice.fromEnumConst(ColumnSamplingModeOption.ABSOLUTE));
        }
    }

    enum ColumnSamplingModeOption {
            @Label(value = "All columns", description = """
                    Disable column sampling and use all available attributes for every tree.
                    """)
            NONE(ColumnSamplingMode.None), @Label(value = "Square root", description = """
                    Sample the square root of the number of available attributes for each tree (default random forest \
                    behaviour).
                    """)
            SQUARE_ROOT(ColumnSamplingMode.SquareRoot), @Label(value = "Linear fraction", description = """
                    Sample a fraction of the available attributes for each tree.
                    """)
            LINEAR(ColumnSamplingMode.Linear), @Label(value = "Absolute number", description = """
                    Sample a fixed number of attributes for each tree.
                    """)
            ABSOLUTE(ColumnSamplingMode.Absolute);

        ColumnSamplingModeOption(final ColumnSamplingMode delegate) {
            m_delegate = delegate;
        }

        ColumnSamplingMode toLegacy() {
            return m_delegate;
        }

        static ColumnSamplingModeOption fromLegacy(final ColumnSamplingMode legacy) {
            return switch (legacy) {
                case Linear -> LINEAR;
                case Absolute -> ABSOLUTE;
                case None -> NONE;
                default -> SQUARE_ROOT;
            };
        }

        private final ColumnSamplingMode m_delegate;
    }

    private static final class ColumnSamplingModePersistor
        implements NodeParametersPersistor<ColumnSamplingModeOption> {
        @Override
        public ColumnSamplingModeOption load(final NodeSettingsRO settings) throws InvalidSettingsException {
            var legacy =
                ColumnSamplingMode.valueOf(settings.getString(TreeEnsembleLearnerConfiguration.KEY_COLUMN_SAMPLING_MODE,
                    TreeEnsembleLearnerConfiguration.DEF_COLUMN_SAMPLING_MODE.name()));
            return ColumnSamplingModeOption.fromLegacy(legacy);
        }

        @Override
        public void save(final ColumnSamplingModeOption value, final NodeSettingsWO settings) {
            settings.addString(TreeEnsembleLearnerConfiguration.KEY_COLUMN_SAMPLING_MODE, value.toLegacy().name());
        }

        @Override
        public String[][] getConfigPaths() {
            return new String[][]{{TreeEnsembleLearnerConfiguration.KEY_COLUMN_SAMPLING_MODE}};
        }
    }

    private interface ColumnSamplingModeRef extends ParameterReference<ColumnSamplingModeOption> {
    }

    private static final class ColumnFractionEnabledPredicate implements EffectPredicateProvider {
        @Override
        public EffectPredicate init(final PredicateInitializer initializer) {
            return initializer.getEnum(ColumnSamplingModeRef.class).isOneOf(ColumnSamplingModeOption.LINEAR);
        }
    }

    private static final class ColumnAbsoluteEnabledPredicate implements EffectPredicateProvider {
        @Override
        public EffectPredicate init(final PredicateInitializer initializer) {
            return initializer.getEnum(ColumnSamplingModeRef.class).isOneOf(ColumnSamplingModeOption.ABSOLUTE);
        }
    }

    protected interface ColumnSamplingModeWidgetRef extends Modification.Reference {
    }

    @Layout(EnsembleConfigurationSection.class)
    @ChoicesProvider(ColumnSamplingModeChoices.class)
    @Persistor(ColumnSamplingModePersistor.class)
    @ValueReference(ColumnSamplingModeRef.class)
    @Modification.WidgetReference(ColumnSamplingModeWidgetRef.class)
    ColumnSamplingModeOption m_columnSamplingMode = ColumnSamplingModeOption.SQUARE_ROOT;

    private static final class ColumnFractionPersistor implements NodeParametersPersistor<Double> {
        @Override
        public Double load(final NodeSettingsRO settings) throws InvalidSettingsException {
            return settings.getDouble(TreeEnsembleLearnerConfiguration.KEY_COLUMN_FRACTION_LINEAR,
                TreeEnsembleLearnerConfiguration.DEF_COLUMN_FRACTION);
        }

        @Override
        public void save(final Double value, final NodeSettingsWO settings) {
            settings.addDouble(TreeEnsembleLearnerConfiguration.KEY_COLUMN_FRACTION_LINEAR, value);
        }

        @Override
        public String[][] getConfigPaths() {
            return new String[][]{{TreeEnsembleLearnerConfiguration.KEY_COLUMN_FRACTION_LINEAR}};
        }
    }

    private static final class ColumnFractionMaxValidation extends MaxValidation {
        @Override
        protected double getMax() {
            return 1.0;
        }
    }

    protected interface ColumnFractionWidgetRef extends Modification.Reference {
    }

    @Layout(EnsembleConfigurationSection.class)
    @NumberInputWidget(minValidation = IsPositiveDoubleValidation.class,
        maxValidation = ColumnFractionMaxValidation.class)
    @Effect(predicate = ColumnFractionEnabledPredicate.class, type = EffectType.SHOW)
    @Persistor(ColumnFractionPersistor.class)
    @Modification.WidgetReference(ColumnFractionWidgetRef.class)
    double m_columnFractionLinear = TreeEnsembleLearnerConfiguration.DEF_COLUMN_FRACTION;

    private static final class ColumnAbsolutePersistor implements NodeParametersPersistor<Integer> {
        @Override
        public Integer load(final NodeSettingsRO settings) throws InvalidSettingsException {
            return settings.getInt(TreeEnsembleLearnerConfiguration.KEY_COLUMN_ABSOLUTE,
                TreeEnsembleLearnerConfiguration.DEF_COLUMN_ABSOLUTE);
        }

        @Override
        public void save(final Integer value, final NodeSettingsWO settings) {
            settings.addInt(TreeEnsembleLearnerConfiguration.KEY_COLUMN_ABSOLUTE, value);
        }

        @Override
        public String[][] getConfigPaths() {
            return new String[][]{{TreeEnsembleLearnerConfiguration.KEY_COLUMN_ABSOLUTE}};
        }
    }

    protected interface ColumnAbsoluteWidgetRef extends Modification.Reference {
    }

    @Layout(EnsembleConfigurationSection.class)
    @NumberInputWidget(minValidation = IsPositiveIntegerValidation.class)
    @Effect(predicate = ColumnAbsoluteEnabledPredicate.class, type = EffectType.SHOW)
    @Persistor(ColumnAbsolutePersistor.class)
    @Modification.WidgetReference(ColumnAbsoluteWidgetRef.class)
    int m_columnAbsolute = TreeEnsembleLearnerConfiguration.DEF_COLUMN_ABSOLUTE;

    enum AttributeReuseOption {
            @Label("Use same set of attributes for entire tree")
            SAME_FOR_TREE, @Label("Use different set of attributes for each tree node")
            DIFFERENT_FOR_EACH_NODE;

        static AttributeReuseOption fromBoolean(final boolean useDifferent) {
            return useDifferent ? DIFFERENT_FOR_EACH_NODE : SAME_FOR_TREE;
        }

        boolean toBoolean() {
            return this == DIFFERENT_FOR_EACH_NODE;
        }
    }

    private static final class AttributeReusePersistor implements NodeParametersPersistor<AttributeReuseOption> {
        @Override
        public AttributeReuseOption load(final NodeSettingsRO settings) throws InvalidSettingsException {
            return AttributeReuseOption.fromBoolean(settings
                .getBoolean(TreeEnsembleLearnerConfiguration.KEY_IS_USE_DIFFERENT_ATTRIBUTES_AT_EACH_NODE, true));
        }

        @Override
        public void save(final AttributeReuseOption value, final NodeSettingsWO settings) {
            settings.addBoolean(TreeEnsembleLearnerConfiguration.KEY_IS_USE_DIFFERENT_ATTRIBUTES_AT_EACH_NODE,
                value.toBoolean());
        }

        @Override
        public String[][] getConfigPaths() {
            return new String[][]{{TreeEnsembleLearnerConfiguration.KEY_IS_USE_DIFFERENT_ATTRIBUTES_AT_EACH_NODE}};
        }
    }

    protected interface AttributeReuseWidgetRef extends Modification.Reference {
    }

    @Layout(EnsembleConfigurationSection.class)
    @RadioButtonsWidget
    @Persistor(AttributeReusePersistor.class)
    @Modification.WidgetReference(AttributeReuseWidgetRef.class)
    AttributeReuseOption m_attributeReuse = AttributeReuseOption.DIFFERENT_FOR_EACH_NODE;

    private static final class SeedPersistor implements NodeParametersPersistor<Optional<Long>> {
        @Override
        public Optional<Long> load(final NodeSettingsRO settings) throws InvalidSettingsException {
            var seedString = settings.getString(TreeEnsembleLearnerConfiguration.KEY_SEED, null);
            if (seedString == null || seedString.isBlank()) {
                return Optional.empty();
            }
            try {
                return Optional.of(Long.parseLong(seedString));
            } catch (NumberFormatException nfe) {
                throw new InvalidSettingsException("Unable to parse seed \"" + seedString + "\"", nfe);
            }
        }

        @Override
        public void save(final Optional<Long> value, final NodeSettingsWO settings) {
            settings.addString(TreeEnsembleLearnerConfiguration.KEY_SEED, value.map(Object::toString).orElse(null));
        }

        @Override
        public String[][] getConfigPaths() {
            return new String[][]{{TreeEnsembleLearnerConfiguration.KEY_SEED}};
        }
    }

    protected interface SeedRef extends Modification.Reference {
    }

    @Layout(EnsembleConfigurationSection.class)
    @Modification.WidgetReference(SeedRef.class)
    @OptionalWidget(defaultProvider = SeedDefaultProvider.class)
    @NumberInputWidget(minValidation = IsPositiveIntegerValidation.class)
    @Persistor(SeedPersistor.class)
    Optional<Long> m_seed = Optional.empty();

}
