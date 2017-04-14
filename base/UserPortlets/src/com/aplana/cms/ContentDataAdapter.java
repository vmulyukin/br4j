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
package com.aplana.cms;

import java.io.UnsupportedEncodingException;
import java.util.*;

import com.aplana.cms.cache.CounterCache;
import com.aplana.cms.view_template.AttributeMapper;
import com.aplana.cms.view_template.LinkViewAttribute;
import com.aplana.cms.view_template.CardViewData;
import com.aplana.cms.view_template.ColumnSortAttributes;
import com.aplana.cms.view_template.SortAttributeMapper;
import com.aplana.cms.view_template.ViewAttribute;
import com.aplana.dbmi.action.Action;
import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.model.*;
import com.aplana.dbmi.model.workstation.AttributeValue;
import com.aplana.dbmi.model.workstation.SearchFilter;
import com.aplana.dbmi.model.workstation.SortAttribute;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.ServiceException;
import com.aplana.dbmi.service.ServiceLocator;
import com.aplana.dbmi.service.workstation.AreaWorkstationDataServiceInterface;
import com.aplana.dbmi.service.workstation.CommonCardDataServiceInterface;

import com.aplana.dbmi.service.workstation.GetCardAreaDTO;
import com.aplana.dbmi.service.workstation.GetCardAreaQtyDTO;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Represents Content Data Adapter
 *
 * @author skashanski
 */
public class ContentDataAdapter implements ContentDataInterface, ContentProducerSetterInterface {


	private static final String CARD_ON_PERSONAL_EXECUTION_FOR_SUPERVISOR_ATTRIBUTES = "cardOnPersonalExecutionForSupervisorAttributes";
	
    private static final String CONSIDER_URGENTLY_CARDS_FOR_SUPERVISOR_ATTRIBUTES = "considerUrgentlyCardsForSupervisorAttributes";

    private static final String SIGN_URGENTLY_CARDS_FOR_SUPERVISOR_ATTRIBUTES = "signUrgentlyCardsForSupervisorAttributes";

    private static final String SIGN_CARDS_FOR_SUPERVISOR_ATTRIBUTES = "signCardsForSupervisorAttributes";

    private static final String CARD_ON_CONSIDERATION_FOR_SUPERVISOR_ATTRIBUTES = "cardOnConsiderationForSupervisorAttributes";

    private static final String CARD_ON_EXECUTION_FOR_SUPERVISOR_ATTRIBUTES = "cardOnExecutionForSupervisorAttributes";
    
    private static final String EXTERNAL_CONTROL_FOR_SUPERVISOR_ATTRIBUTES = "externalControlCardsForSupervisorAttributes";

    private static final String MY_DOCUMENTS_CARDS_FOR_SUPERVISOR_ATTRIBUTES = "myDocumentsForSupervisorAttributes";

    private static final String DELEGATION_CARDS_FOR_SUPERVISOR_ATTRIBUTES = "delegationsForSupervisorAttributes";

    private static final String DELEGATION_HISTORY_CARDS_FOR_SUPERVISOR_ATTRIBUTES = "delegationsHistoryForSupervisorAttributes";
    
    private static final String ADVANCED_SEARCH_CARDS_FOR_SUPERVISOR_ATTRIBUTES = "advancedSearchForSupervisorAttributes";
    
    private static final String SENT_DOCS_FOR_SUPERVISOR_ATTRIBUTES = "sentDocsForSupervisorAttributes";
    
    protected Log logger = LogFactory.getLog(getClass());


    private ContentProducer contentProducer = null;


    private CommonCardDataServiceInterface commonCardDataService = null;
    


    public void setContentProducer(ContentProducer contentProducer) {
        this.contentProducer = contentProducer;

    }
    
    
    /**
     * Sign Folder for supervisor
     */
    public static final String SUP_SIGN_FOLDER_ID = "47320";
    public static final String SUP_SIGN_COORD_FOLDER_ID = "46900";
    public static final String SUP_SIGN_SIGN_FOLDER_ID = "44380";
    
    
    /**
     * Sign Urgently Folder for supervisor
     */
    public static final String SUP_SIGN_URGENTLY_FOLDER_ID = "47445";
    public static final String SUP_SIGN_URGENTLY_COORD_FOLDER_ID = "8008";
    public static final String SUP_SIGN_URGENTLY_SIGN_FOLDER_ID = "8009";

    
    /**
     * On Execution Folder for supervisor
     */
    public static final String ON_EXECUTION_FOLDER_ID = "9102";
    public static final String ON_EXECUTION_MISSED_DEADLINE_FOLDER_ID = "8018";
    public static final String ON_EXECUTION_DEADLINE_TOMORROW_FOLDER_ID = "8019";
    public static final String ON_EXECUTION_DEADLINE_IN_3_DAYS_FOLDER_ID = "8020";
    public static final String ON_EXECUTION_DEADLINE_IN_WEEK_FOLDER_ID = "8021";
    
    /**
     * On Execution Folder and control for supervisor
     */
    public static final String ON_EXECUTIONCONT_FOLDER_ID = "8610";
    public static final String ON_EXECUTIONCONT_MISSED_DEADLINE_FOLDER_ID = "8611";
    public static final String ON_EXECUTIONCONT_DEADLINE_TOMORROW_FOLDER_ID = "8612";
    public static final String ON_EXECUTIONCONT_DEADLINE_IN_3_DAYS_FOLDER_ID = "8613";
    public static final String ON_EXECUTIONCONT_DEADLINE_IN_WEEK_FOLDER_ID = "8614";

    
    /**
     * Personal execution folder for supervisor
     */    
    public static final String PERSONAL_EXECUTION_FOLDER_ID = "47100";    
    public static final String PERSONAL_EXECUTION_MISSED_DEADLINE_FOLDER_ID = "8014";
    public static final String PERSONAL_EXECUTION_DEADLINE_TOMORROW_FOLDER_ID = "8015";
    public static final String PERSONAL_EXECUTION_DEADLINE_IN_3_DAYS_FOLDER_ID = "8016";
    public static final String PERSONAL_EXECUTION_DEADLINE_IN_WEEK_FOLDER_ID = "8017";
    
    
    /**
     * On consideration Folder for supervisor
     */
    public static final String SUP_CONSIDER_FOLDER_ID = "41460";
    public static final String SUP_CONSIDER_CONSIDER_FOLDER_ID = "8002";
    public static final String SUP_CONSIDER_ACQUAINT_FOLDER_ID = "8004";
    public static final String SUP_CONSIDER_EXECUTE_FOLDER_ID = "8003";
    
    
    public static final String SUP_CONSIDER_URGENTLY_FOLDER_ID = "47447";
    public static final String SUP_CONSIDER_URGENTLY_CONSIDER_FOLDER_ID = "8005";
    public static final String SUP_CONSIDER_URGENTLY_ACQUAINT_FOLDER_ID = "8007";
    public static final String SUP_CONSIDER_URGENTLY_EXECUTE_FOLDER_ID = "8006";


