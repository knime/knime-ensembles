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
 *   14.01.2016 (Adrian Nembach): created
 */
package org.knime.base.node.mine.treeensemble2.node.gradientboosting.learner.classification;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.knime.base.node.mine.treeensemble2.data.TreeData;
import org.knime.base.node.mine.treeensemble2.data.TreeDataCreator;
import org.knime.base.node.mine.treeensemble2.learner.gradientboosting.AbstractGradientBoostingLearner;
import org.knime.base.node.mine.treeensemble2.learner.gradientboosting.LKGradientBoostedTreesLearner;
import org.knime.base.node.mine.treeensemble2.model.AbstractGradientBoostingModel;
import org.knime.base.node.mine.treeensemble2.model.GradientBoostingModelPortObject;
import org.knime.base.node.mine.treeensemble2.model.TreeEnsembleModelPortObjectSpec;
import org.knime.base.node.mine.treeensemble2.node.gradientboosting.learner.GradientBoostingLearnerConfiguration;
import org.knime.base.node.mine.treeensemble2.node.learner.TreeEnsembleLearnerConfiguration.FilterLearnColumnRearranger;
import org.knime.core.data.DataTableSpec;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;

/**
 *
 * @author Adrian Nembach, KNIME GmbH, Konstanz, Germany
 */
public class GradientBoostingClassificationLearnerNodeModel extends NodeModel {

    private GradientBoostingLearnerConfiguration m_configuration;

    /**
     * If set to true, AP-7245 is fixed. For problems with many classes, it was possible that an overflow
     * occurred in the softmax which led to NaN values in the model parameters.
     * The solution is to use a variant of the softmax that is safeguarded against numerical overflow.
     */
    private final boolean m_safeSoftmax;

    /**
     * If set to true, AP-12360 is fixed. During the update of the model estimates, the last nominal value of
     * categorical columns without any missing values was treated as missing.
     */
    private final boolean m_fixNominalValueMixup;

    /**
     * Constructor for nodes created prior to KNIME Analytics Platform 4.0.1
     * @deprecated Use {@link GradientBoostingClassificationLearnerNodeModel#GradientBoostingClassificationLearnerNodeModel(boolean)} instead.
     */
    @Deprecated
    protected GradientBoostingClassificationLearnerNodeModel() {
        this(true);
    }

    /**
     * @param pre401 set to true for versions prior to 4.0.1
     */
    protected GradientBoostingClassificationLearnerNodeModel(final boolean pre401) {
        super(new PortType[]{BufferedDataTable.TYPE}, new PortType[]{GradientBoostingModelPortObject.TYPE});
        m_safeSoftmax = !pre401;
        m_fixNominalValueMixup = !pre401;
    }



    /**
     * {@inheritDoc}
     */
    @Override
    protected PortObjectSpec[] configure(final PortObjectSpec[] inSpecs) throws InvalidSettingsException {
        // guaranteed to not be null (according to API)
        DataTableSpec inSpec = (DataTableSpec)inSpecs[0];
        if (m_configuration == null) {
            throw new InvalidSettingsException("No configuration available");
        }
        final FilterLearnColumnRearranger learnRearranger = m_configuration.filterLearnColumns(inSpec);
//        final String warn = learnRearranger.getWarning();
//        if (warn != null) {
//            setWarningMessage(warn);
//        }
        m_configuration.checkColumnSelection(inSpec);
        DataTableSpec learnSpec = learnRearranger.createSpec();
        TreeEnsembleModelPortObjectSpec ensembleSpec = m_configuration.createPortObjectSpec(learnSpec);
        ensembleSpec.assertTargetTypeMatches(false);
        // the following call may return null, which is OK during configure
        // but not upon execution (spec may not be populated yet, e.g.
        // predecessor not executed)
        // if the possible values is not null, the following call checks
        // for duplicates in the toString() representation
        ensembleSpec.getTargetColumnPossibleValueMap();

        return new PortObjectSpec[]{ensembleSpec};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected PortObject[] execute(final PortObject[] inData, final ExecutionContext exec) throws Exception {
        BufferedDataTable t = (BufferedDataTable)inData[0];
        DataTableSpec spec = t.getDataTableSpec();
        final FilterLearnColumnRearranger learnRearranger = m_configuration.filterLearnColumns(spec);
        List<String> warnings = new ArrayList<>();
        warnings.add(learnRearranger.getWarning());
        BufferedDataTable learnTable = exec.createColumnRearrangeTable(t, learnRearranger, exec.createSubProgress(0.0));
        DataTableSpec learnSpec = learnTable.getDataTableSpec();
        TreeEnsembleModelPortObjectSpec ensembleSpec = m_configuration.createPortObjectSpec(learnSpec);
        ExecutionMonitor readInExec = exec.createSubProgress(0.1);
        ExecutionMonitor learnExec = exec.createSubProgress(0.8);
        exec.setProgress("Reading data into memory");
        TreeData data = createTreeData(warnings, learnTable, readInExec);
        readInExec.setProgress(1.0);
        exec.setMessage("Learning trees");
        AbstractGradientBoostingLearner learner = new LKGradientBoostedTreesLearner(m_configuration, data, m_safeSoftmax, m_fixNominalValueMixup);
        AbstractGradientBoostingModel model;
//        m_configuration.setMissingValueHandling(MissingValueHandling.XGBoost);
//        try {
            model = learner.learn(learnExec);
//        } catch (ExecutionException e) {
//            Throwable cause = e.getCause();
//            if (cause instanceof Exception) {
//                throw (Exception)cause;
//            }
//            throw e;
//        }
        GradientBoostingModelPortObject modelPortObject = new GradientBoostingModelPortObject(ensembleSpec, model);
        learnExec.setProgress(1.0);
        var warn = warnings.stream()//
            .filter(Objects::nonNull)//
            .filter(s -> !s.isBlank())//
            .collect(Collectors.joining("\n"));
        if (!warn.isBlank()) {
            setWarningMessage(warn);
        }
        return new PortObject[]{modelPortObject};
    }

    private TreeData createTreeData(final List<String> warnings, final BufferedDataTable learnTable,
        final ExecutionMonitor readInExec) throws CanceledExecutionException {
        TreeDataCreator dataCreator =
            new TreeDataCreator(m_configuration, learnTable.getDataTableSpec(), learnTable.getRowCount());
        TreeData data = dataCreator.readData(learnTable, m_configuration, readInExec);
        String dataCreationWarning = dataCreator.getAndClearWarningMessage();
        warnings.add(dataCreationWarning);
        return data;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadInternals(final File nodeInternDir, final ExecutionMonitor exec)
        throws IOException, CanceledExecutionException {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveInternals(final File nodeInternDir, final ExecutionMonitor exec)
        throws IOException, CanceledExecutionException {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        if (m_configuration != null) {
            m_configuration.save(settings);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
        new GradientBoostingLearnerConfiguration(false).loadInModel(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
        GradientBoostingLearnerConfiguration config = new GradientBoostingLearnerConfiguration(false);
        config.loadInModel(settings);
        m_configuration = config;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void reset() {
        // TODO Auto-generated method stub

    }

}
