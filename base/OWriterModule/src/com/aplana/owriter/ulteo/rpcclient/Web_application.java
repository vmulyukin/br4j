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
 * Web_application.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.aplana.owriter.ulteo.rpcclient;

public class Web_application  implements java.io.Serializable {
    private java.lang.String id;

    private java.lang.String url_prefix;

    private java.lang.String raw_configuration;

    private com.aplana.owriter.ulteo.rpcclient.Any values;

    public Web_application() {
    }

    public Web_application(
           java.lang.String id,
           java.lang.String url_prefix,
           java.lang.String raw_configuration,
           com.aplana.owriter.ulteo.rpcclient.Any values) {
           this.id = id;
           this.url_prefix = url_prefix;
           this.raw_configuration = raw_configuration;
           this.values = values;
    }


    /**
     * Gets the id value for this Web_application.
     * 
     * @return id
     */
    public java.lang.String getId() {
        return id;
    }


    /**
     * Sets the id value for this Web_application.
     * 
     * @param id
     */
    public void setId(java.lang.String id) {
        this.id = id;
    }


    /**
     * Gets the url_prefix value for this Web_application.
     * 
     * @return url_prefix
     */
    public java.lang.String getUrl_prefix() {
        return url_prefix;
    }


    /**
     * Sets the url_prefix value for this Web_application.
     * 
     * @param url_prefix
     */
    public void setUrl_prefix(java.lang.String url_prefix) {
        this.url_prefix = url_prefix;
    }


    /**
     * Gets the raw_configuration value for this Web_application.
     * 
     * @return raw_configuration
     */
    public java.lang.String getRaw_configuration() {
        return raw_configuration;
    }


    /**
     * Sets the raw_configuration value for this Web_application.
     * 
     * @param raw_configuration
     */
    public void setRaw_configuration(java.lang.String raw_configuration) {
        this.raw_configuration = raw_configuration;
    }


    /**
     * Gets the values value for this Web_application.
     * 
     * @return values
     */
    public com.aplana.owriter.ulteo.rpcclient.Any getValues() {
        return values;
    }


    /**
     * Sets the values value for this Web_application.
     * 
     * @param values
     */
    public void setValues(com.aplana.owriter.ulteo.rpcclient.Any values) {
        this.values = values;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof Web_application)) return false;
        Web_application other = (Web_application) obj;
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
            ((this.url_prefix==null && other.getUrl_prefix()==null) || 
             (this.url_prefix!=null &&
              this.url_prefix.equals(other.getUrl_prefix()))) &&
            ((this.raw_configuration==null && other.getRaw_configuration()==null) || 
             (this.raw_configuration!=null &&
              this.raw_configuration.equals(other.getRaw_configuration()))) &&
            ((this.values==null && other.getValues()==null) || 
             (this.values!=null &&
              this.values.equals(other.getValues())));
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
        if (getUrl_prefix() != null) {
            _hashCode += getUrl_prefix().hashCode();
        }
        if (getRaw_configuration() != null) {
            _hashCode += getRaw_configuration().hashCode();
        }
        if (getValues() != null) {
            _hashCode += getValues().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(Web_application.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("uri:ovd", "web_application"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("id");
        elemField.setXmlName(new javax.xml.namespace.QName("", "id"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("url_prefix");
        elemField.setXmlName(new javax.xml.namespace.QName("", "url_prefix"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("raw_configuration");
        elemField.setXmlName(new javax.xml.namespace.QName("", "raw_configuration"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("values");
        elemField.setXmlName(new javax.xml.namespace.QName("", "values"));
        elemField.setXmlType(new javax.xml.namespace.QName("uri:ovd", "any"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
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
