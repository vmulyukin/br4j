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
package org.aplana.br4j.dynamicaccess;

import org.apache.commons.cli.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aplana.br4j.dynamicaccess.db_export.DBOperationUtil;
import org.aplana.br4j.dynamicaccess.xmldef.AccessConfig;
import org.aplana.br4j.dynamicaccess.xmldef.Template;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
/**
 * @author etarakanov
 *         Date: 27.02.15
 *         Time: 14:56
 */

/**
 * ����� ��� ������ � �������� Dynamic Access Utility ����� ���������� ������
 * <p>
 *�������� ���������� ��������� ��������:<p>
 * - ��������� ���������� ���� � ������ � �� � �������� �������������� ���� (- c rlrcPartial -m filename.xml);<p>
 * - ������������ ��� ����� � ������� � �� (- c rlACR -m filename.xml);<p>
 * - ������������ ����� � ������� ��� ������� � �� (- c rlTmpl -t filename.xml);<p>
 * - ����������� ����� (- c rcACL);<p>
 * - ������������ ��������� ������� ������� � �� � ����������� ����� (- c rlrcACR -t filename.xml -r Role1,Role2,Role3...RoleN);<p>
 * - ������������ ��������� ������� ��� ��������� �������� ������� � �� � ����������� ����� (- c rlrcACRByStatus -t filename.xml -r Role1,Role2,Role3...RoleN -s Status1,Status2,Status3...StatusN).<p>
 *
 * ��� ���������� ��������� ������� ���������� ������� ��������� ���������:<p>
 * url - ���� ����������� � ��;<p>
 * userName - ������� ������ ������������ ��;<p>
 * userPassword- ������ ������������ ��;<p>
 * command - �������� ��� ����������;<p>
 * config - ���� xml ���������� ������������ ������ � ���� ������� ��� ���� �������� ({@link org.aplana.br4j.dynamicaccess.xmldef.AccessConfig});<p>
 * template - ���� xml ���������� ������������ ������ � ���� ������� ��� ������ ������� ({@link org.aplana.br4j.dynamicaccess.xmldef.Template});<p>
 * ruleNames - ������ ���� ������;<p>
 * statusIDs - ������ �������� ��������;<p>
 * help - ����� �������;<p>
 *
 * ������ �������:<p>
 * java -cp C:\Users\etarakanov\FSIN_base\DynamicAccessRule\build\jars/postgresql-9.0-801.jdbc3.jar;c:\Project\BossRef\_Build\tool\DynamicAccessRule\DynamicAccessRule.jar org.aplana.br4j.dynamicaccess.DynamicAccessCLI -command rcACL -url jdbc:postgresql://localhost:5432/fsin_boev -userName postgres -userPassword XSW@zaq1
 */
public class DynamicAccessCLI
{
    private static final String URL_OPTION = "url";
    private static final String NAME_OPTION = "userName";
    private static final String PASSWORD_OPTION = "userPassword";
    private static final String COMMAND_OPTION = "command";
    private static final String CONFIG_OPTION = "config";
    private static final String TEMPLATE_OPTION = "template";
    private static final String ROLE_NAME_OPTION = "ruleNames";
    private static final String STATUS_OPTION = "statusIDs";
    private static final String HELP_OPTION = "help";

    private static final String URL_OPT = "u";
    private static final String NAME_OPT = "n";
    private static final String PASSWORD_OPT = "p";
    private static final String COMMAND_OPT = "c";
    private static final String CONFIG_OPT = "m";
    private static final String TEMPLATE_OPT = "t";
    private static final String ROLE_NAME_OPT = "r";
    private static final String STATUS_OPT = "s";
    private static final String HELP_OPT = "h";

    private static final String SAVE_ACCESS_RULES_COMMAND = "rlACR";
    private static final String SAVE_TEMPLATE_COMMAND = "rlTmpl";
    private static final String UPDATE_ACCESS_LIST_COMMAND = "rcACL";
    private static final String UPDATE_ACCESS_RULES_COMMAND = "rlrcACR";
    private static final String UPDATE_ACCESS_RULES_BY_STATUS_COMMAND = "rlrcACRByStatus";
    private static final String UPDATE_PARTIAL_COMMAND = "rlrcPartial";

    private static final Log logger = LogFactory.getLog(DynamicAccessCLI.class);

    public static void main( String[] args ) throws CLIException {
        logger.info("Dynamic Access Utility from Command Line Interface started...");
        Exception result;
        try {
            result = getCommand(getCommandLine(args)).execute();
        } catch (Exception e) {
            logger.error(e);
            throw new CLIException(e);
        }
        if (result != null)
        {
            logger.error(result);
            throw new CLIException(result);
        }
    }

    private static CommandLine getCommandLine (String[] args) throws CLIException {
        CommandLineParser parser = new GnuParser();
        CommandLine commandLine;
        try {
            commandLine = parser.parse(getOptions(), args);
        }
        catch( ParseException exp ) {
            throw new CLIException("Parsing failed.  Reason: " + exp.getMessage());
        }
        return commandLine;
    }

