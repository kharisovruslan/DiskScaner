/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vd.DiskScaner.Files;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import vd.DiskScaner.ConfigProperties;

/**
 *
 * @author Kharisov Ruslan
 */
@Component
@Scope("prototype")
public class FilesCompare {

    private org.slf4j.Logger log = LoggerFactory.getLogger(FilesCompare.class);
    private List<FileChangeInfo> changes;
    private ConfigProperties config;
    @Autowired
    private ApplicationContext context;

    @Autowired
    public FilesCompare(ConfigProperties config) {
        this.config = config;
    }

    private FileChangeInfo compareChangeFiles(FileInfo scan, FileInfo load) {
        if (scan.getSize() != load.getSize()) {
            return context.getBean(FileChangeInfo.class, scan, load, ChangeInfo.Change);
        }
        if (config.isCheckModificationTime()) {
            if (scan.gettChange() != load.gettChange()) {
                return context.getBean(FileChangeInfo.class, scan, load, ChangeInfo.Change);
            }
        }
        if (scan.gettCreate() != load.gettCreate()) {
            return context.getBean(FileChangeInfo.class, scan, load, ChangeInfo.Change);
        }
        if (scan.getSha().compareTo(load.getSha()) != 0) {
            return context.getBean(FileChangeInfo.class, scan, load, ChangeInfo.Change);
        }
        return null;
    }

    private String makeKeyFromFileChangeInfo(FileChangeInfo fc) {
        FileInfo fi;
        if (fc.getChange() == ChangeInfo.Delete) {
            fi = fc.getPrevFile();
        } else {
            fi = fc.getNewFile();
        }
        StringBuffer bkey = new StringBuffer();
        bkey.append(Long.toString(fi.getSize()));
        bkey.append("*");
        bkey.append(Long.toString(fi.gettChange()));
        bkey.append("*");
        bkey.append(Long.toString(fi.gettCreate()));
        return bkey.toString();
    }

    private void findMoveRenameFiles() {
        Map<String, FileChangeInfo> newfiles = new HashMap<>();
        Iterator<FileChangeInfo> ic = changes.iterator();
        while (ic.hasNext()) {
            FileChangeInfo fc = ic.next();
            if (fc.getChange() == ChangeInfo.New) {
                if (fc.getNewFile().isDirectory()) {
                    continue;
                }
                newfiles.put(makeKeyFromFileChangeInfo(fc), fc);
            }
            if (fc.getChange() == ChangeInfo.Delete) {
                if (fc.getPrevFile().isDirectory()) {
                    continue;
                }
            }
        }
        ic = changes.iterator();
        while (ic.hasNext()) {
            FileChangeInfo fc = ic.next();
            if (fc.getChange() == ChangeInfo.Delete) {
                if (fc.getPrevFile().isDirectory()) {
                    continue;
                }
                String key = makeKeyFromFileChangeInfo(fc);
                if (newfiles.containsKey(key)) {
                    FileChangeInfo nf = newfiles.get(key);
                    nf.setChange(ChangeInfo.Rename);
                    nf.setPrevFile(fc.getPrevFile());
                    ic.remove();
                }
            }
        }
    }

    public List<FileChangeInfo> compareFiles(FileInfo scan, FileInfo load) {
        changes = new ArrayList<>();
        compareListStream(scan, load);
        findMoveRenameFiles();
        return changes;
    }

    private void compareListStream(FileInfo scan, FileInfo load) {
        Map<String, FileInfo> sm = scan.getFiles().stream().collect(Collectors.toMap(FileInfo::getName, Function.identity()));
        Map<String, FileInfo> lm = load.getFiles().stream().collect(Collectors.toMap(FileInfo::getName, Function.identity()));
        Set<String> ssn = scan.getFiles().stream().map(FileInfo::getName).collect(Collectors.toSet());
        Set<String> lsn = load.getFiles().stream().map(FileInfo::getName).collect(Collectors.toSet());
        Set<String> dubsn = new HashSet<>(ssn);
        dubsn.retainAll(lsn);
        ssn.removeAll(dubsn);
        lsn.removeAll(dubsn);
        dubsn.forEach(name -> {
            FileInfo s = sm.get(name);
            FileInfo l = lm.get(name);
            if (config.isExceptionsFiles(s.getAbsolutePath())) {
                return;
            }
            if (s.isDirectory()) {
                compareListStream(s, l);
            }
            FileChangeInfo fci = compareChangeFiles(s, l);
            if (fci != null) {
                changes.add(fci);
            }
        });
        ssn.forEach(name -> {
            FileInfo s = sm.get(name);
            if (config.isExceptionsFiles(s.getAbsolutePath())) {
                return;
            }
            changes.add(context.getBean(FileChangeInfo.class, s, null, ChangeInfo.New));
            if (s.isDirectory()) {
                compareListStream(s, new FileInfo());
            }
        });
        lsn.forEach(name -> {
            FileInfo l = lm.get(name);
            if (config.isExceptionsFiles(l.getAbsolutePath())) {
                return;
            }
            changes.add(context.getBean(FileChangeInfo.class, null, l, ChangeInfo.Delete));
            if (l.isDirectory()) {
                compareListStream(new FileInfo(), l);
            }
        });
    }
}
