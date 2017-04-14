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

import java.util.Collection;
import java.util.Iterator;

import javax.portlet.PortletRequest;

import com.aplana.dbmi.card.CardPortletSessionBean.PortletMessage.PortletMessageType;
import com.aplana.dbmi.card.FilesAndCommentsUtils.RoundDataFiles;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.ServiceException;

public class FilesAndCommentsAttributeViewer extends CardLinkAttributeViewer {
	
	ObjectId fileLinkAttributeId;
	boolean hideWhenDSP = false;
	boolean hideChildren = false;

	public FilesAndCommentsAttributeViewer() {
		setParameter(PARAM_JSP, "/WEB-INF/jsp/html/attr/FilesAndCommentsView.jsp");				
	}

	@Override
	public void initEditor(PortletRequest request, Attribute attr) throws DataException{
		super.initEditor(request, attr);
		CardPortlet.getSessionBean(request).getActiveCardInfo().setAttributeEditorData(attr.getId(), FilesAndCommentsUtils.PARAM_FILE_LINK, fileLinkAttributeId);
	}
	
	@Override
	public void loadAttributeValues(Attribute attr, PortletRequest request) {
		
		CardPortletSessionBean sessionBean = CardPortlet.getSessionBean(request);
		CardPortletCardInfo cardInfo = sessionBean.getActiveCardInfo();	
		Card documentCard = cardInfo.getCard();

		
		FilesAndCommentsUtils utils = new FilesAndCommentsUtils(documentCard, attr, sessionBean.getServiceBean());
		if (!utils.isRoundExists()) {
			super.loadAttributeValues(attr, request);
		}
		else {
			if (utils.getCurrentRound() == 0 || hideChildren) {
				super.loadAttributeValues(attr, request);
				@SuppressWarnings("unchecked")
				Collection<Card> cards = (Collection<Card>) cardInfo.getAttributeEditorData(attr.getId(), LinkedCardUtils.ATTR_LINK_CARDS_LIST);
				Iterator<Card> i = cards.iterator();
				while(i.hasNext()){
					Card c = i.next();
					try{
						sessionBean.getServiceBean().getById(c.getId());
					} catch (DataException e){
						i.remove();
					} catch (ServiceException e) {
						logger.error(e);
					}
				}
			}
			else {
				try {
					RoundDataFiles roundDataFiles = utils.loadLinkedData((hideWhenDSP && utils.isDSP(documentCard)));
					saveRoundDataFiles(roundDataFiles, cardInfo, attr);					
				} catch (Exception e) {
					if (sessionBean != null)
						sessionBean.setMessageWithType("edit.link.error.create", new Object[] { e.getMessage() }, PortletMessageType.ERROR);
				}
			}
		}
	}
	
	/**
	 * ���������� ���������� � ��������� � cardInfo.
	 * @param roundDataFiles
	 * @param cardInfo
	 * @param attr
	 */
	private void saveRoundDataFiles(RoundDataFiles roundDataFiles, CardPortletCardInfo cardInfo, Attribute attr){
		cardInfo.setAttributeEditorData(attr.getId(), FilesAndCommentsUtils.ROUND_DATA_ARRAY, roundDataFiles.getRoundDatas());
		cardInfo.setAttributeEditorData(attr.getId(), LinkedCardUtils.ATTR_LINK_COLUMNS_LIST, roundDataFiles.getColumns());
	}
	
	@Override
	public void setParameter(String name, String value){
		if(name.equalsIgnoreCase(FilesAndCommentsUtils.PARAM_FILE_LINK)) fileLinkAttributeId = ObjectIdUtils.getAttrObjectId(value, ":");
		else if(name.equalsIgnoreCase(FilesAndCommentsUtils.PARAM_HIDE_WHEN_DSP)) hideWhenDSP = Boolean.parseBoolean(value);
		else if (name.equalsIgnoreCase(FilesAndCommentsUtils.PARAM_HIDE_CHILDREN))	hideChildren = Boolean.parseBoolean(value);
		else super.setParameter(name, value);
	}
	
}
