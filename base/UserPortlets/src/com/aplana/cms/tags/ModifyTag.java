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
package com.aplana.cms.tags;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aplana.cms.*;
import com.aplana.cms.cache.CounterCache;
import com.aplana.dbmi.action.*;
import com.aplana.dbmi.card.util.SearchUtils;
import com.aplana.dbmi.delegate.DelegateHelper;
import com.aplana.dbmi.model.*;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.service.AsyncDataServiceBean;
import com.aplana.dbmi.service.AsyncDataServiceBean.ExecuteOption;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.ServiceException;

public class ModifyTag implements TagProcessor, FormProcessor
{
	public static final String ATTR_CREATE = "create";
	public static final String ATTR_PROCEED = "proceed";
	public static final String ATTR_SUCCESS = "success";
	public static final String ATTR_FAILURE = "failure";
	public static final String ATTR_HTMLID = "htmlid";
	public static final String ATTR_EXECUTE_MODE = "executemode"; 
	
	public static final String PARAM_SESSION_BEAN = "batchSessionBean";
	public static final String PARAM_CARD = "card";
	public static final String PARAM_TEMPLATE = "template";
	public static final String PARAM_PROCEED = "move";
	public static final String PARAM_DS = "moveds";
	public static final String PARAM_SUCCESS_URL = "success";
	public static final String PARAM_FAILURE_URL = "failure";
	public static final String PARAM_ACTION = "actionMode";
	public static final String PARAM_EXECUTE_MODE = "executemode";
	public static final String EXECUTE_MODE_ASYNC = "async";
	public static final String EXECUTE_MODE_SYNC = "sync";
	public static final String BATCH_ASYNC_ACTION = "batchAsync";
	
	// ��� �������. ��� ����������\�������� �� ������� �������� ��� ������.
	public static final String ACTION_ACQUAINTANCE = "acquaintance";
	
	public static final String VAR_FORM = "form";
	public static final String FORM_NAME = "modify";

	// (YNikitin, 2011/02/24) ����� ����������� ��������, ����������� ��� ����������� � ���������� ������� ��������
	private static final String FORM_MODE = "mode";

	private static final ObjectId BOSS_CONTROL_INSPECTOR 	= ObjectId.predefined(PersonAttribute.class, "jbr.boss.control.inspector");
	private static final ObjectId BOSS_CONTROL_DOCUMENT 	= ObjectId.predefined(CardLinkAttribute.class, "jbr.boss.control.document");
	private static final ObjectId ATTR_FAVORITE_PERSON      = ObjectId.predefined(PersonAttribute.class, "jbr.boss.favorite.person");
	private static final ObjectId ATTR_FAVORITE_DOCUMENT    = ObjectId.predefined(CardLinkAttribute.class, "jbr.boss.favorite.document");

    private static final ObjectId CHECK_TEMPLATE			= ObjectId.predefined(Template.class, "jbr.boss.control");
    private static final ObjectId TEMPLATE_FAVORITE			= ObjectId.predefined(Template.class, "jbr.boss.favorite");

    private static final ObjectId CARD_STATE_PUBLISHED		= ObjectId.predefined(CardState.class, "jbr.reservationRequest.published");

    private static final ObjectId CONTROL_DELETE_WFM_ID 	= ObjectId.predefined(WorkflowMove.class, "static.toDoublets");
    private static final ObjectId WFM_TO_DUPLICATES 	    = ObjectId.predefined(WorkflowMove.class, "toDuplicates");

    private static final ObjectId WFM_TO_TRASH1 	    = new ObjectId(WorkflowMove.class, 219974932);
    private static final ObjectId WFM_TO_TRASH2 	    = new ObjectId(WorkflowMove.class, 219974933);
    private static final ObjectId DELEGATION_NOTICE_TEMPLATE = ObjectId.predefined(Template.class, "jbr.delegate_notice");
    private static final ObjectId DELEGATION_USER_FROM = ObjectId.predefined(PersonAttribute.class, "jbr.delegation.from");
    private static final ObjectId DELEGATION_USER_TO = ObjectId.predefined(PersonAttribute.class, "jbr.delegation.to");

    private static final String FOLDER_PERSONAL_CONTROL = "8544";
    private static final String FOLDER_FAVORITES = "8058";
	
    private final static long extraTime = 23*60*60*1000 + 59*60*1000 + 59*1000 + 999;
	// ���������� "��������� �� ������������":
	// ������ "��������"
	private static final ObjectId STATE_DRAFT =
		ObjectId.predefined(CardState.class, "draft");
	private static final ObjectId ATTR_PERSON =
		ObjectId.predefined(PersonAttribute.class, "jbr.information.person");
	// ������� "��������" -> "�� ������������" 
	private static final ObjectId MOVE_SEND =
		ObjectId.predefined(WorkflowMove.class, "jbr.info.send");
	
	protected Log logger = LogFactory.getLog(getClass());
	private String url;
	private ObjectId templateId;
	private ObjectId moveId;
	private Integer moveDsValue;
	private Card card;

