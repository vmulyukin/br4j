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
 * OvdAdminServiceLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.aplana.owriter.ulteo.rpcclient;

public class OvdAdminServiceLocator extends org.apache.axis.client.Service implements com.aplana.owriter.ulteo.rpcclient.OvdAdminService {

/**
 * Ovd Session Manager administration console API
 */

    public OvdAdminServiceLocator() {
    }


    public OvdAdminServiceLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public OvdAdminServiceLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for OvdAdminPort
    private java.lang.String OvdAdminPort_address = "https://172.16.125.107/ovd/service/admin";

    public java.lang.String getOvdAdminPortAddress() {
        return OvdAdminPort_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String OvdAdminPortWSDDServiceName = "OvdAdminPort";

    public java.lang.String getOvdAdminPortWSDDServiceName() {
        return OvdAdminPortWSDDServiceName;
    }

    public void setOvdAdminPortWSDDServiceName(java.lang.String name) {
        OvdAdminPortWSDDServiceName = name;
    }

    public com.aplana.owriter.ulteo.rpcclient.OvdAdminPortType getOvdAdminPort() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(OvdAdminPort_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getOvdAdminPort(endpoint);
    }

    public com.aplana.owriter.ulteo.rpcclient.OvdAdminPortType getOvdAdminPort(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            com.aplana.owriter.ulteo.rpcclient.OvdAdminBindingStub _stub = new com.aplana.owriter.ulteo.rpcclient.OvdAdminBindingStub(portAddress, this);
            _stub.setPortName(getOvdAdminPortWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setOvdAdminPortEndpointAddress(java.lang.String address) {
        OvdAdminPort_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (com.aplana.owriter.ulteo.rpcclient.OvdAdminPortType.class.isAssignableFrom(serviceEndpointInterface)) {
                com.aplana.owriter.ulteo.rpcclient.OvdAdminBindingStub _stub = new com.aplana.owriter.ulteo.rpcclient.OvdAdminBindingStub(new java.net.URL(OvdAdminPort_address), this);
                _stub.setPortName(getOvdAdminPortWSDDServiceName());
                return _stub;
            }
        }
        catch (java.lang.Throwable t) {
            throw new javax.xml.rpc.ServiceException(t);
        }
        throw new javax.xml.rpc.ServiceException("There is no stub implementation for the interface:  " + (serviceEndpointInterface == null ? "null" : serviceEndpointInterface.getName()));
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(javax.xml.namespace.QName portName, Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        if (portName == null) {
            return getPort(serviceEndpointInterface);
        }
        java.lang.String inputPortName = portName.getLocalPart();
        if ("OvdAdminPort".equals(inputPortName)) {
            return getOvdAdminPort();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("uri:ovd", "OvdAdminService");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("uri:ovd", "OvdAdminPort"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        
if ("OvdAdminPort".equals(portName)) {
            setOvdAdminPortEndpointAddress(address);
        }
        else 
{ // Unknown Port Name
            throw new javax.xml.rpc.ServiceException(" Cannot set Endpoint Address for Unknown Port" + portName);
        }
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(javax.xml.namespace.QName portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        setEndpointAddress(portName.getLocalPart(), address);
    }

}
