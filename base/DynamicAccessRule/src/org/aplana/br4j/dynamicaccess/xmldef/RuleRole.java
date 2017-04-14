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
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.aplana.br4j.dynamicaccess.db_export.AccessRule;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;

/**
 * Class RuleRole.
 * 
 * @version $Revision$ $Date$
 */
public class RuleRole implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field _name
     */
    private java.lang.String _name;

    /**
	 * Field _ruleHash
	 */
	private java.lang.String _ruleHash;
	
	
    /**
     * Field _roleCode
     */
    private java.lang.String _roleCode;


      //----------------/
     //- Constructors -/
    //----------------/

    public RuleRole() {
        super();
    } //-- org.aplana.br4j.dynamicaccess.xmldef.RuleRole()


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
     * Returns the value of field 'roleCode'.
     * 
     * @return String
     * @return the value of field 'roleCode'.
     */
    public java.lang.String getRoleCode()
    {
        return this._roleCode;
    } //-- java.lang.String getRoleCode() 

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
     * Sets the value of field 'roleCode'.
     * 
     * @param roleCode the value of field 'roleCode'.
     */
    public void setRoleCode(java.lang.String roleCode)
    {
        this._roleCode = roleCode;
    } //-- void setRoleCode(java.lang.String) 

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
        return (org.aplana.br4j.dynamicaccess.xmldef.RuleRole) Unmarshaller.unmarshal(org.aplana.br4j.dynamicaccess.xmldef.RuleRole.class, reader);
    } //-- java.lang.Object unmarshal(java.io.Reader) 

	/**
     * Returns the value of field 'ruleHash'.
     * 
     * @return String
     * @return the value of field 'ruleHash'.
     */
    public java.lang.String getRuleHash()
    {
    	return this._ruleHash != null ? this._ruleHash : generateRuleHash();
    } //-- java.lang.String getRuleHash() 
	
	
    /**
     * Sets the value of field 'ruleHash'.
     * 
     * @param name the value of field 'ruleHash'.
     */
    public void setRuleHash(java.lang.String ruleHash)
    {
    	this._ruleHash = ruleHash;
    } //-- void setRuleHash(java.lang.String) 
    
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
	public boolean equals(Object object) {
		if (!(object instanceof RuleRole)) {
			return false;
		}
		RuleRole anotherRuleRole = (RuleRole) object;
		return new EqualsBuilder()
			.append(getRoleCode(), anotherRuleRole.getRoleCode())
			.isEquals();
	}
    
    /**
     * ���-��� ������� � ���������� ����� �������������� 
     * ��� ��������� ����������� ����� ��� ������� ����� (�������� ������������)
     * ������ #####################(21 �������� #) ������������� ��� ������ �������� �������� ��������� ������� (��� �������������� ������������� � ����� �� ���������) 
     */
    @Override
    public int hashCode() {
    	return new HashCodeBuilder(17, 37).
    	append(this.toString()).
        toHashCode();
    }

    @Override
    public String toString() {
    	StringBuilder result = new StringBuilder(this.getClass().getSimpleName()).append(AccessRule.BORDER).
    	append((getRoleCode()!=null&&!getRoleCode().isEmpty())?getRoleCode():AccessRule.EMPTY_STRING);
    	return result.toString();
    }

    public String generateRuleHash() {
    	this._ruleHash = DigestUtils.md5Hex(this.toString());
    	return this._ruleHash;
    }
}
