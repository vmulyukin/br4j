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
package com.aplana.dbmi.module.recalczonedow;

import com.aplana.dbmi.action.ChangeZonesOfDocsForUser;
import com.aplana.dbmi.action.xml.SearchXmlHelper;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.SchedulerParameter;
import com.aplana.dbmi.task.AbstractParametrizedTask;
import com.aplana.dbmi.task.TaskInfoBuilder;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.*;

/**
 * (2014/05/23, GoRik)
 * ��������, ������� ����� ������������� ���� ��� � ����������-����������, � ������� �����-�� ������� ��������� ������� ������������ 
 * (�� ��� ������������ �� ������� �������������)
 * @author ynikitin
 * ����� ������ ��������, ������� ��� ���������� ������ ������� ��������� ��������� ������� �� ����������� ������� ���������� scheduler_parameter 
 * ��� ���� (����������) ����������, ��������� � ���� ������� ����� ���� ��������� �� ������ ����� �������, �.�. � �������� ��� �������� API ��� ������ � �����������
 * 
 * ������ ���������� ��� ��������� ������ ����� ��������� ���:
 * <?xml version="1.0" encoding="UTF-8"?>
 * <config>
 * 		<cardId>123456</cardId>
 * 		<isDepartment>true</isDepartment>
 * </config>
 * , ��� ��� isDepartment - ��������������
 */
public class RecalculateZoneDOWTask extends AbstractParametrizedTask {
	private static final String EVENT_ACTION_NAME = "RECALC_ZONE_DOW";
	private static final Logger logger = Logger.getLogger(RecalculateZoneDOWTask.class); 
	
	public void process(Map<?, ?> parameters) {
		long startTime = System.currentTimeMillis();
		logger.debug("Start task RecalculateZoneDOWTask");

		try{			
			String taskId = null;
			if (parameters!=null){
				taskId = (String)parameters.get(TaskInfoBuilder.CURR_TASK_ID); 
			}
			
			// ������� ��������� ��������� �������
			List<SchedulerParameter> params = getTaskParameters(taskId);
			if (params==null||params.isEmpty()){
				logger.info("There is no parameters for start 'RecalculateZoneDOW'. Stop executing task.");
				return;
			}
			logger.info("There is "+params.size()+" for current task");
			
			List<Long> paramIds = new ArrayList<Long>();
			Set<ChangeZonesOfDocsForUser> execActions = new HashSet<ChangeZonesOfDocsForUser>();
			// ��������������� ������������ ������� ���������, ��� ���� ��� ������������ ������� �������� �������� �� ������������ (���� �������� ������ ���������� ����)  
			for(SchedulerParameter param : params){
				try {
					byte[] paramData = param.getParamValue();
					final String strParamData = new String(param.getParamValue(), "UTF-8"); 
					if (logger.isInfoEnabled())
						logger.info("Try change ZonesDOW by param "+param.getParamId() + ":\n"+new String(param.getParamValue(), "UTF-8")+"\n");
					ChangeZonesOfDocsForUser changeAction = parseData(paramData);
					if (changeAction==null||changeAction.getCardId()==null){
						logger.error("Incorrect param "+param.getParamId() + ":\n"+strParamData);
						continue;
					}
					// ���������, ��� �������������� �������� ����� ���� � ������� ����������� 
					if (execActions.contains(changeAction)){
						paramIds.add(param.getParamId());
						continue;
					}
					Long docCount = (Long)serviceBean.doAction(changeAction);
					if (docCount==null){
						docCount = 0l;
					}
					addInfoMessageInEventLog(changeAction.getCardId(), EVENT_ACTION_NAME, docCount.toString()+" documents are updated for input "+(changeAction.isDepartment()?"department":"person"), strParamData, true);
					execActions.add(changeAction);
					paramIds.add(param.getParamId());
				} catch (Exception e){
					logger.error("Error when try change ZonesDOW by param "+param.getParamId() + ":\n"+new String(param.getParamValue(), "UTF-8")+"\n"+e);
				}
			}
			// ����� ����, ��� ������� ��������� ���������� (�������� �� ��� �������), �������-������������ ��������� ������ �� ��, ����� ��� ��������� ������� ����� �� ��������� ��� �� �������������� ��������
			deleteParameters(taskId, paramIds);
		}catch(Exception ex){
			logger.error("Error on task 'RecalculateZoneDOW'", ex);
		}finally{
			logger.debug( "Finish processing task 'RecalculateZoneDOW': workingtime = "+ (System.currentTimeMillis()-startTime));
		}
	}	

	// ������ ������� ��������-xml � ���������� ��� � ����
	private ChangeZonesOfDocsForUser parseData(byte[] paramData){
		ChangeZonesOfDocsForUser action = new ChangeZonesOfDocsForUser();
		try{
			InputStream input = new ByteArrayInputStream(paramData);

			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(input);
			Element root = doc.getDocumentElement();
			if (!"config".equals(root.getNodeName()))
				throw new Exception("config element expected");
			Long cardId = Long.decode(SearchXmlHelper.getTagContent(root, "./cardId", null, null));
			String versionIdTagValue = SearchXmlHelper.getTagContent(root, "./lastVersionId", null, null);
			Long versionId = (versionIdTagValue != null && !versionIdTagValue.trim().isEmpty()) ? Long.decode(versionIdTagValue) : null;
			boolean isDepartment = Boolean.parseBoolean(SearchXmlHelper.getTagContent(root, "./isDepartment", null, null));
			boolean isZoneAccess = Boolean.parseBoolean(SearchXmlHelper.getTagContent(root, "./isZoneAccess", null, null));

			action.setCardId(new ObjectId(Card.class, cardId));
			action.setDepartment(isDepartment);
			action.setVersionId(versionId);
			action.setZoneAccess(isZoneAccess);
			return action;
		} catch(Exception ex){
			logger.warn("Error read 'RecalculateZoneDOWTask' config. Clear settings.", ex);
		}
		return null;
	}
}
