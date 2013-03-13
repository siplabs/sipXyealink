/*
 * Copyright (C) 2013 SibTelCom, JSC., certain elements licensed under a Contributor Agreement.
 * Author: Konstantin S. Vishnivetsky
 * E-mail: info@siplabs.ru
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
*/

package org.sipfoundry.sipxconfig.phone.yealink;

import org.sipfoundry.sipxconfig.phone.PhoneModel;

/**
 * Static differences in yealink models
 */
public final class yealinkModel extends PhoneModel {
    private boolean m_hasSeparateDialNow;
    private boolean m_usePhonebook;
    private String m_name;
    private String m_directoryProfileTemplate;
    private String m_dialNowProfileTemplate;
    private boolean m_noHD;
    private int m_softKeyCount;

    public yealinkModel() {
    }

    public yealinkModel(String beanId) {
        super(beanId);
    }

    public void setName(String name) {
        m_name = name;
    }

    public String getName() {
        return m_name;
    }

    public void setDirectoryProfileTemplate(String name) {
        m_directoryProfileTemplate = name;
    }

    public String getDirectoryProfileTemplate() {
        return m_directoryProfileTemplate;
    }

    public void setdialNowProfileTemplate(String name) {
        m_dialNowProfileTemplate = name;
    }

    public String getdialNowProfileTemplate() {
        return m_dialNowProfileTemplate;
    }

    public boolean getHasSeparateDialNow() {
        return m_hasSeparateDialNow;
    }

    public void setHasSeparateDialNow(boolean hasSeparateDialNow) {
        m_hasSeparateDialNow = hasSeparateDialNow;
    }

    public boolean getUsePhonebook() {
        return m_usePhonebook;
    }

    public void setUsePhonebook(boolean usePhonebook) {
        m_usePhonebook = usePhonebook;
    }

    public boolean getnoHD() {
        return m_noHD;
    }

    public void setnoHD(boolean noHD) {
        m_noHD = noHD;
    }

    public int getSoftKeyCount() {
        return m_softKeyCount;
    }

    public void setSoftKeyCount(int softKeyCount) {
        m_softKeyCount = softKeyCount;
    }
}
