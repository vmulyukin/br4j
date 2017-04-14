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
package com.aplana.util;

import java.util.ArrayList;
import java.util.List;

import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.action.SignCard;
import com.aplana.dbmi.action.ValidateMandatoryAttributes;
import com.aplana.dbmi.card.CardPortlet;
import com.aplana.dbmi.crypto.SignatureConfig;
import com.aplana.dbmi.crypto.SignatureData;
import com.aplana.dbmi.jbr.processors.GetAttachments;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.HtmlAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.ServiceException;

/**
 * Utility class used to perform commonly used actions with signing by digital signature.
 * 
 * @author EStatkevich
 */
public class DigitalSignatureUtil {
	
	public static final ObjectId ATTR_SIGNATURE = ObjectId.predefined(HtmlAttribute.class, "jbr.uzdo.signature");
	
	/**
	 * Checks if current user has certificate.
	 * 
	 * @param dataServiceBean
	 * @return
	 * @throws DataException
	 * @throws ServiceException
	 */
	public static boolean isDsSupport(DataServiceBean dataServiceBean) throws DataException, ServiceException {
		Card persCard = null;
		Person pers = null;
		boolean dsSupport = false;

		Search action = new Search();
		dataServiceBean.canDo(action); // ��� ������������� ������������ ������ DS

		pers = dataServiceBean.getPerson();
		if (pers != null && pers.getCardId() != null) {

			ObjectId id = pers.getCardId();

			persCard = (Card) dataServiceBean.getById(id);
			CardLinkAttribute attrHash = (CardLinkAttribute) persCard.getAttributeById(SignatureData.actualCertificateAttrId);
			if (attrHash != null) {
				ObjectId[] certs = attrHash.getIdsArray();
				if (certs != null && certs.length > 0) {
					dsSupport = true;
				}
			}
		}
		return dsSupport;
	}
	
	/**
	 * @param dataServiceBean
	 * @param card
	 * @param signAttachments
	 * @return
	 * @throws DataException
	 * @throws ServiceException
	 */
	public static ArrayList<String> prepareSignatureParams(DataServiceBean dataServiceBean, Card card, boolean signAttachments, String materialBaseLink) throws DataException, ServiceException {
		//������������ ������ �������� ����������. ���������� ����� ����� ��������� �������� ��� �������
		ArrayList<String[]> params = new ArrayList<String[]>();
		//��������� ��������� ��� ���������� ��������� 
		params.add(prepareCardParams(dataServiceBean, card, materialBaseLink));
		//��������� ��������� ��� ���������� ��������
		if (signAttachments) {
			GetAttachments action = new GetAttachments();
			action.setCardId(card.getId());
			action.setOnlyFromLastIteration(true); //����������� ������ �������� � ��������� ��������
			List<Card> attachments = (List<Card>) dataServiceBean.doAction(action);
			if (attachments.size() > 0) {
				for (int i = 0; i < attachments.size(); i++) {
					Card attachmentCard = attachments.get(i);
					params.add(prepareCardParams(dataServiceBean, attachmentCard, materialBaseLink));
				}
			}
		}
		
		return concatParams(params);
	}

	/**
	 * @param dataServiceBean
	 * @param card
	 * @return String[]
	 */
	private static String[] prepareCardParams(DataServiceBean dataServiceBean, Card card, String materialBaseLink) {
		SignatureConfig sConf = new SignatureConfig(dataServiceBean, card);
		SignatureData sData = new SignatureData(sConf, card);
		HtmlAttribute signatureAttribute = (HtmlAttribute) card.getAttributeById(ATTR_SIGNATURE);
		
		return new String[] {
				sData.getAttrValues(dataServiceBean, false, materialBaseLink),
				sData.getAttrValues(dataServiceBean, true, null),
				sData.getAttrXML(),
				(signatureAttribute==null?null:signatureAttribute.getStringValue()),
				card.getId().getId().toString() };
	}
	
	/**
	 * Concatenates parameters for signature from different cards. 
	 * 
	 * @param params
	 * @return ArrayList<String>
	 */
	private static ArrayList<String> concatParams(ArrayList<String[]> params) {
	
		StringBuffer ids = new StringBuffer();
		StringBuffer attrsToSign = new StringBuffer();
		StringBuffer hashesToSign = new StringBuffer();
		StringBuffer attrXmls = new StringBuffer();
		StringBuffer currentSigns = new StringBuffer();
		for (int i = 0; i < params.size(); i++) {
			if (i > 0) {
				ids.append(",");
				attrsToSign.append(",");
				hashesToSign.append(",");
				attrXmls.append(",");
				currentSigns.append(",");
			}
			attrsToSign.append("\"").append(params.get(i)[0]).append("\"");
			hashesToSign.append("\"").append(params.get(i)[1]).append("\"");
			attrXmls.append("'").append(params.get(i)[2]).append("'");
			currentSigns.append("'").append(params.get(i)[3]).append("'");
			ids.append("\"").append(params.get(i)[4]).append("\"");
		}

		ArrayList<String> concatenatedParams = new ArrayList<String>();
		concatenatedParams.add(attrsToSign.toString());
		concatenatedParams.add(hashesToSign.toString());
		concatenatedParams.add(attrXmls.toString());
		concatenatedParams.add(currentSigns.toString());
		concatenatedParams.add(ids.toString());

		return concatenatedParams;
	}	
	
	/**
	 * Fills signature attribute of the card with template 'signature' (also it's attachments) with generated value and stores it in database.
	 * 
	 * @param request
	 * @param card
	 * @param dataServiceBean
	 * @param signBaseCard
	 * @throws DataException
	 * @throws ServiceException
	 */
	public static void storeDigitalSignature(String signatureParam, Card card, DataServiceBean dataServiceBean, boolean signBaseCard) throws DataException, ServiceException {

		// �������� ����� ���� �����
		String[] signatures = signatureParam.split("###");
		// �� ������ ���� ����������� ��������������� ��������
		long currentCardId = card == null ? -1 : ((Long) card.getId().getId()).longValue();
		for (int i = 0; i < signatures.length; i++) {
			// �� ��������� id ��������, ����� ��������������� �������
			String[] signatureArr = signatures[i].split("::");
			long id = Long.parseLong(signatureArr[0]);
			String signature = signatureArr[1];
			Card signedCard = null;

			if (id == currentCardId) {
				signedCard = card;

				if (signBaseCard) {
					HtmlAttribute signatureAttribute = (HtmlAttribute) card.getAttributeById(ATTR_SIGNATURE);
					signatureAttribute.setValue(signature);
				}

				ValidateMandatoryAttributes validationAction = new ValidateMandatoryAttributes();
				validationAction.setCard(card);
				dataServiceBean.doAction(validationAction);
			} else {
				signedCard = (Card) dataServiceBean.getById(new ObjectId(Card.class, id));
				HtmlAttribute attachmentSignatureAttribute = (HtmlAttribute) signedCard.getAttributeById(ATTR_SIGNATURE);
				attachmentSignatureAttribute.setValue(signature);
			}

			SignCard signCardAction = new SignCard();
			signCardAction.setCard(signedCard);
			dataServiceBean.doAction(signCardAction);
		}
	}
	
}
