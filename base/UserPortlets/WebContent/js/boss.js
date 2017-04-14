/*
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
dojo.require('dijit.dijit');
dojo.require("dijit.Tooltip");
dojo.require("dijit.Dialog");
dojo.require("dijit.form.Button");
dojo.require('dijit.form.ComboBox');
dojo.require("dijit.form.FilteringSelect");
dojo.require("dijit.form.TextBox");
dojo.require("dojo.data.ItemFileReadStore");
dojo.require("dojo.date.locale");
dojo.require("dojox.xml.parser");
dojo.require("dojox.json.query");
dojo.require('dbmiCustom.DateTimeWidget');
dojo.require("dojox.grid.EnhancedGrid");        
dojo.require("dojo.data.ItemFileReadStore");
dojo.require("dojox.grid.enhanced.plugins.IndirectSelection");
dojo.require("dojo.date.stamp");

var titles = [];
titles['add'] = "Добавить новый проект резолюции";
titles['del'] = "Переместить проект резолюции в корзину";
titles['edit'] = "Изменить проект резолюции";
//titles['links'] = "Открыть карточку документа";
titles['print'] = "Открыть карточку документа";
titles['document'] = "Открыть документ";
titles['editDocument'] = "Редактировать документ";
titles['editPaperOriginal'] = "Работать с бумажным оригиналом";
titles['list'] = "Список документов";
titles['control'] = "Поставить на личный контроль";
titles['control_remove'] = "Снять с личного контроля";
titles['favorite'] = "Добавить в избранное";
titles['favorite_remove'] = "Удалить из избранного";
titles['download'] = "Загрузить документ";
titles['refresh'] = "Обновить экран";
titles['turnmonitor'] = "Синий интерфейс";
titles['create'] = "Создать новую карточку";
titles['shield'] = "Личный контроль";
titles['incoming'] = "Поступившие";
titles['first'] = "Первая";
titles['prev'] = "Предыдущая";
titles['next'] = "Следующая";
titles['last'] = "Последняя";
titles['approve'] = "Утвердить";
titles['decline'] = "Отклонить";
titles['link_doc'] = "Связи";
titles['report_ds'] = "Проверить ЭП";
titles['deleteBig'] = "Удалить";
titles['createDelegation'] = "Добавить делегирование";

var printForms = [];
printForms['Входящий'] = "reportChartIncoming";
printForms['Исходящий'] = "reportChartOutgoing";
printForms['Внутренний документ'] = "reportChartInside";
printForms['ОРД'] = "reportChartORD";
printForms['Обращения граждан'] = "reportChartOG";
printForms['НПА'] = "reportChartORD";
printForms['Независимое поручение'] = "reportChartIR";

var reportsFolderId = "8570";

var CountersVars = {
	Sync: true,
	Nodes: [],
	timeout: 60000*5
}

var isDragging = false;
var GtableHeight;

var DocsVars = {
	currentDoc: null,
	currentView: null,
	currentArea: null,
	currentDialog: null,
	currentSubcard: null,
	currentNext: null
}

var SortVars = {
	sortColumnId: null,
	sortAttrCode: null,
	sortAsc: null
}
//шаблон выбранного документа
var currTemp;
//id выбранной карточки
var currId;

dojo.addOnLoad(function(){
	setTitles(document.body);

    var menu_li = dojo.query('#menu a, .toolbar_top LI a, a.button.incoming, a.button.shield');
    menu_li.connect("onclick", function(e){
	        lockScreen();
	        dojo.stopEvent(e);
	        document.cookie = "CURRTHCLASS=; -1; path=/;";
	        document.cookie = "CURRSORTORDER=; -1; path=/;";
	        document.cookie = "CURRSORTCODE=; -1; path=/;";
	        location.href = this.href;
    });
    if(!dojo.byId("workstation_advanced_search")){
    	var buttons = dojo.query("div.docDetailsLeftHeader a.document, div.docDetailsLeftHeader a.links, div.docDetailsLeftHeader a.list");
		if(buttons){
			buttons.connect("onclick", function(e){lockScreen();});
		}
	}else{
		var q = dojo.query("A.document");
		if(q && q.length>0) q[0].target = "_blank";
	}

	var q = dojo.query(".button.refresh");
	if(q && q.length>0){
		q[0].onClick = null;
		q[0].onclick = null;
		q[0].href = self.location.href.split("#")[0]
	}


    var currThClassCookieValue = getCookie('CURRTHCLASS');
    if (currThClassCookieValue != '') {
        var currThNode = dojo.query('th.' + currThClassCookieValue);
        if (currThNode && currThNode[0]) {
            var currSortOrderCookieValue = getCookie('CURRSORTORDER');
            var sortArrowSpanClass = eval(currSortOrderCookieValue) ? 'downArrow' : 'upArrow';
            currThNode[0].innerHTML = currThNode[0].innerHTML + "<span class='" + sortArrowSpanClass + "'></span>";
        }
    }
    
    try{//фикс для кнопок нижнего фрейма на меленьких мониторах
    var w = Math.round(dojo.position(dojo.query(".docDetailsLeftHeader")[0]).w);
	if(w <450){
		dojo.byId("docDetailsHeader").style.width = (w - 230) + "px"
	}
	}catch(ex){}
	
    var isDragging = false;	
    checkAppletExists();
    checkLayoutSize();
    setBottomFrameHeight();

    var docBtn = dojo.query(".document");
    dojo.connect(docBtn, "onclick", function(ev){
    	currTemp = getSelectedTemplateName();
    	currId = getSelectedCardId();
    });
    openTargetDoc();
    addGroupExecutionButton();
    addGroupResolutionButton();
})

// set bottom frame height on window resize
dojo.connect ( window, 'onresize', function(e){
    setBottomFrameHeight();
});

function checkLayoutSize() {
    var body = dojo.query('body');
    dojo.style(body[0], {"overflow-y":"hidden"});
    dojo.style(body[0], {"min-width":"1024px"});

    var storedHeightValue = getCookie('TABLEHEIGHT');
    if (storedHeightValue != '' ) {
        var topFrameDiv = getTopFrameSizableDiv();

        if ((!topFrameDiv) || (topFrameDiv.length==0))
        	return;

        var tableHeight = storedHeightValue;
        GtableHeight = tableHeight;
        topFrameDiv[0].style.height = tableHeight + 'px';
        var tbody = dojo.query('tbody', topFrameDiv[0]);
        if(tbody && tbody.length > 0){
            tbody[0].style.height = tableHeight - 41 + 'px';
        }
    }

    setDocFrameHeight();
}

function setDocFrameHeight() {
    var resizer = dojo.query('.toolbar_top')[0];
    var top_area = dojo.query('.content_lr_border')[0];

    var bottomArea = dojo.byId('DocumentDetails');
    if(!bottomArea && dojo.byId('workstation_advanced_search')) {
        var topFrameDiv = dojo.query('.main_table');
        if(topFrameDiv && topFrameDiv[0]) {
            bottomArea = dojo.byId('workstation_advanced_search');
        }
    }

    if(!bottomArea) {
        return;
    }

    disableDraggingFor(resizer[0]);
    dojo.connect(resizer,"mousedown", dragDown);
    dojo.connect(top_area,"mouseup", releaseDrag);
    dojo.connect(bottomArea,"mouseup", releaseDrag);
    dojo.connect(top_area,"mousemove", dragMoveUp);
    dojo.connect(bottomArea,"mousemove", dragMoveDown);

}


function dragMoveUp(e) {
    if (isDragging == true) {
        var topFrameDiv = getTopFrameSizableDiv();
        var tableHeight = e.clientY - 127;
        GtableHeight = tableHeight;
        topFrameDiv[0].style.height = tableHeight + 'px';
        var tbody = dojo.query('tbody', topFrameDiv[0]);
        if(tbody && tbody.length > 0){
            tbody[0].style.height = tableHeight - 41 + 'px';
        }
        setBottomFrameHeight();
    }
}
function dragMoveDown(e) {
    if (isDragging == true) {
        var topFrameDiv = getTopFrameSizableDiv();
        var tableHeight = e.clientY - 150;
        GtableHeight = tableHeight;
        topFrameDiv[0].style.height = tableHeight + 'px';
        var tbody = dojo.query('tbody', topFrameDiv[0]);
        if(tbody && tbody.length > 0){
            tbody[0].style.height = tableHeight - 41 + 'px';
        }
        setBottomFrameHeight();
    }
}

function getTopFrameSizableDiv() {
    var topFrameDiv = dojo.query('.main_table');
    if(!topFrameDiv || topFrameDiv.length == 0) {
        topFrameDiv = dojo.query('div.no_documents');
    }
    return topFrameDiv;
}


function setBottomFrameHeight() {
    if (document.getElementById("DocumentDetailsLeftFrame")) {

        var left = document.getElementById("DocumentDetailsLeftFrame");
        var right = document.getElementById("DocumentDetailsRightFrame");
        var doc = document.getElementById("DocumentDetails");

        var topFrameDiv = dojo.query('.main_table');
        if (topFrameDiv && topFrameDiv.length > 0) { // regular mode (docs list at top)
        	var bottomOffset = 191;
        	if (dojo.byId("workstation_advanced_search")){
        		bottomOffset = 204;
        	}
            var bottomHeight = document.body.clientHeight - topFrameDiv[0].clientHeight - bottomOffset; //TODO: 250 for ExtSearch
        } else { // single doc view mode
            topFrameDiv = dojo.query('div.document');
            var bottomHeight = document.body.clientHeight - topFrameDiv[0].clientHeight - 154;
        }

        left.style.height = bottomHeight + 'px';
        if(right){
        	right.style.height = bottomHeight + 'px';
        }
        doc.style.height = bottomHeight + 'px';
    }

    var advSearchDiv = dojo.byId("workstation_advanced_search");
    if (advSearchDiv) {
    	var bottomIndent = 108;
    	var searchResultsDiv = dojo.byId("searchResults");

    	if(searchResultsDiv) {
    		if(searchResultsDiv.getAttribute("class") == 'Expanded') {
    			return;
    		}
    		var bottomIndent = 125;
    	}

    	var bottomHeight = document.body.clientHeight - bottomIndent;
    	advSearchDiv.style.height = bottomHeight + 'px';
    }
}

function switchExtendedSearchRegions(arrowId){
	expandOrCollapse('searchResults');
	expandOrCollapse('searchParams');
	changeArrowDirection(arrowId);
	setBottomFrameHeight();
}

function changeArrowDirection(arrowId){
	var arrow = dojo.byId(arrowId);
	if(arrow != null) {
		arrow.setAttribute("class", arrow.getAttribute("class") == 'extended_search_delimiter_arrow_up' ? "extended_search_delimiter_arrow_down" : "extended_search_delimiter_arrow_up");
	}
}

function dragDown(){
    isDragging = true;
}

function releaseDrag(e){
    if (isDragging == true) {
        isDragging = false;
    }
    if (GtableHeight != '') {
        document.cookie = 'TABLEHEIGHT' + "=" + GtableHeight + "; path=/;";
    }
}

function disableDraggingFor(e) {
    // FF, Webkit
    if(!e)return;

    e.draggable = false;
    // old IE
    e.onmousedown = function(event) {
        event.preventDefault();
        return false;
    };
}

function to2digits(v){
	return (v<10)?"0"+v : v
}

function convertXMLDate(xmlDate){
	var arr = xmlDate.split("T");
	if(arr.length != 2) return (new Date(xmlDate));
	var dArr = arr[0].split("-");
	var tArr = arr[1].split(":");
	return new Date(dArr[0]*1,(dArr[1]*1)-1,dArr[2]*1,tArr[0]*1,tArr[1]*1,tArr[2]*1)
}

function getCounters(){
    var q = dojo.query("strong.counter");
    if( !q || q.length == 0) return;
    var k = 0;
	for(var i=0; i< q.length; i++){
		var countNode = q[i];
		var parent = getParent(countNode, "LI");

		if(parent){
			var link = parent.getElementsByTagName("A")[0];
			var id = link.href.split('?area=')[1];
			if(id != reportsFolderId){//отчеты
				CountersVars.Nodes[k] = {"id": id, "node":countNode};
				if(CountersVars.Sync == false) getCounter(id, k);
				if(dojo.hasClass(parent, "current")) CountersVars.current = k;
				k++
			}
		}
	}
	if(CountersVars.Nodes.length >0 && CountersVars.Sync){
		 getCounter(CountersVars.Nodes[0].id, 0)
	}

	setTimeout(getCounters, CountersVars.timeout)
}

function getCounter(id, i, node){
	if(!CountersVars.Nodes[i] && !node) return;
	if(id.startsWith(reportsFolderId)) return;

	dojo.xhrGet({
		url:  "/DBMI-UserPortlets/content?item="+id+"&views=13640" ,
		itemId: id,
		node: node || CountersVars.Nodes[i].node,
		preventCache: true,
		load: function(resp, ioArgs){
			ioArgs.args.node.innerHTML =
				(resp == "" ? "<span>[</span>0<span>]</span>" : "<span>[</span>" + resp + "<span>]</span>");
			if(!node && CountersVars.Sync) getCounter(CountersVars.Nodes[i+1].id, i+1);
			if(CountersVars.current >=0 && CountersVars.Nodes[CountersVars.current].id == id){
			//	newNotify()
			}
		}
	});
}

function refreshCounter(nodeStyle, itemId){
	var countNode = dojo.query(".counter", dojo.query("LI ."+nodeStyle)[0].parentNode)[0];
    getCounter(itemId, null, countNode)
}

function getParent(obj, tag, classNames) {
	var parent = obj;
	var found = false;
	for (var i=0; i<15; i++) {
		if( (!tag || parent.tagName == tag) && (!classNames || countClasses(parent,classNames) > 0) ) {
			found = true;
			break;
		}
		parent = parent.parentNode;
	}
	if (!found) {
		return false;
	}
	return parent;
}

function countClasses(parent, classes) {
	if (classes) {
		classes = classes.split(' ');
	}
	var result = 0;
	for ( i = 0 ; i < classes.length ; i++ ) {
		if( dojo.hasClass(parent,classes[i]) ) {
			result++;
		}
	}
	return result;
}

function isHiddenByParent(obj){
	var parent = obj;
	for(var i=0; i<10; i++){
		if( parent.style && parent.style.display == "none" ){
			return true;
		}
		parent = parent.parentNode;
	}

	return false
}

function openDoc(id, view, template, resetCache) {
	if(id.indexOf("row")>-1){
		id = id.split("row")[1]
	}

	// Set document details header
	var docTypeInput = dojo.byId("docType" + id);
	var regNumInput = dojo.byId("regNum" + id);
	var regDateInput = dojo.byId("regDate" + id);

	var docType = null != docTypeInput ? docTypeInput.value : "";
	var regNum = null != regNumInput ? regNumInput.value : "";
	var regDate = null != regDateInput ? regDateInput.value : "";

	var docDetailsHeader = dojo.byId("docDetailsHeader");
	if(null != docDetailsHeader) {
		var theHeader = docType + " " + regNum + " " + regDate;
		theHeader = theHeader.replace(/\n/g,"").replace(/<br>/g,"");
		docDetailsHeader.innerHTML = theHeader;
	}

    //var view = self.areaViews || '8564,8578,28980,28961,29020,29021';
    var view = '41462,8578,43340,43540,43541,543812,543813,42743,29021';

    
    

	var q = dojo.query(".row_sel");
	if(q && q.length >0) {
		clazzNames = String(q[0].className);
		if(clazzNames.indexOf('even') > -1) {
			q[0].className = "row even";
		} else {
			q[0].className = "row";
		}
	}

	var r = dojo.byId("row"+id);
	if(r){
		clazzNames = String(r.className);
		if(clazzNames.indexOf('even') > -1) {
			r.className = "row_sel even";
		} else {
			r.className = "row_sel";
		}
		var form = dojo.byId("control");
		if(form){
			form.set_C_JBR_PCON_DOC.value = id;
		}
		var url = self.location.href.split("/boss/")[0] + "/boss/";

        var documents = dojo.query("A.document");
        if(documents[0]) {
		    documents[0].href = url + "document?item="+id;
		    if(dojo.byId("workstation_advanced_search")){
		    	documents[0].href += '&directUrl=true';
			}
        }

        var links = dojo.query("A.links");
        if(links[0]) {
            links[0].href = url + "links?item="+id;
        }
        refreshInFavoritesAndPersControl(r, id);
        refreshPrintLink(r);
	}
	
    DocsVars.currentDoc = id;
    DocsVars.currentView = view;
    var cardNextId=nextDoc();
    if(cardNextId){
	    if(cardNextId.id.indexOf("row")>-1){
	    		cardNextId = cardNextId.id.split("row")[1]
	    }
    }
    DocsVars.currentNext = cardNextId;
    loadContentServlet(resetCache);
}

function refreshInFavoritesAndPersControl(elem, id) {
	var personalControlInput = dojo.byId("onPersonalControl" + id);
	var favoriteInput = dojo.byId("inFavorites" + id);
	if(id && (!personalControlInput || !favoriteInput)) {
		dojo.xhrGet({
			url:  "/DBMI-UserPortlets/content?item="+id+"&views=9999&strict=true",
			preventCache: true,
			appendTo: elem,
			id: id,
			load: function(resp, ioArgs){
				if(ioArgs.url.indexOf("item="+this.id+"&") == -1){
					//не дождались и открыли другой
				}else{
					if(this.appendTo) {
						var containerId = 'btn_container_'+this.id;
						var container = dojo.byId(containerId);
						if(container)
							dojo.destroy(container);
						var newElem = dojo.create('div',{id:containerId,style:'display:none'});
						newElem.innerHTML = resp;
						this.appendTo.appendChild(newElem);
						refreshPersonalControl(this.id);
						refreshInFavorites(this.id);
					}
	   			}
	        }
	    });
	} else {
		refreshPersonalControl(id);
		refreshInFavorites(id);
	}
}

function refreshPrintLink(elem) {
	// скрываем для уведомления о делегировании
	if(dojo.attr(elem, 'template') == 'Уведомление о делегировании') {
		var printLink = dojo.query("A.print")[0];
		if (printLink) {
			dojo.style(printLink, 'visibility','hidden');
			dojo.style(printLink, 'width','0');
		}
	} else {
		var printLink = dojo.query("A.print")[0];
		if (printLink) {
			dojo.removeAttr(printLink, 'style');
		}
	}
}

function loadContentServlet(resetCache){	

	dojo.byId("DocumentDetails").innerHTML = '<br><br><br><center><img src="/DBMI-Portal/js/dbmiCustom/images/dbmi_loading.gif"></center>'
	    var bttn = dojo.byId("DocumentDetails");
		dojo.style(bttn, {"height":"800px"});
	    dojo.xhrGet({
			url:  "/DBMI-UserPortlets/content?item="+DocsVars.currentDoc+"&views="+DocsVars.currentView+"&nextItem="+ DocsVars.currentNext,// + (resetCache?"&resetCache=true":""), кэш не сбрасываем
			docid: DocsVars.currentDoc*1,
			preventCache: true,
			load: function(resp, ioArgs){
				if(ioArgs.url.indexOf("item="+DocsVars.currentDoc+"&") == -1){
					//не дождались и открыли другой
				}else{
					var root = dojo.byId("DocumentDetails");
					root.style.cursor = "default";
	        		root.innerHTML = resp;

	                initDelegationEditFormControls();

					var q = dojo.query(".ReportsSet", root);
					if(q && q.length >0){
						for(var i=0; i < q.length; i++){
							try{searchReports(q[i]);}catch(ex){}
						}
					}else{
						searchReports(root);
					}
	   				search4LongTexts(root);
	   				showButtons();
	   				hideButtons();
	   				setTitles(root);
	   				//start "only for direct mode functions"
	   				updateTittleIfNeed();
	   				updareBackAndDoneURLsToDirectMode();
	   				//end
	   			}
	            setBottomFrameHeight();

	            var bttn = dojo.byId("DocumentDetails");
	            dojo.style(bttn, {"height":"auto"});
	        }
	    });
}

function getSelectedCardId() {

	var q = dojo.query(".row_sel");
	if(!q || q.length == 0)
	{
		alert("Не выделен документ");
		return;
	}
	return q[0].id.split("row")[1];

}

function getSelectedTemplateName() {

	var q = dojo.query(".row_sel");
	if(!q || q.length == 0)
	{
		alert("Не выделен документ");
		return;
	}
	var attributes = q[0].attributes;
	
	for(var i=0; i<attributes.length; i++){
		if (attributes[i].name == "template")
			return attributes[i].value; 
	}	

}

function editDocIndepResolution(docId) {
	lockScreen()
	var cardId = docId;
	if(!cardId) {
		cardId = getSelectedCardId();
	}

	var url = self.location.href;
	
	var baseUrl = url.split('?')[0];
		var	url = "/portal/auth/portal/boss/indepResolution/Content?formAction=initIndepRes&action=1&stateInit=initEdit&editCardId="+cardId+"&backURL="+baseUrl + "?item=" + cardId+"&doneURL=%2Fportal%2Fauth%2Fportal%2Fboss%2Ffolder%2F%3Fitem%3D"+cardId
	 	location.replace(url);
}

function editDocument(docId, formPrefix) {
	lockScreen()
	var cardId = docId;
	if(!cardId) {
		var selectedCardId = getSelectedCardId();
		if(!selectedCardId) {
			unlockScreen()
			return;
		}
		cardId = selectedCardId;
	}
	var curTemp;
	if(!(formPrefix && formPrefix.length > 0)) {
		curTemp = getSelectedTemplateName();
	}
	if(curTemp == 'Независимое поручение') {
		editIndependentResolution();
	} else {
		var url = self.location.href;
		
		var baseUrl = url.split('?')[0];
		var backUrl = "&MI_BACK_URL_FIELD=" + baseUrl + "?item=" + cardId;
			var formPref = '';
			if(formPrefix && formPrefix.length > 0) {
				formPref = "&FORM_PREFIX=" + formPrefix;
			}
		 	location.replace("/portal/auth/portal/boss/workstationCard/WorkstationCardWindow?action=e&mode=view&MI_OPEN_FOR_EDIT=true&MI_ACTION_FIELD=MI_EDIT_CARD_ACTION&windowstate=maximized&MI_EDIT_CARD=" + cardId + formPref + backUrl);
	}
}

function editPaperOriginal(docId, isIndepRes) {
	//checking if the document is an independent resolution
	if(isIndepRes) {
		editDocIndepResolution(docId);
	} else {
	editDocument(docId, 'paper');
}
}

function refreshPersonalControl(id) {
	
	var isPersonalControlView = dojo.byId("isPersonalControlView");
	if (isPersonalControlView && isPersonalControlView.value == 'true') {
		// document in the "personal control view" can only be removed from the control
		return;
	}
	var personalControlInput = dojo.byId("onPersonalControl" + id);
	var personalControl = null != personalControlInput ? personalControlInput.value : "false";

	var controlLink = dojo.query("A.control")[0];
	if (!controlLink) {
		controlLink = dojo.query("A.control_remove")[0];
	}
	if (controlLink) {
		if(personalControl == 'true') {
			controlLink.href ="javascript:removeControl()";
			controlLink.setAttribute("class", "control_remove");
			controlLink.title=titles['control_remove'];
		} else {
			controlLink.href ="javascript:toControl()";
			controlLink.setAttribute("class", "control");
			controlLink.title=titles['control'];
		}
	}
}

function setOnPersonalControl(id, onControl) {
	var personalControlInput = dojo.byId("onPersonalControl" + id);
	if (personalControlInput) {
		personalControlInput.value = onControl;
		refreshPersonalControl(id);
	}
}

function refreshInFavorites(id) {
	var favoriteInput = dojo.byId("inFavorites" + id);
	var isFavorite = null != favoriteInput ? favoriteInput.value : "false";

	var favoriteLink = dojo.query("A.favorite")[0];
	if (!favoriteLink) {
		favoriteLink = dojo.query("A.favorite_remove")[0];
	}
	if (favoriteLink) {
		if(isFavorite == 'true') {
			favoriteLink.href ="javascript:removeFromFavorites()";
			favoriteLink.setAttribute("class", "favorite_remove");
			favoriteLink.title=titles['favorite_remove'];
		} else {
			favoriteLink.href ="javascript:toFavorites()";
			favoriteLink.setAttribute("class", "favorite");
			favoriteLink.title=titles['favorite'];
		}
	}
}

function setFavorite(id, isFavorite) {
	var favoriteInput = dojo.byId("inFavorites" + id);
	if (favoriteInput) {
		favoriteInput.value = isFavorite;
		refreshInFavorites(id);
	}
}

function showRightFrame(id){
	DocsVars.currentSubcard = id;
	dojo.byId("DocumentDetailsRightFrame").innerHTML = '<br><br><br><center><img src="/DBMI-Portal/js/dbmiCustom/images/dbmi_loading.gif"></center>';
	var rf = dojo.byId("DocumentDetailsRightFrame");
	 dojo.xhrGet({
			url:  "/DBMI-UserPortlets/content?item="+id,
			docid: DocsVars.currentSubcard*1,
			preventCache: true,
			load: function(resp, ioArgs){
				if(ioArgs.url.indexOf("item="+DocsVars.currentSubcard+"&") == -1){
					//не дождались и открыли другой
				}else{
					var root = dojo.byId("DocumentDetailsRightFrame");
					root.style.cursor = "default";
	        		root.innerHTML = resp;
					var q = dojo.query(".ReportsSet", root);
					if(q && q.length >0){
						for(var i=0; i < q.length; i++){
							try{searchReports(q[i]);}catch(ex){}
						}
					}else{
						searchReports(root);
					}
	   				search4LongTexts(root);
	   				//showButtons();
	   				setTitles(root);
	   				setDuration();
	   			}
	        }
	    });
}

function selectNextDoc(){
	
	var qSel = dojo.query(".row_sel");
	var q = dojo.query(".row");
	if(!q || q.length == 0){
		//имеем дело не со списком, а с 1 доком на страницу
		q = dojo.query(".navigator a.next");
		if(q && q.length == 1){
			var url = dojo.attr(q[0], "href");
			if(url != ""){
				self.location.replace(url)
			}else{
				self.location.replace(self.location.href.split("/boss/")[0] + "/boss/folder/");
			}
		}
		return false;
	}
	
	var nextRow = nextDoc();

	if(!nextRow) return false;

	openDoc(nextRow.id);
	return true
}

function nextDoc(){
	var qSel = dojo.query(".row_sel");
	var q = dojo.query(".row");
	
	var nextRow = null;
	if(DocsVars.currentDoc == null || (!qSel || qSel.length == 0)){
		nextRow = q[0];
	}else{
        var docRows = dojo.query(".main_table tbody tr[class^=row]");
        if(!docRows || docRows.length < 2) {
            return null;
        }
        if(docRows[docRows.length - 1].className.indexOf("row_sel") >= 0) {
            nextRow = docRows[0];
        } else {
            for(i = 0; i < docRows.length - 1; i++) {
                if(docRows[i].className.indexOf("row_sel") >= 0) {
                    nextRow = docRows[i+1];
                    break;
                }
            }
        }
	}
	if(!nextRow) return null;
	
	return nextRow;
}

function removeCurrentDocFromList(force){
	if(getQueryVariable('directUrl')){
		window.location = 'portal/auth/portal/boss/folder';
	}
	var c = DocsVars.currentDoc;
	selectNextDoc();
	removeDocFromList(c, force);
}

function removeDocFromList(id, force){
	if(dojo.byId("workstation_advanced_search")) return;  //в расш. поиске не надо удалять
	if( (DocsVars.currentArea == "8058" || DocsVars.currentArea == "8544") && !force) return;  //в Избранном и ЛК не надо удалять
	var row = dojo.byId("row"+id);
	if(row){
		row.parentNode.removeChild(row);
		getCounters();
	}
	if(id == DocsVars.currentDoc){
		dojo.byId("DocumentDetails").innerHTML = "";
        DocsVars.currentDoc = null;
	}
	var cardList = dojo.query("tr[id^='row']");
	if(cardList && cardList.length==0) { // if there is a cards list and the list is empty
										// 	then removing all card details and showing no documents label
		var leftHeader =  dojo.query(".docDetailsLeftHeader");
		if(leftHeader)
			leftHeader[0].innerHTML = "";
		
		var rightHeader =  dojo.query(".docDetailsRightHeader");
		if(rightHeader)
			rightHeader[0].innerHTML = "";
		
		var docContent = dojo.byId("DocumentDetails");
		if(docContent)
			docContent.innerHTML = "";
		
		var formContent =  dojo.query(".content_lr_border > form");
		if(formContent)
			formContent[0].innerHTML = "";
		dojo.create("div", {class: "no_documents", innerHTML:"Нет документов"}, formContent[0]);
	}
}

function reopenCurDoc(){
	if(DocsVars.currentDoc){
		openDoc(DocsVars.currentDoc, DocsVars.currentView, false, true);
	}
}

function search4LongTexts(baseNode){
	var q = dojo.query(".bigContent", baseNode);
	if(!q || q.length ==0) return;

	for(var i=0; i < q.length; i++){
		var obj = q[i];
		setLongText(obj);
	}
}

function setLongText(obj, lim){
  var lim = lim || 150;
  var text = obj.innerHTML;
  if(text.length <= lim){return }

  var disp = text.substr(0, lim);
  var last = disp.substr(disp.length-1,1);
  if(last != " " && last != "." & last != ">"){
     disp = disp.substr(0, disp.lastIndexOf(" "));
  }
  disp += "...";
  obj.innerHTML = disp;

  var tt = new dijit.Tooltip({
     connectId: [obj.id],
     label: '<div style="max-width:500px; max-height:600px; ">'+text+'</div>'
  });
}

function setDuration(){
	var durationElement = dojo.byId("duration");
	if (durationElement != null){
		var d = parseInt(durationElement.innerHTML);
		if(d >= 0) {
			durationElement.innerHTML = "Осталось(дней): " + d;
			durationElement.setAttribute("class", "Green");
		} else if (d < 0){
			durationElement.innerHTML = "Просрочено(дней): " + (-d);
			durationElement.setAttribute("class", "Overdue");
		}
	}
}

function expandOrCollapse(id, source){
	var node = dojo.byId(id);
	if(node != null) {
		node.setAttribute("class", node.getAttribute("class") == 'Collapsed' ? "Expanded" : "Collapsed");
		if(source) {
			if(source.getAttribute("src") == "/DBMI-Portal/boss/images/bullet.gif")
				source.setAttribute("src", "/DBMI-Portal/boss/images/bullet_down.gif");
			else if(source.getAttribute("src") == "/DBMI-Portal/boss/images/bullet_down.gif")
				source.setAttribute("src", "/DBMI-Portal/boss/images/bullet.gif");
		}
	}
}

function searchReports(baseNode){
	var q = dojo.query(".reportItem", baseNode);
	
	if(!q || q.length ==0){ 
		if(baseNode.previousSibling && baseNode.previousSibling.tagName == "H3"){
			baseNode.previousSibling.style.display = "none"  //прячем заголовок
		}
		return
	};

	if(dojo.hasClass(baseNode, "ReportsSet")==false){
		//отчеты об исполнении
		for(var i=0; i < q.length; i++){
			setReport(q[i])
		}
		return
	}

	var byRound = [];

	//подписание и согласование
	for(var i=0; i < q.length; i++){
	 	var x = dojo.query('DIV[id="'+q[i].id+'"]'); //проверка на дублирование в допсогласовании
        if(x.length == 1 || dojo.hasClass(baseNode, "ReportsSetImportant")){
			var dom = parseReport(q[i]);
			var hasAny = false;
			if(dom){
				//проходим по истории
				var nodes = dom.firstChild.childNodes;

				for(var n=0; n<nodes.length; n++){
					if(nodes[n].nodeName.toLowerCase() == "part"){
						var round = nodes[n].getAttribute('round') || 1;
						round = round - 1;
						var obj = {
							user: dojo.attr(q[i], "user"),
	                        status: nodes[n].getAttribute('action'),
							date: convertXMLDate(nodes[n].getAttribute('timestamp')),
							node: nodes[n]
							};
						if(byRound[round] && dojo.isArray(byRound[round])){
							byRound[round].push(obj)
						}else{
							byRound[round] = [obj]
						}
						hasAny = true
					}
				}
			}
				//добавим текущую визу, если не завершена
			var lastObj = {
				status: dojo.attr(q[i], "status"),
				round: dojo.attr(q[i], "round") || 1,
				user: dojo.attr(q[i], "user")
				}

			if(dojo.byId('MyReport')){ //ищем текущую визу слева и берем проект решения
				var qMy = dojo.query('input[value="'+q[i].id.split('reportitem')[1]+'"]');
				if(qMy.length>0){
					lastObj.node = dojo.byId('MyReport');
					lastObj.isproject = true
				}
			}

			lastObj.round = lastObj.round-1;
			if(lastObj.round > round || hasAny == false){
				round = lastObj.round;
				if(byRound[round] && dojo.isArray(byRound[round])){
					byRound[round].push(lastObj)
				}else{
					byRound[round] = [lastObj]
				}
			}
		}
	}
	var html = "";
	if(byRound.length > 0){
		for(var i=0; i < byRound.length; i++){
			if(byRound[i]){
				html += '<p>';
				if(byRound.length > 1){
					html += '<b>'+(dojo.attr(baseNode, "RoundTitle") || 'Итерация')+' '+(i+1) + '</b><br>';
				}

				for(var n=0; n < byRound[i].length; n++){
					var obj = byRound[i][n];
					html += '<img src="/DBMI-Portal/boss/images/bullet.gif"><b>'+obj.user+'</b>';
					if(obj.node){
						var d = obj.date;
						var fact = obj.node.getAttribute("fact-user");
						var state = obj.node.getAttribute("to-state");
						var showComment = true;
						if(fact){
							html += " " + ( (obj.user.indexOf(fact.split(' ')[0]) == -1)?"("+fact+")":"" );
						}
                        if(obj.status){
                            html += ' <i>' + obj.status + '</i>';
                        }
						if(d){
                            html += ' '+to2digits(d.getDate()) + "."+to2digits(d.getMonth()+1)+"."+d.getFullYear() + " ";
						}
						if(state){
							//if(state=="107" || state=="6833780"){ //В АРМ и на допсогл	- ?????
							if(state=="6833780"){ //на допсогл
								showComment = false
							}else if(state=="201"){
								showComment = false;
								html += ' <b>Согласовано</b> '
							}else if(state=="202"){
								html += ' <b>Согласовано с замечаниями:</b> '
							}else if(state=="6092498"){
								html += ' <b>Отклонен</b> '
							}else if(state=="6840598"){
								html += ' <b>На доработку</b> '
							}
						}
						if(obj.node.firstChild && showComment){
							var text = obj.node.firstChild.nodeValue.replace(/~newline~/g, '<br>');
							html += (obj.isproject?" Проект решения: ":"")+ ' <span id="text_'+(Math.random()*100000000)+'" class="bigContent">'+text+'</span>';
						}
					} else if(obj.status){
                        html += ' <i>'+obj.status+'</i>';
                    }
					html += '<br>';
				}
				html += '</p>';
			}

		}
	}
	if(html != ""){
		var div = dojo.create("span", false, q[0], 'before');
		div.innerHTML = html
	}else{
		var q = dojo.query("H3", baseNode);
		if(q || q.length >0){
			q[0].style.display = "none"
		}
	}

}

function setReport(obj){
	var dom = parseReport(obj);
	if(!dom) return;
	var parts = dom.firstChild.childNodes;
	var html = '<span id="text_'+obj.id.split("item")[1]+'" class="Report">';
	for (var i = 0; i < parts.length; i++){
		html+= parts[i].textContent.replace(/~newline~/g, '<br>').replace(/&#13;/g, '<br>') + '<br>';
		var d = convertXMLDate(parts[i].getAttribute('timestamp'));
		html+= to2digits(d.getDate()) + "." + to2digits(d.getMonth()+1)+"."+d.getFullYear() + '<br><br>';
	}
	html += '</span>';
	/*
	var last = dom.firstChild.lastChild;
	var d = new Date(last.getAttribute('timestamp'));
	var text = last.firstChild.nodeValue.replace(/~newline~/g, '<br>');
	var out = '<span id="text_'+obj.id.split("item")[1]+'" class="bigContent">';
	out += text + '<br>'+d.getFullYear() + "."+to2digits(d.getMonth()+1)+"."+to2digits(d.getDate())+'</span>';
	*/
	var div = dojo.create("p", false, obj, 'before');
	div.innerHTML = html;
}

