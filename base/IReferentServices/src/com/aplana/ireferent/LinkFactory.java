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
package com.aplana.ireferent;

import java.util.Collection;

import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.ireferent.config.Type;
import com.aplana.ireferent.types.WSOLink;
import com.aplana.ireferent.types.WSOMDocument;

public class LinkFactory extends WSObjectFactory {

    private WSObjectFactory documentFactory;
    private WSOMDocument sourceDocument;

    protected LinkFactory() {
    }

    /*
     * (non-Javadoc)
     *
     * @see com.aplana.ireferent.util.WSObjectFactory#initialize(com.aplana.dbmi.service.DataServiceBean,
     *      com.aplana.ireferent.config.Type)
     */
    @Override
    protected void initialize(DataServiceBean factoryServiceBean,
	    Type factoryType) {
	super.initialize(factoryServiceBean, factoryType);
	documentFactory = WSObjectFactory.newInstance(serviceBean, "Document");
	documentFactory.setMObject(true);
    }

    @Override
    protected WSOLink newWSObject(Card card) throws IReferentException {
	if (sourceDocument == null) {
	    sourceDocument = (WSOMDocument) documentFactory
		    .newWSObject(parentCard);
	}
	WSOMDocument targetDocument = (WSOMDocument) documentFactory
		.newWSObject(card);
	WSOLink link = new WSOLink();
	link.setSource(sourceDocument);
	link.setTarget(targetDocument);
	return link;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.aplana.ireferent.util.WSObjectFactory#getRequiredAttributes()
     */
    @Override
    protected Collection<ObjectId> getRequiredAttributes() {
	return documentFactory.getRequiredAttributes();
    }
}
