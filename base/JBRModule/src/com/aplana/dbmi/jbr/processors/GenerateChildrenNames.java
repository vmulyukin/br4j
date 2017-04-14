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
package com.aplana.dbmi.jbr.processors;

import com.aplana.dbmi.jbr.util.AttributeLocator;
import com.aplana.dbmi.jbr.util.CardUtils;
import com.aplana.dbmi.model.*;
import com.aplana.dbmi.service.DataException;

import java.util.Collection;

public class GenerateChildrenNames extends GenerateName {
	private static final long serialVersionUID = 1L;
	
	public static final String PARAM_DST_CHILDREN_ATTR_ID  = "childrenCardLinkAttrId";
	
	public GenerateChildrenNames() throws DataException {
		super();
	}
	
	@Override
	public Object process() throws DataException {
		getParameters();
		AttributeLocator childrenCardLinkLocator = makeObjectId(super.getParameter( PARAM_DST_CHILDREN_ATTR_ID, null));
		if (childrenCardLinkLocator == null) {
			logger.warn("parameter " + PARAM_DST_CHILDREN_ATTR_ID + " is not set -> exiting");
			return null;
		}
		
		Card card = getCard();
		LinkAttribute clAtrr = card.getAttributeById(childrenCardLinkLocator.getAttrId());
		if (clAtrr != null) {
			Collection<ObjectId> children;
			if (CardLinkAttribute.class.isAssignableFrom(clAtrr.getId().getType())) {
				children = clAtrr.getIdsLinked();
			} else if (BackLinkAttribute.class.isAssignableFrom(clAtrr.getId().getType())) {
				children = CardUtils.getCardIdsByBackLink(clAtrr.getId(), card.getId(),
						getQueryFactory(), getDatabase(), getUser());
			} else {
				throw new ClassCastException("Attribute " + clAtrr.getId().getId() + " must be CardLink or BackLink");
			}
			
			if (children != null) {
				for (ObjectId childId : children) {
					Card childCard = loadCardById(childId);
					preparedProcessSrcAttributes(card, childCard, true);
				}
			}
		}
		return null;
	}
}
