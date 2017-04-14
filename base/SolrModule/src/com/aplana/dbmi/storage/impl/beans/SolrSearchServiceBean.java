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
package com.aplana.dbmi.storage.impl.beans;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.impl.BinaryRequestWriter;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.client.solrj.request.AbstractUpdateRequest;
import org.apache.solr.client.solrj.request.ContentStreamUpdateRequest;
import org.apache.solr.client.solrj.request.UpdateRequest;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.util.ContentStream;
import org.springframework.beans.factory.InitializingBean;

import com.aplana.dbmi.storage.impl.FileStorageSettings;
import com.aplana.dbmi.storage.search.FileItem;
import com.aplana.dbmi.storage.search.SearchException;
import com.aplana.dbmi.storage.search.SearchService;

/**
 * TODO Javadoc
 *
 * @author Ogalkin, RAbdullin.
 *
 */
public class SolrSearchServiceBean implements SearchService, InitializingBean 
{
	private SolrServer solrServer;
	private String solrServerUrl;
	
	private boolean waitFlush = true;
	private boolean waitSearcher =true;


	/**
	 * @return ������� ���� �������� ������ ��� commit/rollback/optimize
	 */
	public boolean isWaitFlush() {
		return this.waitFlush;
	}

	/**
	 * ������ ���� �������� ������ ��� commit/rollback/optimize
	 * @param waitFlush the waitFlush to set
	 */
	public void setWaitFlush(boolean waitFlush) {
		this.waitFlush = waitFlush;
	}

	/**
	 * @return ������� ���� �������� ������.
	 */
	public boolean isWaitSearcher() {
		return this.waitSearcher;
	}

	/**
	 * ������ ���� �������� ���������� ���������� ��� commit/rollback/optimize
	 * @param waitSearcher
	 */
	public void setWaitSearcher(boolean waitSearcher) {
		this.waitSearcher = waitSearcher;
	}
	
	/**
	 * TODO Javadoc
	 * @param solrServerUrl the solrServerUrl to set
	 */
	public void setSolrServerUrl(String solrServerUrl) {
		this.solrServerUrl = solrServerUrl;
	}

	/* (non-Javadoc)
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	public void afterPropertiesSet() throws Exception {
		setSolrServerUrl(FileStorageSettings.getSolrServerURL());
		solrServer = startRemoteSolr();
	}
	
	/**
	 * Starts a connection to a remote Solr using HTTP as the transport mechanism.
	 * 
	 * @return a {@link CommonsHttpSolrServer} instance
	 * @throws MalformedURLException if the Solr server URL is invalid
	 */
	protected SolrServer startRemoteSolr() 
		throws MalformedURLException {

		Validate.notEmpty(solrServerUrl, "Solr server URL must not be empty");

		final CommonsHttpSolrServer solrHttpServer = new CommonsHttpSolrServer(solrServerUrl);
		solrHttpServer.setRequestWriter( new BinaryRequestWriter() );
		this.solrServer = solrHttpServer;

		return this.solrServer;
	}

	/* (non-Javadoc)
	 * @see com.aplana.dbmi.search.SearchService#index(java.lang.String, java.lang.String, org.apache.solr.common.util.ContentStream)
	 */
	public void index(String id, String filename, ContentStream stream)
		throws SearchException, IOException 
	{
		try {
			final ContentStreamUpdateRequest request 
				= new ContentStreamUpdateRequest("/update/extract");

			request.addContentStream(stream);
			request.setParam("literal.url", id);
			request.setParam("literal.filename", filename);
			request.setAction(AbstractUpdateRequest.ACTION.COMMIT, isWaitFlush(), isWaitSearcher());

			solrServer.request(request);
		} catch (Exception e) {
			throw new SearchException("solr.search.update", e);
		}
	}

	/* (non-Javadoc)
	 * @see com.aplana.dbmi.search.SearchService#query(java.lang.String, java.lang.Integer, java.lang.Integer)
	 */
	public List<FileItem> query(String query, Integer startRow, Integer rowCount)
					throws SearchException 
	{
		try {
			final SolrQuery solrQuery = new SolrQuery(query);
			solrQuery.setStart(startRow);
			solrQuery.setRows(rowCount);
			QueryResponse response = solrServer.query(solrQuery);
			return response.getBeans(FileItem.class);
		} catch (Exception e) {
			throw new SearchException("solr.search.query", e);
		}
	}

	/* (non-Javadoc)
	 * @see com.aplana.dbmi.filestorage.search.SearchService#delete(java.lang.String)
	 */
	public void delete(String id) throws SearchException {
		try {
			final ContentStreamUpdateRequest request 
				= new ContentStreamUpdateRequest("/delete");
		
			request.setParam("literal.url", id);
			request.setAction(AbstractUpdateRequest.ACTION.COMMIT, false, false); // no waiting
			solrServer.request(request);
			
		} catch (Exception e) {
			throw new SearchException( "solr.search.delete id = ''{0}''", new Object[] {id}, e);
		}
	}
	

	/* (non-Javadoc)
	 * @see com.aplana.dbmi.filestorage.search.SearchService#optimize(boolean, boolean)
	 */
	public void optimize(Boolean flWaitFlush, Boolean flWaitSearcher)
			throws SearchException {
		boolean effWaitFlush = (flWaitFlush != null) 
						? flWaitFlush.booleanValue()
						: this.isWaitFlush();

		boolean effWaitSearcher = (flWaitSearcher != null)
						? flWaitSearcher.booleanValue()
						: this.isWaitSearcher();
		try {

			final org.apache.solr.client.solrj.request.UpdateRequest request 
				= new UpdateRequest("/update");

			request.setAction(AbstractUpdateRequest.ACTION.OPTIMIZE, effWaitFlush, effWaitSearcher); // no waiting
			solrServer.request(request);

		} catch (Exception e) {
				throw new SearchException( "solr.search.optimize wait_flush={0}, wait_search={1}", 
						new Object[] { effWaitFlush, effWaitSearcher}, e);
		}

	}
}
