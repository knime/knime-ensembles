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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.Border;

import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataValue;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.util.ColumnSelectionComboxBox;

/**
 *
 * @author Thorsten Meinl, University of Konstanz
 */
public class BoostingLearnerNodeDialog extends NodeDialogPane {
    private final ColumnSelectionComboxBox m_classColumn =
            new ColumnSelectionComboxBox((Border)null, DataValue.class);

    private final ColumnSelectionComboxBox m_predictionColumn =
            new ColumnSelectionComboxBox((Border)null, DataValue.class);

    private final JSpinner m_iterations = new JSpinner(new SpinnerNumberModel(
            1000, 1, Integer.MAX_VALUE, 1));

    private final BoostingLearnerSettings m_settings =
            new BoostingLearnerSettings();

    /**
     *
     */
    public BoostingLearnerNodeDialog() {
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(2, 0, 2, 0);

        c.gridx = 0;
        c.gridy = 0;
        p.add(new JLabel("Real class column   "), c);
        c.gridx = 1;
        p.add(m_classColumn, c);

        c.gridx = 0;
        c.gridy++;
        p.add(new JLabel("Predicted class column   "), c);
        c.gridx = 1;
        p.add(m_predictionColumn, c);

        c.gridx = 0;
        c.gridy++;
        p.add(new JLabel("Number of iterations   "), c);
        c.gridx = 1;
        p.add(m_iterations, c);

        addTab("Standard settings", p);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadSettingsFrom(final NodeSettingsRO settings,
            final PortObjectSpec[] specs) throws NotConfigurableException {
        m_settings.loadSettingsForDialog(settings);

        m_classColumn.update((DataTableSpec)specs[1], m_settings.classColumn());
        m_predictionColumn.update((DataTableSpec)specs[1],
                m_settings.predictionColumn());
        m_iterations.setValue(m_settings.maxIterations());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings)
            throws InvalidSettingsException {
        m_settings.maxIterations((Integer)m_iterations.getValue());
        m_settings.classColumn(m_classColumn.getSelectedColumn());
        m_settings.predictionColumn(m_predictionColumn.getSelectedColumn());
        m_settings.saveSettings(settings);
    }
}
