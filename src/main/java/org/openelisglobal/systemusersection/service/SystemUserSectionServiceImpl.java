package org.openelisglobal.systemusersection.service;

import java.util.List;

import org.openelisglobal.common.exception.LIMSDuplicateRecordException;
import org.openelisglobal.common.service.BaseObjectServiceImpl;
import org.openelisglobal.systemusersection.dao.SystemUserSectionDAO;
import org.openelisglobal.systemusersection.valueholder.SystemUserSection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SystemUserSectionServiceImpl extends BaseObjectServiceImpl<SystemUserSection, String>
        implements SystemUserSectionService {
    @Autowired
    protected SystemUserSectionDAO baseObjectDAO;

    SystemUserSectionServiceImpl() {
        super(SystemUserSection.class);
    }

    @Override
    protected SystemUserSectionDAO getBaseObjectDAO() {
        return baseObjectDAO;
    }

    @Override
    @Transactional(readOnly = true)
    public void getData(SystemUserSection systemUserSection) {
        getBaseObjectDAO().getData(systemUserSection);

    }

    @Override
    @Transactional(readOnly = true)
    public List getAllSystemUserSections() {
        return getBaseObjectDAO().getAllSystemUserSections();
    }

    @Override
    @Transactional(readOnly = true)
    public List getPageOfSystemUserSections(int startingRecNo) {
        return getBaseObjectDAO().getPageOfSystemUserSections(startingRecNo);
    }

    @Override
    @Transactional(readOnly = true)
    public List getNextSystemUserSectionRecord(String id) {
        return getBaseObjectDAO().getNextSystemUserSectionRecord(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Integer getTotalSystemUserSectionCount() {
        return getBaseObjectDAO().getTotalSystemUserSectionCount();
    }

    @Override
    @Transactional(readOnly = true)
    public List getPreviousSystemUserSectionRecord(String id) {
        return getBaseObjectDAO().getPreviousSystemUserSectionRecord(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List getAllSystemUserSectionsBySystemUserId(int systemUserId) {
        return getBaseObjectDAO().getAllSystemUserSectionsBySystemUserId(systemUserId);
    }

    @Override
    public String insert(SystemUserSection systemUserSection) {
        if (duplicateSystemUserSectionExists(systemUserSection)) {
            throw new LIMSDuplicateRecordException("Duplicate record exists for " + systemUserSection.getSysUserId());
        }
        return super.insert(systemUserSection);
    }

    @Override
    public SystemUserSection save(SystemUserSection systemUserSection) {
        if (duplicateSystemUserSectionExists(systemUserSection)) {
            throw new LIMSDuplicateRecordException("Duplicate record exists for " + systemUserSection.getSysUserId());
        }
        return super.save(systemUserSection);
    }

    @Override
    public SystemUserSection update(SystemUserSection systemUserSection) {
        if (duplicateSystemUserSectionExists(systemUserSection)) {
            throw new LIMSDuplicateRecordException("Duplicate record exists for " + systemUserSection.getSysUserId());
        }
        return super.update(systemUserSection);
    }

    private boolean duplicateSystemUserSectionExists(SystemUserSection systemUserSection) {
        return baseObjectDAO.duplicateSystemUserSectionExists(systemUserSection);
    }
}
