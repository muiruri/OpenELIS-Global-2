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
*/
package org.openelisglobal.samplepdf.daoimpl;

import java.util.List;

import org.hibernate.Session;
import org.openelisglobal.common.daoimpl.BaseDAOImpl;
import org.openelisglobal.common.exception.LIMSRuntimeException;
import org.openelisglobal.common.log.LogEvent;
import org.openelisglobal.samplepdf.dao.SamplePdfDAO;
import org.openelisglobal.samplepdf.valueholder.SamplePdf;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Hung Nguyen
 */
@Component
@Transactional
public class SamplePdfDAOImpl extends BaseDAOImpl<SamplePdf, String> implements SamplePdfDAO {

    public SamplePdfDAOImpl() {
        super(SamplePdf.class);
    }

    @Override
    public boolean isAccessionNumberFound(int accessionNumber) throws LIMSRuntimeException {
        Boolean isFound = false;
        try {
            String sql = "from SamplePdf s where s.accessionNumber = :param and s.allowView='Y'";
            org.hibernate.Query query = entityManager.unwrap(Session.class).createQuery(sql);
            query.setParameter("param", accessionNumber);
            List list = query.list();
            if ((list != null) && !list.isEmpty()) {
                isFound = true;
            }
            // entityManager.unwrap(Session.class).flush(); // CSL remove old
            // entityManager.unwrap(Session.class).clear(); // CSL remove old
        } catch (Exception e) {
            // bugzilla 2154
            LogEvent.logError("SamplePdfDAOImpl", "isAccessionNumberFound()", e.toString());
            throw new LIMSRuntimeException("Error in SamplePdf isAccessionNumberFound()", e);
        }

        return isFound;
    }

    // bugzilla 2529,2530,2531
    @Override
    @Transactional(readOnly = true)
    public SamplePdf getSamplePdfByAccessionNumber(SamplePdf samplePdf) throws LIMSRuntimeException {
        try {
            String sql = "from SamplePdf s where s.accessionNumber = :param";
            org.hibernate.Query query = entityManager.unwrap(Session.class).createQuery(sql);
            query.setParameter("param", samplePdf.getAccessionNumber());

            List list = query.list();
            if ((list != null) && !list.isEmpty()) {
                samplePdf = (SamplePdf) list.get(0);
            }

            // entityManager.unwrap(Session.class).flush(); // CSL remove old
            // entityManager.unwrap(Session.class).clear(); // CSL remove old

        } catch (Exception e) {
            LogEvent.logError("SamplePdfDAOImpl", "getSamplePdfByAccessionNumber()", e.toString());
            throw new LIMSRuntimeException("Error in SamplePdf getSamplePdfByAccessionNumber()", e);
        }

        return samplePdf;
    }
}