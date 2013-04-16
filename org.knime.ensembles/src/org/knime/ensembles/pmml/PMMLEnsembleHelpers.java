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
package org.knime.ensembles.pmml;

import java.util.ArrayList;
import java.util.List;

import org.apache.xmlbeans.XmlException;
import org.dmg.pmml.PMMLDocument;
import org.dmg.pmml.DataFieldDocument.DataField;
import org.dmg.pmml.MiningFieldDocument.MiningField;
import org.dmg.pmml.MiningSchemaDocument.MiningSchema;
import org.dmg.pmml.PMMLDocument.PMML;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTable;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.def.BooleanCell;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.IntCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.data.xml.PMMLValue;
import org.knime.core.node.port.pmml.PMMLModelWrapper;

/**
 * Contains helper functions for PMML ensemble processing.
 *
 * @author Alexander Fillbrunn, Universitaet Konstanz
 * @since 2.8
 * 
 */
public final class PMMLEnsembleHelpers {

    private PMMLEnsembleHelpers() {
        // don't initialize.
    }

    /**
     * Creates a list of model wrappers from a table with pmml content.
     * @param inputTable The table containing a column with pmml documents
     * @param pmmlCol The index of the column that contains pmml documents
     * @return A list of model wrappers
     * @throws XmlException if the XML document in a cell cannot be parsed as a pmml document
     */
    public static List<PMMLModelWrapper> getModelListFromInput(
            final DataTable inputTable, final String pmmlCol) throws XmlException {
        DataTableSpec dtspec = inputTable.getDataTableSpec();
        int pmmlColIndex = dtspec.findColumnIndex(pmmlCol);
        ArrayList<PMMLModelWrapper> wrappers = new ArrayList<PMMLModelWrapper>();
        // Go through table and add all models we can find
        for (DataRow r : inputTable) {
            PMMLValue val = (PMMLValue) r.getCell(pmmlColIndex);
            PMMLDocument pmmldoc = PMMLDocument.Factory
                    .parse(val.getDocument());
            PMML sourcePMML = pmmldoc.getPMML();
            wrappers.addAll(PMMLModelWrapper.getModelListFromPMML(sourcePMML));
        }
        return wrappers;
    }

    /**
     * Creates a tables spec from a pmml data dictionary.
     * @param inputTable The input table containing a column with pmml documents
     * @param pmmlCol The name of the pmml column
     * @return Specs created from the data dictionaries of the models in the input table
     * @throws XmlException if the XML document in a cell cannot be parsed as a pmml document
     */
    public static DataTableSpec createTableSpec(final DataTable inputTable, final String pmmlCol) throws XmlException {
        DataTableSpec dtspec = inputTable.getDataTableSpec();
        int pmmlColIndex = dtspec.findColumnIndex(pmmlCol);

        ArrayList<String> names = new ArrayList<String>();
        ArrayList<DataType> dataTypes = new ArrayList<DataType>();

        for (DataRow r : inputTable) {
            PMMLValue val = (PMMLValue) r.getCell(pmmlColIndex);
            PMMLDocument pmmldoc = PMMLDocument.Factory
                    .parse(val.getDocument());
            PMML sourcePMML = pmmldoc.getPMML();

            for (DataField field : sourcePMML.getDataDictionary().getDataFieldList()) {
                // If field has not already been added, we create a new column for it in the specs
                if (!names.contains(field.getName())) {
                    names.add(field.getName());
                    if (field.getDataType() == org.dmg.pmml.DATATYPE.BOOLEAN) {
                        dataTypes.add(DataType.getType(BooleanCell.class));
                    } else if (field.getDataType() == org.dmg.pmml.DATATYPE.DOUBLE
                            || field.getDataType() == org.dmg.pmml.DATATYPE.FLOAT) {
                        dataTypes.add(DataType.getType(DoubleCell.class));
                    } else if (field.getDataType() == org.dmg.pmml.DATATYPE.INTEGER) {
                        dataTypes.add(DataType.getType(IntCell.class));
                    } else if (field.getDataType() == org.dmg.pmml.DATATYPE.STRING) {
                        dataTypes.add(DataType.getType(StringCell.class));
                    }
                }
            }
        }
        return new DataTableSpec(names.toArray(new String[0]),
                dataTypes.toArray(new DataType[0]));
    }

