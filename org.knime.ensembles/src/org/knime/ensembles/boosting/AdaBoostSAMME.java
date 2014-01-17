/* 
 * ------------------------------------------------------------------------
 *
 *  Copyright by 
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

import java.util.Arrays;
import java.util.Random;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataRow;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionMonitor;

/**
 * This class implements AdaBoost.SAMME, as described in "Multi-class AdaBoost"
 * by Zhu et. al (2006).
 *
 * @author Thorsten Meinl, University of Konstanz
 */
public class AdaBoostSAMME implements BoostingStrategy {
    private final double[] m_weights;

    private final double[] m_samples;

    private final Random m_rand = new Random();

    private final double m_classCorrection;

    /**
     * Creates a new boosting strategy.
     *
     * @param numberOfRows the total number or rows or patterns
     * @param classCount the number of possible classes for all patterns
     */
    public AdaBoostSAMME(final int numberOfRows, final int classCount) {
        m_weights = new double[numberOfRows];
        m_samples = new double[numberOfRows];

        for (int i = 0; i < numberOfRows; i++) {
            m_weights[i] = 1.0 / numberOfRows;
            m_samples[i] = i / (double)numberOfRows;
        }

        m_classCorrection = Math.log(classCount - 1);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double sampleWeight(final int rowIndex) {
        return m_weights[rowIndex];
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int nextSample() {
        int index = Arrays.binarySearch(m_samples, m_rand.nextDouble());
        if (index < 0) {
            index = -(index + 1) - 1;
        }
        assert (index >= 0) && (index < m_samples.length);
        return index;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double[] score(final BufferedDataTable table,
            final int predictionColIndex, final int classColIndex,
            final ExecutionMonitor exec) throws CanceledExecutionException {
        if (table.getRowCount() != m_weights.length) {
            throw new IllegalStateException(
                    "Current table does not have the same number " 
                   + "of rows as the previous table");
        }

        int count = 0;
        boolean[] correct = new boolean[table.getRowCount()];
        double correctSum = 0;

        final double max = table.getRowCount();
        ExecutionMonitor subexec = exec.createSubProgress(0.8);
        exec.setMessage("Checking preditions");
        for (DataRow row : table) {
            subexec.checkCanceled();
            subexec.setProgress(count / max);
            DataCell realValue = row.getCell(classColIndex);
            DataCell predictedValue = row.getCell(predictionColIndex);
            if (realValue.equals(predictedValue)) {
                correct[count] = true;
                correctSum += m_weights[count];
            }
            count++;
        }

        exec.setMessage("Updating weights");
        double error = 1 - correctSum;
        double modelWeight = Math.log((1 - error) / error) + m_classCorrection;

        subexec = exec.createSubProgress(0.1);
        double sum = 0;
        for (int i = 0; i < correct.length; i++) {
            if (i % 1000 == 0) {
                subexec.checkCanceled();
                subexec.setProgress(i / max);
            }
            if (correct[i]) {
                m_weights[i] *= Math.exp(-modelWeight);
            }
            sum += m_weights[i];
        }

        subexec = exec.createSubProgress(0.1);
        m_weights[0] /= sum;
        for (int i = 1; i < m_weights.length; i++) {
            if (i % 1000 == 0) {
                subexec.checkCanceled();
                subexec.setProgress(i / max);
            }
            m_samples[i] = m_samples[i - 1] + m_weights[i - 1];
        }

        exec.setProgress(1);
        return new double[]{error, modelWeight};
    }
}
