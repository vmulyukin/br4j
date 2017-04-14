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
package com.aplana.dbmi.module.docflow.calendar;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.aplana.dbmi.service.DataException;

/**
 * @author lyakin
 * @�������� ����� ��� ������ � ������� ���������
 */
public class WorkCalendar extends CalendarAPI {

	private ArrayList<String> holidays = new ArrayList<String>();
	private ArrayList<String> workdays = new ArrayList<String>();
	
	protected WorkCalendar() {	  
		if (doc != null)
			setExtradates(doc);
	}
	
	/** ��������� ����-���������� �� XML ����� � 2 �������:
	 * <br>holidays - ��������� ��� 
	 * <br>workdays - ������� ���
	 * @param node - XML-���
	 */
	private void setExtradates(Node node) {
		int type = node.getNodeType();
		switch (type) {
			case Node.DOCUMENT_NODE: 
			{
				setExtradates(((Document)node).getDocumentElement());
				break;
			}
			case Node.ELEMENT_NODE: 
			{
				String  nodeName = node.getNodeName();
				if ((nodeName.equals("holiday"))||(nodeName.equals("workday"))){
					NamedNodeMap attrs = node.getAttributes();
					for (int i = 0; i < attrs.getLength(); i++){
						String date = ((Attr)attrs.item(i)).getValue();
						if (nodeName.equals("holiday")){
							holidays.add(date);
						} else {
							workdays.add(date);
						}
					}
				} else {
					NodeList children = node.getChildNodes();
					for (int i = 0; i < children.getLength(); i++)
						setExtradates(children.item(i));
				}
				break;
			}
		}
	}
	
	@Override
	public boolean isWorkDay(Date date) throws ParseException {
		boolean returnvalue = true;
		Date holiday;
		DateFormat formatter ; 
		Calendar calendar;
		Calendar holidayCalendar;
		calendar = Calendar.getInstance();
		holidayCalendar = Calendar.getInstance();
		calendar.setTime(date);
		clearParameters(calendar);
		int day = calendar.get(Calendar.DAY_OF_WEEK);
		if (day==1 || day==7) {
			returnvalue = false;
			Iterator<String> iter = workdays.iterator();
			while(iter.hasNext() && returnvalue == false) {
				String str_holiday = iter.next();
				formatter = new SimpleDateFormat("dd.MM.yyyy");
		        holiday = formatter.parse(str_holiday);
		        holidayCalendar.setTime(holiday);
		        if (holidayCalendar.equals(calendar)){
		        	returnvalue = true;
		        }
			}		
		} else {
			Iterator<String> iter = holidays.iterator();
			while(iter.hasNext() && returnvalue == true) {
				String str_holiday = iter.next();
				formatter = new SimpleDateFormat("dd.MM.yyyy");
		        holiday = formatter.parse(str_holiday);
		        holidayCalendar.setTime(holiday);
		        if (holidayCalendar.equals(calendar)){
		        	returnvalue = false;
		        }
			}				
		}
		return returnvalue;
	}
	
	@Override
	public Date addToToday(int increment) throws DataException {
		try {
			Date aDate = new Date();
			return addToDate(increment, aDate);
		} catch (Exception e) {
			throw new DataException("CAN_NOT_ADD_TO_DATE", e);
		}
	}
	
	@Override
	public Date addToDate(int increment, Date date) throws DataException {
		try {
			int shift = 0;
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(date);
			calendar = clearParameters(calendar);
			while (shift != increment){
				if (increment > 0){
					calendar.add(Calendar.DAY_OF_YEAR, 1);
					if (isWorkDay(calendar.getTime())) {
						shift++;
					}
				} else {
					calendar.add(Calendar.DAY_OF_YEAR, -1);
					if (isWorkDay(calendar.getTime())) {
						shift--;
					}
				}
			}
			return calendar.getTime();
		}
		catch(Exception e){
			throw new DataException("CAN_NOT_ADD_TO_DATE", e);
		}
	}
	
	@Override
	public int diff(Date firstDate, Date secondDate) throws DataException {
		try {
			int result = 0;
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(firstDate);
			calendar = clearParameters(calendar);
			
			Calendar secondCalendar = Calendar.getInstance();
			secondCalendar.setTime(secondDate);
			secondCalendar = clearParameters(secondCalendar);
			
			while (calendar.getTime().compareTo(secondCalendar.getTime()) != 0) {
				if (calendar.getTime().compareTo(secondCalendar.getTime()) < 0) {
					calendar.add(Calendar.DAY_OF_YEAR, 1);
					if (isWorkDay(calendar.getTime())) {
						result++;
					}					
				} else if(calendar.getTime().compareTo(secondCalendar.getTime()) > 0) {
					calendar.add(Calendar.DAY_OF_YEAR, -1);
					if (isWorkDay(calendar.getTime())) {
						result--;
					}										
				}
			}
			return result;
		} catch(Exception e) {
			throw new DataException("CAN_NOT_DIFF",e);
		}
	}
}
