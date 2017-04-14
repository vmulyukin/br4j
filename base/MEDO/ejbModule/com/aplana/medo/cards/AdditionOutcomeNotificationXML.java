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
package com.aplana.medo.cards;

import java.util.Properties;
import java.util.UUID;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

/**
 * @author PPanichev
 *
 */
public class AdditionOutcomeNotificationXML {

    private Document xml = null;
    private Log logger = LogFactory.getLog(getClass());
    private static Properties propertiesSt;
    
    public AdditionOutcomeNotificationXML(Document xml) throws CardException {
	this.xml = xml;
	if (propertiesSt == null) throw new CardException("jbr.medo.card.AdditionOutcomeNotificationXML.notProperties");
	if (xml == null) throw new CardException("jbr.medo.card.AdditionOutcomeNotificationXML.notXML");
    }
    
    public static void setProperties(Properties properties) {
	propertiesSt = properties;
    }
    
    public Document additionTimeToXml(String time) throws CardException {
	String notificationTimeCode = propertiesSt.getProperty("code.notification.time");
	if (notificationTimeCode == null) {
	    logger.error("jbr.medo.card.AdditionOutcomeNotificationXML.code.document.uid property should be set");
	    throw new CardException();
	}
	Element oldElement = null;
	try {
	    oldElement = getAttributeByCode(notificationTimeCode, xml);
	} catch (XPathExpressionException ex) {
	    logger.error("jbr.medo.card.AdditionOutcomeNotificationXML.not.notificationTimeCode", ex);
	}
	if (oldElement != null) {
	    xml.getDocumentElement().replaceChild(CardXMLBuilder.createAttribute(xml, notificationTimeCode,
			CardXMLBuilder.DATE_TYPE, time), oldElement);
	} else {
	    xml.getDocumentElement().appendChild(
		    CardXMLBuilder.createAttribute(xml, notificationTimeCode,
				CardXMLBuilder.DATE_TYPE, time));
	}
	return xml;
    }
    
    public Document additionHeaderUIDToXml() throws CardException {
	String headerUidCode = propertiesSt.getProperty("code.header.uid");
	if (headerUidCode == null) {
	    logger.error("jbr.medo.card.AdditionOutcomeNotificationXML.code.header.uid property should be set");
	    throw new CardException();
	}
	
	xml.getDocumentElement().appendChild(
		CardXMLBuilder.createAttribute(xml, headerUidCode,
			CardXMLBuilder.STRING_TYPE, UUID.randomUUID()
				.toString()));
	return xml;
    }
    
    public Document additionPersonInternalToXml(PersonInternal cardPersonInternal, String code) {
	
	String notificationPerson = propertiesSt.getProperty(String.format("code.notification.%s.person", code), "");
	if (notificationPerson.equals("")) {
	    logger.error(String.format("jbr.medo.card.AdditionOutcomeNotificationXML.code.notification.%s.person property should be set", code));
	} else {
	   /* xml.getDocumentElement().appendChild(
			CardXMLBuilder.createAttribute(xml, notificationPerson,
				CardXMLBuilder.STRING_TYPE, String.format("%s %s %s", cardPersonInternal.getLastName(), cardPersonInternal.getFirstName(), cardPersonInternal.getMiddleName()) ));*/
		xml.getDocumentElement().appendChild(
    			CardXMLBuilder.createAttributeValueDescription(xml, notificationPerson,
    				CardXMLBuilder.CARD_LINK_TYPE, String.format("%s %s %s", cardPersonInternal.getLastName(), cardPersonInternal.getFirstName(), cardPersonInternal.getMiddleName()), cardPersonInternal.getId()));
	}
	
	String notificationPost = propertiesSt.getProperty(String.format("code.notification.%s.post", code), "");
	if (notificationPost.equals("")) {
	    logger.error(String.format("jbr.medo.card.AdditionOutcomeNotificationXML.code.notification.%s.post property should be set", code));
	} else {
	    xml.getDocumentElement().appendChild(
			CardXMLBuilder.createAttribute(xml, notificationPost,
				CardXMLBuilder.STRING_TYPE, cardPersonInternal.getPosition()));
	}
        return xml;
    }
    
    public Document additionContactInfoPersonInternalToXml(PersonInternal cardPersonInternal, String code) {
	String notificationContactInfo = propertiesSt.getProperty(String.format("code.notification.%s.contactInfo", code), "");
	if (notificationContactInfo.equals("")) {
	    logger.error(String.format("jbr.medo.card.AdditionOutcomeNotificationXML.code.notification.%s.contactInfo property should be set", code));
	} else {
	    xml.getDocumentElement().appendChild(
		    CardXMLBuilder.createAttribute(xml, notificationContactInfo,
			    CardXMLBuilder.STRING_TYPE, cardPersonInternal.getContactInfo()));
	    }
	return xml;
    }
    
    public Document additionDepartmentToXml(Department cardDepartment, String code) {
	
	String notificationDepartment = propertiesSt.getProperty(String.format("code.notification.%s.department", code), "");
	if (notificationDepartment.equals("")) {
	    logger.error(String.format("jbr.medo.card.AdditionOutcomeNotificationXML.code.notification.%s.department property should be set", code));
	} else {
	    xml.getDocumentElement().appendChild(
			CardXMLBuilder.createAttribute(xml, notificationDepartment,
				CardXMLBuilder.STRING_TYPE, cardDepartment.getFullName()));
	}
	
	return xml;
    }
    
