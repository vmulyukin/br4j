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
package com.aplana.dbmi.filestorage.convertmanager;

import com.aplana.dbmi.action.Material;
import com.aplana.dbmi.common.utils.file.DefinesTypeFile;
import com.aplana.dbmi.common.utils.file.MimeContentTypeReestrBean;
import com.aplana.dbmi.filestorage.converters.*;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.pdfa.converter.PDFAConverter;
import com.aplana.dbmi.storage.impl.PdfConvertorSettings;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jmx.export.annotation.*;

import java.io.File;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@ManagedResource(objectName="br4j:name=pdfConvertManager", description="MBean for PdfConvertManager")
public class PdfConvertManager {
	final static long DEFAULT_REACTION_MSEC = 2000;
	final static int DEFAULT_TASK_QUEUE_LIMIT = 10;
	
	final static int PRIORITY_BACKGROUND = 0;
	final static int PRIORITY_NORMAL = 50;
	final static int PRIORITY_MAX = 1000000;
	final static int[] PRIORITY_WEIGHTS = new int[]{4,7,12,19,28,30};

	protected final Log logger = LogFactory.getLog(getClass());

	private List<Task> tasks = null;

	private ExecutorService service = null;
	private Runnable runn = null;

	private long reactionTime = DEFAULT_REACTION_MSEC;
	private int taskQueueLimit = DEFAULT_TASK_QUEUE_LIMIT;
	
	private ActiveTaskQueueController activeTaskQueueController = null;

	final private ConverterStatistic stat = new SimpleStat();

	//create PDF/A converter
	private PDFAConverter pdfaConverter = new PDFAConverter();

	public PdfConvertManager() {
		logger.info("Create PdfConvertManager ");
		activeTaskQueueController = ActiveTaskQueueController.getInstance();
		activeTaskQueueController.addConverterStatistic(stat);
		tasks = Collections.synchronizedList(new LinkedList<Task>());		
		service = new ThreadPoolExecutor(1, 1, 1000, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(5), new ThreadPoolExecutor.DiscardPolicy()); 		
		setVerifyTime(PdfConvertorSettings.getReactionTime());
		setTaskQueueLimit(PdfConvertorSettings.getQueueSize());

		//set PDF/A converter multi-processing
		pdfaConverter.setMaxProcessCount(this.taskQueueLimit);
		//set PDF/A version
		pdfaConverter.setPDFAVersion(PdfConvertorSettings.getPdfAVersion());
		//set PDF/A compatibility policy
		pdfaConverter.setPDFACompatibilityPolicy(PdfConvertorSettings.getPdfACompatibilityPolicy());
		//set process color model
		pdfaConverter.setProcessColorModel(PdfConvertorSettings.getPdfAColorModel());
		//set ICC profile
		pdfaConverter.setIccFilePath(PdfConvertorSettings.getPdfAIccFilePath());
		//set PDF/A definition file
		pdfaConverter.setPdfADefFilePath(PdfConvertorSettings.getPdfADefFilePath());
		new File(PdfConvertorSettings.getConvertorTempDir()).mkdirs();
		new File(PdfConvertorSettings.getConvertorLogDir()).mkdirs();

		// Update ICC profile file path in PDFA definition file
		PdfConvertorSettings.updatePdfADefFile();
		
		//Update class path (required to properly run PDF/A converter in separate JVM process)
		String jbossServerHome = System.getProperty("jboss.server.home.dir");
		String jbossHome = System.getProperty("jboss.home.dir");
		String pathSeparator = System.getProperty("path.separator");
		StringBuilder classPath = new StringBuilder(System.getProperty("java.class.path"));
		classPath.append(pathSeparator).append(jbossServerHome).append("/lib/*");
		classPath.append(pathSeparator).append(jbossHome).append("/lib/*");
		
		System.setProperty("java.class.path", classPath.toString());
		
		newThread();
		newTimer();
	}
	
