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
package com.aplana.dbmi.admin;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;

import org.springframework.beans.BeanUtils;

import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.LocalizedString;
import com.aplana.dbmi.model.TemplateBlock;
import com.aplana.dbmi.model.WorkflowMove;
import com.aplana.dbmi.model.WorkflowMoveRequiredField;
import com.aplana.dbmi.service.DataException;

public class WebTemplateWorkflowMovesReqAttrBean implements Serializable {
	private static final String EMPTY_STRING = "";
	public final static String APPLY_TEMPLATE = "APPLY_TEMPLATE";
	public final static String CLOSE_TEMPLATE = "CLOSE_TEMPLATE";

	public final static String ALL_WORKFLOW_MOVES = "";
	public final static String ALL_TEMPLATE_BLOCKS = "";
	public final static String ALL_ATTRIBUTES = "";

	public final static String ACTION_ADD_ATTR = "add";
	public final static String ACTION_REM_ATTR = "rem";
	
	private static final long serialVersionUID = 3270165188141809849L;

	private String message;


	private Collection workflowMovesConst;
	private Collection blocksConst;

	// Collection<ShowWorkflowMoveRequiredField>
	private final Collection workflowAllMovesRequiredFlds = new ArrayList();

	// ������ workflowAllRequiredFlds, ��������������� �� workflowId:
	// 		Map< String(wfmid), Collection<Attribute> >
	// private final Map wfmRequiredFields = new HashMap(); // OLD: Collection

	private final Collection showWorkflowMovesRequiredFields= new ArrayList();
	static private final Collection requiredStates = new ArrayList();
	static {
		requiredStates.add( new ItemTag( WorkflowMoveRequiredField.MUSTBESET_ASTEMPLATE, 
				"��� � �������"));
		requiredStates.add( new ItemTag( WorkflowMoveRequiredField.MUSTBESET_ASSIGNED, 
				"������������/���������"));
		requiredStates.add( new ItemTag( WorkflowMoveRequiredField.MUSTBESET_BLANK, 
				"������������/�� ���������"));
	}


	private String selectedWorkflowMoveId;
	private String selectedTemplateBlockId;
	private String selectedAttributeId;
	private String templateApplyClose;
	private String selectedRequiredState; // (2010/01/21, RuSA)
	private String selectedAction; // ��������� �������� ('rem', 'add')


	private String removeWorkflowMoveId;
	private String removeTemplateBlockId;
	private String removeAttributeId;


	public void reset() {
		workflowMovesConst = null;
		blocksConst = null;
		message = null;
		workflowAllMovesRequiredFlds.clear();
		// wfmRequiredFields.clear();
		showWorkflowMovesRequiredFields.clear();

		selectedWorkflowMoveId = null;
		selectedTemplateBlockId = null;
		selectedAttributeId = null;
		selectedRequiredState = null; 
	}


	static WorkflowMove wfmCopy(final WorkflowMove src)
	{
		if (src == null)
			return null;

		// new WorkflowMove();
		final WorkflowMove result = (src.getId() != null)
					? (WorkflowMove) WorkflowMove.createFromId(src.getId())
					: new WorkflowMove(); 

		// significant properties: "id", "fromState", "toState", "needConfirmation", "logAction"
		BeanUtils.copyProperties( src, result, new String[] {
				"name", "defaultName", "confirmation"
		}); // do not copy: "name", "defaultName", "confirmation"

		result.getDefaultName().assign(src.getDefaultName());
		result.getConfirmation().assign(src.getConfirmation());
		result.getName().assign(src.getName());
		return result;
	}


	static WorkflowMoveRequiredField wfmReqFldCopy(WorkflowMoveRequiredField src) 
	{
		if (src == null) return null;
		final WorkflowMoveRequiredField result = (src.getId() != null)
					? (WorkflowMoveRequiredField) WorkflowMoveRequiredField.createFromId( src.getId() )
					: new WorkflowMoveRequiredField();
		BeanUtils.copyProperties( src, result, new String[] { "id" });
		return result;
	}


