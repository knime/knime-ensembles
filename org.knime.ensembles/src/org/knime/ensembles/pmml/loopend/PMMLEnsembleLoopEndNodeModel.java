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
 * ------------------------------------------------------------------------
 */

package org.knime.ensembles.pmml.loopend;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.dmg.pmml.MiningFieldDocument.MiningField;
import org.dmg.pmml.PMMLDocument;
import org.knime.core.data.DataTableSpec;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.flowvariable.FlowVariablePortObject;
import org.knime.core.node.port.pmml.PMMLModelWrapper;
import org.knime.core.node.port.pmml.PMMLPortObject;
import org.knime.core.node.port.pmml.PMMLPortObjectSpecCreator;
import org.knime.core.node.workflow.FlowVariable;
import org.knime.core.node.workflow.LoopEndNode;
import org.knime.core.node.workflow.LoopStartNodeTerminator;
import org.knime.ensembles.pmml.PMMLEnsembleHelpers;
import org.knime.ensembles.pmml.PMMLMiningModelTranslator;
import org.knime.ensembles.pmml.combine.PMMLEnsembleNodeModel;



/**
 * This is the model implementation of PMMLEnsembleLoopEnd.
 * Collects PMML from a loop and outputs a pmml ensemble.
 *
 * @author Alexander Fillbrunn, Universitaet Konstanz
 * @since 2.8
 */
public class PMMLEnsembleLoopEndNodeModel extends NodeModel implements LoopEndNode {

    private List<PMMLDocument> m_documents;
    private List<Double> m_weights;

    /**
     * Constructor for the node model.
     */
    protected PMMLEnsembleLoopEndNodeModel() {
        super(new PortType[]{FlowVariablePortObject.TYPE_OPTIONAL , PMMLPortObject.TYPE},
                new PortType[]{PMMLPortObject.TYPE});
        m_documents = null;
        m_weights = null;
    }

    /**
     * The default name for the pmml column.
     */
    public static final String PMML_COLUMN_NAME = "PMML";

    /**
     * The default name for the weight column.
     */
    public static final String WEIGHT_COLUMN_NAME = "Weight";

    /**The name of the settings tag which determines how multiple models should be treated. */
    public static final String MULTIMODELMETHOD = "multiModelMethod";

    /** The name of the settings tag which holds the name for the weight flow variable. */
    public static final String WEIGHT_FLOW_VARIABLE_NAME = "weightFlowVarName";

    /**
     * Creates a SettingsModelString for storing the method used for treating multiple models.
     * @return the created SettingsModel
     */
    public static SettingsModelString createMultiModelMethodSettingsModel() {
        return new SettingsModelString(MULTIMODELMETHOD, PMMLEnsembleNodeModel.MULTIMODELMETHOD_CHOICES[0]);
    }

    /**
     * Creates a SettingsModelString for storing the name of the flow variable that is used for weights.
     * @return the created SettingsModel
     */
    public static SettingsModelString createWeightFlowVarNameSettingsModel() {
        return new SettingsModelString(WEIGHT_FLOW_VARIABLE_NAME, null);
    }

    private SettingsModelString m_multiModelMethod = createMultiModelMethodSettingsModel();

    private SettingsModelString m_weightFlowVarName = createWeightFlowVarNameSettingsModel();

