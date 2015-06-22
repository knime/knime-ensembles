/*
 * ------------------------------------------------------------------------
 *
 *  Copyright by KNIME GmbH, Konstanz, Germany
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
 *   22.06.2015 (Alexander): created
 */
package org.knime.ensembles.pmml.combine;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.xmlbeans.XmlException;
import org.dmg.pmml.MiningFieldDocument.MiningField;
import org.dmg.pmml.PMMLDocument;
import org.knime.core.data.DataTableSpec;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.port.pmml.PMMLModelWrapper;
import org.knime.core.node.port.pmml.PMMLPortObject;
import org.knime.core.node.port.pmml.PMMLPortObjectSpecCreator;
import org.knime.ensembles.pmml.ModelMismatchException;
import org.knime.ensembles.pmml.PMMLEnsembleHelpers;
import org.knime.ensembles.pmml.PMMLMiningModelTranslator;

/**
 *
 * @author Alexander Fillbrunn
 */
public final class PMMLEnsembleUtilities {

    private PMMLEnsembleUtilities() {
    }

    /**
     * Converts a list of documents into a PMML ensemble Port Object.
     * @param documents the documents
     * @param weights a list of weights or null
     * @param mm the multiple model method
     * @param exec execution context to check for cancellation
     * @return PMMLPortObject with the ensemble
     * @throws XmlException when the input is not well formatted
     * @throws CanceledExecutionException when the user cancelled the execution
     * @throws ModelMismatchException when the models in the list cannot be merged into an ensemble
     */
    public static PMMLPortObject convertToPmmlEnsemble(final List<PMMLDocument> documents,
        final List<Double> weights, final org.dmg.pmml.MULTIPLEMODELMETHOD.Enum mm,
        final ExecutionContext exec) throws XmlException, CanceledExecutionException, ModelMismatchException {

        ExecutionContext getModelsCtx = exec.createSubExecutionContext(0.4);
        List<PMMLModelWrapper> wrappers = PMMLEnsembleHelpers.getModelListFromDocuments(documents, getModelsCtx);
        if (weights != null && weights.size() != documents.size()) {
            throw new IllegalArgumentException("Weights must either be null or have the same size as documents");
        }

        PMMLEnsembleHelpers.checkInputTablePMML(wrappers);
        Set<String> targetCols = new LinkedHashSet<String>();
        Set<String> learningCols = new LinkedHashSet<String>();
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
        ExecutionContext createTableSpecCtx = exec.createSubExecutionContext(0.8);
        DataTableSpec fakeSpec = PMMLEnsembleHelpers.createTableSpec(documents, createTableSpecCtx);
        PMMLPortObjectSpecCreator creator = new PMMLPortObjectSpecCreator(fakeSpec);
        creator.setTargetColsNames(new ArrayList<String>(targetCols));
        creator.setLearningColsNames(new ArrayList<String>(learningCols));
        PMMLPortObject outPMMLPort = new PMMLPortObject(creator.createSpec());
        PMMLMiningModelTranslator trans;
        trans = new PMMLMiningModelTranslator(documents, weights, mm);
        outPMMLPort.addModelTranslater(trans);
        return outPMMLPort;
    }

}
