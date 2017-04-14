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
package com.aplana.dbmi.jbr.processors;

import java.security.cert.X509Certificate;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.aplana.dbmi.card.CertificateInfo;
import padeg.lib.Padeg;

import com.aplana.crypto.CryptoLayer;
import com.aplana.dbmi.action.Action;
import com.aplana.dbmi.action.ChangeState;
import com.aplana.dbmi.action.CreateCard;
import com.aplana.dbmi.action.LockObject;
import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.action.Search.TextSearchConfigValue;
import com.aplana.dbmi.action.UnlockObject;
import com.aplana.dbmi.action.file.CopyMaterialWithStamp;
import com.aplana.dbmi.crypto.SignatureData;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.CardState;
import com.aplana.dbmi.model.DataObject;
import com.aplana.dbmi.model.DateAttribute;
import com.aplana.dbmi.model.HtmlAttribute;
import com.aplana.dbmi.model.IntegerAttribute;
import com.aplana.dbmi.model.ListAttribute;
import com.aplana.dbmi.model.MaterialAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.model.PersonAttribute;
import com.aplana.dbmi.model.ReferenceValue;
import com.aplana.dbmi.model.Stamp;
import com.aplana.dbmi.model.StringAttribute;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.model.TextAttribute;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ActionQueryBase;
import com.aplana.dbmi.service.impl.ObjectQueryBase;
import com.aplana.dbmi.service.impl.UserData;

/**
 * ��������� ��� ��������� ��������������� ������ �� ����� PDF �������� � ��������� ��������� ��� �������� � ������ ���������������
 *
 * @author Vlad Alexandrov
 * @version 1.0
 * @since   2014-03-27
 */

public class AddRegStamp extends ProcessCard {

	private static final ObjectId ACTIVE_STATE_ID = ObjectId.predefined(CardState.class, "active");
	private static final ObjectId ATTR_DOCLINKS_ID = ObjectId.predefined(CardLinkAttribute.class, "jbr.files");
	private static final ObjectId BLANK_TYPE_CARD_ID = ObjectId.predefined(CardLinkAttribute.class, "jbr.person.blank");
	private static final ObjectId EXECUTOR_ID = ObjectId.predefined(PersonAttribute.class, "jbr.document.executor");
	private static final ObjectId MATERIAL_NAME_ID = ObjectId.predefined(StringAttribute.class, "jbr.materialName");
	private static final ObjectId PRIMACY_ID = ObjectId.predefined(ListAttribute.class, "jbr.prime");
	private static final ObjectId REGNUM_ID = ObjectId.predefined(StringAttribute.class, "regnumber");
	private static final ObjectId REGDATE_ID = ObjectId.predefined(DateAttribute.class, "regdate");
	private static final ObjectId SIGNATORY_ID = ObjectId.predefined(PersonAttribute.class, "jbr.outcoming.signatory");
	private static final ObjectId FACT_SIGNATORY_ID = ObjectId.predefined(PersonAttribute.class, "jbr.sign.actual_signer");
	private static final ObjectId TEMPLATE_FILE = ObjectId.predefined(Template.class, "jbr.file");
	
	private static final ObjectId YES_ID = ObjectId.predefined(ReferenceValue.class, "jbr.incoming.control.yes");
	private static final ObjectId YES_ID_2 = ObjectId.predefined(ReferenceValue.class, "jbr.commission.control.yes");
	private static final ObjectId NO_ID = ObjectId.predefined(ReferenceValue.class, "jbr.incoming.control.no");

	private static final ObjectId XPos_ID = ObjectId.predefined(IntegerAttribute.class, "jbr.blank.type.x");
	private static final ObjectId YPos_ID = ObjectId.predefined(IntegerAttribute.class, "jbr.blank.type.y");
	
    public static final ObjectId DOC_TYPE = ObjectId.predefined(CardLinkAttribute.class, "jbr.reg.doctype");
    
