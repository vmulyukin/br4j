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
/*
    Copyright 2004 Jenkov Development

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/



package com.jenkov.prizetags.navigator.impl;

import com.jenkov.prizetags.navigator.itf.ILink;

/**
 * @author Jakob Jenkov - Copyright 2005 Jenkov Development
 */
public class Link implements ILink {

    protected String id   = null;
    protected String text = null;
    protected String url  = null;

    public Link() {
    }

    public Link(String id) {
        this.id = id;
    }

    public Link(String id, String text) {
        this.id = id;
        this.text = text;
    }

    public Link(String id, String text, String url) {
        this.id = id;
        this.text = text;
        this.url = url;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int hashCode() {
        if(this.id != null) return this.id.hashCode();
        return 0;
    }

    public boolean equals(Object obj) {
        if(obj == null) return false;
        if(!(obj instanceof ILink)) return false;

        ILink otherLink = (ILink) obj;
        if(this.id == null && otherLink.getId() == null) return true;
        if(this.id != null && otherLink.getId() != null) return getId().equals(otherLink.getId());
        return false;
    }

    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("id = ").append(getId());
        buffer.append(", text = ").append(getText());
        buffer.append(", url = ").append(getUrl());
        return buffer.toString();
    }
}
