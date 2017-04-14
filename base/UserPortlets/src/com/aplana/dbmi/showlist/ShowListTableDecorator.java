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
/*
 * ��������� ������ � ��������:
 * 		1) URL: ���� �������� url � ���� ���������� {0}, ������� ����� ���������� �� cardId;
 * 		2) Page � ����� ����������� ���� pgID:winID:{0} // �������� cardID.
 * 		3) �� ���� �������������� ������: backURL: cardID
 */

package com.aplana.dbmi.showlist;

import com.aplana.dbmi.Portal;
import com.aplana.dbmi.PortletService;
import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.card.CardPortlet;
import com.aplana.dbmi.model.ContextProvider;
import com.aplana.dbmi.model.ObjectId;
import org.displaytag.decorator.TableDecorator;

import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import java.text.MessageFormat;
import java.util.*;

public class ShowListTableDecorator extends TableDecorator {

	final private PortletService service;
	private String backURL;
	final private String emptyValue;
	final private RenderRequest request;
	final private RenderResponse response;

	private String linkPg;
	private String linkUrl;

	final private List<SearchResult.Column> metaDataDesc;
	final private RowExData rowExData;

	public ShowListTableDecorator(RenderRequest request, RenderResponse response) {
	    this.request = request;
	    this.response = response;
		service = Portal.getFactory().getPortletService();
		this.backURL = (String)request.getAttribute(MIShowListPortlet.BACK_URL_ATTR);

		emptyValue = ResourceBundle.getBundle("com.aplana.dbmi.showlist.nl.MIShowListPortletResource",
				request.getLocale()).getString("name.empty");

		metaDataDesc = (List<SearchResult.Column>) request.getAttribute("metaDataDesc");
		rowExData = (RowExData) request.getAttribute("dataColumns");
	}

	public void setLinkPg( String linkPg)
	{
		this.linkPg = linkPg;
	}

	public void setLinkUrl( String linkUrl)
	{
		this.linkUrl = linkUrl;
	}

    public String getCardLink(int i) {
    	final List currentRow = (List) getCurrentRowObject();
        final String linkRowItemID = currentRow.get(0).toString();

        // �������� ��� ��������������� ������...
        String stringValue = emptyValue;
        {	// ������� ��������� ��� �������� columnValue
            final Object columnValue = currentRow.get(i);
            if (columnValue != null) {
                stringValue = columnValue.toString();
                if (stringValue.length() == 0) {
                    stringValue = emptyValue;
                }
            }
        }

        String cardPageId = service.getPageProperty("cardPage", request, response);
        if (cardPageId == null) {
            cardPageId = "dbmi.Card";
        }

        final Map<String, String> params = new HashMap<String, String>();

        /* ��������� ���� ������ */
        String sLink;
        if (this.linkUrl != null) {
        	// ����� url...
        	sLink = MessageFormat.format(this.linkUrl, linkRowItemID);
        } else if (this.linkPg != null) {

        	// ������ ��� id � linkPg � ����:
        	// 		<idPage> ':' <idWin> ':' <idParamName>
        	final StringTokenizer ids = new StringTokenizer( this.linkPg, ":", false);
        	final String idPage = (ids.hasMoreElements()) ? ids.nextToken() : "";
        	final String idWin = (ids.hasMoreElements()) ? ids.nextToken() : "";
        	final String paramName = (ids.hasMoreElements()) ? ids.nextToken() : "";

        	params.put( paramName, linkRowItemID);
        	sLink = service.generateLink( idPage, idWin, params, request, response);
        } else {
        	// ������� ������ � ���������� cardId
        	params.put( CardPortlet.EDIT_CARD_ID_FIELD, linkRowItemID);
    		params.put( CardPortlet.BACK_URL_FIELD, backURL);
        	sLink = service.generateLink(cardPageId, "dbmi.Card.w.Card", params, request, response);
        }

		return "<a href=\"" + sLink + "\">" + stringValue + "</a>";
    }

