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
package com.aplana.ireferent.types;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for WSO_COLLECTION complex type.
 *
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 *
 * <pre>
 * &lt;complexType name=&quot;WSO_COLLECTION&quot;&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base=&quot;{http://www.w3.org/2001/XMLSchema}anyType&quot;&gt;
 *       &lt;sequence&gt;
 *         &lt;element name=&quot;COUNT&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}integer&quot;/&gt;
 *         &lt;element name=&quot;DATA&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}anyType&quot; maxOccurs=&quot;unbounded&quot; minOccurs=&quot;0&quot;/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 */
@XmlSeeAlso( { WSOLink.class, WSOIncomeDocument.class,
	WSOInternalDocument.class, WSOOutcomeDocument.class,
	WSOOrdDocument.class, WSOOgDocument.class, WSONpaDocument.class, WSOPerson.class, WSOExternalPerson.class,
	WSOGroup.class, WSOFile.class, WSOURLFile.class, WSOTask.class, WSOTaskReport.class, WSOTaskReportForMerge.class, WSOTaskWithReports.class,
	WSOTypeResolution.class, WSOItem.class, WSOApproval.class,
	WSOApprovalReview.class, WSOGroup.class, Favorites.class, WSONp.class})
@XmlAccessorType(XmlAccessType.PROPERTY)
@XmlType(name = "WSO_COLLECTION", propOrder = { "count", "data"})
public class WSOCollection {

    protected Integer count;

    protected List<Object> data;

	@XmlTransient
    protected String userId;
    /**
     * Gets the value of the count property.
     *
     * @return possible object is {@link Integer }
     *
     */

    @XmlElement(name = "COUNT", required = true)
    public Integer getCount() {
	return data == null ? 0 : data.size();
    }

    /**
     * Gets the value of the data property.
     *
     * <p>
     * This accessor method returns a reference to the live list, not a
     * snapshot. Therefore any modification you make to the returned list will
     * be present inside the JAXB object. This is why there is not a
     * <CODE>set</CODE> method for the data property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     *
     * <pre>
     * getDATA().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list {@link Object }
     *
     */
    @XmlElementWrapper(name = "DATA", nillable = true)
    @XmlElement(name = "item", nillable = true)
    public List<Object> getData() {
	if (data == null) {
	    data = new ArrayList<Object>();
	}
	return this.data;
    }
    
    @XmlTransient
    public String getUserId() {
    	return this.userId;
    }
    public void setUserId(String ownerId) {
    	this.userId = ownerId;
    }
}
