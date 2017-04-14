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
package com.aplana.web.tag;




import javax.servlet.jsp.JspException;
import java.util.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.text.DateFormatSymbols;
import java.text.Format;
import java.io.IOException;

import org.ajax4jsf.javascript.ScriptUtils;
import org.ajax4jsf.javascript.JSFunction;
import org.springframework.web.servlet.tags.form.AbstractHtmlInputElementTag;
import org.springframework.web.servlet.tags.form.TagWriter;

/**
 * Created by IntelliJ IDEA.
 * User: ipolukhin
 * Date: Dec 20, 2007
 * Time: 2:16:32 PM
 * To change this template use File | Settings | File Templates.
 */
public class CalendarSpringTag extends AbstractHtmlInputElementTag {
    public static final String SIZE_ATTRIBUTE = "size";

    public static final String MAXLENGTH_ATTRIBUTE = "maxlength";

    public static final String ALT_ATTRIBUTE = "alt";

    public static final String ONSELECT_ATTRIBUTE = "onselect";

    public static final String READONLY_ATTRIBUTE = "readonly";

    public static final String AUTOCOMPLETE_ATTRIBUTE = "autocomplete";


    private String size;



    private String alt;

    private String onselect;

    private String autocomplete;



    private String accesskey;
    private String inputClass;
    private String maxlength;
    private String oninputblur;
    private String oninputchange;
    private String oninputclick;
    private String oninputfocus;
    private String inputStyle;
    private Date value;
    private String timeZone;
    private String datePattern;
    private String buttonClass;
    private String buttonIcon;
    private String buttonLabel;
    private String jointPoint;
    private String direction;
    private String toolTipMode;
    private String boundaryDatesMode;
    private String popup;
    private String enableManualInput;
    private String showInput;
    private String verticalOffset;
    private String horizontalOffset;
    private String zindex;
    private String todayControlMode;
    private String showScrollerBar;
    private String showWeeksBar;
    private String showWeekDaysBar;
    private String showApplyButton;



    public String getAccesskey() {
        return accesskey;
    }

    public void setAccesskey(String accesskey) {
        this.accesskey = accesskey;
    }

    public String getInputClass() {
        return inputClass;
    }

    public void setInputClass(String inputClass) {
        this.inputClass = inputClass;
    }

    public String getMaxlength() {
        return maxlength;
    }

    public void setMaxlength(String maxlength) {
        this.maxlength = maxlength;
    }

    public String getOninputblur() {
        return oninputblur;
    }

    public void setOninputblur(String oninputblur) {
        this.oninputblur = oninputblur;
    }

    public String getOninputchange() {
        return oninputchange;
    }

    public void setOninputchange(String oninputchange) {
        this.oninputchange = oninputchange;
    }

    public String getOninputclick() {
        return oninputclick;
    }

    public void setOninputclick(String oninputclick) {
        this.oninputclick = oninputclick;
    }

    public String getOninputfocus() {
        return oninputfocus;
    }

    public void setOninputfocus(String oninputfocus) {
        this.oninputfocus = oninputfocus;
    }

    public String getInputStyle() {
        return inputStyle;
    }

    public void setInputStyle(String inputStyle) {
        this.inputStyle = inputStyle;
    }

