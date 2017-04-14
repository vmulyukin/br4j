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
package com.aplana.dbmi.jbr.processors;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;

import com.aplana.dbmi.action.ListProject;
import com.aplana.dbmi.jbr.util.CardUtils;
import com.aplana.dbmi.jbr.util.IdUtils;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.BackLinkAttribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.CardState;
import com.aplana.dbmi.model.DataObject;
import com.aplana.dbmi.model.DateAttribute;
import com.aplana.dbmi.model.ListAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.model.PersonAttribute;
import com.aplana.dbmi.model.ReferenceValue;
import com.aplana.dbmi.model.Role;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.service.DataException;

/**
 * (2010/09/24)
 * ���� � ��������� ���� ���� ���� ����������� ����� ��������� ������� ������
 * (������ ��������� ��������� ������ ��� �������), �� ������� ��������, ���� �
 * ���������� (����) ���������� � ��������-���������. ���� ����������� �������
 * ��������� ������� ������ ���������, �� ���� ����� ���������� � ���� ���������,
 * � ����������� � �������� ���������� ����.
 * ����: ��������� = ���������.
 * ��������:
 *   �����������, ��� ������ ��������� ����� ������ ��� �������� ���������/324 
 * (���������). � ���� �� B-������/'JBR_DOCB_BYDOC' ����� ������� �������� ���������.
 *   �����������, ��� ������ ��������� ������� ������ (�.�. ���������������
 * ��������� � C-������ ��������� /'JBR_IMPL_RESOLUT' ���-���������).
 *   ����� ���� ��������� �� (� �� ������ ������ ��������) ����� �������� ������
 * �� ��� �� �������� � ������������, �������� ���� ���� �� ��������� ������ 
 * (JBR_IMPL_RESOLUT). ���� ���� ������ ��������� ���� - ��������-��������� 
 * ������� � ��������, ����� ������������: max(����) � �������� ���� �����������. 
 * @author RAbdullin
 */
