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
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "File")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "File", propOrder = { "eds" })
public class File extends DMSIObject {

    @XmlElement(name = "EDS")
    private List<EDS> eds;
    @XmlAttribute(name = "description")
    private String description;
    @XmlAttribute(name = "name")
    private String name;
    @XmlAttribute(name = "location")
    private String location = "out";
    @XmlAttribute(name = "type")
    private FileEnumType type = FileEnumType.DOCUMENT;

    public List<EDS> getEds() {
	if (this.eds == null) {
	    this.eds = new ArrayList<EDS>();
	}
	return this.eds;
    }

    public String getDescription() {
	return this.description;
    }

    public void setDescription(String description) {
	this.description = description;
    }

    public String getName() {
	return this.name;
    }

    public void setName(String name) {
	this.name = name;
    }

    public String getLocation() {
	return this.location;
    }

    public void setLocation(String location) {
	this.location = location;
    }

    public FileEnumType getType() {
	return this.type;
    }

    public void setType(FileEnumType type) {
	this.type = type;
    }

}
