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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.Map;

import com.itextpdf.awt.geom.AffineTransform;
import org.hsqldb.Types;

import com.aplana.dbmi.action.ConvertToPdf;
import com.aplana.dbmi.action.Material;
import com.aplana.dbmi.action.file.CopyMaterialWithStamp;
import com.aplana.dbmi.common.utils.file.DefinesTypeFile;
import com.aplana.dbmi.common.utils.file.MimeContentTypeReestrBean;
import com.aplana.dbmi.filestorage.convertmanager.MaterialMapper;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Stamp;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ActionQueryBase;
import com.aplana.dbmi.service.impl.query.WriteQuery;
import com.aplana.dbmi.storage.ContentStorage;
import com.aplana.dbmi.storage.content.ContentReader;
import com.aplana.dbmi.storage.content.ContentWriter;
import com.aplana.dbmi.storage.impl.SolrContentReaderStream;
import com.aplana.dbmi.storage.search.SearchException;
import com.aplana.dbmi.common.utils.pdf.StampSettings;
import com.aplana.dbmi.common.utils.pdf.PdfUtils;
import com.aplana.dbmi.common.utils.pdf.PdfUtils.Position;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfImportedPage;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;
import com.itextpdf.text.pdf.parser.TextExtractionStrategy;
import com.itextpdf.text.pdf.parser.Vector;
import com.itextpdf.text.Rectangle;

import org.apache.commons.io.IOUtils;

/**
 * Query ��� ���������� �������� {@link CopyMaterialWithStamp}.
 * ������������ ����������� ��������� �� ����� �������� � ������ � ���������������� 
 * � ������ PDF/A-1 � ���������� ��������������� ������ �� ����� PDF ��������
 *
 * @author Vlad Alexandrov
 * @version 1.1
 * @since   2014-12-31
 */

public class doCopyMaterialWithStamp extends actionFileStorageUseBase implements WriteQuery {
	private static final long serialVersionUID = 1L;

