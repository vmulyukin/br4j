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
package com.aplana.dbmi.filestorage.convertmanager;


import java.io.IOException;
import java.io.InputStream;

import org.mozilla.universalchardet.UniversalDetector;

/**
 * 
 * @author ���������
 *
 */
public class CharacterDetector {

        
    private CharacterDetector() {}
    
    private static UniversalDetector detector = new UniversalDetector(null);
    

/**
 * ���������� ��� ������������ ���������    
 * @param stream �������� ����
 * @return ���������� �������� ���������
 * @throws IOException
 */

public static String detect(InputStream stream) throws IOException {
        // Reset detector before using
        detector.reset();
        // Buffer
        byte[] buf = new byte[1024];
        try {
            int nread;
            while ((nread = stream.read(buf)) > 0 && !detector.isDone()) {
              detector.handleData(buf, 0, nread);
            }
            detector.dataEnd();
            return detector.getDetectedCharset();
        } finally {
            detector.reset();
        }
    }
}