	public boolean prepareData(Tag tag, Card item, ContentProducer cms) throws Exception
	{
		url = cms.getResponse().createActionURL(prepareUrlParams(cms));
		if (tag.hasAttribute(ATTR_CREATE)) {
			templateId = cms.findTemplate(tag.getAttribute(ATTR_CREATE), cms.getContentDataServiceFacade().allTemplates());
			if (!cms.getContentDataServiceFacade().canDo(new CreateCard(templateId))) {
				logger.warn("User doesn't have rights for creating card by template " +
						tag.getAttribute(ATTR_CREATE) + "; form skipped");
				return false;
			}
			card = (Card) cms.getContentDataServiceFacade().doAction(new CreateCard(templateId));
			card.setId(0);	// it won't be really written in DB, just to avoid errors in CMS
		} else {
			if (!cms.getContentDataServiceFacade().canChange(item.getId())) {
				logger.warn("User doesn't have rights for modifying card " + item.getId() +
						"; form skipped");
				return false;
			}
			card = item;
		}
		if (tag.hasAttribute(ATTR_PROCEED)){
			WorkflowMove wfm = findMove(tag.getAttribute(ATTR_PROCEED), item, cms);
			moveId = wfm.getId();	//***** item - invalid in case of card creation!
			if(moveId == null){
				moveDsValue = 0;
				logger.warn("Card " + item.getId() + " doesn't have move " + tag.getAttribute(ATTR_PROCEED) +
				"; form skipped");
				return false;
			} else {
				moveDsValue = wfm.getApplyDigitalSignatureOnMove();
			}
		}
		return true;
	}

	/**
	 * Prepares parameters to form action URL.
	 * 
	 * @param cms
	 * @return HashMap
	 */
	protected HashMap<String, String> prepareUrlParams(ContentProducer cms) {
		HashMap<String, String> params = new HashMap<String, String>();
		params.put(ContentViewPortlet.PARAM_FORM, FORM_NAME);
		Card currentContent = cms.getCurrentContent(true);
		if (currentContent != null) {
			params.put(ContentRequest.PARAM_ITEM, currentContent.getId().getId().toString());
		}
		return params;
	}

	public void writeHtml(PrintWriter out, Tag tag, Card item, ContentProducer cms)
	{
		out.write("<form action='" + url + "' method='post'");
		if (tag.hasAttribute(ATTR_HTMLID))
			out.write(" id='" + cms.expandContent(tag.getAttribute(ATTR_HTMLID), item) + "'");
		cms.writeHtmlAttributes(out, tag, item);
		out.write(">");
		
		writeFormContent(out, tag, item, cms);
		
		out.write("</form>");
	}

	protected void writeFormContent(PrintWriter out, Tag tag, Card item, ContentProducer cms) {
		if (tag.hasAttribute(ATTR_CREATE)) {
			out.write("<input type='hidden' name='" + PARAM_TEMPLATE + "' value='" +
					templateId.getId() + "'>");
		} else {
			out.write("<input type='hidden' name='" + PARAM_CARD + "' value='" +
					item.getId().getId() + "'>");
		}
		if (tag.hasAttribute(ATTR_PROCEED)) {
			out.write("<input type='hidden' name='" + PARAM_PROCEED + "' value='" +
					moveId.getId() + "'>");
			out.write("<input type='hidden' name='" + PARAM_DS + "' value='" +
					moveDsValue + "'>");
			if(tag.hasAttribute(ATTR_EXECUTE_MODE)){
				out.write("<input type='hidden' name='" + PARAM_EXECUTE_MODE + "' value='" +
						tag.getAttribute(ATTR_EXECUTE_MODE) + "'>");
				out.write("<input type='hidden' name='batchAsync' value='true'>");
			}
		}
		if (tag.hasAttribute(ATTR_SUCCESS)) {
			out.write("<input type='hidden' name='" + PARAM_SUCCESS_URL + "' value='");
			cms.writeContent(out, tag.getAttribute(ATTR_SUCCESS), item);
			out.write("'/>");
		}
		if (tag.hasAttribute(ATTR_FAILURE)) {
			out.write("<input type='hidden' name='" + PARAM_SUCCESS_URL + "' value='");
			cms.writeContent(out, tag.getAttribute(ATTR_FAILURE), item);
			out.write("'/>");
		}
		cms.writeContent(out, tag.getContent(), card);
	}

