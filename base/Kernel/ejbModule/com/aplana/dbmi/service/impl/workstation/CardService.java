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

import com.aplana.dbmi.model.*;
import com.aplana.dbmi.model.workstation.*;
import com.aplana.dbmi.service.impl.query.AttributeOptions;
import com.aplana.dbmi.service.impl.workstation.dao.CommonWorkstationQuery;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.*;

import static com.aplana.dbmi.service.impl.workstation.Util.ENDL;
import static com.aplana.dbmi.service.impl.workstation.Util.isEmpty;

/**
 * @author Denis Mitavskiy
 *         Date: 13.04.11
 */
public class CardService {
	private static final Object TEMPLATE_RETRIEVE_LOCK = new Object();

    private static final Map<String, AttributeDef> _attributeDefinitionCache = Collections.synchronizedMap( new HashMap<String, AttributeDef>( 100 ) );
    private static final Map<Integer, Template> _templateDefinitionCache = Collections.synchronizedMap( new HashMap<Integer, Template>( 100 ) );
    private static final Map<Integer, Status> _statusDefinitionCache = Collections.synchronizedMap( new HashMap<Integer, Status>( 100 ) );
    private static final Map<String, Status> _statusDefinitionByRussionNameCache = Collections.synchronizedMap( new HashMap<String, Status>( 100 ) );
    private static final Map<String, ArrayList<AttributeDef>> _attributesByRussianNameCache = Collections.synchronizedMap( new HashMap<String, ArrayList<AttributeDef>>( 100 ) );
    private static final Map<String, String> _attributeBackLinkCache = Collections.synchronizedMap( new HashMap<String, String>( 100 ) );
    
	private CommonWorkstationQuery commonQuery;
    
    public CardService() {
	}
    

    public void setCommonQuery(CommonWorkstationQuery commonQuery) {
		this.commonQuery = commonQuery;
	}

    public HashMap<Long, ArrayList<Long>> getBackLinkedCards( long[] cardIds, Collection<String> attributeCodes ) {
        ArrayList<String> backLinkAttributeCodes = new ArrayList<String>( attributeCodes.size() );
        for ( String code : attributeCodes ) {
            backLinkAttributeCodes.add( getBackLinkAttributeCodeByAttributeCode( code ) );
        }
        List queryResult = commonQuery.getBackLinkedCardsByAttribute( cardIds, backLinkAttributeCodes );
        HashMap<Long, ArrayList<Long>> result = new HashMap<Long, ArrayList<Long>>();
        for ( Object row : queryResult ) {
            long[] rowArray = ( long[] ) row;
            long cardId = rowArray[ 0 ];
            long backLinkCardId = rowArray[ 1 ];
            ArrayList<Long> backlinkCards = result.get( cardId );
            if ( backlinkCards == null ) {
                backlinkCards = new ArrayList<Long>();
                result.put( cardId, backlinkCards );
            }
            backlinkCards.add( backLinkCardId );
        }
        return result;
    }

	/**
     * Method returns list of card's attributes. Method auto-fills type-field of attributes received
     * @param cardIds Card IDs to find attributes for
     * @param attributes Attributes to retrieve
     * @return mapping between Card IDs and Attributes
     * @deprecated
     */
    public HashMap<Long, Collection<AttributeValue>> getCardsAttributes( long[] cardIds, Collection<AttributeValue> attributes ) {
        return getCardsAttributes( cardIds, attributes, -1, null );
    }

    /**
     * Method returns list of card's attributes. Method auto-fills type-field of attributes received
     * @param cardIds Card IDs to find attributes for
     * @param attributes Attributes to retrieve
     * @param userId ID of User who's accessing attributes
     * @param permissionTypes User permission types
     * @return mapping between Card IDs and Attributes
     */
    public HashMap<Long, Collection<AttributeValue>> getCardsAttributes( long[] cardIds, Collection<AttributeValue> attributes, int userId, long[] permissionTypes ) {
        return getCardsAttributes( cardIds, attributes, userId, permissionTypes, false );
    }

