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

import java.util.Calendar;
import java.util.Date;

/**
 * 
 * @author ppolushkin
 * ��������� ���������� ������� �� ����
 */
public class SplitByYears {
	
	public static String getInterval(Date begin, Date end, String before, String middle, String after){
		String s = "";
		for(int i = getDate(begin); i<=getDate(end); i++){
			if(i!=getDate(begin))
				s+=middle+" ";
			s+=before+" "+i+" "+after+" ";
		}; 
		return s;
	}
	
	public static String getInterval(Date begin, Date end){
		return getInterval(begin, end, "", "", "");
	}
	
	public static int getDate(Date d){
		Calendar c = Calendar.getInstance();
		c.setTime(d); 
		return c.get(Calendar.YEAR);
	}

}