    // todo not optimized yet
	public boolean processForm(ProcessRequest request, ProcessResponse response,
			DataServiceBean service)
	{
		// No initialized data available here!
		Card card = null;
		String redirect = null;
		BatchActionsSessionBean sessionBean = null;
		try {
							/* ��� �������� (�������� actionMode(PARAM_ACTION)) ���
							*    "��������� �� ������������"
							*     �������� card - �������� �������� ��� �������� "������������" */
			String form = request.getParameter(PARAM_ACTION);
			if (form != null && form.equals(ACTION_ACQUAINTANCE)) {
				ObjectId cardId = new ObjectId(Card.class, Long
						.parseLong(request.getParameter(PARAM_CARD)));
				card = (Card) service.getById(cardId);		/* ������������ ��������
															*   ( �������� ������
															*     �� �������� "�� ������������"(524))*/

							/* ���������� �������� "��������"(1)->"������������"(67424)
							* ��� �������� "�� ������������" (template_id = 524)*/
				redirect = request.getParameter(PARAM_SUCCESS_URL); // ������������ �� ���������� url
				return sendAcquaintance(card, service);
			}

							/* ���� �� ������ ��� �������� (�������� actionMode (PARAM_ACTION)),
							 *   �� ��������� "�� ������ ��������":*/
			if (request.getParameter(PARAM_TEMPLATE) != null) {
				ObjectId templateId = new ObjectId(Template.class,
						Long.parseLong(request.getParameter(PARAM_TEMPLATE)));
				card = (Card) service.doAction(new CreateCard(templateId));
			} else {
				ObjectId cardId = new ObjectId(Card.class,
						Long.parseLong(request.getParameter(PARAM_CARD)));
				service.doAction(new LockObject(cardId));
				card = (Card) service.getById(cardId);
			}
			
			boolean changed = false;
			for (Enumeration<?> params = request.getParameterNames(); params.hasMoreElements(); ) {
				String param = (String) params.nextElement();
				if (!param.contains("_"))
					continue;
				String tag = param.substring(0, param.indexOf("_"));
				TagProcessor proc = TagFactory.getProcessor(tag);
				if (proc == null || !(proc instanceof FieldProcessor)) {
					logger.error("Unknown parameter: " + tag);
					continue;
				}
				changed = changed |
					((FieldProcessor) proc).processFields(param, card, request, service);
			}

            // (2011/02/22, YNikitin) ��������� �������������� �������� ��� �������� "������ ��������", ����� �� ���������
			// ToDo: ���� �� ��������, ������� � ���������� � ������� ��� �� ���-�� �������� 
			// ����� �������� ��������: add - ������� �������� ������� ��������, remove - �������� �������� ������� �������� (������� � ������ �������)  
			if (request.getParameterMap().containsKey(FORM_MODE)) {
				String mode = request.getParameter(FORM_MODE).toLowerCase();
				if (mode.equals("add")) {
					addToPersonalControl(card, changed, request, service);
				} else if (mode.equals("remove")) {
					card = removeFromPersonalControl(card, request, service);
				} else if (mode.equals("add_to_favorites")) {
					addToFavorites(card, changed, request, service);
				} else if (mode.equals("remove_from_favorites")) {
					card = removeFromFavorites(card, request, service);
				} else {
					card = null; // �������� �� ���� ���������
				}
			} else {
				performRequiredAction(card, changed, request, service);
			}
			redirect = request.getParameter(PARAM_SUCCESS_URL);
/*		} catch (CardVersionException e) {
			logger.error("Error processing form", e);
			request.setSessionAttribute(ContentProducer.SESS_ATTR_ERROR, e.getMessage()+"����������, �������� ������ ���������� � ��������� ����������� ��������.");	//***** local
			redirect = request.getParameter(PARAM_FAILURE_URL);
			return false;*/
		} catch (Exception e) {
			logger.error("Error processing form", e);
			request.setSessionAttribute(ContentProducer.SESS_ATTR_ERROR, e.getMessage());	//***** local
			redirect = request.getParameter(PARAM_FAILURE_URL);
			return false;
		} finally {
			if (null != card && null != card.getId()) {
				/**
				 * ��������� �������� ��� �������� �������, ���� �������� ������� ����������� unlock,
				 * �� ������������. ��� ���� ��������� �������, ���������� ����� modifyTag,
				 * ������ �������. �������� ��� ������
				 */
				sessionBean = (BatchActionsSessionBean)request.getSessionAttribute(PARAM_SESSION_BEAN);
				if (sessionBean == null || sessionBean.unlock)
					try {
						service.doAction(new UnlockObject(card.getId()));
					} catch (Exception e) {
						logger.error("Error unlocking card", e);
					}
			}
			if (redirect != null)
				try {
					response.sendRedirect(redirect);
				} catch (IOException e) {
					logger.error("Can't redirect user to page " + redirect, e);
				}
			/**
			 * ��� �������� �������. ������������ ��������, ������� ����� ���� ������������� 
			 * � �������� � �������� �����.
			 */
			if (sessionBean != null) {
				sessionBean.unlock = true;
				if (sessionBean.lastCard) {
					for (ObjectId obj : sessionBean.unlockCards) {
						try {
							service.doAction(new UnlockObject(obj));
						} catch (Exception e) {
							logger.error("Error unlocking card", e);
						}
					}
					sessionBean.clearUnlockCards();
				}
			}
		}

		return true;
	}

