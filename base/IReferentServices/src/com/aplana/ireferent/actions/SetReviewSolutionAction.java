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
package com.aplana.ireferent.actions;

import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.ireferent.IReferentException;
import com.aplana.ireferent.config.ConfigurationException;
import com.aplana.ireferent.types.WSOApprovalReviewSolution;
import com.aplana.ireferent.types.WSObject;

public class SetReviewSolutionAction extends ChangeCardAction {
    private static final String SOLUTION_VALUE_PARAM = "solution";
    private static final String IS_SOLUTION_MANDATORY_PARAM = "solutionMandatory";
    private static final String DEFAULT_SOLUTION_VALUE = "defaultValue";

    private Object solutionValue;
    private boolean isSolutionMandatory;
    private Object defaultValue;

    @Override
    public void setParameter(String key, Object value) {
	if (SOLUTION_VALUE_PARAM.equals(key)) {
	    solutionValue = value;
	} else if (IS_SOLUTION_MANDATORY_PARAM.equals(key)) {
	    isSolutionMandatory = Boolean.parseBoolean((String) value);
	} else if (DEFAULT_SOLUTION_VALUE.equals(key)) {
		defaultValue = value;
	} else {
	    super.setParameter(key, value);
	}
    }

    @Override
    public void doAction(DataServiceBean serviceBean, WSObject object)
	    throws IReferentException {
	final String solution = getSolutionValue();
	final boolean isSolutionDefined = !"".equals(solution);
	if (!isSolutionDefined) {
	    if (isSolutionMandatory) {
		throw new IReferentException("Solution is empty");
	    }
	    return;
	}
	super.doAction(serviceBean, object);
    }

    @Override
    protected WSObject createObject() {
	WSObject reviewObject = super.createObject();
	if (!(reviewObject instanceof Reviewed)) {
	    throw new ConfigurationException("Review type should implement "
		    + Reviewed.class.getName());
	}

	Reviewed review = (Reviewed) reviewObject;
	WSOApprovalReviewSolution reviewSolution = createSolution();
	review.setSolution(reviewSolution);
	return reviewObject;
    }

    private WSOApprovalReviewSolution createSolution() {
	WSOApprovalReviewSolution reviewSolution = new WSOApprovalReviewSolution();
	reviewSolution.setText(getSolutionValue());
	return reviewSolution;
    }

    @Override
    protected boolean isConstructNewObject() {
	return true;
    }

    private String getSolutionValue() {
    	if ("".equals(solutionValue) || solutionValue == null)
    		solutionValue = defaultValue;
	return solutionValue == null ? "" : solutionValue.toString().trim();
    }
}
