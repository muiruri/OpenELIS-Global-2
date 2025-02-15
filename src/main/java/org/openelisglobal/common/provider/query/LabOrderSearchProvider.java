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
package org.openelisglobal.common.provider.query;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.validator.GenericValidator;
import org.openelisglobal.common.services.StatusService;
import org.openelisglobal.common.services.StatusService.ExternalOrderStatus;
import org.openelisglobal.common.util.XMLUtil;
import org.openelisglobal.dataexchange.order.valueholder.ElectronicOrder;
import org.openelisglobal.dataexchange.service.order.ElectronicOrderService;
import org.openelisglobal.internationalization.MessageUtil;
import org.openelisglobal.panel.service.PanelService;
import org.openelisglobal.panel.valueholder.Panel;
import org.openelisglobal.panelitem.service.PanelItemService;
import org.openelisglobal.panelitem.valueholder.PanelItem;
import org.openelisglobal.patient.service.PatientService;
import org.openelisglobal.patient.valueholder.Patient;
import org.openelisglobal.person.service.PersonService;
import org.openelisglobal.spring.util.SpringContext;
import org.openelisglobal.test.service.TestService;
import org.openelisglobal.test.valueholder.Test;
import org.openelisglobal.typeofsample.service.TypeOfSampleService;
import org.openelisglobal.typeofsample.service.TypeOfSampleTestService;
import org.openelisglobal.typeofsample.valueholder.TypeOfSample;
import org.openelisglobal.typeofsample.valueholder.TypeOfSampleTest;

import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.model.v251.group.OML_O21_OBSERVATION_REQUEST;
import ca.uhn.hl7v2.model.v251.group.OML_O21_ORDER_PRIOR;
import ca.uhn.hl7v2.model.v251.message.OML_O21;
import ca.uhn.hl7v2.model.v251.segment.OBX;
import ca.uhn.hl7v2.model.v251.segment.ORC;
import ca.uhn.hl7v2.parser.Parser;

public class LabOrderSearchProvider extends BaseQueryProvider {
//	private TestDAO testDAO = new TestDAOImpl();
//	private PanelDAO panelDAO = new PanelDAOImpl();
//	private PanelItemDAO panelItemDAO = new PanelItemDAOImpl();
//	private TypeOfSampleTestDAO typeOfSampleTest = new TypeOfSampleTestDAOImpl();

    protected TestService testService = SpringContext.getBean(TestService.class);
    protected PanelService panelService = SpringContext.getBean(PanelService.class);
    protected PanelItemService panelItemService = SpringContext.getBean(PanelItemService.class);
    protected TypeOfSampleTestService typeOfSampleTestService = SpringContext.getBean(TypeOfSampleTestService.class);
    protected ElectronicOrderService electronicOrderService = SpringContext.getBean(ElectronicOrderService.class);
    private TypeOfSampleService typeOfSampleService = SpringContext.getBean(TypeOfSampleService.class);

    private Map<TypeOfSample, PanelTestLists> typeOfSampleMap;
    private Map<Panel, List<TypeOfSample>> panelSampleTypesMap;
    private Map<String, List<TestSampleType>> testNameTestSampleTypeMap;

    private static final String NOT_FOUND = "Not Found";
    private static final String CANCELED = "Canceled";
    private static final String REALIZED = "Realized";
    private static final String PROVIDER_FIRST_NAME = "firstName";
    private static final String PROVIDER_LAST_NAME = "lastName";
    private static final String PROVIDER_PHONE = "phone";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String orderNumber = request.getParameter("orderNumber");

        StringBuilder xml = new StringBuilder();

        String result = createSearchResultXML(orderNumber, xml);

        if (!result.equals(VALID)) {
            if (result.equals(NOT_FOUND)) {
                result = MessageUtil.getMessage("electronic.order.message.orderNotFound");
            } else if (result.equals(CANCELED)) {
                result = MessageUtil.getMessage("electronic.order.message.canceled");
            } else if (result.equals(REALIZED)) {
                result = MessageUtil.getMessage("electronic.order.message.realized");
            }
            result += "\n\n" + MessageUtil.getMessage("electronic.order.message.suggestion");
            xml.append("empty");
        }

