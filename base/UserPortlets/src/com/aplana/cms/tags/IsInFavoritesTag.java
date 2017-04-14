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

import com.aplana.cms.ContentProducer;
import com.aplana.cms.Tag;
import com.aplana.cms.TagProcessor;
import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.model.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class IsInFavoritesTag implements TagProcessor {

	private static final ObjectId ATTR_FAVORITE_PERSON = ObjectId.predefined(PersonAttribute.class, "jbr.boss.favorite.person");
	private static final ObjectId ATTR_FAVORITE_DOCUMENT = ObjectId.predefined(CardLinkAttribute.class, "jbr.boss.favorite.document");

    private boolean isInFavorites = false;

	protected Log logger = LogFactory.getLog(getClass());

	public boolean prepareData(Tag tag, Card item, ContentProducer cms) throws Exception {
		checkForExistsPrivateControl(item.getId(), cms);
		return true;
	}

	public void writeHtml(PrintWriter out, Tag tag, Card item, ContentProducer cms) {
		StringBuilder sb = new StringBuilder();
		sb.append("<input id=\"inFavorites").append(item.getId().getId())
		.append("\" type=\"hidden\" value=\"").append(String.valueOf(isInFavorites))
		.append("\"/>");
		out.write(sb.toString());
	}

	/**
	 * ��������� ������� � ��������� �������� "���������".
	 *
	 * @param baseCardId
	 * @param cms
	 */
	private void checkForExistsPrivateControl(ObjectId baseCardId, ContentProducer cms) throws Exception {

		// ���� �������� ���������� � ������� ������� �� ��������� � ������������� �������� �������� ������������ �
		// "������ �� ��������� ��������" �������� id �������� ���������
		final Search search = new Search();
		search.setByAttributes(true);
		List<SearchResult.Column> columns = new ArrayList<SearchResult.Column>(2);
		SearchResult.Column col = new SearchResult.Column();
		col.setAttributeId(ATTR_FAVORITE_PERSON);
		columns.add(col);
		col = new SearchResult.Column();
		col.setAttributeId(ATTR_FAVORITE_DOCUMENT);
		columns.add(col);
		search.setColumns(columns);

		final Person curPerson = (Person) cms.getService().getById(Person.ID_CURRENT);
		search.addPersonAttribute(ATTR_FAVORITE_PERSON, curPerson.getId());
		search.addCardLinkAttribute(ATTR_FAVORITE_DOCUMENT, baseCardId);

		try {
			List searchCards = cms.searchCards(search);
			if (searchCards != null && searchCards.size() > 0) {
				isInFavorites = true;
			}
		} catch (Exception e) {
			logger.error("Error on in favorites check: " + e);
		}
	}
}
