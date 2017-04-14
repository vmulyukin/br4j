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
package com.aplana.dbmi.module.notif;

import com.aplana.dbmi.action.Action;
import com.aplana.dbmi.action.ChangeState;
import com.aplana.dbmi.action.LockObject;
import com.aplana.dbmi.action.ObjectAction;
import com.aplana.dbmi.action.UnlockObject;
import com.aplana.dbmi.jbr.processors.ProcessCardWithConditions;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.CardState;
import com.aplana.dbmi.model.DataObject;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.model.TextAttribute;
import com.aplana.dbmi.model.WorkflowMove;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ActionQueryBase;
import com.aplana.dbmi.service.impl.ParametrizedProcessor;
import com.aplana.dbmi.service.impl.ObjectQueryBase;
import com.aplana.dbmi.service.impl.SaveQueryBase;

/**
 * Class, implementing notification of users on any event linked to card.
 * This class can be configured as a pre- or postprocessor for card storing, removing
 * or even fetching (though in this case notifications could occur too frequently),
 * and also for any card-related action ({@link com.aplana.dbmi.action.ObjectAction ObjectAction}
 * that returns card's ID in {@link com.aplana.dbmi.action.ObjectAction#getObjectId() getObjectId()}
 * method, i.e. {@link com.aplana.dbmi.action.ChangeState ChangeState},
 * {@link com.aplana.dbmi.action.DownloadFile} etc.)
 * <p>
 * When <code>CardNotification</code> is declared in <code>queries.xml</code> file,
 * it must be accompanied by an obligatory parameter: <code>beanName</code>.
 * This parameter defines the name of the {@link NotificationBean} object,
 * which could be created through <code>BeanFactory</code>. Usually that bean
 * is defined in <code>beans.xml</code> file.
 * <p>
 * <b>Example <code>queries.xml</code> (part):</b>
 * <pre>
 *	<object type="Card">
 *		<store>
 *			<post-process class="CardNotification">
 *				<parameter name="beanName" value="notifyAuthor"/>
 *			</post-process>
 *		</store>
 *	 </object>
 * </pre>
 * <p>
 * See {@link NotificationBean} for example of <code>beans.xml</code> configuration.
 * <p>
 * This class defines the following variables for message templates:
 * <ul>
 * <li><code>card</code> &mdash; currently processing {@link com.aplana.dbmi.model.Card Card} object;
 * <li><code>action</code> &mdash; currently processing {@link com.aplana.dbmi.action.Action Action} object;
 * <li><code>user</code> &mdash; {@link com.aplana.dbmi.model.Person Person} who initiated the action.
 * </ul>
 *
 * @author apirozhkov
 */
public class CardNotification extends ProcessCardWithConditions
{
	/**
	 * Name of parameter, defining the name of the notification processing bean.
	 * That be should be an instance of {@link NotificationBean} class.
	 * This parameter is obligatory.
	 */
	public static final String PARAM_BEAN = "beanName";

	public static final String PARAM_ERROR = "createErrorCard";

	public static final String PARAM_ASSISTANT_BEAN = "assistantBeanName";

	/**
	 * Name of variable, containing currently processing card.
	 */
	public static final String VAR_CARD = "card";
	/**
	 * Name of variable, containing currently processing action.
	 */
	public static final String VAR_ACTION = "action";
	/**
	 * Name of variable, containing the user who initiated the action.
	 */
	public static final String VAR_USER = "user";

	private static final ObjectId TEMPLATE_ID = ObjectId.predefined(
			Template.class, "jbr.ProcessingDistribution");

	private static final ObjectId RESULT_PROCESSING = ObjectId.predefined(
			TextAttribute.class, "resultProcessing");
	public static final ObjectId SENDING_INFO_ATTRIBUTE_ID = ObjectId.predefined(
			CardLinkAttribute.class, "jbr.distributionItem.processing");
	
	private String beanName;
	private NotificationBean notifier;
	private Boolean errorCard = false;

