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
package com.aplana.dmsi;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.WebServiceException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aplana.dbmi.Portal;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.ws.goststatisticserviceproxy.GostStatisticResponse;
import com.aplana.dbmi.ws.goststatisticserviceproxy.GostStatisticService;
import com.aplana.dbmi.ws.goststatisticserviceproxy.GostStatisticServiceImplService;
import com.aplana.dbmi.ws.goststatisticserviceproxy.ReceiveStatisticResponse;
import com.aplana.dbmi.ws.goststatisticserviceproxy.SendStatisticResponse;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.export.JExcelApiExporter;
import net.sf.jasperreports.engine.export.JExcelApiExporterParameter;
import net.sf.jasperreports.engine.util.FileBufferedOutputStream;

/**
 * ����� <code>GostStatisticReportGenerator</code> ���������� Excel ����� � ���������� ���� � ���� <code>InputStream</code>.
 * <p>
 * ������������������ �������� ����������:<br>
 * 1) ���� ���������� � ������� ������� � ��������� ������ � ����� <code>HashMap</code>. ���� � ������-���� ������� ������ ���������
 * �� ����������, �� ������ ������������.<br>
 * 2) ��������� ���������� ������. ������������ ����������� ������������ � ���������� ��������� �� ����������� uuid.<br>
 * 3) ���������� ������ ��� ���������� jasper ������� � ��������� ������� ��� ������ ��������
 *  ("������������ ���������", "��������������", "��������������").<br>
 * 4) �� ��������� ��������������� ������� ���������� ������ ���������� ������.
 * <p>
 *
 * @author  aklyuev
 */
public class GostStatisticReportGenerator {
	private Log logger = LogFactory.getLog(getClass());

	private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("dd.MM.yyyy");
	private static final String JASPER_REPORT_FOLDER = "dbmi/jasperReports/";
	private static final String RECEIVED_DOCS_SHEET_JASPTER_PATH = JASPER_REPORT_FOLDER + "gostStatisticReceivedDocsSheet.jasper";
	private static final String UNSHIPPED_SHEET_JASPTER_PATH     = JASPER_REPORT_FOLDER + "gostStatisticUnshippedDocsSheet.jasper";
	private static final String UNDELIVERED_SHEET_JASPTER_PATH   = JASPER_REPORT_FOLDER + "gostStatisticUndeliveredDocsSheet.jasper";

	private static final ObjectId SENT_CARD_STATUS          = ObjectId.state("sent");
	private static final ObjectId READY_TO_SEND_CARD_STATUS = ObjectId.state("jbr.distributionItem.readyForSend");
	private static final ObjectId NOT_SEND_CARD_STATUS      = ObjectId.state("jbr.distributionItem.notSent");

	private static final ObjectId OUTGOING_TEMPLATE_ID = ObjectId.template("jbr.outcoming");
	private static final ObjectId ORD_TEMPLATE_ID      = ObjectId.template("jbr.ord");

	private static final ObjectId REGISTERED_CARD_STATUS_ID         = ObjectId.state("registration");// ���������������
	private static final ObjectId IN_DELO_CARD_STATUS_ID            = ObjectId.state("delo"); // � ����
	private static final ObjectId IN_PROGRESS_CARD_STATUS_ID        = ObjectId.state("execution"); // ����������
	private static final ObjectId DONE_CARD_STATUS_ID               = ObjectId.state("done"); // ��������
	private static final ObjectId READY_TO_WRITE_OFF_CARD_STATUS_ID = ObjectId.state("ready-to-write-off"); // ����� � �������� � ����

	private static final Set<Object> UNSHIPPED_OUTGOING_STATUSES = new HashSet<Object>();
	{
		UNSHIPPED_OUTGOING_STATUSES.add(REGISTERED_CARD_STATUS_ID.getId());
		UNSHIPPED_OUTGOING_STATUSES.add(IN_DELO_CARD_STATUS_ID.getId());
	}

	private static final Set<Object> UNSHIPPED_ORD_STATUSES = new HashSet<Object>();
	{
		UNSHIPPED_ORD_STATUSES.add(REGISTERED_CARD_STATUS_ID.getId());
		UNSHIPPED_ORD_STATUSES.add(IN_DELO_CARD_STATUS_ID.getId());
		UNSHIPPED_ORD_STATUSES.add(IN_PROGRESS_CARD_STATUS_ID.getId());
		UNSHIPPED_ORD_STATUSES.add(DONE_CARD_STATUS_ID.getId());
		UNSHIPPED_ORD_STATUSES.add(READY_TO_WRITE_OFF_CARD_STATUS_ID.getId());
	}

