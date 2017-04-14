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
package com.aplana.dbmi.numerator.query;

import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilderFactory;

import com.aplana.dbmi.action.*;
import org.springframework.jdbc.core.RowMapper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.aplana.dbmi.Portal;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.AttributeBlock;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.ContextProvider;
import com.aplana.dbmi.model.DataObject;
import com.aplana.dbmi.model.DateAttribute;
import com.aplana.dbmi.model.IntegerAttribute;
import com.aplana.dbmi.model.ListAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.model.PersonAttribute;
import com.aplana.dbmi.model.ReferenceValue;
import com.aplana.dbmi.model.StringAttribute;
import com.aplana.dbmi.numerator.action.SetRegistrationNumber;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ActionQueryBase;
import com.aplana.dbmi.service.impl.Database;
import com.aplana.dbmi.service.impl.ObjectQueryBase;
import com.aplana.dbmi.service.impl.SaveQueryBase;
import com.aplana.dbmi.service.impl.UserData;
import com.aplana.dbmi.service.impl.query.WriteQuery;

/* (YNikitin, 2014/03/21, BR4J00035457) � ����� ����������� ��������� ����������� �������� ���������� �������� �����������
 * �������� �� ������������� ���������� ��������� (SaveCard �������� �� OvewriteCardAttributes), 
 * �.�. ��� ������ ���������� ��������������� ����� (������� ��������), � ������� �������� ��������, �� ������� ������� ����� �� ��������, 
 * �� � ���������� ���������� ���������� � ������ ������ ������
 * �������� ���� �������� �� ������������� ���������� ��������� (SaveCard �������� �� OvewriteCardAttributes) � � ������� ��������, �� ��� ���� �� ������, 
 * ��� �������� ������ �������� �������� �� �������� � ��������������� � ����������� ������ ��� ��� ����������
 */
public class DoSetRegistrationNumber extends ActionQueryBase implements WriteQuery {
	public Person person = null;
	public Card card = null;
	public UserData sysUser = null;
	private final Set<Card> cardsToSave = new HashSet<Card>(5);
	private final Map<Card, Collection> saveCardAttributes= new HashMap<Card, Collection>();

	private Card format = null;
	private Card journal = null;
	private Card numerator = null;
	private ObjectId numAttrId = null; 
	private ObjectId numDigAttrId = null;
	private ObjectId dateAttrId = null;
	private ObjectId journalAttrId = null;
	private ObjectId registrarAttrId = null;
	private boolean preliminary = false;

	final static ObjectId numFmtBlockId = ObjectId.predefined(AttributeBlock.class, "numerator.formatelements");

	private String index = "";
	private static final String CONFIG_FILE = "dbmi/setRegistrationNumber.xml";
	public static final String NUMPART_INDEX = "index";
	static final ObjectId numCountAttrId = ObjectId.predefined(IntegerAttribute.class, "numerator.count");
	static final ObjectId numDateAttrId = ObjectId.predefined(DateAttribute.class, "numerator.resetdate"); 
	static final ObjectId numDigitsAttrId = ObjectId.predefined(IntegerAttribute.class, "numerator.digits");
	static final ObjectId DATE_REG = ObjectId.predefined(DateAttribute.class, "regdate");
	final ObjectId FORMAT = ObjectId.predefined(CardLinkAttribute.class, "numeratorformat");
	final ObjectId clinkId = ObjectId.predefined(CardLinkAttribute.class, "numerator");
	public static final ObjectId manuallyNumberAttrId = ObjectId.predefined(
			IntegerAttribute.class, "jbr.manually.number");
	

