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
 *   Sep 12, 2019 (Adrian Nembach, KNIME GmbH, Konstanz, Germany): created
 */
package org.knime.base.node.mine.treeensemble2.data;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.filestore.FileStoreFactory;
import org.knime.core.data.probability.nominal.NominalDistributionCell;
import org.knime.core.data.probability.nominal.NominalDistributionCellFactory;
import org.knime.core.data.probability.nominal.NominalDistributionValueMetaData;

/**
 *
 * @author Adrian Nembach, KNIME GmbH, Konstanz, Germany
 */
public class TreeTargetProbabilisticNominalColumnDataCreatorTest {

    private TreeTargetProbabilisticNominalColumnDataCreator m_creator;

    private static final String[] VALUES = {"A", "B", "C"};

    private static final NominalDistributionCellFactory FACTORY =
        new NominalDistributionCellFactory(FileStoreFactory.createNotInWorkflowFileStoreFactory(), VALUES);

    private static NominalDistributionCell prob(final double... ds) {
        assert ds.length == VALUES.length;
        return FACTORY.createCell(ds, 1e-5);
    }

    @Before
    public void init() {
        final DataColumnSpecCreator specCreator =
            new DataColumnSpecCreator("test", NominalDistributionCellFactory.TYPE);
        specCreator.addMetaData(new NominalDistributionValueMetaData(VALUES), true);
        final DataColumnSpec spec = specCreator.createSpec();
        m_creator = new TreeTargetProbabilisticNominalColumnDataCreator(spec);
    }

    @Test
    public void testCreate() throws Exception {
        NominalDistributionCell[] cells = {prob(0.3, 0.2, 0.5), prob(0.7, 0.1, 0.2), prob(0.2, 0.8, 0)};
        Arrays.stream(cells).forEach(m_creator::add);
        final TreeTargetProbabilisticNominalColumnData target = m_creator.createColumnData();
        for (int i = 0; i < cells.length; i++) {
            for (int c = 0; c < 3; c++) {
                assertEquals(cells[i].getProbability(VALUES[c]), target.getProbability(i, c), 1e-6);
            }
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIncompatibleTypeInConstructor() throws Exception {
        final DataColumnSpecCreator specCreator = new DataColumnSpecCreator("test", DoubleCell.TYPE);
        new TreeTargetProbabilisticNominalColumnDataCreator(specCreator.createSpec());
    }

}
