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
package com.aplana.dbmi.archive;

import java.util.Map;
import java.util.Set;

import com.aplana.dbmi.model.ObjectId;

/**
 * ����������� ������������ ������ (����� �������� ��� ������ ������� ���������� � ����� ��� �������� � ����)
 * @author ppolushkin
 * @since 19.12.2014
 */
public class ArchiveConfig {
	
	private Long template;
	private Set<ObjectId> attributes;
	private Map<Long, Set<ObjectId>> children;
	
	public Long getTemplate() {
		return template;
	}
	
	public void setTemplate(Long template) {
		this.template = template;
	}
	
	public Set<ObjectId> getAttributes() {
		return attributes;
	}
	
	public void setAttributes(Set<ObjectId> attributes) {
		this.attributes = attributes;
	}
	
	public Map<Long, Set<ObjectId>> getChildren() {
		return children;
	}
	
	public void setChildren(Map<Long, Set<ObjectId>> children) {
		this.children = children;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((attributes == null) ? 0 : attributes.hashCode());
		result = prime * result
				+ ((children == null) ? 0 : children.hashCode());
		result = prime * result
				+ ((template == null) ? 0 : template.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ArchiveConfig other = (ArchiveConfig) obj;
		if (attributes == null) {
			if (other.attributes != null)
				return false;
		} else if (!attributes.equals(other.attributes))
			return false;
		if (children == null) {
			if (other.children != null)
				return false;
		} else if (!children.equals(other.children))
			return false;
		if (template == null) {
			if (other.template != null)
				return false;
		} else if (!template.equals(other.template))
			return false;
		return true;
	}

}
