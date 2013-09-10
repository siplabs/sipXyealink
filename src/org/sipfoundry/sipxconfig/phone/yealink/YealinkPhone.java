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
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import static java.lang.String.format;

import org.sipfoundry.sipxconfig.registrar.RegistrarSettings;
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
import org.sipfoundry.sipxconfig.phone.PhoneModel;
import org.sipfoundry.sipxconfig.phone.PhoneContext;
import org.sipfoundry.sipxconfig.phonebook.Phonebook;
import org.sipfoundry.sipxconfig.phonebook.PhonebookEntry;
import org.sipfoundry.sipxconfig.phonebook.PhonebookManager;
import org.sipfoundry.sipxconfig.setting.AbstractSettingVisitor;
import org.sipfoundry.sipxconfig.setting.Setting;
import org.sipfoundry.sipxconfig.setting.ConditionalSettingImpl;
import org.sipfoundry.sipxconfig.setting.type.EnumSetting;
import org.sipfoundry.sipxconfig.setting.type.MultiEnumSetting;
import org.sipfoundry.sipxconfig.setting.type.StringSetting;
import org.sipfoundry.sipxconfig.setting.type.SettingType;
import org.sipfoundry.sipxconfig.setting.SettingExpressionEvaluator;
import org.sipfoundry.sipxconfig.speeddial.SpeedDial;
import org.sipfoundry.sipxconfig.speeddial.Button;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

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

    private RegistrarSettings m_registrarSettings;

    private LdapManager m_ldapManager;

    private UploadManager m_uploadManager;

    public YealinkPhone() {
        if (null == getSerialNumber()) {
            setSerialNumber(YealinkConstants.MAC_PREFIX);
        }
    }

    @Override
    public void setModel(PhoneModel model) {
        super.setModel(model);
        setDeviceVersion(((YealinkModel) model).getDefaultVersion());
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

    public void setRegistrarSettings(RegistrarSettings rs) {
        m_registrarSettings = rs;
    }

    public RegistrarSettings getRegistrarSettings() {
        return m_registrarSettings;
    }

    public void setLdapManager(LdapManager manager) {
        m_ldapManager = manager;
    }

    public LdapManager getLdapManager() {
        return m_ldapManager;
    }

    public void setUploadManager(UploadManager manager) {
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

    /**
    *
    * @deprecated Use getMaxDSSKeyCount() instead!!!
    */
    @Deprecated
    public int getMemoryKeyCount() {
        YealinkModel model = (YealinkModel) getModel();
        if (null != model) {
            return model.getMemoryKeyCount();
        } else {
            return 0;
        }
    }

    public int getMaxDSSKeyCount() {
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
// DSS keys routines
    private boolean isDSSLineKey(Integer i) {
        if (getModel().getModelId().matches("yealinkPhoneSIPT46.*")) {
            return (i > 4)&&(i < 4 + 1 + getModel().getMaxLineCount());
        } else if (getModel().getModelId().matches("yealinkPhoneSIPT4[12].*")) {
            return (i > 2)&&(i < 2 + 1 + getModel().getMaxLineCount());
        } else if (getModel().getModelId().matches("yealinkPhoneSIPT[123].*")) {
            return i < 1 + getModel().getMaxLineCount();
        } else {
            return false;
        }
    }

    private Setting createSetting(String stName, String stId, String sName, String defaultValue) {
        SettingType st;
        if (stName.equals("enum")) {
            st = new EnumSetting();
        } else {
            st = new StringSetting();
        }
        st.setId(stId);
        ConditionalSettingImpl s = new ConditionalSettingImpl();
        s.setName(sName);
        s.setType(st);
        if (null == s.getValue()) {
            s.setValue(defaultValue);
        }
        return s;
    }

    private void addDSSKeySettings(Setting setting, Integer i, String value, String label, String defaultType) {
        String finalType = (isDSSLineKey(i)?"15":defaultType);

        if (getModel().getModelId().matches("yealinkPhoneSIPT46.*")) {
            setting.addSetting(createSetting("enum", "SIPT4X_DSS_type", String.format("linekey.%d.type", i+1), finalType)); // Set line keys for SIPT46(5-9)
        } else if (getModel().getModelId().matches("yealinkPhoneSIPT4[12].*")) {
            setting.addSetting(createSetting("enum", "SIPT4X_DSS_type", String.format("linekey.%d.type", i+1), finalType)); // Set line keys for SIPT4[12](3-5)
        }
        if (getModel().getModelId().matches("yealinkPhoneSIPT46.*")) {
            setting.addSetting(createSetting("enum", "line_type", String.format("linekey.%d.line", i+1), (isDSSLineKey(i)?String.format("%d", i-4):"0"))); // Set line keys for SIPT46(5-9)
        } else if (getModel().getModelId().matches("yealinkPhoneSIPT4[12].*")) {
            setting.addSetting(createSetting("enum", null, String.format("linekey.%d.line", i+1), (isDSSLineKey(i)?String.format("%d", i-2):"1"))); // Set line keys for SIPT4[12](3-5)
        } else {
            setting.addSetting(createSetting("enum", null, String.format("linekey.%d.line", i+1), isDSSLineKey(i)?String.format("%d", i):"1"));
        }
        setting.addSetting(createSetting("string", null, String.format("linekey.%d.value", i+1), value));
        if (getModel().getModelId().matches("yealinkPhoneSIPT[1-3].*")) {
            setting.addSetting(createSetting("enum", null, String.format("linekey.%d.xml_phonebook", i+1), "0"));
        }
        if (getModel().getModelId().matches("yealinkPhoneSIPT4.*")) {
            setting.addSetting(createSetting("string", null, String.format("linekey.%d.extension", i+1), getRegistrarSettings().getDirectedCallPickupCode()));
            setting.addSetting(createSetting("string", null, String.format("linekey.%d.label", i+1), label));
        } else {
            setting.addSetting(createSetting("enum", "DKtype_type", String.format("linekey.%d.type", i+1), isDSSLineKey(i)?"15":"0"));
            setting.addSetting(createSetting("string", null, String.format("linekey.%d.pickup_value", i+1), getRegistrarSettings().getDirectedCallPickupCode()));
        }
    }

    @Override
    public void setSettings(Setting settings) {
        SpeedDial sd = getPhoneContext().getSpeedDial(this);
        List<Button> sdButtons = null!=sd?sd.getButtons():new ArrayList<Button>();

        YealinkModel model = (YealinkModel) getModel();
        if (null != model) {
            Setting lineKeys = settings.getSetting("DSSKeys/line-keys");
            if (null != lineKeys) {
                Integer sdi = 0;
                for (Integer i = 0; i < getMaxLineCount() + getMaxDSSKeyCount(); i++) {
                    Button sdButton = isDSSLineKey(i)?null:(sdi<sdButtons.size()?sdButtons.get(sdi++):null);
                    addDSSKeySettings(lineKeys, i, null==sdButton?null:sdButton.getNumber(), null==sdButton?null:sdButton.getLabel(), null==sdButton?"0":(sdButton.isBlf()?"16":"13"));
                }
            }
        }

        settings.acceptVisitor(new PhonebooksSetter("remote_phonebook\\.data\\.[1-5]\\.name"));
        settings.acceptVisitor(new PhonebooksSelectSetter(".*\\.xml_phonebook"));
        settings.acceptVisitor(new RingtonesSetter("(distinctive_ring_tones\\.alert_info\\.[0-9]+\\.ringer)|((phone_setting|ringtone)\\.ring_type)"));
        // Commmon
        settings.acceptVisitor(new LineCountSetter(".*\\.((line)|(dialplan\\.area_code\\.line_id))", 1));
        // For W52
        settings.acceptVisitor(new LineCountSetter(".*\\.((dial_out_default_line)|(incoming_lines)|(dial_out_lines))", 1));
        settings.acceptVisitor(new DSSKeySetter("linekey\\.[0-9]+\\.type", getModel().getModelId().matches("yealinkPhoneSIPT4.*")?YealinkConstants.DKTYPES_V71:YealinkConstants.DKTYPES_V70));
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

    public Collection<String> getLanguages() {
        return getModel().isSupported(YealinkConstants.FEATURE_LANGUAGES)?getFileListForDirectory(getMappedDirectoryName(YealinkUpload.DIR_YEALINK + YealinkUpload.DIR_LANGUAGES)):Collections.EMPTY_LIST;
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

    private class PhonebooksSetter extends YealinkEnumSetter {
        private Collection<Phonebook> m_pbs = new ArrayList<Phonebook>();

        public PhonebooksSetter(String pattern) {
            super(pattern);
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
        protected void addEnums(String settingName, EnumSetting enumSetting) {
            // Clean enumerator before adding new values for model.
            enumSetting.clearEnums();
            enumSetting.addEnum(null, null);
            for(Phonebook pb : m_pbs) {
                enumSetting.addEnum(pb.getName(), pb.getName());
            }
        }
    }

    private class PhonebooksSelectSetter extends YealinkEnumSetter {

        public PhonebooksSelectSetter(String pattern) {
            super(pattern);
        }

        @Override
        protected void addEnums(String settingName, EnumSetting enumSetting) {
            enumSetting.clearEnums();
            for(Integer i = 0; i < 5; i++) {
                enumSetting.addEnum(i.toString(), null);
            }
        }
    }

    private class RingtonesSetter extends YealinkEnumSetter {

        public RingtonesSetter(String pattern) {
            super(pattern);
        }

        @Override
        protected void addEnums(String settingName, EnumSetting enumSetting) {
            if (settingName.equals("ringtone.ring_type")) {
                enumSetting.addEnum("common", "common");
            }
            for (String rt : getRingTones()) {
                enumSetting.addEnum(rt, rt);
            }
        }
    }

    private class LineCountSetter extends YealinkEnumSetter {
        private Integer m_base = 0;

        public LineCountSetter(String pattern, Integer base) {
            super(pattern);
            m_base = base;
        }

        @Override
        protected void addEnums(String settingName, EnumSetting enumSetting) {
            // Celan enumerator before adding new values for model.
            enumSetting.clearEnums();
            for (Integer l = 0; l<getMaxLineCount(); l++) {
                Line line = null;
                String userName = null;
                if (l < getLines().size()) {
                    line = getLine(l);
                    userName = line.getUserName();
                }
                enumSetting.addEnum(String.format("%d", l + m_base), null==line?null:(null==userName?String.format("%d", l + m_base):String.format("%d (%s)", l + m_base, userName)));
            }
        }

        @Override
        protected void addMultiEnums(String settingName, MultiEnumSetting enumSetting) {
            // Celan enumerator before adding new values for model.
            enumSetting.clearEnums();
            for (Integer l = 0; l<getMaxLineCount(); l++) {
                Line line = null;
                String userName = null;
                if (l < getLines().size()) {
                    line = getLine(l);
                    userName = line.getUserName();
                }
                enumSetting.addEnum(String.format("enum%d", l + m_base), null==line?String.format("%d", l + m_base):(null==userName?String.format("%d", l + m_base):String.format("%d (%s)", l + m_base, userName)));
            }
        }
    }

    private class DSSKeySetter extends YealinkEnumSetter {
        private String m_keyTypes;

        public DSSKeySetter(String pattern, String keyTypes) {
            super(pattern);
            m_keyTypes = keyTypes;
        }

        @Override
        protected void addEnums(String settingName, EnumSetting enumSetting) {
            enumSetting.clearEnums();
            List<String> keyTypesList = Arrays.asList(StringUtils.split(m_keyTypes, ','));
            for(String dkt : keyTypesList) {
                enumSetting.addEnum(dkt, null);
            }
        }
    }
}
