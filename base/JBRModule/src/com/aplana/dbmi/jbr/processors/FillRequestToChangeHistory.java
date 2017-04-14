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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.springframework.jdbc.core.RowMapper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.aplana.dbmi.action.ChangeState;
import com.aplana.dbmi.action.LockObject;
import com.aplana.dbmi.action.UnlockObject;
import com.aplana.dbmi.jbr.processors.ProcessCard;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.ContextProvider;
import com.aplana.dbmi.model.HtmlAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.model.PersonAttribute;
import com.aplana.dbmi.model.WorkflowMove;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ActionQueryBase;
import com.aplana.dbmi.service.impl.Database;
import com.aplana.dbmi.service.impl.ObjectQueryBase;
import com.aplana.dbmi.service.impl.QueryFactory;
import com.aplana.dbmi.service.impl.SaveQueryBase;
import com.aplana.dbmi.service.impl.UserData;

/**
 * ��������� ������� �������� ������� "������ �� ����� ����������������"
 * @author ppolushkin
 * @since 09.12.14
 */
@SuppressWarnings("unused")
public class FillRequestToChangeHistory extends ProcessCard {
	private static final long serialVersionUID = 1L;
	
	public static final ObjectId REQ_HISTORY = ObjectId.predefined(HtmlAttribute.class, "jbr.reqres.hist");
	private Transformer trans;
	protected String schemaLocation="/conf/RepeatableReportSchema.xsd";
	protected String xsltLocation="/conf/ShowRepeatableReportTable.xslt";
	private static final String JAXP_SCHEMA_LANGUAGE = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";
	private static final String JAXP_SCHEMA_SOURCE = "http://java.sun.com/xml/jaxp/properties/schemaSource";	
	private static final String W3C_XML_SCHEMA = "http://www.w3.org/2001/XMLSchema";
	public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
	
	static final ResourceBundle bundle = ResourceBundle.getBundle("jbrDocFlow", ContextProvider.getContext().getLocale());
	
	protected String transform(Document xml, String xsltPath) throws TransformerException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			getTransformer(xsltPath).transform(new DOMSource(xml), new StreamResult(baos));
			return baos.toString("UTF-8");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private synchronized Transformer getTransformer(String path) throws TransformerConfigurationException {
		if (trans == null) {
			Source xsltSource = new StreamSource(new File(path));
	        TransformerFactory transFact = TransformerFactory.newInstance();
			trans = transFact.newTransformer(xsltSource);
		}
		return trans;
	}
	
	protected Element createPart(Document xml, String text, Date date) {
		Element part = xml.createElement("part");
		part.setAttribute("timestamp", date == null ? "-" : DATE_FORMAT.format(date));
		part.setTextContent(trimAndNewlineRight(text));
		return part;
	}
	
	protected static String trimAndNewlineRight(String input) {
		StringBuilder sb = new StringBuilder();
		sb.append(input);
		int len = input.length();
		for (int i = len - 1; i >= 0; i--) {
			char c = sb.charAt(i);
			if (!Character.isWhitespace(c)) {
				if (i < len - 1)
					sb.replace(i + 1, len, "\n");
				return sb.toString();
			}
		}
		return "";
	}
	
	public Object process() throws DataException {
		try {
			Card card = null;
			final ObjectId cardId = getCardId(); //�������� id ��������
			
			//�������� ��������
			if (cardId != null) {
				ObjectQueryBase query = getQueryFactory().getFetchQuery(Card.class);
				query.setId(cardId);
				card = (Card) getDatabase().executeQuery(getSystemUser(), query);
				if (card == null){
					logger.error("The card does not exist. Exit.");
					return null;
				}
			} else{
				if (!(getResult() instanceof Card)){
					logger.error("The card does not exist. Exit.");
					return null;
				}
				logger.warn("The card is just created (most probably). I'll use it.");
				card = (Card) getResult();
			}
			
			List<RequestHistoryData> list = loadHistory(cardId); 
			
			String xml = formHistoryAttrValue(list, cardId);
			
			HtmlAttribute histAttr = (HtmlAttribute) card.getAttributeById(REQ_HISTORY);				
			histAttr.setValue(xml);
			//��������� �������
			doOverwriteCardAttributes(cardId, getSystemUser(), histAttr);
			return null;
		} catch(DataException ex){
			throw ex;
		} catch(Exception ex){
			throw new DataException("Can not exec FillRequestToChangeHistory processor", ex); 
		}
	}
	
