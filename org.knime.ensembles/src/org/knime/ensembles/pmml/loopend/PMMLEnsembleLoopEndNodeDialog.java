/*
 * ------------------------------------------------------------------------
 *
 *  Copyright (C) 2003 - 2013
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
 * ------------------------------------------------------------------------
 */
package org.knime.ensembles.pmml.loopend;

import java.util.Collection;
import java.util.Map;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentFlowVariableNameSelection;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.workflow.FlowVariable;
import org.knime.ensembles.pmml.combine.PMMLEnsembleNodeModel;


/**
 * <code>NodeDialog</code> for the "PMMLEnsembleLoopEnd" Node.
 * Collects PMML from a loop and outputs a pmml ensemble.
 *
 * @author Alexander Fillbrunn, Universitaet Konstanz
 * @since 2.8
 */
public class PMMLEnsembleLoopEndNodeDialog extends DefaultNodeSettingsPane {

    private static final NodeLogger LOGGER =
        NodeLogger.getLogger(PMMLEnsembleLoopEndNodeDialog.class);    
    
    private SettingsModelString m_flowVarSettingsModel;
    private DialogComponentFlowVariableNameSelection m_flowVarSelection;
    /**
     * New pane for configuring PMMLEnsembleLoopEnd node dialog.
     * This is just a suggestion to demonstrate possible default dialog
     * components.
     */
    protected PMMLEnsembleLoopEndNodeDialog() {
        super();        

        Collection<FlowVariable> flowVars = getAvailableFlowVariables().values();
        
        DialogComponentBoolean weightAvailable = new DialogComponentBoolean(
                PMMLEnsembleLoopEndNodeModel.createWeightAvailableSettingsModel(), "Weight available");

        m_flowVarSettingsModel = PMMLEnsembleLoopEndNodeModel.createWeightFlowVarNameSettingsModel();
        m_flowVarSelection = new DialogComponentFlowVariableNameSelection(
                m_flowVarSettingsModel, "Weight flow variable",
                flowVars, FlowVariable.Type.DOUBLE);
        
        weightAvailable.getModel().addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(final ChangeEvent arg0) {
                m_flowVarSelection.getModel().setEnabled(((SettingsModelBoolean)arg0.getSource()).getBooleanValue());
            }
        });
        
        DialogComponentStringSelection multModelSelection = new DialogComponentStringSelection(
                PMMLEnsembleNodeModel.createMultiModelMethodSettingsModel(),
                "Multiple models method", PMMLEnsembleNodeModel.MULTIMODELMETHOD_CHOICES);
        
        addDialogComponent(weightAvailable);
        addDialogComponent(m_flowVarSelection);
        addDialogComponent(multModelSelection);
    }
    
    /**
     * List of available string flow variables must be updated since it could
     * have changed.
     * 
     * {@inheritDoc}
     */
    @Override
    public void loadAdditionalSettingsFrom(final NodeSettingsRO settings,
            final PortObjectSpec[] specs) throws NotConfigurableException {
        super.loadAdditionalSettingsFrom(settings, specs);
        Map<String, FlowVariable> flowVars = getAvailableFlowVariables();
        
        // check for selected value
        String flowVar = "";
        try {
            flowVar = ((SettingsModelString)m_flowVarSettingsModel
                            .createCloneWithValidatedValue(settings))
                            .getStringValue();
        } catch (InvalidSettingsException e) {
            LOGGER.debug("Settings model could not be cloned with given settings!");
        } finally {
            m_flowVarSelection.replaceListItems(flowVars.values(), flowVar);
        }
    }
}

