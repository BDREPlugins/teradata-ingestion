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

/**
 * Created by MO335755 on 12/22/2015.
 */
public class FileCopyInfo {
    private String fileName;
    private String subProcessId;
    private String serverId;
    private String srcLocation;
    private String tdTable;
    private String fileHash;
    private long fileSize;
    private long timeStamp;
    private String tdDB;
    private String tdUserName;
    private String tdPassword;
    private String tdTpdid;
    private String tdDelimiter;

    public String getTdDB() {
        return tdDB;
    }

    public void setTdDB(String tdDB) {
        this.tdDB = tdDB;
    }

    public String getTdUserName() {
        return tdUserName;
    }

    public void setTdUserName(String tdUserName) {
        this.tdUserName = tdUserName;
    }

    public String getTdPassword() {
        return tdPassword;
    }

    public void setTdPassword(String tdPassword) {
        this.tdPassword = tdPassword;
    }

    public String getTdTpdid() {
        return tdTpdid;
    }

    public void setTdTpdid(String tdTpdid) {
        this.tdTpdid = tdTpdid;
    }

    public String getTdDelimiter() {
        return tdDelimiter;
    }

    public void setTdDelimiter(String tdDelimiter) {
        this.tdDelimiter = tdDelimiter;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getSubProcessId() {
        return subProcessId;
    }

    public void setSubProcessId(String subProcessId) {
        this.subProcessId = subProcessId;
    }

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    public String getSrcLocation() {
        return srcLocation;
    }

    public void setSrcLocation(String srcLocation) {
        this.srcLocation = srcLocation;
    }

    public String getTdTable() {
        return tdTable;
    }

    public void setTdTable(String tdTable) {
        this.tdTable = tdTable;
    }

    public String getFileHash() {
        return fileHash;
    }

    public void setFileHash(String fileHash) {
        this.fileHash = fileHash;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }
}