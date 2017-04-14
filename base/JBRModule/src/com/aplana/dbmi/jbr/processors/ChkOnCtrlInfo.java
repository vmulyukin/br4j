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
package com.aplana.dbmi.jbr.processors;

import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Set;

import com.aplana.dbmi.jbr.util.IdUtils;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.DateAttribute;
import com.aplana.dbmi.model.ListAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.PersonAttribute;
import com.aplana.dbmi.model.ReferenceValue;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.Validator;

/**
 * @author RAbdullin
 * 
 * (2010/09/10) ��������� ��� �������� �������� ��������� ��������.
 * JBOSS00000739: ������������� �������� �������� � ���������� ����� ��������� 
 * � ���� � ��������� � � ���������.
 * 		���� � ��� ��������� ��� � ��� ��������� (��, ���, ��, ��, ��) ������������� 
 * ������� �������������, �� ���� ����� � ���������� ������ ���� ������������� 
 * ��� ����������. ������, ���� ���������� � ��������� ������ ����������� 
 * ������������� ��������� �� �������� ��� (��. �. 1.4). ���� �������� ���� 
 * ���, �� ����������� ����� ������� ������������. ���� ������� �������� 
 * ���������, �� ���� ��������� ������ ���������.
 * 
 */
@SuppressWarnings("synthetic-access")
public class ChkOnCtrlInfo extends ProcessCard implements Validator {

	private static final String PARAM_ATTR_ONCTRL = "attr_OnCtrl"; // "�� ��������"
	private static final String PARAM_VALID_YES = "refId.ctrlIsYes"; // value ref ""Yes" == "�� ��������"

	private static final String PARAM_ATTR_TERM_DATE= "attr_TermDate"; // "����"
	private static final String PARAM_ATTR_CTRLBY = "attr_CtrlBy"; // "���������"

	private static final String PARAM_FORCESAVE = "forceSave";

	/**
	 * ������� ��� ���������-��������� "���������": 
	 * 		224	"��������"
	 * 		764	"���"
	 * 		784	"���������� ��������"
	 * 		864	"��������� �������"
	 * 		865	"�������������� ������"
	 */

	/* 
	 * � �������� ���������-���������:: 
	 * 'JBR_IMPL_ONCONT'/L/"�� ��������" 
	 * listattribute.jbr.incoming.oncontrol=JBR_IMPL_ONCONT
	 * 		referencevalue.jbr.incoming.control.yes=1432
	 * 		referencevalue.jbr.incoming.control.no=1433
	 * 
	 * "���������"(324)::"��������" � ��� �������� "��������"
	 * 324	���������: "�� ��������"/L	'JBR_TCON_ONCONT'
	 * listattribute.jbr.oncontrol=JBR_TCON_ONCONT
	 * 		referencevalue.jbr.commission.control.yes=1449
	 * 		referencevalue.jbr.commission.control.no=1450
	 */
	private ObjectId attrId_OnCont = 
		IdUtils.smartMakeAttrId( "jbr.incoming.oncontrol", ListAttribute.class);
	private ObjectId valId_Yes = 
		IdUtils.smartMakeAttrId( "jbr.incoming.control.yes", ReferenceValue.class); 

	/**
	 * � �������� ���������-���������:: 
	 * 	'JBR_IMPL_DEADLINE'/ "���� ���������� ��� �������������� ������"/D 
	 * 
	 * ��� "���������" (324): 'JBR_TCON_TERM'/D/"����"
	 * 		dateattribute.jbr.resolutionTerm=JBR_TCON_TERM
	 */
	private ObjectId attrId_TermDate = 
			IdUtils.smartMakeAttrId( "JBR_IMPL_DEADLINE", DateAttribute.class);

	/**
	 * � �������� ���������-���������:: 
	 * 	"��������� �� ���������"/U/'JBR_IMPL_INSPECTOR'
	 *	personattribute.jbr.incoming.inspector=JBR_IMPL_INSPECTOR
	 *
	 *	"���������"/324:	'JBR_TCON_INSPECTOR'/U/"���������"
	 *	personattribute.jbr.commission.inspector=JBR_TCON_INSPECTOR
	 *
	 */
	private ObjectId attrId_Inspector =
			IdUtils.smartMakeAttrId( "jbr.incoming.inspector", PersonAttribute.class);