	@Override
	public Object processQuery() throws DataException {
		String newNumber = "";
		final SetRegistrationNumber action = (SetRegistrationNumber)getAction();
		card = action.getCard();	
		if (card == null){
			logger.error("Card is not set !");
			throw new DataException();
		}

		person = action.getRegistrator()!=null?action.getRegistrator():getUser().getPerson();		
		logger.info("Numeration started for card" + card.getId().toString()+ " for user " + person.getLogin());

		numAttrId = action.getNumAttrId() == null ? 
				ObjectId.predefined(StringAttribute.class, "regnumber") : 
				action.getNumAttrId();
		numDigAttrId = action.getNumDigAttrId() == null ? 
				ObjectId.predefined(IntegerAttribute.class, "regnumberdigital") : 
						action.getNumDigAttrId();
		dateAttrId = action.getDateAttrId() == null ?
				ObjectId.predefined(DateAttribute.class, "regdate") :
				action.getDateAttrId();
		journalAttrId = action.getJournalAttrId() == null ?
				ObjectId.predefined(CardLinkAttribute.class, "regjournal") :
				action.getJournalAttrId();

		registrarAttrId = action.getRegistrarAttrId() == null ?
				ObjectId.predefined(PersonAttribute.class, "jbr.outgoing.registrar") :
				action.getRegistrarAttrId();

		preliminary = action.isPreliminary();
		
		//�������� ������� �����, ��������� �������
		int manNum = 0;
		IntegerAttribute manNumAttr =  (IntegerAttribute)card.getAttributeById(manuallyNumberAttrId);
		if (manNumAttr != null && manNumAttr.getValue() != 0){
			manNum = manNumAttr.getValue();
		}

		final StringAttribute attrDocNum = (StringAttribute) card.getAttributeById(numAttrId);
		if( attrDocNum == null){
			throw new DataException("jbr.processor.nodestattr_2", new Object[] { card.getId(), numAttrId} );
		}
		final IntegerAttribute attrDocNumDig = (IntegerAttribute) card.getAttributeById(numDigAttrId);
		if( attrDocNumDig == null){
			throw new DataException("jbr.processor.nodestattr_2", new Object[] { card.getId(), numAttrId} );
		}
		if(attrDocNumDig.getValue() > 0) {//attrDocNum.getStringValue().length() > 0){
			logger.warn( "card" + card.getId().toString() + " has the number already");
			if(this.checkUniqueNumber(attrDocNumDig.getValue(), attrDocNum.getStringValue()) == false) {
				throw new DataException("numerator.notunique");
			}
			return attrDocNum.getStringValue(); 
		}
		try{journal = getLinkedCard(card, journalAttrId);}
		catch (NullPointerException e){
			throw new DataException(
					"numerator.attributeNotSet", 
					new Object[] {getAttributeNameById(journalAttrId)}
			);
		}

		try{format =  getLinkedCard(journal, FORMAT);}
		catch (NullPointerException e){
			throw new DataException(
					"numerator.cardlink.attributeNotSet", 
					new Object[] {
							getAttributeNameById(FORMAT), 
							getAttributeNameById(journalAttrId)
					}
			);
		}

		
		final ObjectId numeratorId = getAttributeCardId(format, clinkId);

		// ��������� ��� ���������� � ����������� (2 ���) ��������� ��������, ����� �������������
		// ������ ����������� ������� ���� ��������� ������ ��� ����������� �� ��������� � LockManagement 
		// (���� ��� �������������� ��� �����������), � �� �������� "�����" ��������������.
		if(!preliminary) {
			getLock(numeratorId, 2 * 60 * 1000);
		}
		try{
			numerator = getLinkedCard(format, clinkId);
		} 
		catch (NullPointerException e){
			throw new DataException(
					"numerator.cardlink.attributeNotSet", 
					new Object[] {
							getAttributeNameById(clinkId), 
							getAttributeNameById(FORMAT)
					}
			);
		}
		
		//������� ����� ��� ������ �������
		if (manNum != 0){
			index = String.valueOf(manNum);
			try {
				newNumber = getFormattedNum();
			}catch (DataException e) {
				logger.error("Error on set reg number", e);
				throw e;
			}catch (Exception e) {
				logger.error("Error on set reg number", e);
				throw new DataException("Error on set reg number", e);
			}
			if (checkUniqueNumber(Integer.parseInt(index), newNumber) == false){
				throw new DataException("numerator.notunique");
			}

		}else{
			//������� ����� �������� �� ����������
			//get string representation of number & save it in context card
            String prevIterationNumber = null;
			do 
			{
				index = getIndex(null);
				try {
                    newNumber = getFormattedNum();
                }
				catch (DataException e) {
					logger.error("Error on set reg number", e);
					throw e;
				}
				catch (Exception e) {
					logger.error("Error on set reg number", e);
					throw new DataException("Error on set reg number", e);
				}
                if (prevIterationNumber != null && prevIterationNumber.equals(newNumber)) {
                    // ����� �������� ������������, ���������, ��� �� ��������� �������� ����� ���������
                    throw new DataException("Error on set reg number. Check numerator format.");
                }
                prevIterationNumber = newNumber;
			} while (!preliminary && !checkUniqueNumber(Integer.parseInt(index), newNumber));
		}

			if (!preliminary){
				for (Card sCard: cardsToSave) {
					Collection changeAttributes = saveCardAttributes.get(sCard);
					if (changeAttributes!=null&&!changeAttributes.isEmpty()){
						saveCard(sCard, changeAttributes, sysUser);
					} else {
						saveCard(sCard, sysUser);
					}
					removeLock(sCard.getId());
				}
				attrDocNum.setValue(newNumber);	
				attrDocNumDig.setValue(new Integer(index));

				setRegDate();
				final PersonAttribute attrRegPerson = (PersonAttribute) card.getAttributeById(registrarAttrId);
				if(attrRegPerson.getPerson() == null) {
					attrRegPerson.setPerson(person);
				}
				final Collection<Attribute> changeAttributes = new ArrayList<Attribute>();
				changeAttributes.add(attrDocNum);
				changeAttributes.add(attrDocNumDig);
				changeAttributes.add(attrRegPerson);
				changeAttributes.add(card.getAttributeById(dateAttrId));
				// ������ ������������ ���������� ������� ��������, ��������� ������ � �������� (� ����� � ���� ���������� �� ������������ �������� �������� ��������� ����� � ��������� �����������)
				saveCard(card, changeAttributes, getUser());
				//saveCard(card, getUser());		
			}

		logger.info("Numeration "+ newNumber + " generated for card" + card.getId().toString()+ " for user " + person.getLogin());
		return newNumber;
	}

