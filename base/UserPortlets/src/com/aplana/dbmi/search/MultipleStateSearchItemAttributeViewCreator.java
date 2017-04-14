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
package com.aplana.dbmi.search;

import com.aplana.dbmi.gui.SearchAttributeView;
import com.aplana.dbmi.model.MultipleStateSearchItemAttribute;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.ServiceException;

import javax.portlet.PortletRequest;

public class MultipleStateSearchItemAttributeViewCreator  extends
		SearchAttributeViewCreator<MultipleStateSearchItemAttribute> {
	
	public MultipleStateSearchItemAttributeViewCreator(MultipleStateSearchItemAttribute attribute) {
		super(attribute);
	}

	@Override
	public SearchAttributeView create(PortletRequest request,
			SearchFilterPortletSessionBean sessionBean)
			throws ServiceException, DataException {
		SearchAttributeView attributeView = createAndInitSearchAttributeView(request);
		return attributeView;
	}
}
