/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vd.DiskScaner;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;
import javax.annotation.PostConstruct;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import vd.DiskScaner.Files.DiskScaner;
import vd.DiskScaner.Files.FilesStorage;
import vd.DiskScaner.Files.FindFiles;

/**
 *
 * @author Kharisov Ruslan
 */
@Controller
public class DiskScanerController {

    @Autowired
    private DiskScaner diskScaner;
    @Autowired
    private ApplicationContext context;
    @Autowired
    private FindFiles findFiles;
    @Autowired
    private ConfigProperties config;
    @Autowired
    FilesStorage storage;

    boolean isComplete = false;
    boolean isSaveExceptionsFiles = false;

    private List<Future<DriveFilesChangeInfo>> diskwait;
    private Map<String, DriveFilesChangeInfo> drives;
    private org.slf4j.Logger log = LoggerFactory.getLogger(DiskScaner.class);

    @PostConstruct
    public void init() {
        diskwait = new ArrayList<>();
        drives = new HashMap<>();
        if (config.isAutoStartScanAllDisks()) {
            startscandisks(findFiles.getDisks());
        }
    }

    public void startscandisks(List<String> disks) {
        isComplete = false;
        isSaveExceptionsFiles = false;
        drives.clear();
        config.setExceptionsFiles(storage.loadExceptionsFilesList());
        for (String drive : disks) {
            if (!(new File(drive)).exists()) {
                continue;
            }
            String d = drive.substring(0, 1);
            drives.put(d, new DriveFilesChangeInfo(d));
            AtomicReference<String> path = new AtomicReference<>();
            path.set(drive);
            Future<DriveFilesChangeInfo> future = diskScaner.startDisk(path);
            diskwait.add(future);
        }

    }

    @GetMapping("status")
    public String getDiskInfo(Model model) {
        model.addAttribute("status", status());
        return "diskinfo::diskresult";
    }

    public List<DriveFilesChangeInfo> status() {
        if (!isComplete) {
            isComplete = true;
            for (Future<DriveFilesChangeInfo> fd : diskwait) {
                if (fd.isDone()) {
                    try {
                        DriveFilesChangeInfo df = fd.get();
                        drives.put(df.getDrive(), df);
                    } catch (InterruptedException ex) {
                        log.error(ex.getMessage(), ex);
                    } catch (ExecutionException ex) {
                        log.error(ex.getMessage(), ex);
                    }
                } else {
                    isComplete = false;
                }
            }
        }
        if (!isSaveExceptionsFiles && isComplete) {
            storage.saveExceptionsFilesList(config.getExceptionsFiles());
            isSaveExceptionsFiles = true;
        }
        return new ArrayList<>(drives.values());
    }

    @GetMapping("/")
    public String choosedisk(Model model) {
        model.addAttribute("disks", findFiles.getDisks());
        return "choosedisk";
    }

    @PostMapping("result")
    public String result(@RequestParam("disks") List<String> disks, Model model) {
        startscandisks(disks);
        List<String> drives = new ArrayList<>();
        List<DriveFilesChangeInfo> status = status();
        model.addAttribute("status", status);
        return "result";
    }

    @GetMapping("update")
    public String update(Model model) {
        List<String> disks = new ArrayList<>();
        for (DriveFilesChangeInfo d : drives.values()) {
            disks.add(d.getDrive() + ":");
        }
        startscandisks(disks);
        List<String> drives = new ArrayList<>();
        List<DriveFilesChangeInfo> status = status();
        model.addAttribute("status", status);
        return "result";
    }

    @GetMapping("close")
    public void diskClose(Model model) {
        SpringApplication.exit(context, new ExitCodeGenerator() {
            @Override
            public int getExitCode() {
                return 0;
            }
        });
    }

    @GetMapping("diskinfo")
    public String diskInfo(@RequestParam("name") String name, Model model) {
        for (DriveFilesChangeInfo d : status()) {
            if (d.getDrive().compareTo(name.substring(0, 1)) == 0) {
                model.addAttribute("drive", d.getDrive());
                model.addAttribute("changes", d.getFiles());
                break;
            }
        }
        return "disk";
    }
}
