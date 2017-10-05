/*
 * ------------------------------------------------------------------------
 *  Copyright by KNIME GmbH, Konstanz, Germany
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
 *   30.03.2011 (meinl): created
 */
package org.knime.ensembles.boosting;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.NominalValue;
import org.knime.core.data.RowKey;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.data.container.SingleCellFactory;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.workflow.LoopEndNode;
import org.knime.core.node.workflow.LoopStartNode;
import org.knime.core.node.workflow.LoopStartNodeTerminator;

/**
 * This is the model for the end node of a boosting predictor loop. It collects
 * the prediction from all models and weighs them according to the model weight.
 *
 * @author Thorsten Meinl, University of Konstanz
 */
public class BoostingPredictorLoopEndNodeModel extends NodeModel implements
        LoopEndNode {
    private final BoostingPredictorEndSettings m_settings =
            new BoostingPredictorEndSettings();

    private Map<RowKey, Map<DataCell, Double>> m_predictions =
            new HashMap<RowKey, Map<DataCell, Double>>();

    /**
     * Creates a new node model.
     */
    public BoostingPredictorLoopEndNodeModel() {
        super(1, 1);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected DataTableSpec[] configure(final DataTableSpec[] inSpecs)
            throws InvalidSettingsException {
        if (m_settings.predictionColumn() == null) {
            for (int i = inSpecs[0].getNumColumns() - 1; i >= 0; i--) {
                if (inSpecs[0].getColumnSpec(i).getType()
                        .isCompatible(NominalValue.class)) {
                    m_settings.predictionColumn(inSpecs[0].getColumnSpec(i)
                            .getName());
                    setWarningMessage("Auto-selected column '"
                            + inSpecs[0].getColumnSpec(i).getName()
                            + "' as prediction column");
                    break;
                }
            }
            if (m_settings.predictionColumn() == null) {
                throw new InvalidSettingsException(
                        "No column with nominal values in input table");
            }
        }
        DataColumnSpec pSpec =
                inSpecs[0].getColumnSpec(m_settings.predictionColumn());
        if (pSpec == null) {
            throw new InvalidSettingsException("Prediction column '"
                    + m_settings.predictionColumn()
                    + "' does not exist in second input table");
        }
        if (!pSpec.getType().isCompatible(NominalValue.class)) {
            throw new InvalidSettingsException("Prediction column '"
                    + m_settings.predictionColumn()
                    + "' does not contain nominal values");
        }

        ColumnRearranger crea = createRearranger(inSpecs[0]);
        return new DataTableSpec[]{crea.createSpec()};
    }


    private ColumnRearranger createRearranger(final DataTableSpec inSpec) {
        ColumnRearranger crea = new ColumnRearranger(inSpec);

        SingleCellFactory predictionFactory =
                new SingleCellFactory(inSpec.getColumnSpec(m_settings
                        .predictionColumn())) {
                    @Override
                    public DataCell getCell(final DataRow row) {
                        return predict(row);
                    }
                };

        crea.replace(predictionFactory, m_settings.predictionColumn());

        String name =
                DataTableSpec.getUniqueColumnName(inSpec,
                        "Prediction probability");
        DataColumnSpec cs =
                new DataColumnSpecCreator(name, DoubleCell.TYPE).createSpec();
        SingleCellFactory probabilityFactory = new SingleCellFactory(cs) {
            @Override
            public DataCell getCell(final DataRow row) {
                return new DoubleCell(probability(row));
            }
        };

        crea.append(probabilityFactory);

        return crea;
    }

    /* Return the probability for the selected class for this row. */
    private double probability(final DataRow row) {
        Map<DataCell, Double> map = m_predictions.get(row.getKey());
        if (map == null) {
            return Double.NaN;
        }

        Double maxProb = 0.0;
        double sum = 0;
        for (Map.Entry<DataCell, Double> e : map.entrySet()) {
            if (e.getValue() > maxProb) {
                maxProb = e.getValue();
            }
            sum += e.getValue();
        }

        return maxProb / sum;
    }

    /* Return the predicted i.e. most likely class for this row. */
    private DataCell predict(final DataRow row) {
        Map<DataCell, Double> map = m_predictions.get(row.getKey());
        if (map == null) {
            return DataType.getMissingCell();
        }

        Double maxProb = 0.0;
        DataCell maxValue = DataType.getMissingCell();
        for (Map.Entry<DataCell, Double> e : map.entrySet()) {
            if (e.getValue() > maxProb) {
                maxProb = e.getValue();
                maxValue = e.getKey();
            }
        }

        return maxValue;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
            final ExecutionContext exec) throws Exception {
        final LoopStartNode loopStart = getLoopStartNode();
        if (!(loopStart instanceof BoostingPredictorLoopStartNodeModel)) {
            throw new IllegalStateException(
                    "Loop start node is not a boosting predictor node");
        }

        final double modelWeight =
                ((BoostingPredictorLoopStartNodeModel)loopStart)
                        .getCurrentModelWeight();

        final int predictionIndex =
                inData[0].getDataTableSpec().findColumnIndex(
                        m_settings.predictionColumn());
        ExecutionContext subExec;
        if (((LoopStartNodeTerminator)loopStart).terminateLoop()) {
            subExec = exec.createSubExecutionContext(0.5);
        } else {
            subExec = exec;
        }

        final double max = inData[0].getRowCount();
        int i = 0;
        for (DataRow row : inData[0]) {
            subExec.checkCanceled();
            subExec.setProgress(i++ / max);
            DataCell c = row.getCell(predictionIndex);
            Map<DataCell, Double> map = m_predictions.get(row.getKey());
            if (map == null) {
                map = new HashMap<DataCell, Double>();
                m_predictions.put(row.getKey(), map);
            }
            Double weight = map.get(c);
            if (weight == null) {
                map.put(c, modelWeight);
            } else {
                map.put(c, weight + modelWeight);
            }
        }

        if (((LoopStartNodeTerminator)loopStart).terminateLoop()) {
            ColumnRearranger crea =
                    createRearranger(inData[0].getDataTableSpec());
            BufferedDataTable table =
                    exec.createColumnRearrangeTable(inData[0], crea,
                            exec.createSubExecutionContext(0.5));
            return new BufferedDataTable[]{table};
        } else {
            continueLoop();
            return new BufferedDataTable[]{null};
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadInternals(final File nodeInternDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveInternals(final File nodeInternDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        m_settings.saveSettings(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        BoostingPredictorEndSettings s = new BoostingPredictorEndSettings();
        s.loadSettings(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        m_settings.loadSettings(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void reset() {
        m_predictions = new HashMap<RowKey, Map<DataCell, Double>>();
    }
}
