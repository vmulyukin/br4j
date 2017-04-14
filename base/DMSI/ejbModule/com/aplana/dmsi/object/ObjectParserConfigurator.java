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
package com.aplana.dmsi.object;

import com.aplana.dmsi.card.DMSIObjectConverter;
import com.aplana.dmsi.config.ClassConfig;
import com.aplana.dmsi.config.FieldConfig;
import com.aplana.dmsi.types.DMSIObject;

public interface ObjectParserConfigurator {

    DMSIObjectConverter getFieldConverter(FieldConfig fieldConfig);

    boolean isFieldIgnoring(FieldConfig fieldConfig);

    ClassConfig getConfig(DMSIObject object);
}
