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

import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.service.DataException;

/**
 * Represent information about personal searches.<br>
 * Personal search is a query over {@link Card cards} table with customized set of filter parameters.
 * Every user can save search preferences in separate database object and specify name 
 * and description for it.<br>
 * All stored personal search objects could be accessed later via 'Personal searches'
 * page of portal.
 */
public class PersonalSearch extends DataObject 
{
	private static final long serialVersionUID = 3L;
	private ObjectId person;
	private String name;
	/* Personal search area like Income, Ioternal, Personal cabinet */
	private String area;
	private String description;
	private String searchXml;
	private transient Search search;
	
	/**
	 * Sets personal search identifier
	 * @param id desired value of identifier
	 */
	public void setId(long id) {
		super.setId(new ObjectId(PersonalSearch.class, id));
	}
	
	/**
	 * Returns description of personal search object
	 * @return description of personal search object
	 */
	public String getDescription() {
		return description;
	}
	
	/**
	 * Sets description of personal search object
	 * @param description desired value of description
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	
	
	/**
	 * Returns area(region) of personal search object
	 * @return
	 */
	public String getArea() {
		return area;
	}

	public void setArea(String area) {
		this.area = area;
	}

	/**
	 * Gets name of personal search object
	 * @return personal search name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets name for personal search object
	 * @param name desired value of name
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * Returns identifier of person search owner. Should be reference to {@link Person} object
	 * @return identifier of person search owner.
	 */
	public ObjectId getPerson() {
		return person;
	}

	/**
	 * Sets owner of personal search object
	 * @param person identifier of {@link Person} who owns this personal search object
	 * @throws IllegalArgumentException if person is not identifier of {@link Person} object 
	 */
	public void setPerson(ObjectId person) {
		if (!Person.class.equals(person.getType()))
			throw new IllegalArgumentException("Not a person id");
		this.person = person;
	}

	/**
	 * Sets owner of personal search object
	 * @param person numeric identifier of {@link Person} who owns this personal search object
	 */
	public void setPerson(long person) {
		this.person = new ObjectId(Person.class, person);
	}
	
	/**
	 * Gets {@link Search} parameters serialized to XML string
	 * XML should be created in UTF-8 encoding
	 * @return search parameters serialized to XML string
	 */
	public String getSearchXml() {
		return searchXml;
	}
	
	/**
	 * Sets search preferences in form of {@link Search} object serialized to XML string.
	 * XML should be created in UTF-8 encoding  
	 * @param searchXml search preferences in form of {@link Search} object serialized to XML string
	 */
	public void setSearchXml(String searchXml) {
		this.searchXml = searchXml;
		this.search = null;
	}

	/**
	 * Returns search parameters in form of {@link Search} object.
	 * @return search parameters in form of {@link Search} object
	 * @throws DataException if searchXML property contains error in Search object definition
	 * @throws RuntimeException if application server doesn't support UTF-8 encoding 
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
	 * Sets search parameters.
	 * Serializes passed {@link Search} object and stores it's value in searchXML property
	 * @param search search parameters to be used in this personal search object
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
}