	private static final ObjectId ATTR_SIGNER_ATTR_ID = ObjectId.predefined(CardLinkAttribute.class, "jbr.sign.set");
	private static final ObjectId SIGNED_STATE_ID = ObjectId.predefined(CardState.class, "jbr.sign.signed");
	private static final ObjectId SIGN_PERSON_ID = ObjectId.predefined(PersonAttribute.class, "jbr.sign.person");
	private static final ObjectId ATTR_PERS_ORG_ATTR_ID = ObjectId.predefined(CardLinkAttribute.class, "jbr.incoming.organization");
	private static final ObjectId PERSON_LAST_NAME = ObjectId.predefined(StringAttribute.class, "jbr.person.lastName");
	private static final ObjectId PERSON_FIRST_NAME = ObjectId.predefined(StringAttribute.class, "jbr.person.firstName");
	private static final ObjectId PERSON_MIDDLE_NAME = ObjectId.predefined(StringAttribute.class, "jbr.person.middleName");
	public static final ObjectId SIGNATURE_ATTR_ID = ObjectId.predefined(HtmlAttribute.class, "jbr.uzdo.signature");
	private static final ObjectId PERSON_ALL_CERTS_ATTR_ID = ObjectId.predefined(CardLinkAttribute.class, "jbr.person.certificates");
	private static final ObjectId CERT_HASH_ATTR_ID = ObjectId.predefined(StringAttribute.class, "jbr.certificate.certhash");
	private static final ObjectId ORG_FULL_NAME_ATTR_ID = ObjectId.predefined(TextAttribute.class, "jbr.organization.fullName");
	
	private static final ObjectId SOURCE_FILE_FOR_STAMP_ATTR_ID = ObjectId.predefined(ListAttribute.class, "jbr.sourceFileForStamp");

	private static final int PREPOSITIONAL_PADEG = 6;

	private static final String signMarkTemplate = "(�������� ������ � ����������� ����� � {0})";
    public static final String telegramId = "����������";

	private Card document = null;
	private Card signPersonCard;
	private Card actualSignPersonCard;

