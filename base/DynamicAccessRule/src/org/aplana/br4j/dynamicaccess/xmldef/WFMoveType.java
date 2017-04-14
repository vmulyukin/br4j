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
 * ������ ����� ���������
 * 
 * @version $Revision$ $Date$
 */
public class WFMoveType implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _name
     */
    private java.lang.String _name;

    /**
     * Field _wfm_id
     */
    private java.lang.String _wfm_id;

    /**
     * Field _wfm_from
     */
    private java.lang.String _wfm_from;

    /**
     * Field _wfm_to
     */
    private java.lang.String _wfm_to;

    /**
     * Field _wfm_from_status
     */
    private java.lang.String _wfm_from_status;

    /**
     * Field _wfm_to_status
     */
    private java.lang.String _wfm_to_status;


      //----------------/
     //- Constructors -/
    //----------------/

    public WFMoveType() {
        super();
    } //-- org.aplana.br4j.dynamicaccess.xmldef.WFMoveType()


      //-----------/
     //- Methods -/
    //-----------/

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
     * Returns the value of field 'wfm_from'.
     * 
     * @return String
     * @return the value of field 'wfm_from'.
     */
    public java.lang.String getWfm_from()
    {
        return this._wfm_from;
    } //-- java.lang.String getWfm_from() 

    /**
     * Returns the value of field 'wfm_from_status'.
     * 
     * @return String
     * @return the value of field 'wfm_from_status'.
     */
    public java.lang.String getWfm_from_status()
    {
        return this._wfm_from_status;
    } //-- java.lang.String getWfm_from_status() 

    /**
     * Returns the value of field 'wfm_id'.
     * 
     * @return String
     * @return the value of field 'wfm_id'.
     */
    public java.lang.String getWfm_id()
    {
        return this._wfm_id;
    } //-- java.lang.String getWfm_id() 

    /**
     * Returns the value of field 'wfm_to'.
     * 
     * @return String
     * @return the value of field 'wfm_to'.
     */
    public java.lang.String getWfm_to()
    {
        return this._wfm_to;
    } //-- java.lang.String getWfm_to() 

    /**
     * Returns the value of field 'wfm_to_status'.
     * 
     * @return String
     * @return the value of field 'wfm_to_status'.
     */
    public java.lang.String getWfm_to_status()
    {
        return this._wfm_to_status;
    } //-- java.lang.String getWfm_to_status() 

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
     * Sets the value of field 'name'.
     * 
     * @param name the value of field 'name'.
     */
    public void setName(java.lang.String name)
    {
        this._name = name;
    } //-- void setName(java.lang.String) 

    /**
     * Sets the value of field 'wfm_from'.
     * 
     * @param wfm_from the value of field 'wfm_from'.
     */
    public void setWfm_from(java.lang.String wfm_from)
    {
        this._wfm_from = wfm_from;
    } //-- void setWfm_from(java.lang.String) 

    /**
     * Sets the value of field 'wfm_from_status'.
     * 
     * @param wfm_from_status the value of field 'wfm_from_status'.
     */
    public void setWfm_from_status(java.lang.String wfm_from_status)
    {
        this._wfm_from_status = wfm_from_status;
    } //-- void setWfm_from_status(java.lang.String) 

    /**
     * Sets the value of field 'wfm_id'.
     * 
     * @param wfm_id the value of field 'wfm_id'.
     */
    public void setWfm_id(java.lang.String wfm_id)
    {
        this._wfm_id = wfm_id;
    } //-- void setWfm_id(java.lang.String) 

    /**
     * Sets the value of field 'wfm_to'.
     * 
     * @param wfm_to the value of field 'wfm_to'.
     */
    public void setWfm_to(java.lang.String wfm_to)
    {
        this._wfm_to = wfm_to;
    } //-- void setWfm_to(java.lang.String) 

    /**
     * Sets the value of field 'wfm_to_status'.
     * 
     * @param wfm_to_status the value of field 'wfm_to_status'.
     */
    public void setWfm_to_status(java.lang.String wfm_to_status)
    {
        this._wfm_to_status = wfm_to_status;
    } //-- void setWfm_to_status(java.lang.String) 

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
        return (org.aplana.br4j.dynamicaccess.xmldef.WFMoveType) Unmarshaller.unmarshal(org.aplana.br4j.dynamicaccess.xmldef.WFMoveType.class, reader);
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
