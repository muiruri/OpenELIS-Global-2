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
package org.openelisglobal.analysis.daoimpl;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.validator.GenericValidator;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.openelisglobal.analysis.dao.AnalysisDAO;
import org.openelisglobal.analysis.valueholder.Analysis;
import org.openelisglobal.common.daoimpl.BaseDAOImpl;
import org.openelisglobal.common.exception.LIMSRuntimeException;
import org.openelisglobal.common.log.LogEvent;
import org.openelisglobal.common.util.StringUtil;
import org.openelisglobal.common.util.SystemConfiguration;
import org.openelisglobal.result.valueholder.Result;
import org.openelisglobal.sample.valueholder.Sample;
import org.openelisglobal.sampleitem.valueholder.SampleItem;
import org.openelisglobal.test.valueholder.Test;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author diane benz
 */
@Component
@Transactional
public class AnalysisDAOImpl extends BaseDAOImpl<Analysis, String> implements AnalysisDAO {

    public AnalysisDAOImpl() {
        super(Analysis.class);
    }

//	@Override
//	@SuppressWarnings("rawtypes")
//	public void deleteData(List analyses) throws LIMSRuntimeException {
//		// add to audit trail
//		try {
//
//			for (int i = 0; i < analyses.size(); i++) {
//				Analysis data = (Analysis) analyses.get(i);
//
//				Analysis oldData = readAnalysis(data.getId());
//				Analysis newData = new Analysis();
//
//				String sysUserId = data.getSysUserId();
//				String event = IActionConstants.AUDIT_TRAIL_DELETE;
//				String tableName = "ANALYSIS";
//				auditDAO.saveHistory(newData, oldData, sysUserId, event, tableName);
//			}
//		} catch (Exception e) {
//
//			LogEvent.logError("AnalysisDAOImpl", "AuditTrail deleteData()", e.toString());
//			throw new LIMSRuntimeException("Error in Analysis AuditTrail deleteData()", e);
//		}
//
//		try {
//			for (int i = 0; i < analyses.size(); i++) {
//				Analysis data = (Analysis) analyses.get(i);
//				// bugzilla 2206
//				data = readAnalysis(data.getId());
//				entityManager.unwrap(Session.class).delete(data);
//				// entityManager.unwrap(Session.class).flush(); // CSL remove old
//				// entityManager.unwrap(Session.class).clear(); // CSL remove old
//			}
//		} catch (Exception e) {
//
//			LogEvent.logError("AnalysisDAOImpl", "deleteData()", e.toString());
//			throw new LIMSRuntimeException("Error in Analysis deleteData()", e);
//		}
//	}

    /*
     * Warning: duplicateCheck uses SystemConfiguration setting for excluding status
     * (non-Javadoc)
     *
     * @see org.openelisglobal.analysis.dao.AnalysisDAO#insertData(us.mn.state
     * .health.lims.analysis.valueholder.Analysis, boolean)
     */
//	@Override
//	public boolean insertData(Analysis analysis, boolean duplicateCheck) throws LIMSRuntimeException {
//
//		try {
//
//			if (duplicateCheck) {
//				if (duplicateExists(analysis)) {
//					throw new LIMSDuplicateRecordException("Duplicate record exists for this sample and test "
//							+ analysis.getTest().getTestDisplayValue());
//				}
//			}
//			String id = (String) entityManager.unwrap(Session.class).save(analysis);
//			analysis.setId(id);
//
//			// bugzilla 1824 inserts will be logged in history table
//
//			String sysUserId = analysis.getSysUserId();
//			String tableName = "ANALYSIS";
//			auditDAO.saveNewHistory(analysis, sysUserId, tableName);
//
//		} catch (Exception e) {
//			LogEvent.logError("AnalysisDAOImpl", "insertData()", e.toString());
//			throw new LIMSRuntimeException("Error in Analysis insertData()", e);
//		}
//
//		return true;
//	}

//	@Override
//	public void updateData(Analysis analysis) {
//		updateData(analysis, false);
//	}
//
//	@Override
//	public void updateData(Analysis analysis, boolean skipAuditTrail) throws LIMSRuntimeException {
//		Analysis oldData = readAnalysis(analysis.getId());
//
//		if (!skipAuditTrail) {
//			try {
//
//				String sysUserId = analysis.getSysUserId();
//				String event = IActionConstants.AUDIT_TRAIL_UPDATE;
//				String tableName = "ANALYSIS";
//				auditDAO.saveHistory(analysis, oldData, sysUserId, event, tableName);
//			} catch (Exception e) {
//				LogEvent.logError("AnalysisDAOImpl", "AuditTrail updateData()", e.toString());
//				throw new LIMSRuntimeException("Error in Analysis AuditTrail updateData()", e);
//			}
//		}
//
//		try {
//			entityManager.unwrap(Session.class).merge(analysis);
//			// entityManager.unwrap(Session.class).flush(); // CSL remove old
//			// entityManager.unwrap(Session.class).clear(); // CSL remove old
//			// entityManager.unwrap(Session.class).evict // CSL remove old(analysis);
//			// entityManager.unwrap(Session.class).refresh // CSL remove old(analysis);
//		} catch (Exception e) {
//			LogEvent.logError("AnalysisDAOImpl", "updateData()", e.toString());
//			throw new LIMSRuntimeException("Error in Analysis updateData()", e);
//		}
//	}

    @Override
    @Transactional(readOnly = true)
    public void getData(Analysis analysis) throws LIMSRuntimeException {

        try {
            Analysis analysisClone = entityManager.unwrap(Session.class).get(Analysis.class, analysis.getId());
            if (analysisClone != null) {
                PropertyUtils.copyProperties(analysis, analysisClone);
            } else {
                analysis.setId(null);
            }
        } catch (Exception e) {

            LogEvent.logError("AnalysisDAOImpl", "getData()", e.toString());
            throw new LIMSRuntimeException("Error in Analysis getData()", e);
        }
    }

//	@Override
//	@SuppressWarnings("rawtypes")
//	public List getAllAnalyses() throws LIMSRuntimeException {
//		List list = new Vector();
//		try {
//			String sql = "from Analysis a order by a.id";
//			org.hibernate.Query query = entityManager.unwrap(Session.class).createQuery(sql);
//			list = query.list();
//			// entityManager.unwrap(Session.class).flush(); // CSL remove old
//			// entityManager.unwrap(Session.class).clear(); // CSL remove old
//		} catch (Exception e) {
//
//			LogEvent.logError("AnalysisDAOImpl", "getAllAnalyses()", e.toString());
//			throw new LIMSRuntimeException("Error in Analysis getAllAnalyses()", e);
//		}
//
//		return list;
//	}

//	@Override
//	@SuppressWarnings("rawtypes")
//	public List getPageOfAnalyses(int startingRecNo) throws LIMSRuntimeException {
//		List list = new Vector();
//
//		try {
//			// calculate maxRow to be one more than the page size
//			int endingRecNo = startingRecNo + (SystemConfiguration.getInstance().getDefaultPageSize() + 1);
//
//			String sql = "from Analysis a order by a.id";
//			org.hibernate.Query query = entityManager.unwrap(Session.class).createQuery(sql);
//			query.setFirstResult(startingRecNo - 1);
//			query.setMaxResults(endingRecNo - 1);
//
//			list = query.list();
//			// entityManager.unwrap(Session.class).flush(); // CSL remove old
//			// entityManager.unwrap(Session.class).clear(); // CSL remove old
//		} catch (Exception e) {
//
//			LogEvent.logError("AnalysisDAOImpl", "getPageOfAnalyses()", e.toString());
//			throw new LIMSRuntimeException("Error in Analysis getPageOfAnalyses()", e);
//		}
//
//		return list;
//	}

