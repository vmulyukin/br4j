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
package com.aplana.dbmi.filter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.aplana.dbmi.delegate.DelegateListSessionBean.EFilterByActiveMode;
import com.aplana.dbmi.model.Delegation;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.PermissionDelegate;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.model.PersonView;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.ServiceException;

/***************************************************************************
 * ������������ ���������
 */

public class DelegateFilterUtil{

    private DelegateFilterUtil() {}

    /**
     * ������������� ������ list �� ���������� ��������.
     * @param list �������� ����� ��� ������������.
     * @param filter ������, ���� null = ��� �������, ��� ��������.
     * @return ��������������� ������.
     */
    public static List<Delegation>  funcFilterDelegates( List<Delegation> list, 
                IFilter filter)
    {
        if (list == null || list.isEmpty()) 
            return null;

        // final HashSet<PermissionDelegate> result = new HashSet<PermissionDelegate>();
        final ArrayList<Delegation> result = new ArrayList<Delegation>();
        for (Delegation delegate: list) {
            if (filter == null || filter.check(delegate)) // ��������
                result.add(delegate);
        }
        return result;
    }
    
    /**
     * ����������� ��������� ����������� �������� ��������� ��� ������.
     * @param delegates  ������ ��������� ��� ����������.
     * @param showActiveMode ��������� ���������.
     */
/*    public static void filterByActiveMode(List<PermissionDelegate> delegates,
            EFilterByActiveMode showActiveMode) 
    {
        if (showActiveMode != EFilterByActiveMode._Both)
            removeByActive(delegates, showActiveMode == EFilterByActiveMode._ActiveOnly);
    }*/
    
    /**
     * ����������� ��������� ����������� �������� ��������� ��� ������.
     * @param delegates  ������ ��������� ��� ����������.
     * @param showActiveMode ��������� ���������.
     */
    public static void filterByActiveMode(List<Delegation> delegates,
            EFilterByActiveMode showActiveMode) 
    {
        /*
        switch(showActive) {
            case _ActiveOnly:   removeByActive(delegates, true); break;
            case _InactiveOnly: removeByActive(delegates, false);break;
        }
         */
        if (showActiveMode != EFilterByActiveMode._Both)
            removeByActive(delegates, showActiveMode == EFilterByActiveMode._ActiveOnly);
    }
    
    /**
     * ���������� ������� �������������
     * @param delegates ������ ��������� ��� ����������
     */
    public static void filterHidden(List<Delegation> delegates) {
    	if (delegates != null) {
	    	for (Iterator<?> iterator = delegates.iterator(); iterator.hasNext();) {
	            final Delegation delegate = (Delegation) iterator.next();
	            if (delegate.isHidden())
	                iterator.remove();
	        }
    	}
    }

    /**
     * ������� ���������� �������������.
     * @param delegates
     */
    // private void removeByActive(Set<PermissionDelegate> delegates, boolean needActive)
    static void removeByActive(List<Delegation> delegates, boolean needActive)
    {
        if (delegates != null) {
            for (Iterator<?> iterator = delegates.iterator(); iterator.hasNext();) {
                final Delegation permissionDelegate = (Delegation) iterator.next();
                if (permissionDelegate.isActive() != needActive)
                    iterator.remove();
            }
        }
    }
    
    public static List<Delegation> loadFilteredDelegates( DataServiceBean service, 
            IFilter filter) 
        throws DataException, ServiceException 
    {
        // PermissionDelegate[]
        final List<Delegation> list = (List<Delegation>) 
                service.listAll(Delegation.class);
        return funcFilterDelegates(list, filter);
    }
    
    public static List<Delegation> getDelegatesFromPerson( 
            DataServiceBean service, ObjectId id) 
        throws DataException, ServiceException 
    {
        return loadFilteredDelegates( service, new FilterByFromPerson(id));
    }


