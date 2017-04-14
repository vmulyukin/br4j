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

import org.apache.commons.codec.digest.DigestUtils;
import org.aplana.br4j.dynamicaccess.db_export.AccessRule;
import org.aplana.br4j.dynamicaccess.xmldef.types.Action;
import org.aplana.br4j.dynamicaccess.xmldef.types.OperationType;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;

/**
 * ���� �� rule_id ����������� ������� � ��
 * 
 * @version $Revision$ $Date$
 */
public class Operation implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _description
     */
    private java.lang.String _permHash;
    
    /**
     * Field _operationType
     */
    private org.aplana.br4j.dynamicaccess.xmldef.types.OperationType _operationType;
    
    /**
     * Field _action
     */
    private org.aplana.br4j.dynamicaccess.xmldef.types.Action _action;

	private boolean auto;

      //----------------/
     //- Constructors -/
    //----------------/

    public Operation() {
        super();
    } //-- org.aplana.br4j.dynamicaccess.xmldef.ID()
    
    public Operation(OperationType type) {
        super();
        this._operationType = type;
    } //-- org.aplana.br4j.dynamicaccess.xmldef.ID()
    
    public Operation(OperationType type, Action action) {
        super();
        this._operationType = type;
        this._action = action;
        this.auto = true;
    } //-- org.aplana.br4j.dynamicaccess.xmldef.ID()


      //-----------/
     //- Methods -/
    //-----------/

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
     * Returns the value of field 'operationType'.
     * 
     * @return OperationType
     * @return the value of field 'operationType'.
     */
    public org.aplana.br4j.dynamicaccess.xmldef.types.OperationType getOperationType()
    {
        return this._operationType;
    } //-- org.aplana.br4j.dynamicaccess.xmldef.types.OperationType getOperationType() 
    
    /**
     * Sets the value of field 'operationType'.
     * 
     * @param OperationType the value of field 'operationType'.
     */
    public void setOperationType(org.aplana.br4j.dynamicaccess.xmldef.types.OperationType operationType)
    {
        this._operationType = operationType;
    } //-- void setOperationType(org.aplana.br4j.dynamicaccess.xmldef.types.OperationType) 

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
        return (org.aplana.br4j.dynamicaccess.xmldef.Operation) Unmarshaller.unmarshal(org.aplana.br4j.dynamicaccess.xmldef.Operation.class, reader);
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


	public java.lang.String getPermHash() {
		return _permHash;
	}


	public void setPermHash(java.lang.String permHash) {
		this._permHash = permHash;
	}
	
	public String generateRuleHash(String ruleString, String template, String status){
		StringBuilder result = new StringBuilder(ruleString).append(AccessRule.BORDER).
		    	append(template != null && !template.isEmpty() ? template : AccessRule.EMPTY_STRING).append(AccessRule.BORDER).
		    	append(status != null && !status.isEmpty() && !status.equals("NO_STATUS") ? status : AccessRule.EMPTY_STRING).append(AccessRule.BORDER).
		    	append(this._operationType.getDataBaseOperationCode());
		this._permHash =  DigestUtils.md5Hex(result.toString());
		return this._permHash; 
	}

	public boolean isAuto() {
		return auto;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return this._operationType.toString();
	}

	
}
