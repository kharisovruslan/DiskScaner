/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vd.DiskScaner.Files;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;
import vd.DiskScaner.ConfigProperties;

/**
 *
 * @author Kharisov Ruslan
 */
@Component
@Scope("prototype")
public class FindFiles {

    private Logger log = LoggerFactory.getLogger(FindFiles.class);

    @Autowired
    private ApplicationContext context;

    private FileInfo root;

    @Autowired
    private ConfigProperties config;

    private Set<String> exceptionsDirectory = new HashSet<>();

    public FindFiles() {
        String OS = System.getProperty("os.name").toLowerCase();
        if (OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0 || OS.indexOf("aix") > 0) {
            exceptionsDirectory.add("/sys");
            exceptionsDirectory.add("/proc");
        }
    }

    private String getFileSha(String filename) {
        if (config.isCalcShaForFiles()) {
            String sha = "";
            try (FileInputStream fis = new FileInputStream(filename)) {
                sha = DigestUtils.md5DigestAsHex(fis);
            } catch (FileNotFoundException ex) {
                log.error(ex.getMessage() + " file: " + filename, ex);
            } catch (IOException ex) {
                log.error(ex.getMessage() + " file: " + filename, ex);
            }
            return sha;
        }
        return "";
    }

    private void listFiles(FileInfo parent) {
        String findpath = parent.getAbsolutePath();
        if (!exceptionsDirectory.isEmpty()) {
            for (String ed : exceptionsDirectory) {
                if (findpath.startsWith(ed)) {
                    return;
                }
            }
        }
        File fpath = new File(findpath);
        File[] filelist = fpath.listFiles();
        if (filelist == null) {
            log.info("Error find in path " + parent);
            config.addExceptionsFiles(fpath.getAbsolutePath());
            return;
        }
        for (File f : filelist) {
            String filename = "";
            FileInfo fi = null;
            try {
                filename = f.getAbsolutePath();
                BasicFileAttributes a = Files.readAttributes(Paths.get(filename), BasicFileAttributes.class);
                fi = context.getBean(FileInfo.class, parent, f.getName(), a.isDirectory(), a.size(),
                        a.lastAccessTime().toMillis(), a.creationTime().toMillis(),
                        a.lastModifiedTime().toMillis(), (!a.isDirectory()) ? getFileSha(filename) : "");
                if (f.isDirectory()) {
                    fi = context.getBean(FileInfo.class, parent, f.getName(), a.lastAccessTime().toMillis(), a.creationTime().toMillis(),
                            a.lastModifiedTime().toMillis());
                    listFiles(fi);
                }
                parent.getFiles().add(fi);
            } catch (IOException ex) {
                log.error(ex.getMessage() + " file: " + filename, ex);
            }
        }
    }

    public List<String> getDisks() {
        File lroot[] = File.listRoots();
        List<String> r = new ArrayList<>();
        for (File f : lroot) {
            if (f.exists()) {
                String sd = f.getAbsolutePath();
                r.add(sd);
            }
        }
        return r;
    }

    public FileInfo startwithdisk(String drive) {
        root = context.getBean(FileInfo.class, drive);
        listFiles(root);
        return root;
    }

    @Override
    public String toString() {
        return "FindFiles{" + "root=" + root + '}';
    }
}