    /**
     * On delegation Folder for supervisor
     */
    public static final String SUP_DELEGATION_FOLDER_ID = "8060";
    public static final String SUP_DELEGATION_DELEGATED_TO_ME_FOLDER_ID = "8063";
    public static final String SUP_DELEGATION_MY_DELEGATIONS_FOLDER_ID = "8064";
    public static final String SUP_DELEGATION_DELEGATION_HISTORY_FOLDER_ID = "8065";

  
    /**
     * folder contains reports for supervisor
     */
    public static final String SUP_APPROVE_FOLDER_ID = "45021";
    
    /**
     * folder contains cards that are on Personal Control for supervisor 
     */
    public static final String SUP_PERS_CONTROL_FOLDER_ID = "8544";
    
    
    /**
     * folder contains cards that are on Control for supervisor 
     */
    public static final String SUP_EXTERNAL_CONTROL_FOLDER_ID = "8543";   
    public static final String SUP_EXTERNAL_CONTROL_MISSED_DEADLINE_FOLDER_ID = "8010";
    public static final String SUP_EXTERNAL_CONTROL_DEADLINE_TOMORROW_FOLDER_ID = "8011";
    public static final String SUP_EXTERNAL_CONTROL_DEADLINE_IN_3_DAYS_FOLDER_ID = "8012";  
    public static final String SUP_EXTERNAL_CONTROL_DEADLINE_IN_WEEK_FOLDER_ID = "8013";


    public static final String SUP_MY_DOCUMENTS_ALL_FOLDER_ID = "8050";
    public static final String SUP_MY_DOCUMENTS_AGE_1_MONTH_FOLDER_ID = "8048";
    public static final String SUP_MY_DOCUMENTS_AGE_3_MONTHS_FOLDER_ID = "8049";
    
    public static final String SUP_MY_DOCUMENTS_FOR_REWORK_FOLDER_ID = "8056";
    public static final String SUP_FAVORITES_FOLDER_ID = "8058";
    
    public static final String SUP_SENT_DOCS_ALL_FOLDER_ID          = "8100";
    public static final String SUP_SENT_DOCS_AGE_1_MONTH_FOLDER_ID  = "8108";
    public static final String SUP_SENT_DOCS_AGE_3_MONTHS_FOLDER_ID = "8109";
    public static final String SUP_SENT_DOCS_AGE_6_MONTHS_FOLDER_ID = "8110";
    /**
     * On consideration Folder for minister
     */
    public static final String MINISTER_CONSIDER_FOLDER_ID = "8545";
    
    /**
     * folder contains cards that are on Control for Minsiter 
     */
    public static final String MINISTER_CONTROL_FOLDER_ID = "8542";
    /**
     * Signing Folder -> folder contains cards that are need to be signed for minister
     */
    public static final String MINISTER_SINGING_FOLDER_ID = "8568";
    
    public static final String ADVANCED_SEARCH_FOLDER_ID = "8650";

    /**
     * center frame view id..it is used to identify center frame in Supervisor/Minister Workstation  
     */
    public static final String CENTER_FRAIME_VIEW_ID1 = "8547";
    public static final String CENTER_FRAIME_VIEW_ID2 = "8584";
    public static final String CENTER_FRAIME_VIEW_ID3 = "47480";
    public static final String CENTER_FRAIME_VIEW_ID4 = "9582"; 


    
    private static Map<String,CmsFolder> cmsFoldersMap = new HashMap<String,CmsFolder>();
    
