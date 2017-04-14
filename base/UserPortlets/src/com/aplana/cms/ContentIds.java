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
package com.aplana.cms;

import com.aplana.dbmi.model.AttributeBlock;
import com.aplana.dbmi.model.BackLinkAttribute;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.DateAttribute;
import com.aplana.dbmi.model.HtmlAttribute;
import com.aplana.dbmi.model.IntegerAttribute;
import com.aplana.dbmi.model.ListAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.ReferenceValue;
import com.aplana.dbmi.model.StringAttribute;
import com.aplana.dbmi.model.Template;

public interface ContentIds
{
	//public static final ObjectId TPL_CONTENT = new ObjectId(Template.class, 101);
	public static final ObjectId TPL_AREA = new ObjectId(Template.class, 101);
	public static final ObjectId TPL_VIEW = new ObjectId(Template.class, 102);
	public static final ObjectId TPL_NAVIGATION = new ObjectId(Template.class, 103);
	public static final ObjectId TPL_DOCLIST = new ObjectId(Template.class, 104);
	public static final ObjectId TPL_PAGES = new ObjectId(Template.class, 105);
	public static final ObjectId BLOCK_MATERIAL = new ObjectId(AttributeBlock.class, "CMSDOC");
	
	// Site area attributes
	public static final ObjectId ATTR_PARENT = new ObjectId(CardLinkAttribute.class, "CMSPARENT");
	public static final ObjectId ATTR_CHILDREN = new ObjectId(BackLinkAttribute.class, "CMSCHILD");
	public static final ObjectId ATTR_PAGEID = new ObjectId(StringAttribute.class, "CMSPAGE");
	public static final ObjectId ATTR_NAVIGATOR = new ObjectId(StringAttribute.class, "CMSNAVIGATOR");
	public static final ObjectId ATTR_DEFAULT_ITEM = new ObjectId(CardLinkAttribute.class, "CMSDEFITEM");
	public static final ObjectId ATTR_VIEW_LIST = new ObjectId(CardLinkAttribute.class, "CMSVIEWS");
	public static final ObjectId ATTR_ORDER = new ObjectId(IntegerAttribute.class, "CMSORDER");
	public static final ObjectId ATTR_LINKED = new ObjectId(BackLinkAttribute.class, "CMSLINKED");
	public static final ObjectId ATTR_SEARCH = new ObjectId(HtmlAttribute.class, "CMSSEARCH");
	public static final ObjectId ATTR_AUTO_ITEM = new ObjectId(ListAttribute.class, "CMSAUTOITEM");
	
	// Presentation attributes
	public static final ObjectId ATTR_TEMPLATEID = new ObjectId(StringAttribute.class, "CMSTEMPL");
	public static final ObjectId ATTR_HTML = new ObjectId(HtmlAttribute.class, "CMSHTML");
	
	// Navigator attributes
	public static final ObjectId ATTR_ROOT_TYPE = new ObjectId(ListAttribute.class, "CMSSTART");
	public static final ObjectId ATTR_ROOT_AREA = new ObjectId(CardLinkAttribute.class, "CMSROOT");
	//public static final ObjectId ATTR_LEVELS = new ObjectId(IntegerAttribute.class, "CMSLEVELS");
	public static final ObjectId ATTR_LEVELS_UP = new ObjectId(IntegerAttribute.class, "CMSLEVUP");
	public static final ObjectId ATTR_LEVELS_OPEN = new ObjectId(IntegerAttribute.class, "CMSLEVOPEN");
	public static final ObjectId ATTR_LEVELS_DOWN = new ObjectId(IntegerAttribute.class, "CMSLEVDOWN");
	public static final ObjectId ATTR_CURRENT = new ObjectId(ListAttribute.class, "CMSCURR");
	public static final ObjectId ATTR_HTML_HEADER = new ObjectId(HtmlAttribute.class, "CMSHEAD");
	public static final ObjectId ATTR_HTML_FOOTER = new ObjectId(HtmlAttribute.class, "CMSFOOT");
	public static final ObjectId ATTR_HTML_ITEM = new ObjectId(HtmlAttribute.class, "CMSITEM");
	public static final ObjectId ATTR_HTML_SEL_ITEM = new ObjectId(HtmlAttribute.class, "CMSSELITEM");
	public static final ObjectId ATTR_HTML_SEPARATOR = new ObjectId(HtmlAttribute.class, "CMSSEP");
	public static final ObjectId ATTR_HTML_EMPTY = new ObjectId(HtmlAttribute.class, "CMSEMPTY");
	public static final ObjectId ATTR_HTML_LEVEL_START = new ObjectId(HtmlAttribute.class, "CMSLEVSTART");
	public static final ObjectId ATTR_HTML_LEVEL_END = new ObjectId(HtmlAttribute.class, "CMSLEVEND");
	public static final ObjectId ATTR_HTML_OPEN_ITEM = new ObjectId(HtmlAttribute.class, "CMSOPEN");
	
