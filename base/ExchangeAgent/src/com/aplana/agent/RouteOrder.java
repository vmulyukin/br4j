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
package com.aplana.agent;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.aplana.agent.conf.DocumentBodyReader.DocType;
import com.aplana.agent.conf.routetable.Node;

public class RouteOrder {
	private URL letter;
	private File envelopeFile;
	private List<Node> nodes = new ArrayList<Node>();
	private DocType letterType;

	public RouteOrder(URL letter, File envelopeFile, DocType letterType, List<Node> nodes) {
		this.letter = letter;
		this.envelopeFile = envelopeFile;
		this.nodes = nodes;
		this.letterType = letterType;
	}

	public URL getLetter() {
		return letter;
	}

	public List<Node> getNodes() {
		return nodes;
	}

	public void addNode(Node node) {
		nodes.add(node);
	}

	public File getEnvelopeFile() {
		return envelopeFile;
	}

	public DocType getLetterType() {
		return letterType;
	}
}
