/*
 * ------------------------------------------------------------------------
 *  Copyright by KNIME AG, Zurich, Switzerland
 *  Website: http://www.knime.com; Email: contact@knime.com
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License, Version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>.
 *
 *  Additional permission under GNU GPL version 3 section 7:
 *
 *  KNIME interoperates with ECLIPSE solely via ECLIPSE's plug-in APIs.
 *  Hence, KNIME and ECLIPSE are both independent programs and are not
 *  derived from each other. Should, however, the interpretation of the
 *  GNU GPL Version 3 ("License") under any applicable laws result in
 *  KNIME and ECLIPSE being a combined program, KNIME AG herewith grants
 *  you the additional permission to use and propagate KNIME together with
 *  ECLIPSE with only the license terms in place for ECLIPSE applying to
 *  ECLIPSE and the GNU GPL Version 3 applying for KNIME, provided the
 *  license terms of ECLIPSE themselves allow for the respective use and
 *  propagation of ECLIPSE together with KNIME.
 *
 *  Additional permission relating to nodes for KNIME that extend the Node
 *  Extension (and in particular that are based on subclasses of NodeModel,
 *  NodeDialog, and NodeView) and that only interoperate with KNIME through
 *  standard APIs ("Nodes"):
 *  Nodes are deemed to be separate and independent programs and to not be
 *  covered works.  Notwithstanding anything to the contrary in the
 *  License, the License does not apply to Nodes, you are not required to
 *  license Nodes under the License, and you are granted a license to
 *  prepare and propagate Nodes, in each case even if such Nodes are
 *  propagated with or for interoperation with KNIME.  The owner of a Node
 *  may freely choose the license terms applicable to such Node, including
 *  when such Node is propagated with or for interoperation with KNIME.
 * ------------------------------------------------------------------------
 *
 * History
 *   Dec 25, 2011 (wiswedel): created
 */
package org.knime.base.node.mine.treeensemble2.node.regressiontree.learner;

import static org.knime.node.impl.description.PortDescription.fixedPort;

import java.util.List;
import java.util.Map;

import org.knime.core.node.NodeDescription;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;
import org.knime.core.webui.node.dialog.NodeDialog;
import org.knime.core.webui.node.dialog.NodeDialogFactory;
import org.knime.core.webui.node.dialog.NodeDialogManager;
import org.knime.core.webui.node.dialog.SettingsType;
import org.knime.core.webui.node.dialog.defaultdialog.DefaultKaiNodeInterface;
import org.knime.core.webui.node.dialog.defaultdialog.DefaultNodeDialog;
import org.knime.core.webui.node.dialog.kai.KaiNodeInterface;
import org.knime.core.webui.node.dialog.kai.KaiNodeInterfaceFactory;
import org.knime.node.impl.description.DefaultNodeDescriptionUtil;
import org.knime.node.impl.description.PortDescription;
import org.knime.node.impl.description.ViewDescription;

/**
 *
 * @author Bernd Wiswedel, KNIME AG, Zurich, Switzerland
 * @author Magnus Gohm, KNIME GmbH, Konstanz, Germany
 * @author AI Migration Pipeline v1.2
 */
@SuppressWarnings("restriction")
public final class RegressionTreeLearnerNodeFactory extends NodeFactory<RegressionTreeLearnerNodeModel>
    implements NodeDialogFactory, KaiNodeInterfaceFactory {

    @Override
    public RegressionTreeLearnerNodeModel createNodeModel() {
        return new RegressionTreeLearnerNodeModel();
    }

    @Override
    protected int getNrNodeViews() {
        return 1;
    }

    @Override
    public NodeView<RegressionTreeLearnerNodeModel> createNodeView(final int viewIndex,
        final RegressionTreeLearnerNodeModel nodeModel) {
        if (viewIndex != 0) {
            throw new IllegalArgumentException("There exists only one view with index 0.");
        }
        return new RegressionTreeLearnerNodeView(nodeModel);
    }

    @Override
    protected boolean hasDialog() {
        return true;
    }

    private static final String NODE_NAME = "Simple Regression Tree Learner";

    private static final String NODE_ICON = "Simple_Regression_Tree_Learner.png";

    private static final String SHORT_DESCRIPTION = """
            Learns a single regression tree.
            """;

    private static final String FULL_DESCRIPTION = """
            <p> Learns a single regression tree. The procedure follows the algorithm described by "Classification
                and Regression Trees" (Breiman et al, 1984), whereby the current implementation applies a couple of
                simplifications, e.g. no pruning, not necessarily binary trees, etc. </p> <p> In a regression tree the
                predicted value for a leaf node is the mean target value of the records within the leaf. Hence the
                predictions are best (with respect to the training data) if the variance of target values within a leaf
                is minimal. This is achieved by splits that minimize the sum of squared errors in their respective
                children. </p> <p> The currently used missing value handling also differs from the one used by Breiman
                et al, 1984. In each split the algorithm tries to find the best direction for missing values by sending
                them in each direction and selecting the one that yields the best result (i.e. largest gain). The
                procedure is adapted from the well known XGBoost algorithm and is described <a
                href="https://github.com/dmlc/xgboost/issues/21">here</a> . </p>
            """;

    private static final List<PortDescription> INPUT_PORTS = List.of(
            fixedPort("Input Data", """
                The data to learn from. It must contain at least one numeric target column and either a fingerprint
                (bit/byte/double-vector) column or another numeric or nominal column.
                """)
    );

    private static final List<PortDescription> OUTPUT_PORTS = List.of(
            fixedPort("Regression Tree Model", """
                The trained model.
                """)
    );

    private static final List<ViewDescription> VIEWS = List.of(
            new ViewDescription("Regression Tree View", """
                Regression Tree View
                """)
    );

    /**
     * {@inheritDoc}
     * @since 5.10
     */
    @Override
    public NodeDialogPane createNodeDialogPane() {
        return NodeDialogManager.createLegacyFlowVariableNodeDialog(createNodeDialog());
    }

    @Override
    public NodeDialog createNodeDialog() {
        return new DefaultNodeDialog(SettingsType.MODEL, RegressionTreeLearnerNodeParameters.class);
    }

    @Override
    public NodeDescription createNodeDescription() {
        return DefaultNodeDescriptionUtil.createNodeDescription( //
            NODE_NAME, //
            NODE_ICON, //
            INPUT_PORTS, //
            OUTPUT_PORTS, //
            SHORT_DESCRIPTION, //
            FULL_DESCRIPTION, //
            List.of(), //
            RegressionTreeLearnerNodeParameters.class, //
            VIEWS, //
            NodeType.Learner, //
            List.of(), //
            null //
        );
    }

    /**
     * {@inheritDoc}
     * @since 5.10
     */
    @Override
    public KaiNodeInterface createKaiNodeInterface() {
        return new DefaultKaiNodeInterface(Map.of(SettingsType.MODEL, RegressionTreeLearnerNodeParameters.class));
    }

}