    /**
     * Method returns list of card's attributes. Method auto-fills type-field of attributes received
     * @param cardIds Card IDs to find attributes for
     * @param attributes Attributes to retrieve
     * @param userId ID of User who's accessing attributes
     * @param permissionTypes User permission types
     * @param fillEmptyAttributes True if empty attributes should be filled as well
     * @return mapping between Card IDs and Attributes
     */
    public HashMap<Long, Collection<AttributeValue>> getCardsAttributes( long[] cardIds, Collection<AttributeValue> attributes, int userId, long[] permissionTypes, boolean fillEmptyAttributes ) {
        for ( AttributeValue attr : attributes ) {
            attr.setType( getAttributeDefinition( attr ).getType() );
            attr.setRealType( getAttributeRealDefinition( attr ).getType() );
        }
        AttributesByCategory attributesByType = splitCardsAttributesToGroups( attributes );
        HashMap<Long, ArrayList<AttributeValue>> preliminaryResult = new HashMap<Long, ArrayList<AttributeValue>>();
        for ( long id : cardIds ) {
            preliminaryResult.put( id, new ArrayList<AttributeValue>() );
        }
        fillWithPlainAttributes( preliminaryResult, cardIds, attributesByType, userId, permissionTypes );
        fillWithBinaryTextAttributes( preliminaryResult, cardIds, attributesByType, userId, permissionTypes );

        HashMap<Long, HashMap<String, AttributeValue>> groupedCardAttributesMap = groupSimilarAttributes( preliminaryResult );
        HashMap<Long, Collection<AttributeValue>> result = new HashMap<Long, Collection<AttributeValue>>( groupedCardAttributesMap.size() );
        Set<Long> cards = groupedCardAttributesMap.keySet();
        for ( Long cardId : cards ) {
            HashMap<String, AttributeValue> codeToAttributeValueMap = groupedCardAttributesMap.get( cardId );
            ArrayList<AttributeValue> cardAttributes = Util.toArrayList( codeToAttributeValueMap.values() );
            result.put( cardId, cardAttributes );
            if ( !fillEmptyAttributes ) {
                continue;
            }
            for ( AttributeValue attr : attributes ) {
                String linkedCode = attr.getLinkedCode();
                String attributeCombinedCode = linkedCode == null ? attr.getCode() : attr.getCode() + linkedCode;
                if ( !codeToAttributeValueMap.containsKey( attributeCombinedCode ) ) {
                    AttributeValue attributeCopy = new AttributeValue( attr );
                    attributeCopy.setValue( new ArrayList() );
                    cardAttributes.add( attributeCopy );
                }
            }
        }
        return result;
    }
    
    public void setAttributeTypes( Collection<AttributeValue> attributes ) {
    	if(null == attributes) {
    		return;
    	}
    	
        for ( AttributeValue attr : attributes ) {
            attr.setType( getAttributeDefinition( attr ).getType() );
        }
    }

    public void setSortableAttributeTypes( Collection<SortAttribute> attributes ) {
    	if(null == attributes) {
    		return;
    	}

        for ( SortAttribute attr : attributes ) {
            if(attr.isByTemplate() || attr.isByStatus()) {
                continue;
            }
            attr.setType( getAttributeDefinition( attr ).getType() );
        }
    }

    public boolean userHasAccessToCard( ObjectId card, ObjectId userId ) {
        List allowedCards = commonQuery.getAllowedCards( new ObjectId[] { card }, userId );
        return !isEmpty( allowedCards );
    }

    public AttributeDef getAttributeDefinition( AttributeValue attributeValue ) {
        String linkedCode = attributeValue.getLinkedCode();
        return this.getAttributeDefinition(linkedCode == null ? attributeValue.getCode() : linkedCode);
    }
    
    public AttributeDef getAttributeRealDefinition( AttributeValue attributeValue ) {
        String code = attributeValue.getCode();
        return this.getAttributeDefinition(code);
    }

