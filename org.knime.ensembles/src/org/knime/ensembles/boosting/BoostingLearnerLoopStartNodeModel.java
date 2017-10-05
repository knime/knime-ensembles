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
 *   29.03.2011 (meinl): created
 */
package org.knime.ensembles.boosting;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

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
import org.knime.core.node.workflow.LoopStartNode;

/**
 * This class is the model for the boosting learner loop start node. It gets the
 * row/pattern weights from the corresponding loop end node and re-samples the
 * training data in each iteration accordingsly.
 *
 * @author Thorsten Meinl, University of Konstanz
 */
public class BoostingLearnerLoopStartNodeModel extends NodeModel implements
        LoopStartNode {
    private static final int OVERSAMPLE_RATE = 3;

    /**
     * Creates a new node model.
     */
    public BoostingLearnerLoopStartNodeModel() {
        super(1, 2);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected DataTableSpec[] configure(final DataTableSpec[] inSpecs)
            throws InvalidSettingsException {
        return new DataTableSpec[]{inSpecs[0], inSpecs[0]};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
            final ExecutionContext exec) throws Exception {
        if (getLoopEndNode() == null) {
            // first iteration
            return new BufferedDataTable[]{inData[0], inData[0]};
        }
        if (!(getLoopEndNode() instanceof BoostingLearnerLoopEndNodeModel)) {
            throw new InvalidSettingsException(
                    "Loop end node is not a boosting loop end node");
        }

        final BoostingStrategy strategy =
                ((BoostingLearnerLoopEndNodeModel)getLoopEndNode())
                        .getBoostingStrategy();

        final int rowCount = inData[0].getRowCount();
        final int[] distribution = new int[OVERSAMPLE_RATE * rowCount];
        for (int i = 0; i < distribution.length; i++) {
            distribution[i] = strategy.nextSample();
        }
        Arrays.sort(distribution);

        final BufferedDataContainer train =
                exec.createDataContainer(inData[0].getDataTableSpec());

        final double max = rowCount;
        int rowNo = 0, i = 0;
        for (DataRow row : inData[0]) {
            while ((i < distribution.length) && (distribution[i] == rowNo)) {
                RowKey newKey = new RowKey(row.getKey().toString() + "#" + i);
                DefaultRow newRow = new DefaultRow(newKey, row);
                train.addRowToTable(newRow);
                i++;
            }

            exec.setProgress(rowNo / max);
            if (rowNo % 100 == 0) {
                exec.checkCanceled();
            }
            rowNo++;
        }
        train.close();

        return new BufferedDataTable[]{train.getTable(), inData[0]};
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
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void reset() {
    }
}
