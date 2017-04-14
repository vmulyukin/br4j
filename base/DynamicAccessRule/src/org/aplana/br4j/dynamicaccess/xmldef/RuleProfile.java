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

import java.security.MessageDigest;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.aplana.br4j.dynamicaccess.db_export.AccessRule;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;

/**
 * Class RuleProfile.
 * 
 * @version $Revision$ $Date$
 */
public class RuleProfile implements java.io.Serializable {


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
     * Field _profileAttributeCode
     */
    private java.lang.String _profileAttributeCode;

    /**
     * Field _targetAttributeCode
     */
    private java.lang.String _targetAttributeCode;

    /**
     * Field _linkAttributeCode
     */
    private java.lang.String _linkAttributeCode;

    /**
     * Field _intermedAttributeCode
     */
    private java.lang.String _intermedAttributeCode;

    /**
     * Field _linkedStatusId
     */
    private java.lang.String _linkedStatusId;

    /**
     * Field _roleCode
     */
    private java.lang.String _roleCode;


      //----------------/
     //- Constructors -/
    //----------------/

    public RuleProfile() {
        super();
    } //-- org.aplana.br4j.dynamicaccess.xmldef.RuleProfile()


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Returns the value of field 'intermedAttributeCode'.
     * 
     * @return String
     * @return the value of field 'intermedAttributeCode'.
     */
    public java.lang.String getIntermedAttributeCode()
    {
        return this._intermedAttributeCode;
    } //-- java.lang.String getIntermedAttributeCode() 

    /**
     * Returns the value of field 'linkAttributeCode'.
     * 
     * @return String
     * @return the value of field 'linkAttributeCode'.
     */
    public java.lang.String getLinkAttributeCode()
    {
        return this._linkAttributeCode;
    } //-- java.lang.String getLinkAttributeCode() 

    /**
     * Returns the value of field 'linkedStatusId'.
     * 
     * @return String
     * @return the value of field 'linkedStatusId'.
     */
    public java.lang.String getLinkedStatusId()
    {
        return this._linkedStatusId;
    } //-- java.lang.String getLinkedStatusId() 

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
     * Returns the value of field 'profileAttributeCode'.
     * 
     * @return String
     * @return the value of field 'profileAttributeCode'.
     */
    public java.lang.String getProfileAttributeCode()
    {
        return this._profileAttributeCode;
    } //-- java.lang.String getProfileAttributeCode() 

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
     * Returns the value of field 'targetAttributeCode'.
     * 
     * @return String
     * @return the value of field 'targetAttributeCode'.
     */
    public java.lang.String getTargetAttributeCode()
    {
        return this._targetAttributeCode;
    } //-- java.lang.String getTargetAttributeCode() 

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
     * Sets the value of field 'intermedAttributeCode'.
     * 
     * @param intermedAttributeCode the value of field
     * 'intermedAttributeCode'.
     */
    public void setIntermedAttributeCode(java.lang.String intermedAttributeCode)
    {
        this._intermedAttributeCode = intermedAttributeCode;
    } //-- void setIntermedAttributeCode(java.lang.String) 

    /**
     * Sets the value of field 'linkAttributeCode'.
     * 
     * @param linkAttributeCode the value of field
     * 'linkAttributeCode'.
     */
    public void setLinkAttributeCode(java.lang.String linkAttributeCode)
    {
        this._linkAttributeCode = linkAttributeCode;
    } //-- void setLinkAttributeCode(java.lang.String) 

    /**
     * Sets the value of field 'linkedStatusId'.
     * 
     * @param linkedStatusId the value of field 'linkedStatusId'.
     */
    public void setLinkedStatusId(java.lang.String linkedStatusId)
    {
        this._linkedStatusId = linkedStatusId;
    } //-- void setLinkedStatusId(java.lang.String) 

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
     * Sets the value of field 'profileAttributeCode'.
     * 
     * @param profileAttributeCode the value of field
     * 'profileAttributeCode'.
     */
    public void setProfileAttributeCode(java.lang.String profileAttributeCode)
    {
        this._profileAttributeCode = profileAttributeCode;
    } //-- void setProfileAttributeCode(java.lang.String) 

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
     * Sets the value of field 'targetAttributeCode'.
     * 
     * @param targetAttributeCode the value of field
     * 'targetAttributeCode'.
     */
    public void setTargetAttributeCode(java.lang.String targetAttributeCode)
    {
        this._targetAttributeCode = targetAttributeCode;
    } //-- void setTargetAttributeCode(java.lang.String) 

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
        return (org.aplana.br4j.dynamicaccess.xmldef.RuleProfile) Unmarshaller.unmarshal(org.aplana.br4j.dynamicaccess.xmldef.RuleProfile.class, reader);
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

    /**
     * ��� ��������� ������ �������� �� ���������, �� ��� ��������� �����������
     */
    @Override
	public boolean equals(Object object) {
		if (!(object instanceof RuleProfile)) {
			return false;
		}
		RuleProfile anotherRuleProfile = (RuleProfile) object;
		return new EqualsBuilder()
			.append(getProfileAttributeCode(), anotherRuleProfile.getProfileAttributeCode())
			.append(getTargetAttributeCode(), anotherRuleProfile.getTargetAttributeCode())
			.append(getLinkAttributeCode(), anotherRuleProfile.getLinkAttributeCode())		
			.append(getIntermedAttributeCode(), anotherRuleProfile.getIntermedAttributeCode())
			.append(getLinkedStatusId(), anotherRuleProfile.getLinkedStatusId())
			.append(getRoleCode(), anotherRuleProfile.getRoleCode())
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
    	append((getProfileAttributeCode()!=null&&!getProfileAttributeCode().isEmpty())?getProfileAttributeCode():AccessRule.EMPTY_STRING).append(AccessRule.BORDER).
    	append((getTargetAttributeCode()!=null&&!getTargetAttributeCode().isEmpty())?getTargetAttributeCode():AccessRule.EMPTY_STRING).append(AccessRule.BORDER).
        append((getLinkAttributeCode()!=null&&!getLinkAttributeCode().isEmpty())?getLinkAttributeCode():AccessRule.EMPTY_STRING).append(AccessRule.BORDER).
        append((getIntermedAttributeCode()!=null&&!getIntermedAttributeCode().isEmpty())?getIntermedAttributeCode():AccessRule.EMPTY_STRING).append(AccessRule.BORDER).
        append((getLinkedStatusId()!=null&&!getLinkedStatusId().isEmpty())?getLinkedStatusId():AccessRule.EMPTY_STRING).append(AccessRule.BORDER).
        append((getRoleCode()!=null&&!getRoleCode().isEmpty())?getRoleCode():AccessRule.EMPTY_STRING);
    	return result.toString();
    }
    
    public String generateRuleHash() {
    	this._ruleHash = DigestUtils.md5Hex(this.toString());
    	return this._ruleHash;
    }

}