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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.action.SearchResult.Column;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.CardState;
import com.aplana.dbmi.model.HtmlAttribute;
import com.aplana.dbmi.model.IntegerAttribute;
import com.aplana.dbmi.model.ListAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.ReferenceValue;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.ServiceException;

public class FilesAndCommentsUtils {

	private static Log logger = LogFactory.getLog(FilesAndCommentsUtils.class);

	public static final String ROUND_DATA_ARRAY = "ROUND_DATA_ARRAY";
	public static final String PARAM_FILE_LINK = "fileLinkAttribute";
	public static final String PARAM_HIDE_WHEN_DSP = "hideChildrenWhenDSP";
	public static final String PARAM_HIDE_CHILDREN = "hideChildren";
	
	public final ObjectId visaRoundAttrId = ObjectId.predefined(IntegerAttribute.class, "jbr.visa.round");
	public final ObjectId fileRoundAttrId = ObjectId.predefined(IntegerAttribute.class, "jbr.version");
	public final ObjectId visaSetAttrId = ObjectId.predefined(CardLinkAttribute.class, "jbr.visa.set");
	public final ObjectId visaDecisionAttrId = ObjectId.predefined(HtmlAttribute.class, "jbr.visa.decision");

	public final ObjectId draftStateId = ObjectId.predefined(CardState.class, "draft");
	public final ObjectId visaCancelledStateId = ObjectId.predefined(CardState.class, "jbr.visa.cancelled");
	
	public final ObjectId signSetAttrId = ObjectId.predefined(CardLinkAttribute.class, "jbr.sign.set");
	public final ObjectId signFileSetAttrId = ObjectId.predefined(CardLinkAttribute.class, "jbr.sign.attachments");
	
	public static final ObjectId VISA_HISTORY_ATTRIBUTE_ID = ObjectId.predefined(HtmlAttribute.class, "jbr.visa.decision");
	public static final ObjectId VISA_NEGOTIATION_ROUND_ATTRIBUTE_ID = ObjectId.predefined(IntegerAttribute.class, "jbr.visa.visa.round");
	public static final ObjectId SIGN_HISTORY_ATTRIBUTE_ID = ObjectId.predefined(HtmlAttribute.class, "jbr.sign.comment");	
	
	private static ObjectId dspAttrId = ObjectId.predefined(ListAttribute.class, "jbr.dsp");	
	private static ObjectId dspValueId = ObjectId.predefined(ReferenceValue.class, "jbr.isDsp");
	
	@SuppressWarnings("unused")
	private Attribute attr = null;
	//private PortletRequest request = null;
	//private CardPortletSessionBean sessionBean = null;
	//private CardPortletCardInfo cardInfo = null;	
	private Card documentCard = null;
	private boolean isRoundExists = false;
	private int currentRound = 0;
	private DataServiceBean serviceBean = null;
	
	/**
	 * ������������ �������� ��� ������� ����� �������� �������� � ���-���������
	 * @author ppolushkin
	 *
	 */
	enum ChildTemplates {
		VISA, SIGN
	}
	
	/**
	 * ����� ��� �������� �������� �� ��������
	 * setAttrId - �������� �� �������� �������� (���� ��� ����)
	 * fileSetAttrId - �������� �� �������� � �������� ��������
	 * historyAttrId - ������� ������� ������������/���������� � �������� ��������
	 * neededStates - ������ ��������, � ���� �� ������� ������ ���� ������� �������� �������� �� ������ �������� 
	 * 			      ��� ���� ����� �� �������� ������������ � ���-���������
	 * @author ppolushkin
	 *
	 */
	class TemplatesSettings {
		
		ObjectId setAttrId;
		ObjectId fileSetAttrId;
		ObjectId historyAttrId;
		List<String> neededStates;
		
