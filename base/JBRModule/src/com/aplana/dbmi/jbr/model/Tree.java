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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import com.aplana.dbmi.model.ObjectId;

/**
 * ������ ������ ���������� � �������, ������ � ��������
 */
public class Tree {
	protected Map<ObjectId, TreeNode> tree = new HashMap<ObjectId, TreeNode>();
	
	public TreeNode getNode(ObjectId id){
		return tree.get(id);
	}
	
	public Collection<ObjectId> getAllIds(){
		Collection<ObjectId> all = new ArrayList<ObjectId>();
		for (TreeNode node : tree.values()) 
			all.add(node.getId());
		return all;
	}
	
	public Collection<ObjectId> getAllNodeIds(){
		Collection<ObjectId> all = new ArrayList<ObjectId>();
		for (TreeNode node : tree.values()) 
			if (!node.isLeaf())
				all.add(node.getId());
		return all;
	}

	public Collection<ObjectId> getAllLeafIds(){
		Collection<ObjectId> all = new ArrayList<ObjectId>();
		for (TreeNode node : tree.values()) 
			if (node.isLeaf())
				all.add(node.getId());
		return all;
	}

	public Collection<TreeNode> getAllNodes(){
		return Collections.unmodifiableCollection(tree.values());
	}
	
	
	public Collection<ObjectId> getPathTo(ObjectId id){
		Collection<ObjectId> path = new ArrayList<ObjectId>();
		TreeNode curr = tree.get(id);
		if (curr == null)
			return path;
		Collection<TreeNode> nodePath = curr.getPathFromRoot();
		for (TreeNode node : nodePath) 
			path.add(node.getId());
		return path;
	}
	
	public Collection<ObjectId> getChildren(ObjectId id){
		Collection<ObjectId> children = new ArrayList<ObjectId>();
		TreeNode curr = tree.get(id);
		if (curr == null)
			return children;
		Collection<TreeNode> nodeChildren = curr.getChildren();
		for (TreeNode node : nodeChildren) 
			children.add(node.getId());
		return children;
	}

	public Collection<ObjectId> getAllChildren(ObjectId id){
		Collection<ObjectId> children = new ArrayList<ObjectId>();
		TreeNode curr = tree.get(id);
		if (curr == null)
			return children;
		Collection<TreeNode> nodeChildren = curr.getAllChildren();
		for (TreeNode node : nodeChildren) 
			children.add(node.getId());
		return children;
	}
	
	public Collection<ObjectId> getAllChildrenNodes(ObjectId id){
		Collection<ObjectId> children = new ArrayList<ObjectId>();
		TreeNode curr = tree.get(id);
		if (curr == null)
			return children;
		for (TreeNode child : curr.getAllChildren()) 
			if (!child.isLeaf())
				children.add(child.getId());
		return children;
	}
	
	public Collection<ObjectId> getChildrenLeaves(ObjectId id){
		Collection<ObjectId> children = new ArrayList<ObjectId>();
		TreeNode curr = tree.get(id);
		if (curr == null)
			return children;
		for (TreeNode child : curr.getChildren()) 
			if (child.isLeaf())
				children.add(child.getId());
		return children;
	}
	
	/**
	 * �������� � ������ ������ ����� ������� (� ����������� ��������)
	 * @param parent - id ��������
	 * @param child - id �������
	 * @param isLeaf - ������� ����� (����, �� �������� ������ �������� ��������)
	 */
	public void addChild(ObjectId parent, ObjectId child, boolean isLeaf){
		TreeNode parentNode = tree.get(parent);
		if (parentNode == null)
			return;
		TreeNode childNode = new TreeNode(child, isLeaf);
		parentNode.addChild(childNode);
		tree.put(child, childNode);
	}

