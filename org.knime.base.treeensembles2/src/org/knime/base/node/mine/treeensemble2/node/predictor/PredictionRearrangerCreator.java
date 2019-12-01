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
 *   Aug 17, 2016 (adrian): created
 */
package org.knime.base.node.mine.treeensemble2.node.predictor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.knime.base.node.mine.treeensemble2.node.predictor.parser.DefaultPredictionParser;
import org.knime.base.node.mine.treeensemble2.node.predictor.parser.PredictionItemParser;
import org.knime.base.node.mine.treeensemble2.node.predictor.parser.PredictionParser;
import org.knime.base.node.mine.treeensemble2.node.predictor.parser.ProbabilityItemParser;
import org.knime.base.node.mine.treeensemble2.node.predictor.parser.SingleItemParsers;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataTableSpecCreator;
import org.knime.core.data.container.ColumnRearranger;

/**
 * Creates the {@link ColumnRearranger} that creates the output table in a predictor node.
 *
 * @author Adrian Nembach, KNIME GmbH, Konstanz, Germany
 */
public final class PredictionRearrangerCreator {

    private List<PredictionItemParser<? extends Prediction>> m_itemParsers = new ArrayList<>();

    private DataTableSpec m_testSpec;

    private String m_errorMsg;

    private Predictor<? extends Prediction> m_predictor;

    /**
     * Abstract constructor.
     *
     * @param predictSpec {@link DataTableSpec} of the table to predict
     * @param predictor performs the actual prediction (may be null during configure)
     */
    public PredictionRearrangerCreator(final DataTableSpec predictSpec,
        final Predictor<? extends Prediction> predictor) {
        m_testSpec = predictSpec;
        m_predictor = predictor;
    }


    /**
     * Creates the {@link DataTableSpec} that a {@link ColumnRearranger} with the current
     * configuration would create.
     *
     * @return the table spec
     */
    public Optional<DataTableSpec> createSpec() {
        if (hasErrors()) {
            return Optional.empty();
        }
        @SuppressWarnings({"rawtypes", "unchecked"})
        PredictionParser parser = new DefaultPredictionParser(m_testSpec, m_itemParsers);
        DataTableSpecCreator specCreator = new DataTableSpecCreator(m_testSpec);
        specCreator.addColumns(parser.getAppendSpecs());
        return Optional.of(specCreator.createSpec());
    }

    /**
     * Creates the prediction rearranger. Use this method for execution.
     *
     * @return the prediction rearranger
     * @throws IllegalStateException if the rearranger can't be created
     */
    public ColumnRearranger createExecutionRearranger() {
        if (hasErrors()) {
            throw new IllegalStateException("Can't create prediction rearranger: " + m_errorMsg);
        }
        return createRearranger();
    }

    private boolean hasErrors() {
        return m_errorMsg != null;
    }

    private ColumnRearranger createRearranger() {
        ColumnRearranger cr = new ColumnRearranger(m_testSpec);
        @SuppressWarnings({"rawtypes", "unchecked"})
        PredictionParser parser = new DefaultPredictionParser(m_testSpec, m_itemParsers);
        @SuppressWarnings({"rawtypes", "unchecked"})
        PredictionCellFactory pcf = new PredictionCellFactory<>(m_predictor, parser);
        cr.append(pcf);
        return cr;
    }

    /**
     * Call this method if a prediction parser can't be added because information is missing.
     *
     * @param errorMsg the error message to set
     */
    private void setErrorMsg(final String errorMsg) {
        m_errorMsg = errorMsg;
    }

    /**
     * Adds <b>itemParser</b> to the list of item parsers.
     *
     * @param itemParser
     */
    public void addPredictionItemParser(final PredictionItemParser<? extends Prediction> itemParser) {
        m_itemParsers.add(itemParser);
    }

    /**
     * Adds a column containing the class prediction.
     *
     * @param predictionColumnName the name of the appended prediction column
     */
    public void addClassPrediction(final String predictionColumnName) {
        addPredictionItemParser(SingleItemParsers.createClassPredictionItemParser(predictionColumnName));
    }

    /**
     * Adds a column with the prediction confidence i.e. the probability of the most likely class.
     *
     * @param confidenceColName the name of the class column
     */
    public void addPredictionConfidence(final String confidenceColName) {
        addPredictionItemParser(SingleItemParsers.createConfidenceItemParser(confidenceColName));
    }

    /**
     * Adds a column for each of the possible classes containing the probability that a row belongs to this class.
     *
     * @param targetValueMap the targetValueMap
     * @param classLabels the class labels in the order the model uses internally
     * @param prefix e.g. "P ("
     * @param suffix an optional suffix that is appended to the new columns' names
     * @param classColumnName the name of the class column
     */
    public void addClassProbabilities(final Map<String, DataCell> targetValueMap, final String[] classLabels,
        final String prefix, final String suffix, final String classColumnName) {
        if (targetValueMap == null) {
            setErrorMsg("No target values available.");
            return;
        }
        addPredictionItemParser(
            new ProbabilityItemParser(targetValueMap, prefix, suffix, classColumnName, classLabels));
    }

    /**
     * Adds a column containing the number of models used for the prediction. For use with random forest models.
     */
    public void addModelCount() {
        addPredictionItemParser(SingleItemParsers.createModelCountItemParser());
    }

    /**
     * Adds a column containing the regression prediction.
     *
     * @param predictionColumnName the name of the prediction column
     */
    public void addRegressionPrediction(final String predictionColumnName) {
        addPredictionItemParser(SingleItemParsers.createRegressionPredictionItemParser(predictionColumnName));
    }

    /**
     * Adds a column with the variance of the predictions of the individual models.
     *
     * @param predictionColumnName the name of the prediction column
     */
    public void addPredictionVariance(final String predictionColumnName) {
        addPredictionItemParser(SingleItemParsers.createVarianceItemParser(predictionColumnName));
    }

}