	@Override
	public Object process() throws DataException
	{
		document = getCard();
		//�������� �������� �� ����, �.�. �������� ����� ���� �������
		ObjectQueryBase query = getQueryFactory().getFetchQuery(Card.class);
		query.setId(document.getId());
		document = (Card)getDatabase().executeQuery(getSystemUser(), query);
		//�������� �������� �������� ��� ����������� (������� ������� ���� '�������� ��������' � ����)
		Card fileCardToCopy = getFileCardToCopy();
		if(fileCardToCopy == null)
			return null;
		
		execAction( new LockObject(document.getId()));
		
		try
		{
			//���������� ��� ���������
			CardLinkAttribute docType = document.getAttributeById(DOC_TYPE);
			StringAttribute docTypeName = null;
			if (null != docType) {
				query.setId(docType.getSingleLinkedId());
				Card docTypeCard = (Card)getDatabase().executeQuery(getSystemUser(), query);
				docTypeName = docTypeCard.getAttributeById(Attribute.ID_NAME);
			}
			//��������� ���� ������
			final Stamp stamp = new Stamp();
			final Stamp.RegistrationData regData = new Stamp.RegistrationData();
			if (null != docTypeName && docTypeName.getStringValue().equals(telegramId)) {
				// ���� ���� ��������� = �����������, �� ��� ����������� �� ����, 
				// ����� ���� ������ ������ ��� ������� �� ���� ���� ����������,
				// ������ ����� � ���.������� ����� ������ �������� ���������.
				regData.setMainStamp(false);
				regData.setBottomStamp(true);
				regData.setRegDate(this.getRegDate());
				regData.setRegNum(this.getRegNum());
			}else {
				// ��� ��������� ���������� ����������� ����� � ���.������� �� ������ �������� � ����������,
				// ������� ������������ �� ������ �� ���� ���������� ������ �� �������� ���� ������ �� �� �������, 
				// ��������� � ���� "��� ����������" ���������� ���������.
				final Card blankTypeCard = getBlankTypeCard();
				IntegerAttribute XPosAttr = (IntegerAttribute)blankTypeCard.getAttributeById(XPos_ID);
				IntegerAttribute YPosAttr = (IntegerAttribute)blankTypeCard.getAttributeById(YPos_ID);
				regData.setMainStamp(true);
				regData.setBottomStamp(false);
				regData.setRegDate(this.getRegDate());
				regData.setRegNum(this.getRegNum());
				regData.setXPosMM(XPosAttr.getValue());
				regData.setYPosMM(YPosAttr.getValue());

				//--------------------
				//����������� ����� �� �/��� ������� � �������� ������������ ��������� ����� � ��������
				//�������� ������ �� ������ �������� ������� � ������� "���������"
				final Card signCard = getFirstLinkedCardByAttrAndState(document, 
						ATTR_SIGNER_ATTR_ID, SIGNED_STATE_ID);
				
				final Stamp.SignatureData sigData = new Stamp.SignatureData();
				boolean markOnly = false;
				if(null == signCard) {
					//�������� �������� ���������� �� �������� "��� ����������"
					signPersonCard = this.getSignPersonCard(this.document, SIGNATORY_ID);
					if(null == signPersonCard) {
						logger.error( "Signatory user not found in card: " 
								+ this.document.getId().getId().toString()
								+ " -> aborting registration");
						throw new DataException("jbr.processor.stamp.nosiguser2",
								new Object[] {signCard.getId().getId().toString()});
					}
					sigData.setLocationString(getSignature(signPersonCard));
					markOnly = true;
				} else {
					//�������� �������� ���������� �� �������� "�������"
					signPersonCard = this.getSignPersonCard(signCard, SIGN_PERSON_ID);
					if(!signCard.getAttributeById(SIGN_PERSON_ID).
							equalValue(signCard.getAttributeById(FACT_SIGNATORY_ID))) {
						actualSignPersonCard = this.getSignPersonCard(signCard, FACT_SIGNATORY_ID);
					} else {
						actualSignPersonCard = signPersonCard;
					}
					if(signPersonCard == null || actualSignPersonCard == null) {
						logger.error( "Signatory user not found in sign card: " 
								+ signCard.getId().getId().toString()
								+ " -> aborting registration");
						throw new DataException("jbr.processor.stamp.nosiguser",
								new Object[] {signCard.getId().getId().toString()});
					}
					sigData.setLocationString(getSignature(signPersonCard));
		
					//�������� ������ � ����������� ����������:
					// -- �� �������� "�������"
					final SignatureData signerSignData = getLastSignData(signCard);
					
					if (null == signerSignData)
						markOnly = true;
		
					// -- �� �������� "����"
					final SignatureData fileSignData = getLastSignData(fileCardToCopy);
					
					if (null == fileSignData) {
						markOnly = true;
					}
	
					if(!markOnly){
						// �������� X509 ���������� ����������
						final X509Certificate cert = getCert509ByHash(fileSignData.getCertHash());
						if (null == cert) {
							logger.error( "No X509 Certificate found for Signatory user by hash " 
									+ fileSignData.getCertHash() + " -> aborting registration");
							throw new DataException("jbr.processor.stamp.nocert", 
									new Object[] {getSignatoryFIO()});
						}
						final DateFormat df = new SimpleDateFormat("dd.MM.yyyy");

						sigData.setSerial(cert.getSerialNumber().toString());
						sigData.setOwner(CertificateInfo.getCertInfoCommonName(cert.getSubjectDN().getName()));
						sigData.setNotBefore(df.format(cert.getNotBefore()));
						sigData.setNotAfter(df.format(cert.getNotAfter()));
						if(getPrimaryQuery() instanceof ActionQueryBase){
							Action primaryAction = ((ActionQueryBase)getPrimaryQuery()).getAction();
							if(primaryAction instanceof ChangeState){
								sigData.setPosition((String)((ChangeState)primaryAction).getParameter("stampPosition"));
							}
						}
					}
				}
				// �������� ������������ ����������� ����������
				CardLinkAttribute persOrgAttr = signPersonCard.getCardLinkAttributeById(ATTR_PERS_ORG_ATTR_ID);
				Card persOrgCard = loadCardById(persOrgAttr.getSingleLinkedId(), getSystemUser());
				if (null == persOrgCard) {
					logger.error( "Mandatory attribute Organization not filled in Signatory user card " 
							+ signPersonCard.getId().getId() + " -> aborting registration");
					throw new DataException("jbr.processor.stamp.org.notfound", 
							new Object[] {getSignatoryFIO()});
				}
				String orgNameByPadeg = Padeg.getAppointmentPadeg(
						persOrgCard.getAttributeById(ORG_FULL_NAME_ATTR_ID).getStringValue(), PREPOSITIONAL_PADEG);
	
				sigData.setOrgName(MessageFormat.format(signMarkTemplate,
						new Object[]{orgNameByPadeg}));

				sigData.setMarkOnly(markOnly);
				if(!markOnly){//TODO: ������� ��������. ������� ������ ��� ����������� ������.
					stamp.setSigData(sigData);
				}
			}	
			stamp.setRegData(regData);
			//--------------------
			//������� ����� �������� ��� �������� ��������
			Card newFileCard = createNewFileCard();
			//�������� ������ �������� � ������������ � PDF/A-1 (���� �����) � ���������� ��������������� ������
			copyFileWithStamp(fileCardToCopy, newFileCard, stamp);
			//��������� ����� �������� �� �������� ���������
			addFileCardToDocument(newFileCard);
		}
		finally	{
			execAction( new UnlockObject(document.getId()));
		}
		
		return null;
	}

