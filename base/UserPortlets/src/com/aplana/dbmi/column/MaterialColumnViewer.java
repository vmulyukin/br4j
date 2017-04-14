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
package com.aplana.dbmi.column;

import java.util.List;

import com.aplana.dbmi.card.CardPortlet;
import com.aplana.dbmi.column.ColumnViewerFactory.ParamInfo;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.BackLinkAttribute;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.web.tag.util.StringUtils;

/**
 * Class which generates pop-up dialog with list of attached documents
 * for document in {@link com.aplana.dbmi.showlist.MIShowListPortlet}
 * @author aklyuev
 */
public class MaterialColumnViewer extends DefaultColumnViewer {

	public static final String PARAM_MATERIAL_LINK = "materialLink";

	@Override
	protected String getColumnData(List<Object> columnValue, ParamInfo paramInfo, int colNumInt) {
		 String value = null;
		 if(colNumInt < 0){
			 value=paramInfo.getAttrValue(PARAM_DEFAULT_VALUE);
		 } else {
			 value = String.valueOf(columnValue.get(Math.abs(colNumInt)+1));
		 }
		 final boolean isLink = Boolean.parseBoolean(paramInfo.getAttrValue(PARAM_MATERIAL_LINK));
		 if (value != null && isLink) {
//			 String labelValue = null;
//			 if (paramInfo.getAttrValue(PARAM_LABEL_COLUMN) != null) { 
//				 Integer labelCol = Integer.valueOf(paramInfo.getAttrValue(PARAM_LABEL_COLUMN));
//				 labelCol++;//Because column 0 and column 1 are reserved, user columns starts from 2
//				 if (columnValue.get(labelCol) != null) {
//					 labelValue = String.valueOf(columnValue.get(labelCol));
//				 }
//			 }
			 return getMaterialLink(String.valueOf(columnValue.get(0)), paramInfo, value);
		 }
		 return value;
	}

	protected String getMaterialLink(String cardId, ParamInfo paramInfo, String label) {
		if (cardId != null) { 

			StringBuilder sb = new StringBuilder();
			sb.append("<a href=\"/DBMI-UserPortlets/MaterialDownloadServlet?")
			   .append(CardPortlet.CARD_ID_FIELD)
			   .append("="+cardId+"\">")
			   .append(label)
			   .append("</a>");
			 return sb.toString();

		}
		return null;
	}

	protected void parseParamsAttributeForSearch(Attribute attribute){
		
		if(attribute instanceof CardLinkAttribute) {
			CardLinkAttribute attr = (CardLinkAttribute)attribute;
			if(attr.getLabelAttrId() != null
					&& !attr.getLabelAttrId().equals(Attribute.ID_NAME)) {
				words = StringUtils.collectionToCommaDelimitedString(attr.getLabelLinkedMap().values());
			} else {
				words = attr.getLinkedIds();
			}
		}

		if(attribute instanceof BackLinkAttribute) {
			BackLinkAttribute attr = (BackLinkAttribute)attribute;
			if(attr.getLabelAttrId() != null
					&& !attr.getLabelAttrId().equals(Attribute.ID_NAME)) {
				words = StringUtils.collectionToCommaDelimitedString(attr.getLabelLinkedMap().values());
			} else {
				words = attr.getLinkedIds();
			}
		}

		if(words==null || words.isEmpty()) {
			words="0";
		}
	}
}
