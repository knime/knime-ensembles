/**
 *
 */
package org.knime.base.node.mine.treeensemble2.node.predictor;

/**
 * Combines {@link RegressionPrediction} and {@link OutOfBagPrediction} into a common interface for predictions of
 * regression random forests.
 *
 * @author Adrian Nembach, KNIME GmbH, Konstanz, Germany
 */
public interface RandomForestRegressionPrediction extends RegressionPrediction, OutOfBagPrediction {

    /**
     * @return the variance of the predictions
     */
    double getVariance();
}
