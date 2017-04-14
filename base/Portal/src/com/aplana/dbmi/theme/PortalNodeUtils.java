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
package com.aplana.dbmi.theme;

import java.util.Collection;
import java.util.Iterator;

import org.jboss.portal.api.node.PortalNode;

class PortalNodeUtils {
    
    public static String DEFAULT_OBJECT_PROPERTY = "portal.defaultObjectName";
    
	/**
	 * ���������, �������� �� ������ ���� (��������) ���� ���� Windows (��������)
	 * @param node ����, ������ ���� �����-���������
	 * @return
	 */
	public static boolean hasWindowChild(PortalNode node) {
		Iterator i = node.getChildren().iterator();
		while (i.hasNext()) {
			PortalNode child = (PortalNode)i.next();
			if (child.getType() == PortalNode.TYPE_WINDOW) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * ���� �������� �������� ��������, �� ���������� ���� ��������,
	 * � ��������� ������ ���������� ��������-�������, ���������� ��������.
	 */
	public static PortalNode getPageWithWindows(PortalNode page) {
		 if (hasWindowChild(page)) {
			 return page;
		 }		
		 Iterator i = page.getChildren().iterator();
		 while (i.hasNext()) {
			 PortalNode child = (PortalNode)i.next();
			 if (child.getType() == PortalNode.TYPE_PAGE) {
				 child = getPageWithWindows(child);
				 if (child != null) {
					 return child;
				 }
			 }			 
		 }
		 return null;
	}

	/**
	 * ���������� ������ �������� �������� ������� ��� �������� � ������� ��� ���������
	 * � ������, �������� � �������� {@code portal.defaultObjectName} ���������. ���� �������
	 * �� �������, ���������� ������ �������� ��������. ���� �������� �� ����� �������� �������,
	 * ������������ {@code null}. 
	 * 
	 * @param node
	 * @return
	 */
	public static PortalNode getDefaultSubPage(PortalNode node) {
	    if (node.getType() != PortalNode.TYPE_PAGE && node.getType() != PortalNode.TYPE_PORTAL) {
	        throw new IllegalArgumentException("node must be portal or page");
	    }
	    
	    String defaultObjectName = (String) node.getProperties().get(DEFAULT_OBJECT_PROPERTY);
	    	    
	    Collection children = node.getChildren();
	    
	    Iterator childrenIterator = children.iterator();

	    PortalNode page = null;

	    while (childrenIterator.hasNext()) {
            PortalNode child = (PortalNode)childrenIterator.next();
            if (child.getType() == PortalNode.TYPE_PAGE) {
                if (page == null) {
                    page = child;
                }
    	        if (child.getName().equals(defaultObjectName)) {
    	            page = child;
    	            break;
    	        }
            }
	    }
	    
	    return page;
	}
}
