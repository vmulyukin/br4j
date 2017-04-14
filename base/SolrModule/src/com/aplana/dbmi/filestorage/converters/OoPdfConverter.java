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
package com.aplana.dbmi.filestorage.converters;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aplana.dbmi.action.Material;
import com.aplana.dbmi.action.file.DownloadFileStream;
import com.aplana.dbmi.filestorage.convertmanager.CharacterDetector;
import com.aplana.dbmi.filestorage.convertmanager.Task;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.storage.ContentStorage;
import com.aplana.dbmi.storage.content.ContentReader;
import com.aplana.dbmi.storage.content.ContentWriter;
import com.aplana.dbmi.storage.impl.PdfConvertorSettings;
import com.sun.star.beans.PropertyValue;
import com.sun.star.beans.XPropertySet;
import com.sun.star.bridge.UnoUrlResolver;
import com.sun.star.bridge.XUnoUrlResolver;
import com.sun.star.comp.helper.Bootstrap;
import com.sun.star.frame.XComponentLoader;
import com.sun.star.frame.XStorable;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;
/**
 * ��������� ��� ��������������� ".odp", ".ods", ".odt", ".rtf", ".doc", ".docx", ".xls", ".xlsx", ".ppt", ".pptx", ".txt"
 * ���������� ����������� � ������� OpenOffice
 * @author ���������
 *
 */
public class OoPdfConverter implements PdfConverter {

	protected final Log logger = LogFactory.getLog(getClass());

	private String tempFileName;
	private Material material = null;
	private XComponent xComponent = null;
	private boolean autoEncoding = false;
	private List<PropertyValue> additionalOptions = new ArrayList<PropertyValue>();
	
	public OoPdfConverter(){
	}	
	
	/**
	 * �������������� ������������� �����������
	 */
	public void stop() {
		xComponent.dispose();		
	}

	public synchronized InputStream processQuery(Task task) throws DataException {
		material=task.getMaterial();	
		InputStream result=null;

		//------------------------------------------ �������� �������� ����    	
		final String cacheFSname = "\\$"+ PdfConvertorSettings.getCacheStorageName();

		ContentStorage storage = task.getStorage();
		String cacheURL = material.getUrl().replaceAll( "\\$(.)+?\\b", cacheFSname);

		File tempFile = null;
		try {
			// Get directory where convertor stores temporary files
			final File tempDir = new File(PdfConvertorSettings.getConvertorTempDir());
			if(! tempDir.exists()) {
				tempDir.mkdirs();
			}

			// Create temporary file that will be used by OpenOffice server as the source for conversion
			tempFile = new File(PdfConvertorSettings.getConvertorTempDir() + "/" + getTempFileName());
			if(tempFile.exists()) {
				tempFile.delete();
			}
			tempFile.createNewFile();
			logger.debug("Pdf conversion: temporary file " + tempFile.getAbsolutePath() + " created");

			// Write material into temporary file
			final InputStream fileIn = material.getData();

			FileOutputStream fileOut = new FileOutputStream(tempFile);
			try {
				if (autoEncoding) {
					encodeTxtFile(fileIn, fileOut);
				} else {
					IOUtils.copy(fileIn, fileOut);
				}
			} finally {
				IOUtils.closeQuietly(fileIn);
				IOUtils.closeQuietly(fileOut);
			}
                        
			result = convertToPdf(storage, cacheURL, cacheFSname);
		} catch (Exception e) {
			logger.error("Error creating temporary file \"" + getTempFileName() + 
					"\" in directory \"" + PdfConvertorSettings.getConvertorTempDir() + "\"", e);
			//throw new DataException("action.convert.data", e);
			throw new DataException(this.getClass().getName(), e);
		} finally {
			// delete temporary file
			if(tempFile != null) {
				tempFile.delete();
				logger.debug( "Pdf conversion: temporary file " + tempFile.getAbsolutePath() + " deleted");
			}
		}

		return result;
	}


	private String getTempFileName() {
		if(null != tempFileName) {
			return tempFileName;
		}		

		tempFileName = System.currentTimeMillis() +  material.getCardId().getId().toString();
		return tempFileName;
	}

