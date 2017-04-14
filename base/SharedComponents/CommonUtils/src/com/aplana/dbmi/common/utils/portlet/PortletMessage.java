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
package com.aplana.dbmi.common.utils.portlet;

import java.util.List;
import java.util.Map;

import com.aplana.dbmi.model.ObjectId;

	public class PortletMessage{
		
		public static final String STYLE_INFO = "msg";
		public static final String STYLE_ERROR = "err_msg";
		public static final String STYLE_EVENT = "evt_msg";

		private String message;
		private List<ObjectId> cardIds;
		private Map<ObjectId, String> container;
		private PortletMessageType messageType;
		
		/**
		 * @author Aleksandr Smirnov
		 * ��� ��������� 
		 */
	    public enum PortletMessageType{
	        ERROR,		// ������
	        INFO,		// ����������� ���������
	        EVENT		// �������� �������
	    }

		public PortletMessage(){
			this.messageType = PortletMessageType.INFO;
		}

		public PortletMessage( String msg){
			this( msg, null, null, PortletMessageType.INFO);
		}

		public PortletMessage(String msg, List<ObjectId> cardIds){
			this( msg, cardIds, null, PortletMessageType.INFO);
		}
		
		public PortletMessage( String msg, PortletMessageType messageType){
			this( msg, null, null, messageType);
		}

		public PortletMessage(String msg, List<ObjectId> cardIds, Map<ObjectId,String> container){
			this( msg, cardIds, null, PortletMessageType.INFO);
		}
		
		public PortletMessage(String msg, List<ObjectId> cardIds, Map<ObjectId,String> container, PortletMessageType messageType){
			this.message = msg;
			this.cardIds = cardIds;
			this.container = container;
			this.messageType = messageType;
		}

		public String getMessageStyle(){
			
			if (this.messageType.equals(PortletMessageType.ERROR)){
				return STYLE_ERROR;
			}else if (this.messageType.equals(PortletMessageType.EVENT)){
				return STYLE_EVENT;
			}
			return STYLE_INFO;
		}
		
		public String getMessage(){
			return this.message;
		}

		public void setMessage(String msg){
			this.message = msg;
		}

		public List<ObjectId> getCardIds(){
			return this.cardIds;
		}

		public void setCardIds(List<ObjectId> cardIds){
			this.cardIds = cardIds;
		}

		public Map<ObjectId, String> getContainer(){
			return this.container;
		}

		public void setContainer( Map<ObjectId,String> container ){
			this.container = container;
		}
		
		public PortletMessageType getMessageType(){
			return this.messageType;
		}
		
		public void setMessageType(PortletMessageType messageType){
			this.messageType = messageType;
		}

	}