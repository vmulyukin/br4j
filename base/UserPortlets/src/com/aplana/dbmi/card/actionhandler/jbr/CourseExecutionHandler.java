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
package com.aplana.dbmi.card.actionhandler.jbr;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aplana.dbmi.card.actionhandler.CardPortletAttributeEditorActionHandler;
import com.aplana.dbmi.jbr.util.CardUtils;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.BackLinkAttribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.PersonAttribute;
import com.aplana.dbmi.model.StringAttribute;
import com.aplana.dbmi.model.TextAttribute;
import com.aplana.dbmi.model.util.SearchUtils;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.PortletUtil;
import com.aplana.dbmi.service.ServiceException;

public class CourseExecutionHandler extends CardPortletAttributeEditorActionHandler {
	private Log logger = LogFactory.getLog(getClass());
	private DataServiceBean serviceBean;
	
	public static final ObjectId ATTR_RESOLUTION = ObjectId.predefined(
			BackLinkAttribute.class, "jbr.resolutions");
	public static final ObjectId ATTR_SUBRESOLUTION = ObjectId.predefined(
			BackLinkAttribute.class, "jbr.linkedResolutions");
	public static final ObjectId ATTR_SIGNER_RESOLUTION = ObjectId.predefined(
	        PersonAttribute.class, "jbr.resolution.FioSign");
	public static final ObjectId ATTR_COMMENT_RESOLUTION = ObjectId.predefined(
			TextAttribute.class, "jbr.resolution.comment");
	public static final ObjectId ATTR_NAME = ObjectId.predefined(
			StringAttribute.class, "name");
	public static final ObjectId ATTR_REPORT = ObjectId.predefined(
			BackLinkAttribute.class, "jbr.reports");
	public static final ObjectId ATTR_EXECUTOR = ObjectId.predefined(
			PersonAttribute.class, "jbr.report.int.executor");
	public static final ObjectId ATTR_REPORT_TEXT = ObjectId.predefined(
			TextAttribute.class, "jbr.report.hidden.text");
	@Override
	protected void process(Attribute attr, List cardIds, ActionRequest request,
			ActionResponse response) throws DataException {
		serviceBean = PortletUtil.createService(request);
		
		Card card = getCardPortletSessionBean().getActiveCard();
		StringBuffer result = buildInfoDoc(card);
		String oldValue = ((TextAttribute) attr).getValue();
		if (oldValue == null  || oldValue.length() == 0) {
			oldValue = "";
		} else {
			oldValue = oldValue + "\n\n";
		}
		((TextAttribute) attr).setValue(oldValue+result.toString());
	}
 
	// ��������� ���������� �� ���� ���������� � ������� ������� ���. ���������
	private StringBuffer buildInfoDoc(Card doc) {
		StringBuffer result = new StringBuffer();
		try {
			final List<ObjectId> linkRes = SearchUtils.getBackLinkedCardsObjectIds(doc, ATTR_RESOLUTION, serviceBean);
			if (linkRes != null && linkRes.size() > 0) {
				for (int i = 0; i < linkRes.size() - 1; i++) {
					buildInfoResolutionAll(linkRes.get(i), "", result);
					result.append("\n\n");
				}
				buildInfoResolutionAll(linkRes.get(linkRes.size() - 1), "", result);
			}
		} catch (Exception e) {
			logger.error("Error in run CourseExecutionHandler when get data from resolutions and reports", e);
		}
		return result;
	}
	
	// ��������� ���������� �� ������� ��������� � ���� ��� ������������� � �� �������
	private void buildInfoResolutionAll(ObjectId resId, String indent, StringBuffer result) throws DataException, ServiceException {
		Card res= (Card) serviceBean.getById(resId);
		
		buildInfoResolution(res, indent, result);
		
		final List<ObjectId> linkSubres = SearchUtils.getBackLinkedCardsObjectIds(res, ATTR_SUBRESOLUTION, serviceBean);
		if (linkSubres != null) {
			Iterator iter = linkSubres.iterator();
			if (iter.hasNext()) {
				result.append("\n\n");
			}
			while (iter.hasNext()) {
				ObjectId subresId = (ObjectId) iter.next();
				buildInfoResolutionAll(subresId, indent+"", result);
				if (iter.hasNext()) {
					result.append("\n\n");
				}
			}
		}
 	}
	
	// ��������� ���������� �� ������� ��������� � ��� �������
	private void buildInfoResolution(Card res, String indent, StringBuffer result) throws DataException, ServiceException {
		String signer = "-";
		CardLinkAttribute linkSigner = (CardLinkAttribute) res.getAttributeById(ATTR_SIGNER_RESOLUTION);
		if (linkSigner != null) {
			Iterator iter = linkSigner.getIdsLinked().iterator();
			if (iter.hasNext()) {
				ObjectId personId = (ObjectId) iter.next();
				signer = getPersonInfo(personId);
			}
		}
		
		String comment = null;
		if (res.getAttributeById(ATTR_COMMENT_RESOLUTION) != null) {
			comment = ((TextAttribute) res.getAttributeById(ATTR_COMMENT_RESOLUTION)).getValue();
		}
		if (comment == null || comment.length() == 0) {
			comment = "-";
		}
		
		result.append(indent+"\"���������\" "+signer+": "+comment);
		
		final List<ObjectId> linkReports = SearchUtils.getBackLinkedCardsObjectIds(res, ATTR_REPORT, serviceBean);
		if (linkReports != null) {
			Iterator iter = linkReports.iterator();
			if (iter.hasNext()) {
				result.append("\n");
			}
			while (iter.hasNext()) {
				ObjectId reportId = (ObjectId) iter.next();
				String reportInfo = getReportInfo(reportId);
				result.append(indent+"\"�����\" "+reportInfo);
				if (iter.hasNext()) {
					result.append("\n");
				}
			}
		}
	}
	
	// ������� ���� �� ���������� �������
	private String getPersonInfo(ObjectId personId) throws DataException, ServiceException {
		Card person= (Card) serviceBean.getById(personId);
		String result = null;
		if (person.getAttributeById(ATTR_NAME) != null) {
			result = ((StringAttribute) person.getAttributeById(ATTR_NAME)).getValue();
		}
		if (result == null || result.length() == 0) {
			result = "-";
		}
		return result;
	}
	
	// ������� ���� �� �������
	private String getReportInfo(ObjectId reportId) throws DataException, ServiceException {
		Card report = (Card) serviceBean.getById(reportId);
		String authorInfo = "-";
		PersonAttribute author = ((PersonAttribute) report.getAttributeById(ATTR_EXECUTOR));
		if (author != null) {
			ObjectId authorCard = author.getPerson().getCardId();
			// ��������� ������, ����� person �� ����� ����� �������� 
			if (authorCard == null) {
				authorInfo = author.getPerson().getFullName();
			} else {
				authorInfo = getPersonInfo(author.getPerson().getCardId());
			}
		}
		if (authorInfo == null || authorInfo.length() == 0) {
			authorInfo = "-";
		}
		
		String text = null;
		if (report.getAttributeById(ATTR_REPORT_TEXT) != null) {
			text = ((TextAttribute) report.getAttributeById(ATTR_REPORT_TEXT)).getValue();
		}
		if (text == null || text.length() == 0) {
			text = "-";
		}
		
		return authorInfo+": "+text;
	}
}
