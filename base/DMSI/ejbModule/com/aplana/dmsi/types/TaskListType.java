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


import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for TaskListType complex type.
 *
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 *
 * <pre>
 * &lt;complexType name=&quot;TaskListType&quot;&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base=&quot;{http://www.w3.org/2001/XMLSchema}anyType&quot;&gt;
 *       &lt;sequence&gt;
 *         &lt;element name=&quot;Task&quot; maxOccurs=&quot;unbounded&quot;&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base=&quot;{http://www.w3.org/2001/XMLSchema}anyType&quot;&gt;
 *                 &lt;sequence&gt;
 *                   &lt;element ref=&quot;{}TaskNumber&quot;/&gt;
 *                   &lt;element ref=&quot;{}Confident&quot;/&gt;
 *                   &lt;element ref=&quot;{}Referred&quot; maxOccurs=&quot;unbounded&quot;/&gt;
 *                   &lt;element ref=&quot;{}AuthorOrganization&quot; maxOccurs=&quot;unbounded&quot;/&gt;
 *                   &lt;element ref=&quot;{}DocTransfer&quot; maxOccurs=&quot;unbounded&quot; minOccurs=&quot;0&quot;/&gt;
 *                   &lt;element ref=&quot;{}Executor&quot; maxOccurs=&quot;unbounded&quot; minOccurs=&quot;0&quot;/&gt;
 *                 &lt;/sequence&gt;
 *                 &lt;attribute name=&quot;idnumber&quot; use=&quot;required&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}string&quot; /&gt;
 *                 &lt;attribute name=&quot;task_reg&quot; use=&quot;required&quot; type=&quot;{}TaskRegistrationEnumType&quot; /&gt;
 *                 &lt;attribute name=&quot;task_copy&quot; use=&quot;required&quot; type=&quot;{}TaskCopyEnumType&quot; /&gt;
 *                 &lt;attribute name=&quot;kind&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}string&quot; /&gt;
 *                 &lt;attribute name=&quot;task_text&quot; use=&quot;required&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}string&quot; /&gt;
 *                 &lt;attribute name=&quot;deadline&quot; use=&quot;required&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}date&quot; /&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TaskListType", propOrder = { "task" })
public class TaskListType {

    @XmlElement(name = "Task", required = true)
    protected List<Task> task;

    /**
     * Gets the value of the task property.
     *
     * <p>
     * This accessor method returns a reference to the live list, not a
     * snapshot. Therefore any modification you make to the returned list will
     * be present inside the JAXB object. This is why there is not a
     * <CODE>set</CODE> method for the task property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     *
     * <pre>
     * getTask().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Task }
     *
     *
     */
    public List<Task> getTask() {
	if (task == null) {
	    task = new ArrayList<Task>();
	}
	return this.task;
    }

    @Override
    public String toString() {
	List<Task> tasks = getTask();
	StringBuilder tasksBuilder = new StringBuilder();
	for (Task simpleTask : tasks) {
	    if (tasksBuilder.length() > 0) {
		tasksBuilder.append("\n\n");
	    }
	    tasksBuilder.append(simpleTask);
	}
	return tasksBuilder.toString();
    }

}
