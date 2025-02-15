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
import java.util.LinkedHashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.validator.DynaValidatorForm;

import org.openelisglobal.common.action.BaseAction;
import org.openelisglobal.common.services.DisplayListService;
import org.openelisglobal.test.service.TestSectionServiceImpl;
import org.openelisglobal.common.services.TestService;
import org.openelisglobal.common.util.IdValuePair;
import org.openelisglobal.test.valueholder.Test;

public class TestSectionTestAssignAction extends BaseAction {
    @Override
    protected ActionForward performAction(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        ((DynaValidatorForm)form).initialize(mapping);
        List<IdValuePair> testSections = DisplayListService.getInstance().getListWithLeadingBlank(DisplayListService.ListType.TEST_SECTION);
        LinkedHashMap<IdValuePair, List<IdValuePair>> testSectionTestsMap = new LinkedHashMap<IdValuePair, List<IdValuePair>>(testSections.size());

        for( IdValuePair sectionPair : testSections){
            List<IdValuePair> tests = new ArrayList<IdValuePair>();
            testSectionTestsMap.put(sectionPair, tests );
            List<Test> testList = TestSectionServiceImpl.getTestsInSection(sectionPair.getId());

            for( Test test : testList){
                if( test.isActive()) {
                    tests.add(new IdValuePair(test.getId(), TestServiceImpl.getLocalizedTestNameWithType(test)));
                }
            }
        }

        //we can't just append the original list because that list is in the cache
        List<IdValuePair> joinedList = new ArrayList<IdValuePair>(testSections);
        joinedList.addAll(DisplayListService.getInstance().getList(DisplayListService.ListType.TEST_SECTION_INACTIVE));
        PropertyUtils.setProperty(form, "testSectionList", joinedList);
        PropertyUtils.setProperty(form, "sectionTestList", testSectionTestsMap);

        return mapping.findForward(FWD_SUCCESS);
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