	/**
	 * ����� ��� ��������� �������� �������� ��� �����������, ������� ������� ���� '�������� ��������' � ����
	 * @return Card fileCardToCopy
	 */
	private Card getFileCardToCopy() throws DataException
	{
		CardLinkAttribute cardLinkAttr = (CardLinkAttribute) this.document.getAttributeById(ATTR_DOCLINKS_ID);
		List<Card> primAttachmentsList = new ArrayList<Card>();
		List<Card> nonPrimAttachmentsList = new ArrayList<Card>();

		if (cardLinkAttr != null) 
		{
			Iterator<?> iterAttach = cardLinkAttr.getIdsLinked().iterator();

			while (iterAttach.hasNext()) 
			{
				ObjectId attachId = (ObjectId) iterAttach.next();
				ObjectQueryBase objectQuery = getQueryFactory().getFetchQuery(Card.class);
				objectQuery.setId(attachId);
				// ��������� �������� ����� �� ����� ���������� ������������
				Card attachmentCard = (Card)getDatabase().executeQuery(getSystemUser(), objectQuery);

	  			if(attachmentCard.getState().equals(ACTIVE_STATE_ID))
	  			{
	  				ListAttribute primacyAttr = ((ListAttribute) attachmentCard.getAttributeById(PRIMACY_ID));
	  				// ���� 2 ���� ������������ ��/��� (1432/1433 � 1449/1450), ��� ���� � ���� ��� �������� �������� �������� ������������ ������, � � �� ��� �������� ������-�� �������� ������, 
	  				// ���� ����������� ������, � ���� ��� �������� ����� ����� �� ������� ������ ����� ����������� �� ��� ���� ������������ (�������� ��) � ��������� ��� �� ������� �����������.  
	  				if (primacyAttr.getValue() != null && 
	  						(primacyAttr.getValue().getId().equals(YES_ID) || primacyAttr.getValue().getId().equals(YES_ID_2)))
	  				{
	  					primAttachmentsList.add(attachmentCard);
	  					// ��� �� ����, ����������� � �������� "��������", ��� � �������� "�������� ��������" ������� "��", 
	  					// ���������� "���", ����� �������� ����� ����� ����� �����.
	  					ReferenceValue refValue = new ReferenceValue();
	  					refValue.setId(NO_ID);
	  					primacyAttr.setValue(refValue);
	  					// ���������� ��
	  					doOverwriteCardAttributes(attachmentCard.getId(), getSystemUser(), primacyAttr);
	  				}
	  				else if (primAttachmentsList.isEmpty())
	  					nonPrimAttachmentsList.add(attachmentCard);
	  			}
			}
		}

		Comparator<Card> cmp = new Comparator<Card>() {
			public int compare(Card c1, Card c2) {
				int result = 0;
				DateAttribute createdDateAttr1 = (DateAttribute) c1.getAttributeById(Attribute.ID_CREATE_DATE);
				DateAttribute createdDateAttr2 = (DateAttribute) c2.getAttributeById(Attribute.ID_CREATE_DATE);
				if ((null != createdDateAttr1) && (null != createdDateAttr2))
					result = createdDateAttr1.getValue().compareTo(createdDateAttr2.getValue());
				
				return result;
		    }
		};

		Card fileCardToCopy = null;

		if (!primAttachmentsList.isEmpty())
		{
			// ����� ������ � ��������� "�������� ��������" ������������ ����, 
			// ����������� ������ � �� ����������.
			Collections.sort(primAttachmentsList, cmp);
			fileCardToCopy = primAttachmentsList.get(0);
		}
		else if (!nonPrimAttachmentsList.isEmpty())
		{
			// ���� ������� "�������� ��������" � ������ �����������, �� ������������ ����, 
			// ����������� ������ � �� ����������.
			Collections.sort(nonPrimAttachmentsList, cmp);
			fileCardToCopy = nonPrimAttachmentsList.get(0);
		}

		//�������� ���� ���� ��� �������� ���� ��� ��������� ������
		markAttachmentAsSource(fileCardToCopy);

		return fileCardToCopy;
	}