    static {
    	
    	cmsFoldersMap.put(ON_EXECUTION_FOLDER_ID, 
    			new CmsFolder(ON_EXECUTION_FOLDER_ID, "onExecutionWorkSupDataService", 
    					CARD_ON_EXECUTION_FOR_SUPERVISOR_ATTRIBUTES));
    	
    	cmsFoldersMap.put(ON_EXECUTION_MISSED_DEADLINE_FOLDER_ID, 
    			new CmsFolder(ON_EXECUTION_MISSED_DEADLINE_FOLDER_ID, "onExecutionMissedDeadlineWorkSupDataService", 
    					CARD_ON_EXECUTION_FOR_SUPERVISOR_ATTRIBUTES));
    	
    	cmsFoldersMap.put(ON_EXECUTION_DEADLINE_TOMORROW_FOLDER_ID, 
    			new CmsFolder(ON_EXECUTION_DEADLINE_TOMORROW_FOLDER_ID, "onExecutionDeadlineTomorrowWorkSupDataService", 
    					CARD_ON_EXECUTION_FOR_SUPERVISOR_ATTRIBUTES));
    	
    	cmsFoldersMap.put(ON_EXECUTION_DEADLINE_IN_3_DAYS_FOLDER_ID, 
    			new CmsFolder(ON_EXECUTION_DEADLINE_IN_3_DAYS_FOLDER_ID, "onExecutionDeadlineIn3DaysWorkSupDataService", 
    					CARD_ON_EXECUTION_FOR_SUPERVISOR_ATTRIBUTES));
    	
    	cmsFoldersMap.put(ON_EXECUTION_DEADLINE_IN_WEEK_FOLDER_ID, 
    			new CmsFolder(ON_EXECUTION_DEADLINE_IN_WEEK_FOLDER_ID, "onExecutionDeadlineInWeekWorkSupDataService", 
    					CARD_ON_EXECUTION_FOR_SUPERVISOR_ATTRIBUTES));
    	
    	
    	cmsFoldersMap.put(ON_EXECUTIONCONT_FOLDER_ID, 
    			new CmsFolder(ON_EXECUTIONCONT_FOLDER_ID, "onExecutionContWorkSupDataService", 
    					CARD_ON_EXECUTION_FOR_SUPERVISOR_ATTRIBUTES));
    	
    	cmsFoldersMap.put(ON_EXECUTIONCONT_MISSED_DEADLINE_FOLDER_ID, 
    			new CmsFolder(ON_EXECUTIONCONT_MISSED_DEADLINE_FOLDER_ID, "onExecutionContMissedDeadlineWorkSupDataService", 
    					CARD_ON_EXECUTION_FOR_SUPERVISOR_ATTRIBUTES));
    	
    	cmsFoldersMap.put(ON_EXECUTIONCONT_DEADLINE_TOMORROW_FOLDER_ID, 
    			new CmsFolder(ON_EXECUTIONCONT_DEADLINE_TOMORROW_FOLDER_ID, "onExecutionContDeadlineTomorrowWorkSupDataService", 
    					CARD_ON_EXECUTION_FOR_SUPERVISOR_ATTRIBUTES));
    	
    	cmsFoldersMap.put(ON_EXECUTIONCONT_DEADLINE_IN_3_DAYS_FOLDER_ID, 
    			new CmsFolder(ON_EXECUTIONCONT_DEADLINE_IN_3_DAYS_FOLDER_ID, "onExecutionContDeadlineIn3DaysWorkSupDataService", 
    					CARD_ON_EXECUTION_FOR_SUPERVISOR_ATTRIBUTES));
    	
    	cmsFoldersMap.put(ON_EXECUTIONCONT_DEADLINE_IN_WEEK_FOLDER_ID, 
    			new CmsFolder(ON_EXECUTIONCONT_DEADLINE_IN_WEEK_FOLDER_ID, "onExecutionContDeadlineInWeekWorkSupDataService", 
    					CARD_ON_EXECUTION_FOR_SUPERVISOR_ATTRIBUTES));
    	
    	
    	cmsFoldersMap.put(PERSONAL_EXECUTION_FOLDER_ID, 
    			new CmsFolder(PERSONAL_EXECUTION_FOLDER_ID, "personalExecutionWorkSupDataService", 
    					CARD_ON_PERSONAL_EXECUTION_FOR_SUPERVISOR_ATTRIBUTES));
    	
    	cmsFoldersMap.put(PERSONAL_EXECUTION_MISSED_DEADLINE_FOLDER_ID, 
    			new CmsFolder(PERSONAL_EXECUTION_MISSED_DEADLINE_FOLDER_ID, "personalExecutionMissedDeadlineWorkSupDataService", 
    					CARD_ON_PERSONAL_EXECUTION_FOR_SUPERVISOR_ATTRIBUTES));
    	
    	cmsFoldersMap.put(PERSONAL_EXECUTION_DEADLINE_TOMORROW_FOLDER_ID, 
    			new CmsFolder(PERSONAL_EXECUTION_DEADLINE_TOMORROW_FOLDER_ID, "personalExecutionDeadlineTomorrowWorkSupDataService", 
    					CARD_ON_PERSONAL_EXECUTION_FOR_SUPERVISOR_ATTRIBUTES));
    	
    	cmsFoldersMap.put(PERSONAL_EXECUTION_DEADLINE_IN_3_DAYS_FOLDER_ID, 
    			new CmsFolder(PERSONAL_EXECUTION_DEADLINE_IN_3_DAYS_FOLDER_ID, "personalExecutionDeadlineIn3DaysWorkSupDataService", 
    					CARD_ON_PERSONAL_EXECUTION_FOR_SUPERVISOR_ATTRIBUTES));
    	
    	cmsFoldersMap.put(PERSONAL_EXECUTION_DEADLINE_IN_WEEK_FOLDER_ID, 
    			new CmsFolder(PERSONAL_EXECUTION_DEADLINE_IN_WEEK_FOLDER_ID, "personalExecutionDeadlineInWeekWorkSupDataService", 
    					CARD_ON_PERSONAL_EXECUTION_FOR_SUPERVISOR_ATTRIBUTES));
    	
    
    	cmsFoldersMap.put(SUP_CONSIDER_FOLDER_ID, new CmsFolder(SUP_CONSIDER_FOLDER_ID, "considerWorkSupDataService", CARD_ON_CONSIDERATION_FOR_SUPERVISOR_ATTRIBUTES));	
    	cmsFoldersMap.put(SUP_CONSIDER_CONSIDER_FOLDER_ID, new CmsFolder(SUP_CONSIDER_CONSIDER_FOLDER_ID, "considerConsiderWorkSupDataService", CARD_ON_CONSIDERATION_FOR_SUPERVISOR_ATTRIBUTES));
    	cmsFoldersMap.put(SUP_CONSIDER_EXECUTE_FOLDER_ID, new CmsFolder(SUP_CONSIDER_EXECUTE_FOLDER_ID, "considerExecuteWorkSupDataService", CARD_ON_CONSIDERATION_FOR_SUPERVISOR_ATTRIBUTES));
    	cmsFoldersMap.put(SUP_CONSIDER_ACQUAINT_FOLDER_ID, new CmsFolder(SUP_CONSIDER_ACQUAINT_FOLDER_ID, "considerAcquaintWorkSupDataService", CARD_ON_CONSIDERATION_FOR_SUPERVISOR_ATTRIBUTES));
    	
    	cmsFoldersMap.put(SUP_CONSIDER_URGENTLY_FOLDER_ID, new CmsFolder(SUP_CONSIDER_URGENTLY_FOLDER_ID, "considerUrgentlyWorkSupDataService", CONSIDER_URGENTLY_CARDS_FOR_SUPERVISOR_ATTRIBUTES)); 
    	cmsFoldersMap.put(SUP_CONSIDER_URGENTLY_CONSIDER_FOLDER_ID, new CmsFolder(SUP_CONSIDER_URGENTLY_CONSIDER_FOLDER_ID, "considerUrgentlyConsiderWorkSupDataService", CARD_ON_CONSIDERATION_FOR_SUPERVISOR_ATTRIBUTES));
    	cmsFoldersMap.put(SUP_CONSIDER_URGENTLY_EXECUTE_FOLDER_ID, new CmsFolder(SUP_CONSIDER_URGENTLY_EXECUTE_FOLDER_ID, "considerUrgentlyExecuteWorkSupDataService", CARD_ON_CONSIDERATION_FOR_SUPERVISOR_ATTRIBUTES));
    	cmsFoldersMap.put(SUP_CONSIDER_URGENTLY_ACQUAINT_FOLDER_ID, new CmsFolder(SUP_CONSIDER_URGENTLY_ACQUAINT_FOLDER_ID, "considerUrgentlyAcquaintWorkSupDataService", CARD_ON_CONSIDERATION_FOR_SUPERVISOR_ATTRIBUTES));

        cmsFoldersMap.put(SUP_DELEGATION_FOLDER_ID, new CmsFolder(SUP_DELEGATION_FOLDER_ID,
                "delegationWorkSupDataService", DELEGATION_CARDS_FOR_SUPERVISOR_ATTRIBUTES));
    	cmsFoldersMap.put(SUP_DELEGATION_MY_DELEGATIONS_FOLDER_ID, new CmsFolder(SUP_DELEGATION_MY_DELEGATIONS_FOLDER_ID,
                "delegationMyDelegationsWorkSupDataService", DELEGATION_CARDS_FOR_SUPERVISOR_ATTRIBUTES));
    	cmsFoldersMap.put(SUP_DELEGATION_DELEGATED_TO_ME_FOLDER_ID, new CmsFolder(SUP_DELEGATION_DELEGATED_TO_ME_FOLDER_ID,
                "delegationDelegatedToMeWorkSupDataService", DELEGATION_CARDS_FOR_SUPERVISOR_ATTRIBUTES));
    	cmsFoldersMap.put(SUP_DELEGATION_DELEGATION_HISTORY_FOLDER_ID, new CmsFolder(SUP_DELEGATION_DELEGATION_HISTORY_FOLDER_ID,
                "delegationDelegationHistoryWorkSupDataService", DELEGATION_HISTORY_CARDS_FOR_SUPERVISOR_ATTRIBUTES));

    	cmsFoldersMap.put(SUP_SIGN_FOLDER_ID, new CmsFolder(SUP_SIGN_FOLDER_ID, "signingWorkSupDataService", SIGN_CARDS_FOR_SUPERVISOR_ATTRIBUTES));
    	cmsFoldersMap.put(SUP_SIGN_COORD_FOLDER_ID, new CmsFolder(SUP_SIGN_COORD_FOLDER_ID, "signingForCoordWorkSupDataService", SIGN_CARDS_FOR_SUPERVISOR_ATTRIBUTES));
    	cmsFoldersMap.put(SUP_SIGN_SIGN_FOLDER_ID, new CmsFolder(SUP_SIGN_SIGN_FOLDER_ID, "signingForSigningWorkSupDataService", SIGN_CARDS_FOR_SUPERVISOR_ATTRIBUTES)); 	
    	
    	cmsFoldersMap.put(SUP_SIGN_URGENTLY_FOLDER_ID, new CmsFolder(SUP_SIGN_URGENTLY_FOLDER_ID, "signingUrgentlyWorkSupDataService", SIGN_URGENTLY_CARDS_FOR_SUPERVISOR_ATTRIBUTES));
    	cmsFoldersMap.put(SUP_SIGN_URGENTLY_COORD_FOLDER_ID, new CmsFolder(SUP_SIGN_URGENTLY_COORD_FOLDER_ID, "signingUrgentlyForCoordWorkSupDataService", SIGN_URGENTLY_CARDS_FOR_SUPERVISOR_ATTRIBUTES));
    	cmsFoldersMap.put(SUP_SIGN_URGENTLY_SIGN_FOLDER_ID, new CmsFolder(SUP_SIGN_URGENTLY_SIGN_FOLDER_ID, "signingUrgentlyForSignWorkSupDataService", SIGN_URGENTLY_CARDS_FOR_SUPERVISOR_ATTRIBUTES));
    	
    	
    	cmsFoldersMap.put(SUP_EXTERNAL_CONTROL_FOLDER_ID, 
    			new CmsFolder(SUP_EXTERNAL_CONTROL_FOLDER_ID, 
    					"externalControlWorkSupDataService", EXTERNAL_CONTROL_FOR_SUPERVISOR_ATTRIBUTES));
    	
    	cmsFoldersMap.put(SUP_EXTERNAL_CONTROL_MISSED_DEADLINE_FOLDER_ID, 
    			new CmsFolder(SUP_EXTERNAL_CONTROL_MISSED_DEADLINE_FOLDER_ID, 
    					"externalControlMissedDeadlineWorkSupDataService", EXTERNAL_CONTROL_FOR_SUPERVISOR_ATTRIBUTES));
    	
    	cmsFoldersMap.put(SUP_EXTERNAL_CONTROL_DEADLINE_TOMORROW_FOLDER_ID, 
    			new CmsFolder(SUP_EXTERNAL_CONTROL_DEADLINE_TOMORROW_FOLDER_ID, 
    					"externalControlDeadlineTomorrowWorkSupDataService", EXTERNAL_CONTROL_FOR_SUPERVISOR_ATTRIBUTES));
    	
    	cmsFoldersMap.put(SUP_EXTERNAL_CONTROL_DEADLINE_IN_3_DAYS_FOLDER_ID, 
    			new CmsFolder(SUP_EXTERNAL_CONTROL_DEADLINE_IN_3_DAYS_FOLDER_ID, 
    					"externalControlDeadlineIn3DaysWorkSupDataService", EXTERNAL_CONTROL_FOR_SUPERVISOR_ATTRIBUTES));
    	
    	cmsFoldersMap.put(SUP_EXTERNAL_CONTROL_DEADLINE_IN_WEEK_FOLDER_ID, 
    			new CmsFolder(SUP_EXTERNAL_CONTROL_DEADLINE_IN_WEEK_FOLDER_ID, 
    					"externalControlDeadlineInWeekWorkSupDataService", EXTERNAL_CONTROL_FOR_SUPERVISOR_ATTRIBUTES));
    	
    	
    	cmsFoldersMap.put(SUP_APPROVE_FOLDER_ID, new CmsFolder(SUP_APPROVE_FOLDER_ID, "approveWorkSupDataService", "approveCardsForSupervisorAttributes"));
    	cmsFoldersMap.put(SUP_PERS_CONTROL_FOLDER_ID, new CmsFolder(SUP_PERS_CONTROL_FOLDER_ID, "personalControlWorkSupDataService", "persControlCardsForSupervisorAttributes"));

        cmsFoldersMap.put(SUP_MY_DOCUMENTS_ALL_FOLDER_ID, new CmsFolder(SUP_MY_DOCUMENTS_ALL_FOLDER_ID, "myDocumentsAllWorkSupDataService", MY_DOCUMENTS_CARDS_FOR_SUPERVISOR_ATTRIBUTES));
        cmsFoldersMap.put(SUP_MY_DOCUMENTS_AGE_1_MONTH_FOLDER_ID, new CmsFolder(SUP_MY_DOCUMENTS_AGE_1_MONTH_FOLDER_ID, "myDocumentsAge1MonthWorkSupDataService", MY_DOCUMENTS_CARDS_FOR_SUPERVISOR_ATTRIBUTES));
        cmsFoldersMap.put(SUP_MY_DOCUMENTS_AGE_3_MONTHS_FOLDER_ID, new CmsFolder(SUP_MY_DOCUMENTS_AGE_3_MONTHS_FOLDER_ID, "myDocumentsAge3MonthsWorkSupDataService", MY_DOCUMENTS_CARDS_FOR_SUPERVISOR_ATTRIBUTES));
        
    	cmsFoldersMap.put(SUP_MY_DOCUMENTS_FOR_REWORK_FOLDER_ID, new CmsFolder(SUP_MY_DOCUMENTS_FOR_REWORK_FOLDER_ID, "myDocumentsForReworkWorkSupDataService", MY_DOCUMENTS_CARDS_FOR_SUPERVISOR_ATTRIBUTES));
        cmsFoldersMap.put(SUP_FAVORITES_FOLDER_ID, new CmsFolder(SUP_FAVORITES_FOLDER_ID, "favoritesWorkSupDataService", "favoriteCardsForSupervisorAttributes"));

    	cmsFoldersMap.put(MINISTER_CONTROL_FOLDER_ID, new CmsFolder(MINISTER_CONTROL_FOLDER_ID, "controlWorkMinisterDataService", "controlCardsForMinisterAttributes"));
    	cmsFoldersMap.put(MINISTER_CONSIDER_FOLDER_ID, new CmsFolder(MINISTER_CONSIDER_FOLDER_ID, "considerWorkMinisterDataService", "cardOnConsiderationForMinisterAttributes"));
    	cmsFoldersMap.put(MINISTER_SINGING_FOLDER_ID, new CmsFolder(MINISTER_SINGING_FOLDER_ID, "signingWorkMinisterDataService", "signingCardsForMinisterAttributes"));
    	
    	cmsFoldersMap.put(ADVANCED_SEARCH_FOLDER_ID, new CmsFolder(ADVANCED_SEARCH_FOLDER_ID, "considerWorkSupDataService", ADVANCED_SEARCH_CARDS_FOR_SUPERVISOR_ATTRIBUTES));
    	
    	cmsFoldersMap.put(SUP_SENT_DOCS_ALL_FOLDER_ID, new CmsFolder(SUP_SENT_DOCS_ALL_FOLDER_ID, "sentDocsAllWorkSupDataService", SENT_DOCS_FOR_SUPERVISOR_ATTRIBUTES));
    	cmsFoldersMap.put(SUP_SENT_DOCS_AGE_1_MONTH_FOLDER_ID, new CmsFolder(SUP_SENT_DOCS_AGE_1_MONTH_FOLDER_ID, "sentDocsAge1MonthWorkSupDataService", SENT_DOCS_FOR_SUPERVISOR_ATTRIBUTES));
    	cmsFoldersMap.put(SUP_SENT_DOCS_AGE_3_MONTHS_FOLDER_ID, new CmsFolder(SUP_SENT_DOCS_AGE_3_MONTHS_FOLDER_ID, "sentDocsAge3MonthsWorkSupDataService", SENT_DOCS_FOR_SUPERVISOR_ATTRIBUTES));
    	cmsFoldersMap.put(SUP_SENT_DOCS_AGE_6_MONTHS_FOLDER_ID, new CmsFolder(SUP_SENT_DOCS_AGE_3_MONTHS_FOLDER_ID, "sentDocsAge6MonthsWorkSupDataService", SENT_DOCS_FOR_SUPERVISOR_ATTRIBUTES));
    }
    
