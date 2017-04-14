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
package com.aplana.dbmi.card.doclinked;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.List;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;

import com.aplana.dbmi.actionhandler.ActionHandler;
import com.aplana.dbmi.card.CardPortlet;
import com.aplana.dbmi.card.CardPortletCardInfo;
import com.aplana.dbmi.card.CardPortletSessionBean;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.BackLinkAttribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.ReferenceValue;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.model.TypedCardLinkAttribute;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.ServiceException;

/**
 * �������� �������������� ��������� ����������.
 *    ������������: ��� ��������� ��� ���������� ����������� ��������
 * �� ������ "������� ���������...".
 * @author RAbdullin
 */
public class DoclinkCreateActionHandler extends ActionHandler 
{
	private static ObjectId inReplyTo = ObjectId.predefined(ReferenceValue.class, "jbr.inResponse");

	ObjectId attrBackLinkId;	// backlink-������� �������� ��������
	ObjectId attrCardLinkId;	// cardLink ����� ��������, ����-��� attrBackLinkId 
	ObjectId templateId;
	Long linkTypeId;


	/**
	 * @return ��� �������� (���� backlink ��� �������) � �������� ��������, � 
	 * ������ �������� ������ ������� ����������� ��������.
	 */
	public ObjectId getAttrBackLinkId() {
		return this.attrBackLinkId;
	}

	/**
	 * @param attrLinkCode ��� �������� (���� backlink) � �������� ��������, � 
	 * ������ �������� ������ ������� ����������� ��������.
	 * @throws DataException 
	 */
	public void setAttrBackLinkId(ObjectId value) throws DataException {
		if (value != null && !BackLinkAttribute.class.isAssignableFrom(value.getType()))
			//store.cardaccess.wrong.class=�������� ��� ���������\: �������� ''{0}'', �� ��������� ''{1}'' 
			throw new DataException( "store.cardaccess.wrong.class", 
					new Object[] {BackLinkAttribute.class, value.getType()} );

		this.attrBackLinkId = value;
	}

	public void setAttrBackLinkCode(String attrBackLinkKeyOrCode) 
		throws DataException 
	{
		setAttrBackLinkId( DoclinkUtils.tryFindPredefinedObjectId(attrBackLinkKeyOrCode));
	}


	/**
	 * @return id ������� ������������ ���������.
	 */
	public ObjectId getTemplateId() {
		return this.templateId;
	}

	/**
	 * @param templateId id ������� ������������ ���������.
	 */
	public void setTemplateId(ObjectId templateId) {
		this.templateId = templateId;
	}

	/**
	 * @return �������� ��� ������������ ���������, ��� ������, �����
	 * cardlink-������� (�������������� backlink AttrBackLinkId) ����� 
	 * ��� TypedCardLinkLAttribute.
	 */
	public Long getLinkTypeId() {
		return this.linkTypeId;
	}

	/**
	 * @param linkTypeId �������� ��� ������������ ���������, ��� ������, �����
	 * cardlink-������� ���� TypedCardLinkLAttribute.
	 */
	public void setLinkTypeId(Long linkTypeId) {
		this.linkTypeId = linkTypeId;
	}


	private void getParamsFromRequest( ActionRequest request) 
		throws DataException
	{
		/*
		 * ��������� ��� �������� backlink
		 */
		final String attrBackLinkKeyOrCode = 
			request.getParameter(CardPortlet.ATTR_ID_FIELD);
		if (attrBackLinkKeyOrCode == null)
			throw new DataException("jbr.processor.nodestattr_2", new Object[] {
					"DocLink process()", null });
		setAttrBackLinkCode(attrBackLinkKeyOrCode);

		/*
		 * �������� id �������...
		 */
		final String doclink_template = 
			request.getParameter(CardPortlet.PARAM_DOCLINK_TEMPLATE);
		if (doclink_template == null)
			throw new DataException("store.card.template");
		this.templateId = ObjectIdUtils.getObjectId( Template.class, doclink_template, true);

		/*
		 * �������� id-��� ���������
		 */
		final String doclink_type = 
			request.getParameter(CardPortlet.PARAM_DOCLINK_TYPE);
		this.linkTypeId = (doclink_type == null) ? null : Long.parseLong(doclink_type);
	}

