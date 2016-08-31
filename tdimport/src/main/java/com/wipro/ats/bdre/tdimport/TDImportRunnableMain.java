/*
 *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  * http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *  
 */

package com.wipro.ats.bdre.tdimport;

import com.wipro.ats.bdre.BaseStructure;
import com.wipro.ats.bdre.exception.BDREException;
import com.wipro.ats.bdre.md.api.GetGeneralConfig;
import com.wipro.ats.bdre.md.api.GetProcess;
import com.wipro.ats.bdre.md.api.GetProperties;
import com.wipro.ats.bdre.md.beans.ProcessInfo;
import com.wipro.ats.bdre.md.beans.table.GeneralConfig;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;
import org.apache.commons.vfs2.impl.DefaultFileMonitor;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.Properties;

/**
 * Created by cloudera on 5/4/16.
 */

public class TDImportRunnableMain extends BaseStructure {

    public static final String ARCHIVE = "_archive";
    private static final Logger LOGGER = Logger.getLogger(TDImportRunnableMain.class);
    private static String monitoredDirName = "";
    private static String filePattern = "";
    private static boolean deleteCopiedSrc = false;
    private static String tdTable = "";
    private static String subProcessId = "";
    private static long sleepTime;
    private static String defaultFSName;
    private static String tdDB = "";
    private static String tdUserName = "";
    private static String tdPassword = "";
    private static String tdDelimiter = "";
    private static String tdTpdid = "";

    public static long getSleepTime() {
        return sleepTime;
    }

    public static void setSleepTime(long sleepTime) {
        TDImportRunnableMain.sleepTime = sleepTime;
    }


    public static String getFilePattern() {
        return filePattern;
    }

    public static void setFilePattern(String filePattern) {
        TDImportRunnableMain.filePattern = filePattern;
    }

    public static String getMonitoredDirName() {
        return monitoredDirName;
    }

    public static void setMonitoredDirName(String monitoredDirName) {
        TDImportRunnableMain.monitoredDirName = monitoredDirName;
    }

    public static boolean isDeleteCopiedSrc() {
        return deleteCopiedSrc;
    }

    public static void setDeleteCopiedSrc(boolean deleteCopiedSrc) {
        TDImportRunnableMain.deleteCopiedSrc = deleteCopiedSrc;
    }

    public static String getTdTable() {
        return tdTable;
    }

    public static void setTdTable(String tdTable) {
        TDImportRunnableMain.tdTable = tdTable;
    }

    public static String getSubProcessId() {
        return subProcessId;
    }

    public static String getDefaultFSName() {
        return defaultFSName;
    }

    public static void main(String[] args) {
        TDImportRunnableMain f2SFileMonitorMain = new TDImportRunnableMain();
        f2SFileMonitorMain.execute(args);
    }

    public static String getTdDB() {
        return tdDB;
    }

    public static void setTdDB(String tdDB) {
        TDImportRunnableMain.tdDB = tdDB;
    }

    public static String getTdUserName() {
        return tdUserName;
    }

    public static void setTdUserName(String tdUserName) {
        TDImportRunnableMain.tdUserName = tdUserName;
    }

    public static String getTdPassword() {
        return tdPassword;
    }

    public static void setTdPassword(String tdPassword) {
        TDImportRunnableMain.tdPassword = tdPassword;
    }

    public static String getTdDelimiter() {
        return tdDelimiter;
    }

    public static void setTdDelimiter(String tdDelimiter) {
        TDImportRunnableMain.tdDelimiter = tdDelimiter;
    }

    public static String getTdTpdid() {
        return tdTpdid;
    }

    public static void setTdTpdid(String tdTpdid) {
        TDImportRunnableMain.tdTpdid = tdTpdid;
    }

    public static void setSubProcessId(String subProcessId) {
        TDImportRunnableMain.subProcessId = subProcessId;
    }

    private void execute(String[] params) {
        try {
            GetProcess getProcess = new GetProcess();
            List<ProcessInfo> subProcessList = getProcess.getSubProcesses(params);
            subProcessId = subProcessList.get(0).getProcessId().toString();
            LOGGER.info("subProcessId="+subProcessId);
            GetProperties getProperties = new GetProperties();
            Properties properties = getProperties.getProperties(subProcessId, "td-load");
            LOGGER.info("property is " + properties);
            GetGeneralConfig generalConfig = new GetGeneralConfig();
            GeneralConfig gc = generalConfig.byConigGroupAndKey("imconfig", "common.default-fs-name");

            defaultFSName = gc.getDefaultVal();
            monitoredDirName = properties.getProperty("monitored-dir-name");
            filePattern = properties.getProperty("file-pattern");
            tdTable = properties.getProperty("td-table");
            tdDB = properties.getProperty("td-db");
            tdUserName = properties.getProperty("td-username");
            tdPassword = properties.getProperty("td-password");
            tdTpdid = properties.getProperty("td-tpdid");
            tdDelimiter = properties.getProperty("td-delimiter");
            deleteCopiedSrc = Boolean.parseBoolean(properties.getProperty("delete-copied-src"));
            sleepTime = Long.parseLong(properties.getProperty("sleep-time"));
            if (sleepTime < 100) {
                sleepTime=100;
            }

            //Now run the monitoring thread
            //This is a daemon thread
            FileSystemManager fsManager = VFS.getManager();
            //Reading directory paths and adding to the DefaultFileMonitor
            String dir = TDImportRunnableMain.getMonitoredDirName();
            DefaultFileMonitor fm = new DefaultFileMonitor(FileMonitor.getInstance());
            FileObject listenDir = fsManager.resolveFile(dir);
            FileObject archiveDir = fsManager.resolveFile(dir+"/"+ARCHIVE);
            LOGGER.debug("Monitoring directories " + dir);
            fm.setRecursive(true);
            fm.addFile(listenDir);
            fm.removeFile(archiveDir);
            fm.start();
            //Now scan the mondir for existing files and add to queue
            FileScan.scanAndAddToQueue();
            //Now starting the consumer thread
            Thread consumerThread1 = new Thread(new QueueConsumerRunnable());
            consumerThread1.start();

            Thread consumerThread2 = new Thread(new QueueConsumerRunnable());
            consumerThread2.start();
        } catch (Exception err) {
            LOGGER.error(err);
            throw new BDREException(err);
        }
    }
}
