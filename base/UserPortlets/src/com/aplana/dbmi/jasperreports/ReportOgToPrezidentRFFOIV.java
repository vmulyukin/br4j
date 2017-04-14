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

public class ReportOgToPrezidentRFFOIV implements Comparable {
	
	private String name;
	private Boolean empty;
	private Long kolobr;
	private Long kolvopr;
	private Long konststroy;
	private Long osngosupr;
	private Long mo;
	private Long gp;
	private Long indpravakt;
	private Long semia;
	private Long trudizan;
	private Long socobespech;
	private Long obraz;
	private Long zdravohran;
	private Long finansi;
	private Long hosdeyat;
	private Long vnekonodeyat;
	private Long prirodres;
	private Long informacia;
	private Long oborona;
	private Long bezopasnost;
	private Long up;
	private Long pravosud;
	private Long prokur;
	private Long zhilzakon;
	private Long zhilfond;
	private Long nezhilfond;
	private Long pravonazhil;;
	private Long komuslugi;
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Boolean getEmpty() {
		return empty;
	}

	public void setEmpty(Boolean empty) {
		this.empty = empty;
	}

	public Long getKolobr() {
		return kolobr;
	}

	public void setKolobr(Long kolobr) {
		this.kolobr = kolobr;
	}

	public Long getKolvopr() {
		return kolvopr;
	}

	public void setKolvopr(Long kolvopr) {
		this.kolvopr = kolvopr;
	}

	public Long getKonststroy() {
		return konststroy;
	}

	public void setKonststroy(Long konststroy) {
		this.konststroy = konststroy;
	}

	public Long getOsngosupr() {
		return osngosupr;
	}

	public void setOsngosupr(Long osngosupr) {
		this.osngosupr = osngosupr;
	}

	public Long getMo() {
		return mo;
	}

	public void setMo(Long mo) {
		this.mo = mo;
	}

	public Long getGp() {
		return gp;
	}

	public void setGp(Long gp) {
		this.gp = gp;
	}

	public Long getIndpravakt() {
		return indpravakt;
	}

	public void setIndpravakt(Long indpravakt) {
		this.indpravakt = indpravakt;
	}

	public Long getSemia() {
		return semia;
	}

	public void setSemia(Long semia) {
		this.semia = semia;
	}

	public Long getTrudizan() {
		return trudizan;
	}

	public void setTrudizan(Long trudizan) {
		this.trudizan = trudizan;
	}

	public Long getSocobespech() {
		return socobespech;
	}

	public void setSocobespech(Long socobespech) {
		this.socobespech = socobespech;
	}

	public Long getObraz() {
		return obraz;
	}

	public void setObraz(Long obraz) {
		this.obraz = obraz;
	}

	public Long getZdravohran() {
		return zdravohran;
	}

	public void setZdravohran(Long zdravohran) {
		this.zdravohran = zdravohran;
	}

	public Long getFinansi() {
		return finansi;
	}

	public void setFinansi(Long finansi) {
		this.finansi = finansi;
	}

	public Long getHosdeyat() {
		return hosdeyat;
	}

	public void setHosdeyat(Long hosdeyat) {
		this.hosdeyat = hosdeyat;
	}

	public Long getVnekonodeyat() {
		return vnekonodeyat;
	}

	public void setVnekonodeyat(Long vnekonodeyat) {
		this.vnekonodeyat = vnekonodeyat;
	}

	public Long getPrirodres() {
		return prirodres;
	}

	public void setPrirodres(Long prirodres) {
		this.prirodres = prirodres;
	}

	public Long getInformacia() {
		return informacia;
	}

	public void setInformacia(Long informacia) {
		this.informacia = informacia;
	}

	public Long getOborona() {
		return oborona;
	}

	public void setOborona(Long oborona) {
		this.oborona = oborona;
	}

	public Long getBezopasnost() {
		return bezopasnost;
	}

	public void setBezopasnost(Long bezopasnost) {
		this.bezopasnost = bezopasnost;
	}

	public Long getUp() {
		return up;
	}

	public void setUp(Long up) {
		this.up = up;
	}

	public Long getPravosud() {
		return pravosud;
	}

	public void setPravosud(Long pravosud) {
		this.pravosud = pravosud;
	}

	public Long getProkur() {
		return prokur;
	}

	public void setProkur(Long prokur) {
		this.prokur = prokur;
	}

	public Long getZhilzakon() {
		return zhilzakon;
	}

	public void setZhilzakon(Long zhilzakon) {
		this.zhilzakon = zhilzakon;
	}

	public Long getZhilfond() {
		return zhilfond;
	}

	public void setZhilfond(Long zhilfond) {
		this.zhilfond = zhilfond;
	}

	public Long getNezhilfond() {
		return nezhilfond;
	}

	public void setNezhilfond(Long nezhilfond) {
		this.nezhilfond = nezhilfond;
	}

	public Long getPravonazhil() {
		return pravonazhil;
	}

	public void setPravonazhil(Long pravonazhil) {
		this.pravonazhil = pravonazhil;
	}

	public Long getKomuslugi() {
		return komuslugi;
	}

	public void setKomuslugi(Long komuslugi) {
		this.komuslugi = komuslugi;
	}

	public Long getKolobrGod() {
		return kolobrGod;
	}

	public void setKolobrGod(Long kolobrGod) {
		this.kolobrGod = kolobrGod;
	}

	public Long getKolvoprGod() {
		return kolvoprGod;
	}

	public void setKolvoprGod(Long kolvoprGod) {
		this.kolvoprGod = kolvoprGod;
	}

	private Long kolobrGod;
	private Long kolvoprGod;

	public int compareTo(Object o) {
		// TODO Auto-generated method stub
		return 0;
	}

}
