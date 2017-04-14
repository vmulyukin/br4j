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
package com.aplana.cms.card;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.PortletRequest;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.PortletSession;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import com.aplana.dbmi.card.AttributeEditor;
import com.aplana.dbmi.card.CardPortlet;
import com.aplana.dbmi.card.CardPortletCardInfo;
import com.aplana.dbmi.card.CardPortletSessionBean;
import com.aplana.dbmi.card.CardPortletSessionBean.PortletMessage.PortletMessageType;
import com.aplana.dbmi.gui.AttributeView;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.AttributeBlock;
import com.aplana.dbmi.model.AttributeViewParam;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.ContextProvider;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.model.TemplateBlock;
import com.aplana.dbmi.model.TypedCardLinkAttribute;
import com.aplana.dbmi.portlet.ResolutionReportPortlet;
import com.aplana.dbmi.portlet.ResolutionReportPortletSessionBean;
import com.aplana.dbmi.service.AccumulativeDataException;
import com.aplana.dbmi.service.AsyncDataServiceBean;
import com.aplana.dbmi.service.AsyncDataServiceBean.ExecuteOption;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.ServiceException;
import com.aplana.web.tag.util.StringUtils;

/**
 * Represents portlet for handling creation/edition cards inside Supervisor/Minister Workstation
 *  
 * @author skashanski
 *
 */
public class WorkstationCardPortlet extends CardPortlet {
	
	public static final String CMS_JSP_FOLDER = "/WEB-INF/jsp/cms/card/";
	private static final String PARAM_CARD_CREATE_ERROR_URL = "cardCreateErrorURL";
	
	public static final ObjectId ATTR_RELAT_DOCS = ObjectId.predefined(TypedCardLinkAttribute.class, "jbr.relatdocs");

	@Override
	public void doView(RenderRequest request, RenderResponse response)
			throws PortletException, IOException {
		
		// Process URL parameters
		externalRequestHandler(request, response);

		// Set the MIME type for the render response
		response.setContentType(request.getResponseContentType());
		// Set user locale
		ContextProvider.getContext().setLocale(request.getLocale());

		// Check if portlet session exists
		CardPortletSessionBean sessionBean = getSessionBean(request);
		checkSessionBean(response, sessionBean);
		sessionBean.setResourceBundle(getPortletConfig().getResourceBundle(request.getLocale()));
		
		WorkstationCardPortletCardInfo cardInfo = (WorkstationCardPortletCardInfo)loadCardInfo(sessionBean);
		
		if(cardInfo.getCard() == null) {
			createCardError(sessionBean, request, response);
			return;
		}
		
		CmsCardForm cmsCardForm = getCardForm(cardInfo.getCard().getTemplate(), null, sessionBean);
		
		initCardPortletCardInfoAttributesViews(request, cardInfo, cmsCardForm);
			
		if (cardInfo.getPortletFormManager().processRender(request, response)) {
			return;
		}
		
		saveSessionBeanForServlet(request, response, sessionBean);

		// Invoke the JSP to render
		String jspFile = cmsCardForm.getView();
		renderJsp(request, response, jspFile);
	}
	
	/**
	 * ���� ��� �������� �������� � ��� ��� �� ���������, �� ������� ������ (���� ����) � ������ �������� �����
	 * @param sessionBean
	 * @param request
	 * @param response
	 * @throws IllegalStateException
	 * @throws IOException
	 */
	protected void createCardError(CardPortletSessionBean sessionBean, RenderRequest request, RenderResponse response) 
			throws IllegalStateException, IOException {
		PrintWriter out = response.getWriter();
		out.print("<h3>");
		if(sessionBean.getMessage() != null) {
			out.print(sessionBean.getMessage());
		} else {
			out.print(getMessage(request, "db.side.error.msg"));
		}
		String errorParam = portletService.getUrlParameter(request, PARAM_CARD_CREATE_ERROR_URL);
		if(errorParam != null) {
			String returnMsg = getMessage(request, "workstation.error.return");
			out.print("<hr/><a href=\"" + errorParam + "\">" + (returnMsg != null ? returnMsg : "") + "</a>");
		}
		out.print("</h3>");
	}
	
