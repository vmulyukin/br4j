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
package ru.datateh.jbr.iuh.utils

import org.w3c.dom.Document

import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.Transformer
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

class PropertiesUtils {
	
	static Scanner scan = new Scanner(System.in)
	
	static void putValue(Map<String, String> map, String name) {
		
		System.out.print('Enter value of param ' + name + ': ')
		boolean scanText = scan.hasNext()
		System.out.print(scanText)
		Properties props = new Properties()
		File f = new File(map.get('iuh.answers.file'))
		FileInputStream fin = null
		FileOutputStream out = null
		try {
			fin = new FileInputStream(f)
			props.load(fin)
			props.setProperty(name, new Boolean(scanText).toString())
			out = new FileOutputStream(f)
			props.store(out, "---No Comment---")
		} catch(IOException e) {
			throw new Exception(e.getMessage(), e)
		} finally {
			try {
				if(fin != null) {
					fin.close();
				}
				if(out != null) {
					out.close();
				}
			 } catch(IOException e) {}
		}
	}

    public static void commentPropertiesAndSave(File propertyFile, List<String> propertyNames)
    {
        List<String> fileLines = FileUtils.readLines(propertyFile);
        for (String propertyName : propertyNames)
        {
            commentProperty(fileLines, propertyName, null);
        }
        FileUtils.storeLines(propertyFile, fileLines);
    }

    public static void commentPropertyAndSave(File propertyFile, String propertyName, String comment)
    {
        FileUtils.storeLines(propertyFile, commentProperty(FileUtils.readLines(propertyFile), propertyName, comment));
    }

    public static List<String> commentProperty(List<String> fileLines, String propertyName, String comment)
    {
        if (fileLines != null) {
            int propertyLineIndex = getIndexContainsProperty(fileLines, propertyName)
            if (propertyLineIndex != -1)
            {
                String propertyPair = fileLines.get(propertyLineIndex);
                if (propertyPair != null && !propertyPair.isEmpty()) {
                    fileLines.set(propertyLineIndex, "#" + propertyPair);
                }
                if ((propertyPair != null && !propertyPair.isEmpty()) && (comment != null && !comment.isEmpty())) {
                    fileLines.add(propertyLineIndex, "#" + comment);
                }
            }
        }
        return fileLines;
    }

    public static void unCommentPropertiesAndSave(File propertyFile, List<String> propertyNames)
    {
        List<String> fileLines = FileUtils.readLines(propertyFile);
        for (String propertyName : propertyNames)
        {
            unCommentProperty(fileLines, propertyName, null);
        }
        FileUtils.storeLines(propertyFile, fileLines);
    }

    public static void unCommentPropertyAndSave(File propertyFile, String propertyName, String comment)
    {
        FileUtils.storeLines(propertyFile, unCommentProperty(FileUtils.readLines(propertyFile), propertyName, comment));
    }

    public static List<String> unCommentProperty(List<String> fileLines, String propertyName, String comment)
    {
        if (fileLines != null) {
            int propertyLineIndex = getIndexContainsProperty(fileLines, propertyName)
            if (propertyLineIndex != -1)
            {
                String propertyPair = fileLines.get(propertyLineIndex);
                if (propertyPair != null && !propertyPair.isEmpty()) {
                    fileLines.set(propertyLineIndex, propertyPair.substring(1));
                }
                if ((propertyPair != null && !propertyPair.isEmpty()) && (comment != null && !comment.isEmpty())) {
                    fileLines.add(propertyLineIndex, "#" + comment);
                }
            }
        }
        return fileLines;
    }

    public static void addPropertiesAndSave(File propertyFile, Map<String, String> properties)
    {
        List<String> fileLines = FileUtils.readLines(propertyFile);
        Set<String> keys = properties.keySet();
        for (String key : keys)
        {
            addProperty(fileLines, key, properties.get(key), null);
        }
        FileUtils.storeLines(propertyFile, fileLines);
    }

    public static void addPropertyAndSave (File propertyFile, String propertyName, String propertyValue, String comment)
    {
        FileUtils.storeLines(propertyFile, addProperty(FileUtils.readLines(propertyFile), propertyName, propertyValue, comment));
    }

    public static List<String> addProperty (List<String> fileLines, String propertyName, String propertyValue, String comment)
    {
        if (fileLines != null) {
            String addedComment;
            if (comment != null && !comment.isEmpty()) {
                addedComment = "#" + comment;
                fileLines.add(addedComment);
            }
            String propertyPair;
            if ((propertyName != null && !propertyName.isEmpty()) && (propertyValue != null && !propertyValue.isEmpty())) {
                propertyPair = propertyName + "=" + propertyValue;
                fileLines.add(propertyPair);
            }
            else if (addedComment != null)
            {
                fileLines.remove(addedComment);
            }
        }
        return fileLines;
    }

