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
package com.aplana.dbmi.card;


import javax.portlet.ActionRequest;
import javax.portlet.PortletRequest;

import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.StringAttribute;
import com.aplana.dbmi.crypto.SignatureData;


public class CertAttributeEditor extends JspAttributeEditor
{	
	public static final String PARAM_CERTHASH = "CertHash";
	public static final ObjectId certHashAttrId = ObjectId.predefined(StringAttribute.class, "jbr.certificate.certhash");
	public CertAttributeEditor() {		
		setParameter(PARAM_JSP, "/WEB-INF/jsp/html/attr/CertEdit.jsp");

	}	
	
	@Override
	public boolean gatherData(ActionRequest request, Attribute attr){
		
		String hash = request.getParameter(com.aplana.dbmi.card.CertAttributeEditor.PARAM_CERTHASH);
		String value = request.getParameter(CardPortlet.getAttributeFieldName(attr));
		
		if (value == null)
			return false;		
		try{			
			((StringAttribute) attr).setValue(value);
			
			Card card = this.getCardPortletSessionBean(request).getActiveCard();
		
			StringAttribute attrHash = (StringAttribute) card.getAttributeById(certHashAttrId);
			if(attrHash == null){
				logger.error("certhash attribute not found");
			}else{
				attrHash.setValue(hash);
			}
			return true;
		}catch(Exception e){
			logger.error("error getting certhash attribute", e);
			return false;
		}
	}
}
