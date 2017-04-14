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
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import javax.portlet.PortletRequest;

import com.aplana.dbmi.card.CardPortlet;
import com.aplana.dbmi.card.CardPortletSessionBean;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.AttributeBlock;
import com.aplana.dbmi.model.AttributeViewParam;
import com.aplana.dbmi.model.BlockViewParam;
import com.aplana.dbmi.model.ContextProvider;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Tab;
import com.aplana.dbmi.model.TabBlockViewParam;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.model.TemplateBlock;


public class BlockView {

	private ObjectId tab;
	private ObjectId template;
	private ObjectId block;
	private Object layout;
	private String nameRu;
	private String nameEn;
	private int desiredState;

	private int currentState = BlockViewParam.COLLAPSE;
	private List<AttributeView> attrViews = new ArrayList<AttributeView>();

	public BlockView(){
	}

	public BlockView(TabBlockViewParam tbvp){
		if (tbvp == null)
			return;
		tab = tbvp.getTab();
		layout = tbvp.getLayout();
		block = tbvp.getId();
	}

	public ObjectId getTab() {
		return tab;
	}

	public void setTab(long tab) {
		this.tab = new ObjectId(Tab.class, tab);
	}

	public ObjectId getTemplate() {
		return template;
	}

	public void setTemplate(long template) {
		this.template = new ObjectId(Template.class, template);
	}

	public void setTemplate(ObjectId template) {
		this.template = template;
	}

	public ObjectId getBlock() {
		return block;
	}

	public void setBlock(String block) {
		this.tab = (block == null ? null : new ObjectId(AttributeBlock.class, block));
	}

	public Object getLayout() {
		return layout;
	}

	public void setLayout(Object layout) {
		this.layout = layout;
	}

	public int getCurrentState() {
		return currentState;
	}

	public List<AttributeView> getAttributeViews() {
		return attrViews;
	}

	public void setAttributes(Collection<?> attributes){
		attrViews.clear();
		if (attributes ==  null)
			return;

		for (Iterator<?> i = attributes.iterator(); i.hasNext();) {
			try {
				AttributeView av = new AttributeView((Attribute) i.next());
				attrViews.add(av);
			} catch (NullPointerException e) {}
		}

		switch (desiredState){
			case BlockViewParam.COLLAPSE_IF_EMPTY: {
				boolean empty = true;
				Iterator<?> iter = attributes.iterator();
				while (iter.hasNext()) {
					if (!((Attribute)iter.next()).isEmpty()) {
						empty = false;
						break;
					}
				}
				if (empty)
					currentState = BlockViewParam.COLLAPSE;
				else
					currentState = BlockViewParam.OPEN;
				break;
			}
			case BlockViewParam.COLLAPSE: {
				currentState = BlockViewParam.COLLAPSE;
				break;
			}
			case BlockViewParam.OPEN: {
				currentState = BlockViewParam.OPEN;
				break;
			}
			default:
				currentState = BlockViewParam.COLLAPSE;
		}
	}

	public void importFrom(TemplateBlock tb){
		if (tb == null)
			return;
		setTemplate(tb.getTemplate());
		setNameRu(tb.getNameRu());
		setNameEn(tb.getNameEn());
		setAttributes(tb.getAttributes());
	}

	public void importFrom(BlockViewParam bvp){
		if (bvp == null)
			return;
		setDesiredState(bvp.getStateBlock());
	}

	public void setAttributeViewParams(Collection<AttributeViewParam> avps, PortletRequest request){
		if (avps == null)
			return;
		CardPortletSessionBean sessionBean = (CardPortletSessionBean)request.getPortletSession().getAttribute(CardPortlet.SESSION_BEAN);
		ArrayList<AttributeViewParam> avps_s = new ArrayList<AttributeViewParam>(avps);
		for (ListIterator<AttributeView> i = attrViews.listIterator(); i.hasNext();) {
			AttributeView av = (AttributeView) i.next();
			for (Iterator<AttributeViewParam> j = avps_s.iterator(); j.hasNext();) {
				AttributeViewParam avp = j.next();
				if (av.getId().getId().equals(avp.getAttribute().getId()))
					av.applyViewParameters(avp);
			}
			if (!av.isVisible())
				i.remove();
		}
	}

	public void initAttributeEditors(PortletRequest request){
		for (Iterator<AttributeView> i = attrViews.iterator(); i.hasNext();) {
			AttributeView av = (AttributeView)i.next();
			try {
				av.initEditor(request);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public String getNameRu() {
		return nameRu;
	}

	public void setNameRu(String nameRu) {
		this.nameRu = nameRu;
	}

	public String getNameEn() {
		return nameEn;
	}

	public void setNameEn(String nameEn) {
		this.nameEn = nameEn;
	}

	public String getName() {
		return ContextProvider.getContext().getLocaleString(nameRu, nameEn);
	}

	public int getDesiredState() {
		return desiredState;
	}

	public void setDesiredState(int desiredState) {
		this.desiredState = desiredState;
	}

	public void setBlock(ObjectId block) {
		this.block = block;
	}
}