    private void addToPersonalControl(Card card, boolean changed, ProcessRequest request, DataServiceBean service) throws DataException, ServiceException {
        // � ������ ���������� ������� ����� ��������, ���� ��� �� ��������� ������
        final Card existControlCard = checkForExistsPrivateControl(card, service);
        if(null != existControlCard) {
            return;
        }

        saveCard(card, changed, service);

        clearFolderQtyCache(FOLDER_PERSONAL_CONTROL, service);
    }
    
	private void performRequiredAction(Card card, boolean changed, ProcessRequest request, DataServiceBean service) throws DataException, ServiceException {
		saveCard(card, changed, service, ExecuteOption.SYNC);

		if (request.getParameter(PARAM_PROCEED) != null) {
			ObjectId moveId = new ObjectId(WorkflowMove.class, Long.parseLong(request.getParameter(PARAM_PROCEED)));
			ChangeState move = new ChangeState();
			move.setCard(card);
			move.setWorkflowMove((WorkflowMove) DataObject.createFromId(moveId));
			
			if(EXECUTE_MODE_ASYNC.equals(request.getParameter(PARAM_EXECUTE_MODE))){
				String batchParam = request.getParameter(BATCH_ASYNC_ACTION);
				if (batchParam != null && batchParam.contains("true")) {
					//��� ����������, � ������� ������������ ������ ��� �������
					BatchActionsSessionBean sessionBean = (BatchActionsSessionBean)request.getSessionAttribute(PARAM_SESSION_BEAN);
					if (sessionBean == null) {
						sessionBean = new BatchActionsSessionBean();
						request.setSessionAttribute(PARAM_SESSION_BEAN, sessionBean);
					}
					//���� ������ ��� ���������, �� ��������� ��� ������
					if (batchParam.contains("last")) {
						sessionBean.actions.add(move);
						BatchAsyncExecution<ChangeState> action = new BatchAsyncExecution<ChangeState>();
						action.setActions(sessionBean.actions);
						//��� ��������
						ObjectId operationType = null;
						boolean isRassm = false;
						for(Action<?> a : action.getActions()) {
							if(a instanceof ChangeState
									&& ((ChangeState)a).getCard().getTemplate().equals(ObjectId.predefined(Template.class, "jbr.rassm")))
							{
								isRassm = true;
							}
						}
						if(isRassm) {
							//��� �������� ��� �������� �������� ������������ �� ����������
							operationType = ObjectId.predefined(BatchAsyncExecution.class, "jbr.batch_rassm");
							action.setAttrToParent(ObjectId.predefined(BackLinkAttribute.class, "jbr.exam.parent"));
							
							//��� �������� ��� �������� �������� ��������� �� ����������
						} else {
							operationType = ObjectId.predefined(BatchAsyncExecution.class, "jbr.batch_resolutions");
							action.setAttrToParent(ObjectId.predefined(CardLinkAttribute.class, "jbr.main.doc"));
						}
						action.setOperationType(operationType);
						/**
						 * ��� ������ ��������� ��������, �������� � ���� ��� ����,
						 * ����� ������� ���� �������������� ��� ��������� ��������
						 */
						sessionBean.lastCard = true;
						try {
							((AsyncDataServiceBean)service).doAction(action, ExecuteOption.ASYNC);
						} catch(DataException e) {
							throw e;
						} finally {
							sessionBean.clearActions();
						}
					} else {
						/**
						 * �������� �� ��������� ->
						 * 1. ��������� ��� � �������� ����
						 * 2. ��������� �������� � ������ ��������, ������� ���� �������������� ����� ������� ��������� ������
						 * 3. ��������, ��� ��� ������ �� ������� ����������� ������ �������� ���������������� �� ������
						 *    (��� ��������������� �����, ����� ������������ ��������� ������������ �������� � ��� ���������� �������� ����)
						 * 4. ��������, ��� ������ �������� �� ���������, ����� ������� ���� �� ������������� 
						 *    ������������� ������������ �������� �� sessionBean.unlockCards
						 */
						sessionBean.actions.add(move);
						sessionBean.unlockCards.add(card.getId());
						sessionBean.unlock = false;
						sessionBean.lastCard = false;
					}
				} else {
					((AsyncDataServiceBean)service).doAction(move, ExecuteOption.ASYNC);
				}
			} else {
				((AsyncDataServiceBean)service).doAction(move, ExecuteOption.SYNC);
			}
			clearCurrentFolderQtyCache(request, service);

            // ���� ��������� ����������� � �������������, �� ���������� ������� � ���� �������������
            if(Long.valueOf(2290).equals(card.getTemplate().getId()) &&
                    (WFM_TO_TRASH1.equals(moveId)) || WFM_TO_TRASH2.equals(moveId)) {
                Delegation delegation = getDelegation(card);
                DeleteDelegateAction action = new DeleteDelegateAction((Long) delegation.getId().getId());
                service.doAction(action);
            }
		}
	}

