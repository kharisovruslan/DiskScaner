/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vd.DiskScaner.Files;

import java.util.List;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import vd.DiskScaner.DiskScanerApplication;

/**
 *
 * @author Kharisov Ruslan
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = DiskScanerApplication.class)
public class FilesCompareTest {

    @Autowired
    FilesCompare compare;

    @Autowired
    private ApplicationContext context;

    public FilesCompareTest() {
    }

    public FileInfo makeDataLoad() {
        FileInfo root = context.getBean(FileInfo.class, "C:\\");
        FileInfo f1 = context.getBean(FileInfo.class, root, "folder1", true, 0);
        root.getFiles().add(f1);
        root.getFiles().add(context.getBean(FileInfo.class, root, "f1f1.txt", false, 100));
        root.getFiles().add(context.getBean(FileInfo.class, root, "f1f2.txt", false, 200));
        root.getFiles().add(context.getBean(FileInfo.class, root, "f1f3.txt", false, 300));
        root.getFiles().add(context.getBean(FileInfo.class, root, "f1fDelete.txt", false, 300));
        f1.getFiles().add(context.getBean(FileInfo.class, root, "f21f1.txt", false, 400));
        f1.getFiles().add(context.getBean(FileInfo.class, root, "f2f2.txt", false, 500));
        f1.getFiles().add(context.getBean(FileInfo.class, root, "f2f3change.txt", false, 500));
        FileInfo f2 = context.getBean(FileInfo.class, root, "folder2", true, 0);
        f1.getFiles().add(f2);
        f2.getFiles().add(context.getBean(FileInfo.class, root, "f3f1.txt", false, 900));
        f2.getFiles().add(context.getBean(FileInfo.class, root, "f3f2.txt", false, 500));
        f2.getFiles().add(context.getBean(FileInfo.class, root, "f3fDelete.txt", false, 500));
        return root;
    }

    public FileInfo makeDataFind() {
        FileInfo root = context.getBean(FileInfo.class, "C:\\");
        FileInfo f1 = context.getBean(FileInfo.class, root, "folder1", true, 0);
        root.getFiles().add(f1);
        root.getFiles().add(context.getBean(FileInfo.class, root, "f1f1.txt", false, 100));
        root.getFiles().add(context.getBean(FileInfo.class, root, "f1f2.txt", false, 200));
        root.getFiles().add(context.getBean(FileInfo.class, root, "f1f3.txt", false, 300));
        root.getFiles().add(context.getBean(FileInfo.class, root, "f1fNew.txt", false, 500));
        f1.getFiles().add(context.getBean(FileInfo.class, root, "f21f1.txt", false, 400));
        f1.getFiles().add(context.getBean(FileInfo.class, root, "f2f2.txt", false, 500));
        f1.getFiles().add(context.getBean(FileInfo.class, root, "f2f3change.txt", false, 900));
        FileInfo f2 = context.getBean(FileInfo.class, root, "folder2", true, 0);
        f1.getFiles().add(f2);
        f2.getFiles().add(context.getBean(FileInfo.class, root, "f3f1.txt", false, 900));
        f2.getFiles().add(context.getBean(FileInfo.class, root, "f3f2.txt", false, 500));
        return root;
    }

    public FileInfo makeDataLoadFloat() {
        FileInfo root = context.getBean(FileInfo.class, "C:\\");
        root.getFiles().add(context.getBean(FileInfo.class, root, "fa.txt", false, 100));
        root.getFiles().add(context.getBean(FileInfo.class, root, "fc1del.txt", false, 300));
        root.getFiles().add(context.getBean(FileInfo.class, root, "fd.txt", false, 300));
        return root;
    }

    public FileInfo makeDataFindFloat() {
        FileInfo root = context.getBean(FileInfo.class, "C:\\");
        FileInfo f1 = context.getBean(FileInfo.class, root, "folder1", true, 0);
        root.getFiles().add(context.getBean(FileInfo.class, root, "fa.txt", false, 900));
        root.getFiles().add(context.getBean(FileInfo.class, root, "fb1new.txt", false, 200));
        root.getFiles().add(context.getBean(FileInfo.class, root, "fd.txt", false, 300));
        root.getFiles().add(context.getBean(FileInfo.class, root, "fd1new.txt", false, 300));
        root.getFiles().add(context.getBean(FileInfo.class, root, "fd2new.txt", false, 300));
        return root;
    }

    public FileInfo makeDataEmpty() {
        FileInfo root = context.getBean(FileInfo.class, "C:\\");
        return root;
    }

    @Test
    public void testCompareFiles() {
        System.out.println("compareFiles only new");
        FileInfo scan = makeDataFind();
        FileInfo load = makeDataEmpty();
        List<FileChangeInfo> result = compare.compareFiles(scan, load);
        result.forEach(f -> System.out.println("only new: " + f));
        assertTrue(result.size() == 11);

        scan = makeDataEmpty();
        load = makeDataFind();
        result = compare.compareFiles(scan, load);
        result.forEach(f -> System.out.println("only load: " + f));
        assertTrue(result.size() == 11);

        System.out.println("compareFiles");
        scan = makeDataFindFloat();
        load = makeDataLoadFloat();
        result = compare.compareFiles(scan, load);
        result.forEach(f -> System.out.println("more add: " + f));
        //assertTrue(result.size() == 5);

        System.out.println("compareFiles");
        load = makeDataFindFloat();
        scan = makeDataLoadFloat();
        result = compare.compareFiles(scan, load);
        result.forEach(f -> System.out.println("more delete: " + f));
        assertTrue(result.size() == 3);

    }
}
