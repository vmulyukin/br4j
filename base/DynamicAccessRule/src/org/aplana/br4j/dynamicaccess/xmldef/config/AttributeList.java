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
 * �������� ���� "������ ���������"
 * 
 * @version $Revision$ $Date$
 */
public class AttributeList implements java.io.Serializable {


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

    public AttributeList() {
        super();
        _items = new Vector();
    } //-- org.aplana.br4j.dynamicaccess.xmldef.config.AttributeList()


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Method addAttributeListItem
     * 
     * 
     * 
     * @param vAttributeListItem
     */
    public void addAttributeListItem(org.aplana.br4j.dynamicaccess.xmldef.config.AttributeListItem vAttributeListItem)
        throws java.lang.IndexOutOfBoundsException
    {
        _items.addElement(vAttributeListItem);
    } //-- void addAttributeListItem(org.aplana.br4j.dynamicaccess.xmldef.config.AttributeListItem) 

    /**
     * Method addAttributeListItem
     * 
     * 
     * 
     * @param index
     * @param vAttributeListItem
     */
    public void addAttributeListItem(int index, org.aplana.br4j.dynamicaccess.xmldef.config.AttributeListItem vAttributeListItem)
        throws java.lang.IndexOutOfBoundsException
    {
        _items.insertElementAt(vAttributeListItem, index);
    } //-- void addAttributeListItem(int, org.aplana.br4j.dynamicaccess.xmldef.config.AttributeListItem) 

    /**
     * Method enumerateAttributeListItem
     * 
     * 
     * 
     * @return Enumeration
     */
    public java.util.Enumeration enumerateAttributeListItem()
    {
        return _items.elements();
    } //-- java.util.Enumeration enumerateAttributeListItem() 

    /**
     * Method getAttributeListItem
     * 
     * 
     * 
     * @param index
     * @return AttributeListItem
     */
    public org.aplana.br4j.dynamicaccess.xmldef.config.AttributeListItem getAttributeListItem(int index)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _items.size())) {
            throw new IndexOutOfBoundsException();
        }
        
        return (org.aplana.br4j.dynamicaccess.xmldef.config.AttributeListItem) _items.elementAt(index);
    } //-- org.aplana.br4j.dynamicaccess.xmldef.config.AttributeListItem getAttributeListItem(int) 

    /**
     * Method getAttributeListItem
     * 
     * 
     * 
     * @return AttributeListItem
     */
    public org.aplana.br4j.dynamicaccess.xmldef.config.AttributeListItem[] getAttributeListItem()
    {
        int size = _items.size();
        org.aplana.br4j.dynamicaccess.xmldef.config.AttributeListItem[] mArray = new org.aplana.br4j.dynamicaccess.xmldef.config.AttributeListItem[size];
        for (int index = 0; index < size; index++) {
            mArray[index] = (org.aplana.br4j.dynamicaccess.xmldef.config.AttributeListItem) _items.elementAt(index);
        }
        return mArray;
    } //-- org.aplana.br4j.dynamicaccess.xmldef.config.AttributeListItem[] getAttributeListItem() 

    /**
     * Method getAttributeListItemCount
     * 
     * 
     * 
     * @return int
     */
    public int getAttributeListItemCount()
    {
        return _items.size();
    } //-- int getAttributeListItemCount() 

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
     * Method removeAllAttributeListItem
     * 
     */
    public void removeAllAttributeListItem()
    {
        _items.removeAllElements();
    } //-- void removeAllAttributeListItem() 

    /**
     * Method removeAttributeListItem
     * 
     * 
     * 
     * @param index
     * @return AttributeListItem
     */
    public org.aplana.br4j.dynamicaccess.xmldef.config.AttributeListItem removeAttributeListItem(int index)
    {
        java.lang.Object obj = _items.elementAt(index);
        _items.removeElementAt(index);
        return (org.aplana.br4j.dynamicaccess.xmldef.config.AttributeListItem) obj;
    } //-- org.aplana.br4j.dynamicaccess.xmldef.config.AttributeListItem removeAttributeListItem(int) 

    /**
     * Method setAttributeListItem
     * 
     * 
     * 
     * @param index
     * @param vAttributeListItem
     */
    public void setAttributeListItem(int index, org.aplana.br4j.dynamicaccess.xmldef.config.AttributeListItem vAttributeListItem)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _items.size())) {
            throw new IndexOutOfBoundsException();
        }
        _items.setElementAt(vAttributeListItem, index);
    } //-- void setAttributeListItem(int, org.aplana.br4j.dynamicaccess.xmldef.config.AttributeListItem) 

    /**
     * Method setAttributeListItem
     * 
     * 
     * 
     * @param attributeListItemArray
     */
    public void setAttributeListItem(org.aplana.br4j.dynamicaccess.xmldef.config.AttributeListItem[] attributeListItemArray)
    {
        //-- copy array
        _items.removeAllElements();
        for (int i = 0; i < attributeListItemArray.length; i++) {
            _items.addElement(attributeListItemArray[i]);
        }
    } //-- void setAttributeListItem(org.aplana.br4j.dynamicaccess.xmldef.config.AttributeListItem) 

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
        return (org.aplana.br4j.dynamicaccess.xmldef.config.AttributeList) Unmarshaller.unmarshal(org.aplana.br4j.dynamicaccess.xmldef.config.AttributeList.class, reader);
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
