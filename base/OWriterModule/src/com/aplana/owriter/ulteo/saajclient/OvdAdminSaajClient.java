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
package com.aplana.owriter.ulteo.saajclient;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.soap.Detail;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPFault;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;
import javax.xml.ws.soap.SOAPBinding;
import javax.xml.ws.soap.SOAPFaultException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * ����� ��� ���������� �������� �������������� �������� � ������� SAAJ
 *
 * @author Vlad Alexandrov
 * @version 1.0
 * @since   2014-10-03
 */

public class OvdAdminSaajClient {
    public static String nsSchema = "urn:OvdAdmin";
    public static String soapSchema = "http://schemas.xmlsoap.org/soap/envelope/";
    public static String xsiSchema = "http://www.w3.org/2001/XMLSchema-instance";
    public static String xsSchema = "http://www.w3.org/2000/XMLSchema-instance";
    public static String xsdSchema = "http://www.w3.org/2001/XMLSchema";
    public static String encodingStyle = "http://schemas.xmlsoap.org/soap/encoding/";
    public static String targetNS = "uri:ovd";

    protected final Log logger = LogFactory.getLog(getClass());
    
    private Dispatch<Source> dispatch;

    public OvdAdminSaajClient(String serverURL, String wslogin, String wspassword){

	    QName serviceName = new QName(targetNS, "OvdAdminService");
	    QName portName = new QName(targetNS, "OvdAdminPort");
	    Service service = Service.create(serviceName);
	    service.addPort(portName, SOAPBinding.SOAP11HTTP_BINDING, serverURL);

	    dispatch = service.createDispatch(portName, Source.class, Service.Mode.MESSAGE);
	    dispatch.getRequestContext().put(BindingProvider.USERNAME_PROPERTY, wslogin);
	    dispatch.getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, wspassword);
    }

	public SOAPMessage getSoapBodyResponse(String request) throws Exception {
	    Source response = this.dispatch.invoke(
	        new StreamSource(new StringReader(request)));
	        // if using a file for input instead:
	        // new StreamSource(new File("myrequest.xml")));
	    // use SAAJ to open message
	    MessageFactory msgFactory = MessageFactory.newInstance();
	    SOAPMessage respMsg = msgFactory.createMessage();
	    SOAPPart env = respMsg.getSOAPPart();
	    env.setContent(response);
	    if(logger.isDebugEnabled()){
	    	logger.debug("Got SOAP response: " + response.toString());
	    }
	    return respMsg;
	}

    public boolean doesUserExist(String login) throws Exception {
        boolean result = false;
        try {
            String userInfoRequest = "<soapenv:Envelope "
                + " xmlns:xsi=\"" + xsiSchema + "\" "
                + " xmlns:xsd=\"" + xsdSchema + "\" "
                + " xmlns:soapenv=\"" + soapSchema + "\" "
                + " xmlns:urn=\"" + nsSchema + "\"> "
                + "<soapenv:Body>"
                + "  <urn:user_info soapenv:encodingStyle=\"" 
                +           encodingStyle + "\">"
                +           "<parameters xsi:type=\"xsd:string\" " + "xs:type=\"type:string\" " + "xmlns:xs=\"" + xsSchema + "\">" 
                +               login 
                +           "</parameters>"
                +    "</urn:user_info>"
                +"</soapenv:Body>"
                +"</soapenv:Envelope>";
                           
            SOAPMessage respMsg = getSoapBodyResponse(userInfoRequest);
            // writeTo method outputs SOAPMessage, helpful for debugging
            // geocodeMsg.writeTo(System.out);

            if (respMsg.getSOAPBody().hasFault()) {
                SOAPFault fault = respMsg.getSOAPBody().getFault();
	            Detail detail = fault.getDetail();
	            String detailString = "";
	            if (null != detail)
	            	detailString = detail.getValue();
                logger.error("Could not get user info for " 
                   + login + ": " 
                   + fault.getFaultString() + "; " + detailString);
            }
            NodeList list = respMsg.getSOAPBody().
                getElementsByTagName("item");
            if (list.getLength() > 0) {
            	result = true;
            }
        } catch (SOAPFaultException e) {
        	logger.error("SOAPFaultException: " + e.getFault().getFaultString(), e);
        } catch (Exception e) {
        	logger.error("Exception: " + e.getMessage(), e);
        }
        return result;
    }
    
    public String getGroupId(String groupName) throws Exception {
        String groupId = null;
            String groupInfoRequest = "<soapenv:Envelope "
                + " xmlns:xsi=\"" + xsiSchema + "\" "
                + " xmlns:xsd=\"" + xsdSchema + "\" "
                + " xmlns:soapenv=\"" + soapSchema + "\" "
                + " xmlns:urn=\"" + nsSchema + "\"> "
                + "<soapenv:Body>"
                + "  <urn:users_groups_list_partial soapenv:encodingStyle=\"" 
                +           encodingStyle + "\">"
                +           "<search xsi:type=\"xsd:string\" " + "xs:type=\"type:string\" " + "xmlns:xs=\"" + xsSchema + "\">" 
                +               groupName 
                +           "</search>"
                +    "</urn:users_groups_list_partial>"
                +"</soapenv:Body>"
                +"</soapenv:Envelope>";
                           
            SOAPMessage respMsg = getSoapBodyResponse(groupInfoRequest);
            // writeTo method outputs SOAPMessage, helpful for debugging
            // geocodeMsg.writeTo(System.out);

            if (respMsg.getSOAPBody().hasFault()) {
                SOAPFault fault = respMsg.getSOAPBody().getFault();
	            Detail detail = fault.getDetail();
	            String detailString = "";
	            if (null != detail)
	            	detailString = detail.getValue();
                logger.error("Could not get group info for " 
                   + groupName + ": " 
                   + fault.getFaultString() + "; " + detailString);
            }
            NodeList list = respMsg.getSOAPBody().
                getElementsByTagName("key");
            if (list.getLength() > 0) {
            	for(int i = 0; i < list.getLength(); i++){
            		Node node = list.item(i);
            		String value = node.getFirstChild().getNodeValue();
            		if (null != value && value.equals("id")){
            			Node idNode = node.getNextSibling();
            			if (null != idNode){
            				groupId = idNode.getFirstChild().getNodeValue();
            				break;
            			}
            		}
            	}
            }
        return groupId;
    }
    
    public boolean doesUserBelongToGroup(String login, String groupId) throws Exception {
    	boolean result = false;
            String userInfoRequest = "<soapenv:Envelope "
                + " xmlns:xsi=\"" + xsiSchema + "\" "
                + " xmlns:xsd=\"" + xsdSchema + "\" "
                + " xmlns:soapenv=\"" + soapSchema + "\" "
                + " xmlns:urn=\"" + nsSchema + "\"> "
                + "<soapenv:Body>"
                + "  <urn:users_group_info soapenv:encodingStyle=\"" 
                +           encodingStyle + "\">"
                +           "<parameters xsi:type=\"xsd:string\" " + "xs:type=\"type:string\" " + "xmlns:xs=\"" + xsSchema + "\">" 
                +               groupId 
                +           "</parameters>"
                +    "</urn:users_group_info>"
                +"</soapenv:Body>"
                +"</soapenv:Envelope>";
                           
            SOAPMessage respMsg = getSoapBodyResponse(userInfoRequest);
            // writeTo method outputs SOAPMessage, helpful for debugging
            // geocodeMsg.writeTo(System.out);
            
            if (respMsg.getSOAPBody().hasFault()) {
                SOAPFault fault = respMsg.getSOAPBody().getFault();
                Detail detail = fault.getDetail();
                String faultString = fault.getFaultString();
                String detailString = "";
                if (null != detail)
                	detailString = detail.getValue();

                logger.error("Could not get group info for id" 
                   + groupId + ": " 
                   + faultString + "; " + detailString);
            }
            NodeList list = respMsg.getSOAPBody().
                getElementsByTagName("key");
            if (list.getLength() > 0) {
            	for(int i = 0; i < list.getLength(); i++){
            		Node node = list.item(i);
            		String value = node.getFirstChild().getNodeValue();
            		if (null != value && value.equals(login)){
            			result = true;
            			break;
            		}
            	}
            }
        return result;
    }
 
	 public String getServerId() throws Exception {
	     String serverId = null;
	         String serverInfoRequest = "<soapenv:Envelope "
	             + " xmlns:xsi=\"" + xsiSchema + "\" "
	             + " xmlns:xsd=\"" + xsdSchema + "\" "
	             + " xmlns:soapenv=\"" + soapSchema + "\" "
	             + " xmlns:urn=\"" + nsSchema + "\"> "
	             + "<soapenv:Body>"
	             + "  <urn:servers_list soapenv:encodingStyle=\"" 
	             +           encodingStyle + "\">"
	             +           "<parameters xsi:type=\"xsd:string\" " + "xs:type=\"type:string\" " + "xmlns:xs=\"" + xsSchema + "\">" 
	             +               0 
	             +           "</parameters>"
	             +    "</urn:servers_list>"
	             +"</soapenv:Body>"
	             +"</soapenv:Envelope>";
	                        
	         SOAPMessage respMsg = getSoapBodyResponse(serverInfoRequest);
	         // writeTo method outputs SOAPMessage, helpful for debugging
	         // geocodeMsg.writeTo(System.out);
	         
	         if (respMsg.getSOAPBody().hasFault()) {
	                SOAPFault fault = respMsg.getSOAPBody().getFault();
	                Detail detail = fault.getDetail();
	                String detailString = "";
	                if (null != detail)
	                	detailString = detail.getValue();
	             logger.error("Could not get servers list: " 
	            		 + fault.getFaultString() + "; " + detailString);
	         }
	         NodeList list = respMsg.getSOAPBody().
	         getElementsByTagName("key");
		     if (list.getLength() > 0) {
		     	for(int i = 0; i < list.getLength(); i++){
		     		Node node = list.item(i);
		     		String value = node.getFirstChild().getNodeValue();
		     		if (null != value && value.equals("id")){
		     			Node idNode = node.getNextSibling();
		     			if (null != idNode){
		     				serverId = idNode.getFirstChild().getNodeValue();
		     				break;
		     			}
		     		}
		     	}
		     }
	     return serverId;
	 }
	 
	 public String getUserSessionIdByServer(String login, String serverId) throws Exception{
	     String sessionId = null;
	         String userInfoRequest = "<soapenv:Envelope "
	             + " xmlns:xsi=\"" + xsiSchema + "\" "
	             + " xmlns:xsd=\"" + xsdSchema + "\" "
	             + " xmlns:soapenv=\"" + soapSchema + "\" "
	             + " xmlns:urn=\"" + nsSchema + "\"> "
	             + "<soapenv:Body>"
	             + "  <urn:sessions_list_by_server soapenv:encodingStyle=\"" 
	             +           encodingStyle + "\">"
	             +           "<server xsi:type=\"xsd:string\" " + "xs:type=\"type:string\" " + "xmlns:xs=\"" + xsSchema + "\">" 
	             +               serverId
	             +           "</server>"
	             +           "<offset xsi:type=\"xsd:integer\" " + "xs:type=\"type:integer\" " + "xmlns:xs=\"" + xsSchema + "\">" 
	             +               0
	             +           "</offset>"
	             +    "</urn:sessions_list_by_server>"
	             +"</soapenv:Body>"
	             +"</soapenv:Envelope>";
	                        
	         SOAPMessage respMsg = getSoapBodyResponse(userInfoRequest);
	         // writeTo method outputs SOAPMessage, helpful for debugging
	         // geocodeMsg.writeTo(System.out);
	         
	         if (respMsg.getSOAPBody().hasFault()) {
	             SOAPFault fault = respMsg.getSOAPBody().getFault();
	             Detail detail = fault.getDetail();
	             String detailString = "";
	             if (null != detail)
	            	 detailString = detail.getValue();
	             logger.error("Could not get sessions list by server id "
	            	+ serverId + ": "
	                + fault.getFaultString() + "; " + detailString);
	         }
	         NodeList list = respMsg.getSOAPBody().
	         getElementsByTagName("key");
		     if (list.getLength() > 0) {
		     	for(int i = 0; i < list.getLength(); i++){
		     		Node node = list.item(i);
		     		String value = node.getFirstChild().getNodeValue();
		     		if (null != value && value.equals("user_login")){
		     			Node idNode = node.getNextSibling();
		     			if (null != idNode && null != idNode.getFirstChild().getNodeValue()){
		     				if (idNode.getFirstChild().getNodeValue().equals(login)){
		     					Node itemNode = idNode.getParentNode();
		     					if (null != itemNode){
		     						Node valueNode = itemNode.getParentNode();
		     						if (null != valueNode){
		     							Node keyNode = valueNode.getPreviousSibling();
		     							if (null != keyNode){
		     								sessionId = keyNode.getFirstChild().getNodeValue();
		     								break;
		     							}
		     						}
		     					}
		     				}
		     			}
		     		}
		     	}
		     }
	     return sessionId;
	 }
	 
	 
	 public String getPublishedAppGroupId(String appGroupName) throws Exception {
	     String appGroupId = null;
	         String groupInfoRequest = "<soapenv:Envelope "
	             + " xmlns:xsi=\"" + xsiSchema + "\" "
	             + " xmlns:xsd=\"" + xsdSchema + "\" "
	             + " xmlns:soapenv=\"" + soapSchema + "\" "
	             + " xmlns:urn=\"" + nsSchema + "\"> "
	             + "<soapenv:Body>"
	             + "  <urn:applications_groups_list soapenv:encodingStyle=\"" + encodingStyle + "\"/>"
	             +"</soapenv:Body>"
	             +"</soapenv:Envelope>";

	         SOAPMessage respMsg = getSoapBodyResponse(groupInfoRequest);
	         // writeTo method outputs SOAPMessage, helpful for debugging
	         // geocodeMsg.writeTo(System.out);
	         
	         if (respMsg.getSOAPBody().hasFault()) {
	             SOAPFault fault = respMsg.getSOAPBody().getFault();
	             Detail detail = fault.getDetail();
	             String detailString = "";
	             if (null != detail)
	            	 detailString = detail.getValue();
	             logger.error("Could not get applications group list: "
	                + fault.getFaultString() + "; " + detailString);
	         }
	         NodeList nodeList = respMsg.getSOAPBody().getElementsByTagName("value");
	         ArrayList<Map<String, String>> groups = new ArrayList<Map<String, String>>();
	         if (nodeList.getLength() > 0) {
	        	 for(int i = 0; i < nodeList.getLength(); i++){
			     	Node valueNode = nodeList.item(i);	
			     	NodeList childList = valueNode.getChildNodes();
			     	Map items = new HashMap<String,String>();
			     	if (childList.getLength() > 0) {
			        	 for(int j = 0; j < childList.getLength(); j++){
			        		 Node childNode = childList.item(j);
			        		 if (childNode.getFirstChild()!= null)
			        			 items.put(childNode.getFirstChild().getFirstChild().getNodeValue(), childNode.getLastChild().getFirstChild().getNodeValue());
			        	 }
			     	}

			     	groups.add(items);
	        	 }
			     for (Map<String, String> group : groups){
			     	if (group.containsValue(appGroupName) && null != group.get("published") && group.get("published").equals("true")){
			     		appGroupId = group.get("id");
			     		break;
			     	}
			     }
	         }
	     return appGroupId;
	 }
	 
	 public String getServerLogs(String serverId) throws Exception {
	     String log = null;
	         String logDownloadRequest = "<soapenv:Envelope "
	             + " xmlns:xsi=\"" + xsiSchema + "\" "
	             + " xmlns:xsd=\"" + xsdSchema + "\" "
	             + " xmlns:soapenv=\"" + soapSchema + "\" "
	             + " xmlns:urn=\"" + nsSchema + "\"> "
	             + "<soapenv:Body>"
	             + "  <urn:log_download soapenv:encodingStyle=\"" + encodingStyle + "\">"
	             +           "<parameters xsi:type=\"xsd:string\" " + "xs:type=\"type:string\" " + "xmlns:xs=\"" + xsSchema + "\">" 
	             +               serverId
	             +           "</parameters>"
	             +    "</urn:log_download>"
	             +"</soapenv:Body>"
	             +"</soapenv:Envelope>";

	         SOAPMessage respMsg = getSoapBodyResponse(logDownloadRequest);
	         // writeTo method outputs SOAPMessage, helpful for debugging
	         // geocodeMsg.writeTo(System.out);
	         
	         if (respMsg.getSOAPBody().hasFault()) {
	             SOAPFault fault = respMsg.getSOAPBody().getFault();
	             Detail detail = fault.getDetail();
	             String detailString = "";
	             if (null != detail)
	            	 detailString = detail.getValue();
	             logger.error("Could not download server logs: "
	                + fault.getFaultString() + "; " + detailString);
	         }
	         NodeList nodeList = respMsg.getSOAPBody().getElementsByTagName("parameters");
	         if (nodeList.getLength() > 0) {
	        	 for(int i = 0; i < nodeList.getLength(); i++){
			     	Node node = nodeList.item(i);	
			     	if (null != node.getFirstChild()){
			     		log = node.getFirstChild().getNodeValue();
			     		break;
			     	}
	        	 }
	         }
	     return log;
	 }
}