	@Override
	public Object processQuery() throws DataException 
	{
		final CopyMaterialWithStamp action = (CopyMaterialWithStamp)getAction();
		final Object srcId = action.getFromCardId().getId();
		final Object dstId = action.getToCardId().getId();
		final String filename = action.getFileName();
		final Stamp regStamp = action.getRegStamp();

		final ObjectId srcCardId = new ObjectId(Card.class, srcId);

		final ContentStorage dstFstorage = super.getDefaultFileStorage();

		// �������� �������� �� ������������ ��������
		final Material material = (Material)getJdbcTemplate().queryForObject(
				"SELECT file_name,file_store_url FROM card WHERE card_id=?", 
				new Object[]{ srcId }, 
				new MaterialMapper());

		FileInfo srcInfo = queryCardFileInfo(srcCardId, Material.CURRENT_VERSION);
		final ContentStorage srcFstorage = super.chkGetSingleStorageByURL(srcInfo.getFileUrl(), srcCardId, srcInfo.getFileName());
		final ContentReader reader = srcFstorage.getReader(srcInfo.getFileUrl());
		int fileSize = (reader != null) ? (int) reader.getSize() : -1;
		if (fileSize < 0) 
		{
			logger.info(MessageFormat.format("{0}: No valid attachment for source card {1}. Exiting.", getClass(), srcId));
			throw new DataException("action.copy.stamp.nodata", new Object[] {srcId});
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
					mimeType + "). " + "Cannot be converted to PDF -> Aborting.", srcId, material.getName() ));
			throw new DataException("action.copy.stamp.invalid.format", new Object[] {material.getName()});
		} else if (!isPdfAConformant || isConvertableToPdf)
		{
			// ������������ ���� � ������ PDF/A-1 (��� �������� ��� ����������������� �� ����)
			ConvertToPdf convertionAction = new ConvertToPdf();
			material.setCardId(srcCardId);
			material.setData(reader.getContentInputStream());
			convertionAction.setMaterial(material);
			ActionQueryBase queryConvertToPdf = getQueryFactory().getActionQuery(ConvertToPdf.class);
			queryConvertToPdf.setAction(convertionAction);
			pdfInputStream = (InputStream) getDatabase().executeQuery(getSystemUser(), queryConvertToPdf);
		}
		else if (isPdfAConformant)
		{
			// PDF �������� ��� � PDF/A-1 �������
			pdfInputStream = reader.getContentInputStream();
		}
		
		// ������ ��������������� ����� � PDF/A-1 ��������
		String dstUrl = null;
		try
		{
			ContentWriter writer = dstFstorage.getWriter(null);
			
			// ������ ������������ �������������
			dstUrl = (writer.getContentUrl() != null) ? writer.getContentUrl().toExternalForm() : null;
			
			final OutputStream outputStream = writer.getContentOutputStream();
			addStampToPdfFile(regStamp, pdfInputStream, outputStream);
		}
		catch (DocumentException e)
		{
			logger.error("Failed to add stamp to file " + material.getName(), e);
			throw new DataException("action.copy.stamp.apply.fail", new Object[] {material.getName()});
		}
		catch (IOException e)
		{
			logger.error("Failed to add stamp to file " + material.getName(), e);
			throw new DataException("action.copy.stamp.apply.fail", new Object[] {material.getName()});
		}

		try {
			// ���������� ������� ��������
			updateCardTable(filename, fileSize, dstUrl, action.getToCardId());
			
			// ���������� ���������� �������
			// �������� �� ����������� ������������� ������
			if (dstFstorage != null)
			{
				final ContentReader freader =  dstFstorage.getReader(dstUrl);
				// update Solr index ...
				updateSearchIndex( dstId.toString(), freader, filename, null);
				logger.debug("post updateSearchIndex successfull,  URL: "+ dstUrl);
			}
		} catch (Exception ex) {
			throw (ex instanceof DataException) ? (DataException)ex: new DataException(ex);
		}
		if (logger.isInfoEnabled()) {
			logger.info( MessageFormat.format( "{0}: stamped PDF material copied from Card#{1} to Card#{2}",
					getClass(), srcId, dstId ));
		}

		return null;
	}

	/**
	 * ����� ��� ������������ ������ � PDF �������� (��� � ��������������� ������).
	 * @param stamp
	 * @param inputStream
	 * @param outputStream
	 * @throws DataException
	 * @throws DocumentException
	 * @throws IOException
	 */
	private void addStampToPdfFile(Stamp stamp, InputStream inputStream, 
			OutputStream outputStream) throws  DataException, DocumentException, IOException
	{
		final String bottomStampTemplate = "� {0} �� {1}";
		try {
			PdfReader pdfReader = new PdfReader(inputStream);
			PdfStamper pdfStamper = new PdfStamper(pdfReader, outputStream);
			Rectangle pageSize = pdfReader.getPageSize( 1 );
			float XScale = pageSize.getWidth()/PdfUtils.A4XSizeP;
			float YScale = pageSize.getHeight()/PdfUtils.A4YSizeP;

			// ����� �� / ������� � �������� ������������ ���������
			Stamp.SignatureData sigData = stamp.getSigData();
			if (null != sigData) {
				Position signPosition;
				if(sigData.getPosition() != null){
					String[] positionParams = sigData.getPosition().split(":");
					
					signPosition = new PdfUtils.Position(Integer.parseInt(positionParams[0]),
							Integer.parseInt(positionParams[1])*XScale,
							-Integer.parseInt(positionParams[2])*YScale);
				} else {
					//���������� ������� ������ �� / ������� � �������� ������������ ���������
					signPosition = PdfUtils.getLastPosition(sigData.getLocationString(), pdfReader);
					signPosition.setX(signPosition.getX() - StampSettings.getSigStampWidth()*XScale - StampSettings.getSigStampIndentX()*XScale);
					signPosition.setY(-(PdfUtils.A4YSizeP*YScale - signPosition.getY() + StampSettings.getSigStampIndentY()*YScale));
				}
				if (null == signPosition) {
					logger.error("Cannot find location of signature '" 
							+ sigData.getLocationString() + "' in pdf document. " +
							"Cannot add signature stamp.");
					throw new DataException("pdf.stamp.location.unknown", new Object[] {sigData.getLocationString()});
				}
				PdfContentByte background;
				background = pdfStamper.getOverContent(signPosition.getPageNum());
				if(!sigData.isMarkOnly()){
				// ���������� ����� ����� ������ ��
					PdfReader sigStampTemplate = new PdfReader(StampSettings.getSigStampTemplate());
	
			    	ByteArrayOutputStream stampOut = new ByteArrayOutputStream();
					PdfStamper stampStamper = new PdfStamper( sigStampTemplate, stampOut);
					AcroFields form = stampStamper.getAcroFields();
					
					Map<String, String> sigFields = sigData.getFields();
					Iterator<Map.Entry<String,String>> it = sigFields.entrySet().iterator();
					while (it.hasNext()) {
						Map.Entry<String,String> field = it.next();
						String fieldName = field.getKey();
						form.setField(fieldName, field.getValue());
						BaseFont font = PdfUtils.createFont(StampSettings.getSigFieldFontName(fieldName));
						form.setFieldProperty(fieldName, "textfont", font, null );
						form.setFieldProperty(fieldName, "textsize", StampSettings.getSigFieldFontSize(fieldName), null );
						form.regenerateField(fieldName);
					}
					stampStamper.setFormFlattening(true);
					stampStamper.close();
	
					// ����� �� ��������, ����������� ��� �� ��������
					PdfReader stampReader = new PdfReader( stampOut.toByteArray() );
	
					PdfImportedPage stampPage = pdfStamper.getImportedPage(stampReader,1);
					AffineTransform transform = AffineTransform.getTranslateInstance(signPosition.getX(),signPosition.getY());
					transform.scale(XScale, YScale);
					background.addTemplate(stampPage, transform);
				}
				// ������ ������� � �������� ������������ ���������
				PdfReader markTemplateReader = new PdfReader(StampSettings.getMarkStampTemplate());

				ByteArrayOutputStream markOut = new ByteArrayOutputStream();
				PdfStamper markStamper = new PdfStamper( markTemplateReader, markOut);
				AcroFields markForm = markStamper.getAcroFields();

				Map<String, String> markFields = sigData.getMarkFields();
				Iterator<Map.Entry<String,String>> iterator = markFields.entrySet().iterator();
				while (iterator.hasNext()) {
					Map.Entry<String,String> field = iterator.next();
					String fieldName = field.getKey();
					markForm.setField(fieldName, field.getValue());
					BaseFont font = PdfUtils.createFont(StampSettings.getSigFieldFontName(fieldName));
					markForm.setFieldProperty(fieldName, "textfont", font, null );
					markForm.setFieldProperty(fieldName, "textsize", StampSettings.getSigFieldFontSize(fieldName), null );
					markForm.regenerateField(fieldName);
				}
				markStamper.setFormFlattening(true);
				markStamper.close();
				
				PdfReader markReader = new PdfReader( markOut.toByteArray() );
				
				float deltaHeight = StampSettings.getSigStampHeight();
				if(sigData.isMarkOnly()){
					deltaHeight = 4;
				}
				PdfImportedPage markPage = pdfStamper.getImportedPage(markReader,1);
				AffineTransform transform = AffineTransform.getTranslateInstance(0, signPosition.getY()  - deltaHeight*YScale);
				transform.scale(XScale, YScale);
				background.addTemplate(markPage, transform);
			}
			
			// ��������������� �����
			Stamp.RegistrationData regData = stamp.getRegData();
			if (null != regData) {
				BaseFont font = PdfUtils.createFont(StampSettings.getRegStampFontName());
				if (regData.isMainStamp()) {
					// ����������� ���.������ �� ������ �������� ���������
					PdfContentByte overContent = pdfStamper.getOverContent( 1 );
					overContent.saveState();
					overContent.beginText();
					overContent.setFontAndSize(  font, StampSettings.getRegStampFontSize() * YScale);
					
					float dateXPosP = 0;
					float dateWidth = font.getWidthPoint(regData.getRegDate(), StampSettings.getRegStampFontSize());

					switch (StampSettings.getRegStampPlacement()){
						case NUMBER_CHAR:
							dateXPosP = regData.getXPosP() - dateWidth - StampSettings.getRegDateIndent();
							break;
						case REGDATE_START:
							dateXPosP = regData.getXPosP();
							break;
						default:
							dateXPosP = regData.getXPosP();
					}
		
					overContent.setTextMatrix( dateXPosP * XScale, (regData.getYPosP() + 3) * YScale);
					overContent.showText(regData.getRegDate());
					overContent.moveText( (dateWidth + StampSettings.getRegNumIndent()) * XScale, 0);
					overContent.showText(regData.getRegNum());
					overContent.endText();
					overContent.restoreState();
				}
				if (regData.isBottomStamp()) {
					// ����������� ���.������ � ������ ����������� �� ������ �������� ���������
					for (int i = 1; i <= pdfReader.getNumberOfPages(); i++) {
						PdfContentByte overContent = pdfStamper.getOverContent( i );
						overContent.saveState();
						overContent.beginText();
						BaseFont bottomFont = PdfUtils.createFont(StampSettings.getBottomStampFontName());
						overContent.setFontAndSize( bottomFont, StampSettings.getBottomStampFontSize() * YScale );
						
						String bottomStampText = MessageFormat.format(bottomStampTemplate, new Object[]{regData.getRegNum(), regData.getRegDate()});
						float bottomStampIndentX = 0;
						if (StampSettings.isBottomStampRightAlign()) {
							float bottomStampWidth = bottomFont.getWidthPoint(bottomStampText, StampSettings.getBottomStampFontSize());
							bottomStampIndentX = PdfUtils.A4XSizeP - bottomStampWidth - StampSettings.getBottomStampIndentX();
						}else {
							bottomStampIndentX = StampSettings.getBottomStampIndentX();
						}
						overContent.setTextMatrix( bottomStampIndentX * XScale, StampSettings.getBottomStampIndentY() * YScale);
						overContent.showText(bottomStampText);
						overContent.endText();
						overContent.restoreState();
					}
				}
			}
			pdfStamper.close();
			pdfReader.close();
		}
	    finally
		{
			IOUtils.closeQuietly(inputStream);
			IOUtils.closeQuietly(outputStream);
		}
	}
}
