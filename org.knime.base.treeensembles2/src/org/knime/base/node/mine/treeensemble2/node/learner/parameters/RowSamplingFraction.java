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
 *   Dec 1, 2025 (paulbaernreuther): created
 */
package org.knime.base.node.mine.treeensemble2.node.learner.parameters;

import org.knime.base.node.mine.treeensemble2.node.learner.TreeEnsembleLearnerConfiguration;
import org.knime.base.node.mine.treeensemble2.node.learner.parameters.Validations.AtMostOneValidation;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.webui.node.dialog.defaultdialog.widget.Modification;
import org.knime.node.parameters.NodeParameters;
import org.knime.node.parameters.Widget;
import org.knime.node.parameters.persistence.NodeParametersPersistor;
import org.knime.node.parameters.updates.Effect;
import org.knime.node.parameters.updates.Effect.EffectType;
import org.knime.node.parameters.updates.ValueReference;
import org.knime.node.parameters.updates.util.BooleanReference;
import org.knime.node.parameters.widget.number.NumberInputWidget;
import org.knime.node.parameters.widget.number.NumberInputWidgetValidation.MinValidation.IsPositiveDoubleValidation;

/**
 * Parameters for row sampling fraction. We don't use an Optional<Double> although these settings in themselves are
 * equivalent to that, since other settings depend in it and it makes designing the titles and descriptions easier.
 *
 * @author Paul Baernreuther
 */
@SuppressWarnings("restriction")
final class RowSamplingFraction implements NodeParameters {

    @Modification.WidgetReference(EnableRowSamplingWidgetRef.class)
    @ValueReference(EnableRowSamplingWidgetRef.class)
    boolean m_enabled;

    static final class EnableRowSamplingWidgetRef implements BooleanReference, Modification.Reference {
    }

    static void showEnableRowSampling(final Modification.WidgetGroupModifier groupModifier) {
        groupModifier.find(EnableRowSamplingWidgetRef.class).addAnnotation(Widget.class)
            .withProperty("title", "Enable row sampling").withProperty("description", """
                    Enable sampling of training rows for each individual tree. \
                    If disabled, each tree learner gets the full dataset, otherwise \
                    each tree is learned with a different data sample. \
                     A data fraction of 1 (=100%) chosen "with replacement" is called bootstrapping. \
                     For sufficiently large datasets this bootstrap sample contains about 2/3 \
                     different data rows from the input, some of which are replicated multiple times. \
                     Rows that are not used in the training of a tree are called out-of-bag. \
                            """).modify();
    }

    @NumberInputWidget(minValidation = IsPositiveDoubleValidation.class, maxValidation = AtMostOneValidation.class)
    @Modification.WidgetReference(RowSamplingFractionWidgetRef.class)
    @Effect(predicate = EnableRowSamplingWidgetRef.class, type = EffectType.SHOW)
    double m_fraction = 1.;

    private interface RowSamplingFractionWidgetRef extends Modification.Reference {
    }

    /**
     * Only used by Tree Ensemble nodes and Random Forest Classification (not Random Forest Regression).
     *
     * @param groupModifier the group modifier
     */
    static void showRowSamplingFraction(final Modification.WidgetGroupModifier groupModifier) {
        groupModifier.find(RowSamplingFractionWidgetRef.class).addAnnotation(Widget.class)
            .withProperty("title", "Fraction of data to learn single model").withProperty("description", """
                    Fraction of data rows to sample for learning each individual tree. \
                    A value of 1 means 100% of the data rows are sampled. \
                    """).modify();
    }

    RowSamplingFraction() {
        // empty
    }

    private RowSamplingFraction(final boolean enabled, final double fraction) {
        m_enabled = enabled;
        m_fraction = fraction;
    }

    static RowSamplingFraction empty() {
        return new RowSamplingFraction();
    }

    static RowSamplingFraction of(final double fraction) {
        return new RowSamplingFraction(true, fraction);
    }

    double orElse(final double other) {
        return m_enabled ? m_fraction : other;
    }

    static final class Persistor implements NodeParametersPersistor<RowSamplingFraction> {
        @Override
        public RowSamplingFraction load(final NodeSettingsRO settings) throws InvalidSettingsException {
            var fraction = settings.getDouble(TreeEnsembleLearnerConfiguration.KEY_DATA_FRACTION,
                TreeEnsembleLearnerConfiguration.DEF_DATA_FRACTION);
            return fraction < 1.0 ? RowSamplingFraction.of(fraction) : RowSamplingFraction.empty();
        }

        @Override
        public void save(final RowSamplingFraction value, final NodeSettingsWO settings) {
            settings.addDouble(TreeEnsembleLearnerConfiguration.KEY_DATA_FRACTION, value.orElse(1.0));
        }

        @Override
        public String[][] getConfigPaths() {
            return new String[][]{{TreeEnsembleLearnerConfiguration.KEY_DATA_FRACTION}};
        }
    }

}
