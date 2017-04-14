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
 * Class AttributeListItem.
 * 
 * @version $Revision$ $Date$
 */
public class AttributeListItem implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * ������ ����� ���������
     */
    private org.aplana.br4j.dynamicaccess.xmldef.config.Attribute _attribute;


      //----------------/
     //- Constructors -/
    //----------------/

    public AttributeListItem() {
        super();
    } //-- org.aplana.br4j.dynamicaccess.xmldef.config.AttributeListItem()


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Returns the value of field 'attribute'. The field
     * 'attribute' has the following description: ������ �����
     * ���������
     * 
     * @return Attribute
     * @return the value of field 'attribute'.
     */
    public org.aplana.br4j.dynamicaccess.xmldef.config.Attribute getAttribute()
    {
        return this._attribute;
    } //-- org.aplana.br4j.dynamicaccess.xmldef.config.Attribute getAttribute() 

    /**
     * Sets the value of field 'attribute'. The field 'attribute'
     * has the following description: ������ ����� ���������
     * 
     * @param attribute the value of field 'attribute'.
     */
    public void setAttribute(org.aplana.br4j.dynamicaccess.xmldef.config.Attribute attribute)
    {
        this._attribute = attribute;
    } //-- void setAttribute(org.aplana.br4j.dynamicaccess.xmldef.config.Attribute) 

}
