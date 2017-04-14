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
package org.aplana.br4j.dynamicaccess;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableCellRenderer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aplana.br4j.dynamicaccess.xmldef.Rule;
import org.aplana.br4j.dynamicaccess.xmldef.Template;
import org.aplana.br4j.dynamicaccess.xmldef.types.Action;

/**
 * Permission table header cell renderer. It is used to highlight with different
 * colors columns having different rule type.
 * 
 * @author atsvetkov
 * 
 */
public class TableHeaderCellRenderer extends JPanel implements TableCellRenderer {
	
	protected final Log logger = LogFactory.getLog(getClass());

	private static final Color ROLE_COLOR = new Color(252, 242, 228);
	private static final Color PROFILE_COLOR = new Color(225, 253, 208);
	private static final Color DELEGATION_COLOR = new Color(208, 251, 253);
	private static final Color PERSON_COLOR = new Color(253, 208, 222);
	private static final int WORD_HEIGHT = 15;
	
	private Template template;	
	private int maxRuleNameTokens;
	
	public TableHeaderCellRenderer(Template template) {
		this.template = template;
		this.maxRuleNameTokens = getMaxTokensInRuleName(template.getRules().getRule());
	}

	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {		
		this.removeAll();
		
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		String ruleName = (value == null) ? null : value.toString();
		

		Color defaultColor = table.getTableHeader().getBackground();
		
		Color backgrColor = getColorForRuleType(ruleName, defaultColor);
		//����������� ������������ ��� ������� �������������
		if(isRuleRenamed(ruleName)){
			ruleName = "[RN]" + ruleName;
		}
		String[] tokens = getRuleNameTokens(ruleName);
		
		for(int i = 0; i < tokens.length ; i++){
			JLabel textToken = new JLabel(tokens[i]);
			textToken.setBorder(new EmptyBorder(new Insets(0, 2, 0, 0)));
			this.add(textToken);
		}
		
		if(column == 0 ){
			setBorder(BorderFactory.createMatteBorder(0, 1, 1, 1, UIManager.getColor("Table.gridColor")));		
		}else{
			setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, UIManager.getColor("Table.gridColor")));		
				
		}
		setPreferredSize(new Dimension((int)getPreferredSize().getWidth(), maxRuleNameTokens * WORD_HEIGHT));
		setBackground(backgrColor);	
		return this;
	}
	
	public String[] getRuleNameTokens(String ruleName) {
		String modifiedRuleName = ruleName.replace("\\", "\\ ");		
		String[] tokens  = modifiedRuleName.split("\\s", 3);
		return tokens;
	}

	public int getMaxTokensInRuleName(Rule[] rules) {
		int max = 1;
		for (Rule rule : rules) {
			String[] tokens = getRuleNameTokens(rule.getName());
			if (max < tokens.length) {
				max = tokens.length;
			}
		}
		return max;
	}
	
	/**
	 * Returns predefined color for rule type.
	 * @param ruleName name of {@Rule}
	 * @param defaultColor default color
	 * @return {@Color} of table header column.
	 */
	private Color getColorForRuleType(String ruleName, Color defaultColor) {
		Color backgroundColor = null;
		for (int i = 0; i < template.getRules().getRuleCount(); i++) {
			Rule rule = template.getRules().getRule(i);
			if (rule != null && rule.getName().contains(ruleName)) {
				if (rule.getRuleRole() != null) {
					backgroundColor = ROLE_COLOR;
					break;
				} else if (rule.getRulePerson() != null) {
					backgroundColor = PERSON_COLOR;
					break;
				} else if (rule.getRuleDelegation() != null) {
					backgroundColor = DELEGATION_COLOR;
					break;
				} else if (rule.getRuleProfile() != null) {
					backgroundColor = PROFILE_COLOR;
					break;
				}else{
					backgroundColor = defaultColor;
				}
			}
		}	
		return backgroundColor;
	}
	
	private boolean isRuleRenamed(String ruleName){
		for (Rule rule : template.getRules().getRule()) {
			if (rule != null && rule.getName().contains(ruleName)) {
				return Action.RENAME.equals(rule.getAction());
			}
		}
		return false;
	}

}
