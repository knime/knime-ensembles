/*
 * ------------------------------------------------------------------------
 *
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
 *   15 Nov 2019 (Alexander): created
 */
package org.knime.ensembles.pmmlpredict3;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.knime.base.node.mine.util.PredictorHelper;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.pmml.PMMLPortObjectSpec;

/**
 * Node dialog for the PMML Predictor node.
 * @author Alexander Fillbrunn, KNIME GmbH, Konstanz, Germany
 */
public class PMMLPredictorNodeDialog extends NodeDialogPane {

    private SettingsModelBoolean m_changePredictionColumnName;
    private SettingsModelString m_predColumnName;
    private SettingsModelBoolean m_appendClassProbs;
    private SettingsModelString m_probsSuffix;

    private JCheckBox m_changePredictionColumnNameCB;
    private JTextField m_predColumnNameTF;
    private JCheckBox m_appendClassProbsCB;
    private JTextField m_probsSuffixTF;

    /**
     *
     */
    public PMMLPredictorNodeDialog() {
        m_changePredictionColumnName = PredictorHelper.getInstance().createChangePrediction();
        m_predColumnName = PredictorHelper.getInstance().createPredictionColumn();
        m_appendClassProbs = PMMLPredictorNodeModel3.createAppendProbs();
        m_probsSuffix = PredictorHelper.getInstance().createSuffix();

        m_changePredictionColumnNameCB = new JCheckBox("Change name of prediction column");
        m_changePredictionColumnNameCB.addActionListener(e -> {
            boolean enabled = m_changePredictionColumnNameCB.isSelected();
            m_predColumnNameTF.setEnabled(enabled);
        });
        m_predColumnNameTF = new JTextField(20);
        m_appendClassProbsCB = new JCheckBox("Append class probabilities");
        m_appendClassProbsCB.addActionListener(e -> {
            boolean enabled = m_appendClassProbsCB.isSelected();
            m_probsSuffixTF.setEnabled(enabled);
        });
        m_probsSuffixTF = new JTextField(20);
        addTab("Options", createSettingsPanel());
    }

    /**
     * @return
     */
    private JPanel createSettingsPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;

        panel.add(createGeneralPanel(), gbc);
        gbc.gridy++;
        panel.add(createClassificationPanel(), gbc);

        return panel;
    }

    private JPanel createGeneralPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("General"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;

        panel.add(m_changePredictionColumnNameCB, gbc);
        gbc.gridy++;
        panel.add(m_predColumnNameTF, gbc);

        return panel;
    }

    private JPanel createClassificationPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Classification"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;

        panel.add(m_appendClassProbsCB, gbc);
        gbc.gridy++;
        panel.add(new JLabel("Column Name Suffix"), gbc);
        gbc.gridx++;
        panel.add(m_probsSuffixTF, gbc);

        return panel;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) throws InvalidSettingsException {
        m_predColumnName.setStringValue(m_predColumnNameTF.getText());
        m_probsSuffix.setStringValue(m_probsSuffixTF.getText());
        m_appendClassProbs.setBooleanValue(m_appendClassProbsCB.isSelected());
        m_changePredictionColumnName.setBooleanValue(m_changePredictionColumnNameCB.isSelected());

        m_appendClassProbs.saveSettingsTo(settings);
        m_changePredictionColumnName.saveSettingsTo(settings);
        m_predColumnName.saveSettingsTo(settings);
        m_probsSuffix.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadSettingsFrom(final NodeSettingsRO settings, final PortObjectSpec[] specs)
            throws NotConfigurableException {
        try {
            m_appendClassProbs.loadSettingsFrom(settings);
        } catch (InvalidSettingsException e) {
            m_appendClassProbs.setEnabled(true);
            m_appendClassProbs.setBooleanValue(false);
        }

        try {
            m_changePredictionColumnName.loadSettingsFrom(settings);
        } catch (InvalidSettingsException e) {
            m_changePredictionColumnName.setEnabled(true);
            m_changePredictionColumnName.setBooleanValue(false);
        }

        try {
            m_predColumnName.loadSettingsFrom(settings);
        } catch (InvalidSettingsException e) {
            m_predColumnName.setEnabled(true);
            m_predColumnName.setStringValue("");
        }

        try {
            m_probsSuffix.loadSettingsFrom(settings);
        } catch (InvalidSettingsException e) {
            m_probsSuffix.setEnabled(true);
            m_probsSuffix.setStringValue("");
        }

        m_appendClassProbsCB.setSelected(m_appendClassProbs.getBooleanValue());
        m_changePredictionColumnNameCB.setSelected(m_changePredictionColumnName.getBooleanValue());

        m_predColumnNameTF.setEnabled(m_changePredictionColumnNameCB.isSelected());
        m_probsSuffixTF.setText(m_probsSuffix.getStringValue());
        m_probsSuffixTF.setEnabled(m_appendClassProbsCB.isSelected());
        Iterator<DataColumnSpec> tciter = ((PMMLPortObjectSpec)specs[0]).getTargetCols().iterator();
        if (!m_changePredictionColumnNameCB.isSelected() || m_predColumnName.getStringValue() == null) {
            if (tciter.hasNext()) {
                m_predColumnNameTF.setText(
                    PredictorHelper.getInstance().computePredictionDefault(tciter.next().getName()));
            } else {
                m_predColumnNameTF.setText("Cluster");
            }
        } else {
            m_predColumnNameTF.setText(m_predColumnName.getStringValue());
        }
    }
}
