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

/**
 * Stores the visible index of column.
 * @author atsvetkov
 *
 */
public class ColumnRowIndex implements Comparable<ColumnRowIndex> {
	String name;
    int index;
    
	public ColumnRowIndex() {
		name = "";
		index = 0;
	}
	
	ColumnRowIndex(String n, int i) {
		name = n;
		index = i;
	}

	public void setColumn(String n, int i) {
		name = n;
		index = i;
	}
	
	public String getName() {
		return name;
	}

	public int getIndex() {
		return index;
	}

	@Override
	public boolean equals(Object arg0) {
		if( !(arg0 instanceof ColumnRowIndex)){
			return false;
		}
		return ((ColumnRowIndex)arg0).getIndex() == this.getIndex();
	}
	
	public int compareTo(ColumnRowIndex arg0) {
		return arg0.getIndex() - this.getIndex();
	}
	
	@Override
	public String toString() {
		return "index: " + getIndex() + "name: " + getName();
	}
}
