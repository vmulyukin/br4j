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
/**
 * 
 */
package com.aplana.dbmi.storage.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;

/**
 * @author RAbdullin
 *
 */
public final class IOHelper {

	private IOHelper()
	{
	}
	
	/**
	 * ����������� ������� ����� � �������� ������� � ���������� �������� � 
	 * ������� �������.
	 * @param inStm
	 * @param outStm
	 * @param srcOffs: ������ ���������� ������ � inStm, 
	 * 		���� (<0)  ->  ����� �������� (-1);
	 * @param count: ���-�� ���������� ����, ���� (-1) �� "����������� ��� ��� �����".
	 * @return ���-�� ������������� ����.
	 * @throws IOException
	 */
	public static long copyFileStream( FileOutputStream outStm, FileInputStream inStm,
			long srcOffs, long count)
		throws IOException
	{
		if (srcOffs < 0 || inStm == null || outStm == null) 
			return -1;

		long result = -1;
		final FileChannel source = inStm.getChannel();
		try {
			final FileChannel destination = outStm.getChannel();
			try {

				if (count < 0) { 
					count = source.size() - srcOffs;
					if (count < 0) return source.size();
				}

				result = destination.transferFrom( source, srcOffs, count);
			} finally {
				destination.close();
			}
		} finally {
			source.close();
		}
		
		return result;
	}


	/**
	 * ����������� �������� ������.
	 * @param inStm
	 * @param outStm
	 * @throws IOException
	 */
	public static long copyFileStream( FileOutputStream outStm, FileInputStream inStm )
		throws IOException
	{
		return copyFileStream(outStm, inStm, 0, -1);
	}
	
	public final static int MINBUFSIZE = 10240;
	
	/** �������������� ����������� �������.
	 * @param outStm
	 * @param inStm: ����� � ������� ������� �������� ����� ������������� ����������� �� �����.
	 * @param bufsize: ������ ������ (���������� ����� 512).
	 * @param maxcount: ������������ ���-�� ���������� ����
	 * @return ���-�� ������������� ����.
	 * @throws IOException
	 */
	public static long copyStream(OutputStream outStm, InputStream inStm, int bufsize)
		throws IOException 
	{
		if ( (outStm instanceof FileOutputStream) && (inStm instanceof FileInputStream)) 
			return copyFileStream( (FileOutputStream) outStm, (FileInputStream) inStm );

		if (bufsize < MINBUFSIZE) bufsize = MINBUFSIZE;

		// �������������� ������...
		byte[] buffer = new byte[bufsize];
		int len = 0, readlen = 0;
		do {
			 readlen = inStm.read(buffer);
			 if (readlen <= 0) break;
			 outStm.write(buffer, 0, readlen);
			 len += readlen;
		} while (readlen > 0);

		outStm.flush();

		 // inStm.close();
		 // outStm.close();

		return len;
	}

	public static void ensureDirOfFile( String rawFileName )
	{
		 ensureDirs( new File(rawFileName).getParentFile() );
	}

	public static boolean ensureDirs(File dir) 
	{
		if (dir != null && !dir.exists())
			return dir.mkdirs();
		return true;
		 // getInstance().ensureDirsInternal(dir);
	}


	 /*
	 private static IOHelper instance;
	 
	 private static IOHelper getInstance()
	 {
		 if (instance == null)
			 instance = new IOHelper();
		 return instance;
	 }
	 
	 private synchronized void ensureDirsInternal(File dir) 
	 	throws IOException 
	 {
		 // has it been created by another file store or process?
		 if (dir.exists()) return;

		 // 20 attempts with 20 ms wait each time
		 for (int i = 0; i < 20; i++) {
			 boolean created = dir.mkdirs();
			 if (created) return;

			 // wait
			 try {
				 this.wait(20);
			 } catch (InterruptedException e) {
				 // do nothing
			 }

			 // did it get created in meantime?
			 if (dir.exists()) return;
		 }

		 // it still didn't succeed
		 throw new IOException( String.format( "Failed to create directory '%s'", dir));
	 }
	 */
}
