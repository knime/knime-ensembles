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

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;

import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.XmlException;
import org.dmg.pmml.DataDictionaryDocument.DataDictionary;
import org.dmg.pmml.DataFieldDocument.DataField;
import org.dmg.pmml.MiningFieldDocument.MiningField;
import org.dmg.pmml.MiningModelDocument.MiningModel;
import org.dmg.pmml.MiningSchemaDocument.MiningSchema;
import org.dmg.pmml.PMMLDocument;
import org.dmg.pmml.PMMLDocument.PMML;
import org.dmg.pmml.SegmentDocument.Segment;
import org.dmg.pmml.SegmentationDocument.Segmentation;
import org.dmg.pmml.TaxonomyDocument.Taxonomy;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTable;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.xml.PMMLCell;
import org.knime.core.data.xml.PMMLCellFactory;
import org.knime.core.data.xml.PMMLValue;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.port.pmml.PMMLMiningSchemaTranslator;
import org.knime.core.node.port.pmml.PMMLModelWrapper;
import org.knime.core.node.port.pmml.PMMLPortObjectSpec;
import org.knime.core.node.port.pmml.PMMLTranslator;
import org.xml.sax.SAXException;


/**
 * Creates a mining model that contains multiple other models.
 *
 * @author Alexander Fillbrunn, Universitaet Konstanz
 * @since 2.8
 *
 */
public class PMMLMiningModelTranslator implements PMMLTranslator {

    private PMMLCell[] m_pmmlCells;
    private DoubleCell[] m_weightCells;
    private boolean m_weightAvailable = false;
    private org.dmg.pmml.MULTIPLEMODELMETHOD.Enum m_multModelMethod;

    private static final NodeLogger LOGGER = NodeLogger.getLogger(
            PMMLMiningSchemaTranslator.class);

    /**
     * Constructor for a PMMLSegmentedMiningModelTranslator that does not use a weight column.
     * @param treeModelsTable The table with the models to be inserted into the final aggregated model
     * @param pmmlColumnName The name of the column that contains the pmml data
     * @param multimodelmethod The method used for combining the multiple models
     */
    public PMMLMiningModelTranslator(final DataTable treeModelsTable, final String pmmlColumnName,
            final org.dmg.pmml.MULTIPLEMODELMETHOD.Enum multimodelmethod) {
        this(treeModelsTable, pmmlColumnName, null, false, multimodelmethod);
    }

    /**
     * Constructor for a PMMLSegmentedMiningModelTranslator.
     */
    public PMMLMiningModelTranslator() { }

    private PMMLMiningModelTranslator(
            final DataTable treeModelsTable,
            final String pmmlColumnName,
            final String weightColumnName,
            final boolean weightAvailable,
            final org.dmg.pmml.MULTIPLEMODELMETHOD.Enum multimodelmethod) {
        m_weightAvailable = weightAvailable;
        m_multModelMethod = multimodelmethod;

        DataTableSpec dtspec = treeModelsTable.getDataTableSpec();
        int weightColIndex = weightAvailable ? dtspec.findColumnIndex(weightColumnName) : -1;
        int pmmlColIndex = dtspec.findColumnIndex(pmmlColumnName);

        ArrayList<PMMLCell> pmmls = new ArrayList<PMMLCell>();
        ArrayList<DoubleCell> weights = new ArrayList<DoubleCell>();

        for (DataRow row : treeModelsTable) {
             PMMLCell pmmlCell = (PMMLCell)row.getCell(pmmlColIndex);
             pmmls.add(pmmlCell);
             if (weightAvailable) {
                 DoubleCell weightCell = (DoubleCell)row.getCell(weightColIndex);
                 weights.add(weightCell);
             }
        }

        m_pmmlCells = pmmls.toArray(new PMMLCell[0]);
        if (weightAvailable) {
            m_weightCells = weights.toArray(new DoubleCell[0]);
        }
    }

