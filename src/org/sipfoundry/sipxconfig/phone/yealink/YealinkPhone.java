/*
 * Copyright (C) 2013 SibTelCom, JSC., certain elements licensed under a Contributor Agreement.
 * Author: Konstantin S. Vishnivetsky
 * E-mail: info@siplabs.ru
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
*/

package org.sipfoundry.sipxconfig.phone.yealink;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import static java.lang.String.format;

import org.sipfoundry.sipxconfig.bulk.ldap.LdapManager;
import org.sipfoundry.sipxconfig.common.User;
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
import org.sipfoundry.sipxconfig.phonebook.Phonebook;
import org.sipfoundry.sipxconfig.phonebook.PhonebookEntry;
import org.sipfoundry.sipxconfig.phonebook.PhonebookManager;
import org.sipfoundry.sipxconfig.setting.AbstractSettingVisitor;
import org.sipfoundry.sipxconfig.setting.Setting;
import org.sipfoundry.sipxconfig.setting.type.EnumSetting;
import org.sipfoundry.sipxconfig.setting.type.SettingType;
import org.sipfoundry.sipxconfig.setting.SettingExpressionEvaluator;
import org.sipfoundry.sipxconfig.speeddial.SpeedDial;

import org.apache.commons.lang.ArrayUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.sipfoundry.sipxconfig.upload.UploadManager;
import org.sipfoundry.sipxconfig.upload.UploadSpecification;
import org.sipfoundry.sipxconfig.upload.yealink.YealinkUpload;

/**
 * Yealink abstract phone.
 */
public class YealinkPhone extends Phone {
    private static final Log LOG = LogFactory.getLog(YealinkPhone.class);
    public static final String BEAN_ID = "yealink";

    // Common members
    private SpeedDial m_speedDial;

    private LdapManager m_ldapManager;

    private UploadManager m_uploadManager;

