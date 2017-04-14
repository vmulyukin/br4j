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
dojo.require('dijit._Widget');
//dojo.require('dijit._Templated');

//dojo.require('dojox.fx');
dojo.require('dojox.timing');

dojo.provide('dbmiCustom.Notifier');
dojo.declare(
	'dbmiCustom.Notifier',
	[dijit._Widget],
	{

		panel: null,
		expandPanel: null,
		lastReceived: 0,
		fixed: false,
		extraMessages: 0,
		notifs: [],
		groups: [],
		isGroupMode: false,
		
		postCreate: function() {
			this.inherited(arguments);
			this.panel = document.createElement("div");
			this.panel.id = 'notifierPanel';
			this.panel.className = 'notifier';
			this.domNode.appendChild(this.panel);
			
			this.expandPanel = document.createElement("div");
			this.expandPanel.id = 'expandPanel';
			this.expandPanel.className = 'expandPanel';
			this.domNode.appendChild(this.expandPanel);
			

		},

        showMessages: function(data){
            var recivedNotifs = [];

            data.forEach(function(el){
                recivedNotifs.push(new Notif(el.id, el.text, el.groupId, el.groupText,el.sendTime));
            });
            console.log('Messages received: ' + data.length);

            recivedNotifs.forEach(function(el){
                notifier.notifs[el.id] = el;
                if (el.sendTime > notifier.lastReceived){
                    notifier.lastReceived = el.sendTime;
                }
            });

            if(Object.keys(this.notifs).length >= 5){
                if(this.isGroupMode){
                    var groupsToUpdateMessage = [];
                    recivedNotifs.forEach(function(el){
                        if(!notifier.groups[el.groupId]){
                            notifier.groups[el.groupId] = new Group(el.groupId, el.groupText);
                        }
                        notifier.groups[el.groupId].groupNotifs[el.id] = el;
                        groupsToUpdateMessage[el.groupId] = el.groupId;
                    });
                    groupsToUpdateMessage.forEach(function(el){
                        notifier.groups[el].updateText();
                        if(jQuery('#'+el+'mess').length > 0){
                            jQuery('#'+el+'mess').text(notifier.groups[el].text);
                        } else {
                            notifier.addMessage(notifier.groups[el], 'Группа уведомлений', true);
                        }
                        if (notifier.expandPanel.groupId == el ){
                            var expandButton = jQuery('#'+el+'header').find('.expandButton');
                            expandButton.click();
                            expandButton.click();
                        }
                    })

                } else {
                    this.isGroupMode = true;
                    notifier.clearMessages();
                    notifier.buildGroups();
                    this.groups.forEach(function(group){
                        group.updateText();
                        notifier.addMessage(group, 'Группа уведомлений', true);
                    });
                }
            } else {
                recivedNotifs.forEach(function(notif){
                    notifier.addMessage(notif, 'Новое уведомление', false);
                });

            }
        },
		
		checkMessages: function() {
			console.log('Checking for new messages...');
			
			notifier = this;
			
			dojo.xhrGet({
				url: "/DBMI-Portal/ajax/notification",	
				content: {
					startAfter: notifier.lastReceived
				},
				sync: false,
				handleAs: 'json',
				load: function(data) {
                    notifier.showMessages(data)
				},
				error: function(error) {
					console.error(error);
				}
			});
		},
		
		buildGroups: function(){
			notifier = this;
			this.notifs.forEach(function(notif){
				if(!notifier.groups[notif.groupId]){
					notifier.groups[notif.groupId] = new Group(notif.groupId, notif.groupText);
				}
				notifier.groups[notif.groupId].groupNotifs[notif.id] = notif;
			});
		},
		
		clearMessages: function(){
			var messageWraps = jQuery('.messageWrap');
			jQuery.each(messageWraps,function(i, node){
				node.remove();
			});
		},
		
		addMessage: function(message, header, isGroup) {
			/*Header*/
			var messageHeader = document.createElement("div");
			messageHeader.id=message.id + 'header';
			messageHeader.className = 'notifHeader';
			/*Buttons*/
			var messageCloseTab = document.createElement("div");
			messageCloseTab.className = 'closeButton';
			
			/*Body*/
			var messageDiv = document.createElement("div");
			messageDiv.id=message.id + 'mess';
			messageDiv.className = 'message';
			
			/*Container*/
			var messageWrap = document.createElement("div");
			messageWrap.className = 'messageWrap';
			messageWrap.id = message.id + 'wrap';
			
			/*If group*/
			if(isGroup){
				var messageExpandTab = document.createElement("div");
				messageExpandTab.className = 'expandButton';
				var counterSpan = document.createElement("span");
				counterSpan.className = 'counter';
			}
			
			/*Linking*/
			this.panel.appendChild(messageWrap);
			messageWrap.appendChild(messageHeader);
			messageWrap.appendChild(messageDiv);
			
			/*Fill text*/
			messageHeader.innerHTML = header;
			messageDiv.innerHTML = message.text.replace(/\s+/g,' ').substring(0,250);

			messageHeader.appendChild(messageCloseTab);
			if(isGroup){
				messageHeader.appendChild(messageExpandTab);
				dojo.connect(messageExpandTab, 'onclick', this, "expandGroup");
				messageDiv.appendChild(counterSpan);
			}
			

			var messageHeight =  jQuery(messageDiv).outerHeight(true);
			messageHeight =  messageHeight + jQuery(messageHeader).outerHeight(true);
			messageWrap.style.height = messageHeight+'px';
			
			var newHeight = this.getPanelHeight();
			
			messageDiv.style.display = 'none';
			jQuery(this.panel).animate({height:newHeight},100, function(){
					messageDiv.style.display = 'block';
				}
			);

			dojo.connect(messageCloseTab, 'onclick', this, "messageWrapOnClick");

		},
		
		expandGroup: function(e) {
			if(jQuery(this.expandPanel).children().length != 0){
				jQuery(this.expandPanel).children().each(function(i, el){
					jQuery(el).remove();
				});
			}
			var messageWrap = e.target.parentNode.parentNode;
			var groupId = messageWrap.id.replace('wrap','');
			if(this.expandPanel.groupId == groupId){
				this.expandPanel.style.display = 'none';
				delete this.expandPanel.groupId;
				return;
			} else {
				this.expandPanel.groupId = groupId;
			}
			var group;
			this.groups.forEach(function(el){
				if(el.id == groupId){
					group = el;
				}
			});
			if(Object.keys(group.groupNotifs).length == 0){
				return;
			}
			var expandPanel = this.expandPanel;
			var widget = this;
			this.expandPanel.style.display = 'block';
			group.groupNotifs.forEach(function(groupNotif){
				var expandMessage = document.createElement("div");
				expandMessage.id=groupNotif.id + 'exmess';
				expandMessage.className = 'exmess';
				expandPanel.appendChild(expandMessage);
				expandMessage.innerHTML = groupNotif.text.replace(/\s+/g,' ').substring(0,250);
				
				var messageCloseTab = document.createElement("div");
				messageCloseTab.className = 'closeButton';
				expandMessage.appendChild(messageCloseTab);
				dojo.connect(messageCloseTab, 'onclick', widget, "removeNotifFromExpand");
			});
			
			var wrapBottom = jQuery(widget.panel).height() - jQuery(messageWrap).position().top;
			var expandPanelHeight = widget.getExpandPanelHeight();
			if((wrapBottom - expandPanelHeight) < 0){
				expandPanel.style.bottom = '0px';
			} else {
				expandPanel.style.bottom = (wrapBottom - expandPanelHeight) + 'px';
			}
			if(expandPanelHeight > 500){
				expandPanelHeight = 500;
			}
			expandPanel.style.height = expandPanelHeight + 'px';
			
		},
		
		startup: function() {
			window.notifier = this;

			var notifier = this;
			
			this.checkMessages();
			t = new dojox.timing.Timer(60000);
			
			t.onTick = function() {

				notifier.checkMessages();

			};

			t.start();
		},
		
		messageWrapOnClick: function(e) {
			var messageWrap = e.target.parentNode.parentNode;
			var id = messageWrap.id.replace('wrap','');
			if(this.expandPanel.groupId == id){
				if(jQuery(this.expandPanel).children().length != 0){
					jQuery(this.expandPanel).children().each(function(i, el){
						jQuery(el).remove();
					});
				}
				this.expandPanel.style.display = 'none';
				delete this.expandPanel.groupId;
			}
			var globalNotifs = this.notifs;
			var panel = messageWrap.parentNode;

			var notifier = this;
			var newHeight = this.getPanelHeight() - messageWrap.clientHeight;
			var error = false;
			if(this.isGroupMode){
				this.groups[id].groupNotifs.forEach(function(el){
					dojo.xhrGet({
						url: "/DBMI-Portal/ajax/notification",
						content: {
							markRead: el.id
						},
						handleAs: 'json',
						load: function(data) {
							delete globalNotifs[el.id];
						},
						error: function(error) {
							error = true;
							console.error(error);
						}
					});
				});
				delete this.groups[id];
			} else {
				dojo.xhrGet({
					url: "/DBMI-Portal/ajax/notification",
					content: {
						markRead: id
					},
					handleAs: 'json',
					load: function(data) {
						delete globalNotifs[id];
					},
					error: function(error) {
						error = true;
						console.error(error);
					}
				});
				
			}
			if(!error){
				jQuery(messageWrap).animate({left:messageWrap.clientWidth},200,function(){
					jQuery(messageWrap).children().css('display','none');
					jQuery(messageWrap).animate({height: 1},200);
					jQuery('#notifierPanel').animate({height: newHeight},200, function(){
						messageWrap.remove();
						if(jQuery('#notifierPanel').children().length == 0){
							notifier.isGroupMode = false;
						}
					});
				});
			}

		},
		
		removeNotifFromExpand: function(e) {
			var message = e.target.parentNode;
			var panel = message.parentNode;
			//messageWrap.remove();
			var notifier = this;
			var newHeight = this.getExpandPanelHeight() - message.clientHeight;
			var newBottom = "+="+message.clientHeight;
			var id = message.id.replace('exmess','');
			var notif = this.notifs[id];
			dojo.xhrGet({
				url: "/DBMI-Portal/ajax/notification",	
				content: {
					markRead: id
				},
				handleAs: 'json',
				load: function(data) {
					delete notifier.notifs[id];
					delete notifier.groups[notif.groupId].groupNotifs[id];
					notifier.groups[notif.groupId].updateText();

					jQuery( message ).fadeOut( "fast", function() {
						jQuery('#'+notif.groupId+'mess').text(notifier.groups[notif.groupId].text);
						if(newHeight > 640){
							return;
						}
						if(jQuery(panel).children().length == 1){
							panel.style.display = 'none';
							message.remove();
							jQuery('#'+notif.groupId+'wrap').find('.closeButton').click();
						} else {
							jQuery(panel).animate({height: newHeight, bottom: newBottom},100, function(){
								message.remove();
							});
						}
					});
				},
				error: function(error) {
					console.error(error);
				}
			});


			

		},
		
		getPanelHeight: function(){
			var panelHeight = 0;
			var messageWraps = jQuery('.messageWrap');
			jQuery.each(messageWraps,function(i, node){
				panelHeight += node.clientHeight;
			});
			return panelHeight;
		},
		
		getExpandPanelHeight: function(){
			var panelHeight = 0;
			var messageWraps = jQuery('.exmess');
			jQuery.each(messageWraps,function(i, node){
				panelHeight += node.clientHeight;
			});
			return panelHeight;
		}
	}
);

