package org.knime.base.node.mine.treeensemble2.node.learner.parameters;

import java.util.List;

import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.NominalValue;
import org.knime.core.data.probability.nominal.NominalDistributionValue;
import org.knime.core.webui.node.dialog.defaultdialog.widget.Modification;
import org.knime.node.parameters.NodeParametersInput;
import org.knime.node.parameters.updates.ValueProvider;
import org.knime.node.parameters.widget.OptionalWidget;
import org.knime.node.parameters.widget.choices.ChoicesProvider;
import org.knime.node.parameters.widget.choices.EnumChoice;
import org.knime.node.parameters.widget.choices.EnumChoicesProvider;

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

    public static void targetColumn(final Modification.WidgetGroupModifier groupModifier) {
        AbstractTreeLearnerOptions.targetColumn(groupModifier);
        groupModifier.find(TargetColumnWidgetRef.class) //
            .addAnnotation(ValueProvider.class) //
            .withProperty("value", ClassificationAutoSelectionProvider.class) //
            .modify();
        groupModifier.find(TargetColumnWidgetRef.class) //
            .addAnnotation(ChoicesProvider.class) //
            .withProperty("value", ClassificationTargetChoicesProvider.class) //
            .modify();
    }

    public static void minChildNodeSize(final Modification.WidgetGroupModifier groupModifier) {
        AbstractTreeLearnerOptions.minChildNodeSize(groupModifier);
        groupModifier.find(MinNodeSizesParameters.MinChildNodeSizeRef.class) //
            .addAnnotation(OptionalWidget.class) //
            .withProperty("defaultProvider", MinChildNodeSizeDefaultProvider.class) //
            .modify();
    }

    public static void minSplitNodeSize(final Modification.WidgetGroupModifier groupModifier) {
        AbstractTreeLearnerOptions.minSplitNodeSize(groupModifier);
        groupModifier.find(MinNodeSizesParameters.MinNodeSizeWidgetRef.class) //
            .addAnnotation(OptionalWidget.class) //
            .withProperty("defaultProvider", MinSplitNodeSizeDefaultProvider.class) //
            .modify();
    }

    public static void rowSamplingMode(final Modification.WidgetGroupModifier groupModifier) {
        AbstractTreeLearnerOptions.rowSamplingMode(groupModifier);
        groupModifier.find(RowSamplingModeWidgetRef.class) //
            .addAnnotation(ChoicesProvider.class) //
            .withProperty("value", RowSamplingModeChoices.class) //
            .modify();
    }

    private static final class RowSamplingModeChoices implements EnumChoicesProvider<RowSamplingModeOption> {
        @Override
        public List<EnumChoice<RowSamplingModeOption>> computeState(final NodeParametersInput context) {
            return List.of( //
                EnumChoice.fromEnumConst(RowSamplingModeOption.RANDOM), //
                EnumChoice.fromEnumConst(RowSamplingModeOption.STRATIFIED), //
                EnumChoice.fromEnumConst(RowSamplingModeOption.EQUAL_SIZE) //
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
