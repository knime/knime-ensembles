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
import org.knime.base.node.mine.treeensemble2.node.learner.parameters.RowSamplingFraction.EnableRowSamplingWidgetRef;
import org.knime.base.node.mine.treeensemble2.node.learner.parameters.Validations.AtMostOneValidation;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DoubleValue;
import org.knime.core.data.NominalValue;
import org.knime.core.webui.node.dialog.defaultdialog.internal.widget.PersistWithin.PersistEmbedded;
import org.knime.core.webui.node.dialog.defaultdialog.internal.widget.TypedStringFilterWidgetInternal;
import org.knime.core.webui.node.dialog.defaultdialog.util.updates.StateComputationFailureException;
import org.knime.core.webui.node.dialog.defaultdialog.widget.Modification;
import org.knime.node.parameters.Advanced;
import org.knime.node.parameters.NodeParameters;
import org.knime.node.parameters.NodeParametersInput;
import org.knime.node.parameters.Widget;
import org.knime.node.parameters.layout.After;
import org.knime.node.parameters.layout.Layout;
import org.knime.node.parameters.layout.Section;
import org.knime.node.parameters.migration.LoadDefaultsForAbsentFields;
import org.knime.node.parameters.persistence.Persist;
import org.knime.node.parameters.persistence.Persistor;
import org.knime.node.parameters.updates.Effect;
import org.knime.node.parameters.updates.Effect.EffectType;
import org.knime.node.parameters.updates.ParameterReference;
import org.knime.node.parameters.updates.ValueProvider;
import org.knime.node.parameters.updates.ValueReference;
import org.knime.node.parameters.widget.OptionalWidget;
import org.knime.node.parameters.widget.OptionalWidget.DefaultValueProvider;
import org.knime.node.parameters.widget.choices.ChoicesProvider;
import org.knime.node.parameters.widget.choices.ColumnChoicesProvider;
import org.knime.node.parameters.widget.choices.EnumChoicesProvider;
import org.knime.node.parameters.widget.choices.RadioButtonsWidget;
import org.knime.node.parameters.widget.choices.TypedStringChoice;
import org.knime.node.parameters.widget.choices.filter.ColumnFilter;
import org.knime.node.parameters.widget.choices.filter.ColumnFilterWidget;
import org.knime.node.parameters.widget.choices.util.CompatibleColumnsProvider;
import org.knime.node.parameters.widget.number.NumberInputWidget;
import org.knime.node.parameters.widget.number.NumberInputWidgetValidation.MinValidation.IsPositiveDoubleValidation;
import org.knime.node.parameters.widget.number.NumberInputWidgetValidation.MinValidation.IsPositiveIntegerValidation;

/**
 * Shared parameter definition for all tree-based learners.
 * <p>
 * This abstract class contains all possible fields that may be used by any tree learner variant. Subclasses use the
 * {@link Modification} framework to selectively configure which widgets are shown in their dialogs.
 */
