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
package org.aplana.br4j.dynamicaccess.db_export;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;
import javax.swing.SwingWorker;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aplana.br4j.dynamicaccess.db_export.objects.*;
import org.aplana.br4j.dynamicaccess.db_export.objects.AccessRuleDao.RecalculateAccessRule;
import org.aplana.br4j.dynamicaccess.xmldef.AccessConfig;
import org.aplana.br4j.dynamicaccess.xmldef.Operation;
import org.aplana.br4j.dynamicaccess.xmldef.Permission;
import org.aplana.br4j.dynamicaccess.xmldef.Template;
import org.aplana.br4j.dynamicaccess.xmldef.WfMove;
import org.aplana.br4j.dynamicaccess.xmldef.types.Action;
import org.aplana.br4j.dynamicaccess.xmldef.types.PermissionWrapper.RuleType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

/**
 * The {@link SwingWorker} for  updating access list.
 * @author atsvetkov
 *
 */
public class DoUpdateAccessList extends SwingWorker<Exception, String> {

    protected final Log logger = LogFactory.getLog(getClass());
    private static final int BATCH_SIZE = 1;
    
    private String url;
    private String username;
    private String password;
    private AccessConfig ac;
    private String ruleNote;
    
    private DataSource dataSource;

    private final static int ERROR_LIMIT = 100;
    private int errorsCount = 0;

	private boolean onlyChanges;
    
    public DoUpdateAccessList(String url, String username, String password, AccessConfig ac) {
        this.url = url;
        this.username = username;
        this.password = password;
        this.ac = ac;
    }
    
    public String getRuleNote() {
		return ruleNote;
	}

	@Override
    protected Exception doInBackground() throws Exception {
        if (ac == null) { 
        	throw new IllegalArgumentException("AccessConfig is null");
        }
		final DataSourceTransactionManager txManager = new DataSourceTransactionManager();
		txManager.setDataSource(getDataSource());
		
		final DefaultTransactionDefinition transactionDefinition = new DefaultTransactionDefinition();
		transactionDefinition.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
		final TransactionStatus transactionStatus = txManager.getTransaction(transactionDefinition);
        long start = System.currentTimeMillis();
        long updatedRows = 0;
        int index = 0;
        showProgressMonitor();        
        try {
            Connection connection = getConnection();
            List<RecalculateAccessRule> rules = getRules(getDataSource());   
            if(!this.onlyChanges){ //������ �������� ������ ���� ������ �������� ACL
				logger.info("Clearing old data in access list");
				AccessListDao.getInstance(getDataSource()).clearAccessList();
            }
            
            int iterIndex = 0;
            String iterName = (rules.size()>0)?rules.get(0).getRule_name():null;
            for(index = 0; index < rules.size(); index ++) {
				/* ��� ����������� �������� ��������� ���������, ��� ������� ������� ����������� ����� �� 6-� ��������:
				 * 1. ������������ ������� ��� �� � ��� (������������ ��� ������������ ������� �123 (1/200) - 1/2000 � �������)
				 * 2. ������������ ������� � �� � ��� ��� (������������ ��� ������������ ������� c �� �123 (1/200) - 1/2000 � �������)
				 * 3. ������������ ������� � �� � ��� (������������ ��� ������������ ������� c �� � ��� �123 (1/200) - 1/2000 � �������)
				 * 4. ���������� ������� ��� �� � ��� (������������ ��� ���������� ������� �123 (1/200) - 1/2000 � �������)
				 * 5. ���������� ������� � �� � ��� ��� (������������ ��� ���������� ������� c �� �123 (1/200) - 1/2000 � �������)
				 * 6. ���������� ������� � �� � ��� (������������ ��� ���������� ������� c �� � ��� �123 (1/200) - 1/2000 � �������)
				 */
				RecalculateAccessRule currRule = rules.get(index);
				// ��������� ������� � ����� �������� ������ �������� ������� � ������� ������� ������� � �������� 
				if (!currRule.getRule_name().equals(iterName)){
					iterIndex = 0;
					iterName = currRule.getRule_name(); 
				}
            	long toIndex = index + 1;
            	iterIndex++;
            	String oldNode = ruleNote;
	            ruleNote = MessageFormat.format("{0} �{1} ({2}/{3}) - {4}/{5} ", new Object[]{currRule.getRule_name(), currRule.getRule_id(), iterIndex, currRule.getRule_count(), toIndex, rules.size()});  

	            StringBuffer str = new StringBuffer("Updating Access List for rule: "+ruleNote); 
	            logger.info(str);
				firePropertyChange("ruleNote", oldNode, ruleNote);

				
				if (isCancelled()) {
					throw new CancelException("������� access list ����������� �������������");
				}
	            if(this.onlyChanges){ //�������� ���� ������ ��� ��������������� �������
					logger.info("Clearing old data by rule_id = " + currRule.getRule_id());
					AccessListDao.getInstance(getDataSource()).clearAccessList(currRule.getRule_id());
	            }
				updatedRows+= updateAccessToRule(connection, getDataSource(), currRule.getRule_type(), currRule.getRule_id());
				//Do not commit explicitly as JdbcTemplate use autoCommit option 
				//connection.commit();
				
				setProgress( 100 * (index + 1) / rules.size()); // for progress monitor
                
            }
            setProgress( 100 );
			txManager.commit(transactionStatus);			
            logger.info("Updating Access List has finished successfully.");
            
        } catch (Exception e) {
			txManager.rollback(transactionStatus);			

        	logger.error(e.getMessage());
            
            e.printStackTrace();
			// we are using batch update, no need to rollback current
			// transaction because the new run update access list will clear the
			// old data first.
            
            return e;
        } finally {
        	long end = System.currentTimeMillis();
        	logger.info(" Updating access list finished in : " + (end - start) + "ms, updated rows: " +  updatedRows);	
        }
        return null;
    }

