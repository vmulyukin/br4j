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
package com.aplana.dbmi.jbr.processors;

import com.aplana.dbmi.jbr.util.AttributeLocator;
import com.aplana.dbmi.model.*;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ObjectQueryBase;
import com.aplana.dbmi.service.impl.processors.CheckStringAttrLength;

import java.text.MessageFormat;
import java.util.*;

/**
 * ��������� �������� �� ����������� ����������.
 * ���������:
 *    (1) "FORMAT" ��������� ������ � ���� "{0}, {1}, {2} ...";
 *    (2) "PARAM_SRC_ATTR_IDS" ������ ��������� (����� ";"), �������� ������� ���� ��������
 *    � ���������� ������ ���������� � ��������� ������ (1), ������� �������� ����� ��������������
 *    �������������� ������� � ���������� ������� - �����, ������� ��������� ������ � ��� ������, ����
 *    ������� ����� �����-�� ��������;
 *    (3) "PARAM_DST_ATTR_ID" id-��������, � ������� ���� ������� �������������� ������.
 *
 * (!) ��� �������� ���� ��������, ����� ����������� �����������:
 * 		{���:} id {@id2}
 * 		���: ���� �������� ����, �� ��������� = string;
 * 		id: �� ���� - �������� ������ �� objectid.properties ��� ����� ��������;
 * 		id2: ����� ���� ������ ��� ���� backLink, ���� �� �������� �������� �
 * ���� ����������� id.
 * 		���������� �������� ��� �������� ����� (��. AttrUtils.ATTR_TYPE_XXX):
 * 			"string" | "text" | "number" : "date" | "list" | "tree"
 * 			"user" | "link" | "typedLink" | "backLink".
 * ��������,
			<specific property="template" value="jbr.internalPerson">
				<pre-process class="GenerateName">
					<parameter name="format" value="{0}, {1}, {2}, {3} {4} {5}"/>
					<parameter name="dstAttrId" value="NAME"/>
					<parameter name="srcAttrIds" value="link: jbr.person.organization;  link: jbr.personInternal.department;  string: jbr.person.position;  string: jbr.person.lastName;  string: jbr.person.firstName;  string:jbr.person.middleName"/>
				</pre-process>
			</specific>
 * ���������� �������� ��� �������� ����� - ��. SearchXMLHelper.ATTR_TYPE_XXX
 * (� 2010/04/18, RuSA) ���������� ����������� ������� ��� backlink'�� �������
 * ���� - ��� ����������� ����� ��������� ����� ��������� �����;
 *
 * @author RAbdullin
 *
 */
public class GenerateName extends ProcessCard {
	private static final long serialVersionUID = 1L;
	/*
	public final static ObjectId ATTR_FIRST_NAME = ObjectId.predefined(StringAttribute.class, "jbr.person.firstName");
	public final static ObjectId ATTR_MIDDLE_NAME = ObjectId.predefined(StringAttribute.class, "jbr.person.middleName");
	public final static ObjectId ATTR_LAST_NAME = ObjectId.predefined(StringAttribute.class, "jbr.person.lastName");
	 */

	final static String PROCESSORTAG = "GenerateName";

	public static final String PARAM_ATTR_FORMAT  = "format";
	public static final String PARAM_SRC_ATTR_IDS = "srcAttrIds";
	public static final String PARAM_DST_ATTR_ID  = "dstAttrId";
	public static final String PARAM_MAXARGSTRLEN = "maxArgStrLen"; // ����������� �� ����� ������ ��������� (<0 = ���, default=60)
	public static final String PARAM_FORCE_SAVE  = "forceSave";		// (default: false) ��������� ���������� ����������� �������� �����

	public static final ObjectId DefaultAttrId = Attribute.ID_NAME;
	public static final int DEFAULT_ARGSTRLEN = 60;
	public static final int MAX_PREFIX_LENGTH = 100;

	private String fmt = "{0}";
	private final List<AttributeLocator> attrIdList = new ArrayList<AttributeLocator>();
	private final Map<String, String> prefixes = new HashMap<String, String>();

	private AttributeLocator destAttrId = null;

	// DONE: ��������������� ����
	private int maxAttrValueLen = DEFAULT_ARGSTRLEN;

	/**
	 * ������� ��������� ��������. ��-��������� ����� ������ ������� NAME.
	 * @throws DataException
	 */
	public GenerateName() throws DataException {
		attrIdList.add( new AttributeLocator(DefaultAttrId) );
	}

	protected ObjectId getDestAttrId() {
		return this.destAttrId.getAttrId();
	}

