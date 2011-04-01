package org.knime.ensembles.pmmlpredict;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 *
 * @author Iris Adae, University of Konstanz, Germany
 */
public class PMMLPredictorNodeFactory extends NodeFactory<PMMLPredictorNodeModel> {

    /** {@inheritDoc} */
	@Override
	public PMMLPredictorNodeModel createNodeModel() {
		return new PMMLPredictorNodeModel();
	}

	/** {@inheritDoc} */
	@Override
	protected int getNrNodeViews() {
		return 0;
	}

	/** {@inheritDoc} */
	@Override
	public NodeView<PMMLPredictorNodeModel> createNodeView(final int viewIndex,
			final PMMLPredictorNodeModel nodeModel) {
		return null;
	}

	/** {@inheritDoc} */
	@Override
	protected boolean hasDialog() {
		return false;
	}

	/** {@inheritDoc} */
	@Override
	protected NodeDialogPane createNodeDialogPane() {
		return null;
	}

}
