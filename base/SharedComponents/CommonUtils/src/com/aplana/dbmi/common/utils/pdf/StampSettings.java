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
package com.aplana.dbmi.common.utils.pdf;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aplana.dbmi.Portal;
import com.aplana.dbmi.model.Stamp;


public class StampSettings {

	private static Log logger = LogFactory.getLog(StampSettings.class);

	private final static String STAMP_CONFIG = "dbmi/card/stamp/stamp.properties";

	private final static String SIG_STAMP_TEMPLATE = "signature.stamp.template";
	private final static String SIG_STAMP_WIDTH  = "signature.stamp.width";
	private final static String SIG_STAMP_HEIGHT  = "signature.stamp.height";
	private final static String SIG_STAMP_INDENT_X = "signature.stamp.indent.x";
	private final static String SIG_STAMP_INDENT_Y = "signature.stamp.indent.y";
	private final static String SIG_STAMP_MARK_TEMPLATE = "signature.stamp.mark.template";

	private final static String SIG_STAMP_SERIAL_FONT_NAME = "signature.stamp.serial.field.font.name";
	private final static String SIG_STAMP_OWNER_FONT_NAME = "signature.stamp.owner.field.font.name";
	private final static String SIG_STAMP_NOTBEFORE_FONT_NAME = "signature.stamp.notbefore.field.font.name";
	private final static String SIG_STAMP_NOTAFTER_FONT_NAME = "signature.stamp.notafter.field.font.name";
	private final static String SIG_MARK_ORGANISATION_FONT_NAME = "signature.mark.organisation.font.name";

	private final static String SIG_STAMP_SERIAL_FONT_SIZE = "signature.stamp.serial.field.font.size";
	private final static String SIG_STAMP_OWNER_FONT_SIZE = "signature.stamp.owner.field.font.size";
	private final static String SIG_STAMP_NOTBEFORE_FONT_SIZE = "signature.stamp.notbefore.field.font.size";
	private final static String SIG_STAMP_NOTAFTER_FONT_SIZE = "signature.stamp.notafter.field.font.size";
	private final static String SIG_MARK_ORGANISATION_FONT_SIZE = "signature.mark.organisation.field.font.size";

	private final static String REG_STAMP_FONT_NAME = "registration.stamp.font.name";
	private final static String REG_STAMP_FONT_SIZE = "registration.stamp.font.size";
	private final static String REG_DATE_INDENT = "registration.stamp.regdate.indent";
	private final static String REG_NUM_INDENT = "registration.stamp.regnum.indent";
	private final static String REG_STAMP_PLACEMENT = "registration.stamp.placement";
	
	private final static String BOTTOM_STAMP_FONT_NAME = "bottom.stamp.font.name";
	private final static String BOTTOM_STAMP_FONT_SIZE = "bottom.stamp.font.size";
	private final static String BOTTOM_STAMP_INDENT_X = "bottom.stamp.indent.x";
	private final static String BOTTOM_STAMP_INDENT_Y = "bottom.stamp.indent.y";
	private final static String BOTTOM_STAMP_ALIGN = "bottom.stamp.align";

	//Signature stamp default properties
	final static int DEFAULT_SIG_STAMP_HEIGHT = 65;
	final static int DEFAULT_SIG_STAMP_WIDTH = 22;
	final static int DEFAULT_SIG_STAMP_INDENT = 6;
	final static int DEFAULT_SIG_FIELD_FONT_SIZE = 7;

	final static int DEFAULT_REG_STAMP_FONT_SIZE = 10;
	final static int DEFAULT_REG_DATE_INDENT = 4;
	final static int DEFAULT_REG_NUM_INDENT = 10;
	final static Placement DEFAULT_REG_STAMP_PLACEMENT = Placement.NUMBER_CHAR;
	
	final static int DEFAULT_BOTTOM_STAMP_INDENT_X = 20;
	final static int DEFAULT_BOTTOM_STAMP_INDENT_Y = 20;
	
	final static String DEFAULT_FONT_NAME = "DejaVuSerif.ttf";
	
	final static int ALIGN_LEFT = 1;
	final static int ALIGN_RIGHT = 2;
	
	public enum Placement {
		REGDATE_START, NUMBER_CHAR
	}

	private static Properties props;

	public static Properties getProps() {
		if (props == null) {
			try {
				final InputStream is = Portal.getFactory().getConfigService().getConfigFileUrl(STAMP_CONFIG).openStream();
				final Properties p = new Properties();
				p.load(is);
				props = p;
			} catch (IOException e) {
				logger.error("Couldn't read settings file " + STAMP_CONFIG, e);
			}
		}
		return props;
	}

	public static String getSigStampTemplate() {
		return getProps().getProperty(SIG_STAMP_TEMPLATE);
	}
	
	public static String getMarkStampTemplate() {
		return getProps().getProperty(SIG_STAMP_MARK_TEMPLATE);
	}