	private void saveCard(Card card, boolean changed, DataServiceBean service, ExecuteOption option) throws DataException, ServiceException {
		if (changed) {
			
			//If template of the current card is a delegation notification, then doing the following:
			// - changing delegate person attributes from Person to PersonView
			// - adding an extra time to the delegation end date attribute
			// - creating a new delegation
			
			if(DELEGATION_NOTICE_TEMPLATE.equals(card.getTemplate())) {
					
				if (card.getAttributeById(DELEGATION_USER_FROM).isEmpty()) {
		            // ������� ������������ ��� ���������, ���� �� ������� �����
		            ((PersonAttribute)(card.getAttributeById(DELEGATION_USER_FROM))).setPerson(service.getPerson());
		        }
				DateAttribute endDateAttribute =
				                (DateAttribute) card.getAttributeById(ObjectId.predefined(DateAttribute.class, "jbr.delegation.date.end"));				   
				endDateAttribute.setValue(new Date(endDateAttribute.getValue().getTime() + extraTime));
				   
				PersonAttribute persAttrFrom = (PersonAttribute) card.getAttributeById(DELEGATION_USER_FROM);
				   
				PersonAttribute persAttrTo = (PersonAttribute) card.getAttributeById(DELEGATION_USER_TO);
				   
				List<PersonView> personViews = DelegateHelper.getPersonViewDelegatableList(service);
				Person userFrom = null;
				Person userTo = null;
				
				for(Person person : personViews) {
					if(person != null) {
						if(persAttrFrom != null
								&& persAttrFrom.getPerson() != null
								&& person.getId().getId().equals(persAttrFrom.getPerson().getId().getId()))
							userFrom = person;
						if(persAttrTo != null
								&& persAttrTo.getPerson() != null
								&& person.getId().getId().equals(persAttrTo.getPerson().getId().getId()))
							userTo = person;
					}
					if(userFrom !=null && userTo !=null)
						break;
				}
				   
				if(userFrom != null)
					persAttrFrom.setPerson(userFrom);
				if(userTo != null)
					persAttrTo.setPerson(userTo);
				   
				createDelegetion(card, service);
			} else if (card.getId() == null) { //����� ������� ���������� �������� (����\�����)
				ObjectId id = ((AsyncDataServiceBean)service).saveObject(card, ExecuteOption.SYNC);
				card.setId(((Long) id.getId()).longValue());
			} else {
				((AsyncDataServiceBean)service).saveObject(card, option);
			}
		}
	}
	
	private void saveCard(Card card, boolean changed, DataServiceBean service) throws DataException, ServiceException {
		saveCard(card, changed, service, ExecuteOption.UNDEFINED);
	}
	
	@SuppressWarnings("unchecked")
	private void createDelegetion(Card card, DataServiceBean service) throws DataException, ServiceException {
		ObjectId delegationIdAttributeId = ObjectId.predefined(LongAttribute.class, "jbr.delegation.id");
        LongAttribute delegationIdAttribute = (LongAttribute) card.getAttributeById(delegationIdAttributeId);
        Delegation delegation = getDelegation(card);
        	if(delegationIdAttribute == null) {
        		delegationIdAttribute = new LongAttribute();
        	TemplateBlock informBlock = (TemplateBlock) card.getAttributeBlockById(ObjectId.predefined(TemplateBlock.class, "jbr.delegation.information"));
        	if(informBlock != null) {
        		List<Attribute> attrs = new ArrayList<Attribute>((List<Attribute>)informBlock.getAttributes());
        		attrs.add(delegationIdAttribute);
        		informBlock.setAttributes(attrs);
        	}
        }
        if(delegationIdAttribute != null) {
        	if(delegationIdAttribute.getValue() == 0) {
        		//�������� ������ �������������
		        delegation.setCreatedAt(new Date());	        
		        
                delegationIdAttribute.setId(delegationIdAttributeId);
	        } else {
	         	// ���������� �������������
	           	delegation.setId(delegationIdAttribute.getValue());	
	        }
        	if(service.getIsDelegation()) {
	        	delegation.setCreatorId(service.getRealUser().getPerson().getId());
	        } else delegation.setCreatorId(service.getPerson().getId());
        }
        
        final SaveDelegatesAction action = new SaveDelegatesAction(Collections.singletonList(delegation));
        service.doAction(action);
        delegationIdAttribute.setValue((Long) delegation.getId().getId());
        
        ObjectId id = ((AsyncDataServiceBean)service).saveObject(card, ExecuteOption.SYNC);
        card.setId(((Long)id.getId()).longValue());
	}

