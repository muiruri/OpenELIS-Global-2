package org.openelisglobal.analyzerimport.controller;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.openelisglobal.analyzer.service.AnalyzerService;
import org.openelisglobal.analyzer.valueholder.Analyzer;
import org.openelisglobal.analyzerimport.action.beans.NamedAnalyzerTestMapping;
import org.openelisglobal.analyzerimport.form.AnalyzerTestNameMenuForm;
import org.openelisglobal.analyzerimport.service.AnalyzerTestMappingService;
import org.openelisglobal.analyzerimport.util.AnalyzerTestNameCache;
import org.openelisglobal.analyzerimport.util.MappedTestName;
import org.openelisglobal.analyzerimport.valueholder.AnalyzerTestMapping;
import org.openelisglobal.common.constants.Constants;
import org.openelisglobal.common.controller.BaseMenuController;
import org.openelisglobal.common.exception.LIMSRuntimeException;
import org.openelisglobal.common.form.MenuForm;
import org.openelisglobal.common.validator.BaseErrors;
import org.openelisglobal.internationalization.MessageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AnalyzerTestNameMenuController extends BaseMenuController {

    @Autowired
    AnalyzerTestMappingService analyzerTestMappingService;
    @Autowired
    AnalyzerService analyzerService;

    private static final int ANALYZER_NAME = 0;
    private static final int ANALYZER_TEST = 1;

    @RequestMapping(value = "/AnalyzerTestNameMenu", method = RequestMethod.GET)
    public ModelAndView showAnalyzerTestNameMenu(HttpServletRequest request, RedirectAttributes redirectAttributes)
            throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        AnalyzerTestNameMenuForm form = new AnalyzerTestNameMenuForm();

        addFlashMsgsToRequest(request);

        String forward = performMenuAction(form, request);
        if (FWD_FAIL.equals(forward)) {
            Errors errors = new BaseErrors();
            errors.reject("error.generic");
            redirectAttributes.addFlashAttribute(Constants.REQUEST_ERRORS, errors);
            return findForward(forward, form);
        } else {
            return findForward(forward, form);
        }
    }

    @Override
    @SuppressWarnings("rawtypes")
    protected List createMenuList(MenuForm form, HttpServletRequest request) throws Exception {

        request.setAttribute("menuDefinition", "AnalyzerTestNameMenuDefinition");

        String stringStartingRecNo = (String) request.getAttribute("startingRecNo");
        int startingRecNo = Integer.parseInt(stringStartingRecNo);

        List<NamedAnalyzerTestMapping> mappedTestNameList = new ArrayList<>();
        List<String> analyzerList = AnalyzerTestNameCache.instance().getAnalyzerNames();
        Analyzer analyzer = new Analyzer();

        for (String analyzerName : analyzerList) {
            Collection<MappedTestName> mappedTestNames = AnalyzerTestNameCache.instance()
                    .getMappedTestsForAnalyzer(analyzerName).values();
            if (mappedTestNames.size() > 0) {
                analyzer.setId(((MappedTestName) mappedTestNames.toArray()[0]).getAnalyzerId());
                analyzer = analyzerService.get(analyzer.getId());
                mappedTestNameList.addAll(convertedToNamedList(mappedTestNames, analyzer.getName()));
            }
        }

        setDisplayPageBounds(request, mappedTestNameList.size(), startingRecNo);

        return mappedTestNameList.subList(Math.min(mappedTestNameList.size(), startingRecNo - 1),
                Math.min(mappedTestNameList.size(), startingRecNo + getPageSize()));

        // return mappedTestNameList;
    }

    private List<NamedAnalyzerTestMapping> convertedToNamedList(Collection<MappedTestName> mappedTestNameList,
            String analyzerName) {
        List<NamedAnalyzerTestMapping> namedMappingList = new ArrayList<>();

        for (MappedTestName test : mappedTestNameList) {
            NamedAnalyzerTestMapping namedMapping = new NamedAnalyzerTestMapping();
            namedMapping.setActualTestName(test.getOpenElisTestName());
            namedMapping.setAnalyzerTestName(test.getAnalyzerTestName());
            namedMapping.setAnalyzerName(analyzerName);

            namedMappingList.add(namedMapping);
        }

        return namedMappingList;
    }

    private void setDisplayPageBounds(HttpServletRequest request, int listSize, int startingRecNo)
            throws LIMSRuntimeException {
        request.setAttribute(MENU_TOTAL_RECORDS, String.valueOf(listSize));
        request.setAttribute(MENU_FROM_RECORD, String.valueOf(startingRecNo));

        int numOfRecs = 0;
        if (listSize != 0) {
            numOfRecs = Math.min(listSize, getPageSize());

            numOfRecs--;
        }

        int endingRecNo = startingRecNo + numOfRecs;
        request.setAttribute(MENU_TO_RECORD, String.valueOf(endingRecNo));
    }

    @Override
    protected String getDeactivateDisabled() {
        return "false";
    }

    @Override
    protected String getEditDisabled() {
        return "true";
    }

    @RequestMapping(value = "/DeleteAnalyzerTestName", method = RequestMethod.POST)
    public ModelAndView showDeleteAnalyzerTestName(HttpServletRequest request,
            @ModelAttribute("form") @Valid AnalyzerTestNameMenuForm form, BindingResult result,
            RedirectAttributes redirectAttributes)
            throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        if (result.hasErrors()) {
            saveErrors(result);
            return findForward(performMenuAction(form, request), form);
        }

        List<String> selectedIDs = (List<String>) form.get("selectedIDs");

        // String sysUserId = getSysUserId(request);
        List<AnalyzerTestMapping> testMappingList = new ArrayList<>();

        for (int i = 0; i < selectedIDs.size(); i++) {
            String[] ids = selectedIDs.get(i).split(NamedAnalyzerTestMapping.getUniqueIdSeperator());
            AnalyzerTestMapping testMapping = new AnalyzerTestMapping();
            testMapping.setAnalyzerId(AnalyzerTestNameCache.instance().getAnalyzerIdForName(ids[ANALYZER_NAME]));
            testMapping.setAnalyzerTestName(ids[ANALYZER_TEST]);
            testMapping.setSysUserId(getSysUserId(request));
            testMappingList.add(testMapping);
            try {
                analyzerTestMappingService.delete(testMapping);
            } catch (LIMSRuntimeException lre) {
                lre.printStackTrace();
                saveErrors(result);
                return findForward(performMenuAction(form, request), form);
            }
        }

        AnalyzerTestNameCache.instance().reloadCache();
        request.setAttribute("menuDefinition", "AnalyzerTestNameDefinition");
        redirectAttributes.addFlashAttribute(Constants.SUCCESS_MSG, MessageUtil.getMessage("message.success.delete"));
        return findForward(FWD_SUCCESS_DELETE, form);
    }

    @Override
    protected String findLocalForward(String forward) {
        if (FWD_SUCCESS.equals(forward)) {
            return "haitiMasterListsPageDefinition";
        } else if (FWD_FAIL.equals(forward)) {
            return "redirect:/MasterListsPage.do";
        } else if (FWD_SUCCESS_DELETE.equals(forward)) {
            return "redirect:/AnalyzerTestNameMenu.do";
        } else if (FWD_FAIL_DELETE.equals(forward)) {
            return "haitiMasterListsPageDefinition";
        } else {
            return "PageNotFound";
        }
    }

    @Override
    protected String getPageTitleKey() {
        return "analyzerTestName.browse.title";
    }

    @Override
    protected String getPageSubtitleKey() {
        return "analyzerTestName.browse.title";
    }
}
