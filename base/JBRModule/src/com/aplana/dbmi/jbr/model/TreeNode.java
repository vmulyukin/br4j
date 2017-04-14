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
package com.aplana.dbmi.jbr.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;

import com.aplana.dbmi.model.ObjectId;

/**
 * ������� ������ ������: ����, ������ ��� ����
 * (13.10.2010, YNikitin) ������� �������� @param attribute - ��� ��������, �� �������� ��� ����������� ������� � �������� �������� �� ������������� 
 */
public class TreeNode {
	protected ObjectId id;
	protected TreeNode parent;
	protected ObjectId attribute;	// ������� ����� � ���������
	protected HashSet<TreeNode> children = new HashSet<TreeNode>();
	protected boolean leaf;
	
	public TreeNode(ObjectId id, boolean isLeaf){
		this.id = id;
		leaf = isLeaf;
		attribute = null;
	}
	
	public TreeNode(ObjectId id, boolean isLeaf, ObjectId attribute){
		this.id = id;
		leaf = isLeaf;
		this.attribute = attribute;
	}

	public ObjectId getId() {
		return id;
	}

	public boolean isLeaf() {
		return leaf;
	}
	
	public Collection<TreeNode> getLeaves(){
		Collection<TreeNode> leaves = new HashSet<TreeNode>();
		for (TreeNode node : getChildren()) 
			if (node.isLeaf())
				leaves.add(node);
		return leaves;
	}
	
	public Collection<TreeNode> getAllLeaves(){
		Collection<TreeNode> leaves = new HashSet<TreeNode>();
		for (TreeNode node : getAllChildren()) 
			if (node.isLeaf())
				leaves.add(node);
		return leaves;
	}
	
	public void addChild(TreeNode child){
		children.add(child);
		child.setParent(this);
	}

	public void addChildren(Collection<TreeNode> childList){
		childList.addAll(childList);
		for (TreeNode child : childList) {
			child.setParent(this);
		}
	}

	public void removeChild(TreeNode child){
		if (children.contains(child)){
			children.remove(child);
			child.setParent(getParent());
		}
	}

	public Collection<TreeNode> getAllChildren(){
		Collection<TreeNode> allChildren = new HashSet<TreeNode>(getChildren());
		Collection<TreeNode> curLevel = new ArrayList<TreeNode>(getChildren());
		while (!curLevel.isEmpty()){
			Collection<TreeNode> nextLevel = new ArrayList<TreeNode>();
			for (TreeNode node : curLevel) 
				nextLevel.addAll(node.getChildren());
			curLevel = nextLevel;
			allChildren.addAll(curLevel);
		}
		return allChildren;
	}
	
	public List<TreeNode> getPathFromRoot(){
		ArrayList<TreeNode> parents = new ArrayList<TreeNode>();
		TreeNode curNode = getParent();
		while (curNode != null){
			parents.add(curNode);
			curNode = curNode.getParent();
		}
		Collections.reverse(parents);
		return parents;
	}
	
	public Collection<TreeNode> getAllRelatedNodes(){
		Collection<TreeNode> relatedTree = new ArrayList<TreeNode>(getAllChildren());
		relatedTree.addAll(getPathFromRoot());
		return relatedTree;
	}
	
	public Collection<TreeNode> getAllRelatedLeaves(){
		final List<TreeNode> relatedTree = new ArrayList<TreeNode>(getAllChildren());
		relatedTree.addAll(getPathFromRoot());
		for (ListIterator<TreeNode> i = relatedTree.listIterator(); i.hasNext(); ){
			final TreeNode node = i.next();
			if (!node.isLeaf())
				i.remove();
		}
		
		return relatedTree;
	}
	
	public TreeNode getParent(){
		return parent;
	}

	public ObjectId getParentsId(){
		if (getParent() == null)
			return null;
		return getParent().getId();
	}

	public void setParent(TreeNode parent){
		this.parent = parent;
	}
	
	public Collection<TreeNode> getChildren(){
		return new ArrayList<TreeNode>(children);
	}
	
	@Override
	public int hashCode() {
		return id.hashCode();
	}
	
	@Override
	public String toString() {
		return "parent:"+ (parent == null ? "none" : parent.getId().getId()) +
			" id:"+ (id == null ? "none" : id.getId()) +
			" leaf:"+leaf+
			" attrubute:"+attribute;
	}

	/**
	 * @return the attribute
	 */
	public ObjectId getAttribute() {
		return attribute;
	}
}
