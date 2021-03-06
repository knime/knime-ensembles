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
 *   14.07.2016 (Adrian Nembach): created
 */
package org.knime.base.node.mine.treeensemble2.node.predictor.classification;

import org.knime.base.node.mine.treeensemble2.model.TreeNodeClassification;

/**
 * Used to aggregate the votes of trees in classification random forests.
 *
 * @author Adrian Nembach, KNIME GmbH, Konstanz, Germany
 */
public interface Voting {

    /**
     * @param leaf the {@link TreeNodeClassification leaf} the current record ended up in in the current tree
     */
    public void addVote(final TreeNodeClassification leaf);

    /**
     * @return the majority class according to the observed leafs
     */
    public String getMajorityClass();

    /**
     * @return the index of the majority class according to the observed leafs
     */
    public int getMajorityClassIdx();

    /**
     * @param classValue the class for which the probability is required
     * @return the probability of class <b>classValue</b>
     */
    public float getClassProbabilityForClass(String classValue);

    /**
     * @param classIdx the index of the class for which the probability is required
     * @return the probability of the class at index <b>classIdx</b>
     */
    public float getClassProbabilityForClass(int classIdx);

    /**
     * @return the number of trees that voted
     */
    public int getNrVotes();
}