    @SuppressWarnings("static-access")
    private static Options getOptions() {
        Option command = OptionBuilder.withArgName(COMMAND_OPTION).hasArg().withLongOpt(COMMAND_OPTION).withDescription("what to do").create(COMMAND_OPT);
        Option url = OptionBuilder.withArgName(URL_OPTION).hasArg().withLongOpt(URL_OPTION).withDescription("data base url").create(URL_OPT);
        Option userName = OptionBuilder.withArgName(NAME_OPTION).hasArg().withLongOpt(NAME_OPTION).withDescription("data base user login").create(NAME_OPT);
        Option userPassword = OptionBuilder.withArgName(PASSWORD_OPTION).hasArg().withLongOpt(PASSWORD_OPTION).withDescription("data base user password").create(PASSWORD_OPT);
        Option config   = OptionBuilder.withArgName(CONFIG_OPTION).hasArg().withLongOpt(CONFIG_OPTION).withDescription("xml-file with AccessConfig data").create(CONFIG_OPT);
        Option template   = OptionBuilder.withArgName(TEMPLATE_OPTION).hasArg().withLongOpt(TEMPLATE_OPTION).withDescription("xml-file with Template data").create(TEMPLATE_OPT);
        Option roleNames   = OptionBuilder.withArgName(ROLE_NAME_OPTION).hasArg().withLongOpt(ROLE_NAME_OPTION).withDescription("list of rule names").create(ROLE_NAME_OPT);
        Option statusIDs   = OptionBuilder.withArgName(STATUS_OPTION).hasArg().withLongOpt(STATUS_OPTION).withDescription("list of status ids").create(STATUS_OPT);
        Option help   = OptionBuilder.withArgName(HELP_OPTION).withLongOpt(HELP_OPTION).withDescription("help information").create(HELP_OPT);

        Options options = new Options();
        options.addOption(command);
        options.addOption(url);
        options.addOption(userName);
        options.addOption(userPassword);
        options.addOption(config);
        options.addOption(template);
        options.addOption(roleNames);
        options.addOption(statusIDs);
        options.addOption(help);
        return options;
    }

    private static Command getCommand (CommandLine line) throws ParseException {
        String commandName;
        if(line.hasOption(COMMAND_OPTION)){
            commandName =  line.getOptionValue(COMMAND_OPTION);
            if (SAVE_ACCESS_RULES_COMMAND.equalsIgnoreCase(commandName))
            {
                return new DoSaveAccessRules(line);
            }
            else if (UPDATE_ACCESS_LIST_COMMAND.equalsIgnoreCase(commandName))
            {
                return new DoUpdateAccessList(line);
            }
            else if (UPDATE_PARTIAL_COMMAND.equalsIgnoreCase(commandName))
            {
                return new DoUpdatePartial(line);
            }
            else if (SAVE_TEMPLATE_COMMAND.equalsIgnoreCase(commandName))
            {
                return new DoSaveTemplate(line);
            }
            else if (UPDATE_ACCESS_RULES_COMMAND.equalsIgnoreCase(commandName))
            {
                return new DoUpdateAccessRule(line);
            }
            else if (UPDATE_ACCESS_RULES_BY_STATUS_COMMAND.equalsIgnoreCase(commandName))
            {
                return new DoUpdateAccessRuleByStatus(line);
            }
            System.out.println("command: " + commandName + " not found");
        }
        return new HelpCommand();
    }

    private interface Command
    {
        Exception execute() throws Exception;
    }

    private static class BaseCommand
    {
        private String commandName;
        private String url;
        private String userName;
        private String userPassword;

        public String getCommandName() {
            return commandName;
        }

        public String getUrl() {
            return url;
        }

        public String getUserName() {
            return userName;
        }

        public String getUserPassword() {
            return userPassword;
        }

        private BaseCommand() {
        }

        public BaseCommand(CommandLine line) throws ParseException {
            this.url = getOptionValue(line, URL_OPTION);
            this.userName = getOptionValue(line, NAME_OPTION);
            this.userPassword = getOptionValue(line, PASSWORD_OPTION);
            this.commandName = getOptionValue(line, COMMAND_OPTION);
        }

        protected static String getOptionValue (CommandLine line, String optionName) throws ParseException {
            if(line.hasOption(optionName)){
                return line.getOptionValue(optionName);
            }
            else
            {
                throw new ParseException(optionName + " must be set");
            }
        }

        protected static AccessConfig loadAccessConfig (String filePath) throws Exception {
            File file = new File(filePath);
            if (!file.isFile())
            {
                throw new CLIException("Can not find file: " + file);
            }
            InputStreamReader reader = new InputStreamReader(new FileInputStream(file.getAbsolutePath()), "UTF-8");
            return  (AccessConfig) AccessConfig.unmarshal(reader);
        }

        protected static Template loadTemplate (String filePath) throws Exception {
            File file = new File(filePath);
            if (!file.isFile())
            {
                throw new CLIException("Can not find file: " + file);
            }
            InputStreamReader reader = new InputStreamReader(new FileInputStream(file.getAbsolutePath()), "UTF-8");
            return  (Template) Template.unmarshal(reader);
        }
    }

