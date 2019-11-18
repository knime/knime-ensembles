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
 *   8 Nov 2019 (Alexander): created
 */
package org.knime.base.node.mine.treeensemble2.node.regressiontree.predictor;

import java.util.List;

import org.knime.base.node.mine.treeensemble2.model.AbstractTreeEnsembleModel.TreeType;
import org.knime.base.node.mine.treeensemble2.model.RegressionTreeModel;
import org.knime.base.node.mine.treeensemble2.model.RegressionTreeModelPortObjectSpec;
import org.knime.base.node.mine.treeensemble2.model.pmml.AbstractTreeModelPMMLTranslator;
import org.knime.base.node.mine.treeensemble2.model.pmml.RegressionTreeModelPMMLTranslator;
import org.knime.base.node.mine.treeensemble2.node.predictor.TreeEnsemblePredictorConfiguration;
import org.knime.base.predict.PMMLRegressionPredictorOptions;
import org.knime.base.predict.PMMLTablePredictor;
import org.knime.base.predict.PredictorContext;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.DoubleValue;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.port.pmml.PMMLPortObject;
import org.knime.core.node.port.pmml.PMMLPortObjectSpec;
import org.knime.core.node.util.CheckUtils;
import org.knime.core.util.Pair;

/**
 * Predictor class for regression trees.
 * @author Alexander Fillbrunn, KNIME GmbH, Konstanz, Germany
 */
public final class PMMLRegressionTreePredictor implements PMMLTablePredictor {

    private PMMLRegressionPredictorOptions m_options;

    /**
     * Creates a new instance of {@code RegressionTreePMMLPredictor}.
     * @param options the options determining the predictor output
     */
    public PMMLRegressionTreePredictor(final PMMLRegressionPredictorOptions options) {
        m_options = options;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DataTableSpec getOutputSpec(final DataTableSpec inputSpec, final PMMLPortObjectSpec modelSpec,
        final PredictorContext ctx) throws InvalidSettingsException {
        DataType targetType = extractTargetType(modelSpec);
        if (!targetType.isCompatible(DoubleValue.class)) {
            throw new InvalidSettingsException("This node expects a regression model.");
        }
        try {
            AbstractTreeModelPMMLTranslator.checkPMMLSpec(modelSpec);
        } catch (IllegalArgumentException e) {
            throw new InvalidSettingsException(e.getMessage());
        }
        RegressionTreeModelPortObjectSpec regModelSpec = translateSpec(modelSpec);
        String targetColName = regModelSpec.getTargetColumn().getName();

        RegressionTreePredictorConfiguration config = new RegressionTreePredictorConfiguration(targetColName);

        if (!m_options.hasCustomPredictionName()) {
            config.setPredictionColumnName(TreeEnsemblePredictorConfiguration.getPredictColumnName(targetColName));
        } else {
            config.setPredictionColumnName(m_options.getCustomPredictionName());
        }
        final RegressionTreePredictionHandler pred =
            new RegressionTreePredictionHandler(null, regModelSpec, inputSpec, config);
        return (DataTableSpec)pred.configure()[0];
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BufferedDataTable predict(final BufferedDataTable input, final PMMLPortObject model,
        final PredictorContext ctx) throws Exception {
        final ExecutionContext exec = ctx.getExecutionContext();
        Pair<RegressionTreeModel, RegressionTreeModelPortObjectSpec> modelSpecPair = importModel(model, ctx);
        DataTableSpec dataSpec = input.getDataTableSpec();
        String targetColName = model.getSpec().getTargetCols().get(0).getName();
        RegressionTreePredictorConfiguration config = new RegressionTreePredictorConfiguration(targetColName);

        if (!m_options.hasCustomPredictionName()) {
            config.setPredictionColumnName(TreeEnsemblePredictorConfiguration.getPredictColumnName(targetColName));
        } else {
            config.setPredictionColumnName(m_options.getCustomPredictionName());
        }

        final RegressionTreePredictionHandler pred =
            new RegressionTreePredictionHandler(modelSpecPair.getFirst(), modelSpecPair.getSecond(), dataSpec, config);
        BufferedDataTable outTable = exec.createColumnRearrangeTable(input, pred.createExecutionRearranger(), exec);
        return outTable;
    }

    private static RegressionTreeModelPortObjectSpec translateSpec(final PMMLPortObjectSpec pmmlSpec) {
        DataTableSpec pmmlDataSpec = pmmlSpec.getDataTableSpec();
        ColumnRearranger cr = new ColumnRearranger(pmmlDataSpec);
        List<DataColumnSpec> targets = pmmlSpec.getTargetCols();
        CheckUtils.checkArgument(!targets.isEmpty(), "The provided PMML does not declare a target field.");
        CheckUtils.checkArgument(targets.size() == 1, "The provided PMML declares multiple target. "
            + "This behavior is currently not supported.");
        cr.move(targets.get(0).getName(), pmmlDataSpec.getNumColumns());
        return new RegressionTreeModelPortObjectSpec(cr.createSpec());
    }

    private static DataType extractTargetType(final PMMLPortObjectSpec pmmlSpec) {
        return pmmlSpec.getTargetCols().get(0).getType();
    }

    private static String getTargetColumnName(final PMMLPortObjectSpec pmmlSpec) {
        List<DataColumnSpec> targets = pmmlSpec.getTargetCols();
        CheckUtils.checkArgument(!targets.isEmpty(), "The provided PMML does not declare a target field.");
        CheckUtils.checkArgument(targets.size() == 1, "The provided PMML declares multiple target. "
            + "This behavior is currently not supported.");
        return targets.get(0).getName();
    }

    private static Pair<RegressionTreeModel, RegressionTreeModelPortObjectSpec> importModel(
        final PMMLPortObject pmmlPO, final PredictorContext ctx) {
        RegressionTreeModelPMMLTranslator pmmlTranslator = new RegressionTreeModelPMMLTranslator();
        pmmlPO.initializeModelTranslator(pmmlTranslator);
        if (pmmlTranslator.hasWarning()) {
            ctx.setWarningMessage(pmmlTranslator.getWarning());
        }
        return new Pair<>(new RegressionTreeModel(
            pmmlTranslator.getTreeMetaData(), pmmlTranslator.getTree(), TreeType.Ordinary),
                new RegressionTreeModelPortObjectSpec(pmmlTranslator.getLearnSpec()));
    }
}
