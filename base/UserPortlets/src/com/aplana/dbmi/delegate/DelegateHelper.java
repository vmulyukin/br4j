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
package com.aplana.dbmi.delegate;

import com.aplana.dbmi.action.CheckDelegationIsEditableAction;
import com.aplana.dbmi.action.GetUserViewsFromSameDepartamentAction;
import com.aplana.dbmi.action.IsUserDepartamentChief;
import com.aplana.dbmi.model.Delegation;
import com.aplana.dbmi.model.IntegerAttribute;
import com.aplana.dbmi.model.LongAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.model.PersonView;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.ServiceException;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DelegateHelper {	
	
	private static Log logger = LogFactory.getLog(DelegateHelper.class);

    public static List<PersonView> getPersonDictionary(DataServiceBean dataServiceBean) throws DataException, ServiceException {
        final List<PersonView> persons = getPersonViewDelegatableList(dataServiceBean);

        // ��������� �� ��������.
        Collections.sort(persons, new Comparator<Person>(){
            public int compare(Person o1, Person o2) {
                final String v1 = (o1!=null) ? o1.getFullName() : null;
                final String v2 = (o2!=null) ? o2.getFullName() : null;
                if (v1 == null)
                    return (v2 == null)	? 0 	// null == null
                                        : -1; 	// null < x
                if (v2 == null) return 1; 		// x > null
                return v1.compareToIgnoreCase(v2);
            }}
        );

        return persons;
    }

    public static List<PersonView> getPersonViewDelegatableList(DataServiceBean dataServiceBean) throws DataException, ServiceException {
	    GetUserViewsFromSameDepartamentAction action = new GetUserViewsFromSameDepartamentAction();
        return (List<PersonView>) dataServiceBean.doAction(action);
	}

    /**
     * ����� ��������� �������� �� ������������ ������������� ������-������ ������������
     * @param dataServiceBean ������ ���
     * @param isDelegate ���� false ����������� ������������, ������� �����������,
     * 					 ���� true � ������������ ������������ ���������� �������������, 
     * 						  �� ����������� �� ������������ ������������� ���� ����� ��������.
     * @return true ���� ������������ �������� ������������� ������-������ ������������� � false � ��������� ������
     */
    public static boolean isFromPersonSelectable(DataServiceBean dataServiceBean, boolean isDelegate) {
        boolean isFromPersonSelectable = false;

        IsUserDepartamentChief action = new IsUserDepartamentChief();
        action.setDelegate(isDelegate);
		try {
		    // ���� ������������ - ������������ ������������� ��� ������������� (action ���������� Boolean),
		    // �� �� ����� �������� ���� "�� ����":
		    isFromPersonSelectable = !((List<?>) dataServiceBean.doAction(action)).isEmpty();
        } catch (Exception e) {
        	logger.error("An error has occured while executing "+action.getClass()+" action",e);
        }

        return isFromPersonSelectable;
    }
    
    /**
     * ����� ��������� �������� �� ������������ ������������� ������-������ ������������
     * @return true ���� ������������ �������� ������������� ������-������ ������������� � false � ��������� ������
     */
    public static boolean isFromPersonSelectable(DataServiceBean dataServiceBean) {
    	return isFromPersonSelectable(dataServiceBean, false);
    }
    
    /**
     * �������� ����������� �������������� �������������
     * @param service
     * @param delegationId - id �������������
     * @return ���� ����������� �������������� �������������
     */
    public static boolean isDelegationEditable(DataServiceBean service, LongAttribute delegationId) {
    	
    	if(delegationId == null)
			return false;
    	
		return isDelegationEditable(service, delegationId.getValue());
		
	}
    
    public static boolean isDelegationEditable(DataServiceBean service, long delegationId) {
    	
    	if(!service.getIsDelegation())
			return true;
    	
    	CheckDelegationIsEditableAction action = new CheckDelegationIsEditableAction();
		action.setDelegationId(new ObjectId(Delegation.class, delegationId));
		
		try {
				
			Object result = service.doAction(action);
				
			if(result instanceof Boolean) {
				return (Boolean) result;
			}
		} catch (DataException e) {
			 logger.error("An error has occured while executing "+action.getClass()+" action",e);
		} catch (ServiceException e) {
			logger.error("An error has occured while executing "+action.getClass()+" action",e);
		}
		
		return false;
    }
    
    public static Comparator<String> getDelegateListComparator(final Class<?> clazz) {
    	return new Comparator<String>() {

			public int compare(String s1, String s2) {
				return (clazz.equals(IntegerAttribute.class)) ? 
					(Integer.parseInt(s1)) - (Integer.parseInt(s2))
				:
					s1.toString().compareTo(s2.toString())
				;
			}
    		
    	};
    }
    
}
