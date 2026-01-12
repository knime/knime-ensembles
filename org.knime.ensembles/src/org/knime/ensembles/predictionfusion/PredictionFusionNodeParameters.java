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

package org.knime.ensembles.predictionfusion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DoubleValue;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.webui.node.dialog.defaultdialog.internal.button.SimpleButtonWidget;
import org.knime.core.webui.node.dialog.defaultdialog.internal.persistence.ArrayPersistor;
import org.knime.core.webui.node.dialog.defaultdialog.internal.persistence.ElementFieldPersistor;
import org.knime.core.webui.node.dialog.defaultdialog.internal.persistence.PersistArray;
import org.knime.core.webui.node.dialog.defaultdialog.internal.persistence.PersistArrayElement;
import org.knime.core.webui.node.dialog.defaultdialog.internal.widget.ArrayWidgetInternal;
import org.knime.core.webui.node.dialog.defaultdialog.util.updates.StateComputationFailureException;
import org.knime.ensembles.predictionfusion.PredictionFusionNodeParameters.ConfidenceColumn.ClassNameRef;
import org.knime.ensembles.predictionfusion.PredictionFusionNodeParameters.ConfidenceColumn.SyncConfidenceColumnsWithClassesProvider;
import org.knime.ensembles.predictionfusion.PredictionFusionNodeParameters.PredictionItem.ConfidenceColumnsRef;
import org.knime.ensembles.predictionfusion.methods.impl.Maximum;
import org.knime.ensembles.predictionfusion.methods.impl.Mean;
import org.knime.ensembles.predictionfusion.methods.impl.Median;
import org.knime.ensembles.predictionfusion.methods.impl.Minimum;
import org.knime.node.parameters.NodeParameters;
import org.knime.node.parameters.NodeParametersInput;
import org.knime.node.parameters.Widget;
import org.knime.node.parameters.array.ArrayWidget;
import org.knime.node.parameters.layout.After;
import org.knime.node.parameters.layout.Layout;
import org.knime.node.parameters.layout.Section;
import org.knime.node.parameters.migration.LoadDefaultsForAbsentFields;
import org.knime.node.parameters.persistence.NodeParametersPersistor;
import org.knime.node.parameters.persistence.Persistor;
import org.knime.node.parameters.updates.ButtonReference;
import org.knime.node.parameters.updates.Effect;
import org.knime.node.parameters.updates.Effect.EffectType;
import org.knime.node.parameters.updates.EffectPredicate;
import org.knime.node.parameters.updates.EffectPredicateProvider;
import org.knime.node.parameters.updates.ParameterReference;
import org.knime.node.parameters.updates.StateProvider;
import org.knime.node.parameters.updates.ValueProvider;
import org.knime.node.parameters.updates.ValueReference;
import org.knime.node.parameters.widget.choices.ChoicesProvider;
import org.knime.node.parameters.widget.choices.Label;
import org.knime.node.parameters.widget.choices.util.ColumnSelectionUtil;
import org.knime.node.parameters.widget.choices.util.CompatibleColumnsProvider.DoubleColumnsProvider;
import org.knime.node.parameters.widget.choices.util.CompatibleColumnsProvider.StringColumnsProvider;
import org.knime.node.parameters.widget.number.NumberInputWidget;
import org.knime.node.parameters.widget.number.NumberInputWidgetValidation.MinValidation.IsPositiveIntegerValidation;
import org.knime.node.parameters.widget.text.TextInputWidget;
import org.knime.node.parameters.widget.text.TextInputWidgetValidation.PatternValidation.IsNotBlankValidation;

/**
 * Node parameters for Prediction Fusion.
 *
 * @author Magnus Gohm, KNIME GmbH, Konstanz, Germany
 * @author AI Migration Pipeline v1.2
 */
@LoadDefaultsForAbsentFields
@SuppressWarnings("restriction")
class PredictionFusionNodeParameters implements NodeParameters {

    @Section(title = "Classes")
    interface ClassesSection {
    }

    @Section(title = "Class Confidences")
    @After(ClassesSection.class)
    interface ClassConfidencesSection {
    }