@LoadDefaultsForAbsentFields
@SuppressWarnings("restriction")
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

    static final long DETERMINISTIC_SEED_DEFAULT = 1_764_074_296_579L;

    private static final class AttributeColumnsProvider extends CompatibleColumnsProvider {

        private Supplier<String> m_targetSupplier;

        AttributeColumnsProvider() {
            super(List.of(NominalValue.class, DoubleValue.class));
        }

        @Override
        public void init(final StateProviderInitializer initializer) {
            m_targetSupplier = initializer.computeFromValueSupplier(TargetColumnRef.class);
            super.init(initializer);
        }

        @Override
        public boolean isIncluded(final DataColumnSpec col) {
            if (!super.isIncluded(col)) {
                return false;
            }
            var target = m_targetSupplier.get();
            return target == null || !target.equals(col.getName());
        }
    }

    @Layout(AttributeSelectionSection.class)
    @Modification.WidgetReference(TargetColumnWidgetRef.class)
    @ValueReference(TargetColumnRef.class)
    @Persist(configKey = TreeEnsembleLearnerConfiguration.KEY_TARGET_COLUMN)
    @Widget(title = "Target column", description = """
            Select the column containing the value to be learned. Rows with missing values in this column are \
            ignored during the learning process.
            """)
    String m_targetColumn;

    interface TargetColumnRef extends ParameterReference<String> {
    }

    private interface TargetColumnWidgetRef extends Modification.Reference {
    }

    /**
     * Adds classification- or regression-specific providers for target column. Package-private to allow type-specific
     * classes to call with their providers.
     *
     * @param groupModifier the group modifier
     * @param valueProviderClass the value provider class
     * @param choicesProviderClass the choices provider class
     */
    static void setTargetColumnChoices(final Modification.WidgetGroupModifier groupModifier,
        final Class<? extends TargetColumnAutoSelectionProvider> valueProviderClass,
        final Class<? extends ColumnChoicesProvider> choicesProviderClass) {
        groupModifier.find(TargetColumnWidgetRef.class).addAnnotation(ValueProvider.class)
            .withProperty("value", valueProviderClass).modify();
        groupModifier.find(TargetColumnWidgetRef.class).addAnnotation(ChoicesProvider.class)
            .withProperty("value", choicesProviderClass).modify();
    }

    @Persistor(TrainingAttributesParameters.Persistor.class)
    TrainingAttributesParameters m_trainingAttributes = new TrainingAttributesParameters();

    @Layout(AttributeSelectionSection.class)
    @ColumnFilterWidget(choicesProvider = AttributeColumnsProvider.class)
    @TypedStringFilterWidgetInternal(hideTypeFilter = true)
    @Effect(predicate = Predicates.ColumnAttributesSelectedPredicate.class, type = EffectType.SHOW)
    @Persistor(Persistors.ColumnFilterPersistor.class)
    @Widget(title = "Attribute selection",
        description = """
                """)
    ColumnFilter m_attributeColumns = new ColumnFilter().withExcludeUnknownColumns();

    @Layout(AttributeSelectionSection.class)
    @Effect(predicate = Predicates.ColumnAttributesSelectedPredicate.class, type = EffectType.SHOW)
    @Persist(configKey = TreeEnsembleLearnerConfiguration.KEY_IGNORE_COLUMNS_WITHOUT_DOMAIN)
    @Modification.WidgetReference(IgnoreColumnsWithoutDomainWidgetRef.class)
    boolean m_ignoreColumnsWithoutDomain = true;

    private interface IgnoreColumnsWithoutDomainWidgetRef extends Modification.Reference {
    }

    /**
     * Only used by Tree Ensemble Classification and Tree Ensemble Regression (not Random Forest nodes).
     *
     * @param groupModifier the group modifier
     */
    public static void showIgnoreColumnsWithoutDomainInfo(final Modification.WidgetGroupModifier groupModifier) {
        groupModifier.find(IgnoreColumnsWithoutDomainWidgetRef.class) //
            .addAnnotation(Widget.class) //
            .withProperty("title", "Ignore columns without domain information") //
            .withProperty("description",
                """
                        If selected, nominal columns with no domain information are ignored
                        (as they likely have too many possible values anyway).
                        """) //
            .modify();
    }

    @Layout(TreeOptionsSection.class)
    @Persistor(Persistors.MissingValueHandlingPersistor.class)
    @Modification.WidgetReference(MissingValueHandlingWidgetRef.class)
    EnumOptions.MissingValueHandlingOption m_missingValueHandling = EnumOptions.MissingValueHandlingOption.XGBOOST;

    private interface MissingValueHandlingWidgetRef extends Modification.Reference {
    }

    @Layout(TreeOptionsSection.class)
    @Persistor(Persistors.SplitCriterionPersistor.class)
    @Modification.WidgetReference(SplitCriterionWidgetRef.class)
    EnumOptions.SplitCriterionOption m_splitCriterion = EnumOptions.SplitCriterionOption.INFORMATION_GAIN_RATIO;

    private interface SplitCriterionWidgetRef extends Modification.Reference {
    }

    /**
     * Only used by Classification nodes (Tree Ensemble Classification and Random Forest Classification).
     *
     * @param groupModifier the group modifier
     */
    public static void showSplitCriterion(final Modification.WidgetGroupModifier groupModifier) {
        groupModifier.find(SplitCriterionWidgetRef.class).addAnnotation(Widget.class)
            .withProperty("title", "Split criterion")
            .withProperty("description",
                """
                        Choose the
                        <a href="http://en.wikipedia.org/wiki/Decision_tree_learning#Formulae">split criterion</a>
                        here. Gini is usually a good choice and is used in "Classification and Regression Trees"
                        (Breiman et al, 1984) and the original random forest algorithm
                        (as described by Breiman et al, 2001);
                        information gain is used in C4.5; the information gain ratio normalizes the standard
                        information gain by the split entropy to overcome any unfair preference for nominal splits with
                        many child nodes.
                            """)
            .modify();
    }

    @Layout(TreeOptionsSection.class)
    @Persist(configKey = TreeEnsembleLearnerConfiguration.KEY_USE_AVERAGE_SPLIT_POINTS)
    @Modification.WidgetReference(AverageSplitPointsWidgetRef.class)
    boolean m_useAverageSplitPoints = TreeEnsembleLearnerConfiguration.DEF_AVERAGE_SPLIT_POINTS;

    private interface AverageSplitPointsWidgetRef extends Modification.Reference {
    }

    /**
     * Only used by Tree Ensemble Classification and Tree Ensemble Regression (not Random Forest nodes).
     *
     * @param groupModifier the group modifier
     */
    public static void showUseMidpointSplits(final Modification.WidgetGroupModifier groupModifier) {
        groupModifier.find(AverageSplitPointsWidgetRef.class) //
            .addAnnotation(Widget.class) //
            .withProperty("title", "Use mid-point splits") //
            .withProperty("description", """
                    For numerical splits, use the mid-point between two class boundaries. Otherwise the split value \
                    corresponds to the lower boundary with a ≤ comparison.
                    """) //
            .modify();
    }

    @Layout(TreeOptionsSection.class)
    @Persist(configKey = TreeEnsembleLearnerConfiguration.KEY_USE_BINARY_NOMINAL_SPLITS)
    @Modification.WidgetReference(BinaryNominalSplitsWidgetRef.class)
    boolean m_useBinaryNominalSplits = TreeEnsembleLearnerConfiguration.DEF_BINARY_NOMINAL_SPLITS;

    private interface BinaryNominalSplitsWidgetRef extends Modification.Reference {
    }

    /**
     * Only used by Tree Ensemble Classification and Tree Ensemble Regression (not Random Forest nodes).
     *
     * @param groupModifier the group modifier
     */
    public static void showUseBinarySplitsForNominal(final Modification.WidgetGroupModifier groupModifier) {
        groupModifier.find(BinaryNominalSplitsWidgetRef.class) //
            .addAnnotation(Widget.class) //
            .withProperty("title", "Use binary splits for nominal columns") //
            .withProperty("description",
                """
                        If selected, nominal columns also produce binary splits instead of multiway splits in which each
                        nominal value corresponds to one child node.
                        """) //
            .modify();
    }

    @Layout(TreeOptionsSection.class)
    @OptionalWidget(defaultProvider = DefaultProviders.MaxTreeDepthDefaultProvider.class)
    @NumberInputWidget(minValidation = IsPositiveIntegerValidation.class)
    @Persistor(Persistors.MaxDepthPersistor.class)
    @Widget(title = "Limit number of levels (tree depth)", description = """
            Limit the maximal number of tree levels. When disabled the tree depth is unbounded.
            For instance, a value of 1 would only split the (single) root node resulting in a decision stump
            """)
    Optional<Integer> m_maxTreeDepth = Optional.empty();

    @PersistEmbedded
    final MinNodeSizesParameters m_minNodeSizes = new MinNodeSizesParameters();

    @Layout(TreeOptionsSection.class)
    @Persistor(Persistors.HardCodedRootPersistor.class)
    @Modification.WidgetReference(FixedRootAttributeWidgetRef.class)
    Optional<String> m_hardCodedRootColumn = Optional.empty();

    private interface FixedRootAttributeWidgetRef extends Modification.Reference {
    }

    static void showFixedRootAttribute(final Modification.WidgetGroupModifier groupModifier,
        final Class<? extends ColumnChoicesProvider> choicesProviderClass,
        final Class<? extends RootColumnDefaultProvider> defaultProviderClass) {
        final var field = groupModifier.find(FixedRootAttributeWidgetRef.class);
        field.addAnnotation(Widget.class).withProperty("title", "Use fixed root attribute")
            .withProperty("description",
                """
                          Force the selected column to be used as the root split attribute in all trees -- even if the
                        column is not in the attribute sample.
                          """)
            .modify();
        field.addAnnotation(ChoicesProvider.class).withProperty("value", choicesProviderClass).modify();
        field.addAnnotation(OptionalWidget.class)
                .withProperty("defaultProvider", defaultProviderClass).modify();

    }

    static abstract class RootColumnDefaultProvider implements DefaultValueProvider<String> {

        private final Class<? extends ColumnChoicesProvider> m_columnsProvider;

        private Supplier<List<TypedStringChoice>> m_choicesProvider;

        RootColumnDefaultProvider(final Class<? extends ColumnChoicesProvider> columnsProvider) {
            m_columnsProvider = columnsProvider;
        }

        @Override
        public void init(final StateProviderInitializer initializer) {
            DefaultValueProvider.super.init(initializer);
            m_choicesProvider = initializer.computeFromProvidedState(m_columnsProvider);
        }

        @Override
        public String computeState(final NodeParametersInput parametersInput) throws StateComputationFailureException {
            return m_choicesProvider.get().stream().findFirst().orElseThrow(StateComputationFailureException::new).id();
        }

    }

    @Layout(DataSamplingSection.class)
    @Persistor(RowSamplingFraction.Persistor.class)
    RowSamplingFraction m_rowSamplingFraction = RowSamplingFraction.empty();

    @Layout(DataSamplingSection.class)
    @Persist(configKey = TreeEnsembleLearnerConfiguration.KEY_IS_DATA_SELECTION_WITH_REPLACEMENT)
    @Modification.WidgetReference(RowSamplingReplacementWidgetRef.class)
    @Effect(predicate = EnableRowSamplingWidgetRef.class, type = EffectType.SHOW)
    boolean m_rowSamplingWithReplacement = false;

    private interface RowSamplingReplacementWidgetRef extends Modification.Reference {
    }

    /**
     * Only used by Tree Ensemble nodes and Random Forest Classification (not Random Forest Regression).
     *
     * @param groupModifier the group modifier
     */
    private static void showRowSamplingWithReplacement(final Modification.WidgetGroupModifier groupModifier) {
        groupModifier.find(RowSamplingReplacementWidgetRef.class).addAnnotation(Widget.class)
            .withProperty("title", "Sample with replacement").withProperty("description", """
                    Draw sampled rows with replacement (bootstrap sampling). When disabled, rows are sampled without \
                    replacement.
                    """).modify();
    }

    @Layout(DataSamplingSection.class)
    @Persistor(Persistors.RowSamplingModePersistor.class)
    @Modification.WidgetReference(RowSamplingModeWidgetRef.class)
    @Effect(predicate = EnableRowSamplingWidgetRef.class, type = EffectType.SHOW)
    EnumOptions.RowSamplingModeOption m_rowSamplingMode = EnumOptions.RowSamplingModeOption.RANDOM;

    private interface RowSamplingModeWidgetRef extends Modification.Reference {
    }

    /**
     * Base @Widget modification for row sampling mode. Package-private to allow type-specific classes to call with
     * their choices provider.
     *
     * @param groupModifier the group modifier
     */
    private static void showRowSamplingMode(final Modification.WidgetGroupModifier groupModifier) {
        groupModifier.find(RowSamplingModeWidgetRef.class).addAnnotation(Widget.class)
            .withProperty("title", "Data sampling mode")
            .withProperty("description",
                """
                        The sampling mode decides how the rows are sampled.
                        In the random mode, the rows are sampled from the whole dataset, i.e. each row has exactly the
                        same probability as in the sample.
                        In case of equal size sampling, first a sample from the minority class is drawn and then the
                        same number of rows as in the minority sample are drawn from all other classes so that each
                        class is represented with the same number of rows in the sample.
                        If stratified sampling is selected, the same fraction of rows is drawn from each class so the
                        class distribution in the sample is approximately the same as in the full dataset.
                        """)
            .modify();
    }

    /**
     * Only used by Tree Ensemble nodes.
     *
     * @param groupModifier the group modifier
     */
    public static void showDataSamplingSection(final Modification.WidgetGroupModifier groupModifier) {
        RowSamplingFraction.showEnableRowSampling(groupModifier);
        RowSamplingFraction.showRowSamplingFraction(groupModifier);
        showRowSamplingWithReplacement(groupModifier);
        showRowSamplingMode(groupModifier);
    }

    /**
     * Restrict the choices for row sampling mode. Package-private to allow type-specific classes to call with their
     * provider.
     *
     * @param groupModifier the group modifier
     * @param choicesProviderClass the choices provider class
     */
    static void setRowSamplingModeChoices(final Modification.WidgetGroupModifier groupModifier,
        final Class<? extends EnumChoicesProvider<EnumOptions.RowSamplingModeOption>> choicesProviderClass) {
        groupModifier.find(RowSamplingModeWidgetRef.class).addAnnotation(ChoicesProvider.class)
            .withProperty("value", choicesProviderClass).modify();
    }

    @Layout(EnsembleConfigurationSection.class)
    @NumberInputWidget(minValidation = IsPositiveIntegerValidation.class)
    @Persist(configKey = TreeEnsembleLearnerConfiguration.KEY_NR_MODELS)
    @Widget(title = "Number of models",
        description = """
                Number of decision trees to learn.
                Larger ensembles generally provide more stable results but increase runtime.
                For most datasets, a value between 100 and 500 yields good results; however, the optimal number is data
                dependent and should thus be subject to hyperparameter tuning.
                """)
    int m_numberOfModels = TreeEnsembleLearnerConfiguration.DEF_NR_MODELS;

    @Layout(EnsembleConfigurationSection.class)
    @Persistor(Persistors.ColumnSamplingModePersistor.class)
    @ValueReference(ColumnSamplingModeRef.class)
    @Modification.WidgetReference(ColumnSamplingModeWidgetRef.class)
    EnumOptions.ColumnSamplingModeOption m_columnSamplingMode = EnumOptions.ColumnSamplingModeOption.SQUARE_ROOT;

    interface ColumnSamplingModeRef extends ParameterReference<EnumOptions.ColumnSamplingModeOption> {
    }

    private interface ColumnSamplingModeWidgetRef extends Modification.Reference {
    }

    /**
     * Only used by Tree Ensemble nodes and Random Forest Classification (not Random Forest Regression).
     *
     * @param groupModifier the group modifier
     */
    public static void showAttributeSampling(final Modification.WidgetGroupModifier groupModifier) {
        groupModifier.find(ColumnSamplingModeWidgetRef.class).addAnnotation(Widget.class)
            .withProperty("title", "Attribute sampling (columns)")
            .withProperty("description",
                """
                             Defines the sampling of attributes to learn an individual tree.
                             This can either be a function based on the number of attributes
                             (linear fraction or square root) or some absolute value.
                             The latter can be used in conjunction with flow variables
                             to inject some other value derived from the number of attributes (e.g. Breiman suggests
                             starting with the square root of the
                             number of attributes but also to try to double or half that number).
                        """)
            .modify();
    }

    @Layout(EnsembleConfigurationSection.class)
    @NumberInputWidget(minValidation = IsPositiveDoubleValidation.class, maxValidation = AtMostOneValidation.class)
    @Effect(predicate = Predicates.ColumnFractionEnabledPredicate.class, type = EffectType.SHOW)
    @Persist(configKey = TreeEnsembleLearnerConfiguration.KEY_COLUMN_FRACTION_LINEAR)
    @Modification.WidgetReference(ColumnFractionWidgetRef.class)
    double m_columnFractionLinear = TreeEnsembleLearnerConfiguration.DEF_COLUMN_FRACTION;

    private interface ColumnFractionWidgetRef extends Modification.Reference {
    }

    /**
     * Only used by Tree Ensemble nodes and Random Forest Classification (not Random Forest Regression).
     *
     * @param groupModifier the group modifier
     */
    public static void showAttributeSamplingLinearFraction(final Modification.WidgetGroupModifier groupModifier) {
        groupModifier.find(ColumnFractionWidgetRef.class).addAnnotation(Widget.class)
            .withProperty("title", "Linear fraction").withProperty("description", """
                    Fraction of attributes to sample for each tree.
                    """).modify();
    }

    @Layout(EnsembleConfigurationSection.class)
    @NumberInputWidget(minValidation = IsPositiveIntegerValidation.class)
    @Effect(predicate = Predicates.ColumnAbsoluteEnabledPredicate.class, type = EffectType.SHOW)
    @Persist(configKey = TreeEnsembleLearnerConfiguration.KEY_COLUMN_ABSOLUTE)
    @Modification.WidgetReference(ColumnAbsoluteWidgetRef.class)
    int m_columnAbsolute = TreeEnsembleLearnerConfiguration.DEF_COLUMN_ABSOLUTE;

    private interface ColumnAbsoluteWidgetRef extends Modification.Reference {
    }

    /**
     * Only used by Tree Ensemble nodes and Random Forest Classification (not Random Forest Regression).
     *
     * @param groupModifier the group modifier
     */
    public static void showAttributeSamplingAbsolute(final Modification.WidgetGroupModifier groupModifier) {
        groupModifier.find(ColumnAbsoluteWidgetRef.class).addAnnotation(Widget.class)
            .withProperty("title", "Absolute number").withProperty("description", """
                    Number of attributes to sample for each tree.
                    """).modify();
    }

    @Layout(EnsembleConfigurationSection.class)
    @RadioButtonsWidget
    @Persistor(Persistors.AttributeReusePersistor.class)
    @Modification.WidgetReference(AttributeReuseWidgetRef.class)
    EnumOptions.AttributeReuseOption m_attributeReuse = EnumOptions.AttributeReuseOption.DIFFERENT_FOR_EACH_NODE;

    private interface AttributeReuseWidgetRef extends Modification.Reference {
    }

    /**
     * Only used by Tree Ensemble nodes and Random Forest Classification (not Random Forest Regression).
     *
     * @param groupModifier the group modifier
     */
    public static void showAttributeSelectionReuse(final Modification.WidgetGroupModifier groupModifier) {
        groupModifier.find(AttributeReuseWidgetRef.class).addAnnotation(Widget.class)
            .withProperty("title", "Attribute selection")
            .withProperty("description",
                """
                            <p>
                          <i>Use the same set of attributes for each tree</i>
                          means that the attributes are sampled once for each tree
                          and this sample is then used to construct the tree.
                        </p>
                        <p>
                          <i>Use a different set of attributes for each tree node</i>
                          samples a different set of candidate attributes in each of the tree nodes
                          from which the optimal one is chosen to perform the split.
                          This is the option used in random forests.
                        </p>
                        """)
            .modify();
    }

    @Layout(AdvancedSection.class)
    @OptionalWidget(defaultProvider = DefaultProviders.SeedDefaultProvider.class)
    @NumberInputWidget(minValidation = IsPositiveIntegerValidation.class)
    @Persistor(Persistors.SeedPersistor.class)
    @Widget(title = "Use static random seed", description = """
            Provide a seed to obtain deterministic results. Leave disabled to use a time-dependent seed.
            """)
    Optional<Long> m_seed = Optional.of(1764585560353L);

    @Layout(AdvancedSection.class)
    @OptionalWidget(defaultProvider = DefaultProviders.HiliteCountDefaultProvider.class)
    @NumberInputWidget(minValidation = IsPositiveIntegerValidation.class)
    @Persistor(Persistors.HiliteCountPersistor.class)
    @Widget(title = "Enable highlighting (number of patterns to store)", description = """
            If selected, the node stores the selected number of rows and
            allows highlighting them in the node view.
            """)
    Optional<Integer> m_hiliteCount = Optional.empty();

    @Layout(AdvancedSection.class)
    @Modification.WidgetReference(SaveTargetDistributionRef.class)
    @Persist(configKey = TreeEnsembleLearnerConfiguration.KEY_SAVE_TARGET_DISTRIBUTION_IN_NODES)
    boolean m_saveTargetDistributionInNodes;

    private interface SaveTargetDistributionRef extends Modification.Reference {
    }

    /**
     * Only used by Classification nodes (Tree Ensemble Classification and Random Forest Classification).
     *
     * @param groupModifier the group modifier
     */
    public static void showSaveTargetDistribution(final Modification.WidgetGroupModifier groupModifier) {
        groupModifier.find(SaveTargetDistributionRef.class).addAnnotation(Widget.class)
            .withProperty("title", "Save target distribution in tree nodes (memory expensive)")
            .withProperty("description",
                """
                        If selected, the model stores the distribution of the target category values in each tree node.
                        Storing the class distribution may increase memory consumption considerably and we therefore
                        recommend disabling it if your use-case doesn't require it.
                        Class distribution is only needed if
                        <ul>
                            <li>You want to see the class distribution for each tree node in the node view.</li>
                            <li>You want to export individual decision trees to PMML.</li>
                            <li>
                                You want to use soft-voting (i.e. aggregation of probability distributions instead of
                                votes) in the predictor node.
                            </li>
                        </ul>""")
            .modify();
    }

}