	/** 
	 * @see com.aplana.dbmi.actionhandler.ActionHandler#process(java.util.List, javax.portlet.ActionRequest, javax.portlet.ActionResponse)
	 * ������� ��������� (���������) �������� ������� ����.
	 * ���, ������� � ��. ���������� �� ���������� �������.
	 * @param cardIds: ����� �� ������������.
	 * @param request ������, � ������� ��������� ���������: 
	 *    CardPortlet.ATTR_ID_FIELD: ������ ���� ����������� � ��������� 
	 * ��������� ��� backlink-�������� (��� �������) � ����������� ��������, 
	 * ������� ������ � ����-��� link-��������� �������� ��������, � ������ 
	 * �������� ��� �������� (��� ����� ����������� ��������� ��������) �������.
	 *    CardPortlet.PARAM_DOCLINK_TEMPLATE: ������ ���� ����������� � ��������� 
	 * �������� �������� id ������� ������������ ���������� ���������;
	 *    CardPortlet.PARAM_DOCLINK_TYPE: �������� �������� id ���� ������������ 
	 * ���������, ��� ������ ����� cardlink-������� (��������������� �������� 
	 * back-�������� �����) ����� ��� TypedCardLinkAttribute. 
	 * @param response ����� �� ������������.
	 * @throws DataException
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void process( List cardIds, ActionRequest request,
			ActionResponse response ) throws DataException 
	{
		/*
		 * ����� ��������� �� �������...
		 */
		getParamsFromRequest(request);

