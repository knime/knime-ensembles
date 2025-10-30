/*
 * ------------------------------------------------------------------------
 *
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
 * ---------------------------------------------------------------------
 *
 * History
 *   24.10.2017 (Adrian Nembach): created
 */
package org.knime.base.node.mine.treeensemble2.node.gradientboosting.predictor.pmml;

import static org.knime.base.node.mine.treeensemble2.node.randomforest.predictor.TreeEnsemblePredictorOptions.GRADIENT_BOOSTING_CITATION;
import static org.knime.base.node.mine.treeensemble2.node.randomforest.predictor.TreeEnsemblePredictorOptions.GRADIENT_BOOSTING_WIKIPEDIA;
import static org.knime.node.impl.description.PortDescription.fixedPort;

import java.util.List;
import java.util.Map;

import org.knime.base.node.mine.treeensemble2.model.MultiClassGradientBoostedTreesModel;
import org.knime.base.node.mine.treeensemble2.node.gradientboosting.predictor.classification.GradientBoostingClassificationPredictorNodeParameters;
import org.knime.base.node.mine.treeensemble2.node.gradientboosting.predictor.pmml.GradientBoostingPMMLPredictorNodeModel.Version;
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
import org.knime.node.impl.description.ExternalResource;
import org.knime.node.impl.description.PortDescription;

/**
 * NodeFactory for the PMML Gradient Boosted Trees predictor node.
 *
 * @author Adrian Nembach, KNIME GmbH, Konstanz, Germany
 * @author Benjamin Moser, KNIME GmbH, Konstanz, Germany
 * @author AI Migration Pipeline v1.1
 */
@SuppressWarnings("restriction")
public class GradientBoostingClassificationPMMLPredictorNodeFactory3
    extends NodeFactory<GradientBoostingPMMLPredictorNodeModel<MultiClassGradientBoostedTreesModel>>
    implements NodeDialogFactory, KaiNodeInterfaceFactory {

    /**
     * {@inheritDoc}
     */
    @Override
    public GradientBoostingPMMLPredictorNodeModel<MultiClassGradientBoostedTreesModel> createNodeModel() {
        return new GradientBoostingPMMLPredictorNodeModel<>(false, Version.V401);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected int getNrNodeViews() {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings( {"removal", "java:S5738"})
    public NodeView<GradientBoostingPMMLPredictorNodeModel<MultiClassGradientBoostedTreesModel>> createNodeView(
        final int viewIndex,
        final GradientBoostingPMMLPredictorNodeModel<MultiClassGradientBoostedTreesModel> nodeModel) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings( {"removal", "java:S5738"})
    protected boolean hasDialog() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    private static final String NODE_NAME = "PMML Gradient Boosted Trees Predictor";

    private static final String NODE_ICON = "../classification/GradientBoostingPredictor.png";

    private static final String SHORT_DESCRIPTION = """
            Applies classification from a Gradient Boosted Trees model that is provided in PMML format.
            """;

    private static final String FULL_DESCRIPTION = """
            Applies classification from a Gradient Boosted Trees model that is provided in PMML format. Note that it
                is currently not possible to load models that were learned on a bit-, byte- or double-vector column and
                then written to PMML because PMML does not support vector columns.
            """ + GRADIENT_BOOSTING_CITATION;

    private static final List<ExternalResource> EXTERNAL_RESOURCES = List.of(GRADIENT_BOOSTING_WIKIPEDIA);

    private static final List<PortDescription> INPUT_PORTS = List.of(fixedPort("Model", """
            Gradient Boosted Trees model in PMML format.
            """), fixedPort("Input Data", """
            The data to predict.
            """));

    private static final List<PortDescription> OUTPUT_PORTS = List.of(fixedPort("Output Data", """
            The predicted data.
            """));

    @Override
    @SuppressWarnings({"java:S5738", "removal"})
    public NodeDialogPane createNodeDialogPane() {
        return NodeDialogManager.createLegacyFlowVariableNodeDialog(createNodeDialog());
    }

    @Override
    public NodeDialog createNodeDialog() {
        return new DefaultNodeDialog(SettingsType.MODEL,
            GradientBoostingClassificationPredictorNodeParameters.class);
    }

    @Override
    public NodeDescription createNodeDescription() {
        return DefaultNodeDescriptionUtil.createNodeDescription(NODE_NAME, NODE_ICON, INPUT_PORTS, OUTPUT_PORTS,
            SHORT_DESCRIPTION, FULL_DESCRIPTION, EXTERNAL_RESOURCES,
            GradientBoostingClassificationPredictorNodeParameters.class, null, NodeType.Predictor,
            List.of(), null);
    }

    @Override
    public KaiNodeInterface createKaiNodeInterface() {
        return new DefaultKaiNodeInterface(
            Map.of(SettingsType.MODEL, GradientBoostingClassificationPredictorNodeParameters.class));
    }

}
