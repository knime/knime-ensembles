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
 *   Jan 10, 2012 (wiswedel): created
 */
package org.knime.base.node.mine.treeensemble2.node.randomforest.predictor.regression;

import static org.knime.base.node.mine.treeensemble2.node.randomforest.predictor.TreeEnsemblePredictorOptions.MINITAB_COPYRIGHT;
import static org.knime.node.impl.description.PortDescription.fixedPort;

import java.util.List;
import java.util.Map;

import org.knime.base.node.mine.treeensemble2.node.predictor.regression.TreeEnsembleRegressionPredictorNodeModel;
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

/**
 * Factory class for the Random Forest Predictor (Regression) node. Creates node model for predicting numeric values
 * using a random forest model, and provides node dialog and KAI interface implementations.
 *
 * @author Bernd Wiswedel, KNIME AG, Zurich, Switzerland
 * @author Benjamin Moser, KNIME GmbH, Konstanz, Germany
 * @author AI Migration Pipeline v1.1
 */
@SuppressWarnings("restriction")
public class RandomForestRegressionPredictorNodeFactory extends NodeFactory<TreeEnsembleRegressionPredictorNodeModel>
    implements NodeDialogFactory, KaiNodeInterfaceFactory {

    /** {@inheritDoc} */
    @Override
    public final TreeEnsembleRegressionPredictorNodeModel createNodeModel() {
        return new TreeEnsembleRegressionPredictorNodeModel();
    }

    /** {@inheritDoc} */
    @Override
    protected final int getNrNodeViews() {
        return 0;
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings({"java:S5738", "removal"})
    public final NodeView<TreeEnsembleRegressionPredictorNodeModel> createNodeView(final int viewIndex,
        final TreeEnsembleRegressionPredictorNodeModel nodeModel) {
        throw new IndexOutOfBoundsException();
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings({"java:S5738", "removal"})
    protected final boolean hasDialog() {
        return true;
    }

    /** {@inheritDoc} */
    private static final String NODE_NAME = "Random Forest Predictor (Regression)";

    private static final String NODE_ICON = "treeensemble_predictor_regression.png";

    private static final String BASE_DESCRIPTION = """
            Applies regression from a random forest model by using the mean of the individual predictions.
            """;

    private static final String SHORT_DESCRIPTION = BASE_DESCRIPTION;

    private static final String FULL_DESCRIPTION = BASE_DESCRIPTION + MINITAB_COPYRIGHT;

    private static final List<PortDescription> INPUT_PORTS = List.of(fixedPort("Model", """
            The random forest model as produced by the Random Forest Learner (Regression) node.
            """), fixedPort("Input Data", """
            The data to be predicted.
            """));

    private static final List<PortDescription> OUTPUT_PORTS = List.of(fixedPort("Prediction output", """
            The input data along with prediction columns.
            """));

    @Override
    @SuppressWarnings({"java:S5738", "removal"})
    public NodeDialogPane createNodeDialogPane() {
        return NodeDialogManager.createLegacyFlowVariableNodeDialog(createNodeDialog());
    }

    @Override
    public NodeDialog createNodeDialog() {
        return new DefaultNodeDialog(SettingsType.MODEL, RandomForestRegressionPredictorNodeParameters.class);
    }

    @Override
    public NodeDescription createNodeDescription() {
        return DefaultNodeDescriptionUtil.createNodeDescription(NODE_NAME, NODE_ICON, INPUT_PORTS, OUTPUT_PORTS,
            SHORT_DESCRIPTION, FULL_DESCRIPTION, List.of(), RandomForestRegressionPredictorNodeParameters.class, null,
            NodeType.Predictor, List.of(), null);
    }

    @Override
    public KaiNodeInterface createKaiNodeInterface() {
        return new DefaultKaiNodeInterface(
            Map.of(SettingsType.MODEL, RandomForestRegressionPredictorNodeParameters.class));
    }

}