	@Override
	public void processAction(ActionRequest request, ActionResponse response)
			throws PortletException, IOException {
		ContextProvider.getContext().setLocale(request.getLocale());
		
		CardPortletSessionBean sessionBean = getSessionBean(request);
		if(sessionBean != null) {
			
			final String action = request.getParameter(ACTION_FIELD);
			if (CREATE_CARD_ACTION.equals(action)) {
				//�������� ����� ��������
				String templateId = request.getParameter(TEMPLATE_ID_FIELD);
				ObjectId indepRes = ObjectId.predefined(Template.class, "jbr.independent.resolution");
				
				//���� �������� - ����������� ��������� - ���������� ������ �� QuickIndepResolutionPortlet
				if(indepRes != null
						&& indepRes.getId() != null
						&& indepRes.getId().toString().equals(templateId)) {
					
					sessionBean.getActiveCardInfo().getPortletFormManager().closeForm();
					
					response.sendRedirect("/portal/auth/portal/boss/indepResolution/Content?"+
									"formAction=initIndepRes&action=1&isCardLinked=1&stateInit=initCreate"+
									"&backURL=/portal/auth/portal/boss/workstationCard");
					
					return;
				}
			}
		}
		super.processAction(request, response);
	}
	
	@Override
	protected boolean externalRequestHandler(RenderRequest request, RenderResponse response) {
		boolean handled = super.externalRequestHandler(request, response);

		CardPortletSessionBean sessionBean = getSessionBean(request);
		if (sessionBean != null) {
			String backURL = portletService.getUrlParameter(request,
					BACK_URL_FIELD);
			if(backURL != null
					&& !"".equals(backURL)
					&& sessionBean.getBackURL() == null)
				sessionBean.setBackURL(backURL);
			String linkParam = portletService.getUrlParameter(request, CardPortlet.PARAM_LINK_TO_CARD);
			
			if (StringUtils.hasLength(linkParam)) {
				Card activeCard = sessionBean.getActiveCard();
				if (activeCard != null) {
					long linkedCardId = Long.parseLong(linkParam);
					TypedCardLinkAttribute relatDocsAttr = (TypedCardLinkAttribute) activeCard.getAttributeById(ATTR_RELAT_DOCS);
					if (relatDocsAttr != null) {
						relatDocsAttr.addLinkedId(linkedCardId);
						relatDocsAttr.addType(linkedCardId, null);
					}
				}
			}

			String formPrefixParam = portletService.getUrlParameter(request, "FORM_PREFIX");
			if (StringUtils.hasLength(formPrefixParam)) {
				((WorkstationCardPortletSessionBean) sessionBean).setCardFromPrefix(formPrefixParam);
			}
		}

		return handled;
	}

	private void reloadCard(RenderRequest request,
			WorkstationCardPortletCardInfo cardInfo) throws PortletException {
		
		if(!cardInfo.getReloadRequired())
			return;
		
		try {
			reloadCard(request, cardInfo.getMode());
			
		} catch (Exception e) {
			throw new PortletException(e);
		}

	}
	
	private boolean isCardCreated(WorkstationCardPortletCardInfo cardInfo) {
		
		return cardInfo.getCard() != null;
		
	}

	
	@Override
	protected  CardPortletCardInfo createCardPortletCardInfo() {
		
		return new WorkstationCardPortletCardInfo();
		
	}
	
	@Override
	protected CardPortletSessionBean createCardPortletSessionBean() {
		
		return new WorkstationCardPortletSessionBean();
		
	}
	
		
	protected void initCardPortletCardInfoAttributesViews(PortletRequest request, WorkstationCardPortletCardInfo cardInfo, CmsCardForm cmsCardForm) {
		CardPortletSessionBean sessionBean = getSessionBean(request);

		Card card = sessionBean.getActiveCardInfo().getCard();
		Collection<TemplateBlock> templateBlocks = card.getAttributes();

		// merge new created card attributes with attributes defined at {@link
		// CmsCardForm}
		List<AttributeBlockView> attributeBlockViews = merge(request, templateBlocks, cmsCardForm.getBlockAttributes());
		cardInfo.setAttributeBlockViews(attributeBlockViews);
	}
	
	
	protected Collection<AttributeBlock>  getAttributeBlocks(ObjectId template, AsyncDataServiceBean serviceBean) throws ServiceException, DataException  {
		
		return serviceBean.<AttributeBlock, TemplateBlock>listChildren(template, TemplateBlock.class);
		
		
	}
	
	
	@Override
	protected boolean processAttributeAction(Card card, ActionRequest request, ActionResponse response) {
		if (card == null)
			return false;
		final CardPortletSessionBean sessionBean = getSessionBean(request);
		WorkstationCardPortletCardInfo workstationCardInfo = (WorkstationCardPortletCardInfo)sessionBean.getActiveCardInfo();
		
		for(AttributeBlockView attributeBlockView : workstationCardInfo.getAttributeBlockViews()) {
		
			for ( AttributeView av : attributeBlockView.getAttributeViews().values())
			{
				final AttributeEditor editor = av.getEditor();
				final Attribute attr = av.getAttribute();
				if (editor == null)
					continue;
				try {
					if (editor.processAction(request, response, attr)) {
						return true;
					}
				} catch (DataException e) {
					logger.error("Exception caught while processing actions for attribute " + attr.getId().getId(), e);
				}
			}
		}
		return false;
	}
	