    /**
     * Describes Area workstation(folder)
     *  
     * @author skashanski
     *
     */
    private static class CmsFolder {
    	
    	/**folder(area) identifier */
    	private String folderId = null;
    	/** service name that is responsible for getting cards for this area(folder)*/
    	private String serviceName = null;
    	/** spring definition bean name that contains attributes required for this area(folder)*/
    	private String attributes = null;
    	
		public CmsFolder(String folderId, String serviceName, String attributes) {
			super();
			this.folderId = folderId;
			this.serviceName = serviceName;
			this.attributes = attributes;
		}
		
		public String getFolderId() {
			return folderId;
		}
		public void setFolderId(String folderId) {
			this.folderId = folderId;
		}
		public String getServiceName() {
			return serviceName;
		}
		public void setServiceName(String serviceName) {
			this.serviceName = serviceName;
		}
		public String getAttributes() {
			return attributes;
		}
		public void setAttributes(String attributes) {
			this.attributes = attributes;
		}
    	

    	
		
    	
    }
     
    
    
    /**
     * It initializes EJB local references
     * should be called by spring after creation
     */
    public void initServices() throws ServiceException {

        commonCardDataService = ServiceLocator.getInstance().getKernelService( CommonCardDataServiceInterface.NAME, CommonCardDataServiceInterface.class);

    }


