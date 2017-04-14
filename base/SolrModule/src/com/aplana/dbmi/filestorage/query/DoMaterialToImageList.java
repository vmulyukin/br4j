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
package com.aplana.dbmi.filestorage.query;

import java.awt.Image;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.List;

import org.ghost4j.document.PDFDocument;
import org.ghost4j.renderer.SimpleRenderer;

import org.apache.commons.io.IOUtils;

import com.aplana.dbmi.filestorage.convertmanager.MaterialMapper;
import com.aplana.dbmi.storage.ContentStorage;
import com.aplana.dbmi.storage.content.ContentReader;

import com.aplana.dbmi.action.ConvertToPdf;
import com.aplana.dbmi.action.Material;
import com.aplana.dbmi.action.file.CopyMaterialWithStamp;
import com.aplana.dbmi.action.file.MaterialToImageList;
import com.aplana.dbmi.common.utils.file.DefinesTypeFile;
import com.aplana.dbmi.common.utils.file.MimeContentTypeReestrBean;
import com.aplana.dbmi.common.utils.pdf.PdfUtils;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ActionQueryBase;
import com.itextpdf.text.pdf.PdfReader;

public class DoMaterialToImageList extends actionFileStorageUseBase {
	private static final long serialVersionUID = 1L;

	@Override
	public Object processQuery() throws DataException 
	{
		MaterialToImageList action = getAction();
		final ContentStorage dstFstorage = super.getDefaultFileStorage();

		// �������� �������� �� ������������ ��������
		final Material material = (Material)getJdbcTemplate().queryForObject(
				"SELECT file_name,file_store_url FROM card WHERE card_id=?", 
				new Object[]{ action.getObjectId().getId() }, 
				new MaterialMapper());

		FileInfo srcInfo = queryCardFileInfo(action.getObjectId(), Material.CURRENT_VERSION);
		final ContentStorage srcFstorage = super.chkGetSingleStorageByURL(srcInfo.getFileUrl(), action.getObjectId(), srcInfo.getFileName());
		final ContentReader reader = srcFstorage.getReader(srcInfo.getFileUrl());
		int fileSize = (reader != null) ? (int) reader.getSize() : -1;
		if (fileSize < 0) 
		{
			logger.info(MessageFormat.format("{0}: No valid attachment for source card {1}. Exiting.", 
					getClass(), action.getObjectId().getId()));
			throw new DataException("action.copy.stamp.nodata", 
					new Object[] {action.getObjectId().getId()});
		}

		InputStream data = reader.getContentInputStream();
		
		String mimeType = MimeContentTypeReestrBean.getMimeType(data, material.getName());
		
		boolean isPDF = DefinesTypeFile.isPDF(mimeType);
		boolean isConvertableToPdf = false;
		boolean isPdfAConformant = false;
		
		if (isPDF){
			//��������� ��������� PDF ��������� �� ������������ ������� PDF/A-1
			isPdfAConformant = PdfUtils.isValidPDFA1Header(data);
		}
		else {
			isConvertableToPdf = DefinesTypeFile.isConvertable(mimeType);
		}
		
		InputStream pdfInputStream = null;
		
		if (!isPDF && !isConvertableToPdf)
		{
			logger.error(MessageFormat.format("Not valid attachment type for source card {0}: {1} (mime type = " +
					mimeType + "). " + "Cannot be converted to PDF -> Aborting.", action.getObjectId().getId(), material.getName() ));
			return null;
		} else if (!isPdfAConformant || isConvertableToPdf)
		{
			// ������������ ���� � ������ PDF/A-1 (��� �������� ��� ����������������� �� ����)
			ConvertToPdf convertionAction = new ConvertToPdf();
			material.setCardId(action.getObjectId());
			material.setData(data);
			convertionAction.setMaterial(material);
			ActionQueryBase queryConvertToPdf = getQueryFactory().getActionQuery(convertionAction);
			queryConvertToPdf.setAction(convertionAction);
			pdfInputStream = (InputStream) getDatabase().executeQuery(getSystemUser(), queryConvertToPdf);
		}
		else if (isPdfAConformant)
		{
			// PDF �������� ��� � PDF/A-1 �������
			pdfInputStream = reader.getContentInputStream();
		}
		
		try{
			byte[] pdfBytes = IOUtils.toByteArray(pdfInputStream);
			PdfReader pdfReader = new PdfReader(pdfBytes);
			if(PdfUtils.getLastPosition(action.getLocationString(), pdfReader) != null){
				return null;
			}
			
			PDFDocument ghost4jDocument = new PDFDocument();
			ghost4jDocument.load(new ByteArrayInputStream(pdfBytes));
			pdfBytes = null;
			SimpleRenderer renderer = new SimpleRenderer();
			renderer.setResolution(50);
			List<Image> images;
	
			images = renderer.render(ghost4jDocument);
			
			return images;
		} catch (Exception e){
			logger.error(e.getMessage());
			return null;
		}
	}
}
