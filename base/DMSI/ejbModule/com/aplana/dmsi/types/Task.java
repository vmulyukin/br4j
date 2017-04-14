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
package com.aplana.dmsi.types;

import static org.apache.commons.lang.StringUtils.defaultString;
import static org.apache.commons.lang.StringUtils.isBlank;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.datatype.XMLGregorianCalendar;

import com.aplana.dmsi.ObjectFactory;
import com.aplana.dmsi.types.adapters.DocTransferAdapter;

/**
 * <p>
 * Java class for anonymous complex type.
 *
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 *
 * <pre>
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base=&quot;{http://www.w3.org/2001/XMLSchema}anyType&quot;&gt;
 *       &lt;sequence&gt;
 *         &lt;element ref=&quot;{}TaskNumber&quot;/&gt;
 *         &lt;element ref=&quot;{}Confident&quot;/&gt;
 *         &lt;element ref=&quot;{}Referred&quot; maxOccurs=&quot;unbounded&quot;/&gt;
 *         &lt;element ref=&quot;{}AuthorOrganization&quot; maxOccurs=&quot;unbounded&quot;/&gt;
 *         &lt;element ref=&quot;{}DocTransfer&quot; maxOccurs=&quot;unbounded&quot; minOccurs=&quot;0&quot;/&gt;
 *         &lt;element ref=&quot;{}Executor&quot; maxOccurs=&quot;unbounded&quot; minOccurs=&quot;0&quot;/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name=&quot;idnumber&quot; use=&quot;required&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}string&quot; /&gt;
 *       &lt;attribute name=&quot;task_reg&quot; use=&quot;required&quot; type=&quot;{}TaskRegistrationEnumType&quot; /&gt;
 *       &lt;attribute name=&quot;task_copy&quot; use=&quot;required&quot; type=&quot;{}TaskCopyEnumType&quot; /&gt;
 *       &lt;attribute name=&quot;kind&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}string&quot; /&gt;
 *       &lt;attribute name=&quot;task_text&quot; use=&quot;required&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}string&quot; /&gt;
 *       &lt;attribute name=&quot;deadline&quot; use=&quot;required&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}date&quot; /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.PROPERTY)
@XmlType(name = "", propOrder = { "taskNumber", "confident", "referred", "authorOrganization", "docTransfer",
		"executor" }, factoryClass = ObjectFactory.class, factoryMethod = "createTask")
public class Task extends DMSIObject {

	protected TaskNumber taskNumber;
	protected Confident confident = new Confident();
	protected List<Referred> referred;
	protected List<AuthorOrganization> authorOrganization;
	protected List<DocTransfer> docTransfer;
	protected List<Executor> mainExecutor;
	protected List<Executor> executor;
	protected String idnumber;
	protected byte taskReg = 0;
	protected byte taskCopy = 0;
	protected TaskKind kind;
	protected String taskText;
	protected XMLGregorianCalendar deadline;
	protected String authorsDescription;
	protected String executorsDescription;
	protected String coExecutorsDescription;

	/**
	 * Gets the value of the taskNumber property.
	 *
	 * @return possible object is {@link TaskNumber }
	 *
	 */
	@XmlElement(name = "TaskNumber", required = true)
	public TaskNumber getTaskNumber() {
		return taskNumber;
	}

	/**
	 * Sets the value of the taskNumber property.
	 *
	 * @param value
	 *            allowed object is {@link TaskNumber }
	 *
	 */
	public void setTaskNumber(TaskNumber value) {
		this.taskNumber = value;
	}

	/**
	 * Gets the value of the confident property.
	 *
	 * @return possible object is {@link Confident }
	 *
	 */
	@XmlElement(name = "Confident", required = true)
	public Confident getConfident() {
		return confident;
	}

	/**
	 * Sets the value of the confident property.
	 *
	 * @param value
	 *            allowed object is {@link Confident }
	 *
	 */
	public void setConfident(Confident value) {
		this.confident = value;
	}

	/**
	 * Gets the value of the referred property.
	 *
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the referred property.
	 *
	 * <p>
	 * For example, to add a new item, do as follows:
	 *
	 * <pre>
	 * getReferred().add(newItem);
	 * </pre>
	 *
	 *
	 * <p>
	 * Objects of the following type(s) are allowed in the list {@link Referred }
	 *
	 *
	 */
	@XmlElement(name = "Referred", required = true)
	public List<Referred> getReferred() {
		if (referred == null) {
			referred = new ArrayList<Referred>();
		}
		return this.referred;
	}

	/**
	 * Gets the value of the authorOrganization property.
	 *
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the authorOrganization property.
	 *
	 * <p>
	 * For example, to add a new item, do as follows:
	 *
	 * <pre>
	 * getAuthorOrganization().add(newItem);
	 * </pre>
	 *
	 *
	 * <p>
	 * Objects of the following type(s) are allowed in the list
	 * {@link AuthorOrganization }
	 *
	 *
	 */
	@XmlElement(name = "AuthorOrganization", required = true)
	public List<AuthorOrganization> getAuthorOrganization() {
		if (authorOrganization == null) {
			authorOrganization = new ArrayList<AuthorOrganization>();
		}
		return this.authorOrganization;
	}

	/**
	 * Gets the value of the docTransfer property.
	 *
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the docTransfer property.
	 *
	 * <p>
	 * For example, to add a new item, do as follows:
	 *
	 * <pre>
	 * getDocTransfer().add(newItem);
	 * </pre>
	 *
	 *
	 * <p>
	 * Objects of the following type(s) are allowed in the list
	 * {@link DocTransfer }
	 *
	 *
	 */
	@XmlElement(name = "DocTransfer")
	@XmlJavaTypeAdapter(DocTransferAdapter.class)
	public List<DocTransfer> getDocTransfer() {
		if (docTransfer == null) {
			docTransfer = new ArrayList<DocTransfer>();
		}
		return this.docTransfer;
	}

	/**
	 * Gets the value of the executor property.
	 *
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the executor property.
	 *
	 * <p>
	 * For example, to add a new item, do as follows:
	 *
	 * <pre>
	 * getExecutor().add(newItem);
	 * </pre>
	 *
	 *
	 * <p>
	 * Objects of the following type(s) are allowed in the list {@link Executor }
	 *
	 *
	 */
	@XmlElement(name = "Executor")
	public List<Executor> getExecutor() {
		if (executor == null) {
			executor = new ArrayList<Executor>();
		}
		return this.executor;
	}

	@XmlTransient
	public List<Executor> getMainExecutor() {
		if (mainExecutor == null) {
			mainExecutor = new ArrayList<Executor>();
		}
		return this.mainExecutor;
	}

	/**
	 * Gets the value of the idnumber property.
	 *
	 * @return possible object is {@link String }
	 *
	 */
	@XmlAttribute(required = true)
	public String getIdnumber() {
		return idnumber;
	}

	/**
	 * Sets the value of the idnumber property.
	 *
	 * @param value
	 *            allowed object is {@link String }
	 *
	 */
	public void setIdnumber(String value) {
		this.idnumber = value;
	}

	/**
	 * Gets the value of the taskReg property.
	 *
	 */
	@XmlAttribute(name = "task_reg", required = true)
	public byte getTaskReg() {
		return taskReg;
	}

	/**
	 * Sets the value of the taskReg property.
	 *
	 */
	public void setTaskReg(byte value) {
		this.taskReg = value;
	}

	/**
	 * Gets the value of the taskCopy property.
	 *
	 */
	@XmlAttribute(name = "task_copy", required = true)
	public byte getTaskCopy() {
		return taskCopy;
	}

	/**
	 * Sets the value of the taskCopy property.
	 *
	 */
	public void setTaskCopy(byte value) {
		this.taskCopy = value;
	}

	/**
	 * Gets the value of the kind property.
	 *
	 * @return possible object is {@link String }
	 *
	 */
	@XmlAttribute
	public TaskKind getKind() {
		return kind;
	}

	/**
	 * Sets the value of the kind property.
	 *
	 * @param value
	 *            allowed object is {@link String }
	 *
	 */
	public void setKind(TaskKind value) {
		this.kind = value;
	}

	/**
	 * Gets the value of the taskText property.
	 *
	 * @return possible object is {@link String }
	 *
	 */
	@XmlAttribute(name = "task_text", required = true)
	public String getTaskText() {
		return taskText;
	}

	/**
	 * Sets the value of the taskText property.
	 *
	 * @param value
	 *            allowed object is {@link String }
	 *
	 */
	public void setTaskText(String value) {
		this.taskText = value;
	}

	/**
	 * Gets the value of the deadline property.
	 *
	 * @return possible object is {@link XMLGregorianCalendar }
	 *
	 */
	@XmlAttribute(required = true)
	@XmlSchemaType(name = "date")
	public XMLGregorianCalendar getDeadline() {
		return deadline;
	}

	/**
	 * Sets the value of the deadline property.
	 *
	 * @param value
	 *            allowed object is {@link XMLGregorianCalendar }
	 *
	 */
	public void setDeadline(XMLGregorianCalendar value) {
		this.deadline = value;
	}

	@Override
	public String toString() {
		DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
		String executorDescr = getExecutorsDescription(ExecutorEnumType.RESPONSIBLE, ExecutorEnumType.COEXECUTOR);
		String allAuthorsBuilder = getAuthorsDescr();
		String taskDate = getTaskNumber() == null || getTaskNumber().getTaskDate() == null ? "" : dateFormat
				.format(getTaskNumber().getTaskDate().toGregorianCalendar().getTime());
		String plannedDate = getDeadline() == null ? "" : dateFormat.format(getDeadline().toGregorianCalendar()
				.getTime());
		return String.format("����� ���������:\n" + //
				"%s\n" + //
				"���������:\n" + //
				"%s\n" + //
				"�� %s, ���� (�������� ����): %s\n" + //
				"�����������:\n" + //
				"%s", allAuthorsBuilder, defaultString(getTaskText()), taskDate, plannedDate, executorDescr);
	}

	public void fillExecutorsDescription() {
		this.executorsDescription = getExecutorsDescription(ExecutorEnumType.RESPONSIBLE);
	}

	public void fillCoExecutorsDescription() {
		this.coExecutorsDescription = getExecutorsDescription(ExecutorEnumType.COEXECUTOR);
	}

	private String getExecutorsDescription(ExecutorEnumType... filter) {

		StringBuilder executorBuilder = new StringBuilder();
		for (Executor taskExecutor : getExecutor()) {
			if (!Arrays.asList(filter).contains(taskExecutor.getResponsible())) {
				continue;
			}

			Organization executorOrganization = taskExecutor.getOrganization();
			String orgName = defaultString(isBlank(executorOrganization.getShortname()) ? executorOrganization
					.getFullname() : executorOrganization.getShortname());
			if (executorBuilder.length() > 0) {
				executorBuilder.append("; ");
			}
			String executorsString = convertOfficialsToString(executorOrganization);
			executorBuilder.append(orgName + ": " + executorsString);
		}
		return executorBuilder.toString();
	}

	public void fillAuthorsDescription() {
		this.authorsDescription = getAuthorsDescr();
	}

	private String getAuthorsDescr() {
		StringBuilder allAuthorsBuilder = new StringBuilder();
		for (AuthorOrganization author : getAuthorOrganization()) {
			if (allAuthorsBuilder.length() > 0) {
				allAuthorsBuilder.append("; ");
			}
			OrganizationWithSign authorOrg = author.getOrganizationWithSign();
			String orgName = defaultString(isBlank(authorOrg.getShortname()) ? authorOrg.getFullname() : authorOrg
					.getShortname());
			String authorsString = convertOfficialsToString(authorOrg);
			allAuthorsBuilder.append(orgName + ": " + authorsString);
		}
		return allAuthorsBuilder.toString();
	}

	private String convertOfficialsToString(Organization org) {
		StringBuilder executorBuilder = new StringBuilder();
		List<OfficialPerson> officialPersons = org.getOfficialPerson();
		for (OfficialPerson officialPerson : officialPersons) {
			if (executorBuilder.length() > 0) {
				executorBuilder.append(", ");
			}
			executorBuilder.append(getName(officialPerson.getName()));
		}
		return executorBuilder.toString();
	}

	private String convertOfficialsToString(OrganizationWithSign org) {
		List<OfficialPersonWithSign> officialPersons = org.getOfficialPersonWithSign();
		StringBuilder authorsBuilder = new StringBuilder();
		for (OfficialPersonWithSign officialPerson : officialPersons) {
			if (authorsBuilder.length() > 0) {
				authorsBuilder.append(", ");
			}
			authorsBuilder.append(getName(officialPerson.getName()));
		}
		return authorsBuilder.toString();
	}

	private String getName(Name name) {
		String fullDescription = name.getValue();
		if (fullDescription == null || "".equals(fullDescription)) {
			fullDescription = name.getFirstname() + " " + name.getFathersname() + " " + name.getSecname();
		}
		return fullDescription;
	}

}