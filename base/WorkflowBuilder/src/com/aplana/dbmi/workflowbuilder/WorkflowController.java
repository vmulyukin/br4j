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
package com.aplana.dbmi.workflowbuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletRequest;

import org.springframework.validation.Errors;

import com.aplana.dbmi.action.LockObject;
import com.aplana.dbmi.action.UnlockObject;
import com.aplana.dbmi.model.CardState;
import com.aplana.dbmi.model.DataObject;
import com.aplana.dbmi.model.LockableObject;
import com.aplana.dbmi.model.LogAction;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Workflow;
import com.aplana.dbmi.model.WorkflowMove;
import com.aplana.dbmi.service.AsyncDataServiceBean;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.ServiceException;
import com.aplana.dbmi.service.AsyncDataServiceBean.ExecuteOption;

public class WorkflowController extends LockableObjectListController {
	public static final long NOT_SELECTED = -1;
	public static final String ACTION_DEACTIVATE = "makeInactive";
	public static final String ACTION_ADD_WORKFLOW_MOVE = "addMove";
	public static final String ACTION_EDIT_WORKFLOW_MOVE = "editMove";
	public static final String ACTION_DELETE_WORKFLOW_MOVE = "deleteMove";	
	public static final String ACTION_SUBMIT_WORKFLOW_MOVE_OK = "okMove";
	public static final String ACTION_SUBMIT_WORKFLOW_MOVE_CANCEL = "cancelMove";
	public static final String ACTION_ADD_CARD_STATE = "addCardState";
	public static final String ACTION_ADD_LOG_ACTION = "addLogAction";
	public static final String ACTION_SUBMIT_ADDED_OBJECT = "submitAddedObject";
	public static final String ACTION_CANCEL_ADDED_OBJECT = "cancelAddedObject";
	public static final String ACTION_SAVE = "save";
	
	public static final String ADDED_OBJECT_INITIAL_STATE = "initialState";
	public static final String ADDED_OBJECT_FROM_STATE = "fromState";
	public static final String ADDED_OBJECT_TO_STATE = "toState";
	public static final String ADDED_OBJECT_LOG_ACTION = "logAction";
	
	protected Map referenceData(PortletRequest request, Object command,	Errors errors) throws Exception {
		WorkflowCommandBean bean = (WorkflowCommandBean)command;
		Workflow w = (Workflow)bean.getSelectedObject();
		Map result = super.referenceData(request, command, errors);
		if (w != null) {
			result.put("cardStates", bean.getCardStates().values());
			
			List moves = new ArrayList(bean.getMoves().size());
			Map moveMap = bean.getMoves();
			Iterator i = moveMap.keySet().iterator();			 
			while (i.hasNext()) {
				String key = (String)i.next();
				WorkflowMove wm = (WorkflowMove)moveMap.get(key);
				Map item = new HashMap(4);
				item.put("key", key);				
				item.put("name", wm.getMoveName());
				item.put("from", bean.getCardStates().get(wm.getFromState().getId()));
				item.put("to", bean.getCardStates().get(wm.getToState().getId()));
				moves.add(item);
			}
			result.put("moves", moves);
			
			{
				//������ ��
				Map DSModes = new HashMap(3);
				DSModes.put(new Integer(0), getMessage("option.DSNone", request));
				DSModes.put(new Integer(1), getMessage("option.DSAttributesOnly", request));
				DSModes.put(new Integer(2), getMessage("option.DSEverything", request));
				result.put("dsmodes", DSModes);
			}		
		}
		return result;
	}
	
	protected LockableObject newObject() throws InstantiationException,	IllegalAccessException {
		Workflow w = new Workflow();
		w.setMoves(new ArrayList());
		return w;
	}

