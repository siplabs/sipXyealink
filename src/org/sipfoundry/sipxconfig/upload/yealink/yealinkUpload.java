/*
 * Copyright (C) 2013 SibTelCom, JSC., certain elements licensed under a Contributor Agreement.
 * Author: Konstantin S. Vishnivetsky
 * E-mail: info@siplabs.ru
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
*/

package org.sipfoundry.sipxconfig.upload.yealink;

import org.sipfoundry.sipxconfig.upload.Upload;
import org.sipfoundry.sipxconfig.upload.UploadSpecification;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class yealinkUpload extends Upload {

    private static final Log LOG = LogFactory.getLog(yealinkUpload.class);

    public yealinkUpload() {
    }

    protected yealinkUpload(String beanId) {
	super(beanId);
    }

    public yealinkUpload(UploadSpecification specification) {
	super(specification);
    }

    @Override
    public void deploy() {
	super.deploy();
    }

    @Override
    public void undeploy() {
	super.undeploy();
    }
}
