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
package com.aplana.dmsi.types.common;

import java.util.ArrayList;
import java.util.List;

import com.aplana.dmsi.types.DMSIObject;
import com.aplana.dmsi.types.DocTransfer;

public class ImportedDocCard extends DMSIObject {
    private String uid;
    private String processingResult;
    private List<DocTransfer> files;

    public String getUid() {
        return this.uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getProcessingResult() {
        return this.processingResult;
    }

    public void setProcessingResult(String processingResult) {
        this.processingResult = processingResult;
    }

    public List<DocTransfer> getFiles() {
        if (this.files == null) {
            this.files = new ArrayList<DocTransfer>();
        }
        return this.files;
    }

}
