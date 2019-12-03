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
 *   Nov 27, 2018 (Mark Ortmann, KNIME GmbH, Berlin, Germany): created
 */
package org.knime.ensembles.pmmlpredict3;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.dmg.pmml.DerivedFieldDocument.DerivedField;
import org.knime.base.node.mine.bayes.naivebayes.predictor4.PMMLNaiveBayesPredictor;
import org.knime.base.node.mine.cluster.assign.PMMLClusterAssigner;
import org.knime.base.node.mine.cluster.assign.PMMLClusterAssigner.PMMLClusterAssignerOptions;
import org.knime.base.node.mine.decisiontree2.predictor2.PMMLDecisionTreePredictor;
import org.knime.base.node.mine.decisiontree2.predictor2.PMMLDecisionTreePredictor.PMMLDecisionTreePredictorOptions;
import org.knime.base.node.mine.neural.mlp2.PMMLMlpPredictor;
import org.knime.base.node.mine.regression.predict2.PMMLRegressionPredictor;
import org.knime.base.node.mine.svm.predictor2.PMMLSvmPredictor;
import org.knime.base.node.mine.treeensemble2.node.gradientboosting.predictor.pmml.PMMLGradientBoostingPredictor;
import org.knime.base.node.mine.treeensemble2.node.gradientboosting.predictor.pmml.PMMLGradientBoostingPredictor.PMMLGradientBoostingPredictorOptions;
import org.knime.base.node.mine.treeensemble2.node.gradientboosting.predictor.pmml.PMMLGradientBoostingPredictor.Version;
import org.knime.base.node.mine.treeensemble2.node.regressiontree.predictor.PMMLRegressionTreePredictor;
import org.knime.base.node.mine.util.PredictorHelper;
import org.knime.base.pmml.transformation.PmmlPreprocessorNodeModel;
import org.knime.base.predict.DefaultPredictorContext;
import org.knime.base.predict.PMMLClassificationPredictorOptions;
import org.knime.base.predict.PMMLRegressionPredictorOptions;
import org.knime.base.predict.PMMLTablePredictor;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataTableSpec;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.pmml.PMMLPortObject;
import org.knime.core.pmml.PMMLModelType;
import org.knime.ensembles.pmml.predictor2.PMMLEnsemblePredictor2NodeModel;
import org.knime.ensembles.pmml.predictor3.PMMLEnsemblePredictorNodeModel3;
import org.w3c.dom.Node;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

/**
 * Node model of the PMML Predictor node.
 *
 * @author Mark Ortmann, KNIME GmbH, Berlin, Germany
 * @author Adrian Nembach, KNIME GmbH, Konstanz, Germany
 */
public class PMMLPredictorNodeModel3 extends NodeModel {

    private static final String CFGKEY_APPEND_PROBS = "append probabilities";
    private static final boolean DEFAULT_APPEND_PROBS = false;

    private static final int PMML_PORT = 0;

    private final boolean m_applyPreprocessing;

    /**
     * Creates a new model with a PMML input and a data output.
     * Does not apply preprocessing steps defined as part of the PMML.
     */
    protected PMMLPredictorNodeModel3() {
        this(false);
    }

    /**
     * @param applyPreprocessing whether preprocessing defined in the PMML Document should be applied (introduced with
     *            KNIME 4.1.0)
     */
    protected PMMLPredictorNodeModel3(final boolean applyPreprocessing) {
        super(new PortType[]{PMMLPortObject.TYPE, BufferedDataTable.TYPE}, new PortType[]{BufferedDataTable.TYPE});
        m_applyPreprocessing = applyPreprocessing;
    }

    /** Prediction column name. */
    private final SettingsModelString m_predictionColumn = PredictorHelper.getInstance().createPredictionColumn();

    /** Suffix for the probability columns. */
    private final SettingsModelString m_suffix = PredictorHelper.getInstance().createSuffix();

    private final SettingsModelBoolean m_overridePrediction = PredictorHelper.getInstance().createChangePrediction();

    private final SettingsModelBoolean m_appendProbs = createAppendProbs();

    /**
     * @return The "append probabilities" node model.
     */
    static SettingsModelBoolean createAppendProbs() {
        return new SettingsModelBoolean(CFGKEY_APPEND_PROBS, DEFAULT_APPEND_PROBS);
    }

    private static class DerivedNameMapper {
        private final BiMap<String, String> m_nameMap;

        DerivedNameMapper(final PMMLPortObject port) {
            final DerivedField[] derivedFields = port.getDerivedFields();
            final Map<String, String> nameMap = new HashMap<>();
            for (DerivedField derivedField : derivedFields) {
                nameMap.put(derivedField.getDisplayName(), derivedField.getName());
            }
            m_nameMap = HashBiMap.create(nameMap);
        }

