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

import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionMonitor;

/**
 * This interface describes a certain boosting strategy, e.g. AdaBoost.
 *
 * @author Thorsten Meinl, University of Konstanz
 */
public interface BoostingStrategy {
    /**
     * Scores the result of the current prediction and updates the weights
     * accordingly.
     *
     * @param table the table containing the prediction and the real class
     *            values
     * @param predictionColIndex the prediction column's index
     * @param classColIndex the real class column's index
     * @param exec an execution monitor for reporting progress and checking for
     *            cancellation
     * @return the current model's error at index 0 and the corresponding model
     *         weight at index 1
     * @throws CanceledExecutionException if the user canceled the execution
     */
    public double[] score(BufferedDataTable table, int predictionColIndex,
            int classColIndex, ExecutionMonitor exec)
            throws CanceledExecutionException;

    /**
     * Return the current weight for the given row index.
     *
     * @param rowIndex the row's index
     * @return the current weight
     */
    public double sampleWeight(final int rowIndex);

    /**
     * Repeat calls to this method return the row indices in proportion to their
     * current weights. Rows with higher weights have a higher probability to be
     * returned more often than rows with lower weights.
     *
     * @return the index of a single row
     */
    public int nextSample();
}