    private Delegation getDelegation(Card card) {
        Delegation delegation = new Delegation();

        DateAttribute startDateAttribute =
            (DateAttribute) card.getAttributeById(ObjectId.predefined(DateAttribute.class, "jbr.delegation.date.start"));
        if(null != startDateAttribute) {
            delegation.setStartAt(startDateAttribute.getValue());
        }

        DateAttribute endDateAttribute =
                (DateAttribute) card.getAttributeById(ObjectId.predefined(DateAttribute.class, "jbr.delegation.date.end"));
        if(null != endDateAttribute) {
            delegation.setEndAt(endDateAttribute.getValue());
        }

        PersonAttribute fromAttribute =
                (PersonAttribute) card.getAttributeById(ObjectId.predefined(PersonAttribute.class, "jbr.delegation.from"));
        if(null != fromAttribute) {
            delegation.setFromPersonId(fromAttribute.getValue());
        }

        PersonAttribute toAttribute =
                (PersonAttribute) card.getAttributeById(ObjectId.predefined(PersonAttribute.class, "jbr.delegation.to"));
        if(null != toAttribute) {
            delegation.setToPersonId(toAttribute.getValue());
        }
        /*
        PersonAttribute creatorAttribute =
                (PersonAttribute) card.getAttributeById(ObjectId.predefined(PersonAttribute.class, "jbr.delegation.to"));
        if(null != creatorAttribute) {
            delegation.setCreatorPersonId(creatorAttribute.getValue());
        }
        */

        LongAttribute idAttribute =
                (LongAttribute) card.getAttributeById(ObjectId.predefined(LongAttribute.class, "jbr.delegation.id"));
        if(null != idAttribute && idAttribute.getValue() != 0) {
            delegation.setId(idAttribute.getValue());
        }
        
        return delegation;
    }

    private Card removeFromPersonalControl(Card card, ProcessRequest request, DataServiceBean service) throws Exception {
        // � ������ �������� ������� ��������� �������� ������� ��������
        final Card existControlCard = checkForExistsPrivateControl(card, service);
        if(null == existControlCard) {
            return card;
        }

        try{
			service.doAction(new LockObject(existControlCard.getId()));
			// ��������� ������ �������� � ��������
			ChangeState move = new ChangeState();
			move.setCard(existControlCard);
			move.setWorkflowMove((WorkflowMove) DataObject.createFromId(CONTROL_DELETE_WFM_ID));
			((AsyncDataServiceBean)service).doAction(move, ExecuteOption.SYNC);

			Attribute BossControlDocumentAttribute = existControlCard.getAttributeById(BOSS_CONTROL_DOCUMENT);
			// ���� ������� - ������ �� �������� ��������, �� ������� ���
			if (BossControlDocumentAttribute!=null&&BossControlDocumentAttribute instanceof CardLinkAttribute){
				((CardLinkAttribute)BossControlDocumentAttribute).clear();
			}
			final OverwriteCardAttributes action = new OverwriteCardAttributes();
			action.setCardId(existControlCard.getId());
			action.setAttributes(Collections.singletonList(BossControlDocumentAttribute));
			action.setInsertOnly(false);
			service.doAction(action);

			clearFolderQtyCache(FOLDER_PERSONAL_CONTROL, service);
			return null;
		} catch (Exception e) {
			logger.error("Error while remove PrivateControl card", e);
			throw new Exception(e);
		} finally {
			if (null != existControlCard && null != existControlCard.getId()) {
				try {
                    service.doAction(new UnlockObject(existControlCard.getId()));
                } catch (Exception e) {
                    logger.error("Error unlocking card", e);
                    throw new Exception(e);
                }
            }
		}
    }

    private void addToFavorites(Card card, boolean changed, ProcessRequest request, DataServiceBean service) throws DataException, ServiceException {
        // � ������ ���������� ������� ����� ��������, ���� ��� �� ��������� ������
        final Card favoriteCard = getFavorite(card, service);
        if(null != favoriteCard) {
            return;
        }

        saveCard(card, changed, service);

        clearFolderQtyCache(FOLDER_FAVORITES, service);
    }

    private Card removeFromFavorites(Card card, ProcessRequest request, DataServiceBean service) throws Exception {
        // � ������ �������� ��������� ��������� �������� "���������" � ������ "��������"
        final Card favoriteCard = getFavorite(card, service);
        if(null == favoriteCard) {
            return card;
        }

        try{
			service.doAction(new LockObject(favoriteCard.getId()));
			// ��������� ��������� � ��������
			ChangeState move = new ChangeState();
			move.setCard(favoriteCard);
			move.setWorkflowMove((WorkflowMove) DataObject.createFromId(WFM_TO_DUPLICATES));
			service.doAction(move);

			clearFolderQtyCache(FOLDER_FAVORITES, service);
			return null;
		} catch (Exception e) {
			logger.error("Error while remove Favorite card", e);
			throw new Exception(e);
		} finally {
			if (null != favoriteCard && null != favoriteCard.getId()) {
				try {
                    service.doAction(new UnlockObject(favoriteCard.getId()));
                } catch (Exception e) {
                    logger.error("Error unlocking card", e);
                    throw new Exception(e);
                }
            }
		}
    }

