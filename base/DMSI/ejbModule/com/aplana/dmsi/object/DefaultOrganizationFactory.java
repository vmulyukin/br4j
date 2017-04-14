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
package com.aplana.dmsi.object;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.service.client.DataServiceFacade;
import com.aplana.dmsi.Configuration;
import com.aplana.dmsi.DMSIException;
import com.aplana.dmsi.types.Name;
import com.aplana.dmsi.types.OfficialPerson;
import com.aplana.dmsi.types.OfficialPersonWithSign;
import com.aplana.dmsi.types.Organization;
import com.aplana.dmsi.types.OrganizationOnly;
import com.aplana.dmsi.types.OrganizationWithSign;

public class DefaultOrganizationFactory {

	private Log logger = LogFactory.getLog(getClass());

    private DataServiceFacade serviceBean;
    private boolean isNullAllowed;

    public DefaultOrganizationFactory(DataServiceFacade serviceBean) {
	this.serviceBean = serviceBean;
    }

    protected boolean isNullAllowed() {
		return this.isNullAllowed;
	}

	protected void setNullAllowed(boolean isNullAllowed) {
		this.isNullAllowed = isNullAllowed;
	}

	public Organization createOrganization(Object source) throws DMSIException {
	if (source instanceof OfficialPerson) {
	    OfficialPerson officialPerson = (OfficialPerson) source;
	    OrganizationOnly personOrganization = officialPerson.getOrganization();
	    if (personOrganization == null) {
	    	personOrganization = getDefaultOrganization();
	    	logDefaultOrganizationUsage(officialPerson.getName(), personOrganization);
	    }
	    Organization organization = new Organization(personOrganization);
	    organization.getOfficialPerson().add(officialPerson);
	    return organization;
	} else if (source instanceof Organization) {
	    return (Organization) source;
	} else if (isNullAllowed) {
		return null;
	} else
	    throw new IllegalStateException("Source can be only "
		    + OfficialPerson.class.getName() + " or "
		    + Organization.class.getName() + " instance");

    }

    public OrganizationWithSign createOrganizationWithSign(Object source)
	    throws DMSIException {
	if (source instanceof OfficialPersonWithSign) {
	    OfficialPersonWithSign officialPerson = (OfficialPersonWithSign) source;

	    OrganizationOnly personOrganization = officialPerson.getOrganization();
	    if (personOrganization == null) {
	    	personOrganization = getDefaultOrganization();
	    	logDefaultOrganizationUsage(officialPerson.getName(), personOrganization);
	    }

	    OrganizationWithSign organization = new OrganizationWithSign(
		    personOrganization);
	    organization.getOfficialPersonWithSign().add(officialPerson);
	    return organization;
	} else if (source instanceof OrganizationWithSign) {
	    return (OrganizationWithSign) source;
	} else if (isNullAllowed) {
		return null;
	} else
	    throw new IllegalStateException("Source can be only "
		    + OfficialPersonWithSign.class.getName() + " or "
		    + OrganizationWithSign.class.getName() + " instance");
    }

    private OrganizationOnly getDefaultOrganization() throws DMSIException {
	ObjectId defaultOrgId = Configuration.instance()
		.getDefaultOrganizationId();
	DMSIObjectFactory orgFactory = DMSIObjectFactory.newInstance(
		serviceBean, "OrganizationOnly");
	return (OrganizationOnly) orgFactory.newDMSIObject(defaultOrgId);
    }

    private void logDefaultOrganizationUsage(Name personName, OrganizationOnly defaultOrg) {
    	if (logger.isWarnEnabled()) {

    		String person = personName != null ? personName.getSecname() + " " + personName.getFirstname() + " " + personName.getFathersname(): null;
    		String defaultOrgFullname = defaultOrg != null ? defaultOrg.getFullname() : null;
    		logger.warn("Person " + person + " doesn't have organization. Using default: " + defaultOrgFullname);
    	}
    }

}