	@Override
	public Object process() throws DataException {
		prepareParameters();
		Card card = getCard();
		processSrcAttributes(card, card, getBooleanParameter(PARAM_FORCE_SAVE, false));
		return null;
	}

	/**
	 * @throws DataException
	 */
	protected void prepareParameters() throws DataException {
		final StringBuilder buf = new StringBuilder("\n Processor preferences: \n");

		// ������...
		fmt = getParameter( PARAM_ATTR_FORMAT, "{0}");
		buf.append("\t format      : '").append(fmt).append("'\n");

		// ����������� ����� ������...
		try {
			maxAttrValueLen = Integer.parseInt(getParameter(PARAM_MAXARGSTRLEN, String.valueOf(DEFAULT_ARGSTRLEN)));
		} catch (Exception e) {
			maxAttrValueLen = DEFAULT_ARGSTRLEN;
		}
		buf.append("\t max attr len: ").append(maxAttrValueLen).append("\n");

		// id �������� �������� ...
		destAttrId = makeObjectId(getParameter(PARAM_DST_ATTR_ID, "NAME"));
		buf.append("\t dest attr   : ").append(destAttrId).append("\n");

		// ID ����������...
		buf.append("\t <source args> \n");
		final String[] sIds = getParameter(PARAM_SRC_ATTR_IDS, "name").trim().split("\\s*[;,]\\s*");
		if (sIds.length > 0) {
			attrIdList.clear();
			int i = 0;
			for (String s: sIds) {
				if (s == null || s.length() == 0) continue;
				String[] array = s.split("(?<=\\[[^\\]]{0," + MAX_PREFIX_LENGTH + "}\\])", 2);
				String prefix = array.length == 2 ? array[0].replaceAll("[^\\]\\[]*\\[([^\\]]*)\\]", "$1") : "";
				s = array[array.length - 1].trim();
				final AttributeLocator id = makeObjectId(s);
				this.attrIdList.add( id);
				prefixes.put(s, prefix);

				i++;
				buf.append(MessageFormat.format("\t\t [{0}] \t {1} \t->\t {2}\n", i, s, id));
			}
		}
		buf.append("\t </source args> \n");

		// if (this.attrIdList.size() < 1) attrIdList.add( DefaultAttrId);
		if (logger.isDebugEnabled()) {
			logger.debug(buf.toString());
		}
	}

	/**
	 * @param s ������ ���� "{���:} id {@id2}",
	 * {x} = �������������� ����� x.
	 * ��������, "string: jbr.organization.shortName"
	 * 			 "back: jbr.sender: fullname"
	 * ���� ��� ������, �� ������ ���������������� �������� � ����� ������ �
	 * �����-���� �����, ���� ��� ������, �� ����������� �� "string".
	 * @throws DataException
	 */
	protected static AttributeLocator makeObjectId(final String s)
		throws DataException {
		return (s == null) ? null : new AttributeLocator(s);
	}

	private String processSrcAttributes(Card card, Card dstCard, boolean forceSave) throws DataException {
		List<String> args = prepareArguments(dstCard);
		String formattedString = createFormattedString(fmt, args);
		if(formattedString.length() > CheckStringAttrLength.MAX_TEXT_LENGTH) {
			formattedString = formattedString.substring(CheckStringAttrLength.MAX_TEXT_LENGTH);
		}
		assignString(card, dstCard, formattedString, forceSave);
		return formattedString;
	}

