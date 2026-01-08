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
 *   Jan 12, 2026 (paulbaernreuther): created
 */
package org.knime.base.node.mine.treeensemble2.node.gradientboosting.learner;

import org.knime.base.node.mine.treeensemble2.node.gradientboosting.learner.GradientBoostingParameters.GradientBoostingParametersLayout;
import org.knime.base.node.mine.treeensemble2.node.learner.parameters.AbstractTreeLearnerOptions.EnsembleConfigurationSection;
import org.knime.base.node.mine.treeensemble2.node.learner.parameters.Validations.AtMostOneValidation;
import org.knime.core.webui.node.dialog.defaultdialog.widget.Modification;
import org.knime.node.parameters.NodeParameters;
import org.knime.node.parameters.Widget;
import org.knime.node.parameters.layout.After;
import org.knime.node.parameters.layout.Before;
import org.knime.node.parameters.layout.Layout;
import org.knime.node.parameters.migration.LoadDefaultsForAbsentFields;
import org.knime.node.parameters.persistence.Persist;
import org.knime.node.parameters.widget.number.NumberInputWidget;
import org.knime.node.parameters.widget.number.NumberInputWidgetValidation.MinValidation.IsNonNegativeValidation;

/**
 * Additional parameters for the Gradient Boosting Tree Learners.
 */
@LoadDefaultsForAbsentFields
@Layout(GradientBoostingParametersLayout.class)
@SuppressWarnings("restriction")
public class GradientBoostingParameters implements NodeParameters {

    @Before(EnsembleConfigurationSection.ColumnSamplingMode.class)
    @After(EnsembleConfigurationSection.NumberOfModels.class)
    interface GradientBoostingParametersLayout {
    }

    @NumberInputWidget(minValidation = IsNonNegativeValidation.class, maxValidation = AtMostOneValidation.class,
        stepSize = 0.05)
    @Persist(configKey = GradientBoostingLearnerConfiguration.KEY_LEARNINGRATE)
    @Widget(title = "Learning rate", description = """
            The learning rate influences how much influence a single model has on the ensemble result.
            Usually a value of 0.1 is a good starting point but the best learning rate also depends on the
            number of models. The more models the ensemble contains the lower the learning rate has to be.
            """)
    double m_learningRate = GradientBoostingLearnerConfiguration.DEF_LEARNINGRATE;

    @NumberInputWidget(minValidation = IsNonNegativeValidation.class, maxValidation = AtMostOneValidation.class,
        stepSize = 0.05)
    @Persist(configKey = GradientBoostingLearnerConfiguration.KEY_ALPHA_FRACTION)
    @Modification.WidgetReference(AlphaWidgetRef.class)
    double m_alpha = GradientBoostingLearnerConfiguration.DEF_ALPHA_FRACTION;

    interface AlphaWidgetRef extends Modification.Reference {
    }

    /**
     * Only used and thus only shown in regression tree learners.
     *
     * @param groupModifier The group modifier
     */
    public static void showAlpha(final Modification.WidgetGroupModifier groupModifier) {
        groupModifier.find(AlphaWidgetRef.class).addAnnotation(Widget.class).withProperty("title", "Alpha fraction")
            .withProperty("description", """
                    Alpha controls what percentage of the data will be considered as outliers. The higher Alpha
                    the smaller the fraction of outliers. If Alpha is set to 1.0, the algorithm will consider no
                    point to be an outlier. This is discouraged however because outliers can have fatal effects
                    on regression.
                    """).modify();
    }

}