	/**
	 * ����� ����������� �������� �������� "�������� ���� ��� ��������� ���.������" � "��" � �������� "����"
	 */
	private void markAttachmentAsSource(Card fileCardToCopy) throws DataException {
		if (fileCardToCopy != null) {
			ListAttribute sourceFlagAttr = ((ListAttribute) fileCardToCopy.getAttributeById(SOURCE_FILE_FOR_STAMP_ATTR_ID));
			if (sourceFlagAttr != null) {
				//������������� �������� ��(1432)
				ReferenceValue refValue = new ReferenceValue();
				refValue.setId(YES_ID);
				sourceFlagAttr.setValue(refValue);

				// ���������� ��
				doOverwriteCardAttributes(fileCardToCopy.getId(), getSystemUser(), sourceFlagAttr);
			}
		}
	}
	/**
	 * ����� ��� �������� ����� �������� (��������� ��������)
	 * @return Card newFileCard
	 */
	private Card createNewFileCard() throws DataException
	{
		// ��������� �� �� ������� "����" � ������� "��������".
		CreateCard createCardAction = new CreateCard();
		createCardAction.setTemplate(TEMPLATE_FILE);

		Card newFileCard = (Card)execAction(createCardAction, getExecUser());
		StringAttribute name = (StringAttribute)newFileCard.getAttributeById(Attribute.ID_NAME);
		name.setValue("file");
		
		// ������������� ������� "�������� ��������" = ��
		ListAttribute primacyAttr = (ListAttribute) newFileCard.getAttributeById(PRIMACY_ID);
		if (primacyAttr != null)
		{
			ReferenceValue refValue = new ReferenceValue();
			refValue.setId(YES_ID);
			primacyAttr.setValue(refValue);
		}
		
		// ��������� ����� �� ����
		ObjectId newFileCardId = saveCard(newFileCard, getExecUser());
		newFileCard.setId((Long)newFileCardId.getId());
		execAction(new UnlockObject(newFileCardId));
		
		return newFileCard;
	}

	/**
	 * ����� ��� ����������� �������� �������� c ������������ � PDF/A-1 � ������������� ��������������� ������
	 * @param Card cardToCopy
	 * @param Card newCard
	 * @param Stamp stamp
	 */
	private void copyFileWithStamp(Card cardToCopy, Card newCard,  final Stamp stamp) throws DataException
	{

		final String stampedMaterialName = getStampedMaterialName();

		// �������� ���� ���������, ������������ (���� �����) � ����������� ��������������� ������
		CopyMaterialWithStamp copyMaterial = new CopyMaterialWithStamp();
		copyMaterial.setFromCardId(cardToCopy.getId());
		copyMaterial.setToCardId(newCard.getId());
		copyMaterial.setFileName(stampedMaterialName);
		copyMaterial.setRegStamp(stamp);
		
		ActionQueryBase queryCopy = getQueryFactory().getActionQuery(copyMaterial);
		queryCopy.setAction(copyMaterial);
		getDatabase().executeQuery(getSystemUser(), queryCopy);
				
		MaterialAttribute materialAttr = (MaterialAttribute)newCard.getAttributeById(Attribute.ID_MATERIAL);
		if (null != materialAttr)
		{
			materialAttr.setMaterialName(stampedMaterialName);
			materialAttr.setMaterialType(MaterialAttribute.MATERIAL_FILE);
		}

		StringAttribute materialNameAttr = (StringAttribute) newCard.getAttributeById(MATERIAL_NAME_ID);
		if (null != materialNameAttr)
			materialNameAttr.setValue(stampedMaterialName);

		StringAttribute nameAttr = (StringAttribute)newCard.getAttributeById(Attribute.ID_NAME);
		if (null != nameAttr) 
			nameAttr.setValue(stampedMaterialName);

		ObjectId newFileCardId = saveCard(newCard, getExecUser());
		newCard.setId((Long)newFileCardId.getId());
		execAction(new UnlockObject(newFileCardId));
	}

