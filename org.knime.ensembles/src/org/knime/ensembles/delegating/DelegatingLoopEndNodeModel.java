package org.knime.ensembles.delegating;

import java.io.File;
import java.io.IOException;

import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.RowKey;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.workflow.LoopEndNode;

/**
 * This is the model implementation of Delegating.
 * 
 * 
 * @author Iris Adae
 */
public class DelegatingLoopEndNodeModel extends NodeModel 
            implements LoopEndNode {

    

//    private static final NodeLogger LOGGER 
//                       = NodeLogger.getLogger(DelegatingLoopEndNodeModel.class);
    private BufferedDataContainer m_outcontainer;

    /**
     * this variable contains a mapping from columns that are same 
     * (Same datatype and columnname) 
     * if columns, of the input data table are not found in the input of the
     * loop, it tries to find a compatible column (
     * Matching at least, the data type).
     */
//    private HashMap<Integer, Integer> m_mappingLoopInToLoopEnd;

	private BufferedDataTable m_inData;
	private int m_count = 0;

    /**
     * Constructor for the node model.
     */
    protected DelegatingLoopEndNodeModel() {
        super(2, 1);
    }
    
    private static int CollectingIn = 0;
    private static int ResultingIn = 1;
    /**
     * {@inheritDoc}
     */
    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
            final ExecutionContext exec) throws Exception {
    	// outport 0 wird gesammelt und hinten ausgegeben
    	// outport 1 wird an loop start zurück gegeben
    	
    	if(m_outcontainer == null){
    		m_outcontainer =
                    exec.createDataContainer(inData[CollectingIn].getDataTableSpec());
    	}
    	
        BufferedDataContainer loopData = exec.createDataContainer(
        		inData[ResultingIn].getDataTableSpec());
        for(DataRow row:inData[ResultingIn]){
        	loopData.addRowToTable(row);
        }
        loopData.close();
        m_inData  = loopData.getTable();

        for(DataRow row : inData[CollectingIn]){
        	m_outcontainer.addRowToTable(createNewRow(row));
        }
        // stop loop if there is  no more data
        if (m_inData.getRowCount()<1) {
            m_outcontainer.close();
            return new BufferedDataTable[]{m_outcontainer.getTable()};
        }
        // else go on with loop
        m_count++;
        super.continueLoop();
        return new BufferedDataTable[]{null};
    }
    
    private DataRow createNewRow(final DataRow row) {
		RowKey newKey = new RowKey(row.getKey() + "#" + m_count);
        return new DefaultRow(newKey, row);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void reset() {
    	m_count = 0;
        m_outcontainer = null;
//        m_mappingLoopInToLoopEnd = null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected DataTableSpec[] configure(final DataTableSpec[] inSpecs)
            throws InvalidSettingsException {
        // same as the input to the StartNode
        if (getLoopStartNode() != null) {
            return new DataTableSpec[]{
                    ((DelegatingLoopStartNodeModel)getLoopStartNode())
                                .getInputSpec()};
        }
        return new DataTableSpec[]{null};
    }
    
//    private void findColumnMapping(final DataTableSpec initial,
//            final DataTableSpec out) {
//
//        m_mappingLoopInToLoopEnd = new HashMap<Integer, Integer>();
//      
//        // there was a reset initialize the mapping.
//        // we need to simply find to which cell number of the input, the
//        // data is matching best.
//        for (DataColumnSpec in : initial) {
//            for (DataColumnSpec loopendincol : out) {
//                if (in.getName().equals(loopendincol.getName())
//                        && in.getType().equals(loopendincol.getType())) {
//                    int i = initial.findColumnIndex(in.getName());
//                    int o = out.findColumnIndex(loopendincol.getName());
//                    m_mappingLoopInToLoopEnd.put(i, o);
//
//                    LOGGER.warn("Could succesfully match input column " + i
//                            + " to loop end in column " + o
//                            + " both having name " + in.getName()
//                            + " and type " + in.getType().toString());
//                    break;
//                }
//            }
//        }
//        // now we try to find the rest with at least the same data type.
//
//        // we always start at the end, so hopefully
//        // the inputs are at least still in the beginning.
//        for (int inC = initial.getNumColumns() - 1; inC > -1; inC--) {
//            if (!m_mappingLoopInToLoopEnd.containsKey(new Integer(inC))) {
//                DataColumnSpec inDCS = initial.getColumnSpec(inC);
//                for (int outC = out.getNumColumns() - 1; outC > -1; outC--) {
//                    if (!m_mappingLoopInToLoopEnd.containsValue(new Integer(
//                            outC))) {
//                        DataColumnSpec outDCS = out.getColumnSpec(outC);
//                        if (inDCS.getType().equals(outDCS.getType())) {
//                            // at least we find free type matching
//                            int i = inC;
//                            int o = outC;
//                            m_mappingLoopInToLoopEnd.put(i, o);
//
//                            LOGGER
//                                    .warn("There was no perfect fit column in " 
//                                            + "the loop end in"
//                                            + " matching the initial column nr "
//                                            + i
//                                            + " . \r\n"
//                                            + " Used column nr "
//                                            + o
//                                            + " of the loop end inport instead."
//                                            + " \r\n"
//                                            + "Same type put different names ("
//                                            + inDCS.getName()
//                                            + " <-> "
//                                            + outDCS.getName() + ")");
//                            break;
//                        }
//                    }
//                }
//
//            }
//        }
//
//        for (int inC = initial.getNumColumns() - 1; inC > -1; inC--) {
//            if (!m_mappingLoopInToLoopEnd.containsKey(new Integer(inC))) {
//                LOGGER.warn("can't find any matching column in "
//                        + " the indata of the loop end, to column "
//                        + initial.getColumnSpec(inC) + " ( Nr. " + inC
//                        + ")\r\n" + "All cells will be missing.");
//            }
//        }
//    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        // nothing to save
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        // nothing to load
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        // nothing to validate
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadInternals(final File internDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
        // nothing to load
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveInternals(final File internDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
        // nothing to save
    }

	public BufferedDataTable getInData() {
		return m_inData;
	}

}
