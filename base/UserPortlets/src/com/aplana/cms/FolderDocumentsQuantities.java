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
package com.aplana.cms;

import java.util.List;

public class FolderDocumentsQuantities {
	
	private long immediateQty;
	private long veryUrgentQty;
	private long urgentQty;
	private long withoutUrgencyQty;
	private long totalQty;
	
	public FolderDocumentsQuantities() {
		
	}
	
	public FolderDocumentsQuantities(long totalQty) {
		this.totalQty = totalQty;
	}
	
	public FolderDocumentsQuantities(List<Long[]> quantities) {
		if(null == quantities) {
			return;
		}
		
		for(Long[] qty : quantities) {
			if(qty[1].equals(Long.valueOf(0))) {
				withoutUrgencyQty = qty[0];
			} else if(qty[1].equals(Long.valueOf(1910))) {
				immediateQty = qty[0];
			} else if(qty[1].equals(Long.valueOf(1911))) {
				veryUrgentQty = qty[0];
			} else if(qty[1].equals(Long.valueOf(1912))) {
				urgentQty = qty[0];
			}
		}
		
		totalQty = immediateQty + veryUrgentQty + urgentQty + withoutUrgencyQty;
	}

	public long getImmediateQty() {
		return immediateQty;
	}

	public long getVeryUrgentQty() {
		return veryUrgentQty;
	}

	public long getUrgentQty() {
		return urgentQty;
	}

	public long getWithoutUrgencyQty() {
		return withoutUrgencyQty;
	}

	public long getTotalQty() {
		return totalQty;
	}

}
