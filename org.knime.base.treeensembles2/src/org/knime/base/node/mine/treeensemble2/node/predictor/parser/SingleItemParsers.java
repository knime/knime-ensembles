/**
 *
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
