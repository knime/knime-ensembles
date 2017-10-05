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
package org.knime.ensembles.pmml.combine;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.dmg.pmml.PMMLDocument;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataValue;
import org.knime.core.data.DoubleValue;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.xml.PMMLValue;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelColumnName;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.pmml.PMMLPortObject;


/**
 * This is the model implementation of PMMLEnsemble.
 *
 * @author Alexander Fillbrunn, Universitaet Konstanz
 * @since 2.8
 */
public class PMMLEnsembleNodeModel extends NodeModel {

    private static final NodeLogger LOGGER =
        NodeLogger.getLogger(PMMLEnsembleNodeModel.class);

    /**
     * Constructor for the node model.
     */
    protected PMMLEnsembleNodeModel() {
        super(new PortType[]{BufferedDataTable.TYPE},
              new PortType[]{PMMLPortObject.TYPE});
    }

    /**The name of the settings tag which holds the name of the PMML
     * column the user has entered in the dialog as <code>String</code>.*/
    public static final String PMML_COL_NAME = "pmmlCol";

    /**The name of the settings tag which holds the name of the model weight
     * column the user has entered in the dialog as <code>String</code>.*/
    public static final String WEIGHT_COL_NAME = "weightCol";

    /**The name of the settings tag which determines whether a weight column should be used.*/
    public static final String WEIGHT_AVAILABLE = "weightAvailable";

    /**The name of the settings tag which determines how multiple models should be treated. */
    public static final String MULTIMODELMETHOD = "multiModelMethod";

    /**The choices a user has for determining how multiple models are treated. */
    public static final String[] MULTIMODELMETHOD_CHOICES = new String[]{
        "Majority vote",
        "Average",
        "Maximum",
        "Sum",
        "Median",
        //"Model chain", -> currently not supported
        "Select all",
        "Select first",
        "Weighted Average",
        "Weighted Majority Vote"
    };



    private SettingsModelColumnName m_pmmlColumn = createPMMLColumnSettingsModel();

    private SettingsModelBoolean m_weightAvailable = createWeightAvailableSettingsModel();

    private SettingsModelColumnName m_weightColumn = createWeightColumnSettingsModel();

    private SettingsModelString m_multiModelMethod = createMultiModelMethodSettingsModel();

    /**Corresponding Enum values for Strings in MULTIMODELMETHOD_CHOICES.
     * http://www.dmg.org/v4-0-1/MultipleModels.html
     * */
    public static final org.dmg.pmml.MULTIPLEMODELMETHOD.Enum[] MULTIMODELMETHOD_CHOICES_ENUM
            = new org.dmg.pmml.MULTIPLEMODELMETHOD.Enum[]{
                org.dmg.pmml.MULTIPLEMODELMETHOD.MAJORITY_VOTE,
                org.dmg.pmml.MULTIPLEMODELMETHOD.AVERAGE,
                org.dmg.pmml.MULTIPLEMODELMETHOD.MAX,
                org.dmg.pmml.MULTIPLEMODELMETHOD.SUM,
                org.dmg.pmml.MULTIPLEMODELMETHOD.MEDIAN,
                //org.dmg.pmml.MULTIPLEMODELMETHOD.MODEL_CHAIN, -> currently not supported
                org.dmg.pmml.MULTIPLEMODELMETHOD.SELECT_ALL,
                org.dmg.pmml.MULTIPLEMODELMETHOD.SELECT_FIRST,
                org.dmg.pmml.MULTIPLEMODELMETHOD.WEIGHTED_AVERAGE,
                org.dmg.pmml.MULTIPLEMODELMETHOD.WEIGHTED_MAJORITY_VOTE
    };

    /**
     * Creates a SettingsModelColumnName for storing the column name of the pmml clolumn.
     *
     * @return Returns the created SettingsModel
     */
    public static SettingsModelColumnName createPMMLColumnSettingsModel() {
        return new SettingsModelColumnName(PMML_COL_NAME, "PMML");
    }

    /**
     * Creates a SettingsModelColumnName for storing the column name of the weight column.
     * @return Returns the created SettingsModel
     */
    public static SettingsModelColumnName createWeightColumnSettingsModel() {
        SettingsModelColumnName model = new SettingsModelColumnName(WEIGHT_COL_NAME, "P");
        model.setEnabled(false);
        return model;
    }

    /**
     *  Creates a SettingsModelBoolean for storing if a weight column is available in the input table.
     * @return Returns the created SettingsModel
     */
    public static SettingsModelBoolean createWeightAvailableSettingsModel() {
        return new SettingsModelBoolean(WEIGHT_AVAILABLE, false);
    }

    /**
     * Creates a SettingsModelString for storing the method used for treating multiple models.
     * @return Returns the created SettingsModel
     */
    public static SettingsModelString createMultiModelMethodSettingsModel() {
        return new SettingsModelString(MULTIMODELMETHOD, "Majority vote");
    }

