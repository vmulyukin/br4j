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



package com.jenkov.prizetags.icon.impl;

import com.jenkov.prizetags.base.BaseTag;
import com.jenkov.prizetags.util.ServletContextUtil;

import javax.servlet.jsp.JspException;


public class IconTag extends BaseTag{

    protected String dir         = null;
    protected String src        = null;
    protected String alt         = null;
    protected String deactivated = null;
    protected String active      = null;

    protected String onClick     = null;
    protected String onDblClick  = null;
    protected String onKeyDown   = null;
    protected String onKeyPressed= null;
    protected String onMouseDown = null;
    protected String onMouseMove = null;
    protected String onMouseOver = null;

    public String getDir() {
        return dir;
    }

    public void setDir(String dir) {
        this.dir = dir;
    }

    public String getSrc() {
        return src;
    }

    public void setSrc(String src) {
        this.src = src;
    }
    public String getAlt() {
        return alt;
    }

    public void setAlt(String alt) {
        this.alt = alt;
    }

    public String getDeactivated() {
        return deactivated;
    }

    public void setDeactivated(String deactivated) {
        this.deactivated = deactivated;
    }

    public String getActive() {
        return active;
    }

    public void setActive(String active) {
        this.active = active;
    }

    public String getOnClick() {
        return onClick;
    }

    public void setOnClick(String onClick) {
        this.onClick = onClick;
    }

    public String getOnDblClick() {
        return onDblClick;
    }

    public void setOnDblClick(String onDblClick) {
        this.onDblClick = onDblClick;
    }

    public String getOnKeyDown() {
        return onKeyDown;
    }

    public void setOnKeyDown(String onKeyDown) {
        this.onKeyDown = onKeyDown;
    }

    public String getOnKeyPressed() {
        return onKeyPressed;
    }

    public void setOnKeyPressed(String onKeyPressed) {
        this.onKeyPressed = onKeyPressed;
    }

    public String getOnMouseDown() {
        return onMouseDown;
    }

    public void setOnMouseDown(String onMouseDown) {
        this.onMouseDown = onMouseDown;
    }

    public String getOnMouseMove() {
        return onMouseMove;
    }

    public void setOnMouseMove(String onMouseMove) {
        this.onMouseMove = onMouseMove;
    }

    public String getOnMouseOver() {
        return onMouseOver;
    }

    public void setOnMouseOver(String onMouseOver) {
        this.onMouseOver = onMouseOver;
    }

    public int doStartTag() throws JspException {

        StringBuffer buffer = new StringBuffer(150);

        buffer.append("<img src=\"");
        appendDir(buffer);
        if(!isDeactivated()){
            appendImage(buffer);
        } else {
            appendDeactivatedImage(buffer);
        }
        buffer.append("\" ");

        if(!isDeactivated()){
            appendOnClick          (buffer);
            appendOnDblClick       (buffer);
            appendOnKeyDownImage   (buffer);
            appendOnKeyPressedImage(buffer);
            appendOnMouseDownImage (buffer);
            appendOnMouseMoveImage (buffer);
            appendOnMouseOverImage (buffer);
        }

        appendAlt(buffer);
        buffer.append(" border=\"0\" />");

        write(buffer.toString());

        return SKIP_BODY;
    }

    protected void appendDeactivatedImage(StringBuffer buffer) {
        buffer.append(getFileName());
        buffer.append("_deactivated");
        buffer.append(getFileExtension());
    }

    protected void appendImage(StringBuffer buffer){
        buffer.append(getFileName());
        if(isActive()){
            buffer.append("_active");
        }
        buffer.append(getFileExtension());
    }


    protected void appendAlt(StringBuffer buffer) {
        if(getAlt() != null){
            buffer.append(" alt=\"");
            buffer.append(getAlt());
            buffer.append("\" ");
        }
    }

    protected void appendOnClick(StringBuffer buffer) {
        if(isOn(getOnClick(), null, ServletContextUtil.getOnClick(pageContext), "off")){
            appendOnEventImage(buffer, "onClick", "_onclick");
        }
    }