    @Widget(title = "Method", description = """
            The fusion method.
            """)
    @Persistor(FusionMethodPersistor.class)
    @ValueReference(MethodRef.class)
    FusionMethod m_method = FusionMethod.MAXIMUM;

    static final class MethodRef implements ParameterReference<FusionMethod> {
    }

    @Layout(ClassesSection.class)
    @Widget(title = "Classes column", description = """
            String column from which to extract the classes from it's domain.
            """)
    @ChoicesProvider(StringColumnsProvider.class)
    @ValueReference(ClassesColumnRef.class)
    String m_classesColumn;

    static final class ClassesColumnRef implements ParameterReference<String> {
    }

    @Layout(ClassesSection.class)
    @Widget(title = "Add classes from column domain", description = """
            Add classes from the domain of the selected string column.
            """)
    @SimpleButtonWidget(ref = AddClassesFromColumnRef.class)
    Void m_addClassesFromColumn;

    static final class AddClassesFromColumnRef implements ButtonReference {
    }

    @Layout(ClassesSection.class)
    @Widget(title = "Classes", description = """
            The classes that were predicted and for which confidences are available. In case of a tie the higher
            class (in the list) wins.
            """)
    @ArrayWidget(addButtonText = "Add class", elementTitle = "Class", showSortButtons = true)
    @PersistArray(ClassesArrayPersistor.class)
    @ValueProvider(AddClassesFromColumnProvider.class)
    @ValueReference(ClassesRef.class)
    ClassItem[] m_classes = new ClassItem[0];

    static final class ClassesRef implements ParameterReference<ClassItem[]> {
    }

    @Layout(ClassConfidencesSection.class)
    @Widget(title = "Class confidences", description = """
            Each row represents the predicted confidences from one classifier. The fusion method is applied to all
            confidence values of the same class.
            """)
    @PersistArray(PredictionsArrayPersistor.class)
    @ArrayWidget(addButtonText = "Add prediction", elementTitle = "Prediction",
        elementDefaultValueProvider = NewPredictionProvider.class)
    @ValueReference(PredictionsRef.class)
    PredictionItem[] m_predictions = new PredictionItem[0];

    static final class PredictionsRef implements ParameterReference<PredictionItem[]> {
    }

    static final class AddClassesFromColumnProvider implements StateProvider<ClassItem[]> {

        Supplier<ClassItem[]> m_existingClassesSupplier;

        Supplier<String> m_classesColumnNameSupplier;

        @Override
        public void init(final StateProviderInitializer initializer) {
            initializer.computeOnButtonClick(AddClassesFromColumnRef.class);
            m_existingClassesSupplier = initializer.getValueSupplier(ClassesRef.class);
            m_classesColumnNameSupplier = initializer.getValueSupplier(ClassesColumnRef.class);
        }

        @Override
        public ClassItem[] computeState(final NodeParametersInput parametersInput)
            throws StateComputationFailureException {
            final var domainValueStrings = getDomainValueStrings(parametersInput, m_classesColumnNameSupplier);

            var existingClassItems = m_existingClassesSupplier.get();
            if (existingClassItems == null || existingClassItems.length == 0) {
                return domainValueStrings.stream().map(ClassItem::new).toArray(ClassItem[]::new);
            }

            // merge existing classes with classes extracted from the column domain
            var newClassItems = new ArrayList<ClassItem>();
            var encounteredClassItemNames = new HashSet<String>();

            for (ClassItem existingVariable : existingClassItems) {
                if (domainValueStrings.contains(existingVariable.m_className)) {
                    newClassItems.add(existingVariable);
                    encounteredClassItemNames.add(existingVariable.m_className);
                } else {
                    newClassItems.add(existingVariable);
                }
            }

            for (String domainValueString : domainValueStrings) {
                if (encounteredClassItemNames.contains(domainValueString)) {
                    continue;
                }
                newClassItems.add(new ClassItem(domainValueString));
            }

            return newClassItems.toArray(ClassItem[]::new);
        }

