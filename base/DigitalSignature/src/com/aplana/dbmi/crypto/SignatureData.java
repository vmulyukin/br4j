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
package com.aplana.dbmi.crypto;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.aplana.crypto.Base64;
import com.aplana.crypto.CryptoLayer;
import com.aplana.crypto.PKCS7Result;
import com.aplana.crypto.cryptoserviceproxy.CryptoService;
import com.aplana.crypto.cryptoserviceproxy.CryptoServiceException_Exception;
import com.aplana.crypto.cryptoserviceproxy.CryptoServiceService;
import com.aplana.crypto.cryptoserviceproxy.WsPkcs7Result;
import com.aplana.crypto.verifications.impl.CertValidatorCRLJCP;
import com.aplana.crypto.verifications.impl.CertValidatorOCSPJCP;
import com.aplana.dbmi.Portal;
import com.aplana.dbmi.action.DownloadFile;
import com.aplana.dbmi.action.ListProject;
import com.aplana.dbmi.action.Material;
import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.action.Search.TextSearchConfigValue;
import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.BackLinkAttribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.ContextProvider;
import com.aplana.dbmi.model.DataObject;
import com.aplana.dbmi.model.IntegerAttribute;
import com.aplana.dbmi.model.MaterialAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.StringAttribute;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.ServiceException;

public class SignatureData {

	public static final String AED_NEEDSSIGNATTACH = "NeedsSignAttach";
	public static final String AED_ATTACHSIGNATURE = "AttachSignature";
	public static final String PARAM_CERTHASH = "CertHash";
	public static final String PARAM_CERTSTORE = "LKS"; // "HDImageStore";
	public static final String ATTR_CERTHASH = "jbr.certificate.certhash";
	public static final String TEMPLATE_PERSON = "jbr.internalPerson";
	public static final String TEMPLATE_CERTIFICATE = "jbr.certificate";
	//
	// private static final String CONFIG_FILE = "crypto.properties";
	private final static String CONFIG_FILE = "dbmi/card/signature/crypto.properties";
	private final static String CERT_CHECKING_CONFIG_FILE = "dbmi/card/signature/certChecking.properties";
	public static final String CERTIFICATE_CRL_CHECK_ENABLE = "certificate.crl_checking";
	public static final String CERTIFICATE_CRL_URL = "certificate.crl_url";
	public static final String CERTIFICATE_ISSIER_CERT_URL = "certificate.crl_issuer_cert";
	public static final String CERTIFICATE_OCSP_CHECK_ENABLE = "certificate.ocsp_checking";
	public static String verWebSer = "false";
	public static String cryptoServerUrl = null;

	private static CryptoService cryptoService;

	public static final ObjectId certAttrId = ObjectId.predefined(
			StringAttribute.class, "jbr.certificate.cert");
	public static final ObjectId actualCertificateAttrId = ObjectId.predefined(
			CardLinkAttribute.class, "jbr.person.actualCertificate");
	public static final ObjectId certificatePersonAttrId = ObjectId.predefined(
			BackLinkAttribute.class, "jbr.certificate.person");
	
	public static final ObjectId DEPARTMENT_ID = ObjectId.predefined(CardLinkAttribute.class, "jbr.personInternal.department");
	public static final ObjectId ORGANIZATION_ID = ObjectId.predefined(CardLinkAttribute.class, "jbr.person.organization");

	private static Properties checkingConfigProp;
	private String dataString = "";
	private Vector<ObjectId> attributes = new Vector<ObjectId>();
	// private long personCardId = 0;
	private String certHash = "";
	private String signature = "";
	private Card signerPerson = null;
	private String signerPersonDepartmentName = "";
	private String signerPersonOrganizationName = "";
	private DataServiceBean serviceBean = null;
	private X509Certificate cert509 = null;
	private Card signedCard = null;
	private Card certificateCard = null;
	private boolean isAttach = false; // ������� ������
	private String message = "";
	protected static final Log logger = LogFactory.getLog(SignatureData.class);
	private List<SignatureConfig.CardSignAttributes> configMap = null;
	private String pkcs7;
	private Date time;

