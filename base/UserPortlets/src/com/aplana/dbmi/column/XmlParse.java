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
package com.aplana.dbmi.column;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

public class XmlParse implements ContentHandler{

	private IEventListener eventListener = null;	
	private ItemTag itemTag = null;	
	private LinkedList<ItemTag> linkedList = new LinkedList<ItemTag>();
	private ItemTag result = null;
	private boolean flagResult=false;
	

	private XmlParse(IEventListener eventListener) {
		this.eventListener = eventListener;
		result = null;
	}
	
	private XmlParse(){
		result = null;
	}
	
	public static void parse(ItemTag itemTag, String pathname) throws Exception{
		
		parse(itemTag, new File(pathname));
		
	}
	
	public static void parse(ItemTag itemTag, File file) throws Exception{
		XmlParse xmlParse = new XmlParse();
		itemTag.involveParameters(xmlParse.parse(file));	
	}
	

	
	public static void parse(String pathname, IEventListener eventListener) throws Exception{
		File file = new File(pathname);
		parse(file,eventListener);
	}

	public static void parse(File file, IEventListener eventListener) throws Exception {
		if (!file.isFile()) {
			throw new FileNotFoundException("���� �����������!");			
		}
			createXmlReader(eventListener).parse(
					new InputSource(new BufferedInputStream(
							new FileInputStream(file))));		
	}

	public static XMLReader createXmlReader(IEventListener callBack)
			throws ParserConfigurationException, SAXException {
		SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
		SAXParser parser = saxParserFactory.newSAXParser();
		XMLReader xmlReader = parser.getXMLReader();
		xmlReader.setContentHandler(new XmlParse(callBack));
		return xmlReader;
	}
	


	public void setDocumentLocator(Locator locator) {
		// TODO Auto-generated method stub

	}

	public void startDocument() throws SAXException {
		result = null;
	}

	public void endDocument() throws SAXException {
		if(linkedList.size() == 1){
			eventListener.onEvent(linkedList.getLast());
		}else{
			eventListener.onEvent(null);
		}
	}

	public void startPrefixMapping(String prefix, String uri)
			throws SAXException {
		// TODO Auto-generated method stub

	}

	public void endPrefixMapping(String prefix) throws SAXException {
		// TODO Auto-generated method stub

	}

	public void startElement(String uri, String localName, String qName,
			Attributes atts) throws SAXException {

		if (itemTag == null) {
			itemTag = new ItemTag();
		} else {
			ItemTag newItemTag = new ItemTag();
			itemTag.addItemTag(newItemTag);
			itemTag = newItemTag;
		}

		itemTag.setTag(qName);
		itemTag.setAttrMap(loadAttr(atts));

		linkedList.addFirst(itemTag);
	}

	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		if (linkedList.size() > 1) {
			linkedList.removeFirst();
			itemTag = linkedList.getFirst();
		}		
	}

	public void characters(char[] ch, int start, int length)
			throws SAXException {
		String str = new String(ch, start, length);
		itemTag.setMsg(str);

	}

	public void ignorableWhitespace(char[] ch, int start, int length)
			throws SAXException {
		// TODO Auto-generated method stub

	}

	public void processingInstruction(String target, String data)
			throws SAXException {
		// TODO Auto-generated method stub

	}

	public void skippedEntity(String name) throws SAXException {
		// TODO Auto-generated method stub

	}

	private Map<String, String> loadAttr(Attributes atts) {
		Map<String, String> attrMaps = null;
		if (atts != null) {
			int len = atts.getLength();
			attrMaps = new HashMap<String, String>();
			for (int i = 0; i < len; i++) {
				attrMaps.put(atts.getQName(i), atts.getValue(i));
			}
		}
		return attrMaps;
	}
	
	
	
	private ItemTag parse(File file) throws Exception{
		flagResult=false;
		parse(file, new IEventListener() {			
			public void onEvent(ItemTag itemTag) {
				result = itemTag;
				flagResult=true;
			}
		});
		
		while(true){
			Thread.sleep(1);
			if(flagResult){
				break;
			}
		}
		return result;		
	}
	
	






}
