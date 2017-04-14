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
package com.aplana.ireferent.config;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import com.aplana.dbmi.model.ObjectId;
import com.aplana.ireferent.WSObjectFactory;
import com.aplana.ireferent.types.WSObject;

public final class Type {
    private String name;
    private Class<? extends WSObjectFactory> factory;
    private Map<ObjectId, Entity> entitiesByTemplate = new HashMap<ObjectId, Entity>();

    protected static class Entity {
	private ObjectId templateId;
	private Class<? extends WSObject> type;
	private Class<? extends WSObject> mType;

	public ObjectId getTemplateId() {
	    return this.templateId;
	}

	protected void setTemplateId(ObjectId templateId) {
	    this.templateId = templateId;
	}

	public Class<? extends WSObject> getType() {
	    return this.type;
	}

	protected void setType(Class<? extends WSObject> type) {
	    this.type = type;
	}

	public Class<? extends WSObject> getMType() {
	    return this.mType;
	}

	protected void setMType(Class<? extends WSObject> type) {
	    this.mType = type;
	}
    }

    protected String getName() {
	return this.name;
    }

    protected void setName(String name) {
	this.name = name;
    }

    public Class<? extends WSObjectFactory> getFactory() {
	return this.factory;
    }

    protected void setFactory(Class<? extends WSObjectFactory> factory) {
	this.factory = factory;
    }

    protected void addEntity(Entity entity) {
	entitiesByTemplate.put(entity.getTemplateId(), entity);
    }

    public Class<? extends WSObject> getTypeByTemplate(ObjectId templateId) {
	return getEntityType(getEntityByTemplate(templateId));
    }

    public Class<? extends WSObject> getMetaTypeByTemplate(ObjectId templateId) {
	return getEntityMetaType(getEntityByTemplate(templateId));
    }

    private Entity getEntityByTemplate(ObjectId templateId) {
	if (!entitiesByTemplate.containsKey(templateId))
	    throw new IllegalArgumentException(String.format(
		    "Type %s does not support cards of %s template", name,
		    templateId));
	return entitiesByTemplate.get(templateId);
    }

    public Collection<ObjectId> getTypeRequiredAttributes() {
	return getRequiredAttributes(false);
    }

    public Collection<ObjectId> getMetaTypeRequiredAttributes() {
	return getRequiredAttributes(true);
    }

    private Collection<ObjectId> getRequiredAttributes(boolean isMObject) {
	Collection<ObjectId> requiredAttributes = new HashSet<ObjectId>();
	for (Entity entity : entitiesByTemplate.values()) {
	    Class<? extends WSObject> clazz = isMObject ? getEntityMetaType(entity)
		    : getEntityType(entity);
	    ClassConfig config = ClassConfigManager.instance()
		    .getConfigByClass(clazz);
	    requiredAttributes.addAll(config.getRequiredAttributes());
	}
	return requiredAttributes;
    }

    private Class<? extends WSObject> getEntityType(Entity entity) {
	return entity.getType();
    }

    private Class<? extends WSObject> getEntityMetaType(Entity entity) {
	return entity.getMType() == null ? entity.getType() : entity.getMType();
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
	return name;
    }
}
