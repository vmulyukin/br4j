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
package com.aplana.dbmi.module.docflow;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.ResourceBundle;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.aplana.dbmi.action.ChangeState;
import com.aplana.dbmi.action.LockObject;
import com.aplana.dbmi.action.UnlockObject;
import com.aplana.dbmi.jbr.processors.ProcessCard;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.ContextProvider;
import com.aplana.dbmi.model.HtmlAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.model.PersonAttribute;
import com.aplana.dbmi.model.WorkflowMove;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ActionQueryBase;
import com.aplana.dbmi.service.impl.Database;
import com.aplana.dbmi.service.impl.ObjectQueryBase;
import com.aplana.dbmi.service.impl.QueryFactory;
import com.aplana.dbmi.service.impl.SaveQueryBase;
import com.aplana.dbmi.service.impl.UserData;

/**
 *	������� ������������� � ���� "�������" ���� ���������� �������� "�������������� ����������" � ��������� � � ������ ��������.
 * @comment RAbdullin
 */
@SuppressWarnings("unused")
public class FillSignCommentAndMoveSingnToSignedState extends ProcessCard {
	private static final long serialVersionUID = 1L;
	
	public static final ObjectId SIGN_SET = ObjectId.predefined(CardLinkAttribute.class, "jbr.sign.set");
	public static final ObjectId AUTHOR = ObjectId.predefined(PersonAttribute.class, "author");
	public static final ObjectId SIGN_PERSON = ObjectId.predefined(PersonAttribute.class, "jbr.sign.person");
	public static final ObjectId SIGN_COMMENT = ObjectId.predefined(HtmlAttribute.class, "jbr.sign.comment");
	public static final ObjectId WFM_SIGN = ObjectId.predefined(WorkflowMove.class, "jbr.waiting.to.signed");
	private Transformer trans;
	protected String schemaLocation="/conf/RepeatableReportSchema.xsd";
	protected String xsltLocation="/conf/ShowRepeatableReportTable.xslt";
	private static final String JAXP_SCHEMA_LANGUAGE = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";
	private static final String JAXP_SCHEMA_SOURCE = "http://java.sun.com/xml/jaxp/properties/schemaSource";	
	private static final String W3C_XML_SCHEMA = "http://www.w3.org/2001/XMLSchema";
	public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
	
	static final ResourceBundle bundle = ResourceBundle.getBundle("jbrDocFlow", ContextProvider.getContext().getLocale());
	
	protected String transform(Document xml, String xsltPath) throws TransformerException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			getTransformer(xsltPath).transform(new DOMSource(xml), new StreamResult(baos));
			return baos.toString("UTF-8");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private synchronized Transformer getTransformer(String path) throws TransformerConfigurationException {
		if (trans == null) {
			Source xsltSource = new StreamSource(new File(path));
	        TransformerFactory transFact = TransformerFactory.newInstance();
			trans = transFact.newTransformer(xsltSource);
		}
		return trans;
	}
	
	protected Element createPart(Document xml, String text, Date date) {
		Element part = xml.createElement("part");
		part.setAttribute("timestamp", date == null ? "-" : DATE_FORMAT.format(date));
		part.setTextContent(trimAndNewlineRight(text));
		return part;
	}
	
	protected static String trimAndNewlineRight(String input) {
		StringBuilder sb = new StringBuilder();
		sb.append(input);
		int len = input.length();
		for (int i = len - 1; i >= 0; i--) {
			char c = sb.charAt(i);
			if (!Character.isWhitespace(c)) {
				if (i < len - 1)
					sb.replace(i + 1, len, "\n");
				return sb.toString();
			}
		}
		return "";
	}
	