	public void setOnlyChanges(boolean onlyChanges) {
		this.onlyChanges = onlyChanges;
	}

	private void showProgressMonitor() {
		setProgress(1); // for progress monitor
	}

    /**
     * Get list of card ids.
     * @param connection
     * @return {@link List} of card ids.
     */
    private List<Long> getCardIds(DataSource dataSource) {
    	CardDao cardDao = new CardDao(dataSource);
    	return cardDao.getCardIds();
	}

    /**
     * Get list of rule ids.
     * @param connection
     * @return {@link List} of rule ids.
     */
    private List<Long> getRuleIds(DataSource dataSource) {
    	AccessRuleDao cardDao = new AccessRuleDao(dataSource);
    	return cardDao.getRuleIds();
	}

    /**
     * Get list of rule ids with type, all count and description.
     * @param partial2 
     * @param connection
     * @return {@link List} of rule ids.
     */
    private List<RecalculateAccessRule> getRules(DataSource dataSource) {
    	AccessRuleDao ruleDao = new AccessRuleDao(dataSource);
    	if(this.onlyChanges){
    		List<String> permHashes = new ArrayList<String>();
			for(Template template: ac.getTemplate()){
				for(Permission permission : template.getPermission()){
					for(Operation operation: permission.getOperations().getOperations()){
						if(Action.ADD.equals(operation.getAction())){
							if(operation.getPermHash() == null){
								throw new RuntimeException("perm hash for rule " + permission.getRule() + " is null");
							}
							permHashes.add(operation.getPermHash());
						}
					}
					for(WfMove wfMove: permission.getWfMoves().getWfMove()){
						if(Action.ADD.equals((wfMove.getAction()))){
							if(wfMove.getPermHash() == null){
								throw new RuntimeException("perm hash for rule " + permission.getRule() + " is null");
							}
							permHashes.add(wfMove.getPermHash());
						}
					}
				}
			}
			if(!permHashes.isEmpty()){
				return ruleDao.getRulesByHashes(permHashes);
			} else {
				return new ArrayList<AccessRuleDao.RecalculateAccessRule>();
			}
			
    	} else {
    		return ruleDao.getRules();    		
    	}
	}
    /**
	 * Updates access list using batch update.
	 * @param dataSource {@link DataSource} to be used to create appropriate {@link JdbcTemplate}
	 * @param cardIds {@link List} of card id. They will be updated in one batch. 
	 * @return number of rows updated in database.
	 * @throws DbException is thrown if batch update fails. 
	 */
	public long updateAccessToCard(DataSource dataSource, List<Long> cardIds) throws DbException {
        try {
			return AccessListDao.getInstance(dataSource).updateAccessToCardInBatch(cardIds);
        } catch (DbException e) {
            if (++errorsCount > ERROR_LIMIT) {
                logger.error(e);
                logger.error("Update canceled!!! Errors limit " + ERROR_LIMIT +" reached");
                throw e;
            } else {
                logger.error(e);
                logger.error("Update failed " + errorsCount + "times. Continue...");
                return 0;
            }
        }
    }

    /**
	 * Updates access list using rule_id.
	 * @param dataSource {@link DataSource} to be used to create appropriate {@link JdbcTemplate}
	 * @param ruleId {@link List} of rule id. They will be updated. 
	 * @return number of rows updated in database.
	 * @throws DbException is thrown if update fails. 
	 */
	public long updateAccessToRule(Connection connection, DataSource dataSource, RuleType ruleType, Long ruleId) throws Exception {
		try {
			return AccessListDao.getInstance(dataSource).updateAccessListForRule(ruleType, ruleId);
        } catch (Exception e) {
            if (++errorsCount > ERROR_LIMIT) {
                logger.error(e);
                logger.error("Update canceled!!! Errors limit " + ERROR_LIMIT +" reached");
                throw e;
            } else {
                logger.error(e);
                logger.error("Update failed " + errorsCount + "times. Continue...");
                return 0;
            }
        }
    }
	
    private Connection getConnection() throws DbException {
    	Connection connection = ConnectionFactory.getConnection(url, username, password);
    	try{
    		connection.setAutoCommit(false);
    	}catch(SQLException ex){
    		throw new DbException(ex.getMessage());
    	}
    	return connection;
    }

    private DataSource getDataSource() throws DbException {
		if (dataSource == null) {
    		dataSource = ConnectionFactory.getDataSource(url, username, password);
    	}
    	return dataSource;
    }

}
