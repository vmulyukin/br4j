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
package com.aplana.dmsi.expansion;

import java.util.Collections;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.TypeStandard;
import com.aplana.dbmi.service.client.DataServiceFacade;
import com.aplana.dmsi.DMSIException;
import com.aplana.dmsi.types.ExpansionType;
import com.aplana.dmsi.types.Header;

public class ExpansionManager {

	private Map<ExpansionDescription, SpecializedExpansionAdapter> adapters = Collections.emptyMap();
	private Map<ExpansionDescription, ExpansionProcessor> processors = Collections.emptyMap();
	private Map<TypeStandard, ExpansionDescription> defaults = Collections.emptyMap();
	private Log logger = LogFactory.getLog(getClass());

	public SpecializedExpansionAdapter getAdapter(ExpansionType expansion) {
		if (expansion == null || expansion.getOrganization() == null && expansion.getExpVer() == null) {
			return new StubExpansionAdapter();
		}
		String standardAuthor = expansion.getOrganization();
		String standardVersion = expansion.getExpVer();

		ExpansionDescription descr = createExpansionDescription(standardAuthor, standardVersion);
		if (adapters.containsKey(descr)) {
			return adapters.get(descr);
		}
		ExpansionDescription authorDescr = createExpansionDescription(standardAuthor, null);
		if (logger.isDebugEnabled()) {
			logger.debug("There is no adapter for " + descr + ". Trying to find one for " + authorDescr);
		}
		if (adapters.containsKey(authorDescr)) {
			return adapters.get(authorDescr);
		}

		throw new UnsupportedOperationException("There is no handler nor for " + descr + " nor for " + authorDescr);

	}

	public ExpansionProcessor getProcessor(ExpansionType expansion) {
		if (expansion == null || expansion.getOrganization() == null && expansion.getExpVer() == null) {
			return new StubExpansionProcessor();
		}
		String standardAuthor = expansion.getOrganization();
		String standardVersion = expansion.getExpVer();

		ExpansionDescription descr = createExpansionDescription(standardAuthor, standardVersion);
		ExpansionDescription authorDescr = createExpansionDescription(standardAuthor, null);
		return getProcessor(descr, authorDescr);
	}

	public ExpansionProcessor getProcessor(TypeStandard standard) {
		if (!defaults.containsKey(standard)) {
			throw new UnsupportedOperationException("There is no processor for " + standard);
		}
		return getProcessor(defaults.get(standard), null);
	}

	private ExpansionProcessor getProcessor(ExpansionDescription descr, ExpansionDescription alternative)
			throws UnsupportedOperationException {
		if (processors.containsKey(descr)) {
			return processors.get(descr);
		}
		if (alternative != null) {
			if (logger.isDebugEnabled()) {
				logger.debug("There is no processor for " + descr + ". Trying to find one for " + alternative);
			}
			if (processors.containsKey(alternative)) {
				return processors.get(alternative);
			}
		}
		throw new UnsupportedOperationException("There is no processor nor for " + descr + " nor for " + alternative);
	}

	private ExpansionDescription createExpansionDescription(String author, String version) {
		if (author == null) {
			author = "";
		}
		if (version == null) {
			version = "";
		}

		return new ExpansionDescription(author, version);
	}

	public Map<ExpansionDescription, SpecializedExpansionAdapter> getAdapters() {
		return Collections.unmodifiableMap(this.adapters);
	}

	public void setAdapters(Map<ExpansionDescription, SpecializedExpansionAdapter> adapters) {
		if (adapters != null) {
			this.adapters = adapters;
		}
	}

	public Map<ExpansionDescription, ExpansionProcessor> getProcessors() {
		return Collections.unmodifiableMap(this.processors);
	}

	public void setProcessors(Map<ExpansionDescription, ExpansionProcessor> processors) {
		if (processors != null) {
			this.processors = processors;
		}
	}

	public Map<TypeStandard, ExpansionDescription> getDefaults() {
		return Collections.unmodifiableMap(this.defaults);
	}

	public void setDefaults(Map<TypeStandard, ExpansionDescription> defaults) {
		if (defaults != null) {
			this.defaults = defaults;
		}
	}

	private static final class StubExpansionAdapter implements SpecializedExpansionAdapter {
		public StubExpansionAdapter() {
		}

		public ExpansionType createCommon(ExpansionType specializedExpansion) {
			return specializedExpansion;
		}

		public ExpansionType createSpecialized(ExpansionType commonExpansion) {
			return commonExpansion;
		}
	}

	private static final class StubExpansionProcessor implements ExpansionProcessor {

		public StubExpansionProcessor() {
		}

		public void importPreProcess(DataServiceFacade service, Header header) throws DMSIException {
		}

		public void importPostProcess(DataServiceFacade service, Header header) throws DMSIException {
		}

		public void fillExpansion(DataServiceFacade service, Header header, ObjectId cardId) throws DMSIException {
		}

	}

	public static class ExpansionDescription {

		private String author;
		private String version;

		public ExpansionDescription(String author, String version) {
			this.author = author;
			this.version = version;
		}

		public String getAuthor() {
			return this.author;
		}

		public String getVersion() {
			return this.version;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((this.author == null) ? 0 : this.author.hashCode());
			result = prime * result + ((this.version == null) ? 0 : this.version.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ExpansionDescription other = (ExpansionDescription) obj;
			if (this.author == null) {
				if (other.author != null)
					return false;
			} else if (!this.author.equals(other.author))
				return false;
			if (this.version == null) {
				if (other.version != null)
					return false;
			} else if (!this.version.equals(other.version))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "[" + author + "][" + version + "]";
		}
	}

}
