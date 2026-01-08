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
 * ------------------------------------------------------------------------
 */

package org.knime.base.node.mine.treeensemble2.node.gradientboosting.learner.classification;

import java.util.Optional;

import org.knime.base.node.mine.treeensemble2.node.gradientboosting.learner.GradientBoostingParameters;
import org.knime.base.node.mine.treeensemble2.node.learner.parameters.AbstractTreeLearnerOptions;
import org.knime.base.node.mine.treeensemble2.node.learner.parameters.ClassificationTreeLearnerOptions;
import org.knime.base.node.mine.treeensemble2.node.learner.parameters.EnumOptions;
import org.knime.base.node.mine.treeensemble2.node.learner.parameters.Persistors;
import org.knime.core.webui.node.dialog.defaultdialog.internal.widget.PersistWithin.PersistEmbedded;
import org.knime.core.webui.node.dialog.defaultdialog.widget.Modification;
import org.knime.node.parameters.NodeParametersInput;
import org.knime.node.parameters.Widget;
import org.knime.node.parameters.layout.Layout;
import org.knime.node.parameters.migration.LoadDefaultsForAbsentFields;
import org.knime.node.parameters.persistence.Persistor;
import org.knime.node.parameters.updates.ValueReference;
import org.knime.node.parameters.widget.OptionalWidget;
import org.knime.node.parameters.widget.choices.RadioButtonsWidget;
import org.knime.node.parameters.widget.number.NumberInputWidget;
import org.knime.node.parameters.widget.number.NumberInputWidgetValidation.MinValidation.IsPositiveIntegerValidation;

/**
 * Node parameters for Gradient Boosted Trees Learner (Classification).
 *
 * @author Magnus Gohm, KNIME GmbH, Konstanz, Germany
 * @author AI Migration Pipeline v1.2
 */
@SuppressWarnings("restriction")
@LoadDefaultsForAbsentFields
@Modification(GradientBoostingClassificationLearnerNodeFactory2Parameters.WidgetModifier.class)
public final class GradientBoostingClassificationLearnerNodeFactory2Parameters
    extends ClassificationTreeLearnerOptions {

    static final class WidgetModifier implements Modification.Modifier {

        @Override
        public void modify(final Modification.WidgetGroupModifier group) {
            // attribute selection
            ClassificationTreeLearnerOptions.setTargetColumnChoices(group);

            // tree options
            AbstractTreeLearnerOptions.showUseMidpointSplits(group);
            AbstractTreeLearnerOptions.showUseBinarySplitsForNominal(group);
            AbstractTreeLearnerOptions.showMissingValueHandling(group);

            // data sampling options
            AbstractTreeLearnerOptions.showDataSamplingSectionWithoutRowSamplingMode(group);

            // ensemble configuration
            AbstractTreeLearnerOptions.showNumberOfModelsOption(group);
            AbstractTreeLearnerOptions.showAttributeSamplingLinearFraction(group);
            AbstractTreeLearnerOptions.showAttributeSamplingAbsolute(group);

            // advanced options
            AbstractTreeLearnerOptions.showRandomSeedOptions(group);
        }
    }

    GradientBoostingClassificationLearnerNodeFactory2Parameters() {
        super();
    }

    GradientBoostingClassificationLearnerNodeFactory2Parameters(final NodeParametersInput input) {
        super(input);
    }

    /**
     * Overshadows field in superclass to set default to 4.
     */
    @Layout(TreeOptionsSection.class)
    @OptionalWidget(defaultProvider = MaxTreeDepthDefaultProvider.class)
    @NumberInputWidget(minValidation = IsPositiveIntegerValidation.class)
    @Persistor(Persistors.MaxDepthPersistor.class)
    @Widget(title = "Limit number of levels (tree depth)", description = """
            Limit the maximal number of tree levels. When disabled the tree depth is unbounded.
            For instance, a value of 1 would only split the (single) root node resulting in a decision stump.
            For gradient boosted trees usually a depth in the range 4 to 10 is sufficient. Larger trees will
            quickly lead to overfitting.
            """)
    Optional<Integer> m_maxTreeDepth = Optional.of(4);

    static final class MaxTreeDepthDefaultProvider implements OptionalWidget.DefaultValueProvider<Integer> {

        @Override
        public Integer computeState(final NodeParametersInput context) {
            return 4;
        }

    }

    /**
     * Overshadows field in superclass to set default to NONE.
     */
    @Layout(EnsembleConfigurationSection.ColumnSamplingMode.class)
    @Persistor(Persistors.ColumnSamplingModePersistor.class)
    @Widget(title = "Attribute sampling (columns)", description = """
            Defines the sampling of attributes to learn an individual tree. This can either be a function based on the
            number of attributes (linear fraction or square root) or some absolute value. The latter can be used in
            conjunction with flow variables to inject some other value derived from the number of attributes (e.g.
            Breiman suggests starting with the square root of the number of attributes but also to try to double or
            half that number).
            """)
    @ValueReference(ColumnSamplingModeRef.class)
    EnumOptions.ColumnSamplingModeOption m_columnSamplingMode = EnumOptions.ColumnSamplingModeOption.NONE;

    @PersistEmbedded
    GradientBoostingParameters m_gradientBoostingParams = new GradientBoostingParameters();

    /**
     * Overshadows field in superclass to set default to SAME_FOR_TREE.
     */
    @Layout(EnsembleConfigurationSection.Other.class)
    @RadioButtonsWidget
    @Persistor(Persistors.AttributeReusePersistor.class)
    @Widget(title = "Attribute selection", description = """
            <p>
              <i>Use the same set of attributes for each tree</i>
              means that the attributes are sampled once for each tree
              and this sample is then used to construct the tree.
            </p>
            <p>
              <i>Use a different set of attributes for each tree node</i>
              samples a different set of candidate attributes in each of the tree nodes
              from which the optimal one is chosen to perform the split.
              This is the option used in random forests.
            </p>
            """)
    EnumOptions.AttributeReuseOption m_attributeReuse = EnumOptions.AttributeReuseOption.SAME_FOR_TREE;

}
