/*
 * Copyright (C) 2013 SibTelCom, JSC., certain elements licensed under a Contributor Agreement.
 * Author: Konstantin S. Vishnivetsky
 * E-mail: info@siplabs.ru
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
*/

package org.sipfoundry.sipxconfig.phone.yealink;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.sipfoundry.sipxconfig.setting.AbstractSettingVisitor;
import org.sipfoundry.sipxconfig.setting.Setting;
import org.sipfoundry.sipxconfig.setting.SettingImpl;
import org.sipfoundry.sipxconfig.setting.SettingArray;
import org.sipfoundry.sipxconfig.setting.type.EnumSetting;
import org.sipfoundry.sipxconfig.setting.type.MultiEnumSetting;

public abstract class YealinkEnumSetter extends AbstractSettingVisitor {
    private static final Log LOG = LogFactory.getLog(YealinkEnumSetter.class);
    private String m_pattern;

    public YealinkEnumSetter(String pattern) {
        m_pattern = pattern;
    }

    public void setPattern(String value) {
        m_pattern = value;
    }

    public String getPattern() {
        return m_pattern;
    }

    @Override
    public void visitSetting(Setting setting) {
        if ((setting.getType() instanceof EnumSetting)|(setting.getType() instanceof MultiEnumSetting)) {
            if (setting.getPath().matches(getPattern())) {
                LOG.info("SETTINGS: " + setting.getName()  + "(" + setting.getPath() + ")");
                if (setting.getType().getName().equals("enum")) {
                    addEnums(setting.getName(), setting.getIndex(), (EnumSetting)setting.getType());
                } else if (setting.getType().getName().equals("multiEnum")) {
                    addMultiEnums(setting.getName(), setting.getIndex(), (MultiEnumSetting)setting.getType());
                }
            }
        }
    }

    // Override this method in implementation to fill enum
    protected void addEnums(String settingName, Integer settingIndex, EnumSetting setting) {}

    // Override this method in implementation to fill multi-enum
    protected void addMultiEnums(String settingName, Integer settingIndex, MultiEnumSetting setting) {}

};
