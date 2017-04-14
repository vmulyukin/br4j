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
package com.aplana.cms.tags;

import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aplana.cms.ContentProducer;
import com.aplana.cms.Tag;
import com.aplana.cms.TagProcessor;
import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.DateAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.model.PersonAttribute;

public class PersonalControlDataTag implements TagProcessor {

	private static final ObjectId BOSS_CONTROL_INSPECTOR = ObjectId.predefined(PersonAttribute.class, "jbr.boss.control.inspector");
	private static final ObjectId BOSS_CONTROL_DOCUMENT = ObjectId.predefined(CardLinkAttribute.class, "jbr.boss.control.document");
	private static final ObjectId BOSS_CONTROL_DATE = ObjectId.predefined(DateAttribute.class, "jbr.boss.control.date");
	private static final String ATTR_FORMAT = "format";
	private static final String ATTR_HIDE_DATE = "hide";
	private static final String PERSONAL_CONTROL_FOLDER_ID = "8544";
	
	private boolean isOnPersonalControl = false;
	private DateAttribute controlDateAttr;

	protected Log logger = LogFactory.getLog(getClass());

	public boolean prepareData(Tag tag, Card item, ContentProducer cms) throws Exception {
		checkForExistsPrivateControl(item.getId(), cms);
		return true;
	}

	public void writeHtml(PrintWriter out, Tag tag, Card item, ContentProducer cms) {
		StringBuilder theBuilder = new StringBuilder(200);
		theBuilder.append("<input id=\"onPersonalControl");
		theBuilder.append(item.getId().getId());
		theBuilder.append("\" type=\"hidden\" value=\"");
		theBuilder.append(isOnPersonalControl);
		theBuilder.append("\"/>");

		boolean hideDate = Boolean.parseBoolean(tag.getAttribute(ATTR_HIDE_DATE));
		if(!hideDate) {
			String currArea = (String) cms.getVariable(ContentProducer.VAR_AREA);
			if (PERSONAL_CONTROL_FOLDER_ID.equals(currArea) && tag.hasAttribute(ATTR_FORMAT)) {
				if (controlDateAttr != null) {
					String pattern = tag.getAttribute(ATTR_FORMAT);
					String date = new SimpleDateFormat(pattern).format(controlDateAttr.getValue());
					theBuilder.append("��: ");
					theBuilder.append(date);
				}
			}
		}

		out.write(theBuilder.toString());
	}

	/**
	 * ��������� ������� � ��������� �������� "������ ��������".
	 * 
	 * @param baseCardId
	 * @param cms
	 */
	private void checkForExistsPrivateControl(ObjectId baseCardId, ContentProducer cms) throws Exception {

		// ���� �������� ������� ��������� � ������� ������� ��� ������
		// �������� � ������������� �������� �������� ������������ �
		// "������ �� �������� ��������" �������� id �������� ���������
		final Search search = new Search();
		search.setByAttributes(true);
		List<SearchResult.Column> columns = new ArrayList<SearchResult.Column>(2);
		SearchResult.Column col = new SearchResult.Column();
		col.setAttributeId(BOSS_CONTROL_DOCUMENT);
		columns.add(col);
		col = new SearchResult.Column();
		col.setAttributeId(BOSS_CONTROL_INSPECTOR);
		columns.add(col);
		col = new SearchResult.Column();
		col.setAttributeId(BOSS_CONTROL_DATE);
		columns.add(col);
		search.setColumns(columns);

		final Person curPerson = (Person) cms.getService().getById(Person.ID_CURRENT);
		search.addPersonAttribute(BOSS_CONTROL_INSPECTOR, curPerson.getId());
		search.addCardLinkAttribute(BOSS_CONTROL_DOCUMENT, baseCardId);

		try {
			List searchCards = cms.searchCards(search);
			if (searchCards != null && searchCards.size() > 0) {
				Object controlCard = searchCards.get(0);

				controlDateAttr = (DateAttribute) ((Card) controlCard).getAttributeById(BOSS_CONTROL_DATE);
				isOnPersonalControl = true;
			}
		} catch (Exception e) {
			logger.error("Error on personal control check: " + e);
		}
	}
}
