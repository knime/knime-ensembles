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
package org.knime.base.node.mine.treeensemble2.node.randomforest.learner.classification;

import static org.knime.node.impl.description.PortDescription.fixedPort;

import java.util.List;
import java.util.Map;

import org.knime.base.node.mine.treeensemble2.node.learner.TreeEnsembleLearnerNodeView;
import org.knime.base.node.mine.treeensemble2.node.learner.classification.TreeEnsembleClassificationLearnerNodeModel;
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
 * @author Benjamin Moser, KNIME GmbH, Konstanz, Germany
 * @author AI Migration Pipeline v1.2
 */
@SuppressWarnings("restriction")
public class RandomForestClassificationLearnerNodeFactory2 extends
    NodeFactory<TreeEnsembleClassificationLearnerNodeModel> implements NodeDialogFactory, KaiNodeInterfaceFactory {

    /** {@inheritDoc} */
    @Override
    public TreeEnsembleClassificationLearnerNodeModel createNodeModel() {
        return new TreeEnsembleClassificationLearnerNodeModel(false);
    }

    /** {@inheritDoc} */
    @Override
    protected int getNrNodeViews() {
        return 1;
    }

    /** {@inheritDoc} */
    @Override
    public NodeView<TreeEnsembleClassificationLearnerNodeModel> createNodeView(final int viewIndex,
        final TreeEnsembleClassificationLearnerNodeModel nodeModel) {
        return new TreeEnsembleLearnerNodeView<>(nodeModel);
    }

    /** {@inheritDoc} */
    @Override
    protected boolean hasDialog() {
        return true;
    }

    /** {@inheritDoc} */
    private static final String NODE_NAME = "Random Forest Learner";

    private static final String NODE_ICON = "treeensemble_learner.png";

    private static final String SHORT_DESCRIPTION = """
            Learns a random forest for classification.
            """;

    private static final String FULL_DESCRIPTION = """
            <p> Learns a random forest*, which consists of a chosen number of decision trees. Each of the decision
                tree models is built with a different set of rows (records) and for each split within a tree a randomly
                chosen set of columns (describing attributes) is used. The row sets for each decision tree are created
                by bootstrapping and have the same size as the original input table. The attribute set for an individual
                split in a decision tree is determined by randomly selecting sqrt(m) attributes from the available
                attributes where m is the total number of learning columns. The attributes can also be provided as bit
                (fingerprint), byte, or double vector. The output model describes a random forest and is applied in the
                corresponding predictor node. </p> <p> This node provides a subset of the functionality of the <i>Tree
                Ensemble Learner</i> corresponding to a random forest. If you need additional functionality please check
                out the <i>Tree Ensemble Learner</i>. </p> <p> Experiments have shown the results on different datasets
                are very similar to the <a href="http://cran.r-project.org/web/packages/randomForest/">random forest
                implementation available in R</a>. </p> <p> The decision tree construction takes place in main memory
                (all data and all models are kept in memory). </p> <p> The missing value handling corresponds to the
                method described <a href="https://github.com/dmlc/xgboost/issues/21">here</a>. The basic idea is that
                for each split to try to send the missing values in every possible direction; the one yielding the best
                results (i.e. largest gain) is then used. If no missing values are present during training, the
                direction of the split that the most records are following is chosen as the direction for missing values
                during testing. </p> <p> Nominal columns are split in a binary manner. The determination of the split
                depends on the kind of problem: <ul> <li>For two-class classification problems the method described in
                section 9.4 of "Classification and Regression Trees" by Breiman et al. (1984) is used.</li> <li>For
                multi-class classification problems the method described in "Partitioning Nominal Attributes in Decision
                Trees" by Coppersmith et al. (1999) is used.</li> </ul> </p> <br /> (*) RANDOM FORESTS is a registered
                trademark of Minitab, LLC and is used with Minitab’s permission.
            """;

    private static final List<PortDescription> INPUT_PORTS = List.of(fixedPort("Input Data", """
            The data to be learned from. It must contain at least one nominal target column and either a
            fingerprint (bit-vector/byte-vector) column or another numeric or nominal column.
            """));

    private static final List<PortDescription> OUTPUT_PORTS = List.of(fixedPort("Out-of-bag Predictions", """
            The input data with the out-of-bag predictions, i.e. for each input row this is the majority vote of all
            models that did not use the row during their training. The appended columns are equivalent to the
            columns appended by the corresponding predictor node. There is one additional column <i>model count</i>,
            which contains the number of models used for the voting (number of models not using the row throughout
            learning.)
            """), fixedPort("Attribute Statistics", """
            A statistics table on the attributes used in the different trees. Each row represents one training
            attribute with these statistics: <i>#splits (level x)</i> as the number of models, which use the
            attribute as split on level <i>x</i> (with level 0 as root split); <i>#candidates (level x)</i> is the
            number of times an attribute was in the attribute sample for level <i>x</i> (in a random forest setup
            these samples differ from node to node). If no attribute sampling is used <i>#candidates</i> is the
            number of models. Note, these numbers are uncorrected, i.e. if an attribute is selected on level 0 but
            is also in the candidate set of level 1 (but will not be split on level 1 because it has been split one
            level up), the #candidate number will still count the attribute as candidate.
            """), fixedPort("Random Forest Model", """
            The trained model.
            """));

    private static final List<ViewDescription> VIEWS = List.of(new ViewDescription("Tree Views", """
            A decision tree viewer for all the trained models. Use the spinner to iterate through the different
            models.
            """));

    @Override
    @SuppressWarnings({"java:S5738", "removal"})
    public NodeDialogPane createNodeDialogPane() {
        return NodeDialogManager.createLegacyFlowVariableNodeDialog(createNodeDialog());
    }

    @Override
    public NodeDialog createNodeDialog() {
        return new DefaultNodeDialog(SettingsType.MODEL, RandomForestClassificationLearnerNodeFactory2Parameters.class);
    }

    @Override
    public NodeDescription createNodeDescription() {
        return DefaultNodeDescriptionUtil.createNodeDescription(NODE_NAME, NODE_ICON, INPUT_PORTS, OUTPUT_PORTS,
            SHORT_DESCRIPTION, FULL_DESCRIPTION, List.of(),
            RandomForestClassificationLearnerNodeFactory2Parameters.class, VIEWS, NodeType.Learner, List.of(), null);
    }

    @Override
    public KaiNodeInterface createKaiNodeInterface() {
        return new DefaultKaiNodeInterface(
            Map.of(SettingsType.MODEL, RandomForestClassificationLearnerNodeFactory2Parameters.class));
    }

}
