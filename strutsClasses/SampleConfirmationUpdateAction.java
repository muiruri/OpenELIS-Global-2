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
package org.openelisglobal.sample.action;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.validator.GenericValidator;
import org.apache.struts.Globals;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessages;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.hibernate.Transaction;

import org.openelisglobal.analysis.dao.AnalysisDAO;
import org.openelisglobal.analysis.daoimpl.AnalysisDAOImpl;
import org.openelisglobal.analysis.valueholder.Analysis;
import org.openelisglobal.common.action.BaseActionForm;
import org.openelisglobal.common.action.IActionConstants;
import org.openelisglobal.common.formfields.FormFields;
import org.openelisglobal.common.formfields.FormFields.Field;
import org.openelisglobal.common.provider.validation.IAccessionNumberValidator;
import org.openelisglobal.common.provider.validation.IAccessionNumberValidator.ValidationResults;
import org.openelisglobal.common.services.NoteService;
import org.openelisglobal.common.services.RequesterService;
import org.openelisglobal.common.services.StatusService;
import org.openelisglobal.common.services.StatusService.AnalysisStatus;
import org.openelisglobal.common.services.StatusService.OrderStatus;
import org.openelisglobal.common.services.StatusService.SampleStatus;
import org.openelisglobal.common.util.DateUtil;
import org.openelisglobal.common.util.StringUtil;
import org.openelisglobal.common.util.validator.ActionError;
import org.openelisglobal.hibernate.HibernateUtil;
import org.openelisglobal.note.dao.NoteDAO;
import org.openelisglobal.note.daoimpl.NoteDAOImpl;
import org.openelisglobal.note.valueholder.Note;
import org.openelisglobal.observationhistory.dao.ObservationHistoryDAO;
import org.openelisglobal.observationhistory.daoimpl.ObservationHistoryDAOImpl;
import org.openelisglobal.observationhistory.valueholder.ObservationHistory;
import org.openelisglobal.observationhistorytype.dao.ObservationHistoryTypeDAO;
import org.openelisglobal.observationhistorytype.daoImpl.ObservationHistoryTypeDAOImpl;
import org.openelisglobal.observationhistorytype.valueholder.ObservationHistoryType;
import org.openelisglobal.organization.dao.OrganizationContactDAO;
import org.openelisglobal.organization.daoimpl.OrganizationContactDAOImpl;
import org.openelisglobal.organization.daoimpl.OrganizationDAOImpl;
import org.openelisglobal.organization.daoimpl.OrganizationOrganizationTypeDAOImpl;
import org.openelisglobal.organization.valueholder.Organization;
import org.openelisglobal.organization.valueholder.OrganizationContact;
import org.openelisglobal.patient.action.IPatientUpdate;
import org.openelisglobal.patient.action.PatientManagementUpdateAction;
import org.openelisglobal.patient.action.bean.PatientManagementInfo;
import org.openelisglobal.person.dao.PersonDAO;
import org.openelisglobal.person.daoimpl.PersonDAOImpl;
import org.openelisglobal.person.valueholder.Person;
import org.openelisglobal.referral.dao.ReferringTestResultDAO;
import org.openelisglobal.referral.daoimpl.ReferringTestResultDAOImpl;
import org.openelisglobal.referral.valueholder.ReferringTestResult;
import org.openelisglobal.requester.dao.SampleRequesterDAO;
import org.openelisglobal.requester.daoimpl.SampleRequesterDAOImpl;
import org.openelisglobal.requester.valueholder.SampleRequester;
import org.openelisglobal.sample.bean.SampleOrderItem;
import org.openelisglobal.sample.dao.SampleDAO;
import org.openelisglobal.sample.daoimpl.SampleDAOImpl;
import org.openelisglobal.sample.util.AccessionNumberUtil;
import org.openelisglobal.sample.valueholder.Sample;
import org.openelisglobal.samplehuman.dao.SampleHumanDAO;
import org.openelisglobal.samplehuman.daoimpl.SampleHumanDAOImpl;
import org.openelisglobal.samplehuman.valueholder.SampleHuman;
import org.openelisglobal.sampleitem.dao.SampleItemDAO;
import org.openelisglobal.sampleitem.daoimpl.SampleItemDAOImpl;
import org.openelisglobal.sampleitem.valueholder.SampleItem;
import org.openelisglobal.test.dao.TestDAO;
import org.openelisglobal.test.daoimpl.TestDAOImpl;
import org.openelisglobal.test.valueholder.Test;
import org.openelisglobal.typeofsample.dao.TypeOfSampleDAO;
import org.openelisglobal.typeofsample.daoimpl.TypeOfSampleDAOImpl;

