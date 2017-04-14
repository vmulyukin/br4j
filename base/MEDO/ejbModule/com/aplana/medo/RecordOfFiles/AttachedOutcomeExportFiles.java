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
/**
 *
 */
package com.aplana.medo.RecordOfFiles;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.aplana.dbmi.action.DownloadFile;
import com.aplana.dbmi.action.Material;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.HtmlAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.ServiceException;
import com.aplana.medo.ServicesProvider;
import com.aplana.medo.cards.CardException;
import com.aplana.medo.cards.CardXMLBuilder;
import com.aplana.medo.cards.OutcomeExportCardHandler;

/**
 * @author PPanichev
 *
 */
public class AttachedOutcomeExportFiles {

    public static final ObjectId EDS = ObjectId.predefined(HtmlAttribute.class,
            "jbr.uzdo.signature");

    protected Log logger = LogFactory.getLog(getClass());

    private DataServiceBean serviceBean = null;
    private String outDir = null;
    private String exportingFileName = null;
    private Map<ObjectId, Material> downloadedMaterials;

    private ObjectId[] linkedFileCards;

    public AttachedOutcomeExportFiles(String outDir, String exportingFileName)
            throws CardException {
        this.serviceBean = getServiceBean();
        this.outDir = outDir;
        this.exportingFileName = exportingFileName;
    }

    public AttachedOutcomeExportFiles(String exportingFileName)
            throws CardException {
        this(null, exportingFileName);
    }

    public List<String> putFiles() throws IOException, DataException, ServiceException {
        if (outDir == null)
            throw new IllegalStateException("Out dir is not defined");
        List<String> files = new ArrayList<String>();
        files.add(exportingFileName);

        for (Material material : getDownloadedMaterials().values()) {
            FileOutputStream outStream = null;
            try {
                File od = new File(outDir);
                if (!od.exists()) {
                    od.mkdirs();
                }
                File docLink = new File(outDir, material.getName());
                outStream = new FileOutputStream(docLink);
                InputStream inStream = material.getData();

                byte[] buffer = new byte[4 * 1024];
                int read;
                while ((read = inStream.read(buffer)) != -1) {
                    outStream.write(buffer, 0, read);
                }
                outStream.flush();
                files.add(docLink.getName());
            } finally {
                if (outStream != null) {
                    outStream.close();
                }
            }
        }
        return files;
    }

	public void updateDoclinks(Document xml) throws XPathExpressionException,
			DataException, ServiceException {
		XPath xpath = CardXMLBuilder.newXPath();
		String docLinksAttribute = String.format(
				"//%1$s:%2$s[@%3$s='%4$s']/%1$s:%5$s",
				CardXMLBuilder.CardNamespaceContext.DEFAULT_PREFIX,
				CardXMLBuilder.ATTRIBUTE_TAG, CardXMLBuilder.CODE_ATTRIBUTE,
				OutcomeExportCardHandler.JBR_FILES.getId(),
				CardXMLBuilder.VALUE_TAG);
		NodeList docLinkAttributeElements = (NodeList) xpath.evaluate(docLinksAttribute, xml, XPathConstants.NODESET);

		//������ ����� �������� ������ ��������, ������ ����� �������� � ����� �� �������, ��� � � linkedFileCards
		Map<ObjectId, Material> materials = getDownloadedMaterials();
		//����� �������� xml Element ��� ������� ��������, ��� �� ����� �������� �� � ������ �������
		Map<ObjectId, Element> materialElements = new HashMap<ObjectId, Element>();

		Node mainMaterialNode = null;

		for (int i = 0; i < docLinkAttributeElements.getLength(); i++) {
			Element docLinkAttributeElement = (Element) docLinkAttributeElements.item(i);
			if (mainMaterialNode == null) {
				mainMaterialNode = docLinkAttributeElement.getParentNode();
			}
			String id = docLinkAttributeElement.getTextContent();
			ObjectId materialCardId = new ObjectId(Card.class, Long.parseLong(id));
			Material material = materials.get(materialCardId);
			if (material == null) {
				docLinkAttributeElement.getParentNode().removeChild(docLinkAttributeElement);
				continue;
			}
			String fileName = material.getName();
			docLinkAttributeElement.setAttribute(CardXMLBuilder.DESCRIPTION_ATTRIBUTE, fileName);

			//��������� ������� � ��� � ������� �� ��������, ��� �� ����� �������� � ������ �������
			materialElements.put(materialCardId, docLinkAttributeElement);
			docLinkAttributeElement.getParentNode().removeChild(docLinkAttributeElement);
		}

		//��������� ��������� � xml �������� <attribute code="DOCLINKS" ...> � ������ �������
		if (mainMaterialNode != null) {
			for (ObjectId meterialId : getLinkedFileCards()) {
				if (materialElements.get(meterialId) != null) {
					mainMaterialNode.appendChild(materialElements.get(meterialId));
				}
			}
		} else {
			logger.error("Unable to add materials for document due to null material node");
		}
	}

