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
 *   05.04.2016 (adrian): created
 */
package org.knime.base.node.mine.treeensemble2.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.knime.base.node.mine.decisiontree2.PMMLBooleanOperator;
import org.knime.base.node.mine.decisiontree2.PMMLCompoundPredicate;
import org.knime.base.node.mine.decisiontree2.PMMLOperator;
import org.knime.base.node.mine.decisiontree2.PMMLPredicate;
import org.knime.base.node.mine.decisiontree2.PMMLSimplePredicate;
import org.knime.base.node.mine.treeensemble2.data.PredictorRecord;
import org.knime.base.node.mine.treeensemble2.data.TestDataGenerator;
import org.knime.base.node.mine.treeensemble2.data.TreeNumericColumnData;
import org.knime.base.node.mine.treeensemble2.model.TreeNodeNumericCondition.NumericOperator;
import org.knime.base.node.mine.treeensemble2.node.learner.TreeEnsembleLearnerConfiguration;

import com.google.common.collect.Maps;

/**
 * This class contains unit tests for the class {@link TreeNodeNumericCondition}.
 *
 * @author Adrian Nembach, KNIME.com
 */
public class TreeNodeNumericConditionTest {

    /**
     * This method tests the
     * {@link TreeNodeNominalCondition#testCondition(org.knime.base.node.mine.treeensemble2.data.PredictorRecord)}
     * method.
     *
     * @throws Exception
     */
    @Test
    public void testTestCondition() throws Exception {
        final TreeEnsembleLearnerConfiguration config = new TreeEnsembleLearnerConfiguration(false);
        final TestDataGenerator dataGen = new TestDataGenerator(config);
        final TreeNumericColumnData col = dataGen.createNumericAttributeColumn("1,2,3,4,4,5,6,7", "testCol", 0);
        TreeNodeNumericCondition cond = new TreeNodeNumericCondition(col.getMetaData(), 3, NumericOperator.LessThanOrEqual, false);
        final Map<String, Object> map = Maps.newHashMap();
        final String colName = col.getMetaData().getAttributeName();
        map.put(colName, 2.5);
        final PredictorRecord record = new PredictorRecord(map);
        assertTrue(cond.testCondition(record), "2.5 was falsely rejected.");
        map.clear();
        map.put(colName, 3);
        assertTrue(cond.testCondition(record), "3 was falsely rejected.");
        map.clear();
        map.put(colName, 4);
        assertFalse(cond.testCondition(record), "4 was falsely accepted.");
        map.clear();
        map.put(colName, PredictorRecord.NULL);
        assertFalse(cond.testCondition(record), "Missing values were falsely accepted.");

        cond = new TreeNodeNumericCondition(col.getMetaData(), 3, NumericOperator.LessThanOrEqual, true);
        map.clear();
        map.put(colName, 2.5);
        assertTrue(cond.testCondition(record), "2.5 was falsely rejected.");
        map.clear();
        map.put(colName, 3);
        assertTrue(cond.testCondition(record), "3 was falsely rejected.");
        map.clear();
        map.put(colName, 4);
        assertFalse(cond.testCondition(record), "4 was falsely accepted.");
        map.clear();
        map.put(colName, PredictorRecord.NULL);
        assertTrue(cond.testCondition(record), "Missing values were falsely rejected.");

        cond = new TreeNodeNumericCondition(col.getMetaData(), 4, NumericOperator.LargerThan, false);
        map.clear();
        map.put(colName, 2.5);
        assertFalse(cond.testCondition(record), "2.5 was falsely accepted.");
        map.clear();
        map.put(colName, 3);
        assertFalse(cond.testCondition(record), "3 was falsely accepted.");
        map.clear();
        map.put(colName, 4);
        assertFalse(cond.testCondition(record), "4 was falsely accepted.");
        map.clear();
        map.put(colName, 4.01);
        assertTrue(cond.testCondition(record), "4.01 was falsely rejected.");
        map.clear();
        map.put(colName, PredictorRecord.NULL);
        assertFalse(cond.testCondition(record), "Missing values were falsely accepted.");

        cond = new TreeNodeNumericCondition(col.getMetaData(), 4, NumericOperator.LargerThan, true);
        map.clear();
        map.put(colName, 2.5);
        assertFalse(cond.testCondition(record), "2.5 was falsely accepted.");
        map.clear();
        map.put(colName, 3);
        assertFalse(cond.testCondition(record), "3 was falsely accepted.");
        map.clear();
        map.put(colName, 4.01);
        assertTrue(cond.testCondition(record), "4 was falsely rejected.");
        map.clear();
        map.put(colName, PredictorRecord.NULL);
        assertTrue(cond.testCondition(record), "Missing values were falsely rejected.");
    }

    /**
     * This method tests the {@link TreeNodeNumericCondition#toPMMLPredicate()} method.
     *
     * @throws Exception
     */
    @Test
    public void testToPMML() throws Exception {
        final TreeEnsembleLearnerConfiguration config = new TreeEnsembleLearnerConfiguration(false);
        final TestDataGenerator dataGen = new TestDataGenerator(config);
        final TreeNumericColumnData col = dataGen.createNumericAttributeColumn("1,2,3,4,4,5,6,7", "testCol", 0);
        TreeNodeNumericCondition cond = new TreeNodeNumericCondition(col.getMetaData(), 3, NumericOperator.LessThanOrEqual, false);
        PMMLPredicate predicate = cond.toPMMLPredicate();
        assertTrue(predicate instanceof PMMLSimplePredicate);
        PMMLSimplePredicate simplePredicate = (PMMLSimplePredicate)predicate;
        assertEquals(col.getMetaData().getAttributeName(), simplePredicate.getSplitAttribute(), "Wrong attribute");
        assertEquals(PMMLOperator.LESS_OR_EQUAL, simplePredicate.getOperator(), "Wrong operator");
        assertEquals(Double.toString(3), simplePredicate.getThreshold(), "Wrong threshold");

        cond = new TreeNodeNumericCondition(col.getMetaData(), 4.5, NumericOperator.LargerThan, true);
        predicate = cond.toPMMLPredicate();
        assertTrue(predicate instanceof PMMLCompoundPredicate);
        PMMLCompoundPredicate compound = (PMMLCompoundPredicate)predicate;
        assertEquals(PMMLBooleanOperator.OR, compound.getBooleanOperator(), "Wrong boolean operator in compound.");
        List<PMMLPredicate> preds = compound.getPredicates();
        assertEquals(2, preds.size(), "Wrong number of predicates in compound.");
        assertTrue(preds.get(0) instanceof PMMLSimplePredicate);
        simplePredicate = (PMMLSimplePredicate)preds.get(0);
        assertEquals(col.getMetaData().getAttributeName(), simplePredicate.getSplitAttribute(), "Wrong attribute");
        assertEquals(PMMLOperator.GREATER_THAN, simplePredicate.getOperator(), "Wrong operator");
        assertEquals(Double.toString(4.5), simplePredicate.getThreshold(), "Wrong threshold");

        assertTrue(preds.get(1) instanceof PMMLSimplePredicate);
        simplePredicate = (PMMLSimplePredicate)preds.get(1);
        assertEquals(PMMLOperator.IS_MISSING, simplePredicate.getOperator(), "Should be isMissing");

    }
}