	public SignatureData(String data, Card card) {
		// ��� �������� �������
		setSignatureData(data, card);
	}

	/*
	 * public SignatureData(String data, Card card, HashMap confMap){
	 * setConfig(confMap); setSignatureData(data, card); signedCard = card; }
	 */
	public SignatureData(SignatureConfig sConf, Card card) {
		// ��� �������� �������
		setConfig(sConf.getAttributesMap());
		signedCard = card;
	}

	public void setConfig(List<SignatureConfig.CardSignAttributes> configMap) {
		this.configMap = configMap;
	}

	public boolean setSignatureData(String data, Card card) {
		// ������� ���� ������ �������� ���
        String traceString = "[TRACE_SIGNATURE] stackTrace for card "+card.getId().getId().toString()+": \r\n";
		StackTraceElement[] stack = Thread.currentThread().getStackTrace();
		for (StackTraceElement stackTraceElement : stack) {
			traceString += " " + stackTraceElement.getClassName() + "." + stackTraceElement.getMethodName() + "(" + stackTraceElement.getFileName() + ":" + stackTraceElement.getLineNumber() + ")\r\n";
		}
		logger.trace(traceString);		

		dataString = data;
		signedCard = card;
		try {
			logger.debug("got string " + data);

			Document doc = null;
			try {
				String strXML = "<root>" + data + "</root>";

				doc = DocumentBuilderFactory.newInstance().newDocumentBuilder()
						.parse(new ByteArrayInputStream(strXML.getBytes()));
			} catch (Exception e) {
				logger.error("Error initializing signature data from xml", e);
			}

			SignatureConfig conf = new SignatureConfig();

			Element root = doc.getDocumentElement();

			Element signNode = (Element) root.getLastChild();
			NodeList cardsList = signNode.getElementsByTagName("card");
			for (int a = 0; a < cardsList.getLength(); a++) {
				Element cardNode = (Element) cardsList.item(a);
				ObjectId cardId = ObjectIdUtils.getObjectId(Card.class,
						cardNode.getAttribute(SignatureConfig.ATTR_ID), true);
				NodeList attrList = cardNode.getElementsByTagName("attr");
				for (int t = 0; t < attrList.getLength(); t++) {
					conf.addAttrToMap(null, (Element) attrList.item(t), cardId);
				}
			}
			setConfig(conf.getAttributesMap());
			certHash = signNode.hasAttribute("cert") ? signNode.getAttribute("cert") : null;
			signature = signNode.getAttribute("signature");
			pkcs7 = signNode.getAttribute("pkcs7");			
			String timeAsString = signNode.getAttribute("time");
			if (timeAsString != null && timeAsString.length()>0){
				SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
				setTime(format.parse(timeAsString));
			}
					
			return true;
		} catch (Exception e) {
			logger.error("Error during SignatureData init: ", e);
			return false;
		}
	}

	public String getAttrXML() {
		String result = "";

		if (configMap == null) {
			logger.error("getAttrXML: no config supplied");
			return "";
		}

		try {
			for(SignatureConfig.CardSignAttributes cardSignAttributes: configMap) {
				String cardIdStr = cardSignAttributes.getCardId();

				Vector<ObjectId> attrArr = cardSignAttributes.getAttrArr();

				if (attrArr.isEmpty() == false) {
					result += "<card id=\"" + cardIdStr + "\">";

					for (Integer v = 0; v < attrArr.size(); v++) {
						String attrValue = "";
						ObjectId attr2signId = (ObjectId) attrArr.get(v);
						Attribute attr2sign = (Attribute) DataObject
								.createFromId(attr2signId);

						result += "<attr " + SignatureConfig.ATTR_TYPE + "=\""
								+ attr2sign.getType() + "\" "
								+ SignatureConfig.ATTR_ID + "=\""
								+ attr2signId.getId().toString() + "\"/>";
					}
					result += "</card>";
				}
			}

		} catch (Exception e) {
			logger.error("getAttrXML: ", e);
		}

		return result;
	}