	private WorkflowMove findMove(String name, Card card, ContentProducer cms)
	{
		name = name.trim();
		Collection<WorkflowMove> moves;
		try {
			moves = cms.getContentDataServiceFacade().getChildren(card.getId(), WorkflowMove.class);
		} catch (Exception e) {
			logger.error("Error retrieving list of workflow moves", e);
			return null;	//*****
		}
		
		for (Iterator<WorkflowMove> itr = moves.iterator(); itr.hasNext(); ) {
			WorkflowMove move = itr.next();
			if (name.equalsIgnoreCase(move.getName().getValueRu()) ||
					name.equalsIgnoreCase(move.getName().getValueEn()) ||
					name.equalsIgnoreCase(move.getDefaultName().getValueRu()) ||
					name.equalsIgnoreCase(move.getDefaultName().getValueEn()) ||
					name.equals(move.getId().getId().toString()))
				return move;
		}
		
		for (Iterator<WorkflowMove> itr = moves.iterator(); itr.hasNext(); ) {
			WorkflowMove move = itr.next();
			CardState state;
			try {
                // todo should it be optimized
				state = cms.getService().getById(move.getToState());
			} catch (Exception e) {
				logger.error("Error retrieving card state" + move.getToState().getId(), e);
				continue;
			}
			if (name.equalsIgnoreCase(state.getName().getValueRu()) ||
					name.equalsIgnoreCase(state.getName().getValueEn()) ||
					name.equals(state.getId().getId().toString()))
				return move;
		}
		
		logger.error("State or action " + name + " not found");
		return null;	//*****
	}
	/**
	 * ��������� ��� � �� ���� ������ �������� "������ ��������" (� ����� �� ������������ ����� � ������� �� ��������)
	 * @param card
	 * @return 
	 * true - ������ ������ �������� ��� ����
	 * false - ���� 
	 */
	private Card checkForExistsPrivateControl(Card card, DataServiceBean service){
		// ��������� ������ �������� "������� ��������"
		if (!card.getTemplate().equals(CHECK_TEMPLATE))
			return null;
		
		// ���� �������� ������� ��������� � ������� �������� ��� ������ �������� � ������������� � "������ �� �������� ��������" ��������� � ������������ ���������� ������� �������� 
		final Search search = new Search();

		search.setByAttributes(true);
		List<SearchResult.Column> columns = new ArrayList<SearchResult.Column>(2);
		SearchResult.Column col = new SearchResult.Column();
		col.setAttributeId(BOSS_CONTROL_DOCUMENT);
		columns.add(col);
		col = new SearchResult.Column();
		col.setAttributeId(BOSS_CONTROL_INSPECTOR);
		columns.add(col);
		search.setColumns(columns);
		// ��������� �������� ����� ������ ���� ��������
		search.addPersonAttribute(BOSS_CONTROL_INSPECTOR, ((PersonAttribute)card.getAttributeById(BOSS_CONTROL_INSPECTOR)).getValue());
		search.addCardLinkAttribute(BOSS_CONTROL_DOCUMENT, ((CardLinkAttribute)card.getAttributeById(BOSS_CONTROL_DOCUMENT)).getSingleLinkedId());

		try{
			SearchResult sr = (SearchResult)service.doAction(search);
			// ���� ����� ��������
			if (sr.getCards().size()>0)
				return (Card)sr.getCards().get(0);
		}
		catch (Exception e){ 
			return null;
		}
		return null;
	}

    /**
	 * ��������� ��� � �� ���� ������ �������� "���������" (� ����� �� ������������ ����� � ������� �� ��������) � ������� "�����������"
	 * @param card
	 * @return
	 * true - �������� "���������" ��� ����
	 * false - ����
	 */
	private Card getFavorite(Card card, DataServiceBean service){
		// ��������� ������ �������� "���������"
		if (!card.getTemplate().equals(TEMPLATE_FAVORITE))
			return null;

		// ���� �������� ���������� � ������� �������� �� ��������� � ������������� � "������ �� ��������� ��������"
        // ��������� � ������������ ���������� ������� �������� � ������="�����������"
		final Search search = new Search();
        search.setStates(Collections.singleton(CARD_STATE_PUBLISHED));

		search.setByAttributes(true);
		List<SearchResult.Column> columns = new ArrayList<SearchResult.Column>(2);
		SearchResult.Column col = new SearchResult.Column();
		col.setAttributeId(ATTR_FAVORITE_DOCUMENT);
		columns.add(col);
		col = new SearchResult.Column();
		col.setAttributeId(ATTR_FAVORITE_PERSON);
		columns.add(col);
		search.setColumns(columns);
		// ��������� �������� ����� ������ ���� ��������
		search.addPersonAttribute(ATTR_FAVORITE_PERSON, ((PersonAttribute)card.getAttributeById(ATTR_FAVORITE_PERSON)).getValue());
		search.addCardLinkAttribute(ATTR_FAVORITE_DOCUMENT, ((CardLinkAttribute)card.getAttributeById(ATTR_FAVORITE_DOCUMENT)).getSingleLinkedId());

		try{
			SearchResult sr = (SearchResult)service.doAction(search);
			// ���� ����� ��������
			if (sr.getCards().size()>0)
				return (Card)sr.getCards().get(0);
		}
		catch (Exception e){
			return null;
		}
		return null;
	}
	
