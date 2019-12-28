/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vd.DiskScaner.Files;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.PostConstruct;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.io.IOUtils;
import org.slf4j.LoggerFactory;
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
public class FilesStorage {

    org.slf4j.Logger log = LoggerFactory.getLogger(FilesStorage.class);

    @Autowired
    ConfigProperties config;

    private List<FileInfo> files;

    @PostConstruct
    public void init() {
        files = new ArrayList<>();
    }

    public FilesStorage() {
    }

    private File getFileName(String drive) throws FileNotFoundException {
        String filename = config.getPathdata() + config.getSplit() + drive.charAt(0) + "_data.json.bz2";
        return new File(filename);
    }

    private File getExceptionsFileName() {
        String filename = config.getPathdata() + config.getSplit() + "exceptions_files_list.properties";
        return new File(filename);
    }

    public Set<String> loadExceptionsFilesList() {
        Set<String> l = new HashSet<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(getExceptionsFileName()), "UTF-8"));) {
            String r;
            while ((r = br.readLine()) != null) {
                l.add(r);
            }
        } catch (FileNotFoundException ex) {
            log.error(ex.getMessage() + " file " + getExceptionsFileName().getAbsolutePath(), ex);
        } catch (IOException ex) {
            log.error(ex.getMessage() + " file " + getExceptionsFileName().getAbsolutePath(), ex);
        }
        return l;
    }

    public void saveExceptionsFilesList(Set<String> set) {
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(getExceptionsFileName()), "UTF-8"));) {
            for (String r : set) {
                bw.write(r + "\n");
            }
        } catch (IOException ex) {
            log.error(ex.getMessage() + " file " + getExceptionsFileName().getAbsolutePath(), ex);
        }
    }

    public void save(FileInfo root, String drive) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.writeValue(compress(getFileName(drive)), root);
        } catch (FileNotFoundException ex) {
            log.error(ex.getMessage() + " drive " + drive, drive);
        } catch (IOException ex) {
            log.error(ex.getMessage() + " drive " + drive, drive);
        }
    }

    public FileInfo load(String drive) {
        try {
            File file = getFileName(drive);
            if (!file.exists()) {
                return null;
            }
            ObjectMapper mapper = new ObjectMapper();
            FileInfo root = mapper.readValue(decompress(file), FileInfo.class);
            restoreAttributes(root);
            return root;
        } catch (FileNotFoundException ex) {
            log.error(ex.getMessage() + " drive " + drive, ex);
        } catch (IOException ex) {
            log.error(ex.getMessage() + " drive " + drive, ex);
        }
        return null;
    }

    private void restoreAttributes(FileInfo root) {
        root.getFiles().forEach(fi -> {
            fi.setParent(root);
            if (fi.isDirectory()) {
                restoreAttributes(fi);
            }
        });
    }

    private OutputStream compress(File f) {
        try {
            return new GzipCompressorOutputStream(new FileOutputStream(f));
        } catch (FileNotFoundException ex) {
            log.error(ex.getMessage() + " file " + f.getAbsolutePath(), ex);
        } catch (IOException ex) {
            log.error(ex.getMessage() + " file " + f.getAbsolutePath(), ex);
        }
        return null;
    }

    private InputStream decompress(File f) {
        try {
            return new GzipCompressorInputStream(new FileInputStream(f));
        } catch (FileNotFoundException ex) {
            log.error(ex.getMessage() + " file " + f.getAbsolutePath(), ex);
        } catch (IOException ex) {
            log.error(ex.getMessage() + " file " + f.getAbsolutePath(), ex);
        }
        return null;
    }

    private byte[] compress(byte[] a) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            GzipCompressorOutputStream gzip = new GzipCompressorOutputStream(out);
            gzip.write(a, 0, a.length);
            gzip.close();
            out.close();
            return out.toByteArray();
        } catch (IOException ex) {
            log.error(ex.getMessage(), ex);
        }
        return a;
    }

    private byte[] decompress(byte[] a) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            GzipCompressorInputStream gzip = new GzipCompressorInputStream(new ByteArrayInputStream(a));
            return IOUtils.toByteArray(gzip);
        } catch (IOException ex) {
            log.error(ex.getMessage(), ex);
        }
        return a;
    }
}
