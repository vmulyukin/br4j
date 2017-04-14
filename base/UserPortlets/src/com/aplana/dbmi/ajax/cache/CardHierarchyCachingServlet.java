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
package com.aplana.dbmi.ajax.cache;

import java.util.Collection;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aplana.dbmi.ajax.CardHierarchyServlet;
import com.aplana.dbmi.ajax.CardHierarchyServletParameters;
import com.aplana.dbmi.ajax.CardLinkPickerHierarchyParameters;
import com.aplana.dbmi.ajax.HierarchyConnection;
import com.aplana.dbmi.card.hierarchy.Hierarchy;
import com.aplana.dbmi.card.hierarchy.HierarchyLoader;
import com.aplana.dbmi.card.hierarchy.descriptor.HierarchyDescriptor;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.ServiceException;

public class CardHierarchyCachingServlet extends CardHierarchyServlet {
	private static Log logger = LogFactory.getLog(CardHierarchyCachingServlet.class);

	/**
	 * ���� �������� ���� � ����, �������� �, ����� ������ �, ��������� � ��� � ��������� ������� ���������� ��� ��.
	 */
	protected HierarchyConnection createHierarchyConnection(
			final DataServiceBean serviceBean, final Set<ObjectId> checkedCards,
			final String filterQuery, final CardHierarchyServletParameters params)
			throws DataException, ServiceException {
		final HierarchyDescriptor hd = params.getHierarchyDescriptor();
		if (hd.getCacheReloadTime() == 0)
			return super.createHierarchyConnection(serviceBean, checkedCards, filterQuery, params);
		Hierarchy h = HierarchyCacheManager.getHierarchy(hd.getId());

		boolean cacheReset = false;
		if (params instanceof CardLinkPickerHierarchyParameters)
			cacheReset = ((CardLinkPickerHierarchyParameters)params).isCacheReset();
		if (h == null) {
			final HierarchyLoader hl = new HierarchyLoader();
			hl.setServiceBean(serviceBean);
			hl.initializeActions(params.getActionsManager());
			h = HierarchyCacheManager.putHierarchy(
				hd.getId(), 
				hd.getCacheReloadTime(),
				new HierarchyCacheManager.HierarchyCreator() {
					public Hierarchy create() {
						try {
							Hierarchy hierarchy = new Hierarchy(hd);
							hl.setHierarchy(hierarchy);
							Collection cardIds = params.getStoredCards();
							try {
								hl.load(cardIds);
							} catch (Exception e) {
								logger.error("Exception caught during loading of hierarchical card list", e);
								throw new DataException(e);
							}
							return hierarchy;
						} catch (Exception e) {
							logger.error(e.getMessage(), e);
							return null;
						}
					}
				},
				hl);
			logger.info("Hierarchy " + hd.getId() + " created");
		} else if (cacheReset){
			final HierarchyLoader hl = new HierarchyLoader();
			hl.setServiceBean(serviceBean);
			hl.initializeActions(params.getActionsManager());
			h = HierarchyCacheManager.putHierarchy(
					hd.getId(), 
					new HierarchyCacheManager.HierarchyCreator() {
						public Hierarchy create() {
							try {
								Hierarchy hierarchy = new Hierarchy(hd);
								hl.setHierarchy(hierarchy);
								Collection cardIds = params.getStoredCards();
								try {
									hl.load(cardIds);
								} catch (Exception e) {
									logger.error("Exception caught during loading of hierarchical card list", e);
									throw new DataException(e);
								}
								return hierarchy;
							} catch (Exception e) {
								logger.error(e.getMessage(), e);
								return null;
							}
						}
					},
					hl);
				logger.info("Hierarchy " + hd.getId() + " reset and get from cache");
		} else {
			logger.info("Hierarchy " + hd.getId() + " got from cache");
		}
		return new HierarchyConnection(h);
	}
}
