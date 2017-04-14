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
var cryptoDecode64 = {
		_keyStr : "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=",
 
 
		// public method for decoding
		decode : function (input) {
			var output = "";
			var chr1, chr2, chr3;
			var enc1, enc2, enc3, enc4;
			var i = 0;
	 
			input = input.replace(/[^A-Za-z0-9\+\/\=]/g, "");
	 
			while (i < input.length) {
	 
				enc1 = this._keyStr.indexOf(input.charAt(i++));
				enc2 = this._keyStr.indexOf(input.charAt(i++));
				enc3 = this._keyStr.indexOf(input.charAt(i++));
				enc4 = this._keyStr.indexOf(input.charAt(i++));
	 
				chr1 = (enc1 << 2) | (enc2 >> 4);
				chr2 = ((enc2 & 15) << 4) | (enc3 >> 2);
				chr3 = ((enc3 & 3) << 6) | enc4;
	 
				output = output + String.fromCharCode(chr1);
	 
				if (enc3 != 64) {
					output = output + String.fromCharCode(chr2);
				}
				if (enc4 != 64) {
					output = output + String.fromCharCode(chr3);
				}
	 
			}
	 
			output = cryptoDecode64._utf8_decode(output);
	 
			return output;
	 
		},
	  
		// private method for UTF-8 decoding
		_utf8_decode : function (utftext) {
			var string = "";
			var i = 0;
			var c = c1 = c2 = 0;
	 
			while ( i < utftext.length ) {
	 
				c = utftext.charCodeAt(i);
	 
				if (c < 128) {
					string += String.fromCharCode(c);
					i++;
				}
				else if((c > 191) && (c < 224)) {
					c2 = utftext.charCodeAt(i+1);
					string += String.fromCharCode(((c & 31) << 6) | (c2 & 63));
					i += 2;
				}
				else {
					c2 = utftext.charCodeAt(i+1);
					c3 = utftext.charCodeAt(i+2);
					string += String.fromCharCode(((c & 15) << 12) | ((c2 & 63) << 6) | (c3 & 63));
					i += 3;
				}
	 
			}
	 
			return string;
		},
		// public method for encoding
	encode : function (input) {
		var output = "";
		var chr1, chr2, chr3, enc1, enc2, enc3, enc4;
		var i = 0;
 
		input = cryptoDecode64._utf8_encode(input);
 
		while (i < input.length) {
 
			chr1 = input.charCodeAt(i++);
			chr2 = input.charCodeAt(i++);
			chr3 = input.charCodeAt(i++);
 
			enc1 = chr1 >> 2;
			enc2 = ((chr1 & 3) << 4) | (chr2 >> 4);
			enc3 = ((chr2 & 15) << 2) | (chr3 >> 6);
			enc4 = chr3 & 63;
 
			if (isNaN(chr2)) {
				enc3 = enc4 = 64;
			} else if (isNaN(chr3)) {
				enc4 = 64;
			}
 
			output = output +
			this._keyStr.charAt(enc1) + this._keyStr.charAt(enc2) +
			this._keyStr.charAt(enc3) + this._keyStr.charAt(enc4);
 
		}
 
		return output;
	},
		
		// private method for UTF-8 encoding
	_utf8_encode : function (string) {
		string = string.replace(/\r\n/g,"\n");
		var utftext = "";
 
		for (var n = 0; n < string.length; n++) {
 
			var c = string.charCodeAt(n);
 
			if (c < 128) {
				utftext += String.fromCharCode(c);
			}
			else if((c > 127) && (c < 2048)) {
				utftext += String.fromCharCode((c >> 6) | 192);
				utftext += String.fromCharCode((c & 63) | 128);
			}
			else {
				utftext += String.fromCharCode((c >> 12) | 224);
				utftext += String.fromCharCode(((c >> 6) & 63) | 128);
				utftext += String.fromCharCode((c & 63) | 128);
			}
 
		}
 
		return utftext;
	}
}

