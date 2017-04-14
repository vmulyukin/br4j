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

import java.util.ArrayList;
import java.util.List;

import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.column.ColumnViewerFactory.ParamInfo;
import com.aplana.web.tag.util.StringUtils;

public class DefaultColumnViewer extends CommonColumnViewer {

	private List<ParamInfo> attrCols = new ArrayList<ParamInfo>();

	private StringBuilder htmlBufCode = new StringBuilder();

	public String getHtmlCode() {
		return htmlBufCode.toString();
	}

	public void setParameter(String name, Object value) {
		if(name.equals(PARAM_COLUMN)){
			attrCols.add((ParamInfo)value);
		}else if(name.equals(PARAM_SEARCH)){
			search=(Search)((ParamInfo)value).getParamValue();
		}
	}

	protected void createContent(List<ArrayList<Object>> columnData){
		htmlBufCode = new StringBuilder();
		if(columnData.isEmpty()){
			return;
		}
		htmlBufCode.append("<table class=\"res\">");

		for (List<Object> columnValue : columnData) {
			htmlBufCode.append("<tr class=\"even\">");
			for (ParamInfo paramInfo: attrCols) {
				htmlBufCode.append(createColumnHtml(paramInfo, columnValue));
			}
			htmlBufCode.append("</tr>");
		}
		htmlBufCode.append("</table>");	

	}

	protected String createColumnHtml(ParamInfo paramInfo, List<Object> columnValue){
		StringBuilder columnHtmlBuf=new StringBuilder();
		String[] colBehavior =paramInfo.getAttrValue(PARAM_COLS).split(DEFAULT_SPLIT);	
		int colNumInt=0;
		String value = null;
		for(String colNum : colBehavior){
			colNumInt = Integer.valueOf(colNum);
			if(colNumInt==0){
				break;
			}
			value = getColumnData(columnValue, paramInfo, colNumInt);
			
			if(value==null || value.equals("null") || value.isEmpty()){
					continue;
			}
			columnHtmlBuf.append("<td>")
						 .append(StringUtils.stringAddPrefixSuffix(value, paramInfo.getAttrValue(PARAM_PREFIX), paramInfo.getAttrValue(PARAM_SUFFIX)))
						 .append("</td>");
			break;
		}	
		return columnHtmlBuf.toString();

	}

	protected String getColumnData(List<Object> columnValue, ParamInfo paramInfo, int colNumInt){
		 String value=String.valueOf(columnValue.get(Math.abs(colNumInt)+1));
		 if(colNumInt<0 && value.equals("null")){
			 value=paramInfo.getAttrValue(PARAM_DEFAULT_VALUE);
		 }else if(colNumInt<0 && !value.equals("null")){
			 value=null;
		 }
		 return value;
	}

	protected Search buildSearch(){
		if(search==null){
			search=new Search();
		}
		search.setWords(words);
		return search;
	}

}