    public AttributeDef getAttributeDefinition( String code ) {
        if ( _attributeDefinitionCache.containsKey( code ) ) {
            return _attributeDefinitionCache.get( code );
        }
        List attributeDefinitionData = commonQuery.getAttributeDefinitionByCode( code );
        List attributeOptionsData = commonQuery.getAttributeOptions( code );
        if (attributeDefinitionData.size() == 0){
        	throw new RuntimeException("Definition of attribute with code " + code + " not found");
        }
        AttributeDef attributeDef = ( AttributeDef ) attributeDefinitionData.get( 0 );
        if ( attributeDef != null ) {
            for ( Object optionsRow : attributeOptionsData ) {
                AttributeOptionDataRow attributeOption = ( AttributeOptionDataRow ) optionsRow;
                attributeDef.setOption( attributeOption.optionCode, attributeOption.optionValue );
                if (AttributeOptions.FILTER.equals(attributeOption.optionCode)){
                    String xmlFilter = commonQuery.getXmlFilter( attributeOption.optionValue );
                    attributeDef.setFilterXml(xmlFilter);
                }
            }
        }
        _attributeDefinitionCache.put( code, attributeDef );
        return attributeDef;
    }

    public String getBackLinkAttributeCodeByAttributeCode( String code ) {
        if ( _attributeBackLinkCache.containsKey( code ) ) {
            return _attributeBackLinkCache.get( code );
        }
        String result = commonQuery.getBackLinkAttributeCodeForAttributeCode(code);
        _attributeBackLinkCache.put( code, result );
        return result;
    }

    public Template getTemplateDefinition( int id ) {
        if ( _templateDefinitionCache.size() != 0 ) {
            return _templateDefinitionCache.get( id );
        }
        synchronized ( TEMPLATE_RETRIEVE_LOCK ) {
            if ( _templateDefinitionCache.size() == 0 ) { // already filled cache
                initTemplatesDefinitions();
            }
        }
        return _templateDefinitionCache.get( id );
    }

    public Collection<Template> getAllTemplateDefinitions() {
    	synchronized ( TEMPLATE_RETRIEVE_LOCK ) {
            if ( _templateDefinitionCache.size() == 0 ) { // already filled cache
                initTemplatesDefinitions();
            }
        }
        return _templateDefinitionCache.values();
    }

    private void initTemplatesDefinitions() {
        List<Template> allTemplateDefinitions = commonQuery.getAllTemplateDefinitions();
        for ( Template template : allTemplateDefinitions ) {
            _templateDefinitionCache.put( ( (Long) template.getId().getId() ).intValue(), template );
        }
    }

    public Status getStatusDefinition( int id ) {
        if ( _statusDefinitionCache.containsKey( id ) ) {
            return _statusDefinitionCache.get( id );
        }
        Object[] result = ( Object[] ) commonQuery.getStatusDefinitionById(id).get( 0 );
        Status status = new Status();
        status.setId( id );
        status.setNameRu( ( String ) result[ 0 ] );
        status.setNameEn( ( String ) result[ 1 ] );
        _statusDefinitionCache.put( id, status );
        _statusDefinitionByRussionNameCache.put(status.getNameRu(), status);
        return status;
    }
    
    public Status getStatusDefinition( String name ) {
        if ( _statusDefinitionByRussionNameCache.containsKey( name ) ) {
            return _statusDefinitionByRussionNameCache.get( name );
        }
        Object[] result = ( Object[] ) commonQuery.getStatusDefinitionByName( name ).get( 0 );
        Status status = new Status();
        status.setId( ((Long) result[ 0 ]).intValue() );
        status.setNameRu( name );
        status.setNameEn( ( String ) result[ 1 ] );
        _statusDefinitionCache.put( status.getId(), status );
        _statusDefinitionByRussionNameCache.put(name, status);
        return status;
    }

    public long[] getCardTemplateIdAndStatusId( long cardId ) {
        List queryResult = commonQuery.getTemplateIdAndStatusIdByCardId( cardId );
        return ( long[] ) queryResult.get( 0 );
    }
    
