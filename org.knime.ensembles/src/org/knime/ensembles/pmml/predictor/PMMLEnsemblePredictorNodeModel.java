/*
 * ------------------------------------------------------------------------
 *
 *  Copyright (C) 2003 - 2013
 *  University of Konstanz, Germany and
 *  KNIME GmbH, Konstanz, Germany
 *  Website: http://www.knime.org; Email: contact@knime.org
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
 * ------------------------------------------------------------------------
 */
package org.knime.ensembles.pmml.predictor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import org.dmg.pmml.MININGFUNCTION;
import org.dmg.pmml.MULTIPLEMODELMETHOD;
import org.dmg.pmml.MiningModelDocument.MiningModel;
import org.dmg.pmml.PMMLDocument;
import org.dmg.pmml.SegmentDocument.Segment;
import org.knime.base.node.mine.cluster.assign.ClusterAssignerNodeModel;
import org.knime.base.node.mine.decisiontree2.predictor.DecTreePredictorNodeModel;
import org.knime.base.node.mine.neural.mlp.MLPPredictorNodeModel;
import org.knime.base.node.mine.regression.logistic.predict.GeneralRegressionPredictorNodeModel;
import org.knime.base.node.mine.regression.predict.RegressionPredictorNodeModel;
import org.knime.base.node.mine.svm.predictor.SVMPredictorNodeModel;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTable;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.MissingCell;
import org.knime.core.data.RowKey;
import org.knime.core.data.container.DataContainer;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.StringCell;
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
import org.knime.core.node.port.pmml.PMMLModelWrapper;
import org.knime.core.node.port.pmml.PMMLPortObject;
import org.knime.core.node.port.pmml.PMMLPortObjectSpecCreator;
import org.knime.core.pmml.PMMLModelType;
import org.knime.ensembles.pmml.ModelNotSupportedException;



/**
 * This is the model implementation of PMMLEnsemblePredictor.
 * This node takes a PMML ensemble document and an input table and uses each model in the ensemble on the given data.
 * The individual results from applying the models are then combined using the multiple model method specified in the
 * PMML document.
 *
 * @author Alexander Fillbrunn, Universitaet Konstanz
 * @since 2.8
 */
public class PMMLEnsemblePredictorNodeModel extends NodeModel {

    private final SettingsModelString m_tieBreak = createTieBreakSettingsModel();

    /**
     * Flag that indicates whether predictions from individual models should be included
     * in the result table.
     */
    private final SettingsModelBoolean m_returnIndividualPredictions = createReturnIndividualPredictionsSettingsModel();


    /**
     * Flag that indicates whether the used multiple model method should be used as the name
     * of the result column.
     */
    private final SettingsModelBoolean m_useMethodAsColumnName = createUseMethodSettingsModel();

    /**
     *
     * @return the SM for using the column names.
     */
    public static SettingsModelBoolean createUseMethodSettingsModel() {
        return new SettingsModelBoolean("useMethodAsColumnName", false);
    }

    /**
     * Creates a SettingsModelBoolean for determining how ties in majority vote or weighted majority vote are treated.
     * @return The SettingsModel
     */
    public static SettingsModelString createTieBreakSettingsModel() {
        return new SettingsModelString("tieBreakMethod", "missing");
    }

    /**
     * Creates a SettingsModelBoolean for determining whether individual predictions from models are included in output.
     * @return The SettingsModel
     */
    public static SettingsModelBoolean createReturnIndividualPredictionsSettingsModel() {
        return new SettingsModelBoolean("returnIndividualPredictions", false);
    }

    /**
     * Constructor for the node model.
     */
    protected PMMLEnsemblePredictorNodeModel() {
        super(new PortType[]{new PortType(PMMLPortObject.class), new PortType(BufferedDataTable.class)},
                new PortType[]{new PortType(BufferedDataTable.class)});
    }

    // See http://www.dmg.org/v4-0-1/MultipleModels.html
    private static final org.dmg.pmml.MULTIPLEMODELMETHOD.Enum[] CLUSTERINGMETHODS
    = new org.dmg.pmml.MULTIPLEMODELMETHOD.Enum[]{
        org.dmg.pmml.MULTIPLEMODELMETHOD.MAJORITY_VOTE,
        org.dmg.pmml.MULTIPLEMODELMETHOD.WEIGHTED_MAJORITY_VOTE,
        org.dmg.pmml.MULTIPLEMODELMETHOD.SELECT_FIRST,
        org.dmg.pmml.MULTIPLEMODELMETHOD.SELECT_ALL
    };

