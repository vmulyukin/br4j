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
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 0.9.6</a>, using an XML
 * Schema.
 * $Id$
 */

package org.aplana.br4j.dynamicaccess.xmldef.config;

  //---------------------------------/
 //- Imported classes and packages -/
//---------------------------------/

import java.io.Serializable;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;

/**
 * Class AccessRulesConfigItem.
 * 
 * @version $Revision$ $Date$
 */
public class AccessRulesConfigItem implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * ��������
     */
    private org.aplana.br4j.dynamicaccess.xmldef.config.Parameter _parameter;


      //----------------/
     //- Constructors -/
    //----------------/

    public AccessRulesConfigItem() {
        super();
    } //-- org.aplana.br4j.dynamicaccess.xmldef.config.AccessRulesConfigItem()


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Returns the value of field 'parameter'. The field
     * 'parameter' has the following description: ��������
     * 
     * @return Parameter
     * @return the value of field 'parameter'.
     */
    public org.aplana.br4j.dynamicaccess.xmldef.config.Parameter getParameter()
    {
        return this._parameter;
    } //-- org.aplana.br4j.dynamicaccess.xmldef.config.Parameter getParameter() 

    /**
     * Sets the value of field 'parameter'. The field 'parameter'
     * has the following description: ��������
     * 
     * @param parameter the value of field 'parameter'.
     */
    public void setParameter(org.aplana.br4j.dynamicaccess.xmldef.config.Parameter parameter)
    {
        this._parameter = parameter;
    } //-- void setParameter(org.aplana.br4j.dynamicaccess.xmldef.config.Parameter) 

}
