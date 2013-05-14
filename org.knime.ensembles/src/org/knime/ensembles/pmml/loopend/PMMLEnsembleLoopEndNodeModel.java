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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.dmg.pmml.MiningFieldDocument.MiningField;
import org.dmg.pmml.PMMLDocument;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.xml.PMMLCell;
import org.knime.core.data.xml.PMMLCellFactory;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.pmml.PMMLModelWrapper;
import org.knime.core.node.port.pmml.PMMLPortObject;
import org.knime.core.node.port.pmml.PMMLPortObjectSpecCreator;
import org.knime.core.node.workflow.LoopEndNode;
import org.knime.core.node.workflow.LoopStartNodeTerminator;
import org.knime.ensembles.pmml.PMMLEnsembleHelpers;
import org.knime.ensembles.pmml.PMMLMiningModelTranslator;



/**
 * This is the model implementation of PMMLEnsembleLoopEnd.
 * Collects PMML from a loop and outputs a pmml ensemble.
 *
 * @author Alexander Fillbrunn, Universitaet Konstanz
 * @since 2.8
 */
public class PMMLEnsembleLoopEndNodeModel extends NodeModel implements LoopEndNode {

    private BufferedDataContainer m_resultContainer;

    private int m_count;

    /**
     * Constructor for the node model.
     */
    protected PMMLEnsembleLoopEndNodeModel() {
        super(new PortType[]{new PortType(PMMLPortObject.class)},
                new PortType[]{new PortType(PMMLPortObject.class)});
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

    /** The name of the settings tag which holds the information whether a flow variable for weights is available. */
    public static final String WEIGHT_AVAILABLE = "weightAvailable";

    /**
     * Creates a SettingsModelString for storing the method used for treating multiple models.
     * @return the created SettingsModel
     */
    public static SettingsModelString createMultiModelMethodSettingsModel() {
        return new SettingsModelString(MULTIMODELMETHOD, MULTIMODELMETHOD_CHOICES[0]);
    }

    /**
     * Creates a SettingsModelString for storing the name of the flow variable that is used for weights.
     * @return the created SettingsModel
     */
    public static SettingsModelString createWeightFlowVarNameSettingsModel() {
        SettingsModelString sm = new SettingsModelString(WEIGHT_FLOW_VARIABLE_NAME, null);
        sm.setEnabled(false);
        return sm;
    }

    /**
     * Creates a SettingsModelBoolean for storing a value that determines
     * whether a flow variable for weights is available.
     * @return the created SettingsModel
     */
    public static SettingsModelBoolean createWeightAvailableSettingsModel() {
        return new SettingsModelBoolean(WEIGHT_AVAILABLE, false);
    }

    private SettingsModelBoolean m_weightAvailable = createWeightAvailableSettingsModel();

    private SettingsModelString m_multiModelMethod = createMultiModelMethodSettingsModel();

    private SettingsModelString m_weightFlowVarName = createWeightFlowVarNameSettingsModel();

    /**The choices a user has for determining how multiple models are treated. */
    protected static final String[] MULTIMODELMETHOD_CHOICES = new String[]{
        "Majority vote",
        "Average",
        "Maximum",
        "Sum",
        "Median",
        "Select all",
        "Select first",
        "Weighted Average",
        "Weighted Majority Vote"
    };

    /**Corresponding Enum values for Strings in MULTIMODELMETHOD_CHOICES. */
    protected static final org.dmg.pmml.MULTIPLEMODELMETHOD.Enum[] MULTIMODELMETHOD_CHOICES_ENUM
                = new org.dmg.pmml.MULTIPLEMODELMETHOD.Enum[]{
                    org.dmg.pmml.MULTIPLEMODELMETHOD.MAJORITY_VOTE,
                    org.dmg.pmml.MULTIPLEMODELMETHOD.AVERAGE,
                    org.dmg.pmml.MULTIPLEMODELMETHOD.MAX,
                    org.dmg.pmml.MULTIPLEMODELMETHOD.SUM,
                    org.dmg.pmml.MULTIPLEMODELMETHOD.MEDIAN,
                    org.dmg.pmml.MULTIPLEMODELMETHOD.SELECT_ALL,
                    org.dmg.pmml.MULTIPLEMODELMETHOD.SELECT_FIRST,
                    org.dmg.pmml.MULTIPLEMODELMETHOD.WEIGHTED_AVERAGE,
                    org.dmg.pmml.MULTIPLEMODELMETHOD.WEIGHTED_MAJORITY_VOTE
                };

    private DataTableSpec createInternalSpec(final boolean useWeights) {
        DataColumnSpecCreator pmml = new DataColumnSpecCreator(PMML_COLUMN_NAME, PMMLCell.TYPE);
        if (useWeights) {
            DataColumnSpecCreator weight = new DataColumnSpecCreator(WEIGHT_COLUMN_NAME, DoubleCell.TYPE);
            return new DataTableSpec(pmml.createSpec(), weight.createSpec());
        } else {
            return new DataTableSpec(pmml.createSpec());
        }
    }

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

        String flowVarName = m_weightFlowVarName.getStringValue();
        boolean useWeights = flowVarName != null && flowVarName.length() > 0;
        double weight = 0;
        if (useWeights) {
            weight = getAvailableFlowVariables().get(flowVarName).getDoubleValue();
        }

        if (m_resultContainer == null) {
            m_resultContainer = exec.createDataContainer(createInternalSpec(useWeights));
            m_count = 0;
        }
        PMMLDocument pmmldoc = PMMLDocument.Factory.parse(((PMMLPortObject)inData[0]).getPMMLValue().getDocument());
        DataCell pmmlCell = PMMLCellFactory.create(pmmldoc.toString());

        DefaultRow row = null;
        if (useWeights) {
            row = new DefaultRow("Row" + (m_count++), pmmlCell, new DoubleCell(weight));
        } else {
            row = new DefaultRow("Row" + (m_count++), pmmlCell);
        }
        m_resultContainer.addRowToTable(row);

         boolean terminateLoop =
             ((LoopStartNodeTerminator)this.getLoopStartNode())
                     .terminateLoop();
         if (terminateLoop) {
             // this was the last iteration - close container and continue
             m_resultContainer.close();
             BufferedDataTable table = m_resultContainer.getTable();
             DataTableSpec fakeSpec = PMMLEnsembleHelpers.createTableSpec(table, PMML_COLUMN_NAME);

             List<PMMLModelWrapper> wrappers =
                    PMMLEnsembleHelpers.getModelListFromInput(table, PMML_COLUMN_NAME);
                PMMLEnsembleHelpers.checkInputTablePMML(wrappers);

                /*
                 * Learning and target columns are lost when PMML is written in a table.
                 * Here we retrieve it from the mining schema and put it in our output pmml port.
                 */
                Set<String> targetCols = new HashSet<String>();
                Set<String> learningCols = new HashSet<String>();
                for (PMMLModelWrapper model : wrappers) {
                    if (model.getMiningSchema() != null) {
                        for (MiningField field : model.getMiningSchema().getMiningFieldList()) {
                            if (field.getUsageType() == org.dmg.pmml.FIELDUSAGETYPE.PREDICTED) {
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
                for (int i = 0; i < MULTIMODELMETHOD_CHOICES.length; i++) {
                    if (MULTIMODELMETHOD_CHOICES[i].equals(m_multiModelMethod.getStringValue())) {
                        multimodelchoice = i;
                        break;
                    }
                }

                trans = new PMMLMiningModelTranslator(table, PMML_COLUMN_NAME, useWeights ? WEIGHT_COLUMN_NAME : null,
                        MULTIMODELMETHOD_CHOICES_ENUM[multimodelchoice]);

                outPMMLPort.addModelTranslater(trans);
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
        m_resultContainer = null;
        m_count = 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected PortObjectSpec[] configure(final PortObjectSpec[] inSpecs)
            throws InvalidSettingsException {
        return new DataTableSpec[]{null};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        m_multiModelMethod.saveSettingsTo(settings);
        m_weightFlowVarName.saveSettingsTo(settings);
        m_weightAvailable.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        m_multiModelMethod.loadSettingsFrom(settings);
        m_weightFlowVarName.loadSettingsFrom(settings);
        m_weightAvailable.loadSettingsFrom(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        m_multiModelMethod.validateSettings(settings);
        m_weightFlowVarName.validateSettings(settings);
        m_weightAvailable.validateSettings(settings);
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

