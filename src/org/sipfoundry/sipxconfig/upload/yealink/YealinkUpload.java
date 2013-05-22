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

public class YealinkUpload extends Upload {

    public YealinkUpload() {
    }

    protected YealinkUpload(String beanId) {
        super(beanId);
    }

    public YealinkUpload(UploadSpecification specification) {
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
