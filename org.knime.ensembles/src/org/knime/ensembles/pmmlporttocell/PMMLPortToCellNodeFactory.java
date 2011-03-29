package org.knime.ensembles.pmmlporttocell;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

public class PMMLPortToCellNodeFactory extends NodeFactory<PMMLPortToCellNodeModel> {

	@Override
	public PMMLPortToCellNodeModel createNodeModel() {
		return new PMMLPortToCellNodeModel();
	}

	@Override
	protected int getNrNodeViews() {
		return 0;
	}

	@Override
	public NodeView<PMMLPortToCellNodeModel> createNodeView(int viewIndex,
			PMMLPortToCellNodeModel nodeModel) {
		return null;
	}

	@Override
	protected boolean hasDialog() {
		return false;
	}

	@Override
	protected NodeDialogPane createNodeDialogPane() {
		return null;
	}

}
