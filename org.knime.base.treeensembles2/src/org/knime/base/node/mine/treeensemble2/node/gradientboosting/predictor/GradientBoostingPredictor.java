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
 *  KNIME and ECLIPSE being a combined program, KNIME GMBH herewith grants
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
 *   14.01.2016 (Adrian Nembach): created
 */
package org.knime.base.node.mine.treeensemble2.node.gradientboosting.predictor;

import org.knime.base.node.mine.treeensemble2.model.AbstractGradientBoostingModel;
import org.knime.base.node.mine.treeensemble2.model.GradientBoostedTreesModel;
import org.knime.base.node.mine.treeensemble2.model.MultiClassGradientBoostedTreesModel;
import org.knime.base.node.mine.treeensemble2.model.TreeEnsembleModelPortObjectSpec;
import org.knime.base.node.mine.treeensemble2.node.gradientboosting.predictor.classification.LKGradientBoostingPredictorCellFactory;
import org.knime.base.node.mine.treeensemble2.node.gradientboosting.predictor.regression.GradientBoostingPredictorCellFactory;
import org.knime.base.node.mine.treeensemble2.node.predictor.TreeEnsemblePredictorConfiguration;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DoubleValue;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.node.InvalidSettingsException;

/**
 *
 * @author Adrian Nembach, KNIME
 * @param <M> The type of gradient boosted trees model this predictor applies
 */
public class GradientBoostingPredictor <M extends AbstractGradientBoostingModel>{
    private final M m_model;

    private final TreeEnsemblePredictorConfiguration m_config;

    private final TreeEnsembleModelPortObjectSpec m_modelSpec;

    private final ColumnRearranger m_predictionRearranger;

    private final DataTableSpec m_dataSpec;


    /**
     * @param model the gradient boosted trees model to use for prediction
     * @param modelSpec the port object spec of the model
     * @param dataSpec the spec of the input table for which predictions should be obtained
     * @param config the predictor config
     * @throws InvalidSettingsException
     */
    public GradientBoostingPredictor(final M model,
        final TreeEnsembleModelPortObjectSpec modelSpec, final DataTableSpec dataSpec,
        final TreeEnsemblePredictorConfiguration config) throws InvalidSettingsException {
        m_model = model;
        m_modelSpec = modelSpec;
        m_dataSpec = dataSpec;
        m_config = config;

        boolean hasPossibleValues = modelSpec.getTargetColumnPossibleValueMap() != null;
        if (modelSpec.getTargetColumn().getType().isCompatible(DoubleValue.class)) {
            m_predictionRearranger = new ColumnRearranger(dataSpec);
            @SuppressWarnings("unchecked") GradientBoostingPredictor<GradientBoostedTreesModel> pred =
                    (GradientBoostingPredictor<GradientBoostedTreesModel>)this;
            m_predictionRearranger.append(GradientBoostingPredictorCellFactory.createFactory(pred));
        } else if (config.isAppendClassConfidences() && !hasPossibleValues) {
            // can't add confidence columns (possible values unknown)
            m_predictionRearranger = null;
        } else {
            m_predictionRearranger = new ColumnRearranger(dataSpec);
            @SuppressWarnings("unchecked") GradientBoostingPredictor<MultiClassGradientBoostedTreesModel> pred =
                    (GradientBoostingPredictor<MultiClassGradientBoostedTreesModel>)this;
            m_predictionRearranger.append(LKGradientBoostingPredictorCellFactory.createFactory(pred));
        }
    }

    /**
     * @return the {@link ColumnRearranger} that should be used to calculate the output table
     */
    public ColumnRearranger getPredictionRearranger() {
        return m_predictionRearranger;
    }

    /**
     * @return the table spec of the input table
     */
    public DataTableSpec getDataSpec() {
        return m_dataSpec;
    }

    /**
     * @return the gradient boosted trees model
     */
    public M getModel() {
        return m_model;
    }

    /**
     * @return the spec of the gradient boosted trees model
     */
    public TreeEnsembleModelPortObjectSpec getModelSpec() {
        return m_modelSpec;
    }

    /**
     * @return the predictor config
     */
    public TreeEnsemblePredictorConfiguration getConfig() {
        return m_config;
    }
}