	/*BR4J00039252. ����������� ������� ��� ������� ������ � �����������:
	 *  	���������� AssistantPassthrough � AssistantRecipients
	 *  	�������� ChangeState ����� � ������ ���������� (��������� �������).
	 *  ���� ��� ���������� ����� �������� � ������ ������ � ����������� 
	 *  ����� ���������� � ���������� �����, �� ������ �������.  
	 */
	private static final ObjectId ASSISTANT_STATUS = ObjectId.predefined(
			CardState.class, "boss.assistant");
	private String assistantBeanName;

	/**
	 * Defines template variables and delegates notification delivery to
	 * {@link NotificationBean} object, defined through <code>beanName</code> parameter.
	 *
	 * @return Unchanged object or result of the action.
	 */
	@Override
	public Object process() throws DataException {
		if(!checkContidions(getCard())){
			logger.info("Card " + getCard().getId().getId() + "doesn't pass the conditions");
			return getResult();
		}

		
		notifier = (NotificationBean) getBeanFactory().getBean(beanName);
		/*������� BR4J00039252*/
		if (getAction() instanceof ChangeState){
			ChangeState changeState = (ChangeState)getAction();
			if(ASSISTANT_STATUS.equals(changeState.getWorkflowMove().getToState()) &&
					assistantBeanName != null){
				notifier = (NotificationBean) getBeanFactory().getBean(assistantBeanName);
			}
		}
		
		if (notifier == null) {
			logger.error(PARAM_BEAN + " parameter should be specified");
			return getResult();
		}
				
				
		SingleCardNotification notification = new SingleCardNotification();
		notification.setAction(getAction());
		notification.setCard(getCard());
		notification.setUser(getUser().getPerson());
		notifier.setObject(notification);
		int sent = notifier.sendNotifications();
		logger.info("Notification " + beanName + ": " + sent + " message(s) sent");
		if (errorCard && notifier.getError() != null && notifier.getError().length()>0) {
			createAndLinkStatusCard(getCard());
			ObjectId moveId = ObjectId.predefined(WorkflowMove.class, "notification.notsent");
			doChangeState(getCard(), (WorkflowMove) DataObject.createFromId(moveId));
		}
		return getResult();
	}

	/**
	 * Sets up the value of object's parameter.
	 * The only parameter it can receive is <code>beanName</code>.
	 *
	 * @param name Name of the parameter
	 * @param value Value of the parameter
	 * @throws IllegalArgumentException if parameter name differs from "<code>beanName</code>".
	 */
	@Override
	public void setParameter(String name, String value) {
		if (PARAM_BEAN.equalsIgnoreCase(name))
			beanName = value;
		else if (PARAM_ASSISTANT_BEAN.equalsIgnoreCase(name))
			assistantBeanName = value;
		else if (PARAM_ERROR.equalsIgnoreCase(name))
			errorCard = Boolean.parseBoolean(value);
		else
			super.setParameter(name, value);
	}

	private void doChangeState(Card card, WorkflowMove wfm) throws DataException {
		ChangeState move = new ChangeState();
		move.setCard(card);
		move.setWorkflowMove(wfm);
		try {
			execAction(new LockObject(card));
			execAction(move);
		} finally {
			execAction(new UnlockObject(card));
		}
	}

	private void createAndLinkStatusCard(Card card) throws DataException {
		boolean wasLocked = false;
		try {
			LockObject lockCard = new LockObject(card.getId());
			execAction(lockCard);
			wasLocked = true;
			StateDeloCard smc = new StateDeloCard();
			smc.setResultProcessing(new StringBuffer(notifier.getError()));
			Long idDeloState = smc.createCard();
			card.getCardLinkAttributeById(SENDING_INFO_ATTRIBUTE_ID).addLinkedId(idDeloState);
			SaveQueryBase query = getQueryFactory().getSaveQuery(card);
			query.setObject(card);
			getDatabase().executeQuery(getSystemUser(), query);
		} finally {
			if (wasLocked) {
				UnlockObject unlockCard = new UnlockObject(card.getId());
				execAction(unlockCard);
			}
		}
	}
}