		List<String> getNeededStates() {
			return neededStates;
		}
		void setNeededStates(List<String> neededStates) {
			this.neededStates = neededStates;
		}
		ObjectId getSetAttrId() {
			return setAttrId;
		}
		void setSetAttrId(ObjectId setAttrId) {
			this.setAttrId = setAttrId;
		}
		ObjectId getFileSetAttrId() {
			return fileSetAttrId;
		}
		void setFileSetAttrId(ObjectId fileSetAttrId) {
			this.fileSetAttrId = fileSetAttrId;
		}
		ObjectId getHistoryAttrId() {
			return historyAttrId;
		}
		void setHistoryAttrId(ObjectId historyAttrId) {
			this.historyAttrId = historyAttrId;
		}
	}
	
	/**
	 * ����� �������� - ��������
	 */
	Map <ChildTemplates, TemplatesSettings> settings;
	
	
	
	
	/*public FilesAndCommentsUtils(Attribute attr, PortletRequest request) {
		this.attr = attr;
		this.request = request;
		sessionBean = CardPortlet.getSessionBean(request);
		cardInfo = sessionBean.getActiveCardInfo();	
		documentCard = cardInfo.getCard();
		init();
	}*/
	
	public FilesAndCommentsUtils(Card documentCard, Attribute attr, DataServiceBean serviceBean){
		this.attr=attr;
		this.documentCard=documentCard;
		this.serviceBean = serviceBean;
		init();
	}
	
	private void init(){
		
		IntegerAttribute visaRoundAttr = (IntegerAttribute)documentCard.getAttributeById(visaRoundAttrId);
		if (visaRoundAttr != null) {
			currentRound = visaRoundAttr.getValue();
			isRoundExists = true;
		}
		
		settings = initSettings();		
	}
	
	
	/**
	 * ��������� ������ �������� �������� - ���������
	 * @return ������� Map ��������
	 */
	private Map<ChildTemplates, TemplatesSettings> initSettings() {
		
		Map<ChildTemplates, TemplatesSettings> settings
			= new HashMap<FilesAndCommentsUtils.ChildTemplates, FilesAndCommentsUtils.TemplatesSettings>();
		
		TemplatesSettings template = new TemplatesSettings();
		template.setSetAttrId(visaSetAttrId);
		template.setFileSetAttrId(attr.getId());
		template.setHistoryAttrId(VISA_HISTORY_ATTRIBUTE_ID);
		List<String> neededStates = new ArrayList<String>();
		neededStates.add(draftStateId.getId().toString());
		neededStates.add(visaCancelledStateId.getId().toString());
		template.setNeededStates(neededStates);
		
		settings.put(ChildTemplates.VISA, template);
		
		template = new TemplatesSettings();
		template.setSetAttrId(signSetAttrId);
		template.setFileSetAttrId(signFileSetAttrId);
		template.setHistoryAttrId(SIGN_HISTORY_ATTRIBUTE_ID);
		neededStates = new ArrayList<String>();
		neededStates.add(draftStateId.getId().toString());
		template.setNeededStates(neededStates);
		
		settings.put(ChildTemplates.SIGN, template);
		
		return settings;
	}
	
	public int getCurrentRound() {
		return currentRound;
	}
	
	public boolean isRoundExists() {
		return isRoundExists;
	}
	
	public RoundDataFiles loadLinkedData() throws Exception {
		return loadLinkedData(true);
	}

