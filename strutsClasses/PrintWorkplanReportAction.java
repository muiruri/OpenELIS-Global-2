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

import java.io.File;
import java.util.HashMap;
import java.util.List;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.Globals;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessages;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.openelisglobal.common.action.BaseAction;
import org.openelisglobal.common.action.BaseActionForm;
import org.openelisglobal.common.log.LogEvent;
import org.openelisglobal.common.services.TestService;
import org.openelisglobal.common.util.StringUtil;
import org.openelisglobal.common.util.validator.ActionError;
import org.openelisglobal.test.dao.TestDAO;
import org.openelisglobal.test.daoimpl.TestDAOImpl;
import org.openelisglobal.workplan.reports.ElisaWorkplanReport;
import org.openelisglobal.workplan.reports.IWorkplanReport;
import org.openelisglobal.workplan.reports.TestSectionWorkplanReport;
import org.openelisglobal.workplan.reports.TestWorkplanReport;

public class PrintWorkplanReportAction extends BaseAction {

	private final TestDAO testDAO = new TestDAOImpl();
	private static org.openelisglobal.workplan.reports.IWorkplanReport workplanReport;
	private String reportPath;

	@Override
	protected ActionForward performAction(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws Exception {

		// no forward necessary if successful, returning stream
		String forward = FWD_FAIL;

		ActionError error = null;
		ActionMessages errors = new ActionMessages();

		BaseActionForm dynaForm = (BaseActionForm) form;

		request.getSession().setAttribute(SAVE_DISABLED, "true");

		String workplanType = dynaForm.getString("workplanType");
		String workplanName;

		if (workplanType.equals("test")) {
			String testID = (String) dynaForm.get("testTypeID");
			workplanName = getTestTypeName(testID);
		} else {
			workplanType = Character.toUpperCase(workplanType.charAt(0)) + workplanType.substring(1);
			workplanName = dynaForm.getString("testName");
		}

		// get workplan report based on testName
		workplanReport = getWorkplanReport(workplanType, workplanName);

		workplanReport.setReportPath(getReportPath());

		// set jasper report parameters
		HashMap<String, ?> parameterMap = workplanReport.getParameters();

		// prepare report

		// commented out to allow maven compilation - CSL
		// List<?> workplanRows = workplanReport.prepareRows(dynaForm);

		// set Jasper report file name
		String reportFileName = workplanReport.getFileName();
		String reportFile = getServlet().getServletConfig().getServletContext()
				.getRealPath("WEB-INF/reports/" + reportFileName + ".jasper");


		// commented out to allow maven compilation - CSL
		// try {

			byte[] bytes = null;

			// commented out to allow maven compilation - CSL
			// JRDataSource dataSource = createReportDataSource(workplanRows);

			// commented out to allow maven compilation - CSL
			// bytes = JasperRunManager.runReportToPdf(reportFile, parameterMap,
			// dataSource);

			ServletOutputStream servletOutputStream = response.getOutputStream();
			response.setContentType("application/pdf");
			response.setContentLength(bytes.length);

			servletOutputStream.write(bytes, 0, bytes.length);
			servletOutputStream.flush();
			servletOutputStream.close();

			// commented out to allow maven compilation - CSL
		/*} catch (JRException jre) {
			LogEvent.logError("PringWorkplanReportAction", "processRequest()", jre.toString());
			error = new ActionError("error.jasper", null, null);
		} catch (Exception e) {
			LogEvent.logError("PrintWorkplanReportAction", "processRequest()", e.toString());
			error = new ActionError("error.jasper", null, null);
		}*/

		if (error != null) {
			errors.add(ActionMessages.GLOBAL_MESSAGE, error);
			request.setAttribute(Globals.ERROR_KEY, errors);
			forward = FWD_FAIL;
		}

		return mapping.findForward(forward);

	}

	private String getName(String workplanType) {
		String name = StringUtil.getContextualMessageForKey("test.section." + workplanType);

		if (name == null) {
			name = workplanType;
		}
		return name;
	}

	private JRDataSource createReportDataSource(List<?> includedTests) {
		JRBeanCollectionDataSource dataSource;
		dataSource = new JRBeanCollectionDataSource(includedTests);

		return dataSource;
	}

	@Override
	protected String getPageSubtitleKey() {
		return "workplan.title";
	}

	@Override
	protected String getPageTitleKey() {
		return "workplan.title";
	}

	private String getTestTypeName(String id) {
		return TestServiceImpl.getUserLocalizedTestName(id);
	}

	public IWorkplanReport getWorkplanReport(String testType, String name) {

		IWorkplanReport workplan;

		if ("test".equals(testType)) {
			workplan = new TestWorkplanReport(name);
		} else if ("Serology".equals(testType)) {
			workplan = new ElisaWorkplanReport(name);
		} else {
			workplan = new TestSectionWorkplanReport(name);
		}

		return workplan;
	}

	public String getReportPath() {
		if (reportPath == null) {
			reportPath = getServlet().getServletContext().getRealPath("") + File.separator + "WEB-INF" + File.separator
					+ "reports" + File.separator;
		}
		return reportPath;
	}

}
