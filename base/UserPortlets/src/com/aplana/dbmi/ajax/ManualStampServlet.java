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
package com.aplana.dbmi.ajax;

import java.awt.Image;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.json.JSONWriter;

import com.aplana.dbmi.action.file.MaterialToImageList;
import com.aplana.dbmi.common.utils.pdf.StampSettings;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.CardState;
import com.aplana.dbmi.model.DateAttribute;
import com.aplana.dbmi.model.HtmlAttribute;
import com.aplana.dbmi.model.ListAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.model.PersonAttribute;
import com.aplana.dbmi.model.ReferenceValue;
import com.aplana.dbmi.model.StringAttribute;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.ServiceException;

/**
 * Servlet implementation class ManualStampServlet
 */
public class ManualStampServlet extends AbstractDBMIAjaxServlet {
	private static final String PDF_IMAGES = "/pdfImages/";

	private static final long serialVersionUID = 1L;

	private static final String PARAM_CARD_ID = "cardId";
	private static final String PARAM_IMAGE = "image";
	private static final ObjectId ATTR_DOCLINKS_ID = ObjectId.predefined(CardLinkAttribute.class, "jbr.files");
	private static final ObjectId ACTIVE_STATE_ID = ObjectId.predefined(CardState.class, "active");
	private static final ObjectId PRIMACY_ID = ObjectId.predefined(ListAttribute.class, "jbr.prime");
	private static final ObjectId YES_ID = ObjectId.predefined(ReferenceValue.class, "jbr.incoming.control.yes");
	private static final ObjectId YES_ID_2 = ObjectId.predefined(ReferenceValue.class, "jbr.commission.control.yes");
	public static final ObjectId SIGNATURE_ATTR_ID = ObjectId.predefined(HtmlAttribute.class, "jbr.uzdo.signature");
	private static final ObjectId SIGNATORY_ID = ObjectId.predefined(PersonAttribute.class, "jbr.outcoming.signatory");
	private static final ObjectId PERSON_LAST_NAME = ObjectId.predefined(StringAttribute.class, "jbr.person.lastName");
	private static final ObjectId PERSON_FIRST_NAME = ObjectId.predefined(StringAttribute.class, "jbr.person.firstName");
	private static final ObjectId PERSON_MIDDLE_NAME = ObjectId.predefined(StringAttribute.class, "jbr.person.middleName");
	
	@Override
	protected void generateResponse(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		try{
			DataServiceBean serviceBean = getDataServiceBean(request);
			String cardId = request.getParameter(PARAM_CARD_ID);
			String image = request.getParameter(PARAM_IMAGE);
			String tmpDir = System.getProperty("jboss.server.temp.dir");
			
			if(cardId != null){
	
				Card document;
				Card fileCard;
		
				document = serviceBean.getById(new ObjectId(Card.class, Long.parseLong(cardId)));
				fileCard = getFileCardToCopy(serviceBean, document);
				if(fileCard == null){
					return;
				}
				
				final HtmlAttribute signatureAttribute = (HtmlAttribute) fileCard.
						getAttributeById(SIGNATURE_ATTR_ID);
				if(signatureAttribute.isEmpty()){
					return;
				}
				
				Card signPersonCard = getSignPersonCard(document, SIGNATORY_ID, serviceBean);
				
				MaterialToImageList materialToImageListAction = new MaterialToImageList();
				materialToImageListAction.setCardId(fileCard.getId());
				materialToImageListAction.setLocationString(getSignature(signPersonCard));
				List<Image> images = serviceBean.doAction(materialToImageListAction);
				if(images == null){
					return;
				}
				
				JSONWriter writer = new JSONWriter(response.getWriter());
				writer.object();
				writer.key("height").value(StampSettings.getSigStampHeight());
				writer.key("width").value(StampSettings.getSigStampWidth());
				writer.key("images");
				writer.array();
				
	            for (int i = 0; i < images.size() ; i++) {
	            	String imageFileName = fileCard.getId().getId()+"_"+(i + 1) + ".png";
	            	File file = new File(tmpDir+PDF_IMAGES + imageFileName);
	            	if(!file.getParentFile().exists()){
	            		file.getParentFile().mkdir();
	            	}
	            	writer.object();
	            	writer.key("number").value(String.valueOf(i+1));
	            	writer.key("file").value(imageFileName);
	                ImageIO.write((RenderedImage) images.get(i), "png", file);
	                writer.endObject();
	            }
	            writer.endArray();
	            writer.endObject();
			} else if (image != null) {
				File file = new File(tmpDir + PDF_IMAGES + image);
				response.setContentType("image/png");
				response.setContentLength((int)file.length());
				FileInputStream in = new FileInputStream(file);
				OutputStream out = response.getOutputStream();
				if(file.length() > 0){
					IOUtils.copy(in, out);
				}
				
			}
        } catch (Exception e) {
            System.out.println("ERROR: " + e.getMessage());
        }
    }
	
