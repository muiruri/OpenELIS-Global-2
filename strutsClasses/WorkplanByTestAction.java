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
 * Copyright (C) The Minnesota Department of Health.  All Rights Reserved.
 *
 * Contributor(s): CIRG, University of Washington, Seattle WA.
 */
package org.openelisglobal.workplan.action;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.validator.GenericValidator;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import org.openelisglobal.analysis.dao.AnalysisDAO;
import org.openelisglobal.analysis.daoimpl.AnalysisDAOImpl;
import org.openelisglobal.analysis.valueholder.Analysis;
import org.openelisglobal.common.action.BaseActionForm;
import org.openelisglobal.common.formfields.FormFields;
import org.openelisglobal.common.formfields.FormFields.Field;
import org.openelisglobal.common.services.DisplayListService;
import org.openelisglobal.common.services.ObservationHistoryService;
import org.openelisglobal.common.services.ObservationHistoryService.ObservationType;
import org.openelisglobal.common.services.QAService;
import org.openelisglobal.common.services.QAService.QAObservationType;
import org.openelisglobal.common.services.TestService;
import org.openelisglobal.common.util.ConfigurationProperties;
import org.openelisglobal.common.util.ConfigurationProperties.Property;
import org.openelisglobal.common.util.IdValuePair;
import org.openelisglobal.common.util.StringUtil;
import org.openelisglobal.sample.valueholder.Sample;
import org.openelisglobal.sampleqaevent.dao.SampleQaEventDAO;
import org.openelisglobal.sampleqaevent.daoimpl.SampleQaEventDAOImpl;
import org.openelisglobal.sampleqaevent.valueholder.SampleQaEvent;
import org.openelisglobal.test.beanItems.TestResultItem;
//import org.openelisglobal.test.dao.TestSectionDAO;
//import org.openelisglobal.test.daoimpl.TestSectionDAOImpl;
//import org.openelisglobal.test.valueholder.TestSection;

public class WorkplanByTestAction extends BaseWorkplanAction {

	private final AnalysisDAO analysisDAO = new AnalysisDAOImpl();
	private static boolean HAS_NFS_PANEL = false;

	String testType = "";
	String testName = "";
	

	static {
		HAS_NFS_PANEL = ConfigurationProperties.getInstance().isPropertyValueEqual(Property.CONDENSE_NFS_PANEL, "true");
	}

	@Override
	protected ActionForward performAction(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response)
			throws Exception {

		BaseActionForm dynaForm = (BaseActionForm) form;

		request.getSession().setAttribute(SAVE_DISABLED, "true");

		// Initialize the form.
		dynaForm.initialize(mapping);
		String workplan = (request.getParameter("type"));
		setRequestType(workplan);

		if (HAS_NFS_PANEL) {
			setNFSTestIdList();
		}

		List<TestResultItem> workplanTests;

		testType = request.getParameter("selectedSearchID");

		if (!GenericValidator.isBlankOrNull(testType)) {

			if (testType.equals("NFS")) {
				testName = "NFS";
				workplanTests = getWorkplanForNFSTest();
			} else {
				testName = getTestName(testType);
				workplanTests = getWorkplanByTest(testType);
			}

			resultsLoadUtility.sortByAccessionAndSequence(workplanTests);
			PropertyUtils.setProperty(dynaForm, "testTypeID", testType);
			PropertyUtils.setProperty(dynaForm, "testName", testName);
			PropertyUtils.setProperty(dynaForm, "workplanTests", workplanTests);
			PropertyUtils.setProperty(dynaForm, "searchFinished", Boolean.TRUE);

		} else {
			// no search done, set workplanTests as empty
			PropertyUtils.setProperty(dynaForm, "searchFinished", Boolean.FALSE);
			PropertyUtils.setProperty(dynaForm, "testName", null);
			PropertyUtils.setProperty(dynaForm, "workplanTests", new ArrayList<TestResultItem>());
		}



		PropertyUtils.setProperty(dynaForm, "searchTypes", getTestDropdownList());
		PropertyUtils.setProperty(dynaForm, "workplanType", request.getParameter("type"));
		PropertyUtils.setProperty(dynaForm, "searchLabel", StringUtil.getMessageForKey("workplan.test.types"));
		PropertyUtils.setProperty(dynaForm, "searchAction", "WorkPlanByTest.do");

		return mapping.findForward(FWD_SUCCESS);
	}

	private List<IdValuePair> getTestDropdownList() {
		List<IdValuePair> testList = DisplayListService.getInstance().getList( DisplayListService.ListType.ALL_TESTS );

		if( HAS_NFS_PANEL){
			testList = adjustNFSTests(testList);
		}
		Collections.sort( testList, new valueComparator() );
        return testList;
	}

	private List<IdValuePair> adjustNFSTests(List<IdValuePair> allTestsList) {
        List<IdValuePair> adjustedList = new ArrayList<IdValuePair>( allTestsList.size() );
		for (IdValuePair idValuePair : allTestsList) {
			if (!nfsTestIdList.contains( idValuePair.getId() )) {
				adjustedList.add( idValuePair );
			}
		}
        // add NFS to the list
        adjustedList.add( new IdValuePair( "NFS", "NFS" ) );
        return adjustedList;
    }

