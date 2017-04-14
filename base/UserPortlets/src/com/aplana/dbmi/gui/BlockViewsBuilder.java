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
package com.aplana.dbmi.gui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import javax.portlet.PortletRequest;

import com.aplana.dbmi.model.AttributeViewParam;
import com.aplana.dbmi.model.BlockViewParam;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.TabBlockViewParam;
import com.aplana.dbmi.model.TemplateBlock;

public class BlockViewsBuilder {
	private Map<Object,TabBlockViewParam> tbvps = new LinkedHashMap<Object,TabBlockViewParam>();
	private Map<Object, List<BlockView>> bvsAll;
	
	private List<TemplateBlock> tbs;
	private List<BlockViewParam> bvps;
	
	private List<TemplateBlock> tbs_s;
	private List<BlockViewParam> bvps_s;
	
	private List<AttributeViewParam> avps;
	
	private ObjectId cardState;
	private PortletRequest request;
	private ObjectId activeTabId;

	public Map<Object, List<BlockView>> build(){
		if (tbvps == null || bvps == null || tbs == null || activeTabId == null)
			return null;

		initSearchData();
		Map<Object, List<BlockView>> bvs = anotherBuild();
		clearSearchData();
		return bvs;
	}

	private Map<Object, List<BlockView>> anotherBuild(){
		Map<Object, List<BlockView>> bvs = new HashMap<Object, List<BlockView>>();
		bvs.put(activeTabId.getId(), buildSelectedTab(activeTabId));
		return bvs;
	}
	
	private List<BlockView> buildSelectedTab(ObjectId tabId){
		List<BlockView> bvs = new ArrayList<BlockView>();
		for (ListIterator<TemplateBlock> i = tbs.listIterator(); i.hasNext();) {
			TemplateBlock tb = i.next();
			BlockView bv;
			TabBlockViewParam tbvp = tbvps.get(tb.getId().getId());
			boolean onThisTab = tbvp != null;
			if (onThisTab){
				bv = new BlockView(tbvp);
				FindAndApplyBlockViewParam(bv);
				if (FindAndApplyTemplateBlock(bv)){
					bv.setAttributeViewParams(avps, request);
					if (bv.getAttributeViews().size() > 0) {
						bv.initAttributeEditors(request);
						bvs.add(bv);
					}
				}
			}
		}
		return bvs;
	}
	
	public List<BlockView> getActiveTab() {
		if (bvsAll == null || activeTabId == null)
			return null;
		List<BlockView> list = bvsAll.get(activeTabId.getId());
		if (list == null) {
			initSearchData();
			list = buildSelectedTab(activeTabId);
			bvsAll.put(activeTabId.getId(), list);
			clearSearchData();
		}
		return list;
	}

	private void initSearchData(){
		tbs_s = new ArrayList<TemplateBlock>(tbs);
		bvps_s = new ArrayList<BlockViewParam>(bvps);
	}

	private void clearSearchData(){
		tbs_s = null;
		bvps_s = null;
	}

	private boolean FindAndApplyTemplateBlock(BlockView bv){
		if (tbs_s == null)
			return false;
		for (ListIterator<TemplateBlock> j = tbs_s.listIterator(); j.hasNext(); ){
			TemplateBlock tb = j.next();
			if (bv.getBlock().getId().equals(tb.getId().getId())){
				bv.importFrom(tb);
				//j.remove();
				return true;
			}
		}
		return false;
	}

	private boolean FindAndApplyBlockViewParam(BlockView bv){
		if (bvps_s == null)
			return false;
		for (ListIterator<BlockViewParam> j = bvps_s.listIterator(); j.hasNext(); ){
			BlockViewParam bvp = j.next();
			if (bv.getBlock().getId().equals(bvp.getBlock().getId())){
				if (!(cardState == null)){
					if (cardState.getId().equals(bvp.getCardStatus().getId())){
						bv.importFrom(bvp);
						//j.remove();
						return true;
					};
				}else{
					bv.importFrom(bvp);
					//j.remove();
					return true;
				}
			}
		}
		return false;
	}

	public Collection<TabBlockViewParam> getTabBlockViewParams() {
		return tbvps.values();
	}

	public void setTabBlockViewParams(Collection<TabBlockViewParam> tt) {
		this.tbvps.clear();
		if (tbvps == null) return;
			for (TabBlockViewParam tbvParam : tt) {
				tbvps.put(tbvParam.getId().getId(), tbvParam);
			}
	}

	public void setActiveTabId(ObjectId activeTabId) {
		this.activeTabId = activeTabId;
	}
	
	public ObjectId getActiveTabId() {
		return activeTabId;
	}
	
	public Collection<BlockViewParam> getBlockViewParams() {
		return bvps;
	}

	public void setBlockViewParams(Collection<BlockViewParam> bvp) {
		this.bvps = new ArrayList<BlockViewParam>(bvp);
	}

	public Collection<TemplateBlock> getTemplateBlocks() {
		return tbs;
	}

	public void setTemplateBlocks(Collection<TemplateBlock> tb) {
		this.tbs = new ArrayList<TemplateBlock>(tb);
	}

	public ObjectId getCardState() {
		return cardState;
	}

	public void setCardState(ObjectId cardState) {
		this.cardState = cardState;
	}

	public List<AttributeViewParam> getAttributeViewParams() {
		return avps;
	}

	public void setAttributeViewParams(Collection<AttributeViewParam> avps) {
		this.avps = new ArrayList<AttributeViewParam>(avps);
	}

	public PortletRequest getRequest() {
		return request;
	}

	public void setRequest(PortletRequest request) {
		this.request = request;
	}
	
	public void setBvsAll(Map<Object, List<BlockView>> bvsAll) {
		this.bvsAll = bvsAll;
	}
	
	public Map<Object, List<BlockView>> getBvsAll() {
		return bvsAll;
	}

}