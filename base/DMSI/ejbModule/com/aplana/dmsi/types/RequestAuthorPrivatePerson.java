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

import java.util.List;

import javax.xml.bind.annotation.XmlTransient;

import com.aplana.dmsi.types.common.Area;
import com.aplana.dmsi.types.common.Country;
import com.aplana.dmsi.types.common.Region;
import com.aplana.dmsi.types.common.RequestAuthor;
import com.aplana.dmsi.types.common.Town;

@XmlTransient
public class RequestAuthorPrivatePerson extends PrivatePerson {

    private RequestAuthor reqAuthor;

    public RequestAuthorPrivatePerson() {
        super();
    }

    public RequestAuthorPrivatePerson(PrivatePerson another) {
        super(another);
    }

    protected RequestAuthor getReqAuthor() {
        if (reqAuthor == null) {
            reqAuthor = new RequestAuthor();
        }
        return reqAuthor;
    }

    @Override
    public Name getName() {
        Name reqName = new Name();
        reqName.setFathersname(getReqAuthor().getMidleName());
        reqName.setFirstname(getReqAuthor().getFirstName());
        reqName.setSecname(getReqAuthor().getLastName());
        reqName.setValue(getReqAuthor().getName());
        return reqName;
    }

    @Override
    public void setName(Name value) {
        getReqAuthor().setName(value.getValue());
        getReqAuthor().setFirstName(value.getCalculatedFirstname());
        getReqAuthor().setMidleName(value.getCalculatedFathersname());
        getReqAuthor().setLastName(value.getCalculatedSecname());
    }

    @Override
    public Address getAddress() {
        Address reqAddress = new Address();
        RequestAuthor author = getReqAuthor();
        Region region = author.getRegion();
        Country country = author.getCountry();
        Town town = author.getTown();
        Area area = author.getArea();
        reqAddress.setDistrict(region == null ? null : region.getName());
        reqAddress.setCountry(country == null ? null : country.getName());
        reqAddress.setSettlement(town == null ? null : town.getName());
        reqAddress.setRegion(area == null ? null : area.getName());
        reqAddress.setNontypical(author.getComment());
        reqAddress.setPostcode(author.getZipCode());
        return reqAddress;
    }

    @Override
    public void setAddress(Address value) {
        StringBuilder addressBuilder = new StringBuilder();
        if (value.getValue() != null) {
            addressBuilder.append(value.getValue().trim());
        }
        if (addressBuilder.length() > 0) {
            addressBuilder.append("; ");
        }
        addressBuilder.append(value.getNontypical());
        getReqAuthor().setComment(addressBuilder.toString());
        getReqAuthor().setCountry(new Country(value.getCountry()));
        getReqAuthor().setArea(new Area(value.getRegion()));
        getReqAuthor().setRegion(new Region(value.getDistrict()));
        getReqAuthor().setTown(new Town(value.getSettlement()));
        getReqAuthor().setZipCode(value.getPostcode());
    }

    @Override
    protected void setEmailEcontact(EmailEcontact emailEcontact) {
        getReqAuthor().setEmail(emailEcontact.getValue());
    }

    @Override
    public List<Econtact> getEcontact() {
    	List<Econtact> contacts = super.getEcontact();
    	if (contacts.isEmpty()) {
    		EmailEcontact emailContact = new EmailEcontact();
    		emailContact.setValue(getReqAuthor().getEmail());
    		contacts.add(emailContact);
    	}
    	return contacts;
    }

}
