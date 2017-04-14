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
 * ��������� ���������� ���� �������
 * 
 * @version $Revision$ $Date$
 */
public class AccessConfig implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * �������� �������
     */
    private java.util.Vector _templateList;

    /**
     * Field _partial
     */
    private Boolean _partial;
      //----------------/
     //- Constructors -/
    //----------------/

    public AccessConfig() {
        super();
        _templateList = new Vector();
        _partial = Boolean.FALSE;
    } //-- org.aplana.br4j.dynamicaccess.xmldef.AccessConfig()


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Method addTemplate
     * 
     * 
     * 
     * @param vTemplate
     */
    public void addTemplate(org.aplana.br4j.dynamicaccess.xmldef.Template vTemplate)
        throws java.lang.IndexOutOfBoundsException
    {
        _templateList.addElement(vTemplate);
    } //-- void addTemplate(org.aplana.br4j.dynamicaccess.xmldef.Template) 

    /**
     * Method addTemplate
     * 
     * 
     * 
     * @param index
     * @param vTemplate
     */
    public void addTemplate(int index, org.aplana.br4j.dynamicaccess.xmldef.Template vTemplate)
        throws java.lang.IndexOutOfBoundsException
    {
        _templateList.insertElementAt(vTemplate, index);
    } //-- void addTemplate(int, org.aplana.br4j.dynamicaccess.xmldef.Template) 

    /**
     * Method enumerateTemplate
     * 
     * 
     * 
     * @return Enumeration
     */
    public java.util.Enumeration enumerateTemplate()
    {
        return _templateList.elements();
    } //-- java.util.Enumeration enumerateTemplate() 

    /**
     * Method getTemplate
     * 
     * 
     * 
     * @param index
     * @return Template
     */
    public org.aplana.br4j.dynamicaccess.xmldef.Template getTemplate(int index)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _templateList.size())) {
            throw new IndexOutOfBoundsException();
        }
        
        return (org.aplana.br4j.dynamicaccess.xmldef.Template) _templateList.elementAt(index);
    } //-- org.aplana.br4j.dynamicaccess.xmldef.Template getTemplate(int) 

    public org.aplana.br4j.dynamicaccess.xmldef.Template getTemplate(String templateId)
    {
		for(Template template: this.getTemplate()){
			if(template.getTemplate_id().equals(templateId)){
				return template;
			}
		}
		return null;
    } //-- org.aplana.br4j.dynamicaccess.xmldef.Template getTemplate(int) 

    
    /**
     * Method getTemplate
     * 
     * 
     * 
     * @return Template
     */
    public org.aplana.br4j.dynamicaccess.xmldef.Template[] getTemplate()
    {
        int size = _templateList.size();
        org.aplana.br4j.dynamicaccess.xmldef.Template[] mArray = new org.aplana.br4j.dynamicaccess.xmldef.Template[size];
        for (int index = 0; index < size; index++) {
            mArray[index] = (org.aplana.br4j.dynamicaccess.xmldef.Template) _templateList.elementAt(index);
        }
        return mArray;
    } //-- org.aplana.br4j.dynamicaccess.xmldef.Template[] getTemplate() 

    public Vector<Template> getTemplateList()
    {
    	return this._templateList;
    }
    /**
     * Method getTemplateCount
     * 
     * 
     * 
     * @return int
     */
    public int getTemplateCount()
    {
        return _templateList.size();
    } //-- int getTemplateCount() 

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
     * Method removeAllTemplate
     * 
     */
    public void removeAllTemplate()
    {
        _templateList.removeAllElements();
    } //-- void removeAllTemplate() 

    /**
     * Method removeTemplate
     * 
     * 
     * 
     * @param index
     * @return Template
     */
    public org.aplana.br4j.dynamicaccess.xmldef.Template removeTemplate(int index)
    {
        java.lang.Object obj = _templateList.elementAt(index);
        _templateList.removeElementAt(index);
        return (org.aplana.br4j.dynamicaccess.xmldef.Template) obj;
    } //-- org.aplana.br4j.dynamicaccess.xmldef.Template removeTemplate(int) 

    /**
     * Method setTemplate
     * 
     * 
     * 
     * @param index
     * @param vTemplate
     */
    public void setTemplate(int index, org.aplana.br4j.dynamicaccess.xmldef.Template vTemplate)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _templateList.size())) {
            throw new IndexOutOfBoundsException();
        }
        _templateList.setElementAt(vTemplate, index);
    } //-- void setTemplate(int, org.aplana.br4j.dynamicaccess.xmldef.Template) 

    /**
     * Method setTemplate
     * 
     * 
     * 
     * @param templateArray
     */
    public void setTemplate(org.aplana.br4j.dynamicaccess.xmldef.Template[] templateArray)
    {
        //-- copy array
        _templateList.removeAllElements();
        for (int i = 0; i < templateArray.length; i++) {
            _templateList.addElement(templateArray[i]);
        }
    } //-- void setTemplate(org.aplana.br4j.dynamicaccess.xmldef.Template) 

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
        return (org.aplana.br4j.dynamicaccess.xmldef.AccessConfig) Unmarshaller.unmarshal(org.aplana.br4j.dynamicaccess.xmldef.AccessConfig.class, reader);
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
    
    /**
     * Returns the value of field 'partial'.
     * 
     * @return String
     * @return the value of field 'partial'.
     */
    public java.lang.Boolean getPartial()
    {
        return this._partial;
    } //-- java.lang.String getPartial() 

    
    /**
     * Sets the value of field 'partial'.
     * 
     * @param modified the value of field 'partial'.
     */
    public void setPartial(java.lang.Boolean partial)
    {
        this._partial = partial;
    } //-- void Partial(java.lang.String) 
    
    public boolean containsTemplate(String templateId){
    	for(Template template: this.getTemplate()){
    		if(template.getTemplate_id().equals(templateId)){
    			return true;
    		}
    	}
    	return false;
    }
}
