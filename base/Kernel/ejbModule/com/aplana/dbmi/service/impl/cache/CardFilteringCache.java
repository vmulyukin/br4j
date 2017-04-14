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
package com.aplana.dbmi.service.impl.cache;

import static com.aplana.dbmi.service.impl.cache.CardFilteringCacheDataReader.CARD_ID_COLUMN;
import static com.aplana.dbmi.service.impl.cache.CardFilteringCacheDataReader.STRING_VALUE_COLUMN;
import static com.aplana.dbmi.service.impl.cache.CardFilteringCacheDataReader.UPPERCASED_STRING_VALUE_COLUMN;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.utils.StrUtils;
import com.aplana.util.Table;

/**
 * @author Denis Mitavskiy
 *         Date: 18.02.11
 */
public class CardFilteringCache implements ApplicationContextAware {
    private static ApplicationContext CONTEXT;

    private static CardFilteringCache _instance;
    private static final Object CREATION_LOCK = new Object();
    private static final Object CURRENTLY_RETRIEVING_LOCK = new Object();
    private static final Set<Integer> SUPPORTED_TEMPLATE_IDS;

    static {
        SUPPORTED_TEMPLATE_IDS = new HashSet<Integer>();
        //SUPPORTED_TEMPLATE_IDS.add( 222 ); // organization
        
        SUPPORTED_TEMPLATE_IDS.add( 464 ); // outer person
        SUPPORTED_TEMPLATE_IDS.add( 806 ); // country
        SUPPORTED_TEMPLATE_IDS.add( 807 ); // subject
        SUPPORTED_TEMPLATE_IDS.add( 824 ); // settlement
        SUPPORTED_TEMPLATE_IDS.add( 13 ); // street
    }

    private CacheUpdater _cacheUpdater;
    private Map<Key, Table> _cardsByTemplateIdAndStatusId;

    private CardFilteringCache() {
        _cacheUpdater = new CacheUpdater( this );
        _cardsByTemplateIdAndStatusId = Collections.synchronizedMap( new HashMap<Key, Table>() );
    }

    public static CardFilteringCache instance() {
        synchronized ( CREATION_LOCK ) {
            if ( _instance == null ) {
                _instance = new CardFilteringCache();
                _instance._cacheUpdater.setDaemon( true );
                _instance._cacheUpdater.setPriority( Thread.MIN_PRIORITY );
                _instance._cacheUpdater.start();
            }
            return _instance;
        }
    }

    // ������ ������� ����
    public void updateCache(){
        _instance._cacheUpdater.updateAllByEvent();
    }

    public ArrayList<ObjectId> findCards( Search search, Long personId ) {
        if ( !useCache( search ) ) {
            throw new IllegalArgumentException( "This search type is not supported by Cache" );
        }
        Set<Integer> templateIds = getTemplateIds( search );
        Collection states = search.getStates();
        Integer statusId = states == null || states.isEmpty() ? null : ( ( Long ) states.iterator().next() ).intValue();
        List<Long> permissionTypes = getPermissionTypes( search );
        TextFilter textFilter = new TextFilter( search );
        Set<Long> ignoredCardIds = toPlainIds( search.getIgnoredIds() );

        int[] pageSizeAndOffset = getPageSizeAndOffset( search );
        int qty = pageSizeAndOffset == null ? -1 : pageSizeAndOffset[ 0 ];
        int offset = pageSizeAndOffset == null ? -1 : pageSizeAndOffset[ 1 ];

        Table allCards = getAllCards( new Key( templateIds, statusId ) );
        search.getFilter().setWholeSize(allCards.size());
        ArrayList<Integer> cardsIndexes = getCardIndexes( allCards, personId, permissionTypes, textFilter, ignoredCardIds, qty, offset );
        ArrayList<ObjectId> result = new ArrayList<ObjectId>();
        if ( cardsIndexes == null ) {
            return result;
        }
        ArrayList<Object> cardIds = allCards.getColumn( "CARD_ID" );
        for ( Integer cardIndex : cardsIndexes ) {
            result.add( new ObjectId( Card.class, cardIds.get( cardIndex ) ) );
        }
        return result;
    }

    private static CardFilteringCacheDataReader getDataReader() {
        return ( CardFilteringCacheDataReader ) CONTEXT.getBean( "cardFilteringCacheDataReader" );
    }

