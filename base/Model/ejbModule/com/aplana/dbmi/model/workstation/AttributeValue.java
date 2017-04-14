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
package com.aplana.dbmi.model.workstation;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Lightweight DTO for Attributes store
 * At this moment the following types attributes are supported:
 * Integer, Date, String, Text, CardLink, Person
 * @author Denis Mitavskiy
 *         Date: 13.04.11
 */
public class AttributeValue {
    private int _type; // if a linked code is presented, type represents it, not a standard code
    private int _realType; // if a linked code is presented, type represents a standard code
    private String _code;
    private String _linkedCode;
    private boolean _linkedByPerson;
    private Object _value;
    private AttributeValueCorrector corrector;

	public AttributeValue( String code ) {
        this( code, null, false, null );
    }

	public AttributeValue( String code,  AttributeValueCorrector corrector) {
        this( code, null, false, null );
    	this.corrector = corrector;
    }	
	
    public AttributeValue( String code, String linkedCode ) {
        this( code, linkedCode, false, null );
    }

    public AttributeValue( String code, String linkedCode, boolean linkedByPerson ) {
        this( code, linkedCode, linkedByPerson, null );
    }
    
    public AttributeValue(String code, String linkedCode, AttributeValueCorrector corrector, boolean linkedByPerson) {
    	this( code, linkedCode, linkedByPerson, null );
    	this.corrector = corrector;
    }


    public AttributeValue( String code, Object value ) {
        this( code, null, false, value );
    }

    public AttributeValue( AttributeValue attributeValue ) {
        this( attributeValue._code, attributeValue._linkedCode, attributeValue._linkedByPerson, attributeValue._value);
        this._type = attributeValue._type;
        this._realType = attributeValue._realType;
    }
    
    public AttributeValue( String code, String linkedCode, boolean linkedByPerson, Object value) {
        _code = code;
        _linkedCode = linkedCode;
        _value = value;
        _linkedByPerson = linkedByPerson;
    }

    public int getType() {
        return _type;
    }

    public void setType( int type ) {
        _type = type;
    }
    
    public int getRealType() {
        return _realType;
    }

    public void setRealType( int realType ) {
        _realType = realType;
    }

    public String getCode() {
        return _code;
    }

    public void setCode( String code ) {
        _code = code;
    }

    public String getLinkedCode() {
        return _linkedCode;
    }

    public void setLinkedCode( String linkedCode ) {
        _linkedCode = linkedCode;
    }

	/**
     * Returns attribute value
     * Depending on type, there will be different types of Objects stored:<br/>
     * {@link AttributeDef#INTEGER} ==> {@link java.util.List}&lt;{@link java.lang.Integer}&gt;<br/>
     * {@link AttributeDef#CARD_LINK} ==> {@link java.util.List}&lt;{@link java.lang.Long}&gt;<br/>
     * {@link AttributeDef#STRING}, {@link AttributeDef#TEXT}, {@link AttributeDef#HTML} ==> {@link java.util.List}&lt;{@link java.lang.String}&gt;<br/>
     * {@link AttributeDef#LIST} ==> {@link java.util.List}&lt;{@link com.aplana.dbmi.model.ReferenceValue}&gt;<br/>
     * {@link AttributeDef#TREE} ==> {@link java.util.List}&lt;{@link com.aplana.dbmi.model.ReferenceValue}&gt;<br/>
     * {@link AttributeDef#PERSON} ==> {@link java.util.List}&lt;{@link com.aplana.dbmi.model.Person}&gt;<br/>
     * {@link AttributeDef#DATE} ==> {@link java.util.List}&lt;{@link java.sql.Timestamp}&gt;
     * {@link AttributeDef#TYPED_CLINK} ==> {@link java.util.List}&lt;{@link com.aplana.dbmi.model.workstation.TypedCardLinkValue}&gt;
     *
     * @return attribute value
     */
    public Object getValue() {
        return _value;
    }
    
    /*public Object getCorrectedValue(Object value) {
        return corrector==null?value:corrector.changeValue(value);
    }*/

    public void setValue( Object value ) {
        _value = value;
    }

    public boolean isLinkedByPerson() {
        return _linkedByPerson;
    }

    public void setLinkedByPerson(boolean linkedByPerson) {
        _linkedByPerson = linkedByPerson;
    }

    public static ArrayList<String> getCodes( Collection<AttributeValue> attributes ) {
        ArrayList<String> result = new ArrayList<String>( attributes.size() );
        for ( AttributeValue attribute : attributes ) {
            result.add( attribute.getCode() );
        }
        return result;
    }
    
    
    public boolean isAttributeFromLink() {
    	 return _linkedCode != null;
    }

    public AttributeValueCorrector getCorrector() {
		return corrector;
	}

	public void setCorrector(AttributeValueCorrector corrector) {
		this.corrector = corrector;
	}
}