function parseReport(obj, noDom){
	if(obj.innerHTML.indexOf('&lt;?xml version="1.0"')==-1) return false;

	var xmlStr = obj.innerHTML.replace(/<br>/g, "").replace(/&lt;/g, "<").replace(/&gt;/g, ">").replace(/&amp;/g, "&");
	if(xmlStr.length < 10) return false;
	if(noDom) return xmlStr

	xmlStr = xmlStr.replace(/&#13;/g, "~newline~");
	var xmlDom = dojox.xml.parser.parse(xmlStr);
	return xmlDom
}

function addReport(text, id, useRow){
	this.getDate = function(){
		var d = new Date();
		return d.getFullYear() + "-"+to2digits(d.getMonth()+1)+"-"+to2digits(d.getDate())+"T"+to2digits(d.getHours())+":"+to2digits(d.getMinutes())+":"+to2digits(d.getSeconds())
	}
	var obj = dojo.byId("MyReport" + (id?id:""));
	var xml = parseReport(obj, true);
	if(!xml){
		xml = '<?xml version="1.0" encoding="UTF-8"?><report>'
	}
	var htmlText = text.replace(/</g, "&lt;").replace(/>/g, "&gt;").replace(/\n/g, "&#13;")
	if(useRow){
		var newPart = '<raw-part>'+htmlText+"</raw-part>"
	}else{
		var newPart = '<part timestamp="'+this.getDate()+'">'+htmlText+"</part>";
	}
	return xml.split('</report>')[0] + newPart + '</report>'
}

function setTitles(baseNode){
	var q = dojo.query("A", baseNode);
	if(q && q.length >0){
		for(var i=0; i < q.length; i++){
			var c = q[i].className.split(" ");
			c = c[c.length-1];
			if(titles[c]) q[i].title = titles[c]
		}
	}
	var q = dojo.byId("RightFrameRightTitle");
	if(q){
		dojo.query(".docDetailsRightHeader")[0].innerHTML = q.innerHTML
	}
}

function deleteRes(id){
  if(confirm("Удалить проект резолюции?")){
	lockScreen();
 	dojo.xhrPost({
		form:  document.forms['del_'+id] ,
		preventCache: true,
		load: function(resp, ioArgs){
 			unlockScreen();
 			reopenCurDoc()
 		}
    });
  }
}

function execResolution(){
	var q = dojo.query('a.del');
	if((q && q.length>0) || confirm('Отправить документ на исполнение без резолюции?')){
		var forms = getForms(['approve']);
		
		if(forms && forms.length >0){
			submitForms(forms, 0, function(){
				// если папка - Избранное
				if(DocsVars.currentArea == "8058") {
					reopenCurDoc();
				} else {
					cardInPrivate(forms);
				}
				return "on";
			})
		}
		
	}
}

function recordResolution(){
	if(confirm('Отправить документ в дело?')){
		lockScreen();
		dojo.xhrPost({
			form: document.forms['record'],
			preventCache: true,
			load: function(resp, ioArgs){
				unlockScreen();
				if(validateRequestResult(resp)){
					removeCurrentDocFromList();
				}
			}
		});
	}
}

function inCase(){
	lockScreen();
	dojo.xhrPost({
		form:  document.forms['inCaseForm'] ,
		preventCache: true,
		load: function(resp, ioArgs){
			unlockScreen();
			removeCurrentDocFromList();
		}
    });
}

function toControl(){
	var q = dojo.query(".row_sel");
	if(!q || q.length == 0){ alert("Не выделен документ"); return}
	//show dialog with date

	var id = q[0].id.split("row")[1];

	DocsVars.currentDialog = new dateDialog({
		title: 'На личный контроль c:',
		onsubmit: function(repText){
			if (dijit.byId('dateDialogValue').isValid() && repText != null && repText != "") {
			var f = document.forms['control'];
			f.set_D_JBR_PCON_DATE.value = repText;
			f.set_C_JBR_PCON_DOC.value = id;
			f.action = "/DBMI-UserPortlets/content?form=modify&item="+id+"&action=1&mode=add";
			lockScreen();
			dojo.xhrPost({
				form:  f ,
				preventCache: true,
				load: function(resp, ioArgs){
						unlockScreen();
						getCounters();
						setOnPersonalControl(id, 'true');
					}
		    });



		}
			else {
				throw "invalid date";
			}
		}
	})
}

function toFavorites()  {
	var q = dojo.query(".row_sel");
	if(!q || q.length == 0){ alert("Не выделен документ"); return}

	var id = q[0].id.split("row")[1];
	var f = document.forms['favorite'];
    f.set_C_JBR_FAV_DOC_DOC.value = id;
    var a = f.action.split("item=");
    f.action = "/DBMI-UserPortlets/content?form=modify&item="+id+"&action=1&mode=add_to_favorites";
    lockScreen();
    dojo.xhrPost({
        form:  f ,
        preventCache: true,
        load: function(resp, ioArgs){
                unlockScreen();
                getCounters();
                setFavorite(id, 'true');
            }
    });
}

function removeControl(){
	var q = dojo.query(".row_sel");
	if(!q || q.length == 0){ alert("Не выделен документ"); return}
	var id = q[0].id.split("row")[1];

	var f = document.forms['control'];
	f.set_C_JBR_PCON_DOC.value = id;
	f.action = "/DBMI-UserPortlets/content?form=modify&item="+id+"&action=1&mode=remove";
	lockScreen();
	dojo.xhrPost({
		form:  f ,
		preventCache: true,
		load: function(resp, ioArgs){
				unlockScreen();
				var isPersonalControlView = dojo.byId("isPersonalControlView");
				if (isPersonalControlView && isPersonalControlView.value == 'true') {
					removeCurrentDocFromList(true);
				} else {
					setOnPersonalControl(id, 'false');
                    getCounters();
				}
			   }
    });
}

function removeFromFavorites(){
	var q = dojo.query(".row_sel");
	if(!q || q.length == 0){ alert("Не выделен документ"); return}
	var id = q[0].id.split("row")[1];

	var f = document.forms['favorite'];
	f.set_C_JBR_FAV_DOC_DOC.value = id;
	f.action = "/DBMI-UserPortlets/content?form=modify&item="+id+"&action=1&mode=remove_from_favorites";
	lockScreen();
	dojo.xhrPost({
		form:  f ,
		preventCache: true,
		load: function(resp, ioArgs){
				unlockScreen();
				var isFavoritesView = dojo.byId("isFavoritesView");
				if (isFavoritesView && isFavoritesView.value == 'true') {
					removeCurrentDocFromList(true);
				} else {
					setFavorite(id, 'false');
                    getCounters();
				}
			  }
    });
}

function showBottomFrm() {
	var q = dojo.query("#doc_details");
	dojo.style(q[0], {"display":""});
	q = dojo.query("#document_body .main_table");
	dojo.style(q[0], {"height":"400px"});
}

function initDocsList(){
	if(!dojo.byId("DocumentDetails")){
		dojo.addOnLoad(function() {
		initDocsList()
		});
		return
	}
	var q1 = dojo.query(".row_sel");
	if(!(q1 && q1.length > 0)) {
		//вдруг хотели выделить, а карточка такая ушла
		if(self.location.href.indexOf("item=")>-1){
			var nextRow = nextDoc();
			if(nextRow){
				openDoc(nextRow.id);		 		
			}
		}else{
			showBottomFrame = false;
			bottomFrameVisible = false;
		}
	}
	var q = dojo.query(".row_sel,.row");
	if(q && q.length > 0){
		q.connect("onclick", function(e){
			var obj = getParent(e.target, "TR", "row");
			if(obj){
				/*if(dojo.hasAttr(obj, "template")){
					openDoc(obj.id, null, dojo.attr(obj, "template"))
				}else{*/
					openDoc(obj.id);
					if (!bottomFrameVisible) {
						showBottomFrm();
						bottomFrameVisible = true;
						dojo.byId(obj.id).scrollIntoView();
					}
				//}
			}
		});
	}

	var columnHeaders = dojo.query(".main_table thead tr th");
	if(columnHeaders && columnHeaders.length > 0){
		columnHeaders.connect("onclick", function(e){
			prepareSortParams(this);
			window.location = "/portal/auth/portal/boss/folder/Content?action=1&sortColumnId=" + SortVars.sortColumnId;
		});
	}
}

function prepareSortParams(tableHeader){
	 var newCurrThClass = dojo.query(tableHeader).attr('class')[0];

    var currColumnId = tableHeader.getAttribute('columnId');
    var currThClass = getCookie('CURRTHCLASS');
    var currSortOrder = getCookie('CURRSORTORDER');
    var currSortCode = getCookie('CURRSORTCODE');

    var newSortOrder;
    var newSortCode;

    if(!currThClass || currThClass != newCurrThClass) {
        var columnSortData = dojox.json.query("[?columnId='" + currColumnId + "']", columnsSortData);
        if(columnSortData && columnSortData.length > 0) {
            newSortOrder = columnSortData[0].asc;
            newSortCode = columnSortData[0].sortAttrCode;
        } else {
            newSortOrder = true;
            newSortCode = "";
        }
    } else {
        newSortOrder = !eval(currSortOrder);
        newSortCode = currSortCode;
    }

    document.cookie = "CURRTHCLASS=" + newCurrThClass + "; path=/;";
    document.cookie = "CURRSORTORDER=" + newSortOrder + "; path=/;";
    document.cookie = "CURRSORTCODE=" + newSortCode + "; path=/;";

    SortVars.sortColumnId = currColumnId;
    SortVars.sortAsc = newSortOrder;
    SortVars.sortAttrCode = newSortCode;
}

function initSearchResultList() {
	var q = dojo.query(".row_sel,.row");
	if (q && q.length > 0) {
		q.connect("onclick", function(e) {
			var obj = getParent(e.target, "TR", "row row_sel");
			if (obj) {
				openSearchResultDoc(obj.id);
			}
		});
	}

	var columnHeaders = dojo.query(".main_table thead tr th");
	if(columnHeaders && columnHeaders.length > 0){
		var sortableColumns = new dojo.NodeList();
		for(var i=0; i< columnHeaders.length; i++) {
			var headerClass = columnHeaders[i].getAttribute("class");
			if (headerClass.indexOf("unsortable") == -1) {
				sortableColumns.push(columnHeaders[i]);
		    }
		}

		sortableColumns.connect("onclick", function(e){
			prepareSortParams(this);
			submitSearchForm('SEARCH', SortVars.sortAttrCode, SortVars.sortAsc);
		});
	}
}
	function openSearchResultDoc(id) {
		openDoc(id);
	}

	function openSearchResultDocDeprecated(id) {
	if (id.indexOf("row")>-1) {
		id = id.split("row")[1];

		var q = dojo.query(".row_sel");
		if(q && q.length >0) {
			clazzNames = String(q[0].className).split(' ');
			if(clazzNames.indexOf('even') > -1) {
				q[0].className = "row even";
			} else {
				q[0].className = "row";
			}
		}

		var r = dojo.byId("row"+id);
		if(r){
			clazzNames = String(r.className).split(' ');
			if(clazzNames.indexOf('even') > -1) {
				r.className = "row_sel even";
			} else {
				r.className = "row_sel";
			}
		}
	}
	window.open("/portal/auth/portal/boss/document?item=" + id);
}

function newNotify(){
	var num = CountersVars.Nodes[CountersVars.current].node.innerHTML;
	if(num == "") return;
	var c = "";

	var q = dojo.query(".navigator P");

	if(q && q.length > 0){ //single document view
		c = q[0].innerHTML.split(" из ")[1]
	}else{
		c = 0;
		q = dojo.query(".row_sel,.row");
		if(q) c = q.length
	}
	if(!isNaN(c) && num*1 > c){
	//if(true){
		var popup = dojo.byId("NotifyDiv");
		if(!popup){
			popup = dojo.create("div", {id: "NotifyDiv"}, document.body, "first");
			var pos = dojo.coords(document.body, true);
			dojo.style(popup, {
				"position":"absolute",
				"top": "0px",
				"left":(document.body.clientWidth - 230) + "px",
				"height": "50px",
				"width":"200px",
				"padding":"5px",
				"background": "#ccc",
				"border":"1px solid #999",
				"opacity": "0"}
			);
			popup.innerHTML = 'В папке есть новые документы. '+
				'<a href="javascript: self.location.reload(false)">Обновить</a>'

		}
		dojo.fadeIn({node: popup, duration: 900}).play()
	}
}

function ShowPrintFormDepricated(id, myUrl){
	var url = myUrl || "/DBMI-UserPortlets/servlet/JasperReportServlet?nameConfig=reportChartIncoming&card_id=L_"+id+"&noname=1";
	var pane = null;
	if(self.detailsDialog){
		self.detailsDialog.destroyRecursive()
	}
	var h = document.body.clientHeight;//800;
	pane = dojo.create("div", {id: ""}, document.body, "last");
	//dojo.style(pane, {width:'98%',height:'700px', border: "5px solid red"});
	//pane.style.border = "5px solid red"
	pane.innerHTML = '<iframe id="" src="'+url+'" width="800px" height="'+(h-100)+'px" border="0" scrolling="no" ></iframe>';
	self.detailsDialog = new dijit.Dialog({
		id: "dialog3",
		title: "Карточка документа",
		style: {width: '850px', height:(h-50)+'px'}
	},pane);


	self.detailsDialog.show()
}

function ShowPrintForm(id){
	var href = '/portal/auth/portal/boss/documentData/Content?&action=1&cardId='+id;
	window.open(href,'_blank');
}

function textDialog(args){
	this.id = args.id;
	this.title = args.title;
	this.onsubmit = args.onsubmit;

	this.show = function(){
		var d = dijit.byId('textDialog');
		if(d) d.destroy();

		//if(!d){
			dojo.byId("DocumentDetails").innerHTML +=
			'<div dojoType="dijit.Dialog" id="textDialog" title="'+this.title+'"'+
			'onExecute="DocsVars.currentDialog.submit();" aria-describedby="intro"	execute="return false;">'+
			'	<div class="dijitDialogPaneContentArea">'+
			'		<TEXTAREA id="textDialogText" cols=50 rows=10 ></TEXTAREA>'+
			(args.moreHTML || "")+
			'	</div>'+
			'	<div class="dijitDialogPaneActionBar">'+
			'		<button dojoType="dijit.form.Button" type="submit">OK</button>'+
			'		<button dojoType="dijit.form.Button" type="button" onClick="dijit.byId(\'textDialog\').hide();">Отмена</button>'+
			'	</div>'+
			'</div>'
			dojo.parser.parse();
			d = dijit.byId('textDialog');
		//}
		d.show();
	}
	this.submit = function(){
		this.onsubmit(dojo.byId('textDialogText').value)
	}

	this.show()
}

function alertDialog(args){
	var id = args.id || "alertDialog";
	var title = args.title || "Внимание";
	var onsubmit = args.onsubmit;
	
	var d = dijit.byId(id );
	if(d) d.destroyRecursive();
		
	var div= dojo.create("div", {id:""},document.body, "last");
	div.style.width = "350px"
	
	var btns = "";
	
	if(args.buttons){
		dojo.forEach(args.buttons, function(btn){
			btns += '<button id="'+btn.id+'" dojoType="dijit.form.Button">'+btn.title+'</button>'
		})
	}else{
		btns = '<button dojoType="dijit.form.Button" type="submit" onClick="dijit.byId(&quot;'+id+'&quot;).hide()">OK</button>'
	}
	
	div.innerHTML = '<div class="dijitDialogPaneContentArea">' + args.text + '</div>'+
'	<div class="dijitDialogPaneActionBar"><br>'+ btns +	'	</div>'
	
	
	self.AlertDialog = new dijit.Dialog({
		id: id,
		title: title,               
		style: {width: '350px'}
		},div);
	
	if(args.closeButton == true) 
		dojo.style(self.AlertDialog.closeButtonNode,"display","none"); 	
	
	if(args.buttons){
		dojo.forEach(args.buttons, function(btn){
			dojo.connect(dojo.byId(btn.id), "onclick", btn.action);
		})	
    }
	
	self.AlertDialog.show()		
}


function dateDialog(args){
	this.id = args.id;
	this.title = args.title;
	this.onsubmit = args.onsubmit;

	this.show = function(){

		var dateValue = dijit.byId('dateDialogValue');
		if(dateValue) {
			dateValue.destroy();
		}


		var d = dijit.byId('dateDialog');
		if(d) {
			d.destroy();
		}


		//if(!d){
			dojo.byId("DocumentDetails").innerHTML +=
			'<div dojoType="dijit.Dialog" id="dateDialog" title="'+this.title+'"'+
			'onExecute="DocsVars.currentDialog.submit();" aria-describedby="intro"	execute="return false;">'+
			'	<div class="dijitDialogPaneContentArea">'+
			'		<input dojoType="dijit.form.DateTextBox" type="text" id="dateDialogValue" style = "width: 138px;" />'+
			'	</div>'+
			'	<div class="dijitDialogPaneActionBar">'+
			'		<button dojoType="dijit.form.Button" type="submit">OK</button> '+
			'		<button dojoType="dijit.form.Button" type="button" onClick="dijit.byId(\'dateDialog\').hide();">Отмена</button>'+
			'	</div>'+
			'</div>'
			dojo.parser.parse();
			d = dijit.byId('dateDialog');
		//}
		d.show();
	}
	this.submit = function(){
		this.onsubmit(dojo.byId('dateDialogValue').value)
	}

	this.show()
}

function execTask(id, doc){
	lockScreen();
	/*var selectedRow = dojo.query(".row_sel");
	var backLinkDocumentId;
	if(selectedRow && selectedRow.length > 0) {
		backLinkDocumentId = selectedRow[0].id.split('row')[1];
	}*/

	url = "/portal/auth/portal/boss/resolutionReport/Content?formAction=INIT&action=1&editCardId=" + id +
			"&backURL=%2Fportal%2Fauth%2Fportal%2Fboss%2Ffolder";

	if(doc) {
		url = url + "%2F%3Fitem%3D" + doc;
	}

	window.location = url;
}

function signDoc(element){
	window.attachDS=false;
	if(element){
		thisForm = dojo.byId(element).parentNode;
	} else {
		thisForm = undefined;
	}
	if(thisForm){
		if (thisForm.moveds.value > 0){
			attachDsQuery(thisForm);		
		} else {
			signDoc_do();
		}
	} else {
		attachDsQuery(thisForm);
	}
}

function signDoc_do(){
	var f = dojo.byId('signForm');
	var signVisaForms = null;
	if(f.set_T_ADMIN_14759970)	f.set_T_ADMIN_14759970.value = encodeURIComponent("Подписал");
	if(f.set_T_ADMIN_14746670){
		f.set_T_ADMIN_14746670.value = encodeURIComponent("Согласен");
		signVisaForms = dojo.query("#DocumentDetailsRightFrame #signForm");
	}
	//var inp = dojo.query("input", f)[2]
	//inp.value = addReport(inp.value, false, true);
	//reopenCurDoc
	lockScreen();
	dojo.xhrPost({
		form:  f,
		preventCache: true,
		load: function(resp, ioArgs){
				unlockScreen();
				if(validateRequestResult(resp)){
					if(dojo.byId("CryptoApplet") != null && window.attachDS){
						lockDsScreen();
						if(signVisaForms==null || signVisaForms.length<=1){
							signDS(true);
						}else{
							signDS(true, true);
						}	
						unlockScreen();
					}else{
						var isExtendSearch = dojo.byId("workstation_advanced_search");
						if(!isExtendSearch 
								&& (signVisaForms==null || signVisaForms.length<=1)){
							removeCurrentDocFromList();
						}else{
							reopenCurDoc();
						}
					}
				}
			}
    })
}

function attachDsQuery(thisForm){
	if(self.detailsDialog){
		self.detailsDialog.destroyRecursive();
	}
	
	if( !checkAppletExists(true)){
		//no applet - no ds
		if(thisForm && thisForm.moveds.value > 2){
			alert("Переход должен сопровождаться обязательным формированием ЭП. У вас отсутствует сертификат ЭП. Действие отменено.");
			return;
		} else {
			signDoc_do();
			return
		}
	}
	
	var pane = dojo.create("div", {id: ""}, document.body, "last");
	pane.innerHTML = '<div style="text-align: left;">Применить ЭП для карточки?</div>' +
		'<div style="float: right; clear: both;" id="dialogButtons">' +
		'<button id="attachDsQueryYesBtn" dojoType="dijit.form.Button" type="button">Да</button>' +
		'<button id="attachDsQueryNoBtn" dojoType="dijit.form.Button" type="button">Нет</button>' +
		'</div>';
	self.detailsDialog = new dijit.Dialog({
		id: "attachDsQuery",
		title: "Подпись карточки",
		style: {width: '400px', height: '120px'}
		},pane);

	dojo.connect(dojo.byId("attachDsQueryYesBtn"), "onclick", function(e){
		self.detailsDialog.hide();
		if((dojo.byId("CryptoApplet") != null)&& checkPermission && signDS(false)){
			window.attachDS = true;
			signDoc_do();
		} else if (thisForm && thisForm.moveds.value > 2) {
			alert("Формирование ЭП невозможно. Документ не подписан ЭП и не будет направлен далее по маршруту.");
		} else {
			doWithOutDsQuery();
		}
	});
	dojo.connect(dojo.byId("attachDsQueryNoBtn"), "onclick", function(e){
		self.detailsDialog.hide();
		if (thisForm && thisForm.moveds.value > 2) {
			alert("Документ не подписан ЭП и не будет направлен далее по маршруту.");
		} else {
			signDoc_do();
		}
	});

	self.detailsDialog.show();
}	
	
function doWithOutDsQuery(){
		if(self.detailsDialog){
			self.detailsDialog.destroyRecursive();
		}
		var pane = dojo.create("div", {id: ""}, document.body, "last");
		pane.innerHTML = '<div style="text-align: left;">Присоединение ЭП невозможно. Выполнить действие без формирования ЭП?</div>' +
			'<div style="float: right; clear: both;" id="dialogButtons">' +
			'<button id="doWithOutDsQueryYesBtn" dojoType="dijit.form.Button" type="button">Да</button>' +
			'<button id="doWithOutDsQueryNoBtn" dojoType="dijit.form.Button" type="button">Нет</button>' +
			'</div>';
		self.detailsDialog = new dijit.Dialog({
			id: "doWithOutDsQuery",
			title: "Подпись карточки",
			style: {width: '400px', height: '120px'}
			},pane
			);

		dojo.connect(dojo.byId("doWithOutDsQueryYesBtn"), "onclick", 
			function(e){
				self.detailsDialog.hide();
				signDoc_do();
			}
		);

		dojo.connect(dojo.byId("doWithOutDsQueryNoBtn"), "onclick", 
			function(e){
				self.detailsDialog.hide();
			}
		);
	
		self.detailsDialog.show();
}

function prepareSignParams(){
	dojo.byId('requestFlag').value = "get_params";
	var f = dojo.byId('signForm');
	dojo.xhrPost({
		form:  f,
		preventCache: true,
		sync:true,
		load: function(resp, ioArgs){
			if(validateRequestResult(resp)){
				window.signParams = JSON.parse(resp);
			}
		}
    });
	dojo.byId('requestFlag').value = "";
}

function signDS(submit) {
	signDS(submit, false);
}

function signDS(submit, refreshCurDoc){
	try{
		prepareSignParams();
		if(window.signParams){
			var stringsArray = eval("[" + window.signParams[0]+ "]");
			var stringsArrayHash = eval("[" + window.signParams[1]+ "]");
			var signAttrXML = eval("[" + window.signParams[2]+ "]");
			var currentSignature = eval("[" + window.signParams[3]+ "]");
			var ids = eval("[" + window.signParams[4]+ "]");
			
			var args = {
				stringsArrayData: stringsArray,
				stringsArrayHash: stringsArrayHash,
				signAttrXML: signAttrXML,
				currentSignature: currentSignature,
				ids: ids
			};
			window.signParams = undefined;
			var msg = "";
			var signResult = cryptoGetSignature(args);
			if (signResult.success) {
				if(submit){
					submitSignDS(signResult.signature, args);
				} else {
					return true;
				}
			} else {
				if(submit){
					if(refreshCurDoc){
						reopenCurDoc();
					}else{
						removeCurrentDocFromList();
					}
				}
				if(signResult.msg == "noapplet") {
					msg = "Апплет не инициализирован"
				} else if(signResult.msg == "nofields") {
					msg = "Нет подписываемых аттрибутов";
				} else {
					msg = signResult.msg;
				}
				if (msg && msg.length > 0 && submit) {
					alert(msg);
				}
				return false;
			}
		} else {
			return false;
		}
	}
	catch (err){
		return false;
	}
}

function submitSignDS(value, args) {
	var result = "";
	for (var i=0; i<value.length; i++){
		if (i > 0){
			result += "###";
		}
		result += args.ids[i] + "::";
		if (args.currentSignature[i].length > 0){
			result += args.currentSignature[i];
		}
		result += value[i];
	}
	
	dojo.byId('signature').value = result;

	var f = dojo.byId('signForm');
	lockScreen();
	dojo.xhrPost({
		form:  f,
		preventCache: true,
		load: function(resp, ioArgs){
			unlockScreen();
			validateRequestResult(resp);
			removeCurrentDocFromList();
		}
    })
}

function reportDS() {
	var cardId = getSelectedCardId();
	if(cardId == null) return;
	var namespace = dojo.byId("bean_namespace").value;
    window.open("/DBMI-UserPortlets/CardDSInfoViewer?namespace="+namespace+"&cardId=" + cardId, "_blank");
}

function acceptReport(id, taskId){
	submitForm(dojo.byId("accept"+id), function(){
		var flag = false;
		var retain = false;
		//Проверяем, есть ли еще отчеты для утверждения.
		var acceptForms = getForms(['accept']);
		retain = acceptForms.length > 1;

		var q = dojo.query("div.ChildTasksIds");
		if(q && q.length > 0){
			for(var i=0; i<q.length; i++){
				if(q[i].innerHTML.indexOf(taskId)>-1){
					flag = true;
					
					alertDialog({
						title:"Исполнение", 
						text:"Вы утвердили отчет об исполнении. Готовы ли Вы подготовить отчет об исполнении своего поручения?",
						buttons:[
							{id:"acceptConfirmYes",title:"ДА",action:function(){
								dojo.query("A",dojo.query('.ChildTasksIds')[0].parentNode)[0].click(); //Кнопка Исполнить у родительского поручения
								self.AlertDialog.hide()
								}}, 
							{id:"acceptConfirmNo",title:"НЕТ",action:function(){
								var q = dojo.query("a.approve");
								//в папках На исполнении, Мои документы, Рассмотреть, Рассмотреть срочно не удаляем
								if((q && q.length > 1) || DocsVars.currentArea == "8050"
									|| DocsVars.currentArea == "9102" 
										|| DocsVars.currentArea == "41460" || DocsVars.currentArea == "47447"){
									reopenCurDoc()
								}else{
									removeCurrentDocFromList();
								}
								self.AlertDialog.hide()
								}}
							],
						closeButton: true
					});					
					break
				}
			}
		}
		if(!flag){
			//в папках Мои документы, Рассмотреть, Рассмотреть срочно не удаляем
			if(retain || DocsVars.currentArea == "8050" 
				|| DocsVars.currentArea == "41460" || DocsVars.currentArea == "47447"){
				reopenCurDoc()
			}else{
				removeCurrentDocFromList();
			}
		}
		
	})
}

function declineReport(id){
	DocsVars.currentDialog = new textDialog({
		title: 'Отклонение отчета',
		onsubmit: function(repText){
			if (repText != null && repText != "") {
			var f = dojo.byId('declineForm'+id);
			//if(f.set_T_ADMIN_6888758) f.set_T_ADMIN_6888758.value = repText;
			if(f.set_T_RREASON) f.set_T_RREASON.value = encodeURIComponent(repText);

			submitForm(f, function(){
				
				var q = dojo.query("#DocumentDetails FORM[id^=declineForm]");
				if( (q && q.length > 1) || DocsVars.currentArea != "45021" || DocsVars.currentArea == "8050") //(не папка Утверждение) или папка Мои Документы
					reopenCurDoc()
				else				
					removeCurrentDocFromList();
				
			})
		}
			else {
				throw "report_is_empty";
			}
		}
	})

}

function declineNPA(formID){
	var formtitle = 'Причина отправки на доработку';
	if(formID == 'return2'){
		formtitle = 'Причина отклонения'
	}
	DocsVars.currentDialog = new textDialog({
		title: formtitle,
		onsubmit: function(repText){
			if(repText != null && repText != "") {
				var f = dojo.byId(formID || 'return');
				f.set_T_JBR_MANAGER_COMMENT.value = encodeURIComponent(repText);

				submitForm(f, function(){
					removeCurrentDocFromList();
				})
			}else {
				throw "report_is_empty";
			}
		}
	})

}

function showButtons(){
	var showRepLink = 0;
	var url = '/DBMI-UserPortlets/servlet/JasperReportServlet?nameConfig=<param>&card_id=L_'+DocsVars.currentDoc+'&noname=1';

	var q = dojo.query("h3");
	if( q && q.length > 0){
		for(var i=0; i< q.length; i++){
			if(q[i].innerHTML.indexOf("Поручения") >-1 ){
				var q2 = dojo.query(".tasksList DIV, .tasksList P");
				if( !q2 || q2.length == 0){
						q[i].style.display = 'none';
						//break;
				}
				//q[i].innerHTML += '<a href="'+url.replace('<param>','reportPrintExecutionResolution')+'" target="_blank" style="margin-left:30px">Ход исполнения</a>';
				//showRepLink = 1
				break
			//}else if(q[i].innerHTML.indexOf("Ход выполнения") >-1 ){
			//	q[i].innerHTML = '<a href="'+url.replace('<param>','reportPrintExecutionResolution')+'" target="_blank">Ход выполнения</a>';
			//	break
			}
		}
	}

	var template = dojo.byId("DivTemplate"); //(8553/8001)
	if(template){
		template = template.innerHTML;
		var a = dojo.query("A.print")[0];
		a.href = '/portal/auth/portal/boss/documentData/Content?&action=1&cardId='+DocsVars.currentDoc;
		a.target='_blank';
	}

	var editPaperOriginalLink = dojo.query("A.editPaperOriginal")[0];
	if(editPaperOriginalLink) {
		editPaperOriginalLink.href = 'javascript:editPaperOriginal('+DocsVars.currentDoc+',false)';
	}
	
	var editPaperOriginalIndepResLink = dojo.query("A.editPaperOriginalIndepResolution")[0];
	if(editPaperOriginalIndepResLink) {
		editPaperOriginalIndepResLink.href = 'javascript:editPaperOriginal('+DocsVars.currentDoc+',true)';
	}

	var q = dojo.query(".showButton");
	if( !q || q.length == 0) return;

	var shown = [];
	var shownArr = [];
    var html = "";

	for(var i=0; i< q.length; i++){
		//if(isHiddenByParent(q[i]) == false){
		if(!shown[q[i].innerHTML]){
			shown[q[i].innerHTML] = true;
			shownArr.push(q[i].innerHTML)
		}
		//}
	}
	for(var s=0; s < shownArr.length; s++){
		var fName = shownArr[s];
		if(fName == "sendTasks"){
			html += '<a class="button blue" href="javascript: sendTasks();">Утвердить</a>'
		}else if(fName == "take"){
			html += '<a class="button blue" href="javascript: takeTasks();">Принять</a>'
		}else if(fName == "oznakoml" || fName == "oznakoml2"){
			html += '<a class="button blue" href="javascript: oznakoml('+(fName == "oznakoml2")+');" title="'+(fName == "oznakoml2"?"Ознакомиться и закрыть":"")+'">Ознакомился</a>';
			if(showRepLink == 0) showRepLink = 2
		}else if(fName == "noexec"){
			html += '<a class="button blue" href="javascript: noexec();" title="не требует исполнения">Виза</a>'
			if(showRepLink == 0) showRepLink = 2
		}else if(fName == "2acq"){
			html += '<a class="button gray" href="javascript: send2acq();" >Ознакомить</a>'
			if(showRepLink == 0) showRepLink = 2
		}else if(fName == "reqToChange"){
			html += '<a class="button gray mini" href="javascript: reqToChange();" title="Создать запрос на изменение рассматривающего">Запрос на изм.</a>';
			if(showRepLink == 0) showRepLink = 2
		}else if(fName.indexOf('newTask')>-1){
			var actionType = fName.split('newTask')[1];
			var doneUrl = "";
			if(actionType == ''){
				actionType = '&targetInit=execution&typeResolution=first&parentCardId='+DocsVars.currentDoc;
				doneUrl = '%2Fportal%2Fauth%2Fportal%2Fboss%2Ffolder%2F%3Fitem%3D'+DocsVars.currentDoc;
			}else if(actionType == 'Consider'){
				actionType = '&targetInit=consideration&typeResolution=first&parentCardId='+DocsVars.currentDoc;
				doneUrl = '%2Fportal%2Fauth%2Fportal%2Fboss%2Ffolder%2F%3Farea%3D'+DocsVars.currentArea;
			}
			html += '<a onclick="lockScreen()" href="/portal/auth/portal/boss/resolution/Content?formAction=init&action=1&stateInit=initCreate'+actionType+'&typeLink=b&backURL=%2Fportal%2Fauth%2Fportal%2Fboss%2Ffolder%2F%3Fitem%3D'+DocsVars.currentDoc+'&doneURL='+doneUrl+'" class="button gray back-mark">Поручить</a>';
			html += '<a onclick="lockScreen()" href="/portal/auth/portal/boss/massResolution/Content?formAction=init&action=1&stateInit=initCreate&parentCardId='+DocsVars.currentDoc+'&backURL=%2Fportal%2Fauth%2Fportal%2Fboss%2Ffolder%2F%3Fitem%3D'+DocsVars.currentDoc+'&doneURL='+doneUrl+'" class="button gray mini back-mark" title="Создать массовую резолюцию">Мас.поручение</a>';
		}else if(fName == "execNow"){
			html += '<a class="button blue" href="javascript: createReport();" title="Исполнить сейчас">Исполнить</a>'
		}else if(fName){
			alert("Неизвестный тип кнопки: "+fName)
		}
	}
	if(html !=''){
		var place = dojo.byId("buttonsPlace");
		if(!place){
			place = dojo.byId("DocumentDetails");
			html = '<div class="buttons">'+html+'</div>'
		}
		//html +=( (showRepLink == 2)? '<p><a href="'+url+'" target="_blank" style="margin-left:10px">Ход исполнения</a></p>':"")
		place.innerHTML += html
	}
}

function hideButtons(){
	var forms = getForms(['sendForm']);
	if((!forms) || forms.length == 0){
		dojo.destroy(dojo.byId('approveButton'));
	}
}

function getForms(names){
	//получение всех форм по части названия
	var q = dojo.query("FORM", dojo.byId("DocumentDetails"));
	if( !q || q.length == 0) return false;

	var arr = [];

	for(var i=0; i< q.length; i++){
		for(var n=0; n< names.length; n++){
			if(q[i].id.indexOf(names[n])>-1){
				arr.push(q[i])
			}
		}
	}
	return arr
}

function submitForms(forms, i, handler){
	//пакетный сабмит форм
	if(!i) i=0;
	dojo.byId("DocumentDetails").style.cursor = "wait";
	if(i == forms.length ){
		var flagLock = "off";
		if(handler){
			flagLock = handler()
		}
		if(flagLock && flagLock=="on"){
			//тут пока ничего нет.
		}else{
			unlockScreen();
		}		
		return
	}
	var f = forms[i];
		/* backup 
		var elem = document.createElement('input')
		elem.name = 'batchAsync'
		elem.value = 'true'
		elem.type = 'hidden'
		f.appendChild(elem)*/
	if (i == forms.length-1 && f.batchAsync)
		f.batchAsync.value = 'true last'
	lockScreen();
	dojo.xhrPost({
		form: forms[i],
		preventCache: true,
		load: function(resp, ioArgs){
				unlockScreen();
				if(validateRequestResult(resp)){
					submitForms(forms, i+1, handler);
				}
			 }
	});
}

function validateRequestResult(resp){
	if ((resp != "") && (resp.indexOf("error=")>-1)){
		handleError(resp);
		return false;
	}
	return true;
}

function handleError(resp) {

	var errorValue = parseErrorValue(resp);
	if (errorValue && (errorValue != "") )
		alertDialog({text:errorValue,title:"ошибка"});

	//restore cursor to default
	if(dojo.byId("DocumentDetails"))
		dojo.byId("DocumentDetails").style.cursor = "default";

}

function parseErrorValue(resp) {
	if (!resp)
		return "";
	var start = resp.indexOf("error=");
	if (start <=-1)
		return "";

	var errorLabel = "error=";
	var errorValue = resp.substr(start + errorLabel.length);

	return errorValue;
}


function submitForm(f,handler){
	lockScreen();
	dojo.xhrPost({
		form: f,
		preventCache: true,
		load: function(resp, ioArgs) {
			unlockScreen();
			if(validateRequestResult(resp)){
        		handler();
        	}			
		}
	});
}

function sendTasks(){
	//,'leaveForm'
	//отправка черновиков на исп.
	var forms = getForms(['sendForm']);
	
	if(forms && forms.length >0){
		submitForms(forms, 0, function(){
			//Если папка - Избранное, На исполнении, Поручено подчин, Личный контроль, Контроль, Утвердить, Мои документы
			if(DocsVars.currentArea == "8058" || DocsVars.currentArea == "9102"
				|| DocsVars.currentArea == "8610" || DocsVars.currentArea == "8543"
				|| DocsVars.currentArea == "8544" || DocsVars.currentArea == "45021"
				|| DocsVars.currentArea == "8050") {
				reopenCurDoc();
			} else {
				cardInPrivate(forms);
			}
			return "on";
		})
	}else{
		alert("Черновики не найдены")
	}
}

function checkResolutionDrafts(){
	var result = "";
	var forms = getForms(['sendForm']);
	forms.forEach(function (entry){
		var executor = dojo.byId('draft_executor_'+entry.card.value);
		if(!(executor && executor.innerHTML && executor.innerHTML.length > 0)){
			result = result + '\tУ поручения ' + entry.card.value + ' не заполнен исполнитель.\n';
		}
		
		var text = dojo.byId('draft_text_'+entry.card.value);
		if(!(text && text.innerHTML && text.innerHTML.length > 0)){
			result = result + '\tУ поручения ' + entry.card.value + ' не заполнен текст резолюции.\n';
		}

		var term = dojo.byId('draft_term_'+entry.card.value);
		if(term && term.innerHTML && term.innerHTML.length > 0) {
			var termDate = new Date(term.innerHTML);
			termDate.setDate(termDate.getDate() + 1)
			if (termDate <= new Date()){
				result = result + '\tУ поручения ' + entry.card.value + ' срок исполнения меньше текущей даты.\n';
			}
		}
	});
	if(result.length > 0){
		result = 'Действие не было произведено, так как:\n' +  result;
		result = result + 'Исправьте причины или свяжитесь со службой технической поддержки.';
	}
	return result;
}

function cardInPrivate(sendForm){
	var url = getUrlSendFormCard(sendForm);
	submitGet(url, function(data){		
		var forms = getForms(getListForm());
		var leaveForms = getForms(getListLeaveForm(data));
		if(forms && forms.length >0){
			submitForms(forms, 0, function(){
				var noProcessedForms = replaceProcessedForms(leaveForms ,getForms(['leaveForm']));
				//replaceProcessedForms(forms,allForms);
				if(noProcessedForms && noProcessedForms.length>0){
					reopenCurDoc();
				}else{
					removeCurrentDocFromList();
				}
			})
		}else{
			var noProcessedForms = replaceProcessedForms(leaveForms ,getForms(['leaveForm']));
			if(noProcessedForms && noProcessedForms.length>0){
				reopenCurDoc();
			}else{
				removeCurrentDocFromList();
			}
		}	
	});	
}

function replaceProcessedForms(processedForms, allForms){
	var arr = [];
	var temp=false;
	if(allForms && allForms.length>0){
		for(var i=0; i<allForms.length; i++){
			flag=true;
			for(var j=0; j<processedForms.length; j++){
				if(allForms[i].id==processedForms[j].id){
					flag=false;
					continue;
				}
			}
			if(flag==true){
				arr.push(allForms[i]);
			}
		}		
	}
	return arr;
}

/*
 * Это только для кнопки утвердить
 * Формирует список форм для поиска
 */
function getListForm(){
	var arr = [];
	arr.push('oznakomlForm');
	arr.push('noexecForm');
	return arr;
}

function getListLeaveForm(data){
	var arr = [];
	if(!data){
		return arr;
	}
	var cardsId = data.cards;
	for(var i=0; i<cardsId.length; i++ ){
		arr.push('leaveForm'+cardsId[i])
	}
	return arr;
}

function getUrlSendFormCard(sendForm){
	var url="/DBMI-UserPortlets/parentReport";
	var cards="?cards=";
	if(sendForm && sendForm.length >0){
		for(var i=0; i< sendForm.length; i++){
			if(i!=0){
				cards=cards+","
			}
			cards=cards+sendForm[i].card.value
		}
	}
	url=url+cards;
	return url;
}

function submitGet(url, handler){
	  lockScreen();
	  var xhrArgs = {
			    url: url,
			    handleAs: "json",
			    preventCache: true,
			    load: function(data){
			    	unlockScreen()
			    	if(handler){			    		
			    		handler(data);
			    	}
			    }

	  }
	  dojo.xhrGet(xhrArgs)
}



function takeTasks(){
	//принять отчеты об исп.
	var forms = getForms(['takeForm']);
	if(forms && forms.length >0){
		submitForms(forms, 0, reopenCurDoc)
	}else{
		alert("Непринятые отчеты не найдены")
	}
}

function oznakoml(remove){
		//ознакомился
	var forms = getForms(['oznakomlForm']);
	if(forms && forms.length >0){
		var f = function(){
			removeCurrentDocFromList();
			};
		var func = remove?f : reopenCurDoc;

		submitForms(forms, 0, func)
	}else{
		alert("визы ознакомления не найдены")
	}
}
function noexec(id, templ){
		//не исполняется
	var forms = getForms(['noexecForm']);
	if(forms && forms.length >0){
		submitForms(forms, 0, function(){
			removeCurrentDocFromList();
		})
	}else{
		alert("визы ознакомления не найдены")
	}
}

function send2acq(){
	lockScreen();
	window.location.href = '/portal/auth/portal/boss/acquaintance/Content?&formAction=init&action=1&cardId='+DocsVars.currentDoc+'&backURL=%2Fportal%2Fauth%2Fportal%2Fboss%2Ffolder%2F%3Fitem%3D'+DocsVars.currentDoc+'&doneURL=%2Fportal%2Fauth%2Fportal%2Fboss%2Ffolder%2F%3Fitem%3D'+DocsVars.currentDoc;
}

function reqToChange(){
	lockScreen();
	window.location.href = '/portal/auth/portal/boss/requestToChangeCons/Content?formAction=init&action=1&stateInit=initCreate&parentCardId='+DocsVars.currentDoc+'&backURL=%2Fportal%2Fauth%2Fportal%2Fboss%2Ffolder%2F%3Fitem%3D'+DocsVars.currentDoc+'&doneURL=%2Fportal%2Fauth%2Fportal%2Fboss%2Ffolder%2F%3Fitem%3D'+DocsVars.currentDoc;
}

function getAttach(index){
	var i = index || 0;

    var applet = dojo.byId("CryptoApplet");
    var obj = dojo.byId("PDFView");

    if(self.attachPath && self.attachPath != ""){
    	obj.CloseAllDocuments()
    }

    try{
    	var url =  self.location.protocol + "//" + self.location.host + getAttachUrl(AttachmentsArr[i].split("|")[0]);
        try{console.log(url)}catch(e){}
	    var path = applet.downloadAndSaveMaterial(url, document.cookie);

    }catch(ex){alert("Ошибка загрузки вложения. " + ex); return}

    if(path=="failure"){alert("Ошибка загрузки вложения. "); return}

    try{console.log(path)}catch(e){}
    obj.SetDevInfo("PVA20-MU3MQ-ORO0E-IQIWU-HQ1XB-4V6HF","PDFX3$Henry$300604_Allnuts#");
	obj.OpenDocument(path, 0, 0, 0);
	obj.SetProperty("International.LocaleID", 0, 0);
	obj.SetProperty('View.Bars[0].Visible', 1, 0); //меню
    obj.SetProperty('View.Bars[3].Visible', 1, 0); //выделение
    obj.SetProperty('View.Bars[4].Visible', 1, 0); //масштаб
    obj.SetProperty('View.Bars[4].Visible', 1, 0); //масштаб
    obj.SetProperty('View.Bars[6].Visible', 1, 0); //пометки
    obj.SetDocumentProperty(0,'View.Bars[1].Visible', 1, 0); //страницы
    obj.SetDocumentProperty(0,'View.Bars[2].Visible', 1, 0); //расположение
    obj.SetProperty('Documents[0].Pages.Zoom', 80, 0); //зум
    self.attachPath = path;
}

//работа с аттачами через плагин
function getAttachUrl(id){
	return "/DBMI-UserPortlets/MaterialDownloadServlet?MI_CARD_ID_FIELD="+id+"&noname=1&pdf=1";
}

function saveAttach(){
	var obj = dojo.byId("PDFView");
    obj.SaveDocument(0,self.attachPath,0,4)
    var applet = dojo.byId("CryptoApplet");
    var res = applet.postMaterialCard(
    	self.location.protocol + "//" + self.location.host+'/DBMI-UserPortlets/servlet/arm-upload',
        	document.cookie, self.attachPath, "", DocsVars.currentDoc);

}

function writeAttachments(){
	var obj = dojo.byId("PDFView");
    if(!obj || !obj.OpenDocument){
    	var html = "";
    	for(var i=0; i<AttachmentsArr.length; i++){
        	html += '<iframe src="'+getAttachUrl(AttachmentsArr[i].split("|")[0])+'" width="99%"  height="98%" border="0" scrolling="no" >';
        }
    	if(AttachmentsArr.length == 0) {
    		html = '<H1>Документ не имеет вложений</H1>';
    	}
    	dojo.query('.document')[0].innerHTML = html
        return;
    }

	var appl = '<applet name="CryptoApplet"	id="CryptoApplet" codebase="/DBMI-UserPortlets"'+
		'archive="SJBCrypto.jar" code="com.aplana.crypto.CryptoApplet.class"	WIDTH="1" HEIGHT="1">'+
		'<param name="runOnLoad" value="getAttach()"><PARAM name="separate_jvm" value="true">'+
		'<H1>WARNING!</H1>The browser you are using is unable to load Java Applets!</applet>';


	//NOT USED (commented for BR4J00038081)
    /*if(AttachmentsArr.length>1){
    	var html = "<b>Все вложения документа:</b> ";
    	for(var i=0; i<AttachmentsArr.length; i++){
        	html += '<a href="javascript://" onClick="getAttach('+i+'); return false">'+AttachmentsArr[i].split("|")[1]+'</a>';
            if(i < AttachmentsArr.length-1){
            	html+= ", "
            }
        }
        html = '<div id="allAttachments">'+html+'</div>'
    }*/

    dojo.create("div", {innerHTML: appl}, "document_body", "last");

    var optContainer = dojo.byId('control') || dojo.query('.options')[0];
    optContainer.innerHTML += '<a class="save" href="javascript: //" onClick="saveAttach(); return false" title="Сохранить">&nbsp;</a>'
}

function switchView(area,item){
	//автоматич. переход в режим просомтра вложений
	if(area == "9602" || area == "11413192" || area == "9562" || area == "9362"){
		location.replace("/portal/auth/portal/boss/document?item="+item)
	}
}

function printDocsListNav(cur, total){
	//генерация перехода по страницам списка документов
	var hrefStart = '<a href="/portal/auth/portal/boss/folder/?page=';
	var html = prepareNavigation(cur, total, hrefStart);
	document.write('<div class="navigator">' + html + '</div>');
	dojo.query("#menu ul li a").forEach(function(a){dojo.attr(a, 'href',a.href + "&page=1")})
}

function printAdvancedSearchNav(cur, total){
	//генерация перехода по страницам результатов расширенного поиска
	var hrefStart = '<a href="/portal/auth/portal/boss/advancedSearchRes/ExtendedSearchWindow?action=1&ACTION_FIELD=SEARCH&page=';
	var html = prepareNavigation(cur, total, hrefStart);
	document.write('<div class="navigator adv_search">' + html + '</div>');
}

function prepareNavigation(cur, total, hrefStart) {
	var html = '';
	if (cur > 2) {
		html += hrefStart +'1" class="first"> </a>';
	}
	if (cur > 1) {
		html += hrefStart + (cur-1) +'" class="prev"> </a>';
	}
	html += '<p>'+cur + " из " + total+'</p>';
	if (cur < total) {
		html += hrefStart + (cur+1) + '" class="next"> </a>';
	}
	if (cur < (total-1)) {
		html += hrefStart + total + '" class="last"> </a>';
	}
	return html;
}

function lockScreen() {
   var lock = dojo.byId('lockPane');
   lock.className = 'lock_on';
   lock.innerHTML = '<img src="/DBMI-Portal/js/dbmiCustom/images/dbmi_loading.gif" class="loading_image">';
}

function lockDsScreen() {
	   var lock = dojo.byId('lockPane');
	   lock.className = 'lock_on';
	   lock.innerHTML = '<img src="/DBMI-Portal/js/dbmiCustom/images/ds_screen_lock_text.png" class="loading_image">';
	}

function unlockScreen() {
   var lock = dojo.byId('lockPane');
   lock.className = 'lock_off';
}

function getCookie(name) {
    var ne = name + '=';
    var ca = document.cookie.split(';');
    for (var i = 0; i < ca.length; i++) {
        var c = ca[i];
        while (c.charAt(0) == ' ') {
            c = c.substring(1, c.length);
        }
        if (c.indexOf(ne) == 0) {
            return c.substring(ne.length, c.length);
        }
    }
    return false;
}

function switchAttachment(attachmentsSelect) {
	selectedAttachmentId = attachmentsSelect.value;
	dojo.query('.document > iframe')[0].src = getAttachUrl(selectedAttachmentId);
}


function saveAttachment(downloadLink) {
	var attachmentsCombo = document.getElementById("select_attachment");
	var selectedAttachmentId = attachmentsCombo.value;
	var lengthAttachments = attachmentsCombo.options.length;
	if(lengthAttachments <= 0) {
		alert("Документ не имеет вложений");
		return false;
	} else if(selectedAttachmentId == null || "" == dojo.trim(selectedAttachmentId)) {
		alert("Вложение не выделено");
		return false;
	}
	var downloadMaterialURL = "/DBMI-UserPortlets/MaterialDownloadServlet?MI_CARD_ID_FIELD="+selectedAttachmentId;
	downloadLink.href = downloadMaterialURL;
	return true;
}

String.prototype.startsWith = function(str) {
	return (this.match("^"+str)==str);
}

function linkDoc() {
	var q = dojo.query(".row_sel");
	if(!q || q.length == 0){ alert("Не выделен документ"); return }
	// get id from row
	var id = q[0].id.split("row")[1];

	var dlgInput = dojo.byId('linkDocDlg_baseCardId');
	if(dlgInput) {
		dlgInput.value = id;
	}

	var dlg = dijit.byId('linkDocDlg');
	dlg.show();
}

function createLinkedDoc() {
	var dlg = dijit.byId('linkDocDlg');
	if(dlg) {
		dlg.hide();
	}
	var backUrl = '/portal/auth/portal/boss/folder?item='+DocsVars.currentDoc;
	handleCreateDocument(backUrl, DocsVars.currentDoc);
}

function saveCurDocLinks(ids) {
    var linkDocForm = dojo.byId("linkDocForm");
    if(linkDocForm) {
    	linkDocForm.action = "/DBMI-UserPortlets/content?form=modify&item=" + DocsVars.currentDoc;
    	linkDocForm.add_E_JBR_DOCL_RELATDOC.value = ids;
    	linkDocForm.card.value = DocsVars.currentDoc;
    	submitForm(linkDocForm, reopenCurDoc);
    }
}

function execER(id){
	//исполнение внешних резолюций ОГ
	if(dijit.byId("inp_dialogDate")) dijit.byId("inp_dialogDate").destroy();

	DocsVars.currentDialog = new textDialog({
		title: 'Комментарий об исполнении',
        moreHTML:'<br>Дата исполнения: <input dojoType="dijit.form.DateTextBox" type="text" value="now" id="inp_dialogDate" style="width:130px;margin-bottom:10px">',
		onsubmit: function(repText){
			if (repText != null && repText != "") {
				var f = dojo.byId('execERForm'+id);
				if(f.set_T_JBR_DMSI_ER_EXEC_CMT) f.set_T_JBR_DMSI_ER_EXEC_CMT.value = encodeURIComponent(repText);
				if(f.set_D_JBR_DMSI_ER_ACTL_DT) f.set_D_JBR_DMSI_ER_ACTL_DT.value = dojo.byId("inp_dialogDate").value;

				submitForm(f, function(){
						reopenCurDoc()
				})
			}else {
				throw "report_is_empty";
			}
		}
	})
}

function cancelTask(id){
	DocsVars.currentDialog = new textDialog({
		title: 'Причина отклонения',
		onsubmit: function(repText){
			if(repText != null && repText != "") {
				var f = dojo.byId('cancelForm'+id);
				f.set_T_JBR_RIMP_COMMENT.value = encodeURIComponent(repText);

				submitForm(f, function(){
					removeCurrentDocFromList();
				})
			}else {
				throw "report_is_empty";
			}
		}
	})
}

function initDelegationEditFormControls() {
    var delegationEditForm = dojo.byId('delegationForm');
    if(!delegationEditForm) {
        return;
    }

    var delegationEditForm_fromPersons = dojo.byId('delegationEditForm_fromPersonsValue');
    var delegationEditForm_toPersons = dojo.byId('delegationEditForm_toPersonsValue');

    self.fromPersonsStore = new dojo.data.ItemFileReadStore({
        data: {identifier: 'id', label: 'name', items: dojo.fromJson(delegationEditForm_fromPersons.innerHTML)}});
   
    var toPersonsStore = new dojo.data.ItemFileReadStore({
        data: {identifier: 'id', label: 'name', items: dojo.fromJson(delegationEditForm_toPersons.innerHTML)}});

    var delegationEditForm_fromDate = dojo.byId('delegationEditForm_fromDateValue');
    var delegationEditForm_toDate = dojo.byId('delegationEditForm_toDateValue');
    var delegationEditForm_userFrom = dojo.byId('delegationEditForm_userFromValue');
    var delegationEditForm_userTo = dojo.byId('delegationEditForm_userToValue');

    var delegationEditForm_isFromPersonSelectable = dojo.byId('delegationEditForm_isFromPersonSelectableValue');
    var isFromPersonSelectable = delegationEditForm_isFromPersonSelectable ? delegationEditForm_isFromPersonSelectable.value : 'false';
    isFromPersonSelectable = isFromPersonSelectable == 'true';
    
    var delegationEditForm_isDelegationEditable = dojo.byId('delegationEditForm_isDelegationEditable');
    var isDelegationEditable = delegationEditForm_isDelegationEditable ? ( delegationEditForm_isDelegationEditable.value == 'true' ? true : false ) : false;
    
    var delegationButtons = dojo.byId('buttonsPlace');
    var delegationDeleteBtn = dojo.byId('deleteDelegation');
	
	if(isDelegationEditable) {
		if(delegationButtons && dojo.hasAttr(delegationButtons, "style") && dojo.style(delegationButtons, "display") == "none")
			dojo.removeAttr(delegationButtons, "style");
		if(delegationDeleteBtn && dojo.hasAttr(delegationDeleteBtn, "style") && dojo.style(delegationDeleteBtn, "display") == "none")
			dojo.removeAttr(delegationDeleteBtn, "style");
			
	} else {
		if(delegationButtons)
			dojo.style(delegationButtons, "display", "none");
		if(delegationDeleteBtn)
			dojo.style(delegationDeleteBtn, "display", "none");
	}

    widget = dijit.byId('set_D_DLGT_DATE_START');
    if(widget) {
        widget.destroy();
    }
    var date;
	if(delegationEditForm_fromDate)
		date = parseDate(delegationEditForm_fromDate.value, '-');
	
    var widget = new dbmiCustom.DateTimeWidget({
        nameDate: 'set_D_DLGT_DATE_START',
        valueDate: date,
        isShowTime: false,
        _widthDate: 100
    });
    widget.placeAt(dojo.byId("delegationEditForm_fromDate"));

    widget = dijit.byId('set_D_DLGT_DATE_END');
    if(widget) {
        widget.destroy();
    }
    date = null;
	if(delegationEditForm_toDate)
		date = parseDate(delegationEditForm_toDate.value, '-');
	
    widget = new dbmiCustom.DateTimeWidget({
        nameDate: 'set_D_DLGT_DATE_END',
        valueDate: date,
        isShowTime: false,
        _widthDate: 100
    });
    widget.placeAt(dojo.byId("delegationEditForm_toDate"));

    widget = dijit.byId('set_U_DLGT_FROM');
    if(widget) {
        widget.destroy();
    }
    widget = new dijit.form.FilteringSelect({
        name: 'set_U_DLGT_FROM',
        store: fromPersonsStore,       
        searchAttr: 'name',
        autoComplete: true,
        style: 'width:80mm',
        disabled: !isFromPersonSelectable
        }, dojo.byId('set_U_DLGT_FROM')
    );
    
    if(delegationEditForm_userFrom){
    	widget.setValue(delegationEditForm_userFrom.value)
    }

    widget = dijit.byId('set_U_DLGT_TO');
    if(widget) {
        widget.destroy();
    }
    widget = new dijit.form.FilteringSelect({
        name: 'set_U_DLGT_TO',
        store: toPersonsStore,
        value: delegationEditForm_userTo ? delegationEditForm_userTo.value : null,
        searchAttr: 'name',
        autoComplete: true,
        style: 'width:80mm'
        }, dojo.byId('set_U_DLGT_TO')
    );
}

//parsing date without timestamp delimiting by given delimiter in (1.year,2.month,3.day) order
function parseDate(dateStr, delim) {
	var parts = dateStr.split(delim);
	var year = parts[0];
	var month = parts[1];
	var day = parts[2].split(' ')[0];
	return new Date(year, month-1, day);
}

function saveDelegation() {   
	
	var f = dojo.byId('delegationForm');
	
	var msg = '';
	var errors = [];
	
	var error = false;
	
    var userFromInput = dijit.byId('set_U_DLGT_FROM');
    var userToInput = dijit.byId('set_U_DLGT_TO');
    
    var dateFromWidget = dijit.byId('set_D_DLGT_DATE_START');
    var dateToWidget = dijit.byId('set_D_DLGT_DATE_END');
    
    var empty = false;
    
    if(userFromInput.value == null || userFromInput.value == '') {
    	errors.push("'От кого'");
    	empty = true;
    }
    
    if(userToInput.value == null || userToInput.value == '') {
    	errors.push("'Кому'");
    	empty = true;
    }
    
    if(dateFromWidget.value == null || dateFromWidget.value == '') {
    	errors.push("'Дата начала делегирования'");
	}
    
    if(dateToWidget.value == null || dateToWidget.value == '') {
	    errors.push("'Дата окончания делегирования'");
	}
    
    var dateFrom = dateFromWidget.value;
	var dateTo = dateToWidget.value;
	
    if(errors.length > 0) {
		if(errors.length == 1) {
			msg = 'Поле '; 
		} else {
			msg = 'Поля ';
		}
		for(var i=0; i<errors.length; i++) {
			msg += errors[i];
			if(i != (errors.length-1)) {
				msg += ', ';
			}
		}
		if(errors.length == 1) {
			msg += ' должно быть заполнено';
		} else msg += ' должны быть заполнены';
		error = true;
	} else if(dateFrom && dateTo) {
		if(dateFrom > dateTo) {
			msg = 'Дата начала делегирования не может быть позже даты окончания';
		    error = true;
		}
	}
    
    if(!empty && userFromInput.value == userToInput.value) {
    	msg ='Значения полей "От кого" и "Кому" не должны совпадать';
    	error = true;
    }
    
    if(error) {
    	alertDialog({text: msg,
			buttons:[
			{id:"acceptConfirmYes",title:"OK",action:function(){
				self.AlertDialog.hide()
				}}
			]
    	});
    	return;
    }
    
    var dateFromInput = dojo.query('[name=set_D_DLGT_DATE_START]', f)[0];
	dateFromInput.value = dojo.date.locale.format(dateFromWidget.value, {datePattern: "dd.MM.yyyy", selector: "date"});
			        
	var dateToInput = dojo.query('[name=set_D_DLGT_DATE_END]', f)[0];
	dateToInput.value = dojo.date.locale.format(dateToWidget.value, {datePattern: "dd.MM.yyyy", selector: "date"});
    
    submitForm(f, function(){
    	// кэш не сбрасываем
    	
        //if(location.href.indexOf('resetCache')==-1){
        //    location.href = location.href + "&resetCache=true"
        //}else{
            window.location.reload(false)
       // }
    })
}

function cancelDelegation() {
	reopenCurDoc();
}

function prepareDelegationCreation() {
    lockScreen();

    var f = dojo.byId('delegationForm');
    if(f) {
        f.action = f.action + "&template=2290";
    } else {
        dojo.byId("DocumentDetails").innerHTML = getDelegationFormHtml();
        initDelegationEditFormControls();
        f = dojo.byId('delegationForm');
    }

    var widget = dijit.byId('set_D_DLGT_DATE_START');
    if(widget) {
        widget.setValue(null);
    }

    widget = dijit.byId('set_D_DLGT_DATE_END');
    if(widget) {
        widget.setValue(null);
    }

    widget = dijit.byId('set_U_DLGT_FROM');
    if(widget) {
    	widget.setDisabled(true);
		var s = dojo.query("div.userarea select")[0];
		if(s) {
			var cur = dojo.trim(s.options[s.selectedIndex].text);
			if(self.fromPersonsStore && self.fromPersonsStore._arrayOfAllItems.length){
		        dojo.forEach(self.fromPersonsStore._arrayOfAllItems, function(item){
		          	if(item.name[0].indexOf(cur)==0){
		          		widget.setValue(item.id[0]);
		          	}
		        })
			} else if(self.fromPersonsStore && self.fromPersonsStore._jsonData && self.fromPersonsStore._jsonData.items.length) {
				dojo.forEach(self.fromPersonsStore._jsonData.items, function(item){
		          	if(item.name.indexOf(cur)==0){
		          		widget.setValue(item.id);
		          	}
		        })
			}
		}
		// Если используемый юзер является руководителем департамента, то открыть на редактирование выпадающий список "От кого", 
		// в противном случае оставить закрытым от редактирования.
		var delegationEditForm_isFromPersonSelectable = dojo.byId('delegationEditForm_isFromPersonSelectableValue');
	    var isFromPersonSelectable = delegationEditForm_isFromPersonSelectable ? delegationEditForm_isFromPersonSelectable.value : 'false';
	    if(isFromPersonSelectable == 'true') {
	    	widget.setDisabled(false);
	    }
	}

    widget = dijit.byId('set_U_DLGT_TO');
    if(widget) {
        widget.setValue(null);
    }
    
    var delegationButtons = dojo.byId('buttonsPlace');
    if(delegationButtons && dojo.hasAttr(delegationButtons, "style"))
    	dojo.removeAttr(delegationButtons, "style");

    unlockScreen();
}

function deleteDelegation() {
    lockScreen();

    var f = dojo.byId('deleteDelegationForm');

    dojo.xhrPost({
		form:  f,
		preventCache: true,
		load: function(resp, ioArgs){
            unlockScreen();
            removeCurrentDocFromList();
 		}
    });
}

function getDelegationFormHtml() {
    return  '<DIV id="DocumentDetailsLeftFrame" class="doc_info">' + 
            '<table cellpadding="0" cellspacing="0" width="100%">' +
              '<tbody><tr><td valign="top">' +
                    '<form action="/DBMI-UserPortlets/content?form=modify&template=2290" method="post" id="delegationForm">' +
                      '<input name="success" value="/portal/auth/portal/boss/folder/?area=8064" type="hidden">' +
                      '<H3>Новое назначение делегата</H3>' +
                      '<p>' +
                      '<span class="fieldTitle">От кого:</span>' +
                      '<br/>' +
                      '<input id="set_U_DLGT_FROM"/>' +
                      '<br/>' +                      
                      '<span class="fieldTitle">Кому:</span>' +
                      '<br/>' +
                      '<input id="set_U_DLGT_TO"/>' +
                      '<br/>' +
                      '</p>' +
                      '<div id="delegationEditForm_fromDate" style="float:left"><span class="fieldTitle">Начиная с:</span></div>' +
                      '<div id="delegationEditForm_toDate" style="float:left"><span class="fieldTitle">Заканчивая:</span></div>' +
                    '</form>' +
                  '</td>' +
                  '<td>' +
                      '<div id="buttonsPlace" valign="bottom">' +
                         '<ul>' +
                              '<a href="javascript://" onclick="saveDelegation()" class="button blue">Сохранить</a>' +
                              '<a href="javascript://" onclick="cancelDelegation()" class="button gray">Отменить</a>' +
                          '</ul>' +
                      '</div>' +
                  '</td>' +
                '</tr></tbody>' +
              '</table>' +
            '</DIV><DIV id="DocumentDetailsRightFrame" class="doc_info"></DIV>';
}

function hideShowElementsList(id, text) {
	  var hid = dojo.byId(id);
	  if(hid.style.display=='none')
		  hid.style.display='block';
	  else
		  hid.style.display='none';
	  var a = dojo.query('a.'+id);
	  var new_text = a[0].innerHTML;
	  a[0].innerHTML=text;
	  a[0].title=text;
	  a[0].href='javascript:hideShowElementsList(\'' + id + '\', ' + '\'' + new_text + '\')';

}

function checkAppletExists(onlyCheck){
	var ap = document.applets[0];
	if(!ap) return false;
	try{
	if(!ap.getFileName){
		if(!onlyCheck){
			//надо скрыть, но предупреждение оставить кроме айпада
			if(navigator.userAgent.indexOf('iPad')>-1 || navigator.userAgent.indexOf('iPhone')>-1){
				ap.style.display = "none"
			}
			dojo.query(".NoAppletHide").style({"display":"none"})
		}
		return false
	}
	}catch(ex){}
	
	return true
}

function editIndependentResolution() {
	lockScreen();
	url = '/portal/auth/portal/boss/indepResolution/Content?'
		+ 'action=1'  
		+ '&formAction=initIndepRes'
		+ '&stateInit=initEdit'
		+ '&editCardId=' + DocsVars.currentDoc
		+ '&backURL=%2Fportal%2Fauth%2Fportal%2Fboss%2Ffolder%3Fitem%3D' + DocsVars.currentDoc 
		+ '&doneURL=%2Fportal%2Fauth%2Fportal%2Fboss%2Ffolder%3Farea%3D' + DocsVars.currentArea;
	window.location = url;
}

function miniLog(){
	 var xhrArgs = {
		      url: "/DBMI-UserPortlets/minilog/log?error=false",
		      postData: navigator.userAgent
		    }
	 dojo.xhrPost(xhrArgs);
}

function emptyFolderMessage(){
	alert('Не выделен документ');
	//если вдруг экран будет 'заблокирован'
	unlockScreen();
}

function getQueryVariable(variable) {
    var query = window.location.search.substring(1);
    var vars = query.split('&');
    for (var i = 0; i < vars.length; i++) {
        var pair = vars[i].split('=');
        if (decodeURIComponent(pair[0]) == variable) {
            return decodeURIComponent(pair[1]);
        }
    }
}

function openTargetDoc(){
	if(getQueryVariable('directUrl')){
		DocsVars.currentDoc = getQueryVariable('item');
		DocsVars.currentView = '41462,8578,43340,43540,43541,543812,543813,42743,29021';
		loadContentServlet(true);
	}
}

function updateTittleIfNeed(){
	if(getQueryVariable('directUrl')){
		var detailedDocNameInput = dojo.byId("detailedDocName");
	
		var detailedDocName = null != detailedDocNameInput ? detailedDocNameInput.value : "";
	
		var docDetailsHeader = dojo.byId("docDetailsHeader");
		if(null != docDetailsHeader) {
			var theHeader = detailedDocName;
			theHeader = theHeader.replace(/\n/g,"").replace(/<br>/g,"");
			docDetailsHeader.innerHTML = theHeader;
		}
	}
}

function updareBackAndDoneURLsToDirectMode(){
	if(getQueryVariable('directUrl')){
		jQuery('a.button.gray, a.button.blue').each(function(i, el){
			var parts = el.href.split('?');
			if(parts.length >1){
				var params = parts[1].split('&');
				var newParams = '';
				params.forEach(function(param){
					var paramArr = decodeURIComponent(param).split('=');
					console.log(paramArr);
					if(paramArr[0] == 'backURL' || paramArr[0] == 'doneURL'){
						if(jQuery(el).hasClass('back-mark') || paramArr[0] == 'backURL' ){//backURL в любом случае на предыдущее расположение
							newParams = newParams+paramArr[0]+'='+encodeURIComponent(window.location.pathname+window.location.search);
						} else {//doneURL на список карточек, в котором были до перехода по уведомлению 
							newParams = newParams+paramArr[0]+'='+getQueryVariable('currentUrl');
						}
					} else {
						newParams = newParams +param;
					}
					newParams = newParams + '&';
				});
				newParams = newParams.substring(0, newParams.length - 1); 
				el.href = parts[0]+'?'+newParams;
				console.log(el.href);
			}
		});
	}
}

function formatDate(datum){
    var d = dojo.date.stamp.fromISOString(datum);
    if(d){
    	return dojo.date.locale.format(d, {selector: 'date', formatLength: 'medium'});
    } else {
    	return null;
    }
}

function openResolutionReportsList(){
	
	var reports;
	var reportsSize = 0;
	
	dojo.xhrGet({
		url: '/DBMI-UserPortlets/GroupExecutionCardServlet',	
		sync: true,
		content: {
			mode: 'HEAD'
		},
		handleAs: 'json',
		load: function(data) {
			reports = data;
			reportsSize = data.length;
		},
		error: function(error) {
			console.error(error);
		}
	});
	
	if(reportsSize==0){
		alert('Нет принятых отчетов на исполнение');
		return;
	}
	
	var reportsStore = new dojo.data.ItemFileReadStore({
	    data: {
	    	identifier: "id",
	    	items: reports
	    }
	});
	if(dijit.byId("groupExecDialog")){
		var grid = dijit.byId('groupExecGrid');
		grid.store.close();
		grid.setStore(reportsStore);
		dijit.byId("groupExecDialog").show();
		grid.update();
		return;
	}
	
	  var groupExecDialog = new dijit.Dialog({
	       title: 'Групповое исполнение поручений',
	       style: "height:auto; width: auto",
	       id:'groupExecDialog'
	       }
	   );
	  
	  groupExecDialog.setContent('<div id="groupExec" class="groupExec_on"></div>'+
	  							'<div id="actions" class="groupExecActions">'+
	  								'<button id="groupExecButtonNode" type="button"/>'+
	  								'<button id="cancelGroupExec" type="button"/>'+
	  							'</div>');
	groupExecDialog.show();
	  
	
	var layout = [	{field: "resolution",name: "Резолюция",width: "230px"},
					{field: "deadline",name: "Срок",width: "80px", formatter: formatDate},
					{field: "signer",name: "Подписант",width: "150px"},
					{field: "regnum",name: "Рег.номер",width: "100px"},
					{field: "regdate",name: "Рег. дата",width: "80px", formatter: formatDate},
					{field: "descr",name: "Краткое содержание",width: "230px"}
				];
	
	var table = new dojox.grid.EnhancedGrid({
	
	    store: reportsStore,
	    structure: layout,
	    id: "groupExecGrid",
	    rowsPerPage: reportsSize,
	    autoHeight: true,
	    sortInfo: 3,
	    plugins: {indirectSelection: {headerSelector:true, width:"40px", styles:"text-align: center;"}}
	}, document.createElement("div"));
	
	dojo.byId("groupExec").appendChild(table.domNode);
	
	table.startup();
	
	var groupExecButton = new dijit.form.Button({
		label: 'Сформировать отчет',
		id: "groupExecButton",
		onClick: function(){
			var selectedObjectds = dijit.byId('groupExecGrid').selection.getSelected();
			if(selectedObjectds.length == 0){
				alert('Не выбрано ни одного значения');
				return;
			}
			var selectedIds = [];
			selectedObjectds.forEach(function(el){
				selectedIds.push(el.id[0]);
			});
			url = "/portal/auth/portal/boss/resolutionReport/Content?formAction=INIT&action=1"+
			"&backURL=%2Fportal%2Fauth%2Fportal%2Fboss%2Ffolder"+"&REPORT_GROUP="+selectedIds.join("_");
			window.location = url;
		}
	}, "groupExecButtonNode").startup();
	
	var cancelGroupExec = new dijit.form.Button({
		label: 'Отмена',
		id: "cancelGroupExec",
		onClick: function(){
			dijit.byId("groupExecDialog").hide()
		}
	}, "cancelGroupExec").startup();

}

function addGroupExecutionButton(){
	validAreas = ['47447','8006','41460','8003','9102','8018','8019','8020','8021','47100',
	              '8014','8015','8016','8017','8543','8010','8011','8012','8013','8544',
	              '8610','8611','8612','8613','8614'];
	if(validAreas.indexOf(DocsVars.currentArea)!= -1){
		 var nl = dojo.query(".docDetailsLeftHeader .options").forEach(function (el){
			 var a = document.createElement("a");
			 a.className = "groupxecution";
			 a.href = "javascript:openResolutionReportsList()";
			 a.title="Групповое исполнение поручений";
			 el.insertBefore(a, el.firstChild);
		 });
	}
}

function openGroupResolutionDocumentList(){
	var docs;
	var docsSize = 0;

	dojo.xhrGet({
		url: '/DBMI-UserPortlets/GroupResolutionCardServlet',	
		sync: true,
		content: {
			mode: 'HEAD'
		},
		handleAs: 'json',
		load: function(data) {
			docs = data;
			docsSize = data.length;
		},
		error: function(error) {
			console.error(error);
		}
	});
	if(docsSize==0){
		alert('Нет документов для создания резолюций');
		return;
	}
	var docsStore = new dojo.data.ItemFileReadStore({
	    data: {
	    	identifier: "id",
	    	items: docs
	    }
	});
	
	if(dijit.byId("groupResolutionDialog")){
		var grid = dijit.byId('groupResolutionGrid');
		grid.store.close();
		grid.setStore(docsStore);
		dijit.byId("groupResolutionDialog").show();
		grid.update();
		return;
	}
	
	  var groupResolutionDialog = new dijit.Dialog({
	       title: 'Групповая резолюция',
	       style: "height:auto; width: auto",
	       id:'groupResolutionDialog'
	       }
	   );
	  
	  groupResolutionDialog.setContent('<div id="groupResolution" class="groupResolution_on"></div>'+
	  							'<div id="res_actions" class="groupExecActions">'+
	  								'<button id="groupResolutionButtonNode" type="button"/>'+
	  								'<button id="cancelGroupResolution" type="button"/>'+
	  							'</div>');
	  groupResolutionDialog.show();
	  
	
	var layout = [	{field: "template",name: "Шаблон",width: "100px"},
					{field: "regnum",name: "Рег.номер",width: "90px"},
					{field: "regdate",name: "Рег. дата",width: "80px", formatter: formatDate},
					{field: "descr",name: "Краткое содержание",width: "200px"},
	              	{field: "resolution",name: "Резолюция",width: "200px"},
	              	{field: "signer",name: "Подписант",width: "140px"},
					{field: "deadline",name: "Срок",width: "80px", formatter: formatDate}
				];
	
	var table = new dojox.grid.EnhancedGrid({
	
	    store: docsStore,
	    structure: layout,
	    id: "groupResolutionGrid",
	    rowsPerPage: docsSize,
	    autoHeight: true,
	    plugins: {indirectSelection: {headerSelector:true, width:"40px", styles:"text-align: center;"}}
	}, document.createElement("div"));
	
	dojo.byId("groupResolution").appendChild(table.domNode);
	
	table.startup();
	
	var groupResolutionButton = new dijit.form.Button({
		label: 'Сформировать резолюцию',
		id: "groupResolutionButton",
		onClick: function(){
			var selectedObjectds = dijit.byId('groupResolutionGrid').selection.getSelected();
			if(selectedObjectds.length == 0){
				alert('Не выбрано ни одного значения');
				return;
			}
			var selectedIds = [];
			selectedObjectds.forEach(function(el){
				selectedIds.push(el.id[0]);
			});
			url = "/portal/auth/portal/boss/resolution/Content?formAction=init&action=1&stateInit=initCreate&targetInit=prototype&typeResolution=group"+
			"&backURL=%2Fportal%2Fauth%2Fportal%2Fboss%2Ffolder&doneURL=%2Fportal%2Fauth%2Fportal%2Fboss%2Ffolder"+"&DOCS_GROUP="+selectedIds.join("_");
			window.location = url;
		}
	}, "groupResolutionButtonNode").startup();
	
	var cancelGroupResolution = new dijit.form.Button({
		label: 'Отмена',
		id: "cancelGroupResolution",
		onClick: function(){
			dijit.byId("groupResolutionDialog").hide()
		}
	}, "cancelGroupResolution").startup();
}

function addGroupResolutionButton(){
	validAreas = ['47447','8005','8006','41460','8002','8003','9102','8018','8019','8020','8021','47100',
	              '8014','8015','8016','8017','8543','8010','8011','8012','8013','8544',
	              '8610','8611','8612','8613','8614'];
	if(validAreas.indexOf(DocsVars.currentArea)!= -1){
		 var nl = dojo.query(".docDetailsLeftHeader .options").forEach(function (el){
			 var a = document.createElement("a");
			 a.className = "groupresolution";
			 a.href = "javascript:openGroupResolutionDocumentList()";
			 a.title="Создать групповую резолюцию";
			 el.insertBefore(a, el.firstChild);
		 });
	}
}

function declineDoc(showDialog, targetInit) {
	var declineForm = "/portal/auth/portal/boss/resolution/Content?&formAction=init&action=1&targetInit="+targetInit+"&decision=decline&stateInit=initCreate&parentCardId=" + DocsVars.currentDoc + "&backURL=%2Fportal%2Fauth%2Fportal%2Fboss%2Ffolder%2F%3Fitem%3D" + DocsVars.currentDoc + "&doneURL=%2Fportal%2Fauth%2Fportal%2Fboss%2Ffolder%2F%3Farea%3D" + DocsVars.currentArea;
	if (showDialog == true) {
		alertDialog({
			title:"Внимание!", 
			text:"В случае отклонения документа дальнейшая работа с ним будет невозможна. Вы уверены, что хотите выполнить операцию?",
			buttons:[
				{id:"acceptConfirmYes",title:"Да",action:function(){
					self.AlertDialog.hide();
					lockScreen();
					window.location.href = declineForm
					}},
				{id:"acceptConfirmNo",title:"Нет", action:function() {
					self.AlertDialog.hide();
				}}
				],
			closeButton: true
		});
	} else {
		lockScreen();
		window.location.href = declineForm
	}
}