	public static float getSigStampHeight() {
		String value = getProps().getProperty(SIG_STAMP_HEIGHT);
		if (value != null && value.length() > 0) {
			try {
				return Float.valueOf(getProps().getProperty(SIG_STAMP_HEIGHT)) * PdfUtils.pointResolution;
			} catch(Exception ex) {
				logger.error("Problem getting signature stamp height: '" + value + "', default  " +
						DEFAULT_SIG_STAMP_HEIGHT + " is used instead" , ex);
			}
		}
		return DEFAULT_SIG_STAMP_HEIGHT * PdfUtils.pointResolution;
	}
	
	public static float getBottomStampIndentX() {
		String value = getProps().getProperty(BOTTOM_STAMP_INDENT_X);
		if (value != null && value.length() > 0) {
			try {
				return Float.valueOf(getProps().getProperty(BOTTOM_STAMP_INDENT_X)) * PdfUtils.pointResolution;
			} catch(Exception ex) {
				logger.error("Problem getting bottom stamp X indent: '" + value + "', default  " +
						DEFAULT_BOTTOM_STAMP_INDENT_X + " is used instead" , ex);
			}
		}
		return DEFAULT_BOTTOM_STAMP_INDENT_X * PdfUtils.pointResolution;
	}
	
	public static float getBottomStampIndentY() {
		String value = getProps().getProperty(BOTTOM_STAMP_INDENT_Y);
		if (value != null && value.length() > 0) {
			try {
				return Float.valueOf(getProps().getProperty(BOTTOM_STAMP_INDENT_Y)) * PdfUtils.pointResolution;
			} catch(Exception ex) {
				logger.error("Problem getting bottom stamp Y indent: '" + value + "', default  " +
						DEFAULT_BOTTOM_STAMP_INDENT_Y + " is used instead" , ex);
			}
		}
		return DEFAULT_BOTTOM_STAMP_INDENT_Y * PdfUtils.pointResolution;
	}

	public static float getSigStampWidth() {
		String value = getProps().getProperty(SIG_STAMP_WIDTH);
		if (value != null && value.length() > 0) {
			try {
				return Float.valueOf(getProps().getProperty(SIG_STAMP_WIDTH)) * PdfUtils.pointResolution;
			} catch(Exception ex) {
				logger.error("Problem getting signature stamp width: '" + value + "', default  " +
						DEFAULT_SIG_STAMP_WIDTH + " is used instead" , ex);
			}
		}
		return DEFAULT_SIG_STAMP_WIDTH * PdfUtils.pointResolution;
	}

	public static float getSigStampIndentX() {
		return getSigStampIndent(SIG_STAMP_INDENT_X);
	}

	public static float getSigStampIndentY() {
		return getSigStampIndent(SIG_STAMP_INDENT_Y);
	}

	private static float getSigStampIndent(String indent) {
		String value = getProps().getProperty(indent);
		if (value != null && value.length() > 0) {
			try {
				return Float.valueOf(getProps().getProperty(indent)) * PdfUtils.pointResolution;
			} catch(Exception ex) {
				logger.error("Problem getting signature stamp width: '" + value + "', default  " +
						DEFAULT_SIG_STAMP_INDENT + " is used instead" , ex);
			}
		}
		return DEFAULT_SIG_STAMP_INDENT * PdfUtils.pointResolution;
	}

	public static String getRegStampFontName() {
		String value = getProps().getProperty(REG_STAMP_FONT_NAME);
		return (null == value)? DEFAULT_FONT_NAME : value;
	}

	public static float getRegStampFontSize() {
		String value = getProps().getProperty(REG_STAMP_FONT_SIZE);
		if (value != null && value.length() > 0) {
			try {
				return Float.valueOf(getProps().getProperty(REG_STAMP_FONT_SIZE));
			} catch(Exception ex) {
				logger.error("Problem getting registration stamp font size: '" + value + "', default  " +
						DEFAULT_REG_STAMP_FONT_SIZE + " is used instead" , ex);
			}
		}
		return DEFAULT_REG_STAMP_FONT_SIZE;
	}
	
	public static String getBottomStampFontName() {
		String value = getProps().getProperty(BOTTOM_STAMP_FONT_NAME);
		return (null == value)? DEFAULT_FONT_NAME : value;
	}

	public static float getBottomStampFontSize() {
		String value = getProps().getProperty(BOTTOM_STAMP_FONT_SIZE);
		if (value != null && value.length() > 0) {
			try {
				return Float.valueOf(getProps().getProperty(BOTTOM_STAMP_FONT_SIZE));
			} catch(Exception ex) {
				logger.error("Problem getting bottom stamp font size: '" + value + "', default  " +
						DEFAULT_REG_STAMP_FONT_SIZE + " is used instead" , ex);
			}
		}
		return DEFAULT_REG_STAMP_FONT_SIZE;
	}

	public static float getRegNumIndent() {
		String value = getProps().getProperty(REG_NUM_INDENT);
		if (value != null && value.length() > 0) {
			try {
				return Float.valueOf(getProps().getProperty(REG_NUM_INDENT)) * PdfUtils.pointResolution;
			} catch(Exception ex) {
				logger.error("Problem getting registration num indent: '" + value + "', default  " +
						DEFAULT_REG_NUM_INDENT + " is used instead" , ex);
			}
		}
		return DEFAULT_REG_NUM_INDENT * PdfUtils.pointResolution;
	}
	
