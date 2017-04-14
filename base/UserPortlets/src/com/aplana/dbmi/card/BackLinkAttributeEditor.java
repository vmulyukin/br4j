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
package com.aplana.dbmi.card;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.PortletRequest;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import com.aplana.dbmi.Portal;
import com.aplana.dbmi.action.CreateCard;
import com.aplana.dbmi.action.ListProject;
import com.aplana.dbmi.action.LockObject;
import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.action.SearchResult.Column;
import com.aplana.dbmi.action.UnlockObject;
import com.aplana.dbmi.action.xml.SearchXmlHelper;
import com.aplana.dbmi.card.CardPortletSessionBean.PortletMessage.PortletMessageType;
import com.aplana.dbmi.card.actionhandler.AddLinkedCardActionHandler.TemplateForCreationSelector;
import com.aplana.dbmi.card.graph.Graph;
import com.aplana.dbmi.card.graph.GraphDescriptor;
import com.aplana.dbmi.card.graph.GraphDescriptorReader;
import com.aplana.dbmi.card.graph.GraphLoader;
import com.aplana.dbmi.card.util.CardAttributesInitializer;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.BackLinkAttribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.DataObject;
import com.aplana.dbmi.model.IntegerAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.model.util.AttrUtils;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.service.AsyncDataServiceBean;
import com.aplana.dbmi.service.AsyncDataServiceBean.ExecuteOption;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.ServiceException;

public class BackLinkAttributeEditor extends ActionsSupportingAttributeEditor
{
	public static final String PARAM_PAGE = "page";
	public static final String PARAM_TEMPLATE = "template";
	public static final String PARAM_TEMPLATE_IDS = "templatesForSelect";
	public static final String PARAM_CREATE = "create";
	public static final String PARAM_ATTR_COPY = "copyToNewCard";
	public static final String PARAM_REMOVE = "remove";
	public static final String PARAM_SHOW_TITLE = "showTitle";
	public static final String PARAM_SHOW_EMPTY = "showEmpty";

	public static final String ACTION_ADD = "sub_add";
	public static final String ACTION_REMOVE = "sub_remove";

	public static final String FIELD_LINKED_ID = "linked";

	public static final String KEY_CREATE = "create";
	public static final String KEY_REMOVE = "remove";
	public static final String KEY_SHOW_TITLE = "showTitle";
	public static final String KEY_SHOW_EMPTY = "showEmpty";

	public static final String MATERIAL_DOWNLOAD_URL = "/MaterialDownloadServlet?" +
		MaterialDownloadServlet.PARAM_CARD_ID + "={0}";
	public static final String PARAM_CONFIG = "config";

	public static final ObjectId ID_ACCESS = new ObjectId(IntegerAttribute.class, "_ACCESS");
	public static final int ACCESS_NO = 0;
	public static final int ACCESS_EDIT = 1;

	//----------------------------------------------------------
	// TODO: �������� ����� ��� ���������� ��������� � JspAttributeEditor, �.�. ��� ������������ � � TypedCardLinkAttributeViewer
	public static final String PARAM_CONFIG_GRAPH = "configGraph";

	public static final String GRAPH_IS_VIEW = "graphIsView";
	public static final String GRAPH_DATA = "graphData";
	//-------------------------------------------------------------

	private ObjectId templateId;

	private boolean modeCreate = true;
	private boolean modeRemove = true;
	private boolean showTitle = true;
	private boolean showEmpty = true;

	private String configGraph;
	private Graph graph;
	private String config = null;
	private List<ObjectId> templateIds = new ArrayList<ObjectId>();
	@SuppressWarnings("unused")
	private Collection<Column> columns = null;
	private CardAttributesInitializer cardInitializer = new CardAttributesInitializer();
	private SecondaryColumnsManager scm = new SecondaryColumnsManager();
	private Search search;

	public BackLinkAttributeEditor() {
		setParameter(PARAM_JSP, "/WEB-INF/jsp/html/attr/BackLinks.jsp");
		setParameter(PARAM_INIT_JSP, "/WEB-INF/jsp/html/attr/BackLinksInclude.jsp");
	}

	@Override
	public boolean gatherData(ActionRequest request, Attribute attr)
			throws DataException {
		return false;
	}

