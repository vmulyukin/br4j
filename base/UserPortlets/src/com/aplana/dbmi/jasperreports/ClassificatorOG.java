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

/**
 * @author ppolushkin
 *
 */
public class ClassificatorOG {
	
	private String code = null;
	private Long sectionId = null;
	private String sectionName = null;
	private Long subjectId = null;
	private String subjectName = null;
	private Long themeId = null;
	private String themeName = null;
	private Long questionId = null;
	private String questionName = null;
	private Long subquestionId = null;
	private String subquestionName = null;
	private Long kolvo = null;
	
	public ClassificatorOG(){}
	
	// ���
	
	public void setCode(String code){
		this.code = code;
	}
	
	public String getCode(){
		return this.code;
	}
	
	// ������
	
	public void setSectionId(Long sectionId){
		this.sectionId = sectionId;
	}
	
	public Long getSectionId(){
		return this.sectionId;
	}
	
	public void setSectionName(String sectionName){
		this.sectionName = sectionName;
	}
	
	public String getSectionName(){
		return this.sectionName;
	}
	
	// ��������
	
	public void setSubjectId(Long subjectId){
		this.subjectId = subjectId;
	}
	
	public Long getSubjectId(){
		return this.subjectId;
	}
	
	public void setSubjectName(String subjectName){
		this.subjectName = subjectName;
	}
	
	public String getSubjectName(){
		return this.subjectName;
	}
	
	// ����
	
	public void setThemeId(Long themeId){
		this.themeId = themeId;
	}
	
	public Long getThemeId(){
		return this.themeId;
	}
	
	public void setThemeName(String themeName){
		this.themeName = themeName;
	}
	
	public String getThemeName(){
		return this.themeName;
	}
	
	// ������
	
	public void setQuestionId(Long questionId){
		this.questionId = questionId;
	}
	
	public Long getQuestionId(){
		return this.questionId;
	}
	
	public void setQuestionName(String questionName){
		this.questionName = questionName;
	}
	
	public String getQuestionName(){
		return this.questionName;
	}
	
	// ���������
	
	public Long getSubquestionId() {
		return subquestionId;
	}

	public void setSubquestionId(Long subquestionId) {
		this.subquestionId = subquestionId;
	}

	public String getSubquestionName() {
		return subquestionName;
	}

	public void setSubquestionName(String subquestionName) {
		this.subquestionName = subquestionName;
	}
	
	// ����������
	
	public void setKolvo(Long kolvo){
		this.kolvo = kolvo;
	}
	
	public Long getKolvo(){
		return this.kolvo;
	}

}
