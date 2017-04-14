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
package com.aplana.medo.types.cardInfo;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "townShip", "streetType", "streetName",
	"house", "building", "flat", "comment" })
public class AddressExt {
    @XmlElement(name = "TownShip")
    private String townShip;
    @XmlElement(name = "StreetType")
    private String streetType;
    @XmlElement(name = "StreetName")
    private String streetName;
    @XmlElement(name = "House")
    private String house;
    @XmlElement(name = "Building")
    private String building;
    @XmlElement(name = "Flat")
    private String flat;
    @XmlElement(name = "Comment")
    private String comment;

    public String getTownShip() {
	return this.townShip;
    }

    public void setTownShip(String townShip) {
	this.townShip = townShip;
    }

    public String getStreetType() {
	return this.streetType;
    }

    public void setStreetType(String streetType) {
	this.streetType = streetType;
    }

    public String getStreetName() {
	return this.streetName;
    }

    public void setStreetName(String streetName) {
	this.streetName = streetName;
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

    public String getFlat() {
	return this.flat;
    }

    public void setFlat(String flat) {
	this.flat = flat;
    }

    public String getComment() {
	return this.comment;
    }

    public void setComment(String comment) {
	this.comment = comment;
    }

}