	/**
	 * ����� ��� ���������� ����� �������� ����� �� �������� ���������
	 * @param Card fileCard
	 */
	private void addFileCardToDocument(Card fileCard) throws DataException
	{
		CardLinkAttribute cardLinkAttr = (CardLinkAttribute) this.document.getAttributeById(ATTR_DOCLINKS_ID);

		// ��������� ����� �� ���� �� �������� ���������
		CardLinkAttribute docLinks = this.document.getCardLinkAttributeById(cardLinkAttr.getId());
		if (docLinks != null)
			docLinks.addLabelLinkedCard(fileCard);

		// ���������� ��
		doOverwriteCardAttributes(this.document.getId(), getSystemUser(), docLinks);
	}
	
	
	/**
	 * ����� ��� ��������� ������������ �� �������� "�����������"
	 * @param Card card
	 * @return UserData
	 */
	private UserData getExecUser() throws DataException
	{
		UserData execUser = null;
		PersonAttribute executorAttr = (PersonAttribute) this.document.getAttributeById(EXECUTOR_ID);
		if (executorAttr != null &&
			((PersonAttribute)(executorAttr)).getValues().size() > 0)
		{
			 execUser = new UserData();
			// � �������� ������������ ������������� ������� �� �������� "�����������"
			Person execPerson = (Person)(((PersonAttribute)executorAttr).getValues().iterator().next());
			
			execUser.setPerson(getDatabase().resolveUser(execPerson.getLogin()));
			return execUser;
		}
		else
		{
			execUser = this.getSystemUser();
		}
		return execUser;
	}

	/**
	 * ����� ��� ��������� ���� ���������� ������ � ���������� �� ��������� ���������
	 * @return Card blankTypeCard
	 */	
	private Card getBlankTypeCard() throws DataException
	{
		Card blankTypeCard = null;
		final PersonAttribute signatoryAttr = (PersonAttribute) this.document.getAttributeById(SIGNATORY_ID);
		if (null != signatoryAttr)
		{
			final Person sigPerson = signatoryAttr.getPerson();
			
			if (null != sigPerson)
			{
				final Card sigPersonCard = loadCardById(sigPerson.getCardId());
				final CardLinkAttribute blankTypeCardAttr = (CardLinkAttribute)sigPersonCard.getAttributeById(BLANK_TYPE_CARD_ID);
				if (null != blankTypeCardAttr && !blankTypeCardAttr.isEmpty())
				{
					blankTypeCard = loadCardById(blankTypeCardAttr.getIdsLinked().get(0));
				}
				else
				{
					logger.error( "Signatory user " + getSignatoryLastName() +
							" is not assigned with blank type -> aborting registration");
					throw new DataException("jbr.processor.stamp.noblank", new Object[] {getSignatoryLastName()});
				}
			}
			else
			{
				logger.error( "Signatory user is not specified for card: " + this.document.getId() + " -> aborting registration");
				throw new DataException("jbr.processor.stamp.nosiguser");
			}
			
		}
		return blankTypeCard;
	}

	/**
	 * ����� ��� ��������� ���������������� ������ �� ��������� ���������
	 * @return String reg number
	 */	
	private String getRegNum()
	{
		String regNum = "";
		StringAttribute regNumAttr = (StringAttribute) this.document.getAttributeById(REGNUM_ID);
		if (null != regNumAttr)
			regNum = regNumAttr.getValue();
			
		return regNum;
	}

	/**
	 * ����� ��� ��������� ���� ����������� �� ��������� ���������
	 * @return String reg date
	 */	
	private String getRegDate()
	{
		String regDate = "";
		DateAttribute regDateAttr = (DateAttribute) this.document.getAttributeById(REGDATE_ID);
		if (null != regDateAttr)
		{
			DateFormat df = new SimpleDateFormat("dd.MM.yyyy");   
			regDate = df.format(regDateAttr.getValue());
		}	
		return regDate;
	}
	
	/**
	 * ����� ��� ��������� ������� ���������� �� ��������� ���������
	 * @return String signatory last name
	 */	
	private String getSignatoryLastName()
	{
		String sigLastName = "";
		PersonAttribute signatoryAttr = (PersonAttribute) this.document.getAttributeById(SIGNATORY_ID);
		if (null != signatoryAttr)
		{
			String sigFullName = signatoryAttr.getPersonName();
			sigLastName = sigFullName.substring(0, sigFullName.indexOf(" "));
		}
		return sigLastName;
	}

