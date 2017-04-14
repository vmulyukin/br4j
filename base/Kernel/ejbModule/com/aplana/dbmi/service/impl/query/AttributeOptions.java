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
package com.aplana.dbmi.service.impl.query;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.sql.Types;
import java.util.Collection;
import java.util.HashMap;

import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.support.SqlLobValue;

import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.BackLinkAttribute;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.DateAttribute;
import com.aplana.dbmi.model.DatedTypedCardLinkAttribute;
import com.aplana.dbmi.model.HtmlAttribute;
import com.aplana.dbmi.model.IntegerAttribute;
import com.aplana.dbmi.model.ListAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.PersonAttribute;
import com.aplana.dbmi.model.Reference;
import com.aplana.dbmi.model.ReferenceAttribute;
import com.aplana.dbmi.model.ReferenceConsumer;
import com.aplana.dbmi.model.StringAttribute;
import com.aplana.dbmi.model.TextAttribute;
import com.aplana.dbmi.model.TreeAttribute;
import com.aplana.dbmi.model.TypedCardLinkAttribute;
import com.aplana.dbmi.service.DataException;

/**
 * Utility class used to fetch/save additional attribute options 
 * from/into ATTRIBUTE_OPTION table.
 */
public class AttributeOptions
{
	public static final String LENGTH = "LENGTH";
	public static final String ROWS = "ROWS";
	//public static final String MULTIVALUED = "MULTIVALUED";
	public static final String REFERENCE = "REFERENCE";
	public static final String RESTRICTED = "RESTRICTED";
	public static final String FILTER = "FILTER";
	public static final String LINK = "LINK";
	public static final String UPLINK = "UPLINK";
	public static final String SINGLEVALUED = "SINGLEVALUED";
	//public static final String SHOWTIME = "SHOWTIME";
	public static final String TIMEPATTERN = "TIMEPATTERN";
	public static final String LINK_SEPARATOR = ";";
	private static final HashMap options = new HashMap();
	static {
		options.put(StringAttribute.class.getName(),
				new String[] { LENGTH });
		options.put(TextAttribute.class.getName(),
				new String[] { LENGTH, ROWS });
		options.put(IntegerAttribute.class.getName(),
				new String[] { LENGTH });
		//options.put(DateAttribute.class.getName(), new String[] { });
		options.put(ListAttribute.class.getName(),
				new String[] { REFERENCE, RESTRICTED });
		options.put(TreeAttribute.class.getName(),
				new String[] { REFERENCE, RESTRICTED });
		
		options.put(PersonAttribute.class.getName(), 
				new String[] { SINGLEVALUED });
		
		//options.put(SecurityAttribute.class.getName(), new String[] { });
		options.put(CardLinkAttribute.class.getName(),
				new String[] { FILTER, SINGLEVALUED });
		options.put(HtmlAttribute.class.getName(),
				new String[] { LENGTH, ROWS });
		options.put(BackLinkAttribute.class.getName(),
				new String[] { LINK, UPLINK });
		options.put(DateAttribute.class.getName(),
				new String[] { /*SHOWTIME,*/ TIMEPATTERN, /*DATEPATTERN*/ });
		options.put(TypedCardLinkAttribute.class.getName(),
				new String[] { FILTER, REFERENCE });
		options.put(DatedTypedCardLinkAttribute.class.getName(),
				new String[] { FILTER, REFERENCE });
	}
	
	/*private interface OptionPacker
	{
		void pack(Object data, OutputStream )
	}
	private static final HashMap packers = new HashMap();
	static {
		packers.put(LENGTH, new )
	}*/
	
	/**
	 * Gets list of option names which is allowed for given type of attributes.
	 * @param type type of attribute. Should be one of {@link Attribute} descendants.
	 * @return array containing names of options allowed for this type attributes.
	 */
	public static String[] getAttributeOptions(Class type)
	{
		if (!options.containsKey(type.getName()))
			return new String[0];
		return (String[]) options.get(type.getName());
	}
	
