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

import java.math.BigInteger;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "WSO_DOCUMENTCOMMONPROPERTIES",
	propOrder = {
	"template",
	"pagesCount",
	"comments",
	"docType",
	"attachments",
	"links",
	"onDocumentLinks",
	"recipients",
	"executor",
	"signatory",
	"sender",
	"signext"
	})
public class WSODocumentCommonProperties {

	@XmlElement(name = "TEMPLATE", required = true, nillable = true)
    protected String template;
    @XmlElement(name = "PAGESCOUNT", required = true, nillable = true)
    BigInteger pagesCount;
    @XmlElement(name = "COMMENTS", required = true, nillable = true)
    String comments;
    @XmlElement(name = "DOCTYPE", required = true, nillable = true)
    String docType;
    @XmlElement(name = "ATTACHMENTS", required = true, nillable = true)
    WSOCollection attachments;
    @XmlElement(name = "LINKS", required = true, nillable = true)
    WSOCollection links;
    @XmlElement(name = "ONDOCUMENTLINKS", required = true, nillable = true)
    WSOCollection onDocumentLinks;
    @XmlElement(name = "RECIPIENTS", required = true, nillable = true)
    WSOCollection recipients;
    @XmlElement(name = "EXECUTOR", required = true, nillable = true)
    WSOCollection executor;
    @XmlElement(name = "SIGNATORY", required = true, nillable = true)
    WSOCollection signatory;
    @XmlElement(name = "SENDER", required = true, nillable = true)
    WSOCollection sender;
    @XmlElement(name = "SIGNEXT", required = true, nillable = true)
    WSOCollection signext;

    
    public String getTemplate() {
		return template;
	}

	public void setTemplate(String template) {
		this.template = template;
	}

    public BigInteger getPagesCount() {
        return this.pagesCount;
    }

    public void setPagesCount(BigInteger pagesCount) {
        this.pagesCount = pagesCount;
    }

    public String getComments() {
        return this.comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getDocType() {
        return this.docType;
    }

    public void setDocType(String docType) {
        this.docType = docType;
    }

    public WSOCollection getAttachments() {
        return this.attachments;
    }

    public void setAttachments(WSOCollection attachments) {
        this.attachments = attachments;
    }

    public WSOCollection getLinks() {
        return this.links;
    }

    public void setLinks(WSOCollection links) {
        this.links = links;
    }
    
    public WSOCollection getOnDocumentLinks() {
		return onDocumentLinks;
	}

	public void setOnDocumentLinks(WSOCollection onDocumentLinks) {
		this.onDocumentLinks = onDocumentLinks;
	}

	public WSOCollection getRecipients() {
        return this.recipients;
    }

    public void setRecipients(WSOCollection recipients) {
        this.recipients = recipients;
    }

	public WSOCollection getExecutor() {
		return executor;
	}

	public void setExecutor(WSOCollection executor) {
		this.executor = executor;
	}

	public WSOCollection getSignatory() {
		return signatory;
	}

	public void setSignatory(WSOCollection signatory) {
		this.signatory = signatory;
	}

	public WSOCollection getSender() {
		return sender;
	}

	public void setSender(WSOCollection sender) {
		this.sender = sender;
	}

	public WSOCollection getSignext() {
		return signext;
	}

	public void setSignext(WSOCollection signext) {
		this.signext = signext;
	}
    
}
