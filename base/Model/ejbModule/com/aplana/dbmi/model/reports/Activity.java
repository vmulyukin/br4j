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
package com.aplana.dbmi.model.reports;

import java.io.Serializable;
import java.util.Date;

public class Activity implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private String state = null;
	private Date beginningDate = null;
	private Date finishingDate = null;
	private String region = null;
	private String city = null;
	private String name = null;
	private String segmentation = null;
	private String operator = null;
	private String cardId = null;
	private String description = null;
	private String direction = null;
	
	public Date getBeginningDate() {
		return beginningDate;
	}
	public void setBeginningDate(Date beginningDate) {
		this.beginningDate = beginningDate;
	}
	public String getCardId() {
		return cardId;
	}
	public void setCardId(String cardId) {
		this.cardId = cardId;
	}
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getDirection() {
		return direction;
	}
	public void setDirection(String direction) {
		this.direction = direction;
	}
	public Date getFinishingDate() {
		return finishingDate;
	}
	public void setFinishingDate(Date finishingDate) {
		this.finishingDate = finishingDate;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getOperator() {
		return operator;
	}
	public void setOperator(String operator) {
		this.operator = operator;
	}
	public String getRegion() {
		return region;
	}
	public void setRegion(String region) {
		this.region = region;
	}
	public String getSegmentation() {
		return segmentation;
	}
	public void setSegmentation(String segmentation) {
		this.segmentation = segmentation;
	}
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}
	
}
