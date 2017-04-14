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

package org.aplana.br4j.dynamicaccess.xmldef.config;

  //---------------------------------/
 //- Imported classes and packages -/
//---------------------------------/

import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;
import java.io.Writer;
import java.util.Enumeration;
import java.util.Vector;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.ValidationException;
import org.xml.sax.ContentHandler;

/**
 * ���������������� ��������� ��� ������� Access Rule
 * 
 * @version $Revision$ $Date$
 */
public class AccessRulesConfig implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * ������ �����
     */
    private java.lang.Object _version;

    /**
     * Field _items
     */
    private java.util.Vector _items;


      //----------------/
     //- Constructors -/
    //----------------/

    public AccessRulesConfig() {
        super();
        _items = new Vector();
    } //-- org.aplana.br4j.dynamicaccess.xmldef.config.AccessRulesConfig()


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Method addAccessRulesConfigItem
     * 
     * 
     * 
     * @param vAccessRulesConfigItem
     */
    public void addAccessRulesConfigItem(org.aplana.br4j.dynamicaccess.xmldef.config.AccessRulesConfigItem vAccessRulesConfigItem)
        throws java.lang.IndexOutOfBoundsException
    {
        _items.addElement(vAccessRulesConfigItem);
    } //-- void addAccessRulesConfigItem(org.aplana.br4j.dynamicaccess.xmldef.config.AccessRulesConfigItem) 

    /**
     * Method addAccessRulesConfigItem
     * 
     * 
     * 
     * @param index
     * @param vAccessRulesConfigItem
     */
    public void addAccessRulesConfigItem(int index, org.aplana.br4j.dynamicaccess.xmldef.config.AccessRulesConfigItem vAccessRulesConfigItem)
        throws java.lang.IndexOutOfBoundsException
    {
        _items.insertElementAt(vAccessRulesConfigItem, index);
    } //-- void addAccessRulesConfigItem(int, org.aplana.br4j.dynamicaccess.xmldef.config.AccessRulesConfigItem) 

    /**
     * Method enumerateAccessRulesConfigItem
     * 
     * 
     * 
     * @return Enumeration
     */
    public java.util.Enumeration enumerateAccessRulesConfigItem()
    {
        return _items.elements();
    } //-- java.util.Enumeration enumerateAccessRulesConfigItem() 

    /**
     * Method getAccessRulesConfigItem
     * 
     * 
     * 
     * @param index
     * @return AccessRulesConfigItem
     */
    public org.aplana.br4j.dynamicaccess.xmldef.config.AccessRulesConfigItem getAccessRulesConfigItem(int index)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _items.size())) {
            throw new IndexOutOfBoundsException();
        }
        
        return (org.aplana.br4j.dynamicaccess.xmldef.config.AccessRulesConfigItem) _items.elementAt(index);
    } //-- org.aplana.br4j.dynamicaccess.xmldef.config.AccessRulesConfigItem getAccessRulesConfigItem(int) 

    /**
     * Method getAccessRulesConfigItem
     * 
     * 
     * 
     * @return AccessRulesConfigItem
     */
    public org.aplana.br4j.dynamicaccess.xmldef.config.AccessRulesConfigItem[] getAccessRulesConfigItem()
    {
        int size = _items.size();
        org.aplana.br4j.dynamicaccess.xmldef.config.AccessRulesConfigItem[] mArray = new org.aplana.br4j.dynamicaccess.xmldef.config.AccessRulesConfigItem[size];
        for (int index = 0; index < size; index++) {
            mArray[index] = (org.aplana.br4j.dynamicaccess.xmldef.config.AccessRulesConfigItem) _items.elementAt(index);
        }
        return mArray;
    } //-- org.aplana.br4j.dynamicaccess.xmldef.config.AccessRulesConfigItem[] getAccessRulesConfigItem() 

    /**
     * Method getAccessRulesConfigItemCount
     * 
     * 
     * 
     * @return int
     */
    public int getAccessRulesConfigItemCount()
    {
        return _items.size();
    } //-- int getAccessRulesConfigItemCount() 

    /**
     * Returns the value of field 'version'. The field 'version'
     * has the following description: ������ �����
     * 
     * @return Object
     * @return the value of field 'version'.
     */
    public java.lang.Object getVersion()
    {
        return this._version;
    } //-- java.lang.Object getVersion() 

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
     * Method removeAccessRulesConfigItem
     * 
     * 
     * 
     * @param index
     * @return AccessRulesConfigItem
     */
    public org.aplana.br4j.dynamicaccess.xmldef.config.AccessRulesConfigItem removeAccessRulesConfigItem(int index)
    {
        java.lang.Object obj = _items.elementAt(index);
        _items.removeElementAt(index);
        return (org.aplana.br4j.dynamicaccess.xmldef.config.AccessRulesConfigItem) obj;
    } //-- org.aplana.br4j.dynamicaccess.xmldef.config.AccessRulesConfigItem removeAccessRulesConfigItem(int) 

    /**
     * Method removeAllAccessRulesConfigItem
     * 
     */
    public void removeAllAccessRulesConfigItem()
    {
        _items.removeAllElements();
    } //-- void removeAllAccessRulesConfigItem() 

    /**
     * Method setAccessRulesConfigItem
     * 
     * 
     * 
     * @param index
     * @param vAccessRulesConfigItem
     */
    public void setAccessRulesConfigItem(int index, org.aplana.br4j.dynamicaccess.xmldef.config.AccessRulesConfigItem vAccessRulesConfigItem)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _items.size())) {
            throw new IndexOutOfBoundsException();
        }
        _items.setElementAt(vAccessRulesConfigItem, index);
    } //-- void setAccessRulesConfigItem(int, org.aplana.br4j.dynamicaccess.xmldef.config.AccessRulesConfigItem) 

    /**
     * Method setAccessRulesConfigItem
     * 
     * 
     * 
     * @param accessRulesConfigItemArray
     */
    public void setAccessRulesConfigItem(org.aplana.br4j.dynamicaccess.xmldef.config.AccessRulesConfigItem[] accessRulesConfigItemArray)
    {
        //-- copy array
        _items.removeAllElements();
        for (int i = 0; i < accessRulesConfigItemArray.length; i++) {
            _items.addElement(accessRulesConfigItemArray[i]);
        }
    } //-- void setAccessRulesConfigItem(org.aplana.br4j.dynamicaccess.xmldef.config.AccessRulesConfigItem) 

    /**
     * Sets the value of field 'version'. The field 'version' has
     * the following description: ������ �����
     * 
     * @param version the value of field 'version'.
     */
    public void setVersion(java.lang.Object version)
    {
        this._version = version;
    } //-- void setVersion(java.lang.Object) 

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
        return (org.aplana.br4j.dynamicaccess.xmldef.config.AccessRulesConfig) Unmarshaller.unmarshal(org.aplana.br4j.dynamicaccess.xmldef.config.AccessRulesConfig.class, reader);
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

}