	private void newTimer() {
		final long period_msec=1000*60*5; // 5 minutes
		final long delay_msec=1000*10; // 10- seconds
		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				stat.logPrint();
			}
		}, delay_msec, period_msec);
	}

	/**
	 *  ������ ����� ������� ����� ������ ������ �� �����������
	 */
	private void newThread() {
		if (runn == null) {
			runn = new Runnable() {
				public void run() {
					try {
						while (true) {
							Thread.sleep(reactionTime);
							activeTaskQueueController.clearDoneTask();
							
							if (tasks.size() == 0 || !activeTaskQueueController.checkAdd()) {
								continue;
							}

							ThreadTask threadtask = createConvertTask();
							if(threadtask==null){
								continue;
							}
							threadtask.start();
							activeTaskQueueController.addThreadTask(threadtask);		
						}
					} catch (InterruptedException ex) {
						logger.debug( MessageFormat.format(  "PDF convert timedout '''{0}':\n {1}' ", PdfConvertManager.class.getName(), ex));
					}
				}
			};
		}
	}
	
	/**
	 * ������ ������� ������, ������� �������� ��������� � ���� ��� ��������������
	 * @return ���������� ������� ������ ���������������
	 */
	private ThreadTask createConvertTask() {
		final Task task = takeTask();
		final PdfConverter pdfconverter = getPdfConverter(task.getMaterial());

		if(pdfconverter==null){
			return null;
		}

		final ThreadTask threadtask = new ThreadTask(task, pdfconverter);
		return threadtask;
	}
		
	
	/**
	 * ��������� ��������� � ����������� �� ���� ����� 
	 * @param material ���������� � ��������
	 * @return ���������� ���������
	 */
	private PdfConverter getPdfConverter(Material material) {
		PdfConverter pdfconverter=null;
		String mime = MimeContentTypeReestrBean.getMimeType(material);
		if(DefinesTypeFile.isConvertableToPdfByOo(mime)){
			pdfconverter = new OoPdfConverter();
		}else if(DefinesTypeFile.isImage(mime)){
			pdfconverter = new ImgToPdfConverter();
		}else if(DefinesTypeFile.isTiff(mime)){
			pdfconverter = new TiffToPdfConverter();
		}else if(DefinesTypeFile.isTxt(mime)){
			OoPdfConverter ooPdfConverter = new OoPdfConverter();
			ooPdfConverter.setAutoEncoding(true);
			pdfconverter=ooPdfConverter;
		}else if (DefinesTypeFile.isPDF(mime)){
			pdfconverter = new PdfToPdfAConverter(this.pdfaConverter);
		}
		//there will be filename with illegal extension. do not convert them.
		return pdfconverter;
	}
	
	public void setVerifyTime(String value) {
		if (value != null && value.length() > 0) {
			try {
				this.reactionTime=Long.valueOf(value);
				return;
			} catch(Exception ex) {
				logger.error("problem assigning verify time by value '"+ value+ "', default  "+  DEFAULT_REACTION_MSEC+ " is used instead", ex);
			}
		}
		this.reactionTime = DEFAULT_REACTION_MSEC;
	}

	private void setTaskQueueLimit(String newLimit) {
		if (newLimit != null && newLimit.length() > 0) {
			try {
				this.taskQueueLimit= Integer.valueOf(newLimit);
				return;
			} catch(Exception ex) {
				logger.error("problem assigning task queue limit by value '"+ newLimit + "', default  "+  DEFAULT_TASK_QUEUE_LIMIT+ " is used instead", ex);
			}
		}
		this.taskQueueLimit = DEFAULT_TASK_QUEUE_LIMIT;
	}

	@Override
	protected void finalize() throws Throwable {
		logger.error("Manager finalize");
		super.finalize();
	}

	/**
	 * ��������� ����� ������ � �������
	 * @param task ����� ������
	 */
	public synchronized void addTask(Task task) {
		if(activeTaskQueueController.checkExecutionTask(task)){
			return;
		}
		
		if (this.tasks.size() == 0) {
			task.setStartTime(System.currentTimeMillis());
			this.tasks.add(task);
			return;
		}

		// ����������������� ������ ���������� ��� ������� �������� ...
		if( task.getPriority()< PRIORITY_NORMAL && tasks.size() >= this.taskQueueLimit )
		{
			logger.warn( MessageFormat.format(
					" background task skipped -> may be queue limit {0} is too slow (?) \n\t  task skipped {1}",
					this.taskQueueLimit, task
					));
			return;
		}

		final Task existTask = searchTask(task);
		if  (existTask != null) { // task already present ...

			// copy listeners from task to existsent atask ...
			for (EventListener ll: task.getListEventListeners() ) {
				existTask.addIEventListener( ll);
			}

			if (existTask.getPriority() >= task.getPriority())
				// priority of queued material is large enought  ...
				return;

			// increasing priority of existing task  ...
			existTask.setPriority( task.getPriority());
			this.tasks.remove( existTask);
			task = existTask;
		}
		insertTaskByPriority(task);

		logger.info( MessageFormat.format("Queue tasks counter is ''{0}''", tasks.size()));
	}

	/**
	 * ����� ������, ��������� ���� �� ����� ������ � �������
	 * @param task ������ ������� ����� ��������� �� ������������ 
	 * @return � ������ ���������� ������� ������ � ������� ���������� null, 
	 * ����� ���������� ������, ������� ����� � ������� 
	 */
	private Task searchTask( Task task ) {
		final ObjectId taskMateriald = task.getMaterial().getCardId();
		for (Task atask: this.tasks) {
			final ObjectId id = atask.getMaterial().getCardId();
			if ( taskMateriald.equals(id)) 
				return atask; // FOUND
		}
		return null; // NOT FOUND
	}

	/**
	 * ������� ������ �������� � ���������� ...
	 * @param task -- ����� ������ ������� ������ �� �����������
	 * @return ���������� � ����� ��������� � �������
	 */
	private int insertTaskByPriority(Task task) {
		int index = 0;
		for (int j = tasks.size()-1; j >= 0; j--) {
			if (task.getPriority() > this.tasks.get(j).getPriority()) {
				index = j + 1;
				break;
			}
		}
		task.setStartTime(System.currentTimeMillis());
		this.tasks.add(index, task);
		return index;
	}

	/** 
	 * ������ �������� �����������
	 */
	public void startThreadConverter() {
		service.execute(runn);
	}

	/** 
	 * ����� ��������� ������ �� �����������
	 * � ���������� ����������� �������
	 * @return ���������� ������ ��� ����������� Task
	 */
	private synchronized Task takeTask()  {
		int lowCount = 0;
		for (Task t : tasks) {
			if (t.getPriority() < PRIORITY_NORMAL)
				lowCount++;
			else
				break;
		}

		//����� ������ ����������������� (����� ������ � �������) ��������� �� �������� (����� ����� 0.2 �� �������� ����������, �.�. 50*0,2=10)
		//����������������� �� �������� ��������� �� ������ (����� ����� 0.08 �� �������� ����������, �.�. 50*0,08=4)
		//������ ��� ��������� ����������������� ��������� �� ����-���� (1+)
		double maxPriorInc = PRIORITY_NORMAL*0.2;
		double midPriorInc = PRIORITY_NORMAL*0.08;
		
		//x^y     = 10
		//x^(y/2) = 4
		double x = Math.pow(midPriorInc,2)/Math.pow(maxPriorInc,2);
		double y = Math.log(maxPriorInc) / Math.log(Math.pow(midPriorInc,2) / Math.pow(maxPriorInc,2));
		for (int i=0; i<lowCount; i++) {
			double curPow = ((i+1)/(double)lowCount)*y;
			double priorIncrement = Math.pow(x, curPow);
			double curPrior = tasks.get(i).getPriority();
			double newPrior = curPrior + priorIncrement;
			tasks.get(i).setPriority(newPrior);
		}
		
		return tasks.remove(tasks.size()-1);
	}

	public boolean increasePriority(Task task, int weight) {
		double p = task.getPriority();
		if(p >= PRIORITY_MAX){
			return false;
		}
		p += weight;
		if(p >PRIORITY_MAX) p = PRIORITY_MAX;
		task.setPriority( p);
		return true;
	}
	
	//-----------------------MBean operations---------------------------\\
	
	@ManagedAttribute(description="The size of queue for pending tasks")
	public int getQueueSize() {
		return tasks.size();
	}
	
	@ManagedAttribute(description="The limit of queue for pending tasks")
	public int getQueueLimit() {
		return taskQueueLimit;
	}
	
	@ManagedAttribute(description="Set the limit of queue for pending tasks")
	public void setQueueLimit(int l) {
		this.taskQueueLimit = l;
	}
	
	@ManagedAttribute(description="The reaction time (timer tick)")
	public long getReactionTime() {
		return reactionTime;
	}
	
	@ManagedAttribute(description="Set the reaction time (timer tick)")
	public void setReactionTime(long t) {
		this.reactionTime = t;
	}
	
	@ManagedAttribute(description="The size of queue for active tasks")
	public int getActiveQueueSize() {
		return activeTaskQueueController.getActiveTasksSize();
	}
	
	@ManagedAttribute(description="The limit of queue for active tasks ")
	public long getActiveQueueLimit() {
		return activeTaskQueueController.getActiveTasksLimit();
	}
	
	@ManagedAttribute(description="Set the limit of queue for active tasks ")
	public void setActiveQueueLimit(long l) {
		activeTaskQueueController.setActiveTasksLimit(l);
	}
	
	@ManagedAttribute(description="The size of executors (ThreadPoolExecutor) queue. There is a count of the same runnable objects in this.")
	public int getExecutorQueueSize() {
		return ((ThreadPoolExecutor)service).getQueue().size();
	}
	
	@ManagedOperation(description="Active tasks and their work time")
	@ManagedOperationParameters({
	    @ManagedOperationParameter(name = "empty param", description = "don't set this param, just exec method (hack for spring JMX Exporter)")})
	public String getActiveTasksWorkTime(Object o) {
		return activeTaskQueueController.getActiveTasksWorkTime();
	}
	
	@ManagedOperation(description="Pending tasks and their waiting time")
	@ManagedOperationParameters({
	    @ManagedOperationParameter(name = "empty param", description = "don't set this param, just exec method (hack for spring JMX Exporter)")})
	public String getPendingTasksWaitingTime(Object o) {
		if (tasks.isEmpty()) {
			return "";
		}
		StringBuilder res = new StringBuilder();
		res.append("index\t|\tcard id\t\t|\tprior\t|\twait time (ms)\t|\tfilename\n");
        res.append("-----------------------------------------------------------------\n");
		Iterator<Task> iterator = tasks.iterator();
		int index = 0;
		while(iterator.hasNext()){
			Task task = iterator.next();
			res.append(index++).append("\t|\t");
			res.append(task.getMaterial().getCardId().getId()).append("\t|\t");
			res.append(String.format("%.2f", task.getPriority())).append("\t|\t");
			res.append(task.getWaitingTime()).append("\t|\t");
			res.append(task.getMaterial().getName());
			res.append("\n");
		}
		return res.toString();
	}
}