	private static final String[] REPORT_SHEET_NAMES = {"������������ ���������", "��������������", "��������������"};

	private Set<String> addresses;
	private int period;
	private String username;
	private String password;

	Map<String, SendStatisticResponse> sendStatisticMap = new HashMap<String, SendStatisticResponse>();
	Map<String, ReceiveStatisticResponse> receiveStatisticMap = new HashMap<String, ReceiveStatisticResponse>();

	public GostStatisticReportGenerator(int period, Set<String> addresses, final String username, final String password) {
		this.period = period;
		this.addresses = addresses;
		this.username = username;
		this.password = password;
	}

	private GostStatisticService  getService(String address) throws Exception {

		GostStatisticServiceImplService service = new GostStatisticServiceImplService(null, new QName("http://impl.ws.dbmi.aplana.com/", "GostStatisticServiceImplService"));
		GostStatisticService servicePort = service.getGostStatisticServiceImplPort();
		BindingProvider bindingProvider = (BindingProvider) servicePort;
		bindingProvider.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, address + "/GostStatisticService/gostStatistic");
		
		bindingProvider.getRequestContext().put(BindingProvider.USERNAME_PROPERTY, username);
		bindingProvider.getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, password);
		return servicePort;
	}

	public InputStream generateReport() throws Exception {

		Date startTime = Calendar.getInstance().getTime();

		try {
			if (logger.isInfoEnabled()) {
				logger.info("generateReport() called at " + startTime);
			}

			Calendar reportDatePeriodFrom = Calendar.getInstance();
			reportDatePeriodFrom.setTime(startTime);
			reportDatePeriodFrom.add(Calendar.DATE, -period);

			ReceivedJasperPrintGenerator receiveJPGenerator = new ReceivedJasperPrintGenerator(period, startTime, DATE_FORMATTER.format(reportDatePeriodFrom.getTime()), DATE_FORMATTER.format(startTime), Portal.getFactory().getConfigService().loadConfigFile(RECEIVED_DOCS_SHEET_JASPTER_PATH));
			UnshippedUndeliveredJasperPrintGenerator unshippedJPGenerator = new UnshippedUndeliveredJasperPrintGenerator(period, startTime, DATE_FORMATTER.format(reportDatePeriodFrom.getTime()), DATE_FORMATTER.format(startTime), Portal.getFactory().getConfigService().loadConfigFile(UNSHIPPED_SHEET_JASPTER_PATH));
			UnshippedUndeliveredJasperPrintGenerator undeliveredJPGenerator = new UnshippedUndeliveredJasperPrintGenerator(period, startTime, DATE_FORMATTER.format(reportDatePeriodFrom.getTime()), DATE_FORMATTER.format(startTime), Portal.getFactory().getConfigService().loadConfigFile(UNDELIVERED_SHEET_JASPTER_PATH));

			//Get statistic from servers
			//TODO �������� ����������� ����� �������� ��� ������� ���������� ������������������
			for (String address : addresses) {
				try {
					if (logger.isDebugEnabled()) {
						logger.debug("Trying to gather GOST statistic data from " + address + " server");
					}
					GostStatisticService service = getService(address);
					GostStatisticResponse resp = service.getStatistic(period);

					List<SendStatisticResponse> sendStatList = resp.getSendStatisticResponseList();
					List<ReceiveStatisticResponse> receiveStatList = resp.getReceiveStatisticResponseList();

					if (logger.isDebugEnabled()) {
						logger.debug("There are " + sendStatList.size() + " messages which were sent and " + receiveStatList.size() + " received messages");
					}
					//Add data to maps
					for (SendStatisticResponse sendResp : sendStatList) {
						SendStatisticResponse previousVal = sendStatisticMap.put(sendResp.getUuid(), sendResp);
						if (previousVal != null) {
							if (logger.isDebugEnabled()) {
								logger.debug("Warning! SendStatistic entry with following UUID was already added! uuid = " + sendResp.getUuid());
							}
						}
					}

					for (ReceiveStatisticResponse receiveResp : receiveStatList) {
						ReceiveStatisticResponse previousVal = receiveStatisticMap.put(receiveResp.getUuid(), receiveResp);
						if (previousVal != null) {
							if (logger.isDebugEnabled()) {
								logger.debug("Warning! ReceiveStatistic entry with following UUID was already added! old = " + previousVal.getUuid() + " : " + previousVal.getStatus() + " new = " + receiveResp.getUuid() + " : " + receiveResp.getStatus());
							}
						}
					}
				} catch (WebServiceException serviceException) { 
					if (logger.isWarnEnabled()) {
						logger.warn("Cannot retreive gost statistic data from server " + address + " due to exception", serviceException);
					}
				}
			}

			//������ ����������� �� ������ ������������ � ���� ���������� � �����������
			for (String uuid : sendStatisticMap.keySet()) {
				if (uuid != null) {
				SendStatisticResponse sendValue = sendStatisticMap.get(uuid);
					if (receiveStatisticMap.containsKey(uuid)) {
						ReceiveStatisticResponse receiveValue = receiveStatisticMap.get(uuid);
						receiveJPGenerator.addData(sendValue, receiveValue);
					} else {
						//������������ ��� �������������� � ��������������
						if (SENT_CARD_STATUS.getId().equals(sendValue.getElmStatus())) {
							//�������� ���������, �� �� ���������
							undeliveredJPGenerator.addData(sendValue);
						} else {

							/*� ������������� �� ��������� �������� ���������, � ������� ��� ��������� �
							 * �������� "�� ���������" � "����� � ��������", � ��� �� ���� �� ���������,
							 * �� �� ������ ���� � ����� �� ��������: �� ���������, ����� � ��������,
							 * � �� ��� � ����� �� ��������: ���������������, ����������, ��������, ����� � �������� � ����, � ����.
							 */
							if (NOT_SEND_CARD_STATUS.getId().equals(sendValue.getElmStatus())||
								READY_TO_SEND_CARD_STATUS.getId().equals(sendValue.getElmStatus())) {

								if ((OUTGOING_TEMPLATE_ID.getId().equals(sendValue.getBasedocTemplate()) && UNSHIPPED_OUTGOING_STATUSES.contains(sendValue.getBasedocStatus())) ||
								    (ORD_TEMPLATE_ID.getId().equals(sendValue.getBasedocTemplate()) && UNSHIPPED_ORD_STATUSES.contains(sendValue.getBasedocStatus()))) {
									//�������� �� ���������
									unshippedJPGenerator.addData(sendValue);
								}
							}
						}
					}
				} else {
					if (logger.isDebugEnabled()) {
						logger.debug("uuid key is null!");
					}
				}
			}

			/*
			 * ������������ ������ xlsExporter, ������ ��� ������ � ��� �������� ����������, ����������� ������������� ����� ����� � xls (�������� freeze).
			 * �� ��� ����� ��������� ���������� poi.jar.
			 */
			JExcelApiExporter xlsExporter = new JExcelApiExporter();

			xlsExporter.setParameter(JExcelApiExporterParameter.SHEET_NAMES, REPORT_SHEET_NAMES);  

			List<JasperPrint> list = new ArrayList<JasperPrint>();
			list.add(receiveJPGenerator.getJSPrint());
			list.add(unshippedJPGenerator.getJSPrint());
			list.add(undeliveredJPGenerator.getJSPrint());

			xlsExporter.setParameter(JExcelApiExporterParameter.JASPER_PRINT_LIST, list);

			FileBufferedOutputStream stream = new FileBufferedOutputStream();
			xlsExporter.setParameter(JRExporterParameter.OUTPUT_STREAM, stream);
			xlsExporter.exportReport();

			if (logger.isInfoEnabled()) {
				logger.info("generateReport(): report generating process completed successfully in " + (Calendar.getInstance().getTimeInMillis() - startTime.getTime())/1000 + " seconds");
			}

			return stream.getDataInputStream();
		} catch (JRException e) {
			logger.error("Following exception occurred during processing jasper reports : " + e);
		}
		return null;
	}

	protected class ReceivedJasperPrintGenerator {
		int period;
		InputStream jasperFile;
		private List<Map<String, Object>> rowsData = new ArrayList<Map<String, Object>>();
		private Map<String, Object> reportHeadFields = new HashMap<String, Object>();

		//����� ���������� ��� ������������ ����������
		private int sentDocsCount = 0;
		private int regNotifCount = 0;
		private int rejectedCount = 0;
		private int noRegInfoCount = 0;

		public ReceivedJasperPrintGenerator(int period, Date currentTime, String periodFrom, String periodTo, InputStream jasperFile) {
			this.period = period;
			this.jasperFile = jasperFile;
			reportHeadFields.put("reportTime", currentTime);
			reportHeadFields.put("reportDatePeriodTo", periodTo);
			reportHeadFields.put("reportDatePeriodFrom", periodFrom);
		}

		public JasperPrint getJSPrint() throws JRException {
			//���-�� ����������, ������� ����������, �� ���� �� ���������� ����������� �������
			noRegInfoCount = sentDocsCount - (regNotifCount + rejectedCount);

			reportHeadFields.put("sentDocsCount", sentDocsCount);
			reportHeadFields.put("regNotifCount", regNotifCount);
			reportHeadFields.put("rejectedCount", rejectedCount);
			reportHeadFields.put("noRegInfoCount", noRegInfoCount);

			HashMap<String, Object> params = new HashMap<String, Object>();
			params.put(JRParameter.IS_IGNORE_PAGINATION, Boolean.TRUE);

			JasperPrint jrPrint = JasperFillManager.fillReport(jasperFile, params, new CustomDataSource(reportHeadFields, rowsData));
			jrPrint.setProperty("net.sf.jasperreports.print.keep.full.text", Boolean.TRUE.toString());

			return jrPrint;
		}

		public void addData(SendStatisticResponse sendValue, ReceiveStatisticResponse receiveValue) {
			sentDocsCount++;
			if (receiveValue.getIncomingRegRejectReason() != null && !receiveValue.getIncomingRegRejectReason().isEmpty()) {
				rejectedCount++;
			} else if (sendValue.getNotifRegisteredCreated() != null) {
				regNotifCount++;
			}
			rowsData.add(generateReceivedReportRow(sendValue, receiveValue));
		}

		private Map<String, Object> generateReceivedReportRow(SendStatisticResponse sendValue, ReceiveStatisticResponse receiveValue) {
			Map<String, Object> row = new HashMap<String, Object>();
			String uuid = sendValue.getUuid();

			if (logger.isDebugEnabled()) {
				logger.debug("This message was sent and received: " + uuid);
			}

			SendStatisticResponse sendStat = sendStatisticMap.get(uuid);
			ReceiveStatisticResponse receiveStat = receiveStatisticMap.get(uuid);

			//�������� ��� ����������� �����������, ���� �� ������, ���� ��� ����������� �� ���������
			String senderOrg;
			if (sendStat.getSenderOrgFullName() != null && !sendStat.getSenderOrgFullName().isEmpty()) {
				senderOrg = sendStat.getSenderOrgFullName();
			} else {
				senderOrg = sendStat.getDefaultOrgFullName();
			}
			row.put("senderOrgFullName", senderOrg);
			
			//�������� ��������������� ����� ����������\���
			row.put("basedocRegNumber", sendStat.getBasedocRegNumber());
			//���� ����������� ���������\���
			row.put("basedocRegDate", sendStat.getBasedocRegDate());
			//���� � ����� ������������ ���
			row.put("elmCreatedDate", sendStat.getElmCreatedDate());
			//UUID ������
			row.put("uuid", uuid);
			//���� � ����� �������� ��������� (����� �������� xml ���� ���������)
			row.put("gostMessageCreate", sendStat.getGostMessageCreateTime());
			//������������ ����������
			row.put("destOrgFullName", sendStat.getDestOrgFullName());
			//���� �������� ��������
			row.put("deliveryDocCreated", receiveStat.getCreated());
			//���� �������� ��������� ���������
			row.put("incomingDocCreated", receiveStat.getIncomingCreated());
			//���� � ����� �������� ����������� � ��������
			row.put("notifReceivedCreated", receiveStat.getNotifReceivedCreated());
			//���� � ����� ����������� ����������� � �������� � ������� ����������
			row.put("notifReceivedDelivered", sendStat.getNotifReceivedCreated());
			//UUID ����������� � ��������
			row.put("notifReceivedId", receiveStat.getNotifReceivedId());

			if (receiveStat.getIncomingRegRejectReason() != null && !receiveStat.getIncomingRegRejectReason().isEmpty()) {
				//��������� ������� ������ � �����������
				//���� �������� ����������� ����������� � �����������
				row.put("notifRejectedDelivered", sendStat.getNotifRegisteredCreated());
				//���� � ����� �������� ����������� � �����������
				row.put("notifRejectedCreated", receiveStat.getNotifRegCreated());
				//UUID ����������� � �����������
				row.put("notifRejectedId", receiveStat.getNotifRegId());
				//������� ������ � �����������
				row.put("rejectReason", receiveStat.getIncomingRegRejectReason());
			}
			else {
				//��������� ������� ������ ������ � �����������
				//���� �������� ����������� ����������� � �����������
				row.put("notifRegisteredDelivered", sendStat.getNotifRegisteredCreated());
				//���� � ����� �������� ����������� � �����������
				row.put("notifRegCreated", receiveStat.getNotifRegCreated());
				//UUID ����������� � �����������
				row.put("notifRegId", receiveStat.getNotifRegId());
				//��������������� ����� ���������
				row.put("incomingRegNum", receiveStat.getIncomingRegNum());
				//���� ����������� ��������� ���������
				row.put("incomingRegistered", receiveStat.getIncomingRegistered());
			}
			return row;
		}
	}

	/*
	 * ����� ��� ��������� ����� � �������������� �����������
	 */
	protected class UnshippedUndeliveredJasperPrintGenerator {
		int period;
		InputStream jasperFile;
		private List<Map<String, Object>> rowsData = new ArrayList<Map<String, Object>>();
		private Map<String, Object> reportHeadFields = new HashMap<String, Object>();

		//���������� ��� ������������� ����������
		int count = 0;
		int readyToSendCount = 0;//���������� ���������� � ������� "����� � ��������"
		int notSentCount = 0;//���������� ���������� � ������� "�� ���������"

		public UnshippedUndeliveredJasperPrintGenerator(int period, Date currentTime, String periodFrom, String periodTo, InputStream jasperFile) {
			this.period = period;
			this.jasperFile = jasperFile;
			reportHeadFields.put("reportTime", currentTime);
			reportHeadFields.put("reportDatePeriodTo", periodTo);
			reportHeadFields.put("reportDatePeriodFrom", periodFrom);
		}

		public JasperPrint getJSPrint() throws JRException {
			reportHeadFields.put("count", count);
			reportHeadFields.put("readyToSendCount", readyToSendCount);
			reportHeadFields.put("notSentCount", notSentCount);

			HashMap<String, Object> params = new HashMap<String, Object>();
			params.put(JRParameter.IS_IGNORE_PAGINATION, Boolean.TRUE);

			JasperPrint jrPrint = JasperFillManager.fillReport(jasperFile, params, new CustomDataSource(reportHeadFields, rowsData));
			jrPrint.setProperty("net.sf.jasperreports.print.keep.full.text", Boolean.TRUE.toString());

			return jrPrint;
		}

		public void addData(SendStatisticResponse sendValue) {
			count++;
			if (sendValue.getElmStatus().equals(READY_TO_SEND_CARD_STATUS.getId())) {
				readyToSendCount++;
			} else if (sendValue.getElmStatus().equals(NOT_SEND_CARD_STATUS.getId())) { 
				notSentCount++;
			}
			rowsData.add(generateReceivedReportRow(sendValue));
		}

		private Map<String, Object> generateReceivedReportRow(SendStatisticResponse sendValue) {
			Map<String, Object> row = new HashMap<String, Object>();
			String uuid = sendValue.getUuid();
			SendStatisticResponse sendStat = sendStatisticMap.get(uuid);

			//�������� ��� ����������� �����������, ���� �� ������, ���� ��� ����������� �� ���������
			String senderOrg;
			if (sendStat.getSenderOrgFullName() != null && !sendStat.getSenderOrgFullName().isEmpty()) {
				senderOrg = sendStat.getSenderOrgFullName();
			} else {
				senderOrg = sendStat.getDefaultOrgFullName();
			}
			row.put("senderOrgFullName", senderOrg);

			//�������� ��������������� ����� ����������\���
			row.put("basedocRegNumber", sendStat.getBasedocRegNumber());
			//���� ����������� ���������\���
			row.put("basedocRegDate", sendStat.getBasedocRegDate());
			//���� � ����� ������������ ���
			row.put("elmCreatedDate", sendStat.getBasedocRegDate());
			//UUID ������
			row.put("uuid", uuid);
			//���� � ����� �������� ��������� (����� �������� xml ���� ���������)
			row.put("gostMessageCreate", sendStat.getGostMessageCreateTime());
			//������ ���
			row.put("status", sendStat.getElmStatusName());
			//������������ ����������
			row.put("destOrgFullName", sendStat.getDestOrgFullName());

			return row;
		}
	}
}