    public List<long[]> getCardsTemplateIdAndStatusId( long[] cardIds, SearchFilter filter, long userId, int limit ) {    	
    	long[] statusIds = new long[filter.getStatusIds().size() + filter.getStatusNames().size()];
    	int i = 0;
    	for(; i < filter.getStatusIds().size(); i ++) {
    		statusIds[i] = filter.getStatusIds().get(i);
    	}
    	for(int j = 0; j < filter.getStatusNames().size(); j ++) {
    		String statusName = filter.getStatusNames().get(j);
    		Status status = getStatusDefinition(statusName);
    		if(null != status) {
    			statusIds[i + j] = status.getId();
    		}
    	}
 
    	List<List<String>> attributeCodeGroups = 
    		new ArrayList<List<String>>(filter.getAttributeCodes().size() + filter.getAttributeNames().size());
    	for(String attributeCode : filter.getAttributeCodes()) {
    		attributeCodeGroups.add(Collections.singletonList(attributeCode));
    	}
    	for(String attributeName : filter.getAttributeNames()) {
    		ArrayList<AttributeDef> attributesGroup = getAttributesByRussianName(attributeName);
    		
    		if(null == attributesGroup || attributesGroup.isEmpty()) {
    			continue;
    		}
    		
    		List<String> attributeCodesGroup = new ArrayList<String>(attributesGroup.size());
    		for(AttributeDef attributeDef : attributesGroup) {
    			attributeCodesGroup.add(attributeDef.getCode());
    		}
    		
    		attributeCodeGroups.add(attributeCodesGroup);
    	}
    	
    	
    	
        List queryResult = commonQuery.getTemplateIdAndStatusIdByCardIds( cardIds, statusIds, 
        		filter.getTemplateIdsAsArray(), attributeCodeGroups, userId,  limit );
        return queryResult;
    }

    public ArrayList<AttributeDef> getAttributesByRussianName( String nameRu ) {
        if ( _attributesByRussianNameCache.containsKey( nameRu ) ) {
            return _attributesByRussianNameCache.get( nameRu );
        }
        List attributeDefinitionData = commonQuery.getAttributeDefinitionByRussianName( nameRu );
        if ( attributeDefinitionData == null ) {
            _attributesByRussianNameCache.put( nameRu, null );
            return null;
        }
        ArrayList<AttributeDef> result = new ArrayList<AttributeDef>();
        for ( Object attributeRow : attributeDefinitionData ) {
            AttributeDef attributeDef = ( AttributeDef ) attributeRow;
            List attributeOptionsData = commonQuery.getAttributeOptions( attributeDef.getCode() );
            for ( Object optionsRow : attributeOptionsData ) {
                AttributeOptionDataRow attributeOption = ( AttributeOptionDataRow ) optionsRow;
                attributeDef.setOption( attributeOption.optionCode, attributeOption.optionValue );
            }
            result.add( attributeDef );
        }
        _attributesByRussianNameCache.put( nameRu, result );
        return result;
    }
    
    public Collection<Attribute> fillAttributeDefinitions(Collection<Attribute> attributes) {
    	
    	for(Attribute attribute : attributes) {
    		AttributeDef attributeDef = getAttributeDefinition( (String)attribute.getId().getId());
    		
    		copyProperties(attributeDef, attribute);
    	}
    	
    	return attributes;
    }
    
	/**
	 * Copies all properties values from passed attribute
	 * @param attributeDef clone to copy from 
	 * @param attrClone clone to copy to
	 */
	private void copyProperties(AttributeDef attributeDef, Attribute attrClone) {
		
			attrClone.setColumnWidth(attributeDef.getColumnWidth());
			attrClone.setNameEn(attributeDef.getNameEn());
			attrClone.setNameRu(attributeDef.getNameRu());
			attrClone.setActive(attributeDef.isActive());
			attrClone.setHidden(attributeDef.isHidden());
			attrClone.setMandatory(attributeDef.isMandatory());
			attrClone.setReadOnly((AttributeDef.BACK_LINK == attributeDef.getType()) ? true : attributeDef.isReadOnly());
			attrClone.setSystem(attributeDef.isSystem());
			
			//@TODO check all attributes...specific attributes for all types

            if (AttributeDef.CARD_LINK == attributeDef.getType() && attributeDef.getFilterXml() != null){
                ((CardLinkAttribute)attrClone).setFilterXml(attributeDef.getFilterXml());
            }

	}    
    
 
    
