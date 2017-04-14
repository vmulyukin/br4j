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
package com.aplana.dbmi.model.web;

public class IntegerControl extends AbstractControl {
    private Integer from;
    private Integer to;

    public IntegerControl() {
        super();
        // TODO Auto-generated constructor stub
    }
    public IntegerControl(String name, String label) {
        super(name, label);
        // TODO Auto-generated constructor stub
    }
    public IntegerControl(String name, String label, Integer from, Integer to) {
        super(name, label);
        this.from = from;
        this.to = to;
    }
    public Integer getFrom() {
        return from;
    }
    public void setFrom(Integer from) {
        this.from = from;
    }
    public Integer getTo() {
        return to;
    }
    public void setTo(Integer to) {
        this.to = to;
    }
}
