/*
 * Copyright (C) 2013 SibTelCom, JSC., certain elements licensed under a Contributor Agreement.
 * Author: Konstantin S. Vishnivetsky
 * E-mail: info@siplabs.ru
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
*/

package org.sipfoundry.sipxconfig.upload.yealink;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import org.sipfoundry.sipxconfig.upload.Upload;
import org.sipfoundry.sipxconfig.upload.UploadSpecification;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class YealinkUpload extends Upload {
    private static final Log LOG = LogFactory.getLog(YealinkUpload.class);
    public static final String DIR_YEALINK = "/yealink";
    public static final String DIR_WALLPAPERS = "/WallPapers";
    public static final String DIR_RINGTONES = "/RingTones";

    public YealinkUpload() {
    }

    protected YealinkUpload(String beanId) {
        super(beanId);
    }

    public YealinkUpload(UploadSpecification specification) {
        super(specification);
    }

    @Override
    public void deploy() {
        super.setDestinationDirectory(getDestinationDirectory() + DIR_YEALINK);
        super.deploy();
    }

    @Override
    public void undeploy() {
        super.setDestinationDirectory(getDestinationDirectory() + DIR_YEALINK);
        super.undeploy();
        try {
            File mainLoc = new File(getDestinationDirectory());
            if (mainLoc.exists()) {
                FileUtils.deleteDirectory(mainLoc);
            }
            File rtLoc = new File(getDestinationDirectory() + DIR_RINGTONES);
            if (rtLoc.exists()) {
                FileUtils.deleteDirectory(rtLoc);
            }
            File wpLoc = new File(getDestinationDirectory() + DIR_WALLPAPERS);
            if (wpLoc.exists()) {
                FileUtils.deleteDirectory(wpLoc);
            }
        } catch (IOException e) {
            LOG.error("IOException while deleting folder.", e);
        }
    }

    @Override
    public FileRemover createFileRemover() {
        return new FileRemover();
    }

    public class FileRemover extends Upload.FileRemover {
        @Override
        public void removeFile(File dir, String name) {
            File victim = new File(dir, name);
            if (!victim.exists()) {
                String[] splits = name.split("/");
                if (splits.length >= 2) {
                    victim = new File(dir, splits[1]);
                }
            }
            victim.delete();
        }
    }
}
