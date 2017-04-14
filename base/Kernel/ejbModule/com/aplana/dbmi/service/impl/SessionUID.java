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
package com.aplana.dbmi.service.impl;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Random;

public class SessionUID /*implements Serializable*/ {

	public static final int LENGTH = 12;
	
	private static final long serialVersionUID = 1L;
	private byte[] uid;
	
	//public SessionUID() { }
	
	public SessionUID(byte[] uid) {
		if (uid.length != LENGTH)
			throw new IllegalArgumentException("uid length must be exactly " + LENGTH + " bytes");
		this.uid = uid;
	}
	
	public byte[] getUid() {
		return uid;
	}

	public boolean equals(Object obj) {
		return SessionUID.class.equals(obj.getClass()) &&
				Arrays.equals(uid, ((SessionUID) obj).uid);
	}

	public int hashCode() {
		return new BigInteger(uid).intValue();
	}
	
	public static SessionUID generate() {
		byte[] bytes = new byte[LENGTH];
		new Random(System.currentTimeMillis()).nextBytes(bytes);
		return new SessionUID(bytes);
	}
}