	public String getIndex(IntegerAttribute customAttr) {

		final IntegerAttribute attrCount = (customAttr != null) 
					? customAttr 
					: (IntegerAttribute) numerator.getAttributeById(numCountAttrId);
		final DateAttribute attrDate = (DateAttribute) 
					numerator.getAttributeById(numDateAttrId);
		// ��������� ������ ���������� ��������� ��� ����������
		final Collection<Attribute> changeAttributes = new ArrayList<Attribute>();
		changeAttributes.add(attrCount);
		changeAttributes.add(attrDate);
		
		//append 1 to current numeric index
		int newNum = attrCount.getValue()+1;

		if(attrDate.getValue() != null){
			//reset if needed
			final Date today = new Date();
			if(today.after(attrDate.getValue()) ){
				newNum = 1;
				attrDate.setValue(null);
			}
		}

		attrCount.setValue(newNum);
		saveCardAttributes.put(numerator, changeAttributes);	// ����������� � ������� ���������� ������ ���� ���������� ���������
		//������������ �� �������������� �������� ����� ��� ������.
		String strIndex = String.valueOf(newNum);

		return strIndex;
	}	
	

	//�������� ����� ���. ������ ������ ��������� ��� �������������� (��� ��������� �����)
	@SuppressWarnings("unused")
	private String formatDigitsNum(int num){
		String strIndex = "";
		final IntegerAttribute digits = (IntegerAttribute) 
				numerator.getAttributeById( numDigitsAttrId);
		if(digits.getValue()>0){
			strIndex = "0000000000" + num;
			strIndex = strIndex.substring((strIndex.length() - digits.getValue()), strIndex.length());
		}else{
			strIndex = ""+num;
		}

		return strIndex;
	}
	
	private Card getLinkedCard(Card acard, ObjectId id) throws DataException, NullPointerException{	
		final ObjectId cardId = getAttributeCardId(acard, id);
		return getCard(cardId);
	}

	protected ObjectId getAttributeCardId(Card acard, ObjectId id) {
		if (id == null) return null;

		final CardLinkAttribute view = (acard == null) 
					? null 
					: acard.getCardLinkAttributeById(id);
		if (view == null) {
			logger.error( "attribute " + id + " not found in card " + 
					((acard == null) ? null : acard.getId())
				);
			return null;
		}
		return (view.getLinkedCount() > 0) ? view.getSingleLinkedId() : null;
	}

