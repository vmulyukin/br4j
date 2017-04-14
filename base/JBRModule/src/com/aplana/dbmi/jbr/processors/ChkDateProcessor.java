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
/**
 * 
 */
package com.aplana.dbmi.jbr.processors;

import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;

import com.aplana.dbmi.jbr.util.AttributeLocator;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.DateAttribute;
import com.aplana.dbmi.model.DatedTypedCardLinkAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.util.AttrUtils;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.Validator;

/**
 * @author RAbdullin
 * (2010/07/30) ��������� ��� �������� �������� ���: 
 * 		- �����/������ ��������� ����,
 * 		- �����/������ ���� � ������ ��������.
 * ���� ����� ���������� ���� (�� ������������������ �������) ��� ������������
 * ��������� ���� ��� ��������.
 * 
 * 	��������� ������� �������� ��� queries.xml �������� � ����:
 * 		<parameter name="checkDateN" value="id_��������;�;��;����" />
 * 
 * ������ ���� ����������� ������ � �������, ������������ ������ ������.
 *  
 * ��������:
 * 		<parameter name="checkDateN" value="id_��������" />			������ �������: "���� ��������"
 * 		<parameter name="checkDateN" value="id_��������;c" />		������ ������� "�����": "����>=�"
 * 		<parameter name="checkDateN" value="id_��������;c;��" />	������ �������� "����>=� � ����<=��"
 * 		<parameter name="checkDateN" value="id_��������;;��" />		������ ������� "������": "����<=��"
 *  *	(������� ��������� ����� ������ ���������� � ����������� "�"="��")
 * 
 * 		<parameter name="checkDateN" value="JBR_END;+0;+7" />	
 * 			������ ������� "������� JBR_END ������ ���� �� ����� ���� ���� �� �������"

 * 		<parameter name="checkDateN" value="JBR_END;+0;+7;@JBR_START" />	
 * 			������ ������� "������� JBR_END ������ ���� �� ����� ���� ���� ������������ �������� JBR_START"
 * 
 *   ��� 
 *   	��� ��������� ����� ������� ����� �������������.
 *   	N - �������� ����� ����������� ����, ���� ������� ������ ���;
 * ������������ ��� ������������� ��������� � ������� �������� ����� ��������� 
 * ��� �� ���� ����� ���������� (������ ����, ����� "�����������" ��������� 
 * ������� ���������� ��� �������� ������ ����� ���� ����� ������ ������� ������
 * ���������� ���������� � ������� N: "checkDateA", "checkDate1", "checkDateXYZ" � �.�.).
 *
 *		"id_��������": (���� ������� �����������) ���� �����, ���� ��� ��������,
 * �������� �������� ���� ���������. �� ��������� - DateAttribute, �� ����� ��������� ��� ��������,
 * �������� datedTypedLink:jbr.og.addressee.list. ��� �� ����� �������� �������� ��������, ������� �����
 * ������������ � ��������� �� ������, ��������: datedTypedLink:jbr.og.addressee.list%���� � ������ ���������
 *
 *
 *   	"�","��",���� - ���� ����� � ����� �� ���� �����:
 *   		1) @id_��������	"������ �� �������� ���������� ��������", ��������, 
 * ��� �������� �������, ��� ������� date1 ����� date2: "date1;@date2".
 *   		2) +X/-X: "������������� �����" - ���� ������ ������ ���� ��� �����, �� ����������� ��� �����
 * ������ ����� ������������ �������� �������� �������, ������� �� ��������� = 
 * �������, �� ����� ���� ������ ��������� ����������.
 * (X-������������ ����� � ���� (������� ������ ����� ������ ���� � ������); 
 * ������ � ��������� ����������� ������ �������� �������� ���� �������� (�� ��������� dd/mm/yyyy),
 * ��������, dd/mm/yyyy - ������� ������ ����, �� �������� �����.
 *   		3) ��� "��������������� ����" � �������, �������� ���������� "dateFormat",
 */
@SuppressWarnings("synthetic-access")
public class ChkDateProcessor extends ProcessCard implements Validator {

	/**
	 * ��������, ������� ����� ����������� (�������� ������ �������), 
	 * ��� ������� ������� �������� ���.
	 */
	private static final String PARAM_PREFIX_CHECKDATE_i = "checkdate";

	/**
	 * ��������, �������� ������ ��� � �������� ���������. 
	 */
	// DONE: (2010/07/30, RuSA) ����� ��������������� ������ ����.
	private static final String PARAM_DATE_FORMAT = "dateFormat";

	/**
	 * bool-��������: true= ������ �������� ��������� (�.�. NULL-�������� ����� 
	 * ������������� ������ ������� �� ����), ��-��������� false.
	 */
	private static final String PARAM_DATE_NULLISOK = "dateNullIsOk";


	// ��������������� ��������
	final private List<String[]> conditions = new ArrayList<String[]>();
	private String formatOfDates = "dd/MM/yy"; // ex: "dd/MM/yy HH:mm:ss"


