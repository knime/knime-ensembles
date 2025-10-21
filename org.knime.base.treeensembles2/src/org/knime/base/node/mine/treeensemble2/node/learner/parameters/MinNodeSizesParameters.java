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
                        return "Increase the minimum split node size so that it is at least twice the minimum child node size.";
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
