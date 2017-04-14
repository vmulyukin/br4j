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



package com.jenkov.prizetags.util;

import javax.servlet.jsp.PageContext;


public class ServletContextUtil {

    public static String ICON_DIR      = "prizetags.icon.dir";
    public static String ON_CLICK      = "prizetags.icon.onClick";
    public static String ON_DBL_CLICK  = "prizetags.icon.onDblClick";
    public static String ON_KEY_DOWN   = "prizetags.icon.onKeyDown";
    public static String ON_KEY_PRESSED= "prizetags.icon.onKeyPressed";
    public static String ON_MOUSE_DOWN = "prizetags.icon.onMouseDown";
    public static String ON_MOUSE_MOVE = "prizetags.icon.onMouseMove";
    public static String ON_MOUSE_OVER = "prizetags.icon.onMouseOver";


    public static String getIconDir(PageContext pageContext){
        return getContextParameter(pageContext, ICON_DIR);
    }

    public static String getOnClick(PageContext pageContext){
        return getContextParameter(pageContext, ON_CLICK);
    }

    public static String getOnDblClick(PageContext pageContext){
        return getContextParameter(pageContext, ON_DBL_CLICK);
    }

    public static String getOnKeyDown(PageContext pageContext){
        return getContextParameter(pageContext, ON_KEY_DOWN);
    }

    public static String getOnKeyPressed(PageContext pageContext){
        return getContextParameter(pageContext, ON_KEY_PRESSED);
    }

    public static String getOnMouseDown(PageContext pageContext){
        return getContextParameter(pageContext, ON_MOUSE_DOWN);
    }

    public static String getOnMouseMove(PageContext pageContext){
        return getContextParameter(pageContext, ON_MOUSE_MOVE);
    }

    public static String getOnMouseOver(PageContext pageContext){
        return getContextParameter(pageContext, ON_MOUSE_OVER);
    }


    public static String getContextParameter(PageContext pageContext, String parameterName){
        return pageContext.getServletContext().getInitParameter(parameterName);
    }

}
