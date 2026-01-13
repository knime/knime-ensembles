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
import org.knime.core.data.DoubleValue;
import org.knime.core.webui.node.dialog.defaultdialog.widget.Modification;
import org.knime.node.parameters.NodeParametersInput;
import org.knime.node.parameters.widget.OptionalWidget;
import org.knime.node.parameters.widget.choices.EnumChoice;
import org.knime.node.parameters.widget.choices.EnumChoicesProvider;
import org.knime.node.parameters.widget.choices.util.FilteredInputTableColumnsProvider;

@SuppressWarnings({"javadoc"})
public class RegressionTreeLearnerOptions extends AbstractTreeLearnerOptions {

    protected RegressionTreeLearnerOptions() {
        super();
    }

    protected RegressionTreeLearnerOptions(final NodeParametersInput input) {
        super(input);
    }

    static boolean isValidRegressionTargetColumn(final DataColumnSpec col) {
        return col.getType().isCompatible(DoubleValue.class);
    }

    @Override
    boolean isValidTargetColumn(final DataColumnSpec col) {
        return isValidRegressionTargetColumn(col);
    }

    /**
     * Adds regression-specific providers (choices are double value columns).
     *
     * @param groupModifier the widget group modifier
     */
    public static void setTargetColumnChoices(final Modification.WidgetGroupModifier groupModifier) {
        AbstractTreeLearnerOptions.setTargetColumnChoices(groupModifier, RegressionTargetAutoSelectionProvider.class,
            RegressionTargetChoicesProvider.class);
    }

    /**
     * Sets the default minimum split node size to 10.
     *
     * @param groupModifier the widget group modifier
     */
    public static void setMinSplitNodeSizeDefaultToTen(final Modification.WidgetGroupModifier groupModifier) {
        setMinSplitNodeSizeDefault(groupModifier, MinSplitNodeSizeDefaultProvider.class);
    }

    /**
     * Sets the default minimum child node size to 5.
     *
     * @param groupModifier the widget group modifier
     */
    public static void setMinChildNodeSizeDefaultToFive(final Modification.WidgetGroupModifier groupModifier) {
        MinNodeSizesParameters.setMinChildNodeSizeDefault(groupModifier, MinChildNodeSizeDefaultProvider.class);
    }

    /**
     * Sets the default minimum split node size provider.
     *
     * @param groupModifier the widget group modifier
     * @param defaultProviderClass the provider class for the default value of the minimum split node size
     */
    public static void setMinSplitNodeSizeDefault(final Modification.WidgetGroupModifier groupModifier,
        final Class<?> defaultProviderClass) {
        MinNodeSizesParameters.setMinSplitNodeSizeDefault(groupModifier, defaultProviderClass);
    }

    /**
     * EQUAL_SIZE is not supported for regression.
     *
     * @param groupModifier the widget group modifier
     */
    public static void setRowSamplingModeChoices(final Modification.WidgetGroupModifier groupModifier) {
        AbstractTreeLearnerOptions.setRowSamplingModeChoices(groupModifier, RowSamplingModeChoices.class);
    }

    /**
     * Shows the fixed root attribute widget with regression-specific providers.
     *
     * @param groupModifier the widget group modifier
     */
    public static void showFixedRootAttribute(final Modification.WidgetGroupModifier groupModifier) {
        AbstractTreeLearnerOptions.showFixedRootAttribute(groupModifier, RegressionRootColumnProvider.class,
            RegressionRootColumnDefaultProvider.class);
    }

    static class RegressionTargetAutoSelectionProvider extends TargetColumnAutoSelectionProvider {

        @Override
        boolean isValidTargetColumn(final DataColumnSpec col) {
            return isValidRegressionTargetColumn(col);
        }
    }

    static final class RegressionTargetChoicesProvider implements FilteredInputTableColumnsProvider {

        @Override
        public boolean isIncluded(final DataColumnSpec col) {
            return isValidRegressionTargetColumn(col);
        }
    }

    private static final class RegressionRootColumnProvider implements FilteredInputTableColumnsProvider {

        @Override
        public boolean isIncluded(final DataColumnSpec col) {
            return ClassificationTreeLearnerOptions.isValidClassificationTargetColumn(col);
        }
    }

    private static final class RegressionRootColumnDefaultProvider
        extends AbstractTreeLearnerOptions.RootColumnDefaultProvider {

        RegressionRootColumnDefaultProvider() {
            super(RegressionRootColumnProvider.class);
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

    private static final class RowSamplingModeChoices
        implements EnumChoicesProvider<EnumOptions.RowSamplingModeOption> {
        @Override
        public List<EnumChoice<EnumOptions.RowSamplingModeOption>> computeState(final NodeParametersInput context) {
            return List.of( //
                EnumChoice.fromEnumConst(EnumOptions.RowSamplingModeOption.RANDOM), //
                EnumChoice.fromEnumConst(EnumOptions.RowSamplingModeOption.STRATIFIED) //
            );
        }
    }
}
