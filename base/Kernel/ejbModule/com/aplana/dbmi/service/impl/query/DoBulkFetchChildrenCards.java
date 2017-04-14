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
package com.aplana.dbmi.service.impl.query;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.jdbc.core.RowCallbackHandler;

import com.aplana.dbmi.action.BulkFetchChildrenCards;
import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.jbr.util.IdUtils;
import com.aplana.dbmi.model.BackLinkAttribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.PersonAttribute;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ActionQueryBase;

/**
 * Fetches children cards of desired parent cards by provided parent card ids and linked attribute
 * 
 */
public class DoBulkFetchChildrenCards extends ActionQueryBase {
	private static final int BULK_SIZE = 100;
	
	private Map<String, ArrayList<Card>> labelForColumnsMap;

	public Object processQuery() throws DataException {

		final BulkFetchChildrenCards action = (BulkFetchChildrenCards)getAction();
		final boolean isPersonLink = action.getLinkAttributeId().getType().equals(PersonAttribute.class) ? true : false;
		final boolean isChildrenTemplates = action.getChildrenTemplates()!=null && !action.getChildrenTemplates().isEmpty();
		final boolean isChildrenStates = action.getChildrenStates()!=null && !action.getChildrenStates().isEmpty();
		
		String sql;
		String sqlCardLink = isPersonLink 
			? action.isReverseLink()
					?  "select distinct p.card_id as parent_id, av.card_id as child_id from person p "
						+ "join attribute_value av on av.number_value = p.person_id and av.attribute_code = ? "
						+ "where p.card_id in ({0})"	
					: 	"select distinct av.card_id as parent_id, p.card_id as child_id " 
						+ "from attribute_value av " 
						+ "join person p on p.person_id = av.number_value "
						+ "where av.card_id in ({0}) and av.attribute_code = ?"
			: action.isReverseLink()
					? 	"select distinct av.number_value as parent_id, av.card_id as child_id" +
							" from attribute_value av where" +
							" av.attribute_code = ?" +
							" and av.number_value in ({0})"
					:  	"select distinct av.card_id as parent_id, av.number_value as child_id" +
							" from attribute_value av where" +
							" av.attribute_code = ?" +
							" and av.card_id in ({0})"
			;
		
		String sqlBackLink = "select distinct c.card_id as parent_id, \n" +
							 " 	CASE \n" +
							 " 		WHEN upLink.option_value is null \n" +
							 " 			THEN avLinkFrom.card_id \n" +
							 " 		WHEN upLink.option_value is not null \n" +
							 " 			THEN functionbacklink(c.card_id, upLink.option_value, link.option_value) \n" +
							 " 		ELSE NULL \n" +
							 " 	END as child_id \n" +
							 " FROM card c \n" +
							 " LEFT OUTER JOIN attribute_option link on link.attribute_code = ? \n" +
							 " 		and link.option_code=''LINK'' \n" +
							 " LEFT OUTER JOIN attribute_option upLink on upLink.attribute_code = ? \n" +
							 " 		and upLink.option_code=''UPLINK'' \n" +
							 " JOIN attribute_value avLinkFrom \n" +
							 " 		on avLinkFrom.number_value=c.card_id \n" +
							 " 		and avLinkFrom.attribute_code=( \n" +
							 " 				select o.option_value \n" +
							 " 				from attribute_option o \n" +
							 " 				where o.attribute_code=? \n" +
							 " 				and o.option_code=''LINK'' \n" +
							 " 		) \n" +
							 " WHERE c.card_id in ({0}) \n";
		
		if(BackLinkAttribute.class.isAssignableFrom(action.getLinkAttributeId().getType())) {
			sql = sqlBackLink;
		} else {
			sql = sqlCardLink;
		}
									
		if(isChildrenTemplates || isChildrenStates){
			sql = "select * from ("+sql+") as temp \n" 
					+ "\t join card c on temp.child_id = c.card_id \n"
					+ "\t where (1=1)\n ";
			if(isChildrenTemplates){
					sql = sql + "and c.template_id in (" + IdUtils.makeIdCodesEnum(action.getChildrenTemplates(),",") + ")\n";
			}
			if(isChildrenStates){
					sql = sql + "and c.status_id in ("+IdUtils.makeIdCodesEnum(action.getChildrenStates(),",") +")";
			}
		}
		
		MessageFormat sqlFmt = new MessageFormat(sql);
			
		final Collection parentCardIds = action.getParentCardIds();
		final Set allChildren = new LinkedHashSet(0);
		final Map resultMap = new LinkedHashMap(parentCardIds.size());

		final RowCallbackHandler rh = new RowCallbackHandler() {
			public void processRow(ResultSet rs) throws SQLException {
				ObjectId parentId = new ObjectId(Card.class, rs.getLong(1));
				ObjectId childId = new ObjectId(Card.class, rs.getLong(2));
				Set children = (Set)resultMap.get(parentId);
				children.add(childId);
				allChildren.add(childId);
			}
		};
		
		int cnt = 0;
		StringBuffer stParents = new StringBuffer();
		for( Iterator i = parentCardIds.iterator(); i.hasNext(); ) {
			final ObjectId parentId = (ObjectId)i.next();
			resultMap.put(parentId, new LinkedHashSet());
			if (stParents.length() > 0) {
				stParents.append(',');
			}
			stParents.append(parentId.getId().toString());
			++cnt;
			if (cnt % BULK_SIZE == 0 || !i.hasNext()) {
				if(BackLinkAttribute.class.isAssignableFrom(action.getLinkAttributeId().getType())) {
					final String attrStr = (String) action.getLinkAttributeId().getId();
					getJdbcTemplate().query(sqlFmt.format(new Object[] {stParents.toString()}),	new Object[] { attrStr, attrStr, attrStr }, new int[] { Types.VARCHAR, Types.VARCHAR, Types.VARCHAR },rh);
				} else {
					getJdbcTemplate().query(sqlFmt.format(new Object[] {stParents.toString()}),	new Object[] { action.getLinkAttributeId().getId() }, new int[] { Types.VARCHAR },rh);
				}
				cnt = 0;		
				stParents = new StringBuffer();
			}
		}

		final Search search = new Search();
		search.setByCode(true);
		search.setWords(ObjectIdUtils.numericIdsToCommaDelimitedString(allChildren));
		search.setColumns(action.getColumns());		
		ActionQueryBase q = getQueryFactory().getActionQuery(search);
		q.setAction(search);
		
		final SearchResult searchResult = (SearchResult)getDatabase().executeQuery(getUser(), q);
		
		final Map childrenMap = ObjectIdUtils.collectionToObjectIdMap(
			searchResult.getCards()
		);
		for( Iterator i = resultMap.entrySet().iterator(); i.hasNext(); ) {
			final Map.Entry mapEntry = (Map.Entry)i.next();
			final Set childrenIds = (Set)mapEntry.getValue();
			final List childrenCards = new ArrayList(childrenIds.size());
			for( Iterator j = childrenIds.iterator(); j.hasNext(); ) {
				Card c = (Card) childrenMap.get(j.next());
				//Access check
				if(c != null && c.getCanRead()) childrenCards.add(c);
			}
			mapEntry.setValue(childrenCards);
		}
		
		
		final BulkFetchChildrenCards.Result result = new BulkFetchChildrenCards.Result();
		result.setCards(resultMap);
		result.setLabelColumnsMap(searchResult.getLabelColumnsForCards());
		
		return result;
	}
}