	private List<RequestHistoryData> loadHistory(ObjectId cardId) {
		StringBuilder sql = new StringBuilder();
		sql.append("select rtype, created, old_rassm, new_rassm, old_date, new_date, old_otv, new_otv, rdate from \n");
		sql.append("    ( \n");
		sql.append("        select vl_type.value_rus as rtype, \n");
		sql.append("        av_cr_date.date_value as created, \n");
		sql.append("        case when av_type.value_id = 1127 then null else av_prev_rassm_snm.string_value end as old_rassm, \n");
		sql.append("        case when av_type.value_id in (1128,1129) then null else av_new_rassm_snm.string_value end as new_rassm, \n");
		sql.append("        case when av_type.value_id = 1127 then null when av_type.value_id = 1128 then av_todate.date_value else av_state_do.date_value end as old_date, \n");
		sql.append("        case when av_type.value_id = 1128 then null else av_state_after_date.date_value end as new_date, \n");
		sql.append("        case when av_type.value_id in (1127,1129) then null when av_type.value_id = 1128 then av_cons_values.value_rus else av_state_do_values.value_rus end as old_otv, \n");
		sql.append("        case when av_type.value_id in (1128,1129) then null else new_resp.value_rus end as new_otv, \n");
		sql.append("        av_proc_date.date_value as rdate \n\n");

		sql.append("        from card c \n");
		sql.append("        left join attribute_value av_cr_date on av_cr_date.card_id = c.card_id and av_cr_date.attribute_code = 'CREATED' \n");
		sql.append("        left join attribute_value av_proc_date on av_proc_date.card_id = c.card_id and av_proc_date.attribute_code = 'JBR_REQ_RES_DATE' \n");
		sql.append("        left join attribute_value av_type on av_type.card_id = c.card_id and av_type.attribute_code = 'JBR_REQUEST_TYPE' \n");
		sql.append("        left join values_list vl_type on vl_type.value_id= av_type.value_id \n\n");

		sql.append("        left join attribute_value av_rassm on c.card_id = av_rassm.card_id and av_rassm.attribute_code = 'JBR_REQUEST_CONS' \n");
		sql.append("        left join attribute_value av_rassm_p on av_rassm.number_value = av_rassm_p.card_id and av_rassm_p.attribute_code = 'JBR_RASSM_PERSON' \n\n");

		sql.append("        join person p_rassm_p on av_rassm_p.number_value = p_rassm_p.person_id \n\n");

		sql.append("        left join attribute_value av_prev_rassm_snm on p_rassm_p.card_id = av_prev_rassm_snm.card_id and av_prev_rassm_snm.attribute_code = 'JBR_PERS_SNAME_NM' \n");
		sql.append("        left join attribute_value av_todate on av_rassm.number_value = av_todate.card_id and av_todate.attribute_code = 'JBR_RASSM_TODATE' \n");
		sql.append("        left join attribute_value av_cons on av_rassm.number_value = av_cons.card_id and av_cons.attribute_code = 'JBR_RESPONS_CONSIDER' \n");
		sql.append("        left join values_list av_cons_values on av_cons_values.value_id = av_cons.value_id \n\n");

		sql.append("        left join attribute_value av_new_rassm_p on av_new_rassm_p.card_id = c.card_id and av_new_rassm_p.attribute_code = 'JBR_REQUEST_NEW' \n");
		sql.append("        left join person av_new_rassm_person on av_new_rassm_person.card_id = av_new_rassm_p.number_value \n");
		sql.append("        left join attribute_value av_new_rassm_snm on av_new_rassm_p.number_value = av_new_rassm_snm.card_id and av_new_rassm_snm.attribute_code = 'JBR_PERS_SNAME_NM' \n");
		sql.append("        left join attribute_value av_chan_date on av_chan_date.card_id = c.card_id and av_chan_date.attribute_code = 'JBR_REQUEST_CHAN' \n");
		sql.append("        left join attribute_value av_state_do on av_new_rassm_p.number_value in \n");
		sql.append("            ( \n");
		sql.append("                select p_rassm_p.card_id from attribute_value av_rassm_p \n");
		sql.append("                join person p_rassm_p on av_rassm_p.number_value = p_rassm_p.person_id \n");
		sql.append("                where av_rassm_p.card_id = av_state_do.number_value \n");
		sql.append("                and av_rassm_p.attribute_code = 'JBR_RASSM_PERSON' \n");
		sql.append("            ) \n");
		sql.append("            and av_state_do.card_id = c.card_id and av_state_do.attribute_code = 'JBR_REQUEST_PREV' \n");
		sql.append("        left join values_list av_state_do_values on av_state_do_values.value_id = av_state_do.value_id \n\n\n");

		sql.append("        left join attribute_value av_state_after_main on av_state_after_main.card_id = c.card_id and av_state_after_main.attribute_code = 'JBR_MAINDOC_REQUEST' \n");
		sql.append("        left join attribute_value av_state_after on av_state_after.card_id = av_state_after_main.number_value and av_state_after.attribute_code = 'JBR_IMPL_ACQUAINT' \n");
		sql.append("        join card av_rassm_card_id on av_rassm_card_id.card_id = av_state_after.number_value and av_rassm_card_id.status_id <> 34145 \n");
		sql.append("        left join attribute_value av_state_after_p on av_state_after_p.card_id = av_rassm_card_id.card_id and av_state_after_p.attribute_code = 'JBR_RASSM_PERSON' \n");
		sql.append("        left join attribute_value av_state_after_date on av_state_after_date.card_id = av_rassm_card_id.card_id and av_state_after_date.attribute_code = 'JBR_RASSM_TODATE' \n");
		sql.append("        left join attribute_value av_state_after_resp on av_state_after_resp.card_id = av_rassm_card_id.card_id and av_state_after_resp.attribute_code = 'JBR_RESPONS_CONSIDER' \n");
		sql.append("        left join values_list new_resp on new_resp.value_id = av_state_after_resp.value_id \n\n");

		sql.append("        where c.card_id = ? \n");
		sql.append("        and c.status_id = 10000101 \n");
		sql.append("        and (av_state_after_p.number_value = av_new_rassm_person.person_id) \n");
		sql.append("        and av_type.value_id not in (1128, 1129) \n\n");

		sql.append("        UNION \n\n");
						   //������ �� �������� ���������������� � ��������� ����� ������������ 
		sql.append("        select vl_type.value_rus as rtype, \n");
		sql.append("        av_cr_date.date_value as created, \n");
		sql.append("        av_prev_rassm_snm.string_value as old_rassm, \n");
		sql.append("        null as new_rassm, \n");
		sql.append("        case when av_type.value_id = 1128 then av_todate.date_value else av_state_do.date_value end as old_date, \n");
		sql.append("        case when av_type.value_id = 1128 then null else av_chan_date.date_value end as new_date, \n");
		sql.append("        case when av_type.value_id = 1129 then null    when av_type.value_id = 1128 then av_cons_values.value_rus end as old_otv, \n");
		sql.append("        null as new_otv, \n");
		sql.append("        av_proc_date.date_value as rdate \n\n");

		sql.append("        from card c \n");
		sql.append("        left join attribute_value av_cr_date on av_cr_date.card_id = c.card_id and av_cr_date.attribute_code = 'CREATED' \n");
		sql.append("        left join attribute_value av_proc_date on av_proc_date.card_id = c.card_id and av_proc_date.attribute_code = 'JBR_REQ_RES_DATE' \n");
		sql.append("        left join attribute_value av_type on av_type.card_id = c.card_id and av_type.attribute_code = 'JBR_REQUEST_TYPE' \n");
		sql.append("        left join values_list vl_type on vl_type.value_id= av_type.value_id \n\n");

		sql.append("        left join attribute_value av_rassm on c.card_id = av_rassm.card_id and av_rassm.attribute_code = 'JBR_REQUEST_CONS' \n");
		sql.append("        left join attribute_value av_rassm_p on av_rassm.number_value = av_rassm_p.card_id and av_rassm_p.attribute_code = 'JBR_RASSM_PERSON' \n\n");

		sql.append("        join person p_rassm_p on av_rassm_p.number_value = p_rassm_p.person_id \n\n");

		sql.append("        left join attribute_value av_prev_rassm_snm on p_rassm_p.card_id = av_prev_rassm_snm.card_id and av_prev_rassm_snm.attribute_code = 'JBR_PERS_SNAME_NM' \n");
		sql.append("        left join attribute_value av_todate on av_rassm.number_value = av_todate.card_id and av_todate.attribute_code = 'JBR_RASSM_TODATE' \n");
		sql.append("        left join attribute_value av_cons on av_rassm.number_value = av_cons.card_id and av_cons.attribute_code = 'JBR_RESPONS_CONSIDER' \n");
		sql.append("        left join values_list av_cons_values on av_cons_values.value_id = av_cons.value_id \n\n");

		sql.append("        left join attribute_value av_new_rassm_p on av_new_rassm_p.card_id = c.card_id and av_new_rassm_p.attribute_code = 'JBR_REQUEST_NEW' \n");
		sql.append("        left join person av_new_rassm_person on av_new_rassm_person.card_id = av_new_rassm_p.number_value \n");
		sql.append("        left join attribute_value av_chan_date on av_chan_date.card_id = c.card_id and av_chan_date.attribute_code = 'JBR_REQUEST_CHAN' \n");
		sql.append("        left join attribute_value av_state_do on p_rassm_p.card_id in \n");
		sql.append("            ( \n");
		sql.append("                select p_rassm_p.card_id from attribute_value av_rassm_p \n");
		sql.append("                join person p_rassm_p on av_rassm_p.number_value = p_rassm_p.person_id \n");
		sql.append("                where av_rassm_p.card_id = av_state_do.number_value \n");
		sql.append("                and av_rassm_p.attribute_code = 'JBR_RASSM_PERSON' \n");
		sql.append("            ) \n");
		sql.append("            and av_state_do.card_id = c.card_id and av_state_do.attribute_code = 'JBR_REQUEST_PREV' \n\n");

		sql.append("        where c.card_id = ? \n");
		sql.append("        and c.status_id = 10000101 \n");
		sql.append("        and av_type.value_id in (1128, 1129) \n\n");

		sql.append("    ) as hist \n");
		sql.append("order by rdate");
		
		String sqlStr = sql.toString();//TODO Remove after debug

		@SuppressWarnings("unchecked")
		List<RequestHistoryData> list = getJdbcTemplate().query(sql.toString(), 
				new Object[]{ cardId.getId(), cardId.getId()},
				new int[] { Types.NUMERIC, Types.NUMERIC},
				new RowMapper(){
					public Object mapRow(final ResultSet rs, final int row) throws SQLException {
						final RequestHistoryData rhd = new RequestHistoryData();
						String str = rs.getString(1);
						rhd.setRequestType(str != null ? str : "");
						
						Date date = rs.getTimestamp(2);
						rhd.setCreated(date != null ? date : null);
						
						str = rs.getString(3);
						rhd.setOldRassm(str != null ? str : "");
						
						str = rs.getString(4);
						rhd.setNewRassm(str != null ? str : "");
						
						date = rs.getTimestamp(5);
						rhd.setOldTerm(date != null ? date : null);
						
						date = rs.getTimestamp(6);
						rhd.setNewTerm(date != null ? date : null);
						
						str = rs.getString(7);
						rhd.setOldResp(str != null ? str : "");
						
						str = rs.getString(8);
						rhd.setNewResp(str != null ? str : "");
						
						date = rs.getTimestamp(9);
						rhd.setRequestDate(date != null ? date : null);
						
						return rhd;
					}
				});
		
		return list;
	}
	
