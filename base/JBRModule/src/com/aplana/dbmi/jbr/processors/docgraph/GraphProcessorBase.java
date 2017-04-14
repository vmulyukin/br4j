/**
 *   Licensed to the Apache Software Foundation (ASF) under one or more
 *   contributor license agreements.  See the NOTICE file distributed with
 *   this work for additional information regarding copyright ownership.
 *   The ASF licenses this file to you under the Apache License, Version 2.0
 *   (the "License"); you may not use this file except in compliance with
 *   the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
/**
 * 
 */
package com.aplana.dbmi.jbr.processors.docgraph;

import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.aplana.dbmi.jbr.model.GraphProcessor;
import com.aplana.dbmi.jbr.model.Tree;
import com.aplana.dbmi.jbr.processors.ProcessCard;
import com.aplana.dbmi.jbr.util.CardUtils;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.service.DataException;

/**
 * @author RAbdullin
 * ����������� ����� ��� ���� �������������� TreeProcessor-�
 */
public abstract class GraphProcessorBase 
		extends ProcessCard 
		implements GraphProcessor 
{
	private Tree tree;					// �������������� TreeProcessor-�� ������ ������
	
	private ObjectId curNodeId;			// ������� ��������
	private ObjectId originNodeId;		// ����� ������� ��������

	private Set<ObjectId> children;		// ���� ������� ������
	private Set<ObjectId> allChildren;	// ��� ����

	private Set<ObjectId> parents;		
	private Set<ObjectId> allParents;

	private Set<ObjectId> copyToNodeIds;// ������ ������� �������� (���������� ��� ��������� ��������������)

	/* (non-Javadoc)
	 * @see com.aplana.dbmi.jbr.model.GraphProcessor#getTree()
	 */
	public Tree getTree() {
		return this.tree;
	}
	
	/* (non-Javadoc)
	 * @see com.aplana.dbmi.jbr.model.GraphProcessor#setTree()
	 */
	public void setTree(Tree tree) {
		this.tree = tree;
	}
	
	/* (non-Javadoc)
	 * @see com.aplana.dbmi.jbr.model.GraphProcessor#getCurNode()
	 */
	public ObjectId getCurNodeId() {
		return this.curNodeId;
	}

	/* (non-Javadoc)
	 * @see com.aplana.dbmi.jbr.model.GraphProcessor#setCurNode(com.aplana.dbmi.model.ObjectId)
	 */
	public void setCurNodeId(ObjectId nodeId) {
		this.curNodeId = nodeId; 
	}

	/* (non-Javadoc)
	 * @see com.aplana.dbmi.jbr.model.GraphProcessor#getOriginNodeId()
	 */
	public ObjectId getOriginNodeId() {
		return this.originNodeId;
	}

	/* (non-Javadoc)
	 * @see com.aplana.dbmi.jbr.model.GraphProcessor#setOriginNodeId(com.aplana.dbmi.model.ObjectId)
	 */
	public void setOriginNodeId(ObjectId nodeId) {
		this.originNodeId = nodeId;
	}

	/* (non-Javadoc)
	 * @see com.aplana.dbmi.jbr.model.GraphProcessor#getChildNodes()
	 */
	public Set<ObjectId> getChildNodes() {
		return this.children;
	}

	/* (non-Javadoc)
	 * @see com.aplana.dbmi.jbr.model.GraphProcessor#setChildNodes(java.util.Set)
	 */
	public void setChildNodes(Set<ObjectId> children) {
		this.children = children;
	}

	/* (non-Javadoc)
	 * @see com.aplana.dbmi.jbr.model.GraphProcessor#getAllChildNodes()
	 */
	public Set<ObjectId> getAllChildNodes() {
		return this.allChildren;
	}

	/* (non-Javadoc)
	 * @see com.aplana.dbmi.jbr.model.GraphProcessor#setAllChildNodes(java.util.Set)
	 */
	public void setAllChildNodes(Set<ObjectId> children) {
		this.allChildren = children;
	}

	/* (non-Javadoc)
	 * @see com.aplana.dbmi.jbr.model.GraphProcessor#getParentNodes()
	 */
	public Set<ObjectId> getParentNodes() {
		return this.parents;
	}

	/* (non-Javadoc)
	 * @see com.aplana.dbmi.jbr.model.GraphProcessor#setParentNodes(java.util.Set)
	 */
	public void setParentNodes(Set<ObjectId> parents) {
		this.parents = parents;
	}

	/* (non-Javadoc)
	 * @see com.aplana.dbmi.jbr.model.GraphProcessor#getAllParentNodes()
	 */
	public Set<ObjectId> getAllParentNodes() {
		return this.allParents;
	}

	/* (non-Javadoc)
	 * @see com.aplana.dbmi.jbr.model.GraphProcessor#setAllParentNodes(java.util.Set)
	 */
	public void setAllParentNodes(Set<ObjectId> parents) {
		this.allParents = parents;
	}

	public Set<ObjectId> getCopyToNodes() {
		return copyToNodeIds;
	}

	public void setCopyToNodes(Set<ObjectId> destinations) {
		this.copyToNodeIds = destinations;
	}

	/* (non-Javadoc)
	 * @see com.aplana.dbmi.jbr.model.GraphProcessor#processNode()
	 */
	abstract public boolean processNode() throws DataException;

	@Override
	public Object process() {
		return null;
	}
}
