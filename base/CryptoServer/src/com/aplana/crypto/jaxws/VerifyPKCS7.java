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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "verifyPKCS7", namespace = "http://crypto.aplana.com/")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "verifyPKCS7", namespace = "http://crypto.aplana.com/", propOrder = {
    "arg0",
    "arg1"
})
public class VerifyPKCS7 {

    @XmlElement(name = "arg0", namespace = "", nillable = true)
    private byte[] arg0;
    @XmlElement(name = "arg1", namespace = "", nillable = true)
    private byte[] arg1;

    /**
     * 
     * @return
     *     returns byte[]
     */
    public byte[] getArg0() {
        return this.arg0;
    }

    /**
     * 
     * @param arg0
     *     the value for the arg0 property
     */
    public void setArg0(byte[] arg0) {
        this.arg0 = arg0;
    }

    /**
     * 
     * @return
     *     returns byte[]
     */
    public byte[] getArg1() {
        return this.arg1;
    }

    /**
     * 
     * @param arg1
     *     the value for the arg1 property
     */
    public void setArg1(byte[] arg1) {
        this.arg1 = arg1;
    }

}
