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
package com.aplana.dbmi.admin;

import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.springframework.web.portlet.HandlerExceptionResolver;
import org.springframework.web.portlet.ModelAndView;

public class InViewExceptionResolver implements HandlerExceptionResolver {

    public ModelAndView resolveException(RenderRequest request, RenderResponse response, Object handler, Exception e) {
        System.out.println("InViewExceptionResolver.resolveException: request=" + request);
        System.out.println("InViewExceptionResolver.resolveException: response=" + response);
        System.out.println("InViewExceptionResolver.resolveException: handler=" + handler);
        System.out.println("InViewExceptionResolver.resolveException: e=" + e);
        return new ModelAndView("templates");
    }

}
