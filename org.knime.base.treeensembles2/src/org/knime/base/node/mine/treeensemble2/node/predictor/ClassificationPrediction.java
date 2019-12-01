/**
 *
 */
package org.knime.base.node.mine.treeensemble2.node.predictor;

/**
 * A prediction for a classification task.
 *
 * @author Adrian Nembach, KNIME GmbH, Konstanz, Germany
 */
public interface ClassificationPrediction extends Prediction {

    /**
     * @return the predicted class
     */
    String getClassPrediction();

    /**
     * @return the index of the winning class in the class distribution
     */
    int getWinningClassIdx();

    /**
     * @param classIdx the index of the class for which the probability is required
     * @return the probability of the class at index <b>classIdx</b> in the class distribution
     */
    double getProbability(int classIdx);
}
