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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ResourceBundle;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletRequest;
import javax.portlet.PortletSession;

import org.springframework.beans.BeanUtils;
import org.springframework.validation.BindException;
import org.springframework.web.portlet.mvc.SimpleFormController;

import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.ContextProvider;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.model.TemplateBlock;
import com.aplana.dbmi.service.AsyncDataServiceBean;
import com.aplana.dbmi.service.AsyncDataServiceBean.ExecuteOption;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.PortletUtil;
import com.aplana.dbmi.service.ServiceException;
import com.aplana.dbmi.service.UserPrincipal;

public class TemplateAttributesPortlet extends SimpleFormController {
    private ResourceBundle messages;

    protected Object formBackingObject(PortletRequest request) throws Exception {
        ContextProvider.getContext().setLocale(request.getLocale());

        WebTemplateAttributesBean templateAttributesBean = (WebTemplateAttributesBean) super.formBackingObject(request);
        templateAttributesBean.setTemplateAttributesInRezults(new HashMap());
        templateAttributesBean.setTemplateAttributesNumber(new HashMap());
        templateAttributesBean.setTemplateAttributesWidth(new HashMap());
        AsyncDataServiceBean dataService = PortletUtil.createService(request);
        try {
            WebTemplateBean templateBean = (WebTemplateBean) request.getPortletSession().getAttribute("com.aplana.dbmi.admin.TemplatesPortlet.FORM.templateBean");
            templateBean.setDataService(dataService);
            if (request.getParameter("template_id") != null && !"".equals(request.getParameter("template_id"))) {
                templateBean.setNameRu(request.getParameter("nameRu"));
                templateBean.setNameEn(request.getParameter("nameEn"));
                saveTemplate(templateBean, dataService, request);
            }
            BeanUtils.copyProperties(templateBean, templateAttributesBean, new String[] { "locker", "lockTime" });
            for (Iterator itBlock = templateAttributesBean.getBlocks().iterator(); itBlock.hasNext();) {
                TemplateBlock block = (TemplateBlock) itBlock.next();
                for (Iterator itAttribute = block.getAttributes().iterator(); itAttribute.hasNext();) {
                    Attribute attribute = (Attribute) itAttribute.next();
                    Object attributeId = attribute.getId().getId();

                    if (attribute.isSearchShow()) {
                        templateAttributesBean.getTemplateAttributesInRezults().put(attributeId, attributeId);
                    }
                    
                    if (attribute.getColumnWidth() > 0) {
                        templateAttributesBean.getTemplateAttributesWidth().put(attributeId, new Integer(attribute.getColumnWidth()));
                    }

                    if (attribute.getSearchOrder() > 0) {
                        templateAttributesBean.getTemplateAttributesNumber().put(attributeId, new Integer(attribute.getSearchOrder()));
                    }

                }

            }
        } catch (Exception e) {
            e.printStackTrace();
            templateAttributesBean.setMessage(e.getMessage());
        }
        return templateAttributesBean;

    }

    public void onSubmitAction(ActionRequest request, ActionResponse response, Object command, BindException errors) throws Exception {
        ContextProvider.getContext().setLocale(request.getLocale());
        messages = ResourceBundle.getBundle("templates", request.getLocale());

        WebTemplateAttributesBean templateAttributesBean = (WebTemplateAttributesBean) command;
        WebTemplateBean templateBean = (WebTemplateBean) request.getPortletSession().getAttribute("com.aplana.dbmi.admin.TemplatesPortlet.FORM.templateBean");
        try {
			if (!templateBean.isEditAccessExists()){
				throw new DataException("admin.edit.access.error");
			}
            for (Iterator itBlock = templateBean.getBlocks().iterator(); itBlock.hasNext();) {
                TemplateBlock block = (TemplateBlock) itBlock.next();
                for (Iterator itAttribute = block.getAttributes().iterator(); itAttribute.hasNext();) {
                    Attribute attribute = (Attribute) itAttribute.next();
                    Object attributeId = attribute.getId().getId();
                    //System.out.println("TemplateAttributesPortlet.onSubmitAction: attributeId.class=" + attributeId.getClass());
                    

                    Object attInRes = templateAttributesBean.getTemplateAttributesInRezults().get(attribute.getId().getId());
//                    System.out.println("TemplateAttributesPortlet.onSubmitAction: attributeIdStr=" + attributeId + ", attributeInResult=" + attInRes);
//                    if (attInRes != null) {
//                        System.out.println("TemplateAttributesPortlet.onSubmitAction: attInRes.class=" + attInRes.getClass());
//                    }
                    attribute.setSearchShow(attributeId.equals(attInRes));
                    
                    Object width = templateAttributesBean.getTemplateAttributesWidth().get(attribute.getId().getId());
//                    System.out.println("TemplateAttributesPortlet.onSubmitAction: attributeIdStr=" + attributeId + ", width=" + width);
//                    if (width != null) {
//                        System.out.println("TemplateAttributesPortlet.onSubmitAction: width.class=" + width.getClass());
//                    }
                    attribute.setColumnWidth(parseInputInt(width));

                    Object number = templateAttributesBean.getTemplateAttributesNumber().get(attribute.getId().getId());
//                    System.out.println("TemplateAttributesPortlet.onSubmitAction: attributeIdStr=" + attributeId + ", number=" + number);
//                    if (number != null) {
//                        System.out.println("TemplateAttributesPortlet.onSubmitAction: number.class=" + number.getClass());
//                    }
                    attribute.setSearchOrder(parseInputInt(number));
                }

            }
            saveTemplate(templateBean, templateBean.getDataService(), request);
            templateAttributesBean.setMessage(messages.getString("templatesTemplateAttributesSaveSuccess"));
        } catch (Exception e) {
            e.printStackTrace();
            templateAttributesBean.setMessage(e.getMessage());
        }
        request.getPortletSession().setAttribute("com.aplana.dbmi.admin.TemplatesPortlet.FORM.templateBean", templateBean);
        String formAttrName = getFormSessionAttributeName(request);
        request.getPortletSession().setAttribute(formAttrName, command);
        response.setRenderParameter("portlet_action", "editTemplateAttr");

    }

    private int parseInputInt(Object input) {
        int res = 0;
        if (input != null) {
            if (input instanceof Integer) {
                res = ((Integer) input).intValue();
            } else if (input instanceof String) {
                if (((String) input).length() > 0) {
                    res = Integer.parseInt((String) input);
                }
            }
        }
        return res;
    }

    public void saveTemplate(WebTemplateBean templateBean, AsyncDataServiceBean dataService, PortletRequest request) throws DataException, ServiceException {
        Template template = new Template();
        BeanUtils.copyProperties(templateBean, template);
        if (templateBean.getRealId() != null && !"-1".equals(templateBean.getRealId())) {
            template.setId(Long.parseLong(templateBean.getId().getId().toString()));
        }
        template.setBlocks(new ArrayList());
        template.getBlocks().addAll(templateBean.getBlocksLeft());
        template.getBlocks().addAll(templateBean.getBlocksRight());
        template.getBlocks().addAll(templateBean.getBlocksDown());
        template.setActive(true);
        ObjectId id = dataService.saveObject(template, ExecuteOption.SYNC);
        templateBean.setId(Long.parseLong(id.getId().toString()));
        templateBean.setChanged(false);

    }
}