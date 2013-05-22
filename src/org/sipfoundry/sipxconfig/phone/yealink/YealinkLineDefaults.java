/*
 * Copyright (C) 2013 SibTelCom, JSC., certain elements licensed under a Contributor Agreement.
 * Author: Konstantin S. Vishnivetsky
 * E-mail: info@siplabs.ru
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
*/

package org.sipfoundry.sipxconfig.phone.yealink;

import org.sipfoundry.sipxconfig.address.Address;
import org.sipfoundry.sipxconfig.common.User;
import org.sipfoundry.sipxconfig.device.DeviceDefaults;
import org.sipfoundry.sipxconfig.phone.Line;
import org.sipfoundry.sipxconfig.setting.SettingEntry;

public class YealinkLineDefaults {
    private final DeviceDefaults m_defaults;
    private final Line m_line;

    YealinkLineDefaults(DeviceDefaults defaults, Line line) {
        m_defaults = defaults;
        m_line = line;
    }

    @SettingEntry(paths = {
            YealinkConstants.AUTH_ID_V6X_SETTING,
            YealinkConstants.USER_ID_V6X_SETTING,
            YealinkConstants.AUTH_ID_V7X_SETTING,
            YealinkConstants.USER_ID_V7X_SETTING
            })
    public String getUserName() {
        String userName = null;
        User user = m_line.getUser();
        if (user != null) {
            userName = user.getUserName();
        }
        return userName;
    }

    @SettingEntry(paths = {
            YealinkConstants.DISPLAY_NAME_V6X_SETTING,
            YealinkConstants.DISPLAY_NAME_V7X_SETTING
            })
    public String getDisplayName() {
        String displayName = null;
        User user = m_line.getUser();
        if (user != null) {
            displayName = user.getDisplayName();
        }
        return displayName;
    }

    @SettingEntry(paths = {
            YealinkConstants.PASSWORD_V6X_SETTING,
            YealinkConstants.PASSWORD_V7X_SETTING
            })
    public String getPassword() {
        String password = null;
        User user = m_line.getUser();
        if (user != null) {
            password = user.getSipPassword();
        }
        return password;
    }

    @SettingEntry(paths = {
            YealinkConstants.REGISTRATION_SERVER_HOST_V6X_SETTING,
            YealinkConstants.REGISTRATION_SERVER_HOST_V7X_SETTING
            })
    public String getRegistrationServer() {
        return m_defaults.getDomainName();
    }
/*@
    Returns SIP port.
    Port 5060 is internal SIP port in sipXecs by default
*/
    @SettingEntry(paths = {
            YealinkConstants.REGISTRATION_SERVER_PORT_V6X_SETTING,
            YealinkConstants.OUTBOUND_PORT_V6X_SETTING,
            YealinkConstants.BACKUP_OUTBOUND_PORT_V6X_SETTING,
            YealinkConstants.REGISTRATION_SERVER_PORT_V7X_SETTING,
            YealinkConstants.OUTBOUND_PORT_V7X_SETTING,
            YealinkConstants.BACKUP_OUTBOUND_PORT_V7X_SETTING
            })
    public Integer getRegistrationServerPort() {
        return 5060;
    }

    @SettingEntry(paths = {
            YealinkConstants.OUTBOUND_HOST_V6X_SETTING,
            YealinkConstants.BACKUP_OUTBOUND_HOST_V6X_SETTING,
            YealinkConstants.OUTBOUND_HOST_V7X_SETTING,
            YealinkConstants.BACKUP_OUTBOUND_HOST_V7X_SETTING
            })
    public String getOutboundHost() {
        Address outboundProxyAdress = m_defaults.getProxyAddress();
        if (null == outboundProxyAdress) {
            return "";
        }
        return outboundProxyAdress.getAddress();
    }

    @SettingEntry(paths = {
            YealinkConstants.VOICE_MAIL_NUMBER_V6X_SETTING,
            YealinkConstants.VOICE_MAIL_NUMBER_V7X_SETTING
            })
    public String getVoiceMail() {
        String voicemail = null;
        User u = m_line.getUser();
        if (u != null) {
            voicemail = m_defaults.getVoiceMail();
        }
        return voicemail;
    }
/*
    @SettingEntry(path = yealinkConstants.IDLE_SCREEN_SETTING)
    public String getIdleScreenURL() {
            return "tftp://" + m_defaults.getTftpServer() + "/yealink_SIP-T38G_idle_screen.xml";
    }
*/

    @SettingEntry(paths = {
            YealinkConstants.ADVANCED_MUSIC_SERVER_URI_V6X_SETTING,
            YealinkConstants.ADVANCED_MUSIC_SERVER_URI_V7X_SETTING })
    public String getMusicServerUri() {
        String mohUri;
        User u = m_line.getUser();
        if (u != null) {
            mohUri = u.getMusicOnHoldUri();
        } else {
            mohUri = m_defaults.getMusicOnHoldUri();
        }
        return mohUri;
    }

/*
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
*/
}