	/**
	 * �������� � ������ ������ ����� ������� (� ����������� ��������)
	 * @param parent - id ��������
	 * @param child - id �������
	 * @param isLeaf - ������� ����� (����, �� �������� ������ �������� ��������)
	 * @param attribute - ��� ��������, �� �������� �� �������� ��� ������� �������
	 */
	public void addChild(ObjectId parent, ObjectId child, boolean isLeaf, ObjectId attribute){
		TreeNode parentNode = tree.get(parent);
		if (parentNode == null)
			return;
		TreeNode childNode = new TreeNode(child, isLeaf, attribute);
		parentNode.addChild(childNode);
		tree.put(child, childNode);
	}

	public void addChildren(ObjectId parent, Collection<ObjectId> children, boolean isLeaf){
		TreeNode parentNode = tree.get(parent);
		if (parentNode == null)
			return;
		for (ObjectId child : children) {
			TreeNode childNode = new TreeNode(child, isLeaf);
			parentNode.addChild(childNode);
			tree.put(child, childNode);
		}
	}
	
	public void removeChild(ObjectId child){
		TreeNode childNode = tree.get(child);
		if (childNode == null)
			return;

		tree.remove(child);
		if (childNode.getParentsId() == null)
			return;
		TreeNode parentNode = tree.get(childNode.getParentsId());
		if (parentNode != null)
			parentNode.removeChild(childNode);
	}
	
	/** 
	 * ������� ������ ������
	 */
	public void setRoots(Collection<ObjectId> roots){
		for (ObjectId rootId : roots) {
			TreeNode rootNode = new TreeNode(rootId, false);
			tree.put(rootId, rootNode);
		}
	}
	
	/**
	 * ���������� � ������ ������ ����� � ��������� ��������, �� �������� �� ��� ������ �� ������� �������� (�� ��������� � ���� ������ ������, �� ������������ ��� ���������)
	 * @param rootId
	 * @param attribute
	 */
	public void addRoot(ObjectId rootId, ObjectId attribute){
		TreeNode rootNode = new TreeNode(rootId, false, attribute);
		tree.put(rootId, rootNode);
	}

	public void clear(){
		tree.clear();
	}
	
	public int size(){
		return tree.size();
	}
	
	/**
	 * ���������� ��������� ��� �������� ������ (����������)
	 * @param linkAttrIds - �������� ��� ���������� ������
	 * @param linkChildAttrIds - �������� ��� ���������� �����
	 * @param linkLeafAttrIds - �������� ��� ���������� �������
	 * @return ���������
	 */
	public Tree getSubTree(List<ObjectId> linkAttrIds, List<ObjectId> linkChildAttrIds, List<ObjectId> linkLeafAttrIds){
		Tree subTree = new Tree();
		final Collection<ObjectId> allAttrIds = new HashSet<ObjectId>(linkChildAttrIds);
		allAttrIds.addAll(linkLeafAttrIds);	// ������ � ������ ���������-������ � nodeLinkAttrIds � leafLinkAttrIds 
		
		// TODO: �������� ��� ��������� ����� ������� ��������� ������ - ���������� ���������� �� ���� ��������� ��� �� �������� �� ������ �� ������ (�.�. �����, ���� ��� ������)
		// DOING: ��� ������� ����������� ���������� ���������� �� ������������
		// ����������� �� �������� ������,�������������� � ��� ��������, ������� � ������, ����� � ����� � �������
		Collection<TreeNode> allNodes = getAllNodes();
		for (TreeNode curNode: allNodes){
			if (curNode.getParent() == null&&(linkAttrIds==null||linkAttrIds.isEmpty()||linkAttrIds.contains(curNode.getAttribute()))){	// ���� ������� ������� ������ - ������ � ���� ������ linkAttrIds ������ ��� �� �����, ���� ������� ������� ������� ��������� �� �������� �� linkAttrIds 
				subTree.addRoot(curNode.getId(), curNode.getAttribute());	// �������� ������ � ���������
				for(TreeNode childNode: curNode.getAllChildren()){			// �������� �� ���� �����
					if (allAttrIds==null||allAttrIds.isEmpty()||allAttrIds.contains(childNode.getAttribute())){
						subTree.addChild(curNode.getId(), childNode.getId(), childNode.isLeaf(), childNode.getAttribute());
					}
				}
			}
		}
		return subTree;
	}
}
