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
package com.aplana.dbmi.jbr.query;

import com.aplana.dbmi.jbr.processors.AddDigitalSignature;
import com.aplana.crypto.CryptoLayer;
import com.aplana.crypto.Base64;
import com.aplana.dbmi.model.*;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.impl.ActionQueryBase;
import com.aplana.dbmi.service.impl.ObjectQueryBase;
import com.aplana.dbmi.service.impl.SaveQueryBase;
import com.aplana.dbmi.service.impl.query.WriteQuery;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.action.Action;
import com.aplana.dbmi.action.DownloadFile;
import com.aplana.dbmi.action.Material;
import com.aplana.dbmi.action.CreateCard;
import com.aplana.dbmi.action.UploadFile;
import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.Portal;
import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.InputStream;
import java.io.IOException;
import javax.xml.parsers.*;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import org.xml.sax.SAXException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import java.util.Iterator;
import java.util.Vector;
import java.util.Collection;
import java.util.HashMap;
import com.aplana.dbmi.action.LockObject;
import com.aplana.dbmi.action.UnlockObject;
import com.aplana.dbmi.model.MaterialAttribute;
import java.util.List;
import java.util.ArrayList;
import com.aplana.dbmi.action.file.DatabaseIOException;
import com.aplana.dbmi.action.file.UploadFilePart;

public class DoAddDigitalSignature extends ActionQueryBase implements WriteQuery {
	private static final String CONFIG_FOLDER = "dbmi/card/signature";
	private static final String CONFIG_FILE = CONFIG_FOLDER
			+ "/signAttributes.xml";
	private static final String RESULT_FILE_NAME = "SignatureInfo.xml";
	private static final String TAG_TEMPLATE = "template";
	private static final String ATTR_ID = "id";
	private static final String TAG_ATTR = "attribute";
	private static final String CURRENTCARD_MAPKEY = "0";
	private static final String ATTR_TYPE = "type";
	private static final String TAG_LINK = "link";
	
	private static final String TAG_CARD = "card";
	private static final String TAG_COLUMN = "column";
	private static final String TAG_VALUE = "value";
	private static final String TAG_SIGNATURE = "signature";
	private static final String TAG_CERTIFICATE = "certificate";

	private static final String SIGN = "jbr.uzdo.signature";
	private static final ObjectId FILE_TEMPLATE_ID = ObjectId.predefined(
			Template.class, "jbr.file");
	private static final ObjectId DOCLINKS_ATTRIBUTE_ID = ObjectId.predefined(
			CardLinkAttribute.class, "jbr.files");
	private static final String ATTR_CERTHASH = "jbr.certificate.certhash";
	private static final String TEMPLATE_CERTIFICATE = "jbr.certificate";

	private Card contextCard = null;
	private HashMap configMap = null;
	private int localPos = 0;
	private int globalPos = 0;
	private static final int PART_SIZE = 100 * 1024; // 100k
	final static int BUFSIZE_UPLOADPART = 32768;
	private byte[] data = new byte[PART_SIZE];

	@Override
	public Object processQuery() throws DataException {
		configMap = new HashMap();
		AddDigitalSignature action = (AddDigitalSignature) getAction();
		contextCard = loadCard(action.getCardId());
		if (contextCard == null) {
			return null;
		} else {
			getAttributesMap();
			String par = null;
			try {
				par = getAttrValues();
			} catch (Exception e) {
				logger.error("getAttrValues: ", e);
				return null;
			}
			boolean b = uploadFile(par.getBytes());
	return null;
		}
	}

	private void getAttributesMap() {
		Document doc;
		try {
			doc = initFromXml(Portal.getFactory().getConfigService()
					.loadConfigFile(CONFIG_FILE));
			Element root = doc.getDocumentElement();
			NodeList allTmplList = root.getElementsByTagName(TAG_TEMPLATE);
			for (int a = 0; a < allTmplList.getLength(); a++) {
				Element tmplNode = (Element) allTmplList.item(a);
				Long tempId = Long.parseLong(tmplNode.getAttribute(ATTR_ID));
				if (tempId != null
						&& tempId.equals(contextCard.getTemplate().getId())) {
					collectAttributes(tmplNode, contextCard, true);
				}
			}
		} catch (Exception e) {
			logger.error("Can't read signature attributes configuration", e);
		}
	}