	protected void initEdit(LockableObjectListCommandBean command) throws Exception {
		Workflow w = (Workflow)command.getSelectedObject();
		WorkflowCommandBean bean = (WorkflowCommandBean)command;
		
		refreshCardStates(bean);
		refreshLogActions(bean);
		
		Map moves = new HashMap(w.getMoves().size());
		Iterator i = w.getMoves().iterator();
		while (i.hasNext()) {
			WorkflowMove wm = (WorkflowMove)i.next();
			String key = getMoveKey(wm.getFromState(), wm.getToState());
			moves.put(key, wm);
		}
		bean.setMoves(moves);
		bean.setSelectedMove(null);
		bean.setSelectedMoveKey(null);
	}
	
	private void refreshCardStates(WorkflowCommandBean bean) throws DataException, ServiceException {
		List cardStates = (List)bean.getDataService().listAll(CardState.class);
		Collections.sort(cardStates, new Comparator() {
			public int compare(Object obj1, Object obj2) {
				CardState cs1 = (CardState)obj1;
				CardState cs2 = (CardState)obj2;
				return cs1.getName().compareToIgnoreCase(cs2.getName());
			}}
		);
		
		Map map = new LinkedHashMap(cardStates.size());
		DataObjectUtils.fillIdMapFromCollection(map, cardStates);
		bean.setCardStates(map);
	}
	
	private void refreshLogActions(WorkflowCommandBean bean) throws DataException, ServiceException {
		List logActions = (List)bean.getDataService().listAll(LogAction.class);
		Collections.sort(logActions, new Comparator() {
			public int compare(Object obj1, Object obj2) {
				LogAction la1 = (LogAction)obj1;
				LogAction la2 = (LogAction)obj2;
				return la1.getName().compareToIgnoreCase(la2.getName()); 
			}
		});
		bean.setLogActions(logActions);
	}

	protected boolean beforeSave(LockableObjectListCommandBean command, ActionRequest request) throws Exception {
		WorkflowCommandBean bean = (WorkflowCommandBean)command;
		Workflow w = (Workflow)bean.getSelectedObject();
		w.setMoves(new ArrayList(bean.getMoves().values()));
		if (w.getInitialState() == null) {
			// ��� ������, �� ��� ����� �������� ��� ���������� ��������
			// ������� ����� ������� true
			return true;
		}
		// ��������� ���� ��������� �� ���������
		Set available = new HashSet();
		Set all = new HashSet();			
		Iterator i = w.getMoves().iterator();
		while (i.hasNext()) {
			WorkflowMove move = (WorkflowMove)i.next();
			all.add(move.getFromState().getId());
			all.add(move.getToState().getId());
		}
		all.remove(w.getInitialState().getId());
		available.add(w.getInitialState().getId());
		boolean found;
		do {
			found = false;
			i = w.getMoves().iterator();
			while (i.hasNext()) {
				WorkflowMove move = (WorkflowMove)i.next();
				Long toStateId = (Long)move.getToState().getId();
				if (available.contains(move.getFromState().getId()) && !available.contains(toStateId)) {
					available.add(move.getToState().getId());
					all.remove(toStateId);
					found = true;
				}
			}
		} while (found);
		if (!all.isEmpty()) {
			StringBuffer st = new StringBuffer();
			i = all.iterator();
			while (i.hasNext()) {
				CardState cs = (CardState)bean.getCardStates().get(i.next());
				st.append('\'').append(cs.getName()).append('\'');
				if (i.hasNext()) {
					st.append(", ");
				}
			}
			bean.setMessage(getMessage("msg.statesNotReachable", new Object[] {st.toString()}, request));
			return false;
		} else {
			return true;
		}
	}

