/**
 * 
 * @author Iris Adae, University of Konstanz, Germany
 */
package org.knime.ensembles.tabletopmmlport;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

public class TableToPMMLNodeFactory extends NodeFactory<TableToPMMLNodeModel> {

	@Override
	public TableToPMMLNodeModel createNodeModel() {
		return new TableToPMMLNodeModel();
	}

	@Override
	protected int getNrNodeViews() {
		return 0;
	}

	@Override
	public NodeView<TableToPMMLNodeModel> createNodeView(int viewIndex,
			TableToPMMLNodeModel nodeModel) {
		return null;
	}

	@Override
	protected boolean hasDialog() {
		return true;
	}

	@Override
	protected NodeDialogPane createNodeDialogPane() {
		return new TableToPMMLNodeDialog();
	}

}
