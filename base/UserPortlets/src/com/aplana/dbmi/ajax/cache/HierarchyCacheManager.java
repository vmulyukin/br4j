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
package com.aplana.dbmi.ajax.cache;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aplana.dbmi.card.hierarchy.Hierarchy;
import com.aplana.dbmi.card.hierarchy.HierarchyLoader;
import com.aplana.dbmi.card.hierarchy.descriptor.HierarchyDescriptor;

/** �������� ���������� ����� ��� ��������
 * 
 * @author dstarostin
 *
 */
public class HierarchyCacheManager implements Runnable {
	private static Log logger = LogFactory.getLog(HierarchyCacheManager.class);;

    static Map<Integer,Hierarchy> cache = new HashMap<Integer,Hierarchy>();
	
    public interface HierarchyCreator {
        Hierarchy create();
    }
    
    /**
     * ���������� �������� �� ���� �� ��������������
     * @param id ������������� {@link HierarchyDescriptor}
     * @return ������ {@link Hierarchy} ��� null ���� �������� � ���� ���
     */
	public static Hierarchy getHierarchy(int id) {
		synchronized (cache) {
			return cache.get(id);
		}
	}

	/**
	 * ������ ��������, �������� � � ��� (������� ������ ��������), ����� ����� �� ��������� 
	 * @param id ������������� {@link HierarchyDescriptor}
	 * @param hc ������, ��������� ��������
	 * @return ��������� ��������
	 */
	public static Hierarchy putHierarchy(int id, HierarchyCreator hc, HierarchyLoader hl) {
		return putHierarchy(id, 0, hc, hl);
	}
	
	/**
	 * ������ ��������, �������� � � ��� (������� ������� ��������) � ������ �����, 
	 * ������� ������ ����� �������� �������� ����� ��������� �����
	 * @param id ������������� {@link HierarchyDescriptor}
	 * @param reloadTimeMin �������� � ������� ����� ���������� ������
	 * @param hc ������, ��������� ��������
	 * @return ��������� ��������
	 */
	public static Hierarchy putHierarchy(int id, int reloadTimeMin, HierarchyCreator hc, HierarchyLoader hl) {
		Hierarchy h = hc.create();
		if (h != null) {
			synchronized (cache) {
				cache.put(id, h);
			}
			logger.info("Hierarchy " + id + " updated");
			if (reloadTimeMin>0){
				Thread t = new Thread(new HierarchyCacheManager(id, reloadTimeMin, hc, hl), "CacheManager:" + id);
				t.setDaemon(true);
				t.setPriority(Thread.MIN_PRIORITY);
				t.setUncaughtExceptionHandler(exHandler);
				t.start();
			}
		}
		return h;
	}
	
	/**
	 * ���������� ����������������� ���������� ��� ��������
	 */
	private static Thread.UncaughtExceptionHandler exHandler = new Thread.UncaughtExceptionHandler() {
		public void uncaughtException(Thread t, Throwable e) {
			logger.warn("Thread \"" + t.getName() + "\" throws " + 
					e.getClass().getSimpleName() + 
					": " + e.getMessage() +
					e.getCause() != null ? "\nCaused by:\n" + e.getCause().getStackTrace() : "");
			//logger.warn("Thread \"" + t.getName() + "\" throws " + e);
		}
	};
	
	private int reloadTimeMin;
	private HierarchyCreator hc;
	private HierarchyLoader hl;
	private int id;
	public HierarchyCacheManager(int id, int reloadTimeMin, HierarchyCreator hc, HierarchyLoader hl) {
		this.id = id;
		this.reloadTimeMin = reloadTimeMin;
		this.hc = hc;
		this.hl = hl;
	}
	
	public void run() {
		try {
			Thread.sleep(reloadTimeMin * 60000);
			putHierarchy(id, reloadTimeMin, hc, hl);		
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