/**
 * The SampleEntryAction class represents the initial Action for the SampleEntry
 * form of the application
 *
 */
public class SampleConfirmationUpdateAction extends BaseSampleEntryAction {

	private SampleRequester personSampleRequester;
    private SampleRequester organizationSampleRequester;
	private Person personRequester;
    private Organization createdOrganization;
	private OrganizationContact organizationContact;
    private boolean savePersonRequester = false;
	private static boolean useInitialSampleCondition;

	private static SampleDAO sampleDAO = new SampleDAOImpl();
	private static SampleHumanDAO sampleHumanDAO = new SampleHumanDAOImpl();
	private static TestDAO testDAO = new TestDAOImpl();
	private static SampleItemDAO sampleItemDAO = new SampleItemDAOImpl();
	private static AnalysisDAO analysisDAO = new AnalysisDAOImpl();
	private static NoteDAO noteDAO = new NoteDAOImpl();
	private static SampleRequesterDAO sampleRequesterDAO = new SampleRequesterDAOImpl();
	private static PersonDAO personDAO = new PersonDAOImpl();
	private static OrganizationContactDAO orgContactDAO = new OrganizationContactDAOImpl();
	private static ObservationHistoryDAO ohDAO = new ObservationHistoryDAOImpl();
	private static TypeOfSampleDAO typeOfSampleDAO = new TypeOfSampleDAOImpl();
    private static ReferringTestResultDAO referringTestResultDAO = new ReferringTestResultDAOImpl();

	private static String INITIAL_CONDITION_OBSERVATION_ID;

	static {
		ObservationHistoryTypeDAO ohtDAO = new ObservationHistoryTypeDAOImpl();
		ObservationHistoryType oht = ohtDAO.getByName("initialSampleCondition");
		if (oht != null) {
			INITIAL_CONDITION_OBSERVATION_ID = oht.getId();
		}
		
		useInitialSampleCondition = FormFields.getInstance().useField(Field.InitialSampleCondition);
	}

