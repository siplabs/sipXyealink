/*
 * Copyright (C) 2013 SibTelCom, JSC., certain elements licensed under a Contributor Agreement.
 * Author: Konstantin S. Vishnivetsky
 * E-mail: info@siplabs.ru
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
*/

package org.sipfoundry.sipxconfig.phone.yealink;

import org.sipfoundry.sipxconfig.device.DeviceVersion;
import org.sipfoundry.sipxconfig.phone.PhoneModel;

/**
 * Static differences in yealink models
 */
public final class YealinkModel extends PhoneModel {
    /** Firmware 6x or beyond */
    public static final DeviceVersion VER_6X = new DeviceVersion(YealinkPhone.BEAN_ID, "6X");
    public static final DeviceVersion VER_7X = new DeviceVersion(YealinkPhone.BEAN_ID, "7X");
    public static final DeviceVersion[] SUPPORTED_VERSIONS = new DeviceVersion[] {
        VER_6X, VER_7X
    };

    private DeviceVersion m_deviceVersion;

    private boolean m_hasSeparateDialNow;
    private String m_name;
    private String m_directoryProfileTemplate;
    private String m_dialNowProfileTemplate;
    private int m_memoryKeyCount;

    public YealinkModel() {
    }

    public YealinkModel(String beanId) {
        super(beanId);
    }

    public static DeviceVersion getPhoneDeviceVersion(String version) {
        for (DeviceVersion deviceVersion : SUPPORTED_VERSIONS) {
            if (deviceVersion.getName().contains(version)) {
                return deviceVersion;
            }
        }
        return VER_6X;
    }

    public void setDefaultVersion(DeviceVersion value) {
        m_deviceVersion = value;
    }

    public DeviceVersion getDefaultVersion() {
        return m_deviceVersion;
    }

    public void setName(String value) {
        m_name = value;
    }

    public String getName() {
        return m_name;
    }

    public void setDirectoryProfileTemplate(String value) {
        m_directoryProfileTemplate = value;
    }

    public String getDirectoryProfileTemplate() {
        return m_directoryProfileTemplate;
    }

    public void setDialNowProfileTemplate(String value) {
        m_dialNowProfileTemplate = value;
    }

    public String getDialNowProfileTemplate() {
        return m_dialNowProfileTemplate;
    }

    public int getMemoryKeyCount() {
        return m_memoryKeyCount;
    }

    public void setMemoryKeyCount(int value) {
        m_memoryKeyCount = value;
    }
}
