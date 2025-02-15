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
package org.openelisglobal.systemusermodule.dao;

import java.util.List;

import org.openelisglobal.common.dao.BaseDAO;
import org.openelisglobal.common.exception.LIMSRuntimeException;
import org.openelisglobal.systemusermodule.valueholder.PermissionModule;

/**
 * @author Hung Nguyen (Hung.Nguyen@health.state.mn.us) .
 */
public interface PermissionModuleDAO<T extends PermissionModule> extends BaseDAO<T, String> {

    public static final String SUPERVISOR = "Supervisor";

//	public boolean insertData(T permissionModule) throws LIMSRuntimeException;

//	public void deleteData(List permissionModules) throws LIMSRuntimeException;

    public List getAllPermissionModules() throws LIMSRuntimeException;

    public List getPageOfPermissionModules(int startingRecNo) throws LIMSRuntimeException;

    public void getData(T permissionModule) throws LIMSRuntimeException;

//	public void updateData(T permissionModule) throws LIMSRuntimeException;

    public List getNextPermissionModuleRecord(String id) throws LIMSRuntimeException;

    public List getPreviousPermissionModuleRecord(String id) throws LIMSRuntimeException;

    public Integer getTotalPermissionModuleCount() throws LIMSRuntimeException;

    public List getAllPermissionModulesByAgentId(int systemUserId) throws LIMSRuntimeException;

//	public boolean isAgentAllowedAccordingToName(String id, String string) throws LIMSRuntimeException;

    public boolean doesUserHaveAnyModules(int userId) throws LIMSRuntimeException;

}
