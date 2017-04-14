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
 * Date: 12.02.15
 * Time: 12:24
 */
@Log4j
public class MaterialSync extends AbstractExecute
{
    private static String TAG_NAME = "folder";

    private static String ATTRIBUTE_IN = "in";
    private static String ATTRIBUTE_BAD = "bad";

    private List<FolderData> folders;

    @Override
    void install() {
        log.info "MaterialSync is running... "

        File fileProperties = new File(getParam(CommonParameters.System.JBOSS_CONFIGURATION_PATH)
                + File.separator + 'conf' + File.separator + 'dbmi' + File.separator + 'materialsync' + File.separator + 'config.xml');

        File exampleProperties = new File(getParam(CommonParameters.System.JBOSS_CONFIGURATION_PATH)
                + File.separator + 'conf' + File.separator + 'dbmi' + File.separator + 'materialsync' + File.separator + 'config.xml.example');

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
        new MaterialSync().start()
    }

    private boolean checkPropertiesValues (File propertiesFile)
    {
        if (!collectFoldersData().retainAll(readFoldersDataFromFile(propertiesFile)))
        {
            return true;
        }
        return false;
    }

    private List<FolderData> collectFoldersData ()
    {
        if (folders == null) {
            int countFolder = Integer.parseInt(getPropertyValue(OtherParameters.MaterialSync.MATERIAL_SYNC_FOLDER_COUNT, null));
            folders = new ArrayList<FolderData>();
            while (countFolder > 0)
            {
                String inFolder = getPropertyValue(OtherParameters.MaterialSync.MATERIAL_SYNC_IN + "." + countFolder, null);
                String badFolder = getPropertyValue(OtherParameters.MaterialSync.MATERIAL_SYNC_BAD + "." + countFolder, null);
                folders.add(new FolderData(inFolder, badFolder));
                countFolder--;
            }
        }
        return folders;
    }

    private List<FolderData> readFoldersDataFromFile (File propertiesFile)
    {
        Document document = PropertiesUtils.readXmlDocumentFromFile(propertiesFile);
        org.w3c.dom.NodeList nodeList = document.getElementsByTagName(TAG_NAME);
        List<FolderData> result = new ArrayList<FolderData>();
        Iterator<org.w3c.dom.Node> nodeIterator = nodeList.iterator();
        while (nodeIterator.hasNext())
        {
            org.w3c.dom.Node folderNode = nodeIterator.next();
            String inFolder = folderNode.getAttributes().getNamedItem(ATTRIBUTE_IN).getTextContent();
            String badFolder = folderNode.getAttributes().getNamedItem(ATTRIBUTE_BAD).getTextContent();
            result.add(new FolderData(inFolder, badFolder));
        }
        return result;
    }

    private static void updateFolderToFile (File propertiesFile, List<FolderData> foldersData)
    {
        Document document = PropertiesUtils.readXmlDocumentFromFile(propertiesFile);
        org.w3c.dom.Node materialsync = document.getElementsByTagName("materialsync").item(0);
        while (materialsync.hasChildNodes())
            materialsync.removeChild(materialsync.getFirstChild());
        for(FolderData folder : foldersData)
        {
            Element node = document.createElement(TAG_NAME);
            node.setAttribute(ATTRIBUTE_IN, getTransformedPath(folder.getInFolder()));
            node.setAttribute(ATTRIBUTE_BAD, getTransformedPath(folder.getBadFolder()));
            materialsync.appendChild(node);
        }
        PropertiesUtils.storeXmlDocumentToFile(propertiesFile,document);
    }

    private void updateRequiredProperty(File propertiesFile)
    {
        log.info("Updating file: " + propertiesFile);
        log.info("Updating folders data:");
        updateFolderToFile(propertiesFile, collectFoldersData());
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

    private class FolderData
    {
        boolean equals(o) {
            if (this.is(o)) return true
            if (getClass() != o.class) return false

            FolderData that = (FolderData) o

            if (badFolder != that.badFolder) return false
            if (inFolder != that.inFolder) return false

            return true
        }

        int hashCode() {
            int result
            result = inFolder.hashCode()
            result = 31 * result + badFolder.hashCode()
            return result
        }

        FolderData(String inFolder, String badFolder) {
            this.inFolder = inFolder
            this.badFolder = badFolder
        }
        private String inFolder;
        private String badFolder;

        String getInFolder() {
            return inFolder
        }

        void setInFolder(String inFolder) {
            this.inFolder = inFolder
        }

        String getBadFolder() {
            return badFolder
        }

        void setBadFolder(String badFolder) {
            this.badFolder = badFolder
        }
    }
}
