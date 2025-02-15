/*
 * The contents of this file are subject to the Mozilla Public License
 * Version 1.1 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations under
 * the License.
 *
 * The Original Code is OpenELIS code.
 *
 * Copyright (C) ITECH, University of Washington, Seattle WA.  All Rights Reserved.
 */
package org.openelisglobal.reports.action.implementation;

import java.util.ArrayList;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.validator.GenericValidator;
import org.openelisglobal.common.form.BaseForm;

public class ReportSpecificationParameters implements IReportParameterSetter {
    public enum Parameter {
        NO_SPECIFICATION, DATE_RANGE, ACCESSION_RANGE
    }

    private String reportTitle;
    private String instructions;
    private ArrayList<Parameter> parameters = new ArrayList<Parameter>();

    /**
     * Constructor for a single parameter.
     * 
     * @param parameter    The parameter which will appear on the parameter page
     * @param title        The title for the page, it will appear above the
     *                     parameters
     * @param instructions The instructions for the user on how to fill in the
     *                     parameters
     */
    public ReportSpecificationParameters(Parameter parameter, String title, String instructions) {
        parameters.add(parameter);
        reportTitle = title;
        this.instructions = instructions;
    }

    public ReportSpecificationParameters(Parameter[] parameters, String title, String instructions) {
        reportTitle = title;
        this.instructions = instructions;

        for (Parameter newParameter : parameters) {
            this.parameters.add(newParameter);
        }

    }

    @Override
    public void setRequestParameters(BaseForm form) {
        try {
            PropertyUtils.setProperty(form, "reportName", reportTitle);
            if (!GenericValidator.isBlankOrNull(instructions)) {
                PropertyUtils.setProperty(form, "instructions", instructions);
            }
            PropertyUtils.setProperty(form, "reportName", reportTitle);
            for (Parameter parameter : parameters) {
                switch (parameter) {
                case DATE_RANGE: {
                    PropertyUtils.setProperty(form, "useLowerDateRange", true);
                    PropertyUtils.setProperty(form, "useUpperDateRange", true);
                    break;
                }
                case ACCESSION_RANGE: {
                    PropertyUtils.setProperty(form, "useAccessionDirect", true);
                    PropertyUtils.setProperty(form, "useHighAccessionDirect", true);
                    break;
                }
                case NO_SPECIFICATION: {
                    PropertyUtils.setProperty(form, "noRequestSpecifications", true);
                    break;
                }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
