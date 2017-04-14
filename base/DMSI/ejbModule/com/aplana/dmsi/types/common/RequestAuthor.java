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
package com.aplana.dmsi.types.common;

import java.math.BigInteger;

import com.aplana.dmsi.types.DMSIObject;
import com.aplana.dmsi.types.Organization;

public class RequestAuthor extends DMSIObject {

    private String name;
    private String lastName;
    private String firstName;
    private String midleName;
    private Country country;
    private Area area;
    private Region region;
    private Town town;
    private String street;
    private String zipCode;
    private String email;
    private Organization organization;
    private String house;
    private String building;
    private BigInteger flat;
    private String comment;

    public String getName() {
	return this.name;
    }

    public void setName(String name) {
	this.name = name;
    }

    public String getLastName() {
	return this.lastName;
    }

    public void setLastName(String lastName) {
	this.lastName = lastName;
    }

    public String getFirstName() {
	return this.firstName;
    }

    public void setFirstName(String firstName) {
	this.firstName = firstName;
    }

    public String getMidleName() {
	return this.midleName;
    }

    public void setMidleName(String midleName) {
	this.midleName = midleName;
    }

    public Country getCountry() {
	return this.country;
    }

    public void setCountry(Country country) {
	this.country = country;
    }

    public Area getArea() {
	return this.area;
    }

    public void setArea(Area area) {
	this.area = area;
    }

    public Region getRegion() {
	return this.region;
    }

    public void setRegion(Region region) {
	this.region = region;
    }

    public Town getTown() {
	return this.town;
    }

    public void setTown(Town town) {
	this.town = town;
    }

    public String getStreet() {
	return this.street;
    }

    public void setStreet(String street) {
	this.street = street;
    }

    public String getZipCode() {
	return this.zipCode;
    }

    public void setZipCode(String zipCode) {
	this.zipCode = zipCode;
    }

    public String getComment() {
	return this.comment;
    }

    public void setComment(String address) {
	this.comment = address;
    }

    public String getEmail() {
	return this.email;
    }

    public void setEmail(String email) {
	this.email = email;
    }

    public Organization getOrganization() {
	return this.organization;
    }

    public void setOrganization(Organization organization) {
	this.organization = organization;
    }

	public String getHouse() {
		return this.house;
	}

	public void setHouse(String house) {
		this.house = house;
	}

	public String getBuilding() {
		return this.building;
	}

	public void setBuilding(String building) {
		this.building = building;
	}

	public BigInteger getFlat() {
		return this.flat;
	}

	public void setFlat(BigInteger flat) {
		this.flat = flat;
	}

}
