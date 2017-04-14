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

import org.apache.commons.codec.digest.DigestUtils;
import org.aplana.br4j.dynamicaccess.db_export.AccessRule;
import org.aplana.br4j.dynamicaccess.xmldef.types.Action;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.ValidationException;
import org.xml.sax.ContentHandler;

/**
 * ������� ���������� ���������� � ��������
 * 
 * @version $Revision$ $Date$
 */
public class WfMove implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _description
     */
    private java.lang.String _permHash;
    
    /**
     * Field _name
     */
    private java.lang.String _name;

    /**
     * Field _wfm_id
     */
    private java.lang.String _wfm_id;
    
    /**
     * Field _action
     */
    private org.aplana.br4j.dynamicaccess.xmldef.types.Action _action;

	private boolean auto;


      //----------------/
     //- Constructors -/
    //----------------/

    public WfMove() {
        super();
    } //-- org.aplana.br4j.dynamicaccess.xmldef.WfMove()
    

    public WfMove(String name, String wfm_id, Action action) {
        super();
        this._name = name;
        this._wfm_id = wfm_id;
        this._action = action;
        this.auto = true;
    } //-- org.aplana.br4j.dynamicaccess.xmldef.WfMove()


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
     * Sets the value of field 'wfm_id'.
     * 
     * @param wfm_id the value of field 'wfm_id'.
     */
    public void setWfm_id(java.lang.String wfm_id)
    {
        this._wfm_id = wfm_id;
    } //-- void setWfm_id(java.lang.String) 

	public java.lang.String getPermHash() {
		return _permHash;
	}


	public void setPermHash(java.lang.String permHash) {
		this._permHash = permHash;
	}
	
    /**
     * Returns the value of field 'action'.
     * 
     * @return Action
     * @return the value of field 'action'.
     */
    public org.aplana.br4j.dynamicaccess.xmldef.types.Action getAction()
    {
        return this._action;
    } //-- org.aplana.br4j.dynamicaccess.xmldef.types.Action getAction() 
    
    /**
     * Sets the value of field 'action'.
     * 
     * @param Action the value of field 'action'.
     */
    public void setAction(org.aplana.br4j.dynamicaccess.xmldef.types.Action action)
    {
        this._action = action;
    } //-- void setAction(org.aplana.br4j.dynamicaccess.xmldef.types.Action) 
	
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
        return (org.aplana.br4j.dynamicaccess.xmldef.WfMove) Unmarshaller.unmarshal(org.aplana.br4j.dynamicaccess.xmldef.WfMove.class, reader);
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

	public String generateRuleHash(String ruleString, String template, String status){
		StringBuilder result = new StringBuilder(ruleString).append(AccessRule.BORDER).
		    	append(template != null && !template.isEmpty() ? template : AccessRule.EMPTY_STRING).append(AccessRule.BORDER).
		    	append(status != null && !status.isEmpty() ? status : AccessRule.EMPTY_STRING).append(AccessRule.BORDER).
		    	append(this._wfm_id);
		this._permHash =  DigestUtils.md5Hex(result.toString());
		return this._permHash; 
	}


	public boolean isAuto() {
		return auto;
	}

}
