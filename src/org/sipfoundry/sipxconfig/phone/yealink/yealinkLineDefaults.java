/*
 *
 *
 * Author: Konstantin S. Vishnivetsky
 * E-mail: info@siplabs.ru
 * Copyright (C) 2011 SibTelCom, JSC., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 *
 */
package org.sipfoundry.sipxconfig.phone.yealink;

import java.io.File;
import java.util.Collection;
import java.util.List;

import static java.lang.String.format;

import org.sipfoundry.sipxconfig.common.SipUri;
import org.sipfoundry.sipxconfig.common.User;
import org.sipfoundry.sipxconfig.device.Device;
import org.sipfoundry.sipxconfig.device.DeviceDefaults;
import org.sipfoundry.sipxconfig.device.Profile;
import org.sipfoundry.sipxconfig.device.ProfileLocation;
import org.sipfoundry.sipxconfig.device.RestartException;
import org.sipfoundry.sipxconfig.device.ProfileContext;
import org.sipfoundry.sipxconfig.device.ProfileFilter;
import org.sipfoundry.sipxconfig.phone.Line;
import org.sipfoundry.sipxconfig.phone.LineInfo;
import org.sipfoundry.sipxconfig.phone.Phone;
import org.sipfoundry.sipxconfig.phone.PhoneContext;
import org.sipfoundry.sipxconfig.phonebook.PhonebookEntry;
import org.sipfoundry.sipxconfig.setting.SettingEntry;
import org.sipfoundry.sipxconfig.speeddial.Button;
import org.sipfoundry.sipxconfig.speeddial.SpeedDial;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.lang.ArrayUtils;


public class yealinkLineDefaults {
    private final DeviceDefaults m_defaults;
    private final Line m_line;

    yealinkLineDefaults(DeviceDefaults defaults, Line line) {
	m_defaults = defaults;
	m_line = line;
    }

    @SettingEntry(paths = {yealinkConstants.AUTH_ID_SETTING, yealinkConstants.USER_ID_SETTING})
    public String getUserName() {
	String userName = null;
	User user = m_line.getUser();
	if (user != null) {
	    userName = user.getUserName();
	}
	return userName;
    }

    @SettingEntry(path = yealinkConstants.DISPLAY_NAME_SETTING)
    public String getDisplayName() {
	String displayName = null;
	User user = m_line.getUser();
	if (user != null) {
	    displayName = user.getDisplayName();
	}
	return displayName;
    }

    @SettingEntry(path = yealinkConstants.PASSWORD_SETTING)
    public String getPassword() {
	String password = null;
	User user = m_line.getUser();
	if (user != null) {
	    password = user.getSipPassword();
	}
	return password;
    }

    @SettingEntry(path = yealinkConstants.REGISTRATION_SERVER_HOST_SETTING)
    public String getRegistrationServer() {
	return m_defaults.getDomainName();
    }

    @SettingEntry(path = yealinkConstants.REGISTRATION_SERVER_PORT_SETTING)
    public String getRegistrationServerPort() {
	return m_defaults.getProxyServerSipPort();
    }

    @SettingEntry(path = yealinkConstants.OUTBOUND_HOST_SETTING)
    public String getOutboundHost() {
	return m_defaults.getProxyServerAddr();
    }

    @SettingEntry(path = yealinkConstants.OUTBOUND_PORT_SETTING)
    public String getOutboundPort() {
	return m_defaults.getProxyServerSipPort();
    }

    @SettingEntry(path = yealinkConstants.IDLE_SCREEN_SETTING)
    public String getIdleScreenURL() {
	return "tftp://" + m_defaults.getTftpServer() + "/yealink_SIP-T38G_idle_screen.xml";
    }

    @SettingEntry(path = yealinkConstants.VOICE_MAIL_NUMBER_SETTING)
    public String getVoiceMail() {
	String voicemail = null;
	User u = m_line.getUser();
	if (u != null) {
	    voicemail = m_defaults.getVoiceMail();
	}
	return voicemail;
    }

    @SettingEntry(path = yealinkConstants.MOH_URI)
    public String getMusicOnHoldUri() {
	String mohUri;
	User u = m_line.getUser();
	if (u != null) {
	    mohUri = u.getMusicOnHoldUri();
	} else {
	    mohUri = m_defaults.getMusicOnHoldUri();
	}
	return mohUri;
    }

    @SettingEntry(path = yealinkConstants.BLA_NUMBER)
    public String getBLANumber() {
	User u = m_line.getUser();
	String BLANumber = "";
	if (u != null) {
	    if (u.getIsShared())
		BLANumber = u.getUserName();
	}
	return BLANumber;
    }
}