    public StringBuilder debugInfo( HashMap<Long, Collection<AttributeValue>> map ) {
        if ( map == null || map.size() == 0 ) {
            return new StringBuilder( "Map is empty " );
        }
        StringBuilder result = new StringBuilder( 1000 );
        for ( Long cardId : map.keySet() ) {
            result.append( cardId );
            result.append( "----------------------------------------------------" ).append( ENDL );
            Collection<AttributeValue> attributes = map.get( cardId );
            for ( AttributeValue attr : attributes ) {
                result.append( attr.getCode() ).append( " [" ).append( AttributeDef.convertToString( attr.getType() ) ).append( "] " );
                Object value = attr.getValue();
                if ( value == null ) {
                    result.append( "Value: [null]" );
                } else {
                    result.append( "Value: (" ).append( value.getClass() ).append( ") " );
                    if ( value instanceof Collection ) {
                        result.append( "{" );
                        for ( Object entry : ( Collection ) value ) {
                            result.append( entry.toString() ).append( ";" );
                        }
                        result.append( "}" );
                    } else {
                        result.append( value );
                    }
                }
                result.append( ENDL );
            }
        }
        return result;
    }

    /**
     * Groups same name attributes stored in different rows into a single attribute with array value. This relates
     * only to CardLink and Person attributes
     * @param cardAttributes card attributes to group by similar attributes
     * @return grouped card attributes
     */
    private HashMap<Long, HashMap<String, AttributeValue>> groupSimilarAttributes( HashMap<Long, ArrayList<AttributeValue>> cardAttributes ) {
        if ( cardAttributes.size() == 0 ) {
            return new HashMap<Long, HashMap<String, AttributeValue>>();
        }
        HashMap<Long, HashMap<String, AttributeValue>> result = new HashMap<Long, HashMap<String, AttributeValue>>( cardAttributes.size() );
        for ( Long cardId : cardAttributes.keySet() ) {
            ArrayList<AttributeValue> attributes = cardAttributes.get( cardId );
            HashMap<String, AttributeValue> codeToAttributeMap = new HashMap<String, AttributeValue>( attributes.size() );
            for ( AttributeValue attribute : attributes ) {
                String linkedCode = attribute.getLinkedCode();
                String attributeCombinedCode = linkedCode == null ? attribute.getCode() : attribute.getCode() + linkedCode;
                AttributeValue existingAttribute = codeToAttributeMap.get( attributeCombinedCode );
                Object attributeValue = attribute.getValue();
                if ( existingAttribute == null ) {
                    ArrayList attributeValueList = new ArrayList();
                    if ( attributeValue != null ) {
                        attributeValueList.add( attributeValue );
                    }
                    attribute.setValue( attributeValueList ); // substitute value with List
                    codeToAttributeMap.put( attributeCombinedCode, attribute );
                } else {
                    ArrayList attributeValueList = ( ArrayList ) existingAttribute.getValue();
                    if ( attributeValue != null ) {
                        attributeValueList.add( attributeValue );
                    }
                }
            }
            result.put( cardId, codeToAttributeMap );
        }
        return result;

    }

