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
import org.codehaus.groovy.tools.FileSystemCompiler
import ru.datateh.jbr.iuh.msg.Message
import ru.datateh.jbr.iuh.msg.MessageType

@Log4j
public class GroovyWrapper {
	
	public static Message wrap(args, fileName) {
		/**
		 * Compile a groovy script (groovyc) and/or make executable
		 */
		log.debug '...compiling arguments:   ' + args
		try {
            CliBuilder cli = new CliBuilder();
            cli.with {
                h( longOpt: 'help', required: false, 'show usage information' );
                m( longOpt: 'mainclass', argName: 'mainclass', required: true, args: 1, 'fully qualified main class, eg. HelloWorld' );
                c( longOpt: 'groovyc', required: false, 'Run groovyc' );
                cp(longOpt: 'classpath', argName: 'classpath', required: false, 'Class path to imported jars', args: 1);
            }
		
			//--------------------------------------------------------------------------
            def opt = cli.parse(args);
			if (!opt) { return new Message(MessageType.ERROR, 'No compile arguments') }
			if (opt.h) {
				cli.usage()
				return new Message(MessageType.SUCCESS, 'Show usage information')
			}

            String classPath;
            if (opt.cp)
            {
                classPath = opt.cp;
            }
		
			def scriptBase = opt.m;
			def scriptFile = new File( scriptBase + fileName + '.groovy' );
			if (!scriptFile.canRead()) {
				return new Message(MessageType.SUCCESS, "Cannot read script file: ${scriptFile}")
			}
		
			//--------------------------------------------------------------------------
			def ant = new AntBuilder();
			if (opt.c) {
				log.debug '...ant args: ' + [ '-d', scriptBase, scriptBase + fileName + '.groovy' ]
				if (classPath == null) {
                    FileSystemCompiler.main( [ '-d', scriptBase, scriptBase + fileName + '.groovy' ] as String[] );
                } else {
                    FileSystemCompiler.main( [ '-d', scriptBase, '-classpath', classPath, scriptBase + fileName + '.groovy' ] as String[] );
                }
			}
			log.debug '...compiled !'
			return new Message(MessageType.SUCCESS, "Script ${scriptFile} compiled successfully")
		} catch(Exception e) {
			return new Message(MessageType.ERROR, e.getMessage())
		}
	}

}
