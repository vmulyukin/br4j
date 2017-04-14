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
package com.aplana.dbmi.jbr.model;

import java.util.Set;

import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.service.DataException;

/**
 * @author RAbdullin@aplana.com, VMmulyukin@aplana.com
 * @comment RZaynullin ��������� ������, ���������� ��� ����������
 * ������������� CopyPersonFromLinkedCards.
 * ��������� ��� ��������� ���� ����� ����������.
 * ������������� ��� ������ ��������� � ���������� ������ � ������(���) �
 * ���������(��) ��������� (���). 
 */
public interface GraphProcessor {	

	/**
	 * @return ������ ����� ��� ���������.
	 */
	Tree getTree();
	
	/**
	 * @param ������ ����� ��� ���������.
	 */
	void setTree(Tree tree);
	
	
	/**
	 * @return id �������� �������� ���� � ���������.
	 */
	ObjectId getCurNodeId();

	/**
	 * @param id �������� �������� ���� � ���������.
	 * ���������� ��� ����� processNode() ����������c��.
	 */
	void setCurNodeId( ObjectId nodeId);

	/**
	 * @return id �������� ������ �������� ���� � ���������.
	 */
	ObjectId getOriginNodeId();

	/**
	 * @return id �������� ������ �������� ���� � ���������.
	 * ���������� ��� ����� processNode() ����������c��.
	 */
	void setOriginNodeId( ObjectId nodeId);


	/**
	 * @return ���������������� ���� ������� ���� (���������� ����� �����).
	 */
	Set<ObjectId> getChildNodes();

	/**
	 * @param children ���������������� ���� ������� ���� (���������� ����� �����).
	 * ���������� ��� ����� processNode() ����������c��.
	 */
	void setChildNodes(Set<ObjectId> children);


	/**
	 * @return ��� ���� ������� ���� (�� ���� ����������� ������� ������� ����, 
	 * ������� childNodes).
	 */
	Set<ObjectId> getAllChildNodes();

	/**
	 * @param children ��� ���� ������� ���� (�� ���� ����������� ������� 
	 * ������� ����, ������� childNodes).
	 * ���������� ��� ����� processNode() ����������c��.
	 */
	void setAllChildNodes(Set<ObjectId> children);


	/**
	 * @return ���������������� �������� ������� ���� (� ����������� ����� �����).
	 */
	Set<ObjectId> getParentNodes();

	/**
	 * @param parents ������ ���������������� ��������� ������� ���� (� ����������� ����� �����).
	 * ���������� ��� ����� processNode() ����������c��.
	 */
	void setParentNodes(Set<ObjectId> parents);


	/**
	 * @return ��� �������� ������� ���� (�� ���� ����������� ������� ������� 
	 * ����, ������� parentNodes).
	 */
	Set<ObjectId> getAllParentNodes();

	/**
	 * @param parents ��� �������� ������� ���� (�� ���� ����������� ������� 
	 * ������� ����, ������� parentNodes).
	 * ���������� ��� ����� processNode() ����������c��.
	 */
	void setAllParentNodes(Set<ObjectId> parents);

	Set<ObjectId> getCopyToNodes();

	void setCopyToNodes(Set<ObjectId> copyToNodes);


	/**
	 * �������� ����� ���������. ���������� �� ��� ��������������� ��� 
	 * ���������� � ����� (�������� ������� ������). �������� �������� 
	 * ������������� ����� ����������.
	 * @return true, ���� ���� ��������� ����� ������ ������, false, ���� ���. 
	 * @throws DataException 
	 */
	boolean processNode() throws DataException;
}