	@Override
	public boolean processAction(ActionRequest request,
			ActionResponse response, final Attribute attr) throws DataException {
		String action = request.getParameter(CardPortlet.ACTION_FIELD);
		String attrId = request.getParameter(CardPortlet.ATTR_ID_FIELD);
		if (attrId == null || !attrId.equals(attr.getId().getId()))
			return false;
		final CardPortletSessionBean sessionBean = CardPortlet.getSessionBean(request);
		CardPortletCardInfo cardInfo = sessionBean.getActiveCardInfo();
		if (CardPortlet.CARD_EDIT_MODE.equals(cardInfo.getMode())) {
			/*� ����� ������ ��������� ������� ��������:
			1. ���� ������� �������� - �����, �� ��������� ����� �������� Id
			2. ���� ���� �������� � Id, �� ���������, ���� � ���� ������ ��������� ���������, �
			 	� ����� ��������, ����������� �� �������, ������������ ���������� ������ � ��������������� AttributeEditor.
			*/
			try {
				final ObjectId id = sessionBean.getServiceBean().saveObject(cardInfo.getCard(), ExecuteOption.SYNC);
				cardInfo.getCard().setId(Long.parseLong("" + id.getId()));
				//cardInfo.setCard((Card) sessionBean.getServiceBean().getById(id)); //BR4J00037826 
			} catch (Exception e) {
				sessionBean.setMessageWithType(e.getMessage(), PortletMessageType.ERROR);
				return false;
			}
		}
		if (ACTION_ADD.equals(action)) {
			if(templateId == null){
				TemplateForCreationSelector selector = new TemplateForCreationSelector(sessionBean, templateIds);
				selector.select(new TemplateForCreationSelector.SelectionHandler() {
					public void selected(ObjectId templateId) {
						BackLinkAttributeEditor.this.templateId = templateId;
						openNewCard(sessionBean, attr);
					}
				});
			}
			else {
				openNewCard(sessionBean, attr);
			}
		} else if (ACTION_REMOVE.equals(action)) {
			Card card = cardInfo.getCard();
			try {
				removeLink(sessionBean.getServiceBean(),
						new ObjectId(Card.class, Long.parseLong(request.getParameter(FIELD_LINKED_ID))),
						((BackLinkAttribute) attr).getLinkSource(), card.getId());
				sessionBean.setMessage("edit.link.success.remove",
						new Object[] { request.getParameter(FIELD_LINKED_ID) });
			} catch (Exception e) {
				logger.error("Error unlinking card " + request.getParameter(FIELD_LINKED_ID), e);
				sessionBean.setMessageWithType("edit.link.error.remove", new Object[] { e.getMessage() }, PortletMessageType.ERROR);
				// ���� �������� �� ����������, �� �������� ��� �������� �� ������� ���������
				Collection<ObjectId> linkSources = ((BackLinkAttribute) attr).getLinkSources();
				if (linkSources!=null){
					Iterator<ObjectId> linkSourcesIter = linkSources.iterator();

					while(linkSourcesIter.hasNext()){
						try{
							ObjectId nextLinkAttrId = linkSourcesIter.next();
							removeLink(sessionBean.getServiceBean(),
									new ObjectId(Card.class, Long.parseLong(request.getParameter(FIELD_LINKED_ID))),
									nextLinkAttrId, card.getId());
							sessionBean.setMessage("edit.link.success.remove",
									new Object[] { request.getParameter(FIELD_LINKED_ID) });
							break;
						} catch (Exception e2) {
							logger.error("Error unlinking card " + request.getParameter(FIELD_LINKED_ID), e2);
							if (!linkSourcesIter.hasNext())
								sessionBean.setMessage("edit.link.error.remove", new Object[] { e2.getMessage() });
			}
					}
				}
			}
		} else {
			return super.processAction(request, response, attr);
		}
		return true;
	}

