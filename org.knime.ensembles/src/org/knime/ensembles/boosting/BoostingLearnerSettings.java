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
 *   29.03.2011 (meinl): created
 */
package org.knime.ensembles.boosting;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

/**
 * This class holds the settings for the boosting learner loop.
 *
 * @author Thorsten Meinl, University of Konstanz
 */
public class BoostingLearnerSettings {
    private int m_maxIterations = 100;

    private String m_classColumn;

    private String m_predictionColumn;

    private boolean m_useSeed;

    private long m_randomSeed;

    /**
     * Returns the name of the column containing the predicted classes.
     *
     * @return a column name
     */
    public String predictionColumn() {
        return m_predictionColumn;
    }

    /**
     * Sets the name of the column containing the predicted classes.
     *
     * @param colName a column name
     */
    public void predictionColumn(final String colName) {
        m_predictionColumn = colName;
    }

    /**
     * Returns the name of the column containing the real classes.
     *
     * @return a column name
     */
    public String classColumn() {
        return m_classColumn;
    }

    /**
     * Sets the name of the column containing the real classes.
     *
     * @param colName a column name
     */
    public void classColumn(final String colName) {
        m_classColumn = colName;
    }

    /**
     * Returns the maximum number of loop iterations.
     *
     * @return the number of loop iterations
     */
    public int maxIterations() {
        return m_maxIterations;
    }

    /**
     * Sets the maximum number of loop iterations.
     *
     * @param max the number of loop iterations
     */
    public void maxIterations(final int max) {
        m_maxIterations = max;
    }

    /**
     * Returns whether a fixed seed for the random number generator should be used.
     *
     * @return <code>true</code> if a fixed seed should be used, <code>false</code> otherwise
     */
    public boolean useSeed() {
        return m_useSeed;
    }

    /**
     * Sets whether a fixed seed for the random number generator should be used.
     *
     * @param b <code>true</code> if a fixed seed should be used, <code>false</code> otherwise
     */
    public void useSeed(final boolean b) {
        m_useSeed = b;
    }

    /**
     * Returns the seed for the random number generator.
     *
     * @return the seed
     */
    public long randomSeed() {
        return m_randomSeed;
    }

    /**
     * Sets the seed for the random number generator.
     *
     * @param seed the seed
     */
    public void randomSeed(final long seed) {
        m_randomSeed = seed;
    }

    /**
     * Loads the settings from the given settings object.
     *
     * @param settings a settings object
     * @throws InvalidSettingsException if some settings are missing
     */
    public void loadSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        m_maxIterations = settings.getInt("maxIterations");
        m_classColumn = settings.getString("classColumn");
        m_predictionColumn = settings.getString("predictionColumn");
        // since 2.11
        m_useSeed = settings.getBoolean("useSeed", false);
        m_randomSeed = settings.getLong("randomSeed", System.currentTimeMillis());
    }

    /**
     * Loads the settings from the given settings object using default values
     * for missing settings.
     *
     * @param settings a settings object
     */
    public void loadSettingsForDialog(final NodeSettingsRO settings) {
        m_maxIterations = settings.getInt("maxIterations", 100);
        m_classColumn = settings.getString("classColumn", null);
        m_predictionColumn = settings.getString("predictionColumn", null);
        m_useSeed = settings.getBoolean("useSeed", false);
        m_randomSeed = settings.getLong("randomSeed", System.currentTimeMillis());
    }

    /**
     * Saves the settings into the given settings object.
     *
     * @param settings a settings object
     */
    public void saveSettings(final NodeSettingsWO settings) {
        settings.addInt("maxIterations", m_maxIterations);
        settings.addString("classColumn", m_classColumn);
        settings.addString("predictionColumn", m_predictionColumn);
        settings.addBoolean("useSeed", m_useSeed);
        settings.addLong("randomSeed", m_randomSeed);
    }
}