	protected void processFormChangeAction(String action, ActionRequest request, ActionResponse response,
			LockableObjectListCommandBean command) throws Exception {
		WorkflowCommandBean bean = (WorkflowCommandBean)command;
		if (ACTION_DEACTIVATE.equals(action)) {
			if (bean.isEditAccessExists()){
				AsyncDataServiceBean dataService = command.getDataService();
				Workflow w = (Workflow)fetchAndLock(request.getParameter(PARAM_OBJECT_ID), dataService);
				w.setActive(false);
				dataService.saveObject(w, ExecuteOption.SYNC);
				// ������ ����� � ������, ������� ��������� ������ �������
				w.setMoves(null);
				replaceObjectInList(w, command);
				dataService.doAction(new UnlockObject(w));
			}
			else
				throw new DataException("admin.edit.access.error");
		} else if (ACTION_ADD_WORKFLOW_MOVE.equals(action)) {
			if (bean.isEditAccessExists()){
				WorkflowMove move = new WorkflowMove();
				bean.setSelectedMove(move);
				bean.setSelectedMoveKey(null);
			}
			else
				throw new DataException("admin.edit.access.error");
		} else if (ACTION_EDIT_WORKFLOW_MOVE.equals(action)) {
			if (bean.isEditAccessExists()){
				String key = request.getParameter(PARAM_OBJECT_ID);
				WorkflowMove move = (WorkflowMove)bean.getMoves().get(key);
				WorkflowMove newMove;
				if (move.getId() == null) {
					newMove = new WorkflowMove();
				} else {
					newMove = (WorkflowMove)DataObject.createFromId(move.getId());
				}
				newMove.getName().assign(move.getName());
				newMove.getConfirmation().assign(move.getConfirmation());
				newMove.setFromState(move.getFromState());
				newMove.setToState(move.getToState());
				newMove.setNeedConfirmation(move.isNeedConfirmation());
				newMove.setCloseCard(move.isCloseCard());
				newMove.setLogAction(move.getLogAction());
				
				newMove.setApplyDigitalSignatureOnMove(move.getApplyDigitalSignatureOnMove());
				bean.setSelectedMove(newMove);
				bean.setSelectedMoveKey(key);
			}
			else
				throw new DataException("admin.edit.access.error");
		} else if (ACTION_DELETE_WORKFLOW_MOVE.equals(action)) {
			if (bean.isEditAccessExists()){
				String key = request.getParameter(PARAM_OBJECT_ID);
				bean.getMoves().remove(key);
			}
			else
				throw new DataException("admin.edit.access.error");
		} else if (ACTION_SUBMIT_WORKFLOW_MOVE_OK.equals(action)) {
			if (bean.isEditAccessExists()){
				processWorkflowMoveSubmission(bean, request);
			}
			else
				throw new DataException("admin.edit.access.error");
		} else if (ACTION_SUBMIT_WORKFLOW_MOVE_CANCEL.equals(action)) {
			bean.setSelectedMove(null);
			bean.setSelectedMoveKey(null);
		} else if (ACTION_ADD_CARD_STATE.equals(action)) {
			if (bean.isEditAccessExists()){
				bean.setAddedObjectKey(request.getParameter(PARAM_OBJECT_ID));
				bean.setAddedObject(new CardState());
			}
			else
				throw new DataException("admin.edit.access.error");
		} else if (ACTION_ADD_LOG_ACTION.equals(action)) {
			if (bean.isEditAccessExists()){
				bean.setAddedObjectKey(request.getParameter(PARAM_OBJECT_ID));
				bean.setAddedObject(new LogAction());
			}
			else
				throw new DataException("admin.edit.access.error");
		} else if (ACTION_SUBMIT_ADDED_OBJECT.equals(action)) {
			if (bean.isEditAccessExists()){
				processAddedObjectSubmission(bean);
			}
			else
				throw new DataException("admin.edit.access.error");
		} else if (ACTION_CANCEL_ADDED_OBJECT.equals(action)) {
			if (bean.isEditAccessExists()){
				bean.setAddedObject(null);
				bean.setAddedObjectKey(null);
			}
			else
				throw new DataException("admin.edit.access.error");
		} else if (ACTION_SAVE.equals(action)) {
			if (bean.isEditAccessExists()){
				// ���������� ��� �������� �����
				Workflow object = (Workflow)bean.getSelectedObject();
				if (!beforeSave(bean, request)) {
					return;
				}
				ObjectId objectId = bean.getDataService().saveObject(object, ExecuteOption.SYNC);
				if (object.getId() == null) {
					object = (Workflow)bean.getDataService().getById(objectId);
					// ������ ����� � ������, ������� ��������� ������ �������
					object.setMoves(null);
					bean.getDataService().doAction(new LockObject(object));
					bean.getObjects().add(object);
				} else {
					// ������ ����� � ������, ������� ��������� ������ �������
					object.setMoves(null);				
					replaceObjectInList(object, bean);
				}
				// ���������� ����� ����� �� ��, ����� � ������ � � ����� ���� ������ ����������
				object = (Workflow)bean.getDataService().getById(objectId);
				bean.setSelectedObject(object);
				initEdit(bean);
				bean.setMessage(getMessage("msg.SaveSuccess", request));
			}
			else
				throw new DataException("admin.edit.access.error");
		} else {
			super.processFormChangeAction(action, request, response, command);
		}
	}
	