    private AttributesByCategory splitCardsAttributesToGroups( Collection<AttributeValue> attributes ) {
        ArrayList<AttributeValue> plainDirectAttributes = new ArrayList<AttributeValue>();
        ArrayList<AttributeValue> plainLinkedAttributes = new ArrayList<AttributeValue>();
        ArrayList<AttributeValue> plainLinkedByPersonAttributes = new ArrayList<AttributeValue>();
        ArrayList<AttributeValue> cardLinkAttributes = new ArrayList<AttributeValue>();
        ArrayList<AttributeValue> backLinkAttributes = new ArrayList<AttributeValue>();
        ArrayList<AttributeValue> personDirectAttributes = new ArrayList<AttributeValue>();
        ArrayList<AttributeValue> personLinkedAttributes = new ArrayList<AttributeValue>();
        ArrayList<AttributeValue> listAttributes = new ArrayList<AttributeValue>();
        ArrayList<AttributeValue> htmlDirectAttributes = new ArrayList<AttributeValue>();
        ArrayList<AttributeValue> htmlLinkedAttributes = new ArrayList<AttributeValue>();
        for ( AttributeValue attribute : attributes ) {
            int type = attribute.getType();
            if ( type == AttributeDef.CARD_LINK ) { // indirect Card Link attributes are not supported
                cardLinkAttributes.add( attribute );
            } if ( type == AttributeDef.BACK_LINK ) { // indirect Back Link attributes are not supported
                backLinkAttributes.add( attribute );
            } else if ( type == AttributeDef.PERSON ) {
                if ( attribute.getLinkedCode() == null ) {
                    personDirectAttributes.add( attribute );
                } else {
                    personLinkedAttributes.add( attribute );
                }
            } else if ( type == AttributeDef.LIST || type == AttributeDef.TREE || type == AttributeDef.TYPED_CLINK ) {
                listAttributes.add( attribute );
            } else if ( type == AttributeDef.HTML ) {
                if ( attribute.getLinkedCode() == null ) {
                    htmlDirectAttributes.add( attribute );
                } else {
                    htmlLinkedAttributes.add( attribute );
                }
            } else {
                if ( attribute.getLinkedCode() == null ) {
                    plainDirectAttributes.add( attribute );
                } else {
                    if(attribute.isLinkedByPerson()) {
                        plainLinkedByPersonAttributes.add( attribute );
                    } else {
                        plainLinkedAttributes.add( attribute );
                    }
                }
            }
        }
        return new AttributesByCategory ( plainDirectAttributes, plainLinkedAttributes, plainLinkedByPersonAttributes, cardLinkAttributes, personDirectAttributes, personLinkedAttributes, listAttributes, htmlDirectAttributes, htmlLinkedAttributes, backLinkAttributes );
    }

    private void fillWithPlainAttributes( HashMap<Long, ArrayList<AttributeValue>> result, long[] cardIds, AttributesByCategory attributesByCategory, int userId, long[] userPermissionTypes ) {
        if ( attributesByCategory.nonBinaryTextAttributesEmpty() ) {
            return;
        }
        List plainCardsAttributes = commonQuery.getPlainAttributeValues( cardIds, attributesByCategory, userId, userPermissionTypes );
        for ( Object attributesRow : plainCardsAttributes ) {
            AttributeDataRow row = ( AttributeDataRow ) attributesRow;
            AttributeValue attribute = new AttributeValue( row.attributeCode,  
            		attributesByCategory.getAttributeValueCorrectorByCode(row.attributeCode)); 
            attribute.setLinkedCode( isEmpty( row.linkedCardAttributeCode ) ? null : row.linkedCardAttributeCode );
            int type = getAttributeDefinition( attribute ).getType();
            int realType = getAttributeRealDefinition( attribute ).getType();
            attribute.setType( type );
            attribute.setRealType( realType );
            if ( type == AttributeDef.INTEGER || type == AttributeDef.CARD_LINK || type == AttributeDef.BACK_LINK ) {
                BigDecimal value = row.numericValue;
                attribute.setValue( value == null ? null : value.longValue() );
            } else if ( type == AttributeDef.STRING || type == AttributeDef.TEXT || type == AttributeDef.HTML ) {
                attribute.setValue( row.stringValue );
            } else if ( type == AttributeDef.PERSON ) {
                BigDecimal personIdBD = row.numericValue;
                if ( personIdBD != null ) {
                    String fullName = row.stringValue;
                    Person person = new Person();
                    person.setId( personIdBD.longValue() );
                    person.setFullName( fullName );
                    BigDecimal cardId = row.valueId;
                    if ( cardId != null ) {
                        person.setCardId( new ObjectId( Card.class, cardId.longValue() ) );
                    }
                    attribute.setValue( person );
                }
            } else if ( type == AttributeDef.DATE ) {
                attribute.setValue( row.dateValue );
            } else if ( type == AttributeDef.LIST || type == AttributeDef.TREE ) {
                if ( row.valueId != null ) {
                    ReferenceValue refValue = new ReferenceValue();
                    refValue.setId( row.valueId.longValue() );
                    refValue.setValueRu( row.stringValue );
                    refValue.setParent( row.parentValueId == null ? null : new ObjectId( Card.class, row.parentValueId.longValue() ) );
                    attribute.setValue( refValue );
                }
            } else if ( type == AttributeDef.TYPED_CLINK ) {
                BigDecimal cardId = row.numericValue;
                ReferenceValue refValue = null;
                if ( row.valueId != null ) {
                    refValue = new ReferenceValue();
                    refValue.setId( row.valueId.longValue() );
                    refValue.setValueRu( row.stringValue );
                    refValue.setParent( row.parentValueId == null ? null : new ObjectId( Card.class, row.parentValueId.longValue() ) );
                }
                attribute.setValue( new TypedCardLinkValue( cardId == null ? null : cardId.longValue(), refValue ) );
            }
            result.get( row.cardId ).add( attribute );
        }
    }

