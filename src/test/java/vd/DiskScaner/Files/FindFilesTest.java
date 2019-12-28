/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vd.DiskScaner.Files;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import static org.junit.Assert.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import vd.DiskScaner.ConfigProperties;
import vd.DiskScaner.DiskScanerApplication;

/**
 *
 * @author Kharisov Ruslan
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = DiskScanerApplication.class)
public class FindFilesTest {

    org.slf4j.Logger log = LoggerFactory.getLogger(FindFilesTest.class);

    @Autowired
    FindFiles findFiles;
    @Autowired
    FilesStorage storage;
    @Autowired
    ConfigProperties config;

    String folderName;

    public FindFilesTest() {
    }

    @BeforeEach
    public void setUp(@TempDir File folder) {
        folderName = folder.getAbsolutePath();
        config.setPathdata(folderName);
        makeFile(folder, "file1.txt", "text file 1");
        makeFile(folder, "file2.txt", "text file 2");
        makeFile(folder, "file3.txt", "text file 3");
        File folder1 = makeFolder(folder, "folder1");
        makeFile(folder1, "file4.txt", "text file 4");
        makeFile(folder1, "file5.txt", "text file 5");
    }

    @AfterEach
    public void tearDown() {
    }

    @Test
    public void testStartwithdisk() {
        System.out.println("start");
        FileInfo fi = findFiles.startwithdisk(folderName);
        assertTrue("start files don't work correctly", fi.getFiles().size() == 4);
        FileInfo folder = fi.getFiles().get(3);
        assertTrue("start files don't work correctly", folder.getFiles().size() == 2);
    }

    @Test
    public void testSaveAndLoadFiles() {
        System.out.println("start");
        FileInfo root = findFiles.startwithdisk(folderName);
        storage.save(root, "C:");
        FileInfo filesload = storage.load("C:");
        assertEquals(root.getFiles().size(), filesload.getFiles().size());
    }

    private File makeFile(File parentdir, String filename, String text) {
        File f = new File(parentdir, filename);
        try (FileOutputStream fos = new FileOutputStream(f);) {
            fos.write(text.getBytes());
        } catch (FileNotFoundException ex) {
            log.error(ex.getMessage() + " filename: " + filename, ex);
        } catch (IOException ex) {
            log.error(ex.getMessage() + " filename: " + filename, ex);
        }
        return f;
    }

    private File makeFolder(File parentdir, String name) {
        File dir = new File(parentdir, name);
        dir.mkdir();
        return dir;
    }
}