	public static float getRegDateIndent() {
		String value = getProps().getProperty(REG_DATE_INDENT);
		if (value != null && value.length() > 0) {
			try {
				return Float.valueOf(getProps().getProperty(REG_DATE_INDENT)) * PdfUtils.pointResolution;
			} catch(Exception ex) {
				logger.error("Problem getting registration date indent: '" + value + "', default  " +
						DEFAULT_REG_DATE_INDENT + " is used instead" , ex);
			}
		}
		return DEFAULT_REG_DATE_INDENT * PdfUtils.pointResolution;
	}

	public static Placement getRegStampPlacement() {
		String value = getProps().getProperty(REG_STAMP_PLACEMENT);
		Placement placement = DEFAULT_REG_STAMP_PLACEMENT;
		if (value != null && value.length() > 0) {
			try {
				switch (Integer.valueOf(getProps().getProperty(REG_STAMP_PLACEMENT))) {
				case 1:
					placement = Placement.REGDATE_START;
					break;
				case 2:
					placement = Placement.NUMBER_CHAR;
					break;
				}
			} catch(Exception ex) {
				logger.error("Problem getting registration stamp placement type: '" + value + "', default  " +
						DEFAULT_REG_STAMP_PLACEMENT + " is used instead" , ex);
			}
		}
		return placement;
	}
	
	public static String getSigFieldFontName(String fieldName) {
		String value = null;

		if (fieldName.equals(Stamp.SERIAL_FIELD)) {
			value = getProps().getProperty(SIG_STAMP_SERIAL_FONT_NAME);
		} else if (fieldName.equals(Stamp.OWNER_FIELD)){
			value = getProps().getProperty(SIG_STAMP_OWNER_FONT_NAME);
		} else if (fieldName.equals(Stamp.NOTBEFORE_FIELD)){
			value = getProps().getProperty(SIG_STAMP_NOTBEFORE_FONT_NAME);
		} else if (fieldName.equals(Stamp.NOTAFTER_FIELD)){
			value = getProps().getProperty(SIG_STAMP_NOTAFTER_FONT_NAME);
		} else if (fieldName.equals(Stamp.ORG_NAME_FIELD)){
			value = getProps().getProperty(SIG_MARK_ORGANISATION_FONT_NAME);
		}

		return (null == value)? DEFAULT_FONT_NAME : value;
	}
	
	public static float getSigFieldFontSize(String fieldName) {
		float value = DEFAULT_SIG_FIELD_FONT_SIZE;
		try {
			if (fieldName.equals(Stamp.SERIAL_FIELD)) {
				value = Float.valueOf(getProps().getProperty(SIG_STAMP_SERIAL_FONT_SIZE));
			}
			else if (fieldName.equals(Stamp.OWNER_FIELD)){
				value = Float.valueOf(getProps().getProperty(SIG_STAMP_OWNER_FONT_SIZE));
			}
			else if (fieldName.equals(Stamp.NOTBEFORE_FIELD)){
				value = Float.valueOf(getProps().getProperty(SIG_STAMP_NOTBEFORE_FONT_SIZE));
			}
			else if (fieldName.equals(Stamp.NOTAFTER_FIELD)){
				value = Float.valueOf(getProps().getProperty(SIG_STAMP_NOTAFTER_FONT_SIZE));
			} else if (fieldName.equals(Stamp.ORG_NAME_FIELD)){
				value =  Float.valueOf(getProps().getProperty(SIG_MARK_ORGANISATION_FONT_SIZE));
			}
		} catch(Exception ex) {
			logger.error("Problem getting signature stamp font size for field " + fieldName 
				+ ", default  " + DEFAULT_SIG_FIELD_FONT_SIZE + " is used instead" , ex);
		}
		return value;
	}
	
	public static boolean isBottomStampLeftAlign() {
		String value = getProps().getProperty(BOTTOM_STAMP_ALIGN);
		if (value != null && value.length() > 0) {
			try {
				int align = Integer.valueOf(getProps().getProperty(BOTTOM_STAMP_ALIGN));
				if (ALIGN_LEFT == align)
			 		return true;
			} catch(Exception ex) {
				logger.error("Problem getting bottom stamp alignment: '" + value + "', default  " +
						"right alignment is used" , ex);
			}
		}
		return false;
	}

	public static boolean isBottomStampRightAlign() {
		String value = getProps().getProperty(BOTTOM_STAMP_ALIGN);
		if (value != null && value.length() > 0) {
			try {
				int align = Integer.valueOf(getProps().getProperty(BOTTOM_STAMP_ALIGN));
				if (ALIGN_RIGHT == align)
			 		return true;
				else
			 		return false;
			} catch(Exception ex) {
				logger.error("Problem getting bottom stamp alignment: '" + value + "', default  " +
						"right alignment is used" , ex);
			}
		}
		return true;
	}
}