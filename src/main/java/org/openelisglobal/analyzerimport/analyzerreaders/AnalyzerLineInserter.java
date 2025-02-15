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
package org.openelisglobal.analyzerimport.analyzerreaders;

import java.util.List;

import org.openelisglobal.analyzerresults.service.AnalyzerResultsService;
import org.openelisglobal.analyzerresults.valueholder.AnalyzerResults;
import org.openelisglobal.common.exception.LIMSRuntimeException;
import org.openelisglobal.spring.util.SpringContext;

public abstract class AnalyzerLineInserter {

    protected AnalyzerResultsService analyzerResultService = SpringContext.getBean(AnalyzerResultsService.class);

    protected void persistResults(List<AnalyzerResults> results, String systemUserId) {
        analyzerResultService.insertAnalyzerResults(results, systemUserId);
    }

    protected boolean persistImport(String currentUserId, List<AnalyzerResults> results) {

        if (results.size() > 0) {
            for (AnalyzerResults analyzerResults : results) {
                if (analyzerResults.getTestId().equals("-1")) {
                    analyzerResults.setTestId(null);
                    analyzerResults.setReadOnly(true);
                }
            }

            try {
                persistResults(results, currentUserId);
            } catch (LIMSRuntimeException lre) {
                lre.printStackTrace();
                return false;
            }
        }
        return true;
    }

    public abstract boolean insert(List<String> lines, String currentUserId);

    public abstract String getError();
}
