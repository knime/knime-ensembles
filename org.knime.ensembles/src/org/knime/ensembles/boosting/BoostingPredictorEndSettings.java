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

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

/**
 * This class holds the settings for the end node of a boosting predictor loop.
 *
 * @author Thorsten Meinl, University of Konstanz
 */
public class BoostingPredictorEndSettings {
    private String m_predictionColumn;

    /**
     * Returns the name of the column containing the predicted class values.
     *
     * @return a column name
     */
    public String predictionColumn() {
        return m_predictionColumn;
    }

    /**
     * Sets the name of the column containing the predicted class values.
     *
     * @param colName a column name
     */
    public void predictionColumn(final String colName) {
        m_predictionColumn = colName;
    }

    /**
     * Loads the settings from the given settings object.
     *
     * @param settings a settings object
     * @throws InvalidSettingsException if some settings are missing
     */
    public void loadSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        m_predictionColumn = settings.getString("predictionColumn");
    }

    /**
     * Loads the settings from the given settings object using default values
     * for missing settings.
     *
     * @param settings a settings object
     */
    public void loadSettingsForDialog(final NodeSettingsRO settings) {
        m_predictionColumn = settings.getString("predictionColumn", null);
    }

    /**
     * Saves the settings into the given settings object.
     *
     * @param settings a settings object
     */
    public void saveSettings(final NodeSettingsWO settings) {
        settings.addString("predictionColumn", m_predictionColumn);
    }
}
