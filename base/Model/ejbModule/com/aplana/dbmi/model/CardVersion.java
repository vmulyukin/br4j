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
package com.aplana.dbmi.model;

import java.io.Serializable;
import java.util.Date;

/**
 * This class represents information of previous editions of {@link Card} object
 * It stores all information found in {@link Card} supplemeted with (@link #getVersion() version number})
 */
public class CardVersion extends Card
{
	private static final long serialVersionUID = 1L;
	private Date versionDate;

	/**
	 * Always throws IllegalArgumentException to prevent from using this method on CardVersion instances
	 */
	public void setId(long id) {
		throw new IllegalArgumentException("Card version id consists from two numbers");
	}

	/**
	 * Sets CardVersion identifier
	 * @param id identifier of {@link Card} object
	 * @param version version number
	 */
	public void setId(long id, int version) {
		super.setId(new ObjectId(CardVersion.class, new CompositeId(id, version)));
	}

	/**
	 * Sets CardVersion identifier
	 * @param id identifier of {@link Card} object
	 * @param version version number
	 * @throws IllegalArgumentException if id is not a rteference to {@link Card} instance
	 */	
	public void setId(ObjectId id, int version) {
		if (Card.class.equals(id.getType()))
			throw new IllegalArgumentException("Not a card id");
		this.setId(((Long) id.getId()).longValue(), version);
	}

	/**
	 * Sets CardVersion identifier. As argument accepts String representation of {@link CardVersion.CompositeId} 
	 * @param id String representation of CardVersion id in form of : '<CARD_ID>:<VERSION>'
	 * @throws IllegalArgumentException in case of wrong string format 
	 */
	public void setId(String id) {
		String[] ids = id.split(":");
		if (ids.length != 2)
			throw new IllegalArgumentException("Not a card id");
		this.setId(Long.parseLong(ids[0]), Integer.parseInt(ids[1]));
	}

	/**
	 * Gets identifier of corresponding {@link Card} object
	 * @return identifier of corresponding {@link Card} object
	 */
	public long getCardId() {
		return ((CompositeId) getId().getId()).card;
	}

	/**
	 * Gets version number
	 * @return version number
	 */
	public int getVersion() {
		return ((CompositeId) getId().getId()).version;
	}
	
	/**
	 * Gets date of card's edition presented by this CardVersion instance
	 * @return date of card's edition presented by this CardVersion instance
	 */
	public Date getVersionDate() {
		return versionDate;
	}
	
	/**
	 * Sets date of card's edition presented by this CardVersion instance
	 * @param versionDate date of card edition
	 */
	public void setVersionDate(Date versionDate) {
		this.versionDate = versionDate;
	}
	
	/**
	 * Class used to present composite key in CARD_VERSION table
	 */
	public static class CompositeId implements Serializable
	{
		private static final long serialVersionUID = CardVersion.serialVersionUID;
		private long card;
		private int version;
		
		/**
		 * Creates CompositeId with given card id and version number
		 * @param card identifier of {@link Card} object
		 * @param version version number
		 */
		public CompositeId(long card, int version) {
			this.card = card;
			this.version = version;
		}

		/**
		 * Gets identifier of {@link Card} object
		 * @return identifier of {@link Card} object
		 */
		public long getCard() {
			return card;
		}

		/**
		 * Gets version number
		 * @return version number
		 */
		public int getVersion() {
			return version;
		}

		/**
		 * Creates string representation of CompositeId.
		 * This value could be used passed to {@link CardVersion#setId(String)} method
		 * @return string representation of CompositeId
		 */
		public String toString() {
			return "" + card + ":" + version;
		}
	}
}