	private InputStream convertToPdf(ContentStorage storage, String cacheURL, String cacheFSname) throws DataException {
		// get component loader
		XComponentLoader xComponentLoader = getComponentLoader();

		// get temporary file path in a form readable by OpenOffice server
		String openOfficeTempDir = PdfConvertorSettings.getServerTempDir();

		// Create file url in the internal OpenOffice format
		StringBuffer fileUrlBuffer = new StringBuffer("file://");		
		// If Windows we have to add one more back-slash
		if(!openOfficeTempDir.startsWith("/")) {
			fileUrlBuffer.append("/");
		}
		fileUrlBuffer.append(openOfficeTempDir).append("/").append(getTempFileName());
		String fileUrl = fileUrlBuffer.toString();

		//PropertyValue[] conversionProperties = new PropertyValue[] {};
		PropertyValue mypv = new PropertyValue();		
		mypv.Name = "Hidden";
		mypv.Value = new Boolean(false);
		additionalOptions.add(0, mypv);

		long start = System.currentTimeMillis();
		logger.info( MessageFormat.format("OoPdfConverter starting. url: ''{0}'', card_id: {1}", material.getUrl(), material.getCardId().getId()));
		// load
		xComponent = null;
		try { 
			// load temporary file into OpenOffice server
			xComponent = xComponentLoader.loadComponentFromURL(fileUrl, "_blank", 0, additionalOptions.toArray(new PropertyValue[0]));
		} catch (IllegalArgumentException e) {
			long end = System.currentTimeMillis() - start;
			logger.error("OpenOffice failed to read from \"" + fileUrl + "\", time: " + end + " ms", e);
			throw new DataException("action.convert.data", e);
		} catch (com.sun.star.io.IOException e) {
			long end = System.currentTimeMillis() - start;
			logger.error("OpenOffice failed to read from \"" + fileUrl + "\", time: " + end + " ms", e);
			throw new DataException("action.convert.data", e);
		}

		InputStream result = null;
		 // Create PDF filter data
        PropertyValue pdfFilterData[] = new PropertyValue[1];
		PropertyValue[] conversionProperties = new PropertyValue[]{new PropertyValue(), new PropertyValue(), new PropertyValue()};
		//OutputStreamWriter outputStreamWriter=null;
	    
		final ContentWriter wr = storage.getWriter(cacheURL);
		if (wr != null)
			wr.delete();
		
		final OutputStream outputStream = wr.getContentOutputStream();
		try {
			//	Specifies the PDF version that should be generated:
			//	0: PDF 1.4	
			//	1: PDF/A-1 (ISO 19005-1:2005) <- this one is used
            pdfFilterData[0] = new PropertyValue();
            pdfFilterData[0].Name = "SelectPdfVersion";
            pdfFilterData[0].Value = new Integer(1);
            
			conversionProperties[0].Name = "OutputStream";
			conversionProperties[0].Value = outputStream;
			conversionProperties[1].Name = "FilterName";
			conversionProperties[1].Value = "writer_pdf_Export";
            conversionProperties[2].Name = "FilterData";
            conversionProperties[2].Value = pdfFilterData;
			
			final XStorable xStorable = UnoRuntime.queryInterface(XStorable.class, xComponent);
			try {
				// perform conversion and write result into outputStream 				
				xStorable.storeToURL("private:stream", conversionProperties);
			} catch (Exception e) {
				wr.delete();
				logger.error("OpenOffice failed to convert to pdf from \"" + fileUrl + "\"", e);
				throw new DataException("action.convert.data", e);
			}

			//�������� ���������� � ���� finally (��. xComponent.dispose() )
			/* close temporary file on OpenOffice server
			final XCloseable xclosable2 = UnoRuntime.queryInterface(XCloseable.class, xComponent);
			try {
				xclosable2.close(true);
			} catch (CloseVetoException e) {
				logger.warn("OpenOffice forbade to close component", e);
			}*/
			
			ContentReader cr = storage.getReader(cacheURL);
			if (cr != null)
				result = cr.getContentInputStream();

			if (result != null)  {
				logger.info( MessageFormat.format(
						"PDF image of  ''{1}''\n\t cached into storage ''{0}''\n\t as ''{2}'' "
						, cacheFSname, material.getUrl(), cacheURL));
			}
		} finally {
			long end = System.currentTimeMillis() - start;
			logger.info( MessageFormat.format("OoPdfConverter ending. Time: {2} ms. url: ''{0}'', card_id: {1}", material.getUrl(), material.getCardId().getId(), end));
			IOUtils.closeQuietly( outputStream);
			
			try {
				xComponent.dispose();
			} catch (Exception e) {
				logger.error("Cant't dispose document", e);
			}
		}
		return result;
	}
	