    public String getIcon(int i) {
    	String tag, card_id, urlRes = null, onClick = null;
    	final List<?> curList = (List) getCurrentRowObject();
		Long value = (Long) curList.get(i);

    	Map<String, String> icon;
    	if (value == null) {
    		icon = metaDataDesc.get(i).getEmptyIcon();
    	} else {
    		Map<String, Map<String, String>> icons = metaDataDesc.get(i).getIcons();
    		if (icons != null && icons.get(value.toString()) != null) {
    			icon = icons.get(value.toString());
    		} else {
    			icon = metaDataDesc.get(i).getDefaultIcon();
    		}

			SearchResult.Action columnAction = metaDataDesc.get(i).getAction();

			if (columnAction!=null && columnAction.getId()!=null){
				if (columnAction.getId().equals("reportDS")) {
					String servletPath = request.getContextPath() + "/CardDSInfoViewer?";
					card_id = "cardId=" + curList.get(0).toString();
					//urlRes = servletPath+"namespace="+columnAction.getParametrs().get("nameConfig")
					//urlRes = servletPath+response.getNamespace() "namespace=jbpns_2fdbmi_2fcard_2fCardPortletWindowsnpbj" + "&" + card_id;
					urlRes = servletPath + "namespace=" + response.getNamespace() + "&" + card_id;
				} else if (columnAction.getId().equals("showAttachments")){
					String servletPath = request.getContextPath() + "/ShowAllAttachments";
					card_id = curList.get(0).toString();
					onClick="showAttachmentsDialog('"+servletPath+"','" + card_id +"')";
				}
			}
		}
		ObjectId attrId = metaDataDesc.get(i).getAttributeId();
		tag=createIconTag(icon, urlRes, attrId, onClick);
		return tag;
	}

	private String createIconTag(Map<String, String> icon, String urlRes, ObjectId attrId, String onClick){
		String htmlAttrId = generateHtmlAttrId(attrId, "icon", "tooltip");
		StringBuilder tag =new StringBuilder();
		if (icon != null) {
			String tooltipRu = icon.get("tooltipRu");
			String tooltipEn = icon.get("tooltipEn");
			String tooltip = ContextProvider.getContext().getLocaleString(tooltipRu, tooltipEn);
			String tooltipStyle = icon.get("tooltipStyle");
			String dialogStyle = icon.get("dialogStyle");
			String image = icon.get("image");
			String href_image="";

			if(tooltipStyle!=null && !tooltipStyle.isEmpty() && Boolean.valueOf(tooltipStyle)){
				String toolTipContent = getContent(attrId);
				if(toolTipContent==null){
					toolTipContent=tooltip;
				}
				tag.append(createTooltip(toolTipContent, htmlAttrId));
				tooltip="";

			} else if(dialogStyle!=null && !dialogStyle.isEmpty() && Boolean.valueOf(dialogStyle)) {
				String dialogContent = getContent(attrId);
				String dialogTitle = icon.get("dialogTitle");
				StringBuilder sb = new StringBuilder();
				sb.append(" href=\"javascript: //\" onclick=\"showDialog('");
				sb.append(dialogContent != null ? dialogContent.replaceAll("\"", "&quot;").replaceAll("'", "&apos;") : "");
				sb.append("','");
				sb.append(dialogTitle != null ? dialogTitle.replaceAll("\"", "&quot;").replaceAll("'", "&apos;") : "");
				sb.append("'); return false\"");
				href_image = sb.toString();
				tooltip=" title=\""+tooltip+"\" ";
			} else {
				tooltip=" title=\""+tooltip+"\" ";
			}

			if (urlRes!=null)
				href_image="class=\"dbmi_linkImage\" href="+urlRes+" target=\"_blank\"";
			
			if (onClick!=null)
				href_image="href=\"#\" onclick=\"" + onClick + "\"";
			
			tag.append("<a id=\"").append(htmlAttrId).append("\" ")
				.append(href_image).append(tooltip).append(" >")
				.append("<span class=\"").append(image).append("\">&nbsp</span> </a>");
		} else {
			tag.append("<span>&nbsp</span>");
		}
		return tag.toString();
	}

	private String getContent(ObjectId attrId) {
		final Long cardId = getCardId();
		return createContent(rowExData.getCellData(cardId, (String)attrId.getId()));
	}

	private String createTooltip(String content, String connectId){
		return "<div dojoType=\"dijit.Tooltip\" connectId=\"" + connectId + "\" position=\"below\" ><div style='text-align:left'>" + content + "</div></div>";
	}

	private String createContent(String colomnData){
		return colomnData;
	}

	private Long getCardId(){
		List currentRow = (List) getCurrentRowObject();
		return (Long) currentRow.get(0);
	}

	private String generateHtmlAttrId(ObjectId attrId, String prefix, String suffix){
		Long id = getCardId();
		return prefix + "_" + id + "_" + attrId.getId() + "_" + suffix;
	}
}