    public static List<Delegation> getDelegatesToPerson(
            DataServiceBean service, ObjectId id)
        throws DataException, ServiceException
    {
        return loadFilteredDelegates( service, new FilterByToPerson(id));  
    }
    
    /* ===================================================================
     * ============================ ������� ==============================
     * ===================================================================
     */
    
    /**
     * @author RAbdullin
     * ������������ ������ ���������.
     */
    public interface IFilter {
        boolean check( Delegation delegate);
    }
    
    /**
     * @author RAbdullin
     * ������ �� ������� ��������.
     */
    public static class FilterByToPerson implements IFilter {
        final ObjectId toPersonId;

        public FilterByToPerson(ObjectId id) {
            this.toPersonId = id;
        }

        public boolean check(Delegation delegate) {
            return (toPersonId != null) && (delegate != null)
                && ( toPersonId.equals(delegate.getToPersonId()))
                ;
        }
    }
    
    /**
     * @author RAbdullin
     * ������ �� �������� ��������.
     */
    public static class FilterByFromPerson implements IFilter 
    {
        final ObjectId fromPersonId;

        public FilterByFromPerson(ObjectId id) {
            this.fromPersonId = id;
        }

        public boolean check(Delegation delegate) {
            return (fromPersonId != null) && (delegate != null)
                && ( fromPersonId.equals(delegate.getFromPersonId()))
                ;
        }
    }
    
    /**
     * ������ ��� �������
     */
    public static class FilterByPersonHistory implements IFilter 
    {
        final ObjectId fromPersonId;
        final ObjectId toPersonId;

        public FilterByPersonHistory(ObjectId id) {
            this.fromPersonId = id;
            this.toPersonId = id;
        }

        public boolean check(Delegation delegate) {
            return (
            		(fromPersonId != null) && (delegate != null)
            		&& ( fromPersonId.equals(delegate.getFromPersonId()))
                ) 
                || 
                (
                	(toPersonId != null) && (delegate != null)
                     && ( toPersonId.equals(delegate.getToPersonId()))
                );
        }
    }
    
    /**
     * ������ �� ������ PersonView.
     */
    public static class FilterByPersonViewList implements IFilter  {
        final List<PersonView> persons;
        final boolean checkFrom;
        final boolean checkTo;

        /**
         * @param persons - ������ List<PersonView> 
         * @throws DataException
         * @throws ServiceException
         */
        public FilterByPersonViewList(List<PersonView> persons) {
            this.persons = persons;
            this.checkFrom = true;
            this.checkTo = false;
        }
        
        /**
         * @param persons - ������ ��� ��������
         * @param from - �������� ������� � ������ "�� ����" ������������
         * @param to - �������� ������� � ������ "����" ������������
         * @throws DataException
         * @throws ServiceException
         */
        public FilterByPersonViewList(List<PersonView> persons, boolean from, boolean to) {
            this.persons = persons;
            this.checkFrom = from;
            this.checkTo = to;
        }

        public boolean check(Delegation delegate) {
            boolean fromExist = ! this.checkFrom;
            boolean toExist = ! this.checkTo;
            for (Person person : persons) {
                ObjectId personId = person.getId();
                
                if (!fromExist) {
                    if (personId.equals(delegate.getFromPersonId())) {
                        fromExist = true;
                    }
                }
                if (!toExist) {
                    if (personId.equals(delegate.getToPersonId())) {
                        toExist = true;
                    }
                }

                if (fromExist && toExist) {
                    return true;
                }
            }
            return false;
        }
        
    }

    /* *
     * @author RAbdullin
     * ������ �� ����� �������������.
     * ���� null ������������� ���������� �������.
     */
    /*static class FilterByRoleId implements IFilter 
    {
        final ObjectId roleId;

        FilterByRoleId(ObjectId roleId) {
            this.roleId = roleId;
        }

        public boolean check(Delegation delegate) {
            return  (delegate != null) && 
                    (       (roleId == null) 
                        ||  roleId.equals(delegate.getPermissonSetId())
                    );
        }
    }*/
}