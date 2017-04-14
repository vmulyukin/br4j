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
package com.aplana.ireferent.types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "WSO_URLFILE", propOrder = {
	    "object",
	    "objectPropertyName", 
	    "name",
	    "creationDate",
	    "version",	   
	    "author",	   
	    "reference",
	    "filesize"
	})

public class WSOURLFile extends WSObject {
	
	@XmlElement(name = "OBJECT", required = true, nillable = true)
	protected WSObject object;	//������ �� ������, � �������� ��������� ������ ����
	
	@XmlElement(name = "OBJECTPROPERTYNAME", required = true, nillable = true)
	protected String objectPropertyName;	//�������� �������� �������, � ������� ������ ���������� ������ ����
	
	@XmlElement(name = "NAME", required = true, nillable = true)
	protected String name;	//��� �����, ��������� �� ��� �������� � ����������, ����������� ������
	
	@XmlElement(name = "CREATIONDATE", required = true, nillable = true)
	protected XMLGregorianCalendar creationDate;	//���� �������� �����
	
	@XmlElement(name = "VERSION", required = true, nillable = true)
	protected Integer version;	//������ ����� (���������� � 1)
	
	@XmlElement(name = "AUTHOR", required = true, nillable = true)
	protected WSOMPerson author;	//����� �����
	
	@XmlElement(name = "REFERENCE", required = true, nillable = true)
	protected WSOUrl reference;	//������ �� ����, �� ������� ����� �������� ����, ��������� ��������� ���������
	
	@XmlElement(name = "FILESIZE", required = true, nillable = true)
	protected Integer filesize;	//������ ����� � ������
	
	public WSObject getObject() {
		return object;
	}
	
	public void setObject(WSObject object) {
		this.object = object;
	}
	
	public String getObjectPropertyName() {
		return objectPropertyName;
	}
	
	public void setObjectPropertyName(String objectPropertyName) {
		this.objectPropertyName = objectPropertyName;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public XMLGregorianCalendar getCreationDate() {
		return creationDate;
	}
	
	public void setCreationDate(XMLGregorianCalendar creationDate) {
		this.creationDate = creationDate;
	}
	
	public Integer getVersion() {
		return version;
	}
	
	public void setVersion(Integer version) {
		this.version = version;
	}
	
	public WSOMPerson getAuthor() {
		return author;
	}
	
	public void setAuthor(WSOMPerson author) {
		this.author = author;
	}
	
	public WSOUrl getReference() {
		return reference;
	}
	
	public void setReference(WSOUrl reference) {
		this.reference = reference;
	}

	public Integer getFilesize() {
		return filesize;
	}

	public void setFilesize(Integer filesize) {
		this.filesize = filesize;
	}
}
