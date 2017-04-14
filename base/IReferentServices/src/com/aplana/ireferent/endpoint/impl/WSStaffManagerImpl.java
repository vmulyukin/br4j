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
/**
 *
 */
package com.aplana.ireferent.endpoint.impl;

import java.security.Principal;

import javax.annotation.Resource;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.xml.rpc.server.ServletEndpointContext;
import javax.xml.ws.WebServiceContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.ServiceException;
import com.aplana.ireferent.IReferentException;
import com.aplana.ireferent.card.GroupCard;
import com.aplana.ireferent.card.PersonCard;
import com.aplana.ireferent.completion.cards.CompletionGroup;
import com.aplana.ireferent.completion.cards.CompletionOrganizations;
import com.aplana.ireferent.completion.cards.CompletionPerson;
import com.aplana.ireferent.completion.cards.CompletionPersons;
import com.aplana.ireferent.endpoint.WSStaffManager;
import com.aplana.ireferent.types.WSOCollection;
import com.aplana.ireferent.types.WSOContext;
import com.aplana.ireferent.types.WSOFormAction;
import com.aplana.ireferent.types.WSOGroup;
import com.aplana.ireferent.types.WSOPerson;
import com.aplana.ireferent.types.WSObject;
import com.aplana.ireferent.util.IReferentUser;
import com.aplana.ireferent.util.ServiceUtils;
import com.aplana.ireferent.util.ServicesProvider;

/**
 * @author PPanichev
 *
 */

@WebService(name = "WS_StaffManager", targetNamespace = "urn:DefaultNamespace")
@SOAPBinding(style = SOAPBinding.Style.RPC)
public class WSStaffManagerImpl implements WSStaffManager {
    
    @Resource
    WebServiceContext contextEndpoint;

    protected final Log logger = LogFactory.getLog(getClass());
    private DataServiceBean serviceBean = null;

    public WSOPerson getPerson(
	        @WebParam(name = "ID", partName = "ID")
	        String personId,
	        @WebParam(name = "INCLUDEATTACHMENTS", partName = "INCLUDEATTACHMENTS")
	        boolean includeAttachments,
	        @WebParam(name = "CONTEXT", partName = "CONTEXT")
	        WSOContext context) {
	CompletionPerson complPers = null;
	Principal User = new IReferentUser(context.getUserId());
	try {
	    this.serviceBean = ServiceUtils.getServiceBean(User, contextEndpoint);
	    PersonCard personCardIReferent = new PersonCard(personId, serviceBean);
	    complPers = new CompletionPerson(personCardIReferent, includeAttachments, false);
	} catch (DataException ex) {
	    logger.error("com.aplana.ireferent.endpoint.WSStaffManagerImpl.getPerson.DataException", ex);
	} catch (ServiceException ex) {
	    logger.error("com.aplana.ireferent.endpoint.WSStaffManagerImpl.getPerson.ServiceException", ex);
	} catch (IReferentException ex) {
	    logger.error("com.aplana.ireferent.endpoint.WSStaffManagerImpl.getPerson.IReferentException", ex);
	}
	return complPers;
    }

    public WSOGroup getGroup(
	        @WebParam(name = "ID", partName = "ID")
	        String groupId,
	        @WebParam(name = "INCLUDEATTACHMENTS", partName = "INCLUDEATTACHMENTS")
	        boolean includeAttachments,
	        @WebParam(name = "CONTEXT", partName = "CONTEXT")
	        WSOContext context) {
	CompletionGroup complGroup = null;
	Principal User = new IReferentUser(context.getUserId());
	try {
	    this.serviceBean = ServiceUtils.getServiceBean(User, contextEndpoint);
	    GroupCard groupCardIReferent = new GroupCard(groupId, serviceBean);
	    complGroup = new CompletionGroup(groupCardIReferent, includeAttachments, -1, false);
	} catch (DataException ex) {
	    logger.error("com.aplana.ireferent.endpoint.WSStaffManagerImpl.getGroup.DataException", ex);
	} catch (ServiceException ex) {
	    logger.error("com.aplana.ireferent.endpoint.WSStaffManagerImpl.getGroup.ServiceException", ex);
	} catch (IReferentException ex) {
	    logger.error("com.aplana.ireferent.endpoint.WSStaffManagerImpl.getGroup.IReferentException", ex);
	}
	return complGroup;
    }