    public Analysis readAnalysis(String idString) {
        Analysis analysis = null;
        try {
            analysis = entityManager.unwrap(Session.class).get(Analysis.class, idString);
        } catch (Exception e) {

            LogEvent.logError("AnalysisDAOImpl", "readAnalysis()", e.toString());
            throw new LIMSRuntimeException("Error in Analysis readAnalysis()", e);
        }

        return analysis;
    }

//	@Override
//	@SuppressWarnings("rawtypes")
//	public List getAnalyses(String filter) throws LIMSRuntimeException {
//		List list = new Vector();
//		try {
//			String sql = "from Analysis a where upper(a.analysisType) like upper(:param) order by upper(a.analysisType)";
//			org.hibernate.Query query = entityManager.unwrap(Session.class).createQuery(sql);
//			query.setParameter("param", filter + "%");
//			list = query.list();
//			// entityManager.unwrap(Session.class).flush(); // CSL remove old
//			// entityManager.unwrap(Session.class).clear(); // CSL remove old
//		} catch (Exception e) {
//
//			LogEvent.logError("AnalysisDAOImpl", "getAnalyses()", e.toString());
//			throw new LIMSRuntimeException("Error in Analysis getAnalyses(String filter)", e);
//		}
//
//		return list;
//
//	}

//	@Override
//	@SuppressWarnings("rawtypes")
//	public List getNextAnalysisRecord(String id) throws LIMSRuntimeException {
//
//		return getNextRecord(id, "Analysis", Analysis.class);
//
//	}
//
//	@Override
//	@SuppressWarnings("rawtypes")
//	public List getPreviousAnalysisRecord(String id) throws LIMSRuntimeException {
//
//		return getPreviousRecord(id, "Analysis", Analysis.class);
//	}

//	@Override
//	@SuppressWarnings({ "rawtypes", "unchecked" })
//	public List getAllAnalysesPerTest(Test test) throws LIMSRuntimeException {
//		List list = new Vector();
//		try {
//			String sql = "from Analysis a where a.test = :testId and (a.status is null or a.status NOT IN (:exclusionList)) order by a.sampleItem.sample.accessionNumber";
//			org.hibernate.Query query = entityManager.unwrap(Session.class).createQuery(sql);
//			query.setInteger("testId", Integer.parseInt(test.getId()));
//			List statusesToExclude = new ArrayList();
//			statusesToExclude.add(SystemConfiguration.getInstance().getAnalysisStatusCanceled());
//			query.setParameterList("exclusionList", statusesToExclude);
//			list = query.list();
//			// entityManager.unwrap(Session.class).flush(); // CSL remove old
//			// entityManager.unwrap(Session.class).clear(); // CSL remove old
//		} catch (Exception e) {
//
//			LogEvent.logError("AnalysisDAOImpl", "getAllAnalysesPerTest()", e.toString());
//			throw new LIMSRuntimeException("Error in Analysis getAllAnalysesPerTest()", e);
//		}
//
//		return list;
//	}
    @SuppressWarnings("unchecked")