	private void openNewCard(CardPortletSessionBean sessionBean, final Attribute attr) {
		try {
			CardPortletCardInfo cardInfo = sessionBean.getActiveCardInfo();
			Card card = createCard(sessionBean);
			ObjectId linkAttrId = ((BackLinkAttribute) attr).getLinkSource();
			Collection<ObjectId> linkAttrIds = ((BackLinkAttribute) attr).getLinkSources();
			if (linkAttrId != null) {
				CardLinkAttribute cardLinkAttribute = (CardLinkAttribute) card.getAttributeById(linkAttrId);
				// ���� ��������� � �������� �� �������, �� �������� � �������� ��������� ����������, ������� ��������� �� ���� � ��������� �� ������ ������������
				if (cardLinkAttribute==null&&linkAttrIds!=null){
					for(ObjectId nextLinkAttrId: linkAttrIds){
						cardLinkAttribute = (CardLinkAttribute)card.getAttributeById(nextLinkAttrId);
						if (cardLinkAttribute!=null)
							break;
					}
				}
				cardLinkAttribute.addLinkedId(cardInfo.getCard().getId());
			}

			Card activeCard = cardInfo.getCard();
			cardInitializer.setSourceCard(activeCard);
			cardInitializer.initialize(card);

			sessionBean.openNestedCard(
			        card,
				new CardPortletCardInfo.CloseHandler() {
					public void afterClose(CardPortletCardInfo closedCardInfo, CardPortletCardInfo previousCardInfo) {
						if (closedCardInfo.getCard().getId() != null) {
							previousCardInfo.setAttributeEditorData(attr.getId(), KEY_VALUE_CHANGED, Boolean.TRUE);
						}
					}
				},
				true
			);
		} catch (Exception e) {
			logger.error("Can't create new card", e);
			sessionBean.setMessageWithType("edit.link.error.create", new Object[] { e.getMessage() }, PortletMessageType.ERROR);
		}
	}

	protected Card createCard(CardPortletSessionBean sessionBean) throws DataException, ServiceException {
	    CreateCard createCard = new CreateCard();
            createCard.setTemplate(templateId);
            return (Card) sessionBean.getServiceBean().doAction(createCard);
	}

	private void removeLink(AsyncDataServiceBean serviceBean, ObjectId id, ObjectId attrId, ObjectId parentId)
			throws DataException, ServiceException {
		serviceBean.doAction(new LockObject(id));
		try {
			final Card card = (Card) serviceBean.getById(id);
			final CardLinkAttribute attr = (CardLinkAttribute) card.getAttributeById(attrId);
			if (attr == null)
				throw new IllegalStateException("Attribute " + id.getId() + " not found"); //*****
			// (2010/02, RuSA)
			final boolean removed = attr.removeLinkedId(parentId);

			if (!removed)
				throw new IllegalStateException("Not linked to this");	//*****
			serviceBean.saveObject(card);
		} finally {
			serviceBean.doAction(new UnlockObject(id));
		}
	}

	@Override
	public void writeEditorCode(RenderRequest request, RenderResponse response,
			Attribute attr) throws IOException, PortletException {
		//final CardPortletSessionBean sessionBean = CardPortlet.getSessionBean(request);
		//CardPortletCardInfo cardInfo = sessionBean.getActiveCardInfo();
		super.writeEditorCode(request, response, attr);
	}

	@Override
	public void initEditor(PortletRequest request, Attribute attr)
			throws DataException {

		super.initEditor(request, attr);

		final CardPortletSessionBean sessionBean = CardPortlet.getSessionBean(request);
		final AsyncDataServiceBean serviceBean = sessionBean.getServiceBean();
		final CardPortletCardInfo cardInfo = sessionBean.getActiveCardInfo();

		if(config != null){
			try {
				search = new Search();
				final InputStream xml = Portal.getFactory().getConfigService().loadConfigFile(AttributeEditorFactory.CONFIG_FOLDER + config);
				try {
					SearchXmlHelper.initFromXml(search, xml);
				} finally {
					xml.close();
				}
				final Collection<Column> columnsLocal = search.getColumns();
				if(columnsLocal != null && !columnsLocal.isEmpty())
					this.columns = columnsLocal;
			} catch (IOException e) {
				logger.error("Couldn't open hierarchy descriptor file: " + config, e);
				throw new DataException(e);
			}
		}

		if (attr != null && attr instanceof BackLinkAttribute) {
			loadAttributeValues(attr, request);
		}

		if (modeCreate) {
			cardInfo.setAttributeEditorData(attr.getId(), KEY_CREATE, Boolean.TRUE);
			if (templateId != null) {
				final CreateCard create = new CreateCard();
				create.setTemplate(templateId);
				try {
					if (!serviceBean.canDo(create)) {
						cardInfo.setAttributeEditorData(attr.getId(), KEY_CREATE, Boolean.FALSE);
					}
				} catch (Exception e) {
					logger.error("Error accessing data service", e);
				}
			}
		} else {
			cardInfo.setAttributeEditorData(attr.getId(), KEY_CREATE, Boolean.FALSE);
		}

		if (modeRemove)
			cardInfo.setAttributeEditorData(attr.getId(), KEY_REMOVE, Boolean.TRUE);
		cardInfo.setAttributeEditorData(attr.getId(), KEY_SHOW_TITLE, new Boolean(showTitle));
		cardInfo.setAttributeEditorData(attr.getId(), KEY_SHOW_EMPTY, new Boolean(showEmpty));
		// --------------------------------------------------------------
		// �������� ����� ������� � ��������� ����� ������ ������, �.�. ���� ��� ������������ � � TypedCardLinkAttributeViewer
		// ��������� configGraph ���� �� �����
		if (configGraph != null) {
			InputStream stream;
			try {
				stream = Portal.getFactory().getConfigService().loadConfigFile(AttributeEditorFactory.CONFIG_FOLDER + configGraph);
			} catch (IOException e) {
				logger.error("Couldn't open graph descriptor file: " + configGraph, e);
				throw new DataException(e);
			}
			GraphDescriptor descripGraph;
			try {
				GraphDescriptorReader reader = new GraphDescriptorReader();
				descripGraph = reader.read(stream);
				GraphLoader loader = new GraphLoader(descripGraph, serviceBean);
				if (sessionBean.getActiveCard() != null && sessionBean.getActiveCard().getId() != null) {
					Long cardId = (Long)sessionBean.getActiveCard().getId().getId();
					graph = loader.load(cardId);
					cardInfo.setAttributeEditorData(attr.getId(), GRAPH_IS_VIEW, Boolean.TRUE);
					cardInfo.setAttributeEditorData(attr.getId(), GRAPH_DATA, graph);
				} else {
					cardInfo.setAttributeEditorData(attr.getId(), GRAPH_IS_VIEW, Boolean.FALSE);
					cardInfo.setAttributeEditorData(attr.getId(), GRAPH_DATA, null);
				}

			} catch (Exception e) {
				throw new DataException(e);
			}
		} else {
			cardInfo.setAttributeEditorData(attr.getId(), GRAPH_IS_VIEW, Boolean.FALSE);
			cardInfo.setAttributeEditorData(attr.getId(), GRAPH_DATA, null);
		}
		// ------------------------------------------------------------------
	}

