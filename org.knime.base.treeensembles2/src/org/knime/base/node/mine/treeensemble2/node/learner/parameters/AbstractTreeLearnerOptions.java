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
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataTableSpec;
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
import org.knime.node.parameters.updates.ValueReference;
import org.knime.node.parameters.widget.OptionalWidget;
import org.knime.node.parameters.widget.OptionalWidget.DefaultValueProvider;
import org.knime.node.parameters.widget.choices.ChoicesProvider;
import org.knime.node.parameters.widget.choices.ColumnChoicesProvider;
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

    private static final class RootColumnProvider extends CompatibleColumnsProvider {
        RootColumnProvider() {
            super(List.of(NominalValue.class, DoubleValue.class));
        }
    }

    private static final class RootColumnDefaultProvider implements DefaultValueProvider<String> {

        private Supplier<List<TypedStringChoice>> m_choicesProvider;

        @Override
        public void init(final StateProviderInitializer initializer) {
            DefaultValueProvider.super.init(initializer);
            m_choicesProvider = initializer.computeFromProvidedState(RootColumnProvider.class);
        }

        @Override
        public String computeState(final NodeParametersInput parametersInput) throws StateComputationFailureException {
            return m_choicesProvider.get().stream().findFirst().orElseThrow(StateComputationFailureException::new).id();
        }

    }

    abstract static class TargetColumnChoicesProvider implements ColumnChoicesProvider {

        abstract boolean isValidTargetColumn(final DataColumnSpec col);

        @Override
        public List<DataColumnSpec> columnChoices(final NodeParametersInput context) {
            return context.getInTableSpec(0).stream().flatMap(DataTableSpec::stream).filter(this::isValidTargetColumn)
                .toList();
        }

    }

    @Layout(AttributeSelectionSection.class)
    @Modification.WidgetReference(TargetColumnRef.class)
    @ValueReference(TargetColumnRef.class)
    @Persist(configKey = TreeEnsembleLearnerConfiguration.KEY_TARGET_COLUMN)
    @Widget(title = "Target column", description = """
            Select the column containing the value to be learned. Rows with missing values in this column are \
            ignored during the learning process.
            """)
    String m_targetColumn;

    interface TargetColumnRef extends ParameterReference<String>, Modification.Reference {
    }

    @Persistor(TrainingAttributesParameters.Persistor.class)
    TrainingAttributesParameters m_trainingAttributes = new TrainingAttributesParameters();

    @Layout(AttributeSelectionSection.class)
    @ColumnFilterWidget(choicesProvider = AttributeColumnsProvider.class)
    @TypedStringFilterWidgetInternal(hideTypeFilter = true)
    @Effect(predicate = Predicates.ColumnAttributesSelectedPredicate.class, type = EffectType.SHOW)
    @Persistor(Persistors.ColumnFilterPersistor.class)
    @Widget(title = "Attribute selection", description = """
            Select the ordinary columns that should be used as learning attributes. Use the include/exclude \
            lists or pattern matching to manage the selection.
            """)
    ColumnFilter m_attributeColumns = new ColumnFilter().withExcludeUnknownColumns();

    @Layout(AttributeSelectionSection.class)
    @Effect(predicate = Predicates.ColumnAttributesSelectedPredicate.class, type = EffectType.SHOW)
    @Persist(configKey = TreeEnsembleLearnerConfiguration.KEY_IGNORE_COLUMNS_WITHOUT_DOMAIN)
    @Modification.WidgetReference(IgnoreColumnsWithoutDomainWidgetRef.class)
    boolean m_ignoreColumnsWithoutDomain = true;

    protected interface IgnoreColumnsWithoutDomainWidgetRef extends Modification.Reference {
    }

    /**
     * Not configurable for the Random Forest nodes
     *
     * @param groupModifier the group modifier
     */
    public static void showIgnoreColumnsWithoutDomainInfo(final Modification.WidgetGroupModifier groupModifier) {
        groupModifier.find(IgnoreColumnsWithoutDomainWidgetRef.class) //
            .addAnnotation(Widget.class) //
            .withProperty("title", "Ignore columns without domain information") //
            .withProperty("description", """
                    Ignore nominal columns that do not contain domain information, for example when the number of \
                    nominal values is very large.
                    """) //
            .modify();
    }

    @Layout(TreeOptionsSection.class)
    @ChoicesProvider(Choices.MissingValueHandlingChoices.class)
    @Persistor(Persistors.MissingValueHandlingPersistor.class)
    @Modification.WidgetReference(References.MissingValueHandlingWidgetRef.class)
    Options.MissingValueHandlingOption m_missingValueHandling = Options.MissingValueHandlingOption.XGBOOST;

    @Layout(TreeOptionsSection.class)
    @ChoicesProvider(Choices.SplitCriterionChoices.class)
    @Persistor(Persistors.SplitCriterionPersistor.class)
    @Modification.WidgetReference(References.SplitCriterionWidgetRef.class)
    Options.SplitCriterionOption m_splitCriterion = Options.SplitCriterionOption.GINI;

    @Layout(TreeOptionsSection.class)
    @Persist(configKey = TreeEnsembleLearnerConfiguration.KEY_USE_AVERAGE_SPLIT_POINTS)
    @Modification.WidgetReference(AverageSplitPointsWidgetRef.class)
    boolean m_useAverageSplitPoints = TreeEnsembleLearnerConfiguration.DEF_AVERAGE_SPLIT_POINTS;

    interface AverageSplitPointsWidgetRef extends Modification.Reference {
    }

    /**
     * Not configurable for the Random Forest nodes
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

    interface BinaryNominalSplitsWidgetRef extends Modification.Reference {
    }

    /**
     * Not configurable for the Random Forest nodes
     *
     * @param groupModifier the group modifier
     */
    public static void showUseBinarySplitsForNominal(final Modification.WidgetGroupModifier groupModifier) {
        groupModifier.find(BinaryNominalSplitsWidgetRef.class) //
            .addAnnotation(Widget.class) //
            .withProperty("title", "Use binary splits for nominal columns") //
            .withProperty("description", """
                    Allow binary splits for nominal attributes. Disabling keeps the original multi-way splits.
                    """) //
            .modify();
    }

    @Layout(TreeOptionsSection.class)
    @OptionalWidget(defaultProvider = DefaultProviders.MaxTreeDepthDefaultProvider.class)
    @NumberInputWidget(minValidation = IsPositiveIntegerValidation.class)
    @Persistor(Persistors.MaxDepthPersistor.class)
    @Modification.WidgetReference(References.MaxTreeDepthWidgetRef.class)
    Optional<Integer> m_maxTreeDepth = Optional.empty();

    /**
     * Not shown in the Random Forest classification node
     *
     * @param groupModifier
     */
    public static void showLimitNumberOfLevels(final Modification.WidgetGroupModifier groupModifier) {
        groupModifier.find(References.MaxTreeDepthWidgetRef.class) //
            .addAnnotation(Widget.class) //
            .withProperty("title", "Limit number of levels (tree depth)") //
            .withProperty("description", """
                    Limit the maximal number of tree levels. When disabled the tree depth is unbounded.
                    """) //
            .modify();
    }

    @PersistEmbedded
    final MinNodeSizesParameters m_minNodeSizes = new MinNodeSizesParameters();

    @Layout(TreeOptionsSection.class)
    @ChoicesProvider(RootColumnProvider.class)
    @Persistor(Persistors.HardCodedRootPersistor.class)
    @OptionalWidget(defaultProvider = RootColumnDefaultProvider.class)
    @Modification.WidgetReference(References.FixedRootAttributeWidgetRef.class)
    Optional<String> m_hardCodedRootColumn = Optional.empty();

    /**
     * Not shown in the Random Forest nodes
     *
     * @param groupModifier the group modifier
     */
    public static void showFixedRootAttribute(final Modification.WidgetGroupModifier groupModifier) {
        groupModifier.find(References.FixedRootAttributeWidgetRef.class) //
            .addAnnotation(Widget.class) //
            .withProperty("title", "Use fixed root attribute") //
            .withProperty("description", """
                    Force the selected column to be used as the root split attribute in all trees.
                    """) //
            .modify();
    }

    @Layout(DataSamplingSection.class)
    @OptionalWidget(defaultProvider = DefaultProviders.RowSamplingFractionDefaultProvider.class)
    @NumberInputWidget(minValidation = IsPositiveDoubleValidation.class,
        maxValidation = Validations.RowSamplingFractionMaxValidation.class)
    @Persistor(Persistors.RowSamplingFractionPersistor.class)
    @Modification.WidgetReference(References.RowSamplingFractionWidgetRef.class)
    Optional<Double> m_rowSamplingFraction = Optional.empty();

    @Layout(DataSamplingSection.class)
    @Persistor(Persistors.RowSamplingReplacementPersistor.class)
    @Modification.WidgetReference(References.RowSamplingReplacementWidgetRef.class)
    boolean m_rowSamplingWithReplacement = true;

    @Layout(DataSamplingSection.class)
    @Persistor(Persistors.RowSamplingModePersistor.class)
    @Modification.WidgetReference(References.RowSamplingModeWidgetRef.class)
    Options.RowSamplingModeOption m_rowSamplingMode = Options.RowSamplingModeOption.RANDOM;

    @Layout(EnsembleConfigurationSection.class)
    @Modification.WidgetReference(References.NrModelsRef.class)
    @NumberInputWidget(minValidation = IsPositiveIntegerValidation.class)
    @Persist(configKey = TreeEnsembleLearnerConfiguration.KEY_NR_MODELS)
    int m_numberOfModels = TreeEnsembleLearnerConfiguration.DEF_NR_MODELS;

    @Layout(EnsembleConfigurationSection.class)
    @ChoicesProvider(Choices.ColumnSamplingModeChoices.class)
    @Persistor(Persistors.ColumnSamplingModePersistor.class)
    @ValueReference(References.ColumnSamplingModeRef.class)
    @Modification.WidgetReference(References.ColumnSamplingModeWidgetRef.class)
    Options.ColumnSamplingModeOption m_columnSamplingMode = Options.ColumnSamplingModeOption.SQUARE_ROOT;

    @Layout(EnsembleConfigurationSection.class)
    @NumberInputWidget(minValidation = IsPositiveDoubleValidation.class,
        maxValidation = Validations.ColumnFractionMaxValidation.class)
    @Effect(predicate = Predicates.ColumnFractionEnabledPredicate.class, type = EffectType.SHOW)
    @Persistor(Persistors.ColumnFractionPersistor.class)
    @Modification.WidgetReference(References.ColumnFractionWidgetRef.class)
    double m_columnFractionLinear = TreeEnsembleLearnerConfiguration.DEF_COLUMN_FRACTION;

    @Layout(EnsembleConfigurationSection.class)
    @NumberInputWidget(minValidation = IsPositiveIntegerValidation.class)
    @Effect(predicate = Predicates.ColumnAbsoluteEnabledPredicate.class, type = EffectType.SHOW)
    @Persistor(Persistors.ColumnAbsolutePersistor.class)
    @Modification.WidgetReference(References.ColumnAbsoluteWidgetRef.class)
    int m_columnAbsolute = TreeEnsembleLearnerConfiguration.DEF_COLUMN_ABSOLUTE;

    @Layout(EnsembleConfigurationSection.class)
    @RadioButtonsWidget
    @Persistor(Persistors.AttributeReusePersistor.class)
    @Modification.WidgetReference(References.AttributeReuseWidgetRef.class)
    Options.AttributeReuseOption m_attributeReuse = Options.AttributeReuseOption.DIFFERENT_FOR_EACH_NODE;

    @Layout(AdvancedSection.class)
    @Modification.WidgetReference(References.SeedRef.class)
    @OptionalWidget(defaultProvider = DefaultProviders.SeedDefaultProvider.class)
    @NumberInputWidget(minValidation = IsPositiveIntegerValidation.class)
    @Persistor(Persistors.SeedPersistor.class)
    Optional<Long> m_seed = Optional.empty();

    @Layout(AdvancedSection.class)
    @Modification.WidgetReference(References.HiliteCountRef.class)
    @OptionalWidget(defaultProvider = DefaultProviders.HiliteCountDefaultProvider.class)
    @NumberInputWidget(minValidation = IsPositiveIntegerValidation.class)
    @Persistor(Persistors.HiliteCountPersistor.class)
    Optional<Integer> m_hiliteCount = Optional.empty();

    @Layout(AdvancedSection.class)
    @Modification.WidgetReference(References.SaveTargetDistributionRef.class)
    @Persist(configKey = TreeEnsembleLearnerConfiguration.KEY_SAVE_TARGET_DISTRIBUTION_IN_NODES)
    boolean m_saveTargetDistributionInNodes;

}
