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

import java.math.BigInteger;
import java.security.cert.X509Certificate;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import com.aplana.dbmi.crypto.SignatureData;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.HtmlAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.StringAttribute;
import com.aplana.dbmi.service.DataServiceBean;

public class CertificateInfo {

	public static final ObjectId SIGNATURE_ATTR_ID = ObjectId.predefined(
			HtmlAttribute.class, "jbr.uzdo.signature");
	private static final ObjectId NAME_ATTR_ID = ObjectId.predefined(
			StringAttribute.class, "jbr.person.lastnameNM");

	public static final String STATE_STYLE_ACTIVE = "active";
	public static final String STATE_STYLE_INACTIVE = "inactive";
	public static final String STATE_STYLE_INVALID = "invalid";

	private String owner = "";
	private String organization = "";
	private String organizationUnit = "";
	private String certificationCenter = "";
	private String organizationPost = "";
	private String validFromDate = "";
	private String validToDate = "";
	private BigInteger serialNumber = new BigInteger("0");
	private String state = "";
	private String stateStyle = STATE_STYLE_INACTIVE;
	private String signState = "";
	private String signStyle = STATE_STYLE_INVALID;
	private boolean signValid = false;
	private String time = "";

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}
	
	public String getOrganization(){
		return organization;
	}
	
	public void setOrganization(String organization){
		this.organization = organization;
	}

	public String getOrganizationUnit(){
		return organizationUnit;
	}
	
	public void setOrganizationUnit(String organizationUnit){
		this.organizationUnit = organizationUnit;
	}
	public String getOrganizationPost() {
		return organizationPost;
	}

	public void setOrganizationPost(String organizationPost) {
		this.organizationPost = organizationPost;
	}
	public String getCertificationCenter() {
		return certificationCenter;
	}

	public void setCertificationCenter(String certificationCenter) {
		this.certificationCenter = certificationCenter;
	}

	public String getValidFromDate() {
		return validFromDate;
	}

	public void setValidFromDate(String validFromDate) {
		this.validFromDate = validFromDate;
	}

	public String getValidToDate() {
		return validToDate;
	}

	public void setValidToDate(String validToDate) {
		this.validToDate = validToDate;
	}

	public BigInteger getSerialNumber() {
		return serialNumber;
	}

	public void setSerialNumber(BigInteger serialNumber) {
		this.serialNumber = serialNumber;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getStateStyle() {
		return stateStyle;
	}

	public void setStateStyle(String stateStyle) {
		this.stateStyle = stateStyle;
	}

	public static List<CertificateInfo> readCertificateInfo(Card card,
			DataServiceBean serviceBean, ResourceBundle resourceBundle,
			DateFormat dateFormat) {
		HtmlAttribute signatureAttribute = (HtmlAttribute) card
				.getAttributeById(SIGNATURE_ATTR_ID);
		
		List<CertificateInfo> resultList = new ArrayList<CertificateInfo>();
		
		if (null == signatureAttribute || null == signatureAttribute.getValue()
				|| signatureAttribute.getStringValue().length() == 0) {
			return resultList;
		}

		List<SignatureData> signatureDatas = SignatureData
				.getAllSignaturesInfo(signatureAttribute.getStringValue(), card);
		for (SignatureData signData : signatureDatas) {

			boolean result = signData.verify(serviceBean, true);
			X509Certificate cert = signData.getCert509();
			boolean certStatus = signData.checkCertificate(cert);
			CertificateInfo certificateInfo = new CertificateInfo();
			
			if(cert != null){
				//�������� ��������� �������
				if (cert.getSubjectDN() != null){
					String subject = cert.getSubjectDN().getName();
					/*
					Pattern p = Pattern.compile("^.*CN=([^,]*).*$");
					Matcher m = p.matcher(subject);
					if (m.matches()) {
						subject = m.group(1);
					}
					*/
					certificateInfo.setOwner(getCertInfoCommonName(subject));
					certificateInfo.setOrganization(getCertInfoOrganisation(subject));
					certificateInfo.setOrganizationUnit(getCertInfoOrganisationUnit(subject));
					certificateInfo.setOrganizationPost(getCertInfoOrganisationPost(subject));
					
				}
				
	
				/*StringTokenizer stringTokenizer = new StringTokenizer(cert
						.getSubjectX500Principal().getName(), ",");
				if (stringTokenizer.countTokens() > 2) {
					stringTokenizer.nextToken();
					String cn = stringTokenizer.nextToken();
					if (cn.length() > 3) {
						certificateInfo.setCertificationCenter(cn.substring(3));
					}
				}*/
				String certificationCenter = new String(cert.getIssuerDN().getName());
				if(cert.getIssuerDN() != null) certificateInfo.setCertificationCenter(certificationCenter);
	
				if(cert.getNotBefore() != null) certificateInfo.setValidFromDate(dateFormat.format(cert
						.getNotBefore()));
				if(cert.getNotAfter() != null) certificateInfo
						.setValidToDate(dateFormat.format(cert.getNotAfter()));
				certificateInfo.setSerialNumber(cert.getSerialNumber());
	
				long currentTime = System.currentTimeMillis();
				if ((cert.getNotBefore() == null || cert.getNotBefore().getTime() < currentTime)
						&& (cert.getNotAfter() == null || currentTime < cert.getNotAfter().getTime())&& certStatus) {
					certificateInfo.setState(resourceBundle
							.getString("ds.showinfo.active"));
					certificateInfo.setStateStyle(STATE_STYLE_ACTIVE);
				} else {
					certificateInfo.setState(resourceBundle
							.getString("ds.showinfo.inactive"));
					certificateInfo.setStateStyle(STATE_STYLE_INVALID);
				}
			}
			
			//���� � ����������� �� ���������� ���������, ���� ������������ � �������, � �������� ���������� ���� ����������
			if(certificateInfo.getOwner() == null || certificateInfo.getOwner().length() == 0){
				Card signer = signData.getSigner();
				if (null != signer) {
					StringAttribute nameAttribute = (StringAttribute) signer
							.getAttributeById(NAME_ATTR_ID);
					if (null != nameAttribute) {
						certificateInfo.setOwner(nameAttribute.getStringValue());
					}
					certificateInfo.setOrganization(signData.getSignerPersonOrganization());
					certificateInfo.setOrganizationUnit(signData.getSignerPersonDepartment());
				}
			}
			
			certificateInfo.setSignValid(result);
			if (result) {
				certificateInfo.setSignStyle(STATE_STYLE_ACTIVE);
				certificateInfo.setSignState(resourceBundle
						.getString("ds.showinfo.valid"));
			} else {
				certificateInfo.setSignStyle(STATE_STYLE_INVALID);
				certificateInfo.setSignState(resourceBundle
						.getString("ds.showinfo.not.valid"));
			}

			if(signData.getTime() != null) certificateInfo.setTime(dateFormat.format(signData.getTime())); else certificateInfo.setTime("");
			
			resultList.add(certificateInfo);
		}

		return resultList;
	}

	public void setSignState(String signState) {
		this.signState = signState;
	}

	public String getSignState() {
		return signState;
	}

	public void setSignStyle(String signStyle) {
		this.signStyle = signStyle;
	}

	public String getSignStyle() {
		return signStyle;
	}

	public boolean isSignValid() {
		return signValid;
	}

	public void setSignValid(boolean signValid) {
		this.signValid = signValid;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}
	
	private static String getProperty(String subject, String property){
		String[] properties = subject.split(", ");
		for(String pair:properties){
			String key = pair.split("=")[0];
			if (key.equals(property)){
				return pair.split("=")[1];
			}
		}
		return "";
	}

	public static String getCertInfoCommonName(String name){
		return getProperty(name, "CN");
	}

	public static String getCertInfoOrganisation(String name){
		return getProperty(name, "O");
	}

	public static String getCertInfoOrganisationUnit(String name){
		return getProperty(name, "OU");
	}

	public static String getCertInfoOrganisationPost(String name){
		return getProperty(name, "T");
	}
}