	private String getAttrValues() throws UnsupportedEncodingException,
			ParserConfigurationException, SAXException, IOException,
			TransformerFactoryConfigurationError, TransformerException {
		if (configMap == null) {
			logger.error("getAttrValues: no config supplied");
			return "";
		}
		String result = "";
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = dbf.newDocumentBuilder();
		// ������� ����� ���-��������
		Document xmlDoc = builder.newDocument();
		Element rootElement = xmlDoc.createElement("root");
		try {
			Iterator itrKeys = configMap.keySet().iterator();
			while (itrKeys.hasNext()) {
				String cardIdStr = (String) itrKeys.next();
				Vector attrArr = (Vector) configMap.get(cardIdStr);
				if (attrArr.isEmpty() == false) {
					Element cardElement = xmlDoc.createElement(TAG_CARD);
					Card card1;
					if (!cardIdStr.equals(CURRENTCARD_MAPKEY)) {
						ObjectId cardId = ObjectIdUtils.getObjectId(Card.class,
								cardIdStr, true);
						card1 = loadCard(cardId);
						cardElement.setAttribute(ATTR_ID, cardIdStr);
					} else {
						card1 = contextCard;
						cardElement.setAttribute(ATTR_ID, contextCard.getId()
								.getId().toString());
					}
					rootElement.appendChild(cardElement);
					if (card1 != null) {
						for (Integer v = 0; v < attrArr.size(); v++) {
							String attrValue = "";
							ObjectId attr2signId = (ObjectId) attrArr.get(v);
							logger.debug("attr2signId = "
									+ attr2signId.getId().toString());
							Attribute attr2sign = (Attribute) card1
									.getAttributeById(attr2signId);
							Element columnElement = xmlDoc
									.createElement(TAG_COLUMN);
							Element value = xmlDoc.createElement(TAG_VALUE);
							if (attr2sign != null) {
								columnElement.setAttribute(ATTR_ID, attr2signId
										.getId().toString());
								columnElement.setAttribute(ATTR_TYPE, attr2sign
										.getType().toString());
								if (attr2sign.getType().equals(
										Attribute.TYPE_MATERIAL)) {
									attrValue = "" + getAttachHash(card1);
								} else {
									attrValue = attr2sign.getStringValue();
								}
								value.setTextContent(attrValue);
							}
							columnElement.appendChild(value);
							cardElement.appendChild(columnElement);
						}
					}
				}
			}
			Element signElement = xmlDoc.createElement(TAG_SIGNATURE);
			Element certElement = xmlDoc.createElement(TAG_CERTIFICATE);
			ObjectId preSign = ObjectId.predefined(StringAttribute.class, SIGN);
			if (preSign != null) {
				Attribute signAttr = (Attribute) contextCard
						.getAttributeById(preSign);
				if (signAttr != null && signAttr.getStringValue().length() > 0) {
					Document signDoc = initFromXml(new ByteArrayInputStream(
							signAttr.getStringValue().getBytes()));
					Element rootSign = signDoc.getDocumentElement();
					String s = rootSign.getAttribute("signature");
					signElement.setTextContent(s);
					String c = rootSign.getAttribute("cert");
					Card certificateCard = getCertificateCardByHash(c);
					if (certificateCard != null) {
						Attribute certAttr = certificateCard
								.getAttributeById(ObjectId.predefined(
										StringAttribute.class,
										"jbr.certificate.cert"));
						certElement.setTextContent(certAttr.getStringValue());
					}
				}
			}
			rootElement.appendChild(signElement);
			rootElement.appendChild(certElement);
		} catch (Exception e) {
			logger.error("getAttrValues: ", e);
		}
		xmlDoc.appendChild(rootElement);
		final StringWriter stw = new StringWriter();
		final Transformer serializer = TransformerFactory.newInstance()
				.newTransformer();
		serializer.transform(new DOMSource(xmlDoc), new StreamResult(stw));
		return stw.toString();
	}