    private static final org.dmg.pmml.MULTIPLEMODELMETHOD.Enum[] CLASSIFICATIONMETHODS
    = new org.dmg.pmml.MULTIPLEMODELMETHOD.Enum[]{
        org.dmg.pmml.MULTIPLEMODELMETHOD.MAJORITY_VOTE,
        org.dmg.pmml.MULTIPLEMODELMETHOD.WEIGHTED_MAJORITY_VOTE,
        org.dmg.pmml.MULTIPLEMODELMETHOD.SELECT_FIRST,
        org.dmg.pmml.MULTIPLEMODELMETHOD.SELECT_ALL,
    };

    private static final org.dmg.pmml.MULTIPLEMODELMETHOD.Enum[] REGRESSIONMETHODS
    = new org.dmg.pmml.MULTIPLEMODELMETHOD.Enum[]{
        org.dmg.pmml.MULTIPLEMODELMETHOD.AVERAGE,
        org.dmg.pmml.MULTIPLEMODELMETHOD.MAX,
        org.dmg.pmml.MULTIPLEMODELMETHOD.WEIGHTED_AVERAGE,
        org.dmg.pmml.MULTIPLEMODELMETHOD.MEDIAN,
        org.dmg.pmml.MULTIPLEMODELMETHOD.SUM,
        org.dmg.pmml.MULTIPLEMODELMETHOD.SELECT_FIRST,
        org.dmg.pmml.MULTIPLEMODELMETHOD.SELECT_ALL
    };

