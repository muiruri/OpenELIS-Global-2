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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.openelisglobal.common.form.BaseForm;
import org.openelisglobal.common.services.DisplayListService;
import org.openelisglobal.internationalization.MessageUtil;
import org.openelisglobal.reports.action.implementation.reportBeans.ActivityReportBean;
import org.openelisglobal.result.service.ResultServiceImpl;
import org.openelisglobal.result.valueholder.Result;

/**
 */
public class ActivityReportByPanel extends ActivityReport implements IReportCreator, IReportParameterSetter {
    private String panelName;

    @Override
    public void setRequestParameters(BaseForm form) {
        new ReportSpecificationParameters(ReportSpecificationParameters.Parameter.DATE_RANGE,
                MessageUtil.getMessage("report.activity.report.base") + " " + MessageUtil.getMessage("report.by.panel"),
                MessageUtil.getMessage("report.instruction.all.fields")).setRequestParameters(form);
        new ReportSpecificationList(DisplayListService.getInstance().getList(DisplayListService.ListType.PANELS),
                MessageUtil.getMessage("workplan.panel.types")).setRequestParameters(form);
    }

    @Override
    protected String getActivityLabel() {
        return "Panel: " + panelName;
    }

    @Override
    protected void buildReportContent(ReportSpecificationList panelSelection) {
        panelName = panelSelection.getSelectionAsName();
        createReportParameters();

        List<Result> resultList = ResultServiceImpl.getResultsInTimePeriodInPanel(dateRange.getLowDate(),
                dateRange.getHighDate(), panelSelection.getSelection());
        ArrayList<ActivityReportBean> rawResults = new ArrayList<>(resultList.size());
        testsResults = new ArrayList<>();

        String currentAnalysisId = "-1";
        for (Result result : resultList) {
            if (result.getAnalysis() != null) {
                if (!currentAnalysisId.equals(result.getAnalysis().getId())) {
                    rawResults.add(createActivityReportBean(result, true));
                    currentAnalysisId = result.getAnalysis().getId();
                }
            }
        }

        Collections.sort(rawResults, new Comparator<ActivityReportBean>() {
            @Override
            public int compare(ActivityReportBean o1, ActivityReportBean o2) {
                return o1.getAccessionNumber().compareTo(o2.getAccessionNumber());
            }
        });

        String currentAccessionNumber = "";
        for (ActivityReportBean item : rawResults) {
            if (!currentAccessionNumber.equals(item.getAccessionNumber())) {
                testsResults.add(createIdentityActivityBean(item, false));
                currentAccessionNumber = item.getAccessionNumber();
            }
            // hokey way to remove collection date
            item.setCollectionDate(" ");
            testsResults.add(item);
        }
    }
}
