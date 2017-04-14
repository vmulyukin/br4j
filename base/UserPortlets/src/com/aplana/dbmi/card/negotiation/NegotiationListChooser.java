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
package com.aplana.dbmi.card.negotiation;

import java.io.IOException;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.PortletRequest;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import com.aplana.dbmi.card.CardPortlet;
import com.aplana.dbmi.card.CardPortletCardInfo;
import com.aplana.dbmi.card.CardPortletSessionBean;
import com.aplana.dbmi.card.JspAttributeEditor;
import com.aplana.dbmi.card.SavedNegotiationRoutes;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.DateAttribute;
import com.aplana.dbmi.model.ListAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.AsyncDataServiceBean.ExecuteOption;


public class NegotiationListChooser extends JspAttributeEditor{
//	public static final String ACTION_FIELD = 
//		"com_aplana_dbmi_card_negotiation_NegotiationListChooser_ACTION";
	public static final String CONTROL_VALUE = 
		"com_aplana_dbmi_card_negotiation_NegotiationListChooser_CONTROL_VALUE";
	public static final String ACTION_SELECT = 
		"com_aplana_dbmi_card_negotiation_NegotiationListChooser_ACTION_SELECT";
	public static final String ACTION_CLEAR = 
		"com.aplana.dbmi.card.negotiation.NegotiationListChooser.ACTION_CLEAR";
	public static final ObjectId SAVE_NEGOTIATION_LIST_ATTRIBUTE = 
		ObjectId.predefined(CardLinkAttribute.class, "jbr.saveNegotiationList");
	public static final ObjectId VISA_ATTRIBUTE = 
		ObjectId.predefined(CardLinkAttribute.class, "jbr.visa.set");
	public static final ObjectId PLAN_NEGOTIATION_DATE_ATTRIBUTE = 
		ObjectId.predefined(DateAttribute.class, "jbr.plan_negotiation_date");
	public static final ObjectId URGENCY_LEVEL_REGDATA = 
		ObjectId.predefined(ListAttribute.class, "jbr.urgencyLevel");	
	private static final String JSP = "/WEB-INF/jsp/html/NegotiationListChooser.jsp";
	public void initEditor(PortletRequest request, Attribute attr)
		throws DataException{		
	}
	
	@Override
	public boolean gatherData(ActionRequest request, Attribute attr)
			throws DataException {
		return false;
	}
	
	public void writeEditorCode(RenderRequest request, RenderResponse response, Attribute attr)
		throws IOException, PortletException{
		
		CardLinkAttribute cardLinkAttribute = (CardLinkAttribute)attr;
		
		if (cardLinkAttribute.getLinkedCount() > 0){
			ObjectId id = cardLinkAttribute.getIdsArray()[0];
			request.setAttribute(CONTROL_VALUE, 
					cardLinkAttribute.getLinkedCardLabelText(id, null));
		}		
		
		PortletRequestDispatcher rd = request.getPortletSession().getPortletContext()
			.getRequestDispatcher(JSP);
		rd.include(request, response);		
	}
	
	public boolean processAction(ActionRequest request, ActionResponse response, Attribute attr)
		throws DataException{
		boolean result = false;
		try{
			String action = request.getParameter(CardPortlet.ACTION_FIELD); 
			if (action != null){
				CardPortletSessionBean sessionBean = getCardPortletSessionBean(request);
				Card activeCard = sessionBean.getActiveCard();
				if (action.equals(ACTION_SELECT)){
					
					//��������� � �������������� ���������� ������
					ObjectId cardId = sessionBean.getServiceBean().saveObject(activeCard, ExecuteOption.SYNC);
					
					//��������� ��������
		    		CardPortletCardInfo parent = sessionBean.getActiveCardInfo();
					parent.setCard((Card) sessionBean.getServiceBean().getById(cardId));
					
					//���������� ������ ������
					sessionBean.getActiveCardInfo().getPortletFormManager().
					openForm(new SavedNegotiationRoutes());					

					result = true;				
				}else if(action.equals(ACTION_CLEAR)){
					//���������
					//��� ������ ���������, ��� ��� �������� ����� ������� ���������
					//sessionBean.getServiceBean().saveObject(activeCard);
					
					//������� �������� ���� �������� ������������
					CardLinkAttribute negotiationList = 
						(CardLinkAttribute)activeCard.getAttributeById(SAVE_NEGOTIATION_LIST_ATTRIBUTE);
					negotiationList.clear();
					
					//������� �������� ���� ����
					CardLinkAttribute visa = 
						(CardLinkAttribute)activeCard.getAttributeById(VISA_ATTRIBUTE);
					visa.clear();

					//������� �������� ���� �������� ���� ������������
					DateAttribute planNegotiationDate = 
						(DateAttribute)activeCard.getAttributeById(PLAN_NEGOTIATION_DATE_ATTRIBUTE);
					if (planNegotiationDate!=null)
						planNegotiationDate.clear();
					
					//27.01.2011 �.�. ������� ��������� ��������� (��������, ������� �������
					// ��� ���������� CopyAttributeToChildren)
					ListAttribute urgencyRegdata = 
						(ListAttribute)activeCard.getAttributeById(URGENCY_LEVEL_REGDATA);
					if (urgencyRegdata!=null)
						urgencyRegdata.clear();

					//���������
					sessionBean.getServiceBean().saveObject(activeCard, ExecuteOption.SYNC);
					
		      		//��������� ��������
		    		CardPortletCardInfo parent = sessionBean.getActiveCardInfo();
					parent.setCard((Card) sessionBean.getServiceBean().getById(activeCard.getId()));
					parent.setRefreshRequired(true);					
					
					result = true;				
				}
			}
			return result;
		}catch(Exception ex){
			throw new DataException("ERROR_ON_PROCESS_ACTION", ex);
		}
	}
	
}
