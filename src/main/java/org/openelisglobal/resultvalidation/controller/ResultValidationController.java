package org.openelisglobal.resultvalidation.controller;

import static org.apache.commons.validator.GenericValidator.isBlankOrNull;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.validator.GenericValidator;
import org.openelisglobal.analysis.service.AnalysisService;
import org.openelisglobal.analysis.valueholder.Analysis;
import org.openelisglobal.common.exception.LIMSRuntimeException;
import org.openelisglobal.common.log.LogEvent;
import org.openelisglobal.common.services.DisplayListService;
import org.openelisglobal.common.services.DisplayListService.ListType;
import org.openelisglobal.common.services.IResultSaveService;
import org.openelisglobal.common.services.ResultSaveService;
import org.openelisglobal.common.services.StatusService;
import org.openelisglobal.common.services.StatusService.AnalysisStatus;
import org.openelisglobal.common.services.beanAdapters.ResultSaveBeanAdapter;
import org.openelisglobal.common.services.registration.ValidationUpdateRegister;
import org.openelisglobal.common.services.registration.interfaces.IResultUpdate;
import org.openelisglobal.common.services.serviceBeans.ResultSaveBean;
import org.openelisglobal.common.util.ConfigurationProperties;
import org.openelisglobal.common.validator.BaseErrors;
import org.openelisglobal.dataexchange.orderresult.OrderResponseWorker.Event;
import org.openelisglobal.internationalization.MessageUtil;
import org.openelisglobal.note.service.NoteService;
import org.openelisglobal.note.service.NoteServiceImpl.NoteType;
import org.openelisglobal.note.valueholder.Note;
import org.openelisglobal.patient.valueholder.Patient;
import org.openelisglobal.referencetables.service.ReferenceTablesService;
import org.openelisglobal.reports.service.DocumentTrackService;
import org.openelisglobal.reports.service.DocumentTypeService;
import org.openelisglobal.reports.valueholder.DocumentTrack;
import org.openelisglobal.result.action.util.ResultSet;
import org.openelisglobal.result.valueholder.Result;
import org.openelisglobal.resultvalidation.action.util.ResultValidationPaging;
import org.openelisglobal.resultvalidation.bean.AnalysisItem;
import org.openelisglobal.resultvalidation.form.ResultValidationForm;
import org.openelisglobal.resultvalidation.service.ResultValidationService;
import org.openelisglobal.resultvalidation.util.ResultValidationSaveService;
import org.openelisglobal.resultvalidation.util.ResultsValidationUtility;
import org.openelisglobal.sample.valueholder.Sample;
import org.openelisglobal.samplehuman.service.SampleHumanService;
import org.openelisglobal.spring.util.SpringContext;
import org.openelisglobal.systemuser.service.SystemUserService;
import org.openelisglobal.systemuser.valueholder.SystemUser;
import org.openelisglobal.test.service.TestSectionService;
import org.openelisglobal.test.valueholder.TestSection;
import org.openelisglobal.testresult.service.TestResultService;
import org.openelisglobal.testresult.valueholder.TestResult;
import org.openelisglobal.typeoftestresult.service.TypeOfTestResultServiceImpl;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ResultValidationController extends BaseResultValidationController {

    // autowiring not needed, using constructor injection
    private AnalysisService analysisService;
    private TestResultService testResultService;
    private SampleHumanService sampleHumanService;
    private DocumentTrackService documentTrackService;
    private TestSectionService testSectionService;
    private SystemUserService systemUserService;
    private ResultValidationService resultValidationService;
    private NoteService noteService;

    private final String RESULT_SUBJECT = "Result Note";
    private final String RESULT_TABLE_ID;
    private final String RESULT_REPORT_ID;

    public ResultValidationController(AnalysisService analysisService, TestResultService testResultService,
            SampleHumanService sampleHumanService, DocumentTrackService documentTrackService,
            TestSectionService testSectionService, SystemUserService systemUserService,
            ReferenceTablesService referenceTablesService, DocumentTypeService documentTypeService,
            ResultValidationService resultValidationService, NoteService noteService) {

        this.analysisService = analysisService;
        this.testResultService = testResultService;
        this.sampleHumanService = sampleHumanService;
        this.documentTrackService = documentTrackService;
        this.testSectionService = testSectionService;
        this.systemUserService = systemUserService;
        this.resultValidationService = resultValidationService;
        this.noteService = noteService;

        RESULT_TABLE_ID = referenceTablesService.getReferenceTableByName("RESULT").getId();
        RESULT_REPORT_ID = documentTypeService.getDocumentTypeByName("resultExport").getId();
    }

    @RequestMapping(value = "/ResultValidation", method = RequestMethod.GET)
    public ModelAndView showResultValidation(HttpServletRequest request)
            throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        ResultValidationForm form = new ResultValidationForm();
        return getResultValidation(request, form);
    }

    private ModelAndView getResultValidation(HttpServletRequest request, ResultValidationForm form)
            throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {

        request.getSession().setAttribute(SAVE_DISABLED, "true");
        String testSectionId = (request.getParameter("testSectionId"));

        ResultValidationPaging paging = new ResultValidationPaging();
        String newPage = request.getParameter("page");

        TestSection ts = null;

        if (GenericValidator.isBlankOrNull(newPage)) {

            // load testSections for drop down
            PropertyUtils.setProperty(form, "testSections",
                    DisplayListService.getInstance().getList(ListType.TEST_SECTION));
            PropertyUtils.setProperty(form, "testSectionsByName",
                    DisplayListService.getInstance().getList(ListType.TEST_SECTION_BY_NAME));

            if (!GenericValidator.isBlankOrNull(testSectionId)) {
                ts = testSectionService.get(testSectionId);
                PropertyUtils.setProperty(form, "testSectionId", "0");
            }

            List<AnalysisItem> resultList;
            ResultsValidationUtility resultsValidationUtility = SpringContext.getBean(ResultsValidationUtility.class);
            setRequestType(ts == null ? MessageUtil.getMessage("workplan.unit.types") : ts.getLocalizedName());
            if (!GenericValidator.isBlankOrNull(testSectionId)) {
                resultList = resultsValidationUtility.getResultValidationList(getValidationStatus(), testSectionId);

            } else {
                resultList = new ArrayList<>();
            }
            paging.setDatabaseResults(request, form, resultList);

        } else {
            paging.page(request, form, newPage);
        }

        addFlashMsgsToRequest(request);
        return findForward(FWD_SUCCESS, form);
    }

    public List<Integer> getValidationStatus() {
        List<Integer> validationStatus = new ArrayList<>();
        validationStatus
                .add(Integer.parseInt(StatusService.getInstance().getStatusID(AnalysisStatus.TechnicalAcceptance)));
        if (ConfigurationProperties.getInstance()
                .isPropertyValueEqual(ConfigurationProperties.Property.VALIDATE_REJECTED_TESTS, "true")) {
            validationStatus
                    .add(Integer.parseInt(StatusService.getInstance().getStatusID(AnalysisStatus.TechnicalRejected)));
        }

        return validationStatus;
    }

    @RequestMapping(value = "/ResultValidation", method = RequestMethod.POST)
    public ModelAndView showResultValidationSave(HttpServletRequest request,
            @ModelAttribute("form") @Validated(ResultValidationForm.ResultValidation.class) ResultValidationForm form,
            BindingResult result, RedirectAttributes redirectAttributes)
            throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        if ("true".equals(request.getParameter("pageResults"))) {
            return getResultValidation(request, form);
        }

        if (result.hasErrors()) {
            saveErrors(result);
            return findForward(FWD_FAIL_INSERT, form);
        }
        String forward = FWD_SUCCESS_INSERT;
        List<IResultUpdate> updaters = ValidationUpdateRegister.getRegisteredUpdaters();
        boolean areListeners = updaters != null && !updaters.isEmpty();

        request.getSession().setAttribute(SAVE_DISABLED, "true");

        ResultValidationPaging paging = new ResultValidationPaging();
        paging.updatePagedResults(request, form);
        List<AnalysisItem> resultItemList = paging.getResults(request);

        String testSectionName = (String) form.get("testSection");
        String testName = (String) form.get("testName");
        setRequestType(testSectionName);
        // ----------------------
        String url = request.getRequestURL().toString();

        Errors errors = validateModifiedItems(resultItemList);

        if (errors.hasErrors()) {
            saveErrors(errors);
            return findForward(FWD_VALIDATION_ERROR, form);
        }

        createSystemUser();

        // Update Lists
        List<Analysis> analysisUpdateList = new ArrayList<>();
        ArrayList<Sample> sampleUpdateList = new ArrayList<>();
        ArrayList<Note> noteUpdateList = new ArrayList<>();
        ArrayList<Result> resultUpdateList = new ArrayList<>();
        List<Result> deletableList = new ArrayList<>();

        // wrapper object for holding modifedResultSet and newResultSet
        IResultSaveService resultSaveService = new ResultValidationSaveService();

        if (testSectionName.equals("serology")) {
            createUpdateElisaList(resultItemList, analysisUpdateList);
        } else {
            createUpdateList(resultItemList, analysisUpdateList, resultUpdateList, noteUpdateList, deletableList,
                    resultSaveService, areListeners);
        }

        try {
            resultValidationService.persistdata(deletableList, analysisUpdateList, resultUpdateList, resultItemList,
                    sampleUpdateList, noteUpdateList, resultSaveService, updaters, getSysUserId(request));
        } catch (LIMSRuntimeException lre) {
            LogEvent.logErrorStack(this.getClass().getSimpleName(), "showResultValidationSave()", lre);
        }

        for (IResultUpdate updater : updaters) {

            updater.postTransactionalCommitUpdate(resultSaveService);
        }

        // route save back to RetroC specific ResultValidationRetroCAction
        // if
        // (ConfigurationProperties.getInstance().isPropertyValueEqual(Property.configurationName,
        // "CI RetroCI"))
        if (url.contains("RetroC")) {
            forward = "successRetroC";
        }

        redirectAttributes.addFlashAttribute(FWD_SUCCESS, true);
        if (isBlankOrNull(testSectionName)) {
            return findForward(forward, form);
        } else {
            Map<String, String> params = new HashMap<>();
            params.put("type", testSectionName);
            params.put("test", testName);
            params.put("forward", forward);

            return getForwardWithParameters(findForward(forward, form), params);
        }
    }

    private Errors validateModifiedItems(List<AnalysisItem> resultItemList) {
        Errors errors = new BaseErrors();

        for (AnalysisItem item : resultItemList) {
            Errors errorList = new BaseErrors();
            validateQuantifiableItems(item, errorList);

            if (errorList.hasErrors()) {
                StringBuilder augmentedAccession = new StringBuilder(item.getAccessionNumber());
                augmentedAccession.append(" : ");
                augmentedAccession.append(item.getTestName());
                String errorMsg = "errors.followingAccession";
                errors.reject(errorMsg, new String[] { augmentedAccession.toString() }, errorMsg);
                errors.addAllErrors(errorList);

            }
        }

        return errors;
    }

    public void validateQuantifiableItems(AnalysisItem analysisItem, Errors errorList) {
        if (analysisItem.isHasQualifiedResult() && isBlankOrNull(analysisItem.getQualifiedResultValue())
                && analysisItemWillBeUpdated(analysisItem)) {
            errorList.reject("errors.missing.result.details", new String[] { "Result" },
                    "errors.missing.result.details");
        }
        // verify that qualifiedResultValue has been entered if required
        if (!isBlankOrNull(analysisItem.getQualifiedDictionaryId())) {
            String[] qualifiedDictionaryIds = analysisItem.getQualifiedDictionaryId().replace("[", "").replace("]", "")
                    .split(",");
            Set<String> qualifiedDictIdsSet = new HashSet<>(Arrays.asList(qualifiedDictionaryIds));

            if (qualifiedDictIdsSet.contains(analysisItem.getResult())
                    && isBlankOrNull(analysisItem.getQualifiedResultValue())) {
                errorList.reject("errors.missing.result.details", new String[] { "Result" },
                        "errors.missing.result.details");

            }

        }

    }

    private void createUpdateList(List<AnalysisItem> analysisItems, List<Analysis> analysisUpdateList,
            List<Result> resultUpdateList, List<Note> noteUpdateList, List<Result> deletableList,
            IResultSaveService resultValidationSave, boolean areListeners) {

        List<String> analysisIdList = new ArrayList<>();

        for (AnalysisItem analysisItem : analysisItems) {
            if (!analysisItem.isReadOnly() && analysisItemWillBeUpdated(analysisItem)) {

                Analysis analysis = analysisService.get(analysisItem.getAnalysisId());
                analysis.setSysUserId(getSysUserId(request));

                if (!analysisIdList.contains(analysis.getId())) {

                    if (analysisItem.getIsAccepted()) {
                        analysis.setStatusId(StatusService.getInstance().getStatusID(AnalysisStatus.Finalized));
                        analysis.setReleasedDate(new java.sql.Date(Calendar.getInstance().getTimeInMillis()));
                        analysisIdList.add(analysis.getId());
                        analysisUpdateList.add(analysis);
                    }

                    if (analysisItem.getIsRejected()) {
                        analysis.setStatusId(StatusService.getInstance().getStatusID(AnalysisStatus.BiologistRejected));
                        analysisIdList.add(analysis.getId());
                        analysisUpdateList.add(analysis);
                    }
                }

                createNeededNotes(analysisItem, analysis, noteUpdateList);

                if (areResults(analysisItem)) {
                    List<Result> results = createResultFromAnalysisItem(analysisItem, analysis, analysis,
                            noteUpdateList, deletableList);
                    for (Result result : results) {
                        resultUpdateList.add(result);

                        if (areListeners) {
                            addResultSets(analysis, result, resultValidationSave);
                        }
                    }
                }
            }
        }
    }

    private void createNeededNotes(AnalysisItem analysisItem, Analysis analysis, List<Note> noteUpdateList) {
        if (analysisItem.getIsRejected()) {
            Note note = noteService.createSavableNote(analysis, NoteType.INTERNAL,
                    MessageUtil.getMessage("validation.note.retest"), RESULT_SUBJECT, getSysUserId(request));
            noteUpdateList.add(note);
        }

        if (!GenericValidator.isBlankOrNull(analysisItem.getNote())) {
            NoteType noteType = analysisItem.getIsAccepted() ? NoteType.EXTERNAL : NoteType.INTERNAL;
            Note note = noteService.createSavableNote(analysis, noteType, analysisItem.getNote(), RESULT_SUBJECT,
                    getSysUserId(request));
            noteUpdateList.add(note);
        }
    }

    private void addResultSets(Analysis analysis, Result result, IResultSaveService resultValidationSave) {
        Sample sample = analysis.getSampleItem().getSample();
        Patient patient = sampleHumanService.getPatientForSample(sample);
        if (finalResultAlreadySent(result)) {
            result.setResultEvent(Event.CORRECTION);
            resultValidationSave.getModifiedResults()
                    .add(new ResultSet(result, null, null, patient, sample, null, false));
        } else {
            result.setResultEvent(Event.FINAL_RESULT);
            resultValidationSave.getNewResults().add(new ResultSet(result, null, null, patient, sample, null, false));
        }
    }

    // TO DO bug falsely triggered when preliminary result is sent, fails, retries
    // and succeeds
    private boolean finalResultAlreadySent(Result result) {
        List<DocumentTrack> documents = documentTrackService.getByTypeRecordAndTable(RESULT_REPORT_ID, RESULT_TABLE_ID,
                result.getId());
        return documents.size() > 0;
    }

    private boolean analysisItemWillBeUpdated(AnalysisItem analysisItem) {
        return analysisItem.getIsAccepted() || analysisItem.getIsRejected();
    }

    private void createUpdateElisaList(List<AnalysisItem> resultItems, List<Analysis> analysisUpdateList) {

        for (AnalysisItem resultItem : resultItems) {

            if (resultItem.getIsAccepted()) {

                List<Analysis> acceptedAnalysisList = createAnalysisFromElisaAnalysisItem(resultItem);

                for (Analysis analysis : acceptedAnalysisList) {
                    analysis.setStatusId(StatusService.getInstance().getStatusID(AnalysisStatus.Finalized));
                    analysisUpdateList.add(analysis);
                }
            }

            if (resultItem.getIsRejected()) {
                List<Analysis> rejectedAnalysisList = createAnalysisFromElisaAnalysisItem(resultItem);

                for (Analysis analysis : rejectedAnalysisList) {
                    analysis.setStatusId(StatusService.getInstance().getStatusID(AnalysisStatus.BiologistRejected));
                    analysisUpdateList.add(analysis);
                }

            }
        }
    }

    private List<Analysis> createAnalysisFromElisaAnalysisItem(AnalysisItem analysisItem) {

        List<Analysis> analysisList = new ArrayList<>();

        Analysis analysis = new Analysis();

        if (!isBlankOrNull(analysisItem.getMurexResult())) {
            analysis = getAnalysisFromId(analysisItem.getMurexAnalysisId());
            analysisList.add(analysis);
        }
        if (!isBlankOrNull(analysisItem.getBiolineResult())) {
            analysis = getAnalysisFromId(analysisItem.getBiolineAnalysisId());
            analysisList.add(analysis);
        }
        if (!isBlankOrNull(analysisItem.getIntegralResult())) {
            analysis = getAnalysisFromId(analysisItem.getIntegralAnalysisId());
            analysisList.add(analysis);
        }
        if (!isBlankOrNull(analysisItem.getVironostikaResult())) {
            analysis = getAnalysisFromId(analysisItem.getVironostikaAnalysisId());
            analysisList.add(analysis);
        }
        if (!isBlankOrNull(analysisItem.getGenieIIResult())) {
            analysis = getAnalysisFromId(analysisItem.getGenieIIAnalysisId());
            analysisList.add(analysis);
        }
        if (!isBlankOrNull(analysisItem.getGenieII10Result())) {
            analysis = getAnalysisFromId(analysisItem.getGenieII10AnalysisId());
            analysisList.add(analysis);
        }
        if (!isBlankOrNull(analysisItem.getGenieII100Result())) {
            analysis = getAnalysisFromId(analysisItem.getGenieII100AnalysisId());
            analysisList.add(analysis);
        }
        if (!isBlankOrNull(analysisItem.getWesternBlot1Result())) {
            analysis = getAnalysisFromId(analysisItem.getWesternBlot1AnalysisId());
            analysisList.add(analysis);
        }
        if (!isBlankOrNull(analysisItem.getWesternBlot2Result())) {
            analysis = getAnalysisFromId(analysisItem.getWesternBlot2AnalysisId());
            analysisList.add(analysis);
        }
        if (!isBlankOrNull(analysisItem.getP24AgResult())) {
            analysis = getAnalysisFromId(analysisItem.getP24AgAnalysisId());
            analysisList.add(analysis);
        }
        if (!isBlankOrNull(analysisItem.getInnoliaResult())) {
            analysis = getAnalysisFromId(analysisItem.getInnoliaAnalysisId());
            analysisList.add(analysis);
        }

        analysisList.add(analysis);

        return analysisList;
    }

    private Analysis getAnalysisFromId(String id) {
        Analysis analysis = analysisService.get(id);
        analysis.setSysUserId(getSysUserId(request));

        return analysis;
    }

    private List<Result> createResultFromAnalysisItem(AnalysisItem analysisItem, Analysis analysis, Analysis analysis2,
            List<Note> noteUpdateList, List<Result> deletableList) {

        ResultSaveBean bean = ResultSaveBeanAdapter.fromAnalysisItem(analysisItem);
        ResultSaveService resultSaveService = new ResultSaveService(analysis, getSysUserId(request));
        List<Result> results = resultSaveService.createResultsFromTestResultItem(bean, deletableList);
        if (analysisService.patientReportHasBeenDone(analysis) && resultSaveService.isUpdatedResult()) {
            analysis.setCorrectedSincePatientReport(true);
            noteUpdateList.add(noteService.createSavableNote(analysis, NoteType.EXTERNAL,
                    MessageUtil.getMessage("note.corrected.result"), RESULT_SUBJECT, getSysUserId(request)));
        }
        return results;
    }

    protected TestResult getTestResult(AnalysisItem analysisItem) {
        TestResult testResult = null;
        if (TypeOfTestResultServiceImpl.ResultType.DICTIONARY.matches(analysisItem.getResultType())) {
            testResult = testResultService.getTestResultsByTestAndDictonaryResult(analysisItem.getTestId(),
                    analysisItem.getResult());
        } else {
            List<TestResult> testResultList = testResultService.getActiveTestResultsByTest(analysisItem.getTestId());
            // we are assuming there is only one testResult for a numeric type
            // result
            if (!testResultList.isEmpty()) {
                testResult = testResultList.get(0);
            }
        }
        return testResult;
    }

    private boolean areResults(AnalysisItem item) {
        return !(isBlankOrNull(item.getResult())
                || (TypeOfTestResultServiceImpl.ResultType.DICTIONARY.matches(item.getResultType())
                        && "0".equals(item.getResult())))
                || (TypeOfTestResultServiceImpl.ResultType.isMultiSelectVariant(item.getResultType())
                        && !isBlankOrNull(item.getMultiSelectResultValues()));
    }

    private SystemUser createSystemUser() {
        return systemUserService.get(getSysUserId(request));
    }

    @Override
    protected String findLocalForward(String forward) {
        if (FWD_SUCCESS.equals(forward)) {
            return "resultValidationDefinition";
        } else if ("elisaSuccess".equals(forward)) {
            return "elisaAlgorithmResultValidationDefinition";
        } else if (FWD_FAIL.equals(forward)) {
            return "homePageDefinition";
        } else if (FWD_SUCCESS_INSERT.equals(forward)) {
            return "redirect:/ResultValidation.do";
        } else if ("successRetroC".equals(forward)) {
            return "redirect:/ResultValidationRetroC.do";
        } else if (FWD_FAIL_INSERT.equals(forward)) {
            return "homePageDefinition";
        } else if (FWD_VALIDATION_ERROR.equals(forward)) {
            return "resultValidationDefinition";
        } else {
            return "PageNotFound";
        }
    }
}