	public RoundDataFiles loadLinkedData(boolean hideChildren) throws Exception {
		RoundDataFiles roundDataFiles = new RoundDataFiles();
		try {
			//final CardPortletCardInfo cardInfo = sessionBean.getActiveCardInfo();	
			FilesAndCommentsUtils.RoundData[] roundDataArray = new FilesAndCommentsUtils.RoundData[currentRound];
			for (int i = 0; i < currentRound; i++) {
				roundDataArray[i] = new FilesAndCommentsUtils.RoundData(i + 1);
			}
			
			final CardLinkAttribute fileSetAttr = documentCard.getAttributeById(attr.getId());
			SearchResult fileSetResult = loadFileListByFilter(fileSetAttr, fileSetAttr.getLinkedIds(), serviceBean);
			List<Card> fileCardList = fileSetResult.getCards(); 
			for (int i = 0; i < fileCardList.size(); i++) {
				Card fileCard = fileCardList.get(i);
				IntegerAttribute fileRoundAttr = fileCard.getAttributeById(fileRoundAttrId);
				if (fileRoundAttr == null) {
					roundDataArray[0].fileCardList.add(fileCard);
				}
				else if (fileRoundAttr.getValue() == 0) {
					roundDataArray[currentRound - 1].fileCardList.add(fileCard);
				}
				else {
					roundDataArray[fileRoundAttr.getValue() - 1].fileCardList.add(fileCard);
				}
			}
			if(!hideChildren){
				for(ChildTemplates ct : ChildTemplates.values()) {
					
					fillRoundArray(roundDataArray, ct);
				}
			}
			
			roundDataFiles.setRoundDatas(roundDataArray);
			roundDataFiles.setColumns(extracted(fileSetResult));
			
			
			/*cardInfo.setAttributeEditorData(attr.getId(), ROUND_DATA_ARRAY, roundDataArray);
			cardInfo.setAttributeEditorData(attr.getId(), LinkedCardUtils.ATTR_LINK_COLUMNS_LIST, fileSetResult.getColumns());*/
		}
		catch (Exception e) {
			logger.error("Error adding card links", e);
			throw new Exception(e);
			/*if (sessionBean != null)
				sessionBean.setMessageWithType("edit.link.error.create", new Object[] { e.getMessage() }, PortletMessageType.ERROR);*/
		}
		return roundDataFiles;
		
	}

	private List<Column> extracted(SearchResult fileSetResult) {
		return (List<Column>) fileSetResult.getColumns();
	}
	
	/**
	 * ������������ ��� ������ �������� �������� ��������, ������� ���� ��������� �� ��� �� �������� ����� � ���������� �� ���������
	 * ����� ���������� �������� �������� � ������, ������ ������� �������� ������������ ������������ �������� ��� ���������
	 * @param roundDataArray - ������ ��� �������� ���� ��� ����� ���������
	 * @param template - ��������� ������ �������� �������� ��� ������� ���� �������� �������� � ���-���������
	 */
	private void fillRoundArray(FilesAndCommentsUtils.RoundData[] roundDataArray, ChildTemplates template) 
			throws DataException, ServiceException, ParserConfigurationException, UnsupportedEncodingException, SAXException, IOException {
		
		TemplatesSettings ts = settings.get(template);
		
		final CardLinkAttribute setAttr = (CardLinkAttribute)documentCard.getAttributeById(ts.getSetAttrId());
		ObjectId[] idArray = setAttr.getIdsArray();
		if (idArray != null) {
			for (int i = 0; i < idArray.length; i++) {
				RoundVisaData[] dataArray = getVisaNegativeResults(idArray[i], serviceBean, ts);
				for (int k = 0; k < dataArray.length; k++) {
					addDataList(roundDataArray, dataArray[k]);
				}
			}
		}
	}
	
