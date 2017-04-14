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
package com.aplana.dbmi.module.docflow;

import com.aplana.dbmi.action.ChangeState;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.Parametrized;
import com.aplana.dbmi.service.impl.ProcessorBase;

public class PlainTrigger extends ProcessorBase implements Parametrized {
	private static final long serialVersionUID = 1L;

	public static final String PARAM_PROCESSOR_BEAN = "processorBean";

	private DocumentProcessor processor;
	
	public void setParameter(String name, String value) {
		if (PARAM_PROCESSOR_BEAN.equalsIgnoreCase(name))
			processor = (DocumentProcessor) getBeanFactory().getBean(value);
		else
			throw new IllegalArgumentException("Unknown parameter: " + name);
	}

	public Object process() throws DataException {
		if (processor == null)
			throw new IllegalStateException("Processor bean must be set");
		
		ChangeState move = (ChangeState) getAction();
		logger.info("[PlainTrigger] Triggered for document " + move.getCard().getId().getId());

		processor.setDocumentId(move.getCard().getId());
		processor.process();
		return getResult();
	}
}