        DataTableSpec renameDerivedFields(final DataTableSpec spec) {
            return renameColumns(m_nameMap, spec);
        }

        DataTableSpec reverseRenaming(final DataTableSpec spec) {
            return renameColumns(m_nameMap.inverse(), spec);
        }

        private static DataTableSpec renameColumns(final Map<String, String> nameMap, final DataTableSpec tableSpec) {
            final DataColumnSpec[] cspecs = new DataColumnSpec[tableSpec.getNumColumns()];
            for (int i = 0; i < cspecs.length; i++) {
                final DataColumnSpec columnSpec = tableSpec.getColumnSpec(i);
                final DataColumnSpecCreator cc = new DataColumnSpecCreator(columnSpec);
                if (nameMap.containsKey(columnSpec.getName())) {
                    cc.setName(nameMap.get(columnSpec.getName()));
                }
                cspecs[i] = cc.createSpec();
            }
            return new DataTableSpec(cspecs);
        }
    }



    /** {@inheritDoc} */
    @Override
    protected PortObject[] execute(final PortObject[] inObjects, final ExecutionContext exec) throws Exception {
        PMMLPortObject port = (PMMLPortObject)inObjects[PMML_PORT];

        BufferedDataTable table = (BufferedDataTable)inObjects[1];
        double predictionProgress = 1.0;
        DerivedNameMapper derivedNameMapper = null;
        if (port.getDerivedFields().length > 0 && m_applyPreprocessing) {
            final PmmlPreprocessorNodeModel preprocessor = new PmmlPreprocessorNodeModel();
            table = (BufferedDataTable)preprocessor.execute(new PortObject[]{table, port},
                exec.createSubExecutionContext(0.5))[0];
            predictionProgress = 0.5;
            derivedNameMapper = new DerivedNameMapper(port);
            table =
                exec.createSpecReplacerTable(table, derivedNameMapper.renameDerivedFields(table.getDataTableSpec()));
        }

        Set<PMMLModelType> types = port.getPMMLValue().getModelTypes();
        if (types.isEmpty()) {
            throw new InvalidSettingsException("No PMML Model found.");
        }

        PMMLModelType type = types.iterator().next();

        if (types.size() > 1) {
            setWarningMessage("More models are found, the first one is used " + " : " + type.toString());
        }

        List<Node> models = port.getPMMLValue().getModels(type);
        if (models.isEmpty()) {
            throw new InvalidSettingsException("No PMML Model found.");
        }

        Iterator<DataColumnSpec> tciter = port.getSpec().getTargetCols().iterator();
        String target = "Prediction";
        if (tciter.hasNext()) {
            target = PredictorHelper.getInstance().computePredictionColumnName(
                m_predictionColumn.getStringValue(), m_overridePrediction.getBooleanValue(),
                tciter.next().getName());
        }

        PMMLTablePredictor predictor;
        switch (type) {
            case ClusteringModel:
            if (m_overridePrediction.getBooleanValue()) {
                target = m_predictionColumn.getStringValue();
            } else {
                target = "Cluster";
            }
            predictor = new PMMLClusterAssigner(new PMMLClusterAssignerOptions(
                    m_overridePrediction.getBooleanValue() ? target : null));
                break;
            case GeneralRegressionModel:
            case RegressionModel:
            predictor = new PMMLRegressionPredictor(createClassificationOptions(target));
                break;
            case TreeModel:
            if (isSimpleRegressionTree(models.get(0))) {
                    predictor = new PMMLRegressionTreePredictor(new PMMLRegressionPredictorOptions(
                        m_overridePrediction.getBooleanValue() ? target : null));
                    break;
                }
                predictor = new PMMLDecisionTreePredictor(new PMMLDecisionTreePredictorOptions(
                    m_overridePrediction.getBooleanValue() ? target : null,
                    m_appendProbs.getBooleanValue(), m_suffix.getStringValue(), 10000));
                break;
            case SupportVectorMachineModel:
            predictor = new PMMLSvmPredictor(createClassificationOptions(target));
                break;
            case NeuralNetwork:
            predictor = new PMMLMlpPredictor(createClassificationOptions(target));
                break;
            case MiningModel:
            if (isGradientBoostedTreesModel(models.get(0))) {
                    String functionName = models.get(0).getAttributes().getNamedItem("functionName").getNodeValue();
                    boolean isRegression = functionName.equals("regression");
                    predictor = new PMMLGradientBoostingPredictor(Version.V401, isRegression,
                        new PMMLGradientBoostingPredictorOptions(false, false, !m_applyPreprocessing,
                            m_overridePrediction.getBooleanValue() ? target : null,
                            m_appendProbs.getBooleanValue(), m_suffix.getStringValue()));
                    break;
                }
                // Handling of ensembles could not be extracted into a separate class that easily
                // because that node model also calls node models again.
                if (m_applyPreprocessing) {
                    PMMLEnsemblePredictorNodeModel3 model = new PMMLEnsemblePredictorNodeModel3();
                    BufferedDataTable dt = (BufferedDataTable)model.execute(new PortObject[]{table, port},
                        exec.createSubExecutionContext(predictionProgress))[0];
                    return postprocess(exec, derivedNameMapper, dt);
                } else {
                    PMMLEnsemblePredictor2NodeModel model = new PMMLEnsemblePredictor2NodeModel();
                    return model.execute(inObjects, exec);
                }
            case NaiveBayesModel:
            predictor = new PMMLNaiveBayesPredictor(createClassificationOptions(target));
                break;
            default:
                // this should never happen.
                throw new InvalidSettingsException("No suitable predictor found for these model types.");
        }
        // Use the determined predictor to create the output table
        BufferedDataTable dt = predictor.predict(table, port,
            new DefaultPredictorContext(exec.createSubExecutionContext(predictionProgress), this::setWarningMessage));
        return postprocess(exec, derivedNameMapper, dt);
    }

