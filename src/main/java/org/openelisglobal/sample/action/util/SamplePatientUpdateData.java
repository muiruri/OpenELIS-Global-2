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

package org.openelisglobal.sample.action.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.validator.GenericValidator;
import org.openelisglobal.address.valueholder.OrganizationAddress;
import org.openelisglobal.common.formfields.FormFields;
import org.openelisglobal.common.formfields.FormFields.Field;
import org.openelisglobal.common.provider.validation.IAccessionNumberValidator;
import org.openelisglobal.common.services.SampleAddService;
import org.openelisglobal.common.services.SampleAddService.SampleTestCollection;
import org.openelisglobal.common.services.StatusService;
import org.openelisglobal.common.services.StatusService.ExternalOrderStatus;
import org.openelisglobal.common.services.StatusService.OrderStatus;
import org.openelisglobal.common.services.TableIdService;
import org.openelisglobal.common.util.ConfigurationProperties;
import org.openelisglobal.common.util.ConfigurationProperties.Property;
import org.openelisglobal.common.util.DateUtil;
import org.openelisglobal.common.util.StringUtil;
import org.openelisglobal.common.util.SystemConfiguration;
import org.openelisglobal.dataexchange.order.valueholder.ElectronicOrder;
import org.openelisglobal.dataexchange.service.order.ElectronicOrderService;
import org.openelisglobal.observationhistory.service.ObservationHistoryService;
import org.openelisglobal.observationhistory.service.ObservationHistoryServiceImpl.ObservationType;
import org.openelisglobal.observationhistory.valueholder.ObservationHistory;
import org.openelisglobal.observationhistory.valueholder.ObservationHistory.ValueType;
import org.openelisglobal.organization.service.OrganizationService;
import org.openelisglobal.organization.valueholder.Organization;
import org.openelisglobal.patient.service.PatientService;
import org.openelisglobal.patient.util.PatientUtil;
import org.openelisglobal.person.valueholder.Person;
import org.openelisglobal.provider.valueholder.Provider;
import org.openelisglobal.requester.valueholder.SampleRequester;
import org.openelisglobal.sample.bean.SampleOrderItem;
import org.openelisglobal.sample.util.AccessionNumberUtil;
import org.openelisglobal.sample.valueholder.Sample;
import org.openelisglobal.samplehuman.valueholder.SampleHuman;
import org.openelisglobal.spring.util.SpringContext;
import org.springframework.validation.Errors;

/**
 */
public class SamplePatientUpdateData {
    private boolean savePatient = false;
    private Person providerPerson;
    private Provider provider;
    private String patientId;
    private String accessionNumber;

    private Sample sample;
    private SampleHuman sampleHuman = new SampleHuman();
    private SampleRequester requesterSite;
    private List<SampleTestCollection> sampleItemsTests;
    private SampleAddService sampleAddService;
    private Errors patientErrors;
    private Organization newOrganization;
    private Organization currentOrganization;
    private ElectronicOrder electronicOrder = null;

    private boolean useReceiveDateForCollectionDate = !FormFields.getInstance().useField(Field.CollectionDate);
    private String collectionDateFromReceiveDate = null;

    private PatientService patientService = SpringContext.getBean(PatientService.class);
    private OrganizationService orgService = SpringContext.getBean(OrganizationService.class);
    private ElectronicOrderService electronicOrderService = SpringContext.getBean(ElectronicOrderService.class);

    private List<ObservationHistory> observations = new ArrayList<>();
    private List<OrganizationAddress> orgAddressExtra = new ArrayList<>();
    private final String currentUserId;

    public SamplePatientUpdateData(String currentUserId) {
        this.currentUserId = currentUserId;
    }

    public boolean isSavePatient() {
        return savePatient;
    }

    public void setSavePatient(boolean savePatient) {
        this.savePatient = savePatient;
    }

    public Person getProviderPerson() {
        return providerPerson;
    }

    public void setProviderPerson(Person providerPerson) {
        this.providerPerson = providerPerson;
    }

    public Provider getProvider() {
        return provider;
    }