function Notif(id, text, groupId, groupText, sendTime) {
	this.id = id;
	this.text = text;
	this.groupId = groupId;
	this.groupText = groupText;
	this.sendTime = sendTime;
}

function Group(id, templateText) {
	this.id = id;
	this.templateText = templateText;
	this.groupNotifs = [];
	this.text = '';
}

Group.prototype.updateText = function () {
	var count = Object.keys(this.groupNotifs).length;
	if(count > 0){
		this.text = this.templateText.replace('%count%',count);
	}
};

function openUrl(el, card, baseCard){
	while(jQuery(el).find('.closeButton').length == 0){
		el = el.parentElement;
	}
	jQuery(el).find('.closeButton').click();
	card = card.replace(/\s/g, '');
	baseCard = baseCard.replace(/\s/g, '');
	var url = 'http://'+window.location.host;
	if(window.location.toString().indexOf('portal/boss') == -1){
		url = url +
		'/portal/auth/portal/dbmi/card/CardPortletWindow?action=e&windowstate=normal&mode=view&MI_BACK_URL_FIELD=%2Fportal%2Fauth%2Fportal%2Fdbmi%2FPersonalArea%2Farm%2FArmWindowToday%3Frefresh_cards_list%3Dfalse%26action%3D1%26MI_ACTION_FIELD%3DMI_REFRESH_ACTION&MI_EDIT_CARD='
		+card;
	} else {
		url = url + '/portal/auth/portal/boss/document?item='+baseCard+'&directUrl=true&currentUrl='+encodeURIComponent(window.location.pathname+window.location.search);
	}
	window.location = url;
}