	private String formHistoryAttrValue(List<RequestHistoryData> list, ObjectId cardId)
			throws 	ParserConfigurationException, 
					UnsupportedEncodingException, 
					SAXException, 
					IOException, 
					TransformerFactoryConfigurationError, 
					TransformerException {
		Document xmldoc;
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = dbf.newDocumentBuilder();
		xmldoc = builder.parse(new ByteArrayInputStream("<?xml version=\"1.0\" encoding=\"UTF-8\"?><report/>".getBytes("UTF-8")));
		Element root = xmldoc.getDocumentElement();

		Element part;
		
		for(RequestHistoryData rhd : list) {
			part = xmldoc.createElement("part");
			part.setAttribute("id", String.valueOf(cardId.getId()));
			part.setAttribute("req-type", rhd.getRequestType());
			part.setAttribute("created", (rhd.getCreated() == null) ? "-" : DATE_FORMAT.format(rhd.getCreated()));
			part.setAttribute("old-rassm", rhd.getOldRassm());
			part.setAttribute("new-rassm", rhd.getNewRassm());
			part.setAttribute("old-date", (rhd.getOldTerm() == null) ? "-" : DATE_FORMAT.format(rhd.getOldTerm()));
			part.setAttribute("new-date", (rhd.getNewTerm() == null) ? "-" : DATE_FORMAT.format(rhd.getNewTerm()));
			part.setAttribute("old-resp", rhd.getOldResp());
			part.setAttribute("new-resp", rhd.getNewResp());
			part.setAttribute("req-date", (rhd.getRequestDate() == null) ? "-" : DATE_FORMAT.format(rhd.getRequestDate()));
			root.appendChild(part);
		}
	
		final StringWriter stw = new StringWriter();
		final Transformer serializer = TransformerFactory.newInstance().newTransformer();
		serializer.transform(new DOMSource(xmldoc), new StreamResult(stw));
	
		return stw.toString();
	}
	
