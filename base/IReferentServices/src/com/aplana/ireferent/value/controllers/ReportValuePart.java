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
package com.aplana.ireferent.value.controllers;

import java.util.Date;

public class ReportValuePart implements ValuePart {

    private String factUser;
    private String round;
    private Date time;
    private String value;

    public ReportValuePart() {
    }

    public ReportValuePart(String value) {
	this("", "", null, value);
    }

    public ReportValuePart(String factUser, String round, Date time,
	    String value) {
	this.factUser = factUser;
	this.round = round;
	this.time = time;
	this.value = value;
    }

    public String getFactUser() {
	return this.factUser;
    }

    public void setFactUser(String factUser) {
	this.factUser = factUser;
    }

    public String getRound() {
	return this.round;
    }

    public void setRound(String round) {
	this.round = round;
    }

    public Date getTime() {
	return this.time;
    }

    public void setTime(Date time) {
	this.time = time;
    }

    public String getValue() {
	return this.value;
    }

    public void setValue(String value) {
	this.value = value;
    }

    @Override
    public String toString() {
	return value;
    }
}
