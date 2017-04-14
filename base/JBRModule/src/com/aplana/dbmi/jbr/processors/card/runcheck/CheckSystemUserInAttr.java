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
package com.aplana.dbmi.jbr.processors.card.runcheck;

import java.text.MessageFormat;
import java.util.Set;

import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.service.DataException;

public class CheckSystemUserInAttr extends CheckCurrentUserInAttr {

	private static final long serialVersionUID = 1L;
	
	@Override
	   public Object process() throws DataException {
	              
	       Set<Person> persons = getPersonsList(curCard, attrId, false);
	       if (persons == null){
	           logger.debug("Card "+ curCard.getId() + " has empty attribute "+ attrId + "in card " + curCard.getId() +" -> check is FALSE");
	           throw new CardCheckException("No persons in "+attrId+"@"+curCard.getId()+"");
	       }
	       
	       if (!persons.contains((Person)getSystemUser().getPerson())){
	           logger.debug("Card "+ curCard.getId() + " has not system person "+ getSystemUser().getPerson().getId()+ " in attribute "+ attrId +" -> check is FALSE");
	           throw new CardCheckException("No current person in card "+attrId+"@"+curCard.getId()+"");
	       }

	       return null;
	   }
	
	@Override
	   public String toString(){
	       return MessageFormat.format( "CheckSystemUserInAttr ({0}=''{1}'')", 
	                   PARAM_ATTR_ID, attrId != null ? attrId.getId() : "null" );
	   }

}