        private static List<String> getDomainValueStrings(final NodeParametersInput parametersInput,
            final Supplier<String> classesColumnNameSupplier) throws StateComputationFailureException {
            final var specOpt = parametersInput.getInPortSpec(0);
            if (specOpt.isEmpty()) {
                throw new StateComputationFailureException();
            }

            final var classesColumnName = classesColumnNameSupplier.get();
            if (classesColumnName == null || classesColumnName.isEmpty()) {
                throw new StateComputationFailureException();
            }

            final var tableSpec = (DataTableSpec)specOpt.get();
            final var classesColumn = tableSpec.getColumnSpec(classesColumnName);
            final var classesColumnDomain = classesColumn.getDomain();

            if (classesColumnDomain == null || !classesColumnDomain.hasValues()) {
                throw new StateComputationFailureException();
            }
            return classesColumnDomain.getValues().stream().map(DataCell::toString).toList();
		}

    }

    static final class NewPredictionProvider implements StateProvider<PredictionItem> {

        Supplier<ClassItem[]> m_classesSupplier;

        @Override
        public void init(final StateProviderInitializer initializer) {
            m_classesSupplier = initializer.computeFromValueSupplier(ClassesRef.class);
        }

        @Override
        public PredictionItem computeState(final NodeParametersInput parametersInput)
            throws StateComputationFailureException {
            final var classes = m_classesSupplier.get();

            if (classes == null) {
                return new PredictionItem(1, new ConfidenceColumn[0]);
            }

            final var compatibleColumns =
                ColumnSelectionUtil.getCompatibleColumnsOfFirstPort(parametersInput, DoubleValue.class);
            final var confidenceColumnDefault =
                compatibleColumns.isEmpty() ? null : compatibleColumns.get(compatibleColumns.size() - 1).getName();

            final var confidenceColumns = new ConfidenceColumn[classes.length];
            for (int i = 0; i < classes.length; i++) {
                confidenceColumns[i] = new ConfidenceColumn(classes[i].m_className, confidenceColumnDefault);
            }

            return new PredictionItem(1, confidenceColumns);
        }
    }

    static final class FusionMethodPersistor implements NodeParametersPersistor<FusionMethod> {

        @Override
        public FusionMethod load(final NodeSettingsRO settings) throws InvalidSettingsException {
            final var methodString =
                settings.getString(PredictionFusionNodeConfig.METHOD_CFG, FusionMethod.MAXIMUM.getValue());
            return FusionMethod.getFromValue(methodString.isEmpty() ? FusionMethod.MAXIMUM.getValue() : methodString);
        }

        @Override
        public void save(final FusionMethod obj, final NodeSettingsWO settings) {
            settings.addString(PredictionFusionNodeConfig.METHOD_CFG, obj.getValue());
        }

        @Override
        public String[][] getConfigPaths() {
            return new String[][]{{PredictionFusionNodeConfig.METHOD_CFG}};
        }

    }

    static final class ClassesArrayPersistor implements ArrayPersistor<Integer, ClassItem> {

        @Override
        public int getArrayLength(final NodeSettingsRO nodeSettings) {
            final String[] classNames =
                nodeSettings.getStringArray(PredictionFusionNodeConfig.CLASSES_CFG, new String[0]);
            return classNames.length;
        }

        @Override
        public Integer createElementLoadContext(final int index) {
            return index;
        }

        @Override
        public ClassItem createElementSaveDTO(final int index) {
            return new ClassItem();
        }

        @Override
        public void save(final List<ClassItem> savedElements, final NodeSettingsWO nodeSettings) {
            final String[] classNames = savedElements.stream().map(item -> item.m_className).toArray(String[]::new);
            nodeSettings.addStringArray(PredictionFusionNodeConfig.CLASSES_CFG, classNames);
        }

    }

    static final class PredictionsArrayPersistor implements ArrayPersistor<Integer, PredictionItem> {

        @Override
        public int getArrayLength(final NodeSettingsRO nodeSettings) {
            try {
                final var predictionsConfig = nodeSettings.getNodeSettings(PredictionFusionNodeConfig.PREDICTIONS_CFG);
                return predictionsConfig.getInt(PredictionFusionNodeConfig.NR_PREDICTIONS_CFG, 0);
            } catch (InvalidSettingsException e) {
                return 0;
            }
        }

