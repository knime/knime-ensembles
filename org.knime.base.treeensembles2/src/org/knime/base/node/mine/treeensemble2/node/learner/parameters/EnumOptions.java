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

import org.knime.base.node.mine.treeensemble2.node.learner.TreeEnsembleLearnerConfiguration;
import org.knime.base.node.mine.treeensemble2.sample.row.RowSamplerFactory;
import org.knime.node.parameters.widget.choices.Label;

/**
 * Enumeration options for tree ensemble learners.
 *
 * @author Magnus Gohm, KNIME GmbH, Konstanz, Germany
 */
public final class EnumOptions {
    private EnumOptions() {
        // prevent instantiation
    }

    /**
     * Options for attribute reuse in tree ensembles.
     *
     * @author Magnus Gohm, KNIME GmbH, Konstanz, Germany
     */
    public enum AttributeReuseOption {
            /** Option for entire tree */
            @Label("Use same set of attributes for entire tree")
            SAME_FOR_TREE, //
            /** Option for each node */
            @Label("Use different set of attributes for each tree node")
            DIFFERENT_FOR_EACH_NODE;

    }

    enum MissingValueHandlingOption {
            @Label(value = "XGBoost", description = """
                    If this is selected (it is also the default), the learner will calculate which direction \
                    is best suited for missing values, by sending the missing values in each direction of a split. \
                    The direction that yields the best result (i.e. largest gain) is then used as default direction \
                    for missing values. This method works with both, binary and multiway splits.\
                    """)
            XGBOOST(TreeEnsembleLearnerConfiguration.MissingValueHandling.XGBoost),
            @Label(value = "Surrogate", description = """
                    This approach calculates for each split alternative splits that best approximate the best split. \
                    The method was first described in the book "Classification and Regression Trees" by \
                    Breiman et al. (1984). NOTE: This method can only be used with binary nominal splits.\
                    """)
            SURROGATE(TreeEnsembleLearnerConfiguration.MissingValueHandling.Surrogate);

        MissingValueHandlingOption(final TreeEnsembleLearnerConfiguration.MissingValueHandling delegate) {
            m_delegate = delegate;
        }

        TreeEnsembleLearnerConfiguration.MissingValueHandling toLegacy() {
            return m_delegate;
        }

        static MissingValueHandlingOption
            fromLegacy(final TreeEnsembleLearnerConfiguration.MissingValueHandling legacy) {
            return legacy == TreeEnsembleLearnerConfiguration.MissingValueHandling.Surrogate ? SURROGATE : XGBOOST;
        }

        private final TreeEnsembleLearnerConfiguration.MissingValueHandling m_delegate;
    }

    enum SplitCriterionOption {
            @Label(value = "Information Gain", description = """
                    Maximizes the information gain when creating splits. Equivalent to entropy reduction.
                    """)
            INFORMATION_GAIN(TreeEnsembleLearnerConfiguration.SplitCriterion.InformationGain),
            @Label(value = "Information Gain Ratio", description = """
                    Uses the information gain ratio to compensate for the bias towards attributes with many distinct \
                    values.
                    """)
            INFORMATION_GAIN_RATIO(TreeEnsembleLearnerConfiguration.SplitCriterion.InformationGainRatio),
            @Label(value = "Gini", description = """
                    Measures the impurity of a split based on the Gini index. Common default for classification tasks.
                    """)
            GINI(TreeEnsembleLearnerConfiguration.SplitCriterion.Gini);

        SplitCriterionOption(final TreeEnsembleLearnerConfiguration.SplitCriterion delegate) {
            m_delegate = delegate;
        }

        TreeEnsembleLearnerConfiguration.SplitCriterion toLegacy() {
            return m_delegate;
        }

        static SplitCriterionOption fromLegacy(final TreeEnsembleLearnerConfiguration.SplitCriterion legacy) {
            return switch (legacy) {
                case InformationGain -> INFORMATION_GAIN;
                case InformationGainRatio -> INFORMATION_GAIN_RATIO;
                default -> GINI;
            };
        }

        private final TreeEnsembleLearnerConfiguration.SplitCriterion m_delegate;
    }

    enum RowSamplingModeOption {
            @Label(value = "Random", description = """
                    Sample rows uniformly at random.
                    """)
            RANDOM(RowSamplerFactory.RowSamplingMode.Random), //
            @Label(value = "Stratified", description = """
                    Sample rows stratified by the target class distribution.
                    """)
            STRATIFIED(RowSamplerFactory.RowSamplingMode.Stratified), //
            @Label(value = "Equal size", description = """
                    Sample the same number of rows for each class. Available for classification tasks.
                    """)
            EQUAL_SIZE(RowSamplerFactory.RowSamplingMode.EqualSize);

        RowSamplingModeOption(final RowSamplerFactory.RowSamplingMode delegate) {
            m_delegate = delegate;
        }

        RowSamplerFactory.RowSamplingMode toLegacy() {
            return m_delegate;
        }

        static RowSamplingModeOption fromLegacy(final RowSamplerFactory.RowSamplingMode legacy) {
            for (var option : values()) {
                if (option.m_delegate == legacy) {
                    return option;
                }
            }
            return RANDOM;
        }

        private final RowSamplerFactory.RowSamplingMode m_delegate;
    }

    /**
     * Options for column sampling in tree ensembles.
     *
     * @author Magnus Gohm, KNIME GmbH, Konstanz, Germany
     */
    public enum ColumnSamplingModeOption {
            /** Uses all columns */
            @Label(value = "All columns", description = """
                    Disable column sampling and use all available attributes for every tree.
                    """)
            NONE(TreeEnsembleLearnerConfiguration.ColumnSamplingMode.None),
            /** Sample the square root of the number of available attributes for each tree */
            @Label(value = "Square root", description = """
                    Sample the square root of the number of available attributes for each tree (default random forest \
                    behaviour).
                    """)
            SQUARE_ROOT(TreeEnsembleLearnerConfiguration.ColumnSamplingMode.SquareRoot),
            /** Sample a fraction of the available attributes for each tree */
            @Label(value = "Linear fraction", description = """
                    Sample a fraction of the available attributes for each tree.
                    """)
            LINEAR(TreeEnsembleLearnerConfiguration.ColumnSamplingMode.Linear),
            /** Sample a fixed number of attributes for each tree */
            @Label(value = "Absolute number", description = """
                    Sample a fixed number of attributes for each tree.
                    """)
            ABSOLUTE(TreeEnsembleLearnerConfiguration.ColumnSamplingMode.Absolute);

        ColumnSamplingModeOption(final TreeEnsembleLearnerConfiguration.ColumnSamplingMode delegate) {
            m_delegate = delegate;
        }

        TreeEnsembleLearnerConfiguration.ColumnSamplingMode toLegacy() {
            return m_delegate;
        }

        static ColumnSamplingModeOption fromLegacy(final TreeEnsembleLearnerConfiguration.ColumnSamplingMode legacy) {
            return switch (legacy) {
                case Linear -> LINEAR;
                case Absolute -> ABSOLUTE;
                case None -> NONE;
                default -> SQUARE_ROOT;
            };
        }

        private final TreeEnsembleLearnerConfiguration.ColumnSamplingMode m_delegate;
    }
}
