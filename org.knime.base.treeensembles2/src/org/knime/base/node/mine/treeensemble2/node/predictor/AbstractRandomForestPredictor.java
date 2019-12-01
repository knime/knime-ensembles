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
package org.knime.base.node.mine.treeensemble2.node.predictor;

import java.util.HashMap;
import java.util.Map;

import org.knime.base.data.filter.column.FilterColumnRow;
import org.knime.base.node.mine.treeensemble2.data.PredictorRecord;
import org.knime.base.node.mine.treeensemble2.data.TreeTargetColumnData;
import org.knime.base.node.mine.treeensemble2.model.TreeEnsembleModel;
import org.knime.base.node.mine.treeensemble2.model.TreeEnsembleModelPortObjectSpec;
import org.knime.base.node.mine.treeensemble2.sample.row.RowSample;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.RowKey;
import org.knime.core.node.InvalidSettingsException;

/**
 * An abstract implementation of {@link Predictor} that does predictions based on a random forest (tree ensemble)
 * model.
 *
 * @author Adrian Nembach, KNIME GmbH, Konstanz, Germany
 * @param <P> the type of prediction
 */
public abstract class AbstractRandomForestPredictor<P extends OutOfBagPrediction> implements Predictor<P> {

    private RowSample[] m_modelLearnRowSamples;

    private Map<RowKey, Integer> m_rowKeyToLearnIndex;

    /**
     * The {@link TreeEnsembleModel} that should be used for predictions.
     */
    protected final TreeEnsembleModel m_model;

    private final DataTableSpec m_learnSpec;

    private final int[] m_filterIndices;

    /**
     * @param model
     * @param modelSpec
     * @param predictSpec
     * @throws InvalidSettingsException
     */
    public AbstractRandomForestPredictor(final TreeEnsembleModel model, final TreeEnsembleModelPortObjectSpec modelSpec,
        final DataTableSpec predictSpec) throws InvalidSettingsException {
        m_model = model;
        m_filterIndices = modelSpec.calculateFilterIndices(predictSpec);
        m_learnSpec = modelSpec.getLearnTableSpec();
    }

    /**
     * @param model
     * @param modelSpec
     * @param predictSpec
     * @param modelRowSamples
     * @param targetColumnData
     * @throws InvalidSettingsException
     */
    public AbstractRandomForestPredictor(final TreeEnsembleModel model, final TreeEnsembleModelPortObjectSpec modelSpec,
        final DataTableSpec predictSpec, final RowSample[] modelRowSamples, final TreeTargetColumnData targetColumnData)
        throws InvalidSettingsException {
        this(model, modelSpec, predictSpec);
        setOutofBagFilter(modelRowSamples, targetColumnData);
    }

    /* (non-Javadoc)
     * @see org.knime.base.node.mine.treeensemble2.node.predictor.Predictor#predict(org.knime.core.data.DataRow)
     */
    @Override
    public P predict(final DataRow row) {
        FilterColumnRow filterRow = new FilterColumnRow(row, m_filterIndices);
        return predictRecord(m_model.createPredictorRecord(filterRow, m_learnSpec), row.getKey());
    }

    /**
     * @param record the record to predict
     * @param key the row key to access out of bag information
     * @return the prediction
     */
    protected abstract P predictRecord(PredictorRecord record, RowKey key);

    private void setOutofBagFilter(final RowSample[] modelRowSamples, final TreeTargetColumnData targetColumnData) {
        if (modelRowSamples == null || targetColumnData == null) {
            throw new NullPointerException("Argument must not be null.");
        }
        final int nrRows = targetColumnData.getNrRows();
        Map<RowKey, Integer> learnItemMap = new HashMap<>((int)(nrRows / 0.75 + 1));
        for (int i = 0; i < nrRows; i++) {
            RowKey key = targetColumnData.getRowKeyFor(i);
            learnItemMap.put(key, i);
        }
        m_modelLearnRowSamples = modelRowSamples;
        m_rowKeyToLearnIndex = learnItemMap;
    }

    /**
     * @return true if <b>this<b> has an out of bag filter
     */
    protected final boolean hasOutOfBagFilter() {
        return m_modelLearnRowSamples != null;
    }

    /**
     * @param key
     * @param modelIndex
     * @return true if the row with rowkey <b>key</b> in model with index <b>modelIndex</b> is part of the training data
     *         for this model
     */
    protected final boolean isRowPartOfTrainingData(final RowKey key, final int modelIndex) {
        assert m_modelLearnRowSamples != null : "no out of bag filter set";
        Integer indexInteger = m_rowKeyToLearnIndex.get(key);
        if (indexInteger == null) {
            return false;
        }
        int index = indexInteger;
        return m_modelLearnRowSamples[modelIndex].getCountFor(index) > 0;
    }

}
