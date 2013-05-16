/*
 * Copyright (C) 2013 SibTelCom, JSC., certain elements licensed under a Contributor Agreement.
 * Author: Konstantin S. Vishnivetsky
 * E-mail: info@siplabs.ru
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
*/

package org.sipfoundry.sipxconfig.phone.yealink;

import org.sipfoundry.sipxconfig.device.ProfileContext;

/**
 * Responsible for generating ipmid.cfg
 */
public class yealinkDialNowConfiguration extends ProfileContext {

    public yealinkDialNowConfiguration(yealinkPhone device, String profileTemplate) {
        super(device, profileTemplate);
    }
}
