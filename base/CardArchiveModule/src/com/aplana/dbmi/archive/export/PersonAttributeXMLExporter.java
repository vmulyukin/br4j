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
package com.aplana.dbmi.archive.export;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.w3c.dom.Document;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.model.PersonAttribute;

/**
 * ������� � XML Person ���������
 * @author ppolushkin
 *
 */
public class PersonAttributeXMLExporter extends TreeAttributeXMLExporter {

	public PersonAttributeXMLExporter(Document doc, Attribute attr) {
		super(doc, attr);
	}
	
	@Override
	public List<String> getValue() {
		final Collection col = ((PersonAttribute) attr).getValues();
		List<String> list = new ArrayList<String>();
		Iterator it = col.iterator();
		while (it.hasNext())
		{
			Person p = (Person) it.next();
			list.add(String.valueOf(p.getId().getId()));
		}
		return list;
	}

}
