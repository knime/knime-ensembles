package org.knime.ensembles.pmmlporttocell;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 *
 * @author Iris Adae, University of Konstanz, Germany
 */
public class PMMLPortToCellNodeFactory extends NodeFactory<PMMLPortToCellNodeModel> {

    /** {@inheritDoc} */
	@Override
	public PMMLPortToCellNodeModel createNodeModel() {
		return new PMMLPortToCellNodeModel();
	}

	/** {@inheritDoc} */
	@Override
	protected int getNrNodeViews() {
		return 0;
	}

	/** {@inheritDoc} */
	@Override
	public NodeView<PMMLPortToCellNodeModel> createNodeView(final int viewIndex,
			final PMMLPortToCellNodeModel nodeModel) {
		return null;
	}

	/** {@inheritDoc} */
	@Override
	protected boolean hasDialog() {
		return true;
	}

	/** {@inheritDoc} */
	@Override
	protected NodeDialogPane createNodeDialogPane() {
		return new PMMLPortToCellNodeDialog();
	}

}
