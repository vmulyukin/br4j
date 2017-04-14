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
package com.aplana.dmsi.types;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlTransient;

import com.aplana.dmsi.types.common.DeliveryMethod;

@XmlTransient
public class ImportedDocumentType extends DocumentType {

	protected DeliveryMethod deliveryMethod = DeliveryMethod.DELO;
	protected final List<OrganizationWithSign> organizationAuthor = new ArrayList<OrganizationWithSign>();
	protected final List<Object> personAuthor = new ArrayList<Object>();
	private Map<OrganizationWithSign, List<OfficialPersonWithSign>> officialPersonsByOrganization = new HashMap<OrganizationWithSign, List<OfficialPersonWithSign>>();
	protected List<OrganizationOnly> addresseOrganizations;

	protected String writerDescription;
	private OrganizationOnly sender;
	private OrganizationOnly receiver;

	public ImportedDocumentType() {
		super();
	}

	public ImportedDocumentType(DocumentType type) {
		this();
		setRegNumber(type.getRegNumber());
		setConfident(type.getConfident());
		getReferred().addAll(type.getReferred());
		getDocNumber().addAll(type.getDocNumber());
		getAddressee().addAll(type.getAddressee());
		getDocTransfer().addAll(type.getDocTransfer());
		getRegHistory().addAll(type.getRegHistory());
		getAuthor().addAll(type.getAuthor());
		getValidator().addAll(type.getValidator());
		setWriter(type.getWriter());
		setIdnumber(type.getIdnumber());
		setType(type.getType());
		setKind(type.getKind());
		setPages(type.getPages());
		setTitle(type.getTitle());
		setAnnotation(type.getAnnotation());
		setCollection(type.getCollection());
		setId(type.getId());
	}

	@Override
	public void setWriter(Writer value) {
		if(value == null) {
			return;
		}
		super.setWriter(value);
		this.writerDescription = "";
		Object containedObject = value.getContainedObject();
		if (containedObject instanceof Organization) {
			Organization org = (Organization) containedObject;
			StringBuilder officialsBuilder = new StringBuilder();
			for (OfficialPerson official : org.getOfficialPerson()) {
				if (officialsBuilder.length() > 0) {
					officialsBuilder.append(", ");
				}
				officialsBuilder.append(getName(official.getName()));
			}
			this.writerDescription = String.format("%s(%s): %s", org.getFullname() == null ? "" : org.getFullname(),
					org.getShortname(), officialsBuilder);
		} else if (containedObject instanceof PrivatePerson) {
			PrivatePerson pers = (PrivatePerson) containedObject;
			Name personName = pers.getName();
			this.writerDescription = getName(personName);
		}
	}

	private String getName(Name name) {
		String fullDescription = name.getValue();
		if (fullDescription == null || "".equals(fullDescription)) {
			fullDescription = name.getFirstname() + " " + name.getFathersname() + " " + name.getSecname();
		}
		return fullDescription;
	}

	public List<OrganizationOnly> getAddresseOrganizations() {
		if (this.addresseOrganizations == null) {
			this.addresseOrganizations = new ArrayList<OrganizationOnly>();
		}
		return this.addresseOrganizations;
	}

	@Override
	public List<Author> getAuthor() {
		ListWithCallbacks<Author> authorListWithCallbacks = new ListWithCallbacks<Author>(super.getAuthor());
		authorListWithCallbacks.addCallback(new ListWithCallbacks.Callback<Author>() {
			public void elementAdded(Author element) {
				Object obj = element.getContainedObject();
				if (obj instanceof OrganizationWithSign) {
					addOrganization((OrganizationWithSign) obj);
				} else if (obj instanceof PrivatePersonWithSign) {
					addPerson((PrivatePersonWithSign) obj);
				}
			}

			public void elementRemoved(Object element) {
				Object obj = ((Author) element).getContainedObject();
				if (obj instanceof OrganizationWithSign) {
					removeOrganization((OrganizationWithSign) obj);
				} else if (obj instanceof PrivatePersonWithSign) {
					removePerson((PrivatePersonWithSign) obj);
				}
			}
		});
		return authorListWithCallbacks;
	}

	protected void addOrganization(OrganizationWithSign organization) {
		organizationAuthor.add(organization);
		List<OfficialPersonWithSign> officialPersons = organization.getOfficialPersonWithSign();
		List<OfficialPersonWithSign> personAuthors = new ArrayList<OfficialPersonWithSign>(officialPersons.size());
		for (OfficialPersonWithSign officialPerson : officialPersons) {
			OfficialPersonWithSign person = new ExternalOfficialPersonWithSign(officialPerson);
			person.setOrganization(organization.getOrganization());
			personAuthors.add(person);
			personAuthor.add(person);
		}
		officialPersonsByOrganization.put(organization, personAuthors);
	}

	protected void removeOrganization(OrganizationWithSign organization) {
		organizationAuthor.remove(organization);
		personAuthor.removeAll(officialPersonsByOrganization.get(organization));
		officialPersonsByOrganization.remove(organization);
	}

	protected void addPerson(PrivatePersonWithSign person) {
		personAuthor.add(new ExternalPrivatePerson(person.getPrivatePerson()));
	}

	protected void removePerson(PrivatePersonWithSign person) {
		personAuthor.remove(person);
	}

	public OrganizationOnly getSender() {
		return this.sender;
	}

	public void setSender(OrganizationOnly sender) {
		this.sender = sender;
	}

	public DeliveryMethod getDeliveryMethod() {
		return this.deliveryMethod;
	}

	public void setDeliveryMethod(DeliveryMethod deliveryMethod) {
		this.deliveryMethod = deliveryMethod;
	}

	public OrganizationOnly getReceiver() {
		return this.receiver;
	}

	public void setReceiver(OrganizationOnly receiver) {
		this.receiver = receiver;
	}

}