    public boolean canChange(ObjectId objectId) throws DataException,
            ServiceException {

        return contentProducer.getService().canChange(objectId);

    }

    public boolean canDo(Action<?> action) throws DataException, ServiceException {

        return contentProducer.getService().canDo(action);
    }

    public <T> T doAction(Action<T> action) throws DataException,
            ServiceException {

        return contentProducer.getService().doAction(action);
    }

    public Collection<Template> allTemplates() {
    	

        return commonCardDataService.getAllTemplateDefinitions();

    }

    public Card getCardAttributesById(ObjectId cardId,
                                      Collection<Attribute> attributes) throws DataException,
            ServiceException {
        // TODO Auto-generated method stub
        return null;
    }

    public Card getCardById(ObjectId cardId) throws DataException,
            ServiceException {

        return contentProducer.getCardById(cardId);

    }

    public <T extends DataObject> Collection<T> getChildren(ObjectId id, Class<T> type)
            throws DataException, ServiceException {

        return contentProducer.getChildren(id, type);

    }

    private List<AttributeValue> getCardAttribute(String cardAttributes) {

        CardViewData cardViewData = (CardViewData) AppContext.getApplicationContext().getBean(cardAttributes);
        return AttributeMapper.map(cardViewData);

    }
    
    private List<SortAttribute> getCardSortAttributes(String cardAttributes, String columnId, Boolean isStraightOrder) {

        CardViewData cardViewData = (CardViewData) AppContext.getApplicationContext().getBean(cardAttributes);
        return SortAttributeMapper.map(cardViewData, columnId, isStraightOrder);

    }


