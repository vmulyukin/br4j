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
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;

/**
 * ���� ���� ����� - person, role, profile����� ������������ �����
 * ������ �� ���� ����
 * 
 * @version $Revision$ $Date$
 */
public class Rule implements java.io.Serializable {


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
	
	private java.lang.String ruleString;
    private org.aplana.br4j.dynamicaccess.xmldef.types.Action _action;
	

	/**
     * Field _rulePerson
     */
    private org.aplana.br4j.dynamicaccess.xmldef.RulePerson _rulePerson;

    /**
     * Field _ruleRole
     */
    private org.aplana.br4j.dynamicaccess.xmldef.RuleRole _ruleRole;

    /**
     * Field _ruleProfile
     */
    private org.aplana.br4j.dynamicaccess.xmldef.RuleProfile _ruleProfile;

    /**
     * Field _ruleDelegation
     */
    private org.aplana.br4j.dynamicaccess.xmldef.RuleDelegation _ruleDelegation;


      //----------------/
     //- Constructors -/
    //----------------/

    public Rule() {
        super();
    } //-- org.aplana.br4j.dynamicaccess.xmldef.Rule()


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
     * Returns the value of field 'ruleDelegation'.
     * 
     * @return RuleDelegation
     * @return the value of field 'ruleDelegation'.
     */
    public org.aplana.br4j.dynamicaccess.xmldef.RuleDelegation getRuleDelegation()
    {
        return this._ruleDelegation;
    } //-- org.aplana.br4j.dynamicaccess.xmldef.RuleDelegation getRuleDelegation() 

    /**
     * Returns the value of field 'rulePerson'.
     * 
     * @return RulePerson
     * @return the value of field 'rulePerson'.
     */
    public org.aplana.br4j.dynamicaccess.xmldef.RulePerson getRulePerson()
    {
        return this._rulePerson;
    } //-- org.aplana.br4j.dynamicaccess.xmldef.RulePerson getRulePerson() 

    /**
     * Returns the value of field 'ruleProfile'.
     * 
     * @return RuleProfile
     * @return the value of field 'ruleProfile'.
     */
    public org.aplana.br4j.dynamicaccess.xmldef.RuleProfile getRuleProfile()
    {
        return this._ruleProfile;
    } //-- org.aplana.br4j.dynamicaccess.xmldef.RuleProfile getRuleProfile() 

    /**
     * Returns the value of field 'ruleRole'.
     * 
     * @return RuleRole
     * @return the value of field 'ruleRole'.
     */
    public org.aplana.br4j.dynamicaccess.xmldef.RuleRole getRuleRole()
    {
        return this._ruleRole;
    } //-- org.aplana.br4j.dynamicaccess.xmldef.RuleRole getRuleRole() 

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
    
    public void deepUpdateName(java.lang.String name)
    {
        this._name = name;
        if(_ruleDelegation != null){
        	_ruleDelegation.setName(name);
        }
        if(_rulePerson != null){
        	_rulePerson.setName(name);
        }
        if(_ruleProfile != null){
        	_ruleProfile.setName(name);
        }
        if(_ruleRole!= null){
        	_ruleRole.setName(name);
        }
    } //-- void setName(java.lang.String)

    /**
     * Sets the value of field 'ruleDelegation'.
     * 
     * @param ruleDelegation the value of field 'ruleDelegation'.
     */
    public void setRuleDelegation(org.aplana.br4j.dynamicaccess.xmldef.RuleDelegation ruleDelegation)
    {
        this._ruleDelegation = ruleDelegation;
        if(this._ruleHash == null){
        	this._ruleHash = ruleDelegation.getRuleHash();
        	this.ruleString = ruleDelegation.toString();
        }
    } //-- void setRuleDelegation(org.aplana.br4j.dynamicaccess.xmldef.RuleDelegation) 

    /**
     * Sets the value of field 'rulePerson'.
     * 
     * @param rulePerson the value of field 'rulePerson'.
     */
    public void setRulePerson(org.aplana.br4j.dynamicaccess.xmldef.RulePerson rulePerson)
    {
        this._rulePerson = rulePerson;
        if(this._ruleHash == null){
        	this._ruleHash = rulePerson.getRuleHash();
        	this.ruleString = rulePerson.toString();
        }
    } //-- void setRulePerson(org.aplana.br4j.dynamicaccess.xmldef.RulePerson) 

    /**
     * Sets the value of field 'ruleProfile'.
     * 
     * @param ruleProfile the value of field 'ruleProfile'.
     */
    public void setRuleProfile(org.aplana.br4j.dynamicaccess.xmldef.RuleProfile ruleProfile)
    {
        this._ruleProfile = ruleProfile;
        if(this._ruleHash == null){
        	this._ruleHash = ruleProfile.getRuleHash();
        	this.ruleString = ruleProfile.toString();
        }
    } //-- void setRuleProfile(org.aplana.br4j.dynamicaccess.xmldef.RuleProfile) 

    /**
     * Sets the value of field 'ruleRole'.
     * 
     * @param ruleRole the value of field 'ruleRole'.
     */
    public void setRuleRole(org.aplana.br4j.dynamicaccess.xmldef.RuleRole ruleRole)
    {
        this._ruleRole = ruleRole;
        if(this._ruleHash == null){
        	this._ruleHash = ruleRole.getRuleHash();
        	this.ruleString = ruleRole.toString();
        }
    } //-- void setRuleRole(org.aplana.br4j.dynamicaccess.xmldef.RuleRole) 

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
        return (org.aplana.br4j.dynamicaccess.xmldef.Rule) Unmarshaller.unmarshal(org.aplana.br4j.dynamicaccess.xmldef.Rule.class, reader);
    } //-- java.lang.Object unmarshal(java.io.Reader) 
    
	/**
     * Returns the value of field 'ruleHash'.
     * 
     * @return String
     * @return the value of field 'ruleHash'.
     */
    public java.lang.String getRuleHash()
    {
    	return this._ruleHash != null ? this._ruleHash : "64387921751d6866c97fb1f7b32ae58d";
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
    
    public java.lang.String getRuleString() {
	    if(ruleString == null){
		    if(_ruleRole != null){
			    ruleString =  _ruleRole.toString();
		    } else if (_rulePerson != null){
			    ruleString = _rulePerson.toString();
		    } else if (_ruleProfile != null){
			    ruleString = _ruleProfile.toString();
		    } else if (_ruleDelegation != null){
			    ruleString = _ruleDelegation.toString();
		    }
	    }
    	return this.ruleString != null ? this.ruleString : "RuleRole|$##$";
    }

    @Override
	public boolean equals(Object object) {
		if (!(object instanceof Rule)) {
			return false;
		}
		Rule anotherRuleDelegation = (Rule) object;
		return new EqualsBuilder().append(getRulePerson(), anotherRuleDelegation.getRulePerson())
			.append(getRuleRole(), anotherRuleDelegation.getRuleRole())
			.append(getRuleProfile(), anotherRuleDelegation.getRuleProfile())
			.append(getRuleDelegation(), anotherRuleDelegation.getRuleDelegation())
			.isEquals();
	}
    
    @Override
    public int hashCode() {
    	return new HashCodeBuilder(17, 37).
        append(getRulePerson()).
        append(getRuleRole()).
        append(getRuleProfile()).
        append(getRuleDelegation()).        
        toHashCode();
    }


	public org.aplana.br4j.dynamicaccess.xmldef.types.Action getAction() {
		return _action;
	}


	public void setAction(org.aplana.br4j.dynamicaccess.xmldef.types.Action _action) {
		this._action = _action;
	}
    
}
