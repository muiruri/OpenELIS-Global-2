package org.openelisglobal.testconfiguration.controller;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.apache.commons.beanutils.PropertyUtils;
import org.hibernate.HibernateException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.openelisglobal.common.controller.BaseController;
import org.openelisglobal.common.services.DisplayListService;
import org.openelisglobal.common.services.DisplayListService.ListType;
import org.openelisglobal.common.util.IdValuePair;
import org.openelisglobal.common.util.StringUtil;
import org.openelisglobal.common.util.validator.GenericValidator;
import org.openelisglobal.dictionary.service.DictionaryService;
import org.openelisglobal.dictionary.valueholder.Dictionary;
import org.openelisglobal.localization.service.LocalizationServiceImpl;
import org.openelisglobal.localization.valueholder.Localization;
import org.openelisglobal.panel.service.PanelService;
import org.openelisglobal.panelitem.valueholder.PanelItem;
import org.openelisglobal.resultlimit.service.ResultLimitService;
import org.openelisglobal.resultlimits.valueholder.ResultLimit;
import org.openelisglobal.spring.util.SpringContext;
import org.openelisglobal.test.service.TestSectionService;
import org.openelisglobal.test.service.TestService;
import org.openelisglobal.test.valueholder.Test;
import org.openelisglobal.test.valueholder.TestSection;
import org.openelisglobal.testconfiguration.form.TestAddForm;
import org.openelisglobal.testconfiguration.service.TestAddService;
import org.openelisglobal.testconfiguration.validator.TestAddFormValidator;
import org.openelisglobal.testresult.service.TestResultService;
import org.openelisglobal.testresult.valueholder.TestResult;
import org.openelisglobal.typeofsample.service.TypeOfSampleService;
import org.openelisglobal.typeofsample.valueholder.TypeOfSample;
import org.openelisglobal.typeofsample.valueholder.TypeOfSampleTest;
import org.openelisglobal.typeoftestresult.service.TypeOfTestResultService;
import org.openelisglobal.typeoftestresult.service.TypeOfTestResultServiceImpl;
import org.openelisglobal.unitofmeasure.service.UnitOfMeasureService;
import org.openelisglobal.unitofmeasure.valueholder.UnitOfMeasure;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class TestAddController extends BaseController {

    @Autowired
    TestAddFormValidator formValidator;
    @Autowired
    private DictionaryService dictionaryService;
    @Autowired
    private PanelService panelService;
    @Autowired
    private TypeOfSampleService typeOfSampleService;
    @Autowired
    private TestResultService testResultService;
    @Autowired
    private UnitOfMeasureService unitOfMeasureService;
    @Autowired
    private TestAddService testAddService;
    @Autowired
    private TestSectionService testSectionService;
    @Autowired
    private TestService testService;

    @RequestMapping(value = "/TestAdd", method = RequestMethod.GET)
    public ModelAndView showTestAdd(HttpServletRequest request) {

        System.out.println("Hibernate Version: " + org.hibernate.Version.getVersionString());

        TestAddForm form = new TestAddForm();

        List<IdValuePair> allSampleTypesList = new ArrayList<>();
        allSampleTypesList.addAll(DisplayListService.getInstance().getList(ListType.SAMPLE_TYPE_ACTIVE));
        allSampleTypesList.addAll(DisplayListService.getInstance().getList(ListType.SAMPLE_TYPE_INACTIVE));
        try {
            PropertyUtils.setProperty(form, "sampleTypeList", allSampleTypesList);
            PropertyUtils.setProperty(form, "panelList", DisplayListService.getInstance().getList(ListType.PANELS));
            PropertyUtils.setProperty(form, "resultTypeList",
                    DisplayListService.getInstance().getList(ListType.RESULT_TYPE_LOCALIZED));
            PropertyUtils.setProperty(form, "uomList",
                    DisplayListService.getInstance().getList(ListType.UNIT_OF_MEASURE));
            PropertyUtils.setProperty(form, "labUnitList",
                    DisplayListService.getInstance().getList(ListType.TEST_SECTION));
            PropertyUtils.setProperty(form, "ageRangeList",
                    SpringContext.getBean(ResultLimitService.class).getPredefinedAgeRanges());
            PropertyUtils.setProperty(form, "dictionaryList",
                    DisplayListService.getInstance().getList(ListType.DICTIONARY_TEST_RESULTS));
            PropertyUtils.setProperty(form, "groupedDictionaryList", createGroupedDictionaryList());
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return findForward(FWD_SUCCESS, form);
    }

    @RequestMapping(value = "/TestAdd", method = RequestMethod.POST)
    public ModelAndView postTestAdd(HttpServletRequest request, @ModelAttribute("form") @Valid TestAddForm form,
            BindingResult result) {
        formValidator.validate(form, result);
        if (result.hasErrors()) {
            saveErrors(result);
            return findForward(FWD_FAIL_INSERT, form);
        }

        String currentUserId = getSysUserId(request);
        String jsonString = (form.getString("jsonWad"));

        JSONParser parser = new JSONParser();

        JSONObject obj = null;
        try {
            obj = (JSONObject) parser.parse(jsonString);
        } catch (ParseException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        TestAddParams testAddParams = extractTestAddParms(obj, parser);
        List<TestSet> testSets = createTestSets(testAddParams);
        Localization nameLocalization = createNameLocalization(testAddParams);
        Localization reportingNameLocalization = createReportingNameLocalization(testAddParams);

        try {
            testAddService.addTests(testSets, nameLocalization, reportingNameLocalization, currentUserId);
        } catch (HibernateException lre) {
            lre.printStackTrace();
        }

        testService.refreshTestNames();
        SpringContext.getBean(TypeOfSampleService.class).clearCache();

        return findForward(FWD_SUCCESS_INSERT, form);
    }

    private Localization createNameLocalization(TestAddParams testAddParams) {
        return LocalizationServiceImpl.createNewLocalization(testAddParams.testNameEnglish,
                testAddParams.testNameFrench, LocalizationServiceImpl.LocalizationType.TEST_NAME);
    }

    private Localization createReportingNameLocalization(TestAddParams testAddParams) {
        return LocalizationServiceImpl.createNewLocalization(testAddParams.testReportNameEnglish,
                testAddParams.testReportNameFrench, LocalizationServiceImpl.LocalizationType.REPORTING_TEST_NAME);
    }

    private List<TestSet> createTestSets(TestAddParams testAddParams) {
        Double lowValid = null;
        Double highValid = null;
        String significantDigits = testAddParams.significantDigits;
        boolean numericResults = TypeOfTestResultServiceImpl.ResultType.isNumericById(testAddParams.resultTypeId);
        boolean dictionaryResults = TypeOfTestResultServiceImpl.ResultType
                .isDictionaryVarientById(testAddParams.resultTypeId);
        List<TestSet> testSets = new ArrayList<>();
        UnitOfMeasure uom = null;
        if (!GenericValidator.isBlankOrNull(testAddParams.uomId) || "0".equals(testAddParams.uomId)) {
            uom = unitOfMeasureService.getUnitOfMeasureById(testAddParams.uomId);
        }
        TestSection testSection = testSectionService.get(testAddParams.testSectionId);

        if (numericResults) {
            lowValid = StringUtil.doubleWithInfinity(testAddParams.lowValid);
            highValid = StringUtil.doubleWithInfinity(testAddParams.highValid);
        }
        // The number of test sets depend on the number of sampleTypes
        for (int i = 0; i < testAddParams.sampleList.size(); i++) {
            TypeOfSample typeOfSample = typeOfSampleService
                    .getTypeOfSampleById(testAddParams.sampleList.get(i).sampleTypeId);
            if (typeOfSample == null) {
                continue;
            }
            TestSet testSet = new TestSet();
            Test test = new Test();
            test.setUnitOfMeasure(uom);
            test.setLoinc(testAddParams.loinc);
            test.setDescription(testAddParams.testNameEnglish + "(" + typeOfSample.getDescription() + ")");
            test.setTestName(testAddParams.testNameEnglish);
            test.setLocalCode(testAddParams.testNameEnglish);
            test.setIsActive(testAddParams.active);
            test.setOrderable("Y".equals(testAddParams.orderable));
            test.setIsReportable("N");
            test.setTestSection(testSection);
            test.setGuid(String.valueOf(UUID.randomUUID()));
            ArrayList<String> orderedTests = testAddParams.sampleList.get(i).orderedTests;
            for (int j = 0; j < orderedTests.size(); j++) {
                if ("0".equals(orderedTests.get(j))) {
                    test.setSortOrder(String.valueOf(j));
                } else {
                    Test orderedTest = SpringContext.getBean(TestService.class).get(orderedTests.get(j));
                    orderedTest.setSortOrder(String.valueOf(j));
                    testSet.sortedTests.add(orderedTest);
                }
            }

            testSet.test = test;

            TypeOfSampleTest typeOfSampleTest = new TypeOfSampleTest();
            typeOfSampleTest.setTypeOfSampleId(typeOfSample.getId());
            testSet.sampleTypeTest = typeOfSampleTest;

            createPanelItems(testSet.panelItems, testAddParams);
            createTestResults(testSet.testResults, significantDigits, testAddParams);
            if (numericResults) {
                testSet.resultLimits = createResultLimits(lowValid, highValid, testAddParams);
            } else if (dictionaryResults) {
                testSet.resultLimits = createDictionaryResultLimit(testAddParams);
            }

            testSets.add(testSet);
        }

        return testSets;
    }

    private ArrayList<ResultLimit> createDictionaryResultLimit(TestAddParams testAddParams) {
        ArrayList<ResultLimit> resultLimits = new ArrayList<>();
        if (!GenericValidator.isBlankOrNull(testAddParams.dictionaryReferenceId)) {
            ResultLimit limit = new ResultLimit();
            limit.setResultTypeId(testAddParams.resultTypeId);
            limit.setDictionaryNormalId(testAddParams.dictionaryReferenceId);
            resultLimits.add(limit);
        }

        return resultLimits;
    }

    private ArrayList<ResultLimit> createResultLimits(Double lowValid, Double highValid, TestAddParams testAddParams) {
        ArrayList<ResultLimit> resultLimits = new ArrayList<>();
        for (ResultLimitParams params : testAddParams.limits) {
            ResultLimit limit = new ResultLimit();
            limit.setResultTypeId(testAddParams.resultTypeId);
            limit.setGender(params.gender);
            limit.setMinAge(StringUtil.doubleWithInfinity(params.lowAge));
            limit.setMaxAge(StringUtil.doubleWithInfinity(params.highAge));
            limit.setLowNormal(StringUtil.doubleWithInfinity(params.lowLimit));
            limit.setHighNormal(StringUtil.doubleWithInfinity(params.highLimit));
            limit.setLowValid(lowValid);
            limit.setHighValid(highValid);
            resultLimits.add(limit);
        }

        return resultLimits;
    }

    private void createPanelItems(ArrayList<PanelItem> panelItems, TestAddParams testAddParams) {
        for (String panelId : testAddParams.panelList) {
            PanelItem panelItem = new PanelItem();
            panelItem.setPanel(panelService.getPanelById(panelId));
            panelItems.add(panelItem);
        }
    }

    private void createTestResults(ArrayList<TestResult> testResults, String significantDigits,
            TestAddParams testAddParams) {
        TypeOfTestResultServiceImpl.ResultType type = SpringContext.getBean(TypeOfTestResultService.class)
                .getResultTypeById(testAddParams.resultTypeId);

        if (TypeOfTestResultServiceImpl.ResultType.isTextOnlyVariant(type)
                || TypeOfTestResultServiceImpl.ResultType.isNumeric(type)) {
            TestResult testResult = new TestResult();
            testResult.setTestResultType(type.getCharacterValue());
            testResult.setSortOrder("1");
            testResult.setIsActive(true);
            testResult.setSignificantDigits(significantDigits);
            testResults.add(testResult);
        } else if (TypeOfTestResultServiceImpl.ResultType.isDictionaryVariant(type.getCharacterValue())) {
            int sortOrder = 10;
            for (DictionaryParams params : testAddParams.dictionaryParamList) {
                TestResult testResult = new TestResult();
                testResult.setTestResultType(type.getCharacterValue());
                testResult.setSortOrder(String.valueOf(sortOrder));
                sortOrder += 10;
                testResult.setIsActive(true);
                testResult.setValue(params.dictionaryId);
                testResult.setIsQuantifiable(params.isQuantifiable);
                testResults.add(testResult);
            }
        }
    }

    private TestAddParams extractTestAddParms(JSONObject obj, JSONParser parser) {
        TestAddParams testAddParams = new TestAddParams();
        try {

            testAddParams.testNameEnglish = (String) obj.get("testNameEnglish");
            testAddParams.testNameFrench = (String) obj.get("testNameFrench");
            testAddParams.testReportNameEnglish = (String) obj.get("testReportNameEnglish");
            testAddParams.testReportNameFrench = (String) obj.get("testReportNameFrench");
            testAddParams.testSectionId = (String) obj.get("testSection");
            testAddParams.dictionaryReferenceId = (String) obj.get("dictionaryReference");
            extractPanels(obj, parser, testAddParams);
            testAddParams.uomId = (String) obj.get("uom");
            testAddParams.loinc = (String) obj.get("loinc");
            testAddParams.resultTypeId = (String) obj.get("resultType");
            extractSampleTypes(obj, parser, testAddParams);
            testAddParams.active = (String) obj.get("active");
            testAddParams.orderable = (String) obj.get("orderable");
            if (TypeOfTestResultServiceImpl.ResultType.isNumericById(testAddParams.resultTypeId)) {
                testAddParams.lowValid = (String) obj.get("lowValid");
                testAddParams.highValid = (String) obj.get("highValid");
                testAddParams.significantDigits = (String) obj.get("significantDigits");
                extractLimits(obj, parser, testAddParams);
            } else if (TypeOfTestResultServiceImpl.ResultType.isDictionaryVarientById(testAddParams.resultTypeId)) {
                String dictionary = (String) obj.get("dictionary");
                JSONArray dictionaryArray = (JSONArray) parser.parse(dictionary);
                for (int i = 0; i < dictionaryArray.size(); i++) {
                    DictionaryParams params = new DictionaryParams();
                    params.dictionaryId = (String) ((JSONObject) dictionaryArray.get(i)).get("value");
                    params.isQuantifiable = "Y".equals(((JSONObject) dictionaryArray.get(i)).get("qualified"));
                    testAddParams.dictionaryParamList.add(params);
                }
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return testAddParams;
    }

    private void extractLimits(JSONObject obj, JSONParser parser, TestAddParams testAddParams) throws ParseException {
        String lowAge = "0";
        String limits = (String) obj.get("resultLimits");
        JSONArray limitArray = (JSONArray) parser.parse(limits);
        for (int i = 0; i < limitArray.size(); i++) {
            ResultLimitParams params = new ResultLimitParams();
            Boolean gender = (Boolean) ((JSONObject) limitArray.get(i)).get("gender");
            if (gender) {
                params.gender = "M";
            }
            String highAge = (String) (((JSONObject) limitArray.get(i)).get("highAgeRange"));
            params.displayRange = (String) (((JSONObject) limitArray.get(i)).get("reportingRange"));
            params.lowLimit = (String) (((JSONObject) limitArray.get(i)).get("lowNormal"));
            params.highLimit = (String) (((JSONObject) limitArray.get(i)).get("highNormal"));
            params.lowAge = lowAge;
            params.highAge = highAge;
            testAddParams.limits.add(params);

            if (gender) {
                params = new ResultLimitParams();
                params.gender = "F";
                params.displayRange = (String) (((JSONObject) limitArray.get(i)).get("reportingRangeFemale"));
                params.lowLimit = (String) (((JSONObject) limitArray.get(i)).get("lowNormalFemale"));
                params.highLimit = (String) (((JSONObject) limitArray.get(i)).get("highNormalFemale"));
                params.lowAge = lowAge;
                params.highAge = highAge;
                testAddParams.limits.add(params);
            }

            lowAge = highAge;
        }
    }

    private void extractPanels(JSONObject obj, JSONParser parser, TestAddParams testAddParams) throws ParseException {
        String panels = (String) obj.get("panels");
        JSONArray panelArray = (JSONArray) parser.parse(panels);

        for (int i = 0; i < panelArray.size(); i++) {
            testAddParams.panelList.add((String) (((JSONObject) panelArray.get(i)).get("id")));
        }

    }

    private void extractSampleTypes(JSONObject obj, JSONParser parser, TestAddParams testAddParams)
            throws ParseException {
        String sampleTypes = (String) obj.get("sampleTypes");
        JSONArray sampleTypeArray = (JSONArray) parser.parse(sampleTypes);

        for (int i = 0; i < sampleTypeArray.size(); i++) {
            SampleTypeListAndTestOrder sampleTypeTests = new SampleTypeListAndTestOrder();
            sampleTypeTests.sampleTypeId = (String) (((JSONObject) sampleTypeArray.get(i)).get("typeId"));

            JSONArray testArray = (JSONArray) (((JSONObject) sampleTypeArray.get(i)).get("tests"));
            for (int j = 0; j < testArray.size(); j++) {
                sampleTypeTests.orderedTests.add(String.valueOf(((JSONObject) testArray.get(j)).get("id")));
            }
            testAddParams.sampleList.add(sampleTypeTests);
        }
    }

    private List<List<IdValuePair>> createGroupedDictionaryList() {
        List<TestResult> testResults = getSortedTestResults();

        HashSet<String> dictionaryIdGroups = getDictionaryIdGroups(testResults);

        return getGroupedDictionaryPairs(dictionaryIdGroups);
    }

    @SuppressWarnings("unchecked")
    private List<TestResult> getSortedTestResults() {
        List<TestResult> testResults = testResultService.getAllTestResults();

        Collections.sort(testResults, new Comparator<TestResult>() {
            @Override
            public int compare(TestResult o1, TestResult o2) {
                int result = o1.getTest().getId().compareTo(o2.getTest().getId());

                if (result != 0) {
                    return result;
                }

                return GenericValidator.isBlankOrNull(o1.getSortOrder()) ? 0
                        : Integer.parseInt(o1.getSortOrder()) - Integer.parseInt(o2.getSortOrder());
            }
        });
        return testResults;
    }

    private HashSet<String> getDictionaryIdGroups(List<TestResult> testResults) {
        HashSet<String> dictionaryIdGroups = new HashSet<>();
        String currentTestId = null;
        String dictionaryIdGroup = null;
        for (TestResult testResult : testResults) {
            if (TypeOfTestResultServiceImpl.ResultType.isDictionaryVariant(testResult.getTestResultType())) {
                if (testResult.getTest().getId().equals(currentTestId)) {
                    dictionaryIdGroup += "," + testResult.getValue();
                } else {
                    currentTestId = testResult.getTest().getId();
                    if (dictionaryIdGroup != null) {
                        dictionaryIdGroups.add(dictionaryIdGroup);
                    }

                    dictionaryIdGroup = testResult.getValue();
                }
            }
        }

        if (dictionaryIdGroup != null) {
            dictionaryIdGroups.add(dictionaryIdGroup);
        }

        return dictionaryIdGroups;
    }

    private List<List<IdValuePair>> getGroupedDictionaryPairs(HashSet<String> dictionaryIdGroups) {
        List<List<IdValuePair>> groups = new ArrayList<>();
        for (String group : dictionaryIdGroups) {
            List<IdValuePair> dictionaryPairs = new ArrayList<>();
            for (String id : group.split(",")) {
                Dictionary dictionary = dictionaryService.getDictionaryById(id);
                if (dictionary != null) {
                    dictionaryPairs.add(new IdValuePair(id, dictionary.getLocalizedName()));
                }
            }
            groups.add(dictionaryPairs);
        }

        Collections.sort(groups, new Comparator<List<IdValuePair>>() {
            @Override
            public int compare(List<IdValuePair> o1, List<IdValuePair> o2) {
                return o1.size() - o2.size();
            }
        });
        return groups;
    }

    @Override
    protected String findLocalForward(String forward) {
        if (FWD_SUCCESS.equals(forward)) {
            return "testAddDefinition";
        } else if (FWD_SUCCESS_INSERT.equals(forward)) {
            return "redirect:/TestAdd.do";
        } else if (FWD_FAIL_INSERT.equals(forward)) {
            return "testAddDefinition";
        } else {
            return "PageNotFound";
        }
    }

    @Override
    protected String getPageTitleKey() {
        return null;
    }

    @Override
    protected String getPageSubtitleKey() {
        return null;
    }

    public class TestAddParams {
        String testId;
        public String testNameEnglish;
        public String testNameFrench;
        public String testReportNameEnglish;
        public String testReportNameFrench;
        String testSectionId;
        ArrayList<String> panelList = new ArrayList<>();
        String uomId;
        String loinc;
        String resultTypeId;
        ArrayList<SampleTypeListAndTestOrder> sampleList = new ArrayList<>();
        String active;
        String orderable;
        String lowValid;
        String highValid;
        public String significantDigits;
        String dictionaryReferenceId;
        ArrayList<ResultLimitParams> limits = new ArrayList<>();
        public ArrayList<DictionaryParams> dictionaryParamList = new ArrayList<>();
    }

    public class TestSet {
        public Test test;
        public TypeOfSampleTest sampleTypeTest;
        public ArrayList<Test> sortedTests = new ArrayList<>();
        public ArrayList<PanelItem> panelItems = new ArrayList<>();
        public ArrayList<TestResult> testResults = new ArrayList<>();
        public ArrayList<ResultLimit> resultLimits = new ArrayList<>();
    }

    public class SampleTypeListAndTestOrder {
        String sampleTypeId;
        ArrayList<String> orderedTests = new ArrayList<>();
    }

    public class ResultLimitParams {
        String gender;
        String lowAge;
        String highAge;
        String lowLimit;
        String highLimit;
        String displayRange;
    }

    public class DictionaryParams {
        public String dictionaryId;
        public boolean isQuantifiable = false;
    }
}
