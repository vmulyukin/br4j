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

package org.aplana.br4j.dynamicaccess.xmldef.types;

  //---------------------------------/
 //- Imported classes and packages -/
//---------------------------------/

import java.util.Hashtable;

/**
 * �������� ���������� ��������
 * 
 * @version $Revision$ $Date$
 */
public class OperationType implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * The empty type
     */
    public static final int EMPTY_TYPE = 0;

    /**
     * The instance of the empty type
     */
    public static final OperationType EMPTY = new OperationType(EMPTY_TYPE, "empty");

    /**
     * The read type
     */
    public static final int READ_TYPE = 1;

    /**
     * The instance of the read type
     */
    public static final OperationType READ = new OperationType(READ_TYPE, "read");

    /**
     * The read/write type
     */
    public static final int WRITE_TYPE = 2;

    /**
     * The instance of the read/write type
     */
    public static final OperationType WRITE = new OperationType(WRITE_TYPE, "write");

    /**
     * The create type
     */
    public static final int CREATE_TYPE = 3;

    /**
     * The instance of the create type
     */
    public static final OperationType CREATE = new OperationType(CREATE_TYPE, "create");

    /**
     * Field _memberTable
     */
    private static java.util.Hashtable _memberTable = init();

    /**
     * Field type
     */
    private int type = -1;

    /**
     * Field stringValue
     */
    private java.lang.String stringValue = null;


      //----------------/
     //- Constructors -/
    //----------------/

    private OperationType(int type, java.lang.String value) {
        super();
        this.type = type;
        this.stringValue = value;
    } //-- org.aplana.br4j.dynamicaccess.xmldef.types.CardPermissionType(int, java.lang.String)


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Method enumerate
     * 
     * Returns an enumeration of all possible instances of
     * CardPermissionType
     * 
     * @return Enumeration
     */
    public static java.util.Enumeration enumerate()
    {
        return _memberTable.elements();
    } //-- java.util.Enumeration enumerate() 

    /**
     * Method getType
     * 
     * Returns the type of this CardPermissionType
     * 
     * @return int
     */
    public int getType()
    {
        return this.type;
    } //-- int getType() 

    /**
     * Method init
     * 
     * 
     * 
     * @return Hashtable
     */
    private static java.util.Hashtable init()
    {
        Hashtable members = new Hashtable();
        members.put("empty", EMPTY);
        members.put("read", READ);
        members.put("write", WRITE);
        members.put("create", CREATE);
        return members;
    } //-- java.util.Hashtable init() 

    /**
     * Method readResolve
     * 
     *  will be called during deserialization to replace the
     * deserialized object with the correct constant instance.
     * <br/>
     * 
     * @return Object
     */
    private java.lang.Object readResolve()
    {
        return valueOf(this.stringValue);
    } //-- java.lang.Object readResolve() 

    /**
     * Method toString
     * 
     * Returns the String representation of this CardPermissionType
     * 
     * @return String
     */
    public java.lang.String toString()
    {
        return this.stringValue;
    } //-- java.lang.String toString() 

    /**
     * Method valueOf
     * 
     * Returns a new CardPermissionType based on the given String
     * value.
     * 
     * @param string
     * @return CardPermissionType
     */
    public static org.aplana.br4j.dynamicaccess.xmldef.types.OperationType valueOf(java.lang.String string)
    {
        java.lang.Object obj = null;
        if (string != null) obj = _memberTable.get(string);
        if (obj == null) {
            String err = "'" + string + "' is not a valid CardPermissionType";
            throw new IllegalArgumentException(err);
        }
        return (OperationType) obj;
    } //-- org.aplana.br4j.dynamicaccess.xmldef.types.CardPermissionType valueOf(java.lang.String) 


	@Override
	public boolean equals(Object obj) {
		if(obj == null || !(obj instanceof OperationType))
			return false;
		return this.type == ((OperationType) obj).type;
	}
	
	public String getDataBaseOperationCode(){
		if (this.type == READ_TYPE){
			return "R";
		} else if (this.type == WRITE_TYPE){
			return "W";
		} else if (this.type == CREATE_TYPE){
			return "C";
		} else {
			return "$##$";
		}
	}

}
