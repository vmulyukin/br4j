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
package com.aplana.dbmi.jasperreports;

import java.io.InputStream;
import java.sql.*;
import java.text.SimpleDateFormat;



import com.aplana.dbmi.parser.ItemTag;
import com.aplana.dbmi.parser.XmlParse;

/**
 * @author dbashmakov
 *         Date: 17.04.12
 *         Time: 9:50
 */
public class PrintedFormReport {

    enum TYPE_DOC {
        EXECUTERS,
        COAUTHORS,
        NOTE,
        FOREIGN_EXECUTORS
    }

    private static Connection curConn = null;
    private static PreparedStatement prStmtExecutors = null;
    private Long timeZone = null;

    public PrintedFormReport() {
        timeZone = new java.lang.Long(java.util.Calendar.getInstance().getTimeZone().getRawOffset() / 3600000);
    }

    private String getCommonSql(TYPE_DOC type_sql, boolean printOrg){
        StringBuilder sql = new StringBuilder();
        sql.append(" SELECT ");
        sql.append(" av_sname_nm.string_value as sname,");
        sql.append(" av_name_nm.string_value as name,");
        sql.append(" av_mname_nm.string_value as mname,");
        
        if(printOrg)
        	sql.append(" av_org_name.string_value as org_name,");
        
        sql.append(" cs.name_rus as status,");
        sql.append(" att_xml.long_binary_value as xml");
        sql.append(" from card c ");

        

        switch (type_sql){
            case FOREIGN_EXECUTORS:
                sql.append(" join attribute_value av_attr_rep on (av_attr_rep.number_value=c.card_id and av_attr_rep.attribute_code=?)");
            	sql.append(" join attribute_value av_ex_per on (av_ex_per.card_id = av_attr_rep.card_id and av_ex_per.attribute_code=?)");
                sql.append(" left join attribute_value av_sname_nm on (av_sname_nm.card_id = av_ex_per.number_value and av_sname_nm.attribute_code = 'JBR_PERS_SNAME')");
                sql.append(" left join attribute_value av_name_nm on (av_name_nm.card_id = av_ex_per.number_value and av_name_nm.attribute_code = 'JBR_PERS_NAME')");
                sql.append(" left join attribute_value av_mname_nm on (av_mname_nm.card_id = av_ex_per.number_value and av_mname_nm.attribute_code = 'JBR_PERS_MNAME')");
                sql.append(" join card c_rep on (c_rep.card_id=av_attr_rep.card_id)");
                break;
            case NOTE:
                sql.append(" join attribute_value av_attr_rep on (av_attr_rep.card_id=c.card_id and av_attr_rep.attribute_code=?)");
            	sql.append(" join attribute_value av_ex_per on (av_ex_per.card_id = av_attr_rep.number_value and av_ex_per.attribute_code=?)");
            	sql.append(" left join person p on (p.person_id=av_ex_per.number_value)");
                sql.append(" left join attribute_value av_sname_nm on (av_sname_nm.card_id = p.card_id and av_sname_nm.attribute_code = 'JBR_PERS_SNAME')");
                sql.append(" left join attribute_value av_name_nm on (av_name_nm.card_id = p.card_id and av_name_nm.attribute_code = 'JBR_PERS_NAME')");
                sql.append(" left join attribute_value av_mname_nm on (av_mname_nm.card_id = p.card_id and av_mname_nm.attribute_code = 'JBR_PERS_MNAME')");
                if(printOrg) {
	                sql.append(" left join attribute_value av_org_link on (av_org_link.card_id = p.card_id and av_org_link.attribute_code = 'JBR_PERS_ORG')");
	                sql.append(" left join attribute_value av_org_name on (av_org_name.card_id = av_org_link.number_value and av_org_name.attribute_code = 'JBR_DORG_SHORTNAME')");
                }
	            sql.append(" join card c_rep on (c_rep.card_id=av_attr_rep.number_value)");
                break;
            case EXECUTERS:
            	sql.append(" join attribute_value av_e on (av_e.card_id=c.card_id and av_e.attribute_code = ?)");       
                sql.append(" join attribute_value av_attr_rep on (av_attr_rep.number_value=c.card_id and av_attr_rep.attribute_code=?)");
            	sql.append(" join attribute_value av_ex_per on (av_ex_per.card_id = av_attr_rep.card_id and av_ex_per.attribute_code=? and  av_ex_per.number_value=av_e.number_value)");
                sql.append(" left join person p on (p.person_id=av_ex_per.number_value)");
                sql.append(" left join attribute_value av_sname_nm on (av_sname_nm.card_id = p.card_id and av_sname_nm.attribute_code = 'JBR_PERS_SNAME')");
                sql.append(" left join attribute_value av_name_nm on (av_name_nm.card_id = p.card_id and av_name_nm.attribute_code = 'JBR_PERS_NAME')");
                sql.append(" left join attribute_value av_mname_nm on (av_mname_nm.card_id = p.card_id and av_mname_nm.attribute_code = 'JBR_PERS_MNAME')");
                if(printOrg) {
	                sql.append(" left join attribute_value av_org_link on (av_org_link.card_id = p.card_id and av_org_link.attribute_code = 'JBR_PERS_ORG')");
	                sql.append(" left join attribute_value av_org_name on (av_org_name.card_id = av_org_link.number_value and av_org_name.attribute_code = 'JBR_DORG_SHORTNAME')");
                }
	            sql.append(" join card c_rep on (c_rep.card_id=av_attr_rep.card_id)");
                break;
            default:
                sql.append(" join attribute_value av_e on (av_e.card_id=c.card_id and av_e.attribute_code = ?)");       
                sql.append(" join attribute_value av_attr_rep on (av_attr_rep.card_id=c.card_id and av_attr_rep.attribute_code=?)");
            	sql.append(" join attribute_value av_ex_per on (av_ex_per.card_id = av_attr_rep.number_value and av_ex_per.attribute_code=? and  av_ex_per.number_value=av_e.number_value)");
                sql.append(" left join person p on (p.person_id=av_ex_per.number_value)");
                sql.append(" left join attribute_value av_sname_nm on (av_sname_nm.card_id = p.card_id and av_sname_nm.attribute_code = 'JBR_PERS_SNAME')");
                sql.append(" left join attribute_value av_name_nm on (av_name_nm.card_id = p.card_id and av_name_nm.attribute_code = 'JBR_PERS_NAME')");
                sql.append(" left join attribute_value av_mname_nm on (av_mname_nm.card_id = p.card_id and av_mname_nm.attribute_code = 'JBR_PERS_MNAME')");
                if(printOrg) {
	                sql.append(" left join attribute_value av_org_link on (av_org_link.card_id = p.card_id and av_org_link.attribute_code = 'JBR_PERS_ORG')");
	                sql.append(" left join attribute_value av_org_name on (av_org_name.card_id = av_org_link.number_value and av_org_name.attribute_code = 'JBR_DORG_SHORTNAME')");
                }
                sql.append(" join card c_rep on (c_rep.card_id=av_attr_rep.number_value)");
                break;
        }

        sql.append(" join card_status cs on (cs.status_id=c_rep.status_id)");
        sql.append(" left join attribute_value att_xml on (att_xml.card_id=c_rep.card_id and att_xml.attribute_code='ADMIN_702354')");

        sql.append(" where c.card_id = ? order by c_rep.card_id asc");

        return sql.toString();
    }
    /**
     * �������� ���������� ������, � ������������ ���� � ����� "��������� � ����������"
     * @param rs
     * @param type_doc
     * @return
     * @throws Exception
     */
    private String geterateText(ResultSet rs, TYPE_DOC type_doc, boolean printOrg) throws Exception {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            String str = null;
            while (rs.next()) {
                if(stringBuilder.length()>0){
                    stringBuilder.append("\n");
                }
                //str=rs.getString("name");
                if(!isNull(rs.getString("sname"))){
                    stringBuilder.append(rs.getString("sname")).append(" ");
                }
                if(!isNull(rs.getString("name"))){
                    stringBuilder.append(rs.getString("name").substring(0,1)).append(".");
                }
                if(!isNull(rs.getString("mname"))){
                    stringBuilder.append(rs.getString("mname").substring(0,1)).append(". ");
                }
                if(printOrg && !isNull(rs.getString("org_name"))){
                    stringBuilder.append(rs.getString("org_name")).append(" ");
                }
                str=rs.getString("status");
                if(!isNull(str)){
                    stringBuilder.append("(").append(str).append(")");
                }
                switch (type_doc){
                    case NOTE:  break;
                    default:
                       /* if(rs.getTimestamp("date_p")!=null){
                            str = new SimpleDateFormat("dd.MM.yyyy").format(new Date(rs.getTimestamp("date_p").getTime()+timeZone.longValue()));
                            stringBuilder.append(", ").append(str);
                        }    */

                        InputStream inputStream = rs.getBinaryStream("xml");
                        if(inputStream==null){
                            continue;
                        }
                        ItemTag itemTag = new ItemTag();
                        XmlParse.parse(itemTag, inputStream);
                        int count = itemTag.getItemTags().size();
                        itemTag = count==0?null:itemTag.getItemTags().get(count-1);

                        str =  itemTag.getAttrMap().get("timestamp").replace("T"," ");
                        str = new SimpleDateFormat("dd.MM.yyyy").format( new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse(str).getTime()+timeZone.longValue());
                        stringBuilder.append(", ").append(str);
                        
                        if(!isNull(itemTag.getMsg())){
                        	stringBuilder.append(", ").append(itemTag.getMsg().replaceAll("&#13;", " "));
                        }


                        /*str=rs.getString("text_r");
                        if(!isNull(str)){
                            stringBuilder.append(", ").append(str);
                        }  */
                }

            }
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (Exception ignoreEx) {
                }
            }
        }
        return stringBuilder.toString();
    }

    private String getExecutors(Connection connection, Long resId, boolean printOrg) throws Exception {
        String sql = getCommonSql(TYPE_DOC.EXECUTERS, printOrg);
        PreparedStatement preparedStatement = getPsExecutors(connection, sql, "JBR_INFD_EXEC_LINK", resId);        
        ResultSet rs = preparedStatement.executeQuery();
        StringBuilder stringBuilder = new StringBuilder(geterateText(rs, TYPE_DOC.EXECUTERS, printOrg));
        
        preparedStatement = getPsExecutors(connection, sql, "ADMIN_255974", resId);        
        rs = preparedStatement.executeQuery();
        String text = geterateText(rs, TYPE_DOC.EXECUTERS, printOrg);
        if(text!=null && !text.isEmpty() ){
        	stringBuilder.append("\n");
        	stringBuilder.append(text);
        }
                
        
        if(stringBuilder.length()>0){
            stringBuilder.insert(0, "�����������: ");
        }
        String s = stringBuilder.toString();
        return  s;
    }
    
    private PreparedStatement getPsExecutors(Connection connection, String sql, String attrId, Long resId) throws SQLException{
    	PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setString(1, attrId);
        preparedStatement.setString(2, "ADMIN_702311");
        preparedStatement.setString(3, "ADMIN_702335");
        preparedStatement.setLong(4, resId);
        return preparedStatement;
    }

    private String getNote(Connection connection, Long resId, boolean printOrg) throws Exception {
        String sql = getCommonSql(TYPE_DOC.NOTE, printOrg);
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setString(1, "ADMIN_713517");
        preparedStatement.setString(2, "ADMIN_726874");
        preparedStatement.setLong(3, resId);
        final ResultSet rs = preparedStatement.executeQuery();
        StringBuilder stringBuilder = new StringBuilder(geterateText(rs,TYPE_DOC.NOTE, printOrg));
        if(stringBuilder.length()>0){
            stringBuilder.insert(0, "� ��������: ");
        }
        return stringBuilder.toString();
    }

    private String getForeignExecutors(Connection connection, Long resId, boolean printOrg) throws Exception {
        String sql = getCommonSql(TYPE_DOC.FOREIGN_EXECUTORS, printOrg);
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setString(1, "ADMIN_702600");
        preparedStatement.setString(2, "ADMIN_702598");
        preparedStatement.setLong(3, resId);
        final ResultSet rs = preparedStatement.executeQuery();
        StringBuilder stringBuilder = new StringBuilder(geterateText(rs, TYPE_DOC.FOREIGN_EXECUTORS, printOrg));
        if(stringBuilder.length()>0){
            stringBuilder.insert(0, "������� �����������: ");
        }
        return stringBuilder.toString();
    }

    public synchronized String getExecutorsString(Connection conn, Long resId, boolean printOrg) {
        final StringBuilder result = new StringBuilder();

        curConn = conn;
        String s = null;
        try {
            result.append(getFinalText(getExecutors(conn, resId, printOrg),"",""));
            result.append(getFinalText(getForeignExecutors(conn, resId, false),"\n",""));
            result.append(getFinalText(getNote(conn, resId, printOrg),"\n",""));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result.toString();
    }
    
    public synchronized String getExecutorsString(Connection conn, Long resId) {
        return getExecutorsString(conn, resId, false);
    }

    private String getFinalText(String s, String prefix, String suffix){
        if(isNull(s)){
            return "";
        }
        return prefix+s+suffix;
    }

    private boolean isNull(String s){
        s= notNull(s);
        return s.equals("") || s.length()==0;
    }

    static String delSpaceBtwnIO(String fio) {
        return notNull(fio).replace(". ", ".");
    }

    static String notNull(String x) {
        return (x == null) ? "" : x.replace("null", "").replace("NULL", "");
    }

    public synchronized static String getAndSplitReport(InputStream repXml, String configFile) {
        StringBuilder result = new StringBuilder();
        String transformString = XSLTTransform.transform(repXml, configFile);
        if (transformString != null) {
            String[] reports = transformString.split(";");
            for (String report : reports) {
                if (result.length() > 0) {
                    result.append("\n");
                }
                result.append(report);
            }
        }
        return result.toString();   
    }
}
