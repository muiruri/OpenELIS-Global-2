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
package org.openelisglobal.qaevent.dao;

import java.util.List;

import org.openelisglobal.common.dao.BaseDAO;
import org.openelisglobal.common.exception.LIMSRuntimeException;
import org.openelisglobal.qaevent.valueholder.QaEvent;

/**
 * @author diane benz
 *
 *         To change this generated comment edit the template variable
 *         "typecomment": Window>Preferences>Java>Templates. To enable and
 *         disable the creation of type comments go to
 *         Window>Preferences>Java>Code Generation.
 */
public interface QaEventDAO extends BaseDAO<QaEvent, String> {

//	public boolean insertData(QaEvent qaEvent) throws LIMSRuntimeException;

//	public void deleteData(List qaEvents) throws LIMSRuntimeException;

    public List getAllQaEvents() throws LIMSRuntimeException;

    public List getPageOfQaEvents(int startingRecNo) throws LIMSRuntimeException;

    public List getQaEvents(String filter) throws LIMSRuntimeException;

    public void getData(QaEvent qaEvent) throws LIMSRuntimeException;

//	public void updateData(QaEvent qaEvent) throws LIMSRuntimeException;

    public List getNextQaEventRecord(String id) throws LIMSRuntimeException;

    public List getPreviousQaEventRecord(String id) throws LIMSRuntimeException;

    public QaEvent getQaEventByName(QaEvent qaEvent) throws LIMSRuntimeException;

    public Integer getTotalQaEventCount() throws LIMSRuntimeException;

    boolean duplicateQaEventExists(QaEvent qaEvent) throws LIMSRuntimeException;

}
