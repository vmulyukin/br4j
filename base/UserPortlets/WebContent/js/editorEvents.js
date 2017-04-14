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
    var editorEventManager = {
        editors: {},
        subscriptions: {},      
        registerAttributeEditor: function(attrCode, attrHtmlId, isInline, value) {
            var editorData = this.editors[attrCode];
            if (!editorData) {
                var editorData = {
                    'attrCode': attrCode,
                    'attrHtmlId': attrHtmlId,
                    'isInline': isInline,
                    'value': value
                };
                this.editors[attrCode] = editorData;
                this.notifyValueChanged(attrCode, value);
            }                   
        },
        subscribe: function(subscriberAttrCode, valueAttrCode, functionName, functionParameter) {
            var subscription = {
                'subscriberAttrCode': subscriberAttrCode,
                'functionName': functionName,
                'functionParameter': functionParameter
            };

            var subscribers = this.subscriptions[valueAttrCode];
            var isAlreadySubscribed = false;
            if (subscribers) {
                for(var i = 0; i < subscribers.length; ++i) {
                    var existing = subscribers[i];
                    if (existing.subscriberAttrCode == subscriberAttrCode &&
                        existing.functionName == functionName &&
                        existing.functionParameter == functionParameter) {
                        isAlreadySubscribed = true;
                    }
                }
                if (!isAlreadySubscribed) {
                    subscribers.push(subscription);
                }
            } else {
                this.subscriptions[valueAttrCode] = [subscription];
            }

            var editorData = this.editors[valueAttrCode];
            var sData = this.editors[subscription.subscriberAttrCode];
            if (!isAlreadySubscribed && editorData && sData) {
                eval(subscription.functionName + '(sData.attrCode, sData.attrHtmlId, sData.isInline, editorData.value, subscription.functionParameter);');
            }
        },
        notifyValueChanged: function(attrCode, value) {
            var editorData = this.editors[attrCode];
            if (!editorData) {
                console.error('Not registered attribute code: ' + attrCode);
                return;
            }
            else {
                editorData.value = value;
            }
            var subscribers = this.subscriptions[attrCode];
            if (subscribers) {
                for(var i = 0; i < subscribers.length; ++i) {
                    var subscription = subscribers[i];
                    var sData = this.editors[subscription.subscriberAttrCode];
                    if (sData) {
                        eval(subscription.functionName + '(sData.attrCode, sData.attrHtmlId, sData.isInline, value, subscription.functionParameter);');
                    }
                }
            }
        }
    };