    @Override
    @Transactional(readOnly = true)
    public List<Analysis> getAllAnalysisByTestAndStatus(String testId, List<Integer> statusIdList)
            throws LIMSRuntimeException {
        try {
            String sql = "from Analysis a where a.test = :testId and a.statusId IN (:statusIdList) order by a.sampleItem.sample.accessionNumber";

            org.hibernate.Query query = entityManager.unwrap(Session.class).createQuery(sql);
            query.setInteger("testId", Integer.parseInt(testId));
            query.setParameterList("statusIdList", statusIdList);
            return query.list();
        } catch (Exception e) {

            LogEvent.logError("AnalysisDAOImpl", "getAllAnalysisByTestAndStatuses()", e.toString());
            throw new LIMSRuntimeException("Error in Analysis getAllAnalysisByTestAndStatuses()", e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    @Transactional(readOnly = true)
    public List<Analysis> getAllAnalysisByTestsAndStatus(List<String> testIdList, List<Integer> statusIdList)
            throws LIMSRuntimeException {
        List<Integer> testList = new ArrayList<>();
        try {
            String sql = "from Analysis a where a.test.id IN (:testList) and a.statusId IN (:statusIdList) order by a.sampleItem.sample.accessionNumber";

            for (String testId : testIdList) {
                testList.add(Integer.parseInt(testId));
            }

            org.hibernate.Query query = entityManager.unwrap(Session.class).createQuery(sql);
            query.setParameterList("testList", testList);
            query.setParameterList("statusIdList", statusIdList);
            return query.list();
        } catch (Exception e) {
            LogEvent.logError("AnalysisDAOImpl", "getAllAnalysisByTestsAndStatuses()", e.toString());
            throw new LIMSRuntimeException("Error in Analysis getAllAnalysisByTestsAndStatuses()", e);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    @Transactional(readOnly = true)
    public List<Analysis> getAllAnalysisByTestAndExcludedStatus(String testId, List<Integer> statusIdList)
            throws LIMSRuntimeException {
        try {
            String sql = "from Analysis a where a.test = :testId and a.statusId not IN (:statusIdList) order by a.sampleItem.sample.accessionNumber";

            org.hibernate.Query query = entityManager.unwrap(Session.class).createQuery(sql);
            query.setInteger("testId", Integer.parseInt(testId));
            query.setParameterList("statusIdList", statusIdList);
            return query.list();
        } catch (Exception e) {

            LogEvent.logError("AnalysisDAOImpl", "getAllAnalysisByTestAndExcludedStatuses()", e.toString());
            throw new LIMSRuntimeException("Error in Analysis getAllAnalysisByTestAndExcludedStatuses()", e);
        }

    }

    @SuppressWarnings("unchecked")
    @Override
    @Transactional(readOnly = true)
    public List<Analysis> getAllAnalysisByTestSectionAndStatus(String testSectionId, List<Integer> statusIdList,
            boolean sortedByDateAndAccession) throws LIMSRuntimeException {
        try {
            String sql = "from Analysis a where a.testSection.id = :testSectionId and a.statusId IN (:statusIdList) order by a.id";

            if (sortedByDateAndAccession) {
                // sql += " order by a.sampleItem.sample.receivedTimestamp asc,
                // a.sampleItem.sample.accessionNumber";
            }

            org.hibernate.Query query = entityManager.unwrap(Session.class).createQuery(sql);
            query.setInteger("testSectionId", Integer.parseInt(testSectionId));
            query.setParameterList("statusIdList", statusIdList);
            return query.list();
        } catch (Exception e) {

            LogEvent.logError("AnalysisDAOImpl", "getAllAnalysisByTestSectionAndStatuses()", e.toString());
            throw new LIMSRuntimeException("Error in Analysis getAllAnalysisByTestSectionAndStatuses()", e);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    @Transactional(readOnly = true)
    public List<Analysis> getAllAnalysisByTestSectionAndExcludedStatus(String testSectionId, List<Integer> statusIdList)
            throws LIMSRuntimeException {
        try {
            String sql = "from Analysis a where a.testSection.id = :testSectionId and a.statusId NOT IN (:statusIdList) order by a.sampleItem.sample.receivedTimestamp asc, a.sampleItem.sample.accessionNumber ";

            org.hibernate.Query query = entityManager.unwrap(Session.class).createQuery(sql);
            query.setInteger("testSectionId", Integer.parseInt(testSectionId));
            query.setParameterList("statusIdList", statusIdList);
            return query.list();
            // entityManager.unwrap(Session.class).flush(); // CSL remove old
            // entityManager.unwrap(Session.class).clear(); // CSL remove old
        } catch (Exception e) {

            LogEvent.logError("AnalysisDAOImpl", "getAllAnalysisByTestSectionAndExcludedStatuses()", e.toString());
            throw new LIMSRuntimeException("Error in Analysis getAllAnalysisByTestSectionAndExcludedStatuses()", e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    @Transactional(readOnly = true)
    public List<Analysis> getAnalysesBySampleItem(SampleItem sampleItem) throws LIMSRuntimeException {
        List<Analysis> list = null;
        try {
            String sql = "from Analysis a where a.sampleItem.id = :sampleItemId";

            org.hibernate.Query query = entityManager.unwrap(Session.class).createQuery(sql);
            query.setInteger("sampleItemId", Integer.parseInt(sampleItem.getId()));

            list = query.list();
        } catch (Exception e) {
            LogEvent.logError("AnalysisDAOImpl", "getAnalysesBySampleItem()", e.toString());
            throw new LIMSRuntimeException("Error in Analysis getAnalysesBySampleItem()", e);
        }

        return list;
    }

    @Override
    @SuppressWarnings("unchecked")
    @Transactional(readOnly = true)
    public List<Analysis> getAnalysesBySampleItemsExcludingByStatusIds(SampleItem sampleItem, Set<Integer> statusIds)
            throws LIMSRuntimeException {
        if (statusIds == null || statusIds.isEmpty()) {
            return getAnalysesBySampleItem(sampleItem);
        }

        List<Analysis> analysisList = null;

        try {
            String sql = "from Analysis a where a.sampleItem.id = :sampleItemId and a.statusId not in ( :statusList )";

            Query query = entityManager.unwrap(Session.class).createQuery(sql);
            query.setInteger("sampleItemId", Integer.parseInt(sampleItem.getId()));
            query.setParameterList("statusList", statusIds);

            analysisList = query.list();
        } catch (Exception e) {

            LogEvent.logError("AnalysisDAOImpl", "getAnalysesBySampleItemsExcludingByStatusIds()", e.toString());
            throw new LIMSRuntimeException("Error in Analysis getAnalysesBySampleItemsExcludingByStatusIds()", e);
        }

        return analysisList;
    }

    @SuppressWarnings("unchecked")
    @Override
    @Transactional(readOnly = true)
    public List<Analysis> getAnalysesBySampleStatusIdExcludingByStatusId(String statusId, Set<Integer> statusIds)
            throws LIMSRuntimeException {
        if (statusIds == null || statusIds.isEmpty()) {
            return getAnalysesBySampleStatusId(statusId);
        }

        String sql = "from Analysis a where a.sampleItem.sample.statusId = :sampleStatus and a.statusId not in (:excludedStatusIds)";

        try {
            Query query = entityManager.unwrap(Session.class).createQuery(sql);
            query.setInteger("sampleStatus", Integer.parseInt(statusId));
            query.setParameterList("excludedStatusIds", statusIds);

            List<Analysis> analysisList = query.list();
            return analysisList;

        } catch (HibernateException e) {
            handleException(e, "getAnalysesBySampleStatusIdExcludingByStatusId");
        }

        return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    @Transactional(readOnly = true)
    public List<Analysis> getAnalysesBySampleStatusId(String statusId) throws LIMSRuntimeException {
        List<Analysis> analysisList = null;

        try {
            String sql = "from Analysis a where a.sampleItem.sample.statusId = :sampleStatusId";

            Query query = entityManager.unwrap(Session.class).createQuery(sql);
            query.setInteger("sampleStatusId", Integer.parseInt(statusId));

            analysisList = query.list();
        } catch (Exception e) {

            LogEvent.logError("AnalysisDAOImpl", "getAnalysesBySampleItemsExcludingByStatusIds()", e.toString());
            throw new LIMSRuntimeException("Error in Analysis getAnalysesBySampleItemsExcludingByStatusIds()", e);
        }

        return analysisList;
    }

    @SuppressWarnings("unchecked")
    @Override
    @Transactional(readOnly = true)
    public List<Analysis> getAnalysesBySampleIdExcludedByStatusId(String id, Set<Integer> statusIds)
            throws LIMSRuntimeException {
        if (statusIds == null || statusIds.isEmpty()) {
            return getAnalysesBySampleId(id);
        }

        String sql = "from Analysis a where a.sampleItem.sample.id = :sampleId and a.statusId not in ( :excludedIds)";

        try {
            Query query = entityManager.unwrap(Session.class).createQuery(sql);
            query.setInteger("sampleId", Integer.parseInt(id));
            query.setParameterList("excludedIds", statusIds);
            List<Analysis> analysisList = query.list();
            // closeSession(); // CSL remove old
            return analysisList;
        } catch (HibernateException e) {
            handleException(e, "getAnalysesBySampleIdExcludedByStatusId");
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    @Transactional(readOnly = true)
    public List<Analysis> getAnalysesBySampleIdAndStatusId(String id, Set<Integer> statusIds)
            throws LIMSRuntimeException {
        if (statusIds == null || statusIds.isEmpty()) {
            return getAnalysesBySampleId(id);
        }

        String sql = "from Analysis a where a.sampleItem.sample.id = :sampleId and a.statusId in ( :statusIds)";

        try {
            Query query = entityManager.unwrap(Session.class).createQuery(sql);
            query.setInteger("sampleId", Integer.parseInt(id));
            query.setParameterList("statusIds", statusIds);
            List<Analysis> analysisList = query.list();
            // closeSession(); // CSL remove old
            return analysisList;
        } catch (HibernateException e) {
            handleException(e, "getAnalysesBySampleIdAndStatusId");
        }

        return null;

    }

    /**
     * bugzilla 1993 (part of 1942) getAnalysesReadyToBeReported() - returns the
     * tests that should be updated with a printed date of today's date (see
     * ResultsReport)
     *
     */
    @Override
    @SuppressWarnings("unchecked")
    @Transactional(readOnly = true)
    public List<Analysis> getAnalysesReadyToBeReported() throws LIMSRuntimeException {
        try {
            List<String> analysisStatusesToInclude = new ArrayList<>();
            analysisStatusesToInclude.add(SystemConfiguration.getInstance().getAnalysisStatusReleased());

            List<String> sampleStatusesToInclude = new ArrayList<>();
            sampleStatusesToInclude.add(SystemConfiguration.getInstance().getSampleStatusEntry2Complete());
            sampleStatusesToInclude.add(SystemConfiguration.getInstance().getSampleStatusReleased());

            return entityManager.unwrap(Session.class).getNamedQuery("analysis.getAnalysesReadyToBeReported")
                    .setParameterList("analysisStatusesToInclude", analysisStatusesToInclude)
                    .setParameterList("sampleStatusesToInclude", sampleStatusesToInclude).list();

        } catch (Exception e) {
            LogEvent.logError("AnalysisDAOImpl", "getAnalysesReadyToBeReported()", e.toString());
            throw new LIMSRuntimeException("Error in getAnalysesReadyToBeReported()", e);
        } finally {
            // entityManager.unwrap(Session.class).flush(); // CSL remove old
            // entityManager.unwrap(Session.class).clear(); // CSL remove old
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    @Transactional(readOnly = true)
    public List<Analysis> getAllChildAnalysesByResult(Result result) throws LIMSRuntimeException {
        try {
            String sql = "from Analysis a where a.parentResult = :param and a.status NOT IN (:param2)";
            org.hibernate.Query query = entityManager.unwrap(Session.class).createQuery(sql);
            query.setParameter("param", result.getId());
            List<String> statusesToExclude = new ArrayList<>();
            statusesToExclude.add(SystemConfiguration.getInstance().getAnalysisStatusCanceled());
            query.setParameterList("param2", statusesToExclude);
            return query.list();
            // entityManager.unwrap(Session.class).flush(); // CSL remove old
            // entityManager.unwrap(Session.class).clear(); // CSL remove old
        } catch (Exception e) {
            LogEvent.logError("AnalysisDAOImpl", "getallChildAnalysesByResult()", e.toString());
            throw new LIMSRuntimeException("Error in Analysis getallChildAnalysesByResult()", e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    @Transactional(readOnly = true)
    public List<Analysis> getMaxRevisionAnalysesBySample(SampleItem sampleItem) throws LIMSRuntimeException {
        try {
            String sql = "from Analysis a where (a.sampleItem.id, a.test.id, a.revision) IN "
                    + "(select b.sampleItem.id, b.test.id, max(b.revision) from Analysis b "
                    + "group by b.sampleItem.id, b.test.id) " + "and a.sampleItem.id = :param "
                    + "and a.status NOT IN (:param2) " + "order by a.test.id, a.revision desc";

            org.hibernate.Query query = entityManager.unwrap(Session.class).createQuery(sql);
            query.setParameter("param", sampleItem.getId());
            List<String> statusesToExclude = new ArrayList<>();
            statusesToExclude.add(SystemConfiguration.getInstance().getAnalysisStatusCanceled());
            query.setParameterList("param2", statusesToExclude);
            return query.list();
            // entityManager.unwrap(Session.class).flush(); // CSL remove old
            // entityManager.unwrap(Session.class).clear(); // CSL remove old
        } catch (Exception e) {

            LogEvent.logError("AnalysisDAOImpl", "getMaxRevisionAnalysesBySample()", e.toString());
            throw new LIMSRuntimeException("Error in Analysis getMaxRevisionAnalysesBySample()", e);
        }
    }

    // bugzilla 2300 (separate method for sample tracking)
    @SuppressWarnings("unchecked")
    @Override
    @Transactional(readOnly = true)
    public List<Analysis> getMaxRevisionAnalysesBySampleIncludeCanceled(SampleItem sampleItem)
            throws LIMSRuntimeException {
        try {
            String sql = "from Analysis a where (a.sampleItem.id, a.test.id, a.revision) IN "
                    + "(select b.sampleItem.id, b.test.id, max(b.revision) from Analysis b "
                    + "group by b.sampleItem.id, b.test.id) " + "and a.sampleItem.id = :param "
                    + "order by a.test.id, a.revision desc";

            org.hibernate.Query query = entityManager.unwrap(Session.class).createQuery(sql);
            query.setParameter("param", sampleItem.getId());
            return query.list();
            // entityManager.unwrap(Session.class).flush(); // CSL remove old
            // entityManager.unwrap(Session.class).clear(); // CSL remove old
        } catch (Exception e) {
            LogEvent.logError("AnalysisDAOImpl", "getMaxRevisionAnalysesBySample()", e.toString());
            throw new LIMSRuntimeException("Error in Analysis getMaxRevisionAnalysesBySample()", e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    @Transactional(readOnly = true)
    public List<Analysis> getRevisionHistoryOfAnalysesBySample(SampleItem sampleItem) throws LIMSRuntimeException {
        try {
            String sql = "from Analysis a where (a.sampleItem.id, a.test.id, a.revision) NOT IN "
                    + "(select b.sampleItem.id, b.test.id, max(b.revision) from Analysis b "
                    + "group by b.sampleItem.id, b.test.id) " + "and a.sampleItem.id = :param "
                    + "and a.status NOT IN (:param2) " + "order by a.test.id, a.revision desc";

            org.hibernate.Query query = entityManager.unwrap(Session.class).createQuery(sql);
            query.setParameter("param", sampleItem.getId());
            List<String> statusesToExclude = new ArrayList<>();
            statusesToExclude.add(SystemConfiguration.getInstance().getAnalysisStatusCanceled());
            query.setParameterList("param2", statusesToExclude);
            return query.list();
            // entityManager.unwrap(Session.class).flush(); // CSL remove old
            // entityManager.unwrap(Session.class).clear(); // CSL remove old
        } catch (Exception e) {
            LogEvent.logError("AnalysisDAOImpl", "getRevisionHistoryOfAnalysesBySample()", e.toString());
            throw new LIMSRuntimeException("Error in Analysis getRevisionHistoryOfAnalysesBySample()", e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    @Transactional(readOnly = true)
    public List<Analysis> getRevisionHistoryOfAnalysesBySampleAndTest(SampleItem sampleItem, Test test,
            boolean includeLatestRevision) throws LIMSRuntimeException {
        try {
            String sql = "";
            if (includeLatestRevision) {
                sql = "from Analysis a " + "where a.sampleItem.id = :param " + "and a.status NOT IN (:param3) "
                        + "and a.test.id = :param2 " + "order by a.test.id, a.revision desc";
            } else {
                sql = "from Analysis a where (a.sampleItem.id, a.test.id, a.revision) NOT IN "
                        + "(select b.sampleItem.id, b.test.id, max(b.revision) from Analysis b "
                        + "group by b.sampleItem.id, b.test.id) " + "and a.sampleItem.id = :param "
                        + "and a.status NOT IN (:param3) " + "and a.test.id = :param2 "
                        + "order by a.test.id, a.revision desc";
            }

            org.hibernate.Query query = entityManager.unwrap(Session.class).createQuery(sql);
            query.setParameter("param", sampleItem.getId());
            query.setParameter("param2", test.getId());
            List<String> statusesToExclude = new ArrayList<>();
            statusesToExclude.add(SystemConfiguration.getInstance().getAnalysisStatusCanceled());
            query.setParameterList("param3", statusesToExclude);
            return query.list();
            // entityManager.unwrap(Session.class).flush(); // CSL remove old
            // entityManager.unwrap(Session.class).clear(); // CSL remove old
        } catch (Exception e) {
            LogEvent.logError("AnalysisDAOImpl", "getRevisionHistoryOfAnalysesBySample()", e.toString());
            throw new LIMSRuntimeException("Error in Analysis getRevisionHistoryOfAnalysesBySample()", e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    @Transactional(readOnly = true)
    public List<Analysis> getAllMaxRevisionAnalysesPerTest(Test test) throws LIMSRuntimeException {
        try {
            String sql = "from Analysis a where (a.sampleItem.id, a.revision) IN "
                    + "(select b.sampleItem.id, max(b.revision) from Analysis b " + "group by b.sampleItem.id) "
                    + "and a.test = :param " + "and a.status NOT IN (:param2) "
                    + "order by a.sampleItem.sample.accessionNumber";

            org.hibernate.Query query = entityManager.unwrap(Session.class).createQuery(sql);
            query.setParameter("param", test.getId());

            List<String> statusesToExclude = new ArrayList<>();
            statusesToExclude.add(SystemConfiguration.getInstance().getAnalysisStatusCanceled());
            query.setParameterList("param2", statusesToExclude);

            return query.list();
            // entityManager.unwrap(Session.class).flush(); // CSL remove old
            // entityManager.unwrap(Session.class).clear(); // CSL remove old
        } catch (Exception e) {
            LogEvent.logError("AnalysisDAOImpl", "getAllMaxRevisionAnalysesPerTest()", e.toString());
            throw new LIMSRuntimeException("Error in Analysis getAllMaxRevisionAnalysesPerTest()", e);
        }
    }

    // bugzilla 2227, 2258
    @Override
    @SuppressWarnings("unchecked")
    @Transactional(readOnly = true)
    public List<Analysis> getMaxRevisionAnalysesReadyToBeReported() throws LIMSRuntimeException {
        try {
            List<String> analysisStatusesToInclude = new ArrayList<>();
            analysisStatusesToInclude.add(SystemConfiguration.getInstance().getAnalysisStatusReleased());

            List<String> sampleStatusesToInclude = new ArrayList<>();
            sampleStatusesToInclude.add(SystemConfiguration.getInstance().getSampleStatusEntry2Complete());
            sampleStatusesToInclude.add(SystemConfiguration.getInstance().getSampleStatusReleased());

            return entityManager.unwrap(Session.class).getNamedQuery("analysis.getMaxRevisionAnalysesReadyToBeReported")
                    .setParameterList("analysisStatusesToInclude", analysisStatusesToInclude)
                    .setParameterList("sampleStatusesToInclude", sampleStatusesToInclude).list();

        } catch (Exception e) {
            LogEvent.logError("AnalysisDAOImpl", "getMaxRevisionAnalysesReadyToBeReported()", e.toString());
            throw new LIMSRuntimeException("Error in Analysis getMaxRevisionAnalysesReadyToBeReported()", e);
        } finally {
            // entityManager.unwrap(Session.class).flush(); // CSL remove old
            // entityManager.unwrap(Session.class).clear(); // CSL remove old
        }
    }

    // bugzilla 1900
    @Override
    @SuppressWarnings("unchecked")
    @Transactional(readOnly = true)
    public List<Analysis> getMaxRevisionAnalysesReadyForReportPreviewBySample(List accessionNumbers)
            throws LIMSRuntimeException {
        List<Analysis> list = new Vector<>();
        try {
            List<String> analysisStatusesToInclude = new ArrayList<>();
            // see question in 1900 should this be released or results completed
            // status?
            // answer: results completed
            analysisStatusesToInclude.add(SystemConfiguration.getInstance().getAnalysisStatusResultCompleted());

            List<String> sampleStatusesToInclude = new ArrayList<>();
            sampleStatusesToInclude.add(SystemConfiguration.getInstance().getSampleStatusEntry2Complete());
            // see question in 1900 - should this be included? Yes
            sampleStatusesToInclude.add(SystemConfiguration.getInstance().getSampleStatusReleased());

            if (accessionNumbers != null && accessionNumbers.size() > 0) {
                list = entityManager.unwrap(Session.class)
                        .getNamedQuery("analysis.getMaxRevisionAnalysesReadyForPreviewBySample")
                        .setParameterList("analysisStatusesToInclude", analysisStatusesToInclude)
                        .setParameterList("sampleStatusesToInclude", sampleStatusesToInclude)
                        .setParameterList("samplesToInclude", accessionNumbers).list();
            }

        } catch (Exception e) {
            LogEvent.logError("AnalysisDAOImpl", "getMaxRevisionAnalysesReadyForReportPreviewBySample()", e.toString());
            throw new LIMSRuntimeException("Error in getMaxRevisionAnalysesReadyForReportPreviewBySample()", e);
        } finally {
            // entityManager.unwrap(Session.class).flush(); // CSL remove old
            // entityManager.unwrap(Session.class).clear(); // CSL remove old
        }
        return list;
    }

    // bugzilla 1856
    @Override
    @Transactional(readOnly = true)
    public List<Analysis> getAnalysesAlreadyReportedBySample(Sample sample) throws LIMSRuntimeException {
        try {
            return entityManager.unwrap(Session.class).getNamedQuery("analysis.getAnalysesAlreadyReportedBySample")
                    .setParameter("sampleId", sample.getId()).list();
        } catch (Exception e) {
            LogEvent.logError("AnalysisDAOImpl", "getAnalysesAlreadyReportedBySample()", e.toString());
            throw new LIMSRuntimeException("Error in getAnalysesAlreadyReportedBySample()", e);
        } finally {
            // entityManager.unwrap(Session.class).flush(); // CSL remove old
            // entityManager.unwrap(Session.class).clear(); // CSL remove old
        }
    }

    // bugzilla 2264
    @Override
    @SuppressWarnings("unchecked")
    @Transactional(readOnly = true)
    public List<Analysis> getMaxRevisionPendingAnalysesReadyToBeReportedBySample(Sample sample)
            throws LIMSRuntimeException {
        try {
            List<String> analysisStatusesToInclude = new ArrayList<>();
            analysisStatusesToInclude.add(SystemConfiguration.getInstance().getAnalysisStatusAssigned());
            // bugzilla 2264 per Nancy add results completed status to pending
            // tests
            analysisStatusesToInclude.add(SystemConfiguration.getInstance().getAnalysisStatusResultCompleted());

            return entityManager.unwrap(Session.class)
                    .getNamedQuery("analysis.getMaxRevisionPendingAnalysesReadyToBeReportedBySample")
                    .setParameter("sampleId", sample.getId())
                    .setParameterList("analysisStatusesToInclude", analysisStatusesToInclude).list();

        } catch (Exception e) {
            LogEvent.logError("AnalysisDAOImpl", "getMaxRevisionPendingAnalysesReadyToBeReportedBySample()",
                    e.toString());
            throw new LIMSRuntimeException("Error in Analysis getMaxRevisionPendingAnalysesReadyToBeReportedBySample()",
                    e);
        } finally {
            // entityManager.unwrap(Session.class).flush(); // CSL remove old
            // entityManager.unwrap(Session.class).clear(); // CSL remove old
        }
    }

    // bugzilla 1900
    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Transactional(readOnly = true)
    public List getMaxRevisionPendingAnalysesReadyForReportPreviewBySample(Sample sample) throws LIMSRuntimeException {
        List list = new Vector();

        try {

            List analysisStatusesToInclude = new ArrayList();
            analysisStatusesToInclude.add(SystemConfiguration.getInstance().getAnalysisStatusAssigned());
            // see question in 1900 do we need to include this?
            // Answer NO
            // analysisStatusesToInclude.add(SystemConfiguration.getInstance().getAnalysisStatusResultCompleted());

            list = entityManager.unwrap(Session.class)
                    .getNamedQuery("analysis.getMaxRevisionPendingAnalysesReadyToBeReportedBySample")
                    .setParameter("sampleId", sample.getId())
                    .setParameterList("analysisStatusesToInclude", analysisStatusesToInclude).list();

        } catch (Exception e) {

            LogEvent.logError("AnalysisDAOImpl", "getMaxRevisionPendingAnalysesReadyForReportPreviewBySample()",
                    e.toString());
            throw new LIMSRuntimeException("Error in getMaxRevisionPendingAnalysesReadyForReportPreviewBySample()", e);
        } finally {
            // entityManager.unwrap(Session.class).flush(); // CSL remove old
            // entityManager.unwrap(Session.class).clear(); // CSL remove old
        }

        return list;
    }

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Transactional(readOnly = true)
    public Analysis getPreviousAnalysisForAmendedAnalysis(Analysis analysis) throws LIMSRuntimeException {
        Analysis previousAnalysis = null;
        try {
            // Use an expression to read in the Analysis whose
            // revision is 1 less than the analysis passed in

            String sql = "from Analysis a where a.revision = :param and a.sampleItem = :param2 and a.test = :param3 and a.status NOT IN (:param4)";
            Query query = entityManager.unwrap(Session.class).createQuery(sql);

            String revisionString = analysis.getRevision();
            int revision = 0;
            if (!StringUtil.isNullorNill(revisionString)) {
                try {
                    revision = Integer.parseInt(revisionString);
                } catch (NumberFormatException nfe) {

                }
            }

            query.setParameter("param", String.valueOf((revision - 1)));
            query.setParameter("param2", analysis.getSampleItem());
            query.setParameter("param3", analysis.getTest());

            List statusesToExclude = new ArrayList();
            statusesToExclude.add(SystemConfiguration.getInstance().getAnalysisStatusCanceled());
            query.setParameterList("param4", statusesToExclude);
            List list = query.list();
            if ((list != null) && !list.isEmpty()) {
                previousAnalysis = (Analysis) list.get(0);
            }
            // entityManager.unwrap(Session.class).flush(); // CSL remove old
            // entityManager.unwrap(Session.class).clear(); // CSL remove old

        } catch (Exception e) {

            LogEvent.logError("AnalysisDAOImpl", "getPreviousAnalysisForAmendedAnalysis()", e.toString());
            throw new LIMSRuntimeException("Exception occurred in getPreviousAnalysisForAmendedAnalysis", e);
        }
        return previousAnalysis;

    }

//	@Override
//	@SuppressWarnings({ "rawtypes", "unchecked" })
//	protected boolean duplicateExists(Analysis analysis) {
//		try {
//
//			List list = new ArrayList();
//
//			String sql = "from Analysis a where a.sampleItem = :param and a.test = :param2 and a.status NOT IN (:param3)";
//			org.hibernate.Query query = entityManager.unwrap(Session.class).createQuery(sql);
//			query.setParameter("param", analysis.getSampleItem());
//			query.setParameter("param2", analysis.getTest());
//			List statusesToExclude = new ArrayList();
//			statusesToExclude.add(SystemConfiguration.getInstance().getAnalysisStatusCanceled());
//			query.setParameterList("param3", statusesToExclude);
//
//			list = query.list();
//			// entityManager.unwrap(Session.class).flush(); // CSL remove old
//			// entityManager.unwrap(Session.class).clear(); // CSL remove old
//
//			if (list.size() > 0) {
//				return true;
//			} else {
//				return false;
//			}
//
//		} catch (Exception e) {
//
//			LogEvent.logError("AnalysisDAOImpl", "duplicateAnalysisExists()", e.toString());
//			throw new LIMSRuntimeException("Error in duplicateAnalysisExists()", e);
//		}
//	}

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Transactional(readOnly = true)
    public void getMaxRevisionAnalysisBySampleAndTest(Analysis analysis) throws LIMSRuntimeException {

        try {
            Analysis anal = null;
            String sql = "from Analysis a where (a.sampleItem.id, a.test.id, a.revision) IN "
                    + "(select b.sampleItem.id, b.test.id, max(b.revision) from Analysis b "
                    + "group by b.sampleItem.id, b.test.id) " + "and a.sampleItem = :param "
                    + "and a.status NOT IN (:param3) " + "and a.test = :param2";

            org.hibernate.Query query = entityManager.unwrap(Session.class).createQuery(sql);
            query.setParameter("param", analysis.getSampleItem().getId());
            query.setParameter("param2", analysis.getTest().getId());
            List statusesToExclude = new ArrayList();
            statusesToExclude.add(SystemConfiguration.getInstance().getAnalysisStatusCanceled());
            query.setParameterList("param3", statusesToExclude);
            anal = (Analysis) query.uniqueResult();
            // entityManager.unwrap(Session.class).flush(); // CSL remove old
            // entityManager.unwrap(Session.class).clear(); // CSL remove old

            if (anal != null) {
                PropertyUtils.copyProperties(analysis, anal);
            } else {
                analysis.setId(null);
            }

        } catch (Exception e) {

            LogEvent.logError("AnalysisDAOImpl", "getMaxRevisionAnalysisBySampleAndTest()", e.toString());
            throw new LIMSRuntimeException("Error in Analysis getMaxRevisionAnalysisBySampleAndTest()", e);
        }

    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Transactional(readOnly = true)
    public List getMaxRevisionParentTestAnalysesBySample(SampleItem sampleItem) throws LIMSRuntimeException {

        List list = new Vector();
        try {

            String sql = "from Analysis a where (a.sampleItem.id, a.test.id, a.revision) IN "
                    + "(select b.sampleItem.id, b.test.id, max(b.revision) from Analysis b "
                    + "group by b.sampleItem.id, b.test.id) " + "and a.sampleItem.id = :param "
                    + "and a.status NOT IN (:param2) " + "and a.parentAnalysis is null "
                    + "order by a.test.id, a.revision desc";

            org.hibernate.Query query = entityManager.unwrap(Session.class).createQuery(sql);
            query.setParameter("param", sampleItem.getId());
            List statusesToExclude = new ArrayList();
            statusesToExclude.add(SystemConfiguration.getInstance().getAnalysisStatusCanceled());
            query.setParameterList("param2", statusesToExclude);
            list = query.list();
            // entityManager.unwrap(Session.class).flush(); // CSL remove old
            // entityManager.unwrap(Session.class).clear(); // CSL remove old
        } catch (Exception e) {
            LogEvent.logError("AnalysisDAOImpl", "getMaxRevisionAnalysesBySample()", e.toString());
            throw new LIMSRuntimeException("Error in Analysis getMaxRevisionAnalysesBySample()", e);
        }

        return list;
    }

    @Override
    @SuppressWarnings("unchecked")
    @Transactional(readOnly = true)
    public List<Analysis> getAnalysesForStatusId(String statusId) throws LIMSRuntimeException {

        List<Analysis> list = null;

        try {
            String sql = "from Analysis a where a.statusId = :statusId";
            org.hibernate.Query query = entityManager.unwrap(Session.class).createQuery(sql);
            query.setInteger("statusId", Integer.parseInt(statusId));

            list = query.list();
            // closeSession(); // CSL remove old
            return list;
        } catch (HibernateException he) {
            handleException(he, "getAnalysisForStatusId");
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    @Transactional(readOnly = true)
    public List<Analysis> getAnalysisStartedOnExcludedByStatusId(Date collectionDate, Set<Integer> statusIds)
            throws LIMSRuntimeException {
        if (statusIds == null || statusIds.isEmpty()) {
            return getAnalysisStartedOn(collectionDate);
        }

        String sql = "from Analysis a where a.startedDate = :startedDate and a.statusId not in ( :statusList )";

        try {
            Query query = entityManager.unwrap(Session.class).createQuery(sql);
            query.setDate("startedDate", collectionDate);
            query.setParameterList("statusList", statusIds);

            List<Analysis> analysisList = query.list();
            // closeSession(); // CSL remove old
            return analysisList;
        } catch (HibernateException e) {
            handleException(e, "getAnalysisStartedOnExcludedByStatusId");
        }

        return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    @Transactional(readOnly = true)
    public List<Analysis> getAnalysisStartedOn(Date collectionDate) throws LIMSRuntimeException {

        try {
            String sql = "from Analysis a where a.startedDate = :startedDate";
            Query query = entityManager.unwrap(Session.class).createQuery(sql);
            query.setDate("startedDate", collectionDate);

            List<Analysis> list = query.list();
            return list;

        } catch (HibernateException he) {
            handleException(he, "getAnalysisStartedOn");
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    @Transactional(readOnly = true)
    public List<Analysis> getAnalysisCollectedOnExcludedByStatusId(Date collectionDate, Set<Integer> statusIds)
            throws LIMSRuntimeException {
        if (statusIds == null || statusIds.isEmpty()) {
            return getAnalysisStartedOn(collectionDate);
        }

        String sql = "from Analysis a where a.sampleItem.collectionDate = :startedDate and a.statusId not in ( :statusList )";

        try {
            Query query = entityManager.unwrap(Session.class).createQuery(sql);
            query.setDate("startedDate", collectionDate);
            query.setParameterList("statusList", statusIds);

            List<Analysis> analysisList = query.list();
            return analysisList;
        } catch (HibernateException e) {
            handleException(e, "getAnalysisStartedOnExcludedByStatusId");
        }

        return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    @Transactional(readOnly = true)
    public List<Analysis> getAnalysisCollectedOn(Date collectionDate) throws LIMSRuntimeException {

        try {
            String sql = "from Analysis a where a.sampleItem.collectionDate = :startedDate";
            Query query = entityManager.unwrap(Session.class).createQuery(sql);
            query.setDate("startedDate", collectionDate);

            List<Analysis> list = query.list();
            // closeSession(); // CSL remove old
            return list;

        } catch (HibernateException he) {
            handleException(he, "getAnalysisStartedOn");
        }

        return null;
    }

    /**
     * @see org.openelisglobal.analysis.dao.AnalysisDAO#getAnalysisBySampleAndTestIds(java.lang.String,
     *      java.util.List)
     */
    @Override
    @SuppressWarnings("unchecked")
    @Transactional(readOnly = true)
    public List<Analysis> getAnalysisBySampleAndTestIds(String sampleId, List<Integer> testIds) {
        List<Analysis> list = null;
        try {
            if (testIds == null || testIds.size() == 0) {
                return new ArrayList<>();
            }
            String sql = "from Analysis a WHERE a.sampleItem.sample.id = :sampleId AND a.test.id IN ( :testIds )";
            org.hibernate.Query query = entityManager.unwrap(Session.class).createQuery(sql);
            query.setInteger("sampleId", Integer.valueOf(sampleId));
            query.setParameterList("testIds", testIds);

            list = query.list();
            // entityManager.unwrap(Session.class).flush(); // CSL remove old
            // entityManager.unwrap(Session.class).clear(); // CSL remove old

        } catch (HibernateException he) {
            LogEvent.logError("AnalysisDAOImpl", "getAnalysisBySampleAndTestIds()", he.toString());
            throw new LIMSRuntimeException("Error in getAnalysisStartedOn()", he);
        }

        return list;
    }

    @Override
    @SuppressWarnings("unchecked")
    @Transactional(readOnly = true)
    public List<Analysis> getAnalysisByTestSectionAndCompletedDateRange(String sectionID, Date lowDate, Date highDate)
            throws LIMSRuntimeException {

        String sql = "From Analysis a where a.testSection.id = :testSectionId and a.completedDate BETWEEN :lowDate AND :highDate";

        try {
            Query query = entityManager.unwrap(Session.class).createQuery(sql);
            query.setInteger("testSectionId", Integer.parseInt(sectionID));
            query.setDate("lowDate", lowDate);
            query.setDate("highDate", highDate);

            List<Analysis> list = query.list();
            // closeSession(); // CSL remove old
            return list;
        } catch (HibernateException he) {
            handleException(he, "getAnalysisByTestSectionAndCompletedDateRange");
        }

        return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    @Transactional(readOnly = true)
    public List<Analysis> getAnalysisStartedOrCompletedInDateRange(Date lowDate, Date highDate)
            throws LIMSRuntimeException {
        String sql = "From Analysis a where a.startedDate BETWEEN :lowDate AND :highDate or a.completedDate BETWEEN :lowDate AND :highDate";

        try {
            Query query = entityManager.unwrap(Session.class).createQuery(sql);
            query.setDate("lowDate", lowDate);
            query.setDate("highDate", highDate);

            List<Analysis> list = query.list();
            // closeSession(); // CSL remove old
            return list;
        } catch (HibernateException he) {
            handleException(he, "getAnalysisStartedOrCompletedInDateRange");
        }

        return null;

    }

    @Override
    @SuppressWarnings("unchecked")
    @Transactional(readOnly = true)
    public List<Analysis> getAnalysesBySampleId(String id) throws LIMSRuntimeException {
        List<Analysis> list = null;
        if (!GenericValidator.isBlankOrNull(id)) {
            try {
                String sql = "from Analysis a where a.sampleItem.sample.id = :sampleId";

                org.hibernate.Query query = entityManager.unwrap(Session.class).createQuery(sql);
                query.setInteger("sampleId", Integer.parseInt(id));

                list = query.list();
            } catch (Exception e) {
                handleException(e, "getAnalysesBySampleId");
            }
        }
        return list;
    }

    @Override
    @SuppressWarnings("unchecked")
    @Transactional(readOnly = true)
    public List<Analysis> getAllAnalysisByTestSectionAndStatus(String testSectionId, List<Integer> analysisStatusList,
            List<Integer> sampleStatusList) throws LIMSRuntimeException {

        String sql = "From Analysis a WHERE a.testSection.id = :testSectionId AND a.statusId IN (:analysisStatusList) AND a.sampleItem.sample.statusId IN (:sampleStatusList)";

        try {
            Query query = entityManager.unwrap(Session.class).createQuery(sql);
            query.setInteger("testSectionId", Integer.parseInt(testSectionId));
            query.setParameterList("analysisStatusList", analysisStatusList);
            query.setParameterList("sampleStatusList", sampleStatusList);

            List<Analysis> analysisList = query.list();

            // closeSession(); // CSL remove old

            return analysisList;

        } catch (HibernateException e) {
            handleException(e, "getAllAnalysisByTestSectionAndStatus");
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    @Transactional(readOnly = true)
    public List<Analysis> getAnalysisStartedOnRangeByStatusId(Date lowDate, Date highDate, String statusID)
            throws LIMSRuntimeException {
        String sql = "From Analysis a where a.statusId = :statusID and a.startedDate BETWEEN :lowDate AND :highDate";

        try {
            Query query = entityManager.unwrap(Session.class).createQuery(sql);
            query.setInteger("statusID", Integer.parseInt(statusID));
            query.setDate("lowDate", lowDate);
            query.setDate("highDate", highDate);

            List<Analysis> analysisList = query.list();
            // closeSession(); // CSL remove old
            return analysisList;

        } catch (HibernateException e) {
            handleException(e, "getAnalysisStartedOnRangeByStatusId");
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    @Transactional(readOnly = true)
    public List<Analysis> getAnalysisCompleteInRange(Timestamp lowDate, Timestamp highDate)
            throws LIMSRuntimeException {
        String sql = "From Analysis a where a.completedDate >= :lowDate AND a.completedDate < :highDate";

        try {
            Query query = entityManager.unwrap(Session.class).createQuery(sql);
            query.setTimestamp("lowDate", lowDate);
            query.setTimestamp("highDate", highDate);

            List<Analysis> analysisList = query.list();
            // closeSession(); // CSL remove old
            return analysisList;

        } catch (HibernateException e) {
            handleException(e, "getAnalysisCompletedInRange");
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    @Transactional(readOnly = true)
    public List<Analysis> getAnalysisEnteredAfterDate(Timestamp date) throws LIMSRuntimeException {
        String sql = "From Analysis a where a.enteredDate > :date";

        try {
            Query query = entityManager.unwrap(Session.class).createQuery(sql);
            query.setTimestamp("date", date);

            List<Analysis> analysisList = query.list();
            // closeSession(); // CSL remove old
            return analysisList;

        } catch (HibernateException e) {
            handleException(e, "getAnalysisEnteredAfterDate");
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    @Transactional(readOnly = true)
    public List<Analysis> getAnalysisByAccessionAndTestId(String accessionNumber, String testId)
            throws LIMSRuntimeException {
        if (GenericValidator.isBlankOrNull(accessionNumber) || GenericValidator.isBlankOrNull(testId)) {
            return new ArrayList<>();
        }

        String sql = "From Analysis a where a.sampleItem.sample.accessionNumber = :accessionNumber and a.test.id = :testId";

        try {
            Query query = entityManager.unwrap(Session.class).createQuery(sql);
            query.setString("accessionNumber", accessionNumber);
            query.setInteger("testId", Integer.parseInt(testId));
            List<Analysis> analysises = query.list();
            // closeSession(); // CSL remove old
            return analysises;
        } catch (HibernateException e) {
            handleException(e, "getAnalysisByAccessionAndTestId");
        }

        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Analysis> getAnalysisByTestNamesAndCompletedDateRange(List<String> testNames, Date lowDate,
            Date highDate) throws LIMSRuntimeException {
        if (testNames.isEmpty()) {
            return new ArrayList<>();
        }

        String sql = "From Analysis a where a.test.testName in (:testNames) and a.completedDate BETWEEN :lowDate AND :highDate";

        try {
            Query query = entityManager.unwrap(Session.class).createQuery(sql);
            query.setParameterList("testNames", testNames);
            query.setDate("lowDate", lowDate);
            query.setDate("highDate", highDate);

            @SuppressWarnings("unchecked")
            List<Analysis> list = query.list();
            // closeSession(); // CSL remove old
            return list;
        } catch (HibernateException he) {
            handleException(he, "getAnalysisByTestNamesAndCompletedDateRange");
        }

        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Analysis> getAnalysisByTestDescriptionAndCompletedDateRange(List<String> descriptions, Date lowDate,
            Date highDate) throws LIMSRuntimeException {
        if (descriptions.isEmpty()) {
            return new ArrayList<>();
        }

        String sql = "From Analysis a where a.test.description in (:descriptions) and a.completedDate BETWEEN :lowDate AND :highDate";

        try {
            Query query = entityManager.unwrap(Session.class).createQuery(sql);
            query.setParameterList("descriptions", descriptions);
            query.setDate("lowDate", lowDate);
            query.setDate("highDate", highDate);

            @SuppressWarnings("unchecked")
            List<Analysis> list = query.list();
            // closeSession(); // CSL remove old
            return list;
        } catch (HibernateException he) {
            handleException(he, "getAnalysisByTestDescriptionsAndCompletedDateRange");
        }

        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Analysis> getAnalysesBySampleItemIdAndStatusId(String sampleItemId, String statusId)
            throws LIMSRuntimeException {
        try {
            String sql = "from Analysis a where a.sampleItem.id = :sampleItemId and a.statusId = :statusId";

            Query query = entityManager.unwrap(Session.class).createQuery(sql);
            query.setInteger("sampleItemId", Integer.parseInt(sampleItemId));
            query.setInteger("statusId", Integer.parseInt(statusId));

            @SuppressWarnings("unchecked")
            List<Analysis> analysisList = query.list();
            return analysisList;
        } catch (Exception e) {
            handleException(e, "getAnalysesBySampleItemIdAndStatusId");
        }

        return null; // will never get here
    }

    @Override
    @Transactional(readOnly = true)
    public Analysis getAnalysisById(String analysisId) throws LIMSRuntimeException {
        if (analysisId == null) {
            return null;
        }
        try {
            Analysis analysis = entityManager.unwrap(Session.class).get(Analysis.class, analysisId);
            // closeSession(); // CSL remove old
            return analysis;
        } catch (Exception e) {
            handleException(e, "getAnalysisById");
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    @Transactional(readOnly = true)
    public List<Analysis> getAnalysesBySampleIdTestIdAndStatusId(List<Integer> sampleIdList, List<Integer> testIdList,
            List<Integer> statusIdList) throws LIMSRuntimeException {

        if (sampleIdList.isEmpty() || testIdList.isEmpty() || statusIdList.isEmpty()) {
            return new ArrayList<>();
        }
        String sql = "from Analysis a where a.sampleItem.sample.id in (:sampleIdList) and a.test.id in (:testIdList) and a.statusId in (:statusIdList) order by a.releasedDate desc";

        try {
            Query query = entityManager.unwrap(Session.class).createQuery(sql);
            query.setParameterList("sampleIdList", sampleIdList);
            query.setParameterList("testIdList", testIdList);
            query.setParameterList("statusIdList", statusIdList);
            List<Analysis> analysisList = query.list();
            // closeSession(); // CSL remove old
            return analysisList;
        } catch (HibernateException e) {
            handleException(e, "getAnalysesBySampleIdTestIdAndStatusId");
        }

        return null;

    }

//	@Override
//	public Analysis getPatientPreviousAnalysisForTestName(Patient patient, Sample currentSample, String testName) {
//		Analysis previousAnalysis = null;
//		List<Integer> sampIDList = new ArrayList<>();
//		List<Integer> testIDList = new ArrayList<>();
////		TestDAO testDAO = new TestDAOImpl();
////		SampleHumanDAO sampleHumanDAO = new SampleHumanDAOImpl();
//
//		List<Sample> sampList = sampleHumanService.getSamplesForPatient(patient.getId());
//
//		if (sampList.isEmpty() || testService.getTestByName(testName) == null) {
//			return previousAnalysis;
//		}
//
//		testIDList.add(Integer.parseInt(testService.getTestByName(testName).getId()));
//
//		for (Sample sample : sampList) {
//			sampIDList.add(Integer.parseInt(sample.getId()));
//		}
//
//		List<Integer> statusList = new ArrayList<>();
//		statusList.add(Integer.parseInt(StatusService.getInstance().getStatusID(AnalysisStatus.Finalized)));
//
////		AnalysisDAO analysisDAO = new AnalysisDAOImpl();
//		List<Analysis> analysisList = getAnalysesBySampleIdTestIdAndStatusId(sampIDList, testIDList, statusList);
//
//		if (analysisList == null || analysisList.isEmpty()) {
//			return previousAnalysis;
//		}
//
//		for (int i = 0; i < analysisList.size(); i++) {
//			if (i < analysisList.size() - 1 && currentSample.getAccessionNumber()
//					.equals(analysisList.get(i).getSampleItem().getSample().getAccessionNumber())) {
//				previousAnalysis = analysisList.get(i + 1);
//				return previousAnalysis;
//			}
//
//		}
//		return previousAnalysis;
//
//	}

}