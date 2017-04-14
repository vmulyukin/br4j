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
 * Class ParameterGroupItem.
 * 
 * @version $Revision$ $Date$
 */
public class ParameterGroupItem implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * ��������� �������� � ������
     */
    private org.aplana.br4j.dynamicaccess.xmldef.config.Param _param;


      //----------------/
     //- Constructors -/
    //----------------/

    public ParameterGroupItem() {
        super();
    } //-- org.aplana.br4j.dynamicaccess.xmldef.config.ParameterGroupItem()


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Returns the value of field 'param'. The field 'param' has
     * the following description: ��������� �������� � ������
     * 
     * @return Param
     * @return the value of field 'param'.
     */
    public org.aplana.br4j.dynamicaccess.xmldef.config.Param getParam()
    {
        return this._param;
    } //-- org.aplana.br4j.dynamicaccess.xmldef.config.Param getParam() 

    /**
     * Sets the value of field 'param'. The field 'param' has the
     * following description: ��������� �������� � ������
     * 
     * @param param the value of field 'param'.
     */
    public void setParam(org.aplana.br4j.dynamicaccess.xmldef.config.Param param)
    {
        this._param = param;
    } //-- void setParam(org.aplana.br4j.dynamicaccess.xmldef.config.Param) 

}
