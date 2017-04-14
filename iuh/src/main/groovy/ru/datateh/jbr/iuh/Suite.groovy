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
package ru.datateh.jbr.iuh
import groovy.util.logging.Log4j
import ru.datateh.jbr.iuh.parameters.CommonParameters
import ru.datateh.jbr.iuh.utils.FileUtils
import ru.datateh.jbr.iuh.utils.IuhUtils
import ru.datateh.jbr.iuh.msg.HarnessException
import ru.datateh.jbr.iuh.msg.Message
import ru.datateh.jbr.iuh.msg.MessageType

@Log4j
public class Suite {
	
	static public String PASSPORT_FILE = "update-set.id";
	private String path;
	private List<Package> packages = new ArrayList<Package>();
	private boolean force;
	private boolean interactive;
	private volatile InterruptedObject iObj
    private Map<String, String> map;


	public Suite(String path, boolean force, boolean interactive, InterruptedObject iObj) throws HarnessException {

		this.path = path;
		this.force = force;
		this.interactive = interactive;
		this.iObj = iObj;
        this.map = IuhUtils.sharedFileToMap();

	}

	public Suite(String path) throws HarnessException {

		this.path = path;

	}

	public Message execute() {

		log.info "Checking suite \"" + path + "\"..."
		Message result = checkSuiteAndBuildPackages();
		if(result.getState() == MessageType.ERROR) {
			log.error "...checking suite \"" + path + "\" FAILED ! Execution will not continue."
			return result;
		} else {
			log.info "...suite \"" + path + "\" checked successfully"
		}

		for(Package pack : packages) {
			while(iObj.isFlag()) {
				iObj.setFinished(true)
				sleep 100
			}
			log.trace "Start preparing and executing package \"" + pack + "\""
			iObj.setFinished(false)
			result = pack.execute();
			if(result.getState() == MessageType.ERROR) {
				if(!force) {
					log.error 'Execution will not continue.'
					return result;
				} else {
					log.error result
					log.error 'FORCE mode enabled, execution will continue.'
				}
			} else {
				log.info result
			}
		}
	return new Message(MessageType.SUCCESS, "Suite \"" + path + "\" executed!")
	}

    private Message checkAnswerFile()
    {
        String answersFileName = map.get(CommonParameters.Iuh.USER_ANSWER_FILE);
        if (!interactive)
        {
            if (answersFileName != null && !answersFileName.isEmpty())
            {
                File answer = new File(answersFileName);
                if(FileUtils.checkFileExist(answer, false))
                {
                    return new Message(MessageType.SUCCESS, "...using the old version of answers file");
                }
            }
            return new Message(MessageType.ERROR, 'Interactive mode off and answers file: ' + answersFileName + ' not exists');
        }
        else
        {
            if (answersFileName != null && !answersFileName.isEmpty())
            {
                File answer = new File(answersFileName);
                if(FileUtils.checkFileExist(answer, false))
                {
                    answer.delete()
                    answer.createNewFile()
                    log.debug "answer permission changed = " + FileUtils.changeFilePermission(answer, FileUtils.Permission.WRITE, false);
                    return new Message(MessageType.SUCCESS, "...recreated answers file");
                }
            }
            return new Message(MessageType.SUCCESS, "...will not use answers file");
        }
    }

	private Message checkSuiteAndBuildPackages() {

		// ��������� ������� ������
		File suite = new File(path);
		if(!FileUtils.checkDirectoryExist(suite, false)) {
			return new Message(MessageType.ERROR, "Suite \"" + path + "\" does not exist");
		}

		// ��������� ������� update-set.id
		File pass = new File(path + File.separator + PASSPORT_FILE);
		log.debug '...suite path: ' + suite.getAbsolutePath()
		if(!FileUtils.checkFileExist(pass, false)) {
			return new Message(MessageType.ERROR, "Passport file: \"" + PASSPORT_FILE + "\" does not exist for set \"" + path + "\"");
		}

        Message result = checkAnswerFile();
        if (result.getState() != MessageType.SUCCESS)
        {
            return result;
        }

		try {
			List<String> passportContent = FileUtils.readLines(pass);
			String suiteNameFromPassport = passportContent.get(0).trim();
			String suiteNameFromPath = new File(path).getName().trim();
			log.debug '...checked suite\'s passport'
			if (!suiteNameFromPath.equals(suiteNameFromPassport))
			{
				return new Message(MessageType.ERROR, "Suite\'s name: " + suiteNameFromPath + " do not match with " + PASSPORT_FILE + " header: " + suiteNameFromPassport);
			}
			List<String> packageNames = passportContent.subList(1, passportContent.size());
			for(String packageName : packageNames)
			{
				Package pack = new Package(path + File.separator + packageName.trim(), force, iObj);
				packages.add(pack);
				log.debug "...added package \"" + packageName.trim() + "\""
			}
		}
		catch (HarnessException e) { return e.getHarnessMessage();}
		
		if(packages.isEmpty()) {
			return new Message(MessageType.ERROR, "Passport \"" + PASSPORT_FILE + "\" does not contain packages");
		}

		log.trace "Packages of current suite: \"" + this.packages + "\""
		
		return new Message(MessageType.SUCCESS, "Path to Suite and Passport " + PASSPORT_FILE + " checked successfully for " + path);
	}
	
}