	@SuppressWarnings("unchecked")
	private List<TestResultItem> getWorkplanByTest(String testType) {

		List<Analysis> testList;
		List<TestResultItem> workplanTestList = new ArrayList<TestResultItem>();
		String currentAccessionNumber = null;
		String subjectNumber = null;
		String patientName = null;
		String nextVisit = null;
		int sampleGroupingNumber = 0;

		if (!(GenericValidator.isBlankOrNull(testType) || testType.equals("0"))) {

			testList = (List<Analysis>) analysisDAO.getAllAnalysisByTestAndStatus(testType, statusList);

			if (testList.isEmpty()) {
				return new ArrayList<TestResultItem>();
			}

			for (Analysis analysis : testList) {
				TestResultItem testResultItem = new TestResultItem();
				Sample sample = analysis.getSampleItem().getSample();
				testResultItem.setAccessionNumber(sample.getAccessionNumber());
				testResultItem.setReceivedDate( getReceivedDateDisplay(sample));
				testResultItem.setNonconforming(QAService.isAnalysisParentNonConforming(analysis));

				if(FormFields.getInstance().useField(Field.QaEventsBySection))
					testResultItem.setNonconforming(getQaEventByTestSection(analysis));
				
				if (!testResultItem.getAccessionNumber().equals(currentAccessionNumber)) {
					sampleGroupingNumber++;
					currentAccessionNumber = testResultItem.getAccessionNumber();
					subjectNumber = getSubjectNumber(analysis);
					patientName = getPatientName(analysis);
					nextVisit = ObservationHistoryService.getValueForSample( ObservationType.NEXT_VISIT_DATE, sample.getId() );
				}
				testResultItem.setSampleGroupingNumber(sampleGroupingNumber);
				testResultItem.setPatientInfo(subjectNumber);
				testResultItem.setPatientName(patientName);
				testResultItem.setNextVisitDate(nextVisit);
				
				
				workplanTestList.add(testResultItem);
			}

		}

		return workplanTestList;
	}

	@SuppressWarnings("unchecked")
	private List<TestResultItem> getWorkplanForNFSTest() {

		List<Analysis> testList;
		List<TestResultItem> workplanTestList = new ArrayList<TestResultItem>();
		String currentAccessionNumber = null;
		int sampleGroupingNumber = 0;

		TestResultItem testResultItem;

		List<String> testIdList = new ArrayList<String>();

		if (!(GenericValidator.isBlankOrNull(testType) || testType.equals("0"))) {

			testList = analysisDAO.getAllAnalysisByTestsAndStatus(nfsTestIdList, statusList);

			if (testList.isEmpty()) {
				return new ArrayList<TestResultItem>();
			}

			for (Analysis analysis : testList) {

				Sample sample = analysis.getSampleItem().getSample();
				String analysisAccessionNumber = sample.getAccessionNumber();

				if (!analysisAccessionNumber.equals(currentAccessionNumber)) {
					sampleGroupingNumber++;
					currentAccessionNumber = analysisAccessionNumber;
					testIdList = new ArrayList<String>();

				}
				testResultItem = new TestResultItem();
				testResultItem.setAccessionNumber(currentAccessionNumber);
				testResultItem.setReceivedDate(sample.getReceivedDateForDisplay());
				testResultItem.setSampleGroupingNumber(sampleGroupingNumber);
				testResultItem.setNonconforming(QAService.isAnalysisParentNonConforming(analysis));
				testIdList.add(analysis.getTest().getId());

				if (allNFSTestsRequested(testIdList)) {
					workplanTestList.add(testResultItem);
				}

			}

		}

		return workplanTestList;
	}
		
	private String getTestName(String testId) {
		return TestServiceImpl.getUserLocalizedTestName( testId );
	}

	private boolean getQaEventByTestSection(Analysis analysis){
		
		if (analysis.getTestSection()!=null && analysis.getSampleItem().getSample()!=null) {
			Sample sample=analysis.getSampleItem().getSample();
			List<SampleQaEvent> sampleQaEventsList=getSampleQaEvents(sample);
			for(SampleQaEvent event : sampleQaEventsList){
				QAService qa = new QAService(event);
				if(!GenericValidator.isBlankOrNull(qa.getObservationValue( QAObservationType.SECTION )) && qa.getObservationValue( QAObservationType.SECTION ).equals(analysis.getTestSection().getNameKey()))
					 return true;				
			}
		}
		return false;
	}

	public List<SampleQaEvent> getSampleQaEvents(Sample sample){
		SampleQaEventDAO sampleQaEventDAO = new SampleQaEventDAOImpl();
		return sampleQaEventDAO.getSampleQaEventsBySample(sample);
	}

	class valueComparator implements Comparator<IdValuePair> {

		public int compare(IdValuePair p1, IdValuePair p2) {
			return p1.getValue().toUpperCase().compareTo(p2.getValue().toUpperCase());
		}

	}

}