    public Document additionOrganizationToXml(Organization cardOrganization, String code) {
	
	String notificationOrganization = propertiesSt.getProperty(String.format("code.notification.%s.organization", code), "");
	if (notificationOrganization.equals("")) {
	    logger.error(String.format("jbr.medo.card.AdditionOutcomeNotificationXML.code.notification.%s.organization property should be set", code));
	} else {
	    xml.getDocumentElement().appendChild(
			CardXMLBuilder.createAttribute(xml, notificationOrganization,
				CardXMLBuilder.STRING_TYPE, cardOrganization.getFullName()));
	}
	
	return xml;
    }
    
    public Document additionOrganizationWithDescriptionToXml(Organization cardOrganization, String code) {
    	
    	String notificationOrganization = propertiesSt.getProperty(String.format("code.notification.%s.organization", code), "");
    	if (notificationOrganization.equals("")) {
    	    logger.error(String.format("jbr.medo.card.AdditionOutcomeNotificationXML.code.notification.%s.organization property should be set", code));
    	} else {
    	    xml.getDocumentElement().appendChild(
    			CardXMLBuilder.createAttributeValueDescription(xml, notificationOrganization,
    				CardXMLBuilder.CARD_LINK_TYPE, cardOrganization.getFullName(), cardOrganization.getId()));
    	}
    	
    	return xml;
        }
    
    public Document additionOverheadSenderToXml(String overheadSender, String overheadSenderId) throws CardException, XPathExpressionException {
	String notificationOverheadSenderCode = propertiesSt.getProperty("code.notification.overheadSender");
	if (notificationOverheadSenderCode == null || notificationOverheadSenderCode.equals("")) {
	    //logger.error("jbr.medo.card.AdditionOutcomeNotificationXML.code.notification.overheadSender property should be set");
	    throw new CardException("jbr.medo.card.AdditionOutcomeNotificationXML.code.notification.overheadSender property should be set");
	}
	if (overheadSender == null || overheadSender.equals("") || overheadSenderId == null || overheadSenderId.equals("")) 
	    throw new CardException("jbr.medo.card.AdditionOutcomeNotificationXML.additionOverheadSenderToXml No overheadSenderId or overheadSender");
	String description = "description";
	Element oldElement_attr = null;
	Element oldElement_val = null;
	try {
	    oldElement_attr = getAttributeByCode(notificationOverheadSenderCode, xml);
	} catch (XPathExpressionException ex) {
	    logger.error("jbr.medo.card.AdditionOutcomeNotificationXML.not.", ex);
	}
	try {
	    oldElement_val = getAttributeByValue(notificationOverheadSenderCode, xml);
	} catch (XPathExpressionException ex) {
	    logger.error("jbr.medo.card.AdditionOutcomeNotificationXML.not.", ex);
	}
	Element newElement_attr = null;
	Element newElement_val = null;
	if (oldElement_val != null && oldElement_attr != null) {
	    //oldElement_val = getAttributeByValue(notificationOverheadSenderCode, oldElement_attr);
	    newElement_val = createValElement(overheadSender, overheadSenderId, description);
	    oldElement_attr.replaceChild(newElement_val, oldElement_val);
	} else {
	    newElement_attr = xml.createElementNS(CardXMLBuilder.NS, CardXMLBuilder.ATTRIBUTE_TAG);
	    newElement_attr.setAttribute(CardXMLBuilder.CODE_ATTRIBUTE, notificationOverheadSenderCode);
	    newElement_attr.setAttribute(CardXMLBuilder.TYPE_ATTRIBUTE, CardXMLBuilder.CARD_LINK_TYPE);
	    newElement_val = createValElement(overheadSender, overheadSenderId, description);
	    newElement_attr.appendChild(newElement_val);
	    xml.getDocumentElement().appendChild(newElement_attr);
	}
	return xml;
    }
    
    private Element getAttributeByCode(String code, Document doc) throws XPathExpressionException {
	XPath xpath = XPathFactory.newInstance().newXPath();
	xpath.setNamespaceContext(new CardXMLBuilder.CardNamespaceContext());
	XPathExpression expr = xpath.compile(String
		    .format("//jbr:attribute[@code='%s']", code));
	try {
	    return (Element) expr.evaluate(doc, XPathConstants.NODE);
	} catch (XPathExpressionException ex) {
	    throw new IllegalStateException("jbr.medo.card.AdditionOutcomeNotificationXML.getAttributeByCode: ", ex);
	}
    }
    
    private Element getAttributeByValue(String code, Document doc) throws XPathExpressionException {
	XPath xpath = XPathFactory.newInstance().newXPath();
	xpath.setNamespaceContext(new CardXMLBuilder.CardNamespaceContext());
	//XPathExpression expr = xpath.compile("//jbr:value");
	XPathExpression expr = xpath.compile(String
		    .format("//jbr:attribute[@code='%s']/jbr:value", code));
	try {
	    return (Element) expr.evaluate(doc, XPathConstants.NODE);
	} catch (XPathExpressionException ex) {
	    throw new IllegalStateException("jbr.medo.card.AdditionOutcomeNotificationXML.getAttributeByValue: ", ex);
	}
    }
    
    private Element createValElement(String overheadSender, String overheadSenderId, String attr) {
	Element newElement_val = xml.createElementNS(CardXMLBuilder.NS, CardXMLBuilder.VALUE_TAG);
	Text valueText = xml.createTextNode(overheadSenderId);
	newElement_val.appendChild(valueText); 
	newElement_val.setAttribute(attr, overheadSender);
	return newElement_val;
    }
    
}