    private Set<Integer> getTemplateIds( Search search ) {
        Collection templates = search.getTemplates();
        HashSet<Integer> result = new HashSet<Integer>( templates.size() );
        for ( Object template : templates ) {
            result.add( ( ( Long ) ( ( Template ) template ).getId().getId() ).intValue() );
        }
        return result;
    }

    private boolean useCache( Search search ) {
        if ( !search.isByAttributes() || search.isByCode() || search.isByMaterial() || search.isBySql() ) {
            return false;
        }

        // support name attribute, 1 template and 0 or 1 statuses
        Collection attributes = search.getAttributes();
        if ( attributes == null || attributes.size() != 1 || !( ( Map.Entry ) attributes.iterator().next() ).getKey().equals( "NAME" ) ) {
            return false;
        }
        Collection templates = search.getTemplates();
        if ( templates == null || templates.size() == 0 ) {
            return false;
        }
        if ( !SUPPORTED_TEMPLATE_IDS.containsAll( getTemplateIds( search ) ) ) {
            return false;
        }
        Collection statuses = search.getStates();
        if ( statuses != null && statuses.size() > 1 ) {
            return false;
        }
        boolean notNullExternalPath = String.valueOf( Card.MATERIAL_URL ).equals( StrUtils.getAsString( search.getMaterialTypes(), null ) );
        if ( notNullExternalPath ) {
            return false;
        }

        Object searchConfig = ( ( Map.Entry ) search.getAttributes().iterator().next() ).getValue();
        if ( searchConfig != null && searchConfig instanceof Search.TextSearchConfigValue ) {
            int searchType = ( ( Search.TextSearchConfigValue ) searchConfig ).searchType;
            if ( searchType == Search.TextSearchConfigValue.STARTS_FROM ) { // this search config actually not used in the system anywhere at this moment
                return false;
            }
        }
        return true;
    }

    private List<Long> getPermissionTypes( Search search ) {
        Search.Filter filter = search.getFilter();
        if ( filter == null ) {
            return null;
        }
        Long userPermission = filter.getCurrentUserPermission();
        if ( userPermission.equals( Search.Filter.CU_DONT_CHECK_PERMISSIONS ) ) {
            return null;
        }

        ArrayList<Long> result = new ArrayList<Long>();
        if ( userPermission.equals( Search.Filter.CU_RW_PERMISSIONS ) ) {
            result.add( Search.Filter.CU_READ_PERMISSION );
            result.add( Search.Filter.CU_WRITE_PERMISSION );
        } else if ( userPermission.equals( Search.Filter.CU_READ_PERMISSION ) ) {
            result.add( Search.Filter.CU_READ_PERMISSION );
        } else if ( userPermission.equals( Search.Filter.CU_WRITE_PERMISSION ) ) {
            result.add( Search.Filter.CU_READ_PERMISSION );
            result.add( Search.Filter.CU_WRITE_PERMISSION );
        } else {
            result.add( 0L );
        }
        return result;
    }

    private int[] getPageSizeAndOffset( Search search ) {
        Search.Filter filter = search.getFilter();
        if ( filter == null ) {
            return null;
        }
        int pageSize = filter.getPageSize();
        if ( pageSize <= 0 ) {
            return null;
        }
        int page = filter.getPage();
        page = page > 0 ? page : 1;
        return new int[] { pageSize, ( page - 1 ) * pageSize };
    }

    private ArrayList<Integer> getCardIndexes( Table allCards, Long personId, List<Long> permissionTypes, TextFilter textFilter, Set<Long> ignoredCardIds, int qty, int offset ) {
        int startIndex = 0;
        if ( offset > 0 ) {
            ArrayList<Integer> cardIndexes = getCardIndexes( allCards, 0, personId, permissionTypes, textFilter, ignoredCardIds, offset );
            if ( cardIndexes.size() < offset ) {
                return null;
            }
            startIndex = cardIndexes.get( offset - 1 ) + 1;
        }
        return getCardIndexes( allCards, startIndex, personId, permissionTypes, textFilter, ignoredCardIds, qty );
    }