	protected ActionForward performAction(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response)
			throws Exception {

		String forward = "success";

		request.getSession().setAttribute(IActionConstants.SAVE_DISABLED, IActionConstants.TRUE);

		BaseActionForm dynaForm = (BaseActionForm) form;
        SampleOrderItem sampleOrder = (SampleOrderItem)dynaForm.get("sampleOrderItems");

		ActionMessages errors = new ActionMessages();
		validateSample(errors, sampleOrder.getLabNo());

		if (errors.size() > 0) {
			saveErrors(request, errors);
			request.setAttribute(Globals.ERROR_KEY, errors);
			return mapping.findForward(FWD_FAIL);
		}
		
		PatientManagementInfo patientInfo = (PatientManagementInfo ) dynaForm.get("patientProperties");
		IPatientUpdate patientUpdate = new PatientManagementUpdateAction();
		boolean savePatient = testAndInitializePatientForSaving(mapping, request, patientInfo, patientUpdate);

        SampleHuman sampleHuman = new SampleHuman();
        sampleHuman.setSysUserId(currentUserId);

		Sample sample = createSample(sampleOrder);
        List<SampleItemSet> sampleItemSetList = createSampleItemSets(sample, dynaForm);
		createRequesters(sampleOrder);

		Transaction tx = HibernateUtil.getSession().beginTransaction();

		try {
			if (savePatient) {
				patientUpdate.persistPatientData(patientInfo);
			}


			// commented out to allow maven compilation - CSL
			/*String patientId = patientUpdate.getPatientId(dynaForm);

			sampleDAO.insertDataWithAccessionNumber(sample);
			sampleHuman.setSampleId(sample.getId());
			sampleHuman.setPatientId(patientId);
			sampleHumanDAO.insertData(sampleHuman);
*/
            if( createdOrganization != null){
                new OrganizationDAOImpl().insertData(createdOrganization);
                new OrganizationOrganizationTypeDAOImpl().linkOrganizationAndType(createdOrganization, RequesterService.REFERRAL_ORG_TYPE_ID );
                //organizationSampleRequester will not be null if there is a new organization
                organizationSampleRequester.setRequesterId(createdOrganization.getId());

                if(organizationContact != null){
                    //existing organization id set when organizationContact created
                    organizationContact.setOrganizationId(createdOrganization.getId());
                }
            }

			if (organizationSampleRequester != null) {
                organizationSampleRequester.setSampleId(Long.parseLong( sample.getId()));
				sampleRequesterDAO.insertData(organizationSampleRequester);
			}

			if (savePersonRequester && personRequester != null) {
				if (personRequester.getId() != null) {
					personDAO.updateData(personRequester);
				} else {
					personDAO.insertData(personRequester);
				}
			}

			if (personSampleRequester != null && personRequester != null) {
				personSampleRequester.setRequesterId(personRequester.getId());
				personSampleRequester.setSampleId(Long.parseLong( sample.getId()));
				sampleRequesterDAO.insertData(personSampleRequester);
			}

			if (organizationContact != null) {
				organizationContact.setPerson(personRequester);
				orgContactDAO.insert(organizationContact);
			}

			for (SampleItemSet sampleItemSet : sampleItemSetList) {
				sampleItemDAO.insertData(sampleItemSet.sampleItem);

				if (sampleItemSet.note != null) {
					sampleItemSet.note.setReferenceId(sampleItemSet.sampleItem.getId());
					noteDAO.insertData(sampleItemSet.note);
				}

				for (Analysis analysis : sampleItemSet.requestedAnalysisList) {
					analysisDAO.insertData(analysis, false);
				}

                for( ReferringTestResult referringTestResult : sampleItemSet.referringTestResultList){
                    referringTestResult.setSampleItemId(sampleItemSet.sampleItem.getId());
                    referringTestResultDAO.insertData(referringTestResult);
                }
				if (useInitialSampleCondition) {

					// commented out to allow maven compilation - CSL
					//persistInitialSampleConditions(sampleItemSet, patientId);
				}
			}

			tx.commit();
		} catch (Exception e) {
			tx.rollback();
		}

		return mapping.findForward(forward);
	}

	private void validateSample(ActionMessages errors, String accessionNumber) {
		// assure accession number
		ValidationResults result = AccessionNumberUtil.checkAccessionNumberValidity(accessionNumber, null, null, null);

		if (result != IAccessionNumberValidator.ValidationResults.SUCCESS) {
			String message = AccessionNumberUtil.getInvalidMessage(result);
			errors.add(ActionErrors.GLOBAL_MESSAGE, new ActionError(message));
		}

//		// assure that there is at least 1 sample
//		if (sampleItemsTests.isEmpty()) {
//			errors.add(ActionErrors.GLOBAL_MESSAGE, new ActionError("errors.no.sample"));
//		}
//
//		// assure that all samples have tests
//		if (!allSamplesHaveTests()) {
//			errors.add(ActionErrors.GLOBAL_MESSAGE, new ActionError("errors.samples.with.no.tests"));
//		}

	}
	
	private boolean testAndInitializePatientForSaving(ActionMapping mapping, HttpServletRequest request, PatientManagementInfo patientInfo,
			IPatientUpdate patientUpdate) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {

		patientUpdate.setPatientUpdateStatus(patientInfo);
		boolean savePatient = patientUpdate.getPatientUpdateStatus() != PatientManagementUpdateAction.PatientUpdateStatus.NO_ACTION;

		if (savePatient) {

			// commented out to allow maven compilation - CSL
			//patientUpdate.preparePatientData(mapping, request, patientInfo);
		}

        return savePatient;
	}

