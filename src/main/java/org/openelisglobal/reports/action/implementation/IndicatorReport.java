/**
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
* Copyright (C) CIRG, University of Washington, Seattle WA.  All Rights Reserved.
*
*/
package org.openelisglobal.reports.action.implementation;

import java.sql.Date;

import org.apache.commons.validator.GenericValidator;
import org.openelisglobal.common.exception.LIMSRuntimeException;
import org.openelisglobal.common.form.BaseForm;
import org.openelisglobal.common.util.ConfigurationProperties;
import org.openelisglobal.common.util.ConfigurationProperties.Property;
import org.openelisglobal.common.util.DateUtil;
import org.openelisglobal.internationalization.MessageUtil;
import org.openelisglobal.reports.action.implementation.reportBeans.ErrorMessages;

public abstract class IndicatorReport extends Report {

    protected String lowerDateRange;
    protected String upperDateRange;
    protected Date lowDate;
    protected Date highDate;

    public void setRequestParameters(BaseForm form) {
        new ReportSpecificationParameters(ReportSpecificationParameters.Parameter.DATE_RANGE, getNameForReportRequest(),
                null).setRequestParameters(form);
    }

    protected void createReportParameters() {
        super.createReportParameters();

        reportParameters.put("startDate", lowerDateRange);
        reportParameters.put("stopDate", upperDateRange);
        reportParameters.put("siteId", ConfigurationProperties.getInstance().getPropertyValue(Property.SiteCode));
        reportParameters.put("directorName",
                ConfigurationProperties.getInstance().getPropertyValue(Property.labDirectorName));
        reportParameters.put("labName1", getLabNameLine1());
        reportParameters.put("labName2", getLabNameLine2());
        reportParameters.put("reportTitle", getNameForReport());
        if (ConfigurationProperties.getInstance().isPropertyValueEqual(Property.configurationName, "CI LNSP")) {
            reportParameters.put("headerName", "CILNSPHeader.jasper");
        } else {
            reportParameters.put("headerName", "GeneralHeader.jasper");
        }
    }

    protected void setDateRange(BaseForm form) {
        errorFound = false;
        lowerDateRange = form.getString("lowerDateRange");
        upperDateRange = form.getString("upperDateRange");

        if (GenericValidator.isBlankOrNull(lowerDateRange)) {
            errorFound = true;
            ErrorMessages msgs = new ErrorMessages();
            msgs.setMsgLine1(MessageUtil.getMessage("report.error.message.noPrintableItems"));
            errorMsgs.add(msgs);
        }

        if (GenericValidator.isBlankOrNull(upperDateRange)) {
            upperDateRange = lowerDateRange;
        }

        try {
            lowDate = DateUtil.convertStringDateToSqlDate(lowerDateRange);
            highDate = DateUtil.convertStringDateToSqlDate(upperDateRange);
        } catch (LIMSRuntimeException re) {
            errorFound = true;
            ErrorMessages msgs = new ErrorMessages();
            msgs.setMsgLine1(MessageUtil.getMessage("report.error.message.date.format"));
            errorMsgs.add(msgs);
        }
    }

    abstract protected String getNameForReportRequest();

    abstract protected String getNameForReport();

    abstract protected String getLabNameLine1();

    abstract protected String getLabNameLine2();

}
