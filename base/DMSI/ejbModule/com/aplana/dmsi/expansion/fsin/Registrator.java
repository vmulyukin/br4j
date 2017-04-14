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
package com.aplana.dmsi.expansion.fsin;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.aplana.dmsi.types.ContainerObject;
import com.aplana.dmsi.types.DMSIObject;
import com.aplana.dmsi.types.Organization;

@XmlRootElement(name = "Registrator")
@XmlAccessorType(XmlAccessType.PROPERTY)
@XmlType(name = "Registrator")
public class Registrator extends DMSIObject implements ContainerObject {

	protected Object containedObject;

	@XmlElements({ @XmlElement(name = "Organization", type = Organization.class) })
	public Object getContainedObject() {
		return this.containedObject;
	}

	public void setContainedObject(Object containedObject) {
		this.containedObject = containedObject;
	}

}
