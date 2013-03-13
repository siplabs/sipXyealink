/*
 * Copyright (C) 2013 SibTelCom, JSC., certain elements licensed under a Contributor Agreement.
 * Author: Konstantin S. Vishnivetsky
 * E-mail: info@siplabs.ru
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
*/

package org.sipfoundry.sipxconfig.phone.yealink;

import java.util.Collection;
import java.util.Collections;

import org.sipfoundry.sipxconfig.device.ProfileContext;
import org.sipfoundry.sipxconfig.speeddial.Button;

/**
 * Responsible for generating ipmid.cfg
 */
public class yealinkDeviceConfiguration extends ProfileContext {

    public yealinkDeviceConfiguration(yealinkPhone device, String profileTemplate) {
        super(device, profileTemplate);
    }

    public Collection<Button> getSpeedDial() {
        yealinkPhone phone = (yealinkPhone) getDevice();
        if (null == phone.getSpeedDial()) {
            return Collections.emptyList();
        } else {
            return phone.getSpeedDial().getButtons();
        }
    }
}