	@Override
	public void setParameter(String name, String value) {
		if (PARAM_TEMPLATE.equalsIgnoreCase(name))
			this.templateId = ObjectIdUtils.getObjectId(Template.class, value, true);
		else if (PARAM_TEMPLATE_IDS.equalsIgnoreCase(name)){
			templateIds = ObjectIdUtils.commaDelimitedStringToNumericIds(value, Template.class);
		}
		else if (PARAM_CREATE.equalsIgnoreCase(name))
			modeCreate = Boolean.parseBoolean(value);
		else if (PARAM_REMOVE.equalsIgnoreCase(name))
			modeRemove = Boolean.parseBoolean(value);
		else if (PARAM_CONFIG_GRAPH.equalsIgnoreCase(name))
			configGraph = value;
		else if (PARAM_SHOW_TITLE.equalsIgnoreCase(name))
			showTitle = Boolean.parseBoolean(value);
		else if (PARAM_SHOW_EMPTY.equalsIgnoreCase(name))
			showEmpty = Boolean.parseBoolean(value);
		else if (PARAM_CONFIG.equals(name))
			this.config = value;
		else if (name.startsWith(PARAM_ATTR_COPY)) {
			String[] pair = value.split("->");
			if (pair.length != 2) {
				logger.error("Illegal value of parameter " + name + ": " + value);
			}
			ObjectId sourceId = AttrUtils.getAttributeId(pair[0].trim());
			ObjectId destId = AttrUtils.getAttributeId(pair[1].trim());
			this.cardInitializer.addAttributesToCopy(sourceId, destId);
		}
		else
			super.setParameter(name, value);
	}

	@Override
	public boolean doesFullRendering(Attribute attr) {
		return true;
	}

	public boolean isModeCreate() {
		return modeCreate;
	}

	public boolean isModeRemove() {
		return modeRemove;
	}

	public boolean isShowTitle() {
		return showTitle;
	}

	public boolean isShowEmpty() {
		return showEmpty;
	}

	@Override
	public boolean equals(Object obj) {
		if (!super.equals(obj))
			return false;
		final BackLinkAttributeEditor other = (BackLinkAttributeEditor) obj;
		return
			modeCreate == other.modeCreate && modeRemove == other.modeRemove &&
			(templateId == null ? other.templateId == null : templateId.equals(other.templateId));
	}

	@Override
	public int hashCode() {
		return super.hashCode() ^
			(modeCreate ? 0x38E522AD : 0x55555555) ^
			(modeRemove ? 0x4FA1001E : 0x33333333) ^
			(templateId == null ? 0 : templateId.hashCode());
	}

	@Override
	public boolean isValueCollapsable() {
		return true;
	}

