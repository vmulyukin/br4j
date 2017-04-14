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
/**
 * Application.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.aplana.owriter.ulteo.rpcclient;

public class Application  implements java.io.Serializable {
    private java.lang.String id;

    private java.lang.String name;

    private java.lang.String description;

    private java.lang.String type;

    private java.lang.String executable_path;

    private java.lang.String _package;

    private boolean published;

    private java.lang.String desktopfile;

    private com.aplana.owriter.ulteo.rpcclient.Any servers;

    private com.aplana.owriter.ulteo.rpcclient.Any groups;

    private java.lang.String[] mimetypes;

    public Application() {
    }

    public Application(
           java.lang.String id,
           java.lang.String name,
           java.lang.String description,
           java.lang.String type,
           java.lang.String executable_path,
           java.lang.String _package,
           boolean published,
           java.lang.String desktopfile,
           com.aplana.owriter.ulteo.rpcclient.Any servers,
           com.aplana.owriter.ulteo.rpcclient.Any groups,
           java.lang.String[] mimetypes) {
           this.id = id;
           this.name = name;
           this.description = description;
           this.type = type;
           this.executable_path = executable_path;
           this._package = _package;
           this.published = published;
           this.desktopfile = desktopfile;
           this.servers = servers;
           this.groups = groups;
           this.mimetypes = mimetypes;
    }


    /**
     * Gets the id value for this Application.
     * 
     * @return id
     */
    public java.lang.String getId() {
        return id;
    }


    /**
     * Sets the id value for this Application.
     * 
     * @param id
     */
    public void setId(java.lang.String id) {
        this.id = id;
    }


    /**
     * Gets the name value for this Application.
     * 
     * @return name
     */
    public java.lang.String getName() {
        return name;
    }


    /**
     * Sets the name value for this Application.
     * 
     * @param name
     */
    public void setName(java.lang.String name) {
        this.name = name;
    }


    /**
     * Gets the description value for this Application.
     * 
     * @return description
     */
    public java.lang.String getDescription() {
        return description;
    }


    /**
     * Sets the description value for this Application.
     * 
     * @param description
     */
    public void setDescription(java.lang.String description) {
        this.description = description;
    }


    /**
     * Gets the type value for this Application.
     * 
     * @return type
     */
    public java.lang.String getType() {
        return type;
    }


    /**
     * Sets the type value for this Application.
     * 
     * @param type
     */
    public void setType(java.lang.String type) {
        this.type = type;
    }


    /**
     * Gets the executable_path value for this Application.
     * 
     * @return executable_path
     */
    public java.lang.String getExecutable_path() {
        return executable_path;
    }


    /**
     * Sets the executable_path value for this Application.
     * 
     * @param executable_path
     */
    public void setExecutable_path(java.lang.String executable_path) {
        this.executable_path = executable_path;
    }


    /**
     * Gets the _package value for this Application.
     * 
     * @return _package
     */
    public java.lang.String get_package() {
        return _package;
    }


    /**
     * Sets the _package value for this Application.
     * 
     * @param _package
     */
    public void set_package(java.lang.String _package) {
        this._package = _package;
    }


    /**
     * Gets the published value for this Application.
     * 
     * @return published
     */
    public boolean isPublished() {
        return published;
    }


    /**
     * Sets the published value for this Application.
     * 
     * @param published
     */
    public void setPublished(boolean published) {
        this.published = published;
    }


    /**
     * Gets the desktopfile value for this Application.
     * 
     * @return desktopfile
     */
    public java.lang.String getDesktopfile() {
        return desktopfile;
    }


    /**
     * Sets the desktopfile value for this Application.
     * 
     * @param desktopfile
     */
    public void setDesktopfile(java.lang.String desktopfile) {
        this.desktopfile = desktopfile;
    }


    /**
     * Gets the servers value for this Application.
     * 
     * @return servers
     */
    public com.aplana.owriter.ulteo.rpcclient.Any getServers() {
        return servers;
    }


    /**
     * Sets the servers value for this Application.
     * 
     * @param servers
     */
    public void setServers(com.aplana.owriter.ulteo.rpcclient.Any servers) {
        this.servers = servers;
    }


    /**
     * Gets the groups value for this Application.
     * 
     * @return groups
     */
    public com.aplana.owriter.ulteo.rpcclient.Any getGroups() {
        return groups;
    }


    /**
     * Sets the groups value for this Application.
     * 
     * @param groups
     */
    public void setGroups(com.aplana.owriter.ulteo.rpcclient.Any groups) {
        this.groups = groups;
    }


    /**
     * Gets the mimetypes value for this Application.
     * 
     * @return mimetypes
     */
    public java.lang.String[] getMimetypes() {
        return mimetypes;
    }


    /**
     * Sets the mimetypes value for this Application.
     * 
     * @param mimetypes
     */
    public void setMimetypes(java.lang.String[] mimetypes) {
        this.mimetypes = mimetypes;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof Application)) return false;
        Application other = (Application) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.id==null && other.getId()==null) || 
             (this.id!=null &&
              this.id.equals(other.getId()))) &&
            ((this.name==null && other.getName()==null) || 
             (this.name!=null &&
              this.name.equals(other.getName()))) &&
            ((this.description==null && other.getDescription()==null) || 
             (this.description!=null &&
              this.description.equals(other.getDescription()))) &&
            ((this.type==null && other.getType()==null) || 
             (this.type!=null &&
              this.type.equals(other.getType()))) &&
            ((this.executable_path==null && other.getExecutable_path()==null) || 
             (this.executable_path!=null &&
              this.executable_path.equals(other.getExecutable_path()))) &&
            ((this._package==null && other.get_package()==null) || 
             (this._package!=null &&
              this._package.equals(other.get_package()))) &&
            this.published == other.isPublished() &&
            ((this.desktopfile==null && other.getDesktopfile()==null) || 
             (this.desktopfile!=null &&
              this.desktopfile.equals(other.getDesktopfile()))) &&
            ((this.servers==null && other.getServers()==null) || 
             (this.servers!=null &&
              this.servers.equals(other.getServers()))) &&
            ((this.groups==null && other.getGroups()==null) || 
             (this.groups!=null &&
              this.groups.equals(other.getGroups()))) &&
            ((this.mimetypes==null && other.getMimetypes()==null) || 
             (this.mimetypes!=null &&
              java.util.Arrays.equals(this.mimetypes, other.getMimetypes())));
        __equalsCalc = null;
        return _equals;
    }

    private boolean __hashCodeCalc = false;
    public synchronized int hashCode() {
        if (__hashCodeCalc) {
            return 0;
        }
        __hashCodeCalc = true;
        int _hashCode = 1;
        if (getId() != null) {
            _hashCode += getId().hashCode();
        }
        if (getName() != null) {
            _hashCode += getName().hashCode();
        }
        if (getDescription() != null) {
            _hashCode += getDescription().hashCode();
        }
        if (getType() != null) {
            _hashCode += getType().hashCode();
        }
        if (getExecutable_path() != null) {
            _hashCode += getExecutable_path().hashCode();
        }
        if (get_package() != null) {
            _hashCode += get_package().hashCode();
        }
        _hashCode += (isPublished() ? Boolean.TRUE : Boolean.FALSE).hashCode();
        if (getDesktopfile() != null) {
            _hashCode += getDesktopfile().hashCode();
        }
        if (getServers() != null) {
            _hashCode += getServers().hashCode();
        }
        if (getGroups() != null) {
            _hashCode += getGroups().hashCode();
        }
        if (getMimetypes() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getMimetypes());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getMimetypes(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(Application.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("uri:ovd", "application"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("id");
        elemField.setXmlName(new javax.xml.namespace.QName("", "id"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("name");
        elemField.setXmlName(new javax.xml.namespace.QName("", "name"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("description");
        elemField.setXmlName(new javax.xml.namespace.QName("", "description"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("type");
        elemField.setXmlName(new javax.xml.namespace.QName("", "type"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("executable_path");
        elemField.setXmlName(new javax.xml.namespace.QName("", "executable_path"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("_package");
        elemField.setXmlName(new javax.xml.namespace.QName("", "package"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("published");
        elemField.setXmlName(new javax.xml.namespace.QName("", "published"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("desktopfile");
        elemField.setXmlName(new javax.xml.namespace.QName("", "desktopfile"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("servers");
        elemField.setXmlName(new javax.xml.namespace.QName("", "servers"));
        elemField.setXmlType(new javax.xml.namespace.QName("uri:ovd", "any"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("groups");
        elemField.setXmlName(new javax.xml.namespace.QName("", "groups"));
        elemField.setXmlType(new javax.xml.namespace.QName("uri:ovd", "any"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("mimetypes");
        elemField.setXmlName(new javax.xml.namespace.QName("", "mimetypes"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        elemField.setItemQName(new javax.xml.namespace.QName("", "item"));
        typeDesc.addFieldDesc(elemField);
    }

    /**
     * Return type metadata object
     */
    public static org.apache.axis.description.TypeDesc getTypeDesc() {
        return typeDesc;
    }

    /**
     * Get Custom Serializer
     */
    public static org.apache.axis.encoding.Serializer getSerializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new  org.apache.axis.encoding.ser.BeanSerializer(
            _javaType, _xmlType, typeDesc);
    }

    /**
     * Get Custom Deserializer
     */
    public static org.apache.axis.encoding.Deserializer getDeserializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new  org.apache.axis.encoding.ser.BeanDeserializer(
            _javaType, _xmlType, typeDesc);
    }

}