    private void fillWithBinaryTextAttributes( HashMap<Long, ArrayList<AttributeValue>> result, long[] cardIds, AttributesByCategory attributesByCategory, int userId, long[] userPermissionTypes ) {
        if ( attributesByCategory.binaryTextAttributesEmpty() ) {
            return;
        }
        List textAttributes = commonQuery.getBinaryTextAttributeValues( cardIds, attributesByCategory, userId, userPermissionTypes );
        for ( Object attributesRow : textAttributes ) {
            TextAttributeDataRow row = ( TextAttributeDataRow ) attributesRow;
            AttributeValue attribute = new AttributeValue( row.attributeCode );
            attribute.setLinkedCode( isEmpty( row.linkedCardAttributeCode ) ? null : row.linkedCardAttributeCode );
            attribute.setType( getAttributeDefinition( attribute ).getType() );
            attribute.setValue( row.stringValue );
            result.get( row.cardId ).add( attribute );
        }
    }

    public static class AttributeDataRow {
        public final long cardId;
        public final String attributeCode;
        public final String linkedCardAttributeCode;
        public final BigDecimal numericValue;
        public final String stringValue;
        public final Timestamp dateValue;
        public final BigDecimal valueId;
        public final BigDecimal parentValueId;

        public AttributeDataRow( long cardId, String attributeCode, String linkedCardAttributeCode, BigDecimal numericValue, String stringValue, Timestamp dateValue, BigDecimal valueId, BigDecimal parentValueId ) {
            this.cardId = cardId;
            this.attributeCode = attributeCode;
            this.linkedCardAttributeCode = linkedCardAttributeCode;
            this.numericValue = numericValue;
            this.stringValue = stringValue;
            this.dateValue = dateValue;
            this.valueId = valueId;
            this.parentValueId = parentValueId;
        }
    }

    public static class TextAttributeDataRow {
        public final long cardId;
        public final String attributeCode;
        public final String linkedCardAttributeCode;
        public final String stringValue;

        public TextAttributeDataRow( long cardId, String attributeCode, String linkedCardAttributeCode, String value ) {
            this.cardId = cardId;
            this.attributeCode = attributeCode;
            this.linkedCardAttributeCode = linkedCardAttributeCode;
            this.stringValue = value;
        }
    }

    public static class AttributeOptionDataRow {
        public final String optionCode;
        public final String optionValue;

        public AttributeOptionDataRow( String optionCode, String optionValue ) {
            this.optionCode = optionCode;
            this.optionValue = optionValue;
        }
    }

    public static class AttributesByCategory {
    	
