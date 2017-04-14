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
package com.aplana.dbmi.service.impl.workstation;

import com.aplana.dbmi.service.impl.query.AttributeOptions;
import com.aplana.dbmi.service.impl.query.AttributeTypes;

import java.util.HashMap;

/**
 * @author Denis Mitavskiy
 *         Date: 18.04.11
 *         Time: 14:22
 */
public class AttributeDef {
    private static final String[] INT_TO_STRING_MAP = new String[] {
        AttributeTypes.DATE,
        AttributeTypes.INTEGER,
        AttributeTypes.LIST,
        AttributeTypes.PERSON,
        AttributeTypes.STRING,
        AttributeTypes.TEXT,
        AttributeTypes.TREE,
        AttributeTypes.SECURITY,
        AttributeTypes.CARD_LINK,
        AttributeTypes.HTML,
        AttributeTypes.MATERIAL,
        AttributeTypes.BACK_LINK,
        AttributeTypes.TYPED_CLINK,
        AttributeTypes.CARD_HISTORY,
        AttributeTypes.TYPE_PORTAL_USER_LOGIN,
        AttributeTypes.TYPE_PORTAL_USER_ROLES
    };
    private static final HashMap<String, Integer> STRING_TO_INTEGER_MAP = new HashMap<String, Integer>( 16 );

    static {
        for ( int i = 0; i < INT_TO_STRING_MAP.length; ++i ) {
            STRING_TO_INTEGER_MAP.put( INT_TO_STRING_MAP[ i ], i );
        }
    }

    public static final int DATE = convertToInteger( AttributeTypes.DATE );
    public static final int INTEGER = convertToInteger( AttributeTypes.INTEGER );
    public static final int LIST = convertToInteger( AttributeTypes.LIST );
    public static final int PERSON = convertToInteger( AttributeTypes.PERSON );
    public static final int STRING = convertToInteger( AttributeTypes.STRING );
    public static final int TEXT = convertToInteger( AttributeTypes.TEXT );
    public static final int TREE = convertToInteger( AttributeTypes.TREE );
    public static final int SECURITY = convertToInteger( AttributeTypes.SECURITY );
    public static final int CARD_LINK = convertToInteger( AttributeTypes.CARD_LINK );
    public static final int HTML = convertToInteger( AttributeTypes.HTML );
    public static final int MATERIAL = convertToInteger( AttributeTypes.MATERIAL );
    public static final int BACK_LINK = convertToInteger( AttributeTypes.BACK_LINK );
    public static final int TYPED_CLINK = convertToInteger( AttributeTypes.TYPED_CLINK );
    public static final int CARD_HISTORY = convertToInteger( AttributeTypes.CARD_HISTORY );
    public static final int PORTAL_USER_LOGIN = convertToInteger( AttributeTypes.TYPE_PORTAL_USER_LOGIN );
    public static final int PORTAL_USER_ROLES = convertToInteger( AttributeTypes.TYPE_PORTAL_USER_ROLES );

    private String _code;
    private String _refCode;
    private int _type;
    private String _nameRu;
    private String _nameEn;
    private String _blockCode;
    private int _indexInBlock;
    private boolean _active;
    private boolean _system;
    private int _columnWidth;
    private boolean _mandatory;
    private boolean _hidden;
    private boolean _readOnly;
    private HashMap<String, String> _options = new HashMap<String, String>();
    private String _filterXml;

    public static int convertToInteger( String typeDef ) {
        return STRING_TO_INTEGER_MAP.get( typeDef );
    }

    public static String convertToString( int typeDef ) {
        return INT_TO_STRING_MAP[ typeDef ];
    }

    public String getCode() {
        return _code;
    }

    public void setCode( String code ) {
        _code = code;
    }

    public String getRefCode() {
        return _refCode;
    }

    public void setRefCode( String refCode ) {
        _refCode = refCode;
    }

    public int getType() {
        return _type;
    }

    public void setType( int type ) {
        _type = type;
    }

    public void setType( String type ) {
        _type = convertToInteger( type );
    }

    public String getNameRu() {
        return _nameRu;
    }

    public void setNameRu( String nameRu ) {
        _nameRu = nameRu;
    }

    public String getNameEn() {
        return _nameEn;
    }

    public void setNameEn( String nameEn ) {
        _nameEn = nameEn;
    }

    public String getBlockCode() {
        return _blockCode;
    }

    public void setBlockCode( String blockCode ) {
        _blockCode = blockCode;
    }

    public int getIndexInBlock() {
        return _indexInBlock;
    }

    public void setIndexInBlock( int indexInBlock ) {
        _indexInBlock = indexInBlock;
    }

    public boolean isActive() {
        return _active;
    }

    public void setActive( boolean active ) {
        _active = active;
    }

    public boolean isSystem() {
        return _system;
    }

    public void setSystem( boolean system ) {
        _system = system;
    }

    public int getColumnWidth() {
        return _columnWidth;
    }

    public void setColumnWidth( int columnWidth ) {
        _columnWidth = columnWidth;
    }

    public boolean isMandatory() {
        return _mandatory;
    }

    public void setMandatory( boolean mandatory ) {
        _mandatory = mandatory;
    }

    public boolean isHidden() {
        return _hidden;
    }

    public void setHidden( boolean hidden ) {
        _hidden = hidden;
    }

    public boolean isReadOnly() {
        return _readOnly;
    }

    public void setReadOnly( boolean readOnly ) {
        _readOnly = readOnly;
    }

    public String getOption( String code ) {
        return _options.get( code );
    }

    public void setOption( String code, String value ) {
        _options.put( code, value );
    }

    public Integer getLength() {
        return getIntegerOptionValue( AttributeOptions.LENGTH );
    }

    public Integer getRows() {
        return getIntegerOptionValue( AttributeOptions.ROWS );
    }

    public String getReference() {
        return _options.get( AttributeOptions.REFERENCE );
    }

    public Boolean isRestricted() {
        return getBooleanOptionValue( AttributeOptions.RESTRICTED );
    }

    public Long getFilter() {
        return getLongOptionValue( AttributeOptions.FILTER );
    }

    public String getFilterXml(){
        return _filterXml;
    }

    public void setFilterXml(String filterXml) {
        this._filterXml = filterXml;
    }

    public String getLink() {
        return _options.get( AttributeOptions.LINK );
    }

    public String getUplink() {
        return _options.get( AttributeOptions.UPLINK );
    }

    public Boolean isSingleValued() {
        return getBooleanOptionValue( AttributeOptions.SINGLEVALUED );
    }

/*    public Boolean getShowTime() {
        return getBooleanOptionValue( AttributeOptions.SHOWTIME );
    }
*/
    public String getTimePattern() {
        return _options.get( AttributeOptions.TIMEPATTERN );
    }

    private Integer getIntegerOptionValue( String code ) {
        String value = _options.get( code );
        return value == null ? null : Integer.valueOf( value );
    }

    private Long getLongOptionValue( String code ) {
        String value = _options.get( code );
        return value == null ? null : Long.valueOf( value );
    }

    private Boolean getBooleanOptionValue( String code ) {
        return getIntegerOptionValue( code ) == 1;
    }
}
