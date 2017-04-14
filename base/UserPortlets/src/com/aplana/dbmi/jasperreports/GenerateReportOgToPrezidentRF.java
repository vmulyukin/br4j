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

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aplana.dbmi.Portal;
import com.aplana.dbmi.jasperreports.ReportOgToPrezidentRF;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

public class GenerateReportOgToPrezidentRF {
	//--------------------------------------
	final private static String QUESTION = "(������)";
	final private static String SUBQUESTION = "(���������)";
	final private static String CONFIG_FILENAME = "dbmi/jasperReports/confPortlet/parameterized/president.properties";
	final private static Log logger = LogFactory.getLog(GenerateReportOgToPrezidentRF.class);
	
	static final String PARAM_RECEIVED = "docs.received";
	static final String PARAM_REPORTED = "docs.reported";
	static final String PARAM_SENT = "docs.sent";
	static final String PARAM_IMPROPER = "docs.improper";
	static final String PARAM_REJECTION = "docs.rejection";
	static final String PARAM_NULL = "";
	
	public enum ThenaticType {
		
	    KONSTSTROY("��������������� �����"+QUESTION),
	    KONSTSTROY_SUB("��������������� �����"+SUBQUESTION),
	    OSNGOSUPR("������ ���������������� ����������"+QUESTION),
	    OSNGOSUPR_SUB("������ ���������������� ����������"+SUBQUESTION),
	    MO("������������� ���������. ������������� �����"+QUESTION),
	    MO_SUB("������������� ���������. ������������� �����"+SUBQUESTION),
	    GP("����������� �����"+QUESTION),
	    GP_SUB("����������� �����"+SUBQUESTION),
	    INDPRAVAKT("�������������� �������� ���� �� �������� ��������, �������� �����������, �����������, �����������, ���������� �������� � ���� ������"+QUESTION),
	    INDPRAVAKT_SUB("�������������� �������� ���� �� �������� ��������, �������� �����������, �����������, �����������, ���������� �������� � ���� ������"+SUBQUESTION),
	    SEMIA("�����"+QUESTION),
	    SEMIA_SUB("�����"+SUBQUESTION),
	    TRUDIZAN("���� � ��������� ���������"+QUESTION),
	    TRUDIZAN_SUB("���� � ��������� ���������"+SUBQUESTION),
	    SOCOBESPECH("���������� ����������� � ���������� �����������"+QUESTION),
	    SOCOBESPECH_SUB("���������� ����������� � ���������� �����������"+SUBQUESTION),
	    OBRAZ("�����������. �����. ��������"+QUESTION),
	    OBRAZ_SUB("�����������. �����. ��������"+SUBQUESTION),
	    ZDRAVOHRAN("���������������. ���������� �������� � �����. ������"+QUESTION),
	    ZDRAVOHRAN_SUB("���������������. ���������� �������� � �����. ������"+SUBQUESTION),
	    FINANSI("�������"+QUESTION),
	    FINANSI_SUB("�������"+SUBQUESTION),
	    HOSDEYAT("������������� ������������"+QUESTION),
	    HOSDEYAT_SUB("������������� ������������"+SUBQUESTION),
	    VNEKONODEYAT("������������������� ������������"+QUESTION),
	    VNEKONODEYAT_SUB("������������������� ������������"+SUBQUESTION),
	    PRIRODRES("��������� ������� � ������ ���������� ��������� �����"+QUESTION),
	    PRIRODRES_SUB("��������� ������� � ������ ���������� ��������� �����"+SUBQUESTION),
	    INFORMACIA("���������� � ��������������"+QUESTION),
	    INFORMACIA_SUB("���������� � ��������������"+SUBQUESTION),
	    OBORONA("�������"+QUESTION),
	    OBORONA_SUB("�������"+SUBQUESTION),
	    BEZOPASNOST("������������ � ������ ������������"+QUESTION),
	    BEZOPASNOST_SUB("������������ � ������ ������������"+SUBQUESTION),
	    UP("��������� �����. ���������� ���������"+QUESTION),
	    UP_SUB("��������� �����. ���������� ���������"+SUBQUESTION),
	    PRAVOSUD("����������"+QUESTION),
	    PRAVOSUD_SUB("����������"+SUBQUESTION),
	    PROKUR("�����������. ������ �������. ����������. ��������"+QUESTION),
	    PROKUR_SUB("�����������. ������ �������. ����������. ��������"+SUBQUESTION),
	    ZHILZAKON("�������� ���������������� � ��� ����������"+QUESTION),
	    ZHILZAKON_SUB("�������� ���������������� � ��� ����������"+SUBQUESTION),
	    ZHILFOND("������"+QUESTION),
	    ZHILFOND_SUB("������"+SUBQUESTION),
	    NEZHILFOND("������� ����"+QUESTION),
	    NEZHILFOND_SUB("������� ����"+SUBQUESTION),
	    PRAVONAZHIL("����������� ����� �� ������"+QUESTION),
	    PRAVONAZHIL_SUB("����������� ����� �� ������"+SUBQUESTION),
	    KOMUSLUGI("���������� � ����������� ������������� �������� ������ �����"+QUESTION),
	    KOMUSLUGI_SUB("���������� � ����������� ������������� �������� ������ �����"+SUBQUESTION),
	    EMPTY("EMPTY"),
	    OG_COUNT_KVARTAL("���������� ��������� �� �������"),
	    OG_COUNT_YEAR("���������� ��������� � ������ ����"),
	    Q_COUNT_YEAR("���������� �������� �� ���"),
	    EMPTY_STRING("������ ������");
	    
	    private String typeValue;
	    
	    private ThenaticType(String type) {
	        typeValue = type;
	    }
	    
	    static public ThenaticType getType(String pType) {
	        for (ThenaticType type: ThenaticType.values()) {
	            if (type.getTypeValue().equals(pType)) {
	                return type;
	            }
	        }
	        return ThenaticType.EMPTY;
	    }
	    
	    public String getTypeValue() {
	        return typeValue;
	    }
	    
	}
	
	
public enum ThenaticTypeFOIV {
		
	    KONSTSTROY("��������������� �����"),
	    OSNGOSUPR("������ ���������������� ����������"),
	    MO("������������� ���������. ������������� �����"),
	    GP("����������� �����"),
	    INDPRAVAKT("�������������� �������� ���� �� �������� ��������, �������� �����������, �����������, �����������, ���������� �������� � ���� ������"),
	    SEMIA("�����"),
	    TRUDIZAN("���� � ��������� ���������"),
	    SOCOBESPECH("���������� ����������� � ���������� �����������"),
	    OBRAZ("�����������. �����. ��������"),
	    ZDRAVOHRAN("���������������. ���������� �������� � �����. ������"),
	    FINANSI("�������"),
	    HOSDEYAT("������������� ������������"),
	    VNEKONODEYAT("������������������� ������������"),
	    PRIRODRES("��������� ������� � ������ ���������� ��������� �����"),
	    INFORMACIA("���������� � ��������������"),
	    OBORONA("�������"),
	    BEZOPASNOST("������������ � ������ ������������"),
	    UP("��������� �����. ���������� ���������"),
	    PRAVOSUD("����������"),
	    PROKUR("�����������. ������ �������. ����������. ��������"),
	    ZHILZAKON("�������� ���������������� � ��� ����������"),
	    ZHILFOND("������"),
	    NEZHILFOND("������� ����"),
	    PRAVONAZHIL("����������� ����� �� ������"),
	    KOMUSLUGI("���������� � ����������� ������������� �������� ������ �����"),
	    EMPTY("EMPTY"),
	    Q_COUNT_KVARTAL("���������� �������� �� �������"),
	    OG_COUNT_YEAR("���������� ��������� � ������ ����"),
	    Q_COUNT_YEAR("���������� �������� �� ���"),
	    EMPTY_STRING("������ ������");
	    
	    private String typeValue;
	    
	    private ThenaticTypeFOIV(String type) {
	        typeValue = type;
	    }
	    
	    static public ThenaticTypeFOIV getType(String pType) {
	        for (ThenaticTypeFOIV type: ThenaticTypeFOIV.values()) {
	            if (type.getTypeValue().equals(pType)) {
	                return type;
	            }
	        }
	        return ThenaticTypeFOIV.EMPTY;
	    }
	    
	    public String getTypeValue() {
	        return typeValue;
	    }
	    
	}
	//--------------------------------------
	
	
	public static final String TYPE_VISA = "visa";
	public static final String TYPE_SIGN = "sign";
	
	private XPathExpression recordExpression;
	
	private Connection conn = null;
	private List/*<RecordDecision>*/ records = null;
	private XPath xpath;
	
	Properties config;
	
	public GenerateReportOgToPrezidentRF() {
		XPathFactory factory = XPathFactory.newInstance();
		xpath = factory.newXPath();
		loadProperties();
	}
	public JRDataSource generate(Connection conn, Integer pYear, Integer date, String source_type, String classificator) {
		String whereString = "";
		whereString=getWhereString(source_type, classificator, conn);
		return generate(conn, getSql(pYear,date,whereString));
	}
	public JRDataSource generateFOIV(Connection conn, Integer pYear, Integer date, String source_type, String classificator) {
		String whereString = "";
		whereString=getWhereString(source_type, classificator, conn);
		return generateFOIV(conn, getSqlFOIV(pYear,date,whereString));
	}
	private Boolean empty = new Boolean(false);
	private Long kolobr = new Long(0);
	private Long kolvopr = new Long(0);
	private Long konststroy = new Long(0);
	private Long konststroy_sub = new Long(0);
	private Long osngosupr = new Long(0);
	private Long osngosupr_sub = new Long(0);
	private Long mo = new Long(0);
	private Long mo_sub = new Long(0);
	private Long gp = new Long(0);
	private Long gp_sub = new Long(0);
	private Long indpravakt = new Long(0);
	private Long indpravakt_sub = new Long(0);
	private Long semia = new Long(0);
	private Long semia_sub = new Long(0);
	private Long trudizan = new Long(0);
	private Long trudizan_sub = new Long(0);
	private Long socobespech = new Long(0);
	private Long socobespech_sub = new Long(0);
	private Long obraz = new Long(0);
	private Long obraz_sub = new Long(0);
	private Long zdravohran = new Long(0);
	private Long zdravohran_sub = new Long(0);
	private Long finansi = new Long(0);
	private Long finansi_sub = new Long(0);
	private Long hosdeyat = new Long(0);
	private Long hosdeyat_sub = new Long(0);
	private Long vnekonodeyat = new Long(0);
	private Long vnekonodeyat_sub = new Long(0);
	private Long prirodres = new Long(0);
	private Long prirodres_sub = new Long(0);
	private Long informacia = new Long(0);
	private Long informacia_sub = new Long(0);
	private Long oborona = new Long(0);
	private Long oborona_sub = new Long(0);
	private Long bezopasnost = new Long(0);
	private Long bezopasnost_sub = new Long(0);
	private Long up = new Long(0);
	private Long up_sub = new Long(0);
	private Long pravosud = new Long(0);
	private Long pravosud_sub = new Long(0);
	private Long prokur = new Long(0);
	private Long prokur_sub = new Long(0);
	private Long zhilzakon = new Long(0);
	private Long zhilzakon_sub = new Long(0);
	private Long zhilfond = new Long(0);
	private Long zhilfond_sub = new Long(0);
	private Long nezhilfond = new Long(0);
	private Long nezhilfond_sub = new Long(0);
	private Long pravonazhil = new Long(0);
	private Long pravonazhil_sub = new Long(0);
	private Long komuslugi = new Long(0);
	private Long komuslugi_sub = new Long(0);
	private Long kolobrGod = new Long(0);
	private Long kolvoprGod = new Long(0);
	
	private void summa() {
		kolvopr=konststroy+konststroy_sub+osngosupr+osngosupr_sub+mo+mo_sub+gp+gp_sub+indpravakt+indpravakt_sub+semia+semia_sub+
				trudizan+trudizan_sub+socobespech+socobespech_sub+obraz+obraz_sub+zdravohran+zdravohran_sub+finansi+finansi_sub+
				hosdeyat+hosdeyat_sub+vnekonodeyat+vnekonodeyat_sub+prirodres+prirodres_sub+informacia+informacia_sub+
				oborona+oborona_sub+bezopasnost+bezopasnost_sub+up+up_sub+pravosud+pravosud_sub+prokur+prokur_sub+zhilzakon+zhilzakon_sub+
				zhilfond+zhilfond_sub+nezhilfond+nezhilfond_sub+pravonazhil+pravonazhil_sub+komuslugi+komuslugi_sub;
	}
	
	private void summaFOIV() {
		kolobr=konststroy+osngosupr+mo+gp+indpravakt+semia+
				trudizan+socobespech+obraz+zdravohran+finansi+
				hosdeyat+vnekonodeyat+prirodres+informacia+
				oborona+bezopasnost+up+pravosud+prokur+zhilzakon+
				zhilfond+nezhilfond+pravonazhil+komuslugi;
	}
	
    private JRDataSource generate(Connection conn, String sql) {
    	
		this.conn = conn;
		List/*<RecordDecision>*/<ReportOgToPrezidentRF> records = null;
		records = new LinkedList<ReportOgToPrezidentRF>();
		try {
			Statement stmt = this.conn.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			int i=0;
			Boolean f = false;
			String name="";
			while (rs.next()) {
				int cc = rs.getInt("cc");
				if (cc > i){
					if (f){
						summa();
						ReportOgToPrezidentRF row = new ReportOgToPrezidentRF();
						fillRow(row, name);
						records.add(row);
					 f=false;
					 empty=new Boolean(false);
					 kolobr=new Long(0);
					 kolvopr=new Long(0);
					 konststroy = new Long(0);
					 konststroy_sub = new Long(0);
					 osngosupr = new Long(0);
					 osngosupr_sub = new Long(0);
					 mo = new Long(0);
					 mo_sub = new Long(0);
					 gp = new Long(0);
					 gp_sub = new Long(0);
					 indpravakt = new Long(0);
					 indpravakt_sub = new Long(0);
					 semia = new Long(0);
					 semia_sub = new Long(0);
					 trudizan = new Long(0);
					 trudizan_sub = new Long(0);
					 socobespech = new Long(0);
					 socobespech_sub = new Long(0);
					 obraz = new Long(0);
					 obraz_sub = new Long(0);
					 zdravohran = new Long(0);
					 zdravohran_sub = new Long(0);
					 finansi = new Long(0);
					 finansi_sub = new Long(0);
					 hosdeyat = new Long(0);
					 hosdeyat_sub = new Long(0);
					 vnekonodeyat = new Long(0);
					 vnekonodeyat_sub = new Long(0);
					 prirodres = new Long(0);
					 prirodres_sub = new Long(0);
					 informacia = new Long(0);
					 informacia_sub = new Long(0);
					 oborona = new Long(0);
					 oborona_sub = new Long(0);
					 bezopasnost = new Long(0);
					 bezopasnost_sub = new Long(0);
					 up = new Long(0);
					 up_sub = new Long(0);
					 pravosud = new Long(0);
					 pravosud_sub = new Long(0);
					 prokur = new Long(0);
					 prokur_sub = new Long(0);
					 zhilzakon = new Long(0);
					 zhilzakon_sub = new Long(0);
					 zhilfond = new Long(0);
					 zhilfond_sub = new Long(0);
					 nezhilfond = new Long(0);
					 nezhilfond_sub = new Long(0);
					 pravonazhil = new Long(0);
					 pravonazhil_sub = new Long(0);
					 komuslugi = new Long(0);
					 komuslugi_sub = new Long(0);
					 kolobrGod = new Long(0);
					 kolvoprGod = new Long(0);
					}
					name = rs.getString("name");
					i++;
					f=true;
				}
				String thematic = rs.getString("thematic");
				Long hz = rs.getLong("hz");
				ThenaticType thematicType = ThenaticType.getType(thematic);
			        
				    switch(thematicType) {

				        case KONSTSTROY: konststroy=hz; break;
				        case KONSTSTROY_SUB: konststroy_sub=hz; break;
				        case OSNGOSUPR: osngosupr=hz; break;
				        case OSNGOSUPR_SUB: osngosupr_sub=hz; break;
				        case MO: mo=hz; break;
				        case MO_SUB: mo_sub=hz; break;
				        case GP: gp=hz; break;
				        case GP_SUB: gp_sub=hz; break;
				        case INDPRAVAKT: indpravakt=hz; break;
				        case INDPRAVAKT_SUB: indpravakt_sub=hz; break;
				        case SEMIA: semia=hz; break;
				        case SEMIA_SUB: semia_sub=hz; break;
				        case TRUDIZAN: trudizan=hz; break;
				        case TRUDIZAN_SUB: trudizan_sub=hz; break;
				        case SOCOBESPECH: socobespech=hz; break;
				        case SOCOBESPECH_SUB: socobespech_sub=hz; break;
				        case OBRAZ: obraz=hz; break;
				        case OBRAZ_SUB: obraz_sub=hz; break;
				        case ZDRAVOHRAN: zdravohran=hz; break;
				        case ZDRAVOHRAN_SUB: zdravohran_sub=hz; break;
				        case FINANSI: finansi=hz; break;
				        case FINANSI_SUB: finansi_sub=hz; break;
				        case HOSDEYAT: hosdeyat=hz; break;
				        case HOSDEYAT_SUB: hosdeyat_sub=hz; break;
				        case VNEKONODEYAT: vnekonodeyat=hz; break;
				        case VNEKONODEYAT_SUB: vnekonodeyat_sub=hz; break;
				        case PRIRODRES: prirodres=hz; break;
				        case PRIRODRES_SUB: prirodres_sub=hz; break;
				        case INFORMACIA: informacia=hz; break;
				        case INFORMACIA_SUB: informacia_sub=hz; break;
				        case OBORONA: oborona=hz; break;
				        case OBORONA_SUB: oborona_sub=hz; break;
				        case BEZOPASNOST: bezopasnost=hz; break;
				        case BEZOPASNOST_SUB: bezopasnost_sub=hz; break;
				        case UP: up=hz; break;
				        case UP_SUB: up_sub=hz; break;
				        case PRAVOSUD: pravosud=hz; break;
				        case PRAVOSUD_SUB: pravosud_sub=hz; break;
				        case PROKUR: prokur=hz; break;
				        case PROKUR_SUB: prokur_sub=hz; break;
				        case ZHILZAKON: zhilzakon=hz; break;
				        case ZHILZAKON_SUB: zhilzakon_sub=hz; break;
				        case ZHILFOND: zhilfond=hz; break;
				        case ZHILFOND_SUB: zhilfond_sub=hz; break;
				        case NEZHILFOND: nezhilfond=hz; break;
				        case NEZHILFOND_SUB: nezhilfond_sub=hz; break;
				        case PRAVONAZHIL: pravonazhil=hz; break;
				        case PRAVONAZHIL_SUB: pravonazhil_sub=hz; break;
				        case KOMUSLUGI: komuslugi=hz; break;
				        case KOMUSLUGI_SUB: komuslugi_sub=hz; break;
				        case OG_COUNT_KVARTAL:kolobr=hz; break;
				        case OG_COUNT_YEAR: kolobrGod=hz; break;
				        case Q_COUNT_YEAR: kolvoprGod=hz; break;
				        case EMPTY_STRING: empty=true; break;
				        case EMPTY: break;

				        
				    }
				
			}
			
			summa();
			ReportOgToPrezidentRF row = new ReportOgToPrezidentRF();
			fillRow(row, name);
			records.add(row);

			//Collections.sort(records);
			//Collections.reverse(records);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			return new JRBeanCollectionDataSource(records);
		}
    }
    
private JRDataSource generateFOIV(Connection conn, String sql) {
    	
		this.conn = conn;
		List<ReportOgToPrezidentRFFOIV> records = null;
		records = new LinkedList<ReportOgToPrezidentRFFOIV>();
		try {
			Statement stmt = this.conn.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			int i=0;
			Boolean f = false;
			String name="";
			while (rs.next()) {
				int cc = rs.getInt("cc");
				if (cc > i){
					if (f){
						summaFOIV();
						ReportOgToPrezidentRFFOIV row = new ReportOgToPrezidentRFFOIV();
						fillRowFOIV(row, name);
						records.add(row);
					 f=false;
					 empty=new Boolean(false);
					 kolobr=new Long(0);
					 kolvopr=new Long(0);
					 konststroy = new Long(0);
					 osngosupr = new Long(0);
					 mo = new Long(0);
					 gp = new Long(0);
					 indpravakt = new Long(0);
					 semia = new Long(0);
					 trudizan = new Long(0);
					 socobespech = new Long(0);
					 obraz = new Long(0);
					 zdravohran = new Long(0);
					 finansi = new Long(0);
					 hosdeyat = new Long(0);
					 vnekonodeyat = new Long(0);
					 prirodres = new Long(0);
					 informacia = new Long(0);
					 oborona = new Long(0);
					 bezopasnost = new Long(0);
					 up = new Long(0);
					 pravosud = new Long(0);
					 prokur = new Long(0);
					 zhilzakon = new Long(0);
					 zhilfond = new Long(0);
					 nezhilfond = new Long(0);
					 pravonazhil = new Long(0);
					 komuslugi = new Long(0);
					 kolobrGod = new Long(0);
					 kolvoprGod = new Long(0);
					}
					name = rs.getString("name");
					i++;
					f=true;
				}
				String thematic = rs.getString("thematic");
				Long hz = rs.getLong("hz");
				ThenaticTypeFOIV thematicType = ThenaticTypeFOIV.getType(thematic);
			        
				    switch(thematicType) {

				        case KONSTSTROY: konststroy=hz; break;
				        case OSNGOSUPR: osngosupr=hz; break;
				        case MO: mo=hz; break;
				        case GP: gp=hz; break;
				        case INDPRAVAKT: indpravakt=hz; break;
				        case SEMIA: semia=hz; break;
				        case TRUDIZAN: trudizan=hz; break;
				        case SOCOBESPECH: socobespech=hz; break;
				        case OBRAZ: obraz=hz; break;
				        case ZDRAVOHRAN: zdravohran=hz; break;
				        case FINANSI: finansi=hz; break;
				        case HOSDEYAT: hosdeyat=hz; break;
				        case VNEKONODEYAT: vnekonodeyat=hz; break;
				        case PRIRODRES: prirodres=hz; break;
				        case INFORMACIA: informacia=hz; break;
				        case OBORONA: oborona=hz; break;
				        case BEZOPASNOST: bezopasnost=hz; break;
				        case UP: up=hz; break;
				        case PRAVOSUD: pravosud=hz; break;
				        case PROKUR: prokur=hz; break;
				        case ZHILZAKON: zhilzakon=hz; break;
				        case ZHILFOND: zhilfond=hz; break;
				        case NEZHILFOND: nezhilfond=hz; break;
				        case PRAVONAZHIL: pravonazhil=hz; break;
				        case KOMUSLUGI: komuslugi=hz; break;
				        case Q_COUNT_KVARTAL:kolvopr=hz; break;
				        case OG_COUNT_YEAR: kolobrGod=hz; break;
				        case Q_COUNT_YEAR: kolvoprGod=hz; break;
				        case EMPTY_STRING: empty=true; break;
				        case EMPTY: break;				        
				    }				
			}
			
			summaFOIV();
			ReportOgToPrezidentRFFOIV row = new ReportOgToPrezidentRFFOIV();
			fillRowFOIV(row, name);
			records.add(row);

			//Collections.sort(records);
			//Collections.reverse(records);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			return new JRBeanCollectionDataSource(records);
		}
    }
    
    private void fillRow(ReportOgToPrezidentRF row, String name) {
    	row.setName(name);
    	row.setEmpty(empty);
    	row.setKolobr(kolobr);
    	row.setKolvopr(kolvopr);
    	row.setKonststroy(konststroy);
    	row.setKonststroy_sub(konststroy_sub);
    	row.setOsngosupr(osngosupr);
    	row.setOsngosupr_sub(osngosupr_sub);
    	row.setMo(mo);
    	row.setMo_sub(mo_sub);
    	row.setGp(gp);
    	row.setGp_sub(gp_sub);
    	row.setIndpravakt(indpravakt);
    	row.setIndpravakt_sub(indpravakt_sub);
    	row.setSemia(semia);
    	row.setSemia_sub(semia_sub);
    	row.setTrudizan(trudizan);
    	row.setTrudizan_sub(trudizan_sub);
    	row.setSocobespech(socobespech);
    	row.setSocobespech_sub(socobespech_sub);
    	row.setObraz(obraz);
    	row.setObraz_sub(obraz_sub);
    	row.setZdravohran(zdravohran);
    	row.setZdravohran_sub(zdravohran_sub);
    	row.setFinansi(finansi);
    	row.setFinansi_sub(finansi_sub);
    	row.setHosdeyat(hosdeyat);
    	row.setHosdeyat_sub(hosdeyat_sub);
    	row.setVnekonodeyat(vnekonodeyat);
    	row.setVnekonodeyat_sub(vnekonodeyat_sub);
    	row.setPrirodres(prirodres);
    	row.setPrirodres_sub(prirodres_sub);
    	row.setInformacia(informacia);
    	row.setInformacia_sub(informacia_sub);
    	row.setOborona(oborona);
    	row.setOborona_sub(oborona_sub);
    	row.setBezopasnost(bezopasnost);
    	row.setBezopasnost_sub(bezopasnost_sub);
    	row.setUp(up);
    	row.setUp_sub(up_sub);
    	row.setPravosud(pravosud);
    	row.setPravosud_sub(pravosud_sub);
    	row.setProkur(prokur);
    	row.setProkur_sub(prokur_sub);
    	row.setZhilzakon(zhilzakon);
    	row.setZhilzakon_sub(zhilzakon_sub);
    	row.setZhilfond(zhilfond);
    	row.setZhilfond_sub(zhilfond_sub);
    	row.setNezhilfond(nezhilfond);
    	row.setNezhilfond_sub(nezhilfond_sub);
    	row.setPravonazhil(pravonazhil);
    	row.setPravonazhil_sub(pravonazhil_sub);
    	row.setKomuslugi(komuslugi);
    	row.setKomuslugi_sub(komuslugi_sub);
    	row.setKolobrGod(kolobrGod);
    	row.setKolvoprGod(kolvoprGod);
    }
    
    private void fillRowFOIV(ReportOgToPrezidentRFFOIV row, String name) {
    	row.setName(name);
    	row.setEmpty(empty);
    	row.setKolobr(kolobr);
    	row.setKolvopr(kolvopr);
    	row.setKonststroy(konststroy);
    	row.setOsngosupr(osngosupr);
    	row.setMo(mo);
    	row.setGp(gp);
    	row.setIndpravakt(indpravakt);
    	row.setSemia(semia);
    	row.setTrudizan(trudizan);
    	row.setSocobespech(socobespech);
    	row.setObraz(obraz);
    	row.setZdravohran(zdravohran);
    	row.setFinansi(finansi);
    	row.setHosdeyat(hosdeyat);
    	row.setVnekonodeyat(vnekonodeyat);
    	row.setPrirodres(prirodres);
    	row.setInformacia(informacia);
    	row.setOborona(oborona);
    	row.setBezopasnost(bezopasnost);
    	row.setUp(up);
    	row.setPravosud(pravosud);
    	row.setProkur(prokur);
    	row.setZhilzakon(zhilzakon);
    	row.setZhilfond(zhilfond);
    	row.setNezhilfond(nezhilfond);
    	row.setPravonazhil(pravonazhil);
    	row.setKomuslugi(komuslugi);
    	row.setKolobrGod(kolobrGod);
    	row.setKolvoprGod(kolvoprGod);
    }
	
    private String getWhereString(String source_type, String classificator,Connection conn){
    	if (source_type == null && classificator == null) {
    		return "WHERE c.status_id in (101, 102, 103, 301)";
    	}
    	String whereString="WHERE ";
    	
    	if(source_type != null) {
    		if(!"".equals(source_type)) {
    			whereString+="a.value_id in ("+source_type+") and ";
    		}
    	}
    	
    	if(classificator != null) {
    		String question="";
    		String thematic="";
    		String them="";
    		String unit="";
    	    
    		String[] stringCards = classificator.trim().split(",");
    		for(int i=0; i<stringCards.length; i++){
    			Integer tmplId = getTemplateId(stringCards[i], conn);
    			switch(tmplId) {
    			case 884:question+=stringCards[i]+","; break;//question ������
    			case 844:thematic+=stringCards[i]+"," ; break;//thematic �������� topic
    			case 803:them+=stringCards[i]+"," ; break;//them ���� them 
    			case 802:unit+=stringCards[i]+"," ; break;//unit ������ unit
    			}
    		}
    		if (!question.equals("")){question=question.substring(0,question.length()-1);}
    		if (!thematic.equals("")){thematic=thematic.substring(0,thematic.length()-1);}
    		if (!them.equals("")){them=them.substring(0,them.length()-1);}
    		if (!unit.equals("")){unit=unit.substring(0,unit.length()-1);}
    		
    		if (!question.equals("")){whereString+="question.number_value in ("+question+") and ";}
    		if (!thematic.equals("")){whereString+="them.number_value in ("+thematic+") and ";}
    		if (!them.equals("")){whereString+="topic.number_value in ("+them+") and ";}
    		if (!unit.equals("")){whereString+="unit.number_value in ("+unit+") and ";}
    	}
    	
    	whereString+="c.status_id in (101, 102, 103, 301)";
    	return whereString;
    }
    private Integer getTemplateId(String cardId,Connection conn){
    	this.conn = conn;
    	Integer tmplId = new Integer(1);
    	try {
			Statement stmt = this.conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT template_id as tmplid from card c WHERE c.card_id="+cardId);
			rs.next();
			tmplId = rs.getInt("tmplid");
		} catch (Exception e) {
			e.printStackTrace();
		}
			return tmplId;

    }
    
    /**
	 * Load build information from config file.
	 */
	private void loadProperties() {
		
		config = new Properties();
		try {
			config.load(Portal.getFactory().getConfigService().loadConfigFile(CONFIG_FILENAME));
		} catch (IOException ioex) {
			logger.warn("Error loading config from file \'" + CONFIG_FILENAME);
		}
	}
    
	private String getSqlFOIV(Integer pYear, Integer quarter, String whereString) {
		Integer in = new Integer(1);
    	Integer prDate=(quarter-1) > 0 ? quarter-1 : 4; //���������� �������
    	Integer prYear=(quarter-1) > 0 ? pYear : pYear-1; //��� ����������� ��������
    	Integer firstMonthCurQuarter=quarter*3-2; //������ ����� �������� �������� (quarter - ����� �������� ��������)
        StringBuilder sql = new StringBuilder();
        getEmptyRows(sql);
      
      //���������� ��������� � ������ ����
        getOGByYear(sql, prYear, pYear, firstMonthCurQuarter, 
    			prDate, quarter, whereString);
        
      //���������� ��������� �� ������� �� ��������� (��� ����)
      		getOGForQuarterByThematic(sql, prYear, pYear, firstMonthCurQuarter, 
      				prDate, quarter, whereString);
      		
      	//���-�� �������� �� ������ �� ��������� �� ���
    		getVoprByYear(sql, prYear, pYear, firstMonthCurQuarter, 
    				prDate, quarter, whereString);
        
        //���������� �������� �� �������
        getVopForQuarter(sql, prYear, pYear, firstMonthCurQuarter, 
    			prDate, quarter, whereString);
		
        
        System.out.println(sql.toString());
        return sql.toString();
	}
	
	private String getSql(Integer pYear, Integer quarter, String whereString) {
    	Integer in = new Integer(1);
    	Integer prDate=(quarter-1) > 0 ? quarter-1 : 4; //���������� �������
    	Integer prYear=(quarter-1) > 0 ? pYear : pYear-1; //��� ����������� ��������
    	Integer firstMonthCurQuarter=quarter*3-2; //������ ����� �������� �������� (quarter - ����� �������� ��������)
        StringBuilder sql = new StringBuilder();
        getEmptyRows(sql);
      	
        //���������� ��������� � ������ ����
        getOGByYear(sql, prYear, pYear, firstMonthCurQuarter, 
    			prDate, quarter, whereString);
    		
        		//���������� ��������� �� �������
        		getOGForQuarter(sql, prYear, pYear, firstMonthCurQuarter, 
        						prDate, quarter, whereString);
        		
        		//���-�� �������� �� ������ �� ��������� �� ���
        		getVoprByYear(sql, prYear, pYear, firstMonthCurQuarter, 
        				prDate, quarter, whereString);
			
			//���-�� �������� �� ������ �� ��������� �� �������
        	sql.append(
        			" Select  "+
                	" 1 AS cc "+
                	" ,'����������� ������� �� ���������� �������� ������' as name "+
                	" ,name_t.string_value || '" + QUESTION + "' as thematic "+
                	" ,count(distinct cc.card_id) as hz "+
                	" from card c "+
                	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
                	" JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
                	" 	extract('year' from reg_date.date_value) ="+ prYear.toString() +" and extract('quarter' from reg_date.date_value) ="+ prDate.toString() +" "+
                	" join card_version cv on cv.card_id = c.card_id and cv.version_date = (select coalesce ((select cv2.version_date from card_version cv2 where cv2.card_id = c.card_id " +
                	"							and cv2.version_date < date_trunc('day', TO_DATE(" + pYear.toString() + " || '-' || " + firstMonthCurQuarter.toString() + " || '-' || '1', 'YYYY-MM-DD') + interval '0 hour') + INTERVAL '1 DAY' " +
                	"							and cv2.version_date >= date_trunc('day', TO_DATE(" + pYear.toString() + " || '-' || " + firstMonthCurQuarter.toString() + " || '-' || '1', 'YYYY-MM-DD') + interval '0 hour') " +
                	"							and cv2.status_id in (101, 102, 103, 301) " +
                	"							limit 1 " +
      			    "							), " +
                	"		  					(select cv2.version_date from card_version cv2 where cv2.card_id = c.card_id " +
                	"							and cv2.version_date < date_trunc('day', TO_DATE(" + prYear.toString() + " || '-' || " + firstMonthCurQuarter.toString() + " || '-' || '1', 'YYYY-MM-DD') + interval '0 hour') " +
                	"		  					and cv2.status_id in (101, 102, 103, 301) " +
                	"		  					order by cv2.version_date desc " +
                	"		  					limit 1 " +
                	"		  					)) " +
                	" 		  																) " +
                	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
                	" JOIN attribute_value question ON question.card_id = c.card_id and question.attribute_code='ADMIN_277251' "+
                	" JOIN attribute_value topic ON topic.card_id = question.number_value and topic.attribute_code='ADMIN_278045' "+
                	" JOIN attribute_value  them ON them.card_id = topic.number_value and them.attribute_code='ADMIN_275720' "+
                	" JOIN attribute_value name_t ON name_t.card_id = them.number_value and name_t.attribute_code='NAME' "+
                	" JOIN card cc on cc.card_id = question.number_value " +
                  	whereString+
                  	" and cc.template_id = 884 " +
                	" GROUP BY name_t.string_value "+
                	"\n UNION \n"+
                	" Select  "+
                	" 1 AS cc "+
                	" ,'����������� ������� �� ���������� �������� ������' as name "+
                	" ,name_t.string_value || '" + SUBQUESTION + "' as thematic "+
                	" ,count(distinct cc.card_id) as hz "+
                	" from card c "+
                	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
                	" JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
                	" 	extract('year' from reg_date.date_value) ="+ prYear.toString() +" and extract('quarter' from reg_date.date_value) ="+ prDate.toString() +" "+
                	" join card_version cv on cv.card_id = c.card_id and cv.version_date = (select coalesce ((select cv2.version_date from card_version cv2 where cv2.card_id = c.card_id " +
                	"							and cv2.version_date < date_trunc('day', TO_DATE(" + pYear.toString() + " || '-' || " + firstMonthCurQuarter.toString() + " || '-' || '1', 'YYYY-MM-DD') + interval '0 hour') + INTERVAL '1 DAY' " +
                	"							and cv2.version_date >= date_trunc('day', TO_DATE(" + pYear.toString() + " || '-' || " + firstMonthCurQuarter.toString() + " || '-' || '1', 'YYYY-MM-DD') + interval '0 hour') " +
                	"							and cv2.status_id in (101, 102, 103, 301) " +
                	"							limit 1 " +
      			    "							), " +
                	"		  					(select cv2.version_date from card_version cv2 where cv2.card_id = c.card_id " +
                	"							and cv2.version_date < date_trunc('day', TO_DATE(" + prYear.toString() + " || '-' || " + firstMonthCurQuarter.toString() + " || '-' || '1', 'YYYY-MM-DD') + interval '0 hour') " +
                	"		  					and cv2.status_id in (101, 102, 103, 301) " +
                	"		  					order by cv2.version_date desc " +
                	"		  					limit 1 " +
                	"		  					)) " +
                	" 		  																) " +
                	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
                	" JOIN attribute_value subquestion ON subquestion.card_id = c.card_id and subquestion.attribute_code='ADMIN_277251' "+
                	" JOIN attribute_value question ON question.card_id = subquestion.number_value and question.attribute_code='JBR_QUESTION' "+
                	" JOIN attribute_value topic ON topic.card_id = question.number_value and topic.attribute_code='ADMIN_278045' "+
                	" JOIN attribute_value  them ON them.card_id = topic.number_value and them.attribute_code='ADMIN_275720' "+
                	" JOIN attribute_value name_t ON name_t.card_id = them.number_value and name_t.attribute_code='NAME' "+
                	" JOIN card cc on cc.card_id = subquestion.number_value " +
                  	whereString+
                  	" and cc.template_id = 885 " +
                	" GROUP BY name_t.string_value "+
                	"\n UNION \n"+
        	" Select  "+
        	" 2 AS cc "+
        	" ,'" + config.getProperty(PARAM_RECEIVED, PARAM_NULL) + "' as name "+
        	" ,name_t.string_value || '" + QUESTION + "' as thematic "+
        	" ,count(distinct cc.card_id) as hz "+
        	" from card c "+
        	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
        	 " JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
        	" 	extract('year' from reg_date.date_value) ="+ pYear.toString() +" and extract('quarter' from reg_date.date_value) ="+ quarter.toString() +" "+
        	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
        	" JOIN attribute_value question ON question.card_id = c.card_id and question.attribute_code='ADMIN_277251' "+
        	" JOIN attribute_value topic ON topic.card_id = question.number_value and topic.attribute_code='ADMIN_278045' "+
        	" JOIN attribute_value  them ON them.card_id = topic.number_value and them.attribute_code='ADMIN_275720' "+
        	" JOIN attribute_value name_t ON name_t.card_id = them.number_value and name_t.attribute_code='NAME' "+
        	" JOIN card cc on cc.card_id = question.number_value " +
          	whereString+
          	" and cc.template_id = 884 " +
        	" GROUP BY name_t.string_value "+
        	"\n UNION \n"+
        	" Select  "+
        	" 2 AS cc "+
        	" ,'" + config.getProperty(PARAM_RECEIVED, PARAM_NULL) + "' as name "+
        	" ,name_t.string_value || '" + SUBQUESTION + "' as thematic "+
        	" ,count(distinct cc.card_id) as hz "+
        	" from card c "+
        	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
        	 " JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
        	" 	extract('year' from reg_date.date_value) ="+ pYear.toString() +" and extract('quarter' from reg_date.date_value) ="+ quarter.toString() +" "+
        	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
        	" JOIN attribute_value subquestion ON subquestion.card_id = c.card_id and subquestion.attribute_code='ADMIN_277251' "+
        	" JOIN attribute_value question ON question.card_id = subquestion.number_value and question.attribute_code='JBR_QUESTION' "+
        	" JOIN attribute_value topic ON topic.card_id = question.number_value and topic.attribute_code='ADMIN_278045' "+
        	" JOIN attribute_value  them ON them.card_id = topic.number_value and them.attribute_code='ADMIN_275720' "+
        	" JOIN attribute_value name_t ON name_t.card_id = them.number_value and name_t.attribute_code='NAME' "+
        	" JOIN card cc on cc.card_id = subquestion.number_value " +
          	whereString+
          	" and cc.template_id = 885 " +
        	" GROUP BY name_t.string_value "+
        	"\n UNION \n"+
        	" Select  "+
        	" 3 AS cc "+
        	" ,'          � ��� ����� ���������' as name "+
        	" ,name_t.string_value || '" + QUESTION +"' as thematic "+
        	" ,count(distinct cc.card_id) as hz "+
        	" from card c "+
        	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
        	" JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
        	" 	extract('year' from reg_date.date_value) ="+ pYear.toString() +" and extract('quarter' from reg_date.date_value) ="+ quarter.toString() +" "+
        	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
        	" JOIN attribute_value format_obr ON format_obr.card_id = c.card_id and format_obr.attribute_code='JBR_ADDRESS_FORMAT' and format_obr.value_id=2037 "+
        	" JOIN attribute_value question ON question.card_id = c.card_id and question.attribute_code='ADMIN_277251' "+
        	" JOIN attribute_value topic ON topic.card_id = question.number_value and topic.attribute_code='ADMIN_278045' "+
        	" JOIN attribute_value  them ON them.card_id = topic.number_value and them.attribute_code='ADMIN_275720' "+
        	" JOIN attribute_value name_t ON name_t.card_id = them.number_value and name_t.attribute_code='NAME' "+
        	" JOIN card cc on cc.card_id = question.number_value " +
          	whereString+
          	" and cc.template_id = 884 " +
        	" GROUP BY name_t.string_value "+
        	"\n UNION \n"+
        	" Select  "+
        	" 3 AS cc "+
        	" ,'          � ��� ����� ���������' as name "+
        	" ,name_t.string_value || '" + SUBQUESTION +"' as thematic "+
        	" ,count(distinct cc.card_id) as hz "+
        	" from card c "+
        	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
        	" JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
        	" 	extract('year' from reg_date.date_value) ="+ pYear.toString() +" and extract('quarter' from reg_date.date_value) ="+ quarter.toString() +" "+
        	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
        	" JOIN attribute_value format_obr ON format_obr.card_id = c.card_id and format_obr.attribute_code='JBR_ADDRESS_FORMAT' and format_obr.value_id=2037 "+
        	" JOIN attribute_value subquestion ON subquestion.card_id = c.card_id and subquestion.attribute_code='ADMIN_277251' "+
        	" JOIN attribute_value question ON question.card_id = subquestion.number_value and question.attribute_code='JBR_QUESTION' "+
        	" JOIN attribute_value topic ON topic.card_id = question.number_value and topic.attribute_code='ADMIN_278045' "+
        	" JOIN attribute_value  them ON them.card_id = topic.number_value and them.attribute_code='ADMIN_275720' "+
        	" JOIN attribute_value name_t ON name_t.card_id = them.number_value and name_t.attribute_code='NAME' "+
        	" JOIN card cc on cc.card_id = subquestion.number_value " +
          	whereString+
          	" and cc.template_id = 885 " +
        	" GROUP BY name_t.string_value "+
        	"\n UNION \n"+
        	" Select  "+
        	" 4 AS cc "+
        	" ,'          � ��� ����� �����' as name "+
        	" ,name_t.string_value || '" + QUESTION + "' as thematic "+
        	" ,count(distinct cc.card_id) as hz "+
        	" from card c "+
        	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
        	"  JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
        		" extract('year' from reg_date.date_value) ="+ pYear.toString() +" and extract('quarter' from reg_date.date_value) ="+ quarter.toString() +" "+
        	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
        	" JOIN attribute_value format_obr ON format_obr.card_id = c.card_id and format_obr.attribute_code='JBR_ADDRESS_FORMAT' and format_obr.value_id=2036 "+
        	" JOIN attribute_value question ON question.card_id = c.card_id and question.attribute_code='ADMIN_277251' "+
        	" JOIN attribute_value topic ON topic.card_id = question.number_value and topic.attribute_code='ADMIN_278045' "+
        	" JOIN attribute_value  them ON them.card_id = topic.number_value and them.attribute_code='ADMIN_275720' "+
        	" JOIN attribute_value name_t ON name_t.card_id = them.number_value and name_t.attribute_code='NAME' "+
        	" JOIN card cc on cc.card_id = question.number_value " +
          	whereString+
          	" and cc.template_id = 884 " +
        	" GROUP BY name_t.string_value "+
        	"\n UNION \n"+
        	" Select  "+
        	" 4 AS cc "+
        	" ,'          � ��� ����� �����' as name "+
        	" ,name_t.string_value || '" + SUBQUESTION + "' as thematic "+
        	" ,count(distinct cc.card_id) as hz "+
        	" from card c "+
        	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
        	"  JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
        		" extract('year' from reg_date.date_value) ="+ pYear.toString() +" and extract('quarter' from reg_date.date_value) ="+ quarter.toString() +" "+
        	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
        	" JOIN attribute_value format_obr ON format_obr.card_id = c.card_id and format_obr.attribute_code='JBR_ADDRESS_FORMAT' and format_obr.value_id=2036 "+
        	" JOIN attribute_value subquestion ON subquestion.card_id = c.card_id and subquestion.attribute_code='ADMIN_277251' "+
        	" JOIN attribute_value question ON question.card_id = subquestion.number_value and question.attribute_code='JBR_QUESTION' "+
        	" JOIN attribute_value topic ON topic.card_id = question.number_value and topic.attribute_code='ADMIN_278045' "+
        	" JOIN attribute_value  them ON them.card_id = topic.number_value and them.attribute_code='ADMIN_275720' "+
        	" JOIN attribute_value name_t ON name_t.card_id = them.number_value and name_t.attribute_code='NAME' "+
        	" JOIN card cc on cc.card_id = subquestion.number_value " +
          	whereString+
          	" and cc.template_id = 885 " +
        	" GROUP BY name_t.string_value "+
        	"\n UNION \n"+
        	" Select  "+
        	" 5 AS cc "+
        	" ,'          � ��� ����� �����������' as name "+
        	" ,name_t.string_value || '" + QUESTION + "' as thematic "+
        	" ,count(distinct cc.card_id) as hz "+
        	" from card c "+
        	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
        	 " JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
        	" 	extract('year' from reg_date.date_value) ="+ pYear.toString() +" and extract('quarter' from reg_date.date_value) ="+ quarter.toString() +" "+
        	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
        	" JOIN attribute_value format_obr ON format_obr.card_id = c.card_id and format_obr.attribute_code='JBR_ADDRESS_FORMAT' and format_obr.value_id=2038 "+
        	" JOIN attribute_value question ON question.card_id = c.card_id and question.attribute_code='ADMIN_277251' "+
        	" JOIN attribute_value topic ON topic.card_id = question.number_value and topic.attribute_code='ADMIN_278045' "+
        	" JOIN attribute_value  them ON them.card_id = topic.number_value and them.attribute_code='ADMIN_275720' "+
        	" JOIN attribute_value name_t ON name_t.card_id = them.number_value and name_t.attribute_code='NAME' "+
        	" JOIN card cc on cc.card_id = question.number_value " +
          	whereString+
          	" and cc.template_id = 884 " +
        	" GROUP BY name_t.string_value "+
        	"\n UNION \n"+
        	" Select  "+
        	" 5 AS cc "+
        	" ,'          � ��� ����� �����������' as name "+
        	" ,name_t.string_value || '" + SUBQUESTION + "' as thematic "+
        	" ,count(distinct cc.card_id) as hz "+
        	" from card c "+
        	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
        	 " JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
        	" 	extract('year' from reg_date.date_value) ="+ pYear.toString() +" and extract('quarter' from reg_date.date_value) ="+ quarter.toString() +" "+
        	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
        	" JOIN attribute_value format_obr ON format_obr.card_id = c.card_id and format_obr.attribute_code='JBR_ADDRESS_FORMAT' and format_obr.value_id=2038 "+
        	" JOIN attribute_value subquestion ON subquestion.card_id = c.card_id and subquestion.attribute_code='ADMIN_277251' "+
        	" JOIN attribute_value question ON question.card_id = subquestion.number_value and question.attribute_code='JBR_QUESTION' "+
        	" JOIN attribute_value topic ON topic.card_id = question.number_value and topic.attribute_code='ADMIN_278045' "+
        	" JOIN attribute_value  them ON them.card_id = topic.number_value and them.attribute_code='ADMIN_275720' "+
        	" JOIN attribute_value name_t ON name_t.card_id = them.number_value and name_t.attribute_code='NAME' "+
        	" JOIN card cc on cc.card_id = subquestion.number_value " +
          	whereString+
          	" and cc.template_id = 885 " +
        	" GROUP BY name_t.string_value "+
        	"\n UNION \n"+
        	" Select  "+
        	" 7 AS cc "+
        	" ,'          " + config.getProperty(PARAM_REPORTED, PARAM_NULL) + "' as name "+
        	" ,name_t.string_value || '" + QUESTION + "' as thematic "+
        	" ,count(distinct cc.card_id) as hz "+
        	" from card c "+
        	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
        	 " JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
        	" 	extract('year' from reg_date.date_value) ="+ pYear.toString() +" and extract('quarter' from reg_date.date_value) ="+ quarter.toString() +" "+
        	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
        	" JOIN attribute_value gub ON gub.card_id = c.card_id and gub.attribute_code='JBR_IMPL_ACQUAINTERS' "+
        	" JOIN person p ON p.person_id = gub.number_value "+
        	" JOIN person_role pr ON pr.person_id = p.person_id and pr.role_code in ('JBR_BOSS') " +
        	" JOIN attribute_value question ON question.card_id = c.card_id and question.attribute_code='ADMIN_277251' "+
        	" JOIN attribute_value topic ON topic.card_id = question.number_value and topic.attribute_code='ADMIN_278045' "+
        	" JOIN attribute_value  them ON them.card_id = topic.number_value and them.attribute_code='ADMIN_275720' "+
        	" JOIN attribute_value name_t ON name_t.card_id = them.number_value and name_t.attribute_code='NAME' "+
        	" JOIN card cc on cc.card_id = question.number_value " +
          	whereString+
          	" and cc.template_id = 884 " +
        	" GROUP BY name_t.string_value "+
        	"\n UNION \n"+
        	" Select  "+
        	" 7 AS cc "+
        	" ,'          " + config.getProperty(PARAM_REPORTED, PARAM_NULL) + "' as name "+
        	" ,name_t.string_value || '" + SUBQUESTION + "' as thematic "+
        	" ,count(distinct cc.card_id) as hz "+
        	" from card c "+
        	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
        	 " JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
        	" 	extract('year' from reg_date.date_value) ="+ pYear.toString() +" and extract('quarter' from reg_date.date_value) ="+ quarter.toString() +" "+
        	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
        	" JOIN attribute_value gub ON gub.card_id = c.card_id and gub.attribute_code='JBR_IMPL_ACQUAINTERS' "+
        	" JOIN person p ON p.person_id = gub.number_value "+
        	" JOIN person_role pr ON pr.person_id = p.person_id and pr.role_code in ('JBR_BOSS') " +
        	" JOIN attribute_value subquestion ON subquestion.card_id = c.card_id and subquestion.attribute_code='ADMIN_277251' "+
        	" JOIN attribute_value question ON question.card_id = subquestion.number_value and question.attribute_code='JBR_QUESTION' "+
        	" JOIN attribute_value topic ON topic.card_id = question.number_value and topic.attribute_code='ADMIN_278045' "+
        	" JOIN attribute_value  them ON them.card_id = topic.number_value and them.attribute_code='ADMIN_275720' "+
        	" JOIN attribute_value name_t ON name_t.card_id = them.number_value and name_t.attribute_code='NAME' "+
        	" JOIN card cc on cc.card_id = subquestion.number_value " +
          	whereString+
          	" and cc.template_id = 885 " +
        	" GROUP BY name_t.string_value "+
        	"\n UNION \n"+
        	" Select "+
        	" 8 AS cc "+
        	" ,'          ����� �� ��������' as name "+
        	" ,name_t.string_value || '" + QUESTION + "' as thematic "+
        	" ,count(distinct cc.card_id) as hz "+
        	" from card c "+
        	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
        	" JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
        		" extract('year' from reg_date.date_value) ="+ pYear.toString() +" and extract('quarter' from reg_date.date_value) ="+ quarter.toString() +" "+
        	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
        	" JOIN attribute_value av_contrl ON av_contrl.card_id = c.card_id and av_contrl.attribute_code='JBR_IMPL_ONCONT' and av_contrl.value_id=1432 "+
        	" JOIN attribute_value question ON question.card_id = c.card_id and question.attribute_code='ADMIN_277251' "+
        	" JOIN attribute_value topic ON topic.card_id = question.number_value and topic.attribute_code='ADMIN_278045' "+
        	" JOIN attribute_value  them ON them.card_id = topic.number_value and them.attribute_code='ADMIN_275720' "+
        	" JOIN attribute_value name_t ON name_t.card_id = them.number_value and name_t.attribute_code='NAME' "+
        	" JOIN card cc on cc.card_id = question.number_value " +
          	whereString+
          	" and cc.template_id = 884 " +
        	" GROUP BY name_t.string_value "+
        	"\n UNION \n"+
        	" Select "+
        	" 8 AS cc "+
        	" ,'          ����� �� ��������' as name "+
        	" ,name_t.string_value || '" + SUBQUESTION + "' as thematic "+
        	" ,count(distinct cc.card_id) as hz "+
        	" from card c "+
        	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
        	" JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
        		" extract('year' from reg_date.date_value) ="+ pYear.toString() +" and extract('quarter' from reg_date.date_value) ="+ quarter.toString() +" "+
        	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
        	" JOIN attribute_value av_contrl ON av_contrl.card_id = c.card_id and av_contrl.attribute_code='JBR_IMPL_ONCONT' and av_contrl.value_id=1432 "+
        	" JOIN attribute_value subquestion ON subquestion.card_id = c.card_id and subquestion.attribute_code='ADMIN_277251' "+
        	" JOIN attribute_value question ON question.card_id = subquestion.number_value and question.attribute_code='JBR_QUESTION' "+
        	" JOIN attribute_value topic ON topic.card_id = question.number_value and topic.attribute_code='ADMIN_278045' "+
        	" JOIN attribute_value  them ON them.card_id = topic.number_value and them.attribute_code='ADMIN_275720' "+
        	" JOIN attribute_value name_t ON name_t.card_id = them.number_value and name_t.attribute_code='NAME' "+
        	" JOIN card cc on cc.card_id = subquestion.number_value " +
          	whereString+
          	" and cc.template_id = 885 " +
        	" GROUP BY name_t.string_value "+
        	"\n UNION \n"+
        	" Select "+
        	" 9 AS cc "+
        	" ,'          ����������� � ������� �� �����' as name "+
        	" ,name_t.string_value || '" + QUESTION + "' as thematic "+
        	" ,count(distinct cc.card_id) as hz "+
        	" from card c "+
        	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
        	 " JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
        	" 	extract('year' from reg_date.date_value) ="+ pYear.toString() +" and extract('quarter' from reg_date.date_value) ="+ quarter.toString() +" "+
        	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
        	" JOIN attribute_value av_contrl ON av_contrl.card_id = c.card_id and av_contrl.attribute_code='JBR_EXAMIN_PROCEDURE' and av_contrl.value_id=2046 "+
        	" JOIN attribute_value question ON question.card_id = c.card_id and question.attribute_code='ADMIN_277251' "+
        	" JOIN attribute_value topic ON topic.card_id = question.number_value and topic.attribute_code='ADMIN_278045' "+
        	" JOIN attribute_value  them ON them.card_id = topic.number_value and them.attribute_code='ADMIN_275720' "+
        	" JOIN attribute_value name_t ON name_t.card_id = them.number_value and name_t.attribute_code='NAME' "+
        	" JOIN card cc on cc.card_id = question.number_value " +
          	whereString+
          	" and cc.template_id = 884 " +
        	" GROUP BY name_t.string_value "+
        	"\n UNION \n"+
        	" Select "+
        	" 9 AS cc "+
        	" ,'          ����������� � ������� �� �����' as name "+
        	" ,name_t.string_value || '" + SUBQUESTION + "' as thematic "+
        	" ,count(distinct cc.card_id) as hz "+
        	" from card c "+
        	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
        	 " JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
        	" 	extract('year' from reg_date.date_value) ="+ pYear.toString() +" and extract('quarter' from reg_date.date_value) ="+ quarter.toString() +" "+
        	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
        	" JOIN attribute_value av_contrl ON av_contrl.card_id = c.card_id and av_contrl.attribute_code='JBR_EXAMIN_PROCEDURE' and av_contrl.value_id=2046 "+
        	" JOIN attribute_value subquestion ON subquestion.card_id = c.card_id and subquestion.attribute_code='ADMIN_277251' "+
        	" JOIN attribute_value question ON question.card_id = subquestion.number_value and question.attribute_code='JBR_QUESTION' "+
        	" JOIN attribute_value topic ON topic.card_id = question.number_value and topic.attribute_code='ADMIN_278045' "+
        	" JOIN attribute_value  them ON them.card_id = topic.number_value and them.attribute_code='ADMIN_275720' "+
        	" JOIN attribute_value name_t ON name_t.card_id = them.number_value and name_t.attribute_code='NAME' "+
        	" JOIN card cc on cc.card_id = subquestion.number_value " +
          	whereString+
          	" and cc.template_id = 885 " +
        	" GROUP BY name_t.string_value "+
        	"\n UNION \n"+
        	" Select "+
        	" 10 AS cc "+
        	" ,'          ����������� ������������' as name "+
        	" ,name_t.string_value || '" + QUESTION + "' as thematic "+
        	" ,count(distinct cc.card_id) as hz "+
        	" from card c "+
        	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
        	"  JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
        		" extract('year' from reg_date.date_value) ="+ pYear.toString() +" and extract('quarter' from reg_date.date_value) ="+ quarter.toString() +" "+
        	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
        	" JOIN attribute_value av_contrl ON av_contrl.card_id = c.card_id and av_contrl.attribute_code='JBR_EXAMIN_PROCEDURE' and av_contrl.value_id=2047 "+
        	" JOIN attribute_value question ON question.card_id = c.card_id and question.attribute_code='ADMIN_277251' "+
        	" JOIN attribute_value topic ON topic.card_id = question.number_value and topic.attribute_code='ADMIN_278045' "+
        	" JOIN attribute_value  them ON them.card_id = topic.number_value and them.attribute_code='ADMIN_275720' "+
        	" JOIN attribute_value name_t ON name_t.card_id = them.number_value and name_t.attribute_code='NAME' "+
        	" JOIN card cc on cc.card_id = question.number_value " +
          	whereString+
          	" and cc.template_id = 884 " +
        	" GROUP BY name_t.string_value "+
        	"\n UNION \n"+
        	" Select "+
        	" 10 AS cc "+
        	" ,'          ����������� ������������' as name "+
        	" ,name_t.string_value || '" + SUBQUESTION + "' as thematic "+
        	" ,count(distinct cc.card_id) as hz "+
        	" from card c "+
        	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
        	"  JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
        		" extract('year' from reg_date.date_value) ="+ pYear.toString() +" and extract('quarter' from reg_date.date_value) ="+ quarter.toString() +" "+
        	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
        	" JOIN attribute_value av_contrl ON av_contrl.card_id = c.card_id and av_contrl.attribute_code='JBR_EXAMIN_PROCEDURE' and av_contrl.value_id=2047 "+
        	" JOIN attribute_value subquestion ON subquestion.card_id = c.card_id and subquestion.attribute_code='ADMIN_277251' "+
        	" JOIN attribute_value question ON question.card_id = subquestion.number_value and question.attribute_code='JBR_QUESTION' "+
        	" JOIN attribute_value topic ON topic.card_id = question.number_value and topic.attribute_code='ADMIN_278045' "+
        	" JOIN attribute_value  them ON them.card_id = topic.number_value and them.attribute_code='ADMIN_275720' "+
        	" JOIN attribute_value name_t ON name_t.card_id = them.number_value and name_t.attribute_code='NAME' "+
        	" JOIN card cc on cc.card_id = subquestion.number_value " +
          	whereString+
          	" and cc.template_id = 885 " +
        	" GROUP BY name_t.string_value "+
        	"\n UNION \n"+
        	" Select "+
        	" 12 AS cc "+
        	" ,'          ����������' as name "+
        	" ,name_t.string_value || '" + QUESTION + "' as thematic "+
        	" ,count(distinct cc.card_id) as hz "+
        	" from card c "+
        	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
        	 " JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
        	" 	extract('year' from reg_date.date_value) ="+ pYear.toString() +" and extract('quarter' from reg_date.date_value) ="+ quarter.toString() +" "+
        	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
        	" JOIN attribute_value av_contrl ON av_contrl.card_id = c.card_id and av_contrl.attribute_code='ADMIN_283929' and av_contrl.value_id in (Select value_id from values_list where value_rus='����������' or "+ " value_rus='���� �������') "+
        	" JOIN attribute_value question ON question.card_id = c.card_id and question.attribute_code='ADMIN_277251' "+
        	" JOIN attribute_value topic ON topic.card_id = question.number_value and topic.attribute_code='ADMIN_278045' "+
        	" JOIN attribute_value  them ON them.card_id = topic.number_value and them.attribute_code='ADMIN_275720' "+
        	" JOIN attribute_value name_t ON name_t.card_id = them.number_value and name_t.attribute_code='NAME' "+
        	" JOIN card cc on cc.card_id = question.number_value " +
          	whereString+
          	" and cc.template_id = 884 " +
        	" GROUP BY name_t.string_value "+
        	"\n UNION \n"+
        	" Select "+
        	" 12 AS cc "+
        	" ,'          ����������' as name "+
        	" ,name_t.string_value || '" + SUBQUESTION + "' as thematic "+
        	" ,count(distinct cc.card_id) as hz "+
        	" from card c "+
        	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
        	 " JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
        	" 	extract('year' from reg_date.date_value) ="+ pYear.toString() +" and extract('quarter' from reg_date.date_value) ="+ quarter.toString() +" "+
        	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
        	" JOIN attribute_value av_contrl ON av_contrl.card_id = c.card_id and av_contrl.attribute_code='ADMIN_283929' and av_contrl.value_id in (Select value_id from values_list where value_rus='����������' or "+ " value_rus='���� �������') "+
        	" JOIN attribute_value subquestion ON subquestion.card_id = c.card_id and subquestion.attribute_code='ADMIN_277251' "+
        	" JOIN attribute_value question ON question.card_id = subquestion.number_value and question.attribute_code='JBR_QUESTION' "+
        	" JOIN attribute_value topic ON topic.card_id = question.number_value and topic.attribute_code='ADMIN_278045' "+
        	" JOIN attribute_value  them ON them.card_id = topic.number_value and them.attribute_code='ADMIN_275720' "+
        	" JOIN attribute_value name_t ON name_t.card_id = them.number_value and name_t.attribute_code='NAME' "+
        	" JOIN card cc on cc.card_id = subquestion.number_value " +
          	whereString+
          	" and cc.template_id = 885 " +
        	" GROUP BY name_t.string_value "+
        	"\n UNION \n"+
        	" Select "+
        	" 13 AS cc "+
        	" ,'          � ��� ����� ���� �������' as name "+
        	" ,name_t.string_value || '" + QUESTION + "' as thematic "+
        	" ,count(distinct cc.card_id) as hz "+
        	" from card c "+
        	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
        	 " JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
        	" 	extract('year' from reg_date.date_value) ="+ pYear.toString() +" and extract('quarter' from reg_date.date_value) ="+ quarter.toString() +" "+
        	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
        	" JOIN attribute_value av_contrl ON av_contrl.card_id = c.card_id and av_contrl.attribute_code='ADMIN_283929' and av_contrl.value_id in (Select value_id from values_list where value_rus='���� �������') "+
        	" JOIN attribute_value question ON question.card_id = c.card_id and question.attribute_code='ADMIN_277251' "+
        	" JOIN attribute_value topic ON topic.card_id = question.number_value and topic.attribute_code='ADMIN_278045' "+
        	" JOIN attribute_value  them ON them.card_id = topic.number_value and them.attribute_code='ADMIN_275720' "+
        	" JOIN attribute_value name_t ON name_t.card_id = them.number_value and name_t.attribute_code='NAME' "+
        	" JOIN card cc on cc.card_id = question.number_value " +
          	whereString+
          	" and cc.template_id = 884 " +
        	" GROUP BY name_t.string_value "+
        	"\n UNION \n"+
        	" Select "+
        	" 13 AS cc "+
        	" ,'          � ��� ����� ���� �������' as name "+
        	" ,name_t.string_value || '" + SUBQUESTION  + "' as thematic "+
        	" ,count(distinct cc.card_id) as hz "+
        	" from card c "+
        	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
        	 " JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
        	" 	extract('year' from reg_date.date_value) ="+ pYear.toString() +" and extract('quarter' from reg_date.date_value) ="+ quarter.toString() +" "+
        	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
        	" JOIN attribute_value av_contrl ON av_contrl.card_id = c.card_id and av_contrl.attribute_code='ADMIN_283929' and av_contrl.value_id in (Select value_id from values_list where value_rus='���� �������') "+
        	" JOIN attribute_value subquestion ON subquestion.card_id = c.card_id and subquestion.attribute_code='ADMIN_277251' "+
        	" JOIN attribute_value question ON question.card_id = subquestion.number_value and question.attribute_code='JBR_QUESTION' "+
        	" JOIN attribute_value topic ON topic.card_id = question.number_value and topic.attribute_code='ADMIN_278045' "+
        	" JOIN attribute_value  them ON them.card_id = topic.number_value and them.attribute_code='ADMIN_275720' "+
        	" JOIN attribute_value name_t ON name_t.card_id = them.number_value and name_t.attribute_code='NAME' "+
        	" JOIN card cc on cc.card_id = subquestion.number_value " +
          	whereString+
          	" and cc.template_id = 885 " +
        	" GROUP BY name_t.string_value "+
        	"\n UNION \n"+
        	" Select "+
        	" 14 AS cc "+
        	" ,'          ����������' as name "+
        	" ,name_t.string_value || '" + QUESTION + "' as thematic "+
        	" ,count(distinct cc.card_id) as hz "+
        	" from card c "+
        	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
        	"  JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
        		" extract('year' from reg_date.date_value) ="+ pYear.toString() +" and extract('quarter' from reg_date.date_value) ="+ quarter.toString() +" "+
        	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
        	" JOIN attribute_value av_contrl ON av_contrl.card_id = c.card_id and av_contrl.attribute_code='ADMIN_283929' and av_contrl.value_id in (Select value_id from values_list where value_rus='����������') "+
        	" JOIN attribute_value question ON question.card_id = c.card_id and question.attribute_code='ADMIN_277251' "+
        	" JOIN attribute_value topic ON topic.card_id = question.number_value and topic.attribute_code='ADMIN_278045' "+
        	" JOIN attribute_value  them ON them.card_id = topic.number_value and them.attribute_code='ADMIN_275720' "+
        	" JOIN attribute_value name_t ON name_t.card_id = them.number_value and name_t.attribute_code='NAME' "+
        	" JOIN card cc on cc.card_id = question.number_value " +
          	whereString+
          	" and cc.template_id = 884 " +
        	" GROUP BY name_t.string_value "+
        	"\n UNION \n"+
        	" Select "+
        	" 14 AS cc "+
        	" ,'          ����������' as name "+
        	" ,name_t.string_value || '" + SUBQUESTION + "' as thematic "+
        	" ,count(distinct cc.card_id) as hz "+
        	" from card c "+
        	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
        	"  JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
        		" extract('year' from reg_date.date_value) ="+ pYear.toString() +" and extract('quarter' from reg_date.date_value) ="+ quarter.toString() +" "+
        	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
        	" JOIN attribute_value av_contrl ON av_contrl.card_id = c.card_id and av_contrl.attribute_code='ADMIN_283929' and av_contrl.value_id in (Select value_id from values_list where value_rus='����������') "+
        	" JOIN attribute_value subquestion ON subquestion.card_id = c.card_id and subquestion.attribute_code='ADMIN_277251' "+
        	" JOIN attribute_value question ON question.card_id = subquestion.number_value and question.attribute_code='JBR_QUESTION' "+
        	" JOIN attribute_value topic ON topic.card_id = question.number_value and topic.attribute_code='ADMIN_278045' "+
        	" JOIN attribute_value  them ON them.card_id = topic.number_value and them.attribute_code='ADMIN_275720' "+
        	" JOIN attribute_value name_t ON name_t.card_id = them.number_value and name_t.attribute_code='NAME' "+
        	" JOIN card cc on cc.card_id = subquestion.number_value " +
          	whereString+
          	" and cc.template_id = 885 " +
        	" GROUP BY name_t.string_value "+
        	"\n UNION \n"+
        	" Select "+
        	" 15 AS cc "+
        	" ,'          �� ����������' as name "+
        	" ,name_t.string_value || '" + QUESTION + "' as thematic "+
        	" ,count(distinct cc.card_id) as hz "+
        	" from card c "+
        	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
        	 " JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
        	" 	extract('year' from reg_date.date_value) ="+ pYear.toString() +" and extract('quarter' from reg_date.date_value) ="+ quarter.toString() +" "+
        	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
        	" JOIN attribute_value av_contrl ON av_contrl.card_id = c.card_id and av_contrl.attribute_code='ADMIN_283929' and av_contrl.value_id in (Select value_id from values_list where value_rus='�� ����������') "+
        	" JOIN attribute_value question ON question.card_id = c.card_id and question.attribute_code='ADMIN_277251' "+
        	" JOIN attribute_value topic ON topic.card_id = question.number_value and topic.attribute_code='ADMIN_278045' "+
        	" JOIN attribute_value  them ON them.card_id = topic.number_value and them.attribute_code='ADMIN_275720' "+
        	" JOIN attribute_value name_t ON name_t.card_id = them.number_value and name_t.attribute_code='NAME' "+
        	" JOIN card cc on cc.card_id = question.number_value " +
          	whereString+
          	" and cc.template_id = 884 " +
        	" GROUP BY name_t.string_value "+
        	"\n UNION \n"+
        	" Select "+
        	" 15 AS cc "+
        	" ,'          �� ����������' as name "+
        	" ,name_t.string_value || '" + SUBQUESTION + "' as thematic "+
        	" ,count(distinct cc.card_id) as hz "+
        	" from card c "+
        	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
        	 " JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
        	" 	extract('year' from reg_date.date_value) ="+ pYear.toString() +" and extract('quarter' from reg_date.date_value) ="+ quarter.toString() +" "+
        	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
        	" JOIN attribute_value av_contrl ON av_contrl.card_id = c.card_id and av_contrl.attribute_code='ADMIN_283929' and av_contrl.value_id in (Select value_id from values_list where value_rus='�� ����������') "+
        	" JOIN attribute_value subquestion ON subquestion.card_id = c.card_id and subquestion.attribute_code='ADMIN_277251' "+
        	" JOIN attribute_value question ON question.card_id = subquestion.number_value and question.attribute_code='JBR_QUESTION' "+
        	" JOIN attribute_value topic ON topic.card_id = question.number_value and topic.attribute_code='ADMIN_278045' "+
        	" JOIN attribute_value  them ON them.card_id = topic.number_value and them.attribute_code='ADMIN_275720' "+
        	" JOIN attribute_value name_t ON name_t.card_id = them.number_value and name_t.attribute_code='NAME' "+
        	" JOIN card cc on cc.card_id = subquestion.number_value " +
          	whereString+
          	" and cc.template_id = 885 " +
        	" GROUP BY name_t.string_value "+
        	"\n UNION \n"+
        	" Select "+
        	" 16 AS cc "+
        	" ,'" + config.getProperty(PARAM_SENT, PARAM_NULL) + "' as name "+
        	" ,name_t.string_value || '" + QUESTION + "' as thematic "+
        	" ,count(distinct cc.card_id) as hz "+
        	" from card c "+
        	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
        	"  JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
        		" extract('year' from reg_date.date_value) ="+ pYear.toString() +" and extract('quarter' from reg_date.date_value) ="+ quarter.toString() +" "+
        	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
        	" JOIN attribute_value relate_doc ON relate_doc.card_id = c.card_id and relate_doc.attribute_code='JBR_DOCL_RELATDOC' "+
        	" JOIN card relate_doc_card on relate_doc_card.card_id=relate_doc.number_value and relate_doc_card.template_id=364 "+
        		" and relate_doc_card.status_id=101 "+
        	" JOIN attribute_value gub ON gub.card_id = relate_doc_card.card_id and gub.attribute_code='JBR_INFD_SIGNATORY' "+
        	" JOIN person p ON p.person_id = gub.number_value "+
        	" JOIN person_role pr ON pr.person_id = p.person_id and pr.role_code in ('JBR_BOSS','JBR_DEPUTY_MINISTER') " +
        	" JOIN attribute_value question ON question.card_id = c.card_id and question.attribute_code='ADMIN_277251' "+
        	" JOIN attribute_value topic ON topic.card_id = question.number_value and topic.attribute_code='ADMIN_278045' "+
        	" JOIN attribute_value  them ON them.card_id = topic.number_value and them.attribute_code='ADMIN_275720' "+
        	" JOIN attribute_value name_t ON name_t.card_id = them.number_value and name_t.attribute_code='NAME' "+
        	" JOIN card cc on cc.card_id = question.number_value " +
          	whereString+
          	" and cc.template_id = 884 " +
        	" GROUP BY name_t.string_value "+
        	"\n UNION \n"+
        	" Select "+
        	" 16 AS cc "+
        	" ,'" + config.getProperty(PARAM_SENT, PARAM_NULL) + "' as name "+
        	" ,name_t.string_value || '" + SUBQUESTION + "' as thematic "+
        	" ,count(distinct cc.card_id) as hz "+
        	" from card c "+
        	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
        	"  JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
        		" extract('year' from reg_date.date_value) ="+ pYear.toString() +" and extract('quarter' from reg_date.date_value) ="+ quarter.toString() +" "+
        	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
        	" JOIN attribute_value relate_doc ON relate_doc.card_id = c.card_id and relate_doc.attribute_code='JBR_DOCL_RELATDOC' "+
        	" JOIN card relate_doc_card on relate_doc_card.card_id=relate_doc.number_value and relate_doc_card.template_id=364 "+
        		" and relate_doc_card.status_id=101 "+
        	" JOIN attribute_value gub ON gub.card_id = relate_doc_card.card_id and gub.attribute_code='JBR_INFD_SIGNATORY' "+
        	" JOIN person p ON p.person_id = gub.number_value "+
        	" JOIN person_role pr ON pr.person_id = p.person_id and pr.role_code in ('JBR_BOSS','JBR_DEPUTY_MINISTER') " +
        	" JOIN attribute_value subquestion ON subquestion.card_id = c.card_id and subquestion.attribute_code='ADMIN_277251' "+
        	" JOIN attribute_value question ON question.card_id = subquestion.number_value and question.attribute_code='JBR_QUESTION' "+
        	" JOIN attribute_value topic ON topic.card_id = question.number_value and topic.attribute_code='ADMIN_278045' "+
        	" JOIN attribute_value  them ON them.card_id = topic.number_value and them.attribute_code='ADMIN_275720' "+
        	" JOIN attribute_value name_t ON name_t.card_id = them.number_value and name_t.attribute_code='NAME' "+
        	" JOIN card cc on cc.card_id = subquestion.number_value " +
          	whereString+
          	" and cc.template_id = 885 " +
        	" GROUP BY name_t.string_value "+
        	"\n UNION \n"+
        	" Select  "+
        	" 17 AS cc "+
        	" ,'���������� ���������, ������������� ��������� � �������� �������� ��������������' as name "+
        	" ,name_t.string_value || '" + QUESTION + "' as thematic "+
        	" ,count(distinct cc.card_id) as hz "+
        	" from card c "+
        	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
        	 " JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
        	" 	extract('year' from reg_date.date_value) ="+ pYear.toString() +" and extract('quarter' from reg_date.date_value) ="+ quarter.toString() +" "+
        	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
        	" JOIN attribute_value cont ON cont.card_id = c.card_id and cont.attribute_code='JBR_EXAMIN_PROCEDURE' and cont.value_id=2049 "+
        	" JOIN attribute_value question ON question.card_id = c.card_id and question.attribute_code='ADMIN_277251' "+
        	" JOIN attribute_value topic ON topic.card_id = question.number_value and topic.attribute_code='ADMIN_278045' "+
        	" JOIN attribute_value  them ON them.card_id = topic.number_value and them.attribute_code='ADMIN_275720' "+
        	" JOIN attribute_value name_t ON name_t.card_id = them.number_value and name_t.attribute_code='NAME' "+
        	" JOIN card cc on cc.card_id = question.number_value " +
          	whereString+
          	" and cc.template_id = 884 " +
        	" GROUP BY name_t.string_value "+
        	"\n UNION \n"+
        	" Select  "+
        	" 17 AS cc "+
        	" ,'���������� ���������, ������������� ��������� � �������� �������� ��������������' as name "+
        	" ,name_t.string_value || '" + SUBQUESTION + "' as thematic "+
        	" ,count(distinct cc.card_id) as hz "+
        	" from card c "+
        	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
        	 " JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
        	" 	extract('year' from reg_date.date_value) ="+ pYear.toString() +" and extract('quarter' from reg_date.date_value) ="+ quarter.toString() +" "+
        	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
        	" JOIN attribute_value cont ON cont.card_id = c.card_id and cont.attribute_code='JBR_EXAMIN_PROCEDURE' and cont.value_id=2049 "+
        	" JOIN attribute_value subquestion ON subquestion.card_id = c.card_id and subquestion.attribute_code='ADMIN_277251' "+
        	" JOIN attribute_value question ON question.card_id = subquestion.number_value and question.attribute_code='JBR_QUESTION' "+
        	" JOIN attribute_value topic ON topic.card_id = question.number_value and topic.attribute_code='ADMIN_278045' "+
        	" JOIN attribute_value  them ON them.card_id = topic.number_value and them.attribute_code='ADMIN_275720' "+
        	" JOIN attribute_value name_t ON name_t.card_id = them.number_value and name_t.attribute_code='NAME' "+
        	" JOIN card cc on cc.card_id = subquestion.number_value " +
          	whereString+
          	" and cc.template_id = 885 " +
        	" GROUP BY name_t.string_value "+
        	"\n UNION \n"+
        	" Select  "+
        	" 18 AS cc "+
        	" ,'���������� ���������, ������������� ��������� � ���������������� �������� ����������� ������� �������������� ������' as name "+
        	" ,name_t.string_value || '" + QUESTION + "' as thematic "+
        	" ,count(distinct cc.card_id) as hz "+
        	" from card c "+
        	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
        	"  JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
        		" extract('year' from reg_date.date_value) ="+ pYear.toString() +" and extract('quarter' from reg_date.date_value) ="+ quarter.toString() +" "+
        	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
        	" JOIN attribute_value cont ON cont.card_id = c.card_id and cont.attribute_code='JBR_EXAMIN_PROCEDURE' and cont.value_id=2048 "+
        	" JOIN attribute_value question ON question.card_id = c.card_id and question.attribute_code='ADMIN_277251' "+
        	" JOIN attribute_value topic ON topic.card_id = question.number_value and topic.attribute_code='ADMIN_278045' "+
        	" JOIN attribute_value  them ON them.card_id = topic.number_value and them.attribute_code='ADMIN_275720' "+
        	" JOIN attribute_value name_t ON name_t.card_id = them.number_value and name_t.attribute_code='NAME' "+
        	" JOIN card cc on cc.card_id = question.number_value " +
          	whereString+
          	" and cc.template_id = 884 " +
        	" GROUP BY name_t.string_value "+
        	"\n UNION \n"+
        	" Select  "+
        	" 18 AS cc "+
        	" ,'���������� ���������, ������������� ��������� � ���������������� �������� ����������� ������� �������������� ������' as name "+
        	" ,name_t.string_value || '" + SUBQUESTION + "' as thematic "+
        	" ,count(distinct cc.card_id) as hz "+
        	" from card c "+
        	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
        	"  JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
        		" extract('year' from reg_date.date_value) ="+ pYear.toString() +" and extract('quarter' from reg_date.date_value) ="+ quarter.toString() +" "+
        	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
        	" JOIN attribute_value cont ON cont.card_id = c.card_id and cont.attribute_code='JBR_EXAMIN_PROCEDURE' and cont.value_id=2048 "+
        	" JOIN attribute_value subquestion ON subquestion.card_id = c.card_id and subquestion.attribute_code='ADMIN_277251' "+
        	" JOIN attribute_value question ON question.card_id = subquestion.number_value and question.attribute_code='JBR_QUESTION' "+
        	" JOIN attribute_value topic ON topic.card_id = question.number_value and topic.attribute_code='ADMIN_278045' "+
        	" JOIN attribute_value  them ON them.card_id = topic.number_value and them.attribute_code='ADMIN_275720' "+
        	" JOIN attribute_value name_t ON name_t.card_id = them.number_value and name_t.attribute_code='NAME' "+
        	" JOIN card cc on cc.card_id = subquestion.number_value " +
          	whereString+
          	" and cc.template_id = 885 " +
        	" GROUP BY name_t.string_value "+
        	"\n UNION \n"+
        	" Select  "+
        	" 19 AS cc "+
        	" ,'���������� �����, � ������� ������������� ����������� �����' as name "+
        	" ,name_t.string_value || '" + QUESTION + "' as thematic "+
        	" ,count(distinct cc.card_id) as hz "+
        	" from card c "+
        	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
        	 " JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
        	" 	extract('year' from reg_date.date_value) ="+ pYear.toString() +" and extract('quarter' from reg_date.date_value) ="+ quarter.toString() +" "+
        	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
        	" JOIN attribute_value fav_d ON fav_d.card_id = c.card_id and fav_d.attribute_code='JBR_ADDRESS_FORMAT' and fav_d.value_id=2036 "+
        	" JOIN attribute_value d ON d.card_id = c.card_id and d.attribute_code='ADMIN_283926' "+
        	" JOIN attribute_value fav_des ON fav_des.card_id = d.number_value and fav_des.attribute_code='NAME' and  fav_des.string_value='����� �������������' "+
        	" JOIN attribute_value question ON question.card_id = c.card_id and question.attribute_code='ADMIN_277251' "+
        	" JOIN attribute_value topic ON topic.card_id = question.number_value and topic.attribute_code='ADMIN_278045' "+
        	" JOIN attribute_value  them ON them.card_id = topic.number_value and them.attribute_code='ADMIN_275720' "+
        	" JOIN attribute_value name_t ON name_t.card_id = them.number_value and name_t.attribute_code='NAME' "+
        	" JOIN card cc on cc.card_id = question.number_value " +
          	whereString+
          	" and cc.template_id = 884 " +
        	" GROUP BY name_t.string_value "+
        	"\n UNION \n"+
        	" Select  "+
        	" 19 AS cc "+
        	" ,'���������� �����, � ������� ������������� ����������� �����' as name "+
        	" ,name_t.string_value || '" + SUBQUESTION + "' as thematic "+
        	" ,count(distinct cc.card_id) as hz "+
        	" from card c "+
        	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
        	 " JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
        	" 	extract('year' from reg_date.date_value) ="+ pYear.toString() +" and extract('quarter' from reg_date.date_value) ="+ quarter.toString() +" "+
        	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
        	" JOIN attribute_value fav_d ON fav_d.card_id = c.card_id and fav_d.attribute_code='JBR_ADDRESS_FORMAT' and fav_d.value_id=2036 "+
        	" JOIN attribute_value d ON d.card_id = c.card_id and d.attribute_code='ADMIN_283926' "+
        	" JOIN attribute_value fav_des ON fav_des.card_id = d.number_value and fav_des.attribute_code='NAME' and  fav_des.string_value='����� �������������' "+
        	" JOIN attribute_value subquestion ON subquestion.card_id = c.card_id and subquestion.attribute_code='ADMIN_277251' "+
        	" JOIN attribute_value question ON question.card_id = subquestion.number_value and question.attribute_code='JBR_QUESTION' "+
        	" JOIN attribute_value topic ON topic.card_id = question.number_value and topic.attribute_code='ADMIN_278045' "+
        	" JOIN attribute_value  them ON them.card_id = topic.number_value and them.attribute_code='ADMIN_275720' "+
        	" JOIN attribute_value name_t ON name_t.card_id = them.number_value and name_t.attribute_code='NAME' "+
        	" JOIN card cc on cc.card_id = subquestion.number_value " +
          	whereString+
          	" and cc.template_id = 885 " +
        	" GROUP BY name_t.string_value "+
        	"\n UNION \n"+
        	" Select  "+
        	" 20 AS cc "+
        	" ,'���������� �����, �� ����������� ������������ ������� �������� � ��������� ���� ������� ��������' as name "+
        	" ,name_t.string_value || '" + QUESTION + "' as thematic "+
        	" ,count(distinct cc.card_id) as hz "+
        	" from card c "+
        	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
        	"  JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
        		" extract('year' from reg_date.date_value) ="+ pYear.toString() +" and extract('quarter' from reg_date.date_value) ="+ quarter.toString() +" "+
        	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
        	" JOIN attribute_value fav_d ON fav_d.card_id = c.card_id and fav_d.attribute_code='JBR_ADDRESS_FORMAT' and fav_d.value_id=2036 "+
        	" JOIN attribute_value d ON d.card_id = c.card_id and d.attribute_code='ADMIN_283926' "+
        	" JOIN attribute_value fav_des ON fav_des.card_id = d.number_value and fav_des.attribute_code='NAME' and  fav_des.string_value='�������� ��������' "+
        	" JOIN attribute_value question ON question.card_id = c.card_id and question.attribute_code='ADMIN_277251' "+
        	" JOIN attribute_value topic ON topic.card_id = question.number_value and topic.attribute_code='ADMIN_278045' "+
        	" JOIN attribute_value  them ON them.card_id = topic.number_value and them.attribute_code='ADMIN_275720' "+
        	" JOIN attribute_value name_t ON name_t.card_id = them.number_value and name_t.attribute_code='NAME' "+
        	" JOIN card cc on cc.card_id = question.number_value " +
          	whereString+
          	" and cc.template_id = 884 " +
        	" GROUP BY name_t.string_value "+
        	"\n UNION \n"+
        	" Select  "+
        	" 20 AS cc "+
        	" ,'���������� �����, �� ����������� ������������ ������� �������� � ��������� ���� ������� ��������' as name "+
        	" ,name_t.string_value || '" + SUBQUESTION + "' as thematic "+
        	" ,count(distinct cc.card_id) as hz "+
        	" from card c "+
        	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
        	"  JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
        		" extract('year' from reg_date.date_value) ="+ pYear.toString() +" and extract('quarter' from reg_date.date_value) ="+ quarter.toString() +" "+
        	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
        	" JOIN attribute_value fav_d ON fav_d.card_id = c.card_id and fav_d.attribute_code='JBR_ADDRESS_FORMAT' and fav_d.value_id=2036 "+
        	" JOIN attribute_value d ON d.card_id = c.card_id and d.attribute_code='ADMIN_283926' "+
        	" JOIN attribute_value fav_des ON fav_des.card_id = d.number_value and fav_des.attribute_code='NAME' and  fav_des.string_value='�������� ��������' "+
        	" JOIN attribute_value subquestion ON subquestion.card_id = c.card_id and subquestion.attribute_code='ADMIN_277251' "+
        	" JOIN attribute_value question ON question.card_id = subquestion.number_value and question.attribute_code='JBR_QUESTION' "+
        	" JOIN attribute_value topic ON topic.card_id = question.number_value and topic.attribute_code='ADMIN_278045' "+
        	" JOIN attribute_value  them ON them.card_id = topic.number_value and them.attribute_code='ADMIN_275720' "+
        	" JOIN attribute_value name_t ON name_t.card_id = them.number_value and name_t.attribute_code='NAME' "+
        	" JOIN card cc on cc.card_id = subquestion.number_value " +
          	whereString+
          	" and cc.template_id = 885 " +
        	" GROUP BY name_t.string_value "+
        	"\n UNION \n"+
        	" Select  "+
        	" 21 AS cc "+
        	" ,'����������� ��������� � ���������� �����' as name "+
        	" ,name_t.string_value || '" + QUESTION + "' as thematic "+
        	" ,count(distinct cc.card_id) as hz "+
        	" from card c "+
        	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
        	 " JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
        	" 	extract('year' from reg_date.date_value) ="+ pYear.toString() +" and extract('quarter' from reg_date.date_value) ="+ quarter.toString() +" "+
        	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
        	" JOIN attribute_value decision ON decision.card_id=c.card_id and decision.attribute_code='ADMIN_283929' and decision.value_id is not null "+
        	" JOIN attribute_value control_date ON control_date.card_id = c.card_id and control_date.attribute_code='JBR_IMPL_DEADLINE'  and control_date.date_value  is not null "+
        	" JOIN attribute_value answer_date ON answer_date.card_id=c.card_id and answer_date.attribute_code='ADMIN_220912' and answer_date.date_value is not null "+
        	   " and date_trunc('day', answer_date.date_value)>date_trunc('day', control_date.date_value) "+
           	" JOIN attribute_value question ON question.card_id = c.card_id and question.attribute_code='ADMIN_277251' "+
        	" JOIN attribute_value topic ON topic.card_id = question.number_value and topic.attribute_code='ADMIN_278045' "+
        	" JOIN attribute_value  them ON them.card_id = topic.number_value and them.attribute_code='ADMIN_275720' "+
        	" JOIN attribute_value name_t ON name_t.card_id = them.number_value and name_t.attribute_code='NAME' "+
        	" JOIN card cc on cc.card_id = question.number_value " +
          	whereString+
          	" and cc.template_id = 884 " +
        	" GROUP BY name_t.string_value "+
        	"\n UNION \n"+
        	" Select  "+
        	" 21 AS cc "+
        	" ,'����������� ��������� � ���������� �����' as name "+
        	" ,name_t.string_value || '" + SUBQUESTION + "' as thematic "+
        	" ,count(distinct cc.card_id) as hz "+
        	" from card c "+
        	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
        	 " JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
        	" 	extract('year' from reg_date.date_value) ="+ pYear.toString() +" and extract('quarter' from reg_date.date_value) ="+ quarter.toString() +" "+
        	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
        	" JOIN attribute_value decision ON decision.card_id=c.card_id and decision.attribute_code='ADMIN_283929' and decision.value_id is not null "+
        	" JOIN attribute_value control_date ON control_date.card_id = c.card_id and control_date.attribute_code='JBR_IMPL_DEADLINE'  and control_date.date_value  is not null "+
        	" JOIN attribute_value answer_date ON answer_date.card_id=c.card_id and answer_date.attribute_code='ADMIN_220912' and answer_date.date_value is not null "+
        	   " and date_trunc('day', answer_date.date_value)>date_trunc('day', control_date.date_value) "+
           	" JOIN attribute_value subquestion ON subquestion.card_id = c.card_id and subquestion.attribute_code='ADMIN_277251' "+
           	" JOIN attribute_value question ON question.card_id = subquestion.number_value and question.attribute_code='JBR_QUESTION' "+
        	" JOIN attribute_value topic ON topic.card_id = question.number_value and topic.attribute_code='ADMIN_278045' "+
        	" JOIN attribute_value  them ON them.card_id = topic.number_value and them.attribute_code='ADMIN_275720' "+
        	" JOIN attribute_value name_t ON name_t.card_id = them.number_value and name_t.attribute_code='NAME' "+
        	" JOIN card cc on cc.card_id = subquestion.number_value " +
          	whereString+
          	" and cc.template_id = 885 " +
        	" GROUP BY name_t.string_value "+
        	"\n UNION \n"+
        	" Select  "+
        	" 22 AS cc "+
        	" ,'��������� �� ������������ �� 1 ����� ������ ���������� �� �������� ��������, ����������� � �������� �������' as name "+
        	" ,name_t.string_value || '" + QUESTION + "' as thematic "+
        	" ,count(distinct cc.card_id) as hz "+
        	" from card c "+
        	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
        	 " JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
        	" 	extract('year' from reg_date.date_value) ="+ pYear.toString() +" and extract('quarter' from reg_date.date_value) ="+ quarter.toString() +" "+
        	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
        	" JOIN attribute_value control_date ON control_date.card_id = c.card_id and control_date.attribute_code='JBR_IMPL_DEADLINE' "+
        		" and control_date.date_value is not null  and not exists (select 1 from attribute_value v where v.card_id=c.card_id and v.attribute_code='ADMIN_283929' and v.value_id is not null) "+
        	" 	and not exists (select 1 from attribute_value v1 where v1.card_id=c.card_id and v1.attribute_code='ADMIN_220912' and v1.date_value is not null) "+
        	" JOIN attribute_value question ON question.card_id = c.card_id and question.attribute_code='ADMIN_277251' "+
        	" JOIN attribute_value topic ON topic.card_id = question.number_value and topic.attribute_code='ADMIN_278045' "+
        	" JOIN attribute_value  them ON them.card_id = topic.number_value and them.attribute_code='ADMIN_275720' "+
        	" JOIN attribute_value name_t ON name_t.card_id = them.number_value and name_t.attribute_code='NAME' "+
        	" JOIN card cc on cc.card_id = question.number_value " +
          	whereString+
          	" and cc.template_id = 884 " +
        	" GROUP BY name_t.string_value "+
        	"\n UNION \n"+
        	" Select  "+
        	" 22 AS cc "+
        	" ,'��������� �� ������������ �� 1 ����� ������ ���������� �� �������� ��������, ����������� � �������� �������' as name "+
        	" ,name_t.string_value || '" + SUBQUESTION + "' as thematic "+
        	" ,count(distinct cc.card_id) as hz "+
        	" from card c "+
        	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
        	 " JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
        	" 	extract('year' from reg_date.date_value) ="+ pYear.toString() +" and extract('quarter' from reg_date.date_value) ="+ quarter.toString() +" "+
        	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
        	" JOIN attribute_value control_date ON control_date.card_id = c.card_id and control_date.attribute_code='JBR_IMPL_DEADLINE' "+
        		" and control_date.date_value is not null  and not exists (select 1 from attribute_value v where v.card_id=c.card_id and v.attribute_code='ADMIN_283929' and v.value_id is not null) "+
        	" 	and not exists (select 1 from attribute_value v1 where v1.card_id=c.card_id and v1.attribute_code='ADMIN_220912' and v1.date_value is not null) "+
        	" JOIN attribute_value subquestion ON subquestion.card_id = c.card_id and subquestion.attribute_code='ADMIN_277251' "+
        	" JOIN attribute_value question ON question.card_id = subquestion.number_value and question.attribute_code='JBR_QUESTION' "+
        	" JOIN attribute_value topic ON topic.card_id = question.number_value and topic.attribute_code='ADMIN_278045' "+
        	" JOIN attribute_value  them ON them.card_id = topic.number_value and them.attribute_code='ADMIN_275720' "+
        	" JOIN attribute_value name_t ON name_t.card_id = them.number_value and name_t.attribute_code='NAME' "+
        	" JOIN card cc on cc.card_id = subquestion.number_value " +
          	whereString+
          	" and cc.template_id = 885 " +
        	" GROUP BY name_t.string_value "+
        	"\n UNION \n"+
        	" Select  "+
        	" 24 AS cc "+
        	" ,'          " + config.getProperty(PARAM_IMPROPER, PARAM_NULL) + "' as name "+
        	" ,name_t.string_value || '" + QUESTION + "' as thematic "+
        	" ,count(distinct cc.card_id) as hz "+
        	" from card c "+
        	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
        	 " JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
        	" 	extract('year' from reg_date.date_value) ="+ pYear.toString() +" and extract('quarter' from reg_date.date_value) ="+ quarter.toString() +" "+
        	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
        	" JOIN attribute_value fav_d ON fav_d.card_id = c.card_id and fav_d.attribute_code='JBR_ADDRESS_FORMAT' and fav_d.value_id=2036 "+
        	" JOIN attribute_value fav_des ON  fav_des.card_id = c.card_id and  fav_des.attribute_code='JBR_COMPLAINT_REASON' and  fav_des.value_id=2039 "+
        	" JOIN attribute_value question ON question.card_id = c.card_id and question.attribute_code='ADMIN_277251' "+
        	" JOIN attribute_value topic ON topic.card_id = question.number_value and topic.attribute_code='ADMIN_278045' "+
        	" JOIN attribute_value  them ON them.card_id = topic.number_value and them.attribute_code='ADMIN_275720' "+
        	" JOIN attribute_value name_t ON name_t.card_id = them.number_value and name_t.attribute_code='NAME' "+
        	" JOIN card cc on cc.card_id = question.number_value " +
          	whereString+
          	" and cc.template_id = 884 " +
        	" GROUP BY name_t.string_value "+
        	"\n UNION \n"+
        	" Select  "+
        	" 24 AS cc "+
        	" ,'          " + config.getProperty(PARAM_IMPROPER, PARAM_NULL) + "' as name "+
        	" ,name_t.string_value || '" + SUBQUESTION + "' as thematic "+
        	" ,count(distinct cc.card_id) as hz "+
        	" from card c "+
        	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
        	 " JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
        	" 	extract('year' from reg_date.date_value) ="+ pYear.toString() +" and extract('quarter' from reg_date.date_value) ="+ quarter.toString() +" "+
        	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
        	" JOIN attribute_value fav_d ON fav_d.card_id = c.card_id and fav_d.attribute_code='JBR_ADDRESS_FORMAT' and fav_d.value_id=2036 "+
        	" JOIN attribute_value fav_des ON  fav_des.card_id = c.card_id and  fav_des.attribute_code='JBR_COMPLAINT_REASON' and  fav_des.value_id=2039 "+
        	" JOIN attribute_value subquestion ON subquestion.card_id = c.card_id and subquestion.attribute_code='ADMIN_277251' "+
        	" JOIN attribute_value question ON question.card_id = subquestion.number_value and question.attribute_code='JBR_QUESTION' "+
        	" JOIN attribute_value topic ON topic.card_id = question.number_value and topic.attribute_code='ADMIN_278045' "+
        	" JOIN attribute_value  them ON them.card_id = topic.number_value and them.attribute_code='ADMIN_275720' "+
        	" JOIN attribute_value name_t ON name_t.card_id = them.number_value and name_t.attribute_code='NAME' "+
        	" JOIN card cc on cc.card_id = subquestion.number_value " +
          	whereString+
          	" and cc.template_id = 885 " +
        	" GROUP BY name_t.string_value "+
        	"\n UNION \n"+
        	" Select  "+
        	" 25 AS cc "+
        	" ,'          ���������� � ������ ���������� �� �������������� ��������������� �����' as name "+
        	" ,name_t.string_value || '" + QUESTION + "' as thematic "+
        	" ,count(distinct cc.card_id) as hz "+
        	" from card c "+
        	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
        	 " JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
        	" 	extract('year' from reg_date.date_value) ="+ pYear.toString() +" and extract('quarter' from reg_date.date_value) ="+ quarter.toString() +" "+
        	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
        	" JOIN attribute_value fav_d ON fav_d.card_id = c.card_id and fav_d.attribute_code='JBR_ADDRESS_FORMAT' and fav_d.value_id=2036 "+
        	" JOIN attribute_value fav_des ON  fav_des.card_id = c.card_id and  fav_des.attribute_code='JBR_COMPLAINT_REASON' and  fav_des.value_id=2040 "+
        	" JOIN attribute_value question ON question.card_id = c.card_id and question.attribute_code='ADMIN_277251' "+
        	" JOIN attribute_value topic ON topic.card_id = question.number_value and topic.attribute_code='ADMIN_278045' "+
        	" JOIN attribute_value  them ON them.card_id = topic.number_value and them.attribute_code='ADMIN_275720' "+
        	" JOIN attribute_value name_t ON name_t.card_id = them.number_value and name_t.attribute_code='NAME' "+
        	" JOIN card cc on cc.card_id = question.number_value " +
          	whereString+
          	" and cc.template_id = 884 " +
        	" GROUP BY name_t.string_value "+
        	"\n UNION \n"+
        	" Select  "+
        	" 25 AS cc "+
        	" ,'          ���������� � ������ ���������� �� �������������� ��������������� �����' as name "+
        	" ,name_t.string_value || '" + SUBQUESTION + "' as thematic "+
        	" ,count(distinct cc.card_id) as hz "+
        	" from card c "+
        	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
        	 " JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
        	" 	extract('year' from reg_date.date_value) ="+ pYear.toString() +" and extract('quarter' from reg_date.date_value) ="+ quarter.toString() +" "+
        	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
        	" JOIN attribute_value fav_d ON fav_d.card_id = c.card_id and fav_d.attribute_code='JBR_ADDRESS_FORMAT' and fav_d.value_id=2036 "+
        	" JOIN attribute_value fav_des ON  fav_des.card_id = c.card_id and  fav_des.attribute_code='JBR_COMPLAINT_REASON' and  fav_des.value_id=2040 "+
        	" JOIN attribute_value subquestion ON subquestion.card_id = c.card_id and subquestion.attribute_code='ADMIN_277251' "+
        	" JOIN attribute_value question ON question.card_id = subquestion.number_value and question.attribute_code='JBR_QUESTION' "+
        	" JOIN attribute_value topic ON topic.card_id = question.number_value and topic.attribute_code='ADMIN_278045' "+
        	" JOIN attribute_value  them ON them.card_id = topic.number_value and them.attribute_code='ADMIN_275720' "+
        	" JOIN attribute_value name_t ON name_t.card_id = them.number_value and name_t.attribute_code='NAME' "+
        	" JOIN card cc on cc.card_id = subquestion.number_value " +
          	whereString+
          	" and cc.template_id = 885 " +
        	" GROUP BY name_t.string_value "+
        	"\n UNION \n"+
        	" Select  "+
        	" 26 AS cc "+
        	" ,'          ��������� ���������������� � ������������ ��������������� ����������, ������������ ����������� � ������� �����������' as name "+
        	" ,name_t.string_value || '" + QUESTION + "' as thematic "+
        	" ,count(distinct cc.card_id) as hz "+
        	" from card c "+
        	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
        	 " JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
        	" 	extract('year' from reg_date.date_value) ="+ pYear.toString() +" and extract('quarter' from reg_date.date_value) ="+ quarter.toString() +" "+
        	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
        	" JOIN attribute_value fav_d ON fav_d.card_id = c.card_id and fav_d.attribute_code='JBR_ADDRESS_FORMAT' and fav_d.value_id=2036 "+
        	" JOIN attribute_value fav_des ON  fav_des.card_id = c.card_id and  fav_des.attribute_code='JBR_COMPLAINT_REASON' and  fav_des.value_id=2041 "+
        	" JOIN attribute_value question ON question.card_id = c.card_id and question.attribute_code='ADMIN_277251' "+
        	" JOIN attribute_value topic ON topic.card_id = question.number_value and topic.attribute_code='ADMIN_278045' "+
        	" JOIN attribute_value  them ON them.card_id = topic.number_value and them.attribute_code='ADMIN_275720' "+
        	" JOIN attribute_value name_t ON name_t.card_id = them.number_value and name_t.attribute_code='NAME' "+
        	" JOIN card cc on cc.card_id = question.number_value " +
          	whereString+
          	" and cc.template_id = 884 " +
        	" GROUP BY name_t.string_value "+
        	"\n UNION \n"+
        	" Select  "+
        	" 26 AS cc "+
        	" ,'          ��������� ���������������� � ������������ ��������������� ����������, ������������ ����������� � ������� �����������' as name "+
        	" ,name_t.string_value || '" + SUBQUESTION + "' as thematic "+
        	" ,count(distinct cc.card_id) as hz "+
        	" from card c "+
        	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
        	 " JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
        	" 	extract('year' from reg_date.date_value) ="+ pYear.toString() +" and extract('quarter' from reg_date.date_value) ="+ quarter.toString() +" "+
        	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
        	" JOIN attribute_value fav_d ON fav_d.card_id = c.card_id and fav_d.attribute_code='JBR_ADDRESS_FORMAT' and fav_d.value_id=2036 "+
        	" JOIN attribute_value fav_des ON  fav_des.card_id = c.card_id and  fav_des.attribute_code='JBR_COMPLAINT_REASON' and  fav_des.value_id=2041 "+
        	" JOIN attribute_value subquestion ON subquestion.card_id = c.card_id and subquestion.attribute_code='ADMIN_277251' "+
        	" JOIN attribute_value question ON question.card_id = subquestion.number_value and question.attribute_code='JBR_QUESTION' "+
        	" JOIN attribute_value topic ON topic.card_id = question.number_value and topic.attribute_code='ADMIN_278045' "+
        	" JOIN attribute_value  them ON them.card_id = topic.number_value and them.attribute_code='ADMIN_275720' "+
        	" JOIN attribute_value name_t ON name_t.card_id = them.number_value and name_t.attribute_code='NAME' "+
        	" JOIN card cc on cc.card_id = subquestion.number_value " +
          	whereString+
          	" and cc.template_id = 885 " +
        	" GROUP BY name_t.string_value "+
        	"\n UNION \n"+
        	" Select  "+
        	" 27 AS cc "+
        	" ,'          " + config.getProperty(PARAM_REJECTION, PARAM_NULL) + "' as name "+
        	" ,name_t.string_value || '" + QUESTION + "' as thematic "+
        	" ,count(distinct cc.card_id) as hz "+
        	" from card c "+
        	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
        	 " JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
        	" 	extract('year' from reg_date.date_value) ="+ pYear.toString() +" and extract('quarter' from reg_date.date_value) ="+ quarter.toString() +" "+
        	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
        	" JOIN attribute_value fav_d ON fav_d.card_id = c.card_id and fav_d.attribute_code='JBR_ADDRESS_FORMAT' and fav_d.value_id=2036 "+
        	" JOIN attribute_value fav_des ON  fav_des.card_id = c.card_id and  fav_des.attribute_code='JBR_COMPLAINT_REASON' and  fav_des.value_id=2042 "+
        	" JOIN attribute_value question ON question.card_id = c.card_id and question.attribute_code='ADMIN_277251' "+
        	" JOIN attribute_value topic ON topic.card_id = question.number_value and topic.attribute_code='ADMIN_278045' "+
        	" JOIN attribute_value  them ON them.card_id = topic.number_value and them.attribute_code='ADMIN_275720' "+
        	" JOIN attribute_value name_t ON name_t.card_id = them.number_value and name_t.attribute_code='NAME' "+
        	" JOIN card cc on cc.card_id = question.number_value " +
          	whereString+
          	" and cc.template_id = 884 " +
        	" GROUP BY name_t.string_value "+
        	"\n UNION \n"+
        	" Select  "+
        	" 27 AS cc "+
        	" ,'          " + config.getProperty(PARAM_REJECTION, PARAM_NULL) + "' as name "+
        	" ,name_t.string_value || '" + SUBQUESTION + "' as thematic "+
        	" ,count(distinct cc.card_id) as hz "+
        	" from card c "+
        	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
        	 " JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
        	" 	extract('year' from reg_date.date_value) ="+ pYear.toString() +" and extract('quarter' from reg_date.date_value) ="+ quarter.toString() +" "+
        	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
        	" JOIN attribute_value fav_d ON fav_d.card_id = c.card_id and fav_d.attribute_code='JBR_ADDRESS_FORMAT' and fav_d.value_id=2036 "+
        	" JOIN attribute_value fav_des ON  fav_des.card_id = c.card_id and  fav_des.attribute_code='JBR_COMPLAINT_REASON' and  fav_des.value_id=2042 "+
        	" JOIN attribute_value subquestion ON subquestion.card_id = c.card_id and subquestion.attribute_code='ADMIN_277251' "+
        	" JOIN attribute_value question ON question.card_id = subquestion.number_value and question.attribute_code='JBR_QUESTION' "+
        	" JOIN attribute_value topic ON topic.card_id = question.number_value and topic.attribute_code='ADMIN_278045' "+
        	" JOIN attribute_value  them ON them.card_id = topic.number_value and them.attribute_code='ADMIN_275720' "+
        	" JOIN attribute_value name_t ON name_t.card_id = them.number_value and name_t.attribute_code='NAME' "+
        	" JOIN card cc on cc.card_id = subquestion.number_value " +
          	whereString+
          	" and cc.template_id = 885 " +
        	" GROUP BY name_t.string_value "+
        	"\n UNION \n"+
        	" Select  "+
        	" 28 AS cc "+
        	" ,'          ������������� ����������������� � ������������ ���������� �� �������������� ��������������� �����' as name "+
        	" ,name_t.string_value || '" + QUESTION + "' as thematic "+
        	" ,count(distinct cc.card_id) as hz "+
        	" from card c "+
        	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
        	 " JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
        	" 	extract('year' from reg_date.date_value) ="+ pYear.toString() +" and extract('quarter' from reg_date.date_value) ="+ quarter.toString() +" "+
        	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
        	" JOIN attribute_value fav_d ON fav_d.card_id = c.card_id and fav_d.attribute_code='JBR_ADDRESS_FORMAT' and fav_d.value_id=2036 "+
        	" JOIN attribute_value fav_des ON  fav_des.card_id = c.card_id and  fav_des.attribute_code='JBR_COMPLAINT_REASON' and  fav_des.value_id=2043 "+
        	" JOIN attribute_value question ON question.card_id = c.card_id and question.attribute_code='ADMIN_277251' "+
        	" JOIN attribute_value topic ON topic.card_id = question.number_value and topic.attribute_code='ADMIN_278045' "+
        	" JOIN attribute_value  them ON them.card_id = topic.number_value and them.attribute_code='ADMIN_275720' "+
        	" JOIN attribute_value name_t ON name_t.card_id = them.number_value and name_t.attribute_code='NAME' "+
        	" JOIN card cc on cc.card_id = question.number_value " +
          	whereString+
          	" and cc.template_id = 884 " +
        	" GROUP BY name_t.string_value "+
        	"\n UNION \n"+
        	" Select  "+
        	" 28 AS cc "+
        	" ,'          ������������� ����������������� � ������������ ���������� �� �������������� ��������������� �����' as name "+
        	" ,name_t.string_value || '" + SUBQUESTION + "' as thematic "+
        	" ,count(distinct cc.card_id) as hz "+
        	" from card c "+
        	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
        	 " JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
        	" 	extract('year' from reg_date.date_value) ="+ pYear.toString() +" and extract('quarter' from reg_date.date_value) ="+ quarter.toString() +" "+
        	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
        	" JOIN attribute_value fav_d ON fav_d.card_id = c.card_id and fav_d.attribute_code='JBR_ADDRESS_FORMAT' and fav_d.value_id=2036 "+
        	" JOIN attribute_value fav_des ON  fav_des.card_id = c.card_id and  fav_des.attribute_code='JBR_COMPLAINT_REASON' and  fav_des.value_id=2043 "+
        	" JOIN attribute_value subquestion ON subquestion.card_id = c.card_id and subquestion.attribute_code='ADMIN_277251' "+
        	" JOIN attribute_value question ON question.card_id = subquestion.number_value and question.attribute_code='JBR_QUESTION' "+
        	" JOIN attribute_value topic ON topic.card_id = question.number_value and topic.attribute_code='ADMIN_278045' "+
        	" JOIN attribute_value  them ON them.card_id = topic.number_value and them.attribute_code='ADMIN_275720' "+
        	" JOIN attribute_value name_t ON name_t.card_id = them.number_value and name_t.attribute_code='NAME' "+
        	" JOIN card cc on cc.card_id = subquestion.number_value " +
          	whereString+
          	" and cc.template_id = 885 " +
        	" GROUP BY name_t.string_value "+
        	"\n UNION \n"+
        	" Select  "+
        	" 29 AS cc "+
        	" ,'          ������ �������� ����������� �������' as name "+
        	" ,name_t.string_value || '" + QUESTION + "' as thematic "+
        	" ,count(distinct cc.card_id) as hz "+
        	" from card c "+
        	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
        	 " JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
        	" 	extract('year' from reg_date.date_value) ="+ pYear.toString() +" and extract('quarter' from reg_date.date_value) ="+ quarter.toString() +" "+
        	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
        	" JOIN attribute_value fav_d ON fav_d.card_id = c.card_id and fav_d.attribute_code='JBR_ADDRESS_FORMAT' and fav_d.value_id=2036 "+
        	" JOIN attribute_value fav_des ON  fav_des.card_id = c.card_id and  fav_des.attribute_code='JBR_COMPLAINT_REASON' and  fav_des.value_id=2044 "+
        	" JOIN attribute_value question ON question.card_id = c.card_id and question.attribute_code='ADMIN_277251' "+
        	" JOIN attribute_value topic ON topic.card_id = question.number_value and topic.attribute_code='ADMIN_278045' "+
        	" JOIN attribute_value  them ON them.card_id = topic.number_value and them.attribute_code='ADMIN_275720' "+
        	" JOIN attribute_value name_t ON name_t.card_id = them.number_value and name_t.attribute_code='NAME' "+
        	" JOIN card cc on cc.card_id = question.number_value " +
          	whereString+
          	" and cc.template_id = 884 " +
        	" GROUP BY name_t.string_value "+
        	"\n UNION \n"+
        	" Select  "+
        	" 29 AS cc "+
        	" ,'          ������ �������� ����������� �������' as name "+
        	" ,name_t.string_value || '" + SUBQUESTION + "' as thematic "+
        	" ,count(distinct cc.card_id) as hz "+
        	" from card c "+
        	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
        	 " JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
        	" 	extract('year' from reg_date.date_value) ="+ pYear.toString() +" and extract('quarter' from reg_date.date_value) ="+ quarter.toString() +" "+
        	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
        	" JOIN attribute_value fav_d ON fav_d.card_id = c.card_id and fav_d.attribute_code='JBR_ADDRESS_FORMAT' and fav_d.value_id=2036 "+
        	" JOIN attribute_value fav_des ON  fav_des.card_id = c.card_id and  fav_des.attribute_code='JBR_COMPLAINT_REASON' and  fav_des.value_id=2044 "+
        	" JOIN attribute_value subquestion ON subquestion.card_id = c.card_id and subquestion.attribute_code='ADMIN_277251' "+
        	" JOIN attribute_value question ON question.card_id = subquestion.number_value and question.attribute_code='JBR_QUESTION' "+
        	" JOIN attribute_value topic ON topic.card_id = question.number_value and topic.attribute_code='ADMIN_278045' "+
        	" JOIN attribute_value  them ON them.card_id = topic.number_value and them.attribute_code='ADMIN_275720' "+
        	" JOIN attribute_value name_t ON name_t.card_id = them.number_value and name_t.attribute_code='NAME' "+
        	" JOIN card cc on cc.card_id = subquestion.number_value " +
          	whereString+
          	" and cc.template_id = 885 " +
        	" GROUP BY name_t.string_value "+
        	"\n UNION \n"+
        	" Select  "+
        	" 30 AS cc "+
        	" ,'          ������' as name "+
        	" ,name_t.string_value || '" + QUESTION +"' as thematic "+
        	" ,count(distinct cc.card_id) as hz "+
        	" from card c "+
        	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
        	 " JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
        	" 	extract('year' from reg_date.date_value) ="+ pYear.toString() +" and extract('quarter' from reg_date.date_value) ="+ quarter.toString() +" "+
        	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
        	" JOIN attribute_value fav_d ON fav_d.card_id = c.card_id and fav_d.attribute_code='JBR_ADDRESS_FORMAT' and fav_d.value_id=2036 "+
        	" JOIN attribute_value fav_des ON  fav_des.card_id = c.card_id and  fav_des.attribute_code='JBR_COMPLAINT_REASON' and  fav_des.value_id=2045 "+
        	" JOIN attribute_value question ON question.card_id = c.card_id and question.attribute_code='ADMIN_277251' "+
        	" JOIN attribute_value topic ON topic.card_id = question.number_value and topic.attribute_code='ADMIN_278045' "+
        	" JOIN attribute_value  them ON them.card_id = topic.number_value and them.attribute_code='ADMIN_275720' "+
        	" JOIN attribute_value name_t ON name_t.card_id = them.number_value and name_t.attribute_code='NAME' "+
        	" JOIN card cc on cc.card_id = question.number_value " +
          	whereString+
          	" and cc.template_id = 884 " +
        	" GROUP BY name_t.string_value "+
        	"\n UNION \n"+
        	" Select  "+
        	" 30 AS cc "+
        	" ,'          ������' as name "+
        	" ,name_t.string_value || '" + SUBQUESTION +"' as thematic "+
        	" ,count(distinct cc.card_id) as hz "+
        	" from card c "+
        	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
        	 " JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
        	" 	extract('year' from reg_date.date_value) ="+ pYear.toString() +" and extract('quarter' from reg_date.date_value) ="+ quarter.toString() +" "+
        	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
        	" JOIN attribute_value fav_d ON fav_d.card_id = c.card_id and fav_d.attribute_code='JBR_ADDRESS_FORMAT' and fav_d.value_id=2036 "+
        	" JOIN attribute_value fav_des ON  fav_des.card_id = c.card_id and  fav_des.attribute_code='JBR_COMPLAINT_REASON' and  fav_des.value_id=2045 "+
        	" JOIN attribute_value subquestion ON subquestion.card_id = c.card_id and subquestion.attribute_code='ADMIN_277251' "+
        	" JOIN attribute_value question ON question.card_id = subquestion.number_value and question.attribute_code='JBR_QUESTION' "+
        	" JOIN attribute_value topic ON topic.card_id = question.number_value and topic.attribute_code='ADMIN_278045' "+
        	" JOIN attribute_value  them ON them.card_id = topic.number_value and them.attribute_code='ADMIN_275720' "+
        	" JOIN attribute_value name_t ON name_t.card_id = them.number_value and name_t.attribute_code='NAME' "+
        	" JOIN card cc on cc.card_id = subquestion.number_value " +
          	whereString+
          	" and cc.template_id = 885 " +
        	" GROUP BY name_t.string_value "+
        	" ORDER BY cc ");
        	System.out.println(sql.toString());
        return sql.toString();
    }
	
	private void getEmptyRows(StringBuilder sql) {
		sql.append("Select "+
	        	"6 AS cc "+
	        	",'�� ���: ' as name "+
	        	",'������ ������' as thematic "+
	        	",0 as hz \n"+
	        	"UNION \n"+
	        	"Select "+
	        	"11 AS cc "+
	        	",'���������������� �� ������������� � ������������ �� ����������� ���������� �� �������� ������: ' as name "+
	        	",'������ ������' as thematic "+
	        	",0 as hz \n"+
	        	"UNION \n"+
	        	"Select "+
	        	"23 AS cc "+
	        	",'������� �������, ����������� ������������ ������:' as name "+
	        	",'������ ������' as thematic "+
	        	",0 as hz \n"+
	        	"UNION \n");
	}
	
	private void getOGForQuarter(StringBuilder sql, Integer prYear, Integer pYear, Integer firstMonthCurQuarter, 
									Integer prDate, Integer quarter, String whereString) {
		//���������� ��������� �� �������
		sql.append(" Select    "+
      	  " 1 AS cc  "+
      	  " ,'����������� ������� �� ���������� �������� ������' as name  "+
      	  " ,'���������� ��������� �� �������' as thematic  "+
      	  " ,count(distinct c.card_id) as hz  "+
      	  " from card c  "+
      	  " JOIN template t on t.template_id=864 and t.template_id=c.template_id  "+
      	  " JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and  "+
      	  " 	extract('year' from reg_date.date_value) =   "+ prYear.toString() +"  and extract('quarter' from reg_date.date_value) =  "+ prDate.toString() +"    "+
      	  " join card_version cv on cv.card_id = c.card_id and cv.version_date = (select coalesce ((select cv2.version_date from card_version cv2 where cv2.card_id = c.card_id " +
      	  "							and cv2.version_date < date_trunc('day', TO_DATE(" + pYear.toString() + " || '-' || " + firstMonthCurQuarter.toString() + " || '-' || '1', 'YYYY-MM-DD') + interval '0 hour') + INTERVAL '1 DAY' " +
      	  "							and cv2.version_date >= date_trunc('day', TO_DATE(" + pYear.toString() + " || '-' || " + firstMonthCurQuarter.toString() + " || '-' || '1', 'YYYY-MM-DD') + interval '0 hour') " +
      	  "							and cv2.status_id in (101, 102, 103, 301) " +
      	  "							limit 1 " +
		  "							), " +
      	  "		  					(select cv2.version_date from card_version cv2 where cv2.card_id = c.card_id " +
      	  "							and cv2.version_date < date_trunc('day', TO_DATE(" + prYear.toString() + " || '-' || " + firstMonthCurQuarter.toString() + " || '-' || '1', 'YYYY-MM-DD') + interval '0 hour') " +
      	  "		  					and cv2.status_id in (101, 102, 103, 301) " +
      	  "		  					order by cv2.version_date desc " +
      	  "		  					limit 1 " +
      	  "		  					)) " +
      	  " 		  																) " +
      	  " JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
      	whereString+
      	  "\n UNION \n"+ 
      	" Select  "+
    	" 2 AS cc "+
    	" ,'" + config.getProperty(PARAM_RECEIVED, PARAM_NULL) + "' as name "+
    	" ,'���������� ��������� �� �������' as thematic  "+
    	" ,count(distinct c.card_id) as hz "+
    	" from card c "+
    	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
    	 " JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
    	" 	extract('year' from reg_date.date_value) ="+ pYear.toString() +" and extract('quarter' from reg_date.date_value) ="+ quarter.toString() +" "+
    	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
      	whereString+
    	"\n UNION \n"+
    	" Select  "+
    	" 3 AS cc "+
    	" ,'          � ��� ����� ���������' as name "+
    	" ,'���������� ��������� �� �������' as thematic  "+
    	" ,count(distinct c.card_id) as hz "+
    	" from card c "+
    	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
    	" JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
    	" 	extract('year' from reg_date.date_value) ="+ pYear.toString() +" and extract('quarter' from reg_date.date_value) ="+ quarter.toString() +" "+
    	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
    	" JOIN attribute_value format_obr ON format_obr.card_id = c.card_id and format_obr.attribute_code='JBR_ADDRESS_FORMAT' and format_obr.value_id=2037 "+
      	whereString+
    	"\n UNION \n"+
    	" Select  "+
    	" 4 AS cc "+
    	" ,'          � ��� ����� �����' as name "+
    	" ,'���������� ��������� �� �������' as thematic  "+
    	" ,count(distinct c.card_id) as hz "+
    	" from card c "+
    	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
    	"  JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
    		" extract('year' from reg_date.date_value) ="+ pYear.toString() +" and extract('quarter' from reg_date.date_value) ="+ quarter.toString() +" "+
    	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
    	" JOIN attribute_value format_obr ON format_obr.card_id = c.card_id and format_obr.attribute_code='JBR_ADDRESS_FORMAT' and format_obr.value_id=2036 "+
      	whereString+
    	"\n UNION \n"+
    	" Select  "+
    	" 5 AS cc "+
    	" ,'          � ��� ����� �����������' as name "+
    	" ,'���������� ��������� �� �������' as thematic  "+
    	" ,count(distinct c.card_id) as hz "+
    	" from card c "+
    	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
    	 " JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
    	" 	extract('year' from reg_date.date_value) ="+ pYear.toString() +" and extract('quarter' from reg_date.date_value) ="+ quarter.toString() +" "+
    	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
    	" JOIN attribute_value format_obr ON format_obr.card_id = c.card_id and format_obr.attribute_code='JBR_ADDRESS_FORMAT' and format_obr.value_id=2038 "+
      	whereString+
    	"\n UNION \n"+
    	" Select  "+
    	" 7 AS cc "+
    	" ,'          " + config.getProperty(PARAM_REPORTED, PARAM_NULL) + "' as name "+
    	" ,'���������� ��������� �� �������' as thematic  "+
    	" ,count(distinct c.card_id) as hz "+
    	" from card c "+
    	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
    	 " JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
    	" 	extract('year' from reg_date.date_value) ="+ pYear.toString() +" and extract('quarter' from reg_date.date_value) ="+ quarter.toString() +" "+
    	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
    	" JOIN attribute_value gub ON gub.card_id = c.card_id and gub.attribute_code='JBR_IMPL_ACQUAINTERS' "+
    	" JOIN person p ON p.person_id = gub.number_value "+
    	" JOIN person_role pr ON pr.person_id = p.person_id and pr.role_code in ('JBR_BOSS') " +
      	whereString+
    	"\n UNION \n"+
    	" Select "+
    	" 8 AS cc "+
    	" ,'          ����� �� ��������' as name "+
    	" ,'���������� ��������� �� �������' as thematic  "+
    	" ,count(distinct c.card_id) as hz "+
    	" from card c "+
    	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
    	" JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
    		" extract('year' from reg_date.date_value) ="+ pYear.toString() +" and extract('quarter' from reg_date.date_value) ="+ quarter.toString() +" "+
    	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
    	" JOIN attribute_value av_contrl ON av_contrl.card_id = c.card_id and av_contrl.attribute_code='JBR_IMPL_ONCONT' and av_contrl.value_id=1432 "+
      	whereString+
    	"\n UNION \n"+
    	" Select "+
    	" 9 AS cc "+
    	" ,'          ����������� � ������� �� �����' as name "+
    	" ,'���������� ��������� �� �������' as thematic  "+
    	" ,count(distinct c.card_id) as hz "+
    	" from card c "+
    	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
    	 " JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
    	" 	extract('year' from reg_date.date_value) ="+ pYear.toString() +" and extract('quarter' from reg_date.date_value) ="+ quarter.toString() +" "+
    	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
    	" JOIN attribute_value av_contrl ON av_contrl.card_id = c.card_id and av_contrl.attribute_code='JBR_EXAMIN_PROCEDURE' and av_contrl.value_id=2046 "+
      	whereString+
     	"\n UNION \n"+
    	" Select "+
    	" 10 AS cc "+
    	" ,'          ����������� ������������' as name "+
    	" ,'���������� ��������� �� �������' as thematic  "+
    	" ,count(distinct c.card_id) as hz "+
    	" from card c "+
    	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
    	"  JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
    		" extract('year' from reg_date.date_value) ="+ pYear.toString() +" and extract('quarter' from reg_date.date_value) ="+ quarter.toString() +" "+
    	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
    	" JOIN attribute_value av_contrl ON av_contrl.card_id = c.card_id and av_contrl.attribute_code='JBR_EXAMIN_PROCEDURE' and av_contrl.value_id=2047 "+
      	whereString+
    	"\n UNION \n"+
    	" Select "+
    	" 12 AS cc "+
    	" ,'          ����������' as name "+
    	" ,'���������� ��������� �� �������' as thematic  "+
    	" ,count(distinct c.card_id) as hz "+
    	" from card c "+
    	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
    	 " JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
    	" 	extract('year' from reg_date.date_value) ="+ pYear.toString() +" and extract('quarter' from reg_date.date_value) ="+ quarter.toString() +" "+
    	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
    	" JOIN attribute_value av_contrl ON av_contrl.card_id = c.card_id and av_contrl.attribute_code='ADMIN_283929' and av_contrl.value_id in (Select value_id from values_list where value_rus='����������' or "+ " value_rus='���� �������') "+
      	whereString+
    	"\n UNION \n"+
    	" Select "+
    	" 13 AS cc "+
    	" ,'          � ��� ����� ���� �������' as name "+
    	" ,'���������� ��������� �� �������' as thematic  "+
    	" ,count(distinct c.card_id) as hz "+
    	" from card c "+
    	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
    	 " JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
    	" 	extract('year' from reg_date.date_value) ="+ pYear.toString() +" and extract('quarter' from reg_date.date_value) ="+ quarter.toString() +" "+
    	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
    	" JOIN attribute_value av_contrl ON av_contrl.card_id = c.card_id and av_contrl.attribute_code='ADMIN_283929' and av_contrl.value_id in (Select value_id from values_list where value_rus='���� �������') "+
      	whereString+
    	"\n UNION \n"+
    	" Select "+
    	" 14 AS cc "+
    	" ,'          ����������' as name "+
    	" ,'���������� ��������� �� �������' as thematic  "+
    	" ,count(distinct c.card_id) as hz "+
    	" from card c "+
    	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
    	"  JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
    		" extract('year' from reg_date.date_value) ="+ pYear.toString() +" and extract('quarter' from reg_date.date_value) ="+ quarter.toString() +" "+
    	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
    	" JOIN attribute_value av_contrl ON av_contrl.card_id = c.card_id and av_contrl.attribute_code='ADMIN_283929' and av_contrl.value_id in (Select value_id from values_list where value_rus='����������') "+
      	whereString+
    	"\n UNION \n"+
    	" Select "+
    	" 15 AS cc "+
    	" ,'          �� ����������' as name "+
    	" ,'���������� ��������� �� �������' as thematic  "+
    	" ,count(distinct c.card_id) as hz "+
    	" from card c "+
    	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
    	 " JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
    	" 	extract('year' from reg_date.date_value) ="+ pYear.toString() +" and extract('quarter' from reg_date.date_value) ="+ quarter.toString() +" "+
    	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
    	" JOIN attribute_value av_contrl ON av_contrl.card_id = c.card_id and av_contrl.attribute_code='ADMIN_283929' and av_contrl.value_id in (Select value_id from values_list where value_rus='�� ����������') "+
      	whereString+
    	"\n UNION \n"+
    	" Select "+
    	" 16 AS cc "+
    	" ,'" + config.getProperty(PARAM_SENT, PARAM_NULL) + "' as name "+
    	" ,'���������� ��������� �� �������' as thematic  "+
    	" ,count(distinct c.card_id) as hz "+
    	" from card c "+
    	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
    	"  JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
    		" extract('year' from reg_date.date_value) ="+ pYear.toString() +" and extract('quarter' from reg_date.date_value) ="+ quarter.toString() +" "+
    	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
    	" JOIN attribute_value relate_doc ON relate_doc.card_id = c.card_id and relate_doc.attribute_code='JBR_DOCL_RELATDOC' "+
    	" JOIN card relate_doc_card on relate_doc_card.card_id=relate_doc.number_value and relate_doc_card.template_id=364 "+
    		" and relate_doc_card.status_id=101 "+
    	" JOIN attribute_value gub ON gub.card_id = relate_doc_card.card_id and gub.attribute_code='JBR_INFD_SIGNATORY' "+
    	" JOIN person p ON p.person_id = gub.number_value "+
    	" JOIN person_role pr ON pr.person_id = p.person_id and pr.role_code in ('JBR_BOSS','JBR_DEPUTY_MINISTER') " +
      	whereString+
    	"\n UNION \n"+
    	" Select  "+
    	" 17 AS cc "+
    	" ,'���������� ���������, ������������� ��������� � �������� �������� ��������������' as name "+
    	" ,'���������� ��������� �� �������' as thematic  "+
    	" ,count(distinct c.card_id) as hz "+
    	" from card c "+
    	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
    	 " JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
    	" 	extract('year' from reg_date.date_value) ="+ pYear.toString() +" and extract('quarter' from reg_date.date_value) ="+ quarter.toString() +" "+
    	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
    	" JOIN attribute_value cont ON cont.card_id = c.card_id and cont.attribute_code='JBR_EXAMIN_PROCEDURE' and cont.value_id=2049 "+
      	whereString+
    	"\n UNION \n"+
    	" Select  "+
    	" 18 AS cc "+
    	" ,'���������� ���������, ������������� ��������� � ���������������� �������� ����������� ������� �������������� ������' as name "+
    	" ,'���������� ��������� �� �������' as thematic  "+
    	" ,count(distinct c.card_id) as hz "+
    	" from card c "+
    	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
    	"  JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
    		" extract('year' from reg_date.date_value) ="+ pYear.toString() +" and extract('quarter' from reg_date.date_value) ="+ quarter.toString() +" "+
    	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
    	" JOIN attribute_value cont ON cont.card_id = c.card_id and cont.attribute_code='JBR_EXAMIN_PROCEDURE' and cont.value_id=2048 "+
      	whereString+
    	"\n UNION \n"+
    	" Select  "+
    	" 19 AS cc "+
    	" ,'���������� �����, � ������� ������������� ����������� �����' as name "+
    	" ,'���������� ��������� �� �������' as thematic  "+
    	" ,count(distinct c.card_id) as hz "+
    	" from card c "+
    	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
    	 " JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
    	" 	extract('year' from reg_date.date_value) ="+ pYear.toString() +" and extract('quarter' from reg_date.date_value) ="+ quarter.toString() +" "+
    	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
    	" JOIN attribute_value fav_d ON fav_d.card_id = c.card_id and fav_d.attribute_code='JBR_ADDRESS_FORMAT' and fav_d.value_id=2036 "+
    	" JOIN attribute_value d ON d.card_id = c.card_id and d.attribute_code='ADMIN_283926' "+
    	" JOIN attribute_value fav_des ON fav_des.card_id = d.number_value and fav_des.attribute_code='NAME' and  fav_des.string_value='����� �������������' "+
      	whereString+
    	"\n UNION \n"+
    	" Select  "+
    	" 20 AS cc "+
    	" ,'���������� �����, �� ����������� ������������ ������� �������� � ��������� ���� ������� ��������' as name "+
    	" ,'���������� ��������� �� �������' as thematic  "+
    	" ,count(distinct c.card_id) as hz "+
    	" from card c "+
    	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
    	"  JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
    		" extract('year' from reg_date.date_value) ="+ pYear.toString() +" and extract('quarter' from reg_date.date_value) ="+ quarter.toString() +" "+
    	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
    	" JOIN attribute_value fav_d ON fav_d.card_id = c.card_id and fav_d.attribute_code='JBR_ADDRESS_FORMAT' and fav_d.value_id=2036 "+
    	" JOIN attribute_value d ON d.card_id = c.card_id and d.attribute_code='ADMIN_283926' "+
    	" JOIN attribute_value fav_des ON fav_des.card_id = d.number_value and fav_des.attribute_code='NAME' and  fav_des.string_value='�������� ��������' "+
      	whereString+
    	"\n UNION \n"+
    	" Select  "+
    	" 21 AS cc "+
    	" ,'����������� ��������� � ���������� �����' as name "+
    	" ,'���������� ��������� �� �������' as thematic  "+
    	" ,count(distinct c.card_id) as hz "+
    	" from card c "+
    	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
    	 " JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
    	" 	extract('year' from reg_date.date_value) ="+ pYear.toString() +" and extract('quarter' from reg_date.date_value) ="+ quarter.toString() +" "+
    	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
    	" JOIN attribute_value decision ON decision.card_id=c.card_id and decision.attribute_code='ADMIN_283929' and decision.value_id is not null "+
    	" JOIN attribute_value control_date ON control_date.card_id = c.card_id and control_date.attribute_code='JBR_IMPL_DEADLINE'  and control_date.date_value  is not null "+
    	" JOIN attribute_value answer_date ON answer_date.card_id=c.card_id and answer_date.attribute_code='ADMIN_220912' and answer_date.date_value is not null "+
    	   " and date_trunc('day', answer_date.date_value)>date_trunc('day', control_date.date_value) "+
    	whereString+
     	"\n UNION \n"+
    	" Select  "+
    	" 22 AS cc "+
    	" ,'��������� �� ������������ �� 1 ����� ������ ���������� �� �������� ��������, ����������� � �������� �������' as name "+
    	" ,'���������� ��������� �� �������' as thematic  "+
    	" ,count(distinct c.card_id) as hz "+
    	" from card c "+
    	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
    	 " JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
    	" 	extract('year' from reg_date.date_value) ="+ pYear.toString() +" and extract('quarter' from reg_date.date_value) ="+ quarter.toString() +" "+
    	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
    	" JOIN attribute_value control_date ON control_date.card_id = c.card_id and control_date.attribute_code='JBR_IMPL_DEADLINE' "+
    		" and control_date.date_value is not null  and not exists (select 1 from attribute_value v where v.card_id=c.card_id and v.attribute_code='ADMIN_283929' and v.value_id is not null) "+
    	" 	and not exists (select 1 from attribute_value v1 where v1.card_id=c.card_id and v1.attribute_code='ADMIN_220912' and v1.date_value is not null) "+
      	whereString+
    	"\n UNION \n"+
    	" Select  "+
    	" 24 AS cc "+
    	" ,'          " + config.getProperty(PARAM_IMPROPER, PARAM_NULL) + "' as name "+
    	" ,'���������� ��������� �� �������' as thematic  "+
    	" ,count(distinct c.card_id) as hz "+
    	" from card c "+
    	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
    	 " JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
    	" 	extract('year' from reg_date.date_value) ="+ pYear.toString() +" and extract('quarter' from reg_date.date_value) ="+ quarter.toString() +" "+
    	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
    	" JOIN attribute_value fav_d ON fav_d.card_id = c.card_id and fav_d.attribute_code='JBR_ADDRESS_FORMAT' and fav_d.value_id=2036 "+
    	" JOIN attribute_value fav_des ON  fav_des.card_id = c.card_id and  fav_des.attribute_code='JBR_COMPLAINT_REASON' and  fav_des.value_id=2039 "+
      	whereString+
    	"\n UNION \n"+
    	" Select  "+
    	" 25 AS cc "+
    	" ,'          ���������� � ������ ���������� �� �������������� ��������������� �����' as name "+
    	" ,'���������� ��������� �� �������' as thematic  "+
    	" ,count(distinct c.card_id) as hz "+
    	" from card c "+
    	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
    	 " JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
    	" 	extract('year' from reg_date.date_value) ="+ pYear.toString() +" and extract('quarter' from reg_date.date_value) ="+ quarter.toString() +" "+
    	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
    	" JOIN attribute_value fav_d ON fav_d.card_id = c.card_id and fav_d.attribute_code='JBR_ADDRESS_FORMAT' and fav_d.value_id=2036 "+
    	" JOIN attribute_value fav_des ON  fav_des.card_id = c.card_id and  fav_des.attribute_code='JBR_COMPLAINT_REASON' and  fav_des.value_id=2040 "+
      	whereString+
    	"\n UNION \n"+
    	" Select  "+
    	" 26 AS cc "+
    	" ,'          ��������� ���������������� � ������������ ��������������� ����������, ������������ ����������� � ������� �����������' as name "+
    	" ,'���������� ��������� �� �������' as thematic  "+
    	" ,count(distinct c.card_id) as hz "+
    	" from card c "+
    	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
    	 " JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
    	" 	extract('year' from reg_date.date_value) ="+ pYear.toString() +" and extract('quarter' from reg_date.date_value) ="+ quarter.toString() +" "+
    	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
    	" JOIN attribute_value fav_d ON fav_d.card_id = c.card_id and fav_d.attribute_code='JBR_ADDRESS_FORMAT' and fav_d.value_id=2036 "+
    	" JOIN attribute_value fav_des ON  fav_des.card_id = c.card_id and  fav_des.attribute_code='JBR_COMPLAINT_REASON' and  fav_des.value_id=2041 "+
      	whereString+
    	"\n UNION \n"+
    	" Select  "+
    	" 27 AS cc "+
    	" ,'          " + config.getProperty(PARAM_REJECTION, PARAM_NULL) + "' as name "+
    	" ,'���������� ��������� �� �������' as thematic  "+
    	" ,count(distinct c.card_id) as hz "+
    	" from card c "+
    	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
    	 " JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
    	" 	extract('year' from reg_date.date_value) ="+ pYear.toString() +" and extract('quarter' from reg_date.date_value) ="+ quarter.toString() +" "+
    	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
    	" JOIN attribute_value fav_d ON fav_d.card_id = c.card_id and fav_d.attribute_code='JBR_ADDRESS_FORMAT' and fav_d.value_id=2036 "+
    	" JOIN attribute_value fav_des ON  fav_des.card_id = c.card_id and  fav_des.attribute_code='JBR_COMPLAINT_REASON' and  fav_des.value_id=2042 "+
      	whereString+
    	"\n UNION \n"+
    	" Select  "+
    	" 28 AS cc "+
    	" ,'          ������������� ����������������� � ������������ ���������� �� �������������� ��������������� �����' as name "+
    	" ,'���������� ��������� �� �������' as thematic  "+
    	" ,count(distinct c.card_id) as hz "+
    	" from card c "+
    	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
    	 " JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
    	" 	extract('year' from reg_date.date_value) ="+ pYear.toString() +" and extract('quarter' from reg_date.date_value) ="+ quarter.toString() +" "+
    	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
    	" JOIN attribute_value fav_d ON fav_d.card_id = c.card_id and fav_d.attribute_code='JBR_ADDRESS_FORMAT' and fav_d.value_id=2036 "+
    	" JOIN attribute_value fav_des ON  fav_des.card_id = c.card_id and  fav_des.attribute_code='JBR_COMPLAINT_REASON' and  fav_des.value_id=2043 "+
      	whereString+
    	"\n UNION \n"+
    	" Select  "+
    	" 29 AS cc "+
    	" ,'          ������ �������� ����������� �������' as name "+
    	" ,'���������� ��������� �� �������' as thematic  "+
    	" ,count(distinct c.card_id) as hz "+
    	" from card c "+
    	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
    	 " JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
    	" 	extract('year' from reg_date.date_value) ="+ pYear.toString() +" and extract('quarter' from reg_date.date_value) ="+ quarter.toString() +" "+
    	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
    	" JOIN attribute_value fav_d ON fav_d.card_id = c.card_id and fav_d.attribute_code='JBR_ADDRESS_FORMAT' and fav_d.value_id=2036 "+
    	" JOIN attribute_value fav_des ON  fav_des.card_id = c.card_id and  fav_des.attribute_code='JBR_COMPLAINT_REASON' and  fav_des.value_id=2044 "+
      	whereString+
    	"\n UNION \n"+
    	" Select  "+
    	" 30 AS cc "+
    	" ,'          ������' as name "+
    	" ,'���������� ��������� �� �������' as thematic  "+
    	" ,count(distinct c.card_id) as hz "+
    	" from card c "+
    	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
    	 " JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
    	" 	extract('year' from reg_date.date_value) ="+ pYear.toString() +" and extract('quarter' from reg_date.date_value) ="+ quarter.toString() +" "+
    	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
    	" JOIN attribute_value fav_d ON fav_d.card_id = c.card_id and fav_d.attribute_code='JBR_ADDRESS_FORMAT' and fav_d.value_id=2036 "+
    	" JOIN attribute_value fav_des ON  fav_des.card_id = c.card_id and  fav_des.attribute_code='JBR_COMPLAINT_REASON' and  fav_des.value_id=2045 "+
      	whereString+
      	"\n UNION \n");
	}
	
	
	private void getOGForQuarterByThematic(StringBuilder sql, Integer prYear, Integer pYear, Integer firstMonthCurQuarter, 
			Integer prDate, Integer quarter, String whereString) {
		//���������� ��������� �� ������� �� ��������� (��� ����)
    	sql.append(
    			"Select foiv.cc as cc, foiv.name as name, foiv.thematic as thematic, sum(distinct foiv.hz) as hz  from ( " +
    			" Select" +
            	" 1 AS cc "+
            	" ,'����������� ������� �� ���������� �������� ������' as name "+
            	" ,name_t.string_value as thematic "+
            	" ,count(distinct c.card_id) as hz "+
            	" from card c "+
            	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
            	" JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
            	" 	extract('year' from reg_date.date_value) ="+ prYear.toString() +" and extract('quarter' from reg_date.date_value) ="+ prDate.toString() +" "+
            	" join card_version cv on cv.card_id = c.card_id and cv.version_date = (select coalesce ((select cv2.version_date from card_version cv2 where cv2.card_id = c.card_id " +
            	"							and cv2.version_date < date_trunc('day', TO_DATE(" + pYear.toString() + " || '-' || " + firstMonthCurQuarter.toString() + " || '-' || '1', 'YYYY-MM-DD') + interval '0 hour') + INTERVAL '1 DAY' " +
            	"							and cv2.version_date >= date_trunc('day', TO_DATE(" + pYear.toString() + " || '-' || " + firstMonthCurQuarter.toString() + " || '-' || '1', 'YYYY-MM-DD') + interval '0 hour') " +
            	"							and cv2.status_id in (101, 102, 103, 301) " +
            	"							limit 1 " +
  			    "							), " +
            	"		  					(select cv2.version_date from card_version cv2 where cv2.card_id = c.card_id " +
            	"							and cv2.version_date < date_trunc('day', TO_DATE(" + prYear.toString() + " || '-' || " + firstMonthCurQuarter.toString() + " || '-' || '1', 'YYYY-MM-DD') + interval '0 hour') " +
            	"		  					and cv2.status_id in (101, 102, 103, 301) " +
            	"		  					order by cv2.version_date desc " +
            	"		  					limit 1 " +
            	"		  					)) " +
            	" 		  																) " +
            	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
            	" JOIN attribute_value question ON question.card_id = c.card_id and question.attribute_code='ADMIN_277251' "+
            	" JOIN attribute_value topic ON topic.card_id = question.number_value and topic.attribute_code='ADMIN_278045' "+
            	" JOIN attribute_value  them ON them.card_id = topic.number_value and them.attribute_code='ADMIN_275720' "+
            	" JOIN attribute_value name_t ON name_t.card_id = them.number_value and name_t.attribute_code='NAME' "+
            	" JOIN card cc on cc.card_id = question.number_value " +
              	whereString+
              	" and cc.template_id = 884 " +
            	" GROUP BY name_t.string_value "+
            	"\n UNION \n"+
            	" Select  "+
            	" 1 AS cc "+
            	" ,'����������� ������� �� ���������� �������� ������' as name "+
            	" ,name_t.string_value as thematic "+
            	" ,count(distinct c.card_id) as hz "+
            	" from card c "+
            	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
            	" JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
            	" 	extract('year' from reg_date.date_value) ="+ prYear.toString() +" and extract('quarter' from reg_date.date_value) ="+ prDate.toString() +" "+
            	" join card_version cv on cv.card_id = c.card_id and cv.version_date = (select coalesce ((select cv2.version_date from card_version cv2 where cv2.card_id = c.card_id " +
            	"							and cv2.version_date < date_trunc('day', TO_DATE(" + pYear.toString() + " || '-' || " + firstMonthCurQuarter.toString() + " || '-' || '1', 'YYYY-MM-DD') + interval '0 hour') + INTERVAL '1 DAY' " +
            	"							and cv2.version_date >= date_trunc('day', TO_DATE(" + pYear.toString() + " || '-' || " + firstMonthCurQuarter.toString() + " || '-' || '1', 'YYYY-MM-DD') + interval '0 hour') " +
            	"							and cv2.status_id in (101, 102, 103, 301) " +
            	"							limit 1 " +
  			    "							), " +
            	"		  					(select cv2.version_date from card_version cv2 where cv2.card_id = c.card_id " +
            	"							and cv2.version_date < date_trunc('day', TO_DATE(" + prYear.toString() + " || '-' || " + firstMonthCurQuarter.toString() + " || '-' || '1', 'YYYY-MM-DD') + interval '0 hour') " +
            	"		  					and cv2.status_id in (101, 102, 103, 301) " +
            	"		  					order by cv2.version_date desc " +
            	"		  					limit 1 " +
            	"		  					)) " +
            	" 		  																) " +
            	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
            	" JOIN attribute_value subquestion ON subquestion.card_id = c.card_id and subquestion.attribute_code='ADMIN_277251' "+
            	" JOIN attribute_value question ON question.card_id = subquestion.number_value and question.attribute_code='JBR_QUESTION' "+
            	" JOIN attribute_value topic ON topic.card_id = question.number_value and topic.attribute_code='ADMIN_278045' "+
            	" JOIN attribute_value  them ON them.card_id = topic.number_value and them.attribute_code='ADMIN_275720' "+
            	" JOIN attribute_value name_t ON name_t.card_id = them.number_value and name_t.attribute_code='NAME' "+
            	" JOIN card cc on cc.card_id = subquestion.number_value " +
              	whereString+
              	" and cc.template_id = 885 " +
            	" GROUP BY name_t.string_value "+
            	" ) as foiv " +
            	" group by foiv.cc, foiv.name, foiv.thematic " +
            	"\n UNION \n" +
        "Select foiv.cc as cc, foiv.name as name, foiv.thematic as thematic, sum(distinct foiv.hz) as hz  from ( " +
    	" Select  "+
    	" 2 AS cc "+
    	" ,'" + config.getProperty(PARAM_RECEIVED, PARAM_NULL) + "' as name "+
    	" ,name_t.string_value || '" + QUESTION + "' as thematic "+
    	" ,count(distinct c.card_id) as hz "+
    	" from card c "+
    	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
    	 " JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
    	" 	extract('year' from reg_date.date_value) ="+ pYear.toString() +" and extract('quarter' from reg_date.date_value) ="+ quarter.toString() +" "+
    	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
    	" JOIN attribute_value question ON question.card_id = c.card_id and question.attribute_code='ADMIN_277251' "+
    	" JOIN attribute_value topic ON topic.card_id = question.number_value and topic.attribute_code='ADMIN_278045' "+
    	" JOIN attribute_value  them ON them.card_id = topic.number_value and them.attribute_code='ADMIN_275720' "+
    	" JOIN attribute_value name_t ON name_t.card_id = them.number_value and name_t.attribute_code='NAME' "+
    	" JOIN card cc on cc.card_id = question.number_value " +
      	whereString+
      	" and cc.template_id = 884 " +
    	" GROUP BY name_t.string_value "+
    	"\n UNION \n"+
    	" Select  "+
    	" 2 AS cc "+
    	" ,'" + config.getProperty(PARAM_RECEIVED, PARAM_NULL) + "' as name "+
    	" ,name_t.string_value || '" + SUBQUESTION + "' as thematic "+
    	" ,count(distinct c.card_id) as hz "+
    	" from card c "+
    	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
    	 " JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
    	" 	extract('year' from reg_date.date_value) ="+ pYear.toString() +" and extract('quarter' from reg_date.date_value) ="+ quarter.toString() +" "+
    	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
    	" JOIN attribute_value subquestion ON subquestion.card_id = c.card_id and subquestion.attribute_code='ADMIN_277251' "+
    	" JOIN attribute_value question ON question.card_id = subquestion.number_value and question.attribute_code='JBR_QUESTION' "+
    	" JOIN attribute_value topic ON topic.card_id = question.number_value and topic.attribute_code='ADMIN_278045' "+
    	" JOIN attribute_value  them ON them.card_id = topic.number_value and them.attribute_code='ADMIN_275720' "+
    	" JOIN attribute_value name_t ON name_t.card_id = them.number_value and name_t.attribute_code='NAME' "+
    	" JOIN card cc on cc.card_id = subquestion.number_value " +
      	whereString+
      	" and cc.template_id = 885 " +
    	" GROUP BY name_t.string_value "+
    	" ) as foiv " +
    	" group by foiv.cc, foiv.name, foiv.thematic " +
    	"\n UNION \n"+
    	"Select foiv.cc as cc, foiv.name as name, foiv.thematic as thematic, sum(distinct foiv.hz) as hz  from ( " +
    	" Select  "+
    	" 3 AS cc "+
    	" ,'          � ��� ����� ���������' as name "+
    	" ,name_t.string_value || '" + QUESTION +"' as thematic "+
    	" ,count(distinct c.card_id) as hz "+
    	" from card c "+
    	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
    	" JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
    	" 	extract('year' from reg_date.date_value) ="+ pYear.toString() +" and extract('quarter' from reg_date.date_value) ="+ quarter.toString() +" "+
    	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
    	" JOIN attribute_value format_obr ON format_obr.card_id = c.card_id and format_obr.attribute_code='JBR_ADDRESS_FORMAT' and format_obr.value_id=2037 "+
    	" JOIN attribute_value question ON question.card_id = c.card_id and question.attribute_code='ADMIN_277251' "+
    	" JOIN attribute_value topic ON topic.card_id = question.number_value and topic.attribute_code='ADMIN_278045' "+
    	" JOIN attribute_value  them ON them.card_id = topic.number_value and them.attribute_code='ADMIN_275720' "+
    	" JOIN attribute_value name_t ON name_t.card_id = them.number_value and name_t.attribute_code='NAME' "+
    	" JOIN card cc on cc.card_id = question.number_value " +
      	whereString+
      	" and cc.template_id = 884 " +
    	" GROUP BY name_t.string_value "+
    	"\n UNION \n"+
    	" Select  "+
    	" 3 AS cc "+
    	" ,'          � ��� ����� ���������' as name "+
    	" ,name_t.string_value || '" + SUBQUESTION +"' as thematic "+
    	" ,count(distinct c.card_id) as hz "+
    	" from card c "+
    	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
    	" JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
    	" 	extract('year' from reg_date.date_value) ="+ pYear.toString() +" and extract('quarter' from reg_date.date_value) ="+ quarter.toString() +" "+
    	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
    	" JOIN attribute_value format_obr ON format_obr.card_id = c.card_id and format_obr.attribute_code='JBR_ADDRESS_FORMAT' and format_obr.value_id=2037 "+
    	" JOIN attribute_value subquestion ON subquestion.card_id = c.card_id and subquestion.attribute_code='ADMIN_277251' "+
    	" JOIN attribute_value question ON question.card_id = subquestion.number_value and question.attribute_code='JBR_QUESTION' "+
    	" JOIN attribute_value topic ON topic.card_id = question.number_value and topic.attribute_code='ADMIN_278045' "+
    	" JOIN attribute_value  them ON them.card_id = topic.number_value and them.attribute_code='ADMIN_275720' "+
    	" JOIN attribute_value name_t ON name_t.card_id = them.number_value and name_t.attribute_code='NAME' "+
    	" JOIN card cc on cc.card_id = subquestion.number_value " +
      	whereString+
      	" and cc.template_id = 885 " +
    	" GROUP BY name_t.string_value "+
    	" ) as foiv " +
    	" group by foiv.cc, foiv.name, foiv.thematic " +
    	"\n UNION \n"+
    	"Select foiv.cc as cc, foiv.name as name, foiv.thematic as thematic, sum(distinct foiv.hz) as hz  from ( " +
    	" Select  "+
    	" 4 AS cc "+
    	" ,'          � ��� ����� �����' as name "+
    	" ,name_t.string_value || '" + QUESTION + "' as thematic "+
    	" ,count(distinct c.card_id) as hz "+
    	" from card c "+
    	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
    	"  JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
    		" extract('year' from reg_date.date_value) ="+ pYear.toString() +" and extract('quarter' from reg_date.date_value) ="+ quarter.toString() +" "+
    	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
    	" JOIN attribute_value format_obr ON format_obr.card_id = c.card_id and format_obr.attribute_code='JBR_ADDRESS_FORMAT' and format_obr.value_id=2036 "+
    	" JOIN attribute_value question ON question.card_id = c.card_id and question.attribute_code='ADMIN_277251' "+
    	" JOIN attribute_value topic ON topic.card_id = question.number_value and topic.attribute_code='ADMIN_278045' "+
    	" JOIN attribute_value  them ON them.card_id = topic.number_value and them.attribute_code='ADMIN_275720' "+
    	" JOIN attribute_value name_t ON name_t.card_id = them.number_value and name_t.attribute_code='NAME' "+
    	" JOIN card cc on cc.card_id = question.number_value " +
      	whereString+
      	" and cc.template_id = 884 " +
    	" GROUP BY name_t.string_value "+
    	"\n UNION \n"+
    	" Select  "+
    	" 4 AS cc "+
    	" ,'          � ��� ����� �����' as name "+
    	" ,name_t.string_value || '" + SUBQUESTION + "' as thematic "+
    	" ,count(distinct c.card_id) as hz "+
    	" from card c "+
    	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
    	"  JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
    		" extract('year' from reg_date.date_value) ="+ pYear.toString() +" and extract('quarter' from reg_date.date_value) ="+ quarter.toString() +" "+
    	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
    	" JOIN attribute_value format_obr ON format_obr.card_id = c.card_id and format_obr.attribute_code='JBR_ADDRESS_FORMAT' and format_obr.value_id=2036 "+
    	" JOIN attribute_value subquestion ON subquestion.card_id = c.card_id and subquestion.attribute_code='ADMIN_277251' "+
    	" JOIN attribute_value question ON question.card_id = subquestion.number_value and question.attribute_code='JBR_QUESTION' "+
    	" JOIN attribute_value topic ON topic.card_id = question.number_value and topic.attribute_code='ADMIN_278045' "+
    	" JOIN attribute_value  them ON them.card_id = topic.number_value and them.attribute_code='ADMIN_275720' "+
    	" JOIN attribute_value name_t ON name_t.card_id = them.number_value and name_t.attribute_code='NAME' "+
    	" JOIN card cc on cc.card_id = subquestion.number_value " +
      	whereString+
      	" and cc.template_id = 885 " +
    	" GROUP BY name_t.string_value "+
    	" ) as foiv " +
    	" group by foiv.cc, foiv.name, foiv.thematic " +
    	"\n UNION \n"+
    	"Select foiv.cc as cc, foiv.name as name, foiv.thematic as thematic, sum(distinct foiv.hz) as hz  from ( " +
    	" Select  "+
    	" 5 AS cc "+
    	" ,'          � ��� ����� �����������' as name "+
    	" ,name_t.string_value || '" + QUESTION + "' as thematic "+
    	" ,count(distinct c.card_id) as hz "+
    	" from card c "+
    	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
    	 " JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
    	" 	extract('year' from reg_date.date_value) ="+ pYear.toString() +" and extract('quarter' from reg_date.date_value) ="+ quarter.toString() +" "+
    	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
    	" JOIN attribute_value format_obr ON format_obr.card_id = c.card_id and format_obr.attribute_code='JBR_ADDRESS_FORMAT' and format_obr.value_id=2038 "+
    	" JOIN attribute_value question ON question.card_id = c.card_id and question.attribute_code='ADMIN_277251' "+
    	" JOIN attribute_value topic ON topic.card_id = question.number_value and topic.attribute_code='ADMIN_278045' "+
    	" JOIN attribute_value  them ON them.card_id = topic.number_value and them.attribute_code='ADMIN_275720' "+
    	" JOIN attribute_value name_t ON name_t.card_id = them.number_value and name_t.attribute_code='NAME' "+
    	" JOIN card cc on cc.card_id = question.number_value " +
      	whereString+
      	" and cc.template_id = 884 " +
    	" GROUP BY name_t.string_value "+
    	"\n UNION \n"+
    	" Select  "+
    	" 5 AS cc "+
    	" ,'          � ��� ����� �����������' as name "+
    	" ,name_t.string_value || '" + SUBQUESTION + "' as thematic "+
    	" ,count(distinct c.card_id) as hz "+
    	" from card c "+
    	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
    	 " JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
    	" 	extract('year' from reg_date.date_value) ="+ pYear.toString() +" and extract('quarter' from reg_date.date_value) ="+ quarter.toString() +" "+
    	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
    	" JOIN attribute_value format_obr ON format_obr.card_id = c.card_id and format_obr.attribute_code='JBR_ADDRESS_FORMAT' and format_obr.value_id=2038 "+
    	" JOIN attribute_value subquestion ON subquestion.card_id = c.card_id and subquestion.attribute_code='ADMIN_277251' "+
    	" JOIN attribute_value question ON question.card_id = subquestion.number_value and question.attribute_code='JBR_QUESTION' "+
    	" JOIN attribute_value topic ON topic.card_id = question.number_value and topic.attribute_code='ADMIN_278045' "+
    	" JOIN attribute_value  them ON them.card_id = topic.number_value and them.attribute_code='ADMIN_275720' "+
    	" JOIN attribute_value name_t ON name_t.card_id = them.number_value and name_t.attribute_code='NAME' "+
    	" JOIN card cc on cc.card_id = subquestion.number_value " +
      	whereString+
      	" and cc.template_id = 885 " +
    	" GROUP BY name_t.string_value "+
    	" ) as foiv " +
    	" group by foiv.cc, foiv.name, foiv.thematic " +
    	"\n UNION \n"+
    	"Select foiv.cc as cc, foiv.name as name, foiv.thematic as thematic, sum(distinct foiv.hz) as hz  from ( " +
    	" Select  "+
    	" 7 AS cc "+
    	" ,'          " + config.getProperty(PARAM_REPORTED, PARAM_NULL) + "' as name "+
    	" ,name_t.string_value || '" + QUESTION + "' as thematic "+
    	" ,count(distinct c.card_id) as hz "+
    	" from card c "+
    	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
    	 " JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
    	" 	extract('year' from reg_date.date_value) ="+ pYear.toString() +" and extract('quarter' from reg_date.date_value) ="+ quarter.toString() +" "+
    	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
    	" JOIN attribute_value gub ON gub.card_id = c.card_id and gub.attribute_code='JBR_IMPL_ACQUAINTERS' "+
    	" JOIN person p ON p.person_id = gub.number_value "+
    	" JOIN person_role pr ON pr.person_id = p.person_id and pr.role_code in ('JBR_BOSS') " +
    	" JOIN attribute_value question ON question.card_id = c.card_id and question.attribute_code='ADMIN_277251' "+
    	" JOIN attribute_value topic ON topic.card_id = question.number_value and topic.attribute_code='ADMIN_278045' "+
    	" JOIN attribute_value  them ON them.card_id = topic.number_value and them.attribute_code='ADMIN_275720' "+
    	" JOIN attribute_value name_t ON name_t.card_id = them.number_value and name_t.attribute_code='NAME' "+
    	" JOIN card cc on cc.card_id = question.number_value " +
      	whereString+
      	" and cc.template_id = 884 " +
    	" GROUP BY name_t.string_value "+
    	"\n UNION \n"+
    	" Select  "+
    	" 7 AS cc "+
    	" ,'          " + config.getProperty(PARAM_REPORTED, PARAM_NULL) + "' as name "+
    	" ,name_t.string_value || '" + SUBQUESTION + "' as thematic "+
    	" ,count(distinct c.card_id) as hz "+
    	" from card c "+
    	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
    	 " JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
    	" 	extract('year' from reg_date.date_value) ="+ pYear.toString() +" and extract('quarter' from reg_date.date_value) ="+ quarter.toString() +" "+
    	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
    	" JOIN attribute_value gub ON gub.card_id = c.card_id and gub.attribute_code='JBR_IMPL_ACQUAINTERS' "+
    	" JOIN person p ON p.person_id = gub.number_value "+
    	" JOIN person_role pr ON pr.person_id = p.person_id and pr.role_code in ('JBR_BOSS') " +
    	" JOIN attribute_value subquestion ON subquestion.card_id = c.card_id and subquestion.attribute_code='ADMIN_277251' "+
    	" JOIN attribute_value question ON question.card_id = subquestion.number_value and question.attribute_code='JBR_QUESTION' "+
    	" JOIN attribute_value topic ON topic.card_id = question.number_value and topic.attribute_code='ADMIN_278045' "+
    	" JOIN attribute_value  them ON them.card_id = topic.number_value and them.attribute_code='ADMIN_275720' "+
    	" JOIN attribute_value name_t ON name_t.card_id = them.number_value and name_t.attribute_code='NAME' "+
    	" JOIN card cc on cc.card_id = subquestion.number_value " +
      	whereString+
      	" and cc.template_id = 885 " +
    	" GROUP BY name_t.string_value "+
    	" ) as foiv " +
    	" group by foiv.cc, foiv.name, foiv.thematic " +
    	"\n UNION \n"+
    	"Select foiv.cc as cc, foiv.name as name, foiv.thematic as thematic, sum(distinct foiv.hz) as hz  from ( " +
    	" Select "+
    	" 8 AS cc "+
    	" ,'          ����� �� ��������' as name "+
    	" ,name_t.string_value || '" + QUESTION + "' as thematic "+
    	" ,count(distinct c.card_id) as hz "+
    	" from card c "+
    	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
    	" JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
    		" extract('year' from reg_date.date_value) ="+ pYear.toString() +" and extract('quarter' from reg_date.date_value) ="+ quarter.toString() +" "+
    	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
    	" JOIN attribute_value av_contrl ON av_contrl.card_id = c.card_id and av_contrl.attribute_code='JBR_IMPL_ONCONT' and av_contrl.value_id=1432 "+
    	" JOIN attribute_value question ON question.card_id = c.card_id and question.attribute_code='ADMIN_277251' "+
    	" JOIN attribute_value topic ON topic.card_id = question.number_value and topic.attribute_code='ADMIN_278045' "+
    	" JOIN attribute_value  them ON them.card_id = topic.number_value and them.attribute_code='ADMIN_275720' "+
    	" JOIN attribute_value name_t ON name_t.card_id = them.number_value and name_t.attribute_code='NAME' "+
    	" JOIN card cc on cc.card_id = question.number_value " +
      	whereString+
      	" and cc.template_id = 884 " +
    	" GROUP BY name_t.string_value "+
    	"\n UNION \n"+
    	" Select "+
    	" 8 AS cc "+
    	" ,'          ����� �� ��������' as name "+
    	" ,name_t.string_value || '" + SUBQUESTION + "' as thematic "+
    	" ,count(distinct c.card_id) as hz "+
    	" from card c "+
    	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
    	" JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
    		" extract('year' from reg_date.date_value) ="+ pYear.toString() +" and extract('quarter' from reg_date.date_value) ="+ quarter.toString() +" "+
    	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
    	" JOIN attribute_value av_contrl ON av_contrl.card_id = c.card_id and av_contrl.attribute_code='JBR_IMPL_ONCONT' and av_contrl.value_id=1432 "+
    	" JOIN attribute_value subquestion ON subquestion.card_id = c.card_id and subquestion.attribute_code='ADMIN_277251' "+
    	" JOIN attribute_value question ON question.card_id = subquestion.number_value and question.attribute_code='JBR_QUESTION' "+
    	" JOIN attribute_value topic ON topic.card_id = question.number_value and topic.attribute_code='ADMIN_278045' "+
    	" JOIN attribute_value  them ON them.card_id = topic.number_value and them.attribute_code='ADMIN_275720' "+
    	" JOIN attribute_value name_t ON name_t.card_id = them.number_value and name_t.attribute_code='NAME' "+
    	" JOIN card cc on cc.card_id = subquestion.number_value " +
      	whereString+
      	" and cc.template_id = 885 " +
    	" GROUP BY name_t.string_value "+
    	" ) as foiv " +
    	" group by foiv.cc, foiv.name, foiv.thematic " +
    	"\n UNION \n"+
    	"Select foiv.cc as cc, foiv.name as name, foiv.thematic as thematic, sum(distinct foiv.hz) as hz  from ( " +
    	" Select "+
    	" 9 AS cc "+
    	" ,'          ����������� � ������� �� �����' as name "+
    	" ,name_t.string_value || '" + QUESTION + "' as thematic "+
    	" ,count(distinct c.card_id) as hz "+
    	" from card c "+
    	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
    	 " JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
    	" 	extract('year' from reg_date.date_value) ="+ pYear.toString() +" and extract('quarter' from reg_date.date_value) ="+ quarter.toString() +" "+
    	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
    	" JOIN attribute_value av_contrl ON av_contrl.card_id = c.card_id and av_contrl.attribute_code='JBR_EXAMIN_PROCEDURE' and av_contrl.value_id=2046 "+
    	" JOIN attribute_value question ON question.card_id = c.card_id and question.attribute_code='ADMIN_277251' "+
    	" JOIN attribute_value topic ON topic.card_id = question.number_value and topic.attribute_code='ADMIN_278045' "+
    	" JOIN attribute_value  them ON them.card_id = topic.number_value and them.attribute_code='ADMIN_275720' "+
    	" JOIN attribute_value name_t ON name_t.card_id = them.number_value and name_t.attribute_code='NAME' "+
    	" JOIN card cc on cc.card_id = question.number_value " +
      	whereString+
      	" and cc.template_id = 884 " +
    	" GROUP BY name_t.string_value "+
    	"\n UNION \n"+
    	" Select "+
    	" 9 AS cc "+
    	" ,'          ����������� � ������� �� �����' as name "+
    	" ,name_t.string_value || '" + SUBQUESTION + "' as thematic "+
    	" ,count(distinct c.card_id) as hz "+
    	" from card c "+
    	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
    	 " JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
    	" 	extract('year' from reg_date.date_value) ="+ pYear.toString() +" and extract('quarter' from reg_date.date_value) ="+ quarter.toString() +" "+
    	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
    	" JOIN attribute_value av_contrl ON av_contrl.card_id = c.card_id and av_contrl.attribute_code='JBR_EXAMIN_PROCEDURE' and av_contrl.value_id=2046 "+
    	" JOIN attribute_value subquestion ON subquestion.card_id = c.card_id and subquestion.attribute_code='ADMIN_277251' "+
    	" JOIN attribute_value question ON question.card_id = subquestion.number_value and question.attribute_code='JBR_QUESTION' "+
    	" JOIN attribute_value topic ON topic.card_id = question.number_value and topic.attribute_code='ADMIN_278045' "+
    	" JOIN attribute_value  them ON them.card_id = topic.number_value and them.attribute_code='ADMIN_275720' "+
    	" JOIN attribute_value name_t ON name_t.card_id = them.number_value and name_t.attribute_code='NAME' "+
    	" JOIN card cc on cc.card_id = subquestion.number_value " +
      	whereString+
      	" and cc.template_id = 885 " +
    	" GROUP BY name_t.string_value "+
    	" ) as foiv " +
    	" group by foiv.cc, foiv.name, foiv.thematic " +
    	"\n UNION \n"+
    	"Select foiv.cc as cc, foiv.name as name, foiv.thematic as thematic, sum(distinct foiv.hz) as hz  from ( " +
    	" Select "+
    	" 10 AS cc "+
    	" ,'          ����������� ������������' as name "+
    	" ,name_t.string_value || '" + QUESTION + "' as thematic "+
    	" ,count(distinct c.card_id) as hz "+
    	" from card c "+
    	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
    	"  JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
    		" extract('year' from reg_date.date_value) ="+ pYear.toString() +" and extract('quarter' from reg_date.date_value) ="+ quarter.toString() +" "+
    	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
    	" JOIN attribute_value av_contrl ON av_contrl.card_id = c.card_id and av_contrl.attribute_code='JBR_EXAMIN_PROCEDURE' and av_contrl.value_id=2047 "+
    	" JOIN attribute_value question ON question.card_id = c.card_id and question.attribute_code='ADMIN_277251' "+
    	" JOIN attribute_value topic ON topic.card_id = question.number_value and topic.attribute_code='ADMIN_278045' "+
    	" JOIN attribute_value  them ON them.card_id = topic.number_value and them.attribute_code='ADMIN_275720' "+
    	" JOIN attribute_value name_t ON name_t.card_id = them.number_value and name_t.attribute_code='NAME' "+
    	" JOIN card cc on cc.card_id = question.number_value " +
      	whereString+
      	" and cc.template_id = 884 " +
    	" GROUP BY name_t.string_value "+
    	"\n UNION \n"+
    	" Select "+
    	" 10 AS cc "+
    	" ,'          ����������� ������������' as name "+
    	" ,name_t.string_value || '" + SUBQUESTION + "' as thematic "+
    	" ,count(distinct c.card_id) as hz "+
    	" from card c "+
    	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
    	"  JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
    		" extract('year' from reg_date.date_value) ="+ pYear.toString() +" and extract('quarter' from reg_date.date_value) ="+ quarter.toString() +" "+
    	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
    	" JOIN attribute_value av_contrl ON av_contrl.card_id = c.card_id and av_contrl.attribute_code='JBR_EXAMIN_PROCEDURE' and av_contrl.value_id=2047 "+
    	" JOIN attribute_value subquestion ON subquestion.card_id = c.card_id and subquestion.attribute_code='ADMIN_277251' "+
    	" JOIN attribute_value question ON question.card_id = subquestion.number_value and question.attribute_code='JBR_QUESTION' "+
    	" JOIN attribute_value topic ON topic.card_id = question.number_value and topic.attribute_code='ADMIN_278045' "+
    	" JOIN attribute_value  them ON them.card_id = topic.number_value and them.attribute_code='ADMIN_275720' "+
    	" JOIN attribute_value name_t ON name_t.card_id = them.number_value and name_t.attribute_code='NAME' "+
    	" JOIN card cc on cc.card_id = subquestion.number_value " +
      	whereString+
      	" and cc.template_id = 885 " +
    	" GROUP BY name_t.string_value "+
    	" ) as foiv " +
    	" group by foiv.cc, foiv.name, foiv.thematic " +
    	"\n UNION \n"+
    	"Select foiv.cc as cc, foiv.name as name, foiv.thematic as thematic, sum(distinct foiv.hz) as hz  from ( " +
    	" Select "+
    	" 12 AS cc "+
    	" ,'          ����������' as name "+
    	" ,name_t.string_value || '" + QUESTION + "' as thematic "+
    	" ,count(distinct c.card_id) as hz "+
    	" from card c "+
    	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
    	 " JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
    	" 	extract('year' from reg_date.date_value) ="+ pYear.toString() +" and extract('quarter' from reg_date.date_value) ="+ quarter.toString() +" "+
    	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
    	" JOIN attribute_value av_contrl ON av_contrl.card_id = c.card_id and av_contrl.attribute_code='ADMIN_283929' and av_contrl.value_id in (Select value_id from values_list where value_rus='����������' or "+ " value_rus='���� �������') "+
    	" JOIN attribute_value question ON question.card_id = c.card_id and question.attribute_code='ADMIN_277251' "+
    	" JOIN attribute_value topic ON topic.card_id = question.number_value and topic.attribute_code='ADMIN_278045' "+
    	" JOIN attribute_value  them ON them.card_id = topic.number_value and them.attribute_code='ADMIN_275720' "+
    	" JOIN attribute_value name_t ON name_t.card_id = them.number_value and name_t.attribute_code='NAME' "+
    	" JOIN card cc on cc.card_id = question.number_value " +
      	whereString+
      	" and cc.template_id = 884 " +
    	" GROUP BY name_t.string_value "+
    	"\n UNION \n"+
    	" Select "+
    	" 12 AS cc "+
    	" ,'          ����������' as name "+
    	" ,name_t.string_value || '" + SUBQUESTION + "' as thematic "+
    	" ,count(distinct c.card_id) as hz "+
    	" from card c "+
    	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
    	 " JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
    	" 	extract('year' from reg_date.date_value) ="+ pYear.toString() +" and extract('quarter' from reg_date.date_value) ="+ quarter.toString() +" "+
    	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
    	" JOIN attribute_value av_contrl ON av_contrl.card_id = c.card_id and av_contrl.attribute_code='ADMIN_283929' and av_contrl.value_id in (Select value_id from values_list where value_rus='����������' or "+ " value_rus='���� �������') "+
    	" JOIN attribute_value subquestion ON subquestion.card_id = c.card_id and subquestion.attribute_code='ADMIN_277251' "+
    	" JOIN attribute_value question ON question.card_id = subquestion.number_value and question.attribute_code='JBR_QUESTION' "+
    	" JOIN attribute_value topic ON topic.card_id = question.number_value and topic.attribute_code='ADMIN_278045' "+
    	" JOIN attribute_value  them ON them.card_id = topic.number_value and them.attribute_code='ADMIN_275720' "+
    	" JOIN attribute_value name_t ON name_t.card_id = them.number_value and name_t.attribute_code='NAME' "+
    	" JOIN card cc on cc.card_id = subquestion.number_value " +
      	whereString+
      	" and cc.template_id = 885 " +
    	" GROUP BY name_t.string_value "+
    	" ) as foiv " +
    	" group by foiv.cc, foiv.name, foiv.thematic " +
    	"\n UNION \n"+
    	"Select foiv.cc as cc, foiv.name as name, foiv.thematic as thematic, sum(distinct foiv.hz) as hz  from ( " +
    	" Select "+
    	" 13 AS cc "+
    	" ,'          � ��� ����� ���� �������' as name "+
    	" ,name_t.string_value || '" + QUESTION + "' as thematic "+
    	" ,count(distinct c.card_id) as hz "+
    	" from card c "+
    	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
    	 " JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
    	" 	extract('year' from reg_date.date_value) ="+ pYear.toString() +" and extract('quarter' from reg_date.date_value) ="+ quarter.toString() +" "+
    	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
    	" JOIN attribute_value av_contrl ON av_contrl.card_id = c.card_id and av_contrl.attribute_code='ADMIN_283929' and av_contrl.value_id in (Select value_id from values_list where value_rus='���� �������') "+
    	" JOIN attribute_value question ON question.card_id = c.card_id and question.attribute_code='ADMIN_277251' "+
    	" JOIN attribute_value topic ON topic.card_id = question.number_value and topic.attribute_code='ADMIN_278045' "+
    	" JOIN attribute_value  them ON them.card_id = topic.number_value and them.attribute_code='ADMIN_275720' "+
    	" JOIN attribute_value name_t ON name_t.card_id = them.number_value and name_t.attribute_code='NAME' "+
    	" JOIN card cc on cc.card_id = question.number_value " +
      	whereString+
      	" and cc.template_id = 884 " +
    	" GROUP BY name_t.string_value "+
    	"\n UNION \n"+
    	" Select "+
    	" 13 AS cc "+
    	" ,'          � ��� ����� ���� �������' as name "+
    	" ,name_t.string_value || '" + SUBQUESTION  + "' as thematic "+
    	" ,count(distinct c.card_id) as hz "+
    	" from card c "+
    	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
    	 " JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
    	" 	extract('year' from reg_date.date_value) ="+ pYear.toString() +" and extract('quarter' from reg_date.date_value) ="+ quarter.toString() +" "+
    	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
    	" JOIN attribute_value av_contrl ON av_contrl.card_id = c.card_id and av_contrl.attribute_code='ADMIN_283929' and av_contrl.value_id in (Select value_id from values_list where value_rus='���� �������') "+
    	" JOIN attribute_value subquestion ON subquestion.card_id = c.card_id and subquestion.attribute_code='ADMIN_277251' "+
    	" JOIN attribute_value question ON question.card_id = subquestion.number_value and question.attribute_code='JBR_QUESTION' "+
    	" JOIN attribute_value topic ON topic.card_id = question.number_value and topic.attribute_code='ADMIN_278045' "+
    	" JOIN attribute_value  them ON them.card_id = topic.number_value and them.attribute_code='ADMIN_275720' "+
    	" JOIN attribute_value name_t ON name_t.card_id = them.number_value and name_t.attribute_code='NAME' "+
    	" JOIN card cc on cc.card_id = subquestion.number_value " +
      	whereString+
      	" and cc.template_id = 885 " +
    	" GROUP BY name_t.string_value "+
    	" ) as foiv " +
    	" group by foiv.cc, foiv.name, foiv.thematic " +
    	"\n UNION \n"+
    	"Select foiv.cc as cc, foiv.name as name, foiv.thematic as thematic, sum(distinct foiv.hz) as hz  from ( " +
    	" Select "+
    	" 14 AS cc "+
    	" ,'          ����������' as name "+
    	" ,name_t.string_value || '" + QUESTION + "' as thematic "+
    	" ,count(distinct c.card_id) as hz "+
    	" from card c "+
    	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
    	"  JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
    		" extract('year' from reg_date.date_value) ="+ pYear.toString() +" and extract('quarter' from reg_date.date_value) ="+ quarter.toString() +" "+
    	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
    	" JOIN attribute_value av_contrl ON av_contrl.card_id = c.card_id and av_contrl.attribute_code='ADMIN_283929' and av_contrl.value_id in (Select value_id from values_list where value_rus='����������') "+
    	" JOIN attribute_value question ON question.card_id = c.card_id and question.attribute_code='ADMIN_277251' "+
    	" JOIN attribute_value topic ON topic.card_id = question.number_value and topic.attribute_code='ADMIN_278045' "+
    	" JOIN attribute_value  them ON them.card_id = topic.number_value and them.attribute_code='ADMIN_275720' "+
    	" JOIN attribute_value name_t ON name_t.card_id = them.number_value and name_t.attribute_code='NAME' "+
    	" JOIN card cc on cc.card_id = question.number_value " +
      	whereString+
      	" and cc.template_id = 884 " +
    	" GROUP BY name_t.string_value "+
    	"\n UNION \n"+
    	" Select "+
    	" 14 AS cc "+
    	" ,'          ����������' as name "+
    	" ,name_t.string_value || '" + SUBQUESTION + "' as thematic "+
    	" ,count(distinct c.card_id) as hz "+
    	" from card c "+
    	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
    	"  JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
    		" extract('year' from reg_date.date_value) ="+ pYear.toString() +" and extract('quarter' from reg_date.date_value) ="+ quarter.toString() +" "+
    	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
    	" JOIN attribute_value av_contrl ON av_contrl.card_id = c.card_id and av_contrl.attribute_code='ADMIN_283929' and av_contrl.value_id in (Select value_id from values_list where value_rus='����������') "+
    	" JOIN attribute_value subquestion ON subquestion.card_id = c.card_id and subquestion.attribute_code='ADMIN_277251' "+
    	" JOIN attribute_value question ON question.card_id = subquestion.number_value and question.attribute_code='JBR_QUESTION' "+
    	" JOIN attribute_value topic ON topic.card_id = question.number_value and topic.attribute_code='ADMIN_278045' "+
    	" JOIN attribute_value  them ON them.card_id = topic.number_value and them.attribute_code='ADMIN_275720' "+
    	" JOIN attribute_value name_t ON name_t.card_id = them.number_value and name_t.attribute_code='NAME' "+
    	" JOIN card cc on cc.card_id = subquestion.number_value " +
      	whereString+
      	" and cc.template_id = 885 " +
    	" GROUP BY name_t.string_value "+
    	" ) as foiv " +
    	" group by foiv.cc, foiv.name, foiv.thematic " +
    	"\n UNION \n"+
    	"Select foiv.cc as cc, foiv.name as name, foiv.thematic as thematic, sum(distinct foiv.hz) as hz  from ( " +
    	" Select "+
    	" 15 AS cc "+
    	" ,'          �� ����������' as name "+
    	" ,name_t.string_value || '" + QUESTION + "' as thematic "+
    	" ,count(distinct c.card_id) as hz "+
    	" from card c "+
    	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
    	 " JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
    	" 	extract('year' from reg_date.date_value) ="+ pYear.toString() +" and extract('quarter' from reg_date.date_value) ="+ quarter.toString() +" "+
    	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
    	" JOIN attribute_value av_contrl ON av_contrl.card_id = c.card_id and av_contrl.attribute_code='ADMIN_283929' and av_contrl.value_id in (Select value_id from values_list where value_rus='�� ����������') "+
    	" JOIN attribute_value question ON question.card_id = c.card_id and question.attribute_code='ADMIN_277251' "+
    	" JOIN attribute_value topic ON topic.card_id = question.number_value and topic.attribute_code='ADMIN_278045' "+
    	" JOIN attribute_value  them ON them.card_id = topic.number_value and them.attribute_code='ADMIN_275720' "+
    	" JOIN attribute_value name_t ON name_t.card_id = them.number_value and name_t.attribute_code='NAME' "+
    	" JOIN card cc on cc.card_id = question.number_value " +
      	whereString+
      	" and cc.template_id = 884 " +
    	" GROUP BY name_t.string_value "+
    	"\n UNION \n"+
    	" Select "+
    	" 15 AS cc "+
    	" ,'          �� ����������' as name "+
    	" ,name_t.string_value || '" + SUBQUESTION + "' as thematic "+
    	" ,count(distinct c.card_id) as hz "+
    	" from card c "+
    	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
    	 " JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
    	" 	extract('year' from reg_date.date_value) ="+ pYear.toString() +" and extract('quarter' from reg_date.date_value) ="+ quarter.toString() +" "+
    	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
    	" JOIN attribute_value av_contrl ON av_contrl.card_id = c.card_id and av_contrl.attribute_code='ADMIN_283929' and av_contrl.value_id in (Select value_id from values_list where value_rus='�� ����������') "+
    	" JOIN attribute_value subquestion ON subquestion.card_id = c.card_id and subquestion.attribute_code='ADMIN_277251' "+
    	" JOIN attribute_value question ON question.card_id = subquestion.number_value and question.attribute_code='JBR_QUESTION' "+
    	" JOIN attribute_value topic ON topic.card_id = question.number_value and topic.attribute_code='ADMIN_278045' "+
    	" JOIN attribute_value  them ON them.card_id = topic.number_value and them.attribute_code='ADMIN_275720' "+
    	" JOIN attribute_value name_t ON name_t.card_id = them.number_value and name_t.attribute_code='NAME' "+
    	" JOIN card cc on cc.card_id = subquestion.number_value " +
      	whereString+
      	" and cc.template_id = 885 " +
    	" GROUP BY name_t.string_value "+
    	" ) as foiv " +
    	" group by foiv.cc, foiv.name, foiv.thematic " +
    	"\n UNION \n"+
    	"Select foiv.cc as cc, foiv.name as name, foiv.thematic as thematic, sum(distinct foiv.hz) as hz  from ( " +
    	" Select "+
    	" 16 AS cc "+
    	" ,'" + config.getProperty(PARAM_SENT, PARAM_NULL) + "' as name "+
    	" ,name_t.string_value || '" + QUESTION + "' as thematic "+
    	" ,count(distinct c.card_id) as hz "+
    	" from card c "+
    	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
    	"  JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
    		" extract('year' from reg_date.date_value) ="+ pYear.toString() +" and extract('quarter' from reg_date.date_value) ="+ quarter.toString() +" "+
    	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
    	" JOIN attribute_value relate_doc ON relate_doc.card_id = c.card_id and relate_doc.attribute_code='JBR_DOCL_RELATDOC' "+
    	" JOIN card relate_doc_card on relate_doc_card.card_id=relate_doc.number_value and relate_doc_card.template_id=364 "+
    		" and relate_doc_card.status_id=101 "+
    	" JOIN attribute_value gub ON gub.card_id = relate_doc_card.card_id and gub.attribute_code='JBR_INFD_SIGNATORY' "+
    	" JOIN person p ON p.person_id = gub.number_value "+
    	" JOIN person_role pr ON pr.person_id = p.person_id and pr.role_code in ('JBR_BOSS','JBR_DEPUTY_MINISTER') " +
    	" JOIN attribute_value question ON question.card_id = c.card_id and question.attribute_code='ADMIN_277251' "+
    	" JOIN attribute_value topic ON topic.card_id = question.number_value and topic.attribute_code='ADMIN_278045' "+
    	" JOIN attribute_value  them ON them.card_id = topic.number_value and them.attribute_code='ADMIN_275720' "+
    	" JOIN attribute_value name_t ON name_t.card_id = them.number_value and name_t.attribute_code='NAME' "+
    	" JOIN card cc on cc.card_id = question.number_value " +
      	whereString+
      	" and cc.template_id = 884 " +
    	" GROUP BY name_t.string_value "+
    	"\n UNION \n"+
    	" Select "+
    	" 16 AS cc "+
    	" ,'" + config.getProperty(PARAM_SENT, PARAM_NULL) + "' as name "+
    	" ,name_t.string_value || '" + SUBQUESTION + "' as thematic "+
    	" ,count(distinct c.card_id) as hz "+
    	" from card c "+
    	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
    	"  JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
    		" extract('year' from reg_date.date_value) ="+ pYear.toString() +" and extract('quarter' from reg_date.date_value) ="+ quarter.toString() +" "+
    	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
    	" JOIN attribute_value relate_doc ON relate_doc.card_id = c.card_id and relate_doc.attribute_code='JBR_DOCL_RELATDOC' "+
    	" JOIN card relate_doc_card on relate_doc_card.card_id=relate_doc.number_value and relate_doc_card.template_id=364 "+
    		" and relate_doc_card.status_id=101 "+
    	" JOIN attribute_value gub ON gub.card_id = relate_doc_card.card_id and gub.attribute_code='JBR_INFD_SIGNATORY' "+
    	" JOIN person p ON p.person_id = gub.number_value "+
    	" JOIN person_role pr ON pr.person_id = p.person_id and pr.role_code in ('JBR_BOSS','JBR_DEPUTY_MINISTER') " +
    	" JOIN attribute_value subquestion ON subquestion.card_id = c.card_id and subquestion.attribute_code='ADMIN_277251' "+
    	" JOIN attribute_value question ON question.card_id = subquestion.number_value and question.attribute_code='JBR_QUESTION' "+
    	" JOIN attribute_value topic ON topic.card_id = question.number_value and topic.attribute_code='ADMIN_278045' "+
    	" JOIN attribute_value  them ON them.card_id = topic.number_value and them.attribute_code='ADMIN_275720' "+
    	" JOIN attribute_value name_t ON name_t.card_id = them.number_value and name_t.attribute_code='NAME' "+
    	" JOIN card cc on cc.card_id = subquestion.number_value " +
      	whereString+
      	" and cc.template_id = 885 " +
    	" GROUP BY name_t.string_value "+
    	" ) as foiv " +
    	" group by foiv.cc, foiv.name, foiv.thematic " +
    	"\n UNION \n"+
    	"Select foiv.cc as cc, foiv.name as name, foiv.thematic as thematic, sum(distinct foiv.hz) as hz  from ( " +
    	" Select  "+
    	" 17 AS cc "+
    	" ,'���������� ���������, ������������� ��������� � �������� �������� ��������������' as name "+
    	" ,name_t.string_value || '" + QUESTION + "' as thematic "+
    	" ,count(distinct c.card_id) as hz "+
    	" from card c "+
    	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
    	 " JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
    	" 	extract('year' from reg_date.date_value) ="+ pYear.toString() +" and extract('quarter' from reg_date.date_value) ="+ quarter.toString() +" "+
    	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
    	" JOIN attribute_value cont ON cont.card_id = c.card_id and cont.attribute_code='JBR_EXAMIN_PROCEDURE' and cont.value_id=2049 "+
    	" JOIN attribute_value question ON question.card_id = c.card_id and question.attribute_code='ADMIN_277251' "+
    	" JOIN attribute_value topic ON topic.card_id = question.number_value and topic.attribute_code='ADMIN_278045' "+
    	" JOIN attribute_value  them ON them.card_id = topic.number_value and them.attribute_code='ADMIN_275720' "+
    	" JOIN attribute_value name_t ON name_t.card_id = them.number_value and name_t.attribute_code='NAME' "+
    	" JOIN card cc on cc.card_id = question.number_value " +
      	whereString+
      	" and cc.template_id = 884 " +
    	" GROUP BY name_t.string_value "+
    	"\n UNION \n"+
    	" Select  "+
    	" 17 AS cc "+
    	" ,'���������� ���������, ������������� ��������� � �������� �������� ��������������' as name "+
    	" ,name_t.string_value || '" + SUBQUESTION + "' as thematic "+
    	" ,count(distinct c.card_id) as hz "+
    	" from card c "+
    	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
    	 " JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
    	" 	extract('year' from reg_date.date_value) ="+ pYear.toString() +" and extract('quarter' from reg_date.date_value) ="+ quarter.toString() +" "+
    	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
    	" JOIN attribute_value cont ON cont.card_id = c.card_id and cont.attribute_code='JBR_EXAMIN_PROCEDURE' and cont.value_id=2049 "+
    	" JOIN attribute_value subquestion ON subquestion.card_id = c.card_id and subquestion.attribute_code='ADMIN_277251' "+
    	" JOIN attribute_value question ON question.card_id = subquestion.number_value and question.attribute_code='JBR_QUESTION' "+
    	" JOIN attribute_value topic ON topic.card_id = question.number_value and topic.attribute_code='ADMIN_278045' "+
    	" JOIN attribute_value  them ON them.card_id = topic.number_value and them.attribute_code='ADMIN_275720' "+
    	" JOIN attribute_value name_t ON name_t.card_id = them.number_value and name_t.attribute_code='NAME' "+
    	" JOIN card cc on cc.card_id = subquestion.number_value " +
      	whereString+
      	" and cc.template_id = 885 " +
    	" GROUP BY name_t.string_value "+
    	" ) as foiv " +
    	" group by foiv.cc, foiv.name, foiv.thematic " +
    	"\n UNION \n"+
    	"Select foiv.cc as cc, foiv.name as name, foiv.thematic as thematic, sum(distinct foiv.hz) as hz  from ( " +
    	" Select  "+
    	" 18 AS cc "+
    	" ,'���������� ���������, ������������� ��������� � ���������������� �������� ����������� ������� �������������� ������' as name "+
    	" ,name_t.string_value || '" + QUESTION + "' as thematic "+
    	" ,count(distinct c.card_id) as hz "+
    	" from card c "+
    	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
    	"  JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
    		" extract('year' from reg_date.date_value) ="+ pYear.toString() +" and extract('quarter' from reg_date.date_value) ="+ quarter.toString() +" "+
    	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
    	" JOIN attribute_value cont ON cont.card_id = c.card_id and cont.attribute_code='JBR_EXAMIN_PROCEDURE' and cont.value_id=2048 "+
    	" JOIN attribute_value question ON question.card_id = c.card_id and question.attribute_code='ADMIN_277251' "+
    	" JOIN attribute_value topic ON topic.card_id = question.number_value and topic.attribute_code='ADMIN_278045' "+
    	" JOIN attribute_value  them ON them.card_id = topic.number_value and them.attribute_code='ADMIN_275720' "+
    	" JOIN attribute_value name_t ON name_t.card_id = them.number_value and name_t.attribute_code='NAME' "+
    	" JOIN card cc on cc.card_id = question.number_value " +
      	whereString+
      	" and cc.template_id = 884 " +
    	" GROUP BY name_t.string_value "+
    	"\n UNION \n"+
    	" Select  "+
    	" 18 AS cc "+
    	" ,'���������� ���������, ������������� ��������� � ���������������� �������� ����������� ������� �������������� ������' as name "+
    	" ,name_t.string_value || '" + SUBQUESTION + "' as thematic "+
    	" ,count(distinct c.card_id) as hz "+
    	" from card c "+
    	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
    	"  JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
    		" extract('year' from reg_date.date_value) ="+ pYear.toString() +" and extract('quarter' from reg_date.date_value) ="+ quarter.toString() +" "+
    	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
    	" JOIN attribute_value cont ON cont.card_id = c.card_id and cont.attribute_code='JBR_EXAMIN_PROCEDURE' and cont.value_id=2048 "+
    	" JOIN attribute_value subquestion ON subquestion.card_id = c.card_id and subquestion.attribute_code='ADMIN_277251' "+
    	" JOIN attribute_value question ON question.card_id = subquestion.number_value and question.attribute_code='JBR_QUESTION' "+
    	" JOIN attribute_value topic ON topic.card_id = question.number_value and topic.attribute_code='ADMIN_278045' "+
    	" JOIN attribute_value  them ON them.card_id = topic.number_value and them.attribute_code='ADMIN_275720' "+
    	" JOIN attribute_value name_t ON name_t.card_id = them.number_value and name_t.attribute_code='NAME' "+
    	" JOIN card cc on cc.card_id = subquestion.number_value " +
      	whereString+
      	" and cc.template_id = 885 " +
    	" GROUP BY name_t.string_value "+
    	" ) as foiv " +
    	" group by foiv.cc, foiv.name, foiv.thematic " +
    	"\n UNION \n"+
    	"Select foiv.cc as cc, foiv.name as name, foiv.thematic as thematic, sum(distinct foiv.hz) as hz  from ( " +
    	" Select  "+
    	" 19 AS cc "+
    	" ,'���������� �����, � ������� ������������� ����������� �����' as name "+
    	" ,name_t.string_value || '" + QUESTION + "' as thematic "+
    	" ,count(distinct c.card_id) as hz "+
    	" from card c "+
    	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
    	 " JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
    	" 	extract('year' from reg_date.date_value) ="+ pYear.toString() +" and extract('quarter' from reg_date.date_value) ="+ quarter.toString() +" "+
    	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
    	" JOIN attribute_value fav_d ON fav_d.card_id = c.card_id and fav_d.attribute_code='JBR_ADDRESS_FORMAT' and fav_d.value_id=2036 "+
    	" JOIN attribute_value d ON d.card_id = c.card_id and d.attribute_code='ADMIN_283926' "+
    	" JOIN attribute_value fav_des ON fav_des.card_id = d.number_value and fav_des.attribute_code='NAME' and  fav_des.string_value='����� �������������' "+
    	" JOIN attribute_value question ON question.card_id = c.card_id and question.attribute_code='ADMIN_277251' "+
    	" JOIN attribute_value topic ON topic.card_id = question.number_value and topic.attribute_code='ADMIN_278045' "+
    	" JOIN attribute_value  them ON them.card_id = topic.number_value and them.attribute_code='ADMIN_275720' "+
    	" JOIN attribute_value name_t ON name_t.card_id = them.number_value and name_t.attribute_code='NAME' "+
    	" JOIN card cc on cc.card_id = question.number_value " +
      	whereString+
      	" and cc.template_id = 884 " +
    	" GROUP BY name_t.string_value "+
    	"\n UNION \n"+
    	" Select  "+
    	" 19 AS cc "+
    	" ,'���������� �����, � ������� ������������� ����������� �����' as name "+
    	" ,name_t.string_value || '" + SUBQUESTION + "' as thematic "+
    	" ,count(distinct c.card_id) as hz "+
    	" from card c "+
    	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
    	 " JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
    	" 	extract('year' from reg_date.date_value) ="+ pYear.toString() +" and extract('quarter' from reg_date.date_value) ="+ quarter.toString() +" "+
    	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
    	" JOIN attribute_value fav_d ON fav_d.card_id = c.card_id and fav_d.attribute_code='JBR_ADDRESS_FORMAT' and fav_d.value_id=2036 "+
    	" JOIN attribute_value d ON d.card_id = c.card_id and d.attribute_code='ADMIN_283926' "+
    	" JOIN attribute_value fav_des ON fav_des.card_id = d.number_value and fav_des.attribute_code='NAME' and  fav_des.string_value='����� �������������' "+
    	" JOIN attribute_value subquestion ON subquestion.card_id = c.card_id and subquestion.attribute_code='ADMIN_277251' "+
    	" JOIN attribute_value question ON question.card_id = subquestion.number_value and question.attribute_code='JBR_QUESTION' "+
    	" JOIN attribute_value topic ON topic.card_id = question.number_value and topic.attribute_code='ADMIN_278045' "+
    	" JOIN attribute_value  them ON them.card_id = topic.number_value and them.attribute_code='ADMIN_275720' "+
    	" JOIN attribute_value name_t ON name_t.card_id = them.number_value and name_t.attribute_code='NAME' "+
    	" JOIN card cc on cc.card_id = subquestion.number_value " +
      	whereString+
      	" and cc.template_id = 885 " +
    	" GROUP BY name_t.string_value "+
    	" ) as foiv " +
    	" group by foiv.cc, foiv.name, foiv.thematic " +
    	"\n UNION \n"+
    	"Select foiv.cc as cc, foiv.name as name, foiv.thematic as thematic, sum(distinct foiv.hz) as hz  from ( " +
    	" Select  "+
    	" 20 AS cc "+
    	" ,'���������� �����, �� ����������� ������������ ������� �������� � ��������� ���� ������� ��������' as name "+
    	" ,name_t.string_value || '" + QUESTION + "' as thematic "+
    	" ,count(distinct c.card_id) as hz "+
    	" from card c "+
    	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
    	"  JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
    		" extract('year' from reg_date.date_value) ="+ pYear.toString() +" and extract('quarter' from reg_date.date_value) ="+ quarter.toString() +" "+
    	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
    	" JOIN attribute_value fav_d ON fav_d.card_id = c.card_id and fav_d.attribute_code='JBR_ADDRESS_FORMAT' and fav_d.value_id=2036 "+
    	" JOIN attribute_value d ON d.card_id = c.card_id and d.attribute_code='ADMIN_283926' "+
    	" JOIN attribute_value fav_des ON fav_des.card_id = d.number_value and fav_des.attribute_code='NAME' and  fav_des.string_value='�������� ��������' "+
    	" JOIN attribute_value question ON question.card_id = c.card_id and question.attribute_code='ADMIN_277251' "+
    	" JOIN attribute_value topic ON topic.card_id = question.number_value and topic.attribute_code='ADMIN_278045' "+
    	" JOIN attribute_value  them ON them.card_id = topic.number_value and them.attribute_code='ADMIN_275720' "+
    	" JOIN attribute_value name_t ON name_t.card_id = them.number_value and name_t.attribute_code='NAME' "+
    	" JOIN card cc on cc.card_id = question.number_value " +
      	whereString+
      	" and cc.template_id = 884 " +
    	" GROUP BY name_t.string_value "+
    	"\n UNION \n"+
    	" Select  "+
    	" 20 AS cc "+
    	" ,'���������� �����, �� ����������� ������������ ������� �������� � ��������� ���� ������� ��������' as name "+
    	" ,name_t.string_value || '" + SUBQUESTION + "' as thematic "+
    	" ,count(distinct c.card_id) as hz "+
    	" from card c "+
    	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
    	"  JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
    		" extract('year' from reg_date.date_value) ="+ pYear.toString() +" and extract('quarter' from reg_date.date_value) ="+ quarter.toString() +" "+
    	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
    	" JOIN attribute_value fav_d ON fav_d.card_id = c.card_id and fav_d.attribute_code='JBR_ADDRESS_FORMAT' and fav_d.value_id=2036 "+
    	" JOIN attribute_value d ON d.card_id = c.card_id and d.attribute_code='ADMIN_283926' "+
    	" JOIN attribute_value fav_des ON fav_des.card_id = d.number_value and fav_des.attribute_code='NAME' and  fav_des.string_value='�������� ��������' "+
    	" JOIN attribute_value subquestion ON subquestion.card_id = c.card_id and subquestion.attribute_code='ADMIN_277251' "+
    	" JOIN attribute_value question ON question.card_id = subquestion.number_value and question.attribute_code='JBR_QUESTION' "+
    	" JOIN attribute_value topic ON topic.card_id = question.number_value and topic.attribute_code='ADMIN_278045' "+
    	" JOIN attribute_value  them ON them.card_id = topic.number_value and them.attribute_code='ADMIN_275720' "+
    	" JOIN attribute_value name_t ON name_t.card_id = them.number_value and name_t.attribute_code='NAME' "+
    	" JOIN card cc on cc.card_id = subquestion.number_value " +
      	whereString+
      	" and cc.template_id = 885 " +
    	" GROUP BY name_t.string_value "+
    	" ) as foiv " +
    	" group by foiv.cc, foiv.name, foiv.thematic " +
    	"\n UNION \n"+
    	"Select foiv.cc as cc, foiv.name as name, foiv.thematic as thematic, sum(distinct foiv.hz) as hz  from ( " +
    	" Select  "+
    	" 21 AS cc "+
    	" ,'����������� ��������� � ���������� �����' as name "+
    	" ,name_t.string_value || '" + QUESTION + "' as thematic "+
    	" ,count(distinct c.card_id) as hz "+
    	" from card c "+
    	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
    	 " JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
    	" 	extract('year' from reg_date.date_value) ="+ pYear.toString() +" and extract('quarter' from reg_date.date_value) ="+ quarter.toString() +" "+
    	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
    	" JOIN attribute_value decision ON decision.card_id=c.card_id and decision.attribute_code='ADMIN_283929' and decision.value_id is not null "+
    	" JOIN attribute_value control_date ON control_date.card_id = c.card_id and control_date.attribute_code='JBR_IMPL_DEADLINE'  and control_date.date_value  is not null "+
    	" JOIN attribute_value answer_date ON answer_date.card_id=c.card_id and answer_date.attribute_code='ADMIN_220912' and answer_date.date_value is not null "+
    	   " and date_trunc('day', answer_date.date_value)>date_trunc('day', control_date.date_value) "+
       	" JOIN attribute_value question ON question.card_id = c.card_id and question.attribute_code='ADMIN_277251' "+
    	" JOIN attribute_value topic ON topic.card_id = question.number_value and topic.attribute_code='ADMIN_278045' "+
    	" JOIN attribute_value  them ON them.card_id = topic.number_value and them.attribute_code='ADMIN_275720' "+
    	" JOIN attribute_value name_t ON name_t.card_id = them.number_value and name_t.attribute_code='NAME' "+
    	" JOIN card cc on cc.card_id = question.number_value " +
      	whereString+
      	" and cc.template_id = 884 " +
    	" GROUP BY name_t.string_value "+
    	"\n UNION \n"+
    	" Select  "+
    	" 21 AS cc "+
    	" ,'����������� ��������� � ���������� �����' as name "+
    	" ,name_t.string_value || '" + SUBQUESTION + "' as thematic "+
    	" ,count(distinct c.card_id) as hz "+
    	" from card c "+
    	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
    	 " JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
    	" 	extract('year' from reg_date.date_value) ="+ pYear.toString() +" and extract('quarter' from reg_date.date_value) ="+ quarter.toString() +" "+
    	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
    	" JOIN attribute_value decision ON decision.card_id=c.card_id and decision.attribute_code='ADMIN_283929' and decision.value_id is not null "+
    	" JOIN attribute_value control_date ON control_date.card_id = c.card_id and control_date.attribute_code='JBR_IMPL_DEADLINE'  and control_date.date_value  is not null "+
    	" JOIN attribute_value answer_date ON answer_date.card_id=c.card_id and answer_date.attribute_code='ADMIN_220912' and answer_date.date_value is not null "+
    	   " and date_trunc('day', answer_date.date_value)>date_trunc('day', control_date.date_value) "+
       	" JOIN attribute_value subquestion ON subquestion.card_id = c.card_id and subquestion.attribute_code='ADMIN_277251' "+
       	" JOIN attribute_value question ON question.card_id = subquestion.number_value and question.attribute_code='JBR_QUESTION' "+
    	" JOIN attribute_value topic ON topic.card_id = question.number_value and topic.attribute_code='ADMIN_278045' "+
    	" JOIN attribute_value  them ON them.card_id = topic.number_value and them.attribute_code='ADMIN_275720' "+
    	" JOIN attribute_value name_t ON name_t.card_id = them.number_value and name_t.attribute_code='NAME' "+
    	" JOIN card cc on cc.card_id = subquestion.number_value " +
      	whereString+
      	" and cc.template_id = 885 " +
    	" GROUP BY name_t.string_value "+
    	" ) as foiv " +
    	" group by foiv.cc, foiv.name, foiv.thematic " +
    	"\n UNION \n"+
    	"Select foiv.cc as cc, foiv.name as name, foiv.thematic as thematic, sum(distinct foiv.hz) as hz  from ( " +
    	" Select  "+
    	" 22 AS cc "+
    	" ,'��������� �� ������������ �� 1 ����� ������ ���������� �� �������� ��������, ����������� � �������� �������' as name "+
    	" ,name_t.string_value || '" + QUESTION + "' as thematic "+
    	" ,count(distinct c.card_id) as hz "+
    	" from card c "+
    	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
    	 " JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
    	" 	extract('year' from reg_date.date_value) ="+ pYear.toString() +" and extract('quarter' from reg_date.date_value) ="+ quarter.toString() +" "+
    	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
    	" JOIN attribute_value control_date ON control_date.card_id = c.card_id and control_date.attribute_code='JBR_IMPL_DEADLINE' "+
    		" and control_date.date_value is not null  and not exists (select 1 from attribute_value v where v.card_id=c.card_id and v.attribute_code='ADMIN_283929' and v.value_id is not null) "+
    	" 	and not exists (select 1 from attribute_value v1 where v1.card_id=c.card_id and v1.attribute_code='ADMIN_220912' and v1.date_value is not null) "+
    	" JOIN attribute_value question ON question.card_id = c.card_id and question.attribute_code='ADMIN_277251' "+
    	" JOIN attribute_value topic ON topic.card_id = question.number_value and topic.attribute_code='ADMIN_278045' "+
    	" JOIN attribute_value  them ON them.card_id = topic.number_value and them.attribute_code='ADMIN_275720' "+
    	" JOIN attribute_value name_t ON name_t.card_id = them.number_value and name_t.attribute_code='NAME' "+
    	" JOIN card cc on cc.card_id = question.number_value " +
      	whereString+
      	" and cc.template_id = 884 " +
    	" GROUP BY name_t.string_value "+
    	"\n UNION \n"+
    	" Select  "+
    	" 22 AS cc "+
    	" ,'��������� �� ������������ �� 1 ����� ������ ���������� �� �������� ��������, ����������� � �������� �������' as name "+
    	" ,name_t.string_value || '" + SUBQUESTION + "' as thematic "+
    	" ,count(distinct c.card_id) as hz "+
    	" from card c "+
    	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
    	 " JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
    	" 	extract('year' from reg_date.date_value) ="+ pYear.toString() +" and extract('quarter' from reg_date.date_value) ="+ quarter.toString() +" "+
    	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
    	" JOIN attribute_value control_date ON control_date.card_id = c.card_id and control_date.attribute_code='JBR_IMPL_DEADLINE' "+
    		" and control_date.date_value is not null  and not exists (select 1 from attribute_value v where v.card_id=c.card_id and v.attribute_code='ADMIN_283929' and v.value_id is not null) "+
    	" 	and not exists (select 1 from attribute_value v1 where v1.card_id=c.card_id and v1.attribute_code='ADMIN_220912' and v1.date_value is not null) "+
    	" JOIN attribute_value subquestion ON subquestion.card_id = c.card_id and subquestion.attribute_code='ADMIN_277251' "+
    	" JOIN attribute_value question ON question.card_id = subquestion.number_value and question.attribute_code='JBR_QUESTION' "+
    	" JOIN attribute_value topic ON topic.card_id = question.number_value and topic.attribute_code='ADMIN_278045' "+
    	" JOIN attribute_value  them ON them.card_id = topic.number_value and them.attribute_code='ADMIN_275720' "+
    	" JOIN attribute_value name_t ON name_t.card_id = them.number_value and name_t.attribute_code='NAME' "+
    	" JOIN card cc on cc.card_id = subquestion.number_value " +
      	whereString+
      	" and cc.template_id = 885 " +
    	" GROUP BY name_t.string_value "+
    	" ) as foiv " +
    	" group by foiv.cc, foiv.name, foiv.thematic " +
    	"\n UNION \n"+
    	"Select foiv.cc as cc, foiv.name as name, foiv.thematic as thematic, sum(distinct foiv.hz) as hz  from ( " +
    	" Select  "+
    	" 24 AS cc "+
    	" ,'          " + config.getProperty(PARAM_IMPROPER, PARAM_NULL) + "' as name "+
    	" ,name_t.string_value || '" + QUESTION + "' as thematic "+
    	" ,count(distinct c.card_id) as hz "+
    	" from card c "+
    	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
    	 " JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
    	" 	extract('year' from reg_date.date_value) ="+ pYear.toString() +" and extract('quarter' from reg_date.date_value) ="+ quarter.toString() +" "+
    	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
    	" JOIN attribute_value fav_d ON fav_d.card_id = c.card_id and fav_d.attribute_code='JBR_ADDRESS_FORMAT' and fav_d.value_id=2036 "+
    	" JOIN attribute_value fav_des ON  fav_des.card_id = c.card_id and  fav_des.attribute_code='JBR_COMPLAINT_REASON' and  fav_des.value_id=2039 "+
    	" JOIN attribute_value question ON question.card_id = c.card_id and question.attribute_code='ADMIN_277251' "+
    	" JOIN attribute_value topic ON topic.card_id = question.number_value and topic.attribute_code='ADMIN_278045' "+
    	" JOIN attribute_value  them ON them.card_id = topic.number_value and them.attribute_code='ADMIN_275720' "+
    	" JOIN attribute_value name_t ON name_t.card_id = them.number_value and name_t.attribute_code='NAME' "+
    	" JOIN card cc on cc.card_id = question.number_value " +
      	whereString+
      	" and cc.template_id = 884 " +
    	" GROUP BY name_t.string_value "+
    	"\n UNION \n"+
    	" Select  "+
    	" 24 AS cc "+
    	" ,'          " + config.getProperty(PARAM_IMPROPER, PARAM_NULL) + "' as name "+
    	" ,name_t.string_value || '" + SUBQUESTION + "' as thematic "+
    	" ,count(distinct c.card_id) as hz "+
    	" from card c "+
    	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
    	 " JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
    	" 	extract('year' from reg_date.date_value) ="+ pYear.toString() +" and extract('quarter' from reg_date.date_value) ="+ quarter.toString() +" "+
    	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
    	" JOIN attribute_value fav_d ON fav_d.card_id = c.card_id and fav_d.attribute_code='JBR_ADDRESS_FORMAT' and fav_d.value_id=2036 "+
    	" JOIN attribute_value fav_des ON  fav_des.card_id = c.card_id and  fav_des.attribute_code='JBR_COMPLAINT_REASON' and  fav_des.value_id=2039 "+
    	" JOIN attribute_value subquestion ON subquestion.card_id = c.card_id and subquestion.attribute_code='ADMIN_277251' "+
    	" JOIN attribute_value question ON question.card_id = subquestion.number_value and question.attribute_code='JBR_QUESTION' "+
    	" JOIN attribute_value topic ON topic.card_id = question.number_value and topic.attribute_code='ADMIN_278045' "+
    	" JOIN attribute_value  them ON them.card_id = topic.number_value and them.attribute_code='ADMIN_275720' "+
    	" JOIN attribute_value name_t ON name_t.card_id = them.number_value and name_t.attribute_code='NAME' "+
    	" JOIN card cc on cc.card_id = subquestion.number_value " +
      	whereString+
      	" and cc.template_id = 885 " +
    	" GROUP BY name_t.string_value "+
    	" ) as foiv " +
    	" group by foiv.cc, foiv.name, foiv.thematic " +
    	"\n UNION \n"+
    	"Select foiv.cc as cc, foiv.name as name, foiv.thematic as thematic, sum(distinct foiv.hz) as hz  from ( " +
    	" Select  "+
    	" 25 AS cc "+
    	" ,'          ���������� � ������ ���������� �� �������������� ��������������� �����' as name "+
    	" ,name_t.string_value || '" + QUESTION + "' as thematic "+
    	" ,count(distinct c.card_id) as hz "+
    	" from card c "+
    	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
    	 " JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
    	" 	extract('year' from reg_date.date_value) ="+ pYear.toString() +" and extract('quarter' from reg_date.date_value) ="+ quarter.toString() +" "+
    	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
    	" JOIN attribute_value fav_d ON fav_d.card_id = c.card_id and fav_d.attribute_code='JBR_ADDRESS_FORMAT' and fav_d.value_id=2036 "+
    	" JOIN attribute_value fav_des ON  fav_des.card_id = c.card_id and  fav_des.attribute_code='JBR_COMPLAINT_REASON' and  fav_des.value_id=2040 "+
    	" JOIN attribute_value question ON question.card_id = c.card_id and question.attribute_code='ADMIN_277251' "+
    	" JOIN attribute_value topic ON topic.card_id = question.number_value and topic.attribute_code='ADMIN_278045' "+
    	" JOIN attribute_value  them ON them.card_id = topic.number_value and them.attribute_code='ADMIN_275720' "+
    	" JOIN attribute_value name_t ON name_t.card_id = them.number_value and name_t.attribute_code='NAME' "+
    	" JOIN card cc on cc.card_id = question.number_value " +
      	whereString+
      	" and cc.template_id = 884 " +
    	" GROUP BY name_t.string_value "+
    	"\n UNION \n"+
    	" Select  "+
    	" 25 AS cc "+
    	" ,'          ���������� � ������ ���������� �� �������������� ��������������� �����' as name "+
    	" ,name_t.string_value || '" + SUBQUESTION + "' as thematic "+
    	" ,count(distinct c.card_id) as hz "+
    	" from card c "+
    	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
    	 " JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
    	" 	extract('year' from reg_date.date_value) ="+ pYear.toString() +" and extract('quarter' from reg_date.date_value) ="+ quarter.toString() +" "+
    	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
    	" JOIN attribute_value fav_d ON fav_d.card_id = c.card_id and fav_d.attribute_code='JBR_ADDRESS_FORMAT' and fav_d.value_id=2036 "+
    	" JOIN attribute_value fav_des ON  fav_des.card_id = c.card_id and  fav_des.attribute_code='JBR_COMPLAINT_REASON' and  fav_des.value_id=2040 "+
    	" JOIN attribute_value subquestion ON subquestion.card_id = c.card_id and subquestion.attribute_code='ADMIN_277251' "+
    	" JOIN attribute_value question ON question.card_id = subquestion.number_value and question.attribute_code='JBR_QUESTION' "+
    	" JOIN attribute_value topic ON topic.card_id = question.number_value and topic.attribute_code='ADMIN_278045' "+
    	" JOIN attribute_value  them ON them.card_id = topic.number_value and them.attribute_code='ADMIN_275720' "+
    	" JOIN attribute_value name_t ON name_t.card_id = them.number_value and name_t.attribute_code='NAME' "+
    	" JOIN card cc on cc.card_id = subquestion.number_value " +
      	whereString+
      	" and cc.template_id = 885 " +
    	" GROUP BY name_t.string_value "+
    	" ) as foiv " +
    	" group by foiv.cc, foiv.name, foiv.thematic " +
    	"\n UNION \n"+
    	"Select foiv.cc as cc, foiv.name as name, foiv.thematic as thematic, sum(distinct foiv.hz) as hz  from ( " +
    	" Select  "+
    	" 26 AS cc "+
    	" ,'          ��������� ���������������� � ������������ ��������������� ����������, ������������ ����������� � ������� �����������' as name "+
    	" ,name_t.string_value || '" + QUESTION + "' as thematic "+
    	" ,count(distinct c.card_id) as hz "+
    	" from card c "+
    	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
    	 " JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
    	" 	extract('year' from reg_date.date_value) ="+ pYear.toString() +" and extract('quarter' from reg_date.date_value) ="+ quarter.toString() +" "+
    	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
    	" JOIN attribute_value fav_d ON fav_d.card_id = c.card_id and fav_d.attribute_code='JBR_ADDRESS_FORMAT' and fav_d.value_id=2036 "+
    	" JOIN attribute_value fav_des ON  fav_des.card_id = c.card_id and  fav_des.attribute_code='JBR_COMPLAINT_REASON' and  fav_des.value_id=2041 "+
    	" JOIN attribute_value question ON question.card_id = c.card_id and question.attribute_code='ADMIN_277251' "+
    	" JOIN attribute_value topic ON topic.card_id = question.number_value and topic.attribute_code='ADMIN_278045' "+
    	" JOIN attribute_value  them ON them.card_id = topic.number_value and them.attribute_code='ADMIN_275720' "+
    	" JOIN attribute_value name_t ON name_t.card_id = them.number_value and name_t.attribute_code='NAME' "+
    	" JOIN card cc on cc.card_id = question.number_value " +
      	whereString+
      	" and cc.template_id = 884 " +
    	" GROUP BY name_t.string_value "+
    	"\n UNION \n"+
    	" Select  "+
    	" 26 AS cc "+
    	" ,'          ��������� ���������������� � ������������ ��������������� ����������, ������������ ����������� � ������� �����������' as name "+
    	" ,name_t.string_value || '" + SUBQUESTION + "' as thematic "+
    	" ,count(distinct c.card_id) as hz "+
    	" from card c "+
    	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
    	 " JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
    	" 	extract('year' from reg_date.date_value) ="+ pYear.toString() +" and extract('quarter' from reg_date.date_value) ="+ quarter.toString() +" "+
    	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
    	" JOIN attribute_value fav_d ON fav_d.card_id = c.card_id and fav_d.attribute_code='JBR_ADDRESS_FORMAT' and fav_d.value_id=2036 "+
    	" JOIN attribute_value fav_des ON  fav_des.card_id = c.card_id and  fav_des.attribute_code='JBR_COMPLAINT_REASON' and  fav_des.value_id=2041 "+
    	" JOIN attribute_value subquestion ON subquestion.card_id = c.card_id and subquestion.attribute_code='ADMIN_277251' "+
    	" JOIN attribute_value question ON question.card_id = subquestion.number_value and question.attribute_code='JBR_QUESTION' "+
    	" JOIN attribute_value topic ON topic.card_id = question.number_value and topic.attribute_code='ADMIN_278045' "+
    	" JOIN attribute_value  them ON them.card_id = topic.number_value and them.attribute_code='ADMIN_275720' "+
    	" JOIN attribute_value name_t ON name_t.card_id = them.number_value and name_t.attribute_code='NAME' "+
    	" JOIN card cc on cc.card_id = subquestion.number_value " +
      	whereString+
      	" and cc.template_id = 885 " +
    	" GROUP BY name_t.string_value "+
    	" ) as foiv " +
    	" group by foiv.cc, foiv.name, foiv.thematic " +
    	"\n UNION \n"+
    	"Select foiv.cc as cc, foiv.name as name, foiv.thematic as thematic, sum(distinct foiv.hz) as hz  from ( " +
    	" Select  "+
    	" 27 AS cc "+
    	" ,'          " + config.getProperty(PARAM_REJECTION, PARAM_NULL) + "' as name "+
    	" ,name_t.string_value || '" + QUESTION + "' as thematic "+
    	" ,count(distinct c.card_id) as hz "+
    	" from card c "+
    	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
    	 " JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
    	" 	extract('year' from reg_date.date_value) ="+ pYear.toString() +" and extract('quarter' from reg_date.date_value) ="+ quarter.toString() +" "+
    	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
    	" JOIN attribute_value fav_d ON fav_d.card_id = c.card_id and fav_d.attribute_code='JBR_ADDRESS_FORMAT' and fav_d.value_id=2036 "+
    	" JOIN attribute_value fav_des ON  fav_des.card_id = c.card_id and  fav_des.attribute_code='JBR_COMPLAINT_REASON' and  fav_des.value_id=2042 "+
    	" JOIN attribute_value question ON question.card_id = c.card_id and question.attribute_code='ADMIN_277251' "+
    	" JOIN attribute_value topic ON topic.card_id = question.number_value and topic.attribute_code='ADMIN_278045' "+
    	" JOIN attribute_value  them ON them.card_id = topic.number_value and them.attribute_code='ADMIN_275720' "+
    	" JOIN attribute_value name_t ON name_t.card_id = them.number_value and name_t.attribute_code='NAME' "+
    	" JOIN card cc on cc.card_id = question.number_value " +
      	whereString+
      	" and cc.template_id = 884 " +
    	" GROUP BY name_t.string_value "+
    	"\n UNION \n"+
    	" Select  "+
    	" 27 AS cc "+
    	" ,'          " + config.getProperty(PARAM_REJECTION, PARAM_NULL) + "' as name "+
    	" ,name_t.string_value || '" + SUBQUESTION + "' as thematic "+
    	" ,count(distinct c.card_id) as hz "+
    	" from card c "+
    	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
    	 " JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
    	" 	extract('year' from reg_date.date_value) ="+ pYear.toString() +" and extract('quarter' from reg_date.date_value) ="+ quarter.toString() +" "+
    	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
    	" JOIN attribute_value fav_d ON fav_d.card_id = c.card_id and fav_d.attribute_code='JBR_ADDRESS_FORMAT' and fav_d.value_id=2036 "+
    	" JOIN attribute_value fav_des ON  fav_des.card_id = c.card_id and  fav_des.attribute_code='JBR_COMPLAINT_REASON' and  fav_des.value_id=2042 "+
    	" JOIN attribute_value subquestion ON subquestion.card_id = c.card_id and subquestion.attribute_code='ADMIN_277251' "+
    	" JOIN attribute_value question ON question.card_id = subquestion.number_value and question.attribute_code='JBR_QUESTION' "+
    	" JOIN attribute_value topic ON topic.card_id = question.number_value and topic.attribute_code='ADMIN_278045' "+
    	" JOIN attribute_value  them ON them.card_id = topic.number_value and them.attribute_code='ADMIN_275720' "+
    	" JOIN attribute_value name_t ON name_t.card_id = them.number_value and name_t.attribute_code='NAME' "+
    	" JOIN card cc on cc.card_id = subquestion.number_value " +
      	whereString+
      	" and cc.template_id = 885 " +
    	" GROUP BY name_t.string_value "+
    	" ) as foiv " +
    	" group by foiv.cc, foiv.name, foiv.thematic " +
    	"\n UNION \n"+
    	"Select foiv.cc as cc, foiv.name as name, foiv.thematic as thematic, sum(distinct foiv.hz) as hz  from ( " +
    	" Select  "+
    	" 28 AS cc "+
    	" ,'          ������������� ����������������� � ������������ ���������� �� �������������� ��������������� �����' as name "+
    	" ,name_t.string_value || '" + QUESTION + "' as thematic "+
    	" ,count(distinct c.card_id) as hz "+
    	" from card c "+
    	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
    	 " JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
    	" 	extract('year' from reg_date.date_value) ="+ pYear.toString() +" and extract('quarter' from reg_date.date_value) ="+ quarter.toString() +" "+
    	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
    	" JOIN attribute_value fav_d ON fav_d.card_id = c.card_id and fav_d.attribute_code='JBR_ADDRESS_FORMAT' and fav_d.value_id=2036 "+
    	" JOIN attribute_value fav_des ON  fav_des.card_id = c.card_id and  fav_des.attribute_code='JBR_COMPLAINT_REASON' and  fav_des.value_id=2043 "+
    	" JOIN attribute_value question ON question.card_id = c.card_id and question.attribute_code='ADMIN_277251' "+
    	" JOIN attribute_value topic ON topic.card_id = question.number_value and topic.attribute_code='ADMIN_278045' "+
    	" JOIN attribute_value  them ON them.card_id = topic.number_value and them.attribute_code='ADMIN_275720' "+
    	" JOIN attribute_value name_t ON name_t.card_id = them.number_value and name_t.attribute_code='NAME' "+
    	" JOIN card cc on cc.card_id = question.number_value " +
      	whereString+
      	" and cc.template_id = 884 " +
    	" GROUP BY name_t.string_value "+
    	"\n UNION \n"+
    	" Select  "+
    	" 28 AS cc "+
    	" ,'          ������������� ����������������� � ������������ ���������� �� �������������� ��������������� �����' as name "+
    	" ,name_t.string_value || '" + SUBQUESTION + "' as thematic "+
    	" ,count(distinct c.card_id) as hz "+
    	" from card c "+
    	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
    	 " JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
    	" 	extract('year' from reg_date.date_value) ="+ pYear.toString() +" and extract('quarter' from reg_date.date_value) ="+ quarter.toString() +" "+
    	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
    	" JOIN attribute_value fav_d ON fav_d.card_id = c.card_id and fav_d.attribute_code='JBR_ADDRESS_FORMAT' and fav_d.value_id=2036 "+
    	" JOIN attribute_value fav_des ON  fav_des.card_id = c.card_id and  fav_des.attribute_code='JBR_COMPLAINT_REASON' and  fav_des.value_id=2043 "+
    	" JOIN attribute_value subquestion ON subquestion.card_id = c.card_id and subquestion.attribute_code='ADMIN_277251' "+
    	" JOIN attribute_value question ON question.card_id = subquestion.number_value and question.attribute_code='JBR_QUESTION' "+
    	" JOIN attribute_value topic ON topic.card_id = question.number_value and topic.attribute_code='ADMIN_278045' "+
    	" JOIN attribute_value  them ON them.card_id = topic.number_value and them.attribute_code='ADMIN_275720' "+
    	" JOIN attribute_value name_t ON name_t.card_id = them.number_value and name_t.attribute_code='NAME' "+
    	" JOIN card cc on cc.card_id = subquestion.number_value " +
      	whereString+
      	" and cc.template_id = 885 " +
    	" GROUP BY name_t.string_value "+
    	" ) as foiv " +
    	" group by foiv.cc, foiv.name, foiv.thematic " +
    	"\n UNION \n"+
    	"Select foiv.cc as cc, foiv.name as name, foiv.thematic as thematic, sum(distinct foiv.hz) as hz  from ( " +
    	" Select  "+
    	" 29 AS cc "+
    	" ,'          ������ �������� ����������� �������' as name "+
    	" ,name_t.string_value || '" + QUESTION + "' as thematic "+
    	" ,count(distinct c.card_id) as hz "+
    	" from card c "+
    	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
    	 " JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
    	" 	extract('year' from reg_date.date_value) ="+ pYear.toString() +" and extract('quarter' from reg_date.date_value) ="+ quarter.toString() +" "+
    	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
    	" JOIN attribute_value fav_d ON fav_d.card_id = c.card_id and fav_d.attribute_code='JBR_ADDRESS_FORMAT' and fav_d.value_id=2036 "+
    	" JOIN attribute_value fav_des ON  fav_des.card_id = c.card_id and  fav_des.attribute_code='JBR_COMPLAINT_REASON' and  fav_des.value_id=2044 "+
    	" JOIN attribute_value question ON question.card_id = c.card_id and question.attribute_code='ADMIN_277251' "+
    	" JOIN attribute_value topic ON topic.card_id = question.number_value and topic.attribute_code='ADMIN_278045' "+
    	" JOIN attribute_value  them ON them.card_id = topic.number_value and them.attribute_code='ADMIN_275720' "+
    	" JOIN attribute_value name_t ON name_t.card_id = them.number_value and name_t.attribute_code='NAME' "+
    	" JOIN card cc on cc.card_id = question.number_value " +
      	whereString+
      	" and cc.template_id = 884 " +
    	" GROUP BY name_t.string_value "+
    	"\n UNION \n"+
    	" Select  "+
    	" 29 AS cc "+
    	" ,'          ������ �������� ����������� �������' as name "+
    	" ,name_t.string_value || '" + SUBQUESTION + "' as thematic "+
    	" ,count(distinct c.card_id) as hz "+
    	" from card c "+
    	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
    	 " JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
    	" 	extract('year' from reg_date.date_value) ="+ pYear.toString() +" and extract('quarter' from reg_date.date_value) ="+ quarter.toString() +" "+
    	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
    	" JOIN attribute_value fav_d ON fav_d.card_id = c.card_id and fav_d.attribute_code='JBR_ADDRESS_FORMAT' and fav_d.value_id=2036 "+
    	" JOIN attribute_value fav_des ON  fav_des.card_id = c.card_id and  fav_des.attribute_code='JBR_COMPLAINT_REASON' and  fav_des.value_id=2044 "+
    	" JOIN attribute_value subquestion ON subquestion.card_id = c.card_id and subquestion.attribute_code='ADMIN_277251' "+
    	" JOIN attribute_value question ON question.card_id = subquestion.number_value and question.attribute_code='JBR_QUESTION' "+
    	" JOIN attribute_value topic ON topic.card_id = question.number_value and topic.attribute_code='ADMIN_278045' "+
    	" JOIN attribute_value  them ON them.card_id = topic.number_value and them.attribute_code='ADMIN_275720' "+
    	" JOIN attribute_value name_t ON name_t.card_id = them.number_value and name_t.attribute_code='NAME' "+
    	" JOIN card cc on cc.card_id = subquestion.number_value " +
      	whereString+
      	" and cc.template_id = 885 " +
    	" GROUP BY name_t.string_value "+
    	" ) as foiv " +
    	" group by foiv.cc, foiv.name, foiv.thematic " +
    	"\n UNION \n"+
    	"Select foiv.cc as cc, foiv.name as name, foiv.thematic as thematic, sum(distinct foiv.hz) as hz  from ( " +
    	" Select  "+
    	" 30 AS cc "+
    	" ,'          ������' as name "+
    	" ,name_t.string_value || '" + QUESTION +"' as thematic "+
    	" ,count(distinct c.card_id) as hz "+
    	" from card c "+
    	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
    	 " JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
    	" 	extract('year' from reg_date.date_value) ="+ pYear.toString() +" and extract('quarter' from reg_date.date_value) ="+ quarter.toString() +" "+
    	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
    	" JOIN attribute_value fav_d ON fav_d.card_id = c.card_id and fav_d.attribute_code='JBR_ADDRESS_FORMAT' and fav_d.value_id=2036 "+
    	" JOIN attribute_value fav_des ON  fav_des.card_id = c.card_id and  fav_des.attribute_code='JBR_COMPLAINT_REASON' and  fav_des.value_id=2045 "+
    	" JOIN attribute_value question ON question.card_id = c.card_id and question.attribute_code='ADMIN_277251' "+
    	" JOIN attribute_value topic ON topic.card_id = question.number_value and topic.attribute_code='ADMIN_278045' "+
    	" JOIN attribute_value  them ON them.card_id = topic.number_value and them.attribute_code='ADMIN_275720' "+
    	" JOIN attribute_value name_t ON name_t.card_id = them.number_value and name_t.attribute_code='NAME' "+
    	" JOIN card cc on cc.card_id = question.number_value " +
      	whereString+
      	" and cc.template_id = 884 " +
    	" GROUP BY name_t.string_value "+
    	"\n UNION \n"+
    	" Select  "+
    	" 30 AS cc "+
    	" ,'          ������' as name "+
    	" ,name_t.string_value || '" + SUBQUESTION +"' as thematic "+
    	" ,count(distinct c.card_id) as hz "+
    	" from card c "+
    	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
    	 " JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
    	" 	extract('year' from reg_date.date_value) ="+ pYear.toString() +" and extract('quarter' from reg_date.date_value) ="+ quarter.toString() +" "+
    	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
    	" JOIN attribute_value fav_d ON fav_d.card_id = c.card_id and fav_d.attribute_code='JBR_ADDRESS_FORMAT' and fav_d.value_id=2036 "+
    	" JOIN attribute_value fav_des ON  fav_des.card_id = c.card_id and  fav_des.attribute_code='JBR_COMPLAINT_REASON' and  fav_des.value_id=2045 "+
    	" JOIN attribute_value subquestion ON subquestion.card_id = c.card_id and subquestion.attribute_code='ADMIN_277251' "+
    	" JOIN attribute_value question ON question.card_id = subquestion.number_value and question.attribute_code='JBR_QUESTION' "+
    	" JOIN attribute_value topic ON topic.card_id = question.number_value and topic.attribute_code='ADMIN_278045' "+
    	" JOIN attribute_value  them ON them.card_id = topic.number_value and them.attribute_code='ADMIN_275720' "+
    	" JOIN attribute_value name_t ON name_t.card_id = them.number_value and name_t.attribute_code='NAME' "+
    	" JOIN card cc on cc.card_id = subquestion.number_value " +
      	whereString+
      	" and cc.template_id = 885 " +
    	" GROUP BY name_t.string_value "+
    	" ) as foiv " +
    	" group by foiv.cc, foiv.name, foiv.thematic " +
    	"\n UNION \n");
	}
	
	
	private void getVopForQuarter(StringBuilder sql, Integer prYear, Integer pYear, Integer firstMonthCurQuarter, 
			Integer prDate, Integer quarter, String whereString) {
		//���-�� �������� �� ������� (����)
		sql.append(
    			" Select  "+
            	" 1 AS cc "+
            	" ,'����������� ������� �� ���������� �������� ������' as name "+
            	" ,'���������� �������� �� �������' as thematic "+
            	" ,count(distinct cc.card_id) as hz "+
            	" from card c "+
            	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
            	" JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
            	" 	extract('year' from reg_date.date_value) ="+ prYear.toString() +" and extract('quarter' from reg_date.date_value) ="+ prDate.toString() +" "+
            	" join card_version cv on cv.card_id = c.card_id and cv.version_date = (select coalesce ((select cv2.version_date from card_version cv2 where cv2.card_id = c.card_id " +
            	"							and cv2.version_date < date_trunc('day', TO_DATE(" + pYear.toString() + " || '-' || " + firstMonthCurQuarter.toString() + " || '-' || '1', 'YYYY-MM-DD') + interval '0 hour') + INTERVAL '1 DAY' " +
            	"							and cv2.version_date >= date_trunc('day', TO_DATE(" + pYear.toString() + " || '-' || " + firstMonthCurQuarter.toString() + " || '-' || '1', 'YYYY-MM-DD') + interval '0 hour') " +
            	"							and cv2.status_id in (101, 102, 103, 301) " +
            	"							limit 1 " +
  			    "							), " +
            	"		  					(select cv2.version_date from card_version cv2 where cv2.card_id = c.card_id " +
            	"							and cv2.version_date < date_trunc('day', TO_DATE(" + prYear.toString() + " || '-' || " + firstMonthCurQuarter.toString() + " || '-' || '1', 'YYYY-MM-DD') + interval '0 hour') " +
            	"		  					and cv2.status_id in (101, 102, 103, 301) " +
            	"		  					order by cv2.version_date desc " +
            	"		  					limit 1 " +
            	"		  					)) " +
            	" 		  																) " +
            	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
            	" JOIN attribute_value question ON question.card_id = c.card_id and question.attribute_code='ADMIN_277251' "+
            	" JOIN attribute_value topic ON topic.card_id = question.number_value and topic.attribute_code='ADMIN_278045' "+
            	" JOIN attribute_value  them ON them.card_id = topic.number_value and them.attribute_code='ADMIN_275720' "+
            	" JOIN attribute_value name_t ON name_t.card_id = them.number_value and name_t.attribute_code='NAME' "+
            	" JOIN card cc on cc.card_id = question.number_value " +
              	whereString+
              	" and cc.template_id = 884 " +
            	" GROUP BY name_t.string_value "+
              	"\n UNION \n"+
    	" Select  "+
    	" 2 AS cc "+
    	" ,'" + config.getProperty(PARAM_RECEIVED, PARAM_NULL) + "' as name "+
    	" ,'���������� �������� �� �������' as thematic "+
    	" ,count(distinct cc.card_id) as hz "+
    	" from card c "+
    	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
    	 " JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
    	" 	extract('year' from reg_date.date_value) ="+ pYear.toString() +" and extract('quarter' from reg_date.date_value) ="+ quarter.toString() +" "+
    	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
    	" JOIN attribute_value question ON question.card_id = c.card_id and question.attribute_code='ADMIN_277251' "+
    	" JOIN attribute_value topic ON topic.card_id = question.number_value and topic.attribute_code='ADMIN_278045' "+
    	" JOIN attribute_value  them ON them.card_id = topic.number_value and them.attribute_code='ADMIN_275720' "+
    	" JOIN attribute_value name_t ON name_t.card_id = them.number_value and name_t.attribute_code='NAME' "+
    	" JOIN card cc on cc.card_id = question.number_value " +
      	whereString+
      	" and cc.template_id = 884 " +
    	" GROUP BY name_t.string_value "+
    	"\n UNION \n"+
    	" Select  "+
    	" 3 AS cc "+
    	" ,'          � ��� ����� ���������' as name "+
    	" ,'���������� �������� �� �������' as thematic "+
    	" ,count(distinct cc.card_id) as hz "+
    	" from card c "+
    	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
    	" JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
    	" 	extract('year' from reg_date.date_value) ="+ pYear.toString() +" and extract('quarter' from reg_date.date_value) ="+ quarter.toString() +" "+
    	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
    	" JOIN attribute_value format_obr ON format_obr.card_id = c.card_id and format_obr.attribute_code='JBR_ADDRESS_FORMAT' and format_obr.value_id=2037 "+
    	" JOIN attribute_value question ON question.card_id = c.card_id and question.attribute_code='ADMIN_277251' "+
    	" JOIN attribute_value topic ON topic.card_id = question.number_value and topic.attribute_code='ADMIN_278045' "+
    	" JOIN attribute_value  them ON them.card_id = topic.number_value and them.attribute_code='ADMIN_275720' "+
    	" JOIN attribute_value name_t ON name_t.card_id = them.number_value and name_t.attribute_code='NAME' "+
    	" JOIN card cc on cc.card_id = question.number_value " +
      	whereString+
      	" and cc.template_id = 884 " +
    	" GROUP BY name_t.string_value "+
    	"\n UNION \n"+
    	" Select  "+
    	" 4 AS cc "+
    	" ,'          � ��� ����� �����' as name "+
    	" ,'���������� �������� �� �������' as thematic "+
    	" ,count(distinct cc.card_id) as hz "+
    	" from card c "+
    	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
    	"  JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
    		" extract('year' from reg_date.date_value) ="+ pYear.toString() +" and extract('quarter' from reg_date.date_value) ="+ quarter.toString() +" "+
    	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
    	" JOIN attribute_value format_obr ON format_obr.card_id = c.card_id and format_obr.attribute_code='JBR_ADDRESS_FORMAT' and format_obr.value_id=2036 "+
    	" JOIN attribute_value question ON question.card_id = c.card_id and question.attribute_code='ADMIN_277251' "+
    	" JOIN attribute_value topic ON topic.card_id = question.number_value and topic.attribute_code='ADMIN_278045' "+
    	" JOIN attribute_value  them ON them.card_id = topic.number_value and them.attribute_code='ADMIN_275720' "+
    	" JOIN attribute_value name_t ON name_t.card_id = them.number_value and name_t.attribute_code='NAME' "+
    	" JOIN card cc on cc.card_id = question.number_value " +
      	whereString+
      	" and cc.template_id = 884 " +
    	" GROUP BY name_t.string_value "+
    	"\n UNION \n"+
    	" Select  "+
    	" 5 AS cc "+
    	" ,'          � ��� ����� �����������' as name "+
    	" ,'���������� �������� �� �������' as thematic "+
    	" ,count(distinct cc.card_id) as hz "+
    	" from card c "+
    	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
    	 " JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
    	" 	extract('year' from reg_date.date_value) ="+ pYear.toString() +" and extract('quarter' from reg_date.date_value) ="+ quarter.toString() +" "+
    	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
    	" JOIN attribute_value format_obr ON format_obr.card_id = c.card_id and format_obr.attribute_code='JBR_ADDRESS_FORMAT' and format_obr.value_id=2038 "+
    	" JOIN attribute_value question ON question.card_id = c.card_id and question.attribute_code='ADMIN_277251' "+
    	" JOIN attribute_value topic ON topic.card_id = question.number_value and topic.attribute_code='ADMIN_278045' "+
    	" JOIN attribute_value  them ON them.card_id = topic.number_value and them.attribute_code='ADMIN_275720' "+
    	" JOIN attribute_value name_t ON name_t.card_id = them.number_value and name_t.attribute_code='NAME' "+
    	" JOIN card cc on cc.card_id = question.number_value " +
      	whereString+
      	" and cc.template_id = 884 " +
    	" GROUP BY name_t.string_value "+
    	"\n UNION \n"+
    	" Select  "+
    	" 7 AS cc "+
    	" ,'          " + config.getProperty(PARAM_REPORTED, PARAM_NULL) + "' as name "+
    	" ,'���������� �������� �� �������' as thematic "+
    	" ,count(distinct cc.card_id) as hz "+
    	" from card c "+
    	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
    	 " JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
    	" 	extract('year' from reg_date.date_value) ="+ pYear.toString() +" and extract('quarter' from reg_date.date_value) ="+ quarter.toString() +" "+
    	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
    	" JOIN attribute_value gub ON gub.card_id = c.card_id and gub.attribute_code='JBR_IMPL_ACQUAINTERS' "+
    	" JOIN person p ON p.person_id = gub.number_value "+
    	" JOIN person_role pr ON pr.person_id = p.person_id and pr.role_code in ('JBR_BOSS') " +
    	" JOIN attribute_value question ON question.card_id = c.card_id and question.attribute_code='ADMIN_277251' "+
    	" JOIN attribute_value topic ON topic.card_id = question.number_value and topic.attribute_code='ADMIN_278045' "+
    	" JOIN attribute_value  them ON them.card_id = topic.number_value and them.attribute_code='ADMIN_275720' "+
    	" JOIN attribute_value name_t ON name_t.card_id = them.number_value and name_t.attribute_code='NAME' "+
    	" JOIN card cc on cc.card_id = question.number_value " +
      	whereString+
      	" and cc.template_id = 884 " +
    	" GROUP BY name_t.string_value "+
    	"\n UNION \n"+
    	" Select "+
    	" 8 AS cc "+
    	" ,'          ����� �� ��������' as name "+
    	" ,'���������� �������� �� �������' as thematic "+
    	" ,count(distinct cc.card_id) as hz "+
    	" from card c "+
    	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
    	" JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
    		" extract('year' from reg_date.date_value) ="+ pYear.toString() +" and extract('quarter' from reg_date.date_value) ="+ quarter.toString() +" "+
    	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
    	" JOIN attribute_value av_contrl ON av_contrl.card_id = c.card_id and av_contrl.attribute_code='JBR_IMPL_ONCONT' and av_contrl.value_id=1432 "+
    	" JOIN attribute_value question ON question.card_id = c.card_id and question.attribute_code='ADMIN_277251' "+
    	" JOIN attribute_value topic ON topic.card_id = question.number_value and topic.attribute_code='ADMIN_278045' "+
    	" JOIN attribute_value  them ON them.card_id = topic.number_value and them.attribute_code='ADMIN_275720' "+
    	" JOIN attribute_value name_t ON name_t.card_id = them.number_value and name_t.attribute_code='NAME' "+
    	" JOIN card cc on cc.card_id = question.number_value " +
      	whereString+
      	" and cc.template_id = 884 " +
    	" GROUP BY name_t.string_value "+
    	"\n UNION \n"+
    	" Select "+
    	" 9 AS cc "+
    	" ,'          ����������� � ������� �� �����' as name "+
    	" ,'���������� �������� �� �������' as thematic "+
    	" ,count(distinct cc.card_id) as hz "+
    	" from card c "+
    	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
    	 " JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
    	" 	extract('year' from reg_date.date_value) ="+ pYear.toString() +" and extract('quarter' from reg_date.date_value) ="+ quarter.toString() +" "+
    	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
    	" JOIN attribute_value av_contrl ON av_contrl.card_id = c.card_id and av_contrl.attribute_code='JBR_EXAMIN_PROCEDURE' and av_contrl.value_id=2046 "+
    	" JOIN attribute_value question ON question.card_id = c.card_id and question.attribute_code='ADMIN_277251' "+
    	" JOIN attribute_value topic ON topic.card_id = question.number_value and topic.attribute_code='ADMIN_278045' "+
    	" JOIN attribute_value  them ON them.card_id = topic.number_value and them.attribute_code='ADMIN_275720' "+
    	" JOIN attribute_value name_t ON name_t.card_id = them.number_value and name_t.attribute_code='NAME' "+
    	" JOIN card cc on cc.card_id = question.number_value " +
      	whereString+
      	" and cc.template_id = 884 " +
    	" GROUP BY name_t.string_value "+
    	"\n UNION \n"+
    	" Select "+
    	" 10 AS cc "+
    	" ,'          ����������� ������������' as name "+
    	" ,'���������� �������� �� �������' as thematic "+
    	" ,count(distinct cc.card_id) as hz "+
    	" from card c "+
    	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
    	"  JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
    		" extract('year' from reg_date.date_value) ="+ pYear.toString() +" and extract('quarter' from reg_date.date_value) ="+ quarter.toString() +" "+
    	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
    	" JOIN attribute_value av_contrl ON av_contrl.card_id = c.card_id and av_contrl.attribute_code='JBR_EXAMIN_PROCEDURE' and av_contrl.value_id=2047 "+
    	" JOIN attribute_value question ON question.card_id = c.card_id and question.attribute_code='ADMIN_277251' "+
    	" JOIN attribute_value topic ON topic.card_id = question.number_value and topic.attribute_code='ADMIN_278045' "+
    	" JOIN attribute_value  them ON them.card_id = topic.number_value and them.attribute_code='ADMIN_275720' "+
    	" JOIN attribute_value name_t ON name_t.card_id = them.number_value and name_t.attribute_code='NAME' "+
    	" JOIN card cc on cc.card_id = question.number_value " +
      	whereString+
      	" and cc.template_id = 884 " +
    	" GROUP BY name_t.string_value "+
    	"\n UNION \n"+
    	" Select "+
    	" 12 AS cc "+
    	" ,'          ����������' as name "+
    	" ,'���������� �������� �� �������' as thematic "+
    	" ,count(distinct cc.card_id) as hz "+
    	" from card c "+
    	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
    	 " JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
    	" 	extract('year' from reg_date.date_value) ="+ pYear.toString() +" and extract('quarter' from reg_date.date_value) ="+ quarter.toString() +" "+
    	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
    	" JOIN attribute_value av_contrl ON av_contrl.card_id = c.card_id and av_contrl.attribute_code='ADMIN_283929' and av_contrl.value_id in (Select value_id from values_list where value_rus='����������' or "+ " value_rus='���� �������') "+
    	" JOIN attribute_value question ON question.card_id = c.card_id and question.attribute_code='ADMIN_277251' "+
    	" JOIN attribute_value topic ON topic.card_id = question.number_value and topic.attribute_code='ADMIN_278045' "+
    	" JOIN attribute_value  them ON them.card_id = topic.number_value and them.attribute_code='ADMIN_275720' "+
    	" JOIN attribute_value name_t ON name_t.card_id = them.number_value and name_t.attribute_code='NAME' "+
    	" JOIN card cc on cc.card_id = question.number_value " +
      	whereString+
      	" and cc.template_id = 884 " +
    	" GROUP BY name_t.string_value "+
    	"\n UNION \n"+
    	" Select "+
    	" 13 AS cc "+
    	" ,'          � ��� ����� ���� �������' as name "+
    	" ,'���������� �������� �� �������' as thematic "+
    	" ,count(distinct cc.card_id) as hz "+
    	" from card c "+
    	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
    	 " JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
    	" 	extract('year' from reg_date.date_value) ="+ pYear.toString() +" and extract('quarter' from reg_date.date_value) ="+ quarter.toString() +" "+
    	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
    	" JOIN attribute_value av_contrl ON av_contrl.card_id = c.card_id and av_contrl.attribute_code='ADMIN_283929' and av_contrl.value_id in (Select value_id from values_list where value_rus='���� �������') "+
    	" JOIN attribute_value question ON question.card_id = c.card_id and question.attribute_code='ADMIN_277251' "+
    	" JOIN attribute_value topic ON topic.card_id = question.number_value and topic.attribute_code='ADMIN_278045' "+
    	" JOIN attribute_value  them ON them.card_id = topic.number_value and them.attribute_code='ADMIN_275720' "+
    	" JOIN attribute_value name_t ON name_t.card_id = them.number_value and name_t.attribute_code='NAME' "+
    	" JOIN card cc on cc.card_id = question.number_value " +
      	whereString+
      	" and cc.template_id = 884 " +
    	" GROUP BY name_t.string_value "+
    	"\n UNION \n"+
    	" Select "+
    	" 14 AS cc "+
    	" ,'          ����������' as name "+
    	" ,'���������� �������� �� �������' as thematic "+
    	" ,count(distinct cc.card_id) as hz "+
    	" from card c "+
    	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
    	"  JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
    		" extract('year' from reg_date.date_value) ="+ pYear.toString() +" and extract('quarter' from reg_date.date_value) ="+ quarter.toString() +" "+
    	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
    	" JOIN attribute_value av_contrl ON av_contrl.card_id = c.card_id and av_contrl.attribute_code='ADMIN_283929' and av_contrl.value_id in (Select value_id from values_list where value_rus='����������') "+
    	" JOIN attribute_value question ON question.card_id = c.card_id and question.attribute_code='ADMIN_277251' "+
    	" JOIN attribute_value topic ON topic.card_id = question.number_value and topic.attribute_code='ADMIN_278045' "+
    	" JOIN attribute_value  them ON them.card_id = topic.number_value and them.attribute_code='ADMIN_275720' "+
    	" JOIN attribute_value name_t ON name_t.card_id = them.number_value and name_t.attribute_code='NAME' "+
    	" JOIN card cc on cc.card_id = question.number_value " +
      	whereString+
      	" and cc.template_id = 884 " +
    	" GROUP BY name_t.string_value "+
    	"\n UNION \n"+
    	" Select "+
    	" 15 AS cc "+
    	" ,'          �� ����������' as name "+
    	" ,'���������� �������� �� �������' as thematic "+
    	" ,count(distinct cc.card_id) as hz "+
    	" from card c "+
    	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
    	 " JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
    	" 	extract('year' from reg_date.date_value) ="+ pYear.toString() +" and extract('quarter' from reg_date.date_value) ="+ quarter.toString() +" "+
    	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
    	" JOIN attribute_value av_contrl ON av_contrl.card_id = c.card_id and av_contrl.attribute_code='ADMIN_283929' and av_contrl.value_id in (Select value_id from values_list where value_rus='�� ����������') "+
    	" JOIN attribute_value question ON question.card_id = c.card_id and question.attribute_code='ADMIN_277251' "+
    	" JOIN attribute_value topic ON topic.card_id = question.number_value and topic.attribute_code='ADMIN_278045' "+
    	" JOIN attribute_value  them ON them.card_id = topic.number_value and them.attribute_code='ADMIN_275720' "+
    	" JOIN attribute_value name_t ON name_t.card_id = them.number_value and name_t.attribute_code='NAME' "+
    	" JOIN card cc on cc.card_id = question.number_value " +
      	whereString+
      	" and cc.template_id = 884 " +
    	" GROUP BY name_t.string_value "+
    	"\n UNION \n"+
    	" Select "+
    	" 16 AS cc "+
    	" ,'" + config.getProperty(PARAM_SENT, PARAM_NULL) + "' as name "+
    	" ,'���������� �������� �� �������' as thematic "+
    	" ,count(distinct cc.card_id) as hz "+
    	" from card c "+
    	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
    	"  JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
    		" extract('year' from reg_date.date_value) ="+ pYear.toString() +" and extract('quarter' from reg_date.date_value) ="+ quarter.toString() +" "+
    	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
    	" JOIN attribute_value relate_doc ON relate_doc.card_id = c.card_id and relate_doc.attribute_code='JBR_DOCL_RELATDOC' "+
    	" JOIN card relate_doc_card on relate_doc_card.card_id=relate_doc.number_value and relate_doc_card.template_id=364 "+
    		" and relate_doc_card.status_id=101 "+
    	" JOIN attribute_value gub ON gub.card_id = relate_doc_card.card_id and gub.attribute_code='JBR_INFD_SIGNATORY' "+
    	" JOIN person p ON p.person_id = gub.number_value "+
    	" JOIN person_role pr ON pr.person_id = p.person_id and pr.role_code in ('JBR_BOSS','JBR_DEPUTY_MINISTER') " +
    	" JOIN attribute_value question ON question.card_id = c.card_id and question.attribute_code='ADMIN_277251' "+
    	" JOIN attribute_value topic ON topic.card_id = question.number_value and topic.attribute_code='ADMIN_278045' "+
    	" JOIN attribute_value  them ON them.card_id = topic.number_value and them.attribute_code='ADMIN_275720' "+
    	" JOIN attribute_value name_t ON name_t.card_id = them.number_value and name_t.attribute_code='NAME' "+
    	" JOIN card cc on cc.card_id = question.number_value " +
      	whereString+
      	" and cc.template_id = 884 " +
    	" GROUP BY name_t.string_value "+
    	"\n UNION \n"+
    	" Select  "+
    	" 17 AS cc "+
    	" ,'���������� ���������, ������������� ��������� � �������� �������� ��������������' as name "+
    	" ,'���������� �������� �� �������' as thematic "+
    	" ,count(distinct cc.card_id) as hz "+
    	" from card c "+
    	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
    	 " JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
    	" 	extract('year' from reg_date.date_value) ="+ pYear.toString() +" and extract('quarter' from reg_date.date_value) ="+ quarter.toString() +" "+
    	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
    	" JOIN attribute_value cont ON cont.card_id = c.card_id and cont.attribute_code='JBR_EXAMIN_PROCEDURE' and cont.value_id=2049 "+
    	" JOIN attribute_value question ON question.card_id = c.card_id and question.attribute_code='ADMIN_277251' "+
    	" JOIN attribute_value topic ON topic.card_id = question.number_value and topic.attribute_code='ADMIN_278045' "+
    	" JOIN attribute_value  them ON them.card_id = topic.number_value and them.attribute_code='ADMIN_275720' "+
    	" JOIN attribute_value name_t ON name_t.card_id = them.number_value and name_t.attribute_code='NAME' "+
    	" JOIN card cc on cc.card_id = question.number_value " +
      	whereString+
      	" and cc.template_id = 884 " +
    	" GROUP BY name_t.string_value "+
    	"\n UNION \n"+
    	" Select  "+
    	" 18 AS cc "+
    	" ,'���������� ���������, ������������� ��������� � ���������������� �������� ����������� ������� �������������� ������' as name "+
    	" ,'���������� �������� �� �������' as thematic "+
    	" ,count(distinct cc.card_id) as hz "+
    	" from card c "+
    	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
    	"  JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
    		" extract('year' from reg_date.date_value) ="+ pYear.toString() +" and extract('quarter' from reg_date.date_value) ="+ quarter.toString() +" "+
    	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
    	" JOIN attribute_value cont ON cont.card_id = c.card_id and cont.attribute_code='JBR_EXAMIN_PROCEDURE' and cont.value_id=2048 "+
    	" JOIN attribute_value question ON question.card_id = c.card_id and question.attribute_code='ADMIN_277251' "+
    	" JOIN attribute_value topic ON topic.card_id = question.number_value and topic.attribute_code='ADMIN_278045' "+
    	" JOIN attribute_value  them ON them.card_id = topic.number_value and them.attribute_code='ADMIN_275720' "+
    	" JOIN attribute_value name_t ON name_t.card_id = them.number_value and name_t.attribute_code='NAME' "+
    	" JOIN card cc on cc.card_id = question.number_value " +
      	whereString+
      	" and cc.template_id = 884 " +
    	" GROUP BY name_t.string_value "+
    	"\n UNION \n"+
    	" Select  "+
    	" 19 AS cc "+
    	" ,'���������� �����, � ������� ������������� ����������� �����' as name "+
    	" ,'���������� �������� �� �������' as thematic "+
    	" ,count(distinct cc.card_id) as hz "+
    	" from card c "+
    	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
    	 " JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
    	" 	extract('year' from reg_date.date_value) ="+ pYear.toString() +" and extract('quarter' from reg_date.date_value) ="+ quarter.toString() +" "+
    	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
    	" JOIN attribute_value fav_d ON fav_d.card_id = c.card_id and fav_d.attribute_code='JBR_ADDRESS_FORMAT' and fav_d.value_id=2036 "+
    	" JOIN attribute_value d ON d.card_id = c.card_id and d.attribute_code='ADMIN_283926' "+
    	" JOIN attribute_value fav_des ON fav_des.card_id = d.number_value and fav_des.attribute_code='NAME' and  fav_des.string_value='����� �������������' "+
    	" JOIN attribute_value question ON question.card_id = c.card_id and question.attribute_code='ADMIN_277251' "+
    	" JOIN attribute_value topic ON topic.card_id = question.number_value and topic.attribute_code='ADMIN_278045' "+
    	" JOIN attribute_value  them ON them.card_id = topic.number_value and them.attribute_code='ADMIN_275720' "+
    	" JOIN attribute_value name_t ON name_t.card_id = them.number_value and name_t.attribute_code='NAME' "+
    	" JOIN card cc on cc.card_id = question.number_value " +
      	whereString+
      	" and cc.template_id = 884 " +
    	" GROUP BY name_t.string_value "+
    	"\n UNION \n"+
    	" Select  "+
    	" 20 AS cc "+
    	" ,'���������� �����, �� ����������� ������������ ������� �������� � ��������� ���� ������� ��������' as name "+
    	" ,'���������� �������� �� �������' as thematic "+
    	" ,count(distinct cc.card_id) as hz "+
    	" from card c "+
    	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
    	"  JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
    		" extract('year' from reg_date.date_value) ="+ pYear.toString() +" and extract('quarter' from reg_date.date_value) ="+ quarter.toString() +" "+
    	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
    	" JOIN attribute_value fav_d ON fav_d.card_id = c.card_id and fav_d.attribute_code='JBR_ADDRESS_FORMAT' and fav_d.value_id=2036 "+
    	" JOIN attribute_value d ON d.card_id = c.card_id and d.attribute_code='ADMIN_283926' "+
    	" JOIN attribute_value fav_des ON fav_des.card_id = d.number_value and fav_des.attribute_code='NAME' and  fav_des.string_value='�������� ��������' "+
    	" JOIN attribute_value question ON question.card_id = c.card_id and question.attribute_code='ADMIN_277251' "+
    	" JOIN attribute_value topic ON topic.card_id = question.number_value and topic.attribute_code='ADMIN_278045' "+
    	" JOIN attribute_value  them ON them.card_id = topic.number_value and them.attribute_code='ADMIN_275720' "+
    	" JOIN attribute_value name_t ON name_t.card_id = them.number_value and name_t.attribute_code='NAME' "+
    	" JOIN card cc on cc.card_id = question.number_value " +
      	whereString+
      	" and cc.template_id = 884 " +
    	" GROUP BY name_t.string_value "+
    	"\n UNION \n"+
    	" Select  "+
    	" 21 AS cc "+
    	" ,'����������� ��������� � ���������� �����' as name "+
    	" ,'���������� �������� �� �������' as thematic "+
    	" ,count(distinct cc.card_id) as hz "+
    	" from card c "+
    	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
    	 " JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
    	" 	extract('year' from reg_date.date_value) ="+ pYear.toString() +" and extract('quarter' from reg_date.date_value) ="+ quarter.toString() +" "+
    	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
    	" JOIN attribute_value decision ON decision.card_id=c.card_id and decision.attribute_code='ADMIN_283929' and decision.value_id is not null "+
    	" JOIN attribute_value control_date ON control_date.card_id = c.card_id and control_date.attribute_code='JBR_IMPL_DEADLINE'  and control_date.date_value  is not null "+
    	" JOIN attribute_value answer_date ON answer_date.card_id=c.card_id and answer_date.attribute_code='ADMIN_220912' and answer_date.date_value is not null "+
    	   " and date_trunc('day', answer_date.date_value)>date_trunc('day', control_date.date_value) "+
       	" JOIN attribute_value question ON question.card_id = c.card_id and question.attribute_code='ADMIN_277251' "+
    	" JOIN attribute_value topic ON topic.card_id = question.number_value and topic.attribute_code='ADMIN_278045' "+
    	" JOIN attribute_value  them ON them.card_id = topic.number_value and them.attribute_code='ADMIN_275720' "+
    	" JOIN attribute_value name_t ON name_t.card_id = them.number_value and name_t.attribute_code='NAME' "+
    	" JOIN card cc on cc.card_id = question.number_value " +
      	whereString+
      	" and cc.template_id = 884 " +
    	" GROUP BY name_t.string_value "+
    	"\n UNION \n"+
    	" Select  "+
    	" 22 AS cc "+
    	" ,'��������� �� ������������ �� 1 ����� ������ ���������� �� �������� ��������, ����������� � �������� �������' as name "+
    	" ,'���������� �������� �� �������' as thematic "+
    	" ,count(distinct cc.card_id) as hz "+
    	" from card c "+
    	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
    	 " JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
    	" 	extract('year' from reg_date.date_value) ="+ pYear.toString() +" and extract('quarter' from reg_date.date_value) ="+ quarter.toString() +" "+
    	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
    	" JOIN attribute_value control_date ON control_date.card_id = c.card_id and control_date.attribute_code='JBR_IMPL_DEADLINE' "+
    		" and control_date.date_value is not null  and not exists (select 1 from attribute_value v where v.card_id=c.card_id and v.attribute_code='ADMIN_283929' and v.value_id is not null) "+
    	" 	and not exists (select 1 from attribute_value v1 where v1.card_id=c.card_id and v1.attribute_code='ADMIN_220912' and v1.date_value is not null) "+
    	" JOIN attribute_value question ON question.card_id = c.card_id and question.attribute_code='ADMIN_277251' "+
    	" JOIN attribute_value topic ON topic.card_id = question.number_value and topic.attribute_code='ADMIN_278045' "+
    	" JOIN attribute_value  them ON them.card_id = topic.number_value and them.attribute_code='ADMIN_275720' "+
    	" JOIN attribute_value name_t ON name_t.card_id = them.number_value and name_t.attribute_code='NAME' "+
    	" JOIN card cc on cc.card_id = question.number_value " +
      	whereString+
      	" and cc.template_id = 884 " +
    	" GROUP BY name_t.string_value "+
    	"\n UNION \n"+
    	" Select  "+
    	" 24 AS cc "+
    	" ,'          " + config.getProperty(PARAM_IMPROPER, PARAM_NULL) + "' as name "+
    	" ,'���������� �������� �� �������' as thematic "+
    	" ,count(distinct cc.card_id) as hz "+
    	" from card c "+
    	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
    	 " JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
    	" 	extract('year' from reg_date.date_value) ="+ pYear.toString() +" and extract('quarter' from reg_date.date_value) ="+ quarter.toString() +" "+
    	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
    	" JOIN attribute_value fav_d ON fav_d.card_id = c.card_id and fav_d.attribute_code='JBR_ADDRESS_FORMAT' and fav_d.value_id=2036 "+
    	" JOIN attribute_value fav_des ON  fav_des.card_id = c.card_id and  fav_des.attribute_code='JBR_COMPLAINT_REASON' and  fav_des.value_id=2039 "+
    	" JOIN attribute_value question ON question.card_id = c.card_id and question.attribute_code='ADMIN_277251' "+
    	" JOIN attribute_value topic ON topic.card_id = question.number_value and topic.attribute_code='ADMIN_278045' "+
    	" JOIN attribute_value  them ON them.card_id = topic.number_value and them.attribute_code='ADMIN_275720' "+
    	" JOIN attribute_value name_t ON name_t.card_id = them.number_value and name_t.attribute_code='NAME' "+
    	" JOIN card cc on cc.card_id = question.number_value " +
      	whereString+
      	" and cc.template_id = 884 " +
    	" GROUP BY name_t.string_value "+
    	"\n UNION \n"+
    	" Select  "+
    	" 25 AS cc "+
    	" ,'          ���������� � ������ ���������� �� �������������� ��������������� �����' as name "+
    	" ,'���������� �������� �� �������' as thematic "+
    	" ,count(distinct cc.card_id) as hz "+
    	" from card c "+
    	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
    	 " JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
    	" 	extract('year' from reg_date.date_value) ="+ pYear.toString() +" and extract('quarter' from reg_date.date_value) ="+ quarter.toString() +" "+
    	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
    	" JOIN attribute_value fav_d ON fav_d.card_id = c.card_id and fav_d.attribute_code='JBR_ADDRESS_FORMAT' and fav_d.value_id=2036 "+
    	" JOIN attribute_value fav_des ON  fav_des.card_id = c.card_id and  fav_des.attribute_code='JBR_COMPLAINT_REASON' and  fav_des.value_id=2040 "+
    	" JOIN attribute_value question ON question.card_id = c.card_id and question.attribute_code='ADMIN_277251' "+
    	" JOIN attribute_value topic ON topic.card_id = question.number_value and topic.attribute_code='ADMIN_278045' "+
    	" JOIN attribute_value  them ON them.card_id = topic.number_value and them.attribute_code='ADMIN_275720' "+
    	" JOIN attribute_value name_t ON name_t.card_id = them.number_value and name_t.attribute_code='NAME' "+
    	" JOIN card cc on cc.card_id = question.number_value " +
      	whereString+
      	" and cc.template_id = 884 " +
    	" GROUP BY name_t.string_value "+
    	"\n UNION \n"+
    	" Select  "+
    	" 26 AS cc "+
    	" ,'          ��������� ���������������� � ������������ ��������������� ����������, ������������ ����������� � ������� �����������' as name "+
    	" ,'���������� �������� �� �������' as thematic "+
    	" ,count(distinct cc.card_id) as hz "+
    	" from card c "+
    	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
    	 " JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
    	" 	extract('year' from reg_date.date_value) ="+ pYear.toString() +" and extract('quarter' from reg_date.date_value) ="+ quarter.toString() +" "+
    	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
    	" JOIN attribute_value fav_d ON fav_d.card_id = c.card_id and fav_d.attribute_code='JBR_ADDRESS_FORMAT' and fav_d.value_id=2036 "+
    	" JOIN attribute_value fav_des ON  fav_des.card_id = c.card_id and  fav_des.attribute_code='JBR_COMPLAINT_REASON' and  fav_des.value_id=2041 "+
    	" JOIN attribute_value question ON question.card_id = c.card_id and question.attribute_code='ADMIN_277251' "+
    	" JOIN attribute_value topic ON topic.card_id = question.number_value and topic.attribute_code='ADMIN_278045' "+
    	" JOIN attribute_value  them ON them.card_id = topic.number_value and them.attribute_code='ADMIN_275720' "+
    	" JOIN attribute_value name_t ON name_t.card_id = them.number_value and name_t.attribute_code='NAME' "+
    	" JOIN card cc on cc.card_id = question.number_value " +
      	whereString+
      	" and cc.template_id = 884 " +
    	" GROUP BY name_t.string_value "+
    	"\n UNION \n"+
    	" Select  "+
    	" 27 AS cc "+
    	" ,'          " + config.getProperty(PARAM_REJECTION, PARAM_NULL) + "' as name "+
    	" ,'���������� �������� �� �������' as thematic "+
    	" ,count(distinct cc.card_id) as hz "+
    	" from card c "+
    	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
    	 " JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
    	" 	extract('year' from reg_date.date_value) ="+ pYear.toString() +" and extract('quarter' from reg_date.date_value) ="+ quarter.toString() +" "+
    	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
    	" JOIN attribute_value fav_d ON fav_d.card_id = c.card_id and fav_d.attribute_code='JBR_ADDRESS_FORMAT' and fav_d.value_id=2036 "+
    	" JOIN attribute_value fav_des ON  fav_des.card_id = c.card_id and  fav_des.attribute_code='JBR_COMPLAINT_REASON' and  fav_des.value_id=2042 "+
    	" JOIN attribute_value question ON question.card_id = c.card_id and question.attribute_code='ADMIN_277251' "+
    	" JOIN attribute_value topic ON topic.card_id = question.number_value and topic.attribute_code='ADMIN_278045' "+
    	" JOIN attribute_value  them ON them.card_id = topic.number_value and them.attribute_code='ADMIN_275720' "+
    	" JOIN attribute_value name_t ON name_t.card_id = them.number_value and name_t.attribute_code='NAME' "+
    	" JOIN card cc on cc.card_id = question.number_value " +
      	whereString+
      	" and cc.template_id = 884 " +
    	" GROUP BY name_t.string_value "+
    	"\n UNION \n"+
    	" Select  "+
    	" 28 AS cc "+
    	" ,'          ������������� ����������������� � ������������ ���������� �� �������������� ��������������� �����' as name "+
    	" ,'���������� �������� �� �������' as thematic "+
    	" ,count(distinct cc.card_id) as hz "+
    	" from card c "+
    	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
    	 " JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
    	" 	extract('year' from reg_date.date_value) ="+ pYear.toString() +" and extract('quarter' from reg_date.date_value) ="+ quarter.toString() +" "+
    	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
    	" JOIN attribute_value fav_d ON fav_d.card_id = c.card_id and fav_d.attribute_code='JBR_ADDRESS_FORMAT' and fav_d.value_id=2036 "+
    	" JOIN attribute_value fav_des ON  fav_des.card_id = c.card_id and  fav_des.attribute_code='JBR_COMPLAINT_REASON' and  fav_des.value_id=2043 "+
    	" JOIN attribute_value question ON question.card_id = c.card_id and question.attribute_code='ADMIN_277251' "+
    	" JOIN attribute_value topic ON topic.card_id = question.number_value and topic.attribute_code='ADMIN_278045' "+
    	" JOIN attribute_value  them ON them.card_id = topic.number_value and them.attribute_code='ADMIN_275720' "+
    	" JOIN attribute_value name_t ON name_t.card_id = them.number_value and name_t.attribute_code='NAME' "+
    	" JOIN card cc on cc.card_id = question.number_value " +
      	whereString+
      	" and cc.template_id = 884 " +
    	" GROUP BY name_t.string_value "+
    	"\n UNION \n"+
    	" Select  "+
    	" 29 AS cc "+
    	" ,'          ������ �������� ����������� �������' as name "+
    	" ,'���������� �������� �� �������' as thematic "+
    	" ,count(distinct cc.card_id) as hz "+
    	" from card c "+
    	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
    	 " JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
    	" 	extract('year' from reg_date.date_value) ="+ pYear.toString() +" and extract('quarter' from reg_date.date_value) ="+ quarter.toString() +" "+
    	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
    	" JOIN attribute_value fav_d ON fav_d.card_id = c.card_id and fav_d.attribute_code='JBR_ADDRESS_FORMAT' and fav_d.value_id=2036 "+
    	" JOIN attribute_value fav_des ON  fav_des.card_id = c.card_id and  fav_des.attribute_code='JBR_COMPLAINT_REASON' and  fav_des.value_id=2044 "+
    	" JOIN attribute_value question ON question.card_id = c.card_id and question.attribute_code='ADMIN_277251' "+
    	" JOIN attribute_value topic ON topic.card_id = question.number_value and topic.attribute_code='ADMIN_278045' "+
    	" JOIN attribute_value  them ON them.card_id = topic.number_value and them.attribute_code='ADMIN_275720' "+
    	" JOIN attribute_value name_t ON name_t.card_id = them.number_value and name_t.attribute_code='NAME' "+
    	" JOIN card cc on cc.card_id = question.number_value " +
      	whereString+
      	" and cc.template_id = 884 " +
    	" GROUP BY name_t.string_value "+
    	"\n UNION \n"+
    	" Select  "+
    	" 30 AS cc "+
    	" ,'          ������' as name "+
    	" ,'���������� �������� �� �������' as thematic "+
    	" ,count(distinct cc.card_id) as hz "+
    	" from card c "+
    	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
    	 " JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
    	" 	extract('year' from reg_date.date_value) ="+ pYear.toString() +" and extract('quarter' from reg_date.date_value) ="+ quarter.toString() +" "+
    	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
    	" JOIN attribute_value fav_d ON fav_d.card_id = c.card_id and fav_d.attribute_code='JBR_ADDRESS_FORMAT' and fav_d.value_id=2036 "+
    	" JOIN attribute_value fav_des ON  fav_des.card_id = c.card_id and  fav_des.attribute_code='JBR_COMPLAINT_REASON' and  fav_des.value_id=2045 "+
    	" JOIN attribute_value question ON question.card_id = c.card_id and question.attribute_code='ADMIN_277251' "+
    	" JOIN attribute_value topic ON topic.card_id = question.number_value and topic.attribute_code='ADMIN_278045' "+
    	" JOIN attribute_value  them ON them.card_id = topic.number_value and them.attribute_code='ADMIN_275720' "+
    	" JOIN attribute_value name_t ON name_t.card_id = them.number_value and name_t.attribute_code='NAME' "+
    	" JOIN card cc on cc.card_id = question.number_value " +
      	whereString+
      	" and cc.template_id = 884 " +
    	" GROUP BY name_t.string_value "+
    	" ORDER BY cc ");
	}
	
	private void getOGByYear(StringBuilder sql, Integer prYear, Integer pYear, Integer firstMonthCurQuarter, 
			Integer prDate, Integer quarter, String whereString) {
		//���������� ��������� � ������ ����
        sql.append(" Select    "+
              	" 1 AS cc  "+
              	" ,'����������� ������� �� ���������� �������� ������' as name  "+
              	" ,'���������� ��������� � ������ ����' as thematic  "+
              	" ,count(distinct c.card_id) as hz  "+
              	" from card c  "+
              	" JOIN template t on t.template_id=864 and t.template_id=c.template_id  "+
              	" JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and  "+
              	" 	extract('year' from reg_date.date_value) =   "+ pYear.toString() +"    "+
              	" join card_version cv on cv.card_id = c.card_id and cv.version_date = (select coalesce ((select cv2.version_date from card_version cv2 where cv2.card_id = c.card_id " +
            	"							and cv2.version_date < date_trunc('day', TO_DATE(" + pYear.toString() + " || '-' || " + firstMonthCurQuarter.toString() + " || '-' || '1', 'YYYY-MM-DD') + interval '0 hour') + INTERVAL '1 DAY' " +
            	"							and cv2.version_date >= date_trunc('day', TO_DATE(" + pYear.toString() + " || '-' || " + firstMonthCurQuarter.toString() + " || '-' || '1', 'YYYY-MM-DD') + interval '0 hour') " +
            	"							and cv2.status_id in (101, 102, 103, 301) " +
            	"							limit 1 " +
  			    "							), " +
            	"		  					(select cv2.version_date from card_version cv2 where cv2.card_id = c.card_id " +
            	"							and cv2.version_date < date_trunc('day', TO_DATE(" + prYear.toString() + " || '-' || " + firstMonthCurQuarter.toString() + " || '-' || '1', 'YYYY-MM-DD') + interval '0 hour') " +
            	"		  					and cv2.status_id in (101, 102, 103, 301) " +
            	"		  					order by cv2.version_date desc " +
            	"		  					limit 1 " +
            	"		  					)) " +
            	" 		  																) " +
              	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
              	whereString+
              	"\n UNION \n"+ 
    			" Select  "+
            	" 2 AS cc "+
            	" ,'" + config.getProperty(PARAM_RECEIVED, PARAM_NULL) + "' as name "+
            	" ,'���������� ��������� � ������ ����' as thematic  "+
            	" ,count(distinct c.card_id) as hz "+
            	" from card c "+
            	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
            	 " JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
            	" 	extract('year' from reg_date.date_value) ="+ pYear.toString() + " "+
            	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
              	whereString+
            	"\n UNION \n"+
            	" Select  "+
            	" 3 AS cc "+
            	" ,'          � ��� ����� ���������' as name "+
            	" ,'���������� ��������� � ������ ����' as thematic  "+
            	" ,count(distinct c.card_id) as hz "+
            	" from card c "+
            	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
            	" JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
            	" 	extract('year' from reg_date.date_value) ="+ pYear.toString() + " "+
            	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
            	" JOIN attribute_value format_obr ON format_obr.card_id = c.card_id and format_obr.attribute_code='JBR_ADDRESS_FORMAT' and format_obr.value_id=2037 "+
              	whereString+
            	"\n UNION \n"+
            	" Select  "+
            	" 4 AS cc "+
            	" ,'          � ��� ����� �����' as name "+
            	" ,'���������� ��������� � ������ ����' as thematic  "+
            	" ,count(distinct c.card_id) as hz "+
            	" from card c "+
            	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
            	"  JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
            		" extract('year' from reg_date.date_value) ="+ pYear.toString() + " "+
            	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
            	" JOIN attribute_value format_obr ON format_obr.card_id = c.card_id and format_obr.attribute_code='JBR_ADDRESS_FORMAT' and format_obr.value_id=2036 "+
              	whereString+
            	"\n UNION \n"+
            	" Select  "+
            	" 5 AS cc "+
            	" ,'          � ��� ����� �����������' as name "+
            	" ,'���������� ��������� � ������ ����' as thematic  "+
            	" ,count(distinct c.card_id) as hz "+
            	" from card c "+
            	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
            	 " JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
            	" 	extract('year' from reg_date.date_value) ="+ pYear.toString() + " "+
            	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
            	" JOIN attribute_value format_obr ON format_obr.card_id = c.card_id and format_obr.attribute_code='JBR_ADDRESS_FORMAT' and format_obr.value_id=2038 "+
              	whereString+
            	"\n UNION \n"+
            	" Select  "+
            	" 7 AS cc "+
            	" ,'          " + config.getProperty(PARAM_REPORTED, PARAM_NULL) + "' as name "+
            	" ,'���������� ��������� � ������ ����' as thematic  "+
            	" ,count(distinct c.card_id) as hz "+
            	" from card c "+
            	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
            	 " JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
            	" 	extract('year' from reg_date.date_value) ="+ pYear.toString() + " "+
            	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
            	" JOIN attribute_value gub ON gub.card_id = c.card_id and gub.attribute_code='JBR_IMPL_ACQUAINTERS' "+
            	" JOIN person p ON p.person_id = gub.number_value "+
            	" JOIN person_role pr ON pr.person_id = p.person_id and pr.role_code in ('JBR_BOSS') " +
              	whereString+
            	"\n UNION \n"+
            	" Select "+
            	" 8 AS cc "+
            	" ,'          ����� �� ��������' as name "+
            	" ,'���������� ��������� � ������ ����' as thematic  "+
            	" ,count(distinct c.card_id) as hz "+
            	" from card c "+
            	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
            	" JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
            		" extract('year' from reg_date.date_value) ="+ pYear.toString() + " "+
            	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
            	" JOIN attribute_value av_contrl ON av_contrl.card_id = c.card_id and av_contrl.attribute_code='JBR_IMPL_ONCONT' and av_contrl.value_id=1432 "+
              	whereString+
            	"\n UNION \n"+
            	" Select "+
            	" 9 AS cc "+
            	" ,'          ����������� � ������� �� �����' as name "+
            	" ,'���������� ��������� � ������ ����' as thematic  "+
            	" ,count(distinct c.card_id) as hz "+
            	" from card c "+
            	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
            	 " JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
            	" 	extract('year' from reg_date.date_value) ="+ pYear.toString() + " "+
            	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
            	" JOIN attribute_value av_contrl ON av_contrl.card_id = c.card_id and av_contrl.attribute_code='JBR_EXAMIN_PROCEDURE' and av_contrl.value_id=2046 "+
              	whereString+
             	"\n UNION \n"+
            	" Select "+
            	" 10 AS cc "+
            	" ,'          ����������� ������������' as name "+
            	" ,'���������� ��������� � ������ ����' as thematic  "+
            	" ,count(distinct c.card_id) as hz "+
            	" from card c "+
            	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
            	"  JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
            		" extract('year' from reg_date.date_value) ="+ pYear.toString() + " "+
            	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
            	" JOIN attribute_value av_contrl ON av_contrl.card_id = c.card_id and av_contrl.attribute_code='JBR_EXAMIN_PROCEDURE' and av_contrl.value_id=2047 "+
              	whereString+
            	"\n UNION \n"+
            	" Select "+
            	" 12 AS cc "+
            	" ,'          ����������' as name "+
            	" ,'���������� ��������� � ������ ����' as thematic  "+
            	" ,count(distinct c.card_id) as hz "+
            	" from card c "+
            	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
            	 " JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
            	" 	extract('year' from reg_date.date_value) ="+ pYear.toString() + " "+
            	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
            	" JOIN attribute_value av_contrl ON av_contrl.card_id = c.card_id and av_contrl.attribute_code='ADMIN_283929' and av_contrl.value_id in (Select value_id from values_list where value_rus='����������' or "+ " value_rus='���� �������') "+
              	whereString+
            	"\n UNION \n"+
            	" Select "+
            	" 13 AS cc "+
            	" ,'          � ��� ����� ���� �������' as name "+
            	" ,'���������� ��������� � ������ ����' as thematic  "+
            	" ,count(distinct c.card_id) as hz "+
            	" from card c "+
            	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
            	 " JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
            	" 	extract('year' from reg_date.date_value) ="+ pYear.toString() + " "+
            	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
            	" JOIN attribute_value av_contrl ON av_contrl.card_id = c.card_id and av_contrl.attribute_code='ADMIN_283929' and av_contrl.value_id in (Select value_id from values_list where value_rus='���� �������') "+
              	whereString+
            	"\n UNION \n"+
            	" Select "+
            	" 14 AS cc "+
            	" ,'          ����������' as name "+
            	" ,'���������� ��������� � ������ ����' as thematic  "+
            	" ,count(distinct c.card_id) as hz "+
            	" from card c "+
            	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
            	"  JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
            	" extract('year' from reg_date.date_value) ="+ pYear.toString() + " "+
            	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
            	" JOIN attribute_value av_contrl ON av_contrl.card_id = c.card_id and av_contrl.attribute_code='ADMIN_283929' and av_contrl.value_id in (Select value_id from values_list where value_rus='����������') "+
              	whereString+
            	"\n UNION \n"+
            	" Select "+
            	" 15 AS cc "+
            	" ,'          �� ����������' as name "+
            	" ,'���������� ��������� � ������ ����' as thematic  "+
            	" ,count(distinct c.card_id) as hz "+
            	" from card c "+
            	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
            	" JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
            	" 	extract('year' from reg_date.date_value) ="+ pYear.toString() + " "+
            	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
            	" JOIN attribute_value av_contrl ON av_contrl.card_id = c.card_id and av_contrl.attribute_code='ADMIN_283929' and av_contrl.value_id in (Select value_id from values_list where value_rus='�� ����������') "+
              	whereString+
            	"\n UNION \n"+
            	" Select "+
            	" 16 AS cc "+
            	" ,'" + config.getProperty(PARAM_SENT, PARAM_NULL) + "' as name "+
            	" ,'���������� ��������� � ������ ����' as thematic  "+
            	" ,count(distinct c.card_id) as hz "+
            	" from card c "+
            	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
            	"  JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
            	" extract('year' from reg_date.date_value) ="+ pYear.toString() + " "+
            	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
            	" JOIN attribute_value relate_doc ON relate_doc.card_id = c.card_id and relate_doc.attribute_code='JBR_DOCL_RELATDOC' "+
            	" JOIN card relate_doc_card on relate_doc_card.card_id=relate_doc.number_value and relate_doc_card.template_id=364 "+
            	" and relate_doc_card.status_id=101 "+
            	" JOIN attribute_value gub ON gub.card_id = relate_doc_card.card_id and gub.attribute_code='JBR_INFD_SIGNATORY' "+
            	" JOIN person p ON p.person_id = gub.number_value "+
            	" JOIN person_role pr ON pr.person_id = p.person_id and pr.role_code in ('JBR_BOSS','JBR_DEPUTY_MINISTER') " +
              	whereString+
            	"\n UNION \n"+
            	" Select  "+
            	" 17 AS cc "+
            	" ,'���������� ���������, ������������� ��������� � �������� �������� ��������������' as name "+
            	" ,'���������� ��������� � ������ ����' as thematic  "+
            	" ,count(distinct c.card_id) as hz "+
            	" from card c "+
            	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
            	 " JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
            	" 	extract('year' from reg_date.date_value) ="+ pYear.toString() + " "+
            	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
            	" JOIN attribute_value cont ON cont.card_id = c.card_id and cont.attribute_code='JBR_EXAMIN_PROCEDURE' and cont.value_id=2049 "+
              	whereString+
            	"\n UNION \n"+
            	" Select  "+
            	" 18 AS cc "+
            	" ,'���������� ���������, ������������� ��������� � ���������������� �������� ����������� ������� �������������� ������' as name "+
            	" ,'���������� ��������� � ������ ����' as thematic  "+
            	" ,count(distinct c.card_id) as hz "+
            	" from card c "+
            	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
            	"  JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
            		" extract('year' from reg_date.date_value) ="+ pYear.toString() + " "+
            	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
            	" JOIN attribute_value cont ON cont.card_id = c.card_id and cont.attribute_code='JBR_EXAMIN_PROCEDURE' and cont.value_id=2048 "+
              	whereString+
            	"\n UNION \n"+
            	" Select  "+
            	" 19 AS cc "+
            	" ,'���������� �����, � ������� ������������� ����������� �����' as name "+
            	" ,'���������� ��������� � ������ ����' as thematic  "+
            	" ,count(distinct c.card_id) as hz "+
            	" from card c "+
            	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
            	 " JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
            	" 	extract('year' from reg_date.date_value) ="+ pYear.toString() + " "+
            	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
            	" JOIN attribute_value fav_d ON fav_d.card_id = c.card_id and fav_d.attribute_code='JBR_ADDRESS_FORMAT' and fav_d.value_id=2036 "+
            	" JOIN attribute_value d ON d.card_id = c.card_id and d.attribute_code='ADMIN_283926' "+
            	" JOIN attribute_value fav_des ON fav_des.card_id = d.number_value and fav_des.attribute_code='NAME' and  fav_des.string_value='����� �������������' "+
              	whereString+
            	"\n UNION \n"+
            	" Select  "+
            	" 20 AS cc "+
            	" ,'���������� �����, �� ����������� ������������ ������� �������� � ��������� ���� ������� ��������' as name "+
            	" ,'���������� ��������� � ������ ����' as thematic  "+
            	" ,count(distinct c.card_id) as hz "+
            	" from card c "+
            	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
            	"  JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
            		" extract('year' from reg_date.date_value) ="+ pYear.toString() + " "+
            	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
            	" JOIN attribute_value fav_d ON fav_d.card_id = c.card_id and fav_d.attribute_code='JBR_ADDRESS_FORMAT' and fav_d.value_id=2036 "+
            	" JOIN attribute_value d ON d.card_id = c.card_id and d.attribute_code='ADMIN_283926' "+
            	" JOIN attribute_value fav_des ON fav_des.card_id = d.number_value and fav_des.attribute_code='NAME' and  fav_des.string_value='�������� ��������' "+
              	whereString+
            	"\n UNION \n"+
            	" Select  "+
            	" 21 AS cc "+
            	" ,'����������� ��������� � ���������� �����' as name "+
            	" ,'���������� ��������� � ������ ����' as thematic  "+
            	" ,count(distinct c.card_id) as hz "+
            	" from card c "+
            	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
            	 " JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
            	" 	extract('year' from reg_date.date_value) ="+ pYear.toString() + " "+
            	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
            	" JOIN attribute_value decision ON decision.card_id=c.card_id and decision.attribute_code='ADMIN_283929' and decision.value_id is not null "+
            	" JOIN attribute_value control_date ON control_date.card_id = c.card_id and control_date.attribute_code='JBR_IMPL_DEADLINE'  and control_date.date_value  is not null "+
            	" JOIN attribute_value answer_date ON answer_date.card_id=c.card_id and answer_date.attribute_code='ADMIN_220912' and answer_date.date_value is not null "+
            	" and date_trunc('day', answer_date.date_value)>date_trunc('day', control_date.date_value) "+
              	whereString+
             	"\n UNION \n"+
            	" Select  "+
            	" 22 AS cc "+
            	" ,'��������� �� ������������ �� 1 ����� ������ ���������� �� �������� ��������, ����������� � �������� �������' as name "+
            	" ,'���������� ��������� � ������ ����' as thematic  "+
            	" ,count(distinct c.card_id) as hz "+
            	" from card c "+
            	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
            	 " JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
            	" 	extract('year' from reg_date.date_value) ="+ pYear.toString() + " "+
            	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
            	" JOIN attribute_value control_date ON control_date.card_id = c.card_id and control_date.attribute_code='JBR_IMPL_DEADLINE' "+
            		" and control_date.date_value is not null  and not exists (select 1 from attribute_value v where v.card_id=c.card_id and v.attribute_code='ADMIN_283929' and v.value_id is not null) "+
            	" 	and not exists (select 1 from attribute_value v1 where v1.card_id=c.card_id and v1.attribute_code='ADMIN_220912' and v1.date_value is not null) "+
              	whereString+
            	"\n UNION \n"+
            	" Select  "+
            	" 24 AS cc "+
            	" ,'          " + config.getProperty(PARAM_IMPROPER, PARAM_NULL) + "' as name "+
            	" ,'���������� ��������� � ������ ����' as thematic  "+
            	" ,count(distinct c.card_id) as hz "+
            	" from card c "+
            	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
            	 " JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
            	" 	extract('year' from reg_date.date_value) ="+ pYear.toString() + " "+
            	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
            	" JOIN attribute_value fav_d ON fav_d.card_id = c.card_id and fav_d.attribute_code='JBR_ADDRESS_FORMAT' and fav_d.value_id=2036 "+
            	" JOIN attribute_value fav_des ON  fav_des.card_id = c.card_id and  fav_des.attribute_code='JBR_COMPLAINT_REASON' and  fav_des.value_id=2039 "+
              	whereString+
            	"\n UNION \n"+
            	" Select  "+
            	" 25 AS cc "+
            	" ,'          ���������� � ������ ���������� �� �������������� ��������������� �����' as name "+
            	" ,'���������� ��������� � ������ ����' as thematic  "+
            	" ,count(distinct c.card_id) as hz "+
            	" from card c "+
            	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
            	 " JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
            	" 	extract('year' from reg_date.date_value) ="+ pYear.toString() + " "+
            	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
            	" JOIN attribute_value fav_d ON fav_d.card_id = c.card_id and fav_d.attribute_code='JBR_ADDRESS_FORMAT' and fav_d.value_id=2036 "+
            	" JOIN attribute_value fav_des ON  fav_des.card_id = c.card_id and  fav_des.attribute_code='JBR_COMPLAINT_REASON' and  fav_des.value_id=2040 "+
              	whereString+
            	"\n UNION \n"+
            	" Select  "+
            	" 26 AS cc "+
            	" ,'          ��������� ���������������� � ������������ ��������������� ����������, ������������ ����������� � ������� �����������' as name "+
            	" ,'���������� ��������� � ������ ����' as thematic  "+
            	" ,count(distinct c.card_id) as hz "+
            	" from card c "+
            	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
            	 " JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
            	" 	extract('year' from reg_date.date_value) ="+ pYear.toString() + " "+
            	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
            	" JOIN attribute_value fav_d ON fav_d.card_id = c.card_id and fav_d.attribute_code='JBR_ADDRESS_FORMAT' and fav_d.value_id=2036 "+
            	" JOIN attribute_value fav_des ON  fav_des.card_id = c.card_id and  fav_des.attribute_code='JBR_COMPLAINT_REASON' and  fav_des.value_id=2041 "+
              	whereString+
            	"\n UNION \n"+
            	" Select  "+
            	" 27 AS cc "+
            	" ,'          " + config.getProperty(PARAM_REJECTION, PARAM_NULL) + "' as name "+
            	" ,'���������� ��������� � ������ ����' as thematic  "+
            	" ,count(distinct c.card_id) as hz "+
            	" from card c "+
            	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
            	 " JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
            	" 	extract('year' from reg_date.date_value) ="+ pYear.toString() + " "+
            	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
            	" JOIN attribute_value fav_d ON fav_d.card_id = c.card_id and fav_d.attribute_code='JBR_ADDRESS_FORMAT' and fav_d.value_id=2036 "+
            	" JOIN attribute_value fav_des ON  fav_des.card_id = c.card_id and  fav_des.attribute_code='JBR_COMPLAINT_REASON' and  fav_des.value_id=2042 "+
              	whereString+
            	"\n UNION \n"+
            	" Select  "+
            	" 28 AS cc "+
            	" ,'          ������������� ����������������� � ������������ ���������� �� �������������� ��������������� �����' as name "+
            	" ,'���������� ��������� � ������ ����' as thematic  "+
            	" ,count(distinct c.card_id) as hz "+
            	" from card c "+
            	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
            	 " JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
            	" 	extract('year' from reg_date.date_value) ="+ pYear.toString() + " "+
            	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
            	" JOIN attribute_value fav_d ON fav_d.card_id = c.card_id and fav_d.attribute_code='JBR_ADDRESS_FORMAT' and fav_d.value_id=2036 "+
            	" JOIN attribute_value fav_des ON  fav_des.card_id = c.card_id and  fav_des.attribute_code='JBR_COMPLAINT_REASON' and  fav_des.value_id=2043 "+
              	whereString+
            	"\n UNION \n"+
            	" Select  "+
            	" 29 AS cc "+
            	" ,'          ������ �������� ����������� �������' as name "+
            	" ,'���������� ��������� � ������ ����' as thematic  "+
            	" ,count(distinct c.card_id) as hz "+
            	" from card c "+
            	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
            	 " JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
            	" 	extract('year' from reg_date.date_value) ="+ pYear.toString() + " "+
            	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
            	" JOIN attribute_value fav_d ON fav_d.card_id = c.card_id and fav_d.attribute_code='JBR_ADDRESS_FORMAT' and fav_d.value_id=2036 "+
            	" JOIN attribute_value fav_des ON  fav_des.card_id = c.card_id and  fav_des.attribute_code='JBR_COMPLAINT_REASON' and  fav_des.value_id=2044 "+
              	whereString+
            	"\n UNION \n"+
            	" Select  "+
            	" 30 AS cc "+
            	" ,'          ������' as name "+
            	" ,'���������� ��������� � ������ ����' as thematic  "+
            	" ,count(distinct c.card_id) as hz "+
            	" from card c "+
            	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
            	 " JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
            	" 	extract('year' from reg_date.date_value) ="+ pYear.toString() + " "+
            	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
            	" JOIN attribute_value fav_d ON fav_d.card_id = c.card_id and fav_d.attribute_code='JBR_ADDRESS_FORMAT' and fav_d.value_id=2036 "+
            	" JOIN attribute_value fav_des ON  fav_des.card_id = c.card_id and  fav_des.attribute_code='JBR_COMPLAINT_REASON' and  fav_des.value_id=2045 "+
              	whereString+
              	"\n UNION \n");
	}
	
	private void getVoprByYear(StringBuilder sql, Integer prYear, Integer pYear, Integer firstMonthCurQuarter, 
			Integer prDate, Integer quarter, String whereString) {
		//���-�� �������� �� ������ �� ��������� �� ���
    	sql.append(			" Select  "+
                " 1 AS cc "+
                " ,'����������� ������� �� ���������� �������� ������' as name "+
                " ,'���������� �������� �� ���' as thematic "+
                " ,count(question.card_id) as hz "+
                " from card c "+
                " JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
                " JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
                " 	extract('year' from reg_date.date_value) ="+ pYear.toString() +" "+
                " join card_version cv on cv.card_id = c.card_id and cv.version_date = (select coalesce ((select cv2.version_date from card_version cv2 where cv2.card_id = c.card_id " +
            	"							and cv2.version_date < date_trunc('day', TO_DATE(" + pYear.toString() + " || '-' || " + firstMonthCurQuarter.toString() + " || '-' || '1', 'YYYY-MM-DD') + interval '0 hour') + INTERVAL '1 DAY' " +
            	"							and cv2.version_date >= date_trunc('day', TO_DATE(" + pYear.toString() + " || '-' || " + firstMonthCurQuarter.toString() + " || '-' || '1', 'YYYY-MM-DD') + interval '0 hour') " +
            	"							and cv2.status_id in (101, 102, 103, 301) " +
            	"							limit 1 " +
  			    "							), " +
            	"		  					(select cv2.version_date from card_version cv2 where cv2.card_id = c.card_id " +
            	"							and cv2.version_date < date_trunc('day', TO_DATE(" + prYear.toString() + " || '-' || " + firstMonthCurQuarter.toString() + " || '-' || '1', 'YYYY-MM-DD') + interval '0 hour') " +
            	"		  					and cv2.status_id in (101, 102, 103, 301) " +
            	"		  					order by cv2.version_date desc " +
            	"		  					limit 1 " +
            	"		  					)) " +
            	" 		  																) " +
                " JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
            	" JOIN attribute_value question ON question.card_id = c.card_id and question.attribute_code='ADMIN_277251' "+
              	whereString+
                "\n UNION \n"+
            	" Select  "+
            	" 2 AS cc "+
            	" ,'" + config.getProperty(PARAM_RECEIVED, PARAM_NULL) + "' as name "+
            	" ,'���������� �������� �� ���' as thematic "+
            	" ,count(question.card_id) as hz "+
            	" from card c "+
            	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
            	" JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
            	" 	extract('year' from reg_date.date_value) ="+ pYear.toString() + " "+
            	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
            	" JOIN attribute_value question ON  question.card_id = c.card_id and question.attribute_code='ADMIN_277251' "+
              	whereString+
            	"\n UNION \n"+
            	" Select  "+
            	" 3 AS cc "+
            	" ,'          � ��� ����� ���������' as name "+
            	" ,'���������� �������� �� ���' as thematic "+
            	" ,count(question.card_id) as hz "+
            	" from card c "+
            	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
            	" JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
            	" 	extract('year' from reg_date.date_value) ="+ pYear.toString() + " "+
            	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
            	" JOIN attribute_value format_obr ON format_obr.card_id = c.card_id and format_obr.attribute_code='JBR_ADDRESS_FORMAT' and format_obr.value_id=2037 "+
            	" JOIN attribute_value question ON  question.card_id = c.card_id and question.attribute_code='ADMIN_277251' "+
              	whereString+
            	"\n UNION \n"+
            	" Select  "+
            	" 4 AS cc "+
            	" ,'          � ��� ����� �����' as name "+
            	" ,'���������� �������� �� ���' as thematic "+
            	" ,count(question.card_id) as hz "+
            	" from card c "+
            	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
            	"  JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
            	" extract('year' from reg_date.date_value) ="+ pYear.toString() + " "+
            	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
            	" JOIN attribute_value format_obr ON format_obr.card_id = c.card_id and format_obr.attribute_code='JBR_ADDRESS_FORMAT' and format_obr.value_id=2036 "+
            	" JOIN attribute_value question ON  question.card_id = c.card_id and question.attribute_code='ADMIN_277251' "+
              	whereString+
            	"\n UNION \n"+
            	" Select  "+
            	" 5 AS cc "+
            	" ,'          � ��� ����� �����������' as name "+
            	" ,'���������� �������� �� ���' as thematic "+
            	" ,count(question.card_id) as hz "+
            	" from card c "+
            	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
            	 " JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
            	" 	extract('year' from reg_date.date_value) ="+ pYear.toString() + " "+
            	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
            	" JOIN attribute_value format_obr ON format_obr.card_id = c.card_id and format_obr.attribute_code='JBR_ADDRESS_FORMAT' and format_obr.value_id=2038 "+
            	" JOIN attribute_value question ON  question.card_id = c.card_id and question.attribute_code='ADMIN_277251' "+
              	whereString+
            	"\n UNION \n"+
            	" Select  "+
            	" 7 AS cc "+
            	" ,'          " + config.getProperty(PARAM_REPORTED, PARAM_NULL) + "' as name "+
            	" ,'���������� �������� �� ���' as thematic "+
            	" ,count(question.card_id) as hz "+
            	" from card c "+
            	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
            	 " JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
            	" 	extract('year' from reg_date.date_value) ="+ pYear.toString() + " "+
            	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
            	" JOIN attribute_value gub ON gub.card_id = c.card_id and gub.attribute_code='JBR_IMPL_ACQUAINTERS' "+
            	" JOIN person p ON p.person_id = gub.number_value "+
            	" JOIN person_role pr ON pr.person_id = p.person_id and pr.role_code in ('JBR_BOSS') " +
            	" JOIN attribute_value question ON  question.card_id = c.card_id and question.attribute_code='ADMIN_277251' "+
              	whereString+
            	"\n UNION \n"+
            	" Select "+
            	" 8 AS cc "+
            	" ,'          ����� �� ��������' as name "+
            	" ,'���������� �������� �� ���' as thematic "+
            	" ,count(question.card_id) as hz "+
            	" from card c "+
            	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
            	" JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
            	" extract('year' from reg_date.date_value) ="+ pYear.toString() + " "+
            	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
            	" JOIN attribute_value av_contrl ON av_contrl.card_id = c.card_id and av_contrl.attribute_code='JBR_IMPL_ONCONT' and av_contrl.value_id=1432 "+
            	" JOIN attribute_value question ON  question.card_id = c.card_id and question.attribute_code='ADMIN_277251' "+
              	whereString+
            	"\n UNION \n"+
            	" Select "+
            	" 9 AS cc "+
            	" ,'          ����������� � ������� �� �����' as name "+
            	" ,'���������� �������� �� ���' as thematic "+
            	" ,count(question.card_id) as hz "+
            	" from card c "+
            	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
            	 " JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
            	" 	extract('year' from reg_date.date_value) ="+ pYear.toString() + " "+
            	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
            	" JOIN attribute_value av_contrl ON av_contrl.card_id = c.card_id and av_contrl.attribute_code='JBR_EXAMIN_PROCEDURE' and av_contrl.value_id=2046 "+
            	" JOIN attribute_value question ON  question.card_id = c.card_id and question.attribute_code='ADMIN_277251' "+
              	whereString+
            	"\n UNION \n"+
            	" Select "+
            	" 10 AS cc "+
            	" ,'          ����������� ������������' as name "+
            	" ,'���������� �������� �� ���' as thematic "+
            	" ,count(question.card_id) as hz "+
            	" from card c "+
            	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
            	"  JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
            	" extract('year' from reg_date.date_value) ="+ pYear.toString() + " "+
            	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
            	" JOIN attribute_value av_contrl ON av_contrl.card_id = c.card_id and av_contrl.attribute_code='JBR_EXAMIN_PROCEDURE' and av_contrl.value_id=2047 "+
            	" JOIN attribute_value question ON  question.card_id = c.card_id and question.attribute_code='ADMIN_277251' "+
              	whereString+
            	"\n UNION \n"+
            	" Select "+
            	" 12 AS cc "+
            	" ,'          ����������' as name "+
            	" ,'���������� �������� �� ���' as thematic "+
            	" ,count(question.card_id) as hz "+
            	" from card c "+
            	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
            	" JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
            	" 	extract('year' from reg_date.date_value) ="+ pYear.toString() + " "+
            	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
            	" JOIN attribute_value av_contrl ON av_contrl.card_id = c.card_id and av_contrl.attribute_code='ADMIN_283929' and av_contrl.value_id in (Select value_id from values_list where value_rus='����������' or "+ " value_rus='���� �������') "+
            	" JOIN attribute_value question ON  question.card_id = c.card_id and question.attribute_code='ADMIN_277251' "+
              	whereString+
            	"\n UNION \n"+
            	" Select "+
            	" 13 AS cc "+
            	" ,'          � ��� ����� ���� �������' as name "+
            	" ,'���������� �������� �� ���' as thematic "+
            	" ,count(question.card_id) as hz "+
            	" from card c "+
            	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
            	 " JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
            	" 	extract('year' from reg_date.date_value) ="+ pYear.toString() + " "+
            	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
            	" JOIN attribute_value av_contrl ON av_contrl.card_id = c.card_id and av_contrl.attribute_code='ADMIN_283929' and av_contrl.value_id in (Select value_id from values_list where value_rus='���� �������') "+
            	" JOIN attribute_value question ON  question.card_id = c.card_id and question.attribute_code='ADMIN_277251' "+
              	whereString+
            	"\n UNION \n"+
            	" Select "+
            	" 14 AS cc "+
            	" ,'          ����������' as name "+
            	" ,'���������� �������� �� ���' as thematic "+
            	" ,count(question.card_id) as hz "+
            	" from card c "+
            	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
            	"  JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
            	" extract('year' from reg_date.date_value) ="+ pYear.toString() + " "+
            	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
            	" JOIN attribute_value av_contrl ON av_contrl.card_id = c.card_id and av_contrl.attribute_code='ADMIN_283929' and av_contrl.value_id in (Select value_id from values_list where value_rus='����������') "+
            	" JOIN attribute_value question ON  question.card_id = c.card_id and question.attribute_code='ADMIN_277251' "+
              	whereString+
             	"\n UNION \n"+
            	" Select "+
            	" 15 AS cc "+
            	" ,'          �� ����������' as name "+
            	" ,'���������� �������� �� ���' as thematic "+
            	" ,count(question.card_id) as hz "+
            	" from card c "+
            	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
            	 " JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
            	" 	extract('year' from reg_date.date_value) ="+ pYear.toString() + " "+
            	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
            	" JOIN attribute_value av_contrl ON av_contrl.card_id = c.card_id and av_contrl.attribute_code='ADMIN_283929' and av_contrl.value_id in (Select value_id from values_list where value_rus='�� ����������') "+
            	" JOIN attribute_value question ON  question.card_id = c.card_id and question.attribute_code='ADMIN_277251' "+
              	whereString+
            	"\n UNION \n"+
            	" Select "+
            	" 16 AS cc "+
            	" ,'" + config.getProperty(PARAM_SENT, PARAM_NULL) + "' as name "+
            	" ,'���������� �������� �� ���' as thematic "+
            	" ,count(question.card_id) as hz "+
            	" from card c "+
            	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
            	"  JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
            	" extract('year' from reg_date.date_value) ="+ pYear.toString() + " "+
            	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
            	" JOIN attribute_value relate_doc ON relate_doc.card_id = c.card_id and relate_doc.attribute_code='JBR_DOCL_RELATDOC' "+
            	" JOIN card relate_doc_card on relate_doc_card.card_id=relate_doc.number_value and relate_doc_card.template_id=364 "+
            	" and relate_doc_card.status_id=101 "+
            	" JOIN attribute_value gub ON gub.card_id = relate_doc_card.card_id and gub.attribute_code='JBR_INFD_SIGNATORY' "+
            	" JOIN person p ON p.person_id = gub.number_value "+
            	" JOIN person_role pr ON pr.person_id = p.person_id and pr.role_code in ('JBR_BOSS','JBR_DEPUTY_MINISTER') " +
            	" JOIN attribute_value question ON  question.card_id = c.card_id and question.attribute_code='ADMIN_277251' "+
              	whereString+
            	"\n UNION \n"+
            	" Select  "+
            	" 17 AS cc "+
            	" ,'���������� ���������, ������������� ��������� � �������� �������� ��������������' as name "+
            	" ,'���������� �������� �� ���' as thematic "+
            	" ,count(question.card_id) as hz "+
            	" from card c "+
            	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
            	" JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
            	" 	extract('year' from reg_date.date_value) ="+ pYear.toString() + " "+
            	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
            	" JOIN attribute_value cont ON cont.card_id = c.card_id and cont.attribute_code='JBR_EXAMIN_PROCEDURE' and cont.value_id=2049 "+
            	" JOIN attribute_value question ON  question.card_id = c.card_id and question.attribute_code='ADMIN_277251' "+
              	whereString+
            	"\n UNION \n"+
            	" Select  "+
            	" 18 AS cc "+
            	" ,'���������� ���������, ������������� ��������� � ���������������� �������� ����������� ������� �������������� ������' as name "+
            	" ,'���������� �������� �� ���' as thematic "+
            	" ,count(question.card_id) as hz "+
            	" from card c "+
            	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
            	"  JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
            	" extract('year' from reg_date.date_value) ="+ pYear.toString() + " "+
            	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
            	" JOIN attribute_value cont ON cont.card_id = c.card_id and cont.attribute_code='JBR_EXAMIN_PROCEDURE' and cont.value_id=2048 "+
            	" JOIN attribute_value question ON  question.card_id = c.card_id and question.attribute_code='ADMIN_277251' "+
              	whereString+
            	"\n UNION \n"+
            	" Select  "+
            	" 19 AS cc "+
            	" ,'���������� �����, � ������� ������������� ����������� �����' as name "+
            	" ,'���������� �������� �� ���' as thematic "+
            	" ,count(question.card_id) as hz "+
            	" from card c "+
            	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
            	" JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
            	" 	extract('year' from reg_date.date_value) ="+ pYear.toString() + " "+
            	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
            	" JOIN attribute_value fav_d ON fav_d.card_id = c.card_id and fav_d.attribute_code='JBR_ADDRESS_FORMAT' and fav_d.value_id=2036 "+
            	" JOIN attribute_value d ON d.card_id = c.card_id and d.attribute_code='ADMIN_283926' "+
            	" JOIN attribute_value fav_des ON fav_des.card_id = d.number_value and fav_des.attribute_code='NAME' and  fav_des.string_value='����� �������������' "+
            	" JOIN attribute_value question ON  question.card_id = c.card_id and question.attribute_code='ADMIN_277251' "+
              	whereString+
            	"\n UNION \n"+
            	" Select  "+
            	" 20 AS cc "+
            	" ,'���������� �����, �� ����������� ������������ ������� �������� � ��������� ���� ������� ��������' as name "+
            	" ,'���������� �������� �� ���' as thematic "+
            	" ,count(question.card_id) as hz "+
            	" from card c "+
            	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
            	"  JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
            	" extract('year' from reg_date.date_value) ="+ pYear.toString() + " "+
            	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
            	" JOIN attribute_value fav_d ON fav_d.card_id = c.card_id and fav_d.attribute_code='JBR_ADDRESS_FORMAT' and fav_d.value_id=2036 "+
            	" JOIN attribute_value d ON d.card_id = c.card_id and d.attribute_code='ADMIN_283926' "+
            	" JOIN attribute_value fav_des ON fav_des.card_id = d.number_value and fav_des.attribute_code='NAME' and  fav_des.string_value='�������� ��������' "+
            	" JOIN attribute_value question ON  question.card_id = c.card_id and question.attribute_code='ADMIN_277251' "+
              	whereString+
            	"\n UNION \n"+
            	" Select  "+
            	" 21 AS cc "+
            	" ,'����������� ��������� � ���������� �����' as name "+
            	" ,'���������� �������� �� ���' as thematic "+
            	" ,count(question.card_id) as hz "+
            	" from card c "+
            	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
            	 " JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
            	" 	extract('year' from reg_date.date_value) ="+ pYear.toString() + " "+
            	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
            	" JOIN attribute_value decision ON decision.card_id=c.card_id and decision.attribute_code='ADMIN_283929' and decision.value_id is not null "+
            	" JOIN attribute_value control_date ON control_date.card_id = c.card_id and control_date.attribute_code='JBR_IMPL_DEADLINE'  and control_date.date_value  is not null "+
            	" JOIN attribute_value answer_date ON answer_date.card_id=c.card_id and answer_date.attribute_code='ADMIN_220912' and answer_date.date_value is not null "+
            	   " and date_trunc('day', answer_date.date_value)>date_trunc('day', control_date.date_value) "+
            	" JOIN attribute_value question ON  question.card_id = c.card_id and question.attribute_code='ADMIN_277251' "+
              	whereString+
            	"\n UNION \n"+
            	" Select  "+
            	" 22 AS cc "+
            	" ,'��������� �� ������������ �� 1 ����� ������ ���������� �� �������� ��������, ����������� � �������� �������' as name "+
            	" ,'���������� �������� �� ���' as thematic "+
            	" ,count(question.card_id) as hz "+
            	" from card c "+
            	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
            	 " JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
            	" 	extract('year' from reg_date.date_value) ="+ pYear.toString() + " "+
            	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
            	" JOIN attribute_value control_date ON control_date.card_id = c.card_id and control_date.attribute_code='JBR_IMPL_DEADLINE' "+
            		" and control_date.date_value is not null  and not exists (select 1 from attribute_value v where v.card_id=c.card_id and v.attribute_code='ADMIN_283929' and v.value_id is not null) "+
            	" 	and not exists (select 1 from attribute_value v1 where v1.card_id=c.card_id and v1.attribute_code='ADMIN_220912' and v1.date_value is not null) "+
            	" JOIN attribute_value question ON  question.card_id = c.card_id and question.attribute_code='ADMIN_277251' "+
              	whereString+
            	"\n UNION \n"+
            	" Select  "+
            	" 24 AS cc "+
            	" ,'          " + config.getProperty(PARAM_IMPROPER, PARAM_NULL) + "' as name "+
            	" ,'���������� �������� �� ���' as thematic "+
            	" ,count(question.card_id) as hz "+
            	" from card c "+
            	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
            	 " JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
            	" 	extract('year' from reg_date.date_value) ="+ pYear.toString() + " "+
            	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
            	" JOIN attribute_value fav_d ON fav_d.card_id = c.card_id and fav_d.attribute_code='JBR_ADDRESS_FORMAT' and fav_d.value_id=2036 "+
            	" JOIN attribute_value fav_des ON  fav_des.card_id = c.card_id and  fav_des.attribute_code='JBR_COMPLAINT_REASON' and  fav_des.value_id=2039 "+
            	" JOIN attribute_value question ON  question.card_id = c.card_id and question.attribute_code='ADMIN_277251' "+
              	whereString+
            	"\n UNION \n"+
            	" Select  "+
            	" 25 AS cc "+
            	" ,'          ���������� � ������ ���������� �� �������������� ��������������� �����' as name "+
            	" ,'���������� �������� �� ���' as thematic "+
            	" ,count(question.card_id) as hz "+
            	" from card c "+
            	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
            	 " JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
            	" 	extract('year' from reg_date.date_value) ="+ pYear.toString() + " "+
            	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
            	" JOIN attribute_value fav_d ON fav_d.card_id = c.card_id and fav_d.attribute_code='JBR_ADDRESS_FORMAT' and fav_d.value_id=2036 "+
            	" JOIN attribute_value fav_des ON  fav_des.card_id = c.card_id and  fav_des.attribute_code='JBR_COMPLAINT_REASON' and  fav_des.value_id=2040 "+
            	" JOIN attribute_value question ON  question.card_id = c.card_id and question.attribute_code='ADMIN_277251' "+
              	whereString+
            	"\n UNION \n"+
            	" Select  "+
            	" 26 AS cc "+
            	" ,'          ��������� ���������������� � ������������ ��������������� ����������, ������������ ����������� � ������� �����������' as name "+
            	" ,'���������� �������� �� ���' as thematic "+
            	" ,count(question.card_id) as hz "+
            	" from card c "+
            	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
            	 " JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
            	" 	extract('year' from reg_date.date_value) ="+ pYear.toString() + " "+
            	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
            	" JOIN attribute_value fav_d ON fav_d.card_id = c.card_id and fav_d.attribute_code='JBR_ADDRESS_FORMAT' and fav_d.value_id=2036 "+
            	" JOIN attribute_value fav_des ON  fav_des.card_id = c.card_id and  fav_des.attribute_code='JBR_COMPLAINT_REASON' and  fav_des.value_id=2041 "+
            	" JOIN attribute_value question ON  question.card_id = c.card_id and question.attribute_code='ADMIN_277251' "+
              	whereString+
            	"\n UNION \n"+
            	" Select  "+
            	" 27 AS cc "+
            	" ,'          " + config.getProperty(PARAM_REJECTION, PARAM_NULL) + "' as name "+
            	" ,'���������� �������� �� ���' as thematic "+
            	" ,count(question.card_id) as hz "+
            	" from card c "+
            	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
            	 " JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
            	" 	extract('year' from reg_date.date_value) ="+ pYear.toString() + " "+
            	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
            	" JOIN attribute_value fav_d ON fav_d.card_id = c.card_id and fav_d.attribute_code='JBR_ADDRESS_FORMAT' and fav_d.value_id=2036 "+
            	" JOIN attribute_value fav_des ON  fav_des.card_id = c.card_id and  fav_des.attribute_code='JBR_COMPLAINT_REASON' and  fav_des.value_id=2042 "+
            	" JOIN attribute_value question ON  question.card_id = c.card_id and question.attribute_code='ADMIN_277251' "+
              	whereString+
            	"\n UNION \n"+
            	" Select  "+
            	" 28 AS cc "+
            	" ,'          ������������� ����������������� � ������������ ���������� �� �������������� ��������������� �����' as name "+
            	" ,'���������� �������� �� ���' as thematic "+
            	" ,count(question.card_id) as hz "+
            	" from card c "+
            	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
            	 " JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
            	" 	extract('year' from reg_date.date_value) ="+ pYear.toString() + " "+
            	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
            	" JOIN attribute_value fav_d ON fav_d.card_id = c.card_id and fav_d.attribute_code='JBR_ADDRESS_FORMAT' and fav_d.value_id=2036 "+
            	" JOIN attribute_value fav_des ON  fav_des.card_id = c.card_id and  fav_des.attribute_code='JBR_COMPLAINT_REASON' and  fav_des.value_id=2043 "+
            	" JOIN attribute_value question ON  question.card_id = c.card_id and question.attribute_code='ADMIN_277251' "+
              	whereString+
            	"\n UNION \n"+
            	" Select  "+
            	" 29 AS cc "+
            	" ,'          ������ �������� ����������� �������' as name "+
            	" ,'���������� �������� �� ���' as thematic "+
            	" ,count(question.card_id) as hz "+
            	" from card c "+
            	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
            	 " JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
            	" 	extract('year' from reg_date.date_value) ="+ pYear.toString() + " "+
            	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
            	" JOIN attribute_value fav_d ON fav_d.card_id = c.card_id and fav_d.attribute_code='JBR_ADDRESS_FORMAT' and fav_d.value_id=2036 "+
            	" JOIN attribute_value fav_des ON  fav_des.card_id = c.card_id and  fav_des.attribute_code='JBR_COMPLAINT_REASON' and  fav_des.value_id=2044 "+
            	" JOIN attribute_value question ON  question.card_id = c.card_id and question.attribute_code='ADMIN_277251' "+
              	whereString+
            	"\n UNION \n"+
            	" Select  "+
            	" 30 AS cc "+
            	" ,'          ������' as name "+
            	" ,'���������� �������� �� ���' as thematic "+
            	" ,count(question.card_id) as hz "+
            	" from card c "+
            	" JOIN template t on t.template_id=864 and t.template_id=c.template_id "+
            	 " JOIN attribute_value reg_date ON reg_date.card_id = c.card_id and reg_date.attribute_code='JBR_REGD_DATEREG' and "+
            	" 	extract('year' from reg_date.date_value) ="+ pYear.toString() + " "+
            	" JOIN attribute_value a ON a.card_id = c.card_id and a.attribute_code='ADMIN_281718' "+
            	" JOIN attribute_value fav_d ON fav_d.card_id = c.card_id and fav_d.attribute_code='JBR_ADDRESS_FORMAT' and fav_d.value_id=2036 "+
            	" JOIN attribute_value fav_des ON  fav_des.card_id = c.card_id and  fav_des.attribute_code='JBR_COMPLAINT_REASON' and  fav_des.value_id=2045 "+
            	" JOIN attribute_value question ON question.card_id = c.card_id and question.attribute_code='ADMIN_277251' "+
              	whereString+
            	"\n UNION \n");
	}
	
}