    private ArrayList<Integer> getCardIndexes( Table allCards, int startIndex, Long personId, List<Long> permissionTypes, TextFilter textFilter, Set<Long> ignoredCardIds, int qty ) {
        ArrayList<Integer> resultIndexes = new ArrayList<Integer>( qty <= 0 ? 10 : qty );
        while ( true ) {
            ArrayList<Integer> indexesAfterTextFiltering = findByFilter( allCards, textFilter, ignoredCardIds, startIndex, qty );
            int cardsFoundQty = indexesAfterTextFiltering.size();
            boolean nothingFound = cardsFoundQty == 0;
            boolean foundEnd = nothingFound || cardsFoundQty != qty || qty <= 0;
            if ( nothingFound ) {
                return resultIndexes;
            }

         // �������� �� ����� �������, ������������� �� ������ ����� ��� ������ � DoSearch 
         //          ArrayList<Integer> indexesAfterAccessFiltering = filterByUserAccessRights( allCards, indexesAfterTextFiltering, personId, permissionTypes );
          ArrayList<Integer> indexesAfterAccessFiltering = new ArrayList<Integer>(indexesAfterTextFiltering);
            int qtyAfterUserRightsFiltering = indexesAfterAccessFiltering.size();
            int resultSize = resultIndexes.size();
            int toCopyQty = qty <= 0 ? qtyAfterUserRightsFiltering : qty - resultSize;
            if ( toCopyQty > qtyAfterUserRightsFiltering ) {
                toCopyQty = qtyAfterUserRightsFiltering;
            }

            for ( int k = 0; k < toCopyQty; ++k ) {
                resultIndexes.add( indexesAfterAccessFiltering.get( k ) );
            }

            if ( foundEnd || resultSize == qty ) {
                break;
            } else {
                startIndex = indexesAfterTextFiltering.get( cardsFoundQty - 1 ) + 1;
            }
        }
        return resultIndexes;
    }

