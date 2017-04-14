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
package com.aplana.dbmi.module.notif;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.jdbc.core.RowMapper;

import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Distribution;
import com.aplana.dbmi.model.Notification;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.model.Subscription;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ActionQueryBase;
import com.aplana.dbmi.service.impl.QueryBase;

public class NotificationManager extends DataServiceClient implements
		DeliverySource, BeanFactoryAware
{
	public static final String BEAN_NOTIF_SUBSCRIPTION = "subscriptionNotifier";
	public static final String BEAN_NOTIF_DISTRIBUTION = "distributionNotifier";
	
	private String frequency;
	private BeanFactory beanFactory;

	public void setFrequency(String frequency) {
		this.frequency = frequency;
	}

	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		super.setBeanFactory(beanFactory);
		this.beanFactory = beanFactory;
	}

	public boolean buildDelivery(NotificationObject delivery) {
		Notification notification = ((StoredNotification) delivery).getNotification();
		try {
			Search search = new Search();
			search.initFromXml(new ByteArrayInputStream(notification.getSearchXml().getBytes("UTF-8")));
			search.addDateAttribute(Attribute.ID_CHANGE_DATE, notification.getLastSentDate(), new Date());
			ActionQueryBase searchQuery = getQueryFactory().getActionQuery(Search.class);
			searchQuery.setAccessChecker(null);
			searchQuery.setAction(search);
			SearchResult result = (SearchResult) getDatabase().executeQuery(getSystemUser(), searchQuery);
			if (result.getCards().size() == 0)
				return false;

			((StoredNotification) delivery).setFoundCards(result);
			return true;
		} catch (UnsupportedEncodingException e) {
			logger.error("Error processing notification " + notification.getId().getId(), e);
			return false;
		} catch (DataException e) {
			logger.error("Error searching cards for notification " + notification.getId().getId(), e);
			return false;
		}
	}

	public NotificationBean getNotifier(NotificationObject delivery) {
		Notification notification = ((StoredNotification) delivery).getNotification();
		if (notification instanceof Subscription)
			return (NotificationBean) beanFactory.getBean(BEAN_NOTIF_SUBSCRIPTION);
		else /*if (notification instanceof Distribution)*/
			return (NotificationBean) beanFactory.getBean(BEAN_NOTIF_DISTRIBUTION);
	}

	public Collection listDeliveries() {
		try {
			return (Collection) getDatabase().executeQuery(getSystemUser(), new QueryBase() {
				public Object processQuery() throws DataException {
					return getJdbcTemplate().query(
							"SELECT n.notif_rule_id, n.rule_name, n.rule_description, n.search_param, " +
								"n.creation_date, n.person_id, n.is_forced, n.last_send_date " +
							"FROM notification_rule n " +
							"WHERE n.reoccurrence_interval=?",
							new Object[] { frequency },
							new RowMapper() {
								public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
									Notification notif;
									if (rs.getInt(7) == 1) {
										Distribution distr = new Distribution();
										distr.setId(rs.getLong(1));
										distr.setCreator(new ObjectId(Person.class, rs.getLong(6)));
										notif = distr;
									} else {
										Subscription subscr = new Subscription();
										subscr.setId(rs.getLong(1));
										subscr.setPersonId(new ObjectId(Person.class, rs.getLong(6)));
										notif = subscr;
									}
									notif.setName(rs.getString(2));
									notif.setDescription(rs.getString(3));
									notif.setSearchXml(rs.getString(4));
									notif.setCreationDate(rs.getTimestamp(5));
									notif.setLastSentDate(rs.getTimestamp(8));
									return notif;
								}
							});
				}
			});
		} catch (DataException e) {
			logger.error("Error fetching notifications", e);
			return Collections.EMPTY_LIST;
		}
	}
}
