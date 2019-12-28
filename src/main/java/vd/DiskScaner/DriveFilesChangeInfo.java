/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vd.DiskScaner;

import java.util.ArrayList;
import java.util.List;
import vd.DiskScaner.Files.FileChangeInfo;

/**
 *
 * @author Kharisov Ruslan
 */
public class DriveFilesChangeInfo {

    private String drive;
    List<FileChangeInfo> files;
    private boolean complete;
    private String status;
    private boolean makelink;

    public DriveFilesChangeInfo(String drive) {
        this.drive = drive;
        files = new ArrayList<>();
        complete = false;
        status = "scan";
        makelink = false;
    }

    public String getDrive() {
        return drive;
    }

    public boolean isComplete() {
        return complete;
    }

    public String getStatus() {
        return status;
    }

    public boolean isMakelink() {
        return makelink;
    }

    public void setMakelink(boolean makelink) {
        this.makelink = makelink;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setComplete(boolean complete) {
        this.complete = complete;
    }

    public void setDrive(String drive) {
        this.drive = drive;
    }

    public List<FileChangeInfo> getFiles() {
        return files;
    }

    public void setFiles(List<FileChangeInfo> files) {
        this.files = files;
    }

    @Override
    public String toString() {
        return "DriveFilesChangeInfo{" + "drive=" + drive + ", files=" + files.size() + ", complete=" + complete + '}';
    }
}
