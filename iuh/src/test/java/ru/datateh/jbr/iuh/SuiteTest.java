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
package ru.datateh.jbr.iuh;

import java.util.ArrayList;
import java.util.List;

/**
 * @author etarakanov
 *         Date: 10.04.2015
 *         Time: 15:07
 */
public class SuiteTest {

/*    @Mocked
    File file;

    @Mocked
    Package pack;

    @Tested
    Suite testedSuite;

    @Tested
    FileUtils fileUtils;

    @Test
    public void testExecuteSuccess(@Injectable("suiteName") String path, @Injectable("false") boolean force,
                                   @Injectable("false") boolean interactive,
                                   @Injectable final InterruptedObject iObj) throws Exception {

        new Expectations(testedSuite, fileUtils){{
            FileUtils.checkDirectoryExist((File) any, anyBoolean); result = true;
            FileUtils.checkFileExist((File) any, anyBoolean); result = true;
            invoke(testedSuite, "checkAnswerFile"); result = new Message(MessageType.SUCCESS,"Test message success");
            FileUtils.readLines((File) any); result = getPassportContent();
            iObj.isFlag(); result = false;
        }};

        testedSuite.execute();

        new Verifications(){{
            pack.execute(); times = 5;
        }};
    }

    @Test
    public void testExecuteError(@Injectable("suiteName") String path, @Injectable("false") boolean force,
                                   @Injectable("false") boolean interactive,
                                   @Injectable final InterruptedObject iObj) throws Exception {

        new Expectations(testedSuite, fileUtils){{
            file.exists(); result = false;
        }};

        testedSuite.execute();

        new Verifications(){{
            pack.execute(); times = 0;
        }};
    }*/

    private List<String> getPassportContent()
    {
        List<String> passportContent = new ArrayList<String>();
        passportContent.add("suiteName");
        passportContent.add("packageName1");
        passportContent.add("packageName2");
        passportContent.add("packageName3");
        passportContent.add("packageName4");
        passportContent.add("packageName5");
        return passportContent;
    }

/*    @Test
    public void testExecute() throws HarnessException {
        String PASSPORT_FILE = "update-set.id";
        String path = "C:/Users/etarakanov/Iuh/IuhPackages/update";
        File pass = new File(path + File.separator + PASSPORT_FILE);
//        File pass = new File("C:/Users/etarakanov/Iuh/IuhPackages/update/update-set.id");
        if(!pass.exists()) {
            System.out.println("file not found");
        }

        Suite suite = new Suite(path, false, false, InterruptedObject.getInstance());
        suite.execute();
    }*/
}