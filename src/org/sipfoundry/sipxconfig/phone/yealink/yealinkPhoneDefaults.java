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

import java.util.Collection;
import java.util.Iterator;

import org.sipfoundry.sipxconfig.common.User;
import org.sipfoundry.sipxconfig.device.DeviceDefaults;
import org.sipfoundry.sipxconfig.device.DeviceTimeZone;
import org.sipfoundry.sipxconfig.phonebook.Phonebook;
import org.sipfoundry.sipxconfig.phonebook.PhonebookManager;
import org.sipfoundry.sipxconfig.service.UnmanagedService;
import org.sipfoundry.sipxconfig.setting.SettingEntry;

public class yealinkPhoneDefaults {
    private final DeviceDefaults m_defaults;
    private final yealinkPhone m_phone;

    public yealinkPhoneDefaults(DeviceDefaults defaults, yealinkPhone phone) {
	m_defaults = defaults;
	m_phone = phone;
    }

    @SettingEntry(path = yealinkConstants.LOCAL_TIME_SERVER1_SETTING)
    public String getTimeServer1() {
	return m_defaults.getNtpServer();
    }

    @SettingEntry(path = yealinkConstants.LOCAL_TIME_SERVER2_SETTING)
    public String getTimeServer2() {
	return m_defaults.getAlternateNtpServer();
    }

    private DeviceTimeZone getZone() {
	return m_defaults.getTimeZone();
    }

    @SettingEntry(path = yealinkConstants.LOCAL_TIME_ZONE_SETTING)
    public String getTimeZone() {
	Integer tz = getZone().getOffsetInHours();
	if (tz < 0)
	    return "-" + tz.toString();
	else
	    return "+" + tz.toString();
    }

    @SettingEntry(path = yealinkConstants.SYSLOG_SERVER_SETTING)
    public String getSyslogdIP() {
	return m_defaults.getServer(0, UnmanagedService.SYSLOG);
    }

    public String getTFTPServer() {
	return m_defaults.getTftpServer();
    }

    @SettingEntry(path = yealinkConstants.DNS_SERVER1_SETTING)
    public String getNameServer1() {
	return m_defaults.getServer(0, UnmanagedService.DNS);
    }

    @SettingEntry(path = yealinkConstants.DNS_SERVER2_SETTING)
    public String getNameServer2() {
	return m_defaults.getServer(1, UnmanagedService.DNS);
    }

    @SettingEntry(path = yealinkConstants.REMOTE_PHONEBOOK_0_NAME_SETTING)
    public String getName() {
	User user = m_phone.getPrimaryUser();
	if (user != null) {
	    PhonebookManager pbm = m_phone.getPhonebookManager();
	    if (pbm != null) {
		Collection<Phonebook> books = pbm.getAllPhonebooksByUser(user);
		Iterator pbit = books.iterator();
		if (pbit != null) {
		    Phonebook pb0 = (Phonebook)pbit.next();
		    if (pb0 != null) {
			if (pb0.getShowOnPhone()) {
			    return pb0.getName();
			}
		    }
		}
	    }
        }
	return "";
    }

    @SettingEntry(path = yealinkConstants.REMOTE_PHONEBOOK_0_URL_SETTING)
    public String getURL() {
	return  "tftp://" + m_defaults.getTftpServer() + "/" + m_phone.getSerialNumber() + "-" + yealinkConstants.XML_CONTACT_DATA;
    }

    @SettingEntry(path = yealinkConstants.FIRMWARE_SERVER_ADDRESS)
    public String getserver_ip() {
	return  m_defaults.getTftpServer();
    }

    @SettingEntry(path = yealinkConstants.FIRMWARE_URL)
    public String geturl() {
	yealinkModel model = (yealinkModel)m_phone.getModel();
	return  "tftp://" + m_defaults.getTftpServer() + "/" + model.getName() + ".rom";
    }

    @SettingEntry(path = yealinkConstants.FIRMWARE_HTTP_URL)
    public String gethttp_url() {
	yealinkModel model = (yealinkModel)m_phone.getModel();
	return  m_defaults.getProfileRootUrl() + "/" + model.getName() + ".rom";
    }

    @SettingEntry(path = yealinkConstants.AUTOPROVISIONING_SERVER_URL)
    public String getstrServerURL() {
	return  "tftp://" + m_defaults.getTftpServer() + "/" + m_phone.getSerialNumber() + ".cfg";
    }

    @SettingEntry(path = yealinkConstants.AUTOPROVISIONING_SERVER_ADDRESS)
    public String getserver_Address() {
	return  "tftp://" + m_defaults.getTftpServer() + "/" + m_phone.getSerialNumber() + ".cfg";
    }

    @SettingEntry(path = yealinkConstants.AUTOPROVISIONING1_SERVER_ADDRESS)
    public String getserver_addresS() {
	return  "tftp://" + m_defaults.getTftpServer() + "/" + m_phone.getSerialNumber() + ".cfg";
    }

    @SettingEntry(path = yealinkConstants.FIRMWARE_NAME)
    public String getfirmware_name() {
	yealinkModel model = (yealinkModel)m_phone.getModel();
	return  model.getName() + ".rom";
    }

    @SettingEntry(path = yealinkConstants.LANG_FILE_NAME)
    public String getserver_address() {
	return  "tftp://" + m_defaults.getTftpServer() + "/lang+English.txt";
    }

    @SettingEntry(path = yealinkConstants.LOGO_FILE_NAME)
    public String getServer_Address() {
	return  "tftp://" + m_defaults.getTftpServer() + "/yealinkLogo132x64.dob";
    }

    @SettingEntry(path = yealinkConstants.DIAL_NOW)
    public String getServer_address() {
	return  "tftp://" + m_defaults.getTftpServer() + "/" + m_phone.getSerialNumber() + "-" + yealinkConstants.XML_DIAL_NOW;
    }

    @SettingEntry(path = yealinkConstants.DIAL_NOW_URL)
    public String getUrl() {
	return  "tftp://" + m_defaults.getTftpServer() + "/" + m_phone.getSerialNumber() + "-" + yealinkConstants.XML_DIAL_NOW;
    }

//    @SettingEntry(path = yealinkConstants.ALERT_INFO_TEXT_0)
//    public String getText0() {
//	return getPhoneContext().getIntercomForPhone(m_phone).getCode();
//    }

}

