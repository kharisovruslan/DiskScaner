package vd.DiskScaner;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.PostConstruct;
import org.apache.commons.io.FilenameUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Kharisov Ruslan
 */
@Component
@ConfigurationProperties(prefix = "sendfiles")
@Validated
public class ConfigProperties {

    private String pathdata = System.getProperty("user.home");

    private String split = File.separator;

    private boolean checkModificationTime = false;

    private Set<String> exceptionsFiles = new HashSet<>();

    private boolean autoStartScanAllDisks = false;

    private boolean calcShaForFiles = false;

    private String executableExtensions = "exe,com,scr,dll,jar,sh,bat,cmd,js";

    private Set<String> executableExtensionsSet;

    @PostConstruct
    public void init() {
        executableExtensionsSet = getExecutableExtensionsSet();
    }

    public String getPathdata() {
        return pathdata;
    }

    public Boolean isAutoStartScanAllDisks() {
        return autoStartScanAllDisks;
    }

    public void setAutoStartScanAllDisks(Boolean autoStartScanAllDisks) {
        this.autoStartScanAllDisks = autoStartScanAllDisks;
    }

    public boolean isCalcShaForFiles() {
        return calcShaForFiles;
    }

    public void setCalcShaForFiles(boolean calcShaForFiles) {
        this.calcShaForFiles = calcShaForFiles;
    }

    public Set<String> getExceptionsFiles() {
        return exceptionsFiles;
    }

    public synchronized void addExceptionsFiles(String filename) {
        exceptionsFiles.add(filename);
    }

    public synchronized boolean isExceptionsFiles(String filename) {
        return exceptionsFiles.contains(filename);
    }

    public void setExceptionsFiles(Set<String> exceptionsFiles) {
        this.exceptionsFiles = exceptionsFiles;
    }

    public void setPathdata(String pathdata) {
        this.pathdata = pathdata;
    }

    public String getSplit() {
        return split;
    }

    public void setSplit(String split) {
        this.split = split;
    }

    public String getExecutableExtensions() {
        return executableExtensions;
    }

    private Set<String> getExecutableExtensionsSet() {
        return new HashSet<>(Arrays.asList(executableExtensions.split(",")));
    }

    public boolean isExecutableExtensions(String filename) {
        if (executableExtensionsSet.contains(FilenameUtils.getExtension(filename))) {
            return true;
        }
        return false;
    }

    public void setExecutableExtensions(String executableExtensions) {
        this.executableExtensions = executableExtensions;
    }

    public Boolean isCheckModificationTime() {
        return checkModificationTime;
    }

    public void setCheckModificationTime(Boolean checkModificationTime) {
        this.checkModificationTime = checkModificationTime;
    }
}
