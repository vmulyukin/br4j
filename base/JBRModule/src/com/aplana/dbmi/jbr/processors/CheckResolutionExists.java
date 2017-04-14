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
package com.aplana.dbmi.jbr.processors;

import com.aplana.dbmi.action.ChangeState;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.service.DataException;

/**
 * ������������ ��������� �������� ������� ��������� ��� ���������. �� �������� ������� �������� ������������.
 * @author larin
 *
 */
public class CheckResolutionExists extends ProcessCard{ 

	//��������� �� ������, ���������� ����� �������� ����������
	protected static final String PARAM_EMPTY_MESSAGE = "empty_message";
	protected String emptyMessage;

	@Override
	public Object process() throws DataException {
		Card actionCard = ((ChangeState) getAction()).getCard();
		
		//������ �� ��������� ���������� ���������
		/*String query = "select count(*) "; 
		query += "from attribute_value ave ";
		query += "inner join attribute_value avv on (ave.card_id = avv.number_value) ";
		query += "where ave.attribute_code = 'JBR_IMPL_ACQUAINT' and ave.number_value = " + actionCard.getId().getId() + " ";
		query += "and avv.attribute_code = 'JBR_DOCB_BYDOC'";*/

		StringBuilder query = new StringBuilder();
		query.append("select count(*) \n"); 
		query.append("from attribute_value ave \n");
		query.append("inner join attribute_value avv on ave.card_id = avv.number_value and avv.attribute_code = 'JBR_DOCB_BYDOC' \n");
		query.append("inner join card c_res on c_res.card_id = avv.card_id and c_res.template_id = 324 and c_res.status_id in (1,103,206) \n");
		query.append("inner join attribute_value av_rasm on av_rasm.card_id = ave.number_value and av_rasm.attribute_code = 'JBR_RASSM_PERSON' \n");
		query.append("inner join attribute_value av_res on av_res.card_id = avv.card_id and av_res.attribute_code = 'JBR_INFD_SGNEX_LINK' \n");
		query.append("where ave.attribute_code = 'JBR_IMPL_ACQUAINT' and ave.number_value = ").append(actionCard.getId().getId()).append(" \n");
		query.append("and av_rasm.number_value = av_res.number_value \n");
		
		int resCount = getJdbcTemplate().queryForInt(query.toString());
		
		//��������� ������ ���� ��������� ���
		if (resCount == 0){
			if(emptyMessage != null) {
				throw new DataException(emptyMessage);
			}
			throw new DataException("resolution.not.found");
		}
		
		return null;
	}

	@Override
	public void setParameter(String name, String value) {
		if (PARAM_EMPTY_MESSAGE.equalsIgnoreCase(name)){
			emptyMessage = value.trim(); 
}
		super.setParameter(name, value);
	}

}
