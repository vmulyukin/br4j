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
package com.aplana.dbmi.util;

import java.io.InputStream;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import java.io.IOException;
import com.aplana.dbmi.service.DataException;
import java.io.File;
import java.io.FileInputStream;

public class EncodingUtils {
	
	private static Log logger = LogFactory.getLog(EncodingUtils.class);
	
	/**
	 * 
	 * @param input
	 * @return boolean ����: 
	 * true - ������������ ��������� UTF8 ��� BOM
	 * false - ������������ ��������� �� UTF8 ��� BOM
	 * 
	 * ������� � UTF-8 ���������� �������������������� ������ �� 1 �� 4 ���� (�������).
	 *
	 * � �������:
	 * U+000000-U+00007F: 0xxxxxxx (ANSI)
	 * U+000080-U+0007FF: 110xxxxx 10xxxxxx (���� ������ ���������)
	 * U+000800-U+00FFFF: 1110xxxx 10xxxxxx 10xxxxxx
	 * U+010000-U+10FFFF: 11110xxx 10xxxxxx 10xxxxxx 10xxxxxx
	 *
	 * �� ����� ������� ������ ������������ ����� ����� ����� � ������������������, 
	 * � ����� ��� ����������� �� ������������ ����� 10xxxxxx.
	 * ���� �����-�� ���� �� ������������ �����, ������ ��������� �������� �� UTF-8 (�������� win1251).
	 */
	public static boolean verifyEncodingUTF8WithoutBOM(File fileInput) throws DataException, IOException {
		InputStream input = null;
		try {
			input = new FileInputStream(fileInput);
			byte[] data = IOUtils.toByteArray(input);
			if (null == data)
				throw new DataException("Error verify encoding file");
			int[] unsignedData = byteToUnsignedInt(data);
			// ���������� BOM (EF BB BF)
			if (unsignedData.length > 2 && unsignedData[0] == 0xef && unsignedData[1] == 0xbb
					&& unsignedData[2] == 0xbf) {
				return false; // ������������ BOM
			}
	
			int i = 0;
			while (i < unsignedData.length - 1) {
				if (unsignedData[i] > 0x7f) { // �� ANSI-������
					if ((unsignedData[i] >> 5) == 6) {
						if ((i > unsignedData.length - 2) || ((unsignedData[i + 1] >> 6) != 2))
							return false; // win1251
						i++;
					} else if ((unsignedData[i] >> 4) == 14) {
						if ((i > unsignedData.length - 3) || ((unsignedData[i + 1] >> 6) != 2)
								|| ((unsignedData[i + 2] >> 6) != 2))
							return false; // win1251
						i += 2;
					} else if ((unsignedData[i] >> 3) == 30) {
						if ((i > unsignedData.length - 4) || ((unsignedData[i + 1] >> 6) != 2)
								|| ((unsignedData[i + 2] >> 6) != 2)
								|| ((unsignedData[i + 3] >> 6) != 2))
							return false; // win1251
						i += 3;
					} else {
						return false; // win1251
					}
				}
				i++;
			}
			return true; // UTF8 ��� BOM
		} finally {
			if (null != input)
				IOUtils.closeQuietly(input);
		}
	}
	
	// ����������� ���� � unsigned int
	public static int[] byteToUnsignedInt(byte[] bs) {
		int[] i_src = new int[bs.length];
        for(int i=0; i<bs.length; i++) 
        	i_src[i] = bs[i]<0 ? 
        			bs[i] + 256 // �.�. �������� �������� 2 �����
        			: 
        			bs[i]; // bs[i] & 255
        return i_src;
	}
	
	public static String byteToHex(byte b) {
		// Returns hex String representation of byte b
		char hexDigit[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
				'a', 'b', 'c', 'd', 'e', 'f' };
		char[] array = { hexDigit[(b >> 4) & 0x0f], hexDigit[b & 0x0f] };
		return new String(array);
	}

	public static String charToHex(char c) {
		// Returns hex String representation of char c
		byte hi = (byte) (c >>> 8);
		byte lo = (byte) (c & 0xff);
		return byteToHex(hi) + byteToHex(lo);
	}
}