	public boolean verify(DataServiceBean sBean, boolean hashAttachment) {
		serviceBean = sBean;
		boolean result = false;
		if(pkcs7 != null && pkcs7.length() > 0){
			result = verifyPKCS7();
		}
		else if (certHash != null && certHash.length() > 0) {
			final Certificate cert = getCert();
			result = (isAttach) ? verifyAttach(cert, hashAttachment)
					: verifyString(cert, hashAttachment);
		} 
		return result;
	}

	private boolean verifyPKCS7() {
		boolean result = false;
		try {
			readConfig();

			String data = getAttrValues(serviceBean, false, null);
			if (verWebSer.equals("true")) {
				WsPkcs7Result wsResult = ValidPKCS7(
						hexStringToByteArray(pkcs7),
						Base64.Base64semicolonDelimitedToByteArray(data));

				result = wsResult.isValid();

				CertificateFactory cf = CertificateFactory.getInstance("X509");

				cert509 = (X509Certificate) cf
						.generateCertificate(new ByteArrayInputStream(wsResult
								.getCert()));
				setTime(wsResult.getDate().toGregorianCalendar().getTime());
			} else {
				PKCS7Result pkcs7Result = CryptoLayer
						.getInstance()
						.verifyPKCS7(
								hexStringToByteArray(pkcs7),
								/* Base64.base64ToByteArray(data, false) */Base64
										.Base64semicolonDelimitedToByteArray(data));
				result = pkcs7Result.isValid();
				cert509 = (X509Certificate) pkcs7Result.getCert();
				if(pkcs7Result.getTime() != null) setTime(pkcs7Result.getTime());
			}
		} catch (Exception e) {
			logger.error("verifyAttach: ", e);
		}
		return result;
	}
	
	public static boolean checkCertificate(X509Certificate cert509){
		boolean result = false;
		try {
			readConfig();
			if (verWebSer.equals("true")) {
				result = getCryptoService().checkCertificate(cert509.getEncoded());
			} else {
				loadCheckingConfig();
				boolean crlCheking = Boolean.parseBoolean(checkingConfigProp.getProperty(CERTIFICATE_CRL_CHECK_ENABLE));
				String crlUrl = checkingConfigProp.getProperty(CERTIFICATE_CRL_URL);
				String issuerCertUrl = checkingConfigProp.getProperty(CERTIFICATE_ISSIER_CERT_URL);
				boolean ocspCheking = Boolean.parseBoolean(checkingConfigProp.getProperty(CERTIFICATE_OCSP_CHECK_ENABLE));
				if(!crlCheking && !ocspCheking)
					return true;
				if(crlCheking){
					result = new CertValidatorCRLJCP(CryptoLayer.getInstance(),crlUrl,issuerCertUrl).validate(cert509).isGood();
					if(!result)
						return result;
				}
				if(ocspCheking){
					result = new CertValidatorOCSPJCP(CryptoLayer.getInstance()).validate(cert509).isGood();
					if(!result)
						return result;
				}
			}
		} catch (IOException ex) {
			return true;
		} catch (Exception e) {
			logger.error("verifyAttach: ", e);
			return false;
		}
		return result;
	}
	
	private static void loadCheckingConfig() throws IOException{
		InputStream stream = Portal.getFactory().getConfigService().loadConfigFile(CERT_CHECKING_CONFIG_FILE);
		checkingConfigProp = new Properties();
		checkingConfigProp.load(stream); 
		stream.close();
	}
	

