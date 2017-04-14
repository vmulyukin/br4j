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
/**
 *
 */
package com.aplana.dmsi.types;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

class ListWithCallbacks<T> implements List<T> {

    private List<T> sourceList = Collections.emptyList();
    private List<Callback<T>> callbacks = new ArrayList<Callback<T>>();

    public static interface Callback<T> {
	void elementAdded(T element);

	void elementRemoved(Object element);
    }

    public ListWithCallbacks(List<T> sourceList) {
	if (sourceList != null) {
	    this.sourceList = sourceList;
	}
    }

    public void addCallback(Callback<T> callback) {
	callbacks.add(callback);
    }

    private void fireAdded(T element) {
	for (Callback<T> callback : callbacks) {
	    callback.elementAdded(element);
	}
    }

    private void fireRemoved(Object element) {
	for (Callback<T> callback : callbacks) {
	    callback.elementRemoved(element);
	}
    }

    public boolean add(T o) {
	boolean isAdded = sourceList.add(o);
	fireAdded(o);
	return isAdded;
    }

    public void add(int index, T element) {
	sourceList.add(index, element);
	fireAdded(element);
    }

    public boolean addAll(Collection<? extends T> c) {
	boolean isAdded = sourceList.addAll(c);
	for (T element : c) {
	    fireAdded(element);
	}
	return isAdded;
    }

    public boolean addAll(int index, Collection<? extends T> c) {
	boolean isAdded = sourceList.addAll(index, c);
	for (T element : c) {
	    fireAdded(element);
	}
	return isAdded;
    }

    public void clear() {
    for (T element : sourceList) {
    	fireRemoved(element);
    }
	sourceList.clear();
    }

    public boolean contains(Object o) {
	return sourceList.contains(o);
    }

    public boolean containsAll(Collection<?> c) {
	return sourceList.containsAll(c);
    }

    public T get(int index) {
	return sourceList.get(index);
    }

    public int indexOf(Object o) {
	return sourceList.indexOf(o);
    }

    public boolean isEmpty() {
	return sourceList.isEmpty();
    }

    public Iterator<T> iterator() {
	return sourceList.iterator();
    }

    public int lastIndexOf(Object o) {
	return sourceList.lastIndexOf(o);
    }

    public ListIterator<T> listIterator() {
	return sourceList.listIterator();
    }

    public ListIterator<T> listIterator(int index) {
	return sourceList.listIterator(index);
    }

    public boolean remove(Object o) {
	boolean isDeleted = sourceList.remove(o);
	fireRemoved(o);
	return isDeleted;
    }

    public T remove(int index) {
	T obj = sourceList.remove(index);
	fireRemoved(obj);
	return obj;
    }

    public boolean removeAll(Collection<?> c) {
	boolean isDeleted = sourceList.removeAll(c);
	for (Object obj : c) {
	    fireRemoved(obj);
	}
	return isDeleted;
    }

    public boolean retainAll(Collection<?> c) {
	throw new UnsupportedOperationException(
		"Retain operation is not supported for this list");
    }

    public T set(int index, T element) {
	T replacedObj = sourceList.set(index, element);
	fireRemoved(replacedObj);
	fireAdded(element);
	return replacedObj;
    }

    public int size() {
	return sourceList.size();
    }

    public List<T> subList(int fromIndex, int toIndex) {
	return sourceList.subList(fromIndex, toIndex);
    }

    public Object[] toArray() {
	return sourceList.toArray();
    }

    public <P> P[] toArray(P[] a) {
	return sourceList.toArray(a);
    }

}