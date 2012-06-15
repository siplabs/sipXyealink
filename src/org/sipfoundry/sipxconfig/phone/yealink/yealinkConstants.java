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

public class yealinkConstants {

    public static final String MIME_TYPE_PLAIN = "text/plain";
    public static final String MIME_TYPE_XML = "text/xml";

    public static final String XML_DIAL_NOW = "dialnow.xml";
    public static final String XML_CONTACT_DATA = "directory.xml";
    public static final String WEB_ITEMS_LEVEL = "WebItemsLevel.cfg";
    public static final String VENDOR = "Yealink";

    // Line specific settings used in /etc/yealinkPhone/Line.xml
    public static final String USER_ID_SETTING = "account/UserName";
    public static final String AUTH_ID_SETTING = "account/AuthName";
    public static final String IDLE_SCREEN_SETTING = "account/IdleScreenURL";
    public static final String DISPLAY_NAME_SETTING = "account/DisplayName";
    public static final String PASSWORD_SETTING = "account/password";
    public static final String REGISTRATION_SERVER_HOST_SETTING = "account/SIPServerHost";
    public static final String REGISTRATION_SERVER_PORT_SETTING = "account/SIPServerPort";
    public static final String OUTBOUND_HOST_SETTING = "account/OutboundHost";
    public static final String OUTBOUND_PORT_SETTING = "account/OutboundPort";
    public static final String VOICE_MAIL_NUMBER_SETTING = "Message/VoiceNumber";
    public static final String MOH_URI = "account/MusicServerUri";
    public static final String BLA_NUMBER = "account/BLANumber";

    // Phone specific settings used in /etc/yealinkPhone/Phone.xml
    public static final String DNS_SERVER1_SETTING = "DNS/PrimaryDNS";
    public static final String DNS_SERVER2_SETTING = "DNS/SecondaryDNS";
    public static final String LOCAL_TIME_SERVER1_SETTING = "Time/TimeServer1";
    public static final String LOCAL_TIME_SERVER2_SETTING = "Time/TimeServer2";
    public static final String LOCAL_TIME_ZONE_SETTING = "Time/TimeZone";
    public static final String SYSLOG_SERVER_SETTING = "SYSLOG/SyslogdIP";
    public static final String REMOTE_PHONEBOOK_0_URL_SETTING = "RemotePhoneBook/URL0";
    public static final String REMOTE_PHONEBOOK_0_NAME_SETTING = "RemotePhoneBook/Name0";
    public static final String FIRMWARE_SERVER_ADDRESS = "firmware/server_ip";
    public static final String FIRMWARE_URL = "firmware/url";
    public static final String FIRMWARE_HTTP_URL = "firmware/http_url";
    public static final String FIRMWARE_NAME = "firmware/firmware_name";
    public static final String AUTOPROVISIONING_SERVER_URL = "autoprovision/strServerURL";
    public static final String AUTOPROVISIONING_SERVER_ADDRESS = "autoprovision/server_address";
    public static final String AUTOPROVISIONING1_SERVER_ADDRESS = "autoprovision1/Server_address";
    public static final String LANG_FILE_NAME = "LangFile/server_address";
    public static final String LOGO_FILE_NAME = "Logo/server_address";
    public static final String DIAL_NOW = "DialNow/server_address";
    public static final String DIAL_NOW_URL = "DialNow/URL";

}
