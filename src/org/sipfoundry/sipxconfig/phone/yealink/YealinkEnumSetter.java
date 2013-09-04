/*
 * Copyright (C) 2013 SibTelCom, JSC., certain elements licensed under a Contributor Agreement.
 * Author: Konstantin S. Vishnivetsky
 * E-mail: info@siplabs.ru
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
*/

package org.sipfoundry.sipxconfig.phone.yealink;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

import org.sipfoundry.sipxconfig.setting.AbstractSettingVisitor;
import org.sipfoundry.sipxconfig.setting.Setting;
import org.sipfoundry.sipxconfig.setting.type.EnumSetting;

public abstract class YealinkEnumSetter extends AbstractSettingVisitor {
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
        if (setting.getType() instanceof EnumSetting) {
            Pattern pattern = Pattern.compile(getPattern());
            Matcher matcher = pattern.matcher(setting.getName());
            matcher.lookingAt();
            if (matcher.matches()) {
                addEnums(setting.getName(), (EnumSetting)setting.getType());
            }
        }
    }

    protected abstract void addEnums(String settingName, EnumSetting setting);
};