    private String getColumnCompatibleWith(final DataTableSpec inSpec, final Class<? extends DataValue> valueClass) {
        for (int c = 0; c < inSpec.getNumColumns(); c++) {
            DataColumnSpec colSpec = inSpec.getColumnSpec(c);
            if (colSpec.getType().isCompatible(valueClass)) {
                return colSpec.getName();
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected PortObject[] execute(final PortObject[] inData,
            final ExecutionContext exec) throws Exception {

        BufferedDataTable inTable = (BufferedDataTable)inData[0];

        // Autoconfigure & autoguess
        String pmmlColumnName = m_pmmlColumn.getColumnName();
        String weightColName = m_weightColumn.getColumnName();

        DataTableSpec inSpec = inTable.getDataTableSpec();
        if (pmmlColumnName == null || pmmlColumnName.length() == 0) {
            pmmlColumnName = getColumnCompatibleWith(inSpec, PMMLValue.class);
            m_pmmlColumn.setStringValue(pmmlColumnName);
        } else {
            DataColumnSpec s = inSpec.getColumnSpec(pmmlColumnName);
            if (s == null || !s.getType().isCompatible(PMMLValue.class)) {
                pmmlColumnName = getColumnCompatibleWith(inSpec, PMMLValue.class);
                if (pmmlColumnName != null) {
                    LOGGER.warn("Cannot use the given column for pmml. Using column " + pmmlColumnName);
                }
                throw new InvalidSettingsException("No suitable pmml column found");
            }
        }
        if (m_weightAvailable.getBooleanValue()) {
            if (weightColName == null || weightColName.length() == 0) {
                weightColName = getColumnCompatibleWith(inSpec, DoubleValue.class);
                m_weightColumn.setStringValue(weightColName);
            } else {
                DataColumnSpec s = inSpec.getColumnSpec(weightColName);
                if (s == null || !s.getType().isCompatible(DoubleValue.class)) {
                    weightColName = getColumnCompatibleWith(inSpec, DoubleValue.class);
                    if (weightColName != null) {
                        LOGGER.warn("Cannot use the given column for weights. Using column " + weightColName);
                    }
                    throw new InvalidSettingsException("No suitable double column found for weights");
                }
            }
        }
        ArrayList<PMMLDocument> documents = new ArrayList<PMMLDocument>();
        ArrayList<Double> weights = null;
        if (m_weightAvailable.getBooleanValue()) {
            weights = new ArrayList<Double>();
        }
        DataTableSpec dtspec = inTable.getDataTableSpec();
        int pmmlColIndex = dtspec.findColumnIndex(pmmlColumnName);
        int weightColIndex = dtspec.findColumnIndex(m_weightColumn.getColumnName());
        exec.setMessage("Parsing models");
        exec.setProgress(0.1);
        for (DataRow r : inTable) {
            exec.checkCanceled();
            PMMLValue val = (PMMLValue) r.getCell(pmmlColIndex);
            PMMLDocument pmmldoc = PMMLDocument.Factory.parse(val.getDocument());
            documents.add(pmmldoc);

            if (weights != null) {
                Double w = ((DoubleCell)r.getCell(weightColIndex)).getDoubleValue();
                weights.add(w);
            }
        }

        //Find the corresponding MultiModelMethod value for the selected string
        int multimodelchoice = -1;
        for (int i = 0; i < MULTIMODELMETHOD_CHOICES.length; i++) {
            if (MULTIMODELMETHOD_CHOICES[i].equals(m_multiModelMethod.getStringValue())) {
                multimodelchoice = i;
                break;
            }
        }

        //trans = new PMMLMiningModelTranslator(documents, weights,
         //           MULTIMODELMETHOD_CHOICES_ENUM[multimodelchoice]);

        //outPMMLPort.addModelTranslater(trans);
        PMMLPortObject outPMMLPort = PMMLEnsembleUtilities.convertToPmmlEnsemble(documents,
            weights, MULTIMODELMETHOD_CHOICES_ENUM[multimodelchoice], exec);
        return new PortObject[]{outPMMLPort};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void reset() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected PortObjectSpec[] configure(final PortObjectSpec[] inSpecs)
            throws InvalidSettingsException {

        DataTableSpec inSpec = (DataTableSpec)inSpecs[0];
        if (!inSpec.containsName(m_pmmlColumn.getColumnName())) {
            throw new InvalidSettingsException("A PMML column with the given name does not exist");
        } else {
            DataColumnSpec pmmlColSpec = inSpec.getColumnSpec(inSpec.findColumnIndex(m_pmmlColumn.getColumnName()));
            if (!pmmlColSpec.getType().isCompatible(PMMLValue.class)) {
                throw new InvalidSettingsException("The column with the given name does not contain PMML values");
            }
        }

        if (m_weightAvailable.getBooleanValue()) {
            if (!inSpec.containsName(m_weightColumn.getColumnName())) {
                throw
                    new InvalidSettingsException("A double column for the weights with the given name does not exist");
            } else {
                DataColumnSpec weightColSpec = inSpec.getColumnSpec(
                        inSpec.findColumnIndex(m_weightColumn.getColumnName()));
                if (!weightColSpec.getType().isCompatible(DoubleValue.class)) {
                    throw new InvalidSettingsException("The column with the given name does not contain double values");
                }
            }
        }
        return new DataTableSpec[]{null};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        m_pmmlColumn.saveSettingsTo(settings);
        m_weightColumn.saveSettingsTo(settings);
        m_weightAvailable.saveSettingsTo(settings);
        m_multiModelMethod.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        m_pmmlColumn.loadSettingsFrom(settings);
        m_weightColumn.loadSettingsFrom(settings);
        m_weightAvailable.loadSettingsFrom(settings);
        m_multiModelMethod.loadSettingsFrom(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        m_pmmlColumn.validateSettings(settings);
        m_weightColumn.validateSettings(settings);
        m_weightAvailable.validateSettings(settings);
        m_multiModelMethod.validateSettings(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadInternals(final File internDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
        //Nothing to do
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveInternals(final File internDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
       //Nothing to do
    }

}