    public List getDocumentsByFolder(ObjectId folderId, String simpleSearchFilter, Search search, 
    		int page, int pageSize, String sortColumnId, Boolean straightOrder) throws Exception {


    	long[] permissionTypesArray = ContentUtils.getPermissionTypes(search);

        int personId = Integer.parseInt(contentProducer.getService().getPerson().getId().getId().toString());
        
        return getFolderCards(folderId, simpleSearchFilter, page, pageSize, sortColumnId, straightOrder, 
        		permissionTypesArray, personId);


    }
    
    public List<ColumnSortAttributes> getFolderSortColumns(ObjectId folderId) {
        String folderStr = folderId.getId().toString();
		if (cmsFoldersMap.containsKey(folderStr)) {
			CmsFolder cmsFoler = cmsFoldersMap.get(folderStr);
        	CardViewData cardViewData = (CardViewData) AppContext.getApplicationContext().getBean(cmsFoler.getAttributes());
        	return cardViewData.getSortColumns();
		}
        return null;
    }

	private List<Card> getFolderCards(ObjectId folderId, String simpleSearchFilter, int page, int pageSize, 
			String sortColumnId, Boolean straightOrder, long[] permissionTypesArray, int personId) throws ServiceException {
		
		String folderStr = folderId.getId().toString();
		if (cmsFoldersMap.containsKey(folderStr)) {
			CmsFolder cmsFoler = cmsFoldersMap.get(folderStr);
        	List<AttributeValue> attributeValues = getCardAttribute(cmsFoler.getAttributes());
        	List<SortAttribute> sortAttributes = getCardSortAttributes(cmsFoler.getAttributes(), sortColumnId, straightOrder);
        	AreaWorkstationDataServiceInterface areaWorkstationDataService = ServiceLocator.getInstance().getKernelService(cmsFoler.getServiceName(), 
        															AreaWorkstationDataServiceInterface.class);
        	GetCardAreaDTO getCardAreaDTO = new GetCardAreaDTO(personId, permissionTypesArray, 
        			page, pageSize, attributeValues, sortAttributes, simpleSearchFilter);
        	return areaWorkstationDataService.getCards(getCardAreaDTO);
        	

		}
		return new ArrayList<Card>();
	}
	
	private FolderDocumentsQuantities getFolderQty(ObjectId folderId, String simpleSearchFilter, long[] permissionTypesArray, int personId) throws ServiceException {
		
		
		String folderStr = folderId.getId().toString();
		if (cmsFoldersMap.containsKey(folderStr)) {
			CmsFolder cmsFolder = cmsFoldersMap.get(folderStr);
        	List<AttributeValue> attributeValues = getCardAttribute(cmsFolder.attributes);
        	
        	AreaWorkstationDataServiceInterface areaWorkstationDataService = ServiceLocator.getInstance().getKernelService(cmsFolder.serviceName, 
        										AreaWorkstationDataServiceInterface.class);
        	GetCardAreaQtyDTO getCardAreaQtyDTO = new GetCardAreaQtyDTO(personId, permissionTypesArray, simpleSearchFilter); 
        	List<Long[]> quantities = areaWorkstationDataService.getQuantity(getCardAreaQtyDTO);
        	return new FolderDocumentsQuantities(quantities);
		}
		
		return new FolderDocumentsQuantities();
	}	

    public FolderDocumentsQuantities getDocumentsQtyByFolder(ObjectId folderId, String simpleSearchFilter, Search search) throws Exception {

    	long[] permissionTypesArray = ContentUtils.getPermissionTypes(search);
        int personId = Integer.parseInt(contentProducer.getService().getPerson().getId().getId().toString());

        String folderIdStr = folderId.getId().toString();
        
        CounterCache counterCache = CounterCache.instance();
        
        if(null == simpleSearchFilter) { // Do not use cache when calculating documents qty for simple search
	        FolderDocumentsQuantities countValue = counterCache.getCount(folderIdStr, personId, permissionTypesArray);
	        if (countValue != null) {
	            return countValue;
	        }
        }
        
        FolderDocumentsQuantities count = getFolderQty(folderId, simpleSearchFilter, permissionTypesArray, personId);
        
        //put folder quantity into Counter cache
        if(null == simpleSearchFilter) { // Do not use cache when calculating documents qty for simple search
        	counterCache.setCount(folderIdStr, personId, permissionTypesArray, count);
        }
        
        return count;
    }

    private boolean isCenterFrameView(ObjectId viewId) {
        
        if (viewId == null)
            return false;

        return CENTER_FRAIME_VIEW_ID1.equals(viewId.getId().toString()) || CENTER_FRAIME_VIEW_ID2.equals(viewId.getId().toString())
        || CENTER_FRAIME_VIEW_ID3.equals(viewId.getId().toString()) || CENTER_FRAIME_VIEW_ID4.equals(viewId.getId().toString());

    }


