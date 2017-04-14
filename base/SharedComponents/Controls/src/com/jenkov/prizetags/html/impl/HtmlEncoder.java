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
    Copyright 2004 Jenkov Development

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/



package com.jenkov.prizetags.html.impl;

import java.io.StringReader;
import java.io.IOException;

/**
 * @author Jakob Jenkov - Copyright 2005 Jenkov Development
 */
public class HtmlEncoder {

    /**
     * Encodes an html text so that html tags will be displayed as text, and not interpreted as html.
     * In addition regular line breaks will be replaced with &lt;br/&lt; codes, making the text
     * display in the HTML page as close to the way the text looked in the text editor when written.
     * @param htmlText The text with html tags to encode.
     * @return The encoded text.
     */
    public String textStyleEncode(String htmlText){
        StringBuffer buffer = new StringBuffer();
        StringReader reader = new StringReader(htmlText);

        try {
            int character         = 0;
            int previouscharacter = 0;
            while(character  != -1){
                previouscharacter = character;
                character         = reader.read();
                if(character != -1){
                    char theChar  = (char) character;
                    if(theChar == ' '){
                        if(previouscharacter == ' '){
                            buffer.append("&nbsp;");
                        } else {
                            buffer.append(' ');
                        }
                    } else if(theChar == '<' ){
                        encodeTag(character, theChar, reader, buffer);
                    } else if(theChar == '>'){
                        buffer.append("&gt;");
                    } else if(theChar == '\n'){
                        buffer.append("<br/>");
                    }
                    else {
                        buffer.append(theChar);
                    }
                }
            }
        } catch (IOException e) {
            //will never happen!
        }

        return buffer.toString();
    }

    private void encodeTag(int character, char theChar, StringReader reader, StringBuffer buffer) throws IOException {
        StringBuffer readAhead = new StringBuffer();
        while(character != -1 && theChar != '>'){
            character = reader.read();
            theChar = (char) character;
            if(character != -1 && theChar != '>' && theChar != '<'){
                readAhead.append(theChar);
            } else if(character != -1 && theChar == '<'){
               readAhead.append("&lt;");
            }
        }
        String tag = readAhead.toString();
        if(theChar == '>'){
            if("code" .equals(tag)){
                buffer.append("<code>");
            } else if( "/code".equals(tag)){
                buffer.append("</code>");
            } else {
                buffer.append("&lt;");
                buffer.append(tag);
                buffer.append("&gt;");
            }
        } else {
            buffer.append("&lt;");
            buffer.append(tag);
        }
    }


}
