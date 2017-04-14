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
 * �������� ��������� ��� ����������� ������� � ����
 * 
 * @version $Revision$ $Date$
 */
public class WfMoves implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * ������� ���������� ���������� � ��������
     */
    private java.util.Vector _wfMoveList;


      //----------------/
     //- Constructors -/
    //----------------/

    public WfMoves() {
        super();
        _wfMoveList = new Vector();
    } //-- org.aplana.br4j.dynamicaccess.xmldef.WfMoves()


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Method addWfMove
     * 
     * 
     * 
     * @param vWfMove
     */
    public void addWfMove(org.aplana.br4j.dynamicaccess.xmldef.WfMove vWfMove)
        throws java.lang.IndexOutOfBoundsException
    {
        _wfMoveList.addElement(vWfMove);
    } //-- void addWfMove(org.aplana.br4j.dynamicaccess.xmldef.WfMove) 

    /**
     * Method addWfMove
     * 
     * 
     * 
     * @param index
     * @param vWfMove
     */
    public void addWfMove(int index, org.aplana.br4j.dynamicaccess.xmldef.WfMove vWfMove)
        throws java.lang.IndexOutOfBoundsException
    {
        _wfMoveList.insertElementAt(vWfMove, index);
    } //-- void addWfMove(int, org.aplana.br4j.dynamicaccess.xmldef.WfMove) 

    /**
     * Method enumerateWfMove
     * 
     * 
     * 
     * @return Enumeration
     */
    public java.util.Enumeration enumerateWfMove()
    {
        return _wfMoveList.elements();
    } //-- java.util.Enumeration enumerateWfMove() 

    /**
     * Method getWfMove
     * 
     * 
     * 
     * @param index
     * @return WfMove
     */
    public org.aplana.br4j.dynamicaccess.xmldef.WfMove getWfMove(int index)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _wfMoveList.size())) {
            throw new IndexOutOfBoundsException();
        }
        
        return (org.aplana.br4j.dynamicaccess.xmldef.WfMove) _wfMoveList.elementAt(index);
    } //-- org.aplana.br4j.dynamicaccess.xmldef.WfMove getWfMove(int) 

    /**
     * Method getWfMove
     * 
     * 
     * 
     * @return WfMove
     */
    public org.aplana.br4j.dynamicaccess.xmldef.WfMove[] getWfMove()
    {
        int size = _wfMoveList.size();
        org.aplana.br4j.dynamicaccess.xmldef.WfMove[] mArray = new org.aplana.br4j.dynamicaccess.xmldef.WfMove[size];
        for (int index = 0; index < size; index++) {
            mArray[index] = (org.aplana.br4j.dynamicaccess.xmldef.WfMove) _wfMoveList.elementAt(index);
        }
        return mArray;
    } //-- org.aplana.br4j.dynamicaccess.xmldef.WfMove[] getWfMove() 

    public Vector<WfMove> getWfMoveList(){
    	return this._wfMoveList;
    }
    /**
     * Method getWfMoveCount
     * 
     * 
     * 
     * @return int
     */
    public int getWfMoveCount()
    {
        return _wfMoveList.size();
    } //-- int getWfMoveCount() 

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
     * Method removeAllWfMove
     * 
     */
    public void removeAllWfMove()
    {
        _wfMoveList.removeAllElements();
    } //-- void removeAllWfMove() 

    /**
     * Method removeWfMove
     * 
     * 
     * 
     * @param index
     * @return WfMove
     */
    public org.aplana.br4j.dynamicaccess.xmldef.WfMove removeWfMove(int index)
    {
        java.lang.Object obj = _wfMoveList.elementAt(index);
        _wfMoveList.removeElementAt(index);
        return (org.aplana.br4j.dynamicaccess.xmldef.WfMove) obj;
    } //-- org.aplana.br4j.dynamicaccess.xmldef.WfMove removeWfMove(int) 

    /**
     * Method setWfMove
     * 
     * 
     * 
     * @param index
     * @param vWfMove
     */
    public void setWfMove(int index, org.aplana.br4j.dynamicaccess.xmldef.WfMove vWfMove)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _wfMoveList.size())) {
            throw new IndexOutOfBoundsException();
        }
        _wfMoveList.setElementAt(vWfMove, index);
    } //-- void setWfMove(int, org.aplana.br4j.dynamicaccess.xmldef.WfMove) 

    /**
     * Method setWfMove
     * 
     * 
     * 
     * @param wfMoveArray
     */
    public void setWfMove(java.util.Collection wfMoveVector)
    {
        //-- copy array
        _wfMoveList.removeAllElements();
        _wfMoveList.addAll(wfMoveVector);
    } //-- void setWfMove(org.aplana.br4j.dynamicaccess.xmldef.WfMove) 

    public void setWfMove(org.aplana.br4j.dynamicaccess.xmldef.WfMove[] wfMoveArray)
    {
        //-- copy array
        _wfMoveList.removeAllElements();
        for (int i = 0; i < wfMoveArray.length; i++) {
            _wfMoveList.addElement(wfMoveArray[i]);
        }
    } //-- void setWfMove(org.aplana.br4j.dynamicaccess.xmldef.WfMove) 
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
        return (org.aplana.br4j.dynamicaccess.xmldef.WfMoves) Unmarshaller.unmarshal(org.aplana.br4j.dynamicaccess.xmldef.WfMoves.class, reader);
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
