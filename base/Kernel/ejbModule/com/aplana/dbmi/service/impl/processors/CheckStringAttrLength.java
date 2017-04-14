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
package com.aplana.dbmi.service.impl.processors;

import java.util.Collection;
import java.util.Iterator;

import org.springframework.jdbc.core.JdbcTemplate;

import com.aplana.dbmi.action.ChangeState;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.StringAttribute;
import com.aplana.dbmi.model.TemplateBlock;
import com.aplana.dbmi.model.TextAttribute;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.DatabaseClient;
import com.aplana.dbmi.service.impl.ProcessorBase;
import com.aplana.dbmi.service.impl.UserData;
import com.aplana.dbmi.service.impl.Validator;

/**
 * ���������, ����������� ����� ��������� �����
 * @author echirkov
 *
 */
public class CheckStringAttrLength extends ProcessorBase implements DatabaseClient, Validator {

	private static final long serialVersionUID = 1L;
	private JdbcTemplate jdbc;
	
	public static int MAX_TEXT_LENGTH = 4000;

	@Override
	public Object process() throws DataException {
		Card card = getCard();
		for(Iterator i = card.getAttributes().iterator(); i.hasNext(); ) 
		{
			final TemplateBlock block = (TemplateBlock)i.next();
			final Iterator<Attribute> j = block.getAttributes().iterator();
			while (j.hasNext()) {
				final Attribute a = j.next();
				if(a.getClass().equals(StringAttribute.class) || a.getClass().equals(TextAttribute.class)){
					if(a.getStringValue() != null && a.getStringValue().length() > MAX_TEXT_LENGTH){
						throw new DataException("jbr.overflow.text.length", new Object[] {a.getName(), MAX_TEXT_LENGTH});
					}
				}
			}
		}
		return null;
	}

	public void setJdbcTemplate(JdbcTemplate jdbc) {
		this.jdbc = jdbc;
	}
	
	public JdbcTemplate getJdbcTemplate() {
		return jdbc;
	}

	/**
	 * �������� �������� �������� ��� ������� ����������.
	 * ���� ��� ��������, �� ���� id, �������� ����������� (����������� fetch)
	 * �� ����� ���������� ������������.
	 * @param user: ������������ �� ����� �������� ����������� ��������
	 * ������������� ��� �������� ��������, ���� null - �� �� ����� ����������
	 * ������������ (!).
	 * @param attrToChk: ���� �����, �� ���� ������� ����� �����������
	 * ����������� �������� (���������� ������� ��������), �.�., ���� �����
	 * �� ������ �������� ��������, � ������� �� ����� ����� �������� - ��������
	 * ������ ��������� �������� � ����� ��������� �� ����;
	 * ���� ������ null, �� ����� �������� �������� �������� ����� ��������� ������.
	 * @return (������) �������� ��� null, ���� ��� �������� �������� � �� �����
	 * id ��� �������� ���������� (��������, ��� ��������).
	 * @throws DataException
	 */
	public Card getCard( UserData user)
		throws DataException
	{
		if (user == null)
			user = getSystemUser();

		// ��������� �������� �������� � ������ ��������� (����� ���� � ��� ��
		// ��������� ��� ��������� � ������ ���� ���������:
		// Store/ChangeState, pre/post.
		Card acard = null;
		boolean isObject = false;
		boolean isAction = false;
		if (getObject() instanceof Card) {
			isObject = true;
			acard = (Card) getObject();
		} else if (getAction() instanceof ChangeState) {
			isAction = true;
			acard = ((ChangeState) getAction()).getCard();
		} else if (getResult() instanceof Card){
			acard = (Card) getResult();
		}

		return acard;
	}

	/**
	 * �������� �������� �������� ��� ������� ����������. ��� �������������
	 * �������� ���������� �� ����� ���������� ������������.
	 * @return ������ �������� (������� �������� ����������� ����� ��
	 * ������� �������� {@link Attribute.ID_CHANGE_DATE}).
	 * @throws DataException
	 */
	public Card getCard() throws DataException
	{
		return getCard( getSystemUser());
	}

}