	public Card getCard(ObjectId id) throws DataException, NullPointerException {
		if(id == null) {
			throw new NullPointerException();
		}
		ObjectQueryBase query = getQueryFactory().getFetchQuery(Card.class);
		query.setAccessChecker(null);
		query.setId(id);
		return (Card) getDatabase().executeQuery(getUser(), query);
	}
	
	protected void saveCard(Card acard, UserData user) throws DataException{				
		SaveQueryBase query = getQueryFactory().getSaveQuery(acard);
		query.setAccessChecker(null);
		query.setObject(acard);
		getDatabase().executeQuery(user, query);
	}

	/**
	 * ������ ������������ ���������� �������� �������� ���������� ������ ���������
	 * @param acard
	 * @param attributes
	 * @param user
	 * @throws DataException
	 */
	protected void saveCard(Card acard, Collection attributes, UserData user) throws DataException{				
		final OverwriteCardAttributes writer = new OverwriteCardAttributes();
		writer.setCardId(acard.getId());
		writer.setAttributes(attributes);
		execAction(writer);
	}

	protected void getLock(ObjectId cardId) throws DataException{		
		LockObject lo = new LockObject(cardId);
		execAction(lo);
	}

	protected void getLock(ObjectId cardId, long timeout) throws DataException{		
		LockObject lo = new LockObject(cardId);
		lo.setWaitTimeout(timeout);
		execAction(lo);
	}
	
	protected void removeLock(ObjectId cardId) throws DataException {
		UnlockObject ulo = new UnlockObject(cardId);
		execAction(ulo);
	}

	protected <T> T execAction(Action action) throws DataException{
		ActionQueryBase queryA = getQueryFactory().getActionQuery(action.getClass());
		queryA.setAction(action);
		
		queryA.setAccessChecker(null);
		queryA.setUser(getUser());
		return getDatabase().executeQuery(getSystemUser(), queryA);
	}

	public UserData getSystemUser() throws DataException {
		if (sysUser == null) {
			sysUser = new UserData();
			sysUser.setPerson(getDatabase().resolveUser(Database.SYSTEM_USER));
			sysUser.setAddress("internal");
		}
		return sysUser;
	}

	/**
	 * �������� ����� �������� ��������� - �������� ������ ���������� ��� ����� � ����������� �� ����������
	 * @param num2check �������� ����� ��� ������
	 * @param num2checkStr ���. ����� ���������
	 * @return
	 */
	private boolean checkUniqueNumber(int num2check, String num2checkStr){
		// �� ��������� (���� ������ ��� ����� ���������� ���� �� ������� � ����������) ����������� ������ ����������
		try{
			InputStream input = Portal.getFactory().getConfigService().loadConfigFile(CONFIG_FILE);
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(input);
			Element root = doc.getDocumentElement();
			NodeList confList = root.getElementsByTagName("checkUniqueNumber");
			String checkUniqueNumberMethodName = null;
			if (confList.getLength() > 0) {
				Element configElement = (Element)confList.item(0);
				checkUniqueNumberMethodName = configElement.getTextContent(); 
			}else{
				checkUniqueNumberMethodName = "template";
			}
			if ("journal".equalsIgnoreCase(checkUniqueNumberMethodName))
				return checkUniqueNumberForJournal(num2check);
		} catch (Exception ex){
			logger.error("error by load config-file "+CONFIG_FILE+": "+ex.getMessage());
		}
		return checkUniqueNumberForTemplate(num2checkStr);
	}
	/**
	 * �������� �� ������������ ��������� � ������ ����������������� � ���� �� ���� �������� ������ �� ������� (������ ����������)
	 * @param num2check - ����������� �����
	 * @return true/false � ����������� �� ������������ ������
	 */
	private boolean checkUniqueNumberForTemplate(String num2check){
		//������� ������������� �������� �� ����������� ���. ������ � �����������  � ������� ��������
		final String STATES_UNCHEKCED_LIST  = "303990"; // 303990=�������		
		Calendar cal = Calendar.getInstance();
		int curYear = cal.get(Calendar.YEAR);

		final String sql = MessageFormat.format(
			"select c.card_id\n" +
			"from  card c\n" + 
			"\t  join attribute_value av1 on (av1.card_id=c.card_id and av1.attribute_code=''"+numAttrId.getId()+"'')\n" +
			"\t  join attribute_value av_dreg on (av_dreg.card_id = c.card_id and av_dreg.attribute_code = ''"+DATE_REG.getId()+"'')\n"+
			"where\n" + 
			// -- (:UncheckedStates) -- ������������� ���������
			"\t\t c.status_id not in ({0})\n" + 
			// -- (:CheckedTemplates) -- ����������� �������
			"\t\t  and c.template_id in ({1})\n" + 
			"\t\t  and av1.string_value = ?\n" +				
			"\t\t  and c.card_id <> coalesce(?, -1)\n" +
			// -- ��������� ��������� ������ ������ �� ���� �����������
			"\t\t  and extract(year from av_dreg.date_value) = " + curYear
			, STATES_UNCHEKCED_LIST, card.getTemplate().getId().toString()
			);

		final List<?> answer = getJdbcTemplate().queryForList(
				sql,
				new Object[] {
						num2check.replaceFirst("\\[", "").replaceFirst("\\]", ""),
						(Long)card.getId().getId()
					},
				new int[] {Types.VARCHAR, Types.NUMERIC},
				Integer.class
			);

		final boolean isEmpty = (answer == null) || answer.isEmpty();
		if (logger.isDebugEnabled()) {
			logger.debug("answer is " + (isEmpty ? " empty (number is unigue) " : "NOT empty (number already used)"));
		}

		if (isEmpty) 
			return true;

		return false;	
	}

