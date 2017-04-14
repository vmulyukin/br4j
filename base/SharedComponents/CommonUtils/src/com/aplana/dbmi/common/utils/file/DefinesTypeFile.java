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
package com.aplana.dbmi.common.utils.file;

import java.util.List;

/**
 * ���������� ������������, ������ ���� ���� �.�. png, doc � �.�.
 * @author ���������
 *
 */
public class DefinesTypeFile {

	private static final List<String> OO_CONVERTABLE_TYPES = MimeContentTypeReestrBean.getTypes(MimeContentTypeReestrBean.MIME_FOR_OPEN_OFFICE);
	private static final List<String> OO_EDITABLE_TYPES = MimeContentTypeReestrBean.getTypes(MimeContentTypeReestrBean.MIME_FOR_OWRITER);
	private static final List<String> IMG_TYPES = MimeContentTypeReestrBean.getTypes(MimeContentTypeReestrBean.MIME_FOR_IMAGE);
	private static final List<String> TIFF_TYPES = MimeContentTypeReestrBean.getTypes(MimeContentTypeReestrBean.MIME_FOR_TIFF);
	private static final List<String> TXT_TYPE = MimeContentTypeReestrBean.getTypes(MimeContentTypeReestrBean.MIME_FOR_TXT);
	private static final List<String> PDF_TYPE = MimeContentTypeReestrBean.getTypes(MimeContentTypeReestrBean.MIME_FOR_PDF);

	public static boolean isPDF(String m) {
		return checkType(m, PDF_TYPE);
	}

	public static boolean isConvertableToPdfByOo(String m) {
		return checkType(m, OO_CONVERTABLE_TYPES);
	}

	public static boolean isImage(String m) {
		return checkType(m, IMG_TYPES);
	}

	public static boolean isTiff(String m) {
		return checkType(m, TIFF_TYPES);
	}
	
	public static boolean isTxt(String m) {
		return checkType(m, TXT_TYPE);
	}
	
	public static boolean isConvertable(String m) {
		return  checkType(m, IMG_TYPES) || 
				checkType(m, OO_CONVERTABLE_TYPES) ||
				checkType(m, TIFF_TYPES) ||
				checkType(m, PDF_TYPE) ||
				checkType(m, TXT_TYPE);
	}
	
	public static boolean isEditable(String m) {
		return  checkType(m, OO_EDITABLE_TYPES);
	}
	
	private static boolean checkType(String mime, List<?> types){
		if (null == mime || types == null || types.isEmpty()) {
			return false;
		}
		if (types.contains(mime)) {
			return true;
		}
		return false;
	}
}
