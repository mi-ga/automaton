package de.todesbaum.jsite.main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.print.attribute.standard.MediaSize.NA;

public class CopyDirectory {
    /*used for proper copying directorys in JsiteSites*/

    /*direct test*/
    public static void main(String[] args) {

        /*  String home = System.getProperty("user.home");
        File srcFolder = new File(home + "/jSiteSites/uploadFiles/example_com_2");
        System.err.println(srcFolder.getAbsolutePath());
        
        File destFolder = new File(home + "/jSiteSites/movedFiles/example_com_2a");
        
        //make sure source exists
        if (!srcFolder.exists()) {
        
        System.out.println("Directory does not exist.");
        //just exit
        System.exit(0);
        
        } else {
        
        try {
        copyFolder(srcFolder, destFolder);
        } catch (IOException e) {
        e.printStackTrace();
        //error, just exit
        System.exit(0);
        }
        }
        
        System.out.println("Dir"+srcFolder.getName());
        deleteDirectory(srcFolder);
        System.out.println("Done");*/
    }

    public static void copyFolder(File src, File dest)
            throws IOException {

        if (src.isDirectory()) {

            /*if directory not exists, create it
             */

            if (!dest.exists()) {
                dest.mkdir();
                System.out.println("Directory copied from "
                        + src + "  to " + dest);
            }

            /*list all the directory contents*/
            String files[] = src.list();

            for (String file : files) {
                /*construct the src and dest file structure*/
                File srcFile = new File(src, file);
                File destFile = new File(dest, file);
                /*recursive copy*/
                copyFolder(srcFile, destFile);
            }

        } else {
            /*if file, then copy it
            Use bytes stream to support all file types
             */

            InputStream in = new FileInputStream(src);
            OutputStream out = new FileOutputStream(dest);

            byte[] buffer = new byte[1024];

            int length;
            //copy the file content in bytes 
            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }

            in.close();
            out.close();


        }

    }

    public static boolean deleteDirectory(File directory) {
        if (directory.exists()) {
            File[] files = directory.listFiles();
            if (null != files) {
                for (int i = 0; i < files.length; i++) {
                    if (files[i].isDirectory()) {
                        deleteDirectory(files[i]);
                    } else {
                        files[i].delete();
                    }
                }
            }
        }
        return (directory.delete());
    }
}
