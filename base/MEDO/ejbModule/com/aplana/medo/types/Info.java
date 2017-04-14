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
package com.aplana.medo.types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;

import com.aplana.dmsi.types.DMSIObject;

@XmlAccessorType(XmlAccessType.PROPERTY)
@XmlType(name = "", propOrder = { "regNum", "regDate", "rubric", "vid",
	"event", "image", "directionInfo", "notify" })
public class Info extends DMSIObject {

    protected String regNum;
    protected XMLGregorianCalendar regDate;
    protected Rubric rubric;
    protected Vid vid;
    protected Event event;
    protected Image image;
    protected DirectionInfo directionInfo;
    protected Notify notify;
    protected Author author;

    @XmlElement(name = "RegNum", required = true)
    public String getRegNum() {
	return regNum;
    }

    public void setRegNum(String value) {
	this.regNum = value;
    }

    @XmlElement(name = "RegDate", required = true)
    @XmlSchemaType(name = "dateTime")
    public XMLGregorianCalendar getRegDate() {
	return regDate;
    }

    public void setRegDate(XMLGregorianCalendar value) {
	this.regDate = value;
    }

    @XmlElement(name = "Rubric", required = true)
    public Rubric getRubric() {
	return rubric;
    }

    public void setRubric(Rubric value) {
	this.rubric = value;
    }

    @XmlElement(name = "Vid", required = true)
    public Vid getVid() {
	return vid;
    }

    public void setVid(Vid value) {
	this.vid = value;
    }

    @XmlElement(name = "Event", required = true)
    public Event getEvent() {
	return event;
    }

    public void setEvent(Event value) {
	this.event = value;
    }

    @XmlElement(name = "Image", required = true)
    protected Image getImage() {
	return this.image;
    }

    protected void setImage(Image image) {
	this.image = image;
    }

    @XmlElement(name = "DirectionInfo", required = true)
    protected DirectionInfo getDirectionInfo() {
	return this.directionInfo;
    }

    protected void setDirectionInfo(DirectionInfo directionInfo) {
	this.directionInfo = directionInfo;
    }

    @XmlElement(name = "Notify", required = true)
    public Notify getNotify() {
	return notify;
    }

    public void setNotify(Notify value) {
	this.notify = value;
    }

    @XmlTransient
    public Author getAuthor() {
	return this.author;
    }

    public void setAuthor(Author author) {
	this.author = author;
    }

}