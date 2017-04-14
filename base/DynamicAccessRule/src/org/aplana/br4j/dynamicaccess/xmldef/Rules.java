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
 * �������� ������ ���� �������
 * 
 * @version $Revision$ $Date$
 */
public class Rules implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * ���� ���� ����� - person, role, profile
     */
    private java.util.Vector _ruleList;


      //----------------/
     //- Constructors -/
    //----------------/

    public Rules() {
        super();
        _ruleList = new Vector();
    } //-- org.aplana.br4j.dynamicaccess.xmldef.Rules()


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Method addRule
     * 
     * 
     * 
     * @param vRule
     */
    public void addRule(org.aplana.br4j.dynamicaccess.xmldef.Rule vRule)
        throws java.lang.IndexOutOfBoundsException
    {
        _ruleList.addElement(vRule);
    } //-- void addRule(org.aplana.br4j.dynamicaccess.xmldef.Rule) 

    /**
     * Method addRule
     * 
     * 
     * 
     * @param index
     * @param vRule
     */
    public void addRule(int index, org.aplana.br4j.dynamicaccess.xmldef.Rule vRule)
        throws java.lang.IndexOutOfBoundsException
    {
        _ruleList.insertElementAt(vRule, index);
    } //-- void addRule(int, org.aplana.br4j.dynamicaccess.xmldef.Rule) 

    /**
     * Method enumerateRule
     * 
     * 
     * 
     * @return Enumeration
     */
    public java.util.Enumeration enumerateRule()
    {
        return _ruleList.elements();
    } //-- java.util.Enumeration enumerateRule() 

    /**
     * Method getRule
     * 
     * 
     * 
     * @param index
     * @return Rule
     */
    public org.aplana.br4j.dynamicaccess.xmldef.Rule getRule(int index)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _ruleList.size())) {
            throw new IndexOutOfBoundsException();
        }
        
        return (org.aplana.br4j.dynamicaccess.xmldef.Rule) _ruleList.elementAt(index);
    } //-- org.aplana.br4j.dynamicaccess.xmldef.Rule getRule(int) 

    /**
     * Method getRule
     * 
     * 
     * 
     * @return Rule
     */
    public org.aplana.br4j.dynamicaccess.xmldef.Rule[] getRule()
    {
        int size = _ruleList.size();
        org.aplana.br4j.dynamicaccess.xmldef.Rule[] mArray = new org.aplana.br4j.dynamicaccess.xmldef.Rule[size];
        for (int index = 0; index < size; index++) {
            mArray[index] = (org.aplana.br4j.dynamicaccess.xmldef.Rule) _ruleList.elementAt(index);
        }
        return mArray;
    } //-- org.aplana.br4j.dynamicaccess.xmldef.Rule[] getRule() 

    public java.util.Vector getRuleList() {
		return _ruleList;
	}


	/**
     * Method getRuleCount
     * 
     * 
     * 
     * @return int
     */
    public int getRuleCount()
    {
        return _ruleList.size();
    } //-- int getRuleCount() 

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
     * Method removeAllRule
     * 
     */
    public void removeAllRule()
    {
        _ruleList.removeAllElements();
    } //-- void removeAllRule() 

    /**
     * Method removeRule
     * 
     * 
     * 
     * @param index
     * @return Rule
     */
    public org.aplana.br4j.dynamicaccess.xmldef.Rule removeRule(int index)
    {
        java.lang.Object obj = _ruleList.elementAt(index);
        _ruleList.removeElementAt(index);
        return (org.aplana.br4j.dynamicaccess.xmldef.Rule) obj;
    } //-- org.aplana.br4j.dynamicaccess.xmldef.Rule removeRule(int) 

    /**
     * Method setRule
     * 
     * 
     * 
     * @param index
     * @param vRule
     */
    public void setRule(int index, org.aplana.br4j.dynamicaccess.xmldef.Rule vRule)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _ruleList.size())) {
            throw new IndexOutOfBoundsException();
        }
        _ruleList.setElementAt(vRule, index);
    } //-- void setRule(int, org.aplana.br4j.dynamicaccess.xmldef.Rule) 

    /**
     * Method setRule
     * 
     * 
     * 
     * @param ruleArray
     */
    public void setRule(org.aplana.br4j.dynamicaccess.xmldef.Rule[] ruleArray)
    {
        //-- copy array
        _ruleList.removeAllElements();
        for (int i = 0; i < ruleArray.length; i++) {
            _ruleList.addElement(ruleArray[i]);
        }
    } //-- void setRule(org.aplana.br4j.dynamicaccess.xmldef.Rule) 

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
        return (org.aplana.br4j.dynamicaccess.xmldef.Rules) Unmarshaller.unmarshal(org.aplana.br4j.dynamicaccess.xmldef.Rules.class, reader);
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
