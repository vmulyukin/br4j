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
package org.displaytag.util;

import javax.portlet.PortletRequest;
import javax.portlet.PortletSession;
import javax.portlet.RenderRequest;

import org.displaytag.tags.TableTagParameters;

/**
 * Saves sorting state of a displaytag.
 * 
 * @author Avorotnikov
 */
public class SortingState {
    /**
     * Ordinal number of sort column.
     */
    private int sortColumn;
    
    /**
     * Sort order. Descending is 1, Ascending is 2. See displaytag documentation.
     */
    private int sortOrder;
    
    /**
     * Current page number.
     */
    private int pageNum;

    public final static String SORTING_STATE_SESSION_KEY = "SortingState";
    
    public int getPageNum() {
        return pageNum;
    }

    public void setPageNum(int pageNum) {
        this.pageNum = pageNum;
    }

    public int getSortColumn() {
        return sortColumn;
    }

    public void setSortColumn(int sortColumn) {
        this.sortColumn = sortColumn;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }
    
    public void merge(SortingState sortingState) {
        if (sortingState.sortColumn != -1) {
            this.sortColumn = sortingState.sortColumn;
        }
        if (sortingState.sortOrder != 0) {
            this.sortOrder = sortingState.sortOrder;
        }
        if (sortingState.pageNum != 0) {
            this.pageNum = sortingState.pageNum;
        }
    }
    
    public static void configureSortingState(RenderRequest request, String tableId) {
        SortingState requestSortingState = extractSortingStateFromRequest(request, tableId);
        if (requestSortingState != null) {
            PortletSession session = request.getPortletSession();
            SortingState sessionSortingState = loadSortingStateFromSession(tableId, session);
            if (sessionSortingState != null) {
                sessionSortingState.merge(requestSortingState);
            } else {
                saveSortingStateToSession(requestSortingState, tableId, session);
            }
        }
    }

    /**
     * Get display tag sorting state from portlet request. 
     * 
     * @param request Portlet request
     * @param tableId Table Id
     * @return SortingState
     */
    public static SortingState extractSortingStateFromRequest(PortletRequest request, String tableId) {
        SortingState sortingState = null;
        ParamEncoder paramEncoder = new ParamEncoder(tableId);
        String sortColumn = request.getParameter(paramEncoder.encodeParameterName(TableTagParameters.PARAMETER_SORT));
        String sortOrder = request.getParameter(paramEncoder.encodeParameterName(TableTagParameters.PARAMETER_ORDER));
        String pageNum = request.getParameter(paramEncoder.encodeParameterName(TableTagParameters.PARAMETER_PAGE));
        if (sortColumn != null || sortOrder != null || pageNum != null) {
            sortingState = new SortingState();
            sortingState.setSortColumn(sortColumn != null ? Integer.parseInt(sortColumn) : -1);
            sortingState.setSortOrder(sortOrder != null ? Integer.parseInt(sortOrder) : 0);
            sortingState.setPageNum(pageNum != null ? Integer.parseInt(pageNum) : 0);
        }
        return sortingState; 
    }
    
    /**
     * Doesn't work yet.
     * 
     * @param sortingState
     * @param request
     * @param tableId
     *
    public static void setSortingState(SortingState sortingState, PortletRequest request, String tableId) {
        if (sortingState != null) {
            ParamEncoder paramEncoder = new ParamEncoder(tableId);
            if (sortingState.getSortColumn() != null) {
                //request.
            }
        }
    }
*/
    
    /**
     * Saves the given sorting state to portlet session;
     * 
     * @param sortingState
     * @param tableId
     * @param session
     */
    public static void saveSortingStateToSession(SortingState sortingState, String tableId, PortletSession session) {
        session.setAttribute(tableId + SORTING_STATE_SESSION_KEY, sortingState, PortletSession.PORTLET_SCOPE);
    }

    /**
     * Loads a sorting state from portlet session.
     * 
     * @param tableId
     * @param session
     * @return
     */
    public static SortingState loadSortingStateFromSession(String tableId, PortletSession session) {
        return (SortingState) session.getAttribute(tableId + SORTING_STATE_SESSION_KEY, PortletSession.PORTLET_SCOPE);
    }
}
