package org.knime.ensembles.delegating;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * <code>NodeFactory</code> for the "TimeDelayLoopEnd" Node.
 * 
 *
 * @author 
 */
public class DelegatingLoopEndNodeFactory 
        extends NodeFactory<DelegatingLoopEndNodeModel> {

    /**
     * {@inheritDoc}
     */
    @Override
    public DelegatingLoopEndNodeModel createNodeModel() {
        return new DelegatingLoopEndNodeModel();
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
    public NodeView<DelegatingLoopEndNodeModel> createNodeView(
            final int viewIndex,
            final DelegatingLoopEndNodeModel nodeModel) {
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

