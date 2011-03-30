package org.knime.ensembles.tabletomodel;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * <code>NodeFactory</code> for the model to table node.
 *
 * @author Sebastian Peter, University of Konstanz, Germany
 */
public class TableToModelNodeFactory
        extends NodeFactory<TableToModelNodeModel> {

    /**
     * {@inheritDoc}
     */
    @Override
    public TableToModelNodeModel createNodeModel() {
        return new TableToModelNodeModel();
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
    public NodeView<TableToModelNodeModel> createNodeView(final int viewIndex,
            final TableToModelNodeModel nodeModel) {
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