	private Document initFromXml(InputStream configFile) {
		Document doc = null;
		try {
			doc = DocumentBuilderFactory.newInstance().newDocumentBuilder()
					.parse(configFile);
		} catch (Exception e) {
			logger.error(
					"Error initializing signature attributes configuration", e);
		}
		return doc;
	}

	private void collectAttributes(Element aNode, Card card,
			boolean isContextCard) {
		String mapValue = "";
		try {
			NodeList childs = aNode.getChildNodes();
			for (int a = 0; a < childs.getLength(); a++) {
				if (childs.item(a).getNodeName().equals("#text") == false) {
					Element child = (Element) childs.item(a);
					if (child.getTagName().equalsIgnoreCase(TAG_ATTR)) {
						// �������� ����� �������� � map
						if (isContextCard) {
							addAttrToMap(aNode, child);
						} else {
							addAttrToMap(aNode, child, card.getId());
						}
					} else if (child.getTagName().equalsIgnoreCase(TAG_LINK)) {
						ObjectId clId = ObjectIdUtils.getObjectId(
								CardLinkAttribute.class,
								child.getAttribute(ATTR_ID), false);
						CardLinkAttribute clAttr = (CardLinkAttribute) card
								.getAttributeById(clId);
						if (clAttr != null && clAttr.getIdsLinked() != null) {
							Search search = new Search();
							search.setByCode(true);
							search.setWords(ObjectIdUtils
									.numericIdsToCommaDelimitedString(ObjectIdUtils
											.collectionToSetOfIds(clAttr
													.getIdsLinked())));
							final Collection<Card> cards = ((SearchResult) execAction(search))
									.getCards();
							if (cards != null && cards.size() > 0) {
								final Iterator<Card> iter = cards.iterator();
								while (iter.hasNext()) {
									Card linkedCard = loadCard(iter.next()
											.getId());
									collectAttributes(child, linkedCard, false);
								}
							}
						}
					}
				}
			}
		} catch (Exception e) {
			logger.error("Error while collecting attributes for sign", e);
		}
	}

	private void addAttrToMap(Element parentNode, Element attrNode) {
		addAttrToMap(parentNode, attrNode, null);
	}

	public void addAttrToMap(Element parentNode, Element attrNode,
			ObjectId cardId) {
		String mapKey = CURRENTCARD_MAPKEY;
		if (cardId != null) {
			mapKey = cardId.getId().toString();
		}
		ObjectId attrId = makeAttributeId(attrNode.getAttribute(ATTR_TYPE),
				attrNode.getAttribute(ATTR_ID));
		attrId = ObjectIdUtils.getObjectId(attrId.getType(), attrId.getId()
				.toString(), false);
		Vector attrArr = new Vector();
		if (configMap.containsKey(mapKey)) {
			attrArr = (Vector) configMap.get(mapKey);
		}
		attrArr.add(attrId);
		configMap.put(mapKey, attrArr);
	}

	private static ObjectId makeAttributeId(Object type, String id) {
		if (Attribute.TYPE_STRING.equals(type))
			return new ObjectId(StringAttribute.class, id);
		if (Attribute.TYPE_TEXT.equals(type))
			return new ObjectId(TextAttribute.class, id);
		if (Attribute.TYPE_HTML.equals(type))
			return new ObjectId(HtmlAttribute.class, id);
		if (Attribute.TYPE_INTEGER.equals(type))
			return new ObjectId(IntegerAttribute.class, id);
        if (Attribute.TYPE_LONG.equals(type))
			return new ObjectId(LongAttribute.class, id);
		if (Attribute.TYPE_DATE.equals(type))
			return new ObjectId(DateAttribute.class, id);
		if (Attribute.TYPE_LIST.equals(type))
			return new ObjectId(ListAttribute.class, id);
		if (Attribute.TYPE_TREE.equals(type))
			return new ObjectId(TreeAttribute.class, id);
		if (Attribute.TYPE_PERSON.equals(type))
			return new ObjectId(PersonAttribute.class, id);
		if (Attribute.TYPE_CARD_LINK.equals(type))
			return new ObjectId(CardLinkAttribute.class, id);
		if (Attribute.TYPE_MATERIAL.equals(type))
			return new ObjectId(MaterialAttribute.class, id);
		throw new IllegalArgumentException("Unknown attribute type: " + type);
	}

