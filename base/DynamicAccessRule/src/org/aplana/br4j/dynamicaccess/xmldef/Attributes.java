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
import java.util.Vector;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.ValidationException;
import org.xml.sax.ContentHandler;

/**
 * �������� ��������� ��� ����������� ������� � ����
 * 
 * @version $Revision$ $Date$
 */
public class Attributes implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * ������� ���������� ���������� � ��������
     */
    private java.util.Vector _attributeList;


      //----------------/
     //- Constructors -/
    //----------------/

    public Attributes() {
        super();
        _attributeList = new Vector();
    } //-- org.aplana.br4j.dynamicaccess.xmldef.Attributes()


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Method addAttribute
     * 
     * 
     * 
     * @param vAttribute
     */
    public void addAttribute(org.aplana.br4j.dynamicaccess.xmldef.Attribute vAttribute)
        throws java.lang.IndexOutOfBoundsException
    {
        _attributeList.addElement(vAttribute);
    } //-- void addAttribute(org.aplana.br4j.dynamicaccess.xmldef.Attribute) 

    /**
     * Method addAttribute
     * 
     * 
     * 
     * @param index
     * @param vAttribute
     */
    public void addAttribute(int index, org.aplana.br4j.dynamicaccess.xmldef.Attribute vAttribute)
        throws java.lang.IndexOutOfBoundsException
    {
        _attributeList.insertElementAt(vAttribute, index);
    } //-- void addAttribute(int, org.aplana.br4j.dynamicaccess.xmldef.Attribute) 

    /**
     * Method enumerateAttribute
     * 
     * 
     * 
     * @return Enumeration
     */
    public java.util.Enumeration enumerateAttribute()
    {
        return _attributeList.elements();
    } //-- java.util.Enumeration enumerateAttribute() 

    /**
     * Method getAttribute
     * 
     * 
     * 
     * @param index
     * @return Attribute
     */
    public org.aplana.br4j.dynamicaccess.xmldef.Attribute getAttribute(int index)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _attributeList.size())) {
            throw new IndexOutOfBoundsException();
        }
        
        return (org.aplana.br4j.dynamicaccess.xmldef.Attribute) _attributeList.elementAt(index);
    } //-- org.aplana.br4j.dynamicaccess.xmldef.Attribute getAttribute(int) 

    /**
     * Method getAttribute
     * 
     * 
     * 
     * @return Attribute
     */
    public org.aplana.br4j.dynamicaccess.xmldef.Attribute[] getAttribute()
    {
        int size = _attributeList.size();
        org.aplana.br4j.dynamicaccess.xmldef.Attribute[] mArray = new org.aplana.br4j.dynamicaccess.xmldef.Attribute[size];
        for (int index = 0; index < size; index++) {
            mArray[index] = (org.aplana.br4j.dynamicaccess.xmldef.Attribute) _attributeList.elementAt(index);
        }
        return mArray;
    } //-- org.aplana.br4j.dynamicaccess.xmldef.Attribute[] getAttribute() 

    /**
     * Method getAttributeCount
     * 
     * 
     * 
     * @return int
     */
    public int getAttributeCount()
    {
        return _attributeList.size();
    } //-- int getAttributeCount() 

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
     * Method removeAllAttribute
     * 
     */
    public void removeAllAttribute()
    {
        _attributeList.removeAllElements();
    } //-- void removeAllAttribute() 

    /**
     * Method removeAttribute
     * 
     * 
     * 
     * @param index
     * @return Attribute
     */
    public org.aplana.br4j.dynamicaccess.xmldef.Attribute removeAttribute(int index)
    {
        java.lang.Object obj = _attributeList.elementAt(index);
        _attributeList.removeElementAt(index);
        return (org.aplana.br4j.dynamicaccess.xmldef.Attribute) obj;
    } //-- org.aplana.br4j.dynamicaccess.xmldef.Attribute removeAttribute(int) 

    /**
     * Method setAttribute
     * 
     * 
     * 
     * @param index
     * @param vAttribute
     */
    public void setAttribute(int index, org.aplana.br4j.dynamicaccess.xmldef.Attribute vAttribute)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _attributeList.size())) {
            throw new IndexOutOfBoundsException();
        }
        _attributeList.setElementAt(vAttribute, index);
    } //-- void setAttribute(int, org.aplana.br4j.dynamicaccess.xmldef.Attribute) 

    /**
     * Method setAttribute
     * 
     * 
     * 
     * @param attributeArray
     */
    public void setAttribute(org.aplana.br4j.dynamicaccess.xmldef.Attribute[] attributeArray)
    {
        //-- copy array
        _attributeList.removeAllElements();
        for (int i = 0; i < attributeArray.length; i++) {
            _attributeList.addElement(attributeArray[i]);
        }
    } //-- void setAttribute(org.aplana.br4j.dynamicaccess.xmldef.Attribute) 

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
        return (org.aplana.br4j.dynamicaccess.xmldef.Attributes) Unmarshaller.unmarshal(org.aplana.br4j.dynamicaccess.xmldef.Attributes.class, reader);
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
