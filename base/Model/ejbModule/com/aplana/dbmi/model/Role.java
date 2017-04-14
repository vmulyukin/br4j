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

import java.util.Collection;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import com.aplana.dbmi.UserService;

/**
 * Each {@link Person user} in system could have one or several {@link SystemRole system roles}.
 * Role defines access to various portal areas, ability to create new {@link Card} objects,
 * access to existing {@link Card cards} in various {@link CardState states},
 * possibility to perform {@link WorkflowMove changes to card state}, etc.<br>
 * Each {@link SystemRole} granted to user could be restricted to specific 
 * set of {@link Template templates} and regions.<br>
 * {@link Role} object defines additional parameters of {@link SystemRole} object
 * assigned to given {@link Person}. To be more precise, {@link Role} object defines
 * set of {@link Template templates} and regions (records from {@link Reference dictionary} with id = {@link Reference#ID_REGION})
 * for which this {@link SystemRole} is active.<br>
 * Each Role object represents one record from PERSON_ROLE table.
 */
public class Role extends DataObject
{
	/**
	 * String identifier of 'Administrator' {@link SystemRole}
	 */
	public static final String ADMINISTRATOR = UserService.ADMINISTRATOR;
	
	/**
	 * String identifier of 'Editor' {@link SystemRole}
	 */
	public static final String EDITOR = "E";
	/**
	 * String identifier of 'Specialist' {@link SystemRole}
	 */	
	public static final String SPECIALIST = "S";
	/**
	 * String identifier of 'Manager 1' {@link SystemRole}
	 */	
	public static final String MANAGER_1 = "1";
	/**
	 * String identifier of 'Manager 2' {@link SystemRole}
	 */
	public static final String MANAGER_2 = "2";
	/**
	 * String identifier of 'Guest' {@link SystemRole}
	 */
	public static final String GUEST = "G";
	
	/**
	 * String identifier of 'Administrator of users'
	 */
	public static final String ADMINISTRATOR_USERS = "A_USERS";

	/**
	 * String identifier of 'Administrator of dictionaries'
	 */
	public static final String ADMINISTRATOR_DICTIONARIES = "A_DICTIONARIES";

	/**
	 * String identifier of 'Administrator of templates'
	 */
	public static final String ADMINISTRATOR_TEMPLATES = "A_TEMPLATES";
	
	/**
	 * String identifier of 'Administrator of journal'
	 */
	public static final String ADMINISTRATOR_JOURNAL = "A_JOURNAL";

	/**
	 * String identifier of 'Administrator of statistic'
	 */
	public static final String ADMINISTRATOR_STATISTIC = "A_STATISTIC";
	
	/**
	 * String identifier of 'Administrator of tasks'
	 */
	public static final String ADMINISTRATOR_TASKS = "A_TASKS";

	/**
	 * String identifier of 'Administrator of processes'
	 */
	public static final String ADMINISTRATOR_PROCESSES = "A_PROCESSES";
	
	private static final long serialVersionUID = 1L;
	private ObjectId person;
	
	/* � ������ ������ BR4J00036917 ������� ����������� ����-������, ����-������
	private Collection templates;
	private Collection regions;
	*/
	private SystemRole systemRole;
	
	/**
	 * Gets {@link SystemRole} associated with this {@link Role} object
	 * @return {@link SystemRole} object
	 */
	public SystemRole getSystemRole() {
		return systemRole;
	}

	/**
	 * Sets {@link SystemRole} associated with this {@link Role} object
	 * @param role {@link SystemRole} object to associate with this role object
	 */
	public void setSystemRole(SystemRole role) {
		this.systemRole = role;
	}

	/**
	 * Sets numeric identifier of role object
	 * @param id desired value of identifier
	 */
	public void setId(long id) {
		super.setId(new ObjectId(Role.class, id));
	}


//   � ������ ������ BR4J00036917 ������� ����������� ����-������, ����-������
	
//	/**
//	 * Gets collection of regions  
//	 * ({@link ReferenceValue} records from {@link Reference dictionary} with id = {@link Reference#ID_REGION})  
//	 * @return collection of regions for which this {@link Role} is active.
//	 * If empty then this role is active for all regions.
//	 */
//	public Collection getRegions() {
//		return regions;
//	}
//
//	/**
//	 * Sets collection of regions for which this role is active.
//	 * @param regions collection of regions for which this role is active. 
//	 * Should be collection of {@link ReferenceValue} objects with reference = {@link Reference#ID_REGION}.
//	 * If regions is empty, then role will be active for all regions 
//	 */
//	public void setRegions(Collection regions) {
//		this.regions = regions;
//	}
//
//	/**
//	 * Gets collection of {@link Template templates} for which this role is active.
//	 * Note that returned template objects could be only partially initialized
//	 * and could miss information about attrubutes and template blocks included in template.
//	 * @return collection of {@link Template templates} for which this role is active.
//	 * If collection is empty then this role is active for all templates.
//	 */
//	public Collection getTemplates() {
//		return templates;
//	}
//
//	/**
//	 * Sets collection of {@link Template templates} for which this role is active.
//	 * @param templates collection of {@link Template templates} for which this role is active.
//	 * If collection is empty then this role will be active for all templates. 
//	 */
//	public void setTemplates(Collection templates) {
//		this.templates = templates;
//	}
//	
	/**
	 * Use {@link #getSystemRole()}.getId().getId()} instead
	 * @return String identifier of {@link SystemRole} associated with this role object 
	 */ 
	public String getType() {
		return systemRole == null ? null : (String)systemRole.getId().getId();
	}

	/**
	 * Gets english name of {@link SystemRole} associated with this role object
	 * @return  english name of {@link SystemRole} associated with this role object
	 */
	public String getNameEn() {
		return systemRole == null ? null : systemRole.getNameEn();
	}

	/**
	 * Gets russian name of {@link SystemRole} associated with this role object
	 * @return russian name of {@link SystemRole} associated with this role object
	 */	
	public String getNameRu() {
		return systemRole == null ? null : systemRole.getNameRu();
	}

	/**
	 * Gets localized name of {@link SystemRole} associated with this role object
	 * @return value of {@link #getSystemRole()}.getName()
	 */
	public String getName() {
		return systemRole == null ? null : systemRole.getName();
	}

	/**
	 * Gets identifier of {@link Person} associated with this role object
	 * @return identifier of {@link Person} associated with this role object
	 */
	public ObjectId getPerson() {
		return person;
	}

	/**
	 * Sets identifier of {@link Person} associated with this role object 
	 * @param person should be identifier of {@link Person} type
	 */
	public void setPerson(ObjectId person) {
		this.person = person;
	}

	/**
	 * @deprecated
	 * use {@link #getName()} instead
	 * @param type string {@link SystemRole} identifier
	 * @param locale required language
	 * @return localized name of {@link SystemRole} object with given id
	 */ 
	public static String getRoleName(String type, Locale locale) {
		try {
			return ResourceBundle.getBundle(ContextProvider.MESSAGES, locale)
					.getString("role." + type);
		} catch (MissingResourceException e) {
			System.err.println("WARNING! No resource for role " + type);
			return null;
		}
	}
}
