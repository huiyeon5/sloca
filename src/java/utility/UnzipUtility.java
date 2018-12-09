/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utility;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 *
 * @author Huiyeon Kim
 */
public class UnzipUtility {
     private static final int BUFFER_SIZE = 4096;
    /**
     * Extracts a zip file specified by the zipFilePath to a directory specified by
     * destDirectory (will be created if does not exists)
     * @param zipFilePath - path to the zip file
     * @param destDirectory - path to destination
     * @return ArrayList of file path extracted
     * @throws IOException if file not found
     */
    public ArrayList<String> unzip(String zipFilePath, String destDirectory) throws IOException {
        ArrayList<String> files = new ArrayList<>();
        File destDir = new File(destDirectory);
        if (!destDir.exists()) {
            destDir.mkdir();
        }
        ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zipFilePath));
        ZipEntry entry = zipIn.getNextEntry();
        
        String demo = null;
        String loc = null;
        String locData = null;
        
        // iterates over entries in the zip file
        while (entry != null) {
            String filePath = destDirectory + File.separator + entry.getName();
            switch (entry.getName()) {
                case "demographics.csv":
                    demo = destDirectory + File.separator + entry.getName();
                    break;
                case "location.csv":
                    locData = destDirectory + File.separator + entry.getName();
                    break;
                case "location-lookup.csv":
                    loc = destDirectory + File.separator + entry.getName();
                    break;
                default:
                    break;
            }
            
            if (!entry.isDirectory()) {
                // if the entry is a file, extracts it
                extractFile(zipIn, filePath);
            } else {
                // if the entry is a directory, make the directory
                File dir = new File(filePath);
                dir.mkdir();
            }
            zipIn.closeEntry();
            entry = zipIn.getNextEntry();
        }
        zipIn.close();
        
        if(loc!=null){
            files.add(loc);
        }
        if(locData!=null){
            files.add(locData);
        }
        if(demo!=null){
            files.add(demo);
        }
        
        
        return files;
    }
    /**
     * Extracts a zip entry (file entry)
     * @param zipIn - zipinputstream
     * @param filePath - path to file
     * @throws IOException if file not found
     */
    private void extractFile(ZipInputStream zipIn, String filePath) throws IOException {
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath));
        byte[] bytesIn = new byte[BUFFER_SIZE];
        int read = 0;
        while ((read = zipIn.read(bytesIn)) != -1) {
            bos.write(bytesIn, 0, read);
        }
        bos.close();
    }
}