    public static String getEds(ObjectId cardId) throws DataException,
            ServiceException {
        String eds = null;
        Card card = (Card) ServicesProvider.serviceBeanInstance().getById(
                cardId);
        if (card != null) {
            eds = ((HtmlAttribute) card.getAttributeById(EDS)).getValue();
            return eds;
        }
        throw new CardException(
                "jbr.medo.AttachedOutcomeExportFiles.linkedFile.notFound");
    }

    public static void addEdsAttribute(String eds, Properties properties,
            Document xml, String cardId) throws CardException,
            XPathExpressionException {
        String filesTag = properties.getProperty("code.fromTag.file_0");
        if (filesTag == null) {
            throw new CardException(
                    "code.fromTag.file_0 property should be set");
        }
        XPath xpath = CardXMLBuilder.newXPath();
        String values = String.format("//%1$s:%2$s[@%3$s='%4$s']/%1$s:%5$s",
                CardXMLBuilder.CardNamespaceContext.DEFAULT_PREFIX,
                CardXMLBuilder.ATTRIBUTE_TAG, CardXMLBuilder.CODE_ATTRIBUTE,
                filesTag, CardXMLBuilder.VALUE_TAG);
        XPathExpression valueTags = xpath.compile(values);
        Element root = xml.getDocumentElement();
        NodeList valueTagNodes = (NodeList) valueTags.evaluate(root,
                XPathConstants.NODESET);
        for (int i = 0; i < valueTagNodes.getLength(); ++i) {
            Element valueTagElem = (Element) valueTagNodes.item(i);
            if (cardId.equals(valueTagElem.getTextContent()))
                CardXMLBuilder.createAttributeEdsInValue_Tag(xml, valueTagElem,
                        eds);
        }
    }

    /**
     * Returns {@link DataServiceBean} instance or throws {@link CardException}
     * if it is impossible.
     *
     * @return DataServiceBean
     * @throws CardException
     */
    private DataServiceBean getServiceBean() throws CardException {
        try {
            serviceBean = ServicesProvider.serviceBeanInstance();
        } catch (ServiceException ex) {
            throw new CardException();
        }

        if (serviceBean == null) {
            throw new CardException();
        }

        return serviceBean;
    }

    public String getOutDir() {
        return this.outDir;
    }

    public void setOutDir(String outDir) {
        this.outDir = outDir;
    }

    public ObjectId[] getLinkedFileCards() {
        return this.linkedFileCards;
    }

    public void setLinkedFileCards(ObjectId[] linkedFileCards) {
        this.linkedFileCards = linkedFileCards;
        downloadedMaterials = null;
    }

    protected Map<ObjectId, Material> getDownloadedMaterials()
            throws DataException, ServiceException {
        if (this.downloadedMaterials == null) {
            loadMaterials();
        }
        return this.downloadedMaterials;
    }

    private void loadMaterials() throws DataException, ServiceException {
        downloadedMaterials = new HashMap<ObjectId, Material>(
                linkedFileCards.length);
        if (linkedFileCards == null)
            return;
        for (ObjectId cardId : linkedFileCards) {
            DownloadFile downloadFile = new DownloadFile();
            downloadFile.setCardId(cardId);
            Material material = (Material) serviceBean.doAction(downloadFile);
            downloadedMaterials.put(cardId, material);
        }
    }

}
