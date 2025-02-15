package org.openelisglobal.audittrail.dao;

import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.openelisglobal.audittrail.valueholder.History;
import org.openelisglobal.common.daoimpl.BaseDAOImpl;
import org.openelisglobal.common.exception.LIMSRuntimeException;
import org.openelisglobal.common.log.LogEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class HistoryDAOImpl extends BaseDAOImpl<History, String> implements HistoryDAO {
    HistoryDAOImpl() {
        super(History.class);
    }

    @Override
    public List getHistoryByRefIdAndRefTableId(String refId, String tableId) throws LIMSRuntimeException {
        History history = new History();
        history.setReferenceId(refId);
        history.setReferenceTable(tableId);
        return getHistoryByRefIdAndRefTableId(history);
    }

    @Override
    @Transactional(readOnly = true)
    public List getHistoryByRefIdAndRefTableId(History history) throws LIMSRuntimeException {
        String refId = history.getReferenceId();
        String tableId = history.getReferenceTable();
        List list;

        try {
            String sql = "from History h where h.referenceId = :refId and h.referenceTable = :tableId order by h.timestamp desc, h.activity desc";
            org.hibernate.Query query = entityManager.unwrap(Session.class).createQuery(sql);
            query.setInteger("refId", Integer.parseInt(refId));
            query.setInteger("tableId", Integer.parseInt(tableId));
            list = query.list();
        } catch (HibernateException e) {
            LogEvent.logError("AuditTrailDAOImpl", "getHistoryByRefIdAndRefTableId()", e.toString());
            throw new LIMSRuntimeException("Error in AuditTrail getHistoryByRefIdAndRefTableId()", e);
        }
        return list;
    }
}
