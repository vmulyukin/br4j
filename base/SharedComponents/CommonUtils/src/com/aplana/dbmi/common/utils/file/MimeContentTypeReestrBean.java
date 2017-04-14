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

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tika.detect.Detector;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.AutoDetectParser;

import com.aplana.dbmi.Portal;
import com.aplana.dbmi.action.Material;
import com.aplana.dbmi.action.file.DownloadFileStream;
import com.aplana.dbmi.parser.ItemTag;
import com.aplana.dbmi.parser.XmlParse;

/**
 * @author RAbdullin
 * ����� ��� ����������� �������� �� ��������� ������.
 * ���� ���� ������ �� ���������, �� ����������� ��������� 
 * "application/" + ���������� ����� ��� �����.
 */
public class MimeContentTypeReestrBean {
	private final static Log logger = LogFactory.getLog(MimeContentTypeReestrBean.class);
	private final static String OO_MIME_CONFIG = "dbmi/openoffice/mime-types.xml";
	private final static String OWRITER_MIME_CONFIG = "dbmi/owriter/file-types.xml";
	
	public final static String DEFAULT_CONTENT_TYPE = "application/octet-stream";
	
	public final static String MIME_FOR_OPEN_OFFICE = "oo";
	public final static String MIME_FOR_TXT = "txt";
	public final static String MIME_FOR_PDF = "pdf";
	public final static String MIME_FOR_IMAGE = "image";
	public final static String MIME_FOR_TIFF = "tiff";
	public final static String MIME_FOR_OWRITER = "owriter";

	//tika detector for mime type detection
	private static final Detector detector = new AutoDetectParser().getDetector();
	private static HashMap<String, ArrayList<String>> mimeProps;
	
	public static String getMimeType(InputStream m, String filename) {
		String mime = DEFAULT_CONTENT_TYPE;
		if (m == null)
	    	return mime; 
		Metadata md = new Metadata();
	    if (filename != null)
	    	md.add(Metadata.RESOURCE_NAME_KEY, filename);
        try {
        	MediaType mediaType = detector.detect(new BufferedInputStream(m), md);
        	if (mediaType != null) {
        		mime = mediaType.toString();
        	}
		} catch (IOException e) {
			logger.error("Error getting mime detection. Using default mime type.");
		} finally {
			//����� ������ mime ��������� ���������� ����� �� ������
			try {
				if (m instanceof FileInputStream)
					((FileInputStream)m).getChannel().position(0);
				else if (m instanceof DownloadFileStream)
					((DownloadFileStream)m).reset();
				else
					logger.error("Can't reset input stream (" + m + ")");
			} catch (IOException e) {
				logger.error("Can't set position 0 in " + m);
			}
		}
        return mime;
	}
	
	public static String getMimeType(Material m) {
		return getMimeType(m.getData(), m.getName());
	}
	
	private static HashMap<String, ArrayList<String>> getMimeProps() {
		synchronized  (MimeContentTypeReestrBean.class) {
			if (mimeProps == null) {	
				mimeProps = new HashMap<String, ArrayList<String>>();
				ArrayList<String>configList = new ArrayList<String>();
				configList.add(OO_MIME_CONFIG);
				configList.add(OWRITER_MIME_CONFIG);
				for(String configName : configList){
					readConfig(configName);
				}	
			}
		}
		
		return mimeProps;
	}
	
	private static void readConfig(String configName) {
		InputStream is = null;
		try {
			is = Portal.getFactory().getConfigService().getConfigFileUrl(configName).openStream();
			ItemTag root = new ItemTag();
			XmlParse.parse(root, is);
			for (ItemTag tag : root.getItemTags()) {
				ArrayList<?> value = mimeProps.get(tag.getTag());
				if (value == null) {
					mimeProps.put(tag.getTag(), new ArrayList<String>());
				}
				mimeProps.get(tag.getTag()).add(tag.getMsg());
			}
		}catch (Exception e) {
			logger.error("Couldn't read settings file " + configName, e);
		}finally {
			IOUtils.closeQuietly(is);
		}
	}
	
	public static List<String> getTypes(String type) {
		return getMimeProps().get(type);
	}
}
