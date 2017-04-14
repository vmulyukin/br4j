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
package com.aplana.scanner.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;

import javax.swing.AbstractListModel;

import com.aplana.scanner.model.Page;

/**
 * <code>ListModel</code> for the list of scanned pages.
 *
 * @author <a href="mailto:ogalkin@aplana.com">Oleg Galkin</a>
 */
public class PageListModel extends AbstractListModel {
	private static final long serialVersionUID = 3443677073076265391L;
	
	private List<Page> pages = Collections.synchronizedList(new ArrayList<Page>());
	
	private int counter = 0;

	private Comparator<Page> comparator = new Comparator<Page>() {
		public int compare(Page o1, Page o2) {
				return o1.getNumber() - o2.getNumber();
		}
	};

	/* (non-Javadoc)
	 * @see javax.swing.ListModel#getElementAt(int)
	 */
	public Object getElementAt(int index) {
		return pages.get(index);
	}

	/* (non-Javadoc)
	 * @see javax.swing.ListModel#getSize()
	 */
	public int getSize() {
		return pages.size();
	}
	
	/**
   * Tests whether this model has any components.
   *
   * @return <code>true</code> if and only if this model has no components, that is,
   *         its size is zero; <code>false</code> otherwise
   * @see Vector#isEmpty()
   */
	public boolean isEmpty() {
  	return pages.isEmpty();
  }
	
	/**
   * Searches for the first occurrence of the page.
   *
   * @param   page   the page
   * @return  the index of the first occurrence of the argument in this list;
   *          returns <code>-1</code> if the object is not found
   */
  public int indexOf(Page page) {
  	return pages.indexOf(page);
  }
	
	/**
   * Returns the page at the specified position in this list.
   * <p/>
   * Throws an <code>ArrayIndexOutOfBoundsException</code> if the index is out of range
   * (<code>index &lt; 0 || index &gt;= size()</code>).
   *
   * @param index  the index of the page to return
   */
	public Page get(int index) {
		return pages.get(index);
	}
	
	/**
   * Replaces the page at the specified position in this list with the specified page.
   * <p/>
   * Throws an <code>ArrayIndexOutOfBoundsException</code> if the index is out of range
   * (<code>index &lt; 0 || index &gt;= size()</code>).
   *
   * @param  index  the index of page to replace
   * @param  page   the page to be stored at the specified position
   * @return the page previously at the specified position
   */
	public Page set(int index, Page page) {
		Page previous = pages.set(index, page);
		fireContentsChanged(this, index, index);
		return previous;
	}
	
	/**
   * Adds the specified page to the end of this list. 
   *
   * @param  page the {@link Page} to be added
   * @return the index of the added page
   */
	public int add(Page page) {
		int index;
		synchronized(pages) {
			index = pages.size();
			pages.add(page);
			Collections.sort(pages,comparator);
			for(int i = 0; i< pages.size(); i++){
				if (pages.get(i).getNumber() == page.getNumber()){
					index = i;
					break;
				}
			}
		}
		fireIntervalAdded(this, index, index);
		return index;
	}
	
	/**
   * Inserts the specified page at the specified position in this list.
   * <p/>
   * Throws an <code>ArrayIndexOutOfBoundsException</code> if the index is out of range
   * (<code>index &lt; 0 || index &gt; size()</code>).
   *
   * @param index  the index at which the specified page is to be inserted
   * @param page   the page to be inserted
   */
	public void add(int index, Page page) {
		pages.add(index, page);
		fireIntervalAdded(this, index, index);
	}
	
	/**
   * Removes the page at the specified position in this list. Returns the element that was removed
   * from the list.
   * <p/>
   * Throws an <code>ArrayIndexOutOfBoundsException</code> if the index is out of range
   * (<code>index &lt; 0 || index &gt;= size()</code>).
   *
   * @param index  the index of the page to remove
   */
	public Page remove(int index) {
		Page page = pages.remove(index);
		fireIntervalRemoved(this, index, index);
		return page;
	}
	
	/**
   * Removes all of the pages from this list. The list will be empty after this call returns
   * (unless it throws an exception).
   */
	public void clear() {
		int index;
		synchronized(pages) {
			index = pages.size() - 1;
			pages.clear();
		}
		if (index >= 0)
			fireIntervalRemoved(this, 0, index);
	}

	public synchronized int getNextNumber() {
		return counter++;
	}
	
	public void syncPointers(){
		this.counter = pages.size();
		for(int i = 0; i < pages.size(); i++){
			pages.get(i).setNumber(i);
		}
	}
}