    //Check if the multiplemodelmethod is in the array for a mining function
    private boolean methodValidFor(final org.dmg.pmml.MULTIPLEMODELMETHOD.Enum method,
            final org.dmg.pmml.MULTIPLEMODELMETHOD.Enum[] allowed) {
        for (org.dmg.pmml.MULTIPLEMODELMETHOD.Enum m : allowed) {
            if (m == method) {
                return true;
            }
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected PortObject[] execute(final PortObject[] inData,
            final ExecutionContext exec) throws Exception {

        PMMLPortObject pmmlIn = ((PMMLPortObject)inData[0]);
        PMMLDocument pmmldoc = PMMLDocument.Factory.parse(pmmlIn.getPMMLValue().getDocument());
        List<MiningModel> models = pmmldoc.getPMML().getMiningModelList();
        if (models.size() == 0) {
            throw new ModelNotSupportedException("No mining models found");
        }
        MiningModel usedModel = models.get(0);

        // Retrieve a list of all models in the mining model
        List<PMMLModelWrapper> wrappers = PMMLModelWrapper.getModelListFromMiningModel(usedModel);

        // Predict with each model in the mining model
        HashMap<RowKey, ArrayList<DataCell>> results = calculateAllPredictions(wrappers, inData, pmmldoc,
                usedModel.getFunctionName(), exec);

        // Calculate the aggregated result depending on the MultipleModelsMethod
        BufferedDataTable output = combine((BufferedDataTable)inData[1], results, usedModel, wrappers.size(), exec);

        return new PortObject[]{output};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected PortObjectSpec[] configure(final PortObjectSpec[] inSpecs)
            throws InvalidSettingsException {
        // We cannot know from the specs how many columns will be added because we don't know the number of models
        // in the ensemble
        /*if (m_returnIndividualPredictions.getBooleanValue()) {
            return new DataTableSpec[]{null};
        } else {
            DataTableSpec inTableSpec = (DataTableSpec)inSpecs[0];
            return new PortObjectSpec[]{ createOutSpecs(inTableSpec) };
        }*/
        return new DataTableSpec[]{null};
    }

    /*DataTableSpec createOutSpecs(DataTableSpec inSpec) {
        DataTableSpec newColSpec = new DataTableSpec();
        DataColumnSpecCreator c = new DataColumnSpecCreator("Prediction", )
    }*/

    private HashMap<RowKey, ArrayList<DataCell>> calculateAllPredictions(final List<PMMLModelWrapper> wrappers,
                                                  final PortObject[] inData, final PMMLDocument pmmldoc,
                                                  final MININGFUNCTION.Enum funcName, final ExecutionContext exec)
                                                  throws Exception {
        BufferedDataTable inTable = (BufferedDataTable)inData[1];
        HashMap<RowKey, ArrayList<DataCell>> results = new HashMap<RowKey, ArrayList<DataCell>>();
        int count = 0;
        for (PMMLModelWrapper modelwrapper : wrappers) {
            count++;
            if (exec != null) {
                exec.checkCanceled();
            }
            // Create a new document with only one model
            PMMLDocument modelDoc = modelwrapper.createPMMLDocument(pmmldoc.getPMML().getDataDictionary());
            // Create a fake pmml port for using the predictors
            PMMLPortObjectSpecCreator creator = new PMMLPortObjectSpecCreator(inTable.getDataTableSpec());
            creator.setTargetCols(((PMMLPortObject)inData[0]).getSpec().getTargetCols());
            creator.setLearningCols(((PMMLPortObject)inData[0]).getSpec().getLearningCols());
            PMMLPortObject fakePMMLPort = new PMMLPortObject(creator.createSpec(), modelDoc);
            DataTable result = null;

            switch (modelwrapper.getModelType()) {
                case TreeModel:
                    DecTreePredictorNodeModel dectreeModel = new DecTreePredictorNodeModel();
                    result = (DataTable)dectreeModel.execute(
                            new PortObject[]{fakePMMLPort, inTable}, exec)[0];
                    break;
                case NeuralNetwork:
                    MLPPredictorNodeModel mlpModel = new MLPPredictorNodeModel();
                    result = (DataTable)mlpModel.execute(new PortObject[]{fakePMMLPort, inTable}, exec)[0];
                    break;
                case RegressionModel:
                    RegressionPredictorNodeModel regrModel = new RegressionPredictorNodeModel();
                    result = (DataTable)regrModel.execute(new PortObject[]{fakePMMLPort, inTable}, exec)[0];
                    break;
                case GeneralRegressionModel:
                    GeneralRegressionPredictorNodeModel genregrModel =
                        new GeneralRegressionPredictorNodeModel();
                    result = (DataTable)genregrModel.execute(
                            new PortObject[]{fakePMMLPort, inTable}, exec)[0];
                    break;
                case ClusteringModel:
                    ClusterAssignerNodeModel clusterModel = new ClusterAssignerNodeModel();
                    result = (DataTable)clusterModel.execute(
                            new PortObject[]{fakePMMLPort, inTable}, exec)[0];
                    break;
                case SupportVectorMachineModel:
                    SVMPredictorNodeModel svmModel = new SVMPredictorNodeModel();
                    result = (DataTable)svmModel.execute(
                            new PortObject[]{fakePMMLPort, inTable}, exec)[0];
                    break;
                default:
                    throw new ModelNotSupportedException("Model of type "
                            + modelwrapper.getModelType().toString() + " is not supported");
            }
            // Retrieve the value in the result column
            for (DataRow row : result) {
                ArrayList<DataCell> list = results.get(row.getKey());
                if (list == null) {
                    list = new ArrayList<DataCell>();
                    results.put(row.getKey(), list);
                }
                /*
                 * We have to assume that the last column contains the prediction.
                 * One exception is classification with a neural network where the predicted class is
                 * in a column named "PredClass" which is followed by columns with probabilities for each class.
                 */
                if (modelwrapper.getModelType() == PMMLModelType.NeuralNetwork
                        && funcName == org.dmg.pmml.MININGFUNCTION.CLASSIFICATION) {
                    int col = result.getDataTableSpec().findColumnIndex("PredClass");
                    list.add(row.getCell(col));
                } else {
                    list.add(row.getCell(row.getNumCells() - 1));
                }
            }
            if (exec != null) {
                exec.getProgressMonitor().setProgress((double)count / wrappers.size());
            }
        }
        return results;
    }

    private BufferedDataTable combine(final BufferedDataTable inTable,
                                    final HashMap<RowKey, ArrayList<DataCell>> results,
                                    final MiningModel usedModel,
                                    final int numModels,
                                    final ExecutionContext exec)
                  throws ModelNotSupportedException {

        MULTIPLEMODELMETHOD.Enum method = usedModel.getSegmentation().getMultipleModelMethod();
        MININGFUNCTION.Enum funcName = usedModel.getFunctionName();

        /*
         * Collect weights from the segments and store them in an array.
         * Weights are automatically 1 if none is given in the segment declaration.
         */
        double[] weights = new double[usedModel.getSegmentation().getSegmentList().size()];
        int counter = 0;
        for (Segment s : usedModel.getSegmentation().getSegmentList()) {
            weights[counter++] = s.getWeight();
        }

        // Collect specs for the columns in the result table
        String[] names;
        DataType[] types;
        counter = 0;

        if (m_returnIndividualPredictions.getBooleanValue()) {
            names = new String[numModels + 1];
            types = new DataType[numModels + 1];
            for (RowKey key : results.keySet()) {
                for (DataCell c : results.get(key)) {
                    names[counter] = "result" + counter;
                    types[counter] = c.getType();
                    counter++;
                }
                break;
            }
        } else {
            names = new String[1];
            types = new DataType[1];
        }

        // Set the header of the result column
        names[counter] = m_useMethodAsColumnName.getBooleanValue() ? method.toString() : "Prediction";

        // Set the DataType of the result column and check if the MultipleModelMethod is valid
        // SelectAll and SelectFirst are ok for all model types
        if (method == org.dmg.pmml.MULTIPLEMODELMETHOD.MODEL_CHAIN) {
            throw new ModelNotSupportedException("Model chains are currently not supported");
        } else if (method != org.dmg.pmml.MULTIPLEMODELMETHOD.SELECT_ALL
                && method != org.dmg.pmml.MULTIPLEMODELMETHOD.SELECT_FIRST) {

            if (funcName == org.dmg.pmml.MININGFUNCTION.CLASSIFICATION) {
                if (!methodValidFor(method, CLASSIFICATIONMETHODS)) {
                    throw new ModelNotSupportedException(
                            "The multiple model method is not suitable for classification");
                }
                types[counter] = StringCell.TYPE;
            } else if (funcName == org.dmg.pmml.MININGFUNCTION.REGRESSION) {
                if (!methodValidFor(method, REGRESSIONMETHODS)) {
                    throw new ModelNotSupportedException(
                            "The multiple model method is not suitable for regression");
                }
                types[counter] = DoubleCell.TYPE;
            } else if (funcName == org.dmg.pmml.MININGFUNCTION.CLUSTERING) {
                if (!methodValidFor(method, CLUSTERINGMETHODS)) {
                    throw new ModelNotSupportedException(
                            "The multiple model method is not suitable for clustering");
                }
                types[counter] = StringCell.TYPE;
            }
        } else {
            types[counter] = StringCell.TYPE;
        }

        // Combine the cells in the results to one final result according to the multiple model method
        DataContainer cont = exec.createDataContainer(
                new DataTableSpec(inTable.getDataTableSpec(), new DataTableSpec(names, types)));
        for (DataRow row : inTable) {
            RowKey key = row.getKey();
             ArrayList<DataCell> list = results.get(key);
             if (method == org.dmg.pmml.MULTIPLEMODELMETHOD.AVERAGE) {
                 list.add(average(list));
             } else if (method == org.dmg.pmml.MULTIPLEMODELMETHOD.WEIGHTED_AVERAGE) {
                 list.add(weightedAverage(list, weights));
             } else if (method == org.dmg.pmml.MULTIPLEMODELMETHOD.MAJORITY_VOTE) {
                 list.add(majorityVote(list));
             } else if (method == org.dmg.pmml.MULTIPLEMODELMETHOD.WEIGHTED_MAJORITY_VOTE) {
                 list.add(weightedMajorityVote(list, weights));
             } else if (method == org.dmg.pmml.MULTIPLEMODELMETHOD.MAX) {
                 list.add(max(list));
             } else if (method == org.dmg.pmml.MULTIPLEMODELMETHOD.MEDIAN) {
                 list.add(median(list));
             } else if (method == org.dmg.pmml.MULTIPLEMODELMETHOD.SUM) {
                 list.add(sum(list));
             } else if (method == org.dmg.pmml.MULTIPLEMODELMETHOD.SELECT_FIRST) {
                 list.add(list.get(0));
             } else if (method == org.dmg.pmml.MULTIPLEMODELMETHOD.SELECT_ALL) {
                 list.add(selectAll(list));
             } else {
                 throw new ModelNotSupportedException("Multiple model method "
                         + method.toString() +  " is not supported");
             }
             int numCols = inTable.getDataTableSpec().getNumColumns();
             DataCell[] cells = new DataCell[
                          (m_returnIndividualPredictions.getBooleanValue()) ? numCols + numModels + 1 : numCols + 1];

             counter = 0;
             for (DataCell c : row) {
                 cells[counter++] = c;
             }
             if (m_returnIndividualPredictions.getBooleanValue()) {
                 for (DataCell c : list) {
                     cells[counter++] = c;
                 }
             } else {
                 cells[counter++] = list.get(list.size() - 1);
             }
             DataRow newRow = new DefaultRow(key, cells);
             cont.addRowToTable(newRow);
         }

         cont.close();
         return (BufferedDataTable) cont.getTable();
    }

    private StringCell selectAll(final ArrayList<DataCell> cells) {
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < cells.size(); i++) {
            buffer.append("\"");
            buffer.append(cells.get(i).toString());
            buffer.append("\"");
            if (i != cells.size() - 1) {
                buffer.append(";");
            }
        }
        return new StringCell(buffer.toString());
    }

    private DataCell median(final ArrayList<DataCell> cells) {
        if (cells.size() == 1) {
            return cells.get(0);
        }
        ArrayList<DataCell> sortedCells = new ArrayList<DataCell>(cells);
        Collections.sort(sortedCells, new Comparator<DataCell>() {
            @Override
            public int compare(final DataCell arg0, final DataCell arg1) {
                //We only need it to compare double cells
                if (arg0 instanceof DoubleCell && arg1 instanceof DoubleCell) {
                    DoubleCell c1 = (DoubleCell)arg0;
                    DoubleCell c2 = (DoubleCell)arg1;
                    if (c1.getDoubleValue() > c2.getDoubleValue()) {
                        return 1;
                    } else if (c2.getDoubleValue() > c1.getDoubleValue()) {
                        return -1;
                    }
                    return 0;
                }
                return 0;
            }
        });
        if (sortedCells.size() % 2 != 0) {
            // Odd number of samples -> Take the one in the middle
            return new DoubleCell(((DoubleCell)sortedCells.get(sortedCells.size() / 2 + 1)).getDoubleValue());
        } else {
            // Even number of samples -> take the average of the two in the middle
            int idx = sortedCells.size() / 2;
            double val1 = ((DoubleCell)sortedCells.get(idx)).getDoubleValue();
            double val2 = ((DoubleCell)sortedCells.get(idx - 1)).getDoubleValue();
            return new DoubleCell((val1 + val2) / 2);
        }
    }

    private DoubleCell sum(final ArrayList<DataCell> cells) {
        double sum = 0.0;
        for (DataCell c : cells) {
            if (c instanceof DoubleCell) {
                sum += ((DoubleCell)c).getDoubleValue();
            }
        }
        return new DoubleCell(sum);
    }

    private DoubleCell max(final ArrayList<DataCell> cells) {
        Double max = Double.NaN;
        for (DataCell c : cells) {
            if (c instanceof DoubleCell) {
                double v = ((DoubleCell)c).getDoubleValue();
                if (Double.isNaN(max) || v > max) {
                    max = v;
                }
            }
        }
        return new DoubleCell(max);
    }

    private DataCell majorityVote(final ArrayList<DataCell> cells) {
        return vote(cells, new double[0], false);
    }

    private DataCell weightedMajorityVote(final ArrayList<DataCell> cells, final double[] weights) {
        return vote(cells, weights, true);
    }

    private DataCell vote(final ArrayList<DataCell> cells, final double[] weights, final boolean useWeights) {
        HashMap<String, Double> catCount = new HashMap<String, Double>();
        int index = 0;
        ArrayList<String> mostFreq = new ArrayList<String>();

        for (DataCell c : cells) {
            Double val = catCount.get(c.toString());
            Double newVal = (useWeights) ? weights[index] : 1.0;

            if (val != null) {
                newVal += val;
            }
            if (mostFreq.size() > 0) {
                double w = catCount.get(mostFreq.get(0));
                if (w == newVal) {
                    mostFreq.add(c.toString());
                } else if (w < newVal) {
                    mostFreq.clear();
                    mostFreq.add(c.toString());
                }
            } else {
                mostFreq.add(c.toString());
            }
            catCount.put(c.toString(), newVal);
            index++;
        }

        String winner = mostFreq.get(0);
        if (mostFreq.size() > 1) {
            if (m_tieBreak.getStringValue().equals("missing")) {
                return new MissingCell("2 classes with same vote");
            } else if (m_tieBreak.getStringValue().equals("any")) {
                return new StringCell(winner);
            }
        }
        return new StringCell(winner);
    }

    private DoubleCell average(final ArrayList<DataCell> cells) {
        double sum = 0;
        for (DataCell c : cells) {
            sum += ((DoubleCell)c).getDoubleValue();
        }
        return new DoubleCell(sum / cells.size());
    }

    private DoubleCell weightedAverage(final ArrayList<DataCell> cells, final double[] weights) {
        double sum = 0;
        int counter = 0;
        for (DataCell c : cells) {
            sum += ((DoubleCell)c).getDoubleValue() * weights[counter++];
        }
        return new DoubleCell(sum / cells.size());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void reset() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        m_returnIndividualPredictions.saveSettingsTo(settings);
        m_useMethodAsColumnName.saveSettingsTo(settings);
        m_tieBreak.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        m_returnIndividualPredictions.loadSettingsFrom(settings);
        m_useMethodAsColumnName.loadSettingsFrom(settings);
        m_tieBreak.loadSettingsFrom(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        m_returnIndividualPredictions.validateSettings(settings);
        m_useMethodAsColumnName.validateSettings(settings);
        m_tieBreak.validateSettings(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadInternals(final File internDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveInternals(final File internDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {

    }

}