	protected List<String> prepareArguments(Card card) throws DataException {
		final String tag = "(card=" + ((card != null) ? card.getId() : "null") + ")";
		logger.debug("preparing arguments values for "+ tag + "...");

		if (attrIdList.isEmpty()) {
			logger.debug( tag+ ": args list is empty -> exiting");
			return null;
		}

		final StringBuilder buf = new StringBuilder("\n \t [argument] \t\t\t [card attribute] \t\t\t value \n");

		// ������������ ������ ���������� ��������������...
		//
		final ArrayList<String> args = new ArrayList<String>(attrIdList.size());
		for (AttributeLocator item : attrIdList) {

			buf.append("\t").append(item.getAttrId());
			Attribute attr;
			if (Card.ATTR_TEMPLATE.equals(item.getAttrId())){
				attr = new StringAttribute();
				attr.setId((String)Card.ATTR_TEMPLATE.getId());
				if (card != null)
					((StringAttribute)attr).setValue(card.getTemplateName());
			} else {
				attr = (card == null) ? null : card.getAttributeById(item.getAttrId());
			}
			buf.append("\t\t").append(attr);

			String argValue = "";

            if (attr instanceof DateAttribute) {
                argValue = ((DateAttribute) attr).getStringValue("dd-MM-yyyy");
            } else if (attr instanceof ListAttribute) {
				ReferenceValue refVal = ((ListAttribute) attr).getValue();
				if (refVal != null) {
					//������������� ReferenceValue
					if (refVal.getValueRu() == null){
						final ObjectQueryBase cardQuery = getQueryFactory().getFetchQuery(ReferenceValue.class);
						cardQuery.setId(refVal.getId());
						refVal = getDatabase().executeQuery(getSystemUser(), cardQuery);
					}

					argValue = refVal.getValueRu();
					buf.append("\t ref value is \t").append(argValue);
				} else {
					buf.append("\t ref value is null");
				}
			} else if (attr instanceof TreeAttribute) {
				Collection<ReferenceValue> refVals = ((TreeAttribute) attr).getValues();
				if (refVals != null && !refVals.isEmpty()) {
					//������������� ReferenceValue
					for (ReferenceValue refVal : refVals) {
						if (refVal.getValueRu() == null) {
							final ObjectQueryBase cardQuery = getQueryFactory().getFetchQuery(ReferenceValue.class);
							cardQuery.setId(refVal.getId());
							ReferenceValue fetchVal = getDatabase().executeQuery(getSystemUser(), cardQuery);
							refVal.setValueRu(fetchVal.getValueRu());
							refVal.setValueEn(fetchVal.getValueEn());
						}
					}

					argValue = attr.getStringValue();
					buf.append("\t ref values are \t").append(argValue);
				} else {
					buf.append("\t ref values are null or empty");
				}
			} else {
				if (attr instanceof CardLinkAttribute) {
					final Card linked = (card == null) ? null : super.getFirstLinkedCard((CardLinkAttribute) attr, card.getId());
					if (linked != null) {
						ObjectId linkAttrId = (item.getLinkId() == null)
									? DefaultAttrId
									: item.getLinkId();
						attr = linked.getAttributeById(linkAttrId);
						buf.append("\t -link-> \t").append(attr);
					} else {
						buf.append("\t NO LINKED CARD FOUND");
					}
				} else if (attr instanceof BackLinkAttribute) {
					final List<Card> list = (card == null) ? null : super.loadAllBackLinks(card.getId(), (BackLinkAttribute) attr);
					if (list != null && !list.isEmpty()) {
						final ObjectId linkAttrId = (item.getLinkId() == null)
									? DefaultAttrId
									: item.getLinkId();
						attr = list.get(0).getAttributeById(linkAttrId);
						buf.append("\t -back-> \t").append(attr);
					} else {
						buf.append("\t NO BACK LINK CARD FOUND");
					}
				}
				if (attr != null && !attr.isEmpty())
					argValue = attr.getStringValue();
			}

			if (argValue != null
					&& maxAttrValueLen > 0
					&& argValue.length() > maxAttrValueLen)
				argValue = argValue.substring(0, maxAttrValueLen + 1);

			buf.append("\t value='").append(argValue).append("' \n");

			args.add(argValue != null ? ((!argValue.equals("") ? prefixes.get(item.getOriginalRef()) : "")) + argValue : "");
		} // for

		if (logger.isDebugEnabled()) {
			logger.debug(tag + "arguments prepared: \n" + buf.toString());
		}
		return args;
	}

	protected String createFormattedString(String format, List<String> args) {
		isArgHaveValuesForFormat(args, format);
		final String result = MessageFormat.format(format, args.toArray());
		if (logger.isDebugEnabled()) {
			logger.debug( "result:\n"+result);
		}
		return result;
	}

	protected boolean isArgHaveValuesForFormat(List<String> args, String format) {
		String[] formatNumbers = getParsFrt(format);
		boolean isNotEmptyPresent = false;
		for (int i = 0; i < formatNumbers.length; i++) {
			try {
				if (!args.get(Integer.parseInt(formatNumbers[i])).equals("")) {
					isNotEmptyPresent = true;
					break;
				}
			} catch (Exception e) {
				logger.debug("Not args with id= " + i);
			}
		}
		return isNotEmptyPresent;
	}

