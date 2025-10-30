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
import org.knime.node.impl.description.ExternalResource;
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
@SuppressWarnings({"restriction", "java:S1192"})
@LoadDefaultsForAbsentFields
public class TreeEnsemblePredictorOptions implements NodeParameters {

    /** Trademark disclaimer embedded into node descriptions referencing RANDOM FORESTS. */
    public static final String MINITAB_COPYRIGHT = """
            <br/><br/><i>Random Forests</i> is a registered trademark of Minitab, LLC and is used with Minitab’s
            permission.""";

    /** Citation used for gradient boosting nodes */
    public static final String GRADIENT_BOOSTING_CITATION = """
            <br/><br/>
            The implementation follows the algorithms described in <i>Greedy Function Approximation: A Gradient
            Boosting Machine</i> by Jerome H. Friedman (1999).
            """;

    /** External resource used for gradient boosting nodes */
    public static final ExternalResource GRADIENT_BOOSTING_WIKIPEDIA =
        new ExternalResource("https://en.wikipedia.org/wiki/Gradient_boosting", "Wikipedia: Gradient Boosting");

    private static final String PREDICTION_COLUMN_NAME_WIDGET_TITLE = "Prediction column name";

    private static final String CHANGE_PREDICTION_COLUMN_NAME_TITLE = "Change prediction column name";

    private static final String CHANGE_PREDICTION_COLUMN_NAME_DESCRIPTION =
        "Select to customize the name of the column containing the prediction.";

    private static final String PREDICTION_COLUMN_NAME_DESCRIPTION_REGRESSION = """
            The name of the first output column, containing the mean response of all individual tree models.
            A second column with the suffix "(Variance)" stores the variance across the individual model responses.
            """;

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

    /** Boolean reference used for effects depending on {@link #m_appendClassConfidences}. */
    public static final class AppendClassConfidencesEffectRef implements BooleanReference {
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
    @ValueReference(AppendClassConfidencesEffectRef.class)
    boolean m_appendClassConfidences;

    @Modification.WidgetReference(SuffixForClassProbabilitiesRef.class)
    @Persist(configKey = "suffixForClassProbabilities")
    @Effect(predicate = AppendClassConfidencesEffectRef.class, type = Effect.EffectType.ENABLE)
    String m_suffixForClassProbabilities = "";

    @Modification.WidgetReference(AppendModelCountRef.class)
    @Persist(configKey = "appendModelCount")
    boolean m_appendModelCount;

    @Modification.WidgetReference(UseSoftVotingRef.class)
    @Persist(configKey = "useSoftVoting")
    boolean m_useSoftVoting;

    /**
     * Adds the widget metadata for the {@code changePredictionColumnName} field.
     *
     * @param groupModifier -
     */
    public static void useChangePredictionColumnName(final Modification.WidgetGroupModifier groupModifier) {
        groupModifier.find(ChangePredictionColumnNameRef.class) //
            .addAnnotation(Widget.class) //
            .withProperty("title", CHANGE_PREDICTION_COLUMN_NAME_TITLE) //
            .withProperty("description", CHANGE_PREDICTION_COLUMN_NAME_DESCRIPTION) //
            .modify();
    }

    private static void usePredictionColumnName(final Modification.WidgetGroupModifier groupModifier,
        final String description) {
        groupModifier.find(PredictionColumnNameRef.class) //
            .addAnnotation(Widget.class) //
            .withProperty("title", PREDICTION_COLUMN_NAME_WIDGET_TITLE) //
            .withProperty("description", description) //
            .modify();
    }

    /**
     * Adds the widget metadata for the {@code predictionColumnName} field.
     * <p>
     *
     * @param groupModifier -
     */
    public static void usePredictionColumnName(final Modification.WidgetGroupModifier groupModifier) {
        usePredictionColumnName(groupModifier, "Name of the output column containing the prediction.");
    }

    /**
     * Adds the widget metadata for the {@code predictionColumnName} field.
     * <p>
     * Applies only to <i>some</i> regression models.
     *
     * @param groupModifier -
     */
    public static void usePredictionColumnNameForRegression(final Modification.WidgetGroupModifier groupModifier) {
        usePredictionColumnName(groupModifier, PREDICTION_COLUMN_NAME_DESCRIPTION_REGRESSION);
    }

    /**
     * Adds the widget metadata for the {@code appendPredictionConfidence} toggle.
     *
     * @param groupModifier -
     * @param description -
     */
    public static void useAppendPredictionConfidence(final Modification.WidgetGroupModifier groupModifier,
        final String description) {
        groupModifier.find(AppendPredictionConfidenceRef.class) //
            .addAnnotation(Widget.class) //
            .withProperty("title", "Append overall prediction confidence") //
            .withProperty("description", description) //
            .modify();
    }

    /**
     * Adds the widget metadata for the {@code appendPredictionConfidence} toggle.
     * <p>
     * Applies only to classification models.
     *
     * @param groupModifier widget modifier accessing the target field
     */
    public static void useAppendPredictionConfidence(final Modification.WidgetGroupModifier groupModifier) {
        var defaultDescription = """
                Adds the confidence of the predicted class; this is the maximum of all class confidence values,
                which can also be appended individually.
                """;
        useAppendPredictionConfidence(groupModifier, defaultDescription);
    }

    /**
     * Configures the widgets responsible for appending class confidences and the matching probability suffix.
     *
     * @param groupModifier widget modifier accessing the target fields
     * @param description description shown for the class confidence toggle
     */
    public static void useAppendClassConfidencesAndSuffix(final Modification.WidgetGroupModifier groupModifier,
        final String description) {
        groupModifier.find(AppendClassConfidencesRef.class) //
            .addAnnotation(Widget.class) //
            .withProperty("title", "Append individual class probabilities") //
            .withProperty("description", description) //
            .modify();

        groupModifier.find(SuffixForClassProbabilitiesRef.class) //
            .addAnnotation(Widget.class) //
            .withProperty("title", "Suffix for probability columns") //
            .withProperty("description", "Suffix appended to the column names containing class probabilities.") //
            .modify();
    }

    /**
     * Adds the widget metadata for the {@code appendClassConfidences} toggle and the related
     * {@code suffixForClassProbabilities} field.
     * <p>
     * Applies only to classification models.
     *
     * @param groupModifier widget modifier accessing the target field
     */
    public static void useAppendClassConfidencesAndSuffix(final Modification.WidgetGroupModifier groupModifier) {
        var defaultDescription = """
                Adds one column per class containing its prediction confidence: the number of trees voting for that
                class divided by the total number of trees.
                """;
        useAppendClassConfidencesAndSuffix(groupModifier, defaultDescription);
    }

    /**
     * Adds the widget metadata for the {@code useSoftVoting} toggle.
     * <p>
     * Applies only to classification models.
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
