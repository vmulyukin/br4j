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
/*
    Copyright 2004 Jenkov Development

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/



package com.jenkov.prizetags.navigator.itf;


/**
 * Represents a link inside an Navigator instance. Links can be
 * added or removed from an Navigator instance. The links in
 * an Navigator are displayed to the user, to help the user navigate
 * the web site.
 *
 * @see INavigator
 * @author Jakob Jenkov - Copyright 2005 Jenkov Development
 */
public interface ILink {

    /**
     * Returns the id of this link. The code is used to uniquely identify
     * this link inside an Navigator. That way the link can be removed,
     * or obtained,
     * by passing the id of ILink the to remove to the Navigator instance.
     * @return The id of this link.
     */
    public String getId();

    /**
     * Sets the id of this link. The code is used to uniquely identify
     * this link inside an Navigator. That way the link can be removed, or
     * obtained,
     * by passing the id of ILink the to remove to the Navigator instance.
     * @param id The id of this link.
     */
    public void   setId(String id);

    /**
     * Returns the text of this link. This is the text that is displayed
     * in the link in the navigator on the page. If you don't want to
     * hard code the texts of your links inside the application, don't
     * set this. Use the &lt;navigator:linkMatch id="linkId"...&gt;
     * tags to translate from the link id to a link text, inside the JSP page.
     * This can be useful in case you want to do internationalization in the JSP
     * pages. Frameworks like Struts provides nice taglibs for internationalization.
     * @return The text of this link if any.
     */
    public String getText();

    /**
     * Returns the text of this link. This is the text that is displayed
     * in the link in the navigator on the page. If you don't want to
     * hard code the texts of your links inside the application, don't
     * set this. Use the &lt;navigator:linkMatch id="linkId"...&gt;
     * tags to translate from the link id to a link text, inside the JSP page.
     * This can be useful in case you want to do internationalization in the JSP
     * pages. Frameworks like Struts provides nice taglibs for internationalization.
     * @param text The text of this link.
     */
    public void   setText(String text);

    /**
     * Returns the URL this link points to. This URL can be relative or absolute.
     * If you don't want to
     * hard code the urls of your links inside the application, don't
     * set this. Use the &lt;navigator:linkMatch id="linkId"...&gt;
     * tags to translate from the link id to a link url, inside the JSP page.
     * @return The url of this link, if any.
     */
    public String getUrl();

    /**
     * Sets the URL this link points to. This URL can be relative or absolute.
     * If you don't want to
     * hard code the urls of your links inside the application, don't
     * set this. Use the &lt;navigator:linkMatch id="linkId"...&gt;
     * tags to translate from the link id to a link url, inside the JSP page.
     * @param url The url of this link.
     */
    public void setUrl(String url);
}