    public Collection<Card> getLinkedCards(Card card, LinkAttribute attr, Search filter, ObjectId areaId, ObjectId viewId)
            throws DataException, ServiceException {

        if (isCenterFrameView(viewId) ) {
        	
        	if ((attr.getLabelAttrId() == null) ) {
                // in this case we can read needed attributes by LinkViewAttribute's definition
        		List<AttributeValue> attrList = getLinkViewAttributeDefinitions(attr);
        		
        		if (attrList != null) {
	               	return getCardAttributes(attr, filter, attrList);
        		}
        		//delegates to legacy code in this case
        		return contentProducer.getLinkedCards(attr, filter);
        	}	
        		
            List<Card> linkedCards = new ArrayList<Card>();
            
            //so we are creating pseudo card to avoid redundant service side calls
            Card pseudoCard = new Card();
            pseudoCard.setId(card.getId());
			StringAttribute cardLinkAttrValue = new StringAttribute();
			
			cardLinkAttrValue.setId(attr.getLabelAttrId());
			cardLinkAttrValue.setValue(attr.getStringValue());
			
			Collection attributes = new ArrayList();
			attributes.add(cardLinkAttrValue);
			
			attributes = commonCardDataService.fillAttributeDefinitions(attributes);
			pseudoCard.setAttributes(attributes);

            linkedCards.add(pseudoCard);

            return linkedCards;

        }

        // rigth frame optimization
        if ( canBeProcessedWithNewApproach(filter)){

            List<AttributeValue> neededAttrList = null;

            // in this case we know target view and can read needed attributes
            if (viewId != null && CardViewData.containsBean(viewId.getId().toString())) {
                neededAttrList = AttributeMapper.map(CardViewData.getBean(viewId.getId().toString()));
            } else {
                // in this case we can read needed attributes by LinkViewAttribute definition
                neededAttrList = getLinkViewAttributeDefinitions(attr);
            }

            if (neededAttrList != null) {
            	return getCardAttributes(attr, filter, neededAttrList);            }
        }

        return contentProducer.getLinkedCards(attr, filter);
    }


    public Collection<Card> getLinkedCards(Card card, BackLinkAttribute attr, ObjectId viewId)
            throws DataException, ServiceException {

        if (viewId != null) {
            CardViewData cardViewData = CardViewData.getBean(viewId.getId().toString());
            if (cardViewData != null) {
                List<Card> cardsByBackLink = commonCardDataService.getBackLinkedCards(card.getId(), attr);
                if (cardsByBackLink == null) return null;
                if (cardsByBackLink.size() == 0) return cardsByBackLink;
                // read cards with permission check
                return commonCardDataService.getCardsAttributes(cardsByBackLink, AttributeMapper.map(cardViewData), getUserId(), readAccess());
            }

        }
        return contentProducer.getLinkedCards(card, attr);
    }


	private Collection<Card> getCardAttributes(LinkAttribute attr,
			Search filter, List<AttributeValue> attrList) {
		
        Collection idsLinked = attr.getIdsLinked();
        
        if (null == idsLinked || idsLinked.isEmpty()) {
        	return Collections.emptyList();
        }
		
		Long currentUserId = (Long) contentProducer.getService().getPerson().getId().getId();
		SearchFilter searchFilter = SearchFilter.getSearchFilter(filter);
		return  commonCardDataService.getCardsAttributes(idsLinked, attrList, searchFilter, getUserId(), readAccess());
		
	}


	private List<AttributeValue> getLinkViewAttributeDefinitions(LinkAttribute attr) {
		
		List<AttributeValue> neededAttrList = null;
		
		String code = attr.getId().getId().toString();
		
		if (ViewAttribute.containsBean(code)) {
            ViewAttribute viewAttribute = ViewAttribute.getBean(code);
            if (viewAttribute instanceof LinkViewAttribute) {
                neededAttrList = AttributeMapper.map( (LinkViewAttribute) viewAttribute);
            } else {
                neededAttrList = null;
            }
		}
		return neededAttrList;
	}

    private static long[] readAccess() {
        return new long[]{CardAccess.READ_CARD};
    }

    private int getUserId() {
        return ((Long)contentProducer.getService().getPerson().getId().getId()).intValue();
    }

    public Collection<Card> getLinkedCards(Card card,
                                           TypedCardLinkAttribute attr, ObjectId viewId) throws DataException, ServiceException {

        return getLinkedCards(card, (CardLinkAttribute)attr, null, null, viewId);
    }

    public Collection<Card> getLinkedCards(PersonAttribute attr, Search filter, ObjectId viewId)
            throws DataException, ServiceException {
        // view template defined for current view and no filter
        if (viewId != null && filter == null) {
            CardViewData cardViewData = CardViewData.getBean(viewId.getId().toString());
            if (cardViewData != null) {
                Collection persons = attr.getValues();
                if (persons != null) {
                    List<Card> cards = new ArrayList<Card>(persons.size());
                    if (persons.size() == 0) return cards;

                    for (Object person : persons) {
                        Card card = new Card();
                        ObjectId personCardId = ((Person) person).getCardId();
                        card.setId(personCardId);
                        card.setTemplate(10); // Person hardcoded here
                        cards.add(card);
                    }
                    // read concrete attributes
                    return commonCardDataService.getCardsAttributes(cards, AttributeMapper.map(cardViewData),
                            getUserId(), readAccess());
                    // todo sorting!
                    /*
                    List persons = ContentUtils.extendSearchResult(searchResult);
                    ContentUtils.sortCardsById(cards, ids4sort);
                    */
                }
            }
        }
        return contentProducer.getLinkedCards(attr, filter);
    }

    public List listAreaDocuments(ObjectId areaId) throws Exception {

        return contentProducer.listAreaDocuments(areaId);

    }

    public Card getLinkedCardByFound(ObjectId areaId, ObjectId viewId, Card found)
            throws ServiceException, DataException {

        if (isCenterFrameView(viewId)) {
            //we suppose that we already have this linked card so we return it to avoid redundant server-side calls
            return found;
        }

        return contentProducer.getLinkedCardByFound(found);
    }

    public Card findSingleLinkedCard(Card card, LinkAttribute attr, String[] names, String[] values) {
    	if(null == attr || null == attr.getIdsLinked() || attr.getIdsLinked().isEmpty()) {
    		return null;
    	}
    	
    	List<AttributeValue> neededAttrList = null;
        String code = attr.getId().getId().toString();
        if (ViewAttribute.containsBean(code)) {
            neededAttrList = AttributeMapper.map( (LinkViewAttribute) ViewAttribute.getBean(code));
        }

	    if (neededAttrList != null) {
	        Long currentUserId = (Long) contentProducer.getService().getPerson().getId().getId();
	        SearchFilter filter = SearchFilter.getSearchFilter(names, values);    	
	    	return commonCardDataService.findSingleCard(attr.getIdsLinked(), neededAttrList, filter, getUserId(), readAccess());
	    }
    	
    	return contentProducer.findSingleLinkedCard(card, attr, names, values);
    }
    