    public WSOCollection getOrganizations(
	        @WebParam(name = "ISMOBJECT", partName = "ISMOBJECT")
	        boolean isMObject,
	        @WebParam(name = "INCLUDEATTACHMENTS", partName = "INCLUDEATTACHMENTS")
	        boolean includeAttachments,
	        @WebParam(name = "CONTEXT", partName = "CONTEXT")
	        WSOContext context) {
	Principal User = new IReferentUser(context.getUserId());
	CompletionOrganizations complOrg = null;
	try {
	    this.serviceBean = ServiceUtils.getServiceBean(User, contextEndpoint);
	    complOrg = new CompletionOrganizations(includeAttachments, isMObject, serviceBean);
	} catch (DataException ex) {
	    logger.error("com.aplana.ireferent.endpoint.WSStaffManagerImpl.getOrganizations.DataException", ex);
	} catch (ServiceException ex) {
	    logger.error("com.aplana.ireferent.endpoint.WSStaffManagerImpl.getOrganizations.ServiceException", ex);
	} catch (IReferentException ex) {
	    logger.error("com.aplana.ireferent.endpoint.WSStaffManagerImpl.getOrganizations.IReferentException", ex);
	}
	return complOrg;
    }

    public WSOCollection getPersons(
	        @WebParam(name = "GROUPID", partName = "GROUPID")
	        String groupId,
	        @WebParam(name = "ISMOBJECT", partName = "ISMOBJECT")
	        boolean isMObject,
	        @WebParam(name = "CHILDSLEVEL", partName = "CHILDSLEVEL")
	        Integer childsLevel,
	        @WebParam(name = "INCLUDEATTACHMENTS", partName = "INCLUDEATTACHMENTS")
	        boolean includeAttachments,
	        @WebParam(name = "CONTEXT", partName = "CONTEXT")
	        WSOContext context) {
	CompletionPersons complPersons = null;
	Principal User = new IReferentUser(context.getUserId());
	try {
	    this.serviceBean = ServiceUtils.getServiceBean(User, contextEndpoint);
	    GroupCard groupCardIReferent = new GroupCard(groupId, serviceBean);
	    complPersons = new CompletionPersons(groupCardIReferent, includeAttachments, childsLevel, isMObject);
	} catch (DataException ex) {
	    logger.error("com.aplana.ireferent.endpoint.WSStaffManagerImpl.getPersons.DataException", ex);
	} catch (ServiceException ex) {
	    logger.error("com.aplana.ireferent.endpoint.WSStaffManagerImpl.getPersons.ServiceException", ex);
	} catch (IReferentException ex) {
	    logger.error("com.aplana.ireferent.endpoint.WSStaffManagerImpl.getPersons.IReferentException", ex);
	}
	return complPersons;
    }

    public WSOGroup getStructure(
	        @WebParam(name = "GROUPID", partName = "GROUPID")
	        String groupId,
	        @WebParam(name = "ISMOBJECT", partName = "ISMOBJECT")
	        boolean isMObject,
	        @WebParam(name = "CHILDSLEVEL", partName = "CHILDSLEVEL")
	        Integer childsLevel,
	        @WebParam(name = "INCLUDEATTACHMENTS", partName = "INCLUDEATTACHMENTS")
	        boolean includeAttachments,
	        @WebParam(name = "CONTEXT", partName = "CONTEXT")
	        WSOContext context) {
	CompletionGroup complGroup = null;
	Principal User = new IReferentUser(context.getUserId());
	try {
	    this.serviceBean = ServiceUtils.getServiceBean(User, contextEndpoint);
	    GroupCard groupCardIReferent = new GroupCard(groupId, serviceBean);
	    complGroup = new CompletionGroup(groupCardIReferent, includeAttachments, childsLevel, isMObject);
	} catch (IReferentException ex) {
	    logger.error("com.aplana.ireferent.endpoint.WSStaffManagerImpl.getStructure.IReferentException", ex);
	} catch (ServiceException ex) {
	    logger.error("com.aplana.ireferent.endpoint.WSStaffManagerImpl.getStructure.ServiceException", ex);
	} catch (DataException ex) {
	    logger.error("com.aplana.ireferent.endpoint.WSStaffManagerImpl.getStructure.DataException", ex);
	}
	return complGroup;
    }

    /*private DataServiceBean getServiceBean(Principal User) throws IReferentException {
	try {
	    serviceBean = ServicesProvider.serviceBeanInstance(User);
	} catch (ServiceException ex) {
		throw new IReferentException(ex);
	}

	if (serviceBean == null) {
		throw new IReferentException("serviceBean == null");
	}

	return serviceBean;
    }*/

    public WSObject getPersonId(
	    @WebParam(name = "PERSONLOGIN", partName = "PERSONLOGIN")
	    final String personLogin) {
	return ServiceOperationExecutor.execute(new GetPersonIdOperation(personLogin, contextEndpoint));
    }
}
