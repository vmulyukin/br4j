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
/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 0.9.6</a>, using an XML
 * Schema.
 * $Id$
 */

package org.aplana.br4j.dynamicaccess.xmldef;

  //---------------------------------/
 //- Imported classes and packages -/
//---------------------------------/

import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;
import java.io.Writer;
import java.util.Enumeration;
import java.util.Set;
import java.util.Vector;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.ValidationException;
import org.xml.sax.ContentHandler;

/**
 * �������� ������������� ������ ��� �� �������� �� ������ �
 * �������� ���������� ��� ��������� ��� �� ����������
 * 
 * @version $Revision$ $Date$
 */
public class Template implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _name
     */
    private java.lang.String _name;

    /**
     * Field _template_id
     */
    private java.lang.String _template_id;

    /**
     * �������� ����(����������)
     */
    private java.util.Vector _permissionList;

    /**
     * �������� ������ ���� �������
     */
    private org.aplana.br4j.dynamicaccess.xmldef.Rules _rules;

    /**
     * ������� ���� �������� ��������
     */
    private java.util.Vector _statusList;

    /**
     * ������ ����� ���������
     */
    private java.util.Vector _WFMoveTypeList;

    /**
     * ������ ����� ���������
     */
    private java.util.Vector _attributePermissionTypeList;

    /**
     * ������ ��������� ������������� ����� � ����������� ��
     */
    private java.util.Vector _attributeRuleList;


      //----------------/
     //- Constructors -/
    //----------------/

    public Template() {
        super();
        _permissionList = new Vector();
        _statusList = new Vector();
        _WFMoveTypeList = new Vector();
        _attributePermissionTypeList = new Vector();
        _attributeRuleList = new Vector();
    } //-- org.aplana.br4j.dynamicaccess.xmldef.Template()


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Method addAttributePermissionType
     * 
     * 
     * 
     * @param vAttributePermissionType
     */
    public void addAttributePermissionType(org.aplana.br4j.dynamicaccess.xmldef.AttributePermissionType vAttributePermissionType)
        throws java.lang.IndexOutOfBoundsException
    {
        _attributePermissionTypeList.addElement(vAttributePermissionType);
    } //-- void addAttributePermissionType(org.aplana.br4j.dynamicaccess.xmldef.AttributePermissionType) 

    /**
     * Method addAttributePermissionType
     * 
     * 
     * 
     * @param index
     * @param vAttributePermissionType
     */
    public void addAttributePermissionType(int index, org.aplana.br4j.dynamicaccess.xmldef.AttributePermissionType vAttributePermissionType)
        throws java.lang.IndexOutOfBoundsException
    {
        _attributePermissionTypeList.insertElementAt(vAttributePermissionType, index);
    } //-- void addAttributePermissionType(int, org.aplana.br4j.dynamicaccess.xmldef.AttributePermissionType) 

    /**
     * Method addAttributeRule
     * 
     * 
     * 
     * @param vAttributeRule
     */
    public void addAttributeRule(org.aplana.br4j.dynamicaccess.xmldef.AttributeRule vAttributeRule)
        throws java.lang.IndexOutOfBoundsException
    {
        _attributeRuleList.addElement(vAttributeRule);
    } //-- void addAttributeRule(org.aplana.br4j.dynamicaccess.xmldef.AttributeRule) 

    /**
     * Method addAttributeRule
     * 
     * 
     * 
     * @param index
     * @param vAttributeRule
     */
    public void addAttributeRule(int index, org.aplana.br4j.dynamicaccess.xmldef.AttributeRule vAttributeRule)
        throws java.lang.IndexOutOfBoundsException
    {
        _attributeRuleList.insertElementAt(vAttributeRule, index);
    } //-- void addAttributeRule(int, org.aplana.br4j.dynamicaccess.xmldef.AttributeRule) 

    /**
     * Method addPermission
     * 
     * 
     * 
     * @param vPermission
     */
    public void addPermission(org.aplana.br4j.dynamicaccess.xmldef.Permission vPermission)
        throws java.lang.IndexOutOfBoundsException
    {
        _permissionList.addElement(vPermission);
    } //-- void addPermission(org.aplana.br4j.dynamicaccess.xmldef.Permission) 

    /**
     * Method addPermission
     * 
     * 
     * 
     * @param index
     * @param vPermission
     */
    public void addPermission(int index, org.aplana.br4j.dynamicaccess.xmldef.Permission vPermission)
        throws java.lang.IndexOutOfBoundsException
    {
        _permissionList.insertElementAt(vPermission, index);
    } //-- void addPermission(int, org.aplana.br4j.dynamicaccess.xmldef.Permission) 

    /**
     * Method addStatus
     * 
     * 
     * 
     * @param vStatus
     */
    public void addStatus(org.aplana.br4j.dynamicaccess.xmldef.Status vStatus)
        throws java.lang.IndexOutOfBoundsException
    {
        _statusList.addElement(vStatus);
    } //-- void addStatus(org.aplana.br4j.dynamicaccess.xmldef.Status) 

    /**
     * Method addStatus
     * 
     * 
     * 
     * @param index
     * @param vStatus
     */
    public void addStatus(int index, org.aplana.br4j.dynamicaccess.xmldef.Status vStatus)
        throws java.lang.IndexOutOfBoundsException
    {
        _statusList.insertElementAt(vStatus, index);
    } //-- void addStatus(int, org.aplana.br4j.dynamicaccess.xmldef.Status) 

    /**
     * Method addWFMoveType
     * 
     * 
     * 
     * @param vWFMoveType
     */
    public void addWFMoveType(org.aplana.br4j.dynamicaccess.xmldef.WFMoveType vWFMoveType)
        throws java.lang.IndexOutOfBoundsException
    {
        _WFMoveTypeList.addElement(vWFMoveType);
    } //-- void addWFMoveType(org.aplana.br4j.dynamicaccess.xmldef.WFMoveType) 

    /**
     * Method addWFMoveType
     * 
     * 
     * 
     * @param index
     * @param vWFMoveType
     */
    public void addWFMoveType(int index, org.aplana.br4j.dynamicaccess.xmldef.WFMoveType vWFMoveType)
        throws java.lang.IndexOutOfBoundsException
    {
        _WFMoveTypeList.insertElementAt(vWFMoveType, index);
    } //-- void addWFMoveType(int, org.aplana.br4j.dynamicaccess.xmldef.WFMoveType) 

    /**
     * Method enumerateAttributePermissionType
     * 
     * 
     * 
     * @return Enumeration
     */
    public java.util.Enumeration enumerateAttributePermissionType()
    {
        return _attributePermissionTypeList.elements();
    } //-- java.util.Enumeration enumerateAttributePermissionType() 

    /**
     * Method enumerateAttributeRule
     * 
     * 
     * 
     * @return Enumeration
     */
    public java.util.Enumeration enumerateAttributeRule()
    {
        return _attributeRuleList.elements();
    } //-- java.util.Enumeration enumerateAttributeRule() 

    /**
     * Method enumeratePermission
     * 
     * 
     * 
     * @return Enumeration
     */
    public java.util.Enumeration enumeratePermission()
    {
        return _permissionList.elements();
    } //-- java.util.Enumeration enumeratePermission() 

    /**
     * Method enumerateStatus
     * 
     * 
     * 
     * @return Enumeration
     */
    public java.util.Enumeration enumerateStatus()
    {
        return _statusList.elements();
    } //-- java.util.Enumeration enumerateStatus() 

    /**
     * Method enumerateWFMoveType
     * 
     * 
     * 
     * @return Enumeration
     */
    public java.util.Enumeration enumerateWFMoveType()
    {
        return _WFMoveTypeList.elements();
    } //-- java.util.Enumeration enumerateWFMoveType() 

    /**
     * Method getAttributePermissionType
     * 
     * 
     * 
     * @param index
     * @return AttributePermissionType
     */
    public org.aplana.br4j.dynamicaccess.xmldef.AttributePermissionType getAttributePermissionType(int index)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _attributePermissionTypeList.size())) {
            throw new IndexOutOfBoundsException();
        }
        
        return (org.aplana.br4j.dynamicaccess.xmldef.AttributePermissionType) _attributePermissionTypeList.elementAt(index);
    } //-- org.aplana.br4j.dynamicaccess.xmldef.AttributePermissionType getAttributePermissionType(int) 

    /**
     * Method getAttributePermissionType
     * 
     * 
     * 
     * @return AttributePermissionType
     */
    public org.aplana.br4j.dynamicaccess.xmldef.AttributePermissionType[] getAttributePermissionType()
    {
        int size = _attributePermissionTypeList.size();
        org.aplana.br4j.dynamicaccess.xmldef.AttributePermissionType[] mArray = new org.aplana.br4j.dynamicaccess.xmldef.AttributePermissionType[size];
        for (int index = 0; index < size; index++) {
            mArray[index] = (org.aplana.br4j.dynamicaccess.xmldef.AttributePermissionType) _attributePermissionTypeList.elementAt(index);
        }
        return mArray;
    } //-- org.aplana.br4j.dynamicaccess.xmldef.AttributePermissionType[] getAttributePermissionType() 

    /**
     * Method getAttributePermissionTypeCount
     * 
     * 
     * 
     * @return int
     */
    public int getAttributePermissionTypeCount()
    {
        return _attributePermissionTypeList.size();
    } //-- int getAttributePermissionTypeCount() 

    /**
     * Method getAttributeRule
     * 
     * 
     * 
     * @param index
     * @return AttributeRule
     */
    public org.aplana.br4j.dynamicaccess.xmldef.AttributeRule getAttributeRule(int index)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _attributeRuleList.size())) {
            throw new IndexOutOfBoundsException();
        }
        
        return (org.aplana.br4j.dynamicaccess.xmldef.AttributeRule) _attributeRuleList.elementAt(index);
    } //-- org.aplana.br4j.dynamicaccess.xmldef.AttributeRule getAttributeRule(int) 

    /**
     * Method getAttributeRule
     * 
     * 
     * 
     * @return AttributeRule
     */
    public org.aplana.br4j.dynamicaccess.xmldef.AttributeRule[] getAttributeRule()
    {
        int size = _attributeRuleList.size();
        org.aplana.br4j.dynamicaccess.xmldef.AttributeRule[] mArray = new org.aplana.br4j.dynamicaccess.xmldef.AttributeRule[size];
        for (int index = 0; index < size; index++) {
            mArray[index] = (org.aplana.br4j.dynamicaccess.xmldef.AttributeRule) _attributeRuleList.elementAt(index);
        }
        return mArray;
    } //-- org.aplana.br4j.dynamicaccess.xmldef.AttributeRule[] getAttributeRule() 

    /**
     * Method getAttributeRuleCount
     * 
     * 
     * 
     * @return int
     */
    public int getAttributeRuleCount()
    {
        return _attributeRuleList.size();
    } //-- int getAttributeRuleCount() 

    /**
     * Returns the value of field 'name'.
     * 
     * @return String
     * @return the value of field 'name'.
     */
    public java.lang.String getName()
    {
        return this._name;
    } //-- java.lang.String getName() 

    /**
     * Method getPermission
     * 
     * 
     * 
     * @param index
     * @return Permission
     */
    public org.aplana.br4j.dynamicaccess.xmldef.Permission getPermission(int index)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _permissionList.size())) {
            throw new IndexOutOfBoundsException();
        }
        
        return (org.aplana.br4j.dynamicaccess.xmldef.Permission) _permissionList.elementAt(index);
    } //-- org.aplana.br4j.dynamicaccess.xmldef.Permission getPermission(int) 

    /**
     * Method getPermission
     * 
     * 
     * 
     * @return Permission
     */
    public org.aplana.br4j.dynamicaccess.xmldef.Permission[] getPermission()
    {
        int size = _permissionList.size();
        org.aplana.br4j.dynamicaccess.xmldef.Permission[] mArray = new org.aplana.br4j.dynamicaccess.xmldef.Permission[size];
        for (int index = 0; index < size; index++) {
            mArray[index] = (org.aplana.br4j.dynamicaccess.xmldef.Permission) _permissionList.elementAt(index);
        }
        return mArray;
    } //-- org.aplana.br4j.dynamicaccess.xmldef.Permission[] getPermission() 

    /**
     * Method getPermissionCount
     * 
     * 
     * 
     * @return int
     */
    public int getPermissionCount()
    {
        return _permissionList.size();
    } //-- int getPermissionCount() 

    /**
     * Returns the value of field 'rules'. The field 'rules' has
     * the following description: �������� ������ ���� �������
     * 
     * @return Rules
     * @return the value of field 'rules'.
     */
    public org.aplana.br4j.dynamicaccess.xmldef.Rules getRules()
    {
        return this._rules;
    } //-- org.aplana.br4j.dynamicaccess.xmldef.Rules getRules() 

    /**
     * Method getStatus
     * 
     * 
     * 
     * @param index
     * @return Status
     */
    public org.aplana.br4j.dynamicaccess.xmldef.Status getStatus(int index)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _statusList.size())) {
            throw new IndexOutOfBoundsException();
        }
        
        return (org.aplana.br4j.dynamicaccess.xmldef.Status) _statusList.elementAt(index);
    } //-- org.aplana.br4j.dynamicaccess.xmldef.Status getStatus(int) 

    /**
     * Method getStatus
     * 
     * 
     * 
     * @return Status
     */
    public org.aplana.br4j.dynamicaccess.xmldef.Status[] getStatus()
    {
        int size = _statusList.size();
        org.aplana.br4j.dynamicaccess.xmldef.Status[] mArray = new org.aplana.br4j.dynamicaccess.xmldef.Status[size];
        for (int index = 0; index < size; index++) {
            mArray[index] = (org.aplana.br4j.dynamicaccess.xmldef.Status) _statusList.elementAt(index);
        }
        return mArray;
    } //-- org.aplana.br4j.dynamicaccess.xmldef.Status[] getStatus() 

    /**
     * Method getStatusCount
     * 
     * 
     * 
     * @return int
     */
    public int getStatusCount()
    {
        return _statusList.size();
    } //-- int getStatusCount() 

    /**
     * Returns the value of field 'template_id'.
     * 
     * @return String
     * @return the value of field 'template_id'.
     */
    public java.lang.String getTemplate_id()
    {
        return this._template_id;
    } //-- java.lang.String getTemplate_id() 

    public java.lang.Long getTemplateIdLong()
    {
        return Long.parseLong(this._template_id);
    }
    
    /**
     * Method getWFMoveType
     * 
     * 
     * 
     * @param index
     * @return WFMoveType
     */
    public org.aplana.br4j.dynamicaccess.xmldef.WFMoveType getWFMoveType(int index)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _WFMoveTypeList.size())) {
            throw new IndexOutOfBoundsException();
        }
        
        return (org.aplana.br4j.dynamicaccess.xmldef.WFMoveType) _WFMoveTypeList.elementAt(index);
    } //-- org.aplana.br4j.dynamicaccess.xmldef.WFMoveType getWFMoveType(int) 

    /**
     * Method getWFMoveType
     * 
     * 
     * 
     * @return WFMoveType
     */
    public org.aplana.br4j.dynamicaccess.xmldef.WFMoveType[] getWFMoveType()
    {
        int size = _WFMoveTypeList.size();
        org.aplana.br4j.dynamicaccess.xmldef.WFMoveType[] mArray = new org.aplana.br4j.dynamicaccess.xmldef.WFMoveType[size];
        for (int index = 0; index < size; index++) {
            mArray[index] = (org.aplana.br4j.dynamicaccess.xmldef.WFMoveType) _WFMoveTypeList.elementAt(index);
        }
        return mArray;
    } //-- org.aplana.br4j.dynamicaccess.xmldef.WFMoveType[] getWFMoveType() 

    /**
     * Method getWFMoveTypeCount
     * 
     * 
     * 
     * @return int
     */
    public int getWFMoveTypeCount()
    {
        return _WFMoveTypeList.size();
    } //-- int getWFMoveTypeCount() 

    /**
     * Method isValid
     * 
     * 
     * 
     * @return boolean
     */
    public boolean isValid()
    {
        try {
            validate();
        }
        catch (org.exolab.castor.xml.ValidationException vex) {
            return false;
        }
        return true;
    } //-- boolean isValid() 

    /**
     * Method marshal
     * 
     * 
     * 
     * @param out
     */
    public void marshal(java.io.Writer out)
        throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException
    {
        
        Marshaller.marshal(this, out);
    } //-- void marshal(java.io.Writer) 

    /**
     * Method marshal
     * 
     * 
     * 
     * @param handler
     */
    public void marshal(org.xml.sax.ContentHandler handler)
        throws java.io.IOException, org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException
    {
        
        Marshaller.marshal(this, handler);
    } //-- void marshal(org.xml.sax.ContentHandler) 

    /**
     * Method removeAllAttributePermissionType
     * 
     */
    public void removeAllAttributePermissionType()
    {
        _attributePermissionTypeList.removeAllElements();
    } //-- void removeAllAttributePermissionType() 

    /**
     * Method removeAllAttributeRule
     * 
     */
    public void removeAllAttributeRule()
    {
        _attributeRuleList.removeAllElements();
    } //-- void removeAllAttributeRule() 

    /**
     * Method removeAllPermission
     * 
     */
    public void removeAllPermission()
    {
        _permissionList.removeAllElements();
    } //-- void removeAllPermission() 

    /**
     * Method removeAllStatus
     * 
     */
    public void removeAllStatus()
    {
        _statusList.removeAllElements();
    } //-- void removeAllStatus() 

    /**
     * Method removeAllWFMoveType
     * 
     */
    public void removeAllWFMoveType()
    {
        _WFMoveTypeList.removeAllElements();
    } //-- void removeAllWFMoveType() 

    /**
     * Method removeAttributePermissionType
     * 
     * 
     * 
     * @param index
     * @return AttributePermissionType
     */
    public org.aplana.br4j.dynamicaccess.xmldef.AttributePermissionType removeAttributePermissionType(int index)
    {
        java.lang.Object obj = _attributePermissionTypeList.elementAt(index);
        _attributePermissionTypeList.removeElementAt(index);
        return (org.aplana.br4j.dynamicaccess.xmldef.AttributePermissionType) obj;
    } //-- org.aplana.br4j.dynamicaccess.xmldef.AttributePermissionType removeAttributePermissionType(int) 

    /**
     * Method removeAttributeRule
     * 
     * 
     * 
     * @param index
     * @return AttributeRule
     */
    public org.aplana.br4j.dynamicaccess.xmldef.AttributeRule removeAttributeRule(int index)
    {
        java.lang.Object obj = _attributeRuleList.elementAt(index);
        _attributeRuleList.removeElementAt(index);
        return (org.aplana.br4j.dynamicaccess.xmldef.AttributeRule) obj;
    } //-- org.aplana.br4j.dynamicaccess.xmldef.AttributeRule removeAttributeRule(int) 

    /**
     * Method removePermission
     * 
     * 
     * 
     * @param index
     * @return Permission
     */
    public org.aplana.br4j.dynamicaccess.xmldef.Permission removePermission(int index)
    {
        java.lang.Object obj = _permissionList.elementAt(index);
        _permissionList.removeElementAt(index);
        return (org.aplana.br4j.dynamicaccess.xmldef.Permission) obj;
    } //-- org.aplana.br4j.dynamicaccess.xmldef.Permission removePermission(int) 

    /**
     * Method removeStatus
     * 
     * 
     * 
     * @param index
     * @return Status
     */
    public org.aplana.br4j.dynamicaccess.xmldef.Status removeStatus(int index)
    {
        java.lang.Object obj = _statusList.elementAt(index);
        _statusList.removeElementAt(index);
        return (org.aplana.br4j.dynamicaccess.xmldef.Status) obj;
    } //-- org.aplana.br4j.dynamicaccess.xmldef.Status removeStatus(int) 

    /**
     * Method removeWFMoveType
     * 
     * 
     * 
     * @param index
     * @return WFMoveType
     */
    public org.aplana.br4j.dynamicaccess.xmldef.WFMoveType removeWFMoveType(int index)
    {
        java.lang.Object obj = _WFMoveTypeList.elementAt(index);
        _WFMoveTypeList.removeElementAt(index);
        return (org.aplana.br4j.dynamicaccess.xmldef.WFMoveType) obj;
    } //-- org.aplana.br4j.dynamicaccess.xmldef.WFMoveType removeWFMoveType(int) 

    /**
     * Method setAttributePermissionType
     * 
     * 
     * 
     * @param index
     * @param vAttributePermissionType
     */
    public void setAttributePermissionType(int index, org.aplana.br4j.dynamicaccess.xmldef.AttributePermissionType vAttributePermissionType)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _attributePermissionTypeList.size())) {
            throw new IndexOutOfBoundsException();
        }
        _attributePermissionTypeList.setElementAt(vAttributePermissionType, index);
    } //-- void setAttributePermissionType(int, org.aplana.br4j.dynamicaccess.xmldef.AttributePermissionType) 

    /**
     * Method setAttributePermissionType
     * 
     * 
     * 
     * @param attributePermissionTypeArray
     */
    public void setAttributePermissionType(org.aplana.br4j.dynamicaccess.xmldef.AttributePermissionType[] attributePermissionTypeArray)
    {
        //-- copy array
        _attributePermissionTypeList.removeAllElements();
        for (int i = 0; i < attributePermissionTypeArray.length; i++) {
            _attributePermissionTypeList.addElement(attributePermissionTypeArray[i]);
        }
    } //-- void setAttributePermissionType(org.aplana.br4j.dynamicaccess.xmldef.AttributePermissionType) 

    /**
     * Method setAttributeRule
     * 
     * 
     * 
     * @param index
     * @param vAttributeRule
     */
    public void setAttributeRule(int index, org.aplana.br4j.dynamicaccess.xmldef.AttributeRule vAttributeRule)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _attributeRuleList.size())) {
            throw new IndexOutOfBoundsException();
        }
        _attributeRuleList.setElementAt(vAttributeRule, index);
    } //-- void setAttributeRule(int, org.aplana.br4j.dynamicaccess.xmldef.AttributeRule) 

    /**
     * Method setAttributeRule
     * 
     * 
     * 
     * @param attributeRuleArray
     */
    public void setAttributeRule(org.aplana.br4j.dynamicaccess.xmldef.AttributeRule[] attributeRuleArray)
    {
        //-- copy array
        _attributeRuleList.removeAllElements();
        for (int i = 0; i < attributeRuleArray.length; i++) {
            _attributeRuleList.addElement(attributeRuleArray[i]);
        }
    } //-- void setAttributeRule(org.aplana.br4j.dynamicaccess.xmldef.AttributeRule) 

    /**
     * Sets the value of field 'name'.
     * 
     * @param name the value of field 'name'.
     */
    public void setName(java.lang.String name)
    {
        this._name = name;
    } //-- void setName(java.lang.String) 

    /**
     * Method setPermission
     * 
     * 
     * 
     * @param index
     * @param vPermission
     */
    public void setPermission(int index, org.aplana.br4j.dynamicaccess.xmldef.Permission vPermission)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _permissionList.size())) {
            throw new IndexOutOfBoundsException();
        }
        _permissionList.setElementAt(vPermission, index);
    } //-- void setPermission(int, org.aplana.br4j.dynamicaccess.xmldef.Permission) 

    /**
     * Method setPermission
     * 
     * 
     * 
     * @param permissionArray
     */
    public void setPermission(org.aplana.br4j.dynamicaccess.xmldef.Permission[] permissionArray)
    {
        //-- copy array
        _permissionList.removeAllElements();
        for (int i = 0; i < permissionArray.length; i++) {
            _permissionList.addElement(permissionArray[i]);
        }
    } //-- void setPermission(org.aplana.br4j.dynamicaccess.xmldef.Permission) 

    /**
     * Sets the value of field 'rules'. The field 'rules' has the
     * following description: �������� ������ ���� �������
     * 
     * @param rules the value of field 'rules'.
     */
    public void setRules(org.aplana.br4j.dynamicaccess.xmldef.Rules rules)
    {
        this._rules = rules;
    } //-- void setRules(org.aplana.br4j.dynamicaccess.xmldef.Rules) 

    /**
     * Method setStatus
     * 
     * 
     * 
     * @param index
     * @param vStatus
     */
    public void setStatus(int index, org.aplana.br4j.dynamicaccess.xmldef.Status vStatus)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _statusList.size())) {
            throw new IndexOutOfBoundsException();
        }
        _statusList.setElementAt(vStatus, index);
    } //-- void setStatus(int, org.aplana.br4j.dynamicaccess.xmldef.Status) 

    /**
     * Method setStatus
     * 
     * 
     * 
     * @param statusArray
     */
    public void setStatus(org.aplana.br4j.dynamicaccess.xmldef.Status[] statusArray)
    {
        //-- copy array
        _statusList.removeAllElements();
        for (int i = 0; i < statusArray.length; i++) {
            _statusList.addElement(statusArray[i]);
        }
    } //-- void setStatus(org.aplana.br4j.dynamicaccess.xmldef.Status) 

    /**
     * Sets the value of field 'template_id'.
     * 
     * @param template_id the value of field 'template_id'.
     */
    public void setTemplate_id(java.lang.String template_id)
    {
        this._template_id = template_id;
    } //-- void setTemplate_id(java.lang.String) 

    /**
     * Method setWFMoveType
     * 
     * 
     * 
     * @param index
     * @param vWFMoveType
     */
    public void setWFMoveType(int index, org.aplana.br4j.dynamicaccess.xmldef.WFMoveType vWFMoveType)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _WFMoveTypeList.size())) {
            throw new IndexOutOfBoundsException();
        }
        _WFMoveTypeList.setElementAt(vWFMoveType, index);
    } //-- void setWFMoveType(int, org.aplana.br4j.dynamicaccess.xmldef.WFMoveType) 

    /**
     * Method setWFMoveType
     * 
     * 
     * 
     * @param WFMoveTypeArray
     */
    public void setWFMoveType(org.aplana.br4j.dynamicaccess.xmldef.WFMoveType[] WFMoveTypeArray)
    {
        //-- copy array
        _WFMoveTypeList.removeAllElements();
        for (int i = 0; i < WFMoveTypeArray.length; i++) {
            _WFMoveTypeList.addElement(WFMoveTypeArray[i]);
        }
    } //-- void setWFMoveType(org.aplana.br4j.dynamicaccess.xmldef.WFMoveType) 

    /**
     * Method unmarshal
     * 
     * 
     * 
     * @param reader
     * @return Object
     */
    public static java.lang.Object unmarshal(java.io.Reader reader)
        throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException
    {
        return (org.aplana.br4j.dynamicaccess.xmldef.Template) Unmarshaller.unmarshal(org.aplana.br4j.dynamicaccess.xmldef.Template.class, reader);
    } //-- java.lang.Object unmarshal(java.io.Reader) 

    /**
     * Method validate
     * 
     */
    public void validate()
        throws org.exolab.castor.xml.ValidationException
    {
        org.exolab.castor.xml.Validator validator = new org.exolab.castor.xml.Validator();
        validator.validate(this);
    } //-- void validate() 

    @Override
    public String toString() {
    	// TODO Auto-generated method stub
    	return "id: " + _template_id + ", name: " + _name;
    }


	public void setPermission(Set<Permission> set) {
		this._permissionList.clear();
		this._permissionList.addAll(set);
		
	}


	public Vector<Permission> getPermissionList() {
		return this._permissionList;
		
	}


	@Override
	public boolean equals(Object obj) {
		if(obj instanceof Template){
			Template template = (Template) obj;
			return template.getTemplate_id().equals(this.getTemplate_id());
		} else {
			return false;
		}
	}
}
