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
package com.aplana.dbmi.card.dialog;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.util.CollectionUtils;
import org.w3c.dom.Document;

import com.aplana.dbmi.Portal;
import com.aplana.dbmi.action.ChangeState;
import com.aplana.dbmi.action.GetDeepChildren;
import com.aplana.dbmi.action.LockObject;
import com.aplana.dbmi.action.OverwriteCardAttributes;
import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.action.UnlockObject;
import com.aplana.dbmi.action.SearchResult.Column;
import com.aplana.dbmi.card.CardPortlet;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.BackLinkAttribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.CardState;
import com.aplana.dbmi.model.DataObject;
import com.aplana.dbmi.model.DateAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.PersonAttribute;
import com.aplana.dbmi.model.StringAttribute;
import com.aplana.dbmi.model.TextAttribute;
import com.aplana.dbmi.model.WorkflowMove;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.ServiceException;
import com.aplana.dbmi.service.SystemUser;

/**
 * ����� �������� �������� ������ �������� ��� AttributeEditorDialog
 * @author ppolushkin
 *
 */
public class EditorDialogHelper {
	
	public final static ObjectId ATTR_MAINDOC_FROM_REPORT = ObjectId.predefined(CardLinkAttribute.class, "jbr.report.int.document");
	public final static ObjectId ATTR_ALL_RESOLUTIONS = ObjectId.predefined(BackLinkAttribute.class, "jbr.allResolutions");
	public final static ObjectId ATTR_REPORT = ObjectId.predefined(BackLinkAttribute.class, "jbr.reports");
	public final static ObjectId ATTR_EXECUTOR = ObjectId.predefined(PersonAttribute.class, "jbr.report.int.executor");
	public final static ObjectId ATTR_SIGNER = ObjectId.predefined(PersonAttribute.class, "jbr.resolution.FioSign");
	public final static ObjectId ATTR_TEXT = ObjectId.predefined(TextAttribute.class, "jbr.resolutionText");
	public final static ObjectId STATUS_SENT = ObjectId.predefined(CardState.class, "sent");
	public final static ObjectId STATUS_ACCEPTED = ObjectId.predefined(CardState.class, "report.accepted");
	public final static ObjectId STATUS_ASSISTANT = ObjectId.predefined(CardState.class, "boss.assistant");
	public final static ObjectId STATUS_TRASH = ObjectId.predefined(CardState.class, "trash");
	public final static ObjectId WFM_EXECUTE_REPORT_BOSS = ObjectId.predefined(WorkflowMove.class, "jbr.report.int.execute");
	public final static ObjectId WFM_IN_WORK_REPORT = ObjectId.predefined(WorkflowMove.class, "jbr.report.accept");
	public final static ObjectId WFM_ASSISTENT_TO_DONE = ObjectId.predefined(WorkflowMove.class, "jbr.report.execute.after_assistant");
	
	public final static ObjectId ATTR_FIO = ObjectId.predefined(StringAttribute.class, "jbr.person.lastnameNM");
	public final static ObjectId ATTR_ORG = ObjectId.predefined(CardLinkAttribute.class, "jbr.person.organization");
	public final static ObjectId ATTR_SHORTNAME = ObjectId.predefined(StringAttribute.class, "jbr.organization.shortName");
	public final static ObjectId ATTR_TERM = ObjectId.predefined(DateAttribute.class, "jbr.resolutionTerm");
	
	public static final String CONFIG_FILE = "dbmi/card/context/groupResExecSameCardBeans.xml";
	
	private static ApplicationContext ctx = new ClassPathXmlApplicationContext(CONFIG_FILE);
	
	public final static Integer DEPTH = new Integer(20);
	
	protected static Log logger = LogFactory.getLog(EditorDialogHelper.class);
	