    /**
     * Checks if given collection contains just one CardLinkAttribute that has given attribute's code 
     * @param attributes Collection of {@Attribute}
     * @param attrCode attribute's code
     * @return true if given collection contains just one matched attribute otherwise returns false 
     */
    private boolean hasSingleMatchedCardLinkAttribute(Collection<Attribute> attributes, String attrCode ) {
    	
    	int occurrenceCount = 0;
    	
		for (Attribute attribute :  attributes) {
			if (!(attribute instanceof CardLinkAttribute))
				continue;
			
			if (attrCode.equalsIgnoreCase(attribute.getNameRu()) ||
					attrCode.equalsIgnoreCase(attribute.getNameEn()) ||
					attrCode.equals(attribute.getId().getId())) {
				occurrenceCount++; 
			}
			if (occurrenceCount > 1)
				return false; 
			
		}	
		
		return true;
    	
    }
    
    private CardLinkAttribute getCardLinkAttributeByCode(Collection<Attribute> attributes, String attrCode ) {
    	
		for (Attribute attribute :  attributes ) {
			
			if (!(attribute instanceof CardLinkAttribute))
				continue;
			
			if (attrCode.equalsIgnoreCase(attribute.getNameRu()) ||
					attrCode.equalsIgnoreCase(attribute.getNameEn()) ||
					attrCode.equals(attribute.getId().getId()))
				return (CardLinkAttribute) attribute;
				
		}		
		
		return null;	
				
    }
    
    private LinkAttribute getCardLinkAttributeByCodeAndLinkedCode(Collection<Attribute> attributes, String attrCode, String linkedCode) {
    
		for (Attribute attribute :  attributes ) {
			
			if (!(attribute instanceof LinkAttribute))
				continue;
			
			if (attrCode.equalsIgnoreCase(attribute.getNameRu()) ||
					attrCode.equalsIgnoreCase(attribute.getNameEn()) ||
					attrCode.equals(attribute.getId().getId())) {
				
				LinkAttribute linkAttribute = (LinkAttribute)attribute;
				ObjectId labelAttrId = linkAttribute.getLabelAttrId();
				if(labelAttrId == null){
					continue;
				}
				Attribute linkedAttr = new StringAttribute();
				linkedAttr.setId(labelAttrId);
				Collection<Attribute> attributeDefs = commonCardDataService.fillAttributeDefinitions(Collections.singletonList(linkedAttr));
				if (attributeDefs.isEmpty())
					continue;
				
				Attribute linkedAttribute = attributeDefs.iterator().next();
				
				if (linkedCode.equalsIgnoreCase(linkedAttribute.getNameRu()) ||
						linkedCode.equalsIgnoreCase(linkedAttribute.getNameEn()) ||
						linkedCode.equals(linkedAttribute.getId().getId()))
					return (LinkAttribute) attribute;
				
			}
				
		}
		
		return null;
    	
    }

    /**
     * Checks if given card contains collection of {@link Attribute}
     * @param card {@link Card)
     * @return true if given card contains collection of {@link Attribute} otherwise returns false
     */
    private boolean containsAttributes(Card card) {
    	
		Collection attributes = card.getAttributes();
		if (attributes.isEmpty())
			return false;
		
		Object firstElement = attributes.iterator().next();
		if (firstElement instanceof Attribute)
			return true;
		
		return false;
    	
    }
    
    
    public LinkAttribute getCardLinkAttribute(String attrField, String attrCode, Card card) {
    	
   		if ((!containsAttributes(card)) || null == attrField) //hasSingleMatchedCardLinkAttribute(card.getAttributes(), attrCode))
    			return contentProducer.getCardLinkAttribute(attrCode, card);
   		else
   			return getCardLinkAttributeByCodeAndLinkedCode(card.<Attribute>getAttributes(), attrCode, attrField);
    	
    }

    public void readCardTemplateIdAndStatusId(Card card) throws DataException, ServiceException {
        long []templateStatusId = commonCardDataService.getCardTemplateIdAndStatusId(Long.valueOf(card.getId().getId().toString()));
        card.setTemplate(templateStatusId[0]);
        card.setState(new ObjectId(CardState.class, templateStatusId[1]));
    }

    public Card getCardPresentationByViewId(Card cardIdAndTemplate, CardViewData view) throws DataException, ServiceException {
        return getCardPresentationByViewId(cardIdAndTemplate, view, CardAccess.READ_CARD);
    }
    
    public Card getCardPresentationByViewId(Card cardIdAndTemplate, CardViewData view, Long cardAccess) throws DataException, ServiceException {

        Card result = commonCardDataService.getCardAttributes(
                cardIdAndTemplate, AttributeMapper.map(view), getUserId(), cardAccess == null ? null : new long[]{cardAccess} );

        // fill with pseudo BackLink attribute
        if (result == null || result.getAttributes() == null) {
            logger.error("No attributes found for card " + cardIdAndTemplate.getId().getId() + " by view " + view.getViewId());
        }

        return result;
    }


    public Card getDefaultContentBySearch(Search search, ObjectId areaId, String simpleSearchFilter, String sortColumnId, Boolean straightOrder)
            throws DataException, ServiceException {

    	long[] permissionTypesArray = ContentUtils.getPermissionTypes(search);
        int personId = getUserId();

        List<Card> folderCards = getFolderCards(areaId, simpleSearchFilter, 1, 1, sortColumnId, straightOrder, permissionTypesArray, personId);
        
       	Card firstCard = getFirstCard(folderCards);
       	
       	return firstCard;
        
    }

    public boolean doesUserHavePermissions(Card card) throws DataException, ServiceException {
        return commonCardDataService.userHasAccessToCard(card.getId(), 
              contentProducer.getService().getPerson().getId());
    }

    private Card getFirstCard(List<Card> cards) {

        if (cards.isEmpty())
            return null;

        Card firstCard = (Card) cards.iterator().next();

        return firstCard;
    }
    
    public boolean canBeProcessedWithNewApproach(Search filter) {
    	if(null == filter) {
    		return true;
    	}
    	
    	return filter.isByAttributes() 
    			&& null == filter.getColumns() 
				&& (null == filter.getSqlXmlName() || filter.getSqlXmlName().length() == 0)
				&& (null == filter.getMaterialTypes() || filter.getMaterialTypes().isEmpty()) 
				&& Search.Filter.CU_DONT_CHECK_PERMISSIONS.equals(filter.getFilter().getCurrentUserPermission())
				&& (null == filter.getFilter().getOrderedColumns() || filter.getFilter().getOrderedColumns().isEmpty());
    }


	public Search initFromXml(String xml) throws DataException,
			UnsupportedEncodingException {
		
		//do nothing  
		return new Search();
		
	}
    
    
    


}