    public static void updatePropertiesSave (File propertyFile, Map<String, String> properties)
    {
        List<String> fileLines = FileUtils.readLines(propertyFile);
        Set<String> keys = properties.keySet();
        for (String key : keys)
        {
            updateProperty(fileLines, key, properties.get(key), null);
        }
        FileUtils.storeLines(propertyFile, fileLines);
    }

    public static void updatePropertyAndSave (File propertyFile, String propertyName, String propertyNewValue, String comment)
    {
        FileUtils.storeLines(propertyFile, updateProperty(FileUtils.readLines(propertyFile), propertyName, propertyNewValue, comment));
    }

    /**
     * Изменяет значение указанного свойства и добавляет комментарий перед ним
     * @param fileLines список строк содеражащих пары "ключ=значение"
     * @param propertyName имя свойства
     * @param propertyNewValue новое значение свойства
     * @param comment добавляемый комментарий, если null комментарий не добавляется
     * @return обноволенный список строк содеражащих пары "ключ=значение"
     */
    public static List<String> updateProperty (List<String> fileLines, String propertyName, String propertyNewValue, String comment)
    {
        if (fileLines != null) {
            int propertyLineIndex = getIndexContainsProperty(fileLines, propertyName)
            if (propertyLineIndex != -1)
            {
                fileLines.set(propertyLineIndex, propertyName + "=" + propertyNewValue);
                if (comment != null && !comment.isEmpty()) {
                    fileLines.add(propertyLineIndex, "#" + comment);
                }
            }
        }
        return fileLines;
    }

    public static void deletePropertiesAndSave (File propertyFile, List<String> propertyNames)
    {
        List<String> fileLines = FileUtils.readLines(propertyFile);
        for (String propertyName : propertyNames)
        {
            deleteProperty(fileLines, propertyName, null)
        }
        FileUtils.storeLines(propertyFile, fileLines);
    }

    public static void deletePropertyAndSave (File propertyFile, String propertyName, String deletedComment)
    {
        FileUtils.storeLines(propertyFile, deleteProperty(FileUtils.readLines(propertyFile), propertyName, deletedComment));
    }

    public static List<String> deleteProperty (List<String> fileLines, String propertyName, String deletedComment)
    {
        int propertyLineIndex = getIndexContainsProperty(fileLines, propertyName)
        if (propertyLineIndex != -1)
        {
            fileLines.remove(propertyLineIndex);
        }
        if (deletedComment != null && !deletedComment.isEmpty()) {
            fileLines.remove("#" + deletedComment);
        }
        return fileLines;
    }

    public static Map<String, String> getMissingProperties (File sourceFile, File destinationFile)
    {
        Map<String, String> result = new HashMap<String, String>();
        FileUtils.checkFileExist(sourceFile, true);
        FileUtils.checkFileExist(destinationFile, true);
        Properties sourceProps = readPropertiesFromFile(sourceFile);
        Properties destinationProps = readPropertiesFromFile(destinationFile);
        Set<Object> keys = sourceProps.keySet();
        for (Object key : keys)
        {
            String strKey = (Object)key;
            if (destinationProps.getProperty(strKey) == null)
            {
                result.put(strKey, sourceProps.getProperty(strKey));
            }
        }
        return result;
    }

    public static Map<String, String> getExtraProperty (File sourceFile, File destinationFile){
        return getMissingProperties(destinationFile, sourceFile);}

    public static List<String> checkPropertyEquals (File sourceFile, File destinationFile)
    {
        List<String> result = new ArrayList<String>();
        FileUtils.checkFileExist(sourceFile, true);
        FileUtils.checkFileExist(destinationFile, true);
        Properties sourceProps = readPropertiesFromFile(sourceFile);
        Properties destinationProps = readPropertiesFromFile(destinationFile);
        Set<Object> keys = sourceProps.keySet();
        for (Object key : keys)
        {
            String strKey = (Object)key;
            String destinationPropVal = destinationProps.getProperty(strKey)
            if (destinationPropVal != null && !destinationPropVal.equalsIgnoreCase(sourceProps.getProperty(strKey)))
            {
                result.add(strKey);
            }
        }
        return result;
    }

    /**
     * Проверяет равенство значений свойств в файле и в указанной карте
     * @param sourceFile файл свойств
     * @param properties карта свойств
     * @return список имен свойств значения которых не равны значениям в карте свойств
     */
    public static List<String> checkPropertyEquals (File sourceFile, Map<String, String> properties)
    {
        List<String> result = new ArrayList<String>();
        FileUtils.checkFileExist(sourceFile, true);
        Properties sourceProps = readPropertiesFromFile(sourceFile);
        Set<String> keys = properties.keySet();
        for (String key : keys)
        {
            String destinationPropVal = properties.get(key)
            if (destinationPropVal != null && !destinationPropVal.equalsIgnoreCase(sourceProps.getProperty(key)))
            {
                result.add(key);
            }
        }
        return result;
    }

