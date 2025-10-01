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
package org.knime.base.node.mine.treeensemble2.node.randomforest.predictor.regression;

import org.knime.base.node.mine.treeensemble2.node.predictor.TreeEnsemblePredictorConfiguration;
import org.knime.core.webui.node.dialog.defaultdialog.widget.Modification;
import org.knime.node.parameters.NodeParameters;
import org.knime.node.parameters.Widget;
import org.knime.node.parameters.migration.LoadDefaultsForAbsentFields;
import org.knime.node.parameters.persistence.Persist;
import org.knime.node.parameters.updates.Effect;
import org.knime.node.parameters.updates.ValueReference;
import org.knime.node.parameters.updates.util.BooleanReference;

/**
 * Holds the shared tree ensemble predictor options. Settings without widget annotations remain hidden in dialogs by
 * default and can be exposed selectively by subclasses adding {@link org.knime.node.parameters.Widget} annotations e.g.
 * via {@link Modification}s.
 */
@SuppressWarnings("restriction")
@LoadDefaultsForAbsentFields
class TreeEnsemblePredictorOptions implements NodeParameters {

    /** Annotator reference used when exposing {@link #m_changePredictionColumnName}. */
    public interface ChangePredictionColumnNameRef extends Modification.Reference {
    }

    /** Annotator reference used when exposing {@link #m_predictionColumnName}. */
    public interface PredictionColumnNameRef extends Modification.Reference {
    }

    /** Annotator reference used when exposing {@link #m_appendPredictionConfidence}. */
    public interface AppendPredictionConfidenceRef extends Modification.Reference {
    }

    /** Annotator reference used when exposing {@link #m_appendClassConfidences}. */
    public interface AppendClassConfidencesRef extends Modification.Reference {
    }

    /** Annotator reference used when exposing {@link #m_appendModelCount}. */
    public interface AppendModelCountRef extends Modification.Reference {
    }

    /** Annotator reference used when exposing {@link #m_suffixForClassProbabilities}. */
    public interface SuffixForClassProbabilitiesRef extends Modification.Reference {
    }

    /** Annotator reference used when exposing {@link #m_useSoftVoting}. */
    public interface UseSoftVotingRef extends Modification.Reference {
    }

    /** Boolean reference used for effects depending on {@link #m_changePredictionColumnName}. */
    public static final class ChangePredictionColumnNameEffectRef implements BooleanReference {
    }

    @Widget(title = "Change prediction column name",
        description = "Select to customize the name of the column containing the prediction.")
    @Persist(configKey = "changePredictionColumnName")
    @ValueReference(ChangePredictionColumnNameEffectRef.class)
    boolean m_changePredictionColumnName = true;

    @Widget(title = "Prediction column name", description = """
            The name of the output column containing the mean response of all models.
            A second column with the suffix "(Variance)" will be appended, containing the variance of all model
            responses.
            """)
    @Persist(configKey = "predictionColumnName")
    @Effect(predicate = ChangePredictionColumnNameEffectRef.class, type = Effect.EffectType.ENABLE)
    String m_predictionColumnName = TreeEnsemblePredictorConfiguration.getDefPredictColumnName();

    @Modification.WidgetReference(AppendPredictionConfidenceRef.class)
    @Persist(configKey = "appendPredictionConfidence")
    boolean m_appendPredictionConfidence = true;

    @Modification.WidgetReference(AppendClassConfidencesRef.class)
    @Persist(configKey = "appendClassConfidences")
    boolean m_appendClassConfidences = false;

    @Modification.WidgetReference(AppendModelCountRef.class)
    @Persist(configKey = "appendModelCount")
    boolean m_appendModelCount = false;

    @Modification.WidgetReference(SuffixForClassProbabilitiesRef.class)
    @Persist(configKey = "suffixForClassProbabilities")
    String m_suffixForClassProbabilities = "";

    @Modification.WidgetReference(UseSoftVotingRef.class)
    @Persist(configKey = "useSoftVoting")
    boolean m_useSoftVoting = false;
}