	// ������� ��������...
	private Card card;


	@Override
	public Object process() throws DataException {

		this.card = super.getCard();

		final DateCondition cond = new DateCondition();

		for (final String[] condParts : conditions)
		{
			cond.setCondition(condParts);
			cond.checkCondition();
		}

		logger.debug( "Card "+ this.card.getId() + " passes through all "+ conditions.size() +" date conditions");

		// ������ ���� ������ ��������� �������� (� ��������� ���������� ��� 
		// �������), ��� ��� �������� �� �������������...
		return null;
	}

	/* (non-Javadoc)
	 * @see com.aplana.dbmi.jbr.processors.ProcessCard#setParameter(java.lang.String, java.lang.String)
	 */
	@Override
	public void setParameter(String name, String value) 
	{
		if (name == null) return;

		if (ChkDateProcessor.PARAM_DATE_FORMAT.equalsIgnoreCase(name)) {
			this.formatOfDates = value;
		} else if (name.toLowerCase().startsWith(ChkDateProcessor.PARAM_PREFIX_CHECKDATE_i)) {
			// ��������� ������� �������� ��������� ����...
			final String[] parts = value.split(";");
			if (parts.length < 1)
				logger.warn( MessageFormat.format( "Skipping empty/invalid condition ''{0}''=''{1}''", 
						name, value ));
			else
				conditions.add( parts);
		}

		super.setParameter(name, value);
	}

	private class DateCondition{

		private Date from; 
		private Date upto;
		private Date srcAttrVal;

		private String srcAttrName;

		final SimpleDateFormat formatter =
			new SimpleDateFormat( formatOfDates, new Locale("ru", "RU"));

		public DateCondition() {
		}

		public void clear()
		{
			from = null;
			upto = null;
			srcAttrVal = null;
			srcAttrName = null;
		}

		private Date getDateAttributeValue(ObjectId attr)
		{
			if (attr == null) return null;
			final Attribute dat = card.getAttributeById(attr);
			if (dat == null) {
				logger.warn("Card " + card.getId() + " has no date attribute \'"+ attr+ "\'");
				return null;
			}
			if(srcAttrName == null){
				srcAttrName = dat.getName();
			}
			if(DateAttribute.class.equals(attr.getType())){
				return ((DateAttribute)dat).getValue();
			} else if (DatedTypedCardLinkAttribute.class.equals(attr.getType())){
				Date result = null;
				for(Entry<Long,Date> entry :((DatedTypedCardLinkAttribute)dat).getDates().entrySet()){
					if(result == null || (entry.getValue() != null && entry.getValue().before(result))){
						result = entry.getValue();
					}
				}
				return result;
			}
			return null;
		}

		/**
		 * ������ 1, 2, 3 ��� ������ ����� �������.
		 * @param parts: ����-�� id ������������ ��������, ������, �����, ����.
		 * @throws DataException 
		 */
		public void setCondition(final String[] parts) throws DataException{

			final StringBuffer sb = new StringBuffer();
			if (logger.isDebugEnabled()) {
				sb.append( MessageFormat.format("assigning condition parts ({0}):", (parts != null) ? parts.length : "null" ));
				int i = 0;
				if (parts != null)
				{
					for( String s : parts) {
						sb.append(MessageFormat.format( "\t[{0}] ''{1}''", i, s));
						i++;
					}
				}
				sb.append(" .\n");
			}

			try {
				this.clear();

				if (parts == null || parts.length < 1)
					return;
				
				String[] attrInfo = parts[0].split("%");
				String attrStringDef = null;
				if(attrInfo.length > 1){
					attrStringDef = attrInfo[0];
					srcAttrName = attrInfo[1];
				} else {
					attrStringDef = parts[0];
				}
				
				// ����������� ��������
				
				if(attrStringDef.indexOf(":") == -1){
					attrStringDef = AttrUtils.ATTR_TYPE_DATE + ":" + attrStringDef;
				}
				final AttributeLocator srcAttr = new AttributeLocator( 
						attrStringDef);
				sb.append("Parsed values are: \n");
				sb.append( MessageFormat.format( "\t source attribute id: {0}\n", srcAttr));

				srcAttrVal = getDateAttributeValue( srcAttr.getAttrId());
				// ���� ����������� � �������� �������
				if (srcAttrVal != null && formatOfDates != null && 
						formatOfDates.trim().length() > 0) {
					try {
							srcAttrVal= formatter.parse( fmtDate(srcAttrVal) );
					} catch (ParseException e) {
						new DataException( "jbr.card.check.date.invalid.1",
								new Object[] { srcAttrVal + " [format: " + formatOfDates + "]" }, e);
					}
				}

				sb.append( MessageFormat.format( "\t source date: <{0}>\n", srcAttrVal));

				/*
				 * (!) ������ �������� �������� ������� ���� currrent, ����� ������ 
				 * ���� ����� �� ���������� ������������ ����.
				 * �� ��������� = �������
				 */
				Date current = new Date();
				if (parts.length > 3) { // ������� ����� ��� current 
					current = resolveDateLink( parts[3], current);
					sb.append( MessageFormat.format( "\t base date present, value is: <{0}>\n", current));
				} else
					sb.append( MessageFormat.format( "\t base date is default, value is: <{0}>\n", current));

				if (parts.length <= 1) return; 
				from = resolveDateLink( parts[1], current);
				sb.append( MessageFormat.format( "\t from date value is: <{0}>\n", from));

				if (parts.length <= 2) return; 
				upto = resolveDateLink( parts[2], current);
				sb.append( MessageFormat.format( "\t upto date value is: <{0}>\n", upto));

			} finally {
				if (logger.isDebugEnabled()) {
					logger.debug(sb.toString());
				}
			}
		}