	@Override
	public Object process() throws DataException {

		final Card card = super.getCard();
		if (card == null) {
			logger.warn( "Card is null -> exiting");
			return null;
		}

		final ListAttribute onControl = (ListAttribute) card.getAttributeById(attrId_OnCont);
		if (onControl == null) {
			logger.warn( MessageFormat.format( MSG_CARD_0_HAS_NO_ATTRIBUTE_1 + " -> exiting",
					card.getId(), attrId_OnCont));
			return null;
		}

		final Attribute dateAttr = card.getAttributeById(attrId_TermDate);
		final PersonAttribute ctrlByAttr = (PersonAttribute) card.getAttributeById(attrId_Inspector);

		final boolean isOnControl = (onControl.getValue() != null) 
				&&	(onControl.getValue().getId() != null)
				&&	(onControl.getValue().getId().equals(valId_Yes));
		boolean needSave = false;
		if (isOnControl) {
			logger.debug( "Card "+ card.getId() + " is under control -> check filling attributes...");
			// action.state.attrmandatory=������������ �������������� {0} �� ���������
			if (dateAttr.isEmpty()) {
				logger.debug( "Card "+ card.getId() + " has empty attribute '"+ this.attrId_TermDate + "' -> throwing exception");
				throw new DataException("action.state.attrmandatory", new Object[]{dateAttr.getName()});
			}
			if (ctrlByAttr.isEmpty()) {
				logger.debug( "Card "+ card.getId() + " has empty attribute '"+ this.attrId_Inspector + "' -> throwing exception");
				throw new DataException("action.state.attrmandatory", new Object[]{ctrlByAttr.getName()});
			}
		} else { // ��� �������� ��������...
			/*
			logger.debug( "Card "+ card.getId() + " is not under control -> cleaning attribute <controlledBy> inside '"
				+ this.attrId_Inspector + "'");
			if (ctrlByAttr == null || ctrlByAttr.isEmpty()) {
				logger.debug( "Card "+ card.getId() + " has already empty attribute '"+ this.attrId_Inspector+ "' -> nothing to update");
			} else {
				logger.warn( "Card "+ card.getId() + " has NON-empty attribute '"+ this.attrId_Inspector+ "' -> blanking it");
				ctrlByAttr.setValues(null);
				needSave = true;
			}
			*/

			// �� ��������� �����������, ��� ���������� �������� �������� �� ������ ������� "����"
			logger.debug("Card " + card.getId() + " is not under control, but there is attribute <controlDate> inside '" + this.attrId_TermDate + "'");
			
			/*
			logger.debug("Card " + card.getId() + " is not under control -> cleaning attribute <controlDate> inside '" + this.attrId_TermDate + "'");
			if (dateAttr == null || dateAttr.isEmpty()) {
				logger.debug("Card " + card.getId() + " has already empty attribute '" + this.attrId_TermDate + "' -> nothing to update");
			} else {
				logger.warn("Card " + card.getId() + " has NON-empty attribute '" + this.attrId_TermDate + "' -> blanking it");
				// ((DateAttribute) dateAttr).clear();
				dateAttr.clear();
				needSave = true;
			}
			*/
		}

		if (needSave){
			if (!super.getBooleanParameter(PARAM_FORCESAVE, false)) {
				logger.warn( "Updated card not saved due to parameter '"+ PARAM_FORCESAVE+ "' is false");
			} else {
				logger.debug( "Saving updated card "+ card.getId() + " ...");
				super.saveCard(card, getSystemUser());
				reloadCard();
			}
		}

		logger.debug( "Card "+ card.getId() + " checked SUCCESSFULLY");

		return null;
	}


	private static final Set<String> PARAM_NAMES = new HashSet<String>(5);
	{
		PARAM_NAMES.add(PARAM_ATTR_ONCTRL.toLowerCase());
		PARAM_NAMES.add(PARAM_VALID_YES.toLowerCase());
		PARAM_NAMES.add(PARAM_ATTR_TERM_DATE.toLowerCase());
		PARAM_NAMES.add(PARAM_ATTR_CTRLBY.toLowerCase());
		PARAM_NAMES.add(PARAM_FORCESAVE.toLowerCase());
	}

	/* (non-Javadoc)
	 * @see com.aplana.dbmi.jbr.processors.ProcessCard#setParameter(java.lang.String, java.lang.String)
	 */
	@Override
	public void setParameter(String name, String value) 
	{
		if (name == null) return;

		if (!PARAM_NAMES.contains(name.toLowerCase()))
			logger.warn("unknown parameter: '"+name+"'='"+ value+ "'");

		if (PARAM_ATTR_ONCTRL.equalsIgnoreCase(name)) {
			this.attrId_OnCont = 
				IdUtils.smartMakeAttrId( value, ListAttribute.class);
		} else if (PARAM_VALID_YES.equalsIgnoreCase(name)) {
			this.valId_Yes =
				IdUtils.smartMakeAttrId( value, ReferenceValue.class); 
		} else	if (PARAM_ATTR_TERM_DATE.equalsIgnoreCase(name)) {
			this.attrId_TermDate = 
				IdUtils.smartMakeAttrId( value, DateAttribute.class);
		} else if (PARAM_ATTR_CTRLBY.equalsIgnoreCase(name)) {
			this.attrId_Inspector = 
				IdUtils.smartMakeAttrId( value, PersonAttribute.class);
		};

		super.setParameter(name, value);
	}

}
