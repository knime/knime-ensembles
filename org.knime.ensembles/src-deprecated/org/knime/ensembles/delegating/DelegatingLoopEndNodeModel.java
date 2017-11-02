/*
 * ------------------------------------------------------------------------
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
 * ------------------------------------------------------------------------
 */
package org.knime.ensembles.delegating;

import java.io.File;
import java.io.IOException;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.RowKey;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.workflow.LoopEndNode;

/**
 * This is the model implementation of Delegating.
 *
 *
 * @author Iris Adae, University of Konstanz, Germany
 * @deprecated use the recursive loops instead.
 */
@Deprecated
public class DelegatingLoopEndNodeModel extends NodeModel
            implements LoopEndNode {

    private BufferedDataContainer m_outcontainer;

    /**
     * this variable contains a mapping from columns that are same
     * (Same datatype and columnname)
     * if columns, of the input data table are not found in the input of the
     * loop, it tries to find a compatible column (
     * Matching at least, the data type).
     */

    private BufferedDataTable m_inData;
    private int m_iterationnr = 0;

    private SettingsModelIntegerBounded m_maxIterations = DelegatingLoopEndNodeDialog.createIterationsModel();
    private SettingsModelIntegerBounded m_minNumberOfRows = DelegatingLoopEndNodeDialog.createNumOfRowsModel();
    private SettingsModelBoolean m_onlyLastResult = DelegatingLoopEndNodeDialog.createOnlyLastModel();

    /**
     * Constructor for the node model.
     */
    protected DelegatingLoopEndNodeModel() {
        super(2, 1);
    }

    private static int collectingIn = 0;
    private static int resultingIn = 1;

    /**
     * {@inheritDoc}
     */
    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
            final ExecutionContext exec) throws Exception {
        // in port 0: collects the data provided at the output port
        // in port 1: is fed back to loop start node
        BufferedDataContainer loopData = exec.createDataContainer(inData[resultingIn].getDataTableSpec());


        for (DataRow row : inData[resultingIn]) {
            loopData.addRowToTable(createNewRow(row, row.getKey()));
        }
        loopData.close();
        m_inData  = loopData.getTable();
        m_iterationnr++;

        if (m_onlyLastResult.getBooleanValue()) {
            if (m_inData.getRowCount() < m_minNumberOfRows.getIntValue()
                    || m_iterationnr >= m_maxIterations.getIntValue()) {
                return new BufferedDataTable[]{inData[collectingIn]};
            }
        } else {
            if (m_outcontainer == null) {
                m_outcontainer = exec.createDataContainer(inData[collectingIn].getDataTableSpec());
            }

            for (DataRow row : inData[collectingIn]) {
                RowKey newKey = new RowKey(row.getKey() + "#" + m_iterationnr);
                m_outcontainer.addRowToTable(createNewRow(row, newKey));
            }

            // stop loop if there are less rows than needed.
            // or the max number of iterations is reached
            if (m_inData.getRowCount() < m_minNumberOfRows.getIntValue()
                       || m_iterationnr >= m_maxIterations.getIntValue()) {
                m_outcontainer.close();
                return new BufferedDataTable[]{m_outcontainer.getTable()};
            }
        }
        // go on with loop
        super.continueLoop();
        return new BufferedDataTable[]{null};
    }

    private DataRow createNewRow(final DataRow row, final RowKey newKey) {
        DataCell[] cells = new DataCell[row.getNumCells()];
        for (int i = 0; i < row.getNumCells(); i++) {
            cells[i] = row.getCell(i);
        }
        return new DefaultRow(newKey, cells);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void reset() {
        m_iterationnr = 0;
        m_outcontainer = null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected DataTableSpec[] configure(final DataTableSpec[] inSpecs)
            throws InvalidSettingsException {
        if (m_onlyLastResult.getBooleanValue()) {
            // the output may change over the loops
            return new DataTableSpec[]{null};
        }
        return new DataTableSpec[]{inSpecs[collectingIn]};
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        m_maxIterations.saveSettingsTo(settings);
        m_minNumberOfRows.saveSettingsTo(settings);
        m_onlyLastResult.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        m_maxIterations.loadSettingsFrom(settings);
        m_minNumberOfRows.loadSettingsFrom(settings);
        try {
            m_onlyLastResult.loadSettingsFrom(settings);
        } catch (InvalidSettingsException ise) {
            // introduced in KNIME 2.6.1
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        m_maxIterations.validateSettings(settings);
        m_minNumberOfRows.validateSettings(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadInternals(final File internDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
        // nothing to load
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveInternals(final File internDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
        // nothing to save
    }

    /**Call to get the in data table of the last iteration.
     *
     * @return the indata table of the last iteration.
     */
    public BufferedDataTable getInData() {
        return m_inData;
    }

}
