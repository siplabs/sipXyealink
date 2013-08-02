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

import static java.lang.String.format;

import org.sipfoundry.sipxconfig.bulk.ldap.LdapManager;
import org.sipfoundry.sipxconfig.device.Device;
import org.sipfoundry.sipxconfig.device.DeviceVersion;
import org.sipfoundry.sipxconfig.device.Profile;
import org.sipfoundry.sipxconfig.device.ProfileLocation;
import org.sipfoundry.sipxconfig.device.ProfileContext;
import org.sipfoundry.sipxconfig.device.ProfileFilter;
import org.sipfoundry.sipxconfig.phone.Line;
import org.sipfoundry.sipxconfig.phone.LineInfo;
import org.sipfoundry.sipxconfig.phone.Phone;
import org.sipfoundry.sipxconfig.phone.PhoneContext;
import org.sipfoundry.sipxconfig.phonebook.PhonebookEntry;
import org.sipfoundry.sipxconfig.setting.Setting;
import org.sipfoundry.sipxconfig.setting.SettingExpressionEvaluator;
import org.sipfoundry.sipxconfig.speeddial.SpeedDial;

import org.apache.commons.lang.ArrayUtils;

/**
 * Yealink abstract phone.
 */
public class YealinkPhone extends Phone {
    public static final String BEAN_ID = "yealink";

    // Common members
    private SpeedDial m_speedDial;

    private LdapManager m_ldapManager;

    public YealinkPhone() {
    }

    public String getDefaultVersionId() {
        DeviceVersion version = getDeviceVersion();
        return version != null ? version.getVersionId() : null;
    }

    @Override
    public void setDeviceVersion(DeviceVersion version) {
        super.setDeviceVersion(version);
        DeviceVersion myVersion = getDeviceVersion();
        if (myVersion == YealinkModel.VER_7X) {
            getModel().setProfileTemplate("yealinkPhone/config_v7x.vm");
            getModel().setSettingsFile("phone-7X.xml");
            getModel().setLineSettingsFile("line-7X.xml");
        } else {
            // we need to explicitly define these here otherwise changing versions will not work
            getModel().setSettingsFile("phone-6X.xml");
            getModel().setLineSettingsFile("line-6X.xml");
        }
    }

    public void setLdapManager(LdapManager ldapManager) {
        m_ldapManager = ldapManager;
    }

    public LdapManager getLdapManager() {
        return m_ldapManager;
    }

    public int getMaxLineCount() {
        YealinkModel model = (YealinkModel) getModel();
        if (null != model) {
            return model.getMaxLineCount();
        }
        return 0;
    }

    public int getMemoryKeyCount() {
        YealinkModel model = (YealinkModel) getModel();
        if (null != model) {
            return model.getMemoryKeyCount();
        } else {
            return 0;
        }
    }

    public boolean getHasHD() {
        YealinkModel model = (YealinkModel) getModel();
        if (null != model) {
            return !model.getNoHD();
        } else {
            return false;
        }
    }

    public String getDirectedCallPickupString() {
        return getPhoneContext().getPhoneDefaults().getDirectedCallPickupCode();
    }

    @Override
    public void initialize() {
        addDefaultBeanSettingHandler(new YealinkPhoneDefaults(getPhoneContext().getPhoneDefaults(), this));
    }

    @Override
    public void initializeLine(Line line) {
        m_speedDial = getPhoneContext().getSpeedDial(this);
        line.addDefaultBeanSettingHandler(new YealinkLineDefaults(getPhoneContext().getPhoneDefaults(), line));
    }

    /**
    * Copy common configuration file.
    */
    @Override
    protected void copyFiles(ProfileLocation location) {
//        YealinkModel model = (YealinkModel) getModel();
    }

    @Override
    protected SettingExpressionEvaluator getSettingsEvaluator() {
        return new YealinkSettingExpressionEvaluator(getModel().getModelId());
    }

    @Override
    public void removeProfiles(ProfileLocation location) {
        Profile[] profiles = getProfileTypes();
        for (Profile profile : profiles) {
            location.removeProfile(profile.getName());
        }
    }

    @Override
    public Profile[] getProfileTypes() {
        YealinkModel model = (YealinkModel) getModel();
        Profile[] profileTypes = new Profile[] {
            new DeviceProfile(getDeviceFilename())
        };

        if (getPhonebookManager().getPhonebookManagementEnabled()) {
            if (model.getUsePhonebook()) {
                profileTypes = (Profile[]) ArrayUtils.add(profileTypes, new DirectoryProfile(getDirectoryFilename(0)));
            }
        }

        if (model.getHasSeparateDialNow()) {
            profileTypes = (Profile[]) ArrayUtils.add(profileTypes, new DialNowProfile(getDialNowFilename()));
        }

        return profileTypes;
    }