	/**
	 * ��������� ("��������") ����������� required ��������� �� ����� ��������� � ������.
	 * @param src
	 * @param dst
	 * @return dst
	 */
	static Collection wfmReqFldsCopyCollection(Collection src, Collection dst,
			IChecker checker)
	{
		if (dst != null) {
			dst.clear();
			if (src != null && !src.isEmpty()) {
				for (Iterator iterator = src.iterator(); iterator.hasNext();) {
					WorkflowMoveRequiredField item = (WorkflowMoveRequiredField) iterator.next();
					if (checker == null || checker.isOK(item))
						dst.add( wfmReqFldCopy(item) );
				}
			}
		}
		return dst;
	}

	static Collection wfmReqFldsCopyCollection(Collection src, Collection dst)
	{
		return wfmReqFldsCopyCollection(src, dst, null);
	}


	interface IChecker {
		boolean isOK(WorkflowMoveRequiredField item);
	} 

	/**
	 * ����� � ������ ��������� workflowMoveRequiredFields ������ �� ��� id.
	 */
	WorkflowMoveRequiredField findWFMReqFldByAttrId( final String wfmId, 
			final String attributeCode) {
		if ( 		(attributeCode != null) && !EMPTY_STRING.equals(attributeCode)
				&& 	(wfmId != null) && !EMPTY_STRING.equals(wfmId)
				&& 	(workflowAllMovesRequiredFlds != null) 
				&& 	!workflowAllMovesRequiredFlds.isEmpty()
			) 
		{
			for (Iterator iterator = workflowAllMovesRequiredFlds.iterator(); iterator.hasNext();) {
				final WorkflowMoveRequiredField item = (WorkflowMoveRequiredField) iterator.next();
				if (		attributeCode.equals(item.getAttributeCode())
						&& 	wfmId.equals(item.getWorkflowMoveId().toString()) )
					return item; // FOUND / already exists
			}
		}
		return null; // NOT FOUND
	}


	/**
	 * @return ��������� � ������ ��� ��� ������������� ��������
	 */
	int getSelectedMustBeSetCode() {
		final String curSelected = this.getSelectedRequiredState();
		return (curSelected != null && !EMPTY_STRING.equals(curSelected.trim()))
						? Integer.parseInt(curSelected.trim())
						: WorkflowMoveRequiredField.MUSTBESET_ASTEMPLATE;
	}


	TemplateBlock findTemplateBlockById(String idBlock) {
		// final String idBlock = getSelectedTemplateBlockId(); 
		if ( (idBlock != null) && !EMPTY_STRING.equals(idBlock)
				&& 	(getBlocks() != null)
			) 
		{
			for (Iterator iterator = getBlocks().iterator(); iterator.hasNext();) {
				final TemplateBlock block = (TemplateBlock) iterator.next();
				if (idBlock.equals(block.getId().getId())) 
					return block; // FOUND
			}
		}
		return null; // NOT FOUND
	}


	// ��� jsp-��������
	public Collection getSelectedTemplateBlockAttributes() {
		final TemplateBlock block 
			= findTemplateBlockById(getSelectedTemplateBlockId());
		return (block!= null && block.getAttributes() != null)
					? block.getAttributes()
					: Collections.emptyList();
	}


	/**
	 * ����� ������� ������ workflowMoves �� id.
	 * @param wfmId: ������� �������� id ��������.
	 * @return ��������� �������� ��� null.
	 */
	private WorkflowMove findWFMById(String swfmId) 
	{
		if (	(swfmId != null)
				&& (getWorkflowMoves() != null) && !getWorkflowMoves().isEmpty()) 
		{
			for (Iterator iterator = getWorkflowMoves().iterator(); iterator.hasNext();) {
				final WorkflowMove workflowMove = (WorkflowMove) iterator.next();
				//if ( wfmId.equals(workflowMove.getId().getId()))
				Long id = (Long) workflowMove.getId().getId();
				if ( swfmId.equals( String.valueOf(id) ))
					return workflowMove; // FOUND
			}
		}
		return null; // NOT FOUND
	}

