package org.openelisglobal.resultlimits.controller;

import javax.servlet.http.HttpServletRequest;

import org.openelisglobal.common.controller.BaseController;
import org.openelisglobal.common.validator.BaseErrors;
import org.openelisglobal.resultlimits.form.ResultLimitsForm;
import org.springframework.stereotype.Controller;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

//seemingly unused controller
@Controller
public class UpdateResultLimitsController extends BaseController {
    @RequestMapping(value = "/UpdateResultLimits", method = RequestMethod.GET)
    public ModelAndView showUpdateResultLimits(HttpServletRequest request,
            @ModelAttribute("form") ResultLimitsForm form) {
        String forward = FWD_SUCCESS;
        if (form == null) {
            form = new ResultLimitsForm();
        }
        form.setFormAction("");
        Errors errors = new BaseErrors();

        return findForward(forward, form);
    }

    @Override
    protected String findLocalForward(String forward) {
        if (FWD_SUCCESS_INSERT.equals(forward)) {
            return "/ResultLimits.do";
        } else if (FWD_FAIL.equals(forward)) {
            return "resultLimitsDefinition";
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