	/**
	 * ����� ��������� �������� ����� ��������� � ��������������� �������.
	 * ������ �������� �����: "[���� �����������]_[��������������� �����]_[������� ����������].pdf". 
	 * ������ ��������: "12.04.2014_���-1-15377_���������.pdf".
	 * @return String stamped material name
	 */	
	private String getStampedMaterialName()
	{
		//��������������� �����
		String regNum = getRegNum();
		//���� �����������
		String regDate= getRegDate();
		//������� ����������
		String sigLastName= getSignatoryLastName();

		StringBuilder stampedMaterialName = new StringBuilder(50);
		if (!regDate.isEmpty())
		{
			stampedMaterialName.append(regDate);
		}
		if (!regNum.isEmpty())
		{
			stampedMaterialName.append("_");
			stampedMaterialName.append(regNum);
		}
		if (!sigLastName.isEmpty())
		{
			stampedMaterialName.append("_");
			stampedMaterialName.append(sigLastName);
		}
		stampedMaterialName.append(".pdf");

		return stampedMaterialName.toString();
	}
	
	/**
	 * ����� ���������� ������ �� ������ ��������� �������� �� ��������� �������� ����� 
	 * � � ��������� �������
	 * @return Card linkedCard
	 */	
	private Card getFirstLinkedCardByAttrAndState(final Card mainCard, 
			final ObjectId attr, final ObjectId state) throws DataException
	{
		Card linkedCard = null;
		final Attribute attribute = (Attribute) mainCard.getAttributeById(attr);
		final List<Card> linkedCards =  this.loadAllLinkedCardsByAttr(mainCard.getId(),
				attribute);
		if(null == linkedCards || linkedCards.isEmpty()) {
			return null;
		}
		for(Card card : linkedCards) {
			if (card.getState().equals(state)) {
				linkedCard = card;
				break;
			}
		}
		return linkedCard;
	}
	
	/**
	 * ����� ��� ��������� �������� ����������
	 * @return Card personCard
	 */	
	private Card getSignPersonCard(final Card card, final ObjectId attributeId) throws DataException
	{
		Card personCard = null;
		final PersonAttribute signPersonAttr = 
				(PersonAttribute) card.getAttributeById(attributeId);
		if (null != signPersonAttr) {
			final Person person = signPersonAttr.getPerson();
			if (person != null) {
				personCard = loadCardById( person.getCardId(), getSystemUser());
			}
		}
		return personCard;
	}

	/**
	 * ����� ���������� ��������� ������� � ��������� �������� ��� ����������
	 * @return SignatureData
	 */	
	private SignatureData getLastSignData(final Card signedCard) throws DataException
	{
		SignatureData signatureData = null;

		final HtmlAttribute signatureAttribute = (HtmlAttribute) signedCard.
			getAttributeById(SIGNATURE_ATTR_ID);

		final Set<String> certHashes = getSignPersonCertHashes();

		if (null == signatureAttribute || null == signatureAttribute.getValue()
				|| signatureAttribute.getStringValue().isEmpty()
				|| certHashes.isEmpty()) {
			return null;
		}

		final List<SignatureData> signatureDatas = SignatureData
				.getAllSignaturesInfo(signatureAttribute.getStringValue(), signedCard);
		Iterator<SignatureData> iter = signatureDatas.iterator();
		while (iter.hasNext()) {
			SignatureData signData = iter.next();
			if (!certHashes.contains(signData.getCertHash())) {
				iter.remove();
			}
		}
		if (!signatureDatas.isEmpty()) {
			signatureData = signatureDatas.get(signatureDatas.size() - 1);
		}

		return signatureData;
	}
	
