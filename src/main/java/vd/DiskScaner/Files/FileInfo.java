/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vd.DiskScaner.Files;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
@JsonIgnoreProperties(value = {"parent", "absolutePath"})
public class FileInfo {

    private String name;
    private FileInfo parent;
    private long size;
    private long tAccess;
    private long tCreate;
    private long tChange;
    private String sha;
    private boolean directory;
    private List<FileInfo> files;

    public FileInfo() {
        files = new ArrayList<>();
        sha = "";
        name = "";
    }

    public FileInfo(String root) {
        files = new ArrayList<>();
        sha = "";
        this.name = root;
        this.directory = true;
    }

    public FileInfo(FileInfo parent, String name, boolean directory, long size, long tAccess, long tCreate, long tChange, String sha) {
        this.name = name;
        this.parent = parent;
        this.directory = directory;
        this.size = size;
        this.tAccess = tAccess;
        this.tCreate = tCreate;
        this.tChange = tChange;
        this.sha = sha;
        files = new ArrayList<>();
    }

    public FileInfo(FileInfo parent, String name, boolean directory, long size) {
        this.name = name;
        this.parent = parent;
        this.directory = directory;
        this.size = size;
        sha = "";
        files = new ArrayList<>();
    }

    public FileInfo(FileInfo parent, String name, long tAccess, long tCreate, long tChange) {
        this.name = name;
        this.parent = parent;
        this.tAccess = tAccess;
        this.tCreate = tCreate;
        this.tChange = tChange;
        sha = "";
        this.directory = true;
        files = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public String getAbsolutePath() {
        String pathname = getName();
        FileInfo item = this;
        while (item.getParent() != null) {
            item = item.getParent();
            pathname = item.getName() + File.separator + pathname;
        }
        if (pathname.length() == 2) {
            if (pathname.charAt(1) == ':') {
                pathname += File.separator;
            }
        }
        return pathname;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public long gettAccess() {
        return tAccess;
    }

    public void settAccess(long tAccess) {
        this.tAccess = tAccess;
    }

    public long gettCreate() {
        return tCreate;
    }

    public void settCreate(long tCreate) {
        this.tCreate = tCreate;
    }

    public long gettChange() {
        return tChange;
    }

    public void settChange(long tChange) {
        this.tChange = tChange;
    }

    public String getSha() {
        return sha;
    }

    public void setSha(String sha) {
        this.sha = sha;
    }

    public FileInfo getParent() {
        return parent;
    }

    public void setParent(FileInfo parent) {
        this.parent = parent;
    }

    public List<FileInfo> getFiles() {
        return files;
    }

    public void setFiles(List<FileInfo> files) {
        this.files = files;
    }

    public boolean isDirectory() {
        return directory;
    }

    public void setDirectory(boolean directory) {
        this.directory = directory;
    }

    @Override
    public String toString() {
        return "name=" + name + ", parent=" + parent.getName() + ", dir=" + directory + ", size=" + size + ", files=" + files.size() + '}';
    }
}