	private Sample createSample( SampleOrderItem sampleOrder) {
		
		String receivedDate = sampleOrder.getReceivedDateForDisplay();
		String receivedTime = sampleOrder.getReceivedTime();

		receivedDate += GenericValidator.isBlankOrNull(receivedTime) ? " 00:00" : ( " " + receivedTime); 

		
		Sample sample = new Sample();
		sample.setAccessionNumber(sampleOrder.getLabNo());
		sample.setReceivedTimestamp(DateUtil.convertStringDateToTimestamp(receivedDate));
		sample.setCollectionDate(sample.getReceivedTimestamp()); //note there really is no collection date but other code thinks there is
		sample.setSysUserId(currentUserId);
		sample.setDomain("H");
		sample.setEnteredDate(DateUtil.getNowAsSqlDate());
		sample.setStatusId(StatusService.getInstance().getStatusID(OrderStatus.Entered));
        sample.setIsConfirmation(true);

        return sample;
	}

	private List<SampleItemSet> createSampleItemSets(Sample sample, BaseActionForm dynaForm) throws DocumentException {
        List<SampleItemSet> sampleItemSetList = new ArrayList<SampleItemSet>();
		Document requestedTestsDOM = DocumentHelper.parseText(dynaForm.getString("requestAsXML"));

		int sampleItemSortOrder = 0;
		for (Object element : requestedTestsDOM.getRootElement().element("samples").elements("sample")) {
			SampleItemSet sampleItemSet = new SampleItemSet();

			Element sampleItemElement = (Element) element;

			SampleItem sampleItem = new SampleItem();
			sampleItem.setStatusId(StatusService.getInstance().getStatusID(SampleStatus.Entered));
			sampleItemSet.sampleItem = sampleItem;

			String externalId = sampleItemElement.attributeValue("requesterSampleId");
			sampleItem.setExternalId(GenericValidator.isBlankOrNull(externalId) ? null : externalId);

			String sampleTypeId = sampleItemElement.attributeValue("sampleType");
			sampleItem.setTypeOfSample(typeOfSampleDAO.getTypeOfSampleById(sampleTypeId));

			String collectionDate = sampleItemElement.attributeValue("collectionDate");
			String collectionTime = sampleItemElement.attributeValue("collectionTime");

            if (!GenericValidator.isBlankOrNull(collectionDate)) {
                collectionDate += GenericValidator.isBlankOrNull(collectionTime) ? " 00:00" : ( " " + collectionTime);
                sampleItem.setCollectionDate(DateUtil.convertStringDateToTimestamp(collectionDate));
            }

			sampleItem.setSortOrder( String.valueOf(sampleItemSortOrder));
			sampleItemSortOrder++;
			sampleItem.setSysUserId(currentUserId);
			sampleItem.setSample(sample);

			sampleItemSet.note = createNote(sampleItemElement);
			sampleItemSet.requestedAnalysisList = createRequestedAnalysisSet(sampleItemElement, sampleItem);
			sampleItemSet.referringTestResultList = createReferringTestResultList(sampleItemElement, currentUserId);

			List<ObservationHistory> initialConditionList = null;
			if (useInitialSampleCondition) {
				String initialSampleConditionIdString = sampleItemElement.attributeValue("initialConditionIds");
				if ( !GenericValidator.isBlankOrNull(initialSampleConditionIdString)) {
					String[] initialSampleConditionIds = initialSampleConditionIdString.split(",");
					initialConditionList = new ArrayList<ObservationHistory>();

					for ( String initialSampleConditionId : initialSampleConditionIds) {
						ObservationHistory initialSampleConditions = new ObservationHistory();
						initialSampleConditions.setValue(initialSampleConditionId);
						initialSampleConditions.setValueType(ObservationHistory.ValueType.DICTIONARY);
						initialSampleConditions.setObservationHistoryTypeId(INITIAL_CONDITION_OBSERVATION_ID);
						initialConditionList.add(initialSampleConditions);
					}
				}
			}
			sampleItemSet.initialConditionList = initialConditionList;
			
			sampleItemSetList.add(sampleItemSet);
		}

        return sampleItemSetList;
	}

	private Note createNote(Element sampleItemElement) {
		String noteText = sampleItemElement.attributeValue("note");

		if (!GenericValidator.isBlankOrNull(noteText)) {
              return new NoteService( new SampleItem() ).createSavableNote( NoteService.NoteType.INTERNAL, noteText, null, currentUserId );
		}

		return null;
	}

