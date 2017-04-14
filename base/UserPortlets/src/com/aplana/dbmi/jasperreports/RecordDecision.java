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
package com.aplana.dbmi.jasperreports;

import java.io.ByteArrayInputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class RecordDecision implements Comparable {
	private String type;
	private String sname;
	private String org;
	private String name;
	private String mname;
	private String action;
	private Date date;
	private String text;
	private String round;
	private String fact_user;
	private String number;
	private Long visaId;
	private ByteArrayInputStream decisions;
	private Long visaState;
	private String cardStatusName;
	private Long curRound;
	private Long order;
	private Boolean hasChild;
	/**
     * @return the number
     */
    public String getNumber() {
        return number;
    }

    /**
     * @param number the number to set
     */
    public void setNumber(String number) {
        this.number = number;
    }
	
	public RecordDecision() {
		super();
	}
	
	public RecordDecision(String type, String sname, String org, String name, String mname, 
	        String textDate, String action, String fact_user, String round, String number, String text) throws ParseException {
		this.type = type;
		this.sname = sname;
		this.org = org;
		this.name = name;
		this.mname = mname;
		this.action = action;
		this.fact_user = fact_user;
		this.round = round;
		this.number = number;
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		this.date = dateFormat.parse(textDate);
		
		this.text = text;
	}

	public RecordDecision(String type, String sname, String org, String name, String mname, String textDate,  String round, String fact_user, String text, Long visaId, ByteArrayInputStream decisions,Long visaState,String cardStatusName,Long curRound,Long order,Boolean hasChild) throws ParseException {
		this.type = type;
		this.sname = sname;
		this.org = org;
		this.name = name;
		this.mname = mname;
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		if (textDate !=null)
			this.date = dateFormat.parse(textDate);
		else
			this.date = null;
		
		this.text = text;
		this.round = round;
		this.fact_user = fact_user;
		this.visaId = visaId;
		this.visaState = visaState;
		this.decisions = decisions;
		this.cardStatusName=cardStatusName;
		this.curRound=curRound;
		this.order=order;
		this.hasChild=hasChild;
	}

    /**
     * @return the action
     */
    public String getAction() {
        return action;
    }

    /**
     * @param action the action to set
     */
    public void setAction(String action) {
        this.action = action;
    }


    public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}

	public int compareTo(Object arg0) {
		if (date == null){
			return 0;
		}
		if (round == null){
			return 0;
		}
		if (arg0 == null || ((RecordDecision) arg0).getDate()==null) {
			return 1;
		}
		if (arg0 instanceof RecordDecision) {
			RecordDecision rd=(RecordDecision) arg0;
			if (Long.parseLong(round) > Long.parseLong(rd.getRound()))
				return 1;
			else if (Long.parseLong(round) < Long.parseLong(rd.getRound()))
				return -1;
			else
				if (order!=null&&rd.getOrder()!=null&&(order > rd.getOrder()))
					return 1;
				else if (order!=null&&rd.getOrder()!=null&&(order < rd.getOrder()))
					return -1;
				else
					return date.compareTo(((RecordDecision) arg0).getDate());
		} else {
			return 0;
		}
	}

	public String getSname() {
		return sname;
	}

	public void setSname(String sname) {
		this.sname = sname;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getMname() {
		return mname;
	}

	public void setMname(String mname) {
		this.mname = mname;
	}
	
	public String getRound() {
		return round;
	}
	public void setRound(String round) {
		this.round = round;
	}

	public String getFact_user() {
		return fact_user;
	}
	public void setFact_user(String fact_user) {
		this.fact_user = fact_user;
	}
	public Long getVisaId() {
		return visaId;
	}
	public void setVisaId(Long visaId) {
		this.visaId = visaId;
	}
	public ByteArrayInputStream getDecisions() {
		return decisions;
	}
	public void setDecisions(ByteArrayInputStream decisions) {
		this.decisions = decisions;
	}
	public Long getVisaState() {
		return visaState;
	}
	public void setVisaState(Long visaState) {
		this.visaState = visaState;
	}
	public String getCardStatusName() {
		return cardStatusName;
	}
	public void setCardStatusName(String cardStatusName) {
		this.cardStatusName = cardStatusName;
	}
	public Long getCurRound() {
		return curRound;
	}
	public void setCurRound(Long curRound) {
		this.curRound = curRound;
	}
	public Long getOrder() {
		return order;
	}
	public void setOrder(Long order) {
		this.order = order;
	}
	public Boolean getHasChild() {
		return hasChild;
	}
	public void HasChild(Boolean hasChild) {
		this.hasChild = hasChild;
	}

	public String getOrg() {
		return org;
	}

	public void setOrg(String org) {
		this.org = org;
	}	
}