	// Pages attributes
	public static final ObjectId ATTR_PAGE_SIZE = new ObjectId(IntegerAttribute.class, "CMSPGSIZE");
	//public static final ObjectId ATTR_PAGER = new ObjectId(CardLinkAttribute.class, "CMSPAGER");
	
	// Pager attributes
	public static final ObjectId ATTR_PAGES_BEGIN = new ObjectId(IntegerAttribute.class, "CMSPGSTART");
	public static final ObjectId ATTR_PAGES_END = new ObjectId(IntegerAttribute.class, "CMSPGEND");
	public static final ObjectId ATTR_PAGES_CURR = new ObjectId(IntegerAttribute.class, "CMSPGARND");
	public static final ObjectId ATTR_HTML_PAGE = new ObjectId(HtmlAttribute.class, "CMSPGPAGE");
	public static final ObjectId ATTR_HTML_PAGE_CURR = new ObjectId(HtmlAttribute.class, "CMSPGCURR");
	//public static final ObjectId ATTR_HTML_PAGE_SEP = new ObjectId(TextAttribute.class, "CMSPGSEP");
	public static final ObjectId ATTR_HTML_PAGE_SKIP = new ObjectId(HtmlAttribute.class, "CMSPGSKIP");
	public static final ObjectId ATTR_HTML_PAGE_OPEN = new ObjectId(HtmlAttribute.class, "CMSPGOPEN");
	public static final ObjectId ATTR_HTML_PAGE_CLOSE = new ObjectId(HtmlAttribute.class, "CMSPGCLOSE");
	public static final ObjectId ATTR_HTML_PAGE_NOOPEN = new ObjectId(HtmlAttribute.class, "CMSPGNOOPN");
	public static final ObjectId ATTR_HTML_PAGE_NOCLOSE = new ObjectId(HtmlAttribute.class, "CMSPGNOCLS");
	
	// Material attributes
	public static final ObjectId ATTR_AREA = new ObjectId(CardLinkAttribute.class, "CMSAREA");
	public static final ObjectId ATTR_PUBLICATION_DATE = new ObjectId(DateAttribute.class, "CMSPUB");
	public static final ObjectId ATTR_EXPIRATION_DATE = new ObjectId(DateAttribute.class, "CMSEXP");
	
	// Values of ATTR_ROOT_TYPE attribute
	public static final ObjectId VAL_TYPE_CURRENT = new ObjectId(ReferenceValue.class, 701);
	public static final ObjectId VAL_TYPE_ROOT = new ObjectId(ReferenceValue.class, 702);
	public static final ObjectId VAL_TYPE_SELECTED = new ObjectId(ReferenceValue.class, 703);
	
	// Values of ATTR_CURRENT attribute
	public static final ObjectId VAL_CURR_NOSHOW = new ObjectId(ReferenceValue.class, 711);
	public static final ObjectId VAL_CURR_ONLY = new ObjectId(ReferenceValue.class, 712);
	public static final ObjectId VAL_CURR_CHILDREN = new ObjectId(ReferenceValue.class, 713);
	
	// Values of ATTR_AUTO_ITEM attribute
	public static final ObjectId VAL_AUTO_NONE = new ObjectId(ReferenceValue.class, 721);
	public static final ObjectId VAL_AUTO_LINKED = new ObjectId(ReferenceValue.class, 722);
	public static final ObjectId VAL_AUTO_SEARCH = new ObjectId(ReferenceValue.class, 723);
}