    private static PortObject[] postprocess(final ExecutionContext exec, final DerivedNameMapper derivedNameMapper,
        BufferedDataTable dt) {
        if (derivedNameMapper != null) {
            // undo the renaming of the input columns in case we had some transformations
            dt = exec.createSpecReplacerTable(dt, derivedNameMapper.reverseRenaming(dt.getDataTableSpec()));
        }
        return new PortObject[] {dt};
    }

    private PMMLClassificationPredictorOptions createClassificationOptions(final String targetColumnName) {
        return new PMMLClassificationPredictorOptions(
            m_overridePrediction.getBooleanValue() ? targetColumnName : null,
            m_appendProbs.getBooleanValue(),
            m_suffix.getStringValue()
            );
    }

    private static boolean isSimpleRegressionTree(final Node treeModel) {
        if (!treeModel.getNodeName().equals("TreeModel")) {
            return false;
        }
        return treeModel.getAttributes().getNamedItem("functionName").getNodeValue().equals("regression");
    }

    private static boolean isGradientBoostedTreesModel(final Node miningModel) {
        Node modelName = miningModel.getAttributes().getNamedItem("modelName");
        return modelName == null ? false : modelName.getNodeValue().equals("GradientBoostedTrees");
    }

    /** {@inheritDoc} */
    @Override
    protected PortObjectSpec[] configure(final PortObjectSpec[] inSpecs) throws InvalidSettingsException {
        return new PortObjectSpec[]{null};
    }

    /** {@inheritDoc} */
    @Override
    protected void loadInternals(final File nodeInternDir, final ExecutionMonitor exec)
        throws IOException, CanceledExecutionException {
        // nothing to load

    }

    /** {@inheritDoc} */
    @Override
    protected void saveInternals(final File nodeInternDir, final ExecutionMonitor exec)
        throws IOException, CanceledExecutionException {
        // no op
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        if (settings.containsKey(PredictorHelper.CFGKEY_PREDICTION_COLUMN)) {
            m_predictionColumn.loadSettingsFrom(settings);
        }
        if (settings.containsKey(PredictorHelper.CFGKEY_CHANGE_PREDICTION)) {
            m_overridePrediction.loadSettingsFrom(settings);
        }
        if (settings.containsKey(CFGKEY_APPEND_PROBS)) {
            m_appendProbs.loadSettingsFrom(settings);
        }
        if (settings.containsKey(PredictorHelper.CFGKEY_SUFFIX)) {
            m_suffix.loadSettingsFrom(settings);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        m_predictionColumn.saveSettingsTo(settings);
        m_overridePrediction.saveSettingsTo(settings);
        m_appendProbs.saveSettingsTo(settings);
        m_suffix.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        if (settings.containsKey(PredictorHelper.CFGKEY_PREDICTION_COLUMN)) {
            m_predictionColumn.validateSettings(settings);
        }
        if (settings.containsKey(PredictorHelper.CFGKEY_CHANGE_PREDICTION)) {
            m_overridePrediction.validateSettings(settings);
        }
        if (settings.containsKey(CFGKEY_APPEND_PROBS)) {
            m_appendProbs.validateSettings(settings);
        }
        if (settings.containsKey(PredictorHelper.CFGKEY_SUFFIX)) {
            m_suffix.validateSettings(settings);
        }
    }


    /** {@inheritDoc} */
    @Override
    protected void reset() {
        // no op
    }

}