	private List<ReferringTestResult> createReferringTestResultList(Element sampleItemElement, String currentUserId) {
		List<ReferringTestResult> referringTestResultList = new ArrayList<ReferringTestResult>();

		for (Object element : sampleItemElement.element("tests").elements("test")) {
			Element testElement = (Element) element;

			String testName = testElement.attributeValue("name");

            if(!GenericValidator.isBlankOrNull(testName)) {
                ReferringTestResult referringTestResult = new ReferringTestResult();
                referringTestResult.setTestName(testName);
                referringTestResult.setResultValue(testElement.attributeValue("value"));
                referringTestResult.setSysUserId(currentUserId);
                referringTestResultList.add(referringTestResult);
            }
		}

		return referringTestResultList;
	}

	private List<Analysis> createRequestedAnalysisSet(Element sampleItemElement, SampleItem sampleItem) {
		List<Analysis> analysisList = new ArrayList<Analysis>();
		String requestedAanlysisIds = sampleItemElement.attributeValue("requestedTests");

		if (!GenericValidator.isBlankOrNull(requestedAanlysisIds)) {
			String[] splitIds = requestedAanlysisIds.split(",");

			for (String id :splitIds ) {
				Analysis analysis = new Analysis();
				Test test = testDAO.getTestById(id);
				if (test != null) {
					analysis.setTest(test);
					analysis.setTestSection(test.getTestSection());
					analysis.setAnalysisType("MANUAL");
					analysis.setSysUserId(currentUserId);
					analysis.setStatusId(StatusService.getInstance().getStatusID(AnalysisStatus.NotStarted));
					analysis.setIsReportable(test.getIsReportable());
					analysis.setSampleItem(sampleItem);
					analysis.setRevision("0");
					analysis.setStartedDate(DateUtil.getNowAsSqlDate());
					analysisList.add(analysis);
				}
			}

		}

		return analysisList;
	}

	private void createRequesters( SampleOrderItem sampleOrder ) {
        /*
        The concerns here are about the organization and person requesters.  Both or neither may have
        been specified and both or neither may be new.  If the person is not new then the information may
        have been changed.
         */

		personSampleRequester = null;
        organizationSampleRequester = null;
		organizationContact = null;
        createdOrganization = null;

        if( noRequesters(sampleOrder)){
            return;
        }

        String orgId =  sampleOrder.getReferringSiteId();
        String personId = sampleOrder.getProviderId();

        boolean existingOrganization = !(GenericValidator.isBlankOrNull(orgId) || "0".equals(orgId));
        boolean newOrganization = !GenericValidator.isBlankOrNull(sampleOrder.getNewRequesterName());
        boolean existingRequester = !(GenericValidator.isBlankOrNull(personId) || "0".equals(personId));
        boolean hasProviderInformation = !noProviderInformation(sampleOrder);
        boolean newRequester = !existingRequester && hasProviderInformation;


		if( newOrganization ){
            createdOrganization = new Organization();
            createdOrganization.setIsActive("Y");
            createdOrganization.setMlsSentinelLabFlag("N");
            createdOrganization.setOrganizationName(sampleOrder.getNewRequesterName());
            createdOrganization.setCode(sampleOrder.getReferringSiteCode());
            createdOrganization.setSysUserId(currentUserId);
        }


		if ( existingRequester) {
			personRequester = personDAO.getPersonById(personId);
		} else if( newRequester) {
			personRequester = new Person();
		}

        savePersonRequester = false;

        if( (existingRequester || newRequester) && personRequesterChanged(personRequester, sampleOrder) ){
            personRequester.setFirstName(sampleOrder.getProviderFirstName());
            personRequester.setLastName(sampleOrder.getProviderLastName());
            personRequester.setWorkPhone(sampleOrder.getProviderWorkPhone());
            personRequester.setFax(sampleOrder.getProviderFax());
            personRequester.setEmail(sampleOrder.getProviderEmail());
            personRequester.setSysUserId(currentUserId);
            savePersonRequester = true;
        }

        //This checks if there is a requester and organization and that one of them is new
        if( (newRequester || existingRequester ) &&
                (newOrganization || existingOrganization ) &&
                (newOrganization || newRequester) ){
            organizationContact = new OrganizationContact();
            organizationContact.setOrganizationId(orgId); //This may be overridden if new organization
            organizationContact.setPerson(personRequester);
            organizationContact.setSysUserId(currentUserId);
        }

        new RequesterService(null);
        if( newRequester || existingRequester) {
            personSampleRequester = new SampleRequester();
            personSampleRequester.setRequesterTypeId(RequesterService.Requester.PERSON.getId());
            if( !GenericValidator.isBlankOrNull(personId)) {
                personSampleRequester.setRequesterId(personId); //This may be overridden if new provider
            }
            personSampleRequester.setSysUserId(currentUserId);
        }

        if( newOrganization || existingOrganization){

            organizationSampleRequester = new SampleRequester();
            organizationSampleRequester.setRequesterTypeId(RequesterService.Requester.ORGANIZATION.getId());
            if( !GenericValidator.isBlankOrNull(orgId)) {
                organizationSampleRequester.setRequesterId(orgId); //This may be overridden if new organization
            }
            organizationSampleRequester.setSysUserId(currentUserId);
        }
	}

