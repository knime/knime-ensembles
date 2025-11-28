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
package org.knime.base.node.mine.treeensemble2.node.learner.parameters;

import java.util.List;

import org.knime.base.node.mine.treeensemble2.node.learner.TreeEnsembleLearnerConfiguration;
import org.knime.core.data.vector.bitvector.BitVectorValue;
import org.knime.core.data.vector.bytevector.ByteVectorValue;
import org.knime.core.data.vector.doublevector.DoubleVectorValue;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.node.parameters.NodeParameters;
import org.knime.node.parameters.Widget;
import org.knime.node.parameters.layout.Layout;
import org.knime.node.parameters.persistence.NodeParametersPersistor;
import org.knime.node.parameters.updates.Effect;
import org.knime.node.parameters.updates.EffectPredicate;
import org.knime.node.parameters.updates.EffectPredicateProvider;
import org.knime.node.parameters.updates.ParameterReference;
import org.knime.node.parameters.updates.ValueReference;
import org.knime.node.parameters.widget.choices.ChoicesProvider;
import org.knime.node.parameters.widget.choices.Label;
import org.knime.node.parameters.widget.choices.ValueSwitchWidget;
import org.knime.node.parameters.widget.choices.util.CompatibleColumnsProvider;

final class TrainingAttributesParameters implements NodeParameters {

    @Layout(AbstractTreeLearnerOptions.AttributeSelectionSection.class)
    @ValueSwitchWidget
    @ValueReference(TrainingAttributesModeRef.class)
    @Widget(title = "Training attributes", description = """
            Choose whether to derive attributes from a fingerprint vector column or from ordinary table \
            columns.
            """)
    TrainingAttributesModeOption m_trainingAttributesMode = TrainingAttributesModeOption.COLUMNS;

    @Layout(AbstractTreeLearnerOptions.AttributeSelectionSection.class)
    @ChoicesProvider(FingerprintColumnsProvider.class)
    @Effect(predicate = FingerprintAttributesSelectedPredicate.class, type = Effect.EffectType.SHOW)
    @Widget(title = "Fingerprint attribute", description = """
            Use a fingerprint (bit, byte, or double vector) column to learn the model. Each entry of the \
            vector is treated as a separate attribute. All vectors must share the same length.
            """)
    String m_fingerprintColumn;

    enum TrainingAttributesModeOption {
            @Label(value = "Use column", description = """
                    Select ordinary columns and configure include/exclude rules for training attributes.
                    """)
            COLUMNS, //
            @Label(value = "Use fingerprint", description = """
                    Expand a fingerprint (bit, byte, or double vector) column into individual attributes during \
                    training.
                    """)
            FINGERPRINT;

    }

    interface TrainingAttributesModeRef extends ParameterReference<TrainingAttributesModeOption> {
    }

    private static final class FingerprintAttributesSelectedPredicate implements EffectPredicateProvider {
        @Override
        public EffectPredicate init(final PredicateInitializer initializer) {
            return initializer.getEnum(TrainingAttributesModeRef.class)
                .isOneOf(TrainingAttributesModeOption.FINGERPRINT);
        }
    }

    static final class Persistor implements NodeParametersPersistor<TrainingAttributesParameters> {

        @Override
        public TrainingAttributesParameters load(final NodeSettingsRO settings) throws InvalidSettingsException {
            var params = new TrainingAttributesParameters();
            params.m_fingerprintColumn =
                settings.getString(TreeEnsembleLearnerConfiguration.KEY_FINGERPRINT_COLUMN, null);
            params.m_trainingAttributesMode = params.m_fingerprintColumn == null ? TrainingAttributesModeOption.COLUMNS
                : TrainingAttributesModeOption.FINGERPRINT;
            return params;
        }

        @Override
        public void save(final TrainingAttributesParameters value, final NodeSettingsWO settings) {
            if (value.m_trainingAttributesMode == TrainingAttributesModeOption.FINGERPRINT) {
                settings.addString(TreeEnsembleLearnerConfiguration.KEY_FINGERPRINT_COLUMN, value.m_fingerprintColumn);
            } else {
                settings.addString(TreeEnsembleLearnerConfiguration.KEY_FINGERPRINT_COLUMN, null);
            }
        }

        @Override
        public String[][] getConfigPaths() {
            return new String[][]{{TreeEnsembleLearnerConfiguration.KEY_FINGERPRINT_COLUMN}};
        }
    }

    static final class FingerprintColumnsProvider extends CompatibleColumnsProvider {
        FingerprintColumnsProvider() {
            super(List.of(BitVectorValue.class, ByteVectorValue.class, DoubleVectorValue.class));
        }
    }
}
