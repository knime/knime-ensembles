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
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.webui.node.dialog.defaultdialog.widget.Modification;
import org.knime.node.parameters.NodeParameters;
import org.knime.node.parameters.NodeParametersInput;
import org.knime.node.parameters.layout.Layout;
import org.knime.node.parameters.persistence.NodeParametersPersistor;
import org.knime.node.parameters.updates.ParameterReference;
import org.knime.node.parameters.updates.StateProvider;
import org.knime.node.parameters.updates.ValueReference;
import org.knime.node.parameters.widget.number.NumberInputWidget;
import org.knime.node.parameters.widget.number.NumberInputWidgetValidation;

@SuppressWarnings({"MissingJavadoc", "java:S1176"})
public final class MinNodeSizesParameters implements NodeParameters {

    @Layout(AbstractTreeLearnerOptions.TreeOptionsSection.class)
    @NumberInputWidget(minValidation = NumberInputWidgetValidation.MinValidation.IsPositiveIntegerValidation.class)
    @ValueReference(MinNodeSizeRef.class)
    @Modification.WidgetReference(MinNodeSizeWidgetRef.class)
    Optional<Integer> m_minNodeSize = Optional.empty();

    @Layout(AbstractTreeLearnerOptions.TreeOptionsSection.class)
    @Modification.WidgetReference(MinChildNodeSizeRef.class)
    @NumberInputWidget(minValidation = NumberInputWidgetValidation.MinValidation.IsPositiveIntegerValidation.class,
        maxValidationProvider = MinChildSizeMaxValidationProvider.class)
    Optional<Integer> m_minChildNodeSize = Optional.empty();

    protected interface MinChildNodeSizeRef extends Modification.Reference {
    }

    public interface MinNodeSizeWidgetRef extends Modification.Reference {
    }

    private interface MinNodeSizeRef extends ParameterReference<Optional<Integer>> {

    }

    private static final class MinChildSizeMaxValidationProvider
        implements StateProvider<NumberInputWidgetValidation.MaxValidation> {

        private Supplier<Optional<Integer>> m_minNodeSizeSupplier;

        @Override
        public void init(final StateProviderInitializer initializer) {
            m_minNodeSizeSupplier = initializer.computeFromValueSupplier(MinNodeSizeRef.class);
            initializer.computeBeforeOpenDialog();
        }

        @Override
        public NumberInputWidgetValidation.MaxValidation computeState(final NodeParametersInput context) {
            var minNodeSize = m_minNodeSizeSupplier.get().orElse(null);
            if (minNodeSize == null) {
                return NoLimitMaxValidation.INSTANCE;
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

    static final class MinNodeSizesPersistor implements NodeParametersPersistor<MinNodeSizesParameters> {
        @Override
        public MinNodeSizesParameters load(final NodeSettingsRO settings) throws InvalidSettingsException {
            var params = new MinNodeSizesParameters();
            var minNodeSize = settings.getInt(TreeEnsembleLearnerConfiguration.KEY_MIN_NODE_SIZE,
                TreeEnsembleLearnerConfiguration.MIN_NODE_SIZE_UNDEFINED);
            if (minNodeSize != TreeEnsembleLearnerConfiguration.MIN_NODE_SIZE_UNDEFINED) {
                params.m_minNodeSize = Optional.of(minNodeSize);
            }
            var minChildSize = settings.getInt(TreeEnsembleLearnerConfiguration.KEY_MIN_CHILD_SIZE,
                TreeEnsembleLearnerConfiguration.MIN_CHILD_SIZE_UNDEFINED);
            if (minChildSize != TreeEnsembleLearnerConfiguration.MIN_CHILD_SIZE_UNDEFINED) {
                params.m_minChildNodeSize = Optional.of(minChildSize);
            }
            return params;
        }

        @Override
        public void save(final MinNodeSizesParameters value, final NodeSettingsWO settings) {
            var minNodeSize = value != null && value.m_minNodeSize.isPresent() ? value.m_minNodeSize.get()
                : TreeEnsembleLearnerConfiguration.MIN_NODE_SIZE_UNDEFINED;
            var minChildSize = value != null && value.m_minChildNodeSize.isPresent() ? value.m_minChildNodeSize.get()
                : TreeEnsembleLearnerConfiguration.MIN_CHILD_SIZE_UNDEFINED;

            var minNodeToPersist = minNodeSize;
            if (minChildSize != TreeEnsembleLearnerConfiguration.MIN_CHILD_SIZE_UNDEFINED
                && minNodeToPersist == TreeEnsembleLearnerConfiguration.MIN_NODE_SIZE_UNDEFINED) {
                minNodeToPersist = 2 * minChildSize;
            }

            settings.addInt(TreeEnsembleLearnerConfiguration.KEY_MIN_NODE_SIZE, minNodeToPersist);
            settings.addInt(TreeEnsembleLearnerConfiguration.KEY_MIN_CHILD_SIZE, minChildSize);
        }

        @Override
        public String[][] getConfigPaths() {
            return new String[][]{{TreeEnsembleLearnerConfiguration.KEY_MIN_NODE_SIZE},
                {TreeEnsembleLearnerConfiguration.KEY_MIN_CHILD_SIZE}};
        }
    }

    static final class NoLimitMaxValidation extends NumberInputWidgetValidation.MaxValidation {
        private static final NoLimitMaxValidation INSTANCE = new NoLimitMaxValidation();

        @Override
        protected double getMax() {
            return Double.POSITIVE_INFINITY;
        }

        @Override
        public String getErrorMessage() {
            return "";
        }
    }
}