    /**
     * Checks if the requester information has changed from what is in the database
     *
     * @param personRequester The existing requester
     * @param sampleOrder The requester filled in by the user
     * @return true if it has changed, false otherwise
     */
    private boolean personRequesterChanged(Person personRequester, SampleOrderItem sampleOrder) {
        return !(StringUtil.safeEquals(personRequester.getFirstName(), sampleOrder.getProviderFirstName())&&
        StringUtil.safeEquals(personRequester.getLastName(),sampleOrder.getProviderLastName()) &&
        StringUtil.safeEquals(personRequester.getWorkPhone(), sampleOrder.getProviderWorkPhone())&&
        StringUtil.safeEquals(personRequester.getFax(), sampleOrder.getProviderFax())&&
        StringUtil.safeEquals(personRequester.getEmail(), sampleOrder.getProviderEmail()));
    }

    /**
     * checks to see if there are any requesters
     *
     * @param sampleOrder The sample order item
     * @return true if the are no requesters, false if there are
     */
    private boolean noRequesters(SampleOrderItem sampleOrder) {
        /*
        if the org id is 0 and there is not a new org and the requester id is 0 and there is no new information entered
        then there is
         */
        return (GenericValidator.isBlankOrNull(sampleOrder.getReferringSiteId()) || "0".equals(sampleOrder.getReferringSiteId())) &&
                GenericValidator.isBlankOrNull(sampleOrder.getNewRequesterName()) &&
                (GenericValidator.isBlankOrNull(sampleOrder.getProviderId()) || "0".equals(sampleOrder.getProviderId())) &&
                noProviderInformation(sampleOrder);
    }

    private boolean noProviderInformation(SampleOrderItem sampleOrder) {
        return GenericValidator.isBlankOrNull(sampleOrder.getProviderFirstName() +
                sampleOrder.getProviderLastName() +
                sampleOrder.getProviderEmail() +
                sampleOrder.getProviderFax() +
                sampleOrder.getProviderWorkPhone());
    }

    private void persistInitialSampleConditions( SampleItemSet sampleItemSet, String patientId) {
			if (sampleItemSet.initialConditionList != null) {
				for (ObservationHistory observation : sampleItemSet.initialConditionList) {
					observation.setSampleId(sampleItemSet.sampleItem.getSample().getId());
					observation.setSampleItemId(sampleItemSet.sampleItem.getId());
					observation.setPatientId(patientId);
					observation.setSysUserId(currentUserId);
					ohDAO.insertData(observation);
				}
			}

	}
	protected String getPageTitleKey() {
		return StringUtil.getContextualKeyForKey("banner.menu.sample.confirmation.add");
	}

	protected String getPageSubtitleKey() {
		return StringUtil.getContextualKeyForKey("banner.menu.sample.confirmation.add");
	}

	class SampleItemSet {
		public SampleItem sampleItem;
		public Note note;
		public List<Analysis> requestedAnalysisList;
		public List<ObservationHistory> initialConditionList;
        public List<ReferringTestResult> referringTestResultList;
	}


}
