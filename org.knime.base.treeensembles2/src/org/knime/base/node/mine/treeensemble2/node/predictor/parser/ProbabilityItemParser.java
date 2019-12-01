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
package org.knime.base.node.mine.treeensemble2.node.predictor.parser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.knime.base.node.mine.treeensemble2.node.predictor.ClassificationPrediction;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.util.UniqueNameGenerator;

/**
 * {@link PredictionItemParser} that parses ClassificationPredictions to create the probability columns in a
 * classification prediction.
 *
 * @author Adrian Nembach, KNIME GmbH, Konstanz, Germany
 */
public final class ProbabilityItemParser implements PredictionItemParser<ClassificationPrediction> {

    private final Map<String, DataCell> m_targetValueMap;

    private final String m_suffix;

    private final String m_prefix;

    private final String m_classColName;

    private final String[] m_classValues;

    /**
     * Constructor for parsers that append individual class probabilities.
     *
     * @param targetValueMap map of targetValues
     * @param prefix to prepend
     * @param suffix to append
     * @param classColName name of the class column
     * @param classValues possible class values
     */
    public ProbabilityItemParser(final Map<String, DataCell> targetValueMap, final String prefix, final String suffix,
        final String classColName, final String[] classValues) {
        m_targetValueMap = targetValueMap;
        m_prefix = prefix;
        m_suffix = suffix;
        m_classColName = classColName;
        m_classValues = classValues == null ? null : classValues.clone();
    }

    @Override
    public void appendSpecs(final UniqueNameGenerator nameGenerator, final List<DataColumnSpec> specs) {
        final String targetColName = m_classColName;
        for (String val : m_targetValueMap.keySet()) {
            String colName = m_prefix + targetColName + "=" + val + ")" + m_suffix;
            specs.add(nameGenerator.newColumn(colName, DoubleCell.TYPE));
        }

    }

    @Override
    public void appendCells(final List<DataCell> cells, final ClassificationPrediction prediction) {
        if (m_classValues == null) {
            throw new IllegalStateException("No class values available.");
        }
        int nrClasses = m_targetValueMap.size();
        // the map is necessary to ensure that the probabilities are correctly associated with the column header
        final Map<String, Double> classProbMap = new HashMap<>((int)(nrClasses * 1.5));
        for (int i = 0; i < nrClasses; i++) {
            classProbMap.put(m_classValues[i], prediction.getProbability(i));
        }
        for (final String className : m_targetValueMap.keySet()) {
            cells.add(new DoubleCell(classProbMap.get(className)));
        }
    }

}