	@Override
	public final void loadAttributeValues(Attribute attr, PortletRequest request) {
		CardPortletSessionBean sessionBean = getCardPortletSessionBean(request);
		CardPortletCardInfo cardInfo = sessionBean.getActiveCardInfo();

		SearchResult sr = null;
		if (cardInfo.getCard() != null && cardInfo.getCard().getId() != null) {
			sr = getSearchResult(attr, cardInfo.getCard().getId(), sessionBean.getServiceBean());
			scm.fetchColumns(sr);
		}

		cardInfo.setAttributeEditorData(attr.getId(), LinkedCardUtils.ATTR_LINK_CARDS_LIST, (sr != null) ? sr.getCards() : null);
		cardInfo.setAttributeEditorData(attr.getId(), LinkedCardUtils.ATTR_LINK_COLUMNS_LIST, (sr != null) ? sr.getColumns() : null);
		cardInfo.setAttributeEditorData(attr.getId(), LinkedCardUtils.ATTR_LINK_LABEL_COLUMNS_LIST, sr != null ? sr.getLabelColumnsForCards() : null);
		cardInfo.setAttributeEditorData(attr.getId(), LinkedCardUtils.ATTR_LINK_SECONDARY_COLUMNS_MANAGER, scm);
	}
	
	
	protected SearchResult getSearchResult(Attribute attribute, ObjectId cardId, AsyncDataServiceBean serviceBean) {
		try {
			//ListProject search = new ListProject();
			// ���� ������� �� config xml-����� columns, �� ���������� �� � search
			/*
			if(this.columns != null){
				search.setColumns(this.columns);
			}*/
			ListProject linkedCards = new ListProject(cardId);
			linkedCards.setAttribute(attribute.getId());
			linkedCards.setColumns(search.getColumns());
			
			SearchResult result = serviceBean.doAction(linkedCards);
			
			search.setByAttributes(false);
			search.setByCode(true);
			StringBuilder words = new StringBuilder();
			Iterator<Card> iterator = result.getCards().iterator();
			while(iterator.hasNext()){
				Card c = iterator.next();
				words.append(c.getId().getId().toString());
				if(iterator.hasNext()){
					words.append(",");
				}
			}
			search.setWords(words.toString());
			// ���� �������� ����� ��� ����� ���� �������� ������ ������� ...
			//search.setCard(cardId == null ? new ObjectId(Card.class, new Long(-1)) : cardId);
			result = serviceBean.doAction(search);
			
			if (result.getCards().isEmpty()) {
				// ���� �������� �� ����������, �� �������� ��� �������� �� ������� ���������
				Collection<ObjectId> linkSources = ((BackLinkAttribute) attribute).getLinkSources();
				if (linkSources != null) {
					Iterator<ObjectId> linkSourcesIter = linkSources.iterator();
					while (linkSourcesIter.hasNext() && result.getCards().isEmpty()) {
						ObjectId nextLinkAttrId = linkSourcesIter.next();
						search.clearAttributes();
						search.addCardLinkAttribute(nextLinkAttrId, cardId);
						result = serviceBean.doAction(search);
					}
				}
			}

			// �������� id ��������� �������� �� ���������� ������
			//Set<ObjectId> cardIds = ObjectIdUtils.getObjectIds(result.getCards());
			// ����� �� ��������� ��������� (labelAttrIds)
			/*Search searchLabel = new Search();
			searchLabel.setByCode(true);
			if(this.columns != null) {
				searchLabel.setColumns(this.columns);
			}
			searchLabel.setWords(ObjectIdUtils.numericIdsToCommaDelimitedString(cardIds));
	        try {
	            result = (SearchResult) serviceBean.doAction(searchLabel);
	        } catch (Exception e){
	            logger.error("Exception during fetching persons' cards for attribute "+ attribute.getId().getId() + ": " + e.getMessage());
	            e.printStackTrace();
	        }*/

			if (cardId != null) {
				if (modeRemove)
					for (Iterator<Card> itr = result.getCards().iterator(); itr.hasNext(); )
					{
						final Card linked = itr.next();
						IntegerAttribute attr = (IntegerAttribute) DataObject.createFromId(ID_ACCESS);
						attr.setValue(serviceBean.canChange(linked.getId())
								? ACCESS_EDIT : ACCESS_NO);
						linked.getAttributes().add(attr);
					}
			}
			return result;
		} catch (Exception e) {
			logger.error("Error retrieving card list for attribute " + attribute.getId().getId(), e);
			return null;
		}
	}
}
