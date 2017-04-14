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
package com.aplana.dbmi.module.recalcRules;

import com.aplana.dbmi.action.UpdateRulesByRole;
import com.aplana.dbmi.action.xml.SearchXmlHelper;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Role;
import com.aplana.dbmi.model.SchedulerParameter;
import com.aplana.dbmi.task.AbstractParametrizedTask;
import com.aplana.dbmi.task.TaskInfoBuilder;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * ��������, ������� ������������� ����� ��� ���� ������������ (�� prole_id �� person_role)
 * <?xml version="1.0" encoding="UTF-8"?>
 * <config>
 * 		<proleId>20425</proleId>
 * </config>
 */
public class RecalculateRulesByRoleTask extends AbstractParametrizedTask {
	private static final String EVENT_ACTION_NAME = "RECALC_RULES_BY_ROLE";
	private static final Logger logger = Logger.getLogger(RecalculateRulesByRoleTask.class); 
	
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
				logger.info("There is no parameters for start 'RecalculateRulesByRole'. Stop executing task.");
				return;
			}
			logger.info("There is "+params.size()+" for current task");

			for(SchedulerParameter param : params){
				try {
					byte[] paramData = param.getParamValue();
					final String strParamData = new String(param.getParamValue(), "UTF-8"); 
					if (logger.isInfoEnabled())
						logger.info("Try Recalculate Rules By Role with param "+param.getParamId() + ":\n"+new String(param.getParamValue(), "UTF-8")+"\n");
					
					
					UpdateRulesByRole updateAction = parseData(paramData);
					if (updateAction==null||updateAction.getObjectId()==null){
						logger.error("Incorrect param "+param.getParamId() + ":\n"+strParamData);
						continue;
					}
					Long rowsUpdated = serviceBean.doAction(updateAction);
					
					addInfoMessageInEventLog(updateAction.getObjectId(), EVENT_ACTION_NAME, rowsUpdated+" rows updated by role, prole_id =  "+updateAction.getObjectId().getId(), strParamData, true);
					deleteParameters(taskId, Collections.singletonList(param.getParamId()));
				} catch (Exception e){
					logger.error("Error when try change ZonesDOW by param "+param.getParamId() + ":\n"+new String(param.getParamValue(), "UTF-8")+"\n"+e);
				}
			}
		}catch(Exception ex){
			logger.error("Error on task 'RecalculateRulesByRole'", ex);
		}finally{
			logger.debug( "Finish processing task 'RecalculateRulesByRole': workingtime = "+ (System.currentTimeMillis()-startTime));
		}
	}	

	// ������ ������� ��������-xml � ���������� ��� � ����
	private UpdateRulesByRole parseData(byte[] paramData){
		try{
			InputStream input = new ByteArrayInputStream(paramData);

			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(input);
			Element root = doc.getDocumentElement();
			if (!"config".equals(root.getNodeName()))
				throw new Exception("config element expected");
			Long roleId = Long.decode(SearchXmlHelper.getTagContent(root, "./proleId", null, null));
			
			return new UpdateRulesByRole(new ObjectId(Role.class, roleId));
		} catch(Exception ex){
			logger.warn("Error read 'RecalculateZoneDOWTask' config. Clear settings.", ex);
		}
		return null;
	}
}
