package org.knime.ensembles.modeltotable;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * <code>NodeFactory</code> for the "CountryExctractor" Node.
 *
 *
 * @author Peter
 */
public class ModelToTableNodeFactory
        extends NodeFactory<ModelToTableNodeModel> {

    /**
     * {@inheritDoc}
     */
    @Override
    public ModelToTableNodeModel createNodeModel() {
        return new ModelToTableNodeModel();
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
    public NodeView<ModelToTableNodeModel> createNodeView(final int viewIndex,
            final ModelToTableNodeModel nodeModel) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasDialog() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeDialogPane createNodeDialogPane() {
        return new ModelToTableNodeDialog();
    }

}

