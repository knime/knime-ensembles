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
 *   31.03.2011 (meinl): created
 */
package org.knime.ensembles.boosting;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

/**
 * This class holds the settings for the start node of a boosting predictor
 * loop.
 *
 * @author Thorsten Meinl, University of Konstanz
 */
public class BoostingPredictorStartSettings {
    private String m_modelColumn;

    private String m_weightColumn;

    /**
     * Returns the column containing the collected models.
     *
     * @return a column name
     */
    public String modelColumn() {
        return m_modelColumn;
    }

    /**
     * Sets the column containing the collected models.
     *
     * @param colName a column name
     */
    public void modelColumn(final String colName) {
        m_modelColumn = colName;
    }

    /**
     * Returns the column containing the models' weights.
     *
     * @return a column name
     */
    public String weightColumn() {
        return m_weightColumn;
    }

    /**
     * Sets the column containing the models' weights.
     *
     * @param colName a column name
     */
    public void weightColumn(final String colName) {
        m_weightColumn = colName;
    }

    /**
     * Loads the settings from the given settings object.
     *
     * @param settings a settings object
     * @throws InvalidSettingsException if some settings are missing
     */
    public void loadSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        m_modelColumn = settings.getString("modelColumn");
        m_weightColumn = settings.getString("weightColumn");
    }

    /**
     * Loads the settings from the given settings object using default values
     * for missing settings.
     *
     * @param settings a settings object
     */
    public void loadSettingsForDialog(final NodeSettingsRO settings) {
        m_modelColumn = settings.getString("modelColumn", null);
        m_weightColumn = settings.getString("weightColumn", null);
    }

    /**
     * Saves the settings into the given settings object.
     *
     * @param settings a settings object
     */
    public void saveSettings(final NodeSettingsWO settings) {
        settings.addString("modelColumn", m_modelColumn);
        settings.addString("weightColumn", m_weightColumn);
    }
}
