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