	private void processWorkflowMoveSubmission(WorkflowCommandBean bean, PortletRequest request) {
		WorkflowMove move = bean.getSelectedMove();
		String oldKey = bean.getSelectedMoveKey();
		if (move.getFromState() == null) {
			bean.setMessage(getMessage("msg.emptyFromState", request));
			return;				
		}
		if (move.getToState() == null) {
			bean.setMessage(getMessage("msg.emptyToState", request));
			return;
		}
		if (move.getLogAction() == null) {
			bean.setMessage(getMessage("msg.emptyLogAction", request));
			return;	
		}
		
		CardState cs = (CardState)bean.getCardStates().get(move.getToState().getId());
		move.getDefaultName().assign(cs.getDefaultMoveName());
		if (move.getName().hasEmptyValues() && move.getDefaultName().hasEmptyValues()) {
			bean.setMessage(getMessage("msg.emptyMoveName", new Object[] {cs.getName()}, request));
			return;
		}

		String newKey = getMoveKey(move.getFromState(), move.getToState());			
		if (!newKey.equals(oldKey) && bean.getMoves().containsKey(newKey)) {
			bean.setMessage(getMessageSource().getMessage("msg.moveAlreadyExists", null, request.getLocale()));
			return;
		}
		
		if (move.getFromState().equals(move.getToState())) {
			bean.setMessage(getMessage("msg.sameFromAndTo", request));
		}
		
		if (oldKey == null) {
			bean.getMoves().put(newKey, move);
		} else {
			bean.getMoves().remove(oldKey);
			bean.getMoves().put(newKey, move);
		}
		bean.setSelectedMove(null);
		bean.setSelectedMoveKey(null);
	}
	
	private void processAddedObjectSubmission(WorkflowCommandBean bean) throws Exception {
		ObjectId addedObjectId;
		try {
			addedObjectId = bean.getDataService().saveObject(bean.getAddedObject(), ExecuteOption.SYNC);
		} catch (Exception e) {
			bean.setMessage(e.getMessage());
			return;
		}
		String addedObjectKey = bean.getAddedObjectKey();
		if (ADDED_OBJECT_INITIAL_STATE.equals(addedObjectKey)) {
			((Workflow)bean.getSelectedObject()).setInitialState(addedObjectId);
		} else if (ADDED_OBJECT_FROM_STATE.equals(addedObjectKey)) {
			bean.getSelectedMove().setFromState(addedObjectId);
		} else if (ADDED_OBJECT_TO_STATE.equals(addedObjectKey)) {
			bean.getSelectedMove().setToState(addedObjectId);
		} else if (ADDED_OBJECT_LOG_ACTION.equals(addedObjectKey)) {
			bean.getSelectedMove().setLogAction(addedObjectId);
		}
		if (CardState.class.equals(addedObjectId.getType())) {
			refreshCardStates(bean);
		} else {
			refreshLogActions(bean);
		}
		bean.setAddedObject(null);
		bean.setAddedObjectKey(null);		
	}
	
	private String getMoveKey(ObjectId fromStateId, ObjectId toStateId) {
		return fromStateId.getId() + ":" + toStateId.getId();
	}
}