	/**
	 *  ���������� �������� "������������" (template_id = 524) 
	 *  �� ������� "��������"(1) � ������ "������������" (67424) 
	 * @param card - ������������ �������� �� ������� �������� 
	 * �� ������������ (attribute_code = 'JBR_INFORM_LIST')
	 * @param service - DataServiceBean
	 * @return true - �����, false - �������� �������� ��������� ������ ��������
	 */
	private boolean sendAcquaintance(Card card, DataServiceBean service) {
		ObjectId attrId = new ObjectId(CardLinkAttribute.class,
				"JBR_INFORM_LIST");
		CardLinkAttribute attr = card.getAttributeById(attrId);
		List<ObjectId> cardIds = new LinkedList<ObjectId>(attr.getIdsLinked());

		Map cardMap;
		try {
			cardMap = getCards(cardIds, service);
		} catch (Exception e) {
			return false;
		}
		int sentCount = 0; // �������
		for (Iterator<ObjectId> itr = cardIds.iterator(); itr.hasNext();) {
			ObjectId itemId = itr.next();
			Card item = (Card) cardMap.get(itemId);
			if (!STATE_DRAFT.equals(item.getState())) {
				continue; // �������� ��� � ������� "������������"
			}
			ChangeState move = new ChangeState();
			move.setWorkflowMove((WorkflowMove) DataObject
					.createFromId(MOVE_SEND));
			move.setCard(item);
			try {
				service.doAction(move);
			} catch (Exception e) {
				continue; // ���� �� ������� ������� ��� �������� - �����
							// ���������
			}
			sentCount++;
		}
		if (sentCount > 0)
			return true;
		return false; // �� ����� �������� �� ����������
	}

	/**
	 * ���������� �������� �� �� id.
	 * @param ids - ������ id ��������
	 * @param service - DataServiceBean
	 * @return Map<Card> - ������ ��������
	 * @throws DataException
	 * @throws ServiceException
	 */
	private Map getCards(List ids, DataServiceBean service)
			throws DataException, ServiceException {
		Search search = new Search();
		search.setByAttributes(false);
		search.setByCode(true);
		search.setWords(ObjectIdUtils.numericIdsToCommaDelimitedString(ids));
		search.setColumns(new ArrayList(3));
		SearchResult.Column col = new SearchResult.Column();
		col.setAttributeId(Card.ATTR_STATE);
		search.getColumns().add(col);
		col = new SearchResult.Column();
		col.setAttributeId(ATTR_PERSON);
		search.getColumns().add(col);
		SearchResult result = (SearchResult) service.doAction(search);

		HashMap map = new HashMap(ids.size());
		for (Iterator itr = result.getCards().iterator(); itr.hasNext();) {
			Card card = (Card) itr.next();
			map.put(card.getId(), card);
		}
		return map;
	}
	
	//	public static final ObjectId ATTR_BASE_DOC = ObjectId.predefined(
	//		BackLinkAttribute.class, "jbr.main.doc");
	
	//final Long baseId = getCardIdFromBackLink(new ObjectId(Card.class, parentId), ATTR_BASE_DOC, serviceBean);
	private Long getCardIdFromBackLink(ObjectId cardId, ObjectId attrId, DataServiceBean serviceBean) {
		Long linkedId = null;
		try {
			final ListProject search = new ListProject();
			search.setAttribute(attrId);
			search.setCard(cardId);
			final List<Card> cards = SearchUtils.execSearchCards(search, serviceBean);
			final Card linked = cards.get(0);
			linkedId = (Long) linked.getId().getId();
		} catch (Exception e) {
			logger.error("Error in get cardId from backlink: " + e );
		}
		return linkedId;
	}
	
	private void clearCurrentFolderQtyCache(ProcessRequest request, DataServiceBean service) {
        String folderId = (String) request.getSessionAttribute(ContentProducer.SESS_ATTR_AREA);
        clearFolderQtyCache(folderId, service);
	}
	
    private void clearFolderQtyCache(String folderId, DataServiceBean service) {
		long[] permissionTypesArray = ContentUtils.getPermissionTypes(new Search());
        int personId = Integer.parseInt(service.getPerson().getId().getId().toString());
        CounterCache.instance().clearValue(folderId, personId, permissionTypesArray);
	}
	
    private class BatchActionsSessionBean {
    	private ArrayList<ChangeState> actions = new ArrayList<ChangeState>();
    	private ArrayList<ObjectId> unlockCards = new ArrayList<ObjectId>();
    	private boolean unlock = true;
    	private boolean lastCard = false;
    	
    	public void clearActions() {
    		actions = new ArrayList<ChangeState>();
    	}
    	
    	public void clearUnlockCards() {
    		unlockCards.clear();
    	}
    }
}
