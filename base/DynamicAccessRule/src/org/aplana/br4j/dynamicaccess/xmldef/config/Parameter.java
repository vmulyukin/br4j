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
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.ValidationException;
import org.xml.sax.ContentHandler;

/**
 * ��������
 * 
 * @version $Revision$ $Date$
 */
public class Parameter implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _name
     */
    private java.lang.Object _name;

    /**
     * Field _comment
     */
    private java.lang.Object _comment;

    /**
     * ������� �������� ���� ���/��������
     */
    private org.aplana.br4j.dynamicaccess.xmldef.config.SimpleParameter _simpleParameter;

    /**
     * �������� ���� "������ ���������"
     */
    private org.aplana.br4j.dynamicaccess.xmldef.config.AttributeList _attributeList;

    /**
     * ������ ����������
     */
    private org.aplana.br4j.dynamicaccess.xmldef.config.ParameterGroup _parameterGroup;


      //----------------/
     //- Constructors -/
    //----------------/

    public Parameter() {
        super();
    } //-- org.aplana.br4j.dynamicaccess.xmldef.config.Parameter()


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Returns the value of field 'attributeList'. The field
     * 'attributeList' has the following description: �������� ����
     * "������ ���������"
     * 
     * @return AttributeList
     * @return the value of field 'attributeList'.
     */
    public org.aplana.br4j.dynamicaccess.xmldef.config.AttributeList getAttributeList()
    {
        return this._attributeList;
    } //-- org.aplana.br4j.dynamicaccess.xmldef.config.AttributeList getAttributeList() 

    /**
     * Returns the value of field 'comment'.
     * 
     * @return Object
     * @return the value of field 'comment'.
     */
    public java.lang.Object getComment()
    {
        return this._comment;
    } //-- java.lang.Object getComment() 

    /**
     * Returns the value of field 'name'.
     * 
     * @return Object
     * @return the value of field 'name'.
     */
    public java.lang.Object getName()
    {
        return this._name;
    } //-- java.lang.Object getName() 

    /**
     * Returns the value of field 'parameterGroup'. The field
     * 'parameterGroup' has the following description: ������
     * ����������
     * 
     * @return ParameterGroup
     * @return the value of field 'parameterGroup'.
     */
    public org.aplana.br4j.dynamicaccess.xmldef.config.ParameterGroup getParameterGroup()
    {
        return this._parameterGroup;
    } //-- org.aplana.br4j.dynamicaccess.xmldef.config.ParameterGroup getParameterGroup() 

    /**
     * Returns the value of field 'simpleParameter'. The field
     * 'simpleParameter' has the following description: �������
     * �������� ���� ���/��������
     * 
     * @return SimpleParameter
     * @return the value of field 'simpleParameter'.
     */
    public org.aplana.br4j.dynamicaccess.xmldef.config.SimpleParameter getSimpleParameter()
    {
        return this._simpleParameter;
    } //-- org.aplana.br4j.dynamicaccess.xmldef.config.SimpleParameter getSimpleParameter() 

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
     * Sets the value of field 'attributeList'. The field
     * 'attributeList' has the following description: �������� ����
     * "������ ���������"
     * 
     * @param attributeList the value of field 'attributeList'.
     */
    public void setAttributeList(org.aplana.br4j.dynamicaccess.xmldef.config.AttributeList attributeList)
    {
        this._attributeList = attributeList;
    } //-- void setAttributeList(org.aplana.br4j.dynamicaccess.xmldef.config.AttributeList) 

    /**
     * Sets the value of field 'comment'.
     * 
     * @param comment the value of field 'comment'.
     */
    public void setComment(java.lang.Object comment)
    {
        this._comment = comment;
    } //-- void setComment(java.lang.Object) 

    /**
     * Sets the value of field 'name'.
     * 
     * @param name the value of field 'name'.
     */
    public void setName(java.lang.Object name)
    {
        this._name = name;
    } //-- void setName(java.lang.Object) 

    /**
     * Sets the value of field 'parameterGroup'. The field
     * 'parameterGroup' has the following description: ������
     * ����������
     * 
     * @param parameterGroup the value of field 'parameterGroup'.
     */
    public void setParameterGroup(org.aplana.br4j.dynamicaccess.xmldef.config.ParameterGroup parameterGroup)
    {
        this._parameterGroup = parameterGroup;
    } //-- void setParameterGroup(org.aplana.br4j.dynamicaccess.xmldef.config.ParameterGroup) 

    /**
     * Sets the value of field 'simpleParameter'. The field
     * 'simpleParameter' has the following description: �������
     * �������� ���� ���/��������
     * 
     * @param simpleParameter the value of field 'simpleParameter'.
     */
    public void setSimpleParameter(org.aplana.br4j.dynamicaccess.xmldef.config.SimpleParameter simpleParameter)
    {
        this._simpleParameter = simpleParameter;
    } //-- void setSimpleParameter(org.aplana.br4j.dynamicaccess.xmldef.config.SimpleParameter) 

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
        return (org.aplana.br4j.dynamicaccess.xmldef.config.Parameter) Unmarshaller.unmarshal(org.aplana.br4j.dynamicaccess.xmldef.config.Parameter.class, reader);
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
