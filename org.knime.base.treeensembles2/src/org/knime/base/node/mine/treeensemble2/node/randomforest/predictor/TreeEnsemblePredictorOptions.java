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
package org.knime.base.node.mine.treeensemble2.node.randomforest.predictor;

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
 * Shared configuration container for random-forest predictor nodes.
 * <p>
 * The class declares all persistable settings and offers helper methods that configure the corresponding widgets in
 * subclasses.
 */
@SuppressWarnings("restriction")
@LoadDefaultsForAbsentFields
public class TreeEnsemblePredictorOptions implements NodeParameters {

    /** Trademark disclaimer embedded into node descriptions referencing RANDOM FORESTS. */
    public static final String MINITAB_COPYRIGHT = """
            "Random Forests" is a registered trademark of Minitab, LLC and is used with Minitab’s
            permission.""";

    private static final String PREDICTION_COLUMN_NAME_WIDGET_TITLE = "Prediction column name";

    public static final String CHANGE_PREDICTION_COLUMN_NAME_TITLE = "Change prediction column name";

    public static final String CHANGE_PREDICTION_COLUMN_NAME_DESCRIPTION =
        "Select to customize the name of the column containing the prediction.";

    /** Reference that identifies the {@code changePredictionColumnName} widget. */
    interface ChangePredictionColumnNameRef extends Modification.Reference {
    }

    /** Reference that identifies the {@code predictionColumnName} widget. */
    interface PredictionColumnNameRef extends Modification.Reference {
    }

    /** Reference that identifies the {@code appendPredictionConfidence} widget. */
    interface AppendPredictionConfidenceRef extends Modification.Reference {
    }

    /** Reference that identifies the {@code appendClassConfidences} widget. */
    interface AppendClassConfidencesRef extends Modification.Reference {
    }

    /** Reference that identifies the (legacy) {@code appendModelCount} widget. */
    interface AppendModelCountRef extends Modification.Reference {
    }

    /** Reference that identifies the {@code suffixForClassProbabilities} widget. */
    interface SuffixForClassProbabilitiesRef extends Modification.Reference {
    }

    /** Reference that identifies the {@code useSoftVoting} widget. */
    interface UseSoftVotingRef extends Modification.Reference {
    }

    /** Boolean reference used for effects depending on {@link #m_changePredictionColumnName}. */
    public static final class ChangePredictionColumnNameEffectRef implements BooleanReference {
    }

    @Persist(configKey = "changePredictionColumnName")
    @ValueReference(ChangePredictionColumnNameEffectRef.class)
    @Modification.WidgetReference(ChangePredictionColumnNameRef.class)
    boolean m_changePredictionColumnName = true;

    @Persist(configKey = "predictionColumnName")
    @Effect(predicate = ChangePredictionColumnNameEffectRef.class, type = Effect.EffectType.ENABLE)
    @Modification.WidgetReference(PredictionColumnNameRef.class)
    String m_predictionColumnName = TreeEnsemblePredictorConfiguration.getDefPredictColumnName();

    @Modification.WidgetReference(AppendPredictionConfidenceRef.class)
    @Persist(configKey = "appendPredictionConfidence")
    boolean m_appendPredictionConfidence = true;

    @Modification.WidgetReference(AppendClassConfidencesRef.class)
    @Persist(configKey = "appendClassConfidences")
    boolean m_appendClassConfidences;

    @Modification.WidgetReference(AppendModelCountRef.class)
    @Persist(configKey = "appendModelCount")
    boolean m_appendModelCount;

    @Modification.WidgetReference(SuffixForClassProbabilitiesRef.class)
    @Persist(configKey = "suffixForClassProbabilities")
    String m_suffixForClassProbabilities = "";

    @Modification.WidgetReference(UseSoftVotingRef.class)
    @Persist(configKey = "useSoftVoting")
    boolean m_useSoftVoting;

    public static void useChangePredictionColumnName(final Modification.WidgetGroupModifier groupModifier) {
        groupModifier.find(ChangePredictionColumnNameRef.class) //
            .addAnnotation(Widget.class) //
            .withProperty("title", CHANGE_PREDICTION_COLUMN_NAME_TITLE) //
            .withProperty("description", CHANGE_PREDICTION_COLUMN_NAME_DESCRIPTION) //
            .modify();
    }

    public static void usePredictionColumnName(final Modification.WidgetGroupModifier groupModifier,
        final String description) {
        groupModifier.find(PredictionColumnNameRef.class) //
            .addAnnotation(Widget.class) //
            .withProperty("title", PREDICTION_COLUMN_NAME_WIDGET_TITLE) //
            .withProperty("description", description) //
            .modify();
    }

    /**
     * Adds the widget metadata for the {@code appendPredictionConfidence} toggle.
     *
     * @param groupModifier widget modifier accessing the target field
     */
    public static void useAppendPredictionConfidence(final Modification.WidgetGroupModifier groupModifier) {
        groupModifier.find(AppendPredictionConfidenceRef.class) //
            .addAnnotation(Widget.class) //
            .withProperty("title", "Append overall prediction confidence") //
            .withProperty("description", """
                    Adds the confidence of the predicted class; this is the maximum of all class confidence values,
                    which can also be appended individually.
                    """) //
            .modify();
    }

    /**
     * Adds the widget metadata for the {@code appendClassConfidences} toggle.
     *
     * @param groupModifier widget modifier accessing the target field
     */
    public static void useAppendClassConfidences(final Modification.WidgetGroupModifier groupModifier) {
        groupModifier.find(AppendClassConfidencesRef.class) //
            .addAnnotation(Widget.class) //
            .withProperty("title", "Append individual class probabilities") //
            .withProperty("description", """
                    Adds one column per class containing its prediction confidence: the number of trees voting for that
                    class divided by the total number of trees.
                    """) //
            .modify();
    }

    /**
     * Adds the widget metadata for the {@code suffixForClassProbabilities} field.
     *
     * @param groupModifier widget modifier accessing the target field
     */
    public static void useSuffixForClassProbabilities(final Modification.WidgetGroupModifier groupModifier) {
        groupModifier.find(SuffixForClassProbabilitiesRef.class) //
            .addAnnotation(Widget.class) //
            .withProperty("title", "Suffix for probability columns") //
            .withProperty("description", "Suffix appended to the column names containing class probabilities.") //
            .modify();
    }

    /**
     * Adds the widget metadata for the {@code useSoftVoting} toggle.
     *
     * @param groupModifier widget modifier accessing the target field
     */
    public static void useSoftVoting(final Modification.WidgetGroupModifier groupModifier) {
        groupModifier.find(UseSoftVotingRef.class) //
            .addAnnotation(Widget.class) //
            .withProperty("title", "Use soft voting") //
            .withProperty("description", """
                    Switches from hard voting (the most votes win) to soft voting, which aggregates class probabilities
                    from all trees. Requires the random forest model to store class distributions ("Save target
                    distribution in tree nodes" in the learner); enabling this without stored distributions triggers a
                    warning.
                    """) //
            .modify();
    }
}
