package org.openelisglobal.reports.action.implementation;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.validator.GenericValidator;
import org.openelisglobal.analysis.service.AnalysisService;
import org.openelisglobal.analysis.valueholder.Analysis;
import org.openelisglobal.common.services.IReportTrackingService;
import org.openelisglobal.common.services.ReportTrackingService;
import org.openelisglobal.common.services.StatusService;
import org.openelisglobal.common.services.StatusService.AnalysisStatus;
import org.openelisglobal.common.util.DateUtil;
import org.openelisglobal.internationalization.MessageUtil;
import org.openelisglobal.reports.action.implementation.reportBeans.VLReportData;
import org.openelisglobal.result.service.ResultService;
import org.openelisglobal.result.valueholder.Result;
import org.openelisglobal.sampleorganization.service.SampleOrganizationService;
import org.openelisglobal.sampleorganization.valueholder.SampleOrganization;
import org.openelisglobal.spring.util.SpringContext;
import org.openelisglobal.test.service.TestServiceImpl;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

public abstract class PatientVLReport extends RetroCIPatientReport {

    protected static final long YEAR = 1000L * 60L * 60L * 24L * 365L;
    protected static final long THREE_YEARS = YEAR * 3L;
    protected static final long WEEK = YEAR / 52L;
    protected static final long MONTH = YEAR / 12L;

    private AnalysisService analysisService = SpringContext.getBean(AnalysisService.class);
    private ResultService resultService = SpringContext.getBean(ResultService.class);
    private SampleOrganizationService orgService = SpringContext.getBean(SampleOrganizationService.class);

    protected List<VLReportData> reportItems;
    private String invalidValue = MessageUtil.getMessage("report.test.status.inProgress");

    @Override
    protected void initializeReportItems() {
        reportItems = new ArrayList<>();
    }

    @Override
    protected String getReportNameForReport() {
        return MessageUtil.getMessage("reports.label.patient.VL");
    }

    @Override
    public JRDataSource getReportDataSource() throws IllegalStateException {
        if (!initialized) {
            throw new IllegalStateException("initializeReport not called first");
        }

        return errorFound ? new JRBeanCollectionDataSource(errorMsgs) : new JRBeanCollectionDataSource(reportItems);
    }

    @Override
    protected void createReportItems() {
        VLReportData data = new VLReportData();

        setPatientInfo(data);
        setTestInfo(data);
        reportItems.add(data);

    }

    protected void setTestInfo(VLReportData data) {
        boolean atLeastOneAnalysisNotValidated = false;
        List<Analysis> analysisList = analysisService.getAnalysesBySampleId(reportSample.getId());
        Timestamp lastReport = SpringContext.getBean(IReportTrackingService.class)
                .getTimeOfLastNamedReport(reportSample, ReportTrackingService.ReportType.PATIENT, requestedReport);
        Boolean mayBeDuplicate = lastReport != null;

        Date maxCompleationDate = null;
        long maxCompleationTime = 0L;
//		String invalidValue = MessageUtil.getMessage("report.test.status.inProgress");

        for (Analysis analysis : analysisList) {

            if (analysis.getCompletedDate() != null) {
                if (analysis.getCompletedDate().getTime() > maxCompleationTime) {
                    maxCompleationDate = analysis.getCompletedDate();
                    maxCompleationTime = maxCompleationDate.getTime();
                }

            }

            String testName = TestServiceImpl.getUserLocalizedTestName(analysis.getTest());

            List<Result> resultList = resultService.getResultsByAnalysis(analysis);

            boolean valid = ANALYSIS_FINALIZED_STATUS_ID.equals(analysis.getStatusId());
            if (!valid) {
                atLeastOneAnalysisNotValidated = true;
            }

            if (testName.equals("Viral Load")) {
                if (valid) {
                    // data.setShowVirologie(Boolean.TRUE);
                    String resultValue = "";
                    if (resultList.size() > 0) {
                        resultValue = resultList.get(resultList.size() - 1).getValue();
                    }

                    String baseValue = resultValue;
                    if (!GenericValidator.isBlankOrNull(resultValue) && resultValue.contains("(")) {
                        String[] splitValue = resultValue.split("\\(");
                        data.setAmpli2(splitValue[0]);
                        baseValue = splitValue[0];
                    } else {
                        data.setAmpli2(resultValue);
                    }
                    if (!GenericValidator.isBlankOrNull(baseValue) && !"0".equals(baseValue)) {
                        try {
                            double viralLoad = Double.parseDouble(baseValue);
                            data.setAmpli2lo(String.format("%.3g%n", Math.log10(viralLoad)));
                        } catch (NumberFormatException nfe) {
                            data.setAmpli2lo("");
                        }
                    }

                }

            }
            if (mayBeDuplicate && StatusService.getInstance().matches(analysis.getStatusId(), AnalysisStatus.Finalized)
                    && lastReport.before(analysis.getLastupdated())) {
                mayBeDuplicate = false;
            }

        }
        if (maxCompleationDate != null) {
            data.setCompleationdate(DateUtil.convertSqlDateToStringDate(maxCompleationDate));
        }

        data.setDuplicateReport(mayBeDuplicate);
        data.setStatus(atLeastOneAnalysisNotValidated ? MessageUtil.getMessage("report.status.partial")
                : MessageUtil.getMessage("report.status.complete"));
    }

    protected void setPatientInfo(VLReportData data) {

        data.setSubjectno(reportPatient.getNationalId());
        data.setSitesubjectno(reportPatient.getExternalId());
        data.setBirth_date(reportPatient.getBirthDateForDisplay());
        data.setAge(DateUtil.getCurrentAgeForDate(reportPatient.getBirthDate(), reportSample.getCollectionDate()));
        data.setGender(reportPatient.getGender());
        data.setCollectiondate(DateUtil.convertTimestampToStringDateAndTime(reportSample.getCollectionDate()));
        SampleOrganization sampleOrg = new SampleOrganization();
        sampleOrg.setSample(reportSample);
        orgService.getDataBySample(sampleOrg);
        data.setServicename(sampleOrg.getId() == null ? "" : sampleOrg.getOrganization().getOrganizationName());
        data.setDoctor(getObservationValues(OBSERVATION_DOCTOR_ID));
        data.setAccession_number(reportSample.getAccessionNumber());
        data.setReceptiondate(DateUtil.convertTimestampToStringDateAndTime(reportSample.getReceivedTimestamp()));
        Timestamp collectionDate = reportSample.getCollectionDate();

        if (collectionDate != null) {
            long collectionTime = collectionDate.getTime() - reportPatient.getBirthDate().getTime();

            if (collectionTime < THREE_YEARS) {
                data.setAgeWeek(String.valueOf((int) Math.floor(collectionTime / WEEK)));
            } else {
                data.setAgeMonth(String.valueOf((int) Math.floor(collectionTime / MONTH)));
            }

        }
        data.getSampleQaEventItems(reportSample);
    }

    @Override
    protected String getProjectId() {
        return ANTIRETROVIRAL_STUDY_ID + ":" + ANTIRETROVIRAL_FOLLOW_UP_STUDY_ID + ":" + VL_STUDY_ID;
        // return ANTIRETROVIRAL_ID;
    }

}
