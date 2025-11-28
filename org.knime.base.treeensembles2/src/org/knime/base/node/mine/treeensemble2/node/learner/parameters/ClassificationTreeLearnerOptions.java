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

import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.NominalValue;
import org.knime.core.data.probability.nominal.NominalDistributionValue;
import org.knime.core.webui.node.dialog.defaultdialog.widget.Modification;
import org.knime.node.parameters.NodeParametersInput;
import org.knime.node.parameters.Widget;
import org.knime.node.parameters.updates.ValueProvider;
import org.knime.node.parameters.widget.OptionalWidget;
import org.knime.node.parameters.widget.choices.ChoicesProvider;
import org.knime.node.parameters.widget.choices.EnumChoice;
import org.knime.node.parameters.widget.choices.EnumChoicesProvider;

@SuppressWarnings("restriction")
public class ClassificationTreeLearnerOptions extends AbstractTreeLearnerOptions {

    protected ClassificationTreeLearnerOptions() {
        super();
    }

    protected ClassificationTreeLearnerOptions(final NodeParametersInput input) {
        super(input);
    }

    static boolean isValidClassificationTargetColumn(final DataColumnSpec col) {
        return col.getType().isCompatible(NominalValue.class)
            || col.getType().isCompatible(NominalDistributionValue.class);
    }

    @Override
    boolean isValidTargetColumn(final DataColumnSpec col) {
        return isValidClassificationTargetColumn(col);
    }

    /**
     * Choices are nominal and nominal distribution columns.
     *
     * @param groupModifier the widget group modifier
     */
    public static void targetColumn(final Modification.WidgetGroupModifier groupModifier) {
        groupModifier.find(References.TargetColumnWidgetRef.class) //
            .addAnnotation(ValueProvider.class) //
            .withProperty("value", ClassificationAutoSelectionProvider.class) //
            .modify();
        groupModifier.find(References.TargetColumnWidgetRef.class) //
            .addAnnotation(ChoicesProvider.class) //
            .withProperty("value", ClassificationTargetChoicesProvider.class) //
            .modify();
    }

    public static void saveTargetDistribution(final Modification.WidgetGroupModifier groupModifier) {
        groupModifier.find(References.SaveTargetDistributionRef.class) //
            .addAnnotation(Widget.class) //
            .withProperty("title", "Save target distribution in tree nodes") //
            .withProperty("description", """
                            Store the distribution of the target category values in each tree node. This increases \
                             the memory footprint but is required for some downstream views and model exports.
                    """) //
            .modify();
    }

    public static void splitCriterion(final Modification.WidgetGroupModifier groupModifier) {
        groupModifier.find(References.SplitCriterionWidgetRef.class) //
            .addAnnotation(Widget.class) //
            .withProperty("title", "Split criterion") //
            .withProperty("description", """
                    Select the impurity measure used to evaluate candidate splits. Gini is the common default.
                    """) //
            .modify();
    }

    public static void setMinChildNodeSizeDefaultToOne(final Modification.WidgetGroupModifier groupModifier) {
        groupModifier.find(MinNodeSizesParameters.MinChildNodeSizeRef.class) //
            .addAnnotation(OptionalWidget.class) //
            .withProperty("defaultProvider", MinChildNodeSizeDefaultProvider.class) //
            .modify();
    }

    /**
     * Only used by the classification tree learner.
     *
     * @param groupModifier the widget group modifier
     */
    public static void setMinSplitNodeSizeDefaultToTwo(final Modification.WidgetGroupModifier groupModifier) {
        groupModifier.find(MinNodeSizesParameters.MinNodeSizeWidgetRef.class) //
            .addAnnotation(OptionalWidget.class) //
            .withProperty("defaultProvider", MinSplitNodeSizeDefaultProvider.class) //
            .modify();
    }

    public static void rowSamplingMode(final Modification.WidgetGroupModifier groupModifier) {
        WidgetGroupModifiers.rowSamplingMode(groupModifier);
        groupModifier.find(References.RowSamplingModeWidgetRef.class) //
            .addAnnotation(ChoicesProvider.class) //
            .withProperty("value", RowSamplingModeChoices.class) //
            .modify();
    }

    private static final class RowSamplingModeChoices implements EnumChoicesProvider<Options.RowSamplingModeOption> {
        @Override
        public List<EnumChoice<Options.RowSamplingModeOption>> computeState(final NodeParametersInput context) {
            return List.of( //
                EnumChoice.fromEnumConst(Options.RowSamplingModeOption.RANDOM), //
                EnumChoice.fromEnumConst(Options.RowSamplingModeOption.STRATIFIED), //
                EnumChoice.fromEnumConst(Options.RowSamplingModeOption.EQUAL_SIZE) //
            );
        }
    }

    private static class ClassificationAutoSelectionProvider extends TargetColumnAutoSelectionProvider {

        @Override
        boolean isValidTargetColumn(final DataColumnSpec col) {
            return isValidClassificationTargetColumn(col);
        }
    }

    private static final class ClassificationTargetChoicesProvider extends TargetColumnChoicesProvider {

        @Override
        boolean isValidTargetColumn(final DataColumnSpec col) {
            return isValidClassificationTargetColumn(col);
        }
    }

    private static class MinChildNodeSizeDefaultProvider implements OptionalWidget.DefaultValueProvider<Integer> {
        @Override
        public Integer computeState(final NodeParametersInput context) {
            return 1;
        }
    }

    private static class MinSplitNodeSizeDefaultProvider implements OptionalWidget.DefaultValueProvider<Integer> {
        @Override
        public Integer computeState(final NodeParametersInput context) {
            return 2;
        }
    }

}