	/**
	 * Writes value of given attribute option to OutputStream.
	 * Also this method could modify values in XML_DATA table 
	 * to store additional option information.
	 * @param attr attribute to read option from
	 * @param option name of attribute option
	 * @param data OutputStream to write option data into
	 * @param jdbc jdbcTemplate jdbcTemplate to execute direct queries to database 
	 * @return true if option have value to be saved, false otherwise
	 * @throws DataException if any error occurs
	 */
	public static boolean packOption(Attribute attr, String option, OutputStream data, JdbcTemplate jdbc)
			throws DataException
	{
		OutputStreamWriter writer = new OutputStreamWriter(data);
		try {
			if (LENGTH.equals(option)) {
				int length;
				if (attr instanceof StringAttribute)
					length = ((StringAttribute) attr).getDisplayLength();
				//else if (attr instanceof TextAttribute)
				//	length = ((TextAttribute) attr).getDisplayLength();
				else if (attr instanceof IntegerAttribute)
					length = ((IntegerAttribute) attr).getDisplayLength();
				else
					return false;
					//throw new IllegalArgumentException("Invalid option " + option +
					//		" for attribute " + attr.getClass().getName());
				if (length == 0)
					return false;
				writer.write(Integer.toString(length));
				return true;
			}
			if (ROWS.equals(option)) {
				int rows;
				if (attr instanceof TextAttribute)
					rows = ((TextAttribute) attr).getRowsNumber();
				else
					return false;
					//throw new IllegalArgumentException("Invalid option " + option +
					//		" for attribute " + attr.getClass().getName());
				if (rows == 0)
					return false;
				writer.write(Integer.toString(rows));
				return true;
			}
			if (REFERENCE.equals(option)) {
				ObjectId refId;
				if (attr instanceof ReferenceAttribute)
					refId = ((ReferenceAttribute) attr).getReference();
				else
					return false;
					//throw new IllegalArgumentException("Invalid option " + option +
					//		" for attribute " + attr.getClass().getName());
				if (refId == null)
					return false;
				writer.write(refId.getId().toString());
				return true;
			}
			if (RESTRICTED.equals(option)) {
				return attr instanceof ReferenceAttribute &&
					((ReferenceAttribute) attr).isRestrictedList();
			}
			if (FILTER.equals(option)) {
				String filter;
				if (attr instanceof CardLinkAttribute)
					filter = ((CardLinkAttribute) attr).getFilterXml();
				else
					return false;
					//throw new IllegalArgumentException("Invalid option " + option +
					//		" for attribute " + attr.getClass().getName());
				if (filter == null)
					return false;
				// (2010/03) POSGRE
				// OLD: Long id = new Long(jdbc.queryForLong("SELECT seq_system_id.NEXTVAL FROM DUAL"));
				Long id = new Long(jdbc.queryForLong("SELECT nextval('seq_system_id')")); 
				jdbc.update(
						"INSERT INTO xml_data (xml_data_id, xml_type, description, xml_data) " +
						"VALUES (?, ?, ?, ?)",
						new Object[] { id, "SEARCH", "Filter for attribute " + attr.getId().getId(),
								new SqlLobValue( filter.getBytes("UTF-8") 
								/*, new OracleLobHandler() */ ) },
						new int[] { Types.NUMERIC, Types.VARCHAR, Types.VARCHAR, Types.BLOB });
				writer.write(id.toString());
				return true;
			}
			if (LINK.equals(option)) {
				if (!(attr instanceof BackLinkAttribute))
					return false;
				ObjectId idSource = ((BackLinkAttribute) attr).getLinkSource();
				// ������ � ��������� ����� ���� ��������� ����������, ������� ���������� � ���� ��� ����� ����������� LINK_SEPARATOR 
				Collection<ObjectId> idSources = ((BackLinkAttribute) attr).getLinkSources();
				if (idSource == null)
					return false;
				String output = idSource.getId().toString();
				if (idSources!=null){
					for(ObjectId idSourceId: idSources){
						output.concat(LINK_SEPARATOR).concat(idSourceId.getId().toString());
					}
				}
				writer.write(output);
				return true;
			}
			if (UPLINK.equals(option)) {
				if (!(attr instanceof BackLinkAttribute))
					return false;
				ObjectId idLink = ((BackLinkAttribute) attr).getInterimLink();
				if (idLink == null)
					return false;
				writer.write(idLink.getId().toString());
				return true;
			}
			if (SINGLEVALUED.equals(option)) {
				
				if(attr instanceof PersonAttribute){
					return ! ((PersonAttribute) attr).isMultiValued();
				}
				else if(attr instanceof CardLinkAttribute){
					return ! ((CardLinkAttribute) attr).isMultiValued();
				}
				
				
				throw new IllegalArgumentException("class "+attr.getClass()+"not defined for option: "+ SINGLEVALUED);
			}
			/*if (SHOWTIME.equals(option)) {
				return attr instanceof DateAttribute &&
					((DateAttribute) attr).isShowTime();
			}*/
			if (TIMEPATTERN.equals(option)) {
				if (!(attr instanceof DateAttribute)) {
					return false;
				}
				final String timePattern = ((DateAttribute) attr).getTimePattern();
					if (timePattern == null || timePattern.equals("")) {
						return false;
					}
					writer.write(timePattern);
					return true;
				}
			//throw new IllegalArgumentException("Unknown option: " + option);
			return false;
		} catch (IOException e) {
			LogFactory.getLog(AttributeOptions.class).error("Error storing option " + option +
					" for attribute " + attr.getClass().getName(), e);
			throw new DataException("store.attribute.option.value", e);		//*****
		} finally {
			try {
				writer.flush();
			} catch (IOException e) {
				LogFactory.getLog(AttributeOptions.class).error("Error storing option " + option +
						" for attribute " + attr.getClass().getName(), e);
				throw new DataException("store.attribute.option.value", e);		//*****
			}
		}
	}
	