    private Table getAllCards( Key key ) {
        if ( _cardsByTemplateIdAndStatusId.containsKey( key ) ) {
            return _cardsByTemplateIdAndStatusId.get( key );
        }
        _cacheUpdater.initialize( key );
        while ( true ) {
            if ( _cardsByTemplateIdAndStatusId.containsKey( key ) ) {
                return _cardsByTemplateIdAndStatusId.get( key );
            }
            try {
                Thread.sleep( 200 );
            } catch ( InterruptedException e ) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Finds cards in the table by filter
     * @param table Table to search in
     * @param textFilter Filter to use
     * @param ignoredCardIds Card IDs to ignore during search
     * @param startIndex index in the table to start search from
     * @param qty cards quantity to find
     * @return indexes of cards in the table
     */
    private ArrayList<Integer> findByFilter( final Table table, final TextFilter textFilter, final Set<Long> ignoredCardIds, final int startIndex, final int qty ) {
        int size = table.size();
        ArrayList<Integer> result = new ArrayList<Integer>( qty < 0 ? 10 : qty );
        for ( int i = startIndex; i < size; ++i ) {
            if ( ignoredCardIds != null && ignoredCardIds.contains( table.get( CARD_ID_COLUMN, i ) ) ) {
                continue;
            }
            if ( match( table, i, textFilter ) ) {
                result.add( i );
                if ( result.size() == qty ) {
                    return result;
                }
            }
        }
        return result;
    }

    private boolean match( final Table table, final int row, final TextFilter textFilter ) {
        if ( textFilter.noFilter ) {
            return true;
        }
        if ( textFilter.exactSearch ) {
            final String cachedText = ( String ) table.get( STRING_VALUE_COLUMN, row );
            if ( cachedText == null ) {
                return false; // because textFilter.noFilter is false meaning a filter is really set up
            }
            return cachedText.equals( textFilter.filterText );
        }

        final String cachedText = ( String ) table.get( UPPERCASED_STRING_VALUE_COLUMN, row );
        if ( cachedText == null ) {
            return false;
        }
        if ( textFilter.pattern == null ) {
            return cachedText.contains( textFilter.filterTextUppercased );
        } else {
            return textFilter.pattern.matcher( cachedText ).find();
        }
    }

    private ArrayList<Integer> filterByUserAccessRights( Table table, ArrayList<Integer> indexes, Long personId, List<Long> permissionTypes ) {
        if ( permissionTypes == null ) {
            return indexes;
        }
        ArrayList<Long> cardIds = getCardIds( table, indexes );
        Map<Long, Boolean> cardsPermissions = getDataReader().findCardsPermissions( cardIds, personId, permissionTypes );
        int size = indexes.size();
        ArrayList<Integer> resultIndexes = new ArrayList<Integer>( size );
        for ( int i = 0; i < size; ++i ) {
            Long cardId = cardIds.get( i );
            if ( cardsPermissions.get( cardId ) ) {
                Integer index = indexes.get( i );
                resultIndexes.add( index );
            }
        }
        return resultIndexes;
    }

    private ArrayList<Long> getCardIds( Table table, List<Integer> indexes ) {
        ArrayList<Long> result = new ArrayList<Long>( indexes.size() );
        for ( Integer index : indexes ) {
            result.add( (Long) table.get( CARD_ID_COLUMN, index ) );
        }
        return result;
    }


    private boolean contains( Key key ) {
        return _cardsByTemplateIdAndStatusId.containsKey( key );
    }

    private Set<Long> toPlainIds( Set<ObjectId> ids ) {
        if ( ids == null ) {
            return null;
        }
        HashSet<Long> result = new HashSet<Long>( ids.size() );
        for ( ObjectId objectId : ids ) {
            result.add( (Long) objectId.getId() );
        }
        return result;
    }

    private static class CacheUpdater extends Thread {
        private static final int CACHE_UPDATE_PERIOD_MILLIES = 5 * 60 * 1000;
        private CardFilteringCache _cache;
        private HashSet<Key> _currentlyRetrievingTemplateIdsAndStatusIds;
        private final Set<Key> _toInitialize;
        Date nextUpdate;

        public CacheUpdater( CardFilteringCache cache ) {
            _cache = cache;
            _currentlyRetrievingTemplateIdsAndStatusIds = new HashSet<Key>();
            _toInitialize = Collections.synchronizedSet( new HashSet<Key>() );
        }

        @Override
        public void run() {
            nextUpdate = getNextUpdateTime();
            while ( true ) {
                initializeAll();
                if ( new Date().compareTo( nextUpdate ) > 0 ) {
                    updateAll();
                    nextUpdate = getNextUpdateTime();
                }
                try {
                    Thread.sleep( 200 );
                } catch ( InterruptedException e ) {
                    e.printStackTrace();
                }
            }
        }

        /**
         * Method called when cache should be initialized for some Template ID/Status ID pair. If initialized, request is ignored
         * @param templateIdStatusId Template ID/Status ID pair
         */
        public void initialize( Key templateIdStatusId ) {
            if ( needToInitialize( templateIdStatusId ) ) {
                synchronized ( _toInitialize ) {
                    _toInitialize.add( templateIdStatusId );
                }
            }
        }

        private void initializeAll() {
            synchronized ( _toInitialize ) {
                if ( _toInitialize.size() == 0 ) {
                    return;
                }
                for ( Key keys : _toInitialize ) {
                    if ( needToInitialize( keys ) ) {
                        update( keys );
                    }
                    _toInitialize.remove( keys );
                }
            }
        }

        // ������� ���� �� �������
        private void updateAllByEvent() {
            updateAll();
            nextUpdate = getNextUpdateTime();
        }

        private void updateAll() {
            synchronized ( _toInitialize ) {
                Set<Key> toUpdate = _cache._cardsByTemplateIdAndStatusId.keySet();
                for ( Key templateIdStatusId : toUpdate ) {
                    update( templateIdStatusId );
                }
            }
        }

        private void update( Key key ) {
            synchronized ( CURRENTLY_RETRIEVING_LOCK ) {
                if ( !_currentlyRetrievingTemplateIdsAndStatusIds.contains( key ) ) {
                    _currentlyRetrievingTemplateIdsAndStatusIds.add( key );
                } else {
                    return; // we're already retrieving this template ID + status ID - nothing to do
                }
            }

            CardFilteringCacheDataReader dataReader = getDataReader();
            Table cards = dataReader.findCards( key.getTemplateIds(), key.getStatusId(), "NAME" ); // at this moment we support "NAME"-attribute only
            _cache._cardsByTemplateIdAndStatusId.put( key, cards );
            synchronized ( CURRENTLY_RETRIEVING_LOCK ) {
                _currentlyRetrievingTemplateIdsAndStatusIds.remove( key );
            }
        }

        private boolean needToInitialize( Key key ) {
            if ( _cache.contains( key ) ) {
                return false;
            }
            if ( _currentlyRetrievingTemplateIdsAndStatusIds.contains( key ) ) {
                return false;
            }
            return true;
        }

        private Date getNextUpdateTime() {
            return new Date( new Date().getTime() + CACHE_UPDATE_PERIOD_MILLIES );
        }
    }

    private static class Key {
        private Set<Integer> _templateIds;
        private Integer _statusId;

        private Key( Set<Integer> templateIds, Integer statusId ) {
            _templateIds = templateIds;
            _statusId = statusId;
        }

        public Integer getSingleTemplateId() {
            return _templateIds.iterator().next();
        }

        public Set<Integer> getTemplateIds() {
            return _templateIds;
        }

        public Integer getStatusId() {
            return _statusId;
        }

        @Override
        public boolean equals( Object o ) {
            if ( this == o ) {
                return true;
            }
            if ( o == null || getClass() != o.getClass() ) {
                return false;
            }

            Key cacheKey = ( Key ) o;

            if ( _statusId != null ? !_statusId.equals( cacheKey._statusId ) : cacheKey._statusId != null ) {
                return false;
            }
            if ( !_templateIds.equals( cacheKey._templateIds ) ) {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode() {
            int result = _templateIds.hashCode();
            result = 31 * result + ( _statusId != null ? _statusId.hashCode() : 0 );
            return result;
        }
    }

    private static class TextFilter {
        private final Pattern pattern;
        private final String filterTextUppercased;
        private final String filterText;
        private final boolean exactSearch;
        private final boolean noFilter;

        private TextFilter( Search search ) {
            String filterText;
            boolean exactSearch;
            Object searchConfig = ( ( Map.Entry ) search.getAttributes().iterator().next() ).getValue();
            if ( searchConfig != null && searchConfig instanceof Search.TextSearchConfigValue ) {
                int searchType = ( ( Search.TextSearchConfigValue ) searchConfig ).searchType;
                filterText = ( ( Search.TextSearchConfigValue ) searchConfig ).value;
                if ( searchType == Search.TextSearchConfigValue.EXACT_MATCH ) {
                    exactSearch = true;
                } else if ( searchType == Search.TextSearchConfigValue.CONTAINS ) {
                    exactSearch = false;
                } else {
                    exactSearch = false; // STARTS_FROM is not supported currently (cache will throw exception in useCache())
                }
            } else {
                filterText = search.getWords();
                exactSearch = false;
            }

            this.exactSearch = exactSearch;
            this.filterText = filterText;
            if ( exactSearch ) {
                this.pattern = null;
                this.filterTextUppercased = null;
                this.noFilter = filterText == null || "".equals( filterText );
                return;
            }

            if ( filterText == null || "".equals( filterText.trim() ) ) {
                this.noFilter = true;
                this.pattern = null;
                this.filterTextUppercased = null;
                return;
            }

            this.noFilter = false;

            String trimmed = filterText.trim();
            Pattern wildcardsPattern = Pattern.compile( "[%*_ \\\\]+" );
            String filterWithReplacedWildcards =wildcardsPattern.matcher( trimmed ).replaceAll( ".*" );
            filterWithReplacedWildcards = filterWithReplacedWildcards.replaceAll( "\\(", "\\\\(" );
            filterWithReplacedWildcards = filterWithReplacedWildcards.replaceAll( "\\)", "\\\\)" );
            if ( filterWithReplacedWildcards.equals( filterText ) ) { // simple filter without "%", "_" or space symbols. no need to use Patterns due to slowness
                this.pattern = null;
                this.filterTextUppercased = filterWithReplacedWildcards.toUpperCase();
            } else {
                this.pattern = Pattern.compile( filterWithReplacedWildcards.toUpperCase() );
                this.filterTextUppercased = null;
            }
        }
    }

    public static void main( String[] args ) {
        CardFilteringCache cache = CardFilteringCache.instance();
        ArrayList<Long> times = new ArrayList<Long>();

        for ( int i = 0; i < 1000; i += 10 ) {
            long t1 = System.currentTimeMillis();
            ArrayList<Long> permissionTypes = new ArrayList<Long>();
            permissionTypes.add( 2L );
            Set<Integer> templateId_1 = new HashSet<Integer>();
            templateId_1.add( 100 );
            Set<Integer> templateId_2 = new HashSet<Integer>();
            templateId_2.add( 100 );
            Key key1 = new Key( templateId_1, 2 );
            Key key2 = new Key( templateId_2, null );
            Search search = new Search();
            search.setWords( "TEMPLATE" );
            System.out.println( i + ": " + cache.getCardIndexes( cache.getAllCards( key1 ), 10L, permissionTypes, new TextFilter( search ), null, 15, i ) );

            search.setWords( "asdkfhlaskjdhgaslkldjg9238y592813467" );
            System.out.println( i + ": " + cache.getCardIndexes( cache.getAllCards( key2 ), 10L, null, new TextFilter( search ), null, 15, i ) );
            long t2 = System.currentTimeMillis();
            times.add( t2 - t1 );
        }
        System.out.println( times );
    }

	@Override
	public void setApplicationContext(ApplicationContext context)
			throws BeansException {
		CONTEXT = context;
		
	}
}
