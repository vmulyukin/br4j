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
package com.aplana.crypto.verifications;

public class CertCheckResult {
	
	public static enum CERT_STATUS {GOOD, REVOKED, UNKNOWN, ERROR};

	private CERT_STATUS status;	
	private String errNum;		
	private String errMSG;		
	
	public CertCheckResult() {
		super();
	}
	
	public CertCheckResult(CERT_STATUS status, String errNum, String errMSG) {
		super();
		this.status = status;
		this.errNum = errNum;
		this.errMSG = errMSG;
	}
	
	public boolean isGood(){
		if(status!=null && status.equals(CERT_STATUS.GOOD)){
			return true;
		} else {
			return false;
		}
	}
	public boolean isCertValid() {
		return status.equals(CERT_STATUS.GOOD);
	}
	
	public String getStatus() {
		return status.name();
	}

	public void setStatus(CERT_STATUS status) {
		this.status = status;
	}

	public String getErrNum() {
		return errNum;
	}

	public void setErrNum(String errNum) {
		this.errNum = errNum;
	}
	
	public String getErrMSG() {
		return errMSG;
	}
	
	public void setErrMSG(String errMSG) {
		this.errMSG = errMSG;
	}
	
}