    @Override
    public String getProfileFilename() {
        return getSerialNumber();
    }

    public String getDeviceFilename() {
        return format("%s.cfg", getSerialNumber());
    }

    public String getDirectoryFilename(int n) {
        return format("%s-%d-%s", getSerialNumber(), n, YealinkConstants.XML_CONTACT_DATA);
    }

    public String getDialNowFilename() {
        return format("%s-%s", getSerialNumber(), YealinkConstants.XML_DIAL_NOW);
    }

    @Override
    public void restart() {
        sendCheckSyncToFirstLine();
    }

    public SpeedDial getSpeedDial() {
        return m_speedDial;
    }

    /**
     * Each subclass must decide how as much of this generic line information translates into its
     * own setting model.
     */
    @Override
    protected void setLineInfo(Line line, LineInfo info) {
        line.setSettingValue(YealinkConstants.USER_ID_V6X_SETTING, info.getUserId());
        line.setSettingValue(YealinkConstants.DISPLAY_NAME_V6X_SETTING, info.getDisplayName());
        line.setSettingValue(YealinkConstants.PASSWORD_V6X_SETTING, info.getPassword());
        line.setSettingValue(YealinkConstants.REGISTRATION_SERVER_HOST_V6X_SETTING, info.getRegistrationServer());
        line.setSettingValue(YealinkConstants.REGISTRATION_SERVER_PORT_V6X_SETTING, info.getRegistrationServerPort());
        line.setSettingValue(YealinkConstants.VOICE_MAIL_NUMBER_V6X_SETTING, info.getVoiceMail());
    }

    /**
     * Each subclass must decide how as much of this generic line information can be contructed
     * from its own setting model.
     */
    @Override
    protected LineInfo getLineInfo(Line line) {
        LineInfo info = new LineInfo();
        info.setDisplayName(line.getSettingValue(YealinkConstants.DISPLAY_NAME_V6X_SETTING));
        info.setUserId(line.getSettingValue(YealinkConstants.USER_ID_V6X_SETTING));
        info.setPassword(line.getSettingValue(YealinkConstants.PASSWORD_V6X_SETTING));
        info.setRegistrationServer(line.getSettingValue(YealinkConstants.REGISTRATION_SERVER_HOST_V6X_SETTING));
        info.setRegistrationServerPort(line.getSettingValue(YealinkConstants.REGISTRATION_SERVER_PORT_V6X_SETTING));
        info.setVoiceMail(line.getSettingValue(YealinkConstants.VOICE_MAIL_NUMBER_V6X_SETTING));
        return info;
    }

    static class DeviceProfile extends Profile {
        public DeviceProfile(String name) {
            super(name, YealinkConstants.MIME_TYPE_PLAIN);
        }

        @Override
        protected ProfileFilter createFilter(Device device) {
            return null;
        }

        @Override
        protected ProfileContext createContext(Device device) {
            YealinkPhone phone = (YealinkPhone) device;
            YealinkModel model = (YealinkModel) phone.getModel();
            return new YealinkDeviceConfiguration(phone, model.getProfileTemplate());
        }
    }

    static class DialNowProfile extends Profile {
        public DialNowProfile(String name) {
            super(name, YealinkConstants.MIME_TYPE_XML);
        }

        @Override
        protected ProfileFilter createFilter(Device device) {
            return null;
        }

        @Override
        protected ProfileContext createContext(Device device) {
            YealinkPhone phone = (YealinkPhone) device;
            YealinkModel model = (YealinkModel) phone.getModel();
            return new YealinkDialNowConfiguration(phone, model.getDialNowProfileTemplate());
        }
    }

    static class DirectoryProfile extends Profile {
        public DirectoryProfile(String name) {
            super(name, YealinkConstants.MIME_TYPE_PLAIN);
        }

        @Override
        protected ProfileFilter createFilter(Device device) {
            return null;
        }

        @Override
        protected ProfileContext createContext(Device device) {
            YealinkPhone phone = (YealinkPhone) device;
            YealinkModel model = (YealinkModel) phone.getModel();
            PhoneContext phoneContext = phone.getPhoneContext();
            Collection<PhonebookEntry> entries = phoneContext.getPhonebookEntries(phone);
            return new YealinkDirectoryConfiguration(phone, entries, model.getDirectoryProfileTemplate());
        }
    }

    static class YealinkSettingExpressionEvaluator implements SettingExpressionEvaluator {
        private final String m_model;

        public YealinkSettingExpressionEvaluator(String model) {
            m_model = model;
        }

        public boolean isExpressionTrue(String expression, Setting setting_) {
            return m_model.matches(expression);
        }
    }
}
