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
package com.aplana.dbmi.card.download.descriptor;

import java.util.List;

/**
 * class that incapsulates data that necessary to dispatch
 * download or upload actions
 * it holds actions variants
 */
public class FileActionDescriptor {

    private List<FileActionVariantDescriptor> variants;

    public List<FileActionVariantDescriptor> getVariants() {
        return variants;
    }

    public void setVariants(List<FileActionVariantDescriptor> variants) {
        this.variants = variants;
    }

    public FileActionVariantDescriptor getActionVariantDescriptorByActionId(String actionId){
        FileActionVariantDescriptor result = null;
        if (actionId == null) {
            return result;
        }
        for (FileActionVariantDescriptor vd : getVariants()) {

            if (actionId.equals(vd.getActionName())) {
                result = vd;
                break;
            }

        }

        return result;
    }
}