	private WorkflowMove findWFMById(Long wfmId)
	{
		return (wfmId == null) ? null : findWFMById( String.valueOf(wfmId));
	}


	public boolean isWorkflowMovesRequiredFieldValidForRemoveFromTable(){
		return 		(getRemoveWorkflowMoveId() != null) 
						&& 	!EMPTY_STRING.equals(getRemoveWorkflowMoveId()) 
				&& 	(getRemoveTemplateBlockId() != null) 
						&& 	!EMPTY_STRING.equals(getRemoveTemplateBlockId()) 
				&& 	(getRemoveAttributeId() != null) 
						&& 	!EMPTY_STRING.equals(getRemoveAttributeId())
			;
	}


	public void removeWorkflowMovesRequiredFieldFromTable() // throws DataException 
	{
		String wfmId = null;
		String attrId = null;
		if (isWorkflowMovesRequiredFieldValidForRemoveFromTable()) {
			wfmId = getRemoveWorkflowMoveId();
			attrId = getRemoveAttributeId();
		} else {
			wfmId = getSelectedWorkflowMoveId();
			attrId = getSelectedAttributeId();
		}

		final WorkflowMoveRequiredField wfmRF = findWFMReqFldByAttrId( wfmId, attrId);
		if (wfmRF != null) 
			workflowAllMovesRequiredFlds.remove(wfmRF);
	}


	public void resetRemove(){
		removeAttributeId=null;
		removeTemplateBlockId=null;
		removeWorkflowMoveId=null;
	}


	public String getRemoveWorkflowMoveId() {
		return removeWorkflowMoveId;
	}


	public void setRemoveWorkflowMoveId(String removeWorkflowMoveId) {
		this.removeWorkflowMoveId = removeWorkflowMoveId;
	}


	public String getRemoveTemplateBlockId() {
		return removeTemplateBlockId;
	}


	public void setRemoveTemplateBlockId(String removeTemplateBlockId) {
		this.removeTemplateBlockId = removeTemplateBlockId;
	}


	public String getRemoveAttributeId() {
		return removeAttributeId;
	}


	public void setRemoveAttributeId(String removeAttributeId) {
		this.removeAttributeId = removeAttributeId;
	}


	public boolean isShowWorkflowMovesDropDown() {
		return true; // (selectedWorkflowMoveId != null);
	}

	public boolean isShowTemplateBlocksDropDown() {
		return true; // (selectedTemplateBlockId != null);
	}

	public boolean isShowAttributeDropDown() {
		return (selectedWorkflowMoveId != null) 
					&& !EMPTY_STRING.equals(selectedWorkflowMoveId) 
			&& (selectedTemplateBlockId != null) 
					&& !EMPTY_STRING.equals(selectedTemplateBlockId);
	}

	public boolean isShowRequiredRadioItems() {
		return isShowAttributeDropDown() 
			&& (selectedAttributeId != null) 
				&& !EMPTY_STRING.equals(selectedAttributeId.trim());
	}
	
	public boolean isSelectedExist() {
		return null != findWFMReqFldByAttrId(
							getSelectedWorkflowMoveId(), 
							getSelectedAttributeId());
	}

	public Collection getBlocks() {
		return blocksConst;
	}

	public void setBlocks(Collection blocks) {
		this.blocksConst = blocks;
	}

	public String getSelectedTemplateBlockId() {
		return selectedTemplateBlockId;
	}

