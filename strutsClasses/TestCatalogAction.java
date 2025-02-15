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

package org.openelisglobal.testconfiguration.action;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.validator.GenericValidator;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.validator.DynaValidatorForm;

import org.openelisglobal.common.action.BaseAction;
import org.openelisglobal.localization.service.LocalizationServiceImpl;
import org.openelisglobal.resultlimit.service.ResultLimitServiceImpl;
import org.openelisglobal.common.services.TestService;
import org.openelisglobal.typeoftestresult.service.TypeOfTestResultServiceImpl;
import org.openelisglobal.dictionary.dao.DictionaryDAO;
import org.openelisglobal.dictionary.daoimpl.DictionaryDAOImpl;
import org.openelisglobal.dictionary.valueholder.Dictionary;
import org.openelisglobal.panel.valueholder.Panel;
import org.openelisglobal.resultlimits.valueholder.ResultLimit;
import org.openelisglobal.test.daoimpl.TestDAOImpl;
import org.openelisglobal.test.valueholder.Test;
import org.openelisglobal.testconfiguration.beans.ResultLimitBean;
import org.openelisglobal.testconfiguration.beans.TestCatalogBean;
import org.openelisglobal.testresult.valueholder.TestResult;
import org.openelisglobal.typeofsample.valueholder.TypeOfSample;

public class TestCatalogAction extends BaseAction {
    private DictionaryDAO dictionaryDAO = new DictionaryDAOImpl();


    @Override
    protected ActionForward performAction(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        DynaValidatorForm dynaForm = (DynaValidatorForm)form;
        List<TestCatalogBean> testList = createTestList();
        PropertyUtils.setProperty(dynaForm, "testList", testList);

        List<String> testSectionList = new ArrayList<String>();
        for( TestCatalogBean catalogBean : testList){
            if( !testSectionList.contains( catalogBean.getTestUnit())){
                testSectionList.add(catalogBean.getTestUnit());
            }
        }
        PropertyUtils.setProperty(dynaForm, "testSectionList", testSectionList);



        return mapping.findForward(FWD_SUCCESS);
    }

    private List<TestCatalogBean> createTestList() {
        List<TestCatalogBean> beanList = new ArrayList<TestCatalogBean>();

        List<Test> testList = new TestDAOImpl().getAllTests(false);

        for( Test test : testList){

            TestCatalogBean bean = new TestCatalogBean();
            TestService testService = new TestService(test);
            String resultType = testService.getResultType();
            bean.setId(test.getId());
            bean.setEnglishName(test.getLocalizedTestName().getEnglish());
            bean.setFrenchName(test.getLocalizedTestName().getFrench());
            bean.setEnglishReportName(test.getLocalizedReportingName().getEnglish());
            bean.setFrenchReportName(test.getLocalizedReportingName().getFrench());
            bean.setTestSortOrder(Integer.parseInt(test.getSortOrder()));
            bean.setTestUnit(testService.getTestSectionName());
            bean.setPanel(createPanelList(testService));
            bean.setResultType(resultType);
            TypeOfSample typeOfSample = testService.getTypeOfSample();
            bean.setSampleType(typeOfSample != null ? typeOfSample.getLocalizedName() : "n/a");
            bean.setOrderable(test.getOrderable() ? "Orderable" : "Not orderable");
            bean.setLoinc(test.getLoinc());
            bean.setActive(test.isActive() ? "Active" : "Not active");
            bean.setUom(testService.getUOM(false));
            if( TypeOfTestResultServiceImpl.ResultType.NUMERIC.matches(resultType)) {
                bean.setSignificantDigits(testService.getPossibleTestResults().get(0).getSignificantDigits());
                bean.setHasLimitValues(true);
                bean.setResultLimits(getResultLimits(test, bean.getSignificantDigits()));
            }
            bean.setHasDictionaryValues(TypeOfTestResultServiceImpl.ResultType.isDictionaryVariant(bean.getResultType()));
            if( bean.isHasDictionaryValues()){
                bean.setDictionaryValues(createDictionaryValues(testService));
                bean.setReferenceValue(createReferenceValueForDictionaryType(test));
            }
            beanList.add(bean);
        }

        Collections.sort(beanList, new Comparator<TestCatalogBean>() {
            @Override
            public int compare(TestCatalogBean o1, TestCatalogBean o2) {
                //sort by test section, sample type, panel, sort order
                int comparison = o1.getTestUnit().compareTo(o2.getTestUnit());
                if (comparison != 0) {
                    return comparison;
                }

                comparison = o1.getSampleType().compareTo(o2.getSampleType());
                if (comparison != 0) {
                    return comparison;
                }

                comparison = o1.getPanel().compareTo(o2.getPanel());
                if (comparison != 0) {
                    return comparison;
                }

                return o1.getTestSortOrder() - o2.getTestSortOrder();
            }
        });

        return beanList;
    }

