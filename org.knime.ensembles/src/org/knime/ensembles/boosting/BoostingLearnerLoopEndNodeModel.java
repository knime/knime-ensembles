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
 *   29.03.2011 (meinl): created
 */
package org.knime.ensembles.boosting;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.RowKey;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.model.PortObjectCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.node.workflow.LoopEndNode;

/**
 *
 * @author Thorsten Meinl, University of Konstanz
 */
public class BoostingLearnerLoopEndNodeModel extends NodeModel implements
        LoopEndNode {
    private BoostingWeights m_weightModel;

    private final BoostingLearnerSettings m_settings =
            new BoostingLearnerSettings();

    private int m_iteration;

    private BufferedDataContainer m_container;

    private static final double MIN_MODEL_WEIGHT = 0.01;

    private static final DataTableSpec OUT_SPEC;

    static {
        DataColumnSpec modelSpec =
                new DataColumnSpecCreator("Models", PortObjectCell.TYPE)
                        .createSpec();
        DataColumnSpec weightSpec =
                new DataColumnSpecCreator("Model weight", DoubleCell.TYPE)
                        .createSpec();
        DataColumnSpec errorSpec =
                new DataColumnSpecCreator("Model error", DoubleCell.TYPE)
                        .createSpec();
        OUT_SPEC = new DataTableSpec(modelSpec, weightSpec, errorSpec);
    }

    public BoostingLearnerLoopEndNodeModel() {
        super(new PortType[]{new PortType(PortObject.class),
                BufferedDataTable.TYPE}, new PortType[]{BufferedDataTable.TYPE});
    }

    BoostingWeights getWeightModel() {
        return m_weightModel;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected DataTableSpec[] configure(final PortObjectSpec[] inSpecs)
            throws InvalidSettingsException {
        DataTableSpec spec = (DataTableSpec)inSpecs[1];

        if (spec.getNumColumns() < 2) {
            throw new InvalidSettingsException(
                    "Second input table must have at least two column");
        }

        if (m_settings.classColumn() == null) {
            m_settings.classColumn(spec.getColumnSpec(spec.getNumColumns() - 2)
                    .getName());
        }
        DataColumnSpec cSpec = spec.getColumnSpec(m_settings.classColumn());
        if (cSpec == null) {
            throw new InvalidSettingsException("Class column '"
                    + m_settings.classColumn()
                    + "' does not exist in second input table.");
        }

        if (m_settings.predictionColumn() == null) {
            m_settings.predictionColumn(spec.getColumnSpec(
                    spec.getNumColumns() - 1).getName());
        }
        DataColumnSpec pSpec =
                spec.getColumnSpec(m_settings.predictionColumn());
        if (pSpec == null) {
            throw new InvalidSettingsException("Prediction column '"
                    + m_settings.predictionColumn()
                    + "' does not exist in second input table.");
        }

        return new DataTableSpec[]{OUT_SPEC};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected PortObject[] execute(final PortObject[] inData,
            final ExecutionContext exec) throws Exception {
        BufferedDataTable data = (BufferedDataTable)inData[1];

        int classIndex =
                data.getDataTableSpec().findColumnIndex(
                        m_settings.classColumn());
        int predictionIndex =
                data.getDataTableSpec().findColumnIndex(
                        m_settings.predictionColumn());
        if (m_weightModel == null) {
            Set<DataCell> domain =
                    data.getDataTableSpec().getColumnSpec(classIndex)
                            .getDomain().getValues();
            if (domain == null) {
                domain = new HashSet<DataCell>();
                for (DataRow row : data) {
                    domain.add(row.getCell(classIndex));
                }
            }
            m_weightModel =
                    new AdaBoostWeights(data.getRowCount(), domain.size());
            m_container = exec.createDataContainer(OUT_SPEC);
        }

        double[] res = m_weightModel.score(data, predictionIndex, classIndex);

        m_iteration++;
        if (m_iteration >= m_settings.maxIterations()
                || (res[1] <= MIN_MODEL_WEIGHT)) {
            if (res[1] < MIN_MODEL_WEIGHT) {
                setWarningMessage("Prediction error too big. Finishing.");
            }
            m_container.close();
            return new PortObject[]{m_container.getTable()};
        } else {
            DataRow row =
                    new DefaultRow(RowKey.createRowKey(m_iteration),
                            new PortObjectCell(inData[0]), new DoubleCell(
                                    res[1]), new DoubleCell(res[0]));
            m_container.addRowToTable(row);

            exec.setProgress(m_iteration / (double)m_settings.maxIterations(),
                    "Model error " + res[1]);
            continueLoop();
            return new PortObject[]{null};
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
        BoostingLearnerSettings s = new BoostingLearnerSettings();
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
        m_weightModel = null;
        m_iteration = 0;
        m_container = null;
    }
}