	public Card getCertificateCardByHash(String certHash) {
		Card certificateCard = null;
		if (certificateCard == null) {
			try {
				Search search = new Search();
				search.setWords(certHash);
				search.setByAttributes(true);
				search.addStringAttribute(ObjectId.predefined(
						StringAttribute.class, ATTR_CERTHASH));
				final List<DataObject> templates = new ArrayList<DataObject>(1);
				templates.add(DataObject.createFromId(ObjectId.predefined(
						Template.class, TEMPLATE_CERTIFICATE)));
				search.setTemplates(templates);

				final Collection<Card> cards = ((SearchResult) execAction(search))
						.getCards();

				if (cards.size() == 0) {
					logger.error("Could't find certificate card by certhash ");
					return null;
				}
				if (cards.size() > 1) {
					logger.info("more than one certificate cards have equal certhash");
				}
				final Iterator<Card> iter = cards.iterator();
				certificateCard = iter.next();
				// certificateCard = (Card)
				// serviceBean.getById(certificateCard.getId());
			} catch (Exception e) {
				logger.error("Error searching certificate card by cert hash", e);
			}
		}
		return certificateCard;
	}

	/*
	 * ��������� ��������� ���� � ��������. �������� � ������� ����� ��������
	 * ���� ������������ �� ����� �����
	 */
	private boolean uploadFile(byte[] inFileBytes) {
		// �������� id ������� ��������
		ObjectId targetId = contextCard.getId();

		// ������� �������� ��������
		Card fileCard;
		try {
			fileCard = createFileCard();
		} catch (Exception e) {
			logger.error("Could not create card Material", e);
			return false;
		}

		// ��������� � �������� �������� ����
		try {
			attachFileToFileCard(fileCard, inFileBytes);
		} catch (Exception e) {
			logger.error("Unable to download the file  to the card material", e);
			return false;
		}

		// ������������ �������� �������� �������� � ������ ������� ��������
		CardLinkAttribute docLinks = contextCard
				.getCardLinkAttributeById(DOCLINKS_ATTRIBUTE_ID);
		if (docLinks == null) {
			logger.error("Attribute " + DOCLINKS_ATTRIBUTE_ID
					+ " in the target card " + targetId.getId()
					+ " was not found");
			return false;
		}
		// docLinks.addLinkedId(fileCard.getId());
		docLinks.addLabelLinkedCard(fileCard);

		// ��������� ������� ��������
		try {
			execAction(new LockObject(contextCard.getId()));
		} catch (Exception e) {
			logger.error("Unable to lock the card " + targetId.getId()
					+ ", to add material", e);
			return false;
		}
		boolean isSuccess = true;
		try {
			contextCard.setCanWrite(true);
			saveObject(contextCard);
		} catch (Exception e) {
			logger.error("Failed to save target card " + targetId.getId(), e);
			isSuccess = false;
		} finally {
			try {
				execAction(new UnlockObject(contextCard.getId()));
			} catch (Exception e) {
				logger.error("Unable to unlock the card " + targetId.getId(), e);
			}
		}
		return isSuccess;
	}

	// ���������� ����� file � �������� �������� �������� card
	private void attachFileToFileCard(Card card, byte[] inFileBytes)
			throws DataException {
		ByteArrayInputStream bais = new ByteArrayInputStream(inFileBytes);
		UploadFile uploadAction = new UploadFile();
		uploadAction.setCardId(card.getId());
		uploadAction.setFileName(RESULT_FILE_NAME);
		uploadAction.setLength(bais.available());
		uploadAction.setData(bais);
		uploadPart(bais, uploadAction);
		execAction(uploadAction);
		MaterialAttribute attr = (MaterialAttribute) card
				.getAttributeById(Attribute.ID_MATERIAL);
		attr.setMaterialName(RESULT_FILE_NAME);
		attr.setMaterialType(MaterialAttribute.MATERIAL_FILE);
		StringAttribute name = (StringAttribute) card
				.getAttributeById(Attribute.ID_NAME);
		name.setValue(RESULT_FILE_NAME);

		saveObject(card);
	}

