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

@SuppressWarnings({"MissingJavadoc", "java:S1176", "java:S1192"})
public final class WidgetGroupModifiers {

    private WidgetGroupModifiers() {

    }

    public static void targetColumn(final Modification.WidgetGroupModifier groupModifier) {
        groupModifier.find(References.TargetColumnWidgetRef.class) //
            .addAnnotation(Widget.class) //
            .withProperty("title", "Target column") //
            .withProperty("description", """
                    Select the column containing the value to be learned. Rows with missing values in this column are \
                    ignored during the learning process.
                    """).modify();//

    }

    public static void trainingAttributes(final Modification.WidgetGroupModifier groupModifier) {
        groupModifier.find(TrainingAttributesParameters.TrainingAttributesModeWidgetRef.class) //
            .addAnnotation(Widget.class) //
            .withProperty("title", "Training attributes") //
            .withProperty("description", """
                    Choose whether to derive attributes from a fingerprint vector column or from ordinary table \
                    columns.
                    """) //
            .modify();
    }

    public static void useFingerprintAttribute(final Modification.WidgetGroupModifier groupModifier) {
        groupModifier.find(TrainingAttributesParameters.FingerprintAttributeWidgetRef.class) //
            .addAnnotation(Widget.class) //
            .withProperty("title", "Fingerprint attribute") //
            .withProperty("description", """
                    Use a fingerprint (bit, byte, or double vector) column to learn the model. Each entry of the \
                    vector is treated as a separate attribute. All vectors must share the same length.
                    """) //
            .modify();
    }

    public static void attributeColumns(final Modification.WidgetGroupModifier groupModifier) {
        groupModifier.find(References.AttributeColumnsWidgetRef.class) //
            .addAnnotation(Widget.class) //
            .withProperty("title", "Attribute selection") //
            .withProperty("description", """
                    Select the ordinary columns that should be used as learning attributes. Use the include/exclude \
                    lists or pattern matching to manage the selection.
                    """) //
            .modify();
    }

    public static void ignoreColumnsWithoutDomainInfo(final Modification.WidgetGroupModifier groupModifier) {
        groupModifier.find(References.IgnoreColumnsWithoutDomainWidgetRef.class) //
            .addAnnotation(Widget.class) //
            .withProperty("title", "Ignore columns without domain information") //
            .withProperty("description", """
                    Ignore nominal columns that do not contain domain information, for example when the number of \
                    nominal values is very large.
                    """) //
            .modify();
    }

    @SuppressWarnings("java:S1144")
    private static void useMissingValueHandlingWidget(final Modification.WidgetGroupModifier groupModifier) {
        // currently unused but kept for consistency since all nodes share the same configuration object
        groupModifier.find(References.MissingValueHandlingWidgetRef.class) //
            .addAnnotation(Widget.class) //
            .withProperty("title", "Missing value handling") //
            .withProperty("description",
                """
                        Choose how missing attribute values are processed. XGBoost-style handling learns the best \
                        direction for missing values during training; surrogates fall back to alternative splits.
                        """) //
            .modify();
    }

    public static void useMidpointSplits(final Modification.WidgetGroupModifier groupModifier) {
        groupModifier.find(References.AverageSplitPointsWidgetRef.class) //
            .addAnnotation(Widget.class) //
            .withProperty("title", "Use mid-point splits") //
            .withProperty("description", """
                    For numerical splits, use the mid-point between two class boundaries. Otherwise the split value \
                    corresponds to the lower boundary with a ≤ comparison.
                    """) //
            .modify();
    }

    public static void useBinarySplitsForNominal(final Modification.WidgetGroupModifier groupModifier) {
        groupModifier.find(References.BinaryNominalSplitsWidgetRef.class) //
            .addAnnotation(Widget.class) //
            .withProperty("title", "Use binary splits for nominal columns") //
            .withProperty("description", """
                    Allow binary splits for nominal attributes. Disabling keeps the original multi-way splits.
                    """) //
            .modify();
    }

    public static void limitNumberOfLevels(final Modification.WidgetGroupModifier groupModifier) {
        groupModifier.find(References.MaxTreeDepthWidgetRef.class) //
            .addAnnotation(Widget.class) //
            .withProperty("title", "Limit number of levels (tree depth)") //
            .withProperty("description", """
                    Limit the maximal number of tree levels. When disabled the tree depth is unbounded.
                    """) //
            .modify();
    }

    static void minSplitNodeSize(final Modification.WidgetGroupModifier groupModifier) {
        groupModifier.find(MinNodeSizesParameters.MinNodeSizeWidgetRef.class) //
            .addAnnotation(Widget.class) //
            .withProperty("title", "Minimum split node size") //
            .withProperty("description", """
                    Minimum number of records in a node required to attempt another split.
                    """) //
            .modify();
    }

    public static void fixedRootAttribute(final Modification.WidgetGroupModifier groupModifier) {
        groupModifier.find(References.FixedRootAttributeWidgetRef.class) //
            .addAnnotation(Widget.class) //
            .withProperty("title", "Use fixed root attribute") //
            .withProperty("description", """
                    Force the selected column to be used as the root split attribute in all trees.
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

    public static void saveTargetDistribution(final Modification.WidgetGroupModifier groupModifier) {
        groupModifier.find(References.SaveTargetDistributionRef.class) //
            .addAnnotation(Widget.class) //
            .withProperty("title", "Save target distribution in tree nodes") //
            .withProperty("description", """
                            Store the distribution of the target category values in each tree node. This increases \
                             the memory footprint but is required for some downstream views and model exports.
                    """) //
            .modify();
    }

    public static void splitCriterion(final Modification.WidgetGroupModifier groupModifier) {
        groupModifier.find(References.SplitCriterionWidgetRef.class) //
            .addAnnotation(Widget.class) //
            .withProperty("title", "Split criterion") //
            .withProperty("description", """
                    Select the impurity measure used to evaluate candidate splits. Gini is the common default.
                    """) //
            .modify();
    }

    public static void hilighting(final Modification.WidgetGroupModifier groupModifier) {
        groupModifier.find(References.HiliteCountRef.class).addAnnotation(Widget.class)
            .withProperty("title", "Enable highlighting (number of patterns to store)")
            .withProperty("description",
                "If selected, the node stores the selected number of rows and allows highlighting "
                    + "them in the node view.")
            .modify();
    }

    static void minChildNodeSize(final Modification.WidgetGroupModifier groupModifier) {
        groupModifier.find(MinNodeSizesParameters.MinChildNodeSizeRef.class).addAnnotation(Widget.class)
            .withProperty("title", "Minimum child node size").withProperty("description", """
                    Minimum number of records allowed in the child nodes after a split.
                    Must not exceed half the minimum split node size.
                    """).modify();
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
