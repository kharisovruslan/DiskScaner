/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vd.DiskScaner.Files;

import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;
import vd.DiskScaner.DriveFilesChangeInfo;

/**
 *
 * @author Kharisov Ruslan
 *
 * class add for start async process self-invocation – calling the async method
 * from within the same class – won't work
 */
@Component
public class DiskScaner {

    @Autowired
    private ApplicationContext context;

    org.slf4j.Logger log = LoggerFactory.getLogger(DiskScaner.class);

    public DiskScaner() {
    }

    @Async
    public Future<DriveFilesChangeInfo> startDisk(AtomicReference<String> rdrive) {
        String drive = rdrive.get();
        System.out.println("start scan drive: " + drive);
        FindFiles ff = context.getBean(FindFiles.class);
        FilesStorage storage = context.getBean(FilesStorage.class);
        FilesCompare comparator = context.getBean(FilesCompare.class);
        Set<String> exeptionsFiles = storage.loadExceptionsFilesList();
        FileInfo scan = ff.startwithdisk(drive);
        FileInfo load = storage.load(drive);
        DriveFilesChangeInfo drivechangeinfo = new DriveFilesChangeInfo(drive.substring(0, 1));
        if (load != null) {
            List<FileChangeInfo> changes = comparator.compareFiles(scan, load);
            drivechangeinfo.setFiles(changes);
            drivechangeinfo.setStatus("report");
            if (changes.size() == 0) {
                drivechangeinfo.setStatus("no changes");
            }
        } else {
            drivechangeinfo.setStatus("successful make snapshot");
        }
        if (drivechangeinfo.getStatus().compareTo("report") == 0) {
            drivechangeinfo.setMakelink(true);
        }
        storage.save(scan, drive);
        storage.saveExceptionsFilesList(exeptionsFiles);
        drivechangeinfo.setComplete(true);
        return new AsyncResult<DriveFilesChangeInfo>(drivechangeinfo);
    }
}
