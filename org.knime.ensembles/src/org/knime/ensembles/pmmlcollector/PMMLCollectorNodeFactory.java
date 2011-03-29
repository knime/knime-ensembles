package org.knime.ensembles.pmmlcollector;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

public class PMMLCollectorNodeFactory extends NodeFactory<PMMLCollectorNodeModel> {

	@Override
	public PMMLCollectorNodeModel createNodeModel() {
		return new PMMLCollectorNodeModel();
	}

	@Override
	protected int getNrNodeViews() {
		return 0;
	}

	@Override
	public NodeView<PMMLCollectorNodeModel> createNodeView(int viewIndex,
			PMMLCollectorNodeModel nodeModel) {
		return null;
	}

	@Override
	protected boolean hasDialog() {
		return true;
	}

	@Override
	protected NodeDialogPane createNodeDialogPane() {
		return new PMMLCollectorNodeDialog();
	}

}
