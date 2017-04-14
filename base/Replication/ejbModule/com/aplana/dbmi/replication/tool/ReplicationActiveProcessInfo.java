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
package com.aplana.dbmi.replication.tool;

import com.aplana.dbmi.model.ObjectId;

import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;

public class ReplicationActiveProcessInfo {
	private static WeakMultiMap activeReplMap = new WeakMultiMap();
	private static WeakMultiMap activeCopyMap = new WeakMultiMap();

	private static class WeakMultiMap extends WeakHashMap<ObjectId, List<Info>> {
		@Override
		public List<Info> get(Object key) {
			List<Info> list = super.get(key);
			if (list != null) {
				return list;
			}
			list = new ArrayList<Info>();
			this.put((ObjectId) key, list);
			return list;
		}
	}

	private static class Info {
		protected ObjectId cardId;
		protected String addressee;
		protected boolean replicated = false;
		protected boolean copiedFromLocal = false;

		protected Info(ObjectId id, String addressee) {
			this(id, addressee, false);
		}

		protected Info(ObjectId id) {
			this(id, null, false);
		}

		protected Info(ObjectId id, String addressee, boolean copied) {
			this.cardId = id;
			this.addressee = addressee;
			this.copiedFromLocal = copied;
		}
	}

	public static void putAllCardForReplication(ObjectId queryUid, List<String> addressees, ObjectId... ids) {
		List<Info> infoList = activeReplMap.get(queryUid);
		for (String addressee : addressees) {
			for (ObjectId cardId : ids) {
				if (!contains(infoList, cardId, addressee)) {
					infoList.add(new Info(cardId, addressee));
				}
			}
		}
	}

	/**
	 *
	 * @param queryUid ObjectId текущего primary query
	 * @param cardId ObjectId реплицируемой карточки
	 * @return
	 * 	true - если карточку только что пометили для последующей репликации
	 * 	false - если карточка уже была помечена для репликации, повторно не помечаем
	 */
	public static boolean putOnlyCardForReplication(ObjectId queryUid, ObjectId cardId) {
		List<Info> infoList = activeReplMap.get(queryUid);

		if (containsId(infoList, cardId)) {
			return false;
		} else {
			infoList.add(new Info(cardId));
			return true;
		}
	}

	public static boolean markCardForCopyFromLocal(ObjectId uid, ObjectId replicatedCardId) {
		List<Info> infoList = activeCopyMap.get(uid);
		for (Info info : infoList) {
			if (info.cardId.equals(replicatedCardId)) {
				return false;
			}
		}
		infoList.add(new Info(replicatedCardId));
		return true;
	}

	public static boolean isCardReplicationActive(ObjectId queryUid, ObjectId cardId, String addressee) {
		List<Info> infoList = activeReplMap.get(queryUid);
		for (Info info : infoList) {
			if (info.cardId.equals(cardId) && info.addressee != null && info.addressee.equals(addressee)) {
				return !info.replicated;
			}
		}
		return false;
	}

	public static void markCardReplicated(ObjectId queryUid, ObjectId cardId, String addressee) {
		List<Info> infoList = activeReplMap.get(queryUid);
		for (Info info : infoList) {
			if (info.cardId.equals(cardId) && info.addressee != null && info.addressee.equals(addressee)) {
				info.replicated = true;
				break;
			}
		}
	}

	private static boolean contains(List<Info> list, ObjectId id, String addressee) {
		for (Info inf : list) {
			if (inf.cardId.equals(id) && inf.addressee != null && inf.addressee.equals(addressee)) {
				return true;
			}
			if (inf.cardId.equals(id) && inf.addressee == null) {
				inf.addressee = addressee;
				return true;
			}
		}
		return false;
	}

	private static boolean containsId(List<Info> list, ObjectId id) {
		for (Info inf : list) {
			if (inf.cardId.equals(id)) {
				return true;
			}
		}
		return false;
	}

	public static boolean isAllProcessed(ObjectId queryUid) {
		List<Info> infoList = activeReplMap.get(queryUid);
		for (Info info : infoList) {
			if (!info.replicated) {
				return false;
			}
		}
		return true;
	}
}
