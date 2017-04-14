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

import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class PagedList implements PagedListInterface
{	
	protected Log logger = LogFactory.getLog(getClass());
	private List baseList;
	private int pageSize;

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public void setList(List baseList) {
		this.baseList = baseList;
	}

	public List getPage(int page)
	{
		//if (pageSize == 0)
		//	return baseList;
		if (page < 1 || page > totalPages()) {
			logger.warn("Page " + page + " not exists in list");
			return Collections.EMPTY_LIST;
		}
		return baseList.subList((page-1) * pageSize, Math.min(page * pageSize, baseList.size()));
	}
	
	public int totalPages()
	{
		//if (pageSize == 0)
		//	return 1;
		return (baseList.size() + pageSize - 1) / pageSize;
	}
	
	public int pageSize()
	{
		return pageSize;
	}
	
	public int firstItem(int page)
	{
		return ((page-1) * pageSize) + 1;
	}
	
	public int lastItem(int page)
	{
		return Math.min(page * pageSize, baseList.size());
	}

	public void setTotalCount(long totalCount) {
	}
}
