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
package com.aplana.dbmi.crypto;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aplana.dbmi.card.CertificateInfo;
import com.aplana.dbmi.model.BackLinkAttribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.StringAttribute;
import com.aplana.dbmi.model.util.SearchUtils;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.ServiceException;

public class CardDsInfoReader {
	
	public static final String CERTIFICATES_INFO_ATTR = "CERTIFICATES_INFO";
	public static final String VISA_ATTR = "VISA_CERT_INFO";
	public static final String SIGNER_ATTR = "SIGNER_CERT_INFO";
	public static final String EXAMINER_ATTR = "EXAMINER_CERT_INFO";
	public static final String REPORT_ATTR = "REPORT_CERT_INFO";
	public static final String AUTHOR_ATTR = "AUTHOR_CERT_INFO";
	public static final String FILES_ATTR = "FILES_CERT_INFO";
	public static final String ERROR_MESSAGE_ATTR = "ERROR_MESSAGE";
	public static final String R_HEADER = "HEADER";
	public static final String R_VALUE = "VALUE";
	
	private static final ObjectId VISA_ATTR_ID = ObjectId.predefined(CardLinkAttribute.class, "jbr.visa.set");
	private static final ObjectId SIGNER_ATTR_ID = ObjectId.predefined(CardLinkAttribute.class, "jbr.sign.set");
	private static final ObjectId EXAMINER_ATTR_ID = ObjectId.predefined(CardLinkAttribute.class, "jbr.examby");
	private static final ObjectId RESOLUTION_ATTR_ID = ObjectId.predefined(BackLinkAttribute.class, "jbr.resolutions");
	private static final ObjectId REPORT_ATTR_ID = ObjectId.predefined(BackLinkAttribute.class, "jbr.reports");
	private static final ObjectId FILES_ATTR_ID = ObjectId.predefined(CardLinkAttribute.class, "jbr.files");
	private static final ObjectId FILENAME_ATTR_ID = ObjectId.predefined(StringAttribute.class, "jbr.materialName");
	private static final ObjectId CARD_NAME_ATTR_ID = ObjectId.predefined(StringAttribute.class, "name");
	
	private Log logger = LogFactory.getLog(getClass().getSimpleName());
	
	private DataServiceBean serviceBean = null;
	private Card activeCard = null;
	private ResourceBundle bundle = null;
	
	
	
