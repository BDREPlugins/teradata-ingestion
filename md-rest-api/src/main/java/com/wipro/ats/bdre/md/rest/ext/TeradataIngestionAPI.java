package com.wipro.ats.bdre.md.rest.ext;

import com.wipro.ats.bdre.MDConfig;
import com.wipro.ats.bdre.exception.MetadataException;
import com.wipro.ats.bdre.md.dao.ProcessDAO;
import com.wipro.ats.bdre.md.dao.UserRolesDAO;
import com.wipro.ats.bdre.md.dao.jpa.Properties;
import com.wipro.ats.bdre.md.dao.jpa.Process;
import com.wipro.ats.bdre.md.dao.jpa.Users;
import com.wipro.ats.bdre.md.rest.RestWrapper;
import com.wipro.ats.bdre.md.rest.RestWrapperOptions;
import com.wipro.ats.bdre.md.rest.util.Dao2TableUtil;
import com.wipro.ats.bdre.md.rest.util.DateConverter;
import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by SU324335 on 5/4/2016.
 */
@Controller
@RequestMapping("/teradataingestion")
public class TeradataIngestionAPI {
    @Autowired
    SessionFactory sessionFactory;
    @Autowired
    UserRolesDAO userRolesDAO;
    @Autowired
    ProcessDAO processDAO;
    private static final Logger LOGGER = Logger.getLogger(TeradataIngestionAPI.class);
    private static final String TD_LOAD = "td-load";
    @RequestMapping(value = "/createjob", method = RequestMethod.POST)
    @ResponseBody
    public RestWrapper createJob(@RequestParam Map<String, String> map, Principal principal) {
        LOGGER.debug(" value of map is " + map.size());
        RestWrapper restWrapper = null;
        com.wipro.ats.bdre.md.dao.jpa.Properties jpaProperties = null;
        List<Properties> propertiesList = new ArrayList<Properties>();
        String processName = null;
        String processDesc = null;
        Integer busDomainID = null;
        String tableName= null;
        String dbName = null;
        String teradataName = null;

        for (String string : map.keySet()) {
            LOGGER.info("String is " + string + " And value is " + map.get(string));

            if (string.startsWith("teradataProperties_tableName")) {
                tableName = map.get(string);
                jpaProperties = Dao2TableUtil.buildJPAProperties(TD_LOAD, "td-table", map.get(string), "Table Name");
                propertiesList.add(jpaProperties);
            }
            else if (string.startsWith("teradataProperties_dbName")) {
                dbName = map.get(string);
                jpaProperties = Dao2TableUtil.buildJPAProperties(TD_LOAD, "td-db", map.get(string), "Delimiter");
                propertiesList.add(jpaProperties);
            }
            else if (string.startsWith("teradataProperties_delimiter")) {
                jpaProperties = Dao2TableUtil.buildJPAProperties(TD_LOAD, "td-delimiter", map.get(string), "Delimiter");
                propertiesList.add(jpaProperties);
            }
            else if (string.startsWith("teradataProperties_deleteCopiedSource")) {
                jpaProperties = Dao2TableUtil.buildJPAProperties(TD_LOAD, "delete-copied-src", map.get(string), "Delete Copied Source");
                propertiesList.add(jpaProperties);
            } else if (string.startsWith("teradataProperties_filePattern")) {
                jpaProperties = Dao2TableUtil.buildJPAProperties(TD_LOAD, "file-pattern", map.get(string), "File Pattern");
                propertiesList.add(jpaProperties);
            } else if (string.startsWith("teradataProperties_sleepTime")) {
                jpaProperties = Dao2TableUtil.buildJPAProperties(TD_LOAD, "sleep-time", map.get(string), "Sleep Time");
                propertiesList.add(jpaProperties);
            } else if (string.startsWith("teradataProperties_teradataEnv")) {

                teradataName = map.get(string);
                jpaProperties = Dao2TableUtil.buildJPAProperties(TD_LOAD, "teradataName", map.get(string), "Teradata Name");
                propertiesList.add(jpaProperties);
            } else if (string.startsWith("teradataProperties_processName")) {
                processName = map.get(string);
            } else if (string.startsWith("teradataProperties_processDesc")) {
                processDesc = map.get(string);
            } else if (string.startsWith("teradataProperties_busDomainId")) {
                busDomainID = new Integer(map.get(string));
            } else if (string.startsWith("username")) {
                jpaProperties = Dao2TableUtil.buildJPAProperties(TD_LOAD, "td-username", map.get(string), "Teradata username");
                propertiesList.add(jpaProperties);
            }
            else if (string.startsWith("password")) {
                jpaProperties = Dao2TableUtil.buildJPAProperties(TD_LOAD, "td-password", map.get(string), "Teradata password");
                propertiesList.add(jpaProperties);
            }
            else if (string.startsWith("hostname")) {
                jpaProperties = Dao2TableUtil.buildJPAProperties(TD_LOAD, "td-tpdid", map.get(string), "Teradata TDPID");
                propertiesList.add(jpaProperties);
            }


        }

        List<com.wipro.ats.bdre.md.dao.jpa.Process> childProcesses = new ArrayList<com.wipro.ats.bdre.md.dao.jpa.Process>();
        com.wipro.ats.bdre.md.dao.jpa.Process parentProcess = Dao2TableUtil.buildJPAProcess(41, processName,processDesc, 1, busDomainID);
        Users users=new Users();
        users.setUsername(principal.getName());
        parentProcess.setUsers(users);
        parentProcess.setUserRoles(userRolesDAO.minUserRoleId(principal.getName()));

        Integer parentProcessId = processDAO.insert(parentProcess);
        parentProcess.setProcessId(parentProcessId);

        String uploadBasedir = MDConfig.getProperty("upload.base-directory");
        jpaProperties = Dao2TableUtil.buildJPAProperties(TD_LOAD, "monitored-dir-name",uploadBasedir+"/"+parentProcessId.toString()+"/tdload/" , "Teradata Monitor Path");
        propertiesList.add(jpaProperties);

        com.wipro.ats.bdre.md.dao.jpa.Process childProcess = Dao2TableUtil.buildJPAProcess(42, processName+" -ingestion","child process of "+processDesc, 1, busDomainID);
        childProcesses.add(childProcess);

        List<com.wipro.ats.bdre.md.dao.jpa.Process> processList = createTeradataIngestionJob(parentProcess, childProcesses, propertiesList);

        List<com.wipro.ats.bdre.md.beans.table.Process> tableProcessList = Dao2TableUtil.jpaList2TableProcessList(processList);
        Integer counter = tableProcessList.size();
        for (com.wipro.ats.bdre.md.beans.table.Process process:tableProcessList) {
            process.setCounter(counter);
            process.setTableAddTS(DateConverter.dateToString(process.getAddTS()));
            process.setTableEditTS(DateConverter.dateToString(process.getEditTS()));
        }
        restWrapper = new RestWrapper(tableProcessList, RestWrapper.OK);

        LOGGER.info("Process and Properties for teradata ingestion process inserted by" + principal.getName());
        return restWrapper;
    }
    public List<Process> createTeradataIngestionJob(Process parentProcess, List<Process> childProcesses, List<com.wipro.ats.bdre.md.dao.jpa.Properties> appProperties) {
        Session session = sessionFactory.openSession();
        com.wipro.ats.bdre.md.dao.jpa.Process subProcess1 = null;

        Integer parentPid = null;
        Integer subProcessId = null;
        List<Process> processList = new ArrayList<Process>();
        try {
            session.beginTransaction();
            parentPid = parentProcess.getProcessId();
            LOGGER.info("parent processId:" + parentPid);

            processList.add(parentProcess);
            for (Process childProcess : childProcesses) {
                childProcess.setProcess(parentProcess);
                if (childProcess.getProcessType().getProcessTypeId() == 42){
                    subProcess1 = childProcess;
                    subProcess1.setNextProcessId(parentPid.toString());
                    subProcessId = (Integer) session.save(subProcess1);
                    subProcess1.setProcessId(subProcessId);
                }

                processList.add(childProcess);
            }
            parentProcess.setNextProcessId(subProcess1.getProcessId().toString());

            session.update(parentProcess);


            if(appProperties!=null && !appProperties.isEmpty()){
                for (com.wipro.ats.bdre.md.dao.jpa.Properties properties: appProperties) {
                    LOGGER.info("properties key"+properties.getId().getPropKey());
                    properties.getId().setProcessId(subProcessId);
                    properties.setProcess(subProcess1);
                    session.save(properties);
                }
            }
            session.getTransaction().commit();
        }
        catch (MetadataException e) {
            session.getTransaction().rollback();
            LOGGER.error(e);
        } finally {
            session.close();
        }
        return processList;
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
            Criteria criteria = session.createCriteria(Process.class).add(Restrictions.eq("processType.processTypeId", new Integer(42)));
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

}
