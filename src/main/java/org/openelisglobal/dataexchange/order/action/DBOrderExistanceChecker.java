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
 * Copyright (C) ITECH, University of Washington, Seattle WA.  All Rights Reserved.
 *
 */
package org.openelisglobal.dataexchange.order.action;

import java.util.List;

import org.apache.commons.validator.GenericValidator;
import org.openelisglobal.common.services.StatusService;
import org.openelisglobal.common.services.StatusService.ExternalOrderStatus;
import org.openelisglobal.dataexchange.order.valueholder.ElectronicOrder;
import org.openelisglobal.dataexchange.service.order.ElectronicOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DBOrderExistanceChecker implements IOrderExistanceChecker {

    @Autowired
    private ElectronicOrderService eOrderService;

    @Override
    public CheckResult check(String orderId) {
        if (GenericValidator.isBlankOrNull(orderId)) {
            return CheckResult.NOT_FOUND;
        }

        List<ElectronicOrder> eOrders = eOrderService.getElectronicOrdersByExternalId(orderId);
        if (eOrders == null || eOrders.isEmpty()) {
            return CheckResult.NOT_FOUND;
        }

        ElectronicOrder eOrder = eOrders.get(eOrders.size() - 1);
        if (StatusService.getInstance().getStatusID(ExternalOrderStatus.Cancelled).equals(eOrder.getStatusId())) {
            return CheckResult.ORDER_FOUND_CANCELED;
        }

        if (StatusService.getInstance().getStatusID(ExternalOrderStatus.Entered).equals(eOrder.getStatusId())) {
            return CheckResult.ORDER_FOUND_QUEUED;
        }

        return CheckResult.ORDER_FOUND_INPROGRESS;
    }

}
