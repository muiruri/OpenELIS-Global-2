package org.openelisglobal.common.servlet.barcode;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.openelisglobal.barcode.BarcodeLabelMaker;
import org.openelisglobal.common.action.IActionConstants;
import org.openelisglobal.common.provider.validation.IAccessionNumberValidator;
import org.openelisglobal.common.util.StringUtil;
import org.openelisglobal.common.util.Versioning;
import org.openelisglobal.common.util.validator.GenericValidator;
import org.openelisglobal.common.validator.BaseErrors;
import org.openelisglobal.internationalization.MessageUtil;
import org.openelisglobal.login.dao.UserModuleService;
import org.openelisglobal.login.valueholder.UserSessionData;
import org.openelisglobal.sample.util.AccessionNumberUtil;
import org.openelisglobal.spring.util.SpringContext;
import org.springframework.validation.Errors;
import org.springframework.validation.ObjectError;

/**
 * Servlet for pages to send bar code label requests. Returns as pdf unless...
 * User unauthenticated - gives error response Invalid request parameters -
 * gives error response Maximum printing has been reached (pdf length 0) -
 * returns override page
 *
 * @author Caleb
 *
 */
public class LabelMakerServlet extends HttpServlet implements IActionConstants {

    private static final long serialVersionUID = 4756240897909804141L;

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {

        // check for authentication
        UserModuleService userModuleService = SpringContext.getBean(UserModuleService.class);
        if (userModuleService.isSessionExpired(request)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("text/html; charset=utf-8");
            response.getWriter().println(MessageUtil.getMessage("message.error.unauthorized"));
            return;
        }
        // get parameters
        String labNo = request.getParameter("labNo");
        String programCode = request.getParameter("programCode");
        String patientId = request.getParameter("patientId");
        String type = request.getParameter("type");
        String quantity = request.getParameter("quantity");
        String override = request.getParameter("override");
        if (StringUtils.isEmpty(labNo)) { // get last used accession number if none provided
            labNo = (String) request.getSession().getAttribute("lastAccessionNumber");
            labNo = StringUtil.replaceNullWithEmptyString(labNo);
        }
        if (StringUtils.isEmpty(patientId)) { // get last used patient id if none provided
            patientId = (String) request.getSession().getAttribute("lastPatientId");
            patientId = StringUtil.replaceNullWithEmptyString(patientId);
        }
        // set to default values if none provided
        if (StringUtils.isEmpty(type)) {
            type = "default";
        }
        if (StringUtils.isEmpty(quantity)) {
            quantity = "1";
        }
        if (StringUtils.isEmpty(override)) {
            override = "false";
        }
        // correct incorrect formatting of specimen number
        if (labNo.contains("-") && !labNo.contains(".")) {
            labNo = labNo.replace('-', '.');
        }

        // validate the given parameters
        Errors errors = validate(labNo, programCode, patientId, type, quantity, override);
        if (errors.hasErrors()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setContentType("text/html; charset=utf-8");
            response.getWriter().println("One or more fields are invalid");
            response.getWriter().println("<ul>");

            for (ObjectError error : errors.getAllErrors()) {
                response.getWriter().println("<li>" + MessageUtil.getMessage(error.getCode()) + "</li>");
            }
            response.getWriter().println("</ul>");
            return;
        }

        // create requested labels as pdf stream
        BarcodeLabelMaker labelMaker = new BarcodeLabelMaker();
        UserSessionData usd = (UserSessionData) request.getSession().getAttribute(USER_SESSION_DATA);
        labelMaker.setOverride(override);
        labelMaker.setSysUserId(String.valueOf(usd.getSystemUserId()));
        labelMaker.generateLabels(labNo, patientId, type, quantity, override);
        ByteArrayOutputStream labelAsOutputStream = labelMaker.createLabelsAsStream();

        // if empty stream, assume at max printing
        if (labelAsOutputStream.size() == 0) {
            String path = request.getContextPath();
            String basePath = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort()
                    + path + "/";
            String version = Versioning.getBuildNumber();
            response.setContentType("text/html; charset=utf-8");
            response.getWriter().println(MessageUtil.getMessage("barcode.message.maxreached"));
            response.getWriter().println("</br>");
            response.getWriter()
                    .println("<input type='button' id='overrideButton' value='Override' onclick='override();'>");
            response.getWriter().println("<script type=\"text/javascript\" src=\"" + basePath
                    + "scripts/labelMaker.js?ver=" + version + "\" ></script>");
            // else return the pdf
        } else {
            response.setContentType("application/pdf");
            response.addHeader("Content-Disposition", "inline; filename=" + "sample.pdf");
            response.setContentLength(labelAsOutputStream.size());
            labelAsOutputStream.writeTo(response.getOutputStream());
            response.getOutputStream().flush();
            response.getOutputStream().close();
        }
    }

    /**
     * Validate the given parameters
     *
     * @param labNo       Make sure it is properly formatted
     * @param programCode Optional variable to tell what accessionNumberUtil to get
     * @param patientId   Ensure is int
     * @param type        Ensure is default, specimen, order, or blank
     * @param quantity    Ensure is int
     * @param override    Ensure is bool
     * @return any errors that were generated along the way
     */
    private Errors validate(String labNo, String programCode, String patientId, String type, String quantity,
            String override) {
        Errors errors = new BaseErrors();
        // Validate quantity
        if (!GenericValidator.isInt(quantity)) {
            errors.reject("barcode.label.error.quantity.invalid", "barcode.label.error.quantity.invalid");
        }
        // Validate type
        if (!"default".equals(type) && !"order".equals(type) && !"specimen".equals(type) && !"blank".equals(type)) {
            errors.reject("barcode.label.error.type.invalid", "barcode.label.error.type.invalid");
        }
        // Validate patientId
        if (!GenericValidator.isInt(patientId)) {
            errors.reject("barcode.label.error.patientid.invalid", "barcode.label.error.patientid.invalid");
        }
        // Validate "labNo" (either labNo, labNo.itemNo)
        IAccessionNumberValidator accessionNumberValidator = AccessionNumberUtil
                .getAccessionNumberValidator(programCode);
        String accessionNumber;
        String sampleItemNumber;
        if (labNo.indexOf(".") > 0) {
            accessionNumber = labNo.substring(0, labNo.indexOf("."));
            sampleItemNumber = labNo.substring(labNo.indexOf(".") + 1);
        } else {
            accessionNumber = labNo;
            sampleItemNumber = "0";
        }
        if (!(IAccessionNumberValidator.ValidationResults.SUCCESS == accessionNumberValidator
                .validFormat(accessionNumber, false))) {
            errors.reject("barcode.label.error.accession.invalid", "barcode.label.error.accession.invalid");
        }
        // validate override
        if (!GenericValidator.isBool(override) && !GenericValidator.isBlankOrNull(override)) {
            errors.reject("barcode.label.error.override.invalid", "barcode.label.error.override.invalid");
        }

        return errors;
    }

}