	public Card getCertificateCardByHash(String certHash) {
		Card certificateCard = null;
		try {
			if(certHash == null || certHash.equals("")){
				logger.error("No certificate hash provided.");
				return null;
			}
			final Search search = new Search();
			search.setByAttributes(true);
			search.addStringAttribute(ObjectId.predefined(
					StringAttribute.class, SignatureData.ATTR_CERTHASH), certHash, 
					TextSearchConfigValue.EXACT_MATCH);
			final List<DataObject> templates = new ArrayList<DataObject>(1);
			templates.add(DataObject.createFromId(ObjectId.predefined(
					Template.class, SignatureData.TEMPLATE_CERTIFICATE)));
			search.setTemplates(templates);
			final ActionQueryBase searchQuery = getQueryFactory().getActionQuery(search);
			searchQuery.setAction(search);
			final List<Card> cards = ((SearchResult) getDatabase()
					.executeQuery(getSystemUser(), searchQuery)).getCards();

			if (cards.size() == 0) {
				logger.error("Could't find certificate card by certhash " + certHash);
				return null;
			}
			if (cards.size() > 1) {
				logger.warn("More than one certificate cards have equal certhash");
			}
			certificateCard = cards.get(0);
			certificateCard = (Card) this.loadCardById(certificateCard.getId());
		} catch (Exception e) {
				logger.error("Error searching certificate card by cert hash", e);
		}
		return certificateCard;
	}
	
	
	private X509Certificate getCert509ByHash(String certHash) {
		X509Certificate cert509 = null;

		final Card certificateCard = getCertificateCardByHash(certHash);
		
		if (certificateCard != null) {
			final Attribute certAttr = certificateCard
					.getAttributeById(ObjectId.predefined(
							StringAttribute.class, "jbr.certificate.cert"));
			if (certAttr != null && certAttr.getStringValue().length() > 0) {
				try {
					cert509 = (X509Certificate) CryptoLayer.getInstance()
							.getCertFromStringBase64(certAttr.getStringValue());
				} catch (Exception e) {
					logger.error("Failed to get X509 Certificate : ", e);
				}
			} else {
				return null;
			}
		}
		return cert509;
	}	
	/**
	 * ����� ��� ��������� ��� ���������� �� ��������� ���������
	 * ��������, ������ ���� ��������
	 * @return String FIO
	 */	
	private String getSignatoryFIO() throws DataException
	{
		final StringBuilder builder = new StringBuilder(30);

		final StringAttribute lastNameAttr = 
				(StringAttribute) this.actualSignPersonCard.getAttributeById(PERSON_LAST_NAME);
		final StringAttribute firstNameAttr = 
				(StringAttribute) this.actualSignPersonCard.getAttributeById(PERSON_FIRST_NAME);
		final StringAttribute middleNameAttr = 
				(StringAttribute) this.actualSignPersonCard.getAttributeById(PERSON_MIDDLE_NAME);

		builder.append(lastNameAttr.getStringValue());
		builder.append(" ");
		builder.append(firstNameAttr.getStringValue());
		builder.append(" ");
		builder.append(middleNameAttr.getStringValue());

		return builder.toString();
	}

	 
	/**
	 * ����� ��� ��������� ������� � ���������� �� ��������� ���������
	 * ��������, ������ ���� �������� -> �.�. ������
	 * @return String signature
	 */	
	private String getSignature(Card card) throws DataException
	{
		final StringBuilder builder = new StringBuilder(30);
		final List<String> fioVariants = new ArrayList<String>();

		final StringAttribute lastNameAttr = 
				(StringAttribute) card.getAttributeById(PERSON_LAST_NAME);
		final StringAttribute firstNameAttr = 
				(StringAttribute) card.getAttributeById(PERSON_FIRST_NAME);
		final StringAttribute middleNameAttr = 
				(StringAttribute) card.getAttributeById(PERSON_MIDDLE_NAME);

		builder.delete(0, builder.length());
		builder.append(firstNameAttr.getStringValue().charAt(0));
		builder.append(".");
		builder.append(middleNameAttr.getStringValue().charAt(0));
		builder.append(". ");
		builder.append(lastNameAttr.getStringValue());

		return builder.toString();
	}

	/**
	 * ����� ��� ��������� ���� ����� ������������� ������������ ����������
	 * @return Set<String> certHashes
	 */	
	private Set<String> getSignPersonCertHashes() throws DataException
	{
		final Set<String> certHashes = new HashSet<String>();

		final CardLinkAttribute signerAttribute =
				(CardLinkAttribute) this.actualSignPersonCard.getAttributeById(PERSON_ALL_CERTS_ATTR_ID);
		final List<Card> linkedCards =
				this.loadAllLinkedCardsByAttr(this.actualSignPersonCard.getId(), signerAttribute);
		if(null == linkedCards || linkedCards.isEmpty()) {
			return null;
		}
		for (Card certCard : linkedCards) {
			StringAttribute certHashAttribute =
					(StringAttribute) certCard.getAttributeById(CERT_HASH_ATTR_ID);
			if (null != certHashAttribute && !certHashAttribute.getStringValue().isEmpty()) {
				certHashes.add(certHashAttribute.getStringValue());
			}
		}
		return certHashes;
	}
}