        @Override
        public Integer createElementLoadContext(final int index) {
            return index;
        }

        @Override
        public PredictionItem createElementSaveDTO(final int index) {
            return new PredictionItem();
        }

        @Override
        public void save(final List<PredictionItem> savedElements, final NodeSettingsWO nodeSettings) {
            final var predictionsConfig = nodeSettings.addNodeSettings(PredictionFusionNodeConfig.PREDICTIONS_CFG);
            predictionsConfig.addInt(PredictionFusionNodeConfig.NR_PREDICTIONS_CFG, savedElements.size());

            for (int i = 0; i < savedElements.size(); i++) {
                final var predictionConfig =
                    predictionsConfig.addNodeSettings(PredictionFusionNodeConfig.PREDICTION_CFG + i);
                final var prediction = savedElements.get(i);

                predictionConfig.addInt("weight", prediction.m_weight);

                final String[] columns = new String[prediction.m_confidenceColumns.length];
                for (int j = 0; j < prediction.m_confidenceColumns.length; j++) {
                    columns[j] = prediction.m_confidenceColumns[j].m_selectedColumn;
                }
                predictionConfig.addStringArray("columns", columns);
            }
        }

    }

    static final class ClassItem implements NodeParameters {

        ClassItem() {
            this("");
        }

        ClassItem(final String className) {
            m_className = className;
        }

        @Widget(title = "Class name", description = "The name of the class.")
        @TextInputWidget(patternValidation = IsNotBlankValidation.class)
        @PersistArrayElement(ClassNamePersistor.class)
        String m_className;

        static final class ClassNamePersistor implements ElementFieldPersistor<String, Integer, ClassItem> {

            @Override
            public String load(final NodeSettingsRO nodeSettings, final Integer loadContext)
                throws InvalidSettingsException {
                final String[] classNames =
                    nodeSettings.getStringArray(PredictionFusionNodeConfig.CLASSES_CFG, new String[0]);
                return loadContext < classNames.length ? classNames[loadContext] : "";
            }

            @Override
            public void save(final String param, final ClassItem saveDTO) {
                saveDTO.m_className = param;
            }

            @Override
            public String[][] getConfigPaths() {
                return new String[][]{{PredictionFusionNodeConfig.CLASSES_CFG}};
            }

        }

    }

    static final class PredictionItem implements NodeParameters {

        PredictionItem() {
            this(1, new ConfidenceColumn[0]);
        }

        PredictionItem(final int weight, final ConfidenceColumn[] confidenceColumns) {
            m_weight = weight;
            m_confidenceColumns = confidenceColumns;
        }

        @Widget(title = "Weight", description = """
                Multiplies the prediction confidences as if the prediction would have been added n times. This enables
                a prediction to have more influence on the outcome than another. (Affects mean and median but not
                minimum or maximum.)
                """)
        @NumberInputWidget(minValidation = IsPositiveIntegerValidation.class)
        @PersistArrayElement(WeightPersistor.class)
        @ValueReference(WeightRef.class)
        int m_weight = 1;

        static final class WeightRef implements ParameterReference<Integer> {
        }

        @Widget(title = "Confidence columns", description = """
                Select the confidence column for each class. The columns must be compatible with double values.
                """)
        @PersistArrayElement(ConfidenceColumnsPersistor.class)
        @ArrayWidget(hasFixedSize = true)
        @ArrayWidgetInternal(titleProvider = ConfidenceColumnTitleProvider.class)
        @ValueReference(ConfidenceColumnsRef.class)
        @ValueProvider(SyncConfidenceColumnsWithClassesProvider.class)
        ConfidenceColumn[] m_confidenceColumns = new ConfidenceColumn[0];

        static final class ConfidenceColumnsRef implements ParameterReference<ConfidenceColumn[]> {
        }

        static final class ConfidenceColumnTitleProvider implements StateProvider<String> {

            private Supplier<String> m_classNameSupplier;

