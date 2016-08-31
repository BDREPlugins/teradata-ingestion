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

/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.wipro.ats.bdre.tdimport;

import com.wipro.ats.bdre.MDConfig;
import com.wipro.ats.bdre.exception.BDREException;
import com.wipro.ats.bdre.md.api.GetProcess;
import com.wipro.ats.bdre.md.api.GetProperties;
import com.wipro.ats.bdre.md.beans.ProcessInfo;
import com.wipro.ats.bdre.md.dao.ProcessDAO;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.hadoop.conf.Configuration;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.text.SimpleDateFormat;

/**
 * Created by MO335755 on 12/23/2015.
 */
public class QueuedFileUploader {
    @Autowired static ProcessDAO processDAO;
    private static final Logger LOGGER = Logger.getLogger(QueuedFileUploader.class);
    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
    private static Configuration config = new Configuration();

    private QueuedFileUploader(){

    }

    private static void importFile(FileCopyInfo fileCopying) throws IOException {
        try {
            String sCommandString;
            String subProcessId= fileCopying.getSubProcessId();
            GetProcess getProcess = new GetProcess();
            String params[] = {"-p",subProcessId};
            ProcessInfo processInfo=getProcess.getParentInfoFromSubProcess(params);
            String monDirName=new GetProperties().getProperties(subProcessId,"td-load").getProperty("monitored-dir-name");
            String targetDirPath="tdload/"+processInfo.getBusDomainId()+"/"+processInfo.getProcessTypeId()+"/"+processInfo.getProcessId();
            String absoluteFilePath = "tdload/"+processInfo.getBusDomainId()+"/"+processInfo.getProcessTypeId()+"/"+processInfo.getProcessId()+"/"+fileCopying.getFileName();
            String command = "sh " + MDConfig.getProperty("execute.script-path") + "/file-loader-remote.sh" + " " +absoluteFilePath+" "+fileCopying.getTdDB()+" "+fileCopying.getTdTable()+" "+fileCopying.getTdUserName()+" "+fileCopying.getTdPassword()+" "+fileCopying.getTdTpdid()+" "+fileCopying.getTdDelimiter()+" "+targetDirPath+" "+monDirName+" "+fileCopying.getFileName();
            sCommandString = command;
            CommandLine oCmdLine = CommandLine.parse(sCommandString);
            LOGGER.debug("executing command :" + command);
            DefaultExecutor oDefaultExecutor = new DefaultExecutor();
            //oDefaultExecutor.setExitValue(0);
            oDefaultExecutor.execute(oCmdLine);

        } catch (Exception e) {
            FileMonitor.addToQueue(fileCopying.getFileName(), fileCopying);
            LOGGER.error("Error in importing file into TD. Requeuing file " + fileCopying.getFileName(), e);
            throw new IOException(e);
        }
    }

    public static void executeImportScript() {
        // this variable is used to keep details of file being currently copying
        if (FileMonitor.getQueueSize() > 0) {
            FileCopyInfo fileCopying = FileMonitor.getFileInfoFromQueue();
            try {
                importFile(fileCopying);
            } catch (Exception err) {
                LOGGER.error("Error in execute copy process ", err);
                throw new BDREException(err);
            }
        }
    }



}