	private static byte[] hexStringToByteArray(String s) {
		byte[] b = new byte[s.length() / 2];
		for (int i = 0; i < b.length; i++) {
			int index = i * 2;
			int v = Integer.parseInt(s.substring(index, index + 2), 16);
			b[i] = (byte) v;
		}
		return b;
	}

	private boolean verifyString(Certificate cert, boolean hashMaterial) {
		boolean result = false;
		try {
			readConfig();
			String values = getAttrValues(serviceBean, hashMaterial, null);
			logger.debug("verifyString values: " + values);
			logger.debug("verifyString signature: " + signature);
			if (verWebSer.equals("true"))
				result = Valid(values, signature, cert);
			else
				result = CryptoLayer.getInstance().checkStringContentSignature(
						values, signature, cert);
		} catch (Exception e) {
			logger.error("verifyString: ", e);
		}
		return result;
	}

	private boolean verifyAttach(Certificate cert, boolean hashMaterial) {
		boolean result = false;

		try {
			readConfig();
			String hash = getAttach(serviceBean, signedCard, hashMaterial);

			if (hash != null) {
				logger.debug("verifyAttach hash: " + hash);
				logger.debug("verifyAttach signature: " + signature);
				if (verWebSer.equals("true"))
					result = Valid(hash, signature, cert);
				else
					result = CryptoLayer.getInstance()
							.checkStringContentSignature(hash, signature, cert);
			} else {
				logger.error("verifyAttach: error getting file's hash");
			}
		} catch (Exception e) {
			logger.error("verifyAttach: ", e);
		}
		return result;
	}

	public String getAttrValues(DataServiceBean sBean, boolean hashMaterial, String materialBaseLink) {
		if (configMap == null) {
			logger.error("getAttrValues: no config supplied");
			return "";
		}

		String result = "";
		String cardIds = "";

		try {
			for(SignatureConfig.CardSignAttributes cardSignAttributes: configMap) {
				String cardIdStr = cardSignAttributes.getCardId();

				Vector<ObjectId> attrArr = cardSignAttributes.getAttrArr();

				if (attrArr.isEmpty() == false) {
					Card card = signedCard;
					if (!cardIdStr.equals(SignatureConfig.CURRENTCARD_MAPKEY)) {
						ObjectId cardId = ObjectIdUtils.getObjectId(Card.class,
								cardIdStr, true);
						card = (Card) sBean.getById(cardId);
						cardIds += card.getId().getId() + ",";
					}
					if (card != null) {
						cardIds += card.getId().getId() + ",";
						for (Integer v = 0; v < attrArr.size(); v++) {
							String attrValue = "";
							ObjectId attr2signId = (ObjectId) attrArr.get(v);
							logger.debug("attr2signId = "
									+ attr2signId.getId().toString());
							Attribute attr2sign = (Attribute) card
									.getAttributeById(attr2signId);
							if (attr2sign != null) {
								if (attr2sign.getType().equals(
										Attribute.TYPE_MATERIAL)) {
									MaterialAttribute material = (MaterialAttribute) attr2sign;
									attrValue = materialBaseLink == null ? getAttach(sBean, card, hashMaterial) 
											: material.getMaterialType() == MaterialAttribute.MATERIAL_FILE ? materialBaseLink + card.getId().getId() 
											: material.getMaterialType() == MaterialAttribute.MATERIAL_URL ? material.getName() 
											: "";
								} else {
									// Base64Attr1;Base64Attr2;...;Base64AttrN
									attrValue = Base64
											.byteArrayToBase64(attr2sign
													.getStringValue()
													.getBytes());
								}
							}
							if (result.length() > 0) {
								result += ";";
							}
							result += attrValue;
						}
					}
				}
			}

		} catch (Exception e) {
			logger.error("getAttrValues: ", e);
		}

		if (hashMaterial) {
			result = Base64Encoder.byteArrayToBase64(result.getBytes());
		}

		logger.trace("HashCards(" + cardIds + ")=" + (result.length() > 512 ? result.substring(0, 512) + "..." : result));
		return result;
	}

