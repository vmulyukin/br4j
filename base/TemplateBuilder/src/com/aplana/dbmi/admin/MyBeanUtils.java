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

import java.util.Collection;
import java.util.Iterator;

import com.aplana.dbmi.model.DataObject;

public class MyBeanUtils {
     public static void remove(DataObject dataObject, Collection from){
    	 for(Iterator it = from.iterator(); it.hasNext();){
    		 DataObject tmpDataObject = (DataObject) it.next();
    		 if(tmpDataObject.getId().equals(dataObject.getId())){
    			 from.remove(tmpDataObject);
    			 return;
    		 }
    	 }
     }
     public static void removeAll(Collection from, Collection items){
    	 for(Iterator it = items.iterator(); it.hasNext();){
    		 DataObject dataObject = (DataObject) it.next();
    		 remove(dataObject, from);
    	 }
     }
     public static final boolean isEmpty(Object obj){
    	 if(obj == null ||
    		"".equals(obj)){
    		 return true;
    	 }
    	 return false;
     }
}
