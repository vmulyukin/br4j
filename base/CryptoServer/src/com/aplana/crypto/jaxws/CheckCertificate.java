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

package com.aplana.crypto.jaxws;

import java.security.cert.X509Certificate;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "checkCertificate", namespace = "http://crypto.aplana.com/")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "checkCertificate", namespace = "http://crypto.aplana.com/")
public class CheckCertificate {

    @XmlElement(name = "arg0", namespace = "", nillable = true)
    private byte[] arg0;

	public byte[] getArg0() {
		return arg0;
	}

	public void setArg0(byte[] arg0) {
		this.arg0 = arg0;
	}


}