	public void setSelectedTemplateBlockId(String selectedTemplateBlockId) {
		this.selectedTemplateBlockId = selectedTemplateBlockId;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Collection getWorkflowMoves() {
		return workflowMovesConst;
	}


	public void setWorkflowMoves(Collection workflowMoves) {
		// this.workflowMovesConst = workflowMoves;
		if (workflowMoves == null) {
			this.workflowMovesConst = null;
		} else {
			// ����������� � ����������� ������
			this.workflowMovesConst = new ArrayList(workflowMoves.size());
			for (Iterator iter = workflowMoves.iterator(); iter.hasNext();) {
				final WorkflowMove item = (WorkflowMove) iter.next();
				if (item != null) {
					final WorkflowMove wfm = wfmCopy(item); // new WorkflowMove();
					this.workflowMovesConst.add(wfm);

					// ��������: id + �������
					final LocalizedString theName =  wfm.getName();
					/*
					theName.setValueRu( wfm.getId().getId().toString() + " " 
							+ (theName.getValueRu() != null ? theName.getValueRu() : "") 
							);
					 */
					final String sCaption = 
						wfm.getId().getId().toString() + " " + wfm.getMoveName();
					theName.setValueRu( sCaption);
					theName.setValueEn( sCaption);
				}
			}
		}
	}


	/**
	 * public void setTemplateAttributes(Collection templateAttributes) {
	 * this.templateAttributes = templateAttributes; }
	 */
	
	public Collection getRequiredStates()
	{
		return requiredStates;
	}

	public Collection getWorkflowMoveRequiredFields() {
		return 
			wfmReqFldsCopyCollection( 
					workflowAllMovesRequiredFlds, 
					new ArrayList(),
					new IChecker() {
						// ���������� ���, ������� ����������� � ��� � �������
						public boolean isOK(WorkflowMoveRequiredField item) {
							return (item != null) 
								&& (item.getMustBeSetCode() != WorkflowMoveRequiredField.MUSTBESET_ASTEMPLATE);
						}
						
					}
			);
	}

	public void setWorkflowMoveRequiredFields(Collection value) {
		// this.workflowMoveRequiredFields = value;
		wfmReqFldsCopyCollection( value, this.workflowAllMovesRequiredFlds);
	}

	public String getSelectedWorkflowMoveId() {
		return selectedWorkflowMoveId;
	}

	public void setSelectedWorkflowMoveId(String selectedWorkflowMoveId) {
		this.selectedWorkflowMoveId = selectedWorkflowMoveId;
	}

	public String getSelectedAttributeId() {
		return selectedAttributeId;
	}

	public void setSelectedAttributeId(String selectedAttributeId) {
		this.selectedAttributeId = selectedAttributeId;
	}


	/**
	 * @return the selectedAction
	 */
	public String getSelectedAction() {
		return this.selectedAction;
	}


	/**
	 * @param action the selectedAction to set
	 */
	public void setSelectedAction(String action) {
		this.selectedAction = action;
	}


	/**
	 * @return the selectedRequiredState
	 */
	public String getSelectedRequiredState() {
		return (this.selectedRequiredState != null) 
					? this.selectedRequiredState 
					: ""; // String.valueOf(WorkflowMoveRequiredField.MUSTBESET_ASTEMPLATE);
	}

	/**
	 * @param value the selectedRequiredState to set
	 */
	public void setSelectedRequiredState(String value) {
		this.selectedRequiredState = value;
	}


	public String getTemplateApplyClose() {
		return templateApplyClose;
	}

	
	public void setTemplateApplyClose(String templateApplyClose) {
		this.templateApplyClose = templateApplyClose;
	}


	public Collection getShowWorkflowMovesRequiredFields() {
		return showWorkflowMovesRequiredFields;
	}


	private WorkflowMoveRequiredField getOrCreateWorkflowMoveRequiredField(
					final String wfmId,
					final String attributeCode
			) throws DataException 
	{
		WorkflowMoveRequiredField wfmRF = findWFMReqFldByAttrId( wfmId, attributeCode);

		final int needState = this.getSelectedMustBeSetCode();
		if (wfmRF == null) { // create new one ...
			// if (needState == WorkflowMoveRequiredField.MUSTBESET_ASTEMPLATE) return null;
			wfmRF = new WorkflowMoveRequiredField();
			wfmRF.setWorkflowMoveId(Long.valueOf(wfmId.toString()));
			wfmRF.setAttributeCode(attributeCode);
			// w.setRequired(false);
			wfmRF.setMustBeSetCode( needState);
			workflowAllMovesRequiredFlds.add(wfmRF);
		} else { // ���� ��� ����������
			if (!EMPTY_STRING.equals(this.getSelectedRequiredState())) {
				// ���� ������ �������� ���������...
				wfmRF.setMustBeSetCode( needState);
				/*
				if (needState != WorkflowMoveRequiredField.MUSTBESET_ASTEMPLATE) 
					wfmRF.setMustBeSetCode( needState); // ������ ������ ���������... 
				else { 
					// (?!) ��������� ��� � ������� �������� �������� �� ������  
					// ���������� workflowrequired...
					workflowAllMovesRequiredFlds.remove(wfmRF);
					return null;
				}
				 */
			}
		}
		return wfmRF;
	}


	/**
	 * ��������� �������� ������, ���������� ����.
	 * @throws DataException 
	 */
	private void doAddWFMFld() throws DataException
	{
		if (selectedRequiredState == null) return;
		getOrCreateWorkflowMoveRequiredField( 
				getSelectedWorkflowMoveId(), 
				getSelectedAttributeId() 
			);
		selectedRequiredState = null;
	}


	private void doRemoveWFMFld()
	{
		removeWorkflowMovesRequiredFieldFromTable();
		resetRemove();
	}


	/**
	 * ��������� "����������" �������� - ����������/�������� ����
	 * @throws DataException 
	 */
	void processAction() throws DataException
	{
		if (selectedAction == null) return;
		if ( ACTION_ADD_ATTR.equals(selectedAction.trim()) ) {
			doAddWFMFld();
		} else if ( ACTION_REM_ATTR.equals(selectedAction.trim()) ) {
			doRemoveWFMFld();
		} 
		selectedAction = null;
	}

	public void initShowWorkflowMovesRequiredFields() // throws DataException 
	{
		showWorkflowMovesRequiredFields.clear();

		// ��������� �������� ��� ������������...
		final String wfmNowId = getSelectedWorkflowMoveId();
		final WorkflowMove wfmNow = findWFMById( wfmNowId);

		// ��������� �������� �������� � ������ ������ ��� � ��������� ����...
		final WorkflowMoveRequiredField wfmRF= findWFMReqFldByAttrId( wfmNowId, getSelectedAttributeId());
		if (wfmRF != null)
			this.selectedRequiredState = String.valueOf(wfmRF.getMustBeSetCode());

		// ��������� ������ ���������...
		final TemplateBlock tmplNow = findTemplateBlockById( getSelectedTemplateBlockId());
		final HashSet /*<Long>*/ tmplAttributes = new HashSet();
		if (tmplNow != null && (tmplNow.getAttributes() != null)) {
			for (Iterator iterator = tmplNow.getAttributes().iterator(); iterator.hasNext();) 
			{
				final Attribute attr = (Attribute) iterator.next(); 
				tmplAttributes.add( /*(String)*/ attr.getId().getId() ); 
			}
		}

		if (workflowAllMovesRequiredFlds == null) return;

		// ���������� ������...
		/** ���� ��� �������� �������� ������, �������� �� ���������:
		 * 		1) ������� WFM;
		 * 		2) ���� �� �������;
		 * ������ �� ��� �������� ��� ������ ���������� �� �������;
		 * ���� � �����-���� �������� �� ������� ������, �� ��� ��������������
		 * ����� ��� ���������� ����������� �� ������ ������� - �.�. ��������
		 * "������� ���".
		 */
		for (Iterator iterator = workflowAllMovesRequiredFlds.iterator(); iterator.hasNext();) {
			final WorkflowMoveRequiredField w = (WorkflowMoveRequiredField) iterator.next();

			if (wfmNow != null && !wfmNowId.equals(String.valueOf(w.getWorkflowMoveId())) )
				continue; // ���������� �.�. ����� ������ �������

			if (tmplNow != null && !tmplAttributes.contains(w.getAttributeCode()) ) // getTemplateAttributeId()
				continue; // ���������� �.�. ��� � ������ �������

			// if (w.isRequired())
			{
				final ShowWorkflowMoveRequiredField createdReqFld = 
					createShowWorkflowMoveRequiredField(w);
				showWorkflowMovesRequiredFields.add(createdReqFld);
			}
		}
	}


	private ShowWorkflowMoveRequiredField createShowWorkflowMoveRequiredField(
			final WorkflowMoveRequiredField wfmRF) 
	{
		if (wfmRF == null) return null;

		final ShowWorkflowMoveRequiredField result= new ShowWorkflowMoveRequiredField();

		result.setWorkflowMove(findWFMById(wfmRF.getWorkflowMoveId()) );
		result.setWorkflowMoveRequiredField(wfmRF);

		if (getBlocks() != null) {
			for (Iterator iterBlock = getBlocks().iterator(); iterBlock.hasNext();) {
				final TemplateBlock	templateBlock = (TemplateBlock) iterBlock.next();
				final Collection attributes = templateBlock.getAttributes();
				if (attributes == null) continue;

				for (Iterator itemAttr = attributes.iterator(); itemAttr.hasNext();) {
					final Attribute attribute = (Attribute) itemAttr.next();
					if(attribute.getId().getId().equals(wfmRF.getAttributeCode())){
						result.setTemplateBlock(templateBlock);
						result.setAttribute(attribute);
						return result;
					}
				}
			}
		}
		return result;
	}


	public static class ItemTag implements Serializable {
		private static final long serialVersionUID = 1L;
		
		private final String tag;
		private final int id;

		/**
		 * @param id
		 * @param tag
		 */
		public ItemTag( int id, String tag) {
			super();
			this.tag = tag;
			this.id = id;
		}

		public String getTag() {
			return this.tag;
		}

		public int getId() {
			return this.id;
		}
		
	}


	/**
	 * ��� ����������� ��������� ���������� ����.
	 * @author RAbdullin
	 *
	 */
	public class ShowWorkflowMoveRequiredField implements Serializable {

		private static final long serialVersionUID = -1274845139525479145L;
		
		private WorkflowMoveRequiredField workflowMoveRequiredField;
		private WorkflowMove workflowMove;
		private TemplateBlock templateBlock;
		private Attribute attribute;

		public WorkflowMoveRequiredField getWorkflowMoveRequiredField() {
			return workflowMoveRequiredField;
		}

		public void setWorkflowMoveRequiredField(WorkflowMoveRequiredField workflowMoveRequiredField) {
			this.workflowMoveRequiredField = workflowMoveRequiredField;
		}

		public WorkflowMove getWorkflowMove() {
			return workflowMove;
		}

		public void setWorkflowMove(WorkflowMove workflowMove) {
			this.workflowMove = workflowMove;
		}

		public TemplateBlock getTemplateBlock() {
			return templateBlock;
		}

		public void setTemplateBlock(TemplateBlock templateBlock) {
			this.templateBlock = templateBlock;
		}

		public Attribute getAttribute() {
			return attribute;
		}

		public void setAttribute(Attribute attribute) {
			this.attribute = attribute;
		}

		public Long getMustBeSet() {
			final long i = 
				(this.workflowMoveRequiredField != null)
					? this.workflowMoveRequiredField.getMustBeSetCode()
					: WorkflowMoveRequiredField.MUSTBESET_ASTEMPLATE;
			return new Long(i);
		}

		public void setMustBeSet(Long value) throws DataException 
		{
			if (this.workflowMoveRequiredField != null && value != null)
				this.workflowMoveRequiredField.setMustBeSetCode(value.intValue());
		}
		
		public boolean isSet_ASSIGNED()
		{
			return getMustBeSet().longValue() == WorkflowMoveRequiredField.MUSTBESET_ASSIGNED;
		}

		public boolean isSet_BLANK()
		{
			return getMustBeSet().longValue() == WorkflowMoveRequiredField.MUSTBESET_BLANK;
		}
		
		public String getWfmCaption()
		{
			if (this.workflowMove != null) 
				return  this.workflowMove.getMoveName();
			
			return ( this.workflowMoveRequiredField != null &&
					 this.workflowMoveRequiredField.getWorkflowMoveId() != null
					)
					? String.valueOf(this.workflowMoveRequiredField.getWorkflowMoveId())
					: "";
		}

	}
}
