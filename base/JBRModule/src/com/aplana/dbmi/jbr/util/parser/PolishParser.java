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
package com.aplana.dbmi.jbr.util.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.StringTokenizer;

public class PolishParser implements BooleanParser {
	
	private String booleanExpression;
	
	
	/**
	* �������� �������������� �������� � �� ����������.
	*
	* @see #sortingStation(String, java.util.Map)
	*/
		
	 public static final Map<String, Integer> MAIN_MATH_OPERATIONS;
	 
	 static {
	 MAIN_MATH_OPERATIONS = new HashMap<String, Integer>();
	 MAIN_MATH_OPERATIONS.put("*", 1);
	 MAIN_MATH_OPERATIONS.put("+", 2);
	 }
    
	public PolishParser(String booleanExpression) {
		this.booleanExpression = booleanExpression;
	}

	/**
	 * ���������� � ������� �������� �������� ������
	 */

	@Override
	public boolean calculate(Map<String, Boolean> conditionMap) {
		return calculateExpression(booleanExpression, conditionMap);
	}
	
	private static String sortingStation(String expression,
			Map<String, Integer> operations, String leftBracket,
			String rightBracket) {
		if (expression == null || expression.length() == 0)
			throw new IllegalStateException("Expression isn't specified.");
		if (operations == null || operations.isEmpty())
			throw new IllegalStateException("Operations aren't specified.");
		// �������� ������, �������� �� "�������" - �������� � ��������..
		List<String> out = new ArrayList<String>();
		// ���� ��������.
		Stack<String> stack = new Stack<String>();

		// �������� �������� �� ���������.
		expression = expression.replace(" ", "");

		// ��������� "��������", �� ���������� ���������� (�������� � ������).
		Set<String> operationSymbols = new HashSet<String>(operations.keySet());
		operationSymbols.add(leftBracket);
		operationSymbols.add(rightBracket);

		// ������, �� ������� ���������� ������ ������ �� ������� ��������.
		int index = 0;
		// ������� ������������� ������ ���������� ��������.
		boolean findNext = true;
		while (findNext) {
			int nextOperationIndex = expression.length();
			String nextOperation = "";
			// ����� ���������� ��������� ��� ������.
			for (String operation : operationSymbols) {
				int i = expression.indexOf(operation, index);
				if (i >= 0 && i < nextOperationIndex) {
					nextOperation = operation;
					nextOperationIndex = i;
				}
			}
			// �������� �� ������.
			if (nextOperationIndex == expression.length()) {
				findNext = false;
			} else {
				// ���� ��������� ��� ������ ������������ �������, ��������� ���
				// � �������� ������.
				if (index != nextOperationIndex) {
					out.add(expression.substring(index, nextOperationIndex));
				}
				// ��������� ���������� � ������.
				// ����������� ������.
				if (nextOperation.equals(leftBracket)) {
					stack.push(nextOperation);
				}
				// ����������� ������.
				else if (nextOperation.equals(rightBracket)) {
					while (!stack.peek().equals(leftBracket)) {
						out.add(stack.pop());
						if (stack.empty()) {
							throw new IllegalArgumentException(
									"Unmatched brackets");
						}
					}
					stack.pop();
				}
				// ��������.
				else {
					while (!stack.empty()
							&& !stack.peek().equals(leftBracket)
							&& (operations.get(nextOperation) >= operations
									.get(stack.peek()))) {
						out.add(stack.pop());
					}
					stack.push(nextOperation);
				}
				index = nextOperationIndex + nextOperation.length();
			}
		}
		// ���������� � �������� ������ ��������� ����� ���������� ��������.
		if (index != expression.length()) {
			out.add(expression.substring(index));
		}
		// ������������� ��������� ������ � �������� ������.
		while (!stack.empty()) {
			out.add(stack.pop());
		}
		StringBuffer result = new StringBuffer();
		if (!out.isEmpty())
			result.append(out.remove(0));
		while (!out.isEmpty())
			result.append(" ").append(out.remove(0));

		return result.toString();
	}

	private static String sortingStation(String expression,
			Map<String, Integer> operations) {
		return sortingStation(expression, operations, "(", ")");
	}
	
	private static Boolean calculateExpression(String expression, Map<String, Boolean> conditionMap) {
		String rpn = sortingStation(expression, MAIN_MATH_OPERATIONS);
		StringTokenizer tokenizer = new StringTokenizer(rpn, " ");
		Stack<Boolean> stack = new Stack<Boolean>();
		while (tokenizer.hasMoreTokens()) {
			String token = tokenizer.nextToken();
			// �������.
			if (!MAIN_MATH_OPERATIONS.keySet().contains(token)) {
				stack.push(new Boolean(conditionMap.get(token)));
			} else {
				Boolean operand2 = stack.pop();
				Boolean operand1 = stack.empty() ? false : stack.pop();
				if (token.equals("*")) {
					stack.push(operand1 && operand2);
				} else if (token.equals("+")) {
					stack.push(operand1 || operand2);
				}
			}
		}
		if (stack.size() != 1)
			throw new IllegalArgumentException("Expression syntax error.");
		return stack.pop();
	}
}
