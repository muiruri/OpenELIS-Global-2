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
package org.openelisglobal.common.provider.autocomplete;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openelisglobal.spring.util.SpringContext;
import org.openelisglobal.testtrailer.service.TestTrailerService;

/**
 * An example servlet that responds to an ajax:autocomplete tag action. This
 * servlet would be referenced by the baseUrl attribute of the JSP tag.
 * <p>
 * This servlet should generate XML in the following format:
 * </p>
 * <code><![CDATA[<?xml version="1.0"?>
 * <list>
 *   <item value="Item1">First Item</item>
 *   <item value="Item2">Second Item</item>
 *   <item value="Item3">Third Item</item>
 * </list>]]></code>
 * 
 * @author Darren L. Spurgeon
 */
public class TestTrailerAutocompleteProvider extends BaseAutocompleteProvider {

    protected TestTrailerService testTrailerService = SpringContext.getBean(TestTrailerService.class);

    /**
     * @see org.ajaxtags.demo.servlet.BaseAjaxServlet#getXmlContent(javax.servlet.http.HttpServletRequest,
     *      javax.servlet.http.HttpServletResponse)
     */
    public List processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // System.out.println("I am in TestTrailerAutocompleteProvider "
        // + request.getParameter("testTrailerName"));
        String testTrailerName = request.getParameter("testTrailerName");
        // System.out.println("TestTrailerAutocompleteProvider testTrailerName "
        // + testTrailerName);
        List list = testTrailerService.getTestTrailers(testTrailerName);
        // System.out.println("TestTrailerAutocompleteProvider list "
        // + list.size());

        return list;
    }

}
