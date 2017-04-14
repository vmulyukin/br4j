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
import groovy.util.logging.Log4j
import ru.datateh.jbr.iuh.GroovyWrapper
import ru.datateh.jbr.iuh.anno.Parameters
import ru.datateh.jbr.iuh.msg.Message
import ru.datateh.jbr.iuh.msg.MessageType
import ru.datateh.jbr.iuh.parameters.CommonParameters

@Log4j
class ExecUtils {
	
	private static Scanner scan = new Scanner(System.in)

    private static final String CODE_WORD = "2734b4f0-86ec-40bb-b62c-94681a7cc95c";
	
	private static String getUser(Map<String, String> map, Parameters params) {
		String user = map.get('iuh.executor.user.name');
		def isRunAsAdmin = params?.runAsAdmin()
		//def notInit = map.get('notInit')
		if (user != null && !user.equalsIgnoreCase("null")) {
            if(System.getProperty('os.name').startsWith('Win'))
            {
                def machineName = System.getenv("USERDOMAIN").toLowerCase();
                user = (!user.trim().isEmpty() && !isRunAsAdmin) ? 'runas /savecred /user:' + user + '@' + machineName + ' '  : '';
            } else {
                user = (!user.trim().isEmpty() && !isRunAsAdmin) ? 'sudo -u ' + user + ' ' : ''
            }
            if (log.traceEnabled){
                if (user.isEmpty()){
                    log.trace "Run parameter \'user\' was not set."
                }else{
                    log.trace "Use another user account: \'${user}\'"
                }
            }
            return user
        } else {
            return "";
        }
	}
	
	private static String getUser(Map<String, String> map) {
		getUser(map, null)
	}

    private static String getCommand (String user, String scriptPath, String scriptName, String scriptJars, Parameters params)
    {
        String cmd;
        String codeSource = getCodeSourcePath();
        if (System.getProperty('os.name').startsWith("Win")) {
            if (user.isEmpty()) {
                cmd = "$user java -cp " +  scriptPath + File.pathSeparator +
                        codeSource + File.separator + "iuh.jar${File.pathSeparator}" +
                        codeSource + File.separator + "lib" + File.separator + "clipc.jar${File.pathSeparator}" +
                        "${System.getenv("GROOVY_HOME") + File.separator}lib${File.separator}commons-cli-1.2.jar${File.pathSeparator}" +
                        "${System.getenv("GROOVY_HOME") + File.separator}embeddable${File.separator}groovy-all-${GroovySystem.version}.jar" +
                        "${scriptJars} ${scriptName} ${params ? params.vmArgs() : ''}"
            } else {
                cmd = "$user \" java -cp " +  scriptPath + File.pathSeparator +
                        codeSource + File.separator + "iuh.jar${File.pathSeparator}" +
                        codeSource + File.separator + "lib" + File.separator + "clipc.jar${File.pathSeparator}" +
                        "${System.getenv("GROOVY_HOME") + File.separator}lib${File.separator}commons-cli-1.2.jar${File.pathSeparator}" +
                        "${System.getenv("GROOVY_HOME") + File.separator}embeddable${File.separator}groovy-all-${GroovySystem.version}.jar" +
                        "${scriptJars} ${scriptName} ${params ? params.vmArgs() : ''}" + "\""
            }
        } else {
            cmd = "$user java -cp " +  scriptPath + File.pathSeparator +
                    codeSource + File.separator + "iuh.jar${File.pathSeparator}" +
                    codeSource + File.separator + "lib" + File.separator + "clipc.jar${File.pathSeparator}" +
                    "${System.getenv("GROOVY_HOME") + File.separator}lib${File.separator}commons-cli-1.2.jar${File.pathSeparator}" +
                    "${System.getenv("GROOVY_HOME") + File.separator}embeddable${File.separator}groovy-all-${GroovySystem.version}.jar" +
                    "${scriptJars} ${scriptName} ${params ? params.vmArgs() : ''}"
        }
        return cmd;
    }

	private static String getCodeSourcePath()
	{
		Map<String, String> map = IuhUtils.sharedFileToMap();
		String codeSource = map.get(CommonParameters.Iuh.CODE_SOURCE);
		if (codeSource == null || codeSource.isEmpty()) {
			codeSource = FileUtils.getCodeSourcePath();
		}
		return codeSource;
	}

    private static String getJars (String packagePath)
    {
        String jars = "";
        File libsJar = new File(packagePath + File.separator + 'lib')
        if(libsJar.exists()) {
            libsJar.list().each {
                if(it.endsWith('.jar')) {
                    jars += (File.pathSeparator + libsJar.getAbsolutePath() + File.separator + it)
                }
            }
        }
        return jars;
    }

