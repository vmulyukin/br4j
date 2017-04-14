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
package com.aplana.dbmi.model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Date;

import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.service.DataException;

/**
 * Notification is a base abstract class used to organize massive email delivery to users.
 * Each notification object defines subset of {@link Card cards} to be monitored
 * in form of {@link Search} query.
 * Special service monitors changes in database and notifies users subscribed for this
 * notification about changes in {@link Card cards} subset.
 * <br>
 * Frequency of notification could be selected by {@link #setFrequency(String) setting frequency property}
 * of notification object.
 * <br>
 * Users subscription for notification
 * messages should be defined in descendant classes.
 */
public abstract class Notification extends DataObject
{
	//public static final String FREQ_EVENT = "E";
	/**
	 * Constant value, used to {@link #setFrequency(String) assign} 
	 * hourly frequency for notification object 
	 */
	public static final String FREQ_HOURLY = "H";
	/**
	 * Constant value, used to {@link #setFrequency(String) assign} 
	 * daily frequency for notification object 
	 * TODO: rename DAYLY -> DAILY
	 */	
	public static final String FREQ_DAYLY = "D";
	/**
	 * Constant value, used to {@link #setFrequency(String) assign} 
	 * weekly frequency for notification object 
	 */	
	public static final String FREQ_WEEKLY = "W";
	/**
	 * Constant value, used to {@link #setFrequency(String) assign} 
	 * monthly frequency for notification object 
	 */
	public static final String FREQ_MONTHLY = "M";
	/**
	 * Constant value, indicated that frequency for given notification
	 * object is not specified. Such notification is not delivered to users. 
	 */ 	
	public static final String FREQ_NONE = "N";
	
	private String name;
	private String description;
	private String frequency;
	private String searchXml;
	private Date creationDate;
	private Date lastSentDate;
	private Search search;

	/**
	 * Gets description of notification object
	 * @return description of notification object
	 */
	public String getDescription() {
		return description;
	}
	
	/**
	 * Sets description of notification object
	 * @param description desired value of description
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	
	/**
	 * Gets frequency of notification
	 * @return one of the following constants: {@link #FREQ_NONE}, {@link #FREQ_HOURLY},
	 * {@link #FREQ_DAYLY}, {@link #FREQ_WEEKLY}, {@link #FREQ_MONTHLY}.
	 */
	public String getFrequency() {
		return frequency;
	}

	/**
	 * Sets frequency of notification 
	 * @param frequency desired value of notification frequency. Should be
	 * one of the following constants: {@link #FREQ_NONE}, {@link #FREQ_HOURLY},
	 * {@link #FREQ_DAYLY}, {@link #FREQ_WEEKLY}, {@link #FREQ_MONTHLY}.
	 */
	public void setFrequency(String frequency) {
		this.frequency = frequency;
	}
	
	/**
	 * Gets name of notification object
	 * @return name of notification object
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets name of notification object
	 * @param name desired name of notification object
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * Returns XML representation of {@link com.aplana.dbmi.action.Search} object used
	 * to specify set of {@link com.aplana.dbmi.model.Card cards} monitored by this 
	 * notification object
	 * @return XML representation of {@link com.aplana.dbmi.action.Search} object defining
	 * set of cards monitored by this notification object
	 */
	public String getSearchXml() {
		return searchXml;
	}
	
	/**
	 * Sets search object defining set of {@link com.aplana.dbmi.model.Card cards} monitored by this 
	 * notification object. <br>
	 * Note that changing of this property will automatically changes value returned by {@link #getSearch()} 
	 * @param searchXml XML representation of {@link com.aplana.dbmi.action.Search} object.
	 * Should be in UTF-8 encoding.
	 */
	public void setSearchXml(String searchXml) {
		this.searchXml = searchXml;
		this.search = null;
	}
	
	/**
	 * Gets {@link com.aplana.dbmi.action.Search} object used
	 * to specify set of {@link com.aplana.dbmi.model.Card cards} monitored by this 
	 * notification object
	 * @return {@link com.aplana.dbmi.action.Search} object
	 * @throws DataException
	 */
	public Search getSearch() throws DataException {
		if (search == null && searchXml != null) {
			search = new Search();
			try {
				search.initFromXml(new ByteArrayInputStream(searchXml.getBytes("UTF-8")));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				throw new RuntimeException("UTF-8 support needed");
			}
		}
		return search;
	}
	
	/**
	 * Sets {@link com.aplana.dbmi.action.Search} object used
	 * to specify set of {@link com.aplana.dbmi.model.Card cards} monitored by this 
	 * notification object. <br>
	 * Note that changing of this property will automatically changes value returned by {@link #getSearchXml()}
	 * @param search
	 */
	public void setSearch(Search search) {
		ByteArrayOutputStream xml = new ByteArrayOutputStream();
		try {
			search.storeToXml(xml);
		} catch (RuntimeException e) {
			e.printStackTrace();
			throw new RuntimeException("I/O error storing search object to XML", e);
		}
		this.search = search;
		try {
			this.searchXml = xml.toString("UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			throw new RuntimeException("UTF-8 support needed");
		}
	}

	/**
	 * Gets date of this notification object creation
	 * @return date of this notification object creation
	 */
	public Date getCreationDate() {
		return creationDate;
	}

	/**
	 * Sets date of this notification object creation
	 * @param creationDate date of creation
	 */
	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	/**
	 * Gets date of last message sending executed for this notification object
	 * @return date of last message sending executed for this notification object
	 */
	public Date getLastSentDate() {
		return lastSentDate;
	}

	/**
	 * Sets date of last message sending executed for this notification object 
	 * @param lastSentDate desired value of lastSentDate property 
	 */
	public void setLastSentDate(Date lastSentDate) {
		this.lastSentDate = lastSentDate;
	}
}