	/**
	 * Initializes given attribute with value of option stored in given input stream.
	 * Also this method reads data from XML_DATA table if exists
	 * to fetch additional option information.
	 * @param attr attribute to be initialized
	 * @param option name of option
	 * @param data InputStream containing value of option
	 * @param jdbc jdbcTemplate jdbcTemplate to execute direct queries to database
	 * @throws DataException if any error occurs
	 */
	public static void extractOption(Attribute attr, String option, InputStream data, JdbcTemplate jdbc)
			throws DataException
	{
		extractOption(attr, option, data, jdbc, true);
	}

	/**
	 * Initializes given attribute with value of option stored in given input stream.
	 * Also this method could reads data from XML_DATA table 
	 * to fetch additional option information.
	 * @param attr attribute to be initialized
	 * @param option name of option
	 * @param data InputStream containing value of option
	 * @param jdbc jdbcTemplate jdbcTemplate to execute direct queries to database
	 * @param enableXMLSearch boolean flag to read XML_DATA table or not
	 * @throws DataException if any error occurs
	 */
	public static void extractOption(Attribute attr, String option, InputStream data, JdbcTemplate jdbc,
			boolean enableXMLSearch)
			throws DataException
	{
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(data));
			if (LENGTH.equals(option)) {
				int length = Integer.parseInt(reader.readLine());
				if (attr instanceof StringAttribute)
					((StringAttribute) attr).setDisplayLength(length);
				else
					//throw new DataException("");	//*****
					LogFactory.getLog(AttributeOptions.class).warn("Illegal " +
							LENGTH + " option found for attibute " + attr.getId().getId());
			} else if (ROWS.equals(option)) {
				int rows = Integer.parseInt(reader.readLine());
				if (attr instanceof TextAttribute)
					((TextAttribute) attr).setRowsNumber(rows);
				else
					//throw new DataException("");	//*****
					LogFactory.getLog(AttributeOptions.class).warn("Illegal " +
							ROWS + " option found for attibute " + attr.getId().getId());
			} else if (REFERENCE.equals(option)) {
				String refId = reader.readLine();
				if (attr instanceof ReferenceConsumer)
					((ReferenceConsumer) attr).setReference(new ObjectId(Reference.class, refId));
				else
					//throw new DataException("");	//*****
					LogFactory.getLog(AttributeOptions.class).warn("Illegal "
							+ REFERENCE + " option found for attibute " + attr.getId().getId());
			} else if (RESTRICTED.equals(option)) {
				if (attr instanceof ReferenceAttribute)
					((ReferenceAttribute) attr).setRestrictedList(true);
				else
					//throw new DataException("");	//*****
					LogFactory.getLog(AttributeOptions.class).warn("Illegal "
							+ RESTRICTED + " option found for attibute " + attr.getId().getId());
			} else if (FILTER.equals(option) && enableXMLSearch) {
				if (attr instanceof CardLinkAttribute) {
					/*Search filter = new Search();
					filter.initFromXml(data);
					((CardLinkAttribute) attr).setFilter(filter);*/
					/*String xml = "";
					String line;
					while (true) {
						line = reader.readLine();
						if (line == null)
							break;
						xml += line + "\n";
					}
					((CardLinkAttribute) attr).setFilterXml(xml);*/
					Long num = new Long(reader.readLine());
					/* ORA:
					Blob blob = (Blob) jdbc.queryForObject(
							"SELECT xd.xml_data FROM xml_data xd WHERE xd.xml_data_id=?",
							new Object[] { num },
							new int[] { Types.NUMERIC },
							Blob.class);
					try {
						((CardLinkAttribute) attr).setFilterXml(
								new String(blob.getBytes(1, (int) blob.length()), "UTF-8"));
					} catch (SQLException e) {
						throw new DataException("fetch.attribute.option.value", e);		//*****
					}
					*/
					byte[] blob = (byte[]) jdbc.queryForObject(
							"SELECT xd.xml_data FROM xml_data xd WHERE xd.xml_data_id=?",
							new Object[] { num },
							new int[] { Types.NUMERIC },
							byte[].class
							);
					((CardLinkAttribute) attr).setFilterXml( new String( blob, "UTF-8"));
					
				} else
					//throw new DataException("");	//*****
					LogFactory.getLog(AttributeOptions.class).warn("Illegal " +
							FILTER + " option found for attibute " + attr.getId().getId());
			} else if (LINK.equals(option)) {
				//String sourceId = reader.readLine();
				// ������ � �������� � ������ ����� ������ ��������� ���������� ����� ����������� LINK_SEPARATOR, ��� ���� ������ ��������� � ������� linkSource, � �� ������ ��������� ������������� ��������� linkSources
				String[] sources = reader.readLine().split(LINK_SEPARATOR);
				for(String sourceId: sources){
				if (attr instanceof BackLinkAttribute)
					((BackLinkAttribute) attr).setLinkSource(
							new ObjectId(CardLinkAttribute.class, sourceId));
				else
					//throw new DataException("");	//*****
					LogFactory.getLog(AttributeOptions.class).warn("Illegal "
							+ LINK + " option found for attibute " + attr.getId().getId());
				}

			} else if (UPLINK.equals(option)) {
				String linkId = reader.readLine();
				if (attr instanceof BackLinkAttribute)
					((BackLinkAttribute) attr).setInterimLink(
							new ObjectId(CardLinkAttribute.class, linkId));
				else
					//throw new DataException("");	//*****
					LogFactory.getLog(AttributeOptions.class).warn("Illegal "
							+ UPLINK + " option found for attibute " + attr.getId().getId());

			} else if(SINGLEVALUED.equals(option)){
				if( attr instanceof PersonAttribute){
					PersonAttribute personAttribute=(PersonAttribute) attr;
					personAttribute.setMultiValued(false);
				}
				else if( attr instanceof CardLinkAttribute){
					CardLinkAttribute cardLinkAttribute=(CardLinkAttribute) attr;
					cardLinkAttribute.setMultiValued(false);
				}
		
			
			} /*else if (SHOWTIME.equals(option)) {
				if (attr instanceof DateAttribute)
					((DateAttribute) attr).setShowTime(true);
				else
					//throw new DataException("");	//*****
					LogFactory.getLog(AttributeOptions.class).warn("Illegal "
							+ SHOWTIME + " option found for attibute " + attr.getId().getId());
			}*/ else if (TIMEPATTERN.equals(option)) {
				if (attr instanceof DateAttribute)
					((DateAttribute) attr).setTimePattern(reader.readLine());
				else
					//throw new DataException("");	//*****
					LogFactory.getLog(AttributeOptions.class).warn("Illegal "
							+ TIMEPATTERN + " option found for attibute " + attr.getId().getId());
			}
		} catch (NumberFormatException e) {
			throw new DataException("fetch.attribute.option.value", e);	//*****
		} catch (IOException e) {
			throw new DataException("fetch.attribute.option.value", e);	//*****
		}
	}
}