        ajaxServlet.sendData(xml.toString(), result, request, response);

    }

    private String createSearchResultXML(String orderNumber, StringBuilder xml) {

        String success = VALID;

        List<ElectronicOrder> eOrders = electronicOrderService.getElectronicOrdersByExternalId(orderNumber);
        if (eOrders.isEmpty()) {
            return NOT_FOUND;
        }

        ElectronicOrder eOrder = eOrders.get(eOrders.size() - 1);
        ExternalOrderStatus eOrderStatus = StatusService.getInstance()
                .getExternalOrderStatusForID(eOrder.getStatusId());

        if (eOrderStatus == ExternalOrderStatus.Cancelled) {
            return CANCELED;
        } else if (eOrderStatus == ExternalOrderStatus.Realized) {
            return REALIZED;
        }

        String patientGuid = getPatientGuid(eOrder);
        createOrderXML(eOrder.getData(), patientGuid, xml);

        return success;
    }

    private String getPatientGuid(ElectronicOrder eOrder) {
        PatientService patientPatientService = SpringContext.getBean(PatientService.class);
        PersonService personService = SpringContext.getBean(PersonService.class);
        personService.getData(eOrder.getPatient().getPerson());
        return patientPatientService.getGUID(eOrder.getPatient());
    }

    private void createOrderXML(String orderMessage, String patientGuid, StringBuilder xml) {
        List<Request> tests = new ArrayList<>();
        List<Request> panels = new ArrayList<>();
        Map<String, String> requesterValuesMap = new HashMap<>();

        getTestsAndPanels(tests, panels, orderMessage, requesterValuesMap);
        createMaps(tests, panels);
        xml.append("<order>");
        addRequester(xml, requesterValuesMap);
        addPatientGuid(xml, patientGuid);
        addSampleTypes(xml);
        addCrossPanels(xml);
        addCrosstests(xml);
        addAlerts(xml, patientGuid);
        xml.append("</order>");
    }

    private void addRequester(StringBuilder xml, Map<String, String> requesterValuesMap) {
        xml.append("<requester>");
        XMLUtil.appendKeyValue(PROVIDER_FIRST_NAME, requesterValuesMap.get(PROVIDER_FIRST_NAME), xml);
        XMLUtil.appendKeyValue(PROVIDER_LAST_NAME, requesterValuesMap.get(PROVIDER_LAST_NAME), xml);
        XMLUtil.appendKeyValue(PROVIDER_PHONE, requesterValuesMap.get(PROVIDER_PHONE), xml);
        xml.append("</requester>");
    }

    private void getTestsAndPanels(List<Request> tests, List<Request> panels, String orderMessage,
            Map<String, String> requesterValuesMap) {
        HapiContext context = new DefaultHapiContext();
        Parser p = context.getGenericParser();
        try {
            OML_O21 hapiMsg = (OML_O21) p.parse(orderMessage);

            ORC commonOrderSegment = hapiMsg.getORDER().getORC();

            requesterValuesMap.put(PROVIDER_PHONE,
                    commonOrderSegment.getCallBackPhoneNumber(0).getXtn12_UnformattedTelephoneNumber().getValue());
            requesterValuesMap.put(PROVIDER_LAST_NAME,
                    commonOrderSegment.getOrderingProvider(0).getFamilyName().getSurname().getValue());
            requesterValuesMap.put(PROVIDER_FIRST_NAME,
                    commonOrderSegment.getOrderingProvider(0).getGivenName().getValue());

            OML_O21_OBSERVATION_REQUEST orderRequest = hapiMsg.getORDER().getOBSERVATION_REQUEST();

            addToTestOrPanel(tests, panels, commonOrderSegment, orderRequest.getOBSERVATION().getOBX());

            List<OML_O21_ORDER_PRIOR> priorOrders = orderRequest.getPRIOR_RESULT().getORDER_PRIORAll();
            for (OML_O21_ORDER_PRIOR priorOrder : priorOrders) {
                addToTestOrPanel(tests, panels, commonOrderSegment, priorOrder.getOBSERVATION_PRIOR().getOBX());
            }

        } catch (HL7Exception e) {
            e.printStackTrace();
        }
    }

    private void addToTestOrPanel(List<Request> tests, List<Request> panels, ORC orc, OBX obx) {
        String loinc = orc.getOrderType().getIdentifier().toString();
        String testName = testService.getActiveTestsByLoinc(loinc).get(0).getName();
        tests.add(new Request(testName, loinc,
                typeOfSampleService.getTypeOfSampleNameForId(testService.getActiveTestsByLoinc(loinc).get(0).getId())));
    }

    private void createMaps(List<Request> testRequests, List<Request> panelNames) {
        typeOfSampleMap = new HashMap<>();
        panelSampleTypesMap = new HashMap<>();
        testNameTestSampleTypeMap = new HashMap<>();

        createMapsForTests(testRequests);

        createMapsForPanels(panelNames);
    }

    private void addPatientGuid(StringBuilder xml, String patientGuid) {
        xml.append("<patient>");
        XMLUtil.appendKeyValue("guid", patientGuid, xml);
        xml.append("</patient>");
    }

    private void createMapsForTests(List<Request> testRequests) {
        for (Request testRequest : testRequests) {
            List<Test> tests = testService.getActiveTestsByLoinc(testRequest.getLoinc());

            Test singleTest = tests.get(0);
            TypeOfSample singleSampleType = typeOfSampleService.getTypeOfSampleForTest(singleTest.getId());
            boolean hasSingleSampleType = tests.size() == 1;

            if (tests.size() > 1) {
                if (!GenericValidator.isBlankOrNull(testRequest.getSampleType())) {
                    for (Test test : tests) {
                        TypeOfSample typeOfSample = typeOfSampleService.getTypeOfSampleForTest(test.getId());
                        if (typeOfSample.getDescription().equals(testRequest.getSampleType())) {
                            hasSingleSampleType = true;
                            singleSampleType = typeOfSample;
                            singleTest = test;
                            break;
                        }
                    }
                }

                if (!hasSingleSampleType) {
                    List<TestSampleType> testSampleTypeList = testNameTestSampleTypeMap.get(testRequest.getName());

                    if (testSampleTypeList == null) {
                        testSampleTypeList = new ArrayList<>();
                        testNameTestSampleTypeMap.put(testRequest.getName(), testSampleTypeList);
                    }

                    for (Test test : tests) {
                        testSampleTypeList.add(
                                new TestSampleType(test, typeOfSampleService.getTypeOfSampleForTest(test.getId())));
                    }
                }
            }

            if (hasSingleSampleType) {
                PanelTestLists panelTestLists = typeOfSampleMap.get(singleSampleType);
                if (panelTestLists == null) {
                    panelTestLists = new PanelTestLists();
                    typeOfSampleMap.put(singleSampleType, panelTestLists);
                }

                panelTestLists.addTest(singleTest);
            }
        }
    }

    private void createMapsForPanels(List<Request> panelRequests) {
        for (Request panelRequest : panelRequests) {
            Panel panel = panelService.getPanelByName(panelRequest.getName());

            if (panel != null) {
                List<TypeOfSample> typeOfSamples = typeOfSampleService.getTypeOfSampleForPanelId(panel.getId());
                boolean hasSingleSampleType = typeOfSamples.size() == 1;
                TypeOfSample singleTypeOfSample = typeOfSamples.get(0);

                if (!GenericValidator.isBlankOrNull(panelRequest.getSampleType())) {
                    if (typeOfSamples.size() > 1) {
                        for (TypeOfSample typeOfSample : typeOfSamples) {
                            if (typeOfSample.getDescription().equals(panelRequest.getSampleType())) {
                                hasSingleSampleType = true;
                                singleTypeOfSample = typeOfSample;
                                break;
                            }
                        }
                    }
                }

                if (hasSingleSampleType) {
                    PanelTestLists panelTestLists = typeOfSampleMap.get(singleTypeOfSample);
                    if (panelTestLists == null) {
                        panelTestLists = new PanelTestLists();
                        typeOfSampleMap.put(singleTypeOfSample, panelTestLists);
                    }

                    panelTestLists.addPanel(panel);
                } else {
                    panelSampleTypesMap.put(panel, typeOfSamples);
                }
            }
        }
    }

    private void addSampleTypes(StringBuilder xml) {
        xml.append("<sampleTypes>");
        for (TypeOfSample typeOfSample : typeOfSampleMap.keySet()) {
            if (typeOfSample != null) {
                addSampleType(xml, typeOfSample, typeOfSampleMap.get(typeOfSample));
            }
        }
        xml.append("</sampleTypes>");
    }

    private void addSampleType(StringBuilder xml, TypeOfSample typeOfSample, PanelTestLists panelTestLists) {
        xml.append("<sampleType>");
        XMLUtil.appendKeyValue("id", typeOfSample.getId(), xml);
        XMLUtil.appendKeyValue("name", typeOfSample.getLocalizedName(), xml);
        addPanels(xml, panelTestLists.getPanels(), typeOfSample.getId());
        addTests(xml, "tests", panelTestLists.getTests());
        xml.append("</sampleType>");
    }

    private void addPanels(StringBuilder xml, List<Panel> panels, String sampleTypeId) {
        xml.append("<panels>");
        for (Panel panel : panels) {
            xml.append("<panel>");
            XMLUtil.appendKeyValue("id", panel.getId(), xml);
            XMLUtil.appendKeyValue("name", panel.getLocalizedName(), xml);
            addPanelTests(xml, panel.getId(), sampleTypeId);
            xml.append("</panel>");
        }
        xml.append("</panels>");
    }

    private void addPanelTests(StringBuilder xml, String panelId, String sampleTypeId) {
        List<Test> panelTests = getTestsForPanelAndType(panelId, sampleTypeId);
        addTests(xml, "panelTests", panelTests);
    }

    private List<Test> getTestsForPanelAndType(String panelId, String sampleTypeId) {
        List<TypeOfSampleTest> sampleTestList = typeOfSampleTestService.getTypeOfSampleTestsForSampleType(sampleTypeId);
        List<Integer> testList = new ArrayList<>();
        for (TypeOfSampleTest typeTest : sampleTestList) {
            testList.add(Integer.parseInt(typeTest.getTestId()));
        }

        List<PanelItem> panelList = panelItemService.getPanelItemsForPanelAndItemList(panelId, testList);
        List<Test> tests = new ArrayList<>();
        for (PanelItem item : panelList) {
            tests.add(item.getTest());
        }

        return tests;
    }

    private void addTests(StringBuilder xml, String parent, List<Test> tests) {
        xml.append("<");
        xml.append(parent);
        xml.append(">");
        for (Test test : tests) {
            xml.append("<test>");
            XMLUtil.appendKeyValue("id", test.getId(), xml);
            XMLUtil.appendKeyValue("name", test.getLocalizedName(), xml);
            xml.append("</test>");
        }
        xml.append("</");
        xml.append(parent);
        xml.append(">");
    }

    private void addCrossPanels(StringBuilder xml) {
        xml.append("<crosspanels>");
        for (Panel panel : panelSampleTypesMap.keySet()) {
            addCrosspanel(xml, panel, panelSampleTypesMap.get(panel));
        }

        xml.append("</crosspanels>");
    }

    private void addCrosspanel(StringBuilder xml, Panel panel, List<TypeOfSample> typeOfSampleList) {
        xml.append("<crosspanel>");
        XMLUtil.appendKeyValue("name", panel.getLocalizedName(), xml);
        XMLUtil.appendKeyValue("id", panel.getId(), xml);
        addPanelCrosssampletypes(xml, typeOfSampleList);
        xml.append("</crosspanel>");
    }

    private void addPanelCrosssampletypes(StringBuilder xml, List<TypeOfSample> typeOfSampleList) {
        xml.append("<crosssampletypes>");
        for (TypeOfSample typeOfSample : typeOfSampleList) {
            addCrosspanelTypeOfSample(xml, typeOfSample);
        }
        xml.append("</crosssampletypes>");
    }

    private void addCrosspanelTypeOfSample(StringBuilder xml, TypeOfSample typeOfSample) {
        xml.append("<crosssampletype>");
        XMLUtil.appendKeyValue("id", typeOfSample.getId(), xml);
        XMLUtil.appendKeyValue("name", typeOfSample.getLocalizedName(), xml);
        xml.append("</crosssampletype>");
    }

    private void addCrosstests(StringBuilder xml) {
        xml.append("<crosstests>");
        for (String testName : testNameTestSampleTypeMap.keySet()) {
            addCrosstestForTestName(xml, testName, testNameTestSampleTypeMap.get(testName));
        }
        xml.append("</crosstests>");

    }

    private void addCrosstestForTestName(StringBuilder xml, String testName, List<TestSampleType> list) {
        xml.append("<crosstest>");
        XMLUtil.appendKeyValue("name", testName, xml);
        xml.append("<crosssampletypes>");
        for (TestSampleType testSampleType : list) {
            addTestCrosssampleType(xml, testSampleType);
        }
        xml.append("</crosssampletypes>");
        xml.append("</crosstest>");
    }

    private void addTestCrosssampleType(StringBuilder xml, TestSampleType testSampleType) {
        xml.append("<crosssampletype>");
        XMLUtil.appendKeyValue("id", testSampleType.getSampleType().getId(), xml);
        XMLUtil.appendKeyValue("name", testSampleType.getSampleType().getLocalizedName(), xml);
        XMLUtil.appendKeyValue("testid", testSampleType.getTest().getId(), xml);
        xml.append("</crosssampletype>");
    }

    private void addAlerts(StringBuilder xml, String patientGuid) {
        PatientService patientService = SpringContext.getBean(PatientService.class);
        Patient patient = patientService.getPatientForGuid(patientGuid);
        if (GenericValidator.isBlankOrNull(patientService.getEnteredDOB(patient))
                || GenericValidator.isBlankOrNull(patientService.getGender(patient))) {
            XMLUtil.appendKeyValue("user_alert", MessageUtil.getMessage("electroinic.order.warning.missingPatientInfo"),
                    xml);
        }
    }

    public class PanelTestLists {
        private List<Test> tests = new ArrayList<>();
        private List<Panel> panels = new ArrayList<>();

        public List<Test> getTests() {
            return tests;
        }

        public List<Panel> getPanels() {
            return panels;
        }

        public void addPanel(Panel panel) {
            if (panel != null) {
                panels.add(panel);
            }
        }

        public void addTest(Test test) {
            if (test != null) {
                tests.add(test);
            }
        }

    }

    public class TestSampleType {
        private Test test;
        private TypeOfSample sampleType;

        public TestSampleType(Test test, TypeOfSample sampleType) {
            this.test = test;
            this.sampleType = sampleType;
        }

        public Test getTest() {
            return test;
        }

        public TypeOfSample getSampleType() {
            return sampleType;
        }
    }

    private class Request {
        private String name;
        private String loinc;
        private String sampleType;

        public Request(String name, String loinc, String sampleType) {
            this.name = name;
            this.loinc = loinc;
            this.sampleType = sampleType;
        }

        public String getSampleType() {
            return sampleType;
        }

        public String getLoinc() {
            return loinc;
        }

        public String getName() {
            return name;
        }

    }
}
