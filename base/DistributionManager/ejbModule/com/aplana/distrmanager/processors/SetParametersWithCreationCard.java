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
package com.aplana.distrmanager.processors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aplana.dbmi.jbr.processors.PopulateChildrenWithConditions;
import com.aplana.dbmi.jbr.util.IdUtils;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.DataObject;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.ReferenceValue;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.client.DataServiceFacade;
import com.aplana.dbmi.service.impl.ObjectQueryBase;
import com.aplana.distrmanager.action.ParametersAction;
import com.aplana.distrmanager.exceptions.DistributionManagerException;
import com.aplana.distrmanager.handlers.InitExportBeanFactory;
import com.aplana.distrmanager.util.IdStringPair;

/**
 * @author PPanichev
 * ����������� ���������, ����������� �� PopulateChildrenWithConditions ({@link PopulateChildrenWithConditions}) ��� �������� ���������� ����� ������-���� ParametersAction ({@link ParametersAction}).
 *
 * ��������� ����������:
 * - setValueInList: �������� ��� �������� ReferenceValue ��������.
 *   � �������� �������� ������ ���� ������ ����� �� objectids.properties.
 *
 * - setCurrentCardParameters: �������� ��� �������� ������ ����� ���������, ������������ � ��������, ������ ����� ����������� ������ ��������.
 *   �������� ������ ���� ��������� �������� ";".
 *   � �������� �������� ������ ���� ������ ����� �� objectids.properties.
 *
 * - setValuesForParameters: ���� ��������/��������, ��� ��������� ����������� ��������, ����������� ��������� � ������� ��������.
 *   ���� ������ ���� ��������� �������� ";".
 *   � �������� �������� ��������� ������ ���� ������ ����� �� objectids.properties.
 *   � �������� �������� ����������� ���������, ����������� ���������� �������� ��� ����� �������� �� objectids.properties.
 */

public class SetParametersWithCreationCard extends PopulateChildrenWithConditions {

	private final Log logger = LogFactory.getLog(getClass());

	private static final long serialVersionUID = 11L;

	private static final String ERROR_DO_PROCESS= "jbr.DistributionManager.SetParametersWithCreationCard.doProcess.error";

	public static final String SET_VALUE_IN_LIST = "setValueInList";
	public static final String SET_CURRENT_CARD_PARAMETERS = "setCurrentCardParameters";
	public static final String SET_VALUES_FOR_PARAMETERS = "setValuesForParameters";
	public static final String PARAMETERS_PARAM = "param_";

	protected static final String SET_SEPARATOR = "[=]";
	protected static final String PARAM_SEPARATOR = "[,;]";

	private Card currentCard = null;
	private ObjectId refValueId = null;
	private ParametersAction.Result res = null;
	private DataServiceFacade serviceBean = null;
	private String[] listParam = null;
	private List<IdStringPair> setAttrPairsList = new ArrayList<IdStringPair>();
	private Map<String, String> parameters = new HashMap<String, String>();

	@Override
	public Object process() throws DataException {
		try {
			super.process();
			final Card card = loadCard(getCardId());
			ObjectId actionCardId = super.childs.get(0);
			currentCard = card;
			ParametersAction paramAction = new ParametersAction();
			paramAction.setCardId(actionCardId);
			res = ParametersAction.Result.instance();
			if (null != refValueId)
				setInActionRefValue();
			if (null != listParam)
				setParameters();
			if (null != setAttrPairsList && !setAttrPairsList.isEmpty())
				setValuesForParameters();
			res.fillParametersByMap(parameters);
			paramAction.setResult(res);
			serviceBean = InitExportBeanFactory.instance().initServiceBean(getBeanFactory());
			serviceBean.doAction(paramAction);
		} catch (DistributionManagerException eme) {
			logger.error(ERROR_DO_PROCESS, eme);
			throw eme;
		} catch(Exception e) {
			logger.error(ERROR_DO_PROCESS, e);
			throw new DataException(e);
		}
		return null;
	}

	@Override
	public void setParameter(String name, String value) {
		if (SET_VALUE_IN_LIST.equalsIgnoreCase(name)) {
			refValueId = IdUtils.smartMakeAttrId(value.trim(),
					ReferenceValue.class);
		} else if (SET_CURRENT_CARD_PARAMETERS.equalsIgnoreCase(name)) {
			// ��������� ������ �� ������ ���������
			listParam = value.trim().split(PARAM_SEPARATOR);
		} else if (SET_VALUES_FOR_PARAMETERS.equalsIgnoreCase(name)) {
			String[] list = value.trim().split(REG_SEPARATOR); // ��������� ������ �� ������ ����
			for (String setRule : list){
				String[] pairString = setRule.split(SET_SEPARATOR); // ��������� ���� ��: �������, ��������.
				if (pairString.length != 2){ // ���� ��� ���� ��� ��� �����������
					logger.warn("Broken setValuesForParameters rule: "+setRule+" Skipping.");
					continue;
				}
				final IdStringPair pair = new IdStringPair();
				pair.dest.setId( IdUtils.smartMakeAttrId( pairString[0].trim(), null, false));
				if (pair.destId() == null) {
					logger.warn("Broken setValuesForParameters rule: "+setRule+" Can not determine " +
						"destination attribute: "+pairString[0]+" Skipping.");
					continue;
				}
				if (!"NULL".equalsIgnoreCase(pairString[1])){
					pair.setVal(pairString[1].trim());
					if (pair.getVal() == null){
						logger.warn("Broken setValuesForParameters rule: "+setRule+" Can not determine " +
							"source attribute/value: "+ pairString[1]+" Skipping.");
						continue;
					}
				}
				setAttrPairsList.add(pair);
			}
		} else if (name.startsWith(PARAMETERS_PARAM)) {
			parameters.put(name.substring(PARAMETERS_PARAM.length()), value);
		} else {
			super.setParameter(name, value);
		}
	}

	private void setInActionRefValue() {
		ReferenceValue refVal = (ReferenceValue)DataObject
				.createFromId(refValueId);
		res.setRefValue(refVal);
	}

	private void setParameters() {
		for (String param : listParam) {
			if(null != param) {
				ObjectId paramId = IdUtils.smartMakeAttrId(param.trim(), null, false);
				Attribute attr = currentCard.getAttributeById(paramId);
				res.addListParameter(attr);
			}
		}
	}

	private void setValuesForParameters() {
		res.setPair(setAttrPairsList);
	}

	@Override
	protected Card loadCard(ObjectId id) throws DataException
	{
		final ObjectQueryBase query = getQueryFactory().getFetchQuery(Card.class);
		query.setId(id);
		return (Card) getDatabase().executeQuery(getSystemUser(), query);
	}
}