    	public AttributeValueCorrector getAttributeValueCorrectorByCode(String code) {
    		for(AttributeValue attribute:allAttributesForPlainDirectQuery) 
    			if(attribute.getCode().equals(code)) return attribute.getCorrector();

    		for(AttributeValue attribute:plainDirectAttributes) 
    			if(attribute.getCode().equals(code)) return attribute.getCorrector();

    		for(AttributeValue attribute:plainLinkedAttributes) 
    			if(attribute.getCode().equals(code)) return attribute.getCorrector();			
    		
    		for(AttributeValue attribute:plainLinkedByPersonAttributes) 
    			if(attribute.getCode().equals(code)) return attribute.getCorrector();

    		for(AttributeValue attribute:cardLinkAttributes) 
    			if(attribute.getCode().equals(code)) return attribute.getCorrector();
    		
    		for(AttributeValue attribute:backLinkAttributes) 
    			if(attribute.getCode().equals(code)) return attribute.getCorrector();

    		for(AttributeValue attribute:personDirectAttributes) 
    			if(attribute.getCode().equals(code)) return attribute.getCorrector();
    		
    		for(AttributeValue attribute:personLinkedAttributes) 
    			if(attribute.getCode().equals(code)) return attribute.getCorrector();
    		
    		for(AttributeValue attribute:listAttributes) 
    			if(attribute.getCode().equals(code)) return attribute.getCorrector();
    		
    		for(AttributeValue attribute:htmlDirectAttributes) 
    			if(attribute.getCode().equals(code)) return attribute.getCorrector();
    		
    		for(AttributeValue attribute:htmlIndirectAttributes) 
    			if(attribute.getCode().equals(code)) return attribute.getCorrector();
    		
			return null;
    	}
    	
    	
        public final ArrayList<AttributeValue> plainDirectAttributes;
        public final ArrayList<AttributeValue> plainLinkedAttributes;
        public final ArrayList<AttributeValue> plainLinkedByPersonAttributes;
        public final ArrayList<AttributeValue> cardLinkAttributes;
        public final ArrayList<AttributeValue> backLinkAttributes;
        public final ArrayList<AttributeValue> personDirectAttributes;
        public final ArrayList<AttributeValue> personLinkedAttributes;
        public final ArrayList<AttributeValue> listAttributes;
        public final ArrayList<AttributeValue> htmlDirectAttributes;
        public final ArrayList<AttributeValue> htmlIndirectAttributes;

        public final ArrayList<AttributeValue> allAttributesForPlainDirectQuery;

        public AttributesByCategory( ArrayList<AttributeValue> plainDirectAttributes, ArrayList<AttributeValue> plainLinkedAttributes,
                                     ArrayList<AttributeValue> plainLinkedByPersonAttributes, ArrayList<AttributeValue> cardLinkAttributes,
                                     ArrayList<AttributeValue> personDirectAttributes, ArrayList<AttributeValue> personLinkedAttributes,
                                     ArrayList<AttributeValue> listAttributes, ArrayList<AttributeValue> htmlDirectAttributes,
                                     ArrayList<AttributeValue> htmlIndirectAttributes, ArrayList<AttributeValue> backLinkAttributes) {
            this.plainDirectAttributes = plainDirectAttributes;
            this.plainLinkedAttributes = plainLinkedAttributes;
            this.plainLinkedByPersonAttributes = plainLinkedByPersonAttributes;
            this.cardLinkAttributes = cardLinkAttributes;
            this.backLinkAttributes = backLinkAttributes;
            this.personDirectAttributes = personDirectAttributes;
            this.personLinkedAttributes = personLinkedAttributes;
            this.listAttributes = listAttributes;
            this.htmlDirectAttributes = htmlDirectAttributes;
            this.htmlIndirectAttributes = htmlIndirectAttributes;

            allAttributesForPlainDirectQuery = new ArrayList<AttributeValue>();
            allAttributesForPlainDirectQuery.addAll( plainDirectAttributes );
            allAttributesForPlainDirectQuery.addAll( cardLinkAttributes );
        }

        public boolean nonBinaryTextAttributesEmpty() {
            return
                isEmpty( plainDirectAttributes )
                && isEmpty( plainLinkedAttributes )
                && isEmpty( plainLinkedByPersonAttributes )
                && isEmpty( cardLinkAttributes )
                && isEmpty( backLinkAttributes )
                && isEmpty( personDirectAttributes )
                && isEmpty( personLinkedAttributes )
                && isEmpty( listAttributes );
        }

        public boolean binaryTextAttributesEmpty() {
            return
                isEmpty( htmlDirectAttributes )
                && isEmpty( htmlIndirectAttributes );
        }
    }
}