	public Card getSigner() {
		// return getSignerByCert();

		if (signerPerson == null) {
			final Card certificateCard = getCertificateCardByHash();
			if (certificateCard != null) {
				try {
					BackLinkAttribute a = (BackLinkAttribute) certificateCard
							.getAttributeById(certificatePersonAttrId);
					ListProject projectAction = new ListProject();
					projectAction.setCard(certificateCard.getId());
					projectAction.setAttribute(a.getId());
					final SearchResult r = (SearchResult) serviceBean
							.doAction(projectAction);
					Collection<Card> cards = r.getCards();

					if (cards.size() == 0) {
						logger.error("Could't find person by certificate "
								+ certificateCard.getId().getId());
						return null;
					}
					if (cards.size() > 1) {
						logger.info("more than one person cards have equal certificate");
					}
					final Iterator<Card> iter = cards.iterator();
					signerPerson = iter.next();
					signerPerson = (Card) serviceBean.getById(signerPerson
							.getId());
				} catch (ServiceException ex) {
					logger.error("Error searching person by certificate card",
							ex);
				} catch (DataException ex) {
					logger.error("Error searching person by certificate card",
							ex);
				}
			}
		}
		return signerPerson;

		/*
		 * if(signerPerson == null){ try{ ObjectId id = new ObjectId(Card.class,
		 * personCardId); signerPerson = (Card)serviceBean.getById(id);
		 * }catch(Exception e){
		 * logger.error("getSigner:couldn't get Card of person " + personCardId,
		 * e); message =
		 * ContextProvider.getContext().getLocaleMessage("signature.error.usercard"
		 * ); } } return signerPerson;
		 */
	}
	
	public String getSignerPersonOrganization(){
		
		if(signerPersonOrganizationName.length() > 0) return signerPersonOrganizationName;
		
		Card personCard = getSigner();
		if(personCard == null) return "";
		
		CardLinkAttribute orgAttr = (CardLinkAttribute) personCard.getAttributeById(ORGANIZATION_ID);
		if(orgAttr == null || orgAttr.isEmpty()) return "";
		
		ObjectId orgCardId = orgAttr.getSingleLinkedId();
		Search search = new Search();
		search.setByCode(true);
		search.setWords(orgCardId.getId().toString());
		SearchResult.Column column = new SearchResult.Column();
		column.setAttributeId(Attribute.ID_NAME);
		search.setColumns(Collections.singleton(column));
		Card orgCard = null;
		try{
			orgCard = (Card) ((SearchResult) serviceBean.doAction(search)).getCards().iterator().next();
		} catch(Exception e){e.printStackTrace();}
		if(orgCard == null || orgCard.getAttributeById(Attribute.ID_NAME) == null) return "";
		signerPersonOrganizationName = orgCard.getAttributeById(Attribute.ID_NAME).getStringValue();
		return signerPersonOrganizationName;
	}
	
	public String getSignerPersonDepartment(){
		
		if(signerPersonDepartmentName.length() > 0) return signerPersonDepartmentName;
		
		Card personCard = getSigner();
		if(personCard == null) return "";
		
		CardLinkAttribute depAttr = (CardLinkAttribute) personCard.getAttributeById(DEPARTMENT_ID);
		if(depAttr == null || depAttr.isEmpty()) return "";
		
		ObjectId depCardId = depAttr.getSingleLinkedId();
		Search search = new Search();
		search.setByCode(true);
		search.setWords(depCardId.getId().toString());
		SearchResult.Column column = new SearchResult.Column();
		column.setAttributeId(Attribute.ID_NAME);
		search.setColumns(Collections.singleton(column));
		Card depCard = null;
		try{
			depCard = (Card) ((SearchResult) serviceBean.doAction(search)).getCards().iterator().next();
		} catch(Exception e){e.printStackTrace();}
		if(depCard == null || depCard.getAttributeById(Attribute.ID_NAME) == null) return "";
		signerPersonDepartmentName = depCard.getAttributeById(Attribute.ID_NAME).getStringValue();
		return signerPersonDepartmentName;
	}

