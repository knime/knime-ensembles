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
 * ------------------------------------------------------------------------
 */
package org.knime.base.node.mine.treeensemble2.node.predictor.regression;

import static org.knime.node.impl.description.PortDescription.fixedPort;

import java.util.List;
import java.util.Map;

import org.knime.base.node.mine.treeensemble2.node.randomforest.predictor.TreeEnsemblePredictorOptions;
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
 * Node factory for the Tree Ensemble Predictor (Regression).
 */
@SuppressWarnings("restriction")
// TODO manual smoke test
// TODO review description strings
public final class TreeEnsembleRegressionPredictorNodeFactory
    extends NodeFactory<TreeEnsembleRegressionPredictorNodeModel>
    implements NodeDialogFactory, KaiNodeInterfaceFactory {

    private static final String NODE_NAME = "Tree Ensemble Predictor (Regression)";

    private static final String NODE_ICON = "treeensemble_predictor_regression.png";

    private static final String BASE_DESCRIPTION = """
            Applies regression from a tree ensemble model by using the mean of the individual predictions.
            """;

    private static final String SHORT_DESCRIPTION = BASE_DESCRIPTION;

    private static final String FULL_DESCRIPTION = """
            Applies regression from a tree ensemble model by using the mean of the individual predictions.
            <br/><br/>
            <strong>Change prediction column name</strong><br/>
            Check if you want to alter the name of the column that will contain the prediction.
            <br/><br/>
            <strong>Prediction column name</strong><br/>
            Name of the first output column. It contains the mean response of all models. A second column with the
            suffix "(Variance)" is appended containing the variance of all model responses.
            """;

    private static final List<PortDescription> INPUT_PORTS = List.of(
        fixedPort("Tree Ensemble Model", """
                Tree ensemble model as produced by the Tree Ensemble Learner (Regression) node.
                """),
        fixedPort("Input data", """
                Data to be predicted.
                """));

    private static final List<PortDescription> OUTPUT_PORTS = List.of(
        fixedPort("Prediction output", """
                Input data along with prediction columns.
                """));

    @Override
    public TreeEnsembleRegressionPredictorNodeModel createNodeModel() {
        return new TreeEnsembleRegressionPredictorNodeModel();
    }

    @Override
    protected int getNrNodeViews() {
        return 0;
    }

    @Override
    @SuppressWarnings({"java:S5738", "removal"})
    public NodeView<TreeEnsembleRegressionPredictorNodeModel> createNodeView(final int viewIndex,
        final TreeEnsembleRegressionPredictorNodeModel nodeModel) {
        throw new IndexOutOfBoundsException();
    }

    @Override
    @SuppressWarnings({"java:S5738", "removal"})
    protected boolean hasDialog() {
        return true;
    }

    @Override
    @SuppressWarnings({"java:S5738", "removal"})
    public NodeDialogPane createNodeDialogPane() {
        return NodeDialogManager.createLegacyFlowVariableNodeDialog(createNodeDialog());
    }

    @Override
    public NodeDialog createNodeDialog() {
        return new DefaultNodeDialog(SettingsType.MODEL, TreeEnsemblePredictorOptions.class);
    }

    @Override
    public NodeDescription createNodeDescription() {
        return DefaultNodeDescriptionUtil.createNodeDescription(NODE_NAME, NODE_ICON, INPUT_PORTS, OUTPUT_PORTS,
            SHORT_DESCRIPTION, FULL_DESCRIPTION, List.of(), TreeEnsemblePredictorOptions.class, null,
            NodeType.Predictor, List.of(), null);
    }

    @Override
    public KaiNodeInterface createKaiNodeInterface() {
        return new DefaultKaiNodeInterface(
            Map.of(SettingsType.MODEL, TreeEnsemblePredictorOptions.class));
    }
}
