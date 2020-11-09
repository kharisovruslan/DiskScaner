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
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Primary;
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
@Primary
public class FindFiles7 implements FindFiles {

    private Logger log = LoggerFactory.getLogger(FindFiles7.class);

    @Autowired
    private ApplicationContext context;

    @Autowired
    private ConfigProperties config;

    private Set<String> exceptionsDirectory = new HashSet<>();

    public FindFiles7() {
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

    @Override
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

    private boolean isExceptPath(String filepathname) {
        if (!exceptionsDirectory.isEmpty()) {
            for (String ed : exceptionsDirectory) {
                if (filepathname.startsWith(ed)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public FileInfo startwithdisk(String drive) {
        Map<String, FileInfo> parents = new HashMap<>();
        AtomicReference<FileInfo> root = new AtomicReference<>();
        try {
            Files.walkFileTree(Paths.get(drive), new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                    log.info(exc.getMessage());
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path dir, BasicFileAttributes a) throws IOException {
                    String fname = dir.toAbsolutePath().toString();
                    String filename = dir.getFileName().toString();
                    if (isExceptPath(fname)) {
                        return FileVisitResult.CONTINUE;
                    }
                    String fparent;
                    if (dir.getParent() == null) {
                        fparent = drive;
                    } else {
                        fparent = dir.getParent().toAbsolutePath().toString();
                    }
                    FileInfo parent = parents.get(fparent);
                    FileInfo fi = context.getBean(FileInfo.class, parent, filename, a.isDirectory(), a.size(),
                            a.lastAccessTime().toMillis(), a.creationTime().toMillis(),
                            a.lastModifiedTime().toMillis(), (!a.isDirectory()) ? getFileSha(fname) : "");
                    parent.getFiles().add(fi);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes a) throws IOException {
                    String fname = dir.toAbsolutePath().toString();
                    FileInfo parent;
                    if (isExceptPath(fname)) {
                        return FileVisitResult.CONTINUE;
                    }
                    String filename;
                    if (dir.getParent() == null) {
                        parent = null;
                        filename = dir.toAbsolutePath().toString();;
                    } else {
                        String fparent = dir.getParent().toAbsolutePath().toString();
                        parent = parents.get(fparent);
                        filename = dir.getFileName().toString();
                    }
                    FileInfo fi = context.getBean(FileInfo.class, parent, filename, a.lastAccessTime().toMillis(), a.creationTime().toMillis(),
                            a.lastModifiedTime().toMillis());
                    if (parent == null) {
                        root.set(fi);
                    } else {
                        parent.getFiles().add(fi);
                    }
                    parents.put(fname, fi);
                    return FileVisitResult.CONTINUE;
                }

            });
        } catch (IOException ex) {
            log.error(ex.getMessage(), ex);
        }
        return root.get();
    }
}