	/*
	 * @SuppressWarnings("unchecked") public Card getSignerByCert(){
	 * if(signerPerson == null){ try{ Search search = new Search();
	 * search.setWords(this.certHash); search.setByAttributes(true);
	 * search.addStringAttribute(ObjectId.predefined(StringAttribute.class,
	 * ATTR_CERTHASH)); final List<DataObject> templates = new
	 * ArrayList<DataObject>(1);
	 * templates.add(DataObject.createFromId(ObjectId.predefined(Template.class,
	 * TEMPLATE_PERSON))); search.setTemplates(templates);
	 * 
	 * final Collection<Card> cards = ((SearchResult)
	 * serviceBean.doAction(search)).getCards();
	 * 
	 * if (cards.size() == 0) { logger.error("Could't find person by certhash "+
	 * this.certHash); return null; } if (cards.size() > 1) {
	 * logger.info("more than one person cards have equal certhash"); } final
	 * Iterator<Card> iter = cards.iterator(); signerPerson = iter.next();
	 * signerPerson = (Card) serviceBean.getById(signerPerson.getId());
	 * }catch(Exception e){
	 * logger.error("Error searching person card by cert hash", e); } } return
	 * signerPerson; }
	 */

	public Card getCertificateCardByHash() {
		if (certificateCard == null) {
			try {
				if(this.certHash == null || this.certHash.equals("")){
					logger.info("No certificate hash found.");
					return null;
				}
				Search search = new Search();
				//search.setWords(this.certHash);
				search.setByAttributes(true);
				search.addStringAttribute(ObjectId.predefined(
						StringAttribute.class, ATTR_CERTHASH), this.certHash, TextSearchConfigValue.EXACT_MATCH);
				final List<DataObject> templates = new ArrayList<DataObject>(1);
				templates.add(DataObject.createFromId(ObjectId.predefined(
						Template.class, TEMPLATE_CERTIFICATE)));
				search.setTemplates(templates);

				final Collection<Card> cards = ((SearchResult) serviceBean
						.doAction(search)).getCards();

				if (cards.size() == 0) {
					logger.error("Could't find certificate card by certhash "
							+ this.certHash);
					return null;
				}
				if (cards.size() > 1) {
					logger.warn("more than one certificate cards have equal certhash");
				}
				final Iterator<Card> iter = cards.iterator();
				certificateCard = iter.next();
				certificateCard = (Card) serviceBean.getById(certificateCard
						.getId());
			} catch (Exception e) {
				logger.error("Error searching certificate card by cert hash", e);
			}
		}
		return certificateCard;
	}

	private Certificate getCert() {
		Certificate cert = null;

		final Card certificateCard = getCertificateCardByHash();

		if (certificateCard != null) {

			final Attribute certAttr = certificateCard
					.getAttributeById(ObjectId.predefined(
							StringAttribute.class, "jbr.certificate.cert"));

			if (certAttr != null && certAttr.getStringValue().length() > 0) {
				try {
					cert = CryptoLayer.getInstance().getCertFromStringBase64(
							certAttr.getStringValue());
					cert509 = (X509Certificate) CryptoLayer.getInstance()
							.getCertFromStringBase64(certAttr.getStringValue());
				} catch (Exception e) {
					logger.error("getCert: ", e);
					message = ContextProvider.getContext().getLocaleMessage(
							"signature.error.cert");
				}
			} else {
				message = ContextProvider.getContext().getLocaleMessage(
						"signature.error.nocert");
			}
		}
		return cert;
	}

	public X509Certificate getCert509() {
		return cert509;
	}

	public String getMessage() {
		return message;
	}

