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

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.DateAttribute;
import com.aplana.dbmi.model.IntegerAttribute;
import com.aplana.dbmi.model.ListAttribute;
import com.aplana.dbmi.model.PersonAttribute;
import com.aplana.dbmi.model.ReferenceValue;
import com.aplana.dbmi.model.StringAttribute;
import com.aplana.dbmi.model.TextAttribute;
import com.aplana.dbmi.model.TreeAttribute;
import com.aplana.dbmi.model.web.AbstractControl;
import com.aplana.dbmi.model.web.CalendarControl;
import com.aplana.dbmi.model.web.CheckboxControl;
import com.aplana.dbmi.model.web.TextControl;
import com.aplana.dbmi.model.web.TreeControl;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.ServiceException;
import com.jenkov.prizetags.tree.impl.Tree;
import com.jenkov.prizetags.tree.impl.TreeNode;

public class TemplateControlUtils {
   public static  AbstractControl initializeControl(Attribute attribute, DataServiceBean dataService) throws RemoteException, DataException, ServiceException{
	   AbstractControl control  = null;
	   if(attribute instanceof TreeAttribute
		  ){
		   TreeAttribute treeAttribute = (TreeAttribute) attribute;
		   TreeControl treeControl = new TreeControl();
		   Tree tree = new Tree();
	       tree.setSingleSelectionMode(true);
		   treeControl.setTree(tree);
		   Collection rootValues =  dataService.listChildren(treeAttribute.getReference(), ReferenceValue.class);
/*                   
		   if(rootValues.size() ==0){
			   return null;
		   }
*/                   
			   treeControl.setShowRoot(Boolean.TRUE);
			   TreeNode root = new TreeNode();
			   root.setId(treeAttribute.getReference().getId().toString());
			   root.setNameRu(treeAttribute.getNameRu());
			   root.setNameEn(treeAttribute.getNameEn());
			   
		       tree.setRoot(root);
		       tree.expand(root.getId());
		       convertTreeLevel(root, rootValues);
		      control = treeControl;
	   }else if(attribute instanceof StringAttribute){
		   //StringAttribute stringAttribute = (StringAttribute) attribute;
		   CheckboxControl textControl = new CheckboxControl();
		   textControl.setValue(attribute.getId().getId().toString());
		   control = textControl;
	   }else if(attribute instanceof TextAttribute){
		   //TextAttribute textAttribute = (TextAttribute) attribute;
		   CheckboxControl textareaControl = new CheckboxControl();
		   textareaControl.setValue(attribute.getId().getId().toString());
		   control = textareaControl;
	   }
	   else if(attribute instanceof IntegerAttribute){
		   IntegerAttribute integerAttribute = (IntegerAttribute) attribute;
		   TextControl textControl = new TextControl();
		   textControl.setLength(new Integer(integerAttribute.getDisplayLength()));
		   control = textControl;
	   }else if(attribute instanceof DateAttribute){
		   //DateAttribute dateAttribute = (DateAttribute) attribute;
		   CalendarControl calendarControl = new CalendarControl();
		   control = calendarControl;
	   }else if(attribute instanceof ListAttribute){
		   ListAttribute listAttribute = (ListAttribute) attribute;
		   TreeControl treeControl = new TreeControl();
		   Tree tree = new Tree();
	       tree.setSingleSelectionMode(true);
		   treeControl.setTree(tree);
		   Collection rootValues =  dataService.listChildren(listAttribute.getReference(), ReferenceValue.class);
/*
                   if(rootValues.size() ==0){
			   return null;
		   }
*/
			   treeControl.setShowRoot(Boolean.TRUE);
			   TreeNode root = new TreeNode();
			   root.setId(listAttribute.getReference().getId().toString());
			   root.setNameRu(listAttribute.getNameRu());
			   root.setNameEn(listAttribute.getNameEn());
			   
		       tree.setRoot(root);
		       tree.expand(root.getId());
		       convertTreeLevel(root, rootValues);
		      control = treeControl;		   
/*		   ComboboxControl comboboxControl = new ComboboxControl();
		   List options = new ArrayList();
		   Collection listValues =  dataService.listChildren(listAttribute.getReference(), ReferenceValue.class);
		   if(listValues.size() == 0){
			   return null;
		   }
		   for(Iterator it =  listValues.iterator(); it.hasNext(); ){
			   ReferenceValue value = (ReferenceValue) it.next();
			   Option option = new Option(value.getId().getId().toString(), value.getValue());
			   options.add(option);
		   }
		   comboboxControl.setItems(options);
		   control = comboboxControl; */
	   }else if(attribute instanceof PersonAttribute ){
		   return null;
	   }
		   
	   
	   
	   control.setName(attribute.getId().getId().toString());
	   control.setLabel(attribute.getName());
	   control.setLabelEn(attribute.getNameEn());
	   control.setLabelRu(attribute.getNameRu());
	   
	   




	   



	   return control;
   }
   
   private static void convertTreeLevel(TreeNode parent, Collection referenceValues){
	   if(referenceValues == null){
		   return;
	   }
	   
	   for(Iterator it = referenceValues.iterator(); it.hasNext(); ){
		   ReferenceValue referenceValue =  (ReferenceValue) it.next();
//                   TreeNode node = new TreeNode(new CheckboxControl(referenceValue.getId().getId().toString(), 
//                           referenceValue.getValue(), 
//                           referenceValue.getId().getId().toString()));
                   TreeNode node = new TreeNode();
                   node.setObject(referenceValue);
                   node.setNameEn(referenceValue.getValueEn());
		   node.setNameRu(referenceValue.getValueRu());
		   node.setId(referenceValue.getId().getId().toString());
		   parent.addChild(node);
		   convertTreeLevel(node, referenceValue.getChildren());
	   }
   }
   
   public static void getListReferenceValues(String listId, Collection referenceValues, Collection rootReferenceValues, Map objectIds){
	   if(rootReferenceValues == null){
		   return;
	   }
	   for(Iterator it = rootReferenceValues.iterator(); it.hasNext(); ){
		   ReferenceValue referenceValue = (ReferenceValue) it.next();
		   if(objectIds.containsKey(listId)
				&& referenceValue.getId().getId().toString().equals(objectIds.get(listId))   ){
			   referenceValues.add(referenceValue);
		   }
	   }
   }
   
   public static void getTreeReferenceValues(Collection referenceValues, Collection rootReferenceValues, Map objectIds){
	   if(rootReferenceValues == null){
		   return;
	   }
	   for(Iterator it = rootReferenceValues.iterator(); it.hasNext(); ){
		   ReferenceValue referenceValue = (ReferenceValue) it.next();
		   if(objectIds.containsKey(referenceValue.getId().getId().toString())
				&& objectIds.get(referenceValue.getId().getId().toString())!= null
				&& ! "".equals(objectIds.get(referenceValue.getId().getId().toString()))
				){
			   referenceValues.add(referenceValue);
		   }
		   getTreeReferenceValues(referenceValues, referenceValue.getChildren(), objectIds);
	   }
   }

   
}
