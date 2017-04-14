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

import java.util.List;

public interface PagedListInterface {
	
	public static final String PARAM_PAGE = "page";
	public static final String ATTR_PAGES = PagedList.class.getName();
	public static final String ATTR_PAGE_CURRENT = ATTR_PAGES + ".current";
	
	public void setList(List list);
	
	public void setPageSize(int pageSize);
	
	public void setTotalCount(long totalCount);
	
	public List getPage(int page);
	
	public int totalPages();
	
	public int pageSize();
	
	public int firstItem(int page);
	
	public int lastItem(int page);

}
