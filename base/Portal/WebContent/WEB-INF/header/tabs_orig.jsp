<%--

      Licensed to the Apache Software Foundation (ASF) under one or more
      contributor license agreements.  See the NOTICE file distributed with
      this work for additional information regarding copyright ownership.
      The ASF licenses this file to you under the Apache License, Version 2.0
      (the "License"); you may not use this file except in compliance with
      the License.  You may obtain a copy of the License at

          http://www.apache.org/licenses/LICENSE-2.0

      Unless required by applicable law or agreed to in writing, software
      distributed under the License is distributed on an "AS IS" BASIS,
      WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
      See the License for the specific language governing permissions and
      limitations under the License.

--%>
<%@ page import="java.util.Iterator" %>
<%@ page import="org.jboss.portal.api.node.PortalNode" %>
<%@ page import="java.util.Locale" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="org.jboss.portal.api.PortalRuntimeContext" %>

<%
   PortalNode root = (PortalNode)request.getAttribute("org.jboss.portal.api.PORTAL_NODE");
   PortalNode portal = root;
   PortalNode mainPage = portal;

   while (portal.getType() != PortalNode.TYPE_PORTAL)
   {
      mainPage = portal;
      portal = portal.getParent();
   }

   PortalRuntimeContext context = (PortalRuntimeContext)request.getAttribute("org.jboss.portal.api.PORTAL_RUNTIME_CONTEXT");

   // Get a locale
   Locale locale = request.getLocale();
   if (locale == null)
   {
      locale = Locale.getDefault();
   }
%>

<ul id="tabsHeader">
   <%
      ArrayList tmp = new ArrayList(10);
      Iterator childrenIt = portal.getChildren().iterator();
      while (childrenIt.hasNext())
      {
         PortalNode child = (PortalNode)childrenIt.next();

         // Get the list of child pages
         tmp.clear();
         for (Iterator i = child.getChildren().iterator(); i.hasNext();)
         {
            PortalNode childChild = (PortalNode)i.next();
            if (childChild.getType() == PortalNode.TYPE_PAGE)
            {
               tmp.add(childChild);
            }
         }
   %>
   <li <%
      if (child == mainPage)
      {
         out.println(" id=\"current\"");
      } %> onmouseover="this.className='hoverOn'"
                                                                    onmouseout="this.className='hoverOff'"><a
      href="<%= child.createURL(context) %>"><%= child.getDisplayName(locale) %><%
      if (tmp.size() == 0)
      {
    	  out.write("</a>");
      }
      else
      {
      %>
      <!--[if IE 7]><!--></a><!--<![endif]-->
      <!--[if lte IE 6]>&nbsp;&nbsp;&nbsp;<table><tr><td><![endif]-->
      <ul>
         <%
            for (Iterator j = tmp.iterator(); j.hasNext();)
            {
               PortalNode childChild = (PortalNode)j.next();
         %>
         <li><a href='<%= childChild.createURL(context) %>'><%= childChild.getDisplayName(locale) %>
         </a></li>
         <%
            }
         %>
      </ul>
      <!--[if lte IE 6]></td></tr></table></a><![endif]-->
      <%
         }
      %>
   </li>
   <%
      }

   %>
</ul>
