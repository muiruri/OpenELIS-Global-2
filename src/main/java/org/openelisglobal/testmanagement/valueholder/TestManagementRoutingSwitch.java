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
package org.openelisglobal.testmanagement.valueholder;

import org.openelisglobal.common.action.IActionConstants;

/**
 * @author benzd1 bugzilla 2053
 */
public class TestManagementRoutingSwitch implements IActionConstants {

    private boolean resultsEntrySwitch = false;
    private boolean qaEntrySwitch = false;
    // bugzilla 2504
    private boolean qaEntryLineListingSwitch = false;

    public void TestManagementRoutingSwitch() {
        resultsEntrySwitch = false;
        qaEntrySwitch = false;
        qaEntryLineListingSwitch = false;
    }

    public boolean isResultsEntrySwitch() {
        return resultsEntrySwitch;
    }

    public void setResultsEntrySwitch(boolean resultsEntrySwitch) {
        this.resultsEntrySwitch = resultsEntrySwitch;
    }

    public boolean isQaEntryEntrySwitch() {
        return qaEntrySwitch;
    }

    public void setQaEntryEntrySwitch(boolean qaEntrySwitch) {
        this.qaEntrySwitch = qaEntrySwitch;
    }

    public boolean isQaEntryEntryLineListingSwitch() {
        return qaEntryLineListingSwitch;
    }

    public void setQaEntryEntryLineListingSwitch(boolean qaEntryLineListingSwitch) {
        this.qaEntryLineListingSwitch = qaEntryLineListingSwitch;
    }

}