    public static boolean checkEquivalentPropertyFiles (File sourceFile, File destinationFile)
    {
        Map<String, String> missingProperties = getMissingProperties(sourceFile, destinationFile);
        Map<String, String> extraProperties = getExtraProperty(sourceFile, destinationFile);
        List<String> notEqualsProperties = checkPropertyEquals(sourceFile, destinationFile);
        if (missingProperties.isEmpty() && extraProperties.isEmpty() && notEqualsProperties.isEmpty())
        {
            return true;
        }
        return false;
    }


    public static int getIndexContainsProperty (List<String> fileLines, String propertyName)
    {
        if (propertyName != null && !propertyName.isEmpty()) {
            String propertyPattern = propertyName + "=";
            for (String line : fileLines)
            {
                if (line.contains(propertyPattern))
                {
                    return fileLines.indexOf(line);
                }
            }
        }
        return -1;
    }

    public static Properties readPropertiesFromFile (File propertiesFile)
    {
        FileUtils.checkFileExist(propertiesFile, true);
        FileInputStream fileInputStream = new FileInputStream(propertiesFile);
        Reader reader = new InputStreamReader(fileInputStream, "UTF-8");
        Properties props = new Properties();
        props.load(reader);
        fileInputStream.close();
        return props;
    }

    public static storeProperties (File propertiesFile, Properties properties)
    {
        FileOutputStream fileOut = new FileOutputStream(propertiesFile);
        Writer writer = new OutputStreamWriter(fileOut, "UTF-8")
        properties.store(writer, "");
        fileOut.close();
    }

    public static String readPropertyFromFile (File propertiesFile, String propertyName){

        return readPropertiesFromFile(propertiesFile).getProperty(propertyName);
    }

    /**
     * Читает карту свойств ключ->значение из указанного файла
     * @param propertiesFile файл свойств
     * @param propertiesName список свойств, которые нужно прочитать, если null, возвращает все свойства из файла
     * @return карту свойств ключ->значение
     */
    public static Map<String, String> readPropertiesMapFromFile(File propertiesFile, List<String> propertiesName)
    {
        Map<String, String> result = new HashMap<String, String>();
        Properties properties = readPropertiesFromFile(propertiesFile);

        if (propertiesName != null) {
            for (String key : propertiesName)
            {
                result.put(key, properties.getProperty(key));
            }
        } else {
            return convertPropertiesToMap(readPropertiesFromFile(propertiesFile));
        }
        return result;
    }

    /**
     * Читает значение тега в xml-файле
     * @param propertiesFile xml файл
     * @param propertyName имя тега
     * @return значение тега
     */
    public static String readTagValueFromXmlFile(File propertiesFile, String propertyName)
    {
        return readXmlDocumentFromFile(propertiesFile).getElementsByTagName(propertyName).item(0).getTextContent();
    }

    public static String readAttributeValueFromXmlFile(File propertiesFile, String tagName, String attributeName)
    {
        return readXmlDocumentFromFile(propertiesFile).getElementsByTagName(tagName).item(0).getAttributes().getNamedItem(attributeName).getTextContent();
    }

    /**
     * Обновляет значене указанного тега в xml-файле
     * @param propertiesFile xml-файл
     * @param tagName имя тега
     * @param tagValue значение тега
     */
    public static void updateXmlTagValueAndSave(File propertiesFile, String tagName, String tagValue)
    {
        Document document = readXmlDocumentFromFile(propertiesFile);
        document.getElementsByTagName(tagName).item(0).setTextContent(tagValue);
        storeXmlDocumentToFile(propertiesFile, document);
    }

    public static void updateXmlAttributeValueAndSave(File propertiesFile, String tagName, String attributeName, attributeValue)
    {
        Document document = readXmlDocumentFromFile(propertiesFile);
        document.getElementsByTagName(tagName).item(0).getAttributes().getNamedItem(attributeName).setTextContent(attributeValue);
        storeXmlDocumentToFile(propertiesFile, document);
    }

    /**
     * Читает xml-документ из xml-файла
     * @param propertiesFile xml-файл
     * @return xml-документ {@link org.w3c.dom.Document}
     */
    public static Document readXmlDocumentFromFile (File propertiesFile)
    {
        FileUtils.checkFileExist(propertiesFile, true);
        DocumentBuilderFactory factoryNew = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factoryNew.newDocumentBuilder();
        return builder.parse(propertiesFile);
    }

    /**
     * Сохраняет xml-документ в xml-файл
     * @param propertiesFile xml-файл
     * @param document xml-документ {@link org.w3c.dom.Document}
     */
    public static void storeXmlDocumentToFile (File propertiesFile, Document document)
    {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(document);
        StreamResult result = new StreamResult(propertiesFile);
        transformer.transform(source, result);
    }

    /**
     * Конвертирует набор свойст в карту ключ -> значение
     * @param properties набор свойств
     * @return карту ключ -> значение
     */
    public static Map<String, String> convertPropertiesToMap (Properties properties)
    {
        Map<String, String> result = new HashMap<String, String>();
        for (String key : properties.stringPropertyNames()) {
            result.put(key, properties.getProperty(key));
        }
        return result;
    }
}
