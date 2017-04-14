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
 * ������ rule_id ������������ �������
 * 
 * @version $Revision$ $Date$
 */
public class Operations implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * ���� �� rule_id ����������� ������� � ��
     */
    private java.util.Vector OperationList;


      //----------------/
     //- Constructors -/
    //----------------/

    public Operations() {
        super();
        OperationList = new Vector();
    } //-- org.aplana.br4j.dynamicaccess.xmldef.Operations()


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Method addOperation
     * 
     * 
     * 
     * @param vOperation
     */
    public void addOperation(org.aplana.br4j.dynamicaccess.xmldef.Operation vOperation)
        throws java.lang.IndexOutOfBoundsException
    {
        OperationList.addElement(vOperation);
    } //-- void addOperation(org.aplana.br4j.dynamicaccess.xmldef.Operation) 

    /**
     * Method addOperation
     * 
     * 
     * 
     * @param index
     * @param vOperation
     */
    public void addOperation(int index, org.aplana.br4j.dynamicaccess.xmldef.Operation vOperation)
        throws java.lang.IndexOutOfBoundsException
    {
        OperationList.insertElementAt(vOperation, index);
    } //-- void addOperation(int, org.aplana.br4j.dynamicaccess.xmldef.Operation) 

    /**
     * Method enumerateOperation
     * 
     * 
     * 
     * @return Enumeration
     */
    public java.util.Enumeration enumerateOperation()
    {
        return OperationList.elements();
    } //-- java.util.Enumeration enumerateOperation() 

    /**
     * Method getOperation
     * 
     * 
     * 
     * @param index
     * @return Operation
     */
    public org.aplana.br4j.dynamicaccess.xmldef.Operation getOperation(int index)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > OperationList.size())) {
            throw new IndexOutOfBoundsException();
        }
        
        return (org.aplana.br4j.dynamicaccess.xmldef.Operation) OperationList.elementAt(index);
    } //-- org.aplana.br4j.dynamicaccess.xmldef.Operation getOperation(int) 

    /**
     * Method getOperation
     * 
     * 
     * 
     * @return Operation
     */
    public org.aplana.br4j.dynamicaccess.xmldef.Operation[] getOperations()
    {
        int size = OperationList.size();
        org.aplana.br4j.dynamicaccess.xmldef.Operation[] mArray = new org.aplana.br4j.dynamicaccess.xmldef.Operation[size];
        for (int index = 0; index < size; index++) {
            mArray[index] = (org.aplana.br4j.dynamicaccess.xmldef.Operation) OperationList.elementAt(index);
        }
        return mArray;
    } //-- org.aplana.br4j.dynamicaccess.xmldef.Operation[] getOperation() 
    
    public Vector<Operation> getOperationList(){
    	return this.OperationList;
    }
    
    /**
     * Method getOperationCount
     * 
     * 
     * 
     * @return int
     */
    public int getOperationCount()
    {
        return OperationList.size();
    } //-- int getOperationCount() 

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
     * Method removeAllOperation
     * 
     */
    public void removeAllOperation()
    {
        OperationList.removeAllElements();
    } //-- void removeAllOperation() 

    /**
     * Method removeOperation
     * 
     * 
     * 
     * @param index
     * @return Operation
     */
    public org.aplana.br4j.dynamicaccess.xmldef.Operation removeOperation(int index)
    {
        java.lang.Object obj = OperationList.elementAt(index);
        OperationList.removeElementAt(index);
        return (org.aplana.br4j.dynamicaccess.xmldef.Operation) obj;
    } //-- org.aplana.br4j.dynamicaccess.xmldef.Operation removeOperation(int) 

    /**
     * Method setOperation
     * 
     * 
     * 
     * @param index
     * @param vOperation
     */
    public void setOperation(int index, org.aplana.br4j.dynamicaccess.xmldef.Operation vOperation)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > OperationList.size())) {
            throw new IndexOutOfBoundsException();
        }
        OperationList.setElementAt(vOperation, index);
    } //-- void setOperation(int, org.aplana.br4j.dynamicaccess.xmldef.Operation) 

    /**
     * Method setOperation
     * 
     * 
     * 
     * @param OperationArray
     */
    public void setOperations(org.aplana.br4j.dynamicaccess.xmldef.Operation[] OperationArray)
    {
        //-- copy array
        OperationList.removeAllElements();
        for (int i = 0; i < OperationArray.length; i++) {
            OperationList.addElement(OperationArray[i]);
        }
    } //-- void setOperation(org.aplana.br4j.dynamicaccess.xmldef.Operation) 
    
    public void setOperations(java.util.Collection operationVector)
    {
        OperationList.removeAllElements();
        OperationList.addAll(operationVector);

    } //-- void setOperation(org.aplana.br4j.dynamicaccess.xmldef.Operation) 

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
        return (org.aplana.br4j.dynamicaccess.xmldef.Operations) Unmarshaller.unmarshal(org.aplana.br4j.dynamicaccess.xmldef.Operations.class, reader);
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
