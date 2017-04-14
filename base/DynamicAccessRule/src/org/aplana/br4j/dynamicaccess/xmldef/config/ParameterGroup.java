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
 * ������ ����������
 * 
 * @version $Revision$ $Date$
 */
public class ParameterGroup implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _items
     */
    private java.util.Vector _items;


      //----------------/
     //- Constructors -/
    //----------------/

    public ParameterGroup() {
        super();
        _items = new Vector();
    } //-- org.aplana.br4j.dynamicaccess.xmldef.config.ParameterGroup()


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Method addParameterGroupItem
     * 
     * 
     * 
     * @param vParameterGroupItem
     */
    public void addParameterGroupItem(org.aplana.br4j.dynamicaccess.xmldef.config.ParameterGroupItem vParameterGroupItem)
        throws java.lang.IndexOutOfBoundsException
    {
        _items.addElement(vParameterGroupItem);
    } //-- void addParameterGroupItem(org.aplana.br4j.dynamicaccess.xmldef.config.ParameterGroupItem) 

    /**
     * Method addParameterGroupItem
     * 
     * 
     * 
     * @param index
     * @param vParameterGroupItem
     */
    public void addParameterGroupItem(int index, org.aplana.br4j.dynamicaccess.xmldef.config.ParameterGroupItem vParameterGroupItem)
        throws java.lang.IndexOutOfBoundsException
    {
        _items.insertElementAt(vParameterGroupItem, index);
    } //-- void addParameterGroupItem(int, org.aplana.br4j.dynamicaccess.xmldef.config.ParameterGroupItem) 

    /**
     * Method enumerateParameterGroupItem
     * 
     * 
     * 
     * @return Enumeration
     */
    public java.util.Enumeration enumerateParameterGroupItem()
    {
        return _items.elements();
    } //-- java.util.Enumeration enumerateParameterGroupItem() 

    /**
     * Method getParameterGroupItem
     * 
     * 
     * 
     * @param index
     * @return ParameterGroupItem
     */
    public org.aplana.br4j.dynamicaccess.xmldef.config.ParameterGroupItem getParameterGroupItem(int index)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _items.size())) {
            throw new IndexOutOfBoundsException();
        }
        
        return (org.aplana.br4j.dynamicaccess.xmldef.config.ParameterGroupItem) _items.elementAt(index);
    } //-- org.aplana.br4j.dynamicaccess.xmldef.config.ParameterGroupItem getParameterGroupItem(int) 

    /**
     * Method getParameterGroupItem
     * 
     * 
     * 
     * @return ParameterGroupItem
     */
    public org.aplana.br4j.dynamicaccess.xmldef.config.ParameterGroupItem[] getParameterGroupItem()
    {
        int size = _items.size();
        org.aplana.br4j.dynamicaccess.xmldef.config.ParameterGroupItem[] mArray = new org.aplana.br4j.dynamicaccess.xmldef.config.ParameterGroupItem[size];
        for (int index = 0; index < size; index++) {
            mArray[index] = (org.aplana.br4j.dynamicaccess.xmldef.config.ParameterGroupItem) _items.elementAt(index);
        }
        return mArray;
    } //-- org.aplana.br4j.dynamicaccess.xmldef.config.ParameterGroupItem[] getParameterGroupItem() 

    /**
     * Method getParameterGroupItemCount
     * 
     * 
     * 
     * @return int
     */
    public int getParameterGroupItemCount()
    {
        return _items.size();
    } //-- int getParameterGroupItemCount() 

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
     * Method removeAllParameterGroupItem
     * 
     */
    public void removeAllParameterGroupItem()
    {
        _items.removeAllElements();
    } //-- void removeAllParameterGroupItem() 

    /**
     * Method removeParameterGroupItem
     * 
     * 
     * 
     * @param index
     * @return ParameterGroupItem
     */
    public org.aplana.br4j.dynamicaccess.xmldef.config.ParameterGroupItem removeParameterGroupItem(int index)
    {
        java.lang.Object obj = _items.elementAt(index);
        _items.removeElementAt(index);
        return (org.aplana.br4j.dynamicaccess.xmldef.config.ParameterGroupItem) obj;
    } //-- org.aplana.br4j.dynamicaccess.xmldef.config.ParameterGroupItem removeParameterGroupItem(int) 

    /**
     * Method setParameterGroupItem
     * 
     * 
     * 
     * @param index
     * @param vParameterGroupItem
     */
    public void setParameterGroupItem(int index, org.aplana.br4j.dynamicaccess.xmldef.config.ParameterGroupItem vParameterGroupItem)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _items.size())) {
            throw new IndexOutOfBoundsException();
        }
        _items.setElementAt(vParameterGroupItem, index);
    } //-- void setParameterGroupItem(int, org.aplana.br4j.dynamicaccess.xmldef.config.ParameterGroupItem) 

    /**
     * Method setParameterGroupItem
     * 
     * 
     * 
     * @param parameterGroupItemArray
     */
    public void setParameterGroupItem(org.aplana.br4j.dynamicaccess.xmldef.config.ParameterGroupItem[] parameterGroupItemArray)
    {
        //-- copy array
        _items.removeAllElements();
        for (int i = 0; i < parameterGroupItemArray.length; i++) {
            _items.addElement(parameterGroupItemArray[i]);
        }
    } //-- void setParameterGroupItem(org.aplana.br4j.dynamicaccess.xmldef.config.ParameterGroupItem) 

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
        return (org.aplana.br4j.dynamicaccess.xmldef.config.ParameterGroup) Unmarshaller.unmarshal(org.aplana.br4j.dynamicaccess.xmldef.config.ParameterGroup.class, reader);
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
