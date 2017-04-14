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
package com.aplana.medo.types;

import javax.xml.bind.annotation.XmlRegistry;

@XmlRegistry
public class ObjectFactory {

    public ObjectFactory() {
    }

    public Author createAuthor() {
	return new Author();
    }

    public DirectionInfo createDirectionInfo() {
	return new DirectionInfo();
    }

    public Event createEvent() {
	return new Event();
    }

    public Image createImage() {
	return new Image();
    }

    public Info createInfo() {
	return new Info();
    }

    public Notify createNotify() {
	return new Notify();
    }

    public XMLReplyDepart createReplyDepart() {
	return new XMLReplyDepart();
    }

    public Result createResult() {
	return new Result();
    }

    public Rubric createRubric() {
	return new Rubric();
    }

    public Vid createVid() {
	return new Vid();
    }

    public XMLDirectionInfo createXMLDirectionInfo() {
	return new XMLDirectionInfo();
    }

    public XMLImage createXMLImage() {
	return new XMLImage();
    }

    public XMLPacket createXMLPacket() {
	return new XMLPacket();
    }

    public XMLReplyDepart createXMLReplyDepart() {
	return new XMLReplyDepart();
    }
}