            @Override
            public void init(final StateProviderInitializer initializer) {
                initializer.computeOnValueChange(ConfidenceColumnsRef.class);
                m_classNameSupplier = initializer.getValueSupplier(ClassNameRef.class);
            }

            @Override
            public String computeState(final NodeParametersInput context) throws StateComputationFailureException {
                return m_classNameSupplier.get();
            }

        }

        static final class WeightPersistor implements ElementFieldPersistor<Integer, Integer, PredictionItem> {

            @Override
            public Integer load(final NodeSettingsRO nodeSettings, final Integer loadContext)
                throws InvalidSettingsException {
                try {
                    final var predictionsConfig =
                        nodeSettings.getNodeSettings(PredictionFusionNodeConfig.PREDICTIONS_CFG);
                    final var predictionConfig =
                        predictionsConfig.getNodeSettings(PredictionFusionNodeConfig.PREDICTION_CFG + loadContext);
                    return predictionConfig.getInt(PredictionFusionNodeConfig.PredictionConfig.WEIGHT_CFG, 1);
                } catch (InvalidSettingsException e) {
                    return 1;
                }
            }

            @Override
            public void save(final Integer param, final PredictionItem saveDTO) {
                saveDTO.m_weight = param;
            }

            @Override
            public String[][] getConfigPaths() {
                return new String[][]{{PredictionFusionNodeConfig.PREDICTIONS_CFG,
                    PredictionFusionNodeConfig.PREDICTION_CFG + ARRAY_INDEX_PLACEHOLDER,
                    PredictionFusionNodeConfig.PredictionConfig.WEIGHT_CFG}};
            }

        }

        static final class ConfidenceColumnsPersistor
            implements ElementFieldPersistor<ConfidenceColumn[], Integer, PredictionItem> {

            @Override
            public ConfidenceColumn[] load(final NodeSettingsRO nodeSettings, final Integer loadContext)
                throws InvalidSettingsException {
                try {
                    final var predictionsConfig =
                        nodeSettings.getNodeSettings(PredictionFusionNodeConfig.PREDICTIONS_CFG);
                    final var predictionConfig =
                        predictionsConfig.getNodeSettings(PredictionFusionNodeConfig.PREDICTION_CFG + loadContext);
                    final String[] columns = predictionConfig
                        .getStringArray(PredictionFusionNodeConfig.PredictionConfig.COLUMNS_CFG, new String[0]);

                    final String[] classes =
                        nodeSettings.getStringArray(PredictionFusionNodeConfig.CLASSES_CFG, new String[0]);

                    final ConfidenceColumn[] confidenceColumns =
                        new ConfidenceColumn[Math.min(classes.length, columns.length)];
                    for (int i = 0; i < confidenceColumns.length; i++) {
                        confidenceColumns[i] = new ConfidenceColumn(classes[i], columns[i]);
                    }
                    return confidenceColumns;
                } catch (InvalidSettingsException e) {
                    return new ConfidenceColumn[0];
                }
            }

            @Override
            public void save(final ConfidenceColumn[] param, final PredictionItem saveDTO) {
                saveDTO.m_confidenceColumns = param;
            }

            @Override
            public String[][] getConfigPaths() {
                return new String[][]{{PredictionFusionNodeConfig.PREDICTIONS_CFG,
                    PredictionFusionNodeConfig.PREDICTION_CFG + ARRAY_INDEX_PLACEHOLDER,
                    PredictionFusionNodeConfig.PredictionConfig.COLUMNS_CFG}};
            }

        }

    }

    static final class ConfidenceColumn implements NodeParameters {

        ConfidenceColumn() {
            this("", null);
        }

        ConfidenceColumn(final String className, final String selectedColumn) {
            m_className = className;
            m_selectedColumn = selectedColumn;
        }

        @Effect(predicate = AlwaysTruePredicate.class, type = EffectType.DISABLE)
        @Persistor(DoNotPersistString.class)
        @ValueReference(ClassNameRef.class)
        String m_className;

        static final class ClassNameRef implements ParameterReference<String> {
		}

        @Widget(title = "Column", description = "Select the confidence column for this class.")
        @ChoicesProvider(DoubleColumnsProvider.class)
        String m_selectedColumn;