	protected void applyAtributeViewParams(AttributeView attributeView, Collection<AttributeViewParam> attrViewParams) {
		for (AttributeViewParam attributeViewParam : attrViewParams) {
			// if (!attributeViewParam.getId().equals(attributeView.getId()))
			// continue;
			if (attributeView.getId().getId().equals(attributeViewParam.getAttribute().getId())) {
				attributeView.applyViewParameters(attributeViewParam);
				break;
			}
		}
	}
	
	protected List<AttributeBlockView> merge(PortletRequest request, Collection<TemplateBlock> templateBlocks, List<CmsCardBlock> blockAttributes) {
		
		List<AttributeBlockView> attributeBlockViews = new ArrayList<AttributeBlockView>();
		
		for(CmsCardBlock cmsCardBlock : blockAttributes ) {

			AttributeBlockView attributeBlockView = createAttributeBlockView(cmsCardBlock);
			for(CmsViewAttribute cmsViewAttribute : cmsCardBlock.getAttributeViews().values()) {
				
				String attributeCode = cmsViewAttribute.getCode();
				for(TemplateBlock templateBlock : templateBlocks) {
					Attribute attribute = templateBlock.getAttributeByName(attributeCode);
					if (attribute == null)
						continue;
					AttributeView attributeView = createAttributeView(request, attribute);
					attributeBlockView.getAttributeViews().put(attributeCode, attributeView);
				}
			}
			
			attributeBlockViews.add(attributeBlockView);	
		}
		
		return attributeBlockViews; 
		
	}


	private AttributeBlockView createAttributeBlockView(CmsCardBlock cmsCardBlock) {
		
		AttributeBlockView attributeBlockView = new AttributeBlockView(cmsCardBlock.getId());

		attributeBlockView.setName(cmsCardBlock.getName());
		attributeBlockView.setRegion(cmsCardBlock.getRegion());
		
		return attributeBlockView;
		
	}
	

	private AttributeView createAttributeView(PortletRequest request, Attribute attribute) {
		
		AttributeView attributeView = new AttributeView(attribute);
		
		final CardPortletSessionBean sessionBean = getSessionBean(request);
		
		CardPortletCardInfo cardInfo = sessionBean.getActiveCardInfo();
		//apply AttributeViewParam's parameters
		applyAtributeViewParams(attributeView, cardInfo.getAttributeViewParams());
		
		attributeView.initEditor(request);
		
		return attributeView;
		
	}
	
	
	

