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
import groovy.util.logging.Log4j
import org.w3c.dom.Document
import org.w3c.dom.Element
import ru.datateh.jbr.iuh.AbstractExecute
import ru.datateh.jbr.iuh.parameters.CommonParameters
import ru.datateh.jbr.iuh.parameters.OtherParameters
import ru.datateh.jbr.iuh.utils.FileUtils
import ru.datateh.jbr.iuh.utils.PropertiesUtils

/**
 * @author etarakanov
 * Date: 10.02.15
 * Time: 18:40
 */
@Log4j
public class Replication extends AbstractExecute
{
    private static String SERVER_GUID = "ServerGUID";
    private static String INCOMING_FOLDER = "IncomingFolder";
    private static String OUTGOING_FOLDER = "OutgoingFolder";
    private static String GUID = "GUID";

    private Map<String, String> properties;

    private List<String> guids;


    @Override
    void install() {
        log.info "Replication is running... "

        File fileProperties = new File(getParam(CommonParameters.System.JBOSS_CONFIGURATION_PATH)
                + File.separator + 'conf' + File.separator + 'dbmi' + File.separator + 'replication' + File.separator + 'ReplicationNodeConfig.xml');

        File exampleProperties = new File(getParam(CommonParameters.System.JBOSS_CONFIGURATION_PATH)
                + File.separator + 'conf' + File.separator + 'dbmi' + File.separator + 'replication' + File.separator + 'ReplicationNodeConfig.xml.example');

        File exampleProperties2 = new File(getParam(CommonParameters.System.JBOSS_CONFIGURATION_PATH)
                + File.separator + 'conf' + File.separator + 'dbmi' + File.separator + 'replication' + File.separator + 'ReplicationTemplateConfig.xml.example');

        File fileProperties2 = new File(getParam(CommonParameters.System.JBOSS_CONFIGURATION_PATH)
                + File.separator + 'conf' + File.separator + 'dbmi' + File.separator + 'replication' + File.separator + 'ReplicationTemplateConfig.xml');

        log.info "File: " + fileProperties2 + " will be created from copy: " + exampleProperties2;
        FileUtils.copyFile(exampleProperties2, fileProperties2);

        if (!FileUtils.checkFileExist(fileProperties, false) && FileUtils.checkFileExist(exampleProperties, true))
        {
            log.info "Property file: " + fileProperties + " missing. File will be created from copy: " + exampleProperties;
            FileUtils.copyFile(exampleProperties, fileProperties);
            if (!checkPropertiesValues(fileProperties)) {
                updateRequiredProperty(fileProperties);
            }
            FileUtils.changeFilePermission(fileProperties, FileUtils.Permission.WRITE, false);
        }
    }

    public static void main(String[] args) {
        new Replication().start()
    }

    private boolean checkPropertiesValues (File propertiesFile)
    {
        if (collectProperties().get(OtherParameters.Replication.SERVER_GUID).equals(PropertiesUtils.readTagValueFromXmlFile(propertiesFile, SERVER_GUID)) &&
                collectProperties().get(OtherParameters.Replication.INCOMING_FOLDER).equals(PropertiesUtils.readTagValueFromXmlFile(propertiesFile, INCOMING_FOLDER)) &&
                collectProperties().get(OtherParameters.Replication.OUTGOING_FOLDER).equals(PropertiesUtils.readTagValueFromXmlFile(propertiesFile, OUTGOING_FOLDER)) &&
                !collectGUIDS().retainAll(readGUIDfromFile(propertiesFile)))
        {
            return true;
        }
        return false;
    }

    private Map<String, String> collectProperties ()
    {
        if (properties == null)
        {
            properties = new HashMap<String, String>();
            properties.put(OtherParameters.Replication.SERVER_GUID, getPropertyValue(OtherParameters.Replication.SERVER_GUID, null));
            properties.put(OtherParameters.Replication.INCOMING_FOLDER,
                    getPropertyValue(OtherParameters.Replication.INCOMING_FOLDER, null));
            properties.put(OtherParameters.Replication.OUTGOING_FOLDER,
                    getPropertyValue(OtherParameters.Replication.OUTGOING_FOLDER, null));
        }
        return properties
    }

    private List<String> collectGUIDS ()
    {
        if (guids == null) {
            int countGuids = Integer.parseInt(getPropertyValue(OtherParameters.Replication.REPLICATION_MEMBER_GUID + ".count", null));
            guids = new ArrayList<String>();
            while (countGuids > 0)
            {
                guids.add(getPropertyValue(OtherParameters.Replication.REPLICATION_MEMBER_GUID + ".number." + countGuids, null))
                countGuids--;
            }
        }
        return guids;
    }

    private static List<String> readGUIDfromFile (File propertiesFile)
    {
        Document document = PropertiesUtils.readXmlDocumentFromFile(propertiesFile);
        org.w3c.dom.NodeList nodeList = document.getElementsByTagName(GUID);
        List<String> result = new ArrayList<String>();
        Iterator<org.w3c.dom.Node> nodeIterator = nodeList.iterator();
        while (nodeIterator.hasNext())
        {
            result.add(nodeIterator.next().getTextContent());
        }
        return result;
    }

    private static void updateGUIDtoFile (File propertiesFile, List<String> guids)
    {
        Document document = PropertiesUtils.readXmlDocumentFromFile(propertiesFile);
        org.w3c.dom.Node replicationMember = document.getElementsByTagName("ReplicationMember").item(0);
        while (replicationMember.hasChildNodes())
            replicationMember.removeChild(replicationMember.getFirstChild());
        for(String giud : guids)
        {
            Element node = document.createElement(GUID);
            node.setTextContent(giud);
            replicationMember.appendChild(node);
        }
        PropertiesUtils.storeXmlDocumentToFile(propertiesFile,document);
    }

    private void updateRequiredProperty(File propertiesFile)
    {
        log.info("Updating file: " + propertiesFile);
        log.info("Updating property: " + SERVER_GUID);
        PropertiesUtils.updateXmlTagValueAndSave(propertiesFile, SERVER_GUID, collectProperties().get(OtherParameters.Replication.SERVER_GUID));
        log.info("Updating property: " + INCOMING_FOLDER);
        PropertiesUtils.updateXmlTagValueAndSave(propertiesFile, INCOMING_FOLDER, getTransformedPath(collectProperties().get(OtherParameters.Replication.INCOMING_FOLDER)));
        log.info("Updating property: " + OUTGOING_FOLDER);
        PropertiesUtils.updateXmlTagValueAndSave(propertiesFile, OUTGOING_FOLDER, getTransformedPath(collectProperties().get(OtherParameters.Replication.OUTGOING_FOLDER)));
        log.info("Updating property: " + GUID);
        updateGUIDtoFile(propertiesFile, collectGUIDS());
        log.info("Updating file: " + propertiesFile + " finished");
    }

    private static String getTransformedPath (String path)
    {
        File checkedPath = new File(path);
        if (checkedPath.isAbsolute()){
            return path;
        } else {
            return getParam(CommonParameters.System.JBOSS_CONFIGURATION_PATH) + File.separator + path;
        }
    }
}