	/**
	 * �������� �� ������������ ������ � ������ �������� � ��������� �������� ����������� � ������������������ � ���� ���� (����� �������� ���������� ��� ���)
	 * @param num2check - ����������� �����
	 * @return true/false � ����������� �� ������������ ������
	 */
	private boolean checkUniqueNumberForJournal(int num2check){
		final String STATES_UNCHEKCED_LIST  = "303990"; // 1=��������, 303990=�������		
		Calendar cal = Calendar.getInstance();
		int curYear = cal.get(Calendar.YEAR);

		final String sql = MessageFormat.format(
				"with docs_with_same_regnum as (\n" +
				"		  select av1.card_id\n" +
				"		  from card c \n" +
				"		  join attribute_value av1 \n" +
				"		    on c.card_id=av1.card_id \n" +
				"		   and c.status_id not in ({0})\n" +
				"		   and av1.number_value = ?\n" +
				"		   and c.card_id <> coalesce(?, -1)\n" +
				"		   and av1.attribute_code=''"+numDigAttrId.getId()+"''\n" +
				"		  join attribute_value av2 \n" +
				"		    on av2.card_id = av1.card_id\n" +
				"		   and av2.attribute_code = ''"+DATE_REG.getId()+"''\n" +
				"		   and extract(year from av2.date_value) = " + curYear + "\n" +
				"		), reg_journals as (\n" +
				"		  select card_id from attribute_value where attribute_code = ''"+FORMAT.getId()+"'' and number_value in (\n" +
				"		    select card_id from attribute_value where attribute_code = ''"+clinkId.getId()+"'' and number_value = {1}\n" +
				"		  )\n" +
				"		)\n" +
				"		select av.card_id \n" +
				"		from attribute_value av \n" +
				"		   join docs_with_same_regnum docs on av.card_id=docs.card_id \n" +
				"		    and attribute_code = ''"+journalAttrId.getId()+"''\n" +
				"		   join reg_journals jour on av.number_value = jour.card_id\n" +
				"		limit 1;"
			, STATES_UNCHEKCED_LIST, numerator.getId().getId().toString()
			);

			final List<?> answer = getJdbcTemplate().queryForList(
				sql,
				new Object[] {
						num2check,
						(Long)card.getId().getId()
					},
				new int[] {Types.NUMERIC, Types.NUMERIC},
				Integer.class
			);

		final boolean isEmpty = (answer == null) || answer.isEmpty();
		if (logger.isDebugEnabled()) {
			logger.debug("answer is " + (isEmpty ? " empty (number is unigue) " : "NOT empty (number already used)"));
		}

		if (isEmpty) 
			return true;

		return false;	
	}

