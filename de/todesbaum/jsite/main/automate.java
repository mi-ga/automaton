/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.todesbaum.jsite.main;

import de.todesbaum.jsite.application.InsertListener;
import de.todesbaum.jsite.application.Project;
import de.todesbaum.jsite.gui.ProjectFilesPage;
import de.todesbaum.jsite.gui.ProjectPage;
import de.todesbaum.jsite.main.Main.PageType;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author de
 */
public class automate {

    public static info inf;
    private static readFolder r;
    private static boolean NEXT = true;
    private static boolean ALLDONE = true;
    static ProjectPage projectPage;
    private static ProjectFilesPage projectFilesPage;

    public static void setFinalUri(String finalURI) {
        System.out.println("finalUri--> " + finalURI);
        writeToFile(finalURI);





    }

    public static void next() {
       /*called from projectInsertFinished
       */
        
        System.err.println("NEXT--> " + NEXT);
        
        /*set the static var Next to true 
         to activate the Automatisation Thread
         * :readFolder*/
        NEXT = true;

    }
    private static Main aMain;
    public static endless endLess;


    /*test constructor to use for direct start*/
   
    /*public automate() {
    String home = System.getProperty("user.home");
    
    File[] files = new File(home + "/jSiteSites").listFiles();
    showFiles(files);
    
    }*/
    
    
    /* start constructor for autmatisation the JSite*/
    automate(ProjectPage projectPage, ProjectFilesPage projectFilesPage, Main aThis) {
       
        this.aMain = aThis;
        this.projectPage = projectPage;
        this.projectFilesPage = projectFilesPage;


        
        /*set a thread to check if in the Folder:user.home/jSiteSites 
         * are new Files to upload;
         * This folder is also used by the automatic search by findnewurls.java.
           If no folder exist-> create structure
         */
        
        endLess = new endless();
        endLess.run();

    }

    private static class endless implements Runnable {

        @Override
        public void run() {

            /*get or set the main directory to store and pick up information*/
            String home = System.getProperty("user.home");


            boolean success;
            File f = new File(home + "/jSiteSites");
            
            if (!f.exists()) {
                 success = (f.mkdirs());
                if (!success) {
                    System.err.println("Could not create Directory* /jSiteSites");
                    System.exit(0);
                }
            }


            f = new File(home + "/jSiteSites/uploadFiles");
            if (!f.exists()) {
                success = (f.mkdirs());

                if (!success) {
                    System.err.println("Could not create Directory* /jSiteSites/uploadFiles");
                    System.exit(0);
                }

            }


            f = new File(home + "/jSiteSites/movedFiles");
            if (!f.exists()) {
                success = (f.mkdirs());
                if (!success) {
                    System.err.println("Could not create Directory* /jSiteSites/movedFiles");
                    System.exit(0);
                }

            }
            
            
            
            /*  
             * when all is fine, look into the folder 
             * if there are new folders, 
             * including the new files for upload by Jsite*/
            
            File[] files = new java.io.File(home + "/jSiteSites/uploadFiles").listFiles(new FileFilter() {

                @Override
                public boolean accept(File pathname) {
                    // add here logic that identifies the system files and returns false for them.
                    if (pathname.getName().startsWith(".") && pathname.isHidden()) {
                        return false;
                    }
                    return true;
                }
            });


            try {
                Thread.sleep(2000);
            } catch (InterruptedException ex) {
                Logger.getLogger(automate.class.getName()).log(Level.SEVERE, null, ex);
            }

            if (files.length  > 0
                 /*  &&false  --> download and find stresstest*/) {
  
             listFilesAndUpload(files);
           
                } else {
                try {
                    Thread.sleep(2000);//wait 2 secs
                    try {
                         /*there is nothing, so start to create words to find webpages
                         * to upload to freenet*/
                        
                        
                        System.out.print("findNewUrls");
                        findNewUrls.createNewSearch();
                        System.out.println("end- findNewUrls");

                    } catch (IOException ex) {
                        Logger.getLogger(automate.class.getName()).log(Level.SEVERE, null, ex);
                    }

                } catch (InterruptedException ex) {
                    Logger.getLogger(automate.class.getName()).log(Level.SEVERE, null, ex);
                }

       
                /*start same Thread again*/
                run();
             }
        }
    }

