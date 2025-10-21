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

import java.util.Optional;
import java.util.function.Supplier;

import org.knime.base.node.mine.treeensemble2.node.learner.TreeEnsembleLearnerConfiguration;
import org.knime.core.webui.node.dialog.defaultdialog.widget.Modification;
import org.knime.node.parameters.NodeParameters;
import org.knime.node.parameters.NodeParametersInput;
import org.knime.node.parameters.Widget;
import org.knime.node.parameters.layout.Layout;
import org.knime.node.parameters.persistence.Persistor;
import org.knime.node.parameters.updates.ParameterReference;
import org.knime.node.parameters.updates.StateProvider;
import org.knime.node.parameters.updates.ValueReference;
import org.knime.node.parameters.widget.OptionalWidget;
import org.knime.node.parameters.widget.number.NumberInputWidget;
import org.knime.node.parameters.widget.number.NumberInputWidgetValidation;

/**
 * Parameters for minimum node sizes used in tree learners. The min split node size is not always shown and when it is
 * it is restricted to being empty or twice the min child node size. We don't enforce this on apply, since the models
 * load method is capable of handling error- and auto-configure state correctly.
 */
@SuppressWarnings({"restriction"})
public final class MinNodeSizesParameters implements NodeParameters {

    @Layout(AbstractTreeLearnerOptions.TreeOptionsSection.class)
    @NumberInputWidget(minValidation = NumberInputWidgetValidation.MinValidation.IsPositiveIntegerValidation.class)
    @ValueReference(MinNodeSizeWidgetRef.class)
    @Modification.WidgetReference(MinNodeSizeWidgetRef.class)
    @Persistor(MinNodeSizePersistor.class)
    Optional<Integer> m_minNodeSize = Optional.empty();

    private interface MinNodeSizeWidgetRef extends Modification.Reference, ParameterReference<Optional<Integer>> {
    }

    private static void showMinSplitNodeSize(final Modification.WidgetGroupModifier groupModifier) {
        groupModifier.find(MinNodeSizesParameters.MinNodeSizeWidgetRef.class) //
            .addAnnotation(Widget.class) //
            .withProperty("title", "Minimum split node size") //
            .withProperty("description",
                """
                        Minimum number of records in a decision tree node so that another split
                        is attempted.
                        Note, this option does not make any implications on the minimum number of records in a
                        terminal node.
                        If enabled, this number needs to be at least twice as large as the minimum child node size
                        (as otherwise for binary splits one of the two children would have less records than specified).
                        """) //
            .modify();
    }

    @Layout(AbstractTreeLearnerOptions.TreeOptionsSection.class)
    @Modification.WidgetReference(MinChildNodeSizeRef.class)
    @NumberInputWidget(minValidation = NumberInputWidgetValidation.MinValidation.IsPositiveIntegerValidation.class,
        maxValidationProvider = MinChildSizeMaxValidationProvider.class)
    @Persistor(MinChildSizePersistor.class)
    Optional<Integer> m_minChildNodeSize = Optional.empty();

    private interface MinChildNodeSizeRef extends Modification.Reference, ParameterReference<Optional<Integer>> {
    }

    private static void showMinChildNodeSize(final Modification.WidgetGroupModifier groupModifier) {
        groupModifier.find(MinNodeSizesParameters.MinChildNodeSizeRef.class).addAnnotation(Widget.class)
            .withProperty("title", "Minimum child node size").withProperty("description", """
                    Minimum number of records allowed in the child nodes after a split.
                    Must not exceed half the minimum split node size. Note, this parameter is currently ignored for
                    nominal columns if binary nominal splits are disabled.
                    """).modify();
    }

    /**
     * Used to enable full configurability for the tree ensemble learners.
     *
     * @param groupModifier
     */
    public static void showSplitNodeSizes(final Modification.WidgetGroupModifier groupModifier) {
        showMinSplitNodeSize(groupModifier);
        showMinChildNodeSize(groupModifier);
    }

    /**
     * Used to enable only minimum child node size configurability for the Random Forest classification node.
     *
     * @param groupModifier
     */
    public static void showMinChildNodeSizeOnly(final Modification.WidgetGroupModifier groupModifier) {
        showMinChildNodeSize(groupModifier);
    }

    /**
     * Sets the default provider for minimum child node size. Package-private to allow type-specific classes to call
     * with their default provider.
     *
     * @param groupModifier the group modifier
     * @param defaultProviderClass the default provider class
     */
    static void setMinChildNodeSizeDefault(final Modification.WidgetGroupModifier groupModifier,
        final Class<?> defaultProviderClass) {
        groupModifier.find(MinChildNodeSizeRef.class).addAnnotation(OptionalWidget.class)
            .withProperty("defaultProvider", defaultProviderClass).modify();
    }

    /**
     * Sets the default provider for minimum split node size. Package-private to allow type-specific classes to call
     * with their default provider.
     *
     * @param groupModifier the group modifier
     * @param defaultProviderClass the default provider class
     */
    static void setMinSplitNodeSizeDefault(final Modification.WidgetGroupModifier groupModifier,
        final Class<?> defaultProviderClass) {
        groupModifier.find(MinNodeSizeWidgetRef.class).addAnnotation(OptionalWidget.class)
            .withProperty("defaultProvider", defaultProviderClass).modify();
    }

    private static final class MinChildSizePersistor extends Persistors.OptionalEmptyAsSpecialIntPersistor {
        MinChildSizePersistor() {
            super(TreeEnsembleLearnerConfiguration.KEY_MIN_CHILD_SIZE,
                TreeEnsembleLearnerConfiguration.MIN_CHILD_SIZE_UNDEFINED);
        }
    }

    private static final class MinNodeSizePersistor extends Persistors.OptionalEmptyAsSpecialIntPersistor {
        MinNodeSizePersistor() {
            super(TreeEnsembleLearnerConfiguration.KEY_MIN_NODE_SIZE,
                TreeEnsembleLearnerConfiguration.MIN_NODE_SIZE_UNDEFINED);
        }
    }

    private static final class MinChildSizeMaxValidationProvider
        implements StateProvider<NumberInputWidgetValidation.MaxValidation> {

        private Supplier<Optional<Integer>> m_minNodeSizeSupplier;

        @Override
        public void init(final StateProviderInitializer initializer) {
            m_minNodeSizeSupplier = initializer.computeFromValueSupplier(MinNodeSizeWidgetRef.class);
            initializer.computeBeforeOpenDialog();
        }

        @Override
        public NumberInputWidgetValidation.MaxValidation computeState(final NodeParametersInput context) {
            var minNodeSize = m_minNodeSizeSupplier.get().orElse(null);
            if (minNodeSize == null) {
                return null;
            }
            final var maxAllowed = Math.floor(minNodeSize / 2.0);
            return new NumberInputWidgetValidation.MaxValidation() {
                @Override
                protected double getMax() {
                    return maxAllowed;
                }

                @Override
                public String getErrorMessage() {
                    if (maxAllowed <= 0) {
                        return "Increase the minimum split node size so that it is at least twice "
                            + "the minimum child node size.";
                    }
                    return String.format(
                        "Minimum child node size must not exceed %d (half of the minimum split node size).",
                        (int)maxAllowed);
                }
            };
        }
    }

}
