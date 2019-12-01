/**
 *
 */
package org.knime.base.node.mine.treeensemble2.node.predictor;

/**
 * Combines {@link ClassificationPrediction} and {@link OutOfBagPrediction} into a common interface for predictions of a
 * classification random forest.
 *
 * @author Adrian Nembach, KNIME GmbH, Konstanz, Germany
 */
public interface RandomForestClassificationPrediction extends ClassificationPrediction, OutOfBagPrediction {
    // marker interface
}