	public String getAttach(DataServiceBean sBean, Card card, boolean hash)
			throws IOException {
		String result = null;
		readConfig();
		try {
			
			DownloadFile action = new DownloadFile();
			action.setCardId(card.getId());
			Material fileMaterial = (Material) sBean.doAction(action);
			InputStream fileData = fileMaterial.getData();

			byte[] streamBytes = new byte[fileMaterial.getLength()];
			fileData.read(streamBytes);
			if (hash) {
				if (verWebSer.equals("true"))
					result = Base64Encoder
							.byteArrayToBase64(ValidDigest(streamBytes));
				else
					result = Base64Encoder.byteArrayToBase64(CryptoLayer
							.getInstance().getByteArrayDigest(streamBytes));
			} else {
				result = Base64Encoder.byteArrayToBase64(streamBytes);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	public static void readConfig() throws IOException {

		try{
			CryptoLayer.getInstance();
		} catch (RuntimeException ex){
			CryptoLayer.getInstance(Portal.getFactory().getConfigService());
		}
		
		InputStream inputOptions = Portal.getFactory().getConfigService()
				.loadConfigFile(CONFIG_FILE);

		Properties options = new Properties();
		options.load(inputOptions);
		inputOptions.close();
		verWebSer = options.getProperty("verWebSer");
		cryptoServerUrl = options.getProperty("host");

	}

	private byte[] ValidDigest(byte[] streamBytes)
			throws CryptoServiceException_Exception, MalformedURLException {
		byte[] result = getCryptoService().getByteArrayDigest(streamBytes);
		return result;
	}

	private boolean Valid(String hash, String sign, Certificate cert)
			throws CryptoServiceException_Exception,
			CertificateEncodingException, MalformedURLException {
		byte[] certAsByteArr = cert.getEncoded();
		String strSert = Base64.byteArrayToBase64(certAsByteArr);
		boolean result = getCryptoService().checkStringContentSignature(hash,
				sign, strSert);
		return result;
	}

	private WsPkcs7Result ValidPKCS7(byte[] pkcs7, byte[] data)
			throws MalformedURLException, CryptoServiceException_Exception {
		WsPkcs7Result result = getCryptoService().verifyPKCS7(pkcs7, data);
		return result;
	}

	private static CryptoService getCryptoService() throws MalformedURLException {
		if (cryptoService == null) {
			CryptoServiceService service = new CryptoServiceService(new URL(
					cryptoServerUrl), new QName("http://crypto.aplana.com/",
					"CryptoServiceService"));
			cryptoService = service.getCryptoServicePort();
		}
		return cryptoService;
	}

	public static List<SignatureData> getAllSignaturesInfo(String data,
			Card card) {
		List<SignatureData> result = new ArrayList<SignatureData>();

		Document doc = null;
		try {
			String strXML = "<root>" + data + "</root>";

			doc = DocumentBuilderFactory.newInstance().newDocumentBuilder()
					.parse(new ByteArrayInputStream(strXML.getBytes()));

			Element root = doc.getDocumentElement();

			NodeList nodeList = root.getChildNodes();

			for (int i = 0; i < nodeList.getLength(); i++) {
				Element signNode = (Element) nodeList.item(i);

				TransformerFactory transformerFactory = TransformerFactory
						.newInstance();
				Transformer transformer = transformerFactory.newTransformer();
				DOMSource source = new DOMSource(signNode);

				ByteArrayOutputStream out = new ByteArrayOutputStream();
				StreamResult oneSignXml = new StreamResult(out);
				transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION,
						"yes");
				transformer.transform(source, oneSignXml);

				SignatureData signatureData = new SignatureData(out.toString(),
						card);
				result.add(signatureData);
			}

		} catch (Exception e) {
			logger.error("Error initializing signature data from xml", e);
		}
		return result;
	}

	public String getDataString() {
		return dataString;
	}

	public String getCertHash() {
		return certHash;
	}

	public Date getTime() {
		return time;
	}

	public void setTime(Date time) {
		this.time = time;
	}
}
