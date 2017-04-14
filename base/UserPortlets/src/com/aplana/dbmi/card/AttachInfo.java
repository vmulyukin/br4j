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
package com.aplana.dbmi.card;

public class AttachInfo {	
	
	private String attachText;
	private String attId="";
	private String hash;	
	private String data;	
	private String attrXML = "";
	private String currentSignature;
	private boolean isPrime;
	
	public String getAttachText() {
		return attachText;
	}
	public void setAttachText(String attachText) {
		this.attachText = attachText;
	}
	public String getAttId() {
		return attId;
	}
	public void setAttId(String attId) {
		this.attId = attId;
	}
	public String getHash() {
		return hash;
	}
	public void setHash(String hash) {
		this.hash = hash;
	}	
	public String getAttrXML(){
		return attrXML;
	}
	public void setAttrXML(String xml){
		attrXML = xml;
	}
	public String getCurrentSignature() {
		return currentSignature;
	}
	public void setCurrentSignature(String currentSignature) {
		this.currentSignature = currentSignature;
	}
	public String getData() {
		return data;
	}
	public void setData(String data) {
		this.data = data;
	}
	public boolean isPrime(){
		return isPrime;
	}
	public void setPrime(boolean isPrime){
		this.isPrime = isPrime;
	}
}