    /**
     * Constructor for a PMMLSegmentedMiningModelTranslator that uses a weight column.
     * @param treeModelsTable The table with the models to be inserted into the final aggregated model
     * @param pmmlColumnName The name of the column that contains the pmml data
     * @param weightColumnName The name of the column that contains the weights for the models
     * @param multimodelmethod The method used for combining the multiple models
     */
    public PMMLMiningModelTranslator(final DataTable treeModelsTable, final String pmmlColumnName,
            final String weightColumnName, final org.dmg.pmml.MULTIPLEMODELMETHOD.Enum multimodelmethod) {
        this(treeModelsTable, pmmlColumnName, weightColumnName, weightColumnName != null, multimodelmethod);
    }

    @Override
    public void initializeFrom(final PMMLDocument pmmldoc) {
        DataDictionary dataDict = pmmldoc.getPMML().getDataDictionary();
        List<MiningModel> miningModels = pmmldoc.getPMML().getMiningModelList();
        ArrayList<PMMLCell> pmmlCells = new ArrayList<PMMLCell>();
        ArrayList<DoubleCell> weightCells = new ArrayList<DoubleCell>();

        if (miningModels.size() > 0) {
            for (MiningModel mmodel : miningModels) {
                for (Segment s : mmodel.getSegmentation().getSegmentList()) {
                    PMMLModelWrapper model = PMMLModelWrapper.getSegmentContent(s);
                    DataCell pmmlCell = null;
                    try {
                        pmmlCell = PMMLCellFactory.create(model.createPMMLDocument(dataDict).toString());
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (ParserConfigurationException e) {
                        e.printStackTrace();
                    } catch (SAXException e) {
                        e.printStackTrace();
                    } catch (XMLStreamException e) {
                        e.printStackTrace();
                    }
                    double w = s.getWeight();
                    DataCell weightCell = new DoubleCell(w);
                    pmmlCells.add((PMMLCell)pmmlCell);
                    weightCells.add((DoubleCell)weightCell);
                }
            }
        }
        m_pmmlCells = pmmlCells.toArray(new PMMLCell[0]);
        m_weightCells = weightCells.toArray(new DoubleCell[0]);
    }

    /**
     * Returns an array of pmml cells that are used for building the pmml document.
     * @return an array of pmml cells
     */
    public PMMLCell[] getPmmlCells() {
        return m_pmmlCells;
    }

    /**
     * Returns an array of double cells that are used for weighting the pmml mining models.
     * @return an array of weights
     */
    public DoubleCell[] getWeightCells() {
        return m_weightCells;
    }

    @Override
    public SchemaType exportTo(final PMMLDocument pmmlDoc, final PMMLPortObjectSpec spec) {
        /*
         * Structure of the Mining Model:
         * <MiningModel>
         *     <Segmentation>
         *         <Segment>
         *             <TreeModel>
         *                 ...
         *             </TreeModel>
         *             ...
         *         </Segment>
         *     </Segmentation>
         * </MiningModel>
         */

        PMML pmml = pmmlDoc.getPMML();
        MiningModel miningModel = pmml.addNewMiningModel();
        Segmentation segm = miningModel.addNewSegmentation();
        segm.setMultipleModelMethod(m_multModelMethod);
        pmml.setDataDictionary(null);
        //Each row contains one mining model
        for (int i = 0; i < m_pmmlCells.length; i++) {
            PMMLValue val = m_pmmlCells[i];
            PMMLDocument pmmldoc = null;
            PMML sourcePMML = null;

            try {
                pmmldoc = PMMLDocument.Factory.parse(val.getDocument());
            } catch (XmlException e) {
                LOGGER.error("Error parsing the PMML document");
                return null;
            }

            sourcePMML = pmmldoc.getPMML();
            MiningSchema schema = mergeSchemes(sourcePMML);
            pmml.setDataDictionary(mergeDictionaries(pmml.getDataDictionary(), sourcePMML.getDataDictionary()));

            if (miningModel.getMiningSchema() == null) {
                miningModel.setMiningSchema(schema);
            } else {
                miningModel.setMiningSchema(mergeSchemes(miningModel.getMiningSchema(), schema));
            }

            for (PMMLModelWrapper model : PMMLModelWrapper.getModelListFromPMMLDocument(pmmldoc)) {
                if (miningModel.getFunctionName() == null) {
                    miningModel.setFunctionName(model.getFunctionName());
                }
                model.addToSegment(createSegment(segm, m_weightAvailable ? m_weightCells[i].getDoubleValue() : 0));
            }
        }
        return MiningModel.type;
    }

    // This method is used to merge two data dictionaries
    private DataDictionary mergeDictionaries(final DataDictionary dict1, final DataDictionary dict2) {
        if (dict1 == null && dict2 == null) {
            return DataDictionary.Factory.newInstance();
        } else if (dict1 == null) {
            return dict2;
        } else if (dict2 == null) {
            return dict1;
        }

        DataDictionary output = DataDictionary.Factory.newInstance();
        List<DataField> fields = dict1.getDataFieldList();
        List<Taxonomy> tax = dict1.getTaxonomyList();

        for (DataField df1 : dict2.getDataFieldList()) {
            boolean exists = false;
            for (DataField df2 : fields) {
                if (df2.getName().equals(df1.getName())) {
                    exists = true;
                    break;
                }
            }
            if (!exists) {
                fields.add(df1);
            }
        }
        for (Taxonomy tax1 : dict2.getTaxonomyList()) {
            boolean exists = false;
            for (Taxonomy tax2 : tax) {
                if (tax2.getName().equals(tax1.getName())) {
                    exists = true;
                    break;
                }
            }
            if (!exists) {
                tax.add(tax1);
            }
        }

        output.setNumberOfFields(BigInteger.valueOf(fields.size()));
        output.setDataFieldArray(fields.toArray(new DataField[0]));
        output.setTaxonomyArray(tax.toArray(new Taxonomy[0]));
        return output;
    }

    // This method is used to merge mining schemes from a set of models in a mining model
    private MiningSchema mergeSchemes(final PMML pmml) {
        MiningSchema schema = MiningSchema.Factory.newInstance();

        for (PMMLModelWrapper model : PMMLModelWrapper.getModelListFromPMML(pmml)) {
            mergeSchemes(schema, model.getMiningSchema());
        }
        return schema;
    }

    // This method is used to merge two mining schemes from two different models
    private MiningSchema mergeSchemes(final MiningSchema original, final MiningSchema s) {
        ArrayList<MiningField> fields = new ArrayList<MiningField>();
        for (MiningField mf1 : s.getMiningFieldList()) {
            boolean match = false;
            for (MiningField mf2 : original.getMiningFieldList()) {
                if (mf1.getName() == mf2.getName()) {
                    match = true;
                    if (miningFieldsAreEqual(mf1, mf2)) {
                        fields.add(mf1);
                    } else {
                        MiningField merged = (MiningField) mf1.copy();
                        merged.setHighValue(Math.max(mf1.getHighValue(), mf2.getHighValue()));
                        merged.setLowValue(Math.min(mf1.getLowValue(), mf2.getLowValue()));
                        merged.setImportance((mf1.getImportance().compareTo(mf2.getImportance()) == -1)
                                                ? mf1.getImportance() : mf2.getImportance());
                        fields.add(merged);
                    }
                }
            }
            if (!match) {
                fields.add(mf1);
            }
        }
        original.setMiningFieldArray(fields.toArray(new MiningField[0]));
        return original;
    }

    // Since MiningField does not provide a suitable equals method, this method is used to compare two mining fields
    private boolean miningFieldsAreEqual(final MiningField f1, final MiningField f2) {
        return     f1.getHighValue() == f2.getHighValue()
                && f1.getLowValue() == f2.getLowValue()
                && f1.getImportance() == f2.getImportance()
                && f1.getInvalidValueTreatment() == f2.getInvalidValueTreatment()
                && f1.getMissingValueReplacement() == f2.getMissingValueReplacement()
                && f1.getMissingValueTreatment() == f2.getMissingValueTreatment()
                && f1.getOptype() == f2.getOptype()
                && f1.getOutliers() == f2.getOutliers()
                && f1.getUsageType() == f2.getUsageType();
    }

    private Segment createSegment(final Segmentation segm, final double weight) {
        Segment s = segm.addNewSegment();
        if (m_weightAvailable) {
            s.setWeight(weight);
        }
        //Add predicate that tells when the segment should be used. Currently always true
        s.addNewTrue();
        return s;
    }
}
