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

import java.util.List;

public class ListUtils {
    public static void upObject(List list, Object obj){
        int index = list.indexOf(obj);
        if (index >0){
            Object tmpObj =  list.get(index - 1);
            list.set(index-1, obj);
            list.set(index, tmpObj);
        } 
    }
    
    public static  void downObject(List list, Object obj){
        int index = list.indexOf(obj);
        if (index < list.size() - 1){
            Object tmpObj =  list.get(list.indexOf(obj) + 1);
            list.set(index+1, obj);
            list.set(index, tmpObj);
        } 
    }    
}