	protected void assignString(Card card, Card dstCard, String formattedString, boolean forceSave)
			throws DataException {
		final String tag = "(card=" + ((card != null) ? card.getId() : "null") + ")";
		logger.trace( tag + " assigning result...");

		final Attribute destAttr = smartSetAttributeValue(dstCard, this.destAttrId, formattedString);
		if (logger.isDebugEnabled()) {
			logger.debug( tag + ((destAttr != null) ? "result assigned successfully" : " no destination attribute -> skipped") );
		}

		if (forceSave) {
			logger.trace(tag + ": option forceSave = true");
			super.saveCard(dstCard, getSystemUser());
			if (logger.isDebugEnabled()) {
				logger.debug(tag + " saved successfully");
			}
		} else {
			if (logger.isDebugEnabled()) {
				logger.debug(tag + " not saved here (option  forceSave = false)");
			}
		}
	}

	protected String[] getParsFrt(String srtFrt) {
		String temStr = "";
		boolean bb = true;
		while (bb) {
			int fC,lC;
	        fC = srtFrt.indexOf("{");
	        lC = srtFrt.indexOf("}") + 1;
	        if (fC >= 0) {
	          	temStr = temStr.concat(srtFrt.substring(fC+1, lC));
	          	srtFrt = srtFrt.substring(lC, srtFrt.length());
	        }

	        if (!srtFrt.contains("}")) bb = false;
		}
		String[] arrStr = temStr.split("}");
		return arrStr;
	}

	/**
	 * �� ����������� ��������� ��������� �������� ��� ���������� ���������
	 * ��������.
	 * @param card ��������, � ������� ������ �������� ��������
	 * @param loc �������� ��������
	 * @param value ����� ��������
	 * @return ������� � ����������� ���������
	 * @throws DataException
	 */
	public Attribute smartSetAttributeValue(final Card card, AttributeLocator loc, String value)
		throws DataException {
		if (loc.getLinkId() != null) {
			throw new DataException("general.unique",
					new Object[] { PROCESSORTAG + ":: destination linked attribute is not supported" }
				);
		}

		final ObjectId attrId = loc.getAttrId();
		Attribute destAttr = card.getAttributeById(attrId);

		if (destAttr == null) {
			// ������ ��� ������ �������� -> ��� � ������� ?
			// destAttr = tryAddAttribute( card, attrId);
			// if (destAttr == null)
			{
				// throw new DataException( "jbr.processor.nodestattr_2", new Object[] { PROCESSORTAG, attrId.getId() } );
				logger.warn("Card "+ card.getId() + " does not contains attribute \'"+ attrId+ "\'");
				return null;
			}
		}

		// ���� ����� �������, ������� ��������� ��� ������ ...
		java.lang.reflect.Method setter = null;
		try { // �������� �������� setter setValue(String) ...
			setter = destAttr.getClass().getMethod("setValue", String.class);
		} catch (Exception ex) { // SecurityException, NoSuchMethodException
		}

		if (setter == null) {
			try { // �������� �������� setter setValue(Object) ...
				setter = destAttr.getClass().getMethod("setValue", Object.class);
			} catch (Exception ex) { // SecurityException, NoSuchMethodException
			}
		}

		boolean wasAssigned = false;
		if (setter != null) {
			try {
				setter.invoke( destAttr, value); // (!) ����������
				wasAssigned = true;
			} catch (Exception ex) { // IllegalArgumentException, IllegalAccessException, InvocationTargetException
				throw new DataException( "jbr.processor.desterr_3",
					new Object[] { PROCESSORTAG, value, attrId.getId() },
					ex );
			}
		}

		if (!wasAssigned) {
			// ���� ��� (��� �� ����������) ����� setValue(string), ��
			// �������� ���� ������� ������ ����� �������� ������������ ����...
			if (destAttr instanceof StringAttribute) {
				((StringAttribute) destAttr).setValue(value);
			} else if (destAttr instanceof IntegerAttribute) {
				final int ival = (value != null)
						? Integer.parseInt(value.trim())
						: 0;
				((IntegerAttribute) destAttr).setValue(ival);
			} else if (destAttr instanceof ListAttribute) {
					((ListAttribute) destAttr).setValue( makeReferenceValue(value));
			} else
				throw new DataException( "jbr.processor.destsetterfail_2",
						new Object[] { PROCESSORTAG, attrId });
		}

		return destAttr;
	}

	private ReferenceValue makeReferenceValue(String sValue) {
		if (sValue == null || sValue.trim().length() < 1)
			return null;
		ReferenceValue val = DataObject.createFromId(new ObjectId(ReferenceValue.class, Integer.parseInt(sValue.trim())));
		return val;
	}

	protected String preparedProcessSrcAttributes(Card card, Card dstCard, boolean forceSave) throws DataException {
	    prepareParameters();
	    return processSrcAttributes(card, dstCard, forceSave);
	}
}
