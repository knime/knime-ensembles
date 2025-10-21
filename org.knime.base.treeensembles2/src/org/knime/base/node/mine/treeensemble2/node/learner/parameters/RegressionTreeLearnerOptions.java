package org.knime.base.node.mine.treeensemble2.node.learner.parameters;

import java.util.List;

import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DoubleValue;
import org.knime.core.webui.node.dialog.defaultdialog.widget.Modification;
import org.knime.node.parameters.NodeParametersInput;
import org.knime.node.parameters.updates.ValueProvider;
import org.knime.node.parameters.widget.OptionalWidget;
import org.knime.node.parameters.widget.choices.ChoicesProvider;
import org.knime.node.parameters.widget.choices.EnumChoice;
import org.knime.node.parameters.widget.choices.EnumChoicesProvider;

public class RegressionTreeLearnerOptions extends AbstractTreeLearnerOptions {

    protected RegressionTreeLearnerOptions() {
        super();
    }
    protected RegressionTreeLearnerOptions(final NodeParametersInput input) {
        super(input);
    }

    private static boolean isValidRegressionTargetColumn(final DataColumnSpec col) {
        return col.getType().isCompatible(DoubleValue.class);
    }

    @Override
    boolean isValidTargetColumn(final DataColumnSpec col) {
        return isValidRegressionTargetColumn(col);
    }

    public static void targetColumn(final Modification.WidgetGroupModifier groupModifier) {
        AbstractTreeLearnerOptions.targetColumn(groupModifier);
        groupModifier.find(TargetColumnWidgetRef.class) //
                .addAnnotation(ValueProvider.class) //
                .withProperty("value", RegressionTargetAutoSelectionProvider.class) //
                .modify();
        groupModifier.find(TargetColumnWidgetRef.class) //
                .addAnnotation(ChoicesProvider.class) //
                .withProperty("value", RegressionTargetChoicesProvider.class) //
                .modify();
    }

    public static void minSplitNodeSize(final Modification.WidgetGroupModifier groupModifier) {
        AbstractTreeLearnerOptions.minSplitNodeSize(groupModifier);
        groupModifier.find(MinNodeSizesParameters.MinNodeSizeWidgetRef.class) //
                .addAnnotation(OptionalWidget.class) //
                .withProperty("defaultProvider", MinSplitNodeSizeDefaultProvider.class) //
                .modify();
    }

    public static void minChildNodeSize(final Modification.WidgetGroupModifier groupModifier) {
        AbstractTreeLearnerOptions.minChildNodeSize(groupModifier);
        groupModifier.find(MinNodeSizesParameters.MinChildNodeSizeRef.class) //
                .addAnnotation(OptionalWidget.class) //
                .withProperty("defaultProvider", MinChildNodeSizeDefaultProvider.class) //
                .modify();
    }

    public static void rowSamplingMode(final Modification.WidgetGroupModifier groupModifier) {
        AbstractTreeLearnerOptions.rowSamplingMode(groupModifier);
        groupModifier.find(RowSamplingModeWidgetRef.class) //
                .addAnnotation(ChoicesProvider.class) //
                .withProperty("value", RowSamplingModeChoices.class) //
                .modify();
    }

    static class RegressionTargetAutoSelectionProvider extends TargetColumnAutoSelectionProvider {

        @Override
        boolean isValidTargetColumn(final DataColumnSpec col) {
            return isValidRegressionTargetColumn(col);
        }
    }

    static final class RegressionTargetChoicesProvider extends TargetColumnChoicesProvider {

        @Override
        boolean isValidTargetColumn(final DataColumnSpec col) {
            return isValidRegressionTargetColumn(col);
        }
    }

    private static class MinChildNodeSizeDefaultProvider implements OptionalWidget.DefaultValueProvider<Integer> {
        @Override
        public Integer computeState(final NodeParametersInput context) {
            return 5;
        }
    }

    private static class MinSplitNodeSizeDefaultProvider implements OptionalWidget.DefaultValueProvider<Integer> {
        @Override
        public Integer computeState(final NodeParametersInput context) {
            return 10;
        }
    }


    private static final class RowSamplingModeChoices implements EnumChoicesProvider<RowSamplingModeOption> {
        @Override
        public List<EnumChoice<RowSamplingModeOption>> computeState(final NodeParametersInput context) {
            return List.of( //
                    EnumChoice.fromEnumConst(RowSamplingModeOption.RANDOM), //
                    EnumChoice.fromEnumConst(RowSamplingModeOption.STRATIFIED) //
            );
        }
    }
}