	@SuppressWarnings("unchecked")
	public static Collection<ObjectId> loadDeepChildren(DataServiceBean service, Card card) throws DataException, ServiceException {
		
		final GetDeepChildren action = new GetDeepChildren();
		
		final CardLinkAttribute mainDoc = card.getCardLinkAttributeById(ATTR_MAINDOC_FROM_REPORT);
		final Collection<ObjectId> roots = new ArrayList<ObjectId>(1);
		roots.add(mainDoc.getSingleLinkedId());

		action.setDepth(DEPTH);
		action.setChildTypeId(ATTR_ALL_RESOLUTIONS);
		action.setSecondChildTypeId(ATTR_REPORT);
		action.setRoots(roots);

		return (Collection<ObjectId>) service.doAction(action);
		
	}
	
	public static void executeSameReports(DataServiceBean service, 
										  Card card,
										  List<Card> dubCards) {
		try {
			card = (Card) service.getById(card.getId());
		} catch (Exception e) {
			logger.error(e);
		}
		
		copyValues(card);
		List<Attribute> attrs = getAttrsForSave(card);
		
		boolean isLocked = false;
		for(Card dub : dubCards) {
			
			Card c = null;
			try {
				
				c = (Card) service.getById(dub.getId());
				
				if(!c.getState().equals(STATUS_ASSISTANT)
						&& !c.getState().equals(STATUS_ACCEPTED)
						&& !c.getState().equals(STATUS_SENT)) {
					continue;
				}
				
				/**
				 * ��������� ��������� ������������� ������ � ������ ��������
				 */
				
				final ChangeState changeStateAction = new ChangeState();
				changeStateAction.setCard(c);
				
				if(c.getState().equals(STATUS_ASSISTANT)) {
					DataServiceBean tempService = new DataServiceBean();
					boolean isLockedBySystem = false;
					try {
						final WorkflowMove move = (WorkflowMove) DataObject.createFromId(WFM_ASSISTENT_TO_DONE);
						changeStateAction.setWorkflowMove(move);
						tempService.setUser(new SystemUser());
						tempService.setAddress("localhost");
						tempService.doAction(new LockObject(c));
						isLockedBySystem = true;
						tempService.doAction(changeStateAction);
						fillSameCard(tempService, card, c, attrs);
					} catch (Exception e) {
						logger.error(e);
					} finally {
						if (isLockedBySystem) {
							unlockQuietly(c, tempService);
							isLockedBySystem = false;
						}
					}
					continue;
				}
				
				service.doAction(new LockObject(c));
				isLocked = true;
				
				if(c.getState().equals(STATUS_SENT)) {
					final WorkflowMove move = (WorkflowMove) DataObject.createFromId(WFM_IN_WORK_REPORT);
					changeStateAction.setWorkflowMove(move);
					service.doAction(changeStateAction);
				}
				
				final WorkflowMove move = (WorkflowMove) DataObject.createFromId(WFM_EXECUTE_REPORT_BOSS);
				changeStateAction.setWorkflowMove(move);
				service.doAction(changeStateAction);
				
				fillSameCard(service, card, c, attrs);
			} catch (Exception e) {
				if (isLocked) {
					unlockQuietly(c, service);
					isLocked = false;
				}
				logger.error(e);
				return;
			} finally {
				if (isLocked) {
					unlockQuietly(c, service);
					isLocked = false;
				}
			}
		}
	}
	
