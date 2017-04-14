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
import ru.datateh.jbr.iuh.groovy.AbstractExecute
import ru.datateh.jbr.iuh.groovy.utils.FileUtils
import ru.datateh.jbr.iuh.groovy.utils.PropertiesUtils


/**
 * @author etarakanov
 * Date: 10.02.15
 * Time: 18:40
 */
@Log4j
public class ExampleXML extends AbstractExecute
{
    public static String IUH_SERVER_GUID = "br4j.dbmi.replication.ServerGUID";
    public static String IUH_INCOMING_FOLDER = "br4j.dbmi.replication.IncomingFolder";
    public static String IUH_OUTGOING_FOLDER = "br4j.dbmi.replication.OutgoingFolder";
    public static String IUH_REPLICATION_MEMBER_GUID = "br4j.dbmi.replication.GUID";

    public static String IUH_SERVER_GUID_DEFVAL = "164a79d7-2aaf-42e7-a963-58a1fa636d10";
    public static String IUH_INCOMING_FOLDER_DEFVAL = "data/replication/in";
    public static String IUH_OUTGOING_FOLDER_DEFVAL = "data/replication/out";
    public static String IUH_REPLICATION_MEMBER_GUID_DEFVAL = "59c8a1df-7154-443e-a08b-7862403dc5fa";

    public static String SERVER_GUID = "ServerGUID";
    public static String INCOMING_FOLDER = "IncomingFolder";
    public static String OUTGOING_FOLDER = "OutgoingFolder";
    public static String GUID = "GUID";

    Map<String, String> properties;

    List<String> guids;


    @Override
    void install() {
        log.info "ExampleXML is running... "

        File fileProperties = new File(map.get('br4j.jboss.configuration.path')
                + File.separator + 'conf' + File.separator + 'dbmi' + File.separator + 'replication' + File.separator + 'ReplicationNodeConfig.xml');

        File exampleProperties = new File(map.get('br4j.jboss.configuration.path')
                + File.separator + 'conf' + File.separator + 'dbmi' + File.separator + 'replication' + File.separator + 'ReplicationNodeConfig.xml.example');

        File exampleProperties2 = new File(map.get('br4j.jboss.configuration.path')
                + File.separator + 'conf' + File.separator + 'dbmi' + File.separator + 'replication' + File.separator + 'ReplicationTemplateConfig.xml.example');

        File fileProperties2 = new File(map.get('br4j.jboss.configuration.path')
                + File.separator + 'conf' + File.separator + 'dbmi' + File.separator + 'replication' + File.separator + 'ReplicationTemplateConfig.xml');

        log.info "File: " + fileProperties2 + " will be created from copy: " + exampleProperties2;
        //копирует файл
        FileUtils.copyFile(exampleProperties2, fileProperties2);

        if (!FileUtils.checkFileExist(fileProperties, false) && FileUtils.checkFileExist(exampleProperties, true))
        {
            log.info "Property file: " + fileProperties + " missing. File will be created from copy: " + exampleProperties;
            FileUtils.copyFile(exampleProperties, fileProperties);
            //проверяет корректность свойств в файле
            if (!checkPropertiesValues(fileProperties)) {
                //обновляет значения свойств
                updateRequiredProperty(fileProperties);
            }
        }
    }

    public static void main(String[] args) {
        new ExampleXML().start()
    }

    /**
     * Проверка соответсвтвия значений свойств в файле, значениям в карте свойтсв
     * @param propertiesFile файл свойств
     * @return true, если значения свойств в файле идентичны значениям в карте свойств
     */
    private boolean checkPropertiesValues (File propertiesFile)
    {
        if (collectProperties().get(IUH_SERVER_GUID).equals(PropertiesUtils.readTagValueFromXmlFile(propertiesFile, SERVER_GUID)) &&
                collectProperties().get(IUH_INCOMING_FOLDER).equals(PropertiesUtils.readTagValueFromXmlFile(propertiesFile, INCOMING_FOLDER)) &&
                collectProperties().get(IUH_OUTGOING_FOLDER).equals(PropertiesUtils.readTagValueFromXmlFile(propertiesFile, OUTGOING_FOLDER)) &&
                !collectGUIDS().retainAll(readGUIDfromFile(propertiesFile)))
        {
            return true;
        }
        return false;
    }

    /**
     * инициализация карты свойств
     * @return карта свойств
     */
    private Map<String, String> collectProperties ()
    {
        if (properties == null)
        {
            properties = new HashMap<String, String>();
            //заполнение карты свойств
            properties.put(IUH_SERVER_GUID, getPropertyValue(IUH_SERVER_GUID, IUH_SERVER_GUID_DEFVAL));
            properties.put(IUH_INCOMING_FOLDER,
                    getPropertyValue(IUH_INCOMING_FOLDER, map.get('br4j.jboss.configuration.path')
                            + File.separator + IUH_INCOMING_FOLDER_DEFVAL));
            properties.put(IUH_OUTGOING_FOLDER,
                    getPropertyValue(IUH_OUTGOING_FOLDER, map.get('br4j.jboss.configuration.path')
                            + File.separator + IUH_OUTGOING_FOLDER_DEFVAL));
        }
        return properties
    }

    /**
     * Возвращает список значений для тега GUID, если получить не удалось возвращает список значений по умолчанию
     * @return список значений для тега GUID
     */
    private List<String> collectGUIDS ()
    {
        if (guids == null) {
            //получение количества тегов
            int countGuids = Integer.parseInt(getPropertyValue(IUH_REPLICATION_MEMBER_GUID + " count", "1"));
            guids = new ArrayList<String>();
            while (countGuids > 0)
            {
                //получение значений тегов и добавление их в список
                guids.add(getPropertyValue(IUH_REPLICATION_MEMBER_GUID + " number " + countGuids, IUH_REPLICATION_MEMBER_GUID_DEFVAL))
                countGuids--;
            }
        }
        return guids;
    }

    /**
     * Читает список тегов GUID из xml-файла
     * @param propertiesFile xml-файл
     * @return список значений для тега GUID
     */
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

    /**
     * Перезапись тегов GUID дочернем для тега ReplicationMember в xml-файле
     * @param propertiesFile xml-файле
     * @param guids список значений тегов GUID
     */
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

    /**
     * обновление свойств в файле
     * @param propertiesFile файл свойств
     */
    private void updateRequiredProperty(File propertiesFile)
    {
        log.info("Updating file: " + propertiesFile);
        log.info("Updating property: " + SERVER_GUID);
        //обновление значений тегов в xml-файле
        PropertiesUtils.updateXmlTagValueAndSave(propertiesFile, SERVER_GUID, collectProperties().get(IUH_SERVER_GUID));
        log.info("Updating property: " + INCOMING_FOLDER);
        PropertiesUtils.updateXmlTagValueAndSave(propertiesFile, INCOMING_FOLDER, collectProperties().get(IUH_INCOMING_FOLDER));
        log.info("Updating property: " + OUTGOING_FOLDER);
        PropertiesUtils.updateXmlTagValueAndSave(propertiesFile, OUTGOING_FOLDER, collectProperties().get(IUH_OUTGOING_FOLDER));
        log.info("Updating property: " + GUID);
        updateGUIDtoFile(propertiesFile, collectGUIDS());
        log.info("Updating file: " + propertiesFile + " finished");
    }
}