	private CardPortletCardInfo loadCardInfo(CardPortletSessionBean sessionBean)
			throws PortletException {
		
		CardPortletCardInfo cardInfo = sessionBean.getActiveCardInfo();

		AsyncDataServiceBean serviceBean = sessionBean.getServiceBean();
		
		try {
			
			if (cardInfo.getCard() != null && cardInfo.isRefreshRequired()) {
				loadCardInfo(sessionBean, serviceBean);
			}
			
			cardInfo.setMode(cardInfo.getMode());
			
			return cardInfo;
			
		} catch (Exception e) {
			throw new PortletException(e);
		}
		

	}
	
	
	/**
	 * Overwrites the actual content of the edited card in order to not loose
	 * the updated modified field when  is necessary a call to an external form,
	 * before the SAVE command is invoked.
	 * @throws DataException 
	 * */
	protected void fillCardFromRequest(ActionRequest request) throws AccumulativeDataException {

		CardPortletSessionBean sessionBean = getSessionBean(request);
		WorkstationCardPortletCardInfo cardInfo = (WorkstationCardPortletCardInfo) sessionBean.getActiveCardInfo();
		AccumulativeDataException accumulativeExc = null;

		for (AttributeBlockView attributeBlockView : cardInfo.getAttributeBlockViews()) {
			for (Iterator<AttributeView> i = attributeBlockView.getAttributeViews().values().iterator(); i.hasNext();) {
				final AttributeView av = i.next();
				final AttributeEditor editor = av.getEditor();
				if (editor == null)
					continue;
				try {
					editor.gatherData(request, av.getAttribute());
				} catch (DataException e) {
					logger.error(e);
					if (accumulativeExc == null) {
						accumulativeExc = new AccumulativeDataException();
					}
					accumulativeExc.addException(e);
				}
			}
		}

		if (accumulativeExc != null && accumulativeExc.getExceptionsQuantity() > 0) {
			throw accumulativeExc;
		}
	}

	protected void backActionHandler(ActionRequest request, ActionResponse response) 
			throws IOException, DataException, ServiceException 
	{
		CardPortletSessionBean sessionBean = getSessionBean(request);
		fillInBackParams(request, sessionBean);
		super.backActionHandler(request, response);
	}

	protected boolean storeCardHandler(ActionRequest request) {
		boolean actionResult = true;
		CardPortletSessionBean sessionBean = getSessionBean(request);
		Card card = sessionBean.getActiveCard();
		String msg = getMessage(request, "card.store.success.msg");
		PortletMessageType msgType = PortletMessageType.EVENT;
		try {
			final ObjectId id = sessionBean.getServiceBean().saveObject(card, ExecuteOption.SYNC);
			card.setId(Long.parseLong("" + id.getId()));
			reloadCard(request, CARD_EDIT_MODE);
			fillInBackParams(request, sessionBean);
		} catch (Exception e) {
			actionResult = false;
			msg = getMessage(request, "db.side.error.msg") + e.getMessage();
			msgType = PortletMessageType.ERROR;
		}
		sessionBean.setMessageWithType(msg, msgType);
		return actionResult;
	}
	
	protected boolean storeCardSyncHandler(ActionRequest request) {
		return storeCardHandler(request);
	}
	
	private void fillInBackParams(ActionRequest request, CardPortletSessionBean sessionBean) throws IOException {
		PortletSession session = request.getPortletSession();
		if(session.getAttribute(ResolutionReportPortlet.SESSION_BEAN, PortletSession.APPLICATION_SCOPE) != null){
			ResolutionReportPortletSessionBean resolutionReportPortletSessionBean = 
					(ResolutionReportPortletSessionBean) session.getAttribute(ResolutionReportPortlet.SESSION_BEAN, PortletSession.APPLICATION_SCOPE);
			if(resolutionReportPortletSessionBean.getReportPreparedDocs() != null){
				resolutionReportPortletSessionBean.getReportPreparedDocs().add(sessionBean.getActiveCard());
			} else {
				List<Card> preparedDocsList = new ArrayList<Card>();
				preparedDocsList.add(sessionBean.getActiveCard());
				resolutionReportPortletSessionBean.setReportPreparedDocs(preparedDocsList);
			}
		}
	}

	private void renderJsp(RenderRequest request, RenderResponse response,
			String jspFile) throws PortletException, IOException {
		
		PortletRequestDispatcher rd = getPortletContext().getRequestDispatcher(getJspFilePath(jspFile));
		rd.include(request, response);
		
	}


	protected static String getJspFilePath(String jspFile) {
		
		return CMS_JSP_FOLDER + jspFile;
		
	}
	

	private void checkSessionBean(RenderResponse response,
			CardPortletSessionBean sessionBean) throws IOException {
		
		if (sessionBean == null) {
			response.getWriter().println("<b>PORTLET SESSION IS NOT INITIALIZED YET</b>");
			return;
		}
	}
	
	private CmsCardForm getCardForm(ObjectId templateId, ObjectId personId, CardPortletSessionBean sessionBean) {
		return CardFormLocator.getCardForm(templateId, personId, ((WorkstationCardPortletSessionBean) sessionBean).getCardFromPrefix());
	}
}