	/**
	 * ����������� �� ������� �������� ������ �������� � ������ ����� ���� �� ����������� �� ������� ��
	 * @param service - ������ ���
	 * @param fromCard - ������� �������� ������
	 * @param toCard - ������ ����� ���� �� ����������� �� ������� ���������, � ������� ���� ����������� �������� �� �������� ������
	 * @param attrs - ��������, ������� ���������� ����������� �� ������ ������ � ������
	 * @throws DataException
	 * @throws ServiceException
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static void fillSameCard(DataServiceBean service, Card fromCard, Card toCard, List<Attribute> attrs) throws DataException, ServiceException {
		if(attrs != null) {
			final OverwriteCardAttributes writer = new OverwriteCardAttributes();
			writer.setCardId(toCard.getId());
			Collection col = new ArrayList<Attribute>();
			for(Attribute attr : attrs) {
				col.add(attr);
			}
			writer.setAttributes(col);
			service.doAction(writer);
		}
	}
	
	private static boolean unlockQuietly(Card c, DataServiceBean serviceBean) {
		try {
			serviceBean.doAction(new UnlockObject(c));
			return true;
		} catch (Exception e) {
			final String msg = "(!) Failed to unlock card: "
					+ (c == null ? "card is null" : c.getId()) + "\n"
					+ e.getMessage();
			logger.error(msg);
			return false;
		}
	}
	
	public static Map<Card, ArrayList<Object>> getDataDialogCard(DataServiceBean service, Collection<ObjectId> cardIds, Card curCard) throws DataException, ServiceException {
		
		final List<Card> loaded = load(service, cardIds);
		final List<Card> resoluts = getCardsByTemplate(CardPortlet.TEMPLATE_RESOLUT, loaded, STATUS_TRASH);
		final List<Card> reports = getCardsByTemplate(CardPortlet.TEMPLATE_REPORT, loaded);
		
		Map<Card, ArrayList<Object>> result = new HashMap<Card, ArrayList<Object>>();
		final PersonAttribute curExecutor = (PersonAttribute) curCard.getAttributeById(ATTR_EXECUTOR);
		
		for(Iterator<Card> it1 = reports.iterator(); it1.hasNext();) {
			Card report = it1.next();
			final PersonAttribute executor = (PersonAttribute) report.getAttributeById(ATTR_EXECUTOR);
			if(report.getId().equals(curCard.getId()) 
					|| !(report.getState().equals(STATUS_ACCEPTED) || report.getState().equals(STATUS_SENT) || report.getState().equals(STATUS_ASSISTANT))
					|| !executor.getValues().iterator().next().equals(curExecutor.getValues().iterator().next()))
				continue;
			for(Iterator<Card> it2 = resoluts.iterator(); it2.hasNext();) {
				Card res = it2.next();
				final BackLinkAttribute link = (BackLinkAttribute) res.getAttributeById(ATTR_REPORT);
				if(link != null && link.getLabelLinkedMap().containsKey(report.getId())) {
					final TextAttribute resText = (TextAttribute) res.getAttributeById(ATTR_TEXT);
					final PersonAttribute signer = (PersonAttribute) res.getAttributeById(ATTR_SIGNER);
					ArrayList<Object> list = new ArrayList<Object>();
					list.add(report.getId() != null ? report.getId().getId() : "");
					StringBuilder sb = new StringBuilder();
					sb.append(resText != null ? resText.getStringValue() : "");
					
					final SearchResult signerSearchResult = load1(service, new ArrayList<ObjectId>(Arrays.asList(signer.getPerson().getCardId())));
					final List<Card> personCards = signerSearchResult.getCards();
					Card personCard;
					if(!CollectionUtils.isEmpty(personCards) && (personCard = personCards.get(0)) != null) {
						final StringAttribute fio = personCard.getAttributeById(ATTR_FIO);
						sb.append((resText != null && fio != null) ? ", " : "");
						sb.append(fio != null ? fio.getValue() : "");
						final Map<String, ArrayList<Card>> labelColumns = signerSearchResult.getLabelColumnsForCards();
						final String labelStr = !CollectionUtils.isEmpty(labelColumns) ? labelColumns.keySet().iterator().next() : null;
						if(labelStr != null && fio != null) {
							final ArrayList<Card> labelCards = labelColumns.get(labelStr);
							final Card labelCard = !CollectionUtils.isEmpty(labelCards) ? labelCards.get(0) : null;
							final CardLinkAttribute orgAttr = labelCard != null ? labelCard.getCardLinkAttributeById(ATTR_ORG) : null;
							sb.append(orgAttr != null ? " (" + orgAttr.getStringValue() + ")" : "");
						}
					}
					
					DateAttribute term = (DateAttribute) res.getAttributeById(ATTR_TERM);
					sb.append(resText != null && term != null ? ", " : "");
					sb.append(term != null ? new SimpleDateFormat("dd.MM.yyyy").format(term.getValue()) : "");
					list.add(sb.toString());
					list.add(res.getId().getId());
					list.add(res.getTemplate().getId());
					
					result.put(report, list);
					break;
				}
			}
		}
		
		return result;
	}
	
	private static SearchResult load1(DataServiceBean service, Collection<ObjectId> cardIds) throws DataException, ServiceException {
		
		final Search search = new Search();
		
		search.setByCode(true);
		search.setWords(ObjectIdUtils.numericIdsToCommaDelimitedString(cardIds));
		
		List<SearchResult.Column> cols = new ArrayList<SearchResult.Column>();			
		SearchResult.Column col = new SearchResult.Column();
		col.setAttributeId(ATTR_FIO);
		cols.add(col);
		
		col = new SearchResult.Column();
		col.setAttributeId(ATTR_ORG);
		col.setLabelAttrId(ATTR_SHORTNAME);
		cols.add(col);
		
		search.setColumns(cols);
		return (SearchResult) service.doAction(search);
	}
	
	private static List<Card> getCardsByTemplate(ObjectId template, List<Card> cards, ObjectId ...ignoredStates) {
		
		List<Card> result = new ArrayList<Card>();
		for(Iterator<Card> it = cards.iterator(); it.hasNext();) {
			Card c = it.next();
			if(c.getTemplate().equals(template)
					&& Arrays.binarySearch(ignoredStates, c.getState(), new Comparator<ObjectId>() {
						@Override
						public int compare(ObjectId o1, ObjectId o2) {
							if((Long) o1.getId() < (Long) o2.getId())
								return -1;
							if((Long) o1.getId() > (Long) o2.getId())
								return 1;
							return 0;
						}
					}) < 0) {
				result.add(c);
			}
		}
		
		return result;
	}
	
	private static List<Card> load(DataServiceBean service, Collection<ObjectId> cardIds) throws DataException, ServiceException {
		
		final Search search = new Search();
		
		search.setByCode(true);
		search.setWords(ObjectIdUtils.numericIdsToCommaDelimitedString(cardIds));
		
		List<SearchResult.Column> cols = new ArrayList<SearchResult.Column>();			
		SearchResult.Column col = new SearchResult.Column();
		col.setAttributeId(Card.ATTR_TEMPLATE);
		cols.add(col);
		
		col = new SearchResult.Column();
		col.setAttributeId(Card.ATTR_STATE);
		cols.add(col);
		
		col = new SearchResult.Column();
		col.setAttributeId(Card.ATTR_ID);
		cols.add(col);
		
		col = new SearchResult.Column();
		col.setAttributeId(ATTR_REPORT);
		cols.add(col);
		
		col = new SearchResult.Column();
		col.setAttributeId(ATTR_EXECUTOR);
		cols.add(col);
		
		col = new SearchResult.Column();
		col.setAttributeId(ATTR_SIGNER);
		cols.add(col);
		
		col = new SearchResult.Column();
		col.setAttributeId(ATTR_TEXT);
		cols.add(col);
		
		col = new SearchResult.Column();
		col.setAttributeId(ATTR_TERM);
		cols.add(col);
		
		search.setColumns(cols);
		SearchResult searchResult = (SearchResult) service.doAction(search);
		return searchResult.getCards();
		
	}
	
	@SuppressWarnings("unchecked")
	private static List<Attribute> getAttrsForSave(Card card) {
		if(ctx == null) {
			logger.error("Context " + CONFIG_FILE + " is not initialized");
			return null;
		}
		Map<String, ObjectId> objectIds = ctx.getBeansOfType(ObjectId.class);
		List<Attribute> attrsList = new ArrayList<Attribute>(objectIds.size());
		Attribute attr;
		for(ObjectId id : objectIds.values()) {
			attr = card.getAttributeById(id);
			attrsList.add(attr);
		}
		return attrsList;
	}
	
	@SuppressWarnings("unchecked")
	private static void copyValues(Card card) {
		if(ctx == null) {
			logger.error("Context " + CONFIG_FILE + " is not initialized");
			return;
		}
		Map<String, StringAttributeCopyValue> objectIds = ctx.getBeansOfType(StringAttributeCopyValue.class);
		for(StringAttributeCopyValue attrCopyValue : objectIds.values()) {
			attrCopyValue.setCard(card);
			attrCopyValue.copy();
		}
	}
	
	public static class StringAttributeCopyValue {
		
		private Card card;
		
		private ObjectId target;
		
		private ObjectId source;
		
		private String xsltLocation;
		
		private boolean isNeedParse = false;
		
		public StringAttributeCopyValue(ObjectId target, ObjectId source) {
			this.target = target;
			this.source = source;
		}
		
		public StringAttributeCopyValue(ObjectId target, ObjectId source, String xsltLocation) {
			isNeedParse = true;
			this.target = target;
			this.source = source;
			this.xsltLocation = xsltLocation;
		}
		
		public Card getCard() {
			return card;
		}

		public void setCard(Card card) {
			this.card = card;
		}

		public void copy() {
			if(card == null) {
				return;
			}
				StringAttribute attrTarget = card.getAttributeById(target);
				StringAttribute attrSource = card.getAttributeById(source);
				if(attrTarget == null || attrSource == null) {
					return;
				}
				if (!isNeedParse)
					attrTarget.setValue(attrSource.getValue());
				else {
					String resultTransform = parse(attrSource.getValue());
					attrTarget.setValue(resultTransform);
				}
		}
		
		private String parse(String xmlText) {
			String resultTransform = null;
			try {
				final Document xmlDoc = prepareDocument(xmlText);
				if (xmlDoc != null) {
					final InputStream in;
					try {
						in = Portal.getFactory().getConfigService().loadConfigFile(xsltLocation);
					} catch (IOException e) {
						throw new DataException("jbr.xsltcopytextattribute.xsltFileError", new Object[] {xsltLocation, e.getMessage()}, e);
					}
					try {
						resultTransform = transform(xmlDoc, in);
					} finally {
						in.close();
					}
				}
			} catch (Exception e) {
				logger.error("Exception caught: parse xml.", e);
				/*if (e instanceof DataException)
					throw (DataException)e;
				throw new DataException("jbr.xsltcopytextattribute.error", new Object[] {e.getMessage()}, e);*/
			}
			return resultTransform;
		}
		
		private static String transform(Document xml, InputStream xsltStream) throws TransformerException {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			getTransformer(xsltStream).transform(new DOMSource(xml), new StreamResult(baos));
			try {
				return baos.toString("UTF-8");
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException(e);
			}
		}
		
		private static synchronized Transformer getTransformer(InputStream sourceStream) throws TransformerConfigurationException {
			Source xsltSource = new StreamSource(sourceStream);
		    TransformerFactory transFact = TransformerFactory.newInstance();
			return transFact.newTransformer(xsltSource);
		}
		
		protected Document prepareDocument(String text) {
			if (text == null || text.trim().length() < 1) {
				logger.warn( "Empty document XML");
				return null;
			}
			try {
				DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
				return builder.parse(new ByteArrayInputStream(text.getBytes("UTF-8")));
			} catch (Exception e) {
				logger.error("Exception while preparing document >>> \n"+text + "\n<<<", e);
				return null;
			}
		}
	}
}
