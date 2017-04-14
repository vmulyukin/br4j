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
import java.util.HashMap;
import java.util.List;
import java.util.Map;



public class ItemTag {
	
	private String msg = null;
	private String tag= null;
	private Map<String, String> attrMap=null;
	private List<ItemTag> itemTags = null;
	
	public ItemTag(){
		attrMap= new HashMap<String, String>();	
		itemTags=new ArrayList<ItemTag>();
	}
	
	public void addItemTag(ItemTag itemTag){
		itemTags.add(itemTag);
	}
	
	
	
	

	public List<ItemTag> getItemTags() {
		return itemTags;
	}

	public void setItemTags(List<ItemTag> itemTags) {
		this.itemTags = itemTags;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public Map<String, String> getAttrMap() {
		return attrMap;
	}

	public void setAttrMap(Map<String, String> attr_map) {
		this.attrMap = attr_map;
	}
	
	public void involveParameters(ItemTag itemTag){
		this.tag=itemTag.tag;
		this.itemTags=itemTag.itemTags;
		this.msg=itemTag.msg;
		this.attrMap=itemTag.attrMap;
		
	}
	
	public String toString(){
		StringBuffer buffer = new StringBuffer();
		buffer.append(tag);
		return buffer.toString();
	}
	
	

}
