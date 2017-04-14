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
package com.aplana.dbmi.jasperreports;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aplana.dbmi.card.CertificateInfo;
import com.aplana.dbmi.crypto.CardDsInfoReader;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.ContextProvider;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.service.AsyncDataServiceBean;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.ServiceException;
import com.aplana.dbmi.service.UserPrincipal;

public class CardDSInfoReport {
	
	public static final String VISA_ATTR = "VISA_CERT_INFO";
	public static final String SIGNER_ATTR = "SIGNER_CERT_INFO";
	public static final String EXAMINER_ATTR = "EXAMINER_CERT_INFO";
	public static final String REPORT_ATTR = "REPORT_CERT_INFO";
	public static final String AUTHOR_ATTR = "AUTHOR_CERT_INFO";
	public static final String FILES_ATTR = "FILES_CERT_INFO";
	
	private Log log = LogFactory.getLog(getClass().getSimpleName());
	
	private List<CertInfoReportDecorator> certificateInfos = new ArrayList<CertInfoReportDecorator>();
	private Long cardId = null;
	private AsyncDataServiceBean serviceBean = null;
	private ResourceBundle bundle = null;
	
	
	public CardDSInfoReport(){
		
	}
	
	
	public JRDataSource getData(Long cardId){
		this.cardId = cardId;
		if(cardId==null){
			return new JRBeanCollectionDataSource(certificateInfos);
		}
		
		try{
			
			generateCertInfoData();
			
		}catch(Exception e){
			log.error(e);
		}
		return new JRBeanCollectionDataSource(certificateInfos);
		
		
	}
	
	
	@SuppressWarnings("rawtypes")
	private void generateCertInfoData() throws DataException, ServiceException{
	
		serviceBean = new AsyncDataServiceBean();
		serviceBean.setAddress("127.0.0.1");
		serviceBean.setUser(new UserPrincipal("__system__"));
		Card activeCard = (Card) serviceBean.getById(new ObjectId(Card.class, cardId));
		CardDsInfoReader infoReader = CardDsInfoReader.getInstance(serviceBean, activeCard, getBundle());
		Map map = infoReader.getCardDsInfo();
		
		certificateInfos.addAll(getCertInfoFromMap("ds.title.base.doc",AUTHOR_ATTR, map));
		certificateInfos.addAll(getCertInfoFromMap("ds.showinfo.file",FILES_ATTR, map));
		certificateInfos.addAll(getCertInfoFromMap("ds.showinfo.coordinator",VISA_ATTR, map));
		certificateInfos.addAll(getCertInfoFromMap("ds.showinfo.signer",SIGNER_ATTR, map));
		certificateInfos.addAll(getCertInfoFromMap("ds.showinfo.examiner",EXAMINER_ATTR, map));
		certificateInfos.addAll(getCertInfoFromMap("ds.showinfo.report", REPORT_ATTR, map));
		

	}
	

	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private List<CertInfoReportDecorator> getCertInfoFromMap(String templateKeyName,String keyTypeDoc, Map values){
		List<CertInfoReportDecorator> certificateInfos = new ArrayList<CertInfoReportDecorator>();
		Map tempMap = null;
		
		List<Map> certs = (List<Map>)values.get(keyTypeDoc);
		if(!isValid(certs)){
			return certificateInfos;
		}
		for(int i=0; i<certs.size(); i++){
			tempMap = (Map)certs.get(i);
			copyAndAppendType(certificateInfos, (List<CertificateInfo>) tempMap.get("VALUE"), templateKeyName);	
		}
		return certificateInfos;
	}
	
	private void copyAndAppendType(List<CertInfoReportDecorator> toList, List<CertificateInfo> fromList, String templateKeyName){
		String templateName = getBundle().getString(templateKeyName);
		CertInfoReportDecorator decorator = null;
		for (CertificateInfo certificateInfo : fromList) {
			decorator = new CertInfoReportDecorator(templateName, certificateInfo);
			toList.add(decorator);
		}
		
	}
	
	
	private boolean isValid(Object value){
		return value!=null;
	}
	
	private ResourceBundle getBundle(){
		if(bundle==null){
			bundle = ResourceBundle.getBundle("com.aplana.dbmi.archive.CardArchiveResource",ContextProvider.LOCALE_RUS);
		}
		return bundle;
	}

}
