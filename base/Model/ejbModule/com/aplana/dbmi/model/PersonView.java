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
/**
 * 
 */
package com.aplana.dbmi.model;

/**
 * @author iKuznetsov
 * Using for partial name or full name with middle name
 *
 */
public class PersonView extends Person {

    private String firstName;
    private String middleName;
    private String lastName;
    
    public String getFullName() {
        return super.getFullName() + " " + middleName;
    }
    
    /**
     * @return the firstName
     */
    public String getFirstName() {
        return firstName;
    }
    
    /**
     * @param firstName the firstName to set
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    
    /**
     * @return the middleName
     */
    public String getMiddleName() {
        return middleName;
    }
    
    /**
     * @param middleName the middleName to set
     */
    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }
    
    /**
     * @return the lastName
     */
    public String getLastName() {
        return lastName;
    }
    
    /**
     * @param lastName the lastName to set
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
}
