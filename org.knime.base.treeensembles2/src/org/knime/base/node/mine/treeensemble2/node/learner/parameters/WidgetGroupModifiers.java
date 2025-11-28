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

import org.knime.core.webui.node.dialog.defaultdialog.widget.Modification;
import org.knime.node.parameters.Widget;

/**
 * Common modifies the widget group for all tree ensemble learners.
 *
 * @author Paul Bärnreuther
 */
@SuppressWarnings({"restriction"})
public final class WidgetGroupModifiers {

    private WidgetGroupModifiers() {
        // no instance
    }

    @SuppressWarnings({"unused"})
    private static void useMissingValueHandlingWidget(final Modification.WidgetGroupModifier groupModifier) {
        // currently unused but kept for consistency since all nodes share the same configuration object
        groupModifier.find(References.MissingValueHandlingWidgetRef.class) //
            .addAnnotation(Widget.class) //
            .withProperty("title", "Missing value handling") //
            .withProperty("description", """
                    Choose how missing attribute values are processed. XGBoost-style handling learns the best \
                    direction for missing values during training; surrogates fall back to alternative splits.
                    """) //
            .modify();
    }

    public static void rowSamplingFraction(final Modification.WidgetGroupModifier groupModifier) {
        groupModifier.find(References.RowSamplingFractionWidgetRef.class) //
            .addAnnotation(Widget.class) //
            .withProperty("title", "Sample training data (rows)") //
            .withProperty("description", """
                    Sample the training rows for each tree. Disable to use the full dataset for every model. Sampling \
                    with replacement corresponds to bootstrap sampling.
                    """) //
            .modify();
    }

    public static void rowSamplingWithReplacement(final Modification.WidgetGroupModifier groupModifier) {
        groupModifier.find(References.RowSamplingReplacementWidgetRef.class) //
            .addAnnotation(Widget.class) //
            .withProperty("title", "Sample with replacement") //
            .withProperty("description", """
                    Draw sampled rows with replacement (bootstrap sampling). When disabled, rows are sampled without \
                    replacement.
                    """) //
            .modify();
    }

    static void rowSamplingMode(final Modification.WidgetGroupModifier groupModifier) {
        groupModifier.find(References.RowSamplingModeWidgetRef.class) //
            .addAnnotation(Widget.class) //
            .withProperty("title", "Data sampling mode") //
            .withProperty("description", """
                    Choose how rows are sampled when data sampling is enabled.
                    """) //
            .modify();
    }

    public static void attributeSampling(final Modification.WidgetGroupModifier groupModifier) {
        groupModifier.find(References.ColumnSamplingModeWidgetRef.class) //
            .addAnnotation(Widget.class) //
            .withProperty("title", "Attribute sampling (columns)") //
            .withProperty("description", """
                    Control how many attributes are available when learning individual trees.
                    """) //
            .modify();
    }

    public static void attributeSamplingLinearFraction(final Modification.WidgetGroupModifier groupModifier) {
        groupModifier.find(References.ColumnFractionWidgetRef.class) //
            .addAnnotation(Widget.class) //
            .withProperty("title", "Linear fraction") //
            .withProperty("description", """
                    Fraction of attributes to sample for each tree.
                    """) //
            .modify();
    }

    public static void attributeSamplingAbsolute(final Modification.WidgetGroupModifier groupModifier) {
        groupModifier.find(References.ColumnAbsoluteWidgetRef.class) //
            .addAnnotation(Widget.class) //
            .withProperty("title", "Absolute number") //
            .withProperty("description", """
                    Number of attributes to sample for each tree.
                    """) //
            .modify();
    }

    public static void attributeSelectionReuse(final Modification.WidgetGroupModifier groupModifier) {
        groupModifier.find(References.AttributeReuseWidgetRef.class) //
            .addAnnotation(Widget.class) //
            .withProperty("title", "Attribute selection") //
            .withProperty("description", """
                    Choose whether to use the same set of sampled attributes for all nodes in a tree, or sample a new \
                    set for each node.
                    """) //
            .modify();
    }

    /**
     * Not shown for Random Forest Regression (for simplicity)
     *
     * @param groupModifier the group modifier
     */
    public static void hilighting(final Modification.WidgetGroupModifier groupModifier) {
        groupModifier.find(References.HiliteCountRef.class).addAnnotation(Widget.class)
            .withProperty("title", "Enable highlighting (number of patterns to store)")
            .withProperty("description",
                "If selected, the node stores the selected number of rows and allows highlighting "
                    + "them in the node view.")
            .modify();
    }

    public static void numberOfModels(final Modification.WidgetGroupModifier groupModifier) {
        groupModifier.find(References.NrModelsRef.class) //
            .addAnnotation(Widget.class) //
            .withProperty("title", "Number of models") //
            .withProperty("description", """
                    Number of decision trees to learn. Larger ensembles generally provide
                    more stable results but increase runtime.
                    """) //
            .modify();
    }

    public static void randomSeed(final Modification.WidgetGroupModifier groupModifier) {
        groupModifier.find(References.SeedRef.class) //
            .addAnnotation(Widget.class) //
            .withProperty("title", "Use static random seed") //
            .withProperty("description",
                "Provide a seed to obtain deterministic results. Leave disabled to use a time-dependent seed.") //
            .modify();
    }
}
