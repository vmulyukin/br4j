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
package com.aplana.dbmi.card.download.descriptor;

import com.aplana.dbmi.card.util.AbstractXmlDescriptorReader;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.ServiceException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * class that read descriptions of the download and upload file actions
 */
public class FileActionDescriptorReader extends AbstractXmlDescriptorReader {
    public final static String ATTRIBUTE_ACTION = "action";
    public final static String ATTRIBUTE_ACTIONHANDLER = "actionhandler";
    public final static String ATTRIBUTE_PARAM_NAME = "name";
    public final static String ATTRIBUTE_PARAM_VALUE = "value";

    private XPathExpression variantsRootExpr;
    private XPathExpression variantsExpr;

    public FileActionDescriptorReader() throws XPathExpressionException {
        this.variantsRootExpr = xpath.compile("./variants");
        this.variantsExpr = xpath.compile("./variant");
    }

    public FileActionDescriptor read(InputStream stream)
            throws SAXException, IOException, ParserConfigurationException, XPathExpressionException, ParseException, DataException, ServiceException {
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(stream);
        final FileActionDescriptor result = getNewActionDescriptor();

        result.setVariants(new ArrayList<FileActionVariantDescriptor>());
        Element root = doc.getDocumentElement();
        Element variantsRoot = (Element)variantsRootExpr.evaluate(root, XPathConstants.NODE);
        NodeList variantNodes = (NodeList)variantsExpr.evaluate(variantsRoot, XPathConstants.NODESET);
        for (int i = 0; i < variantNodes.getLength(); ++i) {
            Element variantElem = (Element)variantNodes.item(i);
            FileActionVariantDescriptor d;
            d = readVariantDescriptorFromNode(variantElem);
            result.getVariants().add(d);
        }

        return result;
    }

    private FileActionVariantDescriptor readVariantDescriptorFromNode(
            Element variantElem)
            throws DataException, ServiceException, XPathExpressionException, ParseException {
        FileActionVariantDescriptor d = getNewActionVariantDescriptor();
        String p = variantElem.getAttribute(ATTRIBUTE_ACTION);
        if (p == null) {
            logger.error("Expected " + ATTRIBUTE_ACTION + " attribute.");
            throw new DataException("The required attribute " + ATTRIBUTE_ACTION + " is absent.");
        }
        d.setActionName(p);

        p = variantElem.getAttribute(ATTRIBUTE_ACTIONHANDLER);
        if (p == null) {
            logger.error("Expected " + ATTRIBUTE_ACTIONHANDLER + " attribute.");
            throw new DataException("The required attribute " + ATTRIBUTE_ACTIONHANDLER + " is absent.");
        }
        d.setActionClass(p);

        final NodeList params = (NodeList)xpath.evaluate("./parameters/parameter", variantElem, XPathConstants.NODESET);
        final Map<String, String> paramMap = new HashMap<String, String>();
        if (params != null && params.getLength() > 0) {
            for (int j = 0; j < params.getLength(); ++j) {
                Element paramElem = (Element)params.item(j);
                paramMap.put(paramElem.getAttribute(ATTRIBUTE_PARAM_NAME), paramElem.getAttribute(ATTRIBUTE_PARAM_VALUE));
            }
        }
        d.setParameters(paramMap);

        return d;
    }

    protected FileActionDescriptor getNewActionDescriptor() {
        return new FileActionDescriptor();
    }

    protected FileActionVariantDescriptor getNewActionVariantDescriptor() {
        return new FileActionVariantDescriptor();
    }

}