	private CardDsInfoReader(DataServiceBean serviceBean, Card activeCard, ResourceBundle bundle){
		this.serviceBean=serviceBean;
		this.activeCard = activeCard;
		this.bundle=bundle;
	}
	
	
	public static CardDsInfoReader getInstance(DataServiceBean serviceBean, Card activeCard, ResourceBundle bundle){
		CardDsInfoReader cardDsInfo = new CardDsInfoReader(serviceBean, activeCard, bundle);
		return cardDsInfo;		
	}
	
	
	public Map getCardDsInfo() throws DataException{
		Map result = new HashMap();
		
		DateFormat dateFormat = DateFormat.getDateTimeInstance(SimpleDateFormat.MEDIUM, SimpleDateFormat.MEDIUM, Locale.getDefault());

		try {
			/*Integer sessionId = serviceBean.getSessionId();
			serviceBean = new DataServiceBean();
			serviceBean.setAddress(request.getRemoteAddr());
			serviceBean.setUser(new UserPrincipal("__system__"));
			serviceBean.setSessionId(sessionId);*/

			//Map result = new HashMap();
			//request.setAttribute(CERTIFICATES_INFO_ATTR, result);

			//��������� ������� ��� �������� ����
			List<CertificateInfo> certInfo = CertificateInfo.readCertificateInfo(
					activeCard, serviceBean, bundle, dateFormat);
			if(null != certInfo && certInfo.size()>0) {
				List certificatesInfo = new ArrayList();
				//certificatesInfo.addAll(certInfo);
				Map tableRecord = new HashMap();
				tableRecord.put(R_HEADER, activeCard.getAttributeById(CARD_NAME_ATTR_ID).getStringValue());
				tableRecord.put(R_VALUE, certInfo);
				certificatesInfo.add(tableRecord);
				result.put(AUTHOR_ATTR, certificatesInfo);
			}

			//���� �� ����������
			CardLinkAttribute filesAttribute = (CardLinkAttribute) activeCard.getAttributeById(FILES_ATTR_ID);
			fillCertificationInfo(result, FILES_ATTR, filesAttribute, FILENAME_ATTR_ID ,serviceBean, bundle, dateFormat);

			CardLinkAttribute visaAttribute = (CardLinkAttribute) activeCard.getAttributeById(VISA_ATTR_ID);
			fillCertificationInfo(result, VISA_ATTR, visaAttribute, null,serviceBean, bundle, dateFormat);

			CardLinkAttribute signerAttribute = (CardLinkAttribute) activeCard.getAttributeById(SIGNER_ATTR_ID);
			fillCertificationInfo(result, SIGNER_ATTR, signerAttribute, null,serviceBean, bundle, dateFormat);

			CardLinkAttribute examinerAttribute = (CardLinkAttribute) activeCard.getAttributeById(EXAMINER_ATTR_ID);
			fillCertificationInfo(result, EXAMINER_ATTR, examinerAttribute, null,serviceBean, bundle, dateFormat);

			Collection<ObjectId> cardIds = SearchUtils.getBackLinkedCardsObjectIds(activeCard, RESOLUTION_ATTR_ID, serviceBean);
			if(null != cardIds && !cardIds.isEmpty()) {
				Iterator iterator = cardIds.iterator();
				while(iterator.hasNext()) {
					ObjectId resolutionId = (ObjectId) iterator.next();
					Card resolutionCard = (Card) serviceBean.getById(resolutionId);
					Collection<ObjectId> reportIds = SearchUtils.getBackLinkedCardsObjectIds(resolutionCard, REPORT_ATTR_ID, serviceBean);
					fillCertificationInfo(result, REPORT_ATTR, reportIds, null, serviceBean, bundle, dateFormat);

					Collection<ObjectId> resolution2Ids = SearchUtils.getBackLinkedCardsObjectIds(resolutionCard, RESOLUTION_ATTR_ID, serviceBean);
					if(null != resolution2Ids && !resolution2Ids.isEmpty()) {
						Iterator iterator2 = resolution2Ids.iterator();
						while(iterator2.hasNext()) {
							ObjectId resolution2Id = (ObjectId) iterator.next();
							Card resolution2Card = (Card) serviceBean.getById(resolution2Id);
							Collection<ObjectId> report2Ids = SearchUtils.getBackLinkedCardsObjectIds(resolution2Card, REPORT_ATTR_ID, serviceBean);
							fillCertificationInfo(result, REPORT_ATTR, report2Ids, null, serviceBean, bundle, dateFormat);
						}
					}
				}
			}
		} catch (ServiceException e) {
			logger.error(e);
			throw new DataException("db.side.error.msg",e);
			
			
		} catch (DataException e) {
			logger.error(e);
			throw new DataException("db.side.error.msg",e);			
		}

		
		
		return result;
	}
	
	
	private void fillCertificationInfo(Map result, String attributeName, Collection<ObjectId> linkedIds, ObjectId headerAttribute,
			DataServiceBean serviceBean, ResourceBundle bundle, DateFormat dateFormat) throws DataException, ServiceException {
		
		List certificatesInfo = (List) result.get(attributeName);
		if(null == certificatesInfo) {
			certificatesInfo = new ArrayList();
			result.put(attributeName, certificatesInfo);
		}

		Iterator iterator = linkedIds.iterator();
		while(iterator.hasNext()) {
			ObjectId cardId = (ObjectId) iterator.next();
			Card card = (Card) serviceBean.getById(cardId);
			List<CertificateInfo> certInfo = CertificateInfo.readCertificateInfo(card, serviceBean, bundle, dateFormat);
			if(certInfo.size()>0){
				Map tableRecord = new HashMap();
				if(headerAttribute!=null){
					tableRecord.put(R_HEADER, card.getAttributeById(headerAttribute).getStringValue());
				}
				tableRecord.put(R_VALUE, certInfo);
				if(null != certInfo) {
					certificatesInfo.add(tableRecord);
				}
			}
		}
	}
	
	private void fillCertificationInfo(Map result, String attributeName, CardLinkAttribute cardLinkAttribute, ObjectId headerAttribute,
			DataServiceBean serviceBean, ResourceBundle bundle, DateFormat dateFormat) throws DataException, ServiceException {
		
		if(null == cardLinkAttribute) {
			return;
		}

		Collection<ObjectId> linkedIds = cardLinkAttribute.getIdsLinked();
		if(null == linkedIds || linkedIds.isEmpty()) {
			return;
		}
		
		fillCertificationInfo(result, attributeName, linkedIds, headerAttribute,
				serviceBean, bundle, dateFormat);
	}

}