    /**
     * {@inheritDoc}
     */
    @Override
    protected PortObject[] execute(final PortObject[] inData,
            final ExecutionContext exec) throws Exception {
        if (!(this.getLoopStartNode() instanceof LoopStartNodeTerminator)) {
            throw new IllegalStateException("Loop End is not connected"
                    + " to matching/corresponding Loop Start node. You"
                    + " are trying to create an infinite loop!");
        }

        if (m_documents == null) {
            m_documents = new ArrayList<PMMLDocument>();
        }

        String flowVarName = m_weightFlowVarName.getStringValue();
        boolean useWeights = flowVarName != null && !flowVarName.equals("NONE") && flowVarName.length() > 0;
        double weight = 0;
        if (useWeights) {
            if (m_weights == null) {
                m_weights = new ArrayList<Double>();
            }
            FlowVariable fv = getAvailableFlowVariables().get(flowVarName);
            if (fv == null) {
                useWeights = false;
            } else {
                weight = fv.getDoubleValue();
                if (Double.isNaN(weight)) {
                    weight = fv.getIntValue();
                }
            }
        }

        PMMLDocument pmmldoc = PMMLDocument.Factory.parse(((PMMLPortObject)inData[1]).getPMMLValue().getDocument());

         m_documents.add(pmmldoc);
         if (useWeights) {
             m_weights.add(weight);
         }

         boolean terminateLoop = ((LoopStartNodeTerminator)this.getLoopStartNode()).terminateLoop();
         if (terminateLoop) {
             exec.setMessage("Generating output ensemble");

             DataTableSpec fakeSpec = PMMLEnsembleHelpers.createTableSpec(m_documents, exec);
             List<PMMLModelWrapper> wrappers = PMMLEnsembleHelpers.getModelListFromDocuments(m_documents, exec);
             PMMLEnsembleHelpers.checkInputTablePMML(wrappers);

                /*
                 * Learning and target columns are lost when PMML is written in a table.
                 * Here we retrieve it from the mining schema and put it in our output pmml port.
                 */
                Set<String> targetCols = new LinkedHashSet<>();
                Set<String> learningCols = new LinkedHashSet<>();
                for (PMMLModelWrapper model : wrappers) {
                    exec.checkCanceled();
                    if (model.getMiningSchema() != null) {
                        for (MiningField field : model.getMiningSchema().getMiningFieldList()) {
                            if (field.getUsageType() == org.dmg.pmml.FIELDUSAGETYPE.PREDICTED
                                    || field.getUsageType() == org.dmg.pmml.FIELDUSAGETYPE.TARGET) {
                                targetCols.add(field.getName());
                            } else if (field.getUsageType() == org.dmg.pmml.FIELDUSAGETYPE.ACTIVE) {
                                learningCols.add(field.getName());
                            }
                        }
                    }
                }

                PMMLPortObjectSpecCreator creator = new PMMLPortObjectSpecCreator(fakeSpec);
                creator.setTargetColsNames(new ArrayList<String>(targetCols));
                creator.setLearningColsNames(new ArrayList<String>(learningCols));
                PMMLPortObject outPMMLPort = new PMMLPortObject(creator.createSpec());
                PMMLMiningModelTranslator trans;

                //Find the corresponding MultiModelMethod value for the selected string
                int multimodelchoice = -1;
                for (int i = 0; i < PMMLEnsembleNodeModel.MULTIMODELMETHOD_CHOICES.length; i++) {
                    if (PMMLEnsembleNodeModel.MULTIMODELMETHOD_CHOICES[i].equals(m_multiModelMethod.getStringValue())) {
                        multimodelchoice = i;
                        break;
                    }
                }

                trans = new PMMLMiningModelTranslator(m_documents, m_weights,
                    PMMLEnsembleNodeModel.MULTIMODELMETHOD_CHOICES_ENUM[multimodelchoice]);

                outPMMLPort.addModelTranslater(trans);

                m_weights = null;
                m_documents = null;

                return new PortObject[]{outPMMLPort};
         } else {
             continueLoop();
             return new PMMLPortObject[1];
         }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void reset() {
        m_documents = null;
        m_weights = null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected PortObjectSpec[] configure(final PortObjectSpec[] inSpecs)
            throws InvalidSettingsException {
        return new PortObjectSpec[]{inSpecs[1]};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        m_multiModelMethod.saveSettingsTo(settings);
        m_weightFlowVarName.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        m_multiModelMethod.loadSettingsFrom(settings);
        m_weightFlowVarName.loadSettingsFrom(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        m_multiModelMethod.validateSettings(settings);
        m_weightFlowVarName.validateSettings(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadInternals(final File internDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveInternals(final File internDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
    }

}