    public YealinkPhone() {
        if (null == super.getSerialNumber()) {
            super.setSerialNumber("001565");
        }
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

    public void setLdapManager(LdapManager manager) {
        m_ldapManager = manager;
    }

    public LdapManager getLdapManager() {
        return m_ldapManager;
    }

    public void setuploadManager(UploadManager manager) {
        m_uploadManager = manager;
    }

    public UploadManager getUploadManager() {
        return m_uploadManager;
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
        return getModel().isSupported(YealinkConstants.FEATURE_HDSOUND);
    }

    public String getDirectedCallPickupString() {
        return getPhoneContext().getPhoneDefaults().getDirectedCallPickupCode();
    }

    @Override
    public void initialize() {
        addDefaultBeanSettingHandler(new YealinkPhoneDefaults(getPhoneContext().getPhoneDefaults(), this));
    }


    @Override
    public void setSettings(Setting settings) {
        settings.acceptVisitor(new PhonebooksSetter("remote_phonebook\\.data\\.[1-4]\\.name"));
        settings.acceptVisitor(new RingtonesSetter("(distinctive_ring_tones\\.alert_info\\.[1-8]\\.ringer)|((phone_setting|ringtone)\\.ring_type)"));
        super.setSettings(settings);
    }

    @Override
    public void initializeLine(Line line) {
        m_speedDial = getPhoneContext().getSpeedDial(this);
        line.addDefaultBeanSettingHandler(new YealinkLineDefaults(getPhoneContext().getPhoneDefaults(), line));
    }

    /**
    * Copy common configuration files.
    */
    @Override
    protected void copyFiles(ProfileLocation location) {
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
//        YealinkModel model = (YealinkModel) getModel();
        Profile[] profileTypes = new Profile[] {
            new DeviceProfile(getDeviceFilename())
        };

        if (getPhonebookManager().getPhonebookManagementEnabled()) {
            if (getModel().isSupported(YealinkConstants.FEATURE_PHONEBOOK)) {
                PhonebookManager pbm = getPhonebookManager();
                if (null != pbm) {
                    User user = getPrimaryUser();
                    if (null != user) {
                        Collection<Phonebook> phoneBooks = pbm.getAllPhonebooksByUser(user);
                        if (null != phoneBooks) {
                            Integer i = 0;
                            for(Phonebook pb : phoneBooks) {
                                if (null != pb) {
                                    if (pb.getShowOnPhone()) {
                                        profileTypes = (Profile[]) ArrayUtils.add(profileTypes, new DirectoryProfile(getDirectoryFilename(i++), pb));
                                    }
                                }
                            }
                        }
                    }
                }
            }
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

    @Override
    public void restart() {
        sendCheckSyncToFirstLine();
    }

    public SpeedDial getSpeedDial() {
        return m_speedDial;
    }

    private Collection<String> getFileListForDirectory(String dirName) {
        Collection<String> files = new ArrayList<String>();
        File rintonesLoc = new File(dirName);
        if (rintonesLoc.exists()) {
            if (rintonesLoc.isDirectory()) {
                File[] ringtoneFiles = rintonesLoc.listFiles();
                for (File f : ringtoneFiles) {
                    if (f.isFile()) {
                        files.add(f.getName());
                    }
                }
            }
        }
        return files;
    }

    private String getMappedDirectoryName(String dirName) {
        UploadSpecification ylUploadSpec = getUploadManager().getSpecification("yealinkFiles");
        YealinkUpload ylUpload = (YealinkUpload)getUploadManager().newUpload(ylUploadSpec);
        return ylUpload.getDestinationDirectory() + dirName;
    }

    public Collection<String> getRingTones() {
        return getModel().isSupported(YealinkConstants.FEATURE_RINGTONES)?getFileListForDirectory(getMappedDirectoryName(YealinkUpload.DIR_YEALINK + YealinkUpload.DIR_RINGTONES)):Collections.EMPTY_LIST;
    }

    public Collection<String> getWallPapers() {
        return getModel().isSupported(YealinkConstants.FEATURE_WALLPAPERS)?getFileListForDirectory(getMappedDirectoryName(YealinkUpload.DIR_YEALINK + YealinkUpload.DIR_WALLPAPERS)):Collections.EMPTY_LIST;
    }

    public Collection<String> getScreenSavers() {
        return getModel().isSupported(YealinkConstants.FEATURE_SCREENSAVERS)?getFileListForDirectory(getMappedDirectoryName(YealinkUpload.DIR_YEALINK + YealinkUpload.DIR_SCREENSAVERS)):Collections.EMPTY_LIST;
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

    static class DirectoryProfile extends Profile {
       private Phonebook m_phonebook;

        public DirectoryProfile(String name, Phonebook phonebook) {
            super(name, YealinkConstants.MIME_TYPE_PLAIN);
            m_phonebook = phonebook;
        }

        @Override
        protected ProfileFilter createFilter(Device device) {
            return null;
        }

        @Override
        protected ProfileContext createContext(Device device) {
            YealinkPhone phone = (YealinkPhone) device;
            YealinkModel model = (YealinkModel) phone.getModel();
            Collection<PhonebookEntry> entries = phone.getPhonebookManager().getEntries(m_phonebook);
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

    private class PhonebooksSetter extends AbstractSettingVisitor {
        private String m_pattern;
        private Collection<Phonebook> m_pbs = new ArrayList<Phonebook>();

        public PhonebooksSetter(String pattern) {
            m_pattern = pattern;
            PhonebookManager pbm = getPhonebookManager();
            if (null != pbm) {
                User user = getPrimaryUser();
                if (null != user) {
                    Collection<Phonebook> phoneBooks = pbm.getAllPhonebooksByUser(user);
                    if (null != phoneBooks) {
                        Integer i = 0;
                        for(Phonebook pb : phoneBooks) {
                            if (null != pb) {
                                if (pb.getShowOnPhone()) {
                                    m_pbs.add(pb);
                                }
                            }
                        }
                    }
                }
            }
        }

        @Override
        public void visitSetting(Setting setting) {
            if (setting.getType() instanceof EnumSetting) {
                Pattern pattern = Pattern.compile(m_pattern);
                Matcher matcher = pattern.matcher(setting.getName());
                matcher.lookingAt();
                if (matcher.matches()) {
                    EnumSetting ringTonesSetting = (EnumSetting)setting.getType();
                    ringTonesSetting.addEnum(null, null);
                    for(Phonebook pb : m_pbs) {
                        ringTonesSetting.addEnum(pb.getName(), pb.getName());
                    }
                }
            }
        }
    }

    private class RingtonesSetter extends AbstractSettingVisitor {
        private String m_pattern;

        public RingtonesSetter(String pattern) {
            m_pattern = pattern;
        }

        @Override
        public void visitSetting(Setting setting) {
            if (setting.getType() instanceof EnumSetting) {
                Pattern pattern = Pattern.compile(m_pattern);
                Matcher matcher = pattern.matcher(setting.getName());
                matcher.lookingAt();
                if (matcher.matches()) {
                    EnumSetting ringTonesSetting = (EnumSetting)setting.getType();
                    if (setting.getName().equals("ringtone.ring_type")) {
                        ringTonesSetting.addEnum("common", "common");
                    }
                    for (String rt : getRingTones()) {
                        ringTonesSetting.addEnum(rt, rt);
                    }
                }
            }
        }
    }
}