    private static class HelpCommand implements Command
    {
        private final File helpFile = new File("helpMessage.txt");

        @Override
        public Exception execute() throws Exception {
            HelpFormatter formatter = new HelpFormatter();
            List<String> helpLines = readLines(helpFile.getAbsoluteFile());
            for (String line : helpLines) {
                System.out.println(line);
            }
            formatter.printHelp( "DynamicAccessCLI", getOptions());
            return null;
        }

        private static List<String> readLines(File file) throws CLIException {
            List<String> result = new ArrayList<String>();
            String line;
            try {
            InputStream fis = ClassLoader.getSystemResourceAsStream(file.getName());
            InputStreamReader isr = new InputStreamReader(fis, Charset.forName("UTF-8"));
            BufferedReader br = new BufferedReader(isr);
                while ((line = br.readLine()) != null) {
                    result.add(line);
                }
            } catch (Exception e) {
                throw new CLIException(e);
            }
            return result;
        }
    }

    private static class DoSaveAccessRules extends BaseCommand implements Command
    {
        private String accessConfigFile;

        public DoSaveAccessRules(CommandLine line) throws ParseException {
            super(line);
            this.accessConfigFile = getOptionValue(line, CONFIG_OPTION);
        }

        @Override
        public Exception execute() throws Exception {
            DBOperationUtil util = new DBOperationUtil();
            AccessConfig config;
            try {
                config = loadAccessConfig(this.accessConfigFile);
            } catch (Exception e) {
                return e;
            }
            return util.doSaveAccessRules(getUrl(), getUserName(), getUserPassword(), config);
        }
    }

    private static class DoUpdateAccessList extends BaseCommand implements Command
    {
        public DoUpdateAccessList(CommandLine line) throws ParseException {
            super(line);
        }

        @Override
        public Exception execute() throws Exception {
            DBOperationUtil util = new DBOperationUtil();
            return util.doUpdateAccessList(getUrl(), getUserName(), getUserPassword(), null, false);
        }
    }

    private static class DoUpdatePartial extends BaseCommand implements Command
    {
        private String accessConfigFile;

        public DoUpdatePartial(CommandLine line) throws ParseException {
            super(line);
            this.accessConfigFile = getOptionValue(line, CONFIG_OPTION);
        }

        @Override
        public Exception execute() throws Exception {
            DBOperationUtil util = new DBOperationUtil();
            AccessConfig config;
            try {
                config = loadAccessConfig(this.accessConfigFile);
            } catch (Exception e) {
                return e;
            }
            return util.doUpdatePartial(getUrl(), getUserName(), getUserPassword(), config);
        }
    }

    private static class DoSaveTemplate extends BaseCommand implements Command
    {
        private String templateFile;

        public DoSaveTemplate(CommandLine line) throws ParseException {
            super(line);
            this.templateFile = getOptionValue(line, TEMPLATE_OPTION);
        }

        @Override
        public Exception execute() throws Exception {
            DBOperationUtil util = new DBOperationUtil();
            Template template;
            try {
                template = loadTemplate(this.templateFile);
            } catch (Exception e) {
                return e;
            }
            return util.doSaveTemplate(getUrl(), getUserName(), getUserPassword(), template);
        }
    }

    private static class DoUpdateAccessRule extends BaseCommand implements Command
    {
        private String templateFile;
        private List<String> rules;

        public DoUpdateAccessRule(CommandLine line) throws ParseException {
            super(line);
            this.templateFile = getOptionValue(line, TEMPLATE_OPTION);
            this.rules =  Arrays.asList(getOptionValue(line, ROLE_NAME_OPTION).split(","));
        }

        @Override
        public Exception execute() throws Exception {
            DBOperationUtil util = new DBOperationUtil();
            Template template;
            try {
                template = loadTemplate(this.templateFile);
            } catch (Exception e) {
                return e;
            }
            return util.doUpdateAccessRule(getUrl(), getUserName(), getUserPassword(), rules, template);
        }
    }

    private static class DoUpdateAccessRuleByStatus extends BaseCommand implements Command
    {
        private String templateFile;
        private List<String> rules;
        private List<String> statusIDs;


        public DoUpdateAccessRuleByStatus(CommandLine line) throws ParseException {
            super(line);
            this.templateFile = getOptionValue(line, TEMPLATE_OPTION);
            this.rules =  Arrays.asList(getOptionValue(line, ROLE_NAME_OPTION).split(","));
            this.statusIDs =  Arrays.asList(getOptionValue(line, STATUS_OPTION).split(","));
        }

        @Override
        public Exception execute() throws Exception {
            DBOperationUtil util = new DBOperationUtil();
            Template template;
            try {
                template = loadTemplate(this.templateFile);
            } catch (Exception e) {
                return e;
            }
            return util.doUpdateAccessRuleByStatus(getUrl(), getUserName(), getUserPassword(), rules, statusIDs, template);
        }
    }

}