	class RequestHistoryData {
		private String requestType;
		private Date created;
		private String oldRassm;
		private String newRassm;
		private Date oldTerm;
		private Date newTerm;
		private String oldResp;
		private String newResp;
		private Date requestDate;
		
		public Date getCreated() {
			return created;
		}
		public void setCreated(Date created) {
			this.created = created;
		}
		public String getRequestType() {
			return requestType;
		}
		public void setRequestType(String requestType) {
			this.requestType = requestType;
		}
		public Date getRequestDate() {
			return requestDate;
		}
		public void setRequestDate(Date requestDate) {
			this.requestDate = requestDate;
		}
		public String getOldRassm() {
			return oldRassm;
		}
		public void setOldRassm(String oldRassm) {
			this.oldRassm = oldRassm;
		}
		public String getNewRassm() {
			return newRassm;
		}
		public void setNewRassm(String newRassm) {
			this.newRassm = newRassm;
		}
		public Date getOldTerm() {
			return oldTerm;
		}
		public void setOldTerm(Date oldTerm) {
			this.oldTerm = oldTerm;
		}
		public Date getNewTerm() {
			return newTerm;
		}
		public void setNewTerm(Date newTerm) {
			this.newTerm = newTerm;
		}
		public String getOldResp() {
			return oldResp;
		}
		public void setOldResp(String oldResp) {
			this.oldResp = oldResp;
		}
		public String getNewResp() {
			return newResp;
		}
		public void setNewResp(String newResp) {
			this.newResp = newResp;
		}
	}
}