	/**
	 * ������ ���������� ���������� ������ � ��������� � ������ ��������
	 * @param roundDataArray - ������ ��� ��������
	 * @param data - ������ � ��������� �������� ��������
	 */
	private void addDataList(FilesAndCommentsUtils.RoundData[] roundDataArray, RoundVisaData data) {
		
		if (data.round == 0) {
			roundDataArray[currentRound - 1].visaDataList.add(data);
		}
		else {
			roundDataArray[data.round - 1].visaDataList.add(data);
		}
	}
	
	
	protected SearchResult loadFileListByFilter( 
			CardLinkAttribute attr,
			String linkedIds,
			DataServiceBean dataService ) throws UnsupportedEncodingException, DataException, ServiceException {

		Search search = attr.getFilter();
		if (search != null) {
			search = search.makeCopy();
		}
		else {
			search = new Search();
			if (attr.getFilterXml() != null) {
				search.initFromXml(new ByteArrayInputStream(attr.getFilterXml().getBytes("UTF-8")));
			}
		}

		search.setByAttributes(false);
		search.setByMaterial(false);
		search.setByCode(true);  
		search.setWords(linkedIds);

		if (search.getColumns() == null) {
			search.setColumns(new ArrayList<Column>(1));
		}
		final SearchResult.Column col = new SearchResult.Column();
		col.setAttributeId(fileRoundAttrId);
		if (!search.getColumns().contains(col)) {
			search.getColumns().add(col);
		}

		SearchResult result = dataService.doAction(search);

		result.getColumns().remove(col);
		
		Iterator<Card> i = result.getCards().iterator();
		
		while(i.hasNext()){
			Card c = i.next();
			try{
				serviceBean.getById(c.getId());
			} catch (DataException e){
				i.remove();
			}
		}
		
		return result;
	}

	/**
	 * �������� �������� �������� ��������, ������� ���� ��������� �� ��� �� �������� ����� � ���������� �� ���������
	 * @param visaId - id �������� ��������
	 * @param dataService
	 * @param ts - ��������� �������� �������
	 * @return ������ ������ � ��������� ���������
	 */
	protected RoundVisaData[] getVisaNegativeResults(ObjectId visaId, DataServiceBean dataService, TemplatesSettings ts) 
			throws DataException, ServiceException, ParserConfigurationException, UnsupportedEncodingException, SAXException, IOException {
		ArrayList<RoundVisaData> result = new ArrayList<RoundVisaData>();
		
		//�������� ����, �������� ��� ����, ����� �� ���������� ������ �� ���
		Card visa = null;
		try{
			visa = (Card)dataService.getById(visaId);
		}catch(DataException ex){
			//������ �� ������, ������ ��� ���� �� ��� ����
			logger.warn("Not get visa with id=" + visaId + " " + ex.getMessage());
		}

		if (visa != null){
			//�������� �������� ������ ����
			Hashtable<Integer, ArrayList<Card>> filesByRound = new Hashtable<Integer, ArrayList<Card>>();
			CardLinkAttribute filesAttr = visa.getAttributeById(ts.getFileSetAttrId());
			if (filesAttr.getIdsArray() != null){
				for (int i=0; i<filesAttr.getIdsArray().length; i++){
					ObjectId fileCardId = filesAttr.getIdsArray()[i];
					//�������� �������� �������� ����� �������� ��������
					Card fileCard;
					try{
						fileCard = (Card)dataService.getById(fileCardId);
					} catch (DataException ex){
						logger.info(ex.getMessage());
						continue;
					}
					IntegerAttribute fileRoundAttr = fileCard.getAttributeById(fileRoundAttrId);
					int round = 1;
					if (fileRoundAttr != null) {
						if (fileRoundAttr.getValue() != 0) {
							round = fileRoundAttr.getValue();
						}
						else {
							round = currentRound;
						}	
					}
					//��������� ���� � ��������� ������ �� ������� ��������
					ArrayList<Card> filesOnRound = filesByRound.get(round);
					if (filesOnRound == null){
						filesOnRound = new ArrayList<Card>();
						filesByRound.put(round, filesOnRound);
					}
					filesOnRound.add(fileCard);
				}
			}
			
			//�������� ������� ������� � ��� ��������
			HtmlAttribute visaHistory = visa.getAttributeById(ts.getHistoryAttrId());
			String visaHistoryValue = visaHistory.getValue(); 
			
			//������ �������� �������� �������
			Document xmldoc;
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = dbf.newDocumentBuilder();
			if (visaHistoryValue != null && visaHistoryValue.length() != 0){
				xmldoc = builder.parse(new ByteArrayInputStream(visaHistoryValue.getBytes("UTF-8")));
			}else{
				xmldoc = builder.parse(new ByteArrayInputStream("<?xml version=\"1.0\" encoding=\"UTF-8\"?><report/>".getBytes("UTF-8")));
			}
			Element root = xmldoc.getDocumentElement();
			
			//���������� ���� �� ����� XML
			NodeList list = root.getChildNodes();
			for (int i=0; i<list.getLength(); i++){
				Node node = list.item(i);
				if (node.getAttributes().getNamedItem("to-state") != null){
					String toStateId = node.getAttributes().getNamedItem("to-state").getTextContent();
					if (ts.getNeededStates().contains(toStateId)) {
		
						String roundStr = node.getAttributes().getNamedItem("round").getTextContent();
						Integer round = Integer.parseInt(roundStr);
						
						Node mainRoundNode = node.getAttributes().getNamedItem("main-round");
						Integer mainRound = parseMainRound(mainRoundNode, round);
						
						RoundVisaData visaData = new RoundVisaData(String.valueOf(visaId.getId()), mainRound);
						visaData.userName = node.getAttributes().getNamedItem("fact-user").getTextContent();
						visaData.comment = node.getTextContent();
						
						//��������� �����, ��������� �� �������� ����� �� ����� � ���������
						ArrayList<Card> fileCards = filesByRound.get(round);
						if (fileCards != null && fileCards.size() > 0){
							visaData.fileCardList = fileCards;
						}
						
						result.add(visaData);
					}
				}
			}
		}
		
		return (RoundVisaData[])result.toArray(new RoundVisaData[result.size()]);
	}
	
	
	/**
	 * ������ �������� "main-round". ���� �� ������, �� ���������� �������� �������� ���������, � �� ���������
	 * @param mainRoundNode - ���� ��� �����
	 * @param round - ����� �������� ������� ��������
	 * @return ����� �������� ���-���������
	 */
	private Integer parseMainRound(Node mainRoundNode, Integer round) {
		
		String mainRoundStr = mainRoundNode != null ? mainRoundNode.getTextContent() : null;
		if (mainRoundStr != null && Integer.parseInt(mainRoundStr) != round)
			return Integer.parseInt(mainRoundStr);
		else
			return round;
	}
	
