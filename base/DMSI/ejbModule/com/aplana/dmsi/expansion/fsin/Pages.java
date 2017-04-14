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

import java.math.BigInteger;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "Pages")
@XmlAccessorType(XmlAccessType.PROPERTY)
@XmlType(name = "Pages")
public class Pages {
	private BigInteger companion;
	private BigInteger attachment;

	@XmlAttribute
	public BigInteger getCompanion() {
		return this.companion;
	}

	public void setCompanion(BigInteger companion) {
		this.companion = companion;
	}

	@XmlAttribute
	public BigInteger getAttachment() {
		return this.attachment;
	}

	public void setAttachment(BigInteger attachment) {
		this.attachment = attachment;
	}

}
