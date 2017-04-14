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
package org.aplana.br4j.dynamicaccess.xmldef.types;

import org.aplana.br4j.dynamicaccess.xmldef.Rule;
import org.aplana.br4j.dynamicaccess.xmldef.Template;
import org.aplana.br4j.dynamicaccess.xmldef.types.PermissionWrapper.RuleType;

/**
 * Utility class to handle {@link RuleType}'s
 * @author atsvetkov
 *
 */
public class RuleTypeUtility {

	/**
	 * Retrieves {@link RuleType} by rule name
	 * @param ruleName name of rule
	 * @return {@link RuleType} related with rule
	 */
	public static RuleType getRuleTypeByRuleName(String ruleName, Template template) {
		if(template == null){
			throw new IllegalArgumentException("Template is null!");
		}
		for (int i = 0; i < template.getRules().getRuleCount(); i++) {
			if (template.getRules().getRule(i).getName().contains(ruleName)) {
				Rule rule = template.getRules().getRule(i);
				if (rule.getRuleRole() != null) {
					return RuleType.Role;
				} else if (rule.getRulePerson() != null) {
					return RuleType.Person;
				} else if (rule.getRuleProfile() != null) {
					return RuleType.Profile;
				} else if (rule.getRuleDelegation() != null) {
					return RuleType.Delegation;
				}
			}
        }		
		return RuleType.Undefined;
	}
	
}
