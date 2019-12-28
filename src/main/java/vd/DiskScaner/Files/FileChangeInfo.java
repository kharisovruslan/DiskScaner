/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vd.DiskScaner.Files;

import java.text.SimpleDateFormat;
import java.util.Date;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import vd.DiskScaner.ConfigProperties;

/**
 *
 * @author Kharisov Ruslan
 */
@Component
@Scope("prototype")
public class FileChangeInfo {

    private FileInfo newFile;
    private FileInfo prevFile;
    private ChangeInfo change;

    @Autowired
    private ConfigProperties config;
    @Autowired
    private SimpleDateFormat sdf;

    public FileInfo getNewFile() {
        return newFile;
    }

    public void setNewFile(FileInfo newFile) {
        this.newFile = newFile;
    }

    public FileInfo getPrevFile() {
        return prevFile;
    }

    public void setPrevFile(FileInfo prevFile) {
        this.prevFile = prevFile;
    }

    public ChangeInfo getChange() {
        return change;
    }

    public void setChange(ChangeInfo change) {
        this.change = change;
    }

    public String getStyle() {
        boolean isExecutable = config.isExecutableExtensions(getFileName());
        if (change == ChangeInfo.New) {
            return (isExecutable) ? "ExecutableNewFiles" : "NewFiles";
        }
        if (change == ChangeInfo.Delete) {
            return (isExecutable) ? "ExecutableNewFiles" : "DeleteFiles";
        }
        if (change == ChangeInfo.Rename) {
            return (isExecutable) ? "ExecutableNewFiles" : "RenameFiles";
        }
        if (change == ChangeInfo.Change) {
            return (isExecutable) ? "ExecutableNewFiles" : "ChangeFiles";
        }
        return (isExecutable) ? "ExecutableNewFiles" : "DefaultFiles";
    }

    public String getSize() {
        if (change == ChangeInfo.Delete) {
            return org.apache.commons.io.FileUtils.byteCountToDisplaySize(prevFile.getSize());
        } else {
            return org.apache.commons.io.FileUtils.byteCountToDisplaySize(newFile.getSize());
        }
    }

    public String getDate() {
        if (change == ChangeInfo.Delete) {
            return sdf.format(new Date(prevFile.gettCreate()));
        } else {
            return sdf.format(new Date(newFile.gettCreate()));
        }
    }

    public String getDescription() {
        if (change == ChangeInfo.New) {
            return String.format("New file");
        }
        if (change == ChangeInfo.Delete) {
            return String.format("Remove file");
        }
        if (change == ChangeInfo.Rename) {
            return String.format("Prev name: %s", prevFile.getAbsolutePath());
        }
        if (change == ChangeInfo.Change) {
            String description = "";
            if (newFile.getSize() != prevFile.getSize()) {
                description += String.format("change size prev: %s change: %s",
                        org.apache.commons.io.FileUtils.byteCountToDisplaySize(prevFile.getSize()),
                        org.apache.commons.io.FileUtils.byteCountToDisplaySize((newFile.getSize() - prevFile.getSize())));
            } else {
                if (newFile.getSha().compareTo(prevFile.getSha()) != 0) {
                    description += String.format("change sha prev: %s now: %s",
                            prevFile.getSha(), newFile.getSha());
                }
                if (newFile.gettCreate() != prevFile.gettCreate()) {
                    description += String.format("change time create prev: %s now: %s",
                            sdf.format(new Date(prevFile.gettCreate())),
                            sdf.format(new Date(newFile.gettCreate())));
                }
                if (description.isEmpty()) {
                    if (newFile.gettChange() != prevFile.gettChange()) {
                        description += String.format("change time modification prev: %s now: %s",
                                sdf.format(new Date(prevFile.gettChange())),
                                sdf.format(new Date(newFile.gettChange())));
                    }
                }
            }
            return description;
        }
        return "Error find description";
    }

    public FileChangeInfo(FileInfo newFile, FileInfo prevFile, ChangeInfo change) {
        this.newFile = newFile;
        this.prevFile = prevFile;
        this.change = change;
    }

    public String getFileName() {
        if ((change == ChangeInfo.New) || (change == ChangeInfo.Rename)) {
            if (newFile != null) {
                return newFile.getAbsolutePath();
            }
        } else {
            if (prevFile != null) {
                return prevFile.getAbsolutePath();
            }
        }
        return "error";
    }

    @Override
    public String toString() {
        return "Change{" + change + " scan=" + newFile + ", load=" + prevFile + '}';
    }
}