    /**
     * Checks if the mining schemes of a list of models is compatible to be merged.
     * @param wrappers The list of model wrappers containing the models
     * @throws XmlException if the XML document in a cell cannot be parsed as a pmml document
     * @throws ModelMismatchException if the models in the table are not compatible with one another
     */
    public static void checkInputTablePMML(final List<PMMLModelWrapper> wrappers)
            throws XmlException, ModelMismatchException {
        MiningSchema schema = null;
        org.dmg.pmml.MININGFUNCTION.Enum miningFunction = null;

        for (PMMLModelWrapper model : wrappers) {
            if (schema == null) {
                schema = model.getMiningSchema();
            } else {
                miningSchemesAreCompatible(schema, model.getMiningSchema());
            }
            if (miningFunction == null) {
                miningFunction = model.getFunctionName();
            } else if (miningFunction != model.getFunctionName()) {
                throw new ModelMismatchException(
                        "There are models with different mining functions in the ensemble");
            }
        }
    }

    /**
     * Checks if two mining schemas can be merged into one.
     * @param s1 Schema 1
     * @param s2 Schema 2
     * @throws ModelMismatchException if the models in the table are not compatible with one another
     */
    public static void miningSchemesAreCompatible(final MiningSchema s1,
            final MiningSchema s2) throws ModelMismatchException {
        for (MiningField mf1 : s1.getMiningFieldList()) {
            for (MiningField mf2 : s2.getMiningFieldList()) {
                if (mf1.getName().equals(mf2.getName())) {
                    
                    if (mf1.getOptype() != mf2.getOptype()) {
                        throwModelMismatchException(mf1.getName(), "optypes", mf1.getOptype().toString(),
                                mf2.getOptype().toString());
                    }
                    if (mf1.getOutliers() != mf2.getOutliers()) {
                        throwModelMismatchException(mf1.getName(),
                                "outlier treatment", mf1.getOutliers().toString(), mf2.getOutliers().toString());
                    }
                    if (mf1.getUsageType() != mf2.getUsageType()) {
                        throwModelMismatchException(mf1.getName(), "usage type", mf1.getUsageType().toString(),
                                mf2.getUsageType().toString());
                    }
                    if (mf1.getInvalidValueTreatment() != mf2
                            .getInvalidValueTreatment()) {
                        throwModelMismatchException(mf1.getName(),
                                "invalid value treatment", mf1.getInvalidValueTreatment().toString(),
                                mf2.getInvalidValueTreatment().toString());
                    }
                    if (mf1.getMissingValueTreatment() != mf2
                            .getMissingValueTreatment()) {
                        throwModelMismatchException(mf1.getName(),
                                "missing value treatment", mf1.getMissingValueTreatment().toString(),
                                mf2.getMissingValueTreatment().toString());
                    }
                    boolean mf1HasMissValRepl = mf1
                            .getMissingValueReplacement() != null;
                    boolean mf2HasMissValRepl = mf2
                            .getMissingValueReplacement() != null;

                    if ((mf1HasMissValRepl || mf2HasMissValRepl)
                            && (!mf1HasMissValRepl && mf2HasMissValRepl
                                    || mf1HasMissValRepl && !mf2HasMissValRepl || (!mf1
                                    .getMissingValueReplacement().equals(
                                            mf2.getMissingValueReplacement())))) {
                        throwModelMismatchException(mf1.getName(),
                                "missing value replacement", mf1.getMissingValueReplacement(),
                                mf2.getMissingValueReplacement());
                    }
                }
            }
        }
    }

    private static void throwModelMismatchException(
            final String miningFieldName, final String attributeName, final String value1, final String value2)
            throws ModelMismatchException {
        throw new ModelMismatchException("Mining field " + miningFieldName
                + " has differing " + attributeName + " for 2 models: " + value1 + " and " + value2);
    }
}
