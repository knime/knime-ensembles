/**
 *
 */
package org.knime.base.node.mine.treeensemble2.node.predictor.classification;

import org.knime.base.node.mine.treeensemble2.data.PredictorRecord;
import org.knime.base.node.mine.treeensemble2.data.TreeTargetColumnData;
import org.knime.base.node.mine.treeensemble2.model.TreeEnsembleModel;
import org.knime.base.node.mine.treeensemble2.model.TreeEnsembleModelPortObjectSpec;
import org.knime.base.node.mine.treeensemble2.model.TreeModelClassification;
import org.knime.base.node.mine.treeensemble2.model.TreeNodeClassification;
import org.knime.base.node.mine.treeensemble2.node.predictor.AbstractRandomForestPredictor;
import org.knime.base.node.mine.treeensemble2.node.predictor.RandomForestClassificationPrediction;
import org.knime.base.node.mine.treeensemble2.sample.row.RowSample;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.RowKey;
import org.knime.core.node.InvalidSettingsException;

/**
 * Predictor for classification random forests.
 *
 * @author Adrian Nembach, KNIME GmbH, Konstanz, Germany
 */
public final class RandomForestClassificationPredictor
    extends AbstractRandomForestPredictor<RandomForestClassificationPrediction> {

    private final VotingFactory m_votingFactory;

    /**
     * Constructor for out-of-bag predictions.
     *
     * @param model the random forest {@link TreeEnsembleModel model}
     * @param modelSpec the spec of the random forest model
     * @param predictSpec the {@link DataTableSpec} of the input model
     * @param modelRowSamples the row samples used for each tree in {@link TreeEnsembleModel model}
     * @param targetColumnData the {@link TreeTargetColumnData} of the class column
     * @param votingFactory the {@link VotingFactory} used for predictions
     * @throws InvalidSettingsException
     */
    public RandomForestClassificationPredictor(final TreeEnsembleModel model,
        final TreeEnsembleModelPortObjectSpec modelSpec, final DataTableSpec predictSpec,
        final RowSample[] modelRowSamples, final TreeTargetColumnData targetColumnData,
        final VotingFactory votingFactory) throws InvalidSettingsException {
        super(model, modelSpec, predictSpec, modelRowSamples, targetColumnData);
        m_votingFactory = votingFactory;
    }

    /**
     * Constructor for normal predictions i.e. no out-of-bag information is available.
     *
     * @param model the random forest {@link TreeEnsembleModel model}
     * @param modelSpec the spec of the random forest model
     * @param predictSpec the {@link DataTableSpec} of the input model
     * @param votingFactory the {@link VotingFactory} used for predictions
     * @throws InvalidSettingsException
     */
    public RandomForestClassificationPredictor(final TreeEnsembleModel model,
        final TreeEnsembleModelPortObjectSpec modelSpec, final DataTableSpec predictSpec,
        final VotingFactory votingFactory) throws InvalidSettingsException {
        super(model, modelSpec, predictSpec);
        m_votingFactory = votingFactory;
    }

    @Override
    protected RandomForestClassificationPrediction predictRecord(final PredictorRecord record, final RowKey key) {
        return new RFClassificationPrediction(record, key, hasOutOfBagFilter());
    }

    private class RFClassificationPrediction implements RandomForestClassificationPrediction {

        private final Voting m_voting;

        RFClassificationPrediction(final PredictorRecord record, final RowKey key, final boolean hasOutOfBagFilter) {
            m_voting = m_votingFactory.createVoting();
            final int nrModels = m_model.getNrModels();
            for (int i = 0; i < nrModels; i++) {
                if (hasOutOfBagFilter && isRowPartOfTrainingData(key, i)) {
                    // ignore, row was used to train the model
                } else {
                    TreeModelClassification m = m_model.getTreeModelClassification(i);
                    TreeNodeClassification match = m.findMatchingNode(record);
                    m_voting.addVote(match);
                }
            }
        }

        @Override
        public String getClassPrediction() {
            return m_voting.getMajorityClass();
        }

        @Override
        public int getWinningClassIdx() {
            return m_voting.getMajorityClassIdx();
        }

        @Override
        public double getProbability(final int classIdx) {
            return m_voting.getClassProbabilityForClass(classIdx);
        }

        @Override
        public int getModelCount() {
            return m_voting.getNrVotes();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean hasPrediction() {
            return m_voting.getNrVotes() > 0;
        }

    }
}
