/**
 *
 */
package org.knime.base.node.mine.treeensemble2.node.predictor;

/**
 * A {@link Prediction} for regression tasks i.e. the predicted value is numerical.
 *
 * @author Adrian Nembach, KNIME GmbH, Konstanz, Germany
 */
@FunctionalInterface
public interface RegressionPrediction extends Prediction {

    /**
     * @return the model's prediction
     */
    public double getPrediction();
}