        static final class AlwaysTruePredicate implements EffectPredicateProvider {

            @Override
            public EffectPredicate init(final PredicateInitializer i) {
                return i.getConstant(context -> true);
            }

        }

        static final class SyncConfidenceColumnsWithClassesProvider implements StateProvider<ConfidenceColumn[]> {

            Supplier<ClassItem[]> m_classesSupplier;

            Supplier<ConfidenceColumn[]> m_currentColumnsSupplier;

            @Override
            public void init(final StateProviderInitializer initializer) {
                m_classesSupplier = initializer.computeFromValueSupplier(ClassesRef.class);
                m_currentColumnsSupplier = initializer.getValueSupplier(ConfidenceColumnsRef.class);
            }

            @Override
            public ConfidenceColumn[] computeState(final NodeParametersInput parametersInput)
                throws StateComputationFailureException {
                final var classes = m_classesSupplier.get();

                if (classes == null) {
                    return new ConfidenceColumn[0];
                }

                final var currentColumns = m_currentColumnsSupplier.get();
                checkForClassItemChange(currentColumns, classes);

                final var compatibleColumns =
                    ColumnSelectionUtil.getCompatibleColumnsOfFirstPort(parametersInput, DoubleValue.class);
                final var confidenceColumnDefault =
                    compatibleColumns.isEmpty() ? null : compatibleColumns.get(compatibleColumns.size() - 1).getName();

                final var newColumns = new ConfidenceColumn[classes.length];
                for (int i = 0; i < classes.length; i++) {
                    var className = classes[i].m_className;
                    var selectedColumn = confidenceColumnDefault;

                    for (ConfidenceColumn col : currentColumns) {
                        if (col.m_className.equals(className)) {
                            selectedColumn = col.m_selectedColumn;
                            break;
                        }
                    }

                    newColumns[i] = new ConfidenceColumn(className, selectedColumn);
                }

                return newColumns;
            }

            private static void checkForClassItemChange(final ConfidenceColumn[] currentColumns,
                final ClassItem[] classes)
                throws StateComputationFailureException {
                if (currentColumns.length == classes.length) {
                    boolean allMatch = true;
                    for (int i = 0; i < classes.length; i++) {
                        if (!currentColumns[i].m_className.equals(classes[i].m_className)) {
                            allMatch = false;
                            break;
                        }
                    }
                    if (allMatch) {
                        throw new StateComputationFailureException();
                    }
                }
            }

        }

        static final class DoNotPersistString implements NodeParametersPersistor<String> {

            @Override
            public String load(final NodeSettingsRO settings) throws InvalidSettingsException {
                return null;
            }

            @Override
            public void save(final String obj, final NodeSettingsWO settings) {
                // do nothing
            }

            @Override
            public String[][] getConfigPaths() {
                return new String[0][0];
            }

        }

    }

    enum FusionMethod {

            @Label(value = "Maximum", description = "Maximum of all prediction confidences")
            MAXIMUM(Maximum.NAME), //
            @Label(value = "Mean", description = "Mean of all prediction confidences")
            MEAN(Mean.NAME), //
            @Label(value = "Median", description = "Median of all prediction confidences")
            MEDIAN(Median.NAME), //
            @Label(value = "Minimum", description = "Minimum of all prediction confidences")
            MINIMUM(Minimum.NAME);

        private final String m_value;

        FusionMethod(final String value) {
            m_value = value;
        }

        String getValue() {
            return m_value;
        }

        static FusionMethod getFromValue(final String value) throws InvalidSettingsException {
            for (final FusionMethod method : values()) {
                if (method.getValue().equals(value)) {
                    return method;
                }
            }
            throw new InvalidSettingsException(createInvalidSettingsExceptionMessage(value));
        }

        private static String createInvalidSettingsExceptionMessage(final String name) {
            var values =
                Arrays.stream(FusionMethod.values()).map(FusionMethod::getValue).collect(Collectors.joining(", "));
            return String.format("Invalid value '%s'. Possible values: %s", name, values);
        }

    }

}