    public void setProvider(Provider provider) {
        this.provider = provider;
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public String getAccessionNumber() {
        return accessionNumber;
    }

    public void setAccessionNumber(String accessionNumber) {
        this.accessionNumber = accessionNumber;
    }

    public Sample getSample() {
        return sample;
    }

    public void setSample(Sample sample) {
        this.sample = sample;
    }

    public SampleHuman getSampleHuman() {
        return sampleHuman;
    }

    public void setSampleHuman(SampleHuman sampleHuman) {
        this.sampleHuman = sampleHuman;
    }

    public SampleRequester getRequesterSite() {
        return requesterSite;
    }

    public void setRequesterSite(SampleRequester requesterSite) {
        this.requesterSite = requesterSite;
    }

    public List<SampleTestCollection> getSampleItemsTests() {
        return sampleItemsTests;
    }

    public void setSampleItemsTests(List<SampleTestCollection> sampleItemsTests) {
        this.sampleItemsTests = sampleItemsTests;
    }

    public SampleAddService getSampleAddService() {
        return sampleAddService;
    }

    public void setSampleAddService(SampleAddService sampleAddService) {
        this.sampleAddService = sampleAddService;
    }

    public void setPatientErrors(Errors patientErrors) {
        this.patientErrors = patientErrors;
    }

    public Organization getNewOrganization() {
        return newOrganization;
    }

    public void setNewOrganization(Organization newOrganization) {
        this.newOrganization = newOrganization;
    }

    public Organization getCurrentOrganization() {
        return currentOrganization;
    }

    public ElectronicOrder getElectronicOrder() {
        return electronicOrder;
    }

    public void setCollectionDateFromRecieveDateIfNeeded(String collectionDateFromRecieveDate) {
        if (useReceiveDateForCollectionDate) {
            collectionDateFromReceiveDate = collectionDateFromRecieveDate;
        }
    }

    public List<ObservationHistory> getObservations() {
        return observations;
    }

    public List<OrganizationAddress> getOrgAddressExtra() {
        return orgAddressExtra;
    }

    public String getCurrentUserId() {
        return currentUserId;
    }

    public void addOrgAddressExtra(String value, String type, String addressPart) {
        if (!GenericValidator.isBlankOrNull(value)) {
            OrganizationAddress orgAddress = new OrganizationAddress();
            orgAddress.setSysUserId(currentUserId);
            orgAddress.setType(type);
            orgAddress.setValue(value);
            orgAddress.setAddressPartId(addressPart);
            orgAddressExtra.add(orgAddress);
        }
    }

    public void createObservation(String observationData, String observationType,
            ObservationHistory.ValueType valueType) {
        if (!GenericValidator.isBlankOrNull(observationData) && !GenericValidator.isBlankOrNull(observationType)) {
            ObservationHistory observation = new ObservationHistory();
            observation.setObservationHistoryTypeId(observationType);
            observation.setSysUserId(currentUserId);
            observation.setValue(observationData);
            observation.setValueType(valueType);
            observations.add(observation);
        }
    }

    public void validateSample(Errors errors) {
        // assure accession number

        // TODO
        IAccessionNumberValidator.ValidationResults result = AccessionNumberUtil
                .checkAccessionNumberValidity(accessionNumber, null, null, null);

        if (result != IAccessionNumberValidator.ValidationResults.SUCCESS) {
            String message = AccessionNumberUtil.getInvalidMessage(result);
            errors.reject(message);
        }

        // assure that there is at least 1 sample
        if (sampleItemsTests.isEmpty()) {
            errors.reject("errors.no.sample");
        }

        // assure that all samples have tests
        if (!allSamplesHaveTests()) {
            errors.reject("errors.samples.with.no.tests");
        }

        // check patient errors
        if (patientErrors.hasErrors()) {
            errors.addAllErrors(patientErrors);
        }
    }

    private boolean allSamplesHaveTests() {

        for (SampleTestCollection sampleTest : sampleItemsTests) {
            if (sampleTest.tests.size() == 0) {
                return false;
            }
        }

        return true;
    }

    public void createPopulatedSample(String receivedDate, SampleOrderItem sampleOrder) {
        sample = new Sample();
        sample.setSysUserId(currentUserId);
        sample.setAccessionNumber(accessionNumber);

        sample.setEnteredDate(DateUtil.getNowAsSqlDate());

        sample.setReceivedTimestamp(DateUtil.convertStringDateToTimestamp(receivedDate));
        sample.setReferringId(sampleOrder.getRequesterSampleID());

        if (useReceiveDateForCollectionDate) {
            sample.setCollectionDateForDisplay(collectionDateFromReceiveDate);
        }

        sample.setDomain(SystemConfiguration.getInstance().getHumanDomain());
        sample.setStatusId(StatusService.getInstance().getStatusID(OrderStatus.Entered));

        setElectronicOrderIfNeeded(sampleOrder);
    }

    private void setElectronicOrderIfNeeded(SampleOrderItem sampleOrder) {
        electronicOrder = null;
        String externalOrderNumber = sampleOrder.getExternalOrderNumber();
        if (!GenericValidator.isBlankOrNull(externalOrderNumber)) {
            List<ElectronicOrder> orders = electronicOrderService.getElectronicOrdersByExternalId(externalOrderNumber);
            if (!orders.isEmpty()) {
                electronicOrder = orders.get(orders.size() - 1);
                electronicOrder.setStatusId(StatusService.getInstance().getStatusID(ExternalOrderStatus.Realized));
                electronicOrder.setSysUserId(currentUserId);

                sample.setReferringId(externalOrderNumber);
                sample.setClinicalOrderId(electronicOrder.getId());
            }
        }
    }

    public void initProvider(SampleOrderItem sampleOrder) {

        providerPerson = null;
        if (noRequesterInformation(sampleOrder)) {
            provider = PatientUtil.getUnownProvider();
        } else {
            providerPerson = new Person();
            provider = new Provider();

            providerPerson.setFirstName(sampleOrder.getProviderFirstName());
            providerPerson.setLastName(sampleOrder.getProviderLastName());
            providerPerson.setWorkPhone(sampleOrder.getProviderWorkPhone());
            providerPerson.setFax(sampleOrder.getProviderFax());
            providerPerson.setEmail(sampleOrder.getProviderEmail());
            providerPerson.setSysUserId(currentUserId);
            provider.setExternalId(sampleOrder.getRequesterSampleID());
        }

        provider.setSysUserId(currentUserId);
    }

    private boolean noRequesterInformation(SampleOrderItem sampleOrder) {
        return (GenericValidator.isBlankOrNull(sampleOrder.getProviderFirstName())
                && GenericValidator.isBlankOrNull(sampleOrder.getProviderWorkPhone())
                && GenericValidator.isBlankOrNull(sampleOrder.getProviderLastName())
                && GenericValidator.isBlankOrNull(sampleOrder.getRequesterSampleID())
                && GenericValidator.isBlankOrNull(sampleOrder.getProviderFax())
                && GenericValidator.isBlankOrNull(sampleOrder.getProviderEmail()));
    }

    public void buildSampleHuman() {
        sampleHuman.setSysUserId(currentUserId);
        sampleHuman.setSampleId(sample.getId());
        sampleHuman.setPatientId(patientId);
        if (provider != null) {
            sampleHuman.setProviderId(provider.getId());
        }
    }

    public void initializeNewOrganization(SampleOrderItem orderItem) {
        newOrganization.setCode(orderItem.getReferringSiteCode());

        newOrganization.setIsActive("Y");
        newOrganization.setOrganizationName(orderItem.getNewRequesterName());

        // this was left as a warning for copy and paste -- it causes a null
        // pointer exception in session.flush()
        // newOrganization.setOrganizationTypes(ORG_TYPE_SET);
        newOrganization.setSysUserId(currentUserId);
        newOrganization.setMlsSentinelLabFlag("N");
    }

    public void updateCurrentOrgIfNeeded(String code, String orgId) {
        currentOrganization = orgService.getOrganizationById(orgId);
        if (StringUtil.compareWithNulls(code, currentOrganization.getCode()) != 0) {
            currentOrganization.setCode(code);
            currentOrganization.setSysUserId(currentUserId);
        } else {
            currentOrganization = null;
        }
    }

    public void initializeRequester(SampleOrderItem sampleOrder) {
        if (FormFields.getInstance().useField(Field.RequesterSiteList)) {
            setRequesterSite(initSampleRequester(sampleOrder));
        }
    }

    private SampleRequester initSampleRequester(SampleOrderItem orderItem) {
        SampleRequester requester = null;

        String orgId = orderItem.getReferringSiteId();

        if (!GenericValidator.isBlankOrNull(orgId)) {
            requester = createSiteRequester(orgId);
            if (FormFields.getInstance().useField(Field.SampleEntryReferralSiteCode)) {
                updateCurrentOrgIfNeeded(orderItem.getReferringSiteCode(), orgId);
            }

        } else if (!GenericValidator.isBlankOrNull(orderItem.getNewRequesterName())) {

            if (confirmNewRequesterName(orderItem.getNewRequesterName())) {
                // will be corrected after newOrg is persisted
                requester = createSiteRequester("0");

                setNewOrganization(new Organization());

                if (FormFields.getInstance().useField(Field.SampleEntryHealthFacilityAddress)) {
                    addOrgAddressExtra(orderItem.getFacilityPhone(), "T",
                            TableIdService.getInstance().ADDRESS_PHONE_ID);
                    addOrgAddressExtra(orderItem.getFacilityFax(), "T", TableIdService.getInstance().ADDRESS_FAX_ID);
                    addOrgAddressExtra(orderItem.getFacilityAddressCommune(), "T",
                            TableIdService.getInstance().ADDRESS_COMMUNE_ID);
                    addOrgAddressExtra(orderItem.getFacilityAddressStreet(), "T",
                            TableIdService.getInstance().ADDRESS_STREET_ID);
                }

                initializeNewOrganization(orderItem);
            } else {
                Organization organization = new Organization();
                organization.setOrganizationName(orderItem.getNewRequesterName());
                organization = orgService.getOrganizationByName(organization, true);
                orgId = organization.getId();

                if (!GenericValidator.isBlankOrNull(orgId)) {
                    requester = createSiteRequester(orgId);
                }
            }
        }

        return requester;
    }

    /**
     * Check if new requester name is actually a new name
     *
     * @param requesterName The name to check
     * @return if the name is not stored in the database
     */
    private boolean confirmNewRequesterName(String requesterName) {
        boolean newName = true;
        Organization organization = new Organization();
        organization.setOrganizationName(requesterName);
        organization = orgService.getOrganizationByName(organization, true);

        if (organization == null) {
            newName = true;
        } else {
            newName = false;
        }
        return newName;
    }

    private SampleRequester createSiteRequester(String orgId) {
        SampleRequester requester;
        requester = new SampleRequester();
        requester.setRequesterId(orgId);
        requester.setRequesterTypeId(TableIdService.getInstance().ORGANIZATION_REQUESTER_TYPE_ID);
        requester.setSysUserId(currentUserId);
        return requester;
    }

    public void initSampleData(String sampleXML, String receivedDate, boolean trackPayments,
            SampleOrderItem sampleOrder) {
        createPopulatedSample(receivedDate, sampleOrder);

        addObservations(sampleOrder, trackPayments);

        SampleAddService sampleAddService = new SampleAddService(sampleXML, currentUserId, getSample(), receivedDate);
        setSampleItemsTests(sampleAddService.createSampleTestCollection());
        setSampleAddService(sampleAddService);
    }

    private void addObservations(SampleOrderItem sampleOrder, boolean trackPayments) {
        ObservationHistoryService observationHistoryService = SpringContext.getBean(ObservationHistoryService.class);
        if (trackPayments) {
            createObservation(sampleOrder.getPaymentOptionSelection(),
                    observationHistoryService.getObservationTypeIdForType(ObservationType.PAYMENT_STATUS),
                    ValueType.DICTIONARY);
        }

        createObservation(sampleOrder.getRequestDate(),
                observationHistoryService.getObservationTypeIdForType(ObservationType.REQUEST_DATE), ValueType.LITERAL);
        createObservation(sampleOrder.getNextVisitDate(),
                observationHistoryService.getObservationTypeIdForType(ObservationType.NEXT_VISIT_DATE),
                ValueType.LITERAL);
        createObservation(sampleOrder.getTestLocationCode(),
                observationHistoryService.getObservationTypeIdForType(ObservationType.TEST_LOCATION_CODE),
                ValueType.DICTIONARY);
        createObservation(sampleOrder.getOtherLocationCode(),
                observationHistoryService.getObservationTypeIdForType(ObservationType.TEST_LOCATION_CODE_OTHER),
                ValueType.LITERAL);
        createObservation(sampleOrder.getReferringPatientNumber(),
                observationHistoryService.getObservationTypeIdForType(ObservationType.REFERRERS_PATIENT_ID),
                ValueType.LITERAL);
        if (ConfigurationProperties.getInstance().isPropertyValueEqual(Property.USE_BILLING_REFERENCE_NUMBER, "true")) {
            createObservation(sampleOrder.getBillingReferenceNumber(),
                    observationHistoryService.getObservationTypeIdForType(ObservationType.BILLING_REFERENCE_NUMBER),
                    ValueType.LITERAL);
        }
        if (ConfigurationProperties.getInstance().isPropertyValueEqual(Property.ORDER_PROGRAM, "true")) {
            createObservation(sampleOrder.getProgram(),
                    observationHistoryService.getObservationTypeIdForType(ObservationType.PROGRAM),
                    ValueType.DICTIONARY);
        }
    }

}
