package org.openelisglobal.testconfiguration.controller;

import javax.servlet.http.HttpServletRequest;

import org.openelisglobal.common.controller.BaseController;
import org.openelisglobal.common.validator.BaseErrors;
import org.openelisglobal.testconfiguration.form.UomRenameEntryForm;
import org.springframework.stereotype.Controller;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class UomRenameUpdateController extends BaseController {
    @RequestMapping(value = "/UomRenameUpdate", method = RequestMethod.GET)
    public ModelAndView showUomRenameUpdate(HttpServletRequest request,
            @ModelAttribute("form") UomRenameEntryForm form) {
        String forward = FWD_SUCCESS;
        if (form == null) {
            form = new UomRenameEntryForm();
        }
        form.setFormAction("");
        Errors errors = new BaseErrors();

        return findForward(forward, form);
    }

    @Override
    protected String findLocalForward(String forward) {
        if (FWD_SUCCESS.equals(forward)) {
            return "/UomRenameEntry.do";
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
}
