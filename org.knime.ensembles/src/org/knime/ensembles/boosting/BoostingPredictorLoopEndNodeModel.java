/*
 * ------------------------------------------------------------------------
 *
 *  Copyright (C) 2003 - 2011
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
import org.knime.core.data.DoubleValue;
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
 *
 * @author Thorsten Meinl, University of Konstanz
 */
public class BoostingPredictorLoopEndNodeModel extends NodeModel implements
        LoopEndNode {
    private final BoostingPredictorSettings m_settings =
            new BoostingPredictorSettings();

    private Map<RowKey, Map<DataCell, Double>> m_predictions =
            new HashMap<RowKey, Map<DataCell, Double>>();

    public BoostingPredictorLoopEndNodeModel() {
        super(2, 1);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected DataTableSpec[] configure(final DataTableSpec[] inSpecs)
            throws InvalidSettingsException {
        if (m_settings.weightColumn() == null) {
            for (DataColumnSpec cs : inSpecs[0]) {
                if (cs.getType().isCompatible(DoubleValue.class)) {
                    m_settings.weightColumn(cs.getName());
                    setWarningMessage("Auto-selected column '" + cs.getName()
                            + "' as weight column");
                    break;
                }
            }
            if (m_settings.weightColumn() == null) {
                throw new InvalidSettingsException(
                        "No double column for model weights found in first input table");
            }
        }
        DataColumnSpec wSpec =
                inSpecs[0].getColumnSpec(m_settings.weightColumn());
        if (wSpec == null) {
            throw new InvalidSettingsException("Weight column '"
                    + m_settings.weightColumn()
                    + "' does not exist in first input table");
        }
        if (!wSpec.getType().isCompatible(DoubleValue.class)) {
            throw new InvalidSettingsException("Weight column '"
                    + m_settings.weightColumn() + "' is not a double column");
        }

        if (m_settings.predictionColumn() == null) {
            m_settings.predictionColumn(inSpecs[1].getColumnSpec(
                    inSpecs[1].getNumColumns() - 1).getName());
        }
        DataColumnSpec pSpec =
                inSpecs[1].getColumnSpec(m_settings.predictionColumn());
        if (pSpec == null) {
            throw new InvalidSettingsException("Prediction column '"
                    + m_settings.predictionColumn()
                    + "' does not exist in second input table");
        }

        ColumnRearranger crea = createRearranger(inSpecs[1]);
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
                        "Predition probability");
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
        LoopStartNode loopStart = getLoopStartNode();
        if (!(loopStart instanceof LoopStartNodeTerminator)) {
            throw new IllegalStateException("Wrong loop start node used");
        }

        if (inData[1].getRowCount() < 1) {
            throw new IllegalStateException(
                    "First input table does not have any rows");
        }

        int weightIndex =
                inData[0].getDataTableSpec().findColumnIndex(
                        m_settings.weightColumn());
        double modelWeight =
                ((DoubleValue)inData[0].iterator().next().getCell(weightIndex))
                        .getDoubleValue();

        int predictionIndex =
                inData[1].getDataTableSpec().findColumnIndex(
                        m_settings.predictionColumn());
        ExecutionContext subExec;
        if (((LoopStartNodeTerminator)loopStart).terminateLoop()) {
            subExec = exec.createSubExecutionContext(0.5);
        } else {
            subExec = exec;
        }

        final double max = inData[1].getRowCount();
        int i = 0;
        for (DataRow row : inData[1]) {
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
                    createRearranger(inData[1].getDataTableSpec());
            BufferedDataTable table =
                    exec.createColumnRearrangeTable(inData[1], crea,
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
        BoostingPredictorSettings s = new BoostingPredictorSettings();
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
