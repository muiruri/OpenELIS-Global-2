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
* Copyright (C) CIRG, University of Washington, Seattle WA.  All Rights Reserved.
*
*/
package org.openelisglobal.dataexchange.common;

public class AsynchronousExternalSender implements Runnable {

    private IExternalSender sender;
    private ITransmissionResponseHandler responseHandler;
    private String msg;

    public AsynchronousExternalSender(IExternalSender sender, ITransmissionResponseHandler responseHandler,
            String msg) {
        this.sender = sender;
        this.responseHandler = responseHandler;
        this.msg = msg;
    }

    public void sendMessage() {
        Thread thread = new Thread(this);
        thread.start();
    }

    @Override
    public void run() {
        sender.sendMessage();

        if (responseHandler != null) {
            responseHandler.handleResponse(sender.getSendResponse(), sender.getErrors(), msg);
        }
    }

}