    private List<ResultLimitBean> getResultLimits(Test test, String significantDigits) {
        List<ResultLimitBean> limitBeans = new ArrayList<ResultLimitBean>();

        List<ResultLimit> resultLimitList = SpringContext.getBean(ResultLimitService.class).getResultLimits(test);

        Collections.sort(resultLimitList, new Comparator<ResultLimit>() {
            @Override
            public int compare(ResultLimit o1, ResultLimit o2) {
                return (int) (o1.getMinAge() - o2.getMinAge());
            }
        });

        for( ResultLimit limit : resultLimitList){
            ResultLimitBean bean = new ResultLimitBean();
            bean.setNormalRange(SpringContext.getBean(ResultLimitService.class).getDisplayReferenceRange(limit, significantDigits, "-"));
            bean.setValidRange(SpringContext.getBean(ResultLimitService.class).getDisplayValidRange(limit, significantDigits, "-"));
            bean.setGender(limit.getGender());
            bean.setAgeRange( SpringContext.getBean(ResultLimitService.class).getDisplayAgeRange(limit, "-"));
            limitBeans.add(bean);
        }
        return limitBeans;
    }

    private String createReferenceValueForDictionaryType(Test test) {
        List<ResultLimit> resultLimits = SpringContext.getBean(ResultLimitService.class).getResultLimits(test);

        if( resultLimits.isEmpty() ){
            return "n/a";
        }

        return SpringContext.getBean(ResultLimitService.class).getDisplayReferenceRange(resultLimits.get(0),null, null);

    }

    private List<String> createDictionaryValues(TestService testService) {
        List<String> dictionaryList = new ArrayList<String>();
        List<TestResult> testResultList = testService.getPossibleTestResults();
        for( TestResult testResult : testResultList){
            CollectionUtils.addIgnoreNull(dictionaryList, getDictionaryValue(testResult));
        }

        return dictionaryList;
    }

    private String getDictionaryValue(TestResult testResult) {

        if (TypeOfTestResultServiceImpl.ResultType.isDictionaryVariant(testResult.getTestResultType())) {
            Dictionary dictionary = dictionaryDAO.getDataForId(testResult.getValue());
            String displayValue = dictionary.getLocalizedName();

            if ("unknown".equals(displayValue)) {
                displayValue = !GenericValidator.isBlankOrNull(dictionary.getDictEntry()) ?
                        dictionary.getDictEntry() : dictionary.getLocalAbbreviation();
            }

            if (testResult.getIsQuantifiable()) {
                displayValue += " Qualifiable";
            }
            return displayValue;
        }

        return null;
    }

    private String createPanelList(TestService testService) {
        StringBuilder builder = new StringBuilder();

        List<Panel> panelList = testService.getPanels();
        for(Panel panel : panelList){
            builder.append(LocalizationServiceImpl.getLocalizedValueById(panel.getLocalization().getId()));
            builder.append(", ");
        }

        String panelString = builder.toString();
        if( panelString.isEmpty()){
            panelString = "None";
        }else{
            panelString = panelString.substring(0, panelString.length() - 2 );
        }

        return panelString;
    }


    @Override
    protected String getPageTitleKey() {
        return null;
    }

    @Override
    protected String getPageSubtitleKey() {
        return null;
    }
}