    private static Message compileGroovyScript(String scriptPath, String scriptName, String scriptJars)
    {
        String[] scriptArgs;
        if (scriptJars.isEmpty()) {
            scriptArgs = ['-c', '-m', scriptPath + File.separator];
        } else {
            scriptArgs = ['-c', '-m', scriptPath + File.separator, '-cp', scriptJars.substring(1)];
        }

        if(!GroovyWrapper.wrap(scriptArgs, scriptName)) {
            return new Message(MessageType.ERROR, 'Error of compilation script: ' + scriptName)
        }
        return null;
    }

    private static Parameters getExtraStartParameters(String scriptPath, String scriptName)
    {
        File file = new File(scriptPath + File.separator);
        URL url = file.toURI().toURL();
        log.debug '---------URL: ' + url.toString() + '\n'
        URL[] urls = [url];
        ClassLoader cl = new URLClassLoader(urls);
        Class clazz = cl.loadClass(scriptName);
        return clazz.getAnnotation(Parameters.class);
    }

	static Message execShScript(String packagePath, String name, String extention) {
		def File script = new File(packagePath + File.separator + name + '.' + extention);
		log.debug 'path to current package: ' + packagePath
		def map = IuhUtils.sharedFileToMap()

		// ��������� ������
		def user = getUser(map)

		map.put('scriptDir', packagePath)
		IuhUtils.mapToSharedFile(map)
		def run = map.get('run')
		def sc = (user + 'bash '
						+ script.getAbsolutePath()
						+ ' read_args')
		/*sc = (user + 'bash '
			+ script.getAbsolutePath()
			+ ' '
			+ (run != null ? run.trim() :'install')
			)*/
		log.debug 'Bash script to execute: ' + sc
		def sout = new StringBuffer()
		def serr = new StringBuffer()
		def process = sc.execute()
		process.consumeProcessOutput(sout, serr)
		//process.in.eachLine { line -> log.info line }
		process.waitFor()
		log.debug 'process out: ' + sout
		log.debug 'process err: ' + serr
		//log.info 'process.text: ' + process.text
		if(process.exitValue() > 0) {
			return new Message(MessageType.ERROR, process.err.text)
		}


		def fileParams = System.nanoTime() + '.properties'
		// ����� ����� ������� ��������� ��� ��� �������
		Properties scriptProps = new Properties()

		def interactive = map.get('iuh.mode.interactive') ? Boolean.valueOf(map.get('iuh.mode.interactive')) : false
		def answerFileName = map.get('iuh.answers.file')
		log.debug 'answers file name: ' + answerFileName

		File f = null;
		FileInputStream fin = null
		Properties props = new Properties()

		FileInputStream paramsIn = null
		FileOutputStream paramsOut = null

		File paramsFile = null;

		def userInParams = [:]

		try {
			// ���� � ������� ��������� ��������� ��� ��� �������
			paramsFile = new File(packagePath + File.separator + fileParams)
			if(paramsFile.exists()) {
				paramsFile.delete()
			}
			paramsFile.createNewFile()

            scriptProps = PropertiesUtils.readPropertiesFromFile(paramsFile);
			paramsOut = new FileOutputStream(paramsFile)

			// ����� ���������� ��������� � ���� � ����������� ��� �������
			map.each { k, v ->
				scriptProps.setProperty(k, v)
			}

			if(answerFileName) {
				try {
					// ���� ���������� ���� ���������������, �� ����� �������� �� ���� ���� �������� � ���� � ����������� ��� ��� �������
					f = new File(answerFileName)
					fin = new FileInputStream(f)
					props.load(fin)

					props.each { k, v ->
						scriptProps.setProperty(k, v)
					}
				} catch(IOException e) {
					throw new Exception(e.getMessage(), e)
				} finally {
					try {
						fin.close()
					} catch(IOException e) {
						log.warn 'Can not close stream. Execution will continue with an open stream.'
					}
				}
			}

			// ���� ���� ��������� ��� ����� � ����� ������, �� ����������� �� � ���� � ���� ���������� � ���� � ����������� ��� ��� �������
			if(sout) {
				sout.replaceAll('\n','').split(',').each {
					System.out.print 'Please enter parameter "' + it + '": '
					def val = scan.nextLine()
					scriptProps.setProperty(it, val)
					userInParams.put(it, val)
				}
			}

			// ��������� ���� � ����������� ��� ��� �������
            PropertiesUtils.storeProperties(paramsFile, scriptProps);

		} catch(IOException e) {
			throw new Exception(e.getMessage(), e)
		} finally {
			try {
				paramsIn.close()
				paramsOut.close()
			} catch(IOException e) {
				log.warn 'Can not close stream. Execution will continue with an open stream.'
			}
		}

		// ���� ������� ������������� ����� (interactive) � ���������� ���� ��������������� (f)
		// �� ����� �������� ��������� ��������� ������ � ���� ���������������, ���� ������� ���� ���-�� ������ (userInParams)
		if(interactive
			&& userInParams
			&& f != null) {

			FileOutputStream out = null

			try {

				fin = new FileInputStream(f)
				props.load(fin)
				out = new FileOutputStream(f)

				String val = null
				userInParams.each { k, v ->
					props.setProperty(k, v)
				}
				props.store(out, null)
			} catch(IOException e) {
				throw new Exception(e.getMessage(), e)
			} finally {
				try {
					fin.close()
					out.close()
				} catch(IOException e) {
					log.warn 'Can not close stream. Execution will continue with an open stream.'
				}
			}
		}


		sc = (user + 'bash '
				 + script.getAbsolutePath()
				 + ' '
				 + (run != null ? run.trim() : 'install')
				 + ' ' + fileParams
				 )
		log.info 'Bash script to execute: ' + sc
		process = sc.execute()
		process.consumeProcessOutput(System.out, System.err)
		process.waitFor()

		if(paramsFile != null && paramsFile.exists()) {
			paramsFile.delete()
		}

		if(process.exitValue() > 0) {
			return new Message(MessageType.ERROR, process.err.text)
		} else {
			return new Message(MessageType.SUCCESS, "Script ${name}.${extention} executed successfully")
		}
	}

