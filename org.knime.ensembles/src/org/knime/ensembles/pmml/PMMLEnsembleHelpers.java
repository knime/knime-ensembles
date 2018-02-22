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
 * ------------------------------------------------------------------------
 */
package org.knime.ensembles.pmml;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.xmlbeans.XmlException;
import org.dmg.pmml.DataFieldDocument.DataField;
import org.dmg.pmml.MiningFieldDocument.MiningField;
import org.dmg.pmml.MiningSchemaDocument.MiningSchema;
import org.dmg.pmml.PMMLDocument;
import org.dmg.pmml.PMMLDocument.PMML;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTable;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.def.BooleanCell;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.IntCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.data.util.LockedSupplier;
import org.knime.core.data.xml.PMMLValue;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.port.pmml.PMMLModelWrapper;
import org.w3c.dom.Document;

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
     * Returns all PMMLDocuments from a table.
     * @param inputTable the table with the documents
     * @param pmmlCol the column where the PMMLDocuments are stored
     * @param exec the execution context to check for cancellation
     * @return a list of all PMMLDocuments in the table
     * @throws CanceledExecutionException when the user cancels the execution
     * @throws XmlException when a document cannot be parsed
     */
    public static List<PMMLDocument> getPMMLDocumentsFromTable(final DataTable inputTable,
        final String pmmlCol, final ExecutionContext exec) throws CanceledExecutionException, XmlException {
        ArrayList<PMMLDocument> docs = new ArrayList<PMMLDocument>();
        DataTableSpec dtspec = inputTable.getDataTableSpec();
        int pmmlColIndex = dtspec.findColumnIndex(pmmlCol);
        for (DataRow r : inputTable) {
            if (exec != null) {
                exec.checkCanceled();
            }
            PMMLValue val = (PMMLValue) r.getCell(pmmlColIndex);

            try (LockedSupplier<Document> supplier = val.getDocumentSupplier()) {
                PMMLDocument pmmldoc = PMMLDocument.Factory.parse(supplier.get());
                docs.add(pmmldoc);
            }
        }
        return docs;
    }

    /**
     * Creates a list of model wrappers from a list of PMML documents.
     * @param docs List of pmml documents
     * @param exec the execution context to check for cancellation of the operation
     * @return A list of model wrappers
     * @throws XmlException if the XML document in a cell cannot be parsed as a pmml document
     * @throws CanceledExecutionException Thrown when execution is canceled by the user
     */
    public static List<PMMLModelWrapper> getModelListFromDocuments(
            final List<PMMLDocument> docs, final ExecutionContext exec)
                    throws XmlException, CanceledExecutionException {
        ArrayList<PMMLModelWrapper> wrappers = new ArrayList<PMMLModelWrapper>();
        double progressCount = 0;
        for (PMMLDocument pmmldoc : docs) {
            if (exec != null) {
                exec.checkCanceled();
                exec.setProgress(++progressCount / docs.size());
            }
            PMML sourcePMML = pmmldoc.getPMML();
            wrappers.addAll(PMMLModelWrapper.getModelListFromPMML(sourcePMML));
        }
        return wrappers;
    }

    /**
     * Creates a tables spec from a pmml data dictionary.
     * @param docs the list of PMMLDocuments
     * @param exec the execution context to check for cancellation
     * @return Specs created from the data dictionaries of the models in the input table
     * @throws XmlException if the XML document in a cell cannot be parsed as a pmml document
     * @throws CanceledExecutionException when the user cancels the operation
     */
    public static DataTableSpec createTableSpec(final List<PMMLDocument> docs, final ExecutionContext exec)
                throws XmlException, CanceledExecutionException {

        LinkedHashMap<String, DataType> cols = new LinkedHashMap<String, DataType>();
        double progressCount = 0;
        for (PMMLDocument pmmldoc : docs) {
            if (exec != null) {
                exec.checkCanceled();
                exec.setProgress(++progressCount / docs.size());
            }
            PMML sourcePMML = pmmldoc.getPMML();
            for (DataField field : sourcePMML.getDataDictionary().getDataFieldList()) {
                // If field has not already been added, we create a new column for it in the specs
                if (!cols.containsKey((field.getName()))) {
                    if (field.getDataType() == org.dmg.pmml.DATATYPE.BOOLEAN) {
                        cols.put(field.getName(), DataType.getType(BooleanCell.class));
                    } else if (field.getDataType() == org.dmg.pmml.DATATYPE.DOUBLE
                            || field.getDataType() == org.dmg.pmml.DATATYPE.FLOAT) {
                        cols.put(field.getName(), DataType.getType(DoubleCell.class));
                    } else if (field.getDataType() == org.dmg.pmml.DATATYPE.INTEGER) {
                        cols.put(field.getName(), DataType.getType(IntCell.class));
                    } else if (field.getDataType() == org.dmg.pmml.DATATYPE.STRING) {
                        cols.put(field.getName(), DataType.getType(StringCell.class));
                    }
                }
            }
        }
        String[] names = new String[cols.size()];
        DataType[] types = new DataType[cols.size()];
        int count = 0;
        for (String name : cols.keySet()) {
            names[count] = name;
            types[count] = cols.get(name);
            count++;
        }

        return new DataTableSpec(names, types);
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

        LinkedHashMap<String, MiningField> fields2 = new LinkedHashMap<String, MiningField>();

        for (MiningField mf : s2.getMiningFieldList()) {
            fields2.put(mf.getName(), mf);
        }

        for (MiningField mf1 : s1.getMiningFieldList()) {
            MiningField mf2 = fields2.get(mf1.getName());
            if (mf2 == null) {
                continue;
            }
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
            boolean mf1HasMissValRepl = mf1.getMissingValueReplacement() != null;
            boolean mf2HasMissValRepl = mf2.getMissingValueReplacement() != null;

            if ((mf1HasMissValRepl || mf2HasMissValRepl)
                    && (!mf1HasMissValRepl && mf2HasMissValRepl
                            || mf1HasMissValRepl && !mf2HasMissValRepl
                            || (!mf1.getMissingValueReplacement().equals(
                                    mf2.getMissingValueReplacement())
                               )
                       )
                ) {
                throwModelMismatchException(mf1.getName(),
                        "missing value replacement", mf1.getMissingValueReplacement(),
                        mf2.getMissingValueReplacement());
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