function cryptoGetSignature(args){
		var signValue = "";
		var signData = "";
		var msg = "";
		var contentSignature = "";		
		var userName = "1";
		var result = {msg:"", success: false, signature: ""};
						
		if((args.signValues || args.stringsArrayHash || args.stringsArrayData) && args.signAttrXML){			
			try{				
					signValue = args.signValues;
					var sessionKey = "";
					var certHash = "";
					var error = false;
					var certficateId ="";
					
					dojo.xhrGet({
						url:  "/DBMI-UserPortlets/GetSimmKeyServlet" ,		
						preventCache: true, 
						sync: true,		
						load: function(resp, ioArgs){
							var d = "#delim#";
							if(resp.indexOf(d)>-1){
								certficateId = resp.split(d)[0];
								certHash = resp.split(d)[1];
								certStatus =  resp.split(d)[2];
								if(certHash.length<5){
									alert("В карточке сотрудника не обнаружен открытый ключ!");
									error = true;
								}
								if(certStatus == 'false'){
									alert("Ваш сертификат отозван или не прошел проверку!");
									error = true;
								}
							}else{
								alert("Ошибка получения данных с сервера");
								error = true;
							}
							//Получаем не только hash, но и certficateId в связи с изменением формата заполнения xml подписи
							//certHash = resp;
						},
						error: function(err) {
							alert('Вы не можете выполнить операцию в связи приостановлением (прекращением) действия сертификата ключа ЭП. Обратитесь в отдел ЭП для получения нового сертификата.');
							error = true;
						}
					});			
					
					if(error) return result;
					
					/*if(sessionKey == ""){
						alert('Ошибка получения сессионного ключа!')
						return
					}*/
					if(args.attachFilePath){ //файл
						contentSignature = dojo.byId("CryptoApplet").getSignature(2,userName,args.attachFilePath, null, getKeystore(), getKeystorePassword(), certHash)
						if (contentSignature == ""){
							alert('Вложение не подписано!');
							return result;
						}
						setKeystore(dojo.byId("CryptoApplet").getKeystore());
						setKeystorePassword(dojo.byId("CryptoApplet").getKeystorePassword());
						
						signData += certHash + ",";
					}else if(args.stringsArrayHash){//Массив строк
						//Формируем строку с разделителями для груповой подписи
						var signDataWithDelimHash = "";
						var signDataWithDelimData = "";
						for(var s=0; s<args.stringsArrayHash.length; s++ ){
							if (s>0){
								signDataWithDelimHash += ";";
								signDataWithDelimData += "$";
							}
							signDataWithDelimHash += args.stringsArrayHash[s];
							if (args.stringsArrayData !== undefined){
								signDataWithDelimData += args.stringsArrayData[s];
							}
						}

						if (args.stringsArrayData === undefined){
							signDataWithDelimData = null;
						}
						
						//Выполняем подписывание
						var tmp = dojo.byId("CryptoApplet").getSignature(1, userName, signDataWithDelimData, signDataWithDelimHash, getKeystore(), getKeystorePassword(), certHash);
						var signArr = tmp[0].split(";");
						var pkcs7Message = tmp[1].split(";");
						
						var signData = [];
						for(var s=0; s<args.stringsArrayHash.length; s++ ){
							if (signArr[s] == ""){
								alert('Вложение не подписано!');
								return result;
							}							
							if(signArr[s].indexOf("@error")>-1){
								alert('Ошибка подписания: ' + signArr[s].split("@error")[1] );
								return result;
							}
							signData.push(cryptoGetSignXML({cert: certHash, sign: signArr[s], pkcs7: (s < pkcs7Message.length ? pkcs7Message[s]: ""),  attr: args.signAttrXML[s], certId: certficateId}));
							setKeystore(dojo.byId("CryptoApplet").getKeystore());
							setKeystorePassword(dojo.byId("CryptoApplet").getKeystorePassword());							
						}
					}else{ //строка
						contentSignature = dojo.byId("CryptoApplet").getSignature(1, userName, signValue, null, getKeystore(), getKeystorePassword(), certHash)
						if (contentSignature == ""){
							alert('Карточка не подписана!');
							return result;
						}
						signData = cryptoGetSignXML({cert: certHash, sign: contentSignature[0], pkcs7: contentSignature[1], attr: args.signAttrXML, certId: certficateId});
						setKeystore(dojo.byId("CryptoApplet").getKeystore());
						setKeystorePassword(dojo.byId("CryptoApplet").getKeystorePassword());						
					}
					if(contentSignature.indexOf("@error")>-1){
						alert('Ошибка подписания: ' + contentSignature.split("@error")[1] );
						return result;
					}
					
				
				if(dojo.isArray(signData) && signData.length > 0){
					result.signature = signData;
					result.success = true;			
				}else if(contentSignature && contentSignature != ""){					
					result.signature = signData;
					result.success = true;	
				}
				
							
			}catch(ex){
				result.msg = ex.message;
			}		
		}else{
			result.msg = "nofields";
		}
		
		return result
}

function checkTime(i) {
	if (i<10) {
		i="0" + i;
	}
	return i;
}
function cryptoGetSignXML(args){
	/*
	var date = new Date();
	var day=checkTime(date.getDate());
	var month=checkTime(date.getMonth() + 1);
	var year=date.getFullYear();
	var hours = checkTime(date.getHours());
	var menutes = checkTime(date.getMinutes());
	var seconds = checkTime(date.getSeconds());
	*/
	var Date = dojo.byId("CryptoApplet").getTime();
	var result = '<sign cert="'+args.cert+'" certId="' + args.certId + '" signature="'+args.sign + '" pkcs7="' + args.pkcs7 + '" time="' + Date + '">';
	result += args.attr + '</sign>';
	return result
}

function getKeystore(){
	return getCookie("keystore");
}

function setKeystore(keystore){
	var today = new Date();
	setCookie("keystore", keystore, today.getFullYear() + 1, today.getMonth(), today.getDay());
}

function getKeystorePassword(){
	return window.name;
}

function setKeystorePassword(keystorePassword){
	window.name = keystorePassword;
}

function setCookie ( name, value, exp_y, exp_m, exp_d, path, domain, secure )
{
  var cookie_string = name + "=" + escape ( value );
 
  if ( exp_y )
  {
    var expires = new Date ( exp_y, exp_m, exp_d );
    cookie_string += "; expires=" + expires.toGMTString();
  }
 
  if ( path )
        cookie_string += "; path=" + escape ( path );
 
  if ( domain )
        cookie_string += "; domain=" + escape ( domain );
  
  if ( secure )
        cookie_string += "; secure";
  
  document.cookie = cookie_string;
}


function getCookie (cookie_name)
{
  var results = document.cookie.match ( '(^|;) ?' + cookie_name + '=([^;]*)(;|$)' ); 
  if ( results )
    return ( unescape ( results[2] ) );
  else
    return null;
}