		/*
		 * �������� ���������� ���������...
		 */
		try {
			final CardPortletSessionBean bean = CardPortlet.getSessionBean(request);

			// �������� �������� ������� �������� ��������...
			final CardPortletCardInfo cardInfo = bean.getActiveCardInfo();
			if (cardInfo.getCard() == null || cardInfo.getCard().getId() == null)
				// jbr.card.move.orphan=���������� �������� ������ ��������, ���� �� �������� ��������-���������
				throw new DataException("jbr.card.move.orphan");
			final ObjectId mainCardId = cardInfo.getCard().getId();

			/*
			 * backlink-������� �������� ��������...
			 */
			final BackLinkAttribute mainBackLinkAttr = (BackLinkAttribute)
					chkGetAttr("process() backlink", cardInfo.getCard(), this.attrBackLinkId);
			if (mainBackLinkAttr == null)
				throw new DataException("jbr.processor.nodestattr_2", new Object[] {
						"Doclink process()", this.attrBackLinkId});

			// �������� ����� �������� �� ������� � �� �������������� ...
			/*
			  ���� ��� ����� - "� ����� ��..." ���������� ������ �������� � �������� ����� ��� ��������������� ������������ ��������� ���������
			*/
			final Card newCard = CardPortlet.createCard( bean, templateId, inReplyTo.getId().equals(linkTypeId) ? mainCardId : null); 
			logger.info("Card created "+ ((newCard != null) ? newCard.getId() : "null"));
			final CardPortletCardInfo newCardInfo = bean.getActiveCardInfo();

			/* (!) bug overtrap
			 *  �������� ��� ����� �������� ��� � ����� � �������� ����������...
			 *  �.�. ��� �������� ������ backlink, �������� linkSource() (������-��!?)
			 *  ������ CardLink (���� ������ ����� ���� � typedCardLink), ��
			 *  ������� ��������� ��� ��������...
			 */
			ObjectId linkedId = mainBackLinkAttr.getLinkSource();
			Collection<ObjectId> linkedIds = mainBackLinkAttr.getLinkSources();
			CardLinkAttribute cardLinkAttr = (CardLinkAttribute)
				chkGetAttr( "process() linked", newCardInfo.getCard(), linkedId);
			if (cardLinkAttr == null) {
				// ������� ���� �������� ��� TypedCardLink...
				linkedId = new ObjectId( TypedCardLinkAttribute.class, mainBackLinkAttr.getLinkSource().getId());
				cardLinkAttr = (CardLinkAttribute)
					chkGetAttr( "process() linked", newCardInfo.getCard(), linkedId);
			}

			if (cardLinkAttr == null){ 
				// ���� ���� �� ������, ������� �������� �� ������ ������, � ���� ��� ������ �� ����� �������, �� ����� ������� exception
				if (linkedIds!=null){
					for(ObjectId nextLinkedId: linkedIds){
						cardLinkAttr = (CardLinkAttribute)
							chkGetAttr( "process() linked", newCardInfo.getCard(), linkedId);
						if (cardLinkAttr == null) {
							// ������� ���� �������� ��� TypedCardLink...
							linkedId = new ObjectId( TypedCardLinkAttribute.class, nextLinkedId.getId());
							cardLinkAttr = (CardLinkAttribute)chkGetAttr( "process() linked", newCardInfo.getCard(), nextLinkedId);
						}
						if (cardLinkAttr != null) 
							break;
					}
				}
			}
			if (cardLinkAttr == null){ 
				throw new DataException("jbr.processor.nodestattr_2", new Object[] {
						"Doclink process linked", linkedId
						});
			}
			if (cardLinkAttr.isMultiValued())
				cardLinkAttr.addLinkedId(mainCardId);
			else // �������� ������ ���� ��������
				cardLinkAttr.addSingleLinkedId(mainCardId);

			// ������� ���� ����� � ����� �������� ...
			if (cardLinkAttr instanceof TypedCardLinkAttribute)
				((TypedCardLinkAttribute) cardLinkAttr).addType( 
						(Long) mainCardId.getId(), this.linkTypeId);

			newCardInfo.setCloseHandler( new LinkedCardCloseHandler(
					linkedId, this.linkTypeId));

			// �������� � ������ �������� �������� ����� ����� ������ ����� 
			// ���������� ����� ��������, ��� ��� ����� ��������� closeHandler,
			// ������� � �������� ��� ����� �������� ��������� ��������...

			// previousCardInfo.setAttributeEditorData(attrDocLinkId, AttributeEditor.KEY_VALUE_CHANGED, Boolean.TRUE);

		} catch (ServiceException ex) {
			// ex.printStackTrace();
			throw new DataException( "com.aplana.dbmi.service.impl.query.DoNewCard", ex);
		}
	}

	/**
	 * �������� ������� � ��������, ������������� ���������� ��������.
	 * @param infoMethod: �������� ������ ������ ��� �������������� ��� ���������� ��������.
	 * @param card
	 * @param attrId
	 * @return ������� ��� NULL ���� ��� ��� � ��������.
	 */
	Attribute chkGetAttr( String infoMethod, Card card, ObjectId attrId)
	{
		if (card == null || attrId == null) 
			return null;

		final Attribute result = card.getAttributeById(attrId); 
		if (result == null) {
			System.err.append( MessageFormat.format( 
					"Error at {0}.{1}:: attribute ''{2}'' not exists in card {3}", 
					new Object[] { 
							this.getClass().getName(),	infoMethod,
							this.attrBackLinkId,		card.getId()
					} ));
		}
		return result;
	}

	/**
	 * ��� �������� ��������� �������� ���� "���������" �� � ������� ��������
	 * � ������ �������� ����� (this.attrLinkCode).
	 * @author RAbdullin
	 */
	static class LinkedCardCloseHandler implements CardPortletCardInfo.CloseHandler 
	{
		// id �������� ��� ������ ������ �� ��������� ��������...
		final ObjectId attrDocLinkId;
		final Long docLinkType;

		/**
		 * @param attrLinkCode
		 */
		public LinkedCardCloseHandler(ObjectId attrLinkId, Long linkType) {
			this.attrDocLinkId = attrLinkId;
			this.docLinkType = linkType;
		}

		public void afterClose( CardPortletCardInfo closedCardInfo,
				CardPortletCardInfo previousCardInfo)
		{
			if (previousCardInfo == null) return;
			final Card newCard = closedCardInfo.getCard();
			final ObjectId newCardId = newCard.getId();
			final Card prevCard = previousCardInfo.getCard();

			if (newCardId != null && prevCard != null) 
			{
				previousCardInfo.setRefreshRequired(true);

//				final CardLinkAttribute attr = prevCard.getCardLinkAttributeById(attrDocLinkId);
//				if (attr == null)  {
//					System.err.append( MessageFormat.format( "Error at {0}.afterClose():: attribute ''{1}'' not exists in card {2}", 
//							new Object[] { 
//								this.getClass().getName(), 
//								attrDocLinkId,
//								prevCard.getId()
//							} ));
//					return;
//				}
//				if (attr.isMultiValued())
//					attr.addLinkedId(newCardId);
//				else // �������� ������ ���� ��������
//					attr.addSingleLinkedId(newCardId);
//
//				// ������� ���� �����...
//				if (attr instanceof TypedCardLinkAttribute)
//					((TypedCardLinkAttribute) attr).addType( (Long) newCard.getId().getId(), docLinkType);
//
//				previousCardInfo.setAttributeEditorData(attrDocLinkId, AttributeEditor.KEY_VALUE_CHANGED, Boolean.TRUE);
			}
		}
	}

}