	public Object process() throws DataException {
		try{
			Card card = null;
			final ObjectId cardId = getCardId();//�������� id ��������� ��������
			
			//�������� �������� ����������� ���������
			if (cardId != null) {
				ObjectQueryBase query = getQueryFactory().getFetchQuery(Card.class);
				query.setId(cardId);
				card = (Card) getDatabase().executeQuery(getSystemUser(), query);
				if (card == null){
					logger.error("The card does not exist. Exit.");
					return null;
				}
			} else{
				if (!(getResult() instanceof Card)){
					logger.error("The card does not exist. Exit.");
					return null;
				}
				logger.warn("The card is just created (most probably). I'll use it.");
				card = (Card) getResult();
			}   
			
			//�������� ������������
			final  UserData user= getUser();
			
			Boolean flag = true;
			PersonAttribute authorPA = (PersonAttribute) card.getAttributeById(AUTHOR);
			Person  author = authorPA.getPerson();	
			CardLinkAttribute signSet = card.getCardLinkAttributeById(SIGN_SET);
			
			if (signSet !=null){
				Iterator<ObjectId> iter = signSet.getIdsLinked().iterator();
				while (iter.hasNext() && flag) {
					ObjectId signId = iter.next();
					if (signId != null) {
						ObjectQueryBase query = getQueryFactory().getFetchQuery(Card.class);
						query.setId(signId);
						Card sign = (Card) getDatabase().executeQuery(getSystemUser(), query);
						PersonAttribute sign_person_PA = (PersonAttribute) sign.getAttributeById(SIGN_PERSON);
						Person  sign_person = sign_person_PA.getPerson();
						if (sign_person.getCardId().equals(author.getCardId()) ){
							flag=false;
							HtmlAttribute signCommentHA = (HtmlAttribute) sign.getAttributeById(SIGN_COMMENT);
	
							String xml = formHistoryAttrValue(
									"1", 
									sign_person.getFullName(), 
									bundle.getString("AUTO_SIGN_MESSAGE"), 
									bundle.getString("SIGN_ACTION"));
							
							signCommentHA.setValue(xml);
							//��������� ��������
							//���������
							execAction( new LockObject(signId), getSystemUser());
							try {
								final SaveQueryBase sq = getQueryFactory().getSaveQuery(sign);
								sq.setObject(sign);
								getDatabase().executeQuery( getSystemUser(), sq);
	
								//��������� �������� � ��������
							
								QueryFactory queryFactory = getQueryFactory();
								Database database = getDatabase();
								ChangeState changeState = new ChangeState();
								ActionQueryBase changeStateQuery = queryFactory.getActionQuery(ChangeState.class);
								//�������� �������
								ObjectQueryBase wfMoveQuery = queryFactory.getFetchQuery(WorkflowMove.class);
								wfMoveQuery.setId(WFM_SIGN);
								WorkflowMove wfMove = (WorkflowMove)database.executeQuery(user, wfMoveQuery);		
								changeState.setCard(sign);
								changeState.setWorkflowMove(wfMove);
								changeStateQuery.setAction(changeState);
								
								//�������� ��������� ��� ��������� �������������
								UserData sysUser = new UserData();
								sysUser.setPerson(getDatabase().resolveUser(Database.SYSTEM_USER));
								sysUser.setAddress("internal");
								
								database.executeQuery(sysUser, changeStateQuery);
							} catch (DataException ex) {
								logger.error("Exception moving card "+ sign.getId()+ "\n" + ex);
								throw ex;
							} catch (Exception ex) {
								logger.error("Exception moving card "+ sign.getId()+ "\n" + ex);
								throw new DataException( "general.unique", new Object[] {sign.getId()}, ex);
							} finally {
								//������������
								execAction( new UnlockObject(signId), getSystemUser());
							}
						}
					}
				}
			}
			
			return null;
		}catch(DataException ex){
			throw ex;
		}catch(Exception ex){
			throw new DataException("Can not exec FillSignCommentAndMoveSingnToSignedState processor", ex); 
		}
	}
	
	private String formHistoryAttrValue(String round, String person, String resolution, String actionName) 
			throws 	ParserConfigurationException, 
					UnsupportedEncodingException, 
					SAXException, 
					IOException, 
					TransformerFactoryConfigurationError, 
					TransformerException {
		Document xmldoc;
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = dbf.newDocumentBuilder();
		xmldoc = builder.parse(new ByteArrayInputStream("<?xml version=\"1.0\" encoding=\"UTF-8\"?><report/>".getBytes("UTF-8")));
		Element root = xmldoc.getDocumentElement();
	
		final Date date = new Date();
		final Element part = xmldoc.createElement("part");
		part.setAttribute("round", round);
		part.setAttribute("timestamp", (date == null) ? "-" : DATE_FORMAT.format(date));
		part.setAttribute("fact-user", person);
		part.setAttribute("action", actionName);
		part.setTextContent(resolution);
		root.appendChild(part);
	
		final StringWriter stw = new StringWriter();
		final Transformer serializer = TransformerFactory.newInstance().newTransformer();
		serializer.transform(new DOMSource(xmldoc), new StreamResult(stw));
	
		return stw.toString();
	}
}