/**
 *
 */
package org.knime.base.node.mine.treeensemble2.node.predictor;

import org.knime.base.node.mine.treeensemble2.data.PredictorRecord;
import org.knime.core.data.DataRow;

/**
 * A {@link Predictor} produces {@link Prediction Predictions} for {@link DataRow DataRows}.
 *
 * @author Adrian Nembach, KNIME GmbH, Konstanz, Germany
 * @param <P> the type of prediction
 */
@FunctionalInterface
public interface Predictor<P extends Prediction> {

    /**
     * Performs a prediction for a {@link PredictorRecord}.
     *
     * @param row record to predict
     * @return the prediction for <b>predictorRecord</b>
     */
    P predict(DataRow row);
}
