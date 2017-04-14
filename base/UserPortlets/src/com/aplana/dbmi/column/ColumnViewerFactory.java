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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aplana.dbmi.Portal;
import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.action.SearchResult.Column;
import com.aplana.dbmi.parser.ItemTag;
import com.aplana.dbmi.parser.XmlParse;
import com.aplana.dbmi.service.DataException;
/**
 * Factory which instantiates all column viewers and stores them into map 
 *
 */
public class ColumnViewerFactory {

	public static final String CONFIG_FILE_VIEWER_PREFIX = "dbmi/columnViewers/";
	public static final String CONFIG_FILE_SEARCH_PREFIX = "dbmi/searchSqlAttr/";

	private static final String PACKAGE_PREFIX="com.aplana.dbmi.column.";
	private static final String DEFAULT_VIEWER="DefaultColumnViewer";

	public static final String ATTR_CLASS="class";
	public static final String TAG_COLUMN="column";
	public static final String TAG_SEARCH="search_attr";
	public static final String ATTR_NAME="name";
	public static final String ATTR_VALUE="value";

	private Log logger = LogFactory.getLog(getClass());

	private static ColumnViewerFactory columnViewerFactory = null;
	private Map<String, MapViewer> mapViewers=null;

	public static ColumnViewerFactory getFactory(){
		if(columnViewerFactory==null){
			columnViewerFactory=new ColumnViewerFactory();
		}
		return columnViewerFactory;
	}

	private ColumnViewerFactory(){
		mapViewers= new HashMap<String, MapViewer>();
	}	

	public ColumnViewer getViewer(Column column){
		return createClass(column);
	}

	private ColumnViewer createClass(Column column) {
		String filename = (String) column.getParamByName(column.COLUNM_FILE_VIEWER);
		String key =column.getAttributeId().getId()+"_"+filename;
		MapViewer mapViewer = mapViewers.get(key);
		if(mapViewer==null){
			mapViewer = createMapViewerFromXml(filename);
			mapViewers.put(key, mapViewer);
		}
		String clazzName=null;
		try {
			clazzName=getClassName(mapViewer);
			ColumnViewer columnViewer = (ColumnViewer) Class.forName(clazzName).newInstance();
			for(ParamInfo paramInfo : mapViewer.getParamInfos()){
				columnViewer.setParameter(paramInfo.paramName, paramInfo);
			}
			return columnViewer;
		} catch (Exception e) {
			logger.error("Failed to initialize the class: "+clazzName, e);
			return null;
		}
	}

	private String getClassName(MapViewer mapViewer){
		String clazzName = mapViewer.getClassname();
		if(clazzName==null){
			clazzName=DEFAULT_VIEWER;
		}
		return PACKAGE_PREFIX+clazzName;
	}

	private MapViewer createMapViewerFromXml(String filename){
		ItemTag itemTag = new ItemTag();
		try {
			filename= Portal.getFactory().getConfigService().getConfigFileUrl(CONFIG_FILE_VIEWER_PREFIX+filename).getPath();
			XmlParse.parse(itemTag, filename);
		} catch (Exception e) {
			logger.error("Column viewer config file is not valid!", e);
		}
		return createMapViewerFromItemTag(itemTag);
	}

	private MapViewer createMapViewerFromItemTag(ItemTag itemTag){
		MapViewer mapViewer = new MapViewer();
		mapViewer.setClassname(itemTag.getAttrMap().get(ATTR_CLASS));
		List<ItemTag> itemTags = itemTag.getItemTags();
		if(itemTags!=null){
			mapViewer.setParamInfos(getParamFromItemTags(itemTags));
		}
 		return mapViewer;
	}

	private List<ParamInfo>  getParamFromItemTags(List<ItemTag> itemTags){
		ParamInfo paramInfo = null;
		List<ParamInfo> paramInfos = new ArrayList<ColumnViewerFactory.ParamInfo>();
		for (ItemTag itemTag : itemTags) {
			paramInfo = paramProcessing(itemTag);
			if(paramInfo!=null){
				paramInfos.add(paramInfo);
			}
		}
		return paramInfos;
	}

	private ParamInfo paramProcessing(ItemTag itemTag) {
		ParamInfo paramInfo = new ParamInfo();
		try{
			if(itemTag.getTag().equals(TAG_SEARCH)){
				paramInfo.paramName=itemTag.getAttrMap().get(ATTR_NAME);
				paramInfo.paramValue= createSearchFromParseXml(CONFIG_FILE_SEARCH_PREFIX+itemTag.getAttrMap().get(ATTR_VALUE));
			}else {
				paramInfo=universalParamProcessingColumnTag(itemTag);
			}
		}catch (Exception e) {
			logger.error("The parameter is incorrect!", e);
			return null;
		}
		return paramInfo;

	}

	private ParamInfo universalParamProcessingColumnTag(ItemTag itemTag){
		ParamInfo paramInfo = new ParamInfo();
		paramInfo.setParamName(itemTag.getAttrMap().get(ATTR_NAME));
		paramInfo.setParamValue(itemTag.getAttrMap().get(ATTR_VALUE));
		for (ItemTag subItemTag : itemTag.getItemTags()) {
			paramInfo.addAttr(subItemTag.getAttrMap().get(ATTR_NAME), subItemTag.getAttrMap().get(ATTR_VALUE));
		}
		return paramInfo;
	}

	private Search createSearchFromParseXml(String pathFile) throws IOException, DataException{
		Search search = new Search();
		final InputStream inputStream = Portal.getFactory().getConfigService().loadConfigFile(pathFile);
		try{
			search.initFromXml(inputStream);
		}finally{
			IOUtils.closeQuietly(inputStream);
		}
		return search;
	}

	public static class ParamInfo{
		private String paramName=null;
		private Object paramValue=null;
		private Map<String, String> mapAttrs= new HashMap<String, String>();

		public String getParamName() {
			return paramName;
		}
		public void setParamName(String paramName) {
			this.paramName = paramName;
		}
		public Object getParamValue() {
			return paramValue;
		}
		public void setParamValue(Object paramValue) {
			this.paramValue = paramValue;
		}
		public void addAttr(String name, String value){
			mapAttrs.put(name, value);
		}
		public String getAttrValue(String name){
			return mapAttrs.get(name);
		}
		public Map<String, String> getMapAttrs() {
			return mapAttrs;
		}
		public void setMapAttrs(Map<String, String> mapAttrs) {
			this.mapAttrs = mapAttrs;
		}
	}

	private class MapViewer{

		private List<ParamInfo> paramInfos = null;
		private String classname=null;

		public MapViewer(){
			reset();
		}
		public List<ParamInfo> getParamInfos() {
			return paramInfos;
		}
		public void setParamInfos(List<ParamInfo> paramInfos) {
			this.paramInfos = paramInfos;
		}
		public String getClassname() {
			return classname;
		}
		public void setClassname(String classname) {
			this.classname = classname;
		}
		public void reset(){
			paramInfos = new ArrayList<ColumnViewerFactory.ParamInfo>();
			classname=null;
		}
	}
}
