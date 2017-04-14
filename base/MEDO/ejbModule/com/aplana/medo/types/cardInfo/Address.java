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
@XmlType(name = "", propOrder = { "country", "autoRepublic", "land", "area",
	"autoArea", "autoDistrict", "region", "town" })
public class Address {

    @XmlElement(name = "Country")
    private String country;
    @XmlElement(name = "AutoRepublic")
    private String autoRepublic;
    @XmlElement(name = "Land")
    private String land;
    @XmlElement(name = "Area")
    private String area;
    @XmlElement(name = "AutoArea")
    private String autoArea;
    @XmlElement(name = "AutoDistrict")
    private String autoDistrict;
    @XmlElement(name = "Region")
    private String region;
    @XmlElement(name = "Town")
    private String town;

    public String getCountry() {
	return this.country;
    }

    public void setCountry(String country) {
	this.country = country;
    }

    public String getAutoRepublic() {
	return this.autoRepublic;
    }

    public void setAutoRepublic(String autoRepublic) {
	this.autoRepublic = autoRepublic;
    }

    public String getLand() {
	return this.land;
    }

    public void setLand(String land) {
	this.land = land;
    }

    public String getArea() {
	return this.area;
    }

    public void setArea(String area) {
	this.area = area;
    }

    public String getAutoArea() {
	return this.autoArea;
    }

    public void setAutoArea(String autoArea) {
	this.autoArea = autoArea;
    }

    public String getAutoDistrict() {
	return this.autoDistrict;
    }

    public void setAutoDistrict(String autoDistrict) {
	this.autoDistrict = autoDistrict;
    }

    public String getRegion() {
	return this.region;
    }

    public void setRegion(String region) {
	this.region = region;
    }

    public String getTown() {
	return this.town;
    }

    public void setTown(String town) {
	this.town = town;
    }

}
