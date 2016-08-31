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

package com.wipro.ats.bdre.md.rest;

import com.wipro.ats.bdre.MDConfig;
import com.wipro.ats.bdre.exception.MetadataException;
import com.wipro.ats.bdre.md.api.base.MetadataAPIBase;
import com.wipro.ats.bdre.md.beans.ProcessInfo;
import com.wipro.ats.bdre.md.dao.ProcessDAO;
import com.wipro.ats.bdre.md.dao.PropertiesDAO;
import com.wipro.ats.bdre.md.dao.jpa.Process;
import com.wipro.ats.bdre.md.dao.jpa.Properties;
import com.wipro.ats.bdre.md.rest.util.Dao2TableUtil;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by AR288503 on 10/4/2015.
 */
@Controller
@RequestMapping("/tdfilehandler")
public class TDUploaderAPI extends MetadataAPIBase {
    private static final Logger LOGGER = Logger.getLogger(TDUploaderAPI.class);
    private static final String UPLOADBASEDIRECTORY = "upload.base-directory";
    @Autowired
    SessionFactory sessionFactory;
    @Autowired
    ProcessDAO processDAO;
    @Autowired
    PropertiesDAO propertiesDAO;

   //Multipart does not support put
    @RequestMapping(value = "/uploadtr/{processIdValue}", method = RequestMethod.POST)
    @ResponseBody public RestWrapper uploadInTeradata(@PathVariable("processIdValue") Integer processIdValue,
                                                      @RequestParam("file") MultipartFile file, Principal principal) {

        if (!file.isEmpty()) {
            try {

                String uploadedFilesDirectory = MDConfig.getProperty(UPLOADBASEDIRECTORY);
                String name = file.getOriginalFilename();
                byte[] bytes = file.getBytes();
                String monitorPath = null;
                LOGGER.info("processIDvalue is "+processIdValue);
                com.wipro.ats.bdre.md.dao.jpa.Process process = processDAO.get(processIdValue);
                LOGGER.info("Process fetched = "+process.getProcessId());
                List<Properties> propertiesList = propertiesDAO.getByProcessId(process);
                LOGGER.info("No.of Properties fetched= "+ propertiesList.size());
                for(Properties property: propertiesList){
                    LOGGER.debug("property fetched is "+property.getId().getPropKey()+" "+property.getPropValue());
                    if("monitored-dir-name".equals(property.getId().getPropKey())){
                        monitorPath = property.getPropValue();
                    }
                }
                String uploadLocation = monitorPath;
                LOGGER.info("Upload location: " + uploadLocation);
                File fileDir = new File(uploadLocation);
                fileDir.mkdirs();
                File fileToBeSaved = new File(uploadLocation + "/" + name);
                BufferedOutputStream stream =
                        new BufferedOutputStream(new FileOutputStream(fileToBeSaved));
                stream.write(bytes);
                stream.close();
                LOGGER.debug("Uploaded file: " + fileToBeSaved);

                //Populating Uploaded file bean to return in RestWrapper
                UploadedFile uploadedFile = new UploadedFile();
                uploadedFile.setParentProcessId(processIdValue);
               // uploadedFile.setSubDir(subDir);
                uploadedFile.setFileName(name);
                uploadedFile.setFileSize(fileToBeSaved.length());
                LOGGER.debug("The UploadedFile bean:" + uploadedFile);
                LOGGER.info("File uploaded : " + uploadedFile + " uploaded by User:" + principal.getName());

                return new RestWrapper(uploadedFile, RestWrapper.OK);
            } catch (Exception e) {
                LOGGER.error( e);
                return new RestWrapper(e.getMessage(), RestWrapper.ERROR);
            }
        } else {
            return new RestWrapper("You failed to upload because the file was empty.", RestWrapper.ERROR);

        }
    }

    @RequestMapping(value = {"/teradata", "/teradata/"}, method = RequestMethod.POST)
    @ResponseBody public RestWrapperOptions listTeradataProcesses() {
        RestWrapperOptions restWrapperOptions = null;
        try {

            List<com.wipro.ats.bdre.md.dao.jpa.Process> jpaProcessList = teradataProcessesList();
            List<com.wipro.ats.bdre.md.beans.table.Process> tableProcessList = Dao2TableUtil.jpaList2TableProcessList(jpaProcessList);

            List<RestWrapperOptions.Option> options = new ArrayList<RestWrapperOptions.Option>();
            for (com.wipro.ats.bdre.md.beans.table.Process process : tableProcessList) {
                RestWrapperOptions.Option option = new RestWrapperOptions.Option(process.getProcessName(), process.getProcessId());
                options.add(option);
            }
            restWrapperOptions = new RestWrapperOptions(options, RestWrapperOptions.OK);
        } catch (MetadataException e) {
            LOGGER.error(e);
            restWrapperOptions = new RestWrapperOptions(e.getMessage(), RestWrapper.ERROR);
        }
        return restWrapperOptions;
    }

    public List<Process> teradataProcessesList(){
        Session session = sessionFactory.openSession();
        List<Process> returnList = new ArrayList<Process>();
        try {
            session.beginTransaction();
            Criteria criteria = session.createCriteria(Process.class).add(Restrictions.eq("processType.processTypeId", new Integer(40)));
            returnList = criteria.list();
            session.getTransaction().commit();
        } catch (MetadataException e) {
            session.getTransaction().rollback();
            LOGGER.error(e);
        } finally {
            session.close();
        }
        return returnList;

    }

    @Override
    public Object execute(String[] params) {
        return null;
    }
    public class UploadedFile {
        private Integer parentProcessId;
        private String subDir;
        private String fileName;
        private long fileSize;
        private boolean fileExists;
        @Override
        public String toString() {
            return "parentProcessId=" + parentProcessId +
                    " subDir=" + subDir +
                    " fileName=" + fileName +
                    " fileSize=" + fileSize;
        }

        public String getSubDir() {
            return subDir;
        }

        public void setSubDir(String subDir) {
            this.subDir = subDir;
        }

        public Integer getParentProcessId() {
            return parentProcessId;
        }

        public void setParentProcessId(Integer parentProcessId) {
            this.parentProcessId = parentProcessId;
        }

        public String getFileName() {
            return fileName;
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }

        public long getFileSize() {
            return fileSize;
        }

        public void setFileSize(long fileSize) {
            this.fileSize = fileSize;
        }

        public boolean isFileExists() {
            return fileExists;
        }

        public void setFileExists(boolean fileExists) {
            this.fileExists = fileExists;
        }
    }

}
