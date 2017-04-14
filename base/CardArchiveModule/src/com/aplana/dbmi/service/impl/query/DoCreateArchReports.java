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
package com.aplana.dbmi.service.impl.query;

import com.aplana.dbmi.Portal;
import com.aplana.dbmi.action.*;
import com.aplana.dbmi.jasperreports.DefReport;
import com.aplana.dbmi.model.*;
import com.aplana.dbmi.service.*;
import com.aplana.dbmi.service.impl.ActionQueryBase;
import com.aplana.dbmi.service.impl.ObjectQueryBase;
import com.aplana.dbmi.service.impl.SaveQueryBase;
import com.lowagie.text.pdf.*;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import org.apache.commons.io.IOUtils;
import org.w3c.dom.Document;

import javax.ejb.CreateException;
import javax.imageio.ImageIO;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;
import javax.servlet.ServletException;
import javax.sql.DataSource;
import javax.xml.parsers.DocumentBuilderFactory;
import java.awt.image.BufferedImage;
import java.io.*;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Query used to perform {@link CreateArchReports} action
 */
public class DoCreateArchReports extends ActionQueryBase implements WriteQuery {
	private static final long serialVersionUID = 1L;

	private String jasperUrl = null;

	//private static final ObjectId docLinks = ObjectId.predefined(CardLinkAttribute.class, "jbr.files");
	private static final ObjectId material = ObjectId.predefined(MaterialAttribute.class, "jbr.person.material");


	public static final String CONFIG_FILE_PREFIX = "dbmi/jasperReports/";
	public static final String CONFIG_SUB_FILE_PREFIX = "dbmi/jasperReports/subReports/";
	public static final String CONFIG_IMAGE_PREFIX = "dbmi/jasperReports/images/";
	public static final String PARAM_NAME_CONFIG = "nameConfig"; // ��� ��������� ������� ��������� ��� ����. ������
	public static final String CONFIG_BOUND_CARDS = "reportChartBoundCards";
	public static final String PARAM_CARDID = "cardId";
	public static final String PARAM_EXPORT_FORMAT = "exportFormat";

	public static final String PREFIX_TYPEDATA_STRING = "S_";
	public static final String PREFIX_TYPEDATA_DATE = "D_";
	public static final String PREFIX_TYPEDATA_LONG = "L_";
	public static final String PREFIX_TYPEDATA_SELECTKIT = "K_";
	public static final String PREFIX_TYPEDATA_PERSONS = "P_";
	public static final String PREFIX_TYPEDATA_VALUESREF = "R_";
	public static final String PREFIX_TYPEDATA_CARDS = "C_";
	public static final String DATE_FORMAT = "yyyy-MM-dd";

	public static final String EXPORT_PDF = "PDF";
	public static final String EXPORT_DOCX = "DOCX";

	// ����������� �������� ����� ������ ����� � �������� ������� ������ � ���������� 
	private static final ObjectId ATTR_DOCLINKS = ObjectId.predefined(CardLinkAttribute.class, "jbr.docHistory");
	private static final ObjectId TEMPLATE_FILE = ObjectId.predefined(Template.class, "jbr.file");


