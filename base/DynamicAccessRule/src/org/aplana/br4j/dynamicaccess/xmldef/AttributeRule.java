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
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.ValidationException;
import org.xml.sax.ContentHandler;

/**
 * ������ ��������� ������������� ����� � ����������� ��
 * 
 * @version $Revision$ $Date$
 */
public class AttributeRule implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _block_name_rus
     */
    private java.lang.String _block_name_rus;

    /**
     * Field _attribute_code
     */
    private java.lang.String _attribute_code;

    /**
     * Field _attr_name_rus
     */
    private java.lang.String _attr_name_rus;

    /**
     * Field _data_type
     */
    private java.lang.String _data_type;


      //----------------/
     //- Constructors -/
    //----------------/

    public AttributeRule() {
        super();
    } //-- org.aplana.br4j.dynamicaccess.xmldef.AttributeRule()


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Returns the value of field 'attr_name_rus'.
     * 
     * @return String
     * @return the value of field 'attr_name_rus'.
     */
    public java.lang.String getAttr_name_rus()
    {
        return this._attr_name_rus;
    } //-- java.lang.String getAttr_name_rus() 

    /**
     * Returns the value of field 'attribute_code'.
     * 
     * @return String
     * @return the value of field 'attribute_code'.
     */
    public java.lang.String getAttribute_code()
    {
        return this._attribute_code;
    } //-- java.lang.String getAttribute_code() 

    /**
     * Returns the value of field 'block_name_rus'.
     * 
     * @return String
     * @return the value of field 'block_name_rus'.
     */
    public java.lang.String getBlock_name_rus()
    {
        return this._block_name_rus;
    } //-- java.lang.String getBlock_name_rus() 

    /**
     * Returns the value of field 'data_type'.
     * 
     * @return String
     * @return the value of field 'data_type'.
     */
    public java.lang.String getData_type()
    {
        return this._data_type;
    } //-- java.lang.String getData_type() 

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
     * Sets the value of field 'attr_name_rus'.
     * 
     * @param attr_name_rus the value of field 'attr_name_rus'.
     */
    public void setAttr_name_rus(java.lang.String attr_name_rus)
    {
        this._attr_name_rus = attr_name_rus;
    } //-- void setAttr_name_rus(java.lang.String) 

    /**
     * Sets the value of field 'attribute_code'.
     * 
     * @param attribute_code the value of field 'attribute_code'.
     */
    public void setAttribute_code(java.lang.String attribute_code)
    {
        this._attribute_code = attribute_code;
    } //-- void setAttribute_code(java.lang.String) 

    /**
     * Sets the value of field 'block_name_rus'.
     * 
     * @param block_name_rus the value of field 'block_name_rus'.
     */
    public void setBlock_name_rus(java.lang.String block_name_rus)
    {
        this._block_name_rus = block_name_rus;
    } //-- void setBlock_name_rus(java.lang.String) 

    /**
     * Sets the value of field 'data_type'.
     * 
     * @param data_type the value of field 'data_type'.
     */
    public void setData_type(java.lang.String data_type)
    {
        this._data_type = data_type;
    } //-- void setData_type(java.lang.String) 

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
        return (org.aplana.br4j.dynamicaccess.xmldef.AttributeRule) Unmarshaller.unmarshal(org.aplana.br4j.dynamicaccess.xmldef.AttributeRule.class, reader);
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