public class CopyAttributeResolution2MainDoc 
		extends ProcessCard 
{

	/**
	 * �������� ���������� � �������� ����������. 
	 */
	final static String PARAM_PREFIX_STATUS_TEMPLATE = "statuses.maindoc.template.".toLowerCase();
		final static String PARAMVAL_STATUS_TEMPLATE_DEFAULT = "default".toLowerCase();

	final static String PARAM_CURCARD_LINKTOMAINDOC = "attrId.curcard.linkToMainDoc";
	final static String PARAM_CURCARD_SIGNER = "attrId.curcard.signer";
	final static String PARAM_CURCARD_ROLES_SIGNER = "roles.signer";
	final static String PARAM_CURCARD_ONCTRL = "attrId.curcard.onControl";
	// final static String PARAM_CURCARD_ = ;

	final static String PARAM_MAINDOC_RESOLUTIONS = "attrId.mainDoc.resolutions";
	// final static String PARAM_MAINDOC_ = ;

	/**
	 * B-������� ��������� (324) �� ������� �� ���-��������� ('JBR_DOCB_BYDOC').
	 *  cardlinkattribute.jbr.main.doc=JBR_MAINDOC
	 */
	private ObjectId attrId_ResolutionsCardLinkToMainDoc = 
		IdUtils.smartMakeAttrId( "jbr.main.doc", CardLinkAttribute.class); // "attrId.curcard.linkToMainDoc"

	/**
	 * B-������� ���-��������� � ����������� ������� ������ ('JBR_IMPL_RESOLUT').
	 * backlinkattribute.jbr.resolutions=JBR_IMPL_RESOLUT 
	 */
	private ObjectId attrId_MainDocResolutions = 
		IdUtils.smartMakeAttrId( "jbr.resolutions", BackLinkAttribute.class); // "attrId.mainDoc.resolutions"


	/**
	 * 324	���������: "��� ����������"/U 'JBR_INFD_SIGNATORY'
	 * personattribute.jbr.outcoming.signatory=JBR_INFD_SIGNATORY
	 * 
	 * 		'JBR_INFD_SGNEX_LINK'/C/"���������, ������� � �������� ����, ������������ ���������"
	 * cardlinkattribute.jbr.resolution.FioSign=JBR_INFD_SGNEX_LINK
	 * 
	 */
	// "attrId.curcard.signer"
	private ObjectId attrId_Signer = 
			IdUtils.smartMakeAttrId( "jbr.resolution.FioSign", PersonAttribute.class); // �.�. C ��� U ������� ! 

	/**
	 * ����������� ���� ����������� ���������, ������� ������ � ����������� � ������.
	 */
	private Set<ObjectId> signerRoles = // "roles.signer"
			new HashSet<ObjectId>(
					IdUtils.stringToAttrIds( "JBR_MINISTER;JBR_MINISTER_HELPER", Role.class)
				);

	/**
	 * "���������"(324)::"��������" � ��� �������� "��������"
	 * 324	���������: "�� ��������"/L	'JBR_TCON_ONCONT'
	 * listattribute.jbr.oncontrol=JBR_TCON_ONCONT
	 * 		referencevalue.jbr.commission.control.yes=1449
	 * 		referencevalue.jbr.commission.control.no=1450
	 */
	// private static String attr_Check = "jbr.incoming.oncontrol=jbr.commission.control.yes";
	private ObjectId attrId_onCont = 
		IdUtils.smartMakeAttrId( "jbr.oncontrol", ListAttribute.class); // "attrId.curcard.onControl" 
	private ObjectId valId_Yes = 
		IdUtils.smartMakeAttrId( "jbr.commission.control.yes", ReferenceValue.class); // "refId.curcard.CtrlIsYes"
	private ObjectId valId_No = 
		IdUtils.smartMakeAttrId( "jbr.commission.control.no", ReferenceValue.class); // "refId.curcard.CtrlIsNo"

	/**
	 * "���������"(324):: "����"/D 'JBR_TCON_TERM'
	 * dateattribute.jbr.resolutionTerm=JBR_TCON_TERM
	 */
	private ObjectId attrId_Date =
			IdUtils.smartMakeAttrId( "jbr.resolutionTerm", DateAttribute.class); // "attrId.curcard.termDate"

	/**
	 * "���������"(324): "���������"/U	'JBR_TCON_INSPECTOR'
	 * personattribute.jbr.commission.inspector=JBR_TCON_INSPECTOR
	 */
	private ObjectId attrId_ConrtolledBy =
			IdUtils.smartMakeAttrId( "jbr.commission.inspector", PersonAttribute.class); // "attrId.curcard.inspector"

	/**
	 * ������� ��� ���������-��������� "���������": 
	 * 		224	"��������"
	 * 		764	"���"
	 * 		784	"���������� ��������"
	 * 		864	"��������� �������"
	 * 		865	"�������������� ������"
	 */

	/**
	 * � �������� ���������-���������:: 
	 * 	'JBR_IMPL_DEADLINE'/ "���� ���������� ��� �������������� ������"/D
	 *  
	 * dateattribute.jbr.resolutionTerm=JBR_TCON_TERM
	 */
	private ObjectId attrId_MainDocTermDate =
			IdUtils.smartMakeAttrId( "JBR_IMPL_DEADLINE", DateAttribute.class);  // "attrId.mainDoc.termDate"

	/**
	 * � �������� ���������-���������:: 
	 * 	"��������� �� ���������"/U/'JBR_IMPL_INSPECTOR'
	 * personattribute.jbr.incoming.inspector=JBR_IMPL_INSPECTOR
	 */
	private ObjectId attrId_MainDocInspector =
			IdUtils.smartMakeAttrId( "jbr.incoming.inspector", PersonAttribute.class); // "attrId.mainDoc.inspector"

	/* 
	 * � �������� ���������-���������:: 
	 * 'JBR_IMPL_ONCONT'/L/"�� ��������" 
	 * listattribute.jbr.incoming.oncontrol=JBR_IMPL_ONCONT
	 * 		referencevalue.jbr.incoming.control.yes=1432
	 * 		referencevalue.jbr.incoming.control.no=1433
	 */
	private ObjectId attrId_MainDocOnCont = 
		IdUtils.smartMakeAttrId( "jbr.incoming.oncontrol", ListAttribute.class); // "attrId.mainDoc.onControl" 
		private ObjectId valId_MainDocYes = 
			IdUtils.smartMakeAttrId( "jbr.incoming.control.yes", ReferenceValue.class); // "refId.mainDoc.CtrlIses" 
		private ObjectId valId_MainDocNo = 
			IdUtils.smartMakeAttrId( "jbr.incoming.control.no", ReferenceValue.class); // "refId.mainDoc.CtrlIsNo"


	/**
	 * ����� ������ -> ������ ���������.
	 * �������� ����=null ����-�� �������� "��-���������"
	 */
	final Map<ObjectId, Set<ObjectId>> statusByTemplates = 
			new HashMap<ObjectId, Set<ObjectId>>(3); 

	/* (non-Javadoc)
	 * @see com.aplana.dbmi.jbr.processors.ProcessCard#setParameter(java.lang.String, java.lang.String)
	 */
	@Override
	public void setParameter(String name, String value) {
		if (name == null || value == null) return;

		super.setParameter(name, value);

		/* (?) ������ � ����������� ��������� ...
		 */
		if ( name.toLowerCase().startsWith(PARAM_PREFIX_STATUS_TEMPLATE)) {

			// �������� ��������� �� ��������� ��� ������� ABC ����: 
			// "status.template.ABC"
			final String templateName = name.substring(PARAM_PREFIX_STATUS_TEMPLATE.length());
			final List<ObjectId> statuses = IdUtils.stringToAttrIds( value, CardState.class);

			final ObjectId templateId = 
				(PARAMVAL_STATUS_TEMPLATE_DEFAULT.equalsIgnoreCase(templateName))
						? null
						: ObjectIdUtils.getObjectId( Template.class, templateName, true); 
			statusByTemplates.put( templateId, 
					(statuses == null) ? null :new HashSet<ObjectId>(statuses) );
		}
		
	}

	/**
	 * ��������� ����� �� ������������ �������� � ������� ���������.
	 * ������������ ������ � statusByTemplate. 
	 * �������� ������������ ������, ���� ��������� �������� � �� ������ ���� 
	 * � statusByTemplate.
	 * (!) �.�. ���� ������ �������� �� ���������� � ������ ������� �������� 
	 * (���� ��������� ��� ������� ��-���������), �� ��������� FALSE. 
	 * @param card
	 * @return true, ���� �������� "��������", fakse �����. 
	 */
	private boolean chkCardStatus(Card card) {
		if (card == null)
			return false;
		// ���� ������ �� ����� ���� � statusByTemplate, 
		// �� ���������� default-���� (null)
		final Set<ObjectId> statuses =
			statusByTemplates.containsKey(card.getTemplate())
					? statusByTemplates.get(card.getTemplate())
					: statusByTemplates.get(null);
		return (statuses == null) ? false : statuses.contains(card.getState()); 
	}

	@Override
	public Object process() throws DataException {

		/** �������� ������� ��������
		 */
		final Card card = super.getCard();
		if (card == null) {
			logger.warn("card is NULL -> exiting");
			return null;
		}

		final ObjectId cardId = card.getId();
		if (cardId == null) {
			logger.warn("cardId is NULL -> exiting");
			return null;
		}

		/** ��������� ���������-���������
		 */
		final List<Card> mainDocs = super.loadAllLinkedCardsByAttr(cardId, card.getAttributeById(attrId_ResolutionsCardLinkToMainDoc));
		final Card mainDoc = (mainDocs == null || mainDocs.isEmpty()) ? null : mainDocs.get(0);
		if (mainDoc == null) {
			logger.warn("Main document for card "+ cardId.getId() +" via attribute '"+ attrId_ResolutionsCardLinkToMainDoc+ "' not exists -> exiting");
			return null;
		}
		logger.debug("Main document for card "+ cardId.getId() +" via attribute '"+ attrId_ResolutionsCardLinkToMainDoc + "' is " + mainDoc.getId().getId() );

		/**
		 * ��������� ���������-��������� ���������?
		 */
		if (!chkCardStatus(mainDoc)){
			logger.warn("Main document "+ mainDoc.getId().getId()+ " (template "
					+ mainDoc.getTemplate().getId() + ") is at unchecked state "
					+ mainDoc.getState().getId() + " -> exiting");
			return null;
		}


		/** ��������� ������� ����������� ������ ��������� (�.�. ������)...
		 *  ����� �������� �������� �� ��������, ������� ��� �������� ���� ����� � ������
		 */
		if(mainDoc.getId() == null) {
			logger.error( "Main document not contains ID");
			return null;
		}
		ListProject lp = new ListProject(mainDoc.getId());
		lp.setAttribute(attrId_MainDocResolutions);
		
		final List<Card> cards = CardUtils.execSearchCards(lp, getQueryFactory(), getDatabase(), getUser());
		final Collection<ObjectId> cardIds = ObjectIdUtils.getObjectIds(cards);
		
		if (cardIds == null ||
				! cardIds.contains(cardId)) {
			logger.warn( "Document "+cardId.getId()+" is not the first-level resolutuion inside MainDoc "
					+ mainDoc.getId().getId()+ " not exists -> exiting");
			return null;
		}

		/** ������� ������ �� ��������� ��������...
		 */
		//final AttributeSelector ctrlSelect = AttributeSelector.createSelector(attr_Check);

		logger.debug( "Checking card "+ cardId+ " if there are conrtolled resolutions signed by any user with role " + signerRoles);

		/** ������ ���������-��������� �� ���������-��������� ...
		 */
		final List<ResolutionInfo> resolutions = new ArrayList<ResolutionInfo>(5); // ���������
		try {
			/**
			 * �������� ��� ���������/���������: 
			 * 		1) id �������� (id),
			 * 		2) ��������� �������� (listValue),
			 * 		3) ���� (date),
			 * 		4) userId ����������,
			 * 		5) userId ����������.
			 */ 
			final String sql = 
					"SELECT avSigner.card_id, avOnCtrl.value_id, avTime.date_value, avCtrlBy.number_value, prsn.person_id \n" +
					"FROM attribute_value avMainDocResol \n" +
					"	JOIN attribute_value avSigner on avSigner.card_id = avMainDocResol.number_value \n" +
					"		JOIN attribute aSigner on aSigner.attribute_code=avSigner.attribute_code \n" +

					// -- ��� ����������: ���� User-������� ���� cardLink-������� (�� ������������)
					"		JOIN person prsn \n" +
					"			on ( (aSigner.data_type='U') and (prsn.person_id = avSigner.number_value) ) \n" +
					"			or ( (aSigner.data_type='C') and (prsn.card_id = avSigner.number_value) )\n" +
					"		JOIN person_role prole on prole.person_id=prsn.person_id \n" +

					// "�� ��������" ����� ���� �����, ��������� - ��� ��������.
					"	JOIN attribute_value avOnCtrl on avOnCtrl.card_id = avSigner.card_id \n" +
					// "	JOIN values_list valist on valist.value_id=avOnCtrl.value_id \n"+
					"	LEFT JOIN attribute_value avTime on avTime.card_id = avSigner.card_id \n" +
					"	LEFT JOIN attribute_value avCtrlBy on avCtrlBy.card_id = avSigner.card_id \n" +

					"WHERE avMainDocResol.card_id = ? \n" +
					"	AND avMainDocResol.attribute_code = ? \n" +

					"	AND avSigner.attribute_code = ? \n" +
					// "	AND p.person_login in ("+ makeLoginList(logins_SignedBy) +") \n" + // ��� ���������
					"	AND prole.role_code in ("+ IdUtils.makeIdCodesQuotedEnum(signerRoles) +") \n" + // ���� ����������

				"	AND avOnCtrl.attribute_code = ? \n" +
					"	AND avOnCtrl.value_id = ? \n" +

					"	AND avTime.attribute_code = ? \n"+
					"	AND avCtrlBy.attribute_code = ? \n" +

					"ORDER BY avTime.date_value DESC \n" // ����� �� �������� ���� ���������
				;
			getJdbcTemplate().query( sql,
					new Object[] { 
							mainDoc.getId().getId(), 
							attrId_MainDocResolutions.getId(), // ��������� ������� ������ �������� ���������

							attrId_Signer.getId(),				// ���������

							// ctrlSelect.getAttrId().getId(), 	// "�� ��������"
							// ctrlSelect.getValue2Compare(), 	// = id([YES])
							attrId_onCont.getId(),
							valId_Yes.getId(),

							attrId_Date.getId(),				// ��� "����"
							attrId_ConrtolledBy.getId()			// ��� "���������"
						},
					new int[] { 
							Types.NUMERIC, 
							Types.VARCHAR,

							Types.VARCHAR,

							Types.VARCHAR,
							Types.NUMERIC, 

							Types.VARCHAR,
							Types.VARCHAR
						},

						new RowMapper() {
							public Object mapRow(ResultSet rs, int rowNum)
								throws SQLException 
							{
								final ResolutionInfo resol = new ResolutionInfo();
								resol.cardId = rs.getLong(1);
								resol.ctrlValue = rs.getLong(2);
								resol.dateTCON = rs.getDate(3);
								resol.userCtrllById = rs.getLong(4);
								resolutions.add(resol);
								return resol;
							}
						}
				);
		} catch (EmptyResultDataAccessException e) {
		}
		logger.debug("found " + resolutions.size() + " resolutions inside card "+ mainDoc.getId().getId());

		/**
		 * ������� ��������� � ���������-���������...
		 */
//		CardUtils.dropAttributes( getJdbcTemplate(), 
//				new Object[] { 
//					attrId_MainDocTermDate.getId(), 
//					attrId_MainDocInspector.getId(),
//					ctrlSelect.getAttrId().getId()
//				}, 
//				mainDoc.getId());
		Date outCtrlDate = null;
		ObjectId outOnCtrlValueId = valId_No;
		List<ObjectId> outInspectors = null;
		if (resolutions.isEmpty()) {
			/** ������� � ��������, ���� ��� �� ����� ���������� ���������.
			 */
			logger.debug( "No controlled resolutions indide card -> clear MainDoc control attributes...");
		} else {
			/**
			 * ���������� �� ��������...
			 */
			outCtrlDate = resolutions.get(0).dateTCON; // ���� = ����� ������� �� ���� ����������� ���������
			outOnCtrlValueId = valId_Yes; 	// �� �������� = ��

			// ������ �����������
			outInspectors = new ArrayList<ObjectId>( resolutions.size());
			for (ResolutionInfo resolutionInfo : resolutions) {
				outInspectors.add( new ObjectId(Person.class, resolutionInfo.userCtrllById) );
			}
		}

		/** ���������� ������ � ���������-���������...
		 */
		// TODO: ���� ������ ���������� � �� cardlink � �� person (!), ���� ������������ ���-���� ������������.
		final DateAttribute dateAttr = (DateAttribute) getAttribute( mainDoc, attrId_MainDocTermDate);
		if (dateAttr != null)
			dateAttr.setValue(outCtrlDate);

		final ListAttribute onCtrlAttr = (ListAttribute) getAttribute( mainDoc, attrId_MainDocOnCont);
		if (onCtrlAttr != null)
			onCtrlAttr.setValue( mkRefValue(outOnCtrlValueId));

		final PersonAttribute inspectAttr = (PersonAttribute) 
			getAttribute( mainDoc, attrId_MainDocInspector);
		if (inspectAttr != null) {
			if (outInspectors != null && !outInspectors.isEmpty() ) {
				final List<Person> persons = new ArrayList<Person>(outInspectors.size());
				for (ObjectId objectId : outInspectors) {
					final Person p = (Person) DataObject.createFromId(objectId);
					persons.add(p);
				}
				inspectAttr.setValues( persons);
			} else
				inspectAttr.setValues(null);
		}

		super.saveCard( mainDoc, getSystemUser());
		logger.info("Main document "+ mainDoc.getId().getId() + " updated successfully");

		return null;
	}


	/**
	 * @param outOnCtrlValueId
	 * @return
	 */
	private ReferenceValue mkRefValue(ObjectId valueId) {
		// �������������� � ��/��� �������� ���������:
		final ObjectId outValueId = (valId_Yes.equals(valueId))
				? valId_MainDocYes
				: valId_MainDocNo;
		return (valueId == null) ? null 
					: (ReferenceValue) DataObject.createFromId( outValueId);
	}


	/**
	 * @param dstCard
	 * @param dstAttrId
	 */
	private Attribute getAttribute(Card dstCard, ObjectId dstAttrId) {
		final Attribute attr = dstCard.getAttributeById(dstAttrId);
		if (attr == null) {
			logger.warn( MessageFormat.format(MSG_CARD_0_HAS_NO_ATTRIBUTE_1,
					dstCard.getId(), dstAttrId ));
		}
		return attr;
	}

//	/**
//	 * �������� sql-������ ������� � �������� ����� �������.
//	 * @param logins
//	 * @return
//	 */
//	private String makeLoginList(String[] logins) {
//		if (logins == null || logins.length == 0)
//			return "''";
//		final StringBuffer result = new StringBuffer();
//		for (int i = 0; i < logins.length; i++) {
//			final String item = logins[i];
//			if (item == null) continue;
//			result.append("'").append(item).append("'");
//			final boolean hasNext = i < logins.length - 1;
//			if (hasNext) result.append(',');
//		}
//		return result.toString();
//	}

	/**
	 * ������������ ����� ��� ����������� � ������� �������� �������������.
	 */
	private class ResolutionInfo {
		long cardId;

		long ctrlValue;			// �������� ��������� �������� (id in value_list)
		Date dateTCON;			// ����
		long userCtrllById;		// ��������� (id ������� in person)

		public ResolutionInfo() {
		}
	}
}
