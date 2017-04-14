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
import java.util.Collection;
import java.util.Iterator;

import org.springframework.beans.BeanUtils;

import com.aplana.dbmi.model.ListAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.ReferenceValue;
import com.aplana.dbmi.model.TreeAttribute;
import com.aplana.dbmi.model.web.Option;
import com.jenkov.prizetags.tree.impl.TreeNode;
import com.jenkov.prizetags.tree.itf.ITreeNode;

public class WebUtils {
    public static final String ID_PREFIX = "ID_PREFIX";

    public static void getOptionsFromTree(Collection values, ITreeNode root) {
        values.add(getOptionFromNode(root));
        for (Iterator it = root.getChildren().iterator(); it.hasNext();) {
            ITreeNode node = (ITreeNode) it.next();
            ReferenceValue nodeReferenceValue = (ReferenceValue) node.getObject();
            if (nodeReferenceValue != null && nodeReferenceValue.isActive()) {
                getOptionsFromTree1(values, node);
            }            
        }
    }
    private static void getOptionsFromTree1(Collection values, ITreeNode root) {
        ReferenceValue rootReferenceValue = (ReferenceValue) root.getObject();
        if (rootReferenceValue != null && rootReferenceValue.isActive()) {
            values.add(getOptionFromNode(root));
        }
        for (Iterator it = root.getChildren().iterator(); it.hasNext();) {
            ITreeNode node = (ITreeNode) it.next();
            ReferenceValue nodeReferenceValue = (ReferenceValue) node.getObject();
            if (nodeReferenceValue != null && nodeReferenceValue.isActive()) {
                getOptionsFromTree1(values, node);
            }            
        }
    }

    public static Option getOptionFromNode(ITreeNode treeNode) {
        Option option = new Option();
        option.setLabelEn(treeNode.getNameEn());
        option.setLabelRu(treeNode.getNameRu());
        option.setValue(treeNode.getId());
        return option;
    }

    public static ITreeNode getNodeFromReference(WebReferenceValue referenceValue) {
        ReferenceValue newReferenceValue = new ReferenceValue();
        BeanUtils.copyProperties(referenceValue, newReferenceValue);
        ITreeNode node = new TreeNode();
        node.setObject(newReferenceValue);
        node.setNameRu(referenceValue.getValueRu());
        node.setNameEn(referenceValue.getValueEn());
        node.setId(referenceValue.getRealId().toString());
        return node;

    }

    public static ReferenceValue getReferenceFromNode(ITreeNode node, boolean copyId) {
        ReferenceValue referenceValue = new ReferenceValue();
        referenceValue.setActive(((ReferenceValue) node.getObject()).isActive());
        referenceValue.setValueEn(node.getNameEn());
        referenceValue.setValueRu(node.getNameRu());
        if (node.getParent() != null) {
            referenceValue.setParent(new ObjectId(ReferenceValue.class, node.getParent().getId()));
        }
        boolean tmpCopyId = copyId || node.getId().indexOf("-") < 0;
        if (!MyBeanUtils.isEmpty(node.getId()) && tmpCopyId) {
            referenceValue.setId(Long.parseLong(node.getId()));
        }
        return referenceValue;

    }

    public static TreeAttribute getTreeAttributeFormTree(WebAttribute viewAttribute, ITreeNode node) {
        TreeAttribute attribute = new TreeAttribute();
        attribute.setActive(true);
        attribute.setNameEn(viewAttribute.getNameEn());
        attribute.setNameRu(viewAttribute.getNameRu());
        attribute.setId(viewAttribute.getRealId().toString());
        if (node != null) {
            attribute.setReferenceValues(new ArrayList());
            defineReferenceValues(attribute.getReferenceValues(), node);
        }
        return attribute;
    }

    public static void defineReferenceValues(Collection values, ITreeNode node) {
        for (Iterator it = node.getChildren().iterator(); it.hasNext();) {
            ITreeNode child = (ITreeNode) it.next();
            ReferenceValue referenceValue = getReferenceFromNode(child, false);
            referenceValue.setChildren(new ArrayList());
            values.add(referenceValue);
            defineReferenceValues(referenceValue.getChildren(), child);
        }
    }

    public static ListAttribute getListAttributeFormTree(WebAttribute viewAttribute, ITreeNode node) {
        ListAttribute attribute = new ListAttribute();
        attribute.setActive(true);
        attribute.setNameEn(viewAttribute.getNameEn());
        attribute.setNameRu(viewAttribute.getNameRu());
        attribute.setId(viewAttribute.getRealId().toString());
        if (node != null) {
            attribute.setReferenceValues(new ArrayList());
            defineReferenceValues(attribute.getReferenceValues(), node);
        }
        return attribute;
    }
}
