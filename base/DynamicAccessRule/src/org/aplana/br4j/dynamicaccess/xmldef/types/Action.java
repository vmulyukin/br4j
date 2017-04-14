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
public class Action implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * The add action
     */
    public static final int ADD_ACTION = 0;

    /**
     * The instance of the add action
     */
    public static final Action ADD = new Action(ADD_ACTION, "add");

    /**
     * The remove action
     */
    public static final int REMOVE_ACTION = 1;

    /**
     * The instance of the remove action
     */
    public static final Action REMOVE = new Action(REMOVE_ACTION, "remove");

    /**
     * The rename action
     */
    public static final int RENAME_ACTION = 2;

    /**
     * The instance of the rename action
     */
    public static final Action RENAME = new Action(RENAME_ACTION, "rename");

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

    private Action(int type, java.lang.String value) {
        super();
        this.type = type;
        this.stringValue = value;
    } //-- org.aplana.br4j.dynamicaccess.xmldef.types.Action(int, java.lang.String)


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Method enumerate
     * 
     * Returns an enumeration of all possible instances of
     * Action
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
     * Returns the type of this Action
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
        members.put("add", ADD);
        members.put("remove", REMOVE);
        members.put("rename", RENAME);
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
     * Returns the String representation of this Action
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
     * Returns a new Action based on the given String
     * value.
     * 
     * @param string
     * @return Action
     */
    public static org.aplana.br4j.dynamicaccess.xmldef.types.Action valueOf(java.lang.String string)
    {
        java.lang.Object obj = null;
        if (string != null) obj = _memberTable.get(string);
        if (obj == null) {
            String err = "'" + string + "' is not a valid Action";
            throw new IllegalArgumentException(err);
        }
        return (Action) obj;
    } //-- org.aplana.br4j.dynamicaccess.xmldef.types.Action valueOf(java.lang.String) 
    
	@Override
	public boolean equals(Object obj) {
		if(obj == null || !(obj instanceof Action))
			return false;
		return this.type == ((Action) obj).type;
	}    

}
