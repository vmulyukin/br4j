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
package com.aplana.dbmi.model.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Helper for comfortable initializing collections with 
 * syntax sugar (diamond generic types like java 7).
 * Example:
 * before: Map<String, Map<Long, List<Person>>> map = new HashMap<String, Map<Long, List<Person>>>();
 * after:  Map<String, Map<Long, List<Person>>> map = Init.hashMap();
 * @author desu
 *
 */
public abstract class Init {
	
	// maps
	public static <K,V> HashMap<K,V> hashMap() {
		return new HashMap<K, V>();
	}
	public static <K,V> HashMap<K,V> hashMap(Map<? extends K, ? extends V> map) {
		return new HashMap<K, V>(map);
	}
	public static <K,V> HashMap<K,V> hashMap(int size) {
		return new HashMap<K, V>(size);
	}
	public static <K,V> LinkedHashMap<K,V> linkedHashMap() {
		return new LinkedHashMap<K, V>();
	}
	public static <K,V> LinkedHashMap<K,V> linkedHashMap(Map<? extends K, ? extends V> map) {
		return new LinkedHashMap<K, V>(map);
	}
	public static <K,V> LinkedHashMap<K,V> linkedHashMap(int size) {
		return new LinkedHashMap<K, V>(size);
	}
	public static <K,V> WeakHashMap<K,V> weakHashMap() {
		return new WeakHashMap<K, V>();
	}
	public static <K,V> WeakHashMap<K,V> weakHashMap(Map<? extends K, ? extends V> map) {
		return new WeakHashMap<K, V>(map);
	}
	public static <K,V> WeakHashMap<K,V> weakHashMap(int size) {
		return new WeakHashMap<K, V>(size);
	}
	public static <K,V> ConcurrentHashMap<K,V> concurrentHashMap() {
		return new ConcurrentHashMap<K, V>();
	}
	public static <K,V> ConcurrentHashMap<K,V> concurrentHashMap(Map<? extends K, ? extends V> map) {
		return new ConcurrentHashMap<K, V>(map);
	}
	public static <K,V> ConcurrentHashMap<K,V> concurrentHashMap(int size) {
		return new ConcurrentHashMap<K, V>(size);
	}
	// maps end
	
	// sets
	public static <V> HashSet<V> hashSet() {
		return new HashSet<V>();
	}
	public static <V> HashSet<V> hashSet(Collection<? extends V> set) {
		return new HashSet<V>(set);
	}
	public static <V> HashSet<V> hashSet(int size) {
		return new HashSet<V>(size);
	}
	public static <V> LinkedHashSet<V> linkedHashSet() {
		return new LinkedHashSet<V>();
	}
	public static <V> LinkedHashSet<V> linkedHashSet(Collection<? extends V> set) {
		return new LinkedHashSet<V>(set);
	}
	public static <V> LinkedHashSet<V> linkedHashSet(int size) {
		return new LinkedHashSet<V>(size);
	}
	// sets end
	
	// lists
	public static <V> ArrayList<V> arrayList() {
		return new ArrayList<V>();
	}
	public static <V> ArrayList<V> arrayList(Collection<? extends V> list) {
		return new ArrayList<V>(list);
	}
	public static <V> ArrayList<V> arrayList(int size) {
		return new ArrayList<V>(size);
	}
	public static <V> LinkedList<V> linkedList() {
		return new LinkedList<V>();
	}
	public static <V> LinkedList<V> linkedList(Collection<? extends V> list) {
		return new LinkedList<V>(list);
	}
	// lists end
	
	/* ������������
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <K,V, R extends Map<K,V>, T extends Map> R map(Class<T> clazz) {
		try {
			return (R)clazz.newInstance();
		} catch (Exception e) {
			return null;
		}
	}*/
}