		/**
		 * ��������� �������� �������� ������� ��� srcAttr, ������� ���������� 
		 * ��� ������������ �������.  
		 */
		public boolean checkCondition() throws DataException {

			logger.debug( this.toString() );

			// ����� �������� current ��� ���� �� ������.
			boolean ok = true;
			String cardName = card.getAttributeById(Attribute.ID_NAME).getStringValue();
			
			// �������� �� ����������...
			ok = (srcAttrVal != null);
			if (!ok) {
				// ���� ��������� ����-�������� -> ��� � �������...
				if (getBooleanParameter(PARAM_DATE_NULLISOK, false)) {
					logger.debug("Card "+ card.getId() + " has NULL date at \'"
							+ srcAttrName + "\' -> configured to be accepted");
					return true;
				}
				throw new DataException( "jbr.card.check.date.empty.cardinfo.1", 
						new Object[]{ srcAttrName, cardName });
			}

			if (from == null && upto == null) {
				// ������� ��������� �� ���������� ...
				return true;
			} 

			if (from != null 
					&& !( from.equals(srcAttrVal) || from.before(srcAttrVal))
				)
				throw new DataException( "jbr.card.check.date.toosoon.cardinfo.2",
						new Object[]{ srcAttrName, fmtDate(from) , cardName} );

			if (upto != null 
					&& !( upto.equals(srcAttrVal) || upto.after(srcAttrVal))
				)
				throw new DataException( "jbr.card.check.date.toolate.cardinfo.2",
						new Object[]{ srcAttrName, fmtDate(upto), cardName} );

			return true;
		}

		/**
		 * ��������������� ���� �������� formatOfDates.
		 * @param dat
		 * @return
		 */
		String fmtDate(Date dat)
		{
			return (dat == null) ? null : formatter.format(dat);
		}

		/**
		 * ��������� ���� �������� � ����� �� ���� ����:
		 * 		1) ������������ current (+X/-X);
		 * 		2) � ���� ������ �� �������� �������� �������� card;
		 * 		3) � ����� ���� � ������� formatOfDates.
		 * @param dtLink
		 * @return
		 * @throws DataException 
		 */
		private Date resolveDateLink(String dtLink, Date base) 
			throws DataException 
		{
			if (dtLink == null) return null;

			dtLink = dtLink.trim();
			if (dtLink.length() < 1) return null;

			switch(dtLink.charAt(0)) {
				case '+':
				case '-': // ������������� ����� � ���� +float/-float � ���� ��� base
					if (base == null) base = new Date(); // now()
					final double offset = Double.parseDouble(dtLink);
					final Calendar c = Calendar.getInstance();
					c.setTime(base);
					c.setTimeInMillis(
							Math.round(
								c.getTimeInMillis() + offset * 1000 * 24 * 3600
							));
					try {
							// ���������� ����� ��� ������� ����:
							// 	������: dateFormat = "dd.mm.yyyy" - ������� �����
						return formatter.parse(formatter.format(c.getTime()));
					} catch (ParseException e) {
						throw new DataException( "jbr.card.check.date.invalid.1",
								new Object[] { formatOfDates }, e);
					}

				case '@': // ������ �� ������� ��������...
					final AttributeLocator src = new AttributeLocator( AttrUtils.ATTR_TYPE_DATE + ":" + dtLink);
					return getDateAttributeValue(src.getAttrId());

				default: // ���� �������� �����
					try {
						return formatter.parse(dtLink);
					} catch (ParseException ex) {
						logger.error( MessageFormat.format( "Attribute ''{0}'' contains date ''{1}'' not of format ''{2}''", 
								new Object[] { srcAttrName, dtLink, formatOfDates} ));
						throw new DataException( "jbr.card.check.date.invalid.1",
								new Object[] { srcAttrName }, ex);

					}
			}
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return MessageFormat.format( "dateIf( attr=@''{0}''=<{1}> in [ <{2}> .. <{3}> ] )", 
						this.srcAttrName, this.srcAttrVal, this.from, this.upto);
		}
	}

}
