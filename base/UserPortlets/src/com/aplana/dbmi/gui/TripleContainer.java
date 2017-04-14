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
package com.aplana.dbmi.gui;

import java.util.List;

public class TripleContainer extends AttributeContainer {

	public void addComponent(Object component) {
		if (component == null)
			return;
		if (! (component instanceof BlockView))
			return;
		BlockView block = (BlockView)component;
		if (!(block.getLayout() instanceof String))
			return;
		
		try {
			int layout = Integer.parseInt((String)block.getLayout());
/*			if ((layout < 100)||(layout > 399))
				return;
*/			int region = layout / 100;
			super.addComponent(Integer.toString(region), block);
		} catch (NumberFormatException e) {}
	}

	public void addRegion(Object regionID) {
		if (regionID == null)
			return;
		if (! (regionID instanceof String))
			return;
		super.addRegion((String)regionID);
	}

	public void clearRegion(Object regionID) {
		if (regionID == null)
			return;
		if (! (regionID instanceof String))
			return;
		super.clearRegion((String)regionID);
	}

	public List<BlockView> getRegion(Object regionID) {
		if (regionID == null)
			return null;
		if (! (regionID instanceof String))
			return null;
		return super.getRegion((String)regionID);
	}

	public int getRegionSize(Object regionID) {
		if (regionID == null)
			return 0;
		if (! (regionID instanceof String))
			return 0;
		return super.getRegionSize((String)regionID);
	}

	protected int compareComponents(Object o1, Object o2) {
		if (!(o1 instanceof BlockView) || !(o2 instanceof BlockView))
			return 0;
		BlockView block1 = (BlockView)o1;
		BlockView block2 = (BlockView)o2;
		if (!(block1.getLayout() instanceof String) || 
			!(block2.getLayout() instanceof String))
			return 0;
		int i = Integer.parseInt((String)block1.getLayout());
		int order1 = (i / 100);
		order1 = i - (order1*100);
		i = Integer.parseInt((String)block2.getLayout());
		int order2 = (i / 100);
		order2 = i - (order2*100);
		return (new Integer(order1)).compareTo(new Integer(order2));
	}

}