    public static void chain() {
        /*  hack in JSite,
         *  to chain the events, normaly done by a realperson
        */
        
        aMain.showPage(PageType.PAGE_PROJECTS);
        projectPage.wizard.setNextEnabled(true);
        projectPage.addButton.doClick();

        projectFilesPage.defaultFileCheckBox.setEnabled(true);
        Thread add = new Thread() {

            @Override
            public void run() {
                try {
                    sleep(1000);
                    System.err.println("****add");
                    projectPage.wizard.nextButton.doClick();
                } catch (InterruptedException ex) {
                    Logger.getLogger(ProjectFilesPage.class.getName()).log(Level.SEVERE, null, ex);
                }

                Thread t = new Thread() {
                    @Override
                    public void run() {
                        try {
                            // Code here
                            sleep(1000);
                            projectPage.wizard.nextButton.doClick();
                        } catch (InterruptedException ex) {
                            Logger.getLogger(ProjectFilesPage.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                };

                t.start();
            }
        };
        add.start();
       }

    
    
    /*Logic for reading the existing jsite  */
    public static class readFolder implements Runnable {

        private Vector directoryList = null;

        public readFolder(Vector<File> parameter) {
            this.directoryList = parameter;
        }

        public void run() {

            System.out.println("thread read Folder");
            for (int i = 0; i < directoryList.size(); i++) {

                try {
                    File nFile = (File) directoryList.get(i);

                    System.out.println("project: " + i + " Directory: " + nFile.getName());
                    showFiles2(nFile.listFiles());
                    generate_Jsite_infos(nFile);


                    NEXT = false;
                    chain();
                    while (!NEXT) {
                        try {
                            java.lang.Thread.sleep(1000);
                        } catch (InterruptedException ex) {
                            Logger.getLogger(automate.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    };

                    System.out.println(":moveFolder");


                    File source = nFile;
                    System.err.println(source.getAbsolutePath());

                    String home = System.getProperty("user.home");



                    File destination = new File(home + "/jSiteSites/movedFiles/" + nFile.getName() + "_new");




                    CopyDirectory cp = new CopyDirectory();
                    
                    cp.copyFolder(source, destination);
                    cp.deleteDirectory(source);

                    System.out.println("NEXT");
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(automate.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(automate.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            System.err.println("*" + ALLDONE);

            endLess.run();

        }
    }

    public static class info {

        public File file;
        public String f_Name;

        public String getF_Name() {
            return f_Name;
        }

        public void setF_Name(String f_Name) {
            this.f_Name = f_Name;
        }

        public String getF_desc() {
            return f_desc;
        }

        public void setF_desc(String f_desc) {
            this.f_desc = f_desc;
        }

        public File getFile() {
            return file;
        }

        public void setFile(File file) {
            this.file = file;
        }

        public String getLocalPath() {
            return localPath;
        }

        public void setLocalPath(String localPath) {
            this.localPath = localPath;
        }
        public String f_desc;
        public String localPath;

        info(File file) {
            this.file = file;
            this.f_Name = file.getName();
            this.f_desc = file.getName();
            this.localPath = file.getAbsolutePath();

            System.out.println("dend" + file.getAbsolutePath());

        }
    }

    public static void main(String Args[]) throws FileNotFoundException, IOException {
        //for direct start 
        
        // new automate();

        /*   FileReader fr = new FileReader(home + "/test.txt");
        BufferedReader br = new BufferedReader(fr);
        
        String zeile = "";
        
        do {
        zeile = br.readLine();
        System.out.println(zeile);
        } while (zeile != null);
        
        br.close();
         */ 
    
    }

    public static void listFilesAndUpload(File[] files) {
     
        Vector directoryList = new Vector<File>();

        for (File file : files) {
            if (file.isDirectory()) {


                directoryList.add(file);

            } else {
                System.out.println("File: " + file.getName());
            }
        }
        ;


        r = new readFolder(directoryList);
        new Thread(r).start();




    }

    public static void showFiles2(File[] files) {

        for (File file : files) {
            if (file.isDirectory()) {
                System.out.println("Directory2: " + file.getName());
                //   showFiles2(file.listFiles()); // Calls same method again.
            } else {
                System.out.println("File2: " + file.getName());
                if (file.isHidden()) {
                    try {


                        if (file.delete()) {
                            System.out.println(file.getName() + " is deleted!");
                        } else {
                            System.out.println("Delete operation is failed.");
                        }

                    } catch (Exception e) {

                        e.printStackTrace();

                    }
                }


            }




        }
    }

    private static void generate_Jsite_infos(File file) {

        inf = new info(file);


//moveDirectory( file,  file);
    }

    public static void writeToFile(String str) {
        BufferedWriter out = null;

        String home = System.getProperty("user.home");




        try {


            FileWriter fstream = new FileWriter(home + "/jSiteSites/" + "uris.txt", true); //true tells to append data.
            out = new BufferedWriter(fstream);
            Date dNow = new Date();
            System.err.println("*save");
            SimpleDateFormat ft =
                    new SimpleDateFormat("E yyyy.MM.dd 'at' hh:mm:ss a zzz");

            System.out.println("Current Date: " + ft.format(dNow));

            out.write("\n" + dNow + " :");
            out.write("\n" + str);
        } catch (IOException e) {
            PrintWriter writer = null;
            try {
                System.err.println("Error: " + e.getMessage());
                writer = new PrintWriter(home + "/jSiteSites/" + "uris.txt", "UTF-8");
                writer.println("******* freesite uris ******");
                writer.close();


            } catch (FileNotFoundException ex) {
                Logger.getLogger(Project.class.getName()).log(Level.SEVERE, null, ex);
            } catch (UnsupportedEncodingException ex) {
                Logger.getLogger(Project.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                writer.close();
            }




        } finally {
            if (out != null) {
                try {
                    out.close();


                } catch (IOException ex) {
                    Logger.getLogger(Project.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
}