	public Object processQuery() throws DataException {
		Card cardArch = null;
		Boolean createReport = true;
		final CreateArchReports archReports = getDownloadFile();

		jasperUrl = archReports.getjasperUrl();
		try {
			if (jasperUrl == null || jasperUrl.length() == 0)
				jasperUrl = Portal.getFactory().getConfigService().getConfigFileUrl("").toString() + CONFIG_SUB_FILE_PREFIX;
		} catch (Exception e) {
			jasperUrl = "";
		}
		//�������� Id ���������
		final ObjectId cardId = archReports.getCardId();
		// �������� �������� xml ����������������� ����� �������
		final String reportName = archReports.getReportName();
		// �������� ��������
//		ObjectQueryBase subQuery = getQueryFactory().getFetchQuery(Card.class);
//		subQuery.setId(cardId);
		// �������� �������� ���������
		final Card card = loadCardById(cardId);
		// �������� ������ ��������
		final CardLinkAttribute files = card.getAttributeById(ATTR_DOCLINKS);
		Iterator<ObjectId> iter = (files != null) ? files.getIdsLinked().iterator() : null;
		//��� �� ���� ��������� � �������� ��� ��������
		while (iter != null && iter.hasNext()) {
			final ObjectId cardFileID =  iter.next();
			// �������� �������� ���������
			final Card cardFile = loadCardById(cardFileID);
			final String cardFileName = cardFile.<MaterialAttribute>getAttributeById(material).getMaterialName();
			//���� �������� ����� ������ ��� ������� ��������� �� ��-������
			if (cardFileName != null && cardFileName.contains("��������_�����(�����)")) {
				createReport = false;
				cardArch = cardFile;
			}
		}
		if (!createReport) {
			File tempFile = null;
			File newVersion = null;
			try {
				String fileCardId = cardArch.getId().getId().toString();
				DownloadFile action = new DownloadFile();
				action.setCardId(new ObjectId(Card.class, Long.parseLong(fileCardId)));
				/*ActionQueryBase actionQuery = getQueryFactory().getActionQuery(action);
				actionQuery.setAction(action);*/
				//Material material = (Material) getDatabase().executeQuery(getSystemUser(), actionQuery);
				Material material = AsyncServiceBeanInstance().doAction(action);
				InputStream data = material.getData();
				newVersion = generateReport(reportName, cardId.getId().toString());
				List<InputStream> pdfs = new ArrayList<InputStream>();
				pdfs.add(data);
				pdfs.add(new FileInputStream(newVersion));
				tempFile = File.createTempFile("��������_�����(�����)", ".pdf");
				OutputStream output = new FileOutputStream(tempFile);
				concatPDFs(pdfs, output, false);
				attachFileToFileCard(cardArch, tempFile);
				SaveDoc(cardId, card, cardArch);
			} catch (DataException e) {
				logger.error("Error while concat PDFs", e);
				throw e;
			} catch (Exception e) {
				logger.error("Error while concat PDFs", e);
				throw new DataException("Error while concat PDFs", e);
			} finally {
				// delete temporary files
				if (tempFile != null) {
					tempFile.delete();
					if (logger.isDebugEnabled()) {
						logger.debug("temporary file " + tempFile.getAbsolutePath() + " deleted");
					}
				}
				if (newVersion != null) {
					newVersion.delete();
					if (logger.isDebugEnabled()) {
						logger.debug("temporary file " + newVersion.getAbsolutePath() + " deleted");
					}
				}
			}
		}
		// ����� ������� ����� ����
		else {
			File tempFile = null;
			try {
				tempFile = generateReport(reportName, cardId.getId().toString());
				// ������� �������� ��������
				Card fileCard;
				try {
					fileCard = createFileCard();
				} catch (Exception e) {
					logger.error("Could not create card Material", e);
					return false;
				}

				// ��������� � �������� �������� ����
				try {
					attachFileToFileCard(fileCard, tempFile);
				} catch (Exception e) {
					logger.error("Unable to download the file " + tempFile.getName() + " to the card material", e);
					return false;
				}
				SaveDoc(cardId, card, fileCard);
			} catch (DataException e) {
				logger.error("Error while creating report PDF", e);
				throw e;
			} catch (Exception e) {
				logger.error("Error while creating report PDF", e);
				throw new DataException("Error while creating report PDF", e);
			} finally {
				// delete temporary files
				if (tempFile != null) {
					tempFile.delete();
					if (logger.isDebugEnabled()) {
						logger.debug("temporary file " + tempFile.getAbsolutePath() + " deleted");
					}
				}
			}
		}
		return null;
	}

