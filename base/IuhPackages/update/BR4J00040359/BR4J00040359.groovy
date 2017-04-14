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
import ru.datateh.jbr.iuh.AbstractExecute
import ru.datateh.jbr.iuh.utils.FileUtils
import ru.datateh.jbr.iuh.utils.PropertiesUtils

@Log4j
public class BR4J00040359 extends AbstractExecute
{
    Map<String, String> properties;
	
	public void install() {
        File fileProperties = new File(map.get('br4j.jboss.configuration.path')
                + File.separator + 'conf' + File.separator + 'dbmi' + File.separator + 'card' + File.separator 
				+ 'stamp' + File.separator + 'stamp.properties');
		updateRequiredProperty(fileProperties);
    }

    /**
     * ���������� ������� � �����
     * @param propertiesFile ���� �������
     */
    private void updateRequiredProperty(File propertiesFile)
    {
        log.info("Updating file: " + propertiesFile);
        //������ ������ ����� �� ����� �������
        List<String> fileLines = FileUtils.readLines(propertiesFile);
        //��������� �������� �������
        log.info("Delete property: signature.stamp.indent");
        PropertiesUtils.deleteProperty(fileLines, "signature.stamp.indent", 
                "Deleted by BR4J00040359 script at: " + new Date());
		
		if(PropertiesUtils.getIndexContainsProperty(fileLines, "signature.stamp.indent.x") == -1){
			log.info("Add property: signature.stamp.indent.x");
			PropertiesUtils.addProperty(fileLines, "signature.stamp.indent.x", "6",
					"Added by BR4J00040359 script at: " + new Date());
		}
			
		if(PropertiesUtils.getIndexContainsProperty(fileLines, "signature.stamp.indent.y") == -1){
			log.info("Add property: signature.stamp.indent.y");
			PropertiesUtils.addProperty(fileLines, "signature.stamp.indent.y", "6",
					"Added by BR4J00040359 script at: " + new Date());
		}
		
		Map<String,String> testProps = new HashMap<String,String>();
		testProps.put("signature.mark.organisation.field.font.size", "7");
		if(!PropertiesUtils.checkPropertyEquals(propertiesFile, testProps).isEmpty()){
			log.info("Updating property: signature.mark.organisation.field.font.size");
			PropertiesUtils.updateProperty(fileLines, "signature.mark.organisation.field.font.size", "7",
					"Edited by BR4J00040359 script at: " + new Date());
		}
		
        log.info("Updating file: " + propertiesFile + " finished");
		FileUtils.storeLines(propertiesFile, fileLines);
    }

    public static void main(String[] args) {
            new BR4J00040359().start();
    }

}