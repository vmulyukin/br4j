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
package com.aplana.ireferent.util;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aplana.ireferent.endpoint.impl.ObjectsGetterByConfig;
import com.aplana.ireferent.types.WSOCollection;
import com.aplana.ireferent.types.WSOContext;
import com.aplana.ireferent.types.WSOItem;
import com.aplana.ireferent.types.WSObject;

public class LocalCache {
	
	private static Log logger = LogFactory.getLog(LocalCache.class);
	
	private static final String SET_IDS_REMOVAL = "ForRemoval";
	
	private static WSOCollection idsClientCache;
	private static WSOCollection idsServerCache;
	
	private LocalCache() {
	}
	
	public static void add(WSOCollection idsClient, WSOCollection idsServer) {
		idsClientCache = idsClient;
		idsServerCache = idsServer;
	}
	
	public static void calculateDelta(WSOContext context) throws ParseException, DatatypeConfigurationException {
		logger.info("Calculate delta ...");
		if (null == idsClientCache || idsClientCache.getData().isEmpty()) {
			return;
		} else 
		if (null == idsServerCache || idsServerCache.getData().isEmpty()) {
			createClientCollection(idsClientCache.getData());
		} else {
			XMLGregorianCalendar syncDateIn = XmlUtils.getDateSyncIn(context);
			HashMap<Object, List<Object>> idsServClient = new HashMap<Object, List<Object>>();
			HashMap<Object, List<Object>> idsServ = new HashMap<Object, List<Object>>();	
			for(Object object : idsServerCache.getData()) {
				WSObject wsObject = (WSObject)object;
				List<Object> attr = new ArrayList<Object>();
				attr.add(wsObject.getId()); //0�
				attr.add(wsObject.getUpdDate()); //1�
				idsServClient.put(object, attr);
				idsServ.put(object, attr);
			}
			List<Object> idsClient = idsClientCache.getData();
			// �����������
			retainAll(idsServClient, idsClient);
			// ����� ��������� - � ������ �� ���������/����������
			idsServ.keySet().removeAll(idsServClient.keySet());
			// ����� ���������� - � ������ �� ��������
			removeAll(idsClient, idsServClient);
			// ���� DU < DS - ������� �� �����������
			Iterator<Object> itrServClient = idsServClient.keySet().iterator();
			while (itrServClient.hasNext()) {
				Object obj = itrServClient.next();
				XMLGregorianCalendar dateUpdate = (XMLGregorianCalendar)idsServClient.get(obj).get(1);
				if (dateUpdate.compare(syncDateIn) == DatatypeConstants.LESSER) {
					itrServClient.remove();
				}
			}
			// �������� ����������/���������
			HashMap<Object, List<Object>> idsServResume = new HashMap<Object, List<Object>>();
			idsServResume.putAll(idsServClient);
			idsServResume.putAll(idsServ);
			idsServerCache.getData().retainAll(idsServResume.keySet());
			createClientCollection(idsClient);
			logger.info("Calculate delta: SUCCESSFULLY.");
		}
	}
	
	private static void createClientCollection(List<Object> idsClient) {
		if (null == idsServerCache)
			idsServerCache = new WSOCollection();
		for (Object obj : idsClient) {
			WSObject wsObjClient = (WSObject) obj;
			addExtension(wsObjClient, SET_IDS_REMOVAL);
			idsServerCache.getData().add(wsObjClient);
		}
	}

	private static void addExtension(WSObject wsObject, String setId) {
		WSOItem setIdExtension = ExtensionUtils.createItem(ObjectsGetterByConfig.SET_ID_EXTENSION_ID,
				setId);
		ExtensionUtils.addExtensions(wsObject, setIdExtension);
	}
    // � idsServ ��������� ������ idsClient
	private static boolean retainAll(HashMap<Object, List<Object>> idsServ,
			List<Object> idsClient) {
		boolean modified = false;
		Iterator<Object> itrServ = idsServ.keySet().iterator();
		while (itrServ.hasNext()) {
			Object obj = itrServ.next();
			List<Object> listObj = idsServ.get(obj);
			if (!containsInClient(idsClient, listObj.get(0))) {
				itrServ.remove();
				modified = true;
			}
		}
		return modified;
	}
	
	private static boolean containsInClient(List<Object> idsClient, Object id) {
		if (id == null) {
			return false;
		} else {
			Iterator<Object> itrClient = idsClient.iterator();
			while (itrClient.hasNext()) {
				Object obj = itrClient.next();
				Object idClient = ((WSObject)obj).getId();
				if (id.equals(idClient))
					return true;
			}
		}
		return false;
	}
	
	// �� idsClient ������� idsServ
	private static boolean removeAll(List<Object> idsClient,
			HashMap<Object, List<Object>> idsServ) {
		boolean modified = false;
		Iterator<Object> itrClient = idsClient.iterator();
		while (itrClient.hasNext()) {
			if (containsInServ(idsServ, ((WSObject)itrClient.next()).getId())) {
				itrClient.remove();
				modified = true;
			}
		}
		return modified;
	}
	
	private static boolean containsInServ(HashMap<Object, List<Object>> idsServ, Object id) {
		if (id == null) {
			return false;
		} else {
			Iterator<Object> itrServ = idsServ.keySet().iterator();
			while (itrServ.hasNext()) {
				Object obj = itrServ.next();
				Object idServ = idsServ.get(obj).get(0);
				if (id.equals(idServ))
					return true;
			}
		}
		return false;
	}
}