	private Card getFileCardToCopy(DataServiceBean serviceBean, Card document) throws DataException, ServiceException
	{
		CardLinkAttribute cardLinkAttr = document.getCardLinkAttributeById(ATTR_DOCLINKS_ID);
		List<Card> primAttachmentsList = new ArrayList<Card>();
		List<Card> nonPrimAttachmentsList = new ArrayList<Card>();

		if (cardLinkAttr != null) 
		{
			Iterator<?> iterAttach = cardLinkAttr.getIdsLinked().iterator();  				

			while (iterAttach.hasNext()) 
			{  					
				ObjectId attachId = (ObjectId) iterAttach.next();
				// ��������� �������� �����
				Card attachmentCard = serviceBean.getById(attachId);

	  			if(attachmentCard.getState().equals(ACTIVE_STATE_ID))
	  			{
	  				ListAttribute primacyAttr = ((ListAttribute) attachmentCard.getAttributeById(PRIMACY_ID));
	  				// ���� 2 ���� ������������ ��/��� (1432/1433 � 1449/1450), ��� ���� � ���� ��� �������� �������� �������� ������������ ������, � � �� ��� �������� ������-�� �������� ������, 
	  				// ���� ����������� ������, � ���� ��� �������� ����� ����� �� ������� ������ ����� ����������� �� ��� ���� ������������ (�������� ��) � ��������� ��� �� ������� �����������.  
	  				if (primacyAttr.getValue() != null && 
	  						(primacyAttr.getValue().getId().equals(YES_ID) || primacyAttr.getValue().getId().equals(YES_ID_2)))
	  				{
	  					primAttachmentsList.add(attachmentCard);
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

		return fileCardToCopy;
	}

	/**
	 * ����� ��� ��������� �������� ����������
	 * @return Card personCard
	 * @throws ServiceException 
	 */	
	private Card getSignPersonCard(final Card card, final ObjectId attributeId, DataServiceBean serviceBean) throws DataException, ServiceException
	{
		Card personCard = null;
		final PersonAttribute signPersonAttr = 
				(PersonAttribute) card.getAttributeById(attributeId);
		if (null != signPersonAttr) {
			final Person person = signPersonAttr.getPerson();
			if (person != null) {
				personCard = serviceBean.getById(person.getCardId());
			}
		}
		return personCard;
	}
	
	/**
	 * ����� ��� ��������� ������� � ���������� �� ��������� ���������
	 * ��������, ������ ���� �������� -> �.�. ������
	 * @return String signature
	 */	
	private String getSignature(Card card) throws DataException {
		final StringBuilder builder = new StringBuilder(30);

		final StringAttribute lastNameAttr   = card.getAttributeById(PERSON_LAST_NAME);
		final StringAttribute firstNameAttr  = card.getAttributeById(PERSON_FIRST_NAME);
		final StringAttribute middleNameAttr = card.getAttributeById(PERSON_MIDDLE_NAME);

		builder.delete(0, builder.length());
		builder.append(firstNameAttr.getStringValue().charAt(0));
		builder.append(".");
		builder.append(middleNameAttr.getStringValue().charAt(0));
		builder.append(". ");
		builder.append(lastNameAttr.getStringValue());

		return builder.toString();
	}
}