	public CreateArchReports getDownloadFile() {
		return getAction();
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	protected File generateReport(String reportName, String strCardId)
			throws ServletException, IOException, DataException {
		File tempFile = null;
		try {
			tempFile = File.createTempFile("��������_�����(�����)", ".pdf");
		} catch (IOException e) {

		}
		Map params = new HashMap();
		Object objValuePar = getObjectParameter("L_" + strCardId);
		params.put("card_id", objValuePar);
		// ��������-����, ����, ��� �������� key �����
		params.put("card_id" + "_ISNULL", Boolean.FALSE);

		//���� � �������� ����������
		params.put("path", jasperUrl);

		Person currentPerson = getPrimaryQuery().getUser().getPerson();
		objValuePar = getObjectParameter("L_" + currentPerson.getId().getId().toString());
		// �������� � ���������� �������� ������������ (��� �����). ��� ���������: current_user
		params.put("current_user", objValuePar);

		// �������� ��������� ������������� �������� ��������� ������
		DefReport defRC = null;
		try {
			InputStream stream = Portal.getFactory().getConfigService().loadConfigFile(
					CONFIG_FILE_PREFIX + reportName + ".xml");
			try {
				Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(stream);
				defRC = new DefReport();
				defRC.setConfig(doc);
			} finally {
				IOUtils.closeQuietly(stream);
			}
		} catch (Exception e) {
			logger.error("�� ������� �������� ���������������� ���� " + reportName, e);
			throw new DataException("�� ������� �������� ���������������� ���� " + reportName, e);
		}

		// ��������� ������ �����������
		String nameImage = null;
		try {
			Map<String, String> namesImages = defRC.getNamesImages();
			for (String parImage : namesImages.keySet()) {
				nameImage = namesImages.get(parImage);
				InputStream stream = Portal.getFactory().getConfigService().loadConfigFile(
						CONFIG_IMAGE_PREFIX + nameImage);
				try {
					BufferedImage image = ImageIO.read(stream);
					params.put(parImage, image);
				} finally {
					IOUtils.closeQuietly(stream);
				}
			}
		} catch (Exception e) {
			logger.error("�� ������� �������� ���� � ������������ " + nameImage, e);
		}

		// ��������� ������ JasperReport
		InputStream stream = Portal.getFactory().getConfigService().loadConfigFile(
				CONFIG_FILE_PREFIX + defRC.getNameReport() + ".jasper");
		Connection conn = null;
		try {
			JasperPrint jasperPrint;
			if (defRC.isQuery()) {
				conn = getConnection();
				jasperPrint = JasperFillManager.fillReport(stream, params, conn);
			} else {
				jasperPrint = JasperFillManager.fillReport(stream, params, new JREmptyDataSource());
			}

			JRExporter exporter = new JRPdfExporter();
			exporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
			exporter.setParameter(JRExporterParameter.OUTPUT_FILE, tempFile);
			exporter.exportReport();
		} catch (Exception e) {
			logger.error("Error while filling report " + defRC.getNameReport(), e);
			throw new DataException("Can't create archive report", e);
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					logger.error("Couldn't close connection");
				}
			}
			IOUtils.closeQuietly(stream);
		}
		return tempFile;
	}

	public static Connection getConnection() throws SQLException, NamingException {
		InitialContext context = new InitialContext();
		DataSource dataSource = (DataSource) context.lookup("java:/jdbc/DBMIDS");
		Connection con = dataSource.getConnection();
		return con;
	}

	// ���������� ����� file � �������� �������� �������� card 
	private void attachFileToFileCard(Card card, File file) throws FileNotFoundException, DataException, ServiceException {
		FileInputStream fis = new FileInputStream(file);
		try {
			UploadFile uploadAction = new UploadFile();
			uploadAction.setCardId(card.getId());
			//String fileName = file.getName().substring(0, 26)+".pdf";
			//AKiekbaev 2011.12.19 - ������������� ���
			String fileName = "��������_�����(�����).pdf";
			uploadAction.setFileName(fileName);
			uploadAction.setData(fis);
			/*ActionQueryBase uploadQuery = getQueryFactory().getActionQuery(uploadAction);
			uploadQuery.setAction(uploadAction);*/
			//getDatabase().executeQuery(getSystemUser(), uploadQuery);
			AsyncServiceBeanInstance().doAction(uploadAction);
			card = loadCardById(card.getId());    // ����� �������� �������� � �������� ���� ��������� �������� �� ��
			MaterialAttribute attr = card.getAttributeById(Attribute.ID_MATERIAL);
			attr.setMaterialName(fileName);
			attr.setMaterialType(MaterialAttribute.MATERIAL_FILE);

			StringAttribute name = card.getAttributeById(Attribute.ID_NAME);
			name.setValue(fileName);

			final SaveQueryBase query = getQueryFactory().getSaveQuery(card);
			query.setObject(card);
			getDatabase().executeQuery(getUser(), query);
		} finally {
			IOUtils.closeQuietly(fis);
		}
	}

	public void concatPDFs(List<InputStream> streamOfPDFFiles, OutputStream outputStream, boolean paginate) {
		com.lowagie.text.Document document = new com.lowagie.text.Document();
		try {
			List<InputStream> pdfs = streamOfPDFFiles;
			List<PdfReader> readers = new ArrayList<PdfReader>();
			int totalPages = 0;

			// Create Readers for the pdfs.
			for (InputStream pdf : pdfs) {
				PdfReader pdfReader = new PdfReader(pdf);
				readers.add(pdfReader);
				totalPages += pdfReader.getNumberOfPages();
			}
			// Create a writer for the outputstream
			PdfWriter writer = PdfWriter.getInstance(document, outputStream);

			document.open();
			BaseFont bf = BaseFont.createFont(BaseFont.HELVETICA,
					BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
			PdfContentByte cb = writer.getDirectContent(); // Holds the PDF
			// data

			PdfImportedPage page;
			int currentPageNumber = 0;
			int pageOfCurrentReaderPDF = 0;

			// Loop through the PDF files and add to the output.
			for (PdfReader pdfReader : readers) {
				// Create a new page in the target for each source page.
				while (pageOfCurrentReaderPDF < pdfReader.getNumberOfPages()) {
					document.newPage();
					pageOfCurrentReaderPDF++;
					currentPageNumber++;
					page = writer.getImportedPage(pdfReader, pageOfCurrentReaderPDF);
					cb.addTemplate(page, 0, 0);

					// Code for pagination.
					if (paginate) {
						cb.beginText();
						cb.setFontAndSize(bf, 9);
						cb.showTextAligned(PdfContentByte.ALIGN_CENTER, ""
										+ currentPageNumber + " of " + totalPages, 520,
								5, 0);
						cb.endText();
					}
				}
				pageOfCurrentReaderPDF = 0;
				pdfReader.close();
			}
			outputStream.flush();
			document.close();
			outputStream.close();
		} catch (Exception e) {
			logger.error("Error while concatenating PDFs", e);
		} finally {
			if (document.isOpen())
				document.close();
			for (InputStream stream : streamOfPDFFiles) {
				IOUtils.closeQuietly(stream);
			}
			try {
				if (outputStream != null)
					outputStream.close();
			} catch (IOException ioe) {
				logger.error("Error while closing stream", ioe);
			}
		}
	}

	private Object getObjectParameter(String strValue) {
		if (strValue == null || "".equals(strValue)) {
			return null;
		}
		if (strValue.startsWith(PREFIX_TYPEDATA_STRING)) {
			return strValue.substring(PREFIX_TYPEDATA_STRING.length());
		} else if (strValue.startsWith(PREFIX_TYPEDATA_DATE)) {
			String strDate = strValue.substring(PREFIX_TYPEDATA_DATE.length());
			try {
				if (strDate.equals("")) {
					return null;
				} else {
					long time = (new SimpleDateFormat(DATE_FORMAT)).parse(strDate).getTime();
					return new Timestamp(time);
				}
			} catch (ParseException e) {
				e.printStackTrace();
			}
		} else if (strValue.startsWith(PREFIX_TYPEDATA_LONG)) {
			String strLong = strValue.substring(PREFIX_TYPEDATA_LONG.length());
			return Long.valueOf(strLong);
		} else if (strValue.startsWith(PREFIX_TYPEDATA_SELECTKIT)) {
			String choice = strValue.substring(PREFIX_TYPEDATA_SELECTKIT.length());
			return choice;
		} else if (strValue.startsWith(PREFIX_TYPEDATA_PERSONS)) {
			String persons = strValue.substring(PREFIX_TYPEDATA_PERSONS.length());
			return persons;
		} else if (strValue.startsWith(PREFIX_TYPEDATA_VALUESREF)) {
			String valuesRef = strValue.substring(PREFIX_TYPEDATA_VALUESREF.length());
			return valuesRef;
		} else if (strValue.startsWith(PREFIX_TYPEDATA_CARDS)) {
			String cards = strValue.substring(PREFIX_TYPEDATA_CARDS.length());
			return cards;
		}
		return null;
	}

	// �������� ������� ��������
	private Card createFileCard() throws DataException {

		CreateCard createCardAction = new CreateCard();
		createCardAction.setTemplate(TEMPLATE_FILE);

		final ActionQueryBase createCard = getQueryFactory().getActionQuery(createCardAction);
		createCard.setAction(createCardAction);


		Card fileCard = getDatabase().executeQuery(getUser(), createCard);

		StringAttribute name = fileCard.getAttributeById(Attribute.ID_NAME);
		name.setValue("file");
		final SaveQueryBase query = getQueryFactory().getSaveQuery(fileCard);
		query.setObject(fileCard);


		ObjectId fileCardId = getDatabase().executeQuery(getUser(), query);
		fileCard.setId((Long) fileCardId.getId());
		try {
			UnlockObject unlock = new UnlockObject(fileCardId);
			final ActionQueryBase unlockQuery = getQueryFactory().getActionQuery(unlock);
			unlockQuery.setAction(unlock);

			getDatabase().executeQuery(getUser(), unlockQuery);
		} catch (DataException ex) {
			logger.debug("Failed to unlock card: " + fileCardId + "\n" + ex);
			throw ex;
		}
		return fileCard;
	}

	public void SaveDoc(ObjectId docId, Card docCard, Card fileCard) throws DataException {
		ObjectId targetId = docId;
		Card targetCard = docCard;
		CardLinkAttribute docLinks = targetCard.getCardLinkAttributeById(ATTR_DOCLINKS);
		if (docLinks != null)
			docLinks.clear();

		final SaveQueryBase query = getQueryFactory().getSaveQuery(targetCard);
		query.setObject(targetCard);
		getDatabase().executeQuery(getUser(), query);
		// (YNikitin, 2011/12/15) �.�. ����� � ��� ������ �������� � �������� ��������, �� �������� ����� �������� ����� SQL ������ � �������� ������� ������ � ����������
		// ������������ �������� �������� �������� � ������ ������� ��������
		if (docLinks == null) {
			logger.error("Attribute " + ATTR_DOCLINKS + " in the target card " + targetId.getId() + " was not found");
			//return false;
		} else if (fileCard != null && fileCard.getId() != null) {
			String sql = "insert into attribute_value (card_id, attribute_code, number_value)\n" +
					"select ?, '" + ATTR_DOCLINKS.getId() + "', ? \n" +
					"where not exists(select 1 from attribute_value av where av.card_id = ? and av.attribute_code = '" + ATTR_DOCLINKS.getId() + "' and av.number_value = ?)";
			int insertCount = getJdbcTemplate().update(sql,
					new Object[] {targetCard.getId().getId(), fileCard.getId().getId(), targetCard.getId().getId(), fileCard.getId().getId()}, new int[] {Types.NUMERIC, Types.NUMERIC, Types.NUMERIC, Types.NUMERIC});
			//docLinks.addLabelLinkedCard(fileCard);
			if (insertCount > 0)
				logger.info("Attribute " + ATTR_DOCLINKS + " in the target card " + targetId.getId() + " is filled");
		}
	}

	public AsyncDataServiceBean AsyncServiceBeanInstance() {
		AsyncDataServiceBean asyncServiceBean = null;
		InitialContext context = null;
		AsyncDataServiceHome asyncHome = null;
		try {
			context = new InitialContext();
			asyncHome = (AsyncDataServiceHome) PortableRemoteObject.narrow(
					context.lookup("ejb/dbmi_async"),
					AsyncDataServiceHome.class);
		} catch (NamingException ex) {
			logger.error("Error during DataServiceHome context initialization");
		}
		if (context == null || asyncHome == null)
			return null;

		try {
			AsyncDataService service = asyncHome.create();
			asyncServiceBean = new AsyncDataServiceBean();
			asyncServiceBean.setService(service,
					service.authUser(new SystemUser(), "127.0.0.1"), null);
		} catch (RemoteException ex) {
			logger.error("Error during DataServiceBean initialization");
		} catch (CreateException ex) {
			logger.error("Error during DataServiceBean initialization");
		} catch (DataException ex) {
			logger.error("Error during DataServiceBean initialization");
		}

		return asyncServiceBean;
	}

	private Card loadCardById(ObjectId cardId) throws DataException {
		ObjectQueryBase subQuery = getQueryFactory().getFetchQuery(Card.class);
		subQuery.setId(cardId);
		// �������� �������� ���������
		return getDatabase().executeQuery(getUser(), subQuery);
	}
}
