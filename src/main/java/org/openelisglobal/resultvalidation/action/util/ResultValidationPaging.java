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
package org.openelisglobal.resultvalidation.action.util;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.beanutils.PropertyUtils;
import org.openelisglobal.common.action.IActionConstants;
import org.openelisglobal.common.form.BaseForm;
import org.openelisglobal.common.paging.IPageDivider;
import org.openelisglobal.common.paging.IPageFlattener;
import org.openelisglobal.common.paging.IPageUpdater;
import org.openelisglobal.common.paging.PagingBean;
import org.openelisglobal.common.paging.PagingUtility;
import org.openelisglobal.common.util.IdValuePair;
import org.openelisglobal.resultvalidation.bean.AnalysisItem;

public class ResultValidationPaging {
    public static final int VALIDATION_PAGING_SIZE = 240;
    private PagingUtility<List<AnalysisItem>> paging = new PagingUtility<>();
    private static AnalysisItemPageHelper pagingHelper = new AnalysisItemPageHelper();

    public void setDatabaseResults(HttpServletRequest request, BaseForm form, List<AnalysisItem> analysisItems)
            throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {

        paging.setDatabaseResults(request.getSession(), analysisItems, pagingHelper);

        List<AnalysisItem> resultPage = paging.getPage(1, request.getSession());

        if (resultPage != null) {
            PropertyUtils.setProperty(form, "resultList", resultPage);
            PropertyUtils.setProperty(form, "paging", paging.getPagingBeanWithSearchMapping(1, request.getSession()));
        }
    }

    public void page(HttpServletRequest request, BaseForm form, String newPage)
            throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {

        request.getSession().setAttribute(IActionConstants.SAVE_DISABLED, IActionConstants.FALSE);
        List<AnalysisItem> clientAnalysis = (List<AnalysisItem>) form.get("resultList");
        PagingBean bean = (PagingBean) form.get("paging");
        String testSectionId = (request.getParameter("testSectionId"));

        paging.updatePagedResults(request.getSession(), clientAnalysis, bean, pagingHelper);

        int page = Integer.parseInt(newPage);

        List<AnalysisItem> resultPage = paging.getPage(page, request.getSession());
        if (resultPage != null) {
            PropertyUtils.setProperty(form, "resultList", resultPage);
            PropertyUtils.setProperty(form, "testSectionId", testSectionId);
            PropertyUtils.setProperty(form, "paging",
                    paging.getPagingBeanWithSearchMapping(page, request.getSession()));
        }
    }

    public void updatePagedResults(HttpServletRequest request, BaseForm form) {
        List<AnalysisItem> clientAnalysis = (List<AnalysisItem>) form.get("resultList");
        PagingBean bean = (PagingBean) form.get("paging");

        paging.updatePagedResults(request.getSession(), clientAnalysis, bean, pagingHelper);
    }

    public List<AnalysisItem> getResults(HttpServletRequest request) {
        return paging.getAllResults(request.getSession(), pagingHelper);
    }

    private static class AnalysisItemPageHelper implements IPageDivider<List<AnalysisItem>>,
            IPageUpdater<List<AnalysisItem>>, IPageFlattener<List<AnalysisItem>> {

        @Override
        public void createPages(List<AnalysisItem> analysisList, List<List<AnalysisItem>> pagedResults) {
            List<AnalysisItem> page = new ArrayList<>();

            String currentAccessionNumber = null;
            int resultCount = 0;

            for (AnalysisItem item : analysisList) {
                if (currentAccessionNumber != null && !currentAccessionNumber.equals(item.getAccessionNumber())) {
                    resultCount = 0;
                    currentAccessionNumber = null;
                    pagedResults.add(page);
                    page = new ArrayList<>();
                }
                if (resultCount >= VALIDATION_PAGING_SIZE) {
                    currentAccessionNumber = item.getAccessionNumber();
                }

                page.add(item);
                resultCount++;
            }

            if (!page.isEmpty() || pagedResults.isEmpty()) {
                pagedResults.add(page);
            }
        }

        @Override
        public void updateCache(List<AnalysisItem> cacheItems, List<AnalysisItem> clientItems) {
            for (int i = 0; i < clientItems.size(); i++) {
                cacheItems.set(i, clientItems.get(i));
            }

        }

        @Override
        public List<AnalysisItem> flattenPages(List<List<AnalysisItem>> pages) {

            List<AnalysisItem> allResults = new ArrayList<>();

            for (List<AnalysisItem> page : pages) {
                for (AnalysisItem item : page) {
                    allResults.add(item);
                }
            }

            return allResults;

        }

        @Override
        public List<IdValuePair> createSearchToPageMapping(List<List<AnalysisItem>> allPages) {
            List<IdValuePair> mappingList = new ArrayList<>();

            int page = 0;
            for (List<AnalysisItem> analysisList : allPages) {
                page++;
                String pageString = String.valueOf(page);

                String currentAccession = null;

                for (AnalysisItem analysisItem : analysisList) {
                    if (!analysisItem.getAccessionNumber().equals(currentAccession)) {
                        currentAccession = analysisItem.getAccessionNumber();
                        mappingList.add(new IdValuePair(currentAccession, pageString));
                    }
                }
            }

            return mappingList;
        }
    }
}