    public static Message execGroovyScript(String packagePath, String name, String extention) {
        log.debug 'Compiling GROOVY script ' + packagePath + File.separator + name + '.' + extention + '...'
        def Message msg
        String absPath = (new File(packagePath + File.separator)).getAbsolutePath();
        Map<String, String> map = IuhUtils.sharedFileToMap()

        String jars = getJars(packagePath);

        Message compileMessage = compileGroovyScript(absPath, name, jars);
        if (compileMessage != null){return compileMessage;}

        //----------------------
        // Annotation
        Parameters params = getExtraStartParameters(absPath, name);
        //---------------------

        // ��������� ������
        def user = getUser(map, params)
        IuhUtils.mapToSharedFile(map)


        log.debug 'Executing GROOVY script ' + packagePath + File.separator + name + '.' + extention + '...'
        String cmd = getCommand(user, absPath, name, jars, params);

        log.debug '...command: ' + cmd

		log.debug 'Changing owner of log file to ' + map.get(CommonParameters.Iuh.EXECUTOR_USER_NAME);
		FileUtils.changeLogFileOwnerOnNixSystem(map.get(CommonParameters.Iuh.EXECUTOR_USER_NAME));

        final Process process1 = Runtime.getRuntime().exec(cmd);

        new Thread(new Runnable() {
            public void run() {
                InputStreamReader reader = new InputStreamReader(process1.getInputStream());
                Scanner scan = new Scanner(reader);
                while (scan.hasNextLine()) {
                    String processOut = scan.nextLine();
                    if (processOut.contains(CODE_WORD))
                    {
                        PrintWriter pWriter = new PrintWriter(process1.getOutputStream());
                        System.out.println(processOut.replace(CODE_WORD, "Please inter the value"));
                        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
                        String data = "empty";
                        try {
                            data = br.readLine();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        pWriter.println(data);
                        pWriter.flush();
                    }
                    else
                    {
                        System.out.println(processOut);
                    }
                }
            }
        }).start();

        process1.waitFor();


        map = IuhUtils.sharedFileToMap()
        if(process1.exitValue() > 0) {
            msg = new Message(MessageType.ERROR, 'Exit code: ' + process1.exitValue() + ', ' + process1.err.text)
        } else if(map.get('msgKey') != null && map.get('msgVal') != null) {
            msg = new Message(MessageType.getMessageType(map.get('msgKey')), map.get('msgVal'))
            IuhUtils.removeParamFromMap('msgKey')
            IuhUtils.removeParamFromMap('msgVal')
        } else {
            msg = new Message(MessageType.SUCCESS, "Script ${name}.${extention} executed successfully")
        }

        deleteClasses(packagePath)
        return msg
    }

	private static void deleteClasses(String packagePath) {
		File deleteFile = new File(packagePath)
		deleteFile.listFiles().each {
			if(it.isFile() && it.getName().endsWith(".class") && it.exists()) {
				it.delete()
			}
		}
	}

}
