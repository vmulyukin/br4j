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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aplana.br4j.dynamicaccess.xmldef.AccessConfig;
import org.aplana.br4j.dynamicaccess.xmldef.Status;
import org.aplana.br4j.dynamicaccess.xmldef.Template;

/**
 * A utility class for {@link AccessConfig}.
 * @author atsvetkov
 *
 */
public class AccessConfigUtility {

	protected static final Log logger = LogFactory.getLog(AccessConfigUtility.class);

    /**
     * Gets status name by status id
     * @param statusId used to find status 
     * @return status name
     */
    public static String getStatusNameByStatusId(String statusId, AccessConfig accessConfig){                
        if (accessConfig != null && statusId != null) {        
	        for (Template template : accessConfig.getTemplate()) {
	            for (Status status : template.getStatus()) {
	            	if(statusId.equals(status.getStatus_id())) {
	            		return status.getName();
	            	}
	            }
	        }
	        logger.debug("Not found status for statusId: " + statusId);
        }
        return null;
    }

}