    protected void appendOnDblClick(StringBuffer buffer) {
        if(isOn(getOnDblClick(), null, ServletContextUtil.getOnDblClick(pageContext), "off")){
            appendOnEventImage(buffer, "onDblClick", "_ondblclick");
        }
    }

    protected void appendOnKeyDownImage(StringBuffer buffer){
        if(isOn(getOnKeyDown(), null, ServletContextUtil.getOnKeyDown(pageContext), "off")){
            appendOnEventImage(buffer, "onKeyDown", "_onkeydown");
            appendOnEventImage(buffer, "onKeyUp"  , "");
        }
    }

    protected void appendOnKeyPressedImage(StringBuffer buffer){
        if(isOn(getOnKeyPressed(), null, ServletContextUtil.getOnKeyPressed(pageContext), "off")){
            appendOnEventImage(buffer, "onKeyPressed", "_onkeypressed");
        }
    }

    protected void appendOnMouseDownImage(StringBuffer buffer){
        if(isOn(getOnMouseDown(), null, ServletContextUtil.getOnMouseDown(pageContext), "on")){
            appendOnEventImage(buffer, "onMouseDown", "_onmousedown");
            if(isOn(getOnMouseOver(), null, ServletContextUtil.getOnMouseOver(pageContext), "on")){
                appendOnEventImage(buffer, "onMouseUp"  , "_onmouseover");
            } else {
                appendOnEventImage(buffer, "onMouseUp"  , "");
            }
        }
    }

    protected void appendOnMouseMoveImage(StringBuffer buffer){
        if(isOn(getOnMouseMove(), null, ServletContextUtil.getOnMouseMove(pageContext), "off")){
            appendOnEventImage(buffer, "onMouseMove", "_onmousemove");
        }
    }

    protected void appendOnMouseOverImage(StringBuffer buffer){
        if(isOn(getOnMouseOver(), null, ServletContextUtil.getOnMouseOver(pageContext), "on")){
            appendOnEventImage(buffer, "onMouseOver", "_onmouseover");
            if(isActive()){
                appendOnEventImage(buffer, "onMouseOut" , "_active");
            } else {
                appendOnEventImage(buffer, "onMouseOut" , "");
            }
        }
    }


    protected void appendDir(StringBuffer buffer) {
        String dir = getDir();

        if(dir == null){
            dir = ServletContextUtil.getIconDir(pageContext);
        }

        if(dir != null){
            buffer.append(dir);
            if(!dir.endsWith("/")){
                buffer.append("/");
            }
        }
    }


    protected void appendOnEventImage(StringBuffer buffer, String eventListener, String fileNameSuffix){
        buffer.append(" ");
        buffer.append(eventListener);
        buffer.append("=\"this.src='");
        appendDir(buffer);
        buffer.append(getFileName());
        buffer.append(fileNameSuffix);
        buffer.append(getFileExtension());
        buffer.append("';\" ");
    }


    protected boolean isOn(String value, String configValue, String contextValue, String defaultValue){
        String temp = value;

        if(temp == null){
            temp = configValue;
        }
        if(temp == null){
            temp = contextValue;
        }
        if(temp == null){
            temp = defaultValue;
        }
        return isOn(temp);
    }

    protected boolean isOn(String value){
        if(value == null) return false;
        if("on".equals(value.toLowerCase()) || "true".equals(value.toLowerCase())){
            return true;
        }
        return false;
    }

    protected boolean isDeactivated(){
        return isOn(getDeactivated());
    }

    protected boolean isActive(){
        return(isOn(getActive()));
    }

    protected String getFileExtension(){
        int index = getSrc().lastIndexOf('.');
        if(index > -1){
            return getSrc().substring(index);
        }
        return "";
    }

    protected String getFileName(){
        int index = getSrc().lastIndexOf('.');
        if(index > -1){
            return getSrc().substring(0, index);
        }
        return "";
    }

}