    public String getInputValue() {
        if (getValue() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat(getDatePattern());
            return sdf.format(getValue());
        } else {
            return null;
        }
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    public String getDatePattern() {
        return datePattern;
    }

    public void setDatePattern(String datePattern) {
        this.datePattern = datePattern;
    }

    public Date getValue() {
        return value;
    }

    public void setValue(Date value) {
        this.value = value;
    }

    public String getButtonClass() {
        return buttonClass;
    }

    public void setButtonClass(String buttonClass) {
        this.buttonClass = buttonClass;
    }

    public void setButtonIcon(String buttonIcon) {
        this.buttonIcon = buttonIcon;
    }

    public String getButtonIcon() {
        return buttonIcon;
    }

    public void setButtonLabel(String buttonLabel) {
        this.buttonLabel = buttonLabel;
    }

    public String getButtonLabel() {
        return buttonLabel;
    }

    public String getCurrentDate(Date date) {

        return ScriptUtils.toScript(formatDate(date));
    }

    public static Object formatDate(Date date) {

        Calendar calendar = Calendar.getInstance();

        calendar.setTime(date);

        JSFunction result = new JSFunction("new Date");

        result.addParameter(new Integer(calendar.get(1)));

        result.addParameter(new Integer(calendar.get(2)));

        result.addParameter(new Integer(calendar.get(5)));

        return result;
    }


    public static Object formatSelectedDate(Date date) {

        Calendar calendar = Calendar.getInstance();

        calendar.setTime(date);

        JSFunction result = new JSFunction("new Date");

        result.addParameter(new Integer(calendar.get(1)));

        result.addParameter(new Integer(calendar.get(2)));

        result.addParameter(new Integer(calendar.get(5)));

        result.addParameter(new Integer(calendar.get(11)));

        result.addParameter(new Integer(calendar.get(12)));

        result.addParameter(new Integer(0));

        return result;
    }

    public String getSelectedDate(){

/* 488*/
        if (value != null)
/* 489*/ return ScriptUtils.toScript(formatSelectedDate(getValue()));
/* 493*/
        else
/* 493*/            return ScriptUtils.toScript(null);
    }


    private static String[] shiftDates(int minimum, int maximum, String labels[]) {
/* 387*/
        if (minimum == 0 && maximum - minimum == labels.length - 1) {
/* 388*/
            return labels;
        } else {
/* 391*/
            String shiftedLabels[] = new String[(maximum - minimum) + 1];
/* 392*/
            System.arraycopy(labels, minimum, shiftedLabels, 0, (maximum - minimum) + 1);
/* 395*/
            return shiftedLabels;
        }
    }

    protected Map getSymbolsMap() {
/* 399*/
        Map map = new HashMap();
/* 401*/
        java.util.Locale locale = pageContext.getRequest().getLocale();
        Calendar calendar = Calendar.getInstance(pageContext.getRequest().getLocale());
/* 403*/
        int maximum = calendar.getActualMaximum(7);
/* 404*/
        int minimum = calendar.getActualMinimum(7);
/* 406*/
        int monthMax = calendar.getActualMaximum(2);
/* 407*/
        int monthMin = calendar.getActualMinimum(2);
/* 409*/
        DateFormatSymbols symbols = new DateFormatSymbols(locale);
/* 410*/
        String weekDayLabels[] = null;
        weekDayLabels = symbols.getWeekdays();
        weekDayLabels = shiftDates(minimum, maximum, weekDayLabels);

/* 417*/
        String weekDayLabelsShort[] = null;
        weekDayLabelsShort = symbols.getShortWeekdays();
        weekDayLabelsShort = shiftDates(minimum, maximum, weekDayLabelsShort);

/* 425*/
        String monthLabels[] = null;
        monthLabels = symbols.getMonths();
        monthLabels = shiftDates(monthMin, monthMax, monthLabels);

/* 431*/
        String monthLabelsShort[] = null;

/* 434*/
        monthLabelsShort = symbols.getShortMonths();
/* 435*/
        monthLabelsShort = shiftDates(monthMin, monthMax, monthLabelsShort);

/* 438*/
        map.put("weekDayLabels", weekDayLabels);
/* 439*/
        map.put("weekDayLabelsShort", weekDayLabelsShort);
/* 440*/
        map.put("monthLabels", monthLabels);
/* 441*/
        map.put("monthLabelsShort", monthLabelsShort);
/* 443*/
        return map;
    }


    public void writeSymbols(TagWriter tagWriter) throws JspException {

/* 370*/
        Map symbolsMap = getSymbolsMap();
/* 371*/
        Iterator entryIterator = symbolsMap.entrySet().iterator();
/* 372*/
        tagWriter.appendValue(", \n");
/* 373*/
        do {
/* 373*/
            if (!entryIterator.hasNext())
/* 374*/ break;
/* 374*/
            java.util.Map.Entry entry = (java.util.Map.Entry) entryIterator.next();
/* 376*/
            tagWriter.appendValue(ScriptUtils.toScript(entry.getKey()));
/* 377*/
            tagWriter.appendValue(": ");
/* 378*/
            tagWriter.appendValue(ScriptUtils.toScript(entry.getValue()));
/* 380*/
            if (entryIterator.hasNext())
/* 381*/ tagWriter.appendValue(", \n");
        } while (true);
    }


    public String getFirstWeekDay() {
/* 448*/
        Calendar cal = Calendar.getInstance(pageContext.getRequest().getLocale());
/* 449*/
        return String.valueOf(cal.getFirstDayOfWeek() - cal.getActualMinimum(7));
    }

    public String getMinDaysInFirstWeek() {
/* 455*/
        Calendar cal = Calendar.getInstance(pageContext.getRequest().getLocale());
/* 456*/
        return String.valueOf(cal.getMinimalDaysInFirstWeek());
    }

    public String getCurrentDateAsString(Date date)  {
        Format formatter = new SimpleDateFormat("MM/yyyy");
        return formatter.format(date);
    }


    public String getJointPoint() {
        return jointPoint;
    }

    public void setJointPoint(String jointPoint) {
        this.jointPoint = jointPoint;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public String getToolTipMode() {
        return toolTipMode;
    }

    public void setToolTipMode(String toolTipMode) {
        this.toolTipMode = toolTipMode;
    }

    public String getBoundaryDatesMode() {
        return boundaryDatesMode;
    }

    public void setBoundaryDatesMode(String boundaryDatesMode) {
        this.boundaryDatesMode = boundaryDatesMode;
    }

    public String getPopup() {
        return popup;
    }

    public void setPopup(String popup) {
        this.popup = popup;
    }

    public String getEnableManualInput() {
        return enableManualInput;
    }

    public void setEnableManualInput(String enableManualInput) {
        this.enableManualInput = enableManualInput;
    }

    public String getShowInput() {
        return showInput;
    }

    public void setShowInput(String showInput) {
        this.showInput = showInput;
    }

    public String getVerticalOffset() {
        return verticalOffset;
    }

    public void setVerticalOffset(String verticalOffset) {
        this.verticalOffset = verticalOffset;
    }

    public String getHorizontalOffset() {
        return horizontalOffset;
    }

    public void setHorizontalOffset(String horizontalOffset) {
        this.horizontalOffset = horizontalOffset;
    }

    public String getZindex() {
        return zindex;
    }

    public void setZindex(String zindex) {
        this.zindex = zindex;
    }

    public String getTodayControlMode() {
        return todayControlMode;
    }

    public void setTodayControlMode(String todayControlMode) {
        this.todayControlMode = todayControlMode;
    }

    public String getShowScrollerBar() {
        return showScrollerBar;
    }

    public void setShowScrollerBar(String showScrollerBar) {
        this.showScrollerBar = showScrollerBar;
    }

    public String getShowWeeksBar() {
        return showWeeksBar;
    }

    public void setShowWeeksBar(String showWeeksBar) {
        this.showWeeksBar = showWeeksBar;
    }

    public String getShowWeekDaysBar() {
        return showWeekDaysBar;
    }

    public void setShowWeekDaysBar(String showWeekDaysBar) {
        this.showWeekDaysBar = showWeekDaysBar;
    }

    public String getShowApplyButton() {
        return showApplyButton;
    }

    public void setShowApplyButton(String showApplyButton) {
        this.showApplyButton = showApplyButton;
    }


    protected String convertToString(Object obj) {
        return obj != null ? obj.toString() : "";
    }

    protected int writeTagContent(TagWriter tagWriter) throws JspException {
        String tmpId = convertToString(this.getId()).replaceAll("InputDate", "");

        this.setCssClass("rich-calendar-input " + convertToString(getInputClass()));
        this.setCssStyle("vertical-align: middle; " + convertToString(getInputStyle()));
        tagWriter.startTag("span");
        tagWriter.writeAttribute( "id", convertToString(tmpId) + "Popup");
        tagWriter.startTag("input");

        writeDefaultAttributes(tagWriter);
        tagWriter.writeAttribute("type", getType());
        writeValue(tagWriter);

        // custom optional attributes
        writeOptionalAttribute(tagWriter, SIZE_ATTRIBUTE, getSize());
        writeOptionalAttribute(tagWriter, MAXLENGTH_ATTRIBUTE, getMaxlength());
        writeOptionalAttribute(tagWriter, ALT_ATTRIBUTE, getAlt());
        writeOptionalAttribute(tagWriter, ONSELECT_ATTRIBUTE, getOnselect());
        writeOptionalAttribute(tagWriter, AUTOCOMPLETE_ATTRIBUTE, getAutocomplete());

        tagWriter.endTag();



        if (buttonLabel == null || buttonLabel.length() == 0) {
            tagWriter.startTag("img");

            tagWriter.writeOptionalAttributeValue("accesskey", this.getAccesskey());
            tagWriter.writeOptionalAttributeValue("class", "rich-calendar-button " + convertToString(getButtonClass()));
            tagWriter.writeAttribute("id", tmpId + "PopupButton");
            tagWriter.writeAttribute("style", "vertical-align: middle");
            tagWriter.writeOptionalAttributeValue("tabindex", getTabindex());

            tagWriter.writeAttribute("src", getButtonIcon()); 
            tagWriter.endTag(); 
        } else {
            tagWriter.startTag("button");
            tagWriter.writeOptionalAttributeValue("accesskey", this.getAccesskey());
            tagWriter.writeOptionalAttributeValue("class", "rich-calendar-button " + convertToString(getButtonClass()));
            tagWriter.writeAttribute("id", tmpId + "PopupButton");
            tagWriter.writeAttribute("style", "vertical-align: middle");
            tagWriter.writeOptionalAttributeValue("tabindex", getTabindex());

            tagWriter.writeAttribute("type", "button");
            tagWriter.endTag();
        }

        tagWriter.startTag("input");
        tagWriter.writeAttribute("id", tmpId + "InputCurrentDate");
        tagWriter.writeAttribute("name", tmpId + "InputCurrentDate");
        tagWriter.writeAttribute( "style", "display:none");
        tagWriter.writeAttribute("type", "hidden");
        tagWriter.writeAttribute("value", getCurrentDateAsString(new Date()));
        tagWriter.endTag();


        tagWriter.endTag(); 


        tagWriter.startTag("div");
        tagWriter.writeAttribute("id", tmpId + "IFrame");
        tagWriter.writeAttribute("style", "display: none;");
        tagWriter.appendValue(" ");
        tagWriter.endTag();

        tagWriter.startTag("div");
        tagWriter.writeAttribute("id", tmpId);
        tagWriter.writeAttribute("style", "display: none;");
        tagWriter.appendValue(" ");
        tagWriter.endTag();  


        tagWriter.startTag("div");
        tagWriter.writeAttribute("id", tmpId + "Script");
        tagWriter.writeAttribute("style", "display: none;");
        tagWriter.startTag("script");
        tagWriter.writeAttribute("type", "text/javascript");
        tagWriter.appendValue(convertToString("new Calendar('" + tmpId + "', {"));
        tagWriter.appendValue(convertToString("dayListTableId: '" + tmpId + "Day', \n\t\t\tweekNumberBarId: '" + tmpId + "WeekNum', \n\t\t\tweekDayBarId: '" + convertToString(getId()) + "WeekDay',\n\t\t\tcurrentDate: " +
                convertToString(getCurrentDate(new Date())) + ", \n\t\t\tselectedDate: " + convertToString(getSelectedDate()) +
                ", \n\t\t\tdatePattern: '" + convertToString(getDatePattern()) +
                "',\n\t\t\tjointPoint: '" + convertToString(getJointPoint()) +
                "',\n\t\t\tdirection: '" + convertToString(getDirection()) +
                "',\n\t\t\ttoolTipMode:'" + convertToString(getToolTipMode()) +
                "',\n\t\t\tboundaryDatesMode:'" + convertToString(getBoundaryDatesMode()) +
                "',\n\t\t\tpopup: " + convertToString(getPopup()) +
                ",\n\t\t\tenableManualInput: " + convertToString(getEnableManualInput()) +
                ",\n\t\t\tshowInput: " + convertToString(getShowInput()) +
                ",\n\t\t\tdisabled: " + convertToString(getDisabled()) +
                ",\n\t\t\tajaxSingle: true" +
                ",\n\t\t\tverticalOffset:" + convertToString(getVerticalOffset()) +
                ",\n\t\t\thorizontalOffset: " + convertToString(getHorizontalOffset()) +
                ",\n\t\t\tstyle:'z-index: " + convertToString(getZindex()) + "; "
                + "',\n\t\t\tfirstWeekDay: " + convertToString(getFirstWeekDay()) +
                ", \n\t\t\tminDaysInFirstWeek: " + convertToString(getMinDaysInFirstWeek()) +
                ",\n\t\t\ttodayControlMode:'" + convertToString(getTodayControlMode()) +
                "',\n\t\t\tshowScrollerBar:" + convertToString(getShowScrollerBar()) +
                ",\n\t\t\tshowWeeksBar:" + convertToString(getShowWeeksBar()) +
                ",\n\t\t\tshowWeekDaysBar:" + convertToString(getShowWeekDaysBar()) +
                ",\n\t\t\tshowApplyButton:" + convertToString(getShowApplyButton())));
        writeSymbols(tagWriter);
        tagWriter.appendValue(convertToString("}).load("));

        tagWriter.appendValue(convertToString(");"));
        tagWriter.endTag(); 
        tagWriter.endTag();


        return EVAL_PAGE;
    }
    protected void writeValue(TagWriter tagWriter) throws JspException {
        tagWriter.writeAttribute("value", getDisplayString(getBoundValue(), getPropertyEditor()));
    }
    protected String getType() {
        return "text";
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getAlt() {
        return alt;
    }

    public void setAlt(String alt) {
        this.alt = alt;
    }

    public String getOnselect() {
        return onselect;
    }

    public void setOnselect(String onselect) {
        this.onselect = onselect;
    }

    public String getAutocomplete() {
        return autocomplete;
    }

    public void setAutocomplete(String autocomplete) {
        this.autocomplete = autocomplete;
    }
}
