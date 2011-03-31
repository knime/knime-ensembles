package org.knime.ensembles.delegating;

import java.io.File;
import java.io.IOException;
import org.knime.core.data.DataTableSpec;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.workflow.LoopStartNode;

/**
 * This is the model implementation of Delegating.
 * 
 *
 * @author Iris Adae, University of Konstanz, Germany
 */
public class DelegatingLoopStartNodeModel extends NodeModel
		implements LoopStartNode{
 
    private int m_currentiteration;

    private DataTableSpec m_inSpec;
    
    /**
     * Constructor for the node model.
     */
    protected DelegatingLoopStartNodeModel() {
        super(1, 1);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
            final ExecutionContext exec) throws Exception {
    	
        m_currentiteration++;

        m_inSpec = inData[0].getDataTableSpec();

        if (m_currentiteration == 1) {
            // just output the complete data table.
        	 return new BufferedDataTable[]{inData[0]};
        } 

        //otherwise we get the data from the loop end node
        DelegatingLoopEndNodeModel end =
                    (DelegatingLoopEndNodeModel)getLoopEndNode();
        BufferedDataTable fromend = end.getInData();
        System.out.println(fromend.getRowCount());

        return new BufferedDataTable[]{fromend};
    }
    
    /**
     * @return the input spec as inserted to the loop start.
     */
    public DataTableSpec getInputSpec() {
         return m_inSpec;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void reset() {
        m_currentiteration = 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected DataTableSpec[] configure(final DataTableSpec[] inSpecs)
            throws InvalidSettingsException {

        m_inSpec = inSpecs[0];
        return new DataTableSpec[]{m_inSpec};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadInternals(final File internDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
        // TODO: generated method stub
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveInternals(final File internDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
        // TODO: generated method stub
    }

}