	private XComponentLoader getComponentLoader() throws DataException {
		final String connectionString = getOOoConnectionString();

		try {
			final XComponentContext xcomponentcontext = Bootstrap.createInitialComponentContext(null);
			// create a connector, so that it can contact the office

			final XUnoUrlResolver urlResolver = UnoUrlResolver.create(xcomponentcontext);
			final Object initialObject = urlResolver.resolve(connectionString);

			final XMultiComponentFactory xOfficeFactory =
				UnoRuntime.queryInterface(XMultiComponentFactory.class, initialObject);

			// retrieve the component context as property (it is not yet exported from the office)
			// Query for the XPropertySet interface.
			final XPropertySet xProperySet = UnoRuntime.queryInterface(XPropertySet.class, xOfficeFactory);

			// Get the default context from the office server.
			final Object oDefaultContext = xProperySet.getPropertyValue("DefaultContext");

			// Query for the interface XComponentContext.
			final XComponentContext xOfficeComponentContext =
				UnoRuntime.queryInterface(XComponentContext.class, oDefaultContext);

			// now create the desktop service
			// NOTE: use the office component context here!
			final Object oDesktop = xOfficeFactory.createInstanceWithContext("com.sun.star.frame.Desktop", xOfficeComponentContext);

			// query the XComponentLoader interface from the Desktop service
			final XComponentLoader xComponentLoader = 
				UnoRuntime.queryInterface( XComponentLoader.class, oDesktop);

			return xComponentLoader;
		} catch (Exception e) {
			logger.error("Error connecting to OpenOffice server using connection string \"" + connectionString  + "\"", e);
			throw new DataException("action.convert.data", e);
		}
	}

	private String getOOoConnectionString() {
		final String host = PdfConvertorSettings.getServerConnectionHost();
		final String port = PdfConvertorSettings.getServerConnectionPort();

		return "uno:socket,host=" + host + ",port=" + port + ";urp;StarOffice.ServiceManager";
	}
	
	public boolean isAutoEncoding(){
		return autoEncoding;
	}
	
	public void setAutoEncoding(boolean autoEncoding){
		this.autoEncoding=autoEncoding;
	}
	
	private void encodeTxtFile(InputStream in, OutputStream out) throws IOException {
		String fileReadEncoding = CharacterDetector.detect(in);
		if (in instanceof FileInputStream) {
			((FileInputStream)in).getChannel().position(0);
		} else if (in instanceof DownloadFileStream) {
			((DownloadFileStream)in).reset();
		}
		
		//�� ���� ���������� �� UTF-8 � UTF-8, ������ ��������
		if ("UTF-8".equals(getEncoding(fileReadEncoding))) {
			IOUtils.copy(in, out);
		} else {
			InputStreamReader  fileIn  = null;
			OutputStreamWriter fileOut = null;
			try {
				fileIn  = new InputStreamReader (in,  getEncoding(fileReadEncoding));
				fileOut = new OutputStreamWriter(out, "UTF8" );
				IOUtils.copy(fileIn, fileOut);
			} finally {
				IOUtils.closeQuietly(fileIn);
				IOUtils.closeQuietly(fileOut);
			}
		}
		
		PropertyValue propertyValue = new PropertyValue();
		propertyValue.Name = "FilterName";
		propertyValue.Value = "Text (encoded)";					
		additionalOptions.add(propertyValue);
		propertyValue = new PropertyValue();
		propertyValue.Name = "FilterOptions";
		propertyValue.Value = "UTF8,CRLF,Times New Roman,RUSSIAN,"; 
		additionalOptions.add(propertyValue);
	}
	
	public String getEncoding(String encoding){
		String value = null;
		if("UTF-8".equals(encoding)){
			value = "UTF-8";
		}else{
			value = "cp1251";
		}
		return value;
	}
}
