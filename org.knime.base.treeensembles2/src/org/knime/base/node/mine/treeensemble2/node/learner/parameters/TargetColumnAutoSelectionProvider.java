package org.knime.base.node.mine.treeensemble2.node.learner.parameters;

import java.util.Optional;
import java.util.function.Supplier;

import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataTableSpec;
import org.knime.node.parameters.NodeParametersInput;
import org.knime.node.parameters.updates.StateProvider;

abstract class TargetColumnAutoSelectionProvider implements StateProvider<String> {

    private Supplier<String> m_currentTargetSupplier;

    @Override
    public void init(final StateProviderInitializer initializer) {
        m_currentTargetSupplier = initializer.getValueSupplier(AbstractTreeLearnerOptions.TargetColumnValueRef.class);
        initializer.computeBeforeOpenDialog();
    }

    @Override
    public String computeState(final NodeParametersInput context) {
        var current = m_currentTargetSupplier != null ? m_currentTargetSupplier.get() : null;
        var specOpt = context.getInTableSpec(0);
        if (specOpt.isEmpty()) {
            return current;
        }
        var spec = specOpt.get();
        if (isValidSelection(current, spec)) {
            return current;
        }
        return findFallback(spec).orElse(current);
    }

    private boolean isValidSelection(final String current, final DataTableSpec spec) {
        if (current == null) {
            return false;
        }
        var candidate = spec.getColumnSpec(current);
        return candidate != null && isValidTargetColumn(candidate);
    }

    private Optional<String> findFallback(final DataTableSpec spec) {
        for (var idx = spec.getNumColumns() - 1; idx >= 0; idx--) {
            var candidate = spec.getColumnSpec(idx);
            if (isValidTargetColumn(candidate)) {
                return Optional.of(candidate.getName());
            }
        }
        return Optional.empty();
    }

    abstract boolean isValidTargetColumn(final DataColumnSpec col);


}
