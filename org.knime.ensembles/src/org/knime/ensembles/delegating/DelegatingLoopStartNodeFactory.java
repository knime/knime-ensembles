package org.knime.ensembles.delegating;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * <code>NodeFactory</code> for the "TimeDelayLoopStart" Node.
 * 
 *
 * @author Iris Adae, University of Konstanz, Germany
 */
public class DelegatingLoopStartNodeFactory 
        extends NodeFactory<DelegatingLoopStartNodeModel> {

    /**
     * {@inheritDoc}
     */
    @Override
    public DelegatingLoopStartNodeModel createNodeModel() {
        return new DelegatingLoopStartNodeModel();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNrNodeViews() {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeView<DelegatingLoopStartNodeModel> createNodeView(
            final int viewIndex,
            final DelegatingLoopStartNodeModel nodeModel) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasDialog() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeDialogPane createNodeDialogPane() {
        return null;
    }

}