	/**
	 * Loops through attributes in formatelements block and concats their values
	 * @return {@link String} the complete number
	 * @throws ClassNotFoundException 
	 * @throws NoSuchMethodException 
	 * @throws SecurityException 
	 * @throws InvocationTargetException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws IllegalArgumentException 
	 */
	private String getFormattedNum() throws DataException, SecurityException, NoSuchMethodException, ClassNotFoundException, IllegalArgumentException, InstantiationException, IllegalAccessException {
		final StringBuilder result = new StringBuilder();
		final ObjectId blockId = numFmtBlockId;
		cardsToSave.clear();

		// first get elements block
		DataObject block = null;
		for( Iterator<?> itr = format.getAttributes().iterator(); itr.hasNext(); )
		{
			block = (DataObject) itr.next();
			if (blockId.equals(block.getId()))
				break;
			block = null;
		}

		if(block == null)
			throw new DataException("numerator.NumerationFailed.NoElementsBlock", new Object[] {"null","null"});

		for( Iterator<?> itr = ((AttributeBlock) block).getAttributes().iterator();
				itr.hasNext(); )
		{
			final DataObject attribute = (DataObject) itr.next();
			if (((Attribute)attribute).getType().equals(Attribute.TYPE_STRING)){
				//just add value of the string attribute
				result.append( ((Attribute)attribute).getStringValue());
			}else if (((Attribute)attribute).getType().equals(Attribute.TYPE_LIST)){
				final ReferenceValue v = ((ListAttribute)attribute).getValue();
				// predefined alias for index part
				if(v != null && v.getValueEn() != null){
					if (v.getValueEn().equalsIgnoreCase(NUMPART_INDEX)){
						if (!preliminary){
							result.append( index);
							cardsToSave.add(numerator);
						}else
							result.append("["+index+"]");
					} else if(v.getValueEn().length() > 0){
						final String className = NumeratorPart.class.getName()+ v.getValueEn();
						try{
							final Constructor<?> ctor = 
								Class.forName(className).getConstructor( new Class[] { DoSetRegistrationNumber.class});
							final NumeratorPart part = (NumeratorPart) 
									ctor.newInstance( new Object[] {this} );
							part.setPreliminary(preliminary);
							result.append( part.getValue());
						} catch(InvocationTargetException e){
							logger.error("InvocationTargetException " + className, e);
							throw new DataException(e);
						}
					}
				}
			}
		}
		return result.toString();
	}
	
	private void setRegDate() {
		//set registration date if is null yet
		final DateAttribute attrDate = (DateAttribute) card.getAttributeById(dateAttrId);
		if(attrDate.getValue() == null)
			attrDate.setValue(new Date());
	}
	
	protected Card getContextCard(){
		return this.card;
	}

	protected void addCardToSave(Card card2save){
		this.cardsToSave.add(card2save);
	}

	public String getAttributeNameById (ObjectId AttrId) throws DataException
	{
		try{
		final Constructor<?> constructor = 
			Class.forName(AttrId.getType().getName()).getConstructor( new Class[] {});
		final Attribute a = (Attribute) 
				constructor.newInstance( new Object[] {} );
		String sql = "select attr_name_rus, attr_name_eng" +
		" from attribute where attribute_code = " +
		"'" + AttrId.getId().toString() + "'" +
		" and data_type = " +
		"'" + a.getType() + "'" +
		" LIMIT 1";
		ArrayList names = (ArrayList) getJdbcTemplate().query
		(
			sql, 
			new RowMapper()
			{
				public Object mapRow(ResultSet rs, int rowNum) throws SQLException
				{
					ArrayList names = new ArrayList();
					names.add(rs.getString(1));
					names.add(rs.getString(2));
					return names;
				}
			}
		);
		return ContextProvider.getContext().
			getLocaleString( (String)((ArrayList) names.get(0)).get(0), (String)((ArrayList) names.get(0)).get(1));
		}catch(Exception e){throw new DataException(e);}
	}
}