	// �������� ������� ��������
	private Card createFileCard() throws Exception {
		CreateCard createCardAction = new CreateCard();
		createCardAction.setTemplate(FILE_TEMPLATE_ID);
		Card fileCard = (Card) execAction(createCardAction);
		StringAttribute name = (StringAttribute) fileCard
				.getAttributeById(Attribute.ID_NAME);
		name.setValue(RESULT_FILE_NAME);
		ObjectId fileCardId = saveObject(fileCard);
		fileCard.setId((Long) fileCardId.getId());
		return fileCard;
	}

	public String getAttachHash(Card card) {
		String result = null;
		try {
			IntegerAttribute attrLen = (IntegerAttribute) card
					.getAttributeById(Attribute.ID_FILE_SIZE);
			if (attrLen != null) {
				Integer attachLength = new Integer(attrLen.getValue());
				DownloadFile downloadAction = new DownloadFile();
				downloadAction.setCardId(card.getId());
				Material fileMaterial = (Material) execAction(downloadAction);
				InputStream fileData = fileMaterial.getData();
				byte[] streamBytes = new byte[attachLength];
				fileData.read(streamBytes);
				result = Base64.byteArrayToBase64(CryptoLayer.getInstance()
						.getByteArrayDigest(streamBytes));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	private Card loadCard(ObjectId cardId) throws DataException {
		final ObjectQueryBase cardQuery = getQueryFactory().getFetchQuery(
				Card.class);
		cardQuery.setAccessChecker(null);
		cardQuery.setId(cardId);
		return (Card) getDatabase().executeQuery(getUser(), cardQuery);
	}

	private Object execAction(Action action) throws DataException {
		final ActionQueryBase query = getQueryFactory().getActionQuery(action);
		query.setAccessChecker(null);
		query.setAction(action);
		return getDatabase().executeQuery(getUser(), query);
	}

	public ObjectId saveObject(DataObject obj) throws DataException {
		if (obj == null)
			throw new IllegalArgumentException("Object can't be null");
		SaveQueryBase saveQuery = getQueryFactory().getSaveQuery(obj);
		saveQuery.setObject(obj);
		return (ObjectId) getDatabase().executeQuery(getUser(), saveQuery);
	}

	private boolean uploadPart(ByteArrayInputStream bais, UploadFile upload) {
		try {
			final byte[] buf = new byte[BUFSIZE_UPLOADPART];
			while (true) {
				int len = bais.read(buf);
				if (len == -1)
					break;
				write(buf, 0, len, upload);
			}
			flush(upload);

			return true;

		} catch (IOException e) {
			return false;
			/*
			 * if (e instanceof DatabaseIOException) throw
			 * ((DatabaseIOException) e).getDataException(); throw new
			 * DataException(e);
			 */
		}
	}

	public void write(byte[] b, int off, int len, UploadFile upload)
			throws IOException {
		while (localPos + len >= PART_SIZE) {
			final int wrLen = PART_SIZE - localPos;
			System.arraycopy(b, off, data, localPos, wrLen);
			off += wrLen;
			len -= wrLen;
			localPos = PART_SIZE;
			flush(upload);
		}
		System.arraycopy(b, off, data, localPos, len);
		localPos += len;
	}

	public void flush(UploadFile upload) throws IOException {
		final UploadFilePart part = new UploadFilePart();

		part.setCardId(upload.getCardId());
		part.setData(data, localPos);
		part.setOffset(globalPos);
		part.setUrl(upload.getUrl());
		try {
			execAction(part);

			// ��������� ���������������� �����
			if (upload.getUrl() == null)
				upload.setUrl(part.getUrl());

		} catch (DataException e) {
			throw new DatabaseIOException(e);
		}
		globalPos += localPos;
		localPos = 0;
	}

}
