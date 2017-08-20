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
 */
package org.knime.core.gateway.v0.workflow.entity.impl;

import java.util.List;
import java.util.Optional;
import org.knime.core.gateway.v0.workflow.entity.BoundsEnt;
import org.knime.core.gateway.v0.workflow.entity.JobManagerEnt;
import org.knime.core.gateway.v0.workflow.entity.NodeAnnotationEnt;
import org.knime.core.gateway.v0.workflow.entity.NodeEnt;
import org.knime.core.gateway.v0.workflow.entity.NodeInPortEnt;
import org.knime.core.gateway.v0.workflow.entity.NodeMessageEnt;
import org.knime.core.gateway.v0.workflow.entity.NodeOutPortEnt;
import org.knime.core.gateway.v0.workflow.entity.WrappedWorkflowNodeEnt;
import org.knime.core.gateway.v0.workflow.entity.builder.WrappedWorkflowNodeEntBuilder;

import org.knime.core.gateway.entities.EntityBuilderFactory;
import org.knime.core.gateway.entities.EntityBuilderManager;

/**
 * Default implementation of the WrappedWorkflowNodeEntBuilder-interface. E.g. used if no other {@link EntityBuilderFactory}
 * implementation (provided via the respective extension point, see {@link EntityBuilderManager}) is available.
 *
 * @author Martin Horn, University of Konstanz
 */
 public class DefaultWrappedWorkflowNodeEntBuilder implements WrappedWorkflowNodeEntBuilder {
    
	List<NodeOutPortEnt> m_WorkflowIncomingPorts;
	List<NodeInPortEnt> m_WorkflowOutgoingPorts;
	boolean m_IsEncrypted;
	String m_VirtualInNodeID;
	String m_VirtualOutNodeID;
	Optional<String> m_ParentNodeID = Optional.empty();
	String m_RootWorkflowID;
	Optional<JobManagerEnt> m_JobManager = Optional.empty();
	NodeMessageEnt m_NodeMessage;
	List<NodeInPortEnt> m_InPorts;
	List<NodeOutPortEnt> m_OutPorts;
	String m_Name;
	String m_NodeID;
	String m_NodeType;
	BoundsEnt m_Bounds;
	boolean m_IsDeletable;
	String m_NodeState;
	boolean m_HasDialog;
	NodeAnnotationEnt m_NodeAnnotation;

	@Override
    public WrappedWorkflowNodeEnt build() {
        return new DefaultWrappedWorkflowNodeEnt(this);
    }

	@Override
    public WrappedWorkflowNodeEntBuilder setWorkflowIncomingPorts(final List<NodeOutPortEnt> WorkflowIncomingPorts) {
		m_WorkflowIncomingPorts = WorkflowIncomingPorts;			
        return this;
    }
        
	@Override
    public WrappedWorkflowNodeEntBuilder setWorkflowOutgoingPorts(final List<NodeInPortEnt> WorkflowOutgoingPorts) {
		m_WorkflowOutgoingPorts = WorkflowOutgoingPorts;			
        return this;
    }
        
	@Override
    public WrappedWorkflowNodeEntBuilder setIsEncrypted(final boolean IsEncrypted) {
		m_IsEncrypted = IsEncrypted;			
        return this;
    }
        
	@Override
    public WrappedWorkflowNodeEntBuilder setVirtualInNodeID(final String VirtualInNodeID) {
		m_VirtualInNodeID = VirtualInNodeID;			
        return this;
    }
        
	@Override
    public WrappedWorkflowNodeEntBuilder setVirtualOutNodeID(final String VirtualOutNodeID) {
		m_VirtualOutNodeID = VirtualOutNodeID;			
        return this;
    }
        
	@Override
    public WrappedWorkflowNodeEntBuilder setParentNodeID(final Optional<String> ParentNodeID) {
		m_ParentNodeID = ParentNodeID;			
        return this;
    }
        
	@Override
    public WrappedWorkflowNodeEntBuilder setRootWorkflowID(final String RootWorkflowID) {
		m_RootWorkflowID = RootWorkflowID;			
        return this;
    }
        
	@Override
    public WrappedWorkflowNodeEntBuilder setJobManager(final Optional<JobManagerEnt> JobManager) {
		m_JobManager = JobManager;			
        return this;
    }
        
	@Override
    public WrappedWorkflowNodeEntBuilder setNodeMessage(final NodeMessageEnt NodeMessage) {
		m_NodeMessage = NodeMessage;			
        return this;
    }
        
	@Override
    public WrappedWorkflowNodeEntBuilder setInPorts(final List<NodeInPortEnt> InPorts) {
		m_InPorts = InPorts;			
        return this;
    }
        
	@Override
    public WrappedWorkflowNodeEntBuilder setOutPorts(final List<NodeOutPortEnt> OutPorts) {
		m_OutPorts = OutPorts;			
        return this;
    }
        
	@Override
    public WrappedWorkflowNodeEntBuilder setName(final String Name) {
		m_Name = Name;			
        return this;
    }
        
	@Override
    public WrappedWorkflowNodeEntBuilder setNodeID(final String NodeID) {
		m_NodeID = NodeID;			
        return this;
    }
        
	@Override
    public WrappedWorkflowNodeEntBuilder setNodeType(final String NodeType) {
		m_NodeType = NodeType;			
        return this;
    }
        
	@Override
    public WrappedWorkflowNodeEntBuilder setBounds(final BoundsEnt Bounds) {
		m_Bounds = Bounds;			
        return this;
    }
        
	@Override
    public WrappedWorkflowNodeEntBuilder setIsDeletable(final boolean IsDeletable) {
		m_IsDeletable = IsDeletable;			
        return this;
    }
        
	@Override
    public WrappedWorkflowNodeEntBuilder setNodeState(final String NodeState) {
		m_NodeState = NodeState;			
        return this;
    }
        
	@Override
    public WrappedWorkflowNodeEntBuilder setHasDialog(final boolean HasDialog) {
		m_HasDialog = HasDialog;			
        return this;
    }
        
	@Override
    public WrappedWorkflowNodeEntBuilder setNodeAnnotation(final NodeAnnotationEnt NodeAnnotation) {
		m_NodeAnnotation = NodeAnnotation;			
        return this;
    }
        
}