	public boolean isDSP(Card c){
		ListAttribute dspAttribute = (ListAttribute)c.getAttributeById(dspAttrId);
		return dspAttribute != null && dspAttribute.getValue() != null && dspValueId.equals(dspAttribute.getValue().getId());

	}
	
	public class RoundDataFiles{
		private RoundData[] roundDatas = new RoundData[0];
		private List<Column> columns = new ArrayList<SearchResult.Column>();
		
		public RoundDataFiles(){
			
		}

		public RoundData[] getRoundDatas() {
			return roundDatas;
		}

		public void setRoundDatas(RoundData[] roundDatas) {
			this.roundDatas = roundDatas;
		}
		
		public List<Column> getColumns() {
			return columns;
		}

		public void setColumns(List<Column> columns) {
			this.columns = columns;
		}
		
		public void reset(){
			this.columns.clear();
			this.roundDatas = new RoundData[0];
		}
		
	}
	
	

	public class RoundData {
		public int round = 0;
		public List<Card> fileCardList = new ArrayList<Card>();
		public List<RoundVisaData> visaDataList = new ArrayList<RoundVisaData>();
		
		public RoundData(int round) {
			this.round = round;
		}
	}

	public class RoundVisaData {
		public List<Card> fileCardList = new ArrayList<Card>();
		public String comment = null;
		public String visaId = null;
		public String userName = null;
		public int round = 0;
		
		public RoundVisaData(String visaId, int round) {
			this.visaId = visaId;
			this.round = round;
		}
	}
}

