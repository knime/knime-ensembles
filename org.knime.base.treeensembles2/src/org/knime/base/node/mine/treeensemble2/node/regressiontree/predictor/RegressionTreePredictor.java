/**
 *
 */
package org.knime.base.node.mine.treeensemble2.node.regressiontree.predictor;

import java.util.function.Function;

import org.knime.base.node.mine.treeensemble2.data.PredictorRecord;
import org.knime.base.node.mine.treeensemble2.model.RegressionTreeModel;
import org.knime.base.node.mine.treeensemble2.node.predictor.AbstractPredictor;
import org.knime.base.node.mine.treeensemble2.node.predictor.RegressionPrediction;
import org.knime.core.data.DataRow;

/**
 * @author Adrian Nembach, KNIME GmbH, Konstanz, Germany
 */
class RegressionTreePredictor extends AbstractPredictor<RegressionPrediction> {

    private final RegressionTreeModel m_model;

    RegressionTreePredictor(final RegressionTreeModel model, final Function<DataRow, PredictorRecord> rowConverter) {
        super(rowConverter);
        m_model = model;
    }

    /* (non-Javadoc)
     * @see org.knime.base.node.mine.treeensemble2.node.predictor.Predictor#predict(org.knime.base.node.mine.treeensemble2.data.PredictorRecord)
     */
    @Override
    public RegressionPrediction predictRecord(final PredictorRecord record) {
        double prediction = m_model.getTreeModel().findMatchingNode(record).getMean();
        return () -> prediction;
    }

}
