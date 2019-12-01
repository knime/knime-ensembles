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
 *   Aug 17, 2016 (adrian): created
 */
package org.knime.base.node.mine.treeensemble2.node.predictor.parser;

import org.knime.base.node.mine.treeensemble2.node.predictor.ClassificationPrediction;
import org.knime.base.node.mine.treeensemble2.node.predictor.OutOfBagPrediction;
import org.knime.base.node.mine.treeensemble2.node.predictor.RandomForestRegressionPrediction;
import org.knime.base.node.mine.treeensemble2.node.predictor.RegressionPrediction;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.IntCell;
import org.knime.core.data.def.StringCell;

/**
 * Static factory for {@link SingleItemParser SingleItemParsers}.
 *
 * @author Adrian Nembach, KNIME GmbH, Konstanz, Germany
 */
public final class SingleItemParsers {

    private SingleItemParsers() {
        // static factory class
    }

    /**
     * @param confidenceColName the name of the confidence column
     * @return a {@link SingleItemParser} that appends a confidence column with the provided name
     */
    public static SingleItemParser<ClassificationPrediction, DoubleCell>
        createConfidenceItemParser(final String confidenceColName) {
        return new SingleItemParser<>(confidenceColName, DoubleCell.TYPE,
            p -> new DoubleCell(p.getProbability(p.getWinningClassIdx())));
    }

    /**
     * @param predictionColumnName the prediction column name
     * @return a {@link SingleItemParser} that appends a class prediction column with the provided name
     */
    public static SingleItemParser<ClassificationPrediction, StringCell>
        createClassPredictionItemParser(final String predictionColumnName) {
        return new SingleItemParser<>(predictionColumnName, StringCell.TYPE,
            p -> new StringCell(p.getClassPrediction()));
    }

    /**
     * @param pedictionColumnName the prediction column name
     * @return a {@link SingleItemParser} that appends a regression prediction column with the provided name
     */
    public static SingleItemParser<RegressionPrediction, DoubleCell>
        createRegressionPredictionItemParser(final String pedictionColumnName) {
        return new SingleItemParser<>(pedictionColumnName, DoubleCell.TYPE, p -> new DoubleCell(p.getPrediction()));
    }

    /**
     * @return a {@link SingleItemParser} that appends a column with the model count
     */
    public static SingleItemParser<OutOfBagPrediction, IntCell> createModelCountItemParser() {
        return new SingleItemParser<>("model count", IntCell.TYPE, p -> new IntCell(p.getModelCount()));
    }

    /**
     * @param targetColumnName the name of the target column
     * @return a {@link SingleItemParser} that appends a column with the prediction variance
     */
    public static SingleItemParser<RandomForestRegressionPrediction, DoubleCell>
        createVarianceItemParser(final String targetColumnName) {
        return new SingleItemParser<>(targetColumnName + " (Prediction Variance)", DoubleCell.TYPE,
            p -> new DoubleCell(p.getVariance()));
    }

}
