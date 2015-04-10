/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.todesbaum.jsite.main;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Random;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author de
 */
public class findNewUrls {

    //creating a vector for fresh existing urls 
    static Vector<String> urls = new Vector<String>();
    //check list
    static String spaceSeparatedWords = "";
    ;
    //necessary to avoid some word conflict
    static Vector<String> usedW = new Vector<String>();

    static void createNewSearch() throws IOException {
        BufferedReader reader = null;
        String nextWord = "";

        try {

             /*if allready exist a wordbuffer, take that,
             * else start from scratch based on a string of 5 random-chars.
             * The wordBuffer is stored local under your homefolder:
             * user.home/jSiteSites/wordBuffer.txt"
             */
            
            
            String home = System.getProperty("user.home");
            reader = new BufferedReader(new FileReader(home + "/jSiteSites/" + "wordBuffer.txt"));
            String line = null;
            String ww = "";


            /*reading the wordBuffer*/
            while ((line = reader.readLine()) != null) {
                ww += line;
            }

            /*test console output of existing words in file*/
            System.out.println(ww);



            /*if its empty ->5 random chars*/
            if ("".equals(ww)) {
                ww = createRandomString(5);
            }


            /*take one word*/
            String[] parts = ww.split("\\s");//spliting whitespaces
            String str = parts[(int) (parts.length * Math.random())];//take one
            nextWord = str;


            /*write the used words to an array, for avoiding multiple uploads*/

            for (int i = 0; i < parts.length; i++) {
                System.err.print("/" + parts[i]);

                if (i > 265) {
                    break;
                }
                if (!usedW.contains(parts[i])
                        && !str.equals(parts[i])) {
                    usedW.add(parts[i]);
                }

            };


            reader.close();


        } catch (FileNotFoundException ex) {
            /*if any trouble create a new string and go forward*/
            String randS = createRandomString(5);
            System.err.println(randS);
            nextWord = randS;
        } finally {
        }



        searchHtml(nextWord);

    }

    public static void searchHtml(String toSearch) throws IOException {

        try {

            /*for fun_ use a word here*/
            toSearch = toSearch == "" ? "erlauben" : toSearch;
            
            
            /*creating a dynamic string buffer for the new words*/
            Vector<String> wordList = new Vector<String>();

             /* for finding websites, automator is using "BING"- webservice 
             * the search results will be also parsed, to find words and
             * generate somthing like a chaining- serach!!
             * 
             */
            
            URL url = new URL("http://www.bing.com/search?q=" + toSearch);
            BufferedReader reader = null;



            /*catch the search results*/
            
            try {
                try {
                    reader = new BufferedReader(new InputStreamReader(url.openStream(), "UTF-8"));
                } catch (UnsupportedEncodingException ex) {
                    Logger.getLogger(findNewUrls.class.getName()).log(Level.SEVERE, null, ex);
                }
                String s = "";
                for (String line; (line = reader.readLine()) != null;) {
                    s += line;
                }


                /* bing-site specific parsing to get the urls 
                 * bether using later an search api __
                 * but no one would give for that their service !*/
              
                String stringToSearch = s;
                Pattern p = Pattern.compile("<ol id=\"b_results\">(.*?)</ol>");
                Matcher m = p.matcher(stringToSearch);
                String str = null;

                if (m.find()) {
                    String codeGroup = m.group(1);
                    str += codeGroup;
                }
                stringToSearch = str;
                p = Pattern.compile("<li class=\"b_algo\"><h2>(.*?)</h2>");
                m = p.matcher(stringToSearch);

                stringToSearch = "";

                while (m.find()) {

                    String codeGroup = m.group(1);
                    stringToSearch += codeGroup;
                }
                p = Pattern.compile("<a (.*?)</a>");
                m = p.matcher(stringToSearch);



                Vector vS = new Vector<String>();

                while (m.find()) {
                    String codeGroup = m.group(1);
                    vS.add(codeGroup);
                }



                String p3 = "";
                for (int i = 0; i < vS.size(); i++) {
                    String toLook = (String) vS.get(i);
                    p = Pattern.compile("href=\"(.*?)\"");
                    m = p.matcher(toLook);

                    String[] res = new String[2];

                    //System.out.println("-->" + toLook);
                    if (m.find()) {

                        // get the matching group based on regular expressions
                        String codeGroup = m.group(1);
                        String p1 = codeGroup;
                        p = Pattern.compile("href=\".*?\">(.*)");
                        m = p.matcher(toLook);
                        String p2 = "";
                        if (m.find()) {
                            p2 = m.group(1);
                        }


                        p2 = p2.replaceAll("\\<[^>]*>", " ");
                        p2 = p2.replaceAll("[^a-zA-Z]+", " ");
                        System.out.println(p1 + "__" + p2);
                        res[0] = p1;
                        res[1] = p2;
                        p3 += p2;
                        urls.add(p1);
                    }

                }

                String[] splitArray = p3.split("\\s+");


                /*  filter the words against existing words.
                 * 
                 * 
                 */

                for (int i = 0; i < splitArray.length; i++) {

                    String w = splitArray[i];

                    if (w.length() > 3) {
                        if (!usedW.contains(w) && !wordList.contains(w)) {
                            wordList.add(w);
                            spaceSeparatedWords += w + " ";
                            //System.out.println(w);
                        }
                    }
                }

                for (int i = 0; i < usedW.size(); i++) {
                    String w = usedW.get(i);
                    spaceSeparatedWords += w + " ";
                }


                /*print out some interesting vars*/
                System.out.println("(wordList.size());" + wordList.size());
                System.out.println("urls.size()" + urls.size());


            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException ignore) {
                    }
                }
            }
        } catch (MalformedURLException ex) {
            Logger.getLogger(findNewUrls.class.getName()).log(Level.SEVERE, null, ex);
        }


        /*Scoring*/
        if (urls.isEmpty()) {
            System.out.println("new_search");
            createNewSearch();

        } else {
            writeToFile(spaceSeparatedWords);
            downloadHtml_and_createFolder();

        }



    }

    private static void downloadHtml_and_createFolder() throws UnsupportedEncodingException, IOException {

        /**/
        
        
        String content = "";
        String urlStr = "";
        while (urls.size() > 0) {
            try {
                /* pic a url from the url-collection
                 * and check if the content of that url is big enough 
                 * to seems interesting for an upload to Freenet
                 */
                
                int pos = urls.size() / 2;
                urlStr = urls.remove(pos);
                System.out.println("url to look:" + urlStr);

                BufferedReader reader = null;
                URL url = new URL(urlStr);
                reader = new BufferedReader(new InputStreamReader(url.openStream(), "UTF-8"));

                for (String line; (line = reader.readLine()) != null;) {
                    /*the downloaded html, line by line*/
                    content += line;
                }
            } catch (MalformedURLException ex) {
                Logger.getLogger(findNewUrls.class.getName()).log(Level.SEVERE, null, ex);
            }

            if (content.length() >= 100) {
                /*the url looks fine so break*/
                
                break;
            }

        }

        if (content.length() >= 100) {
            /*prepare the folder structure for the manipulated JSite
            */
            
            createFolder(urlStr, content);
       
        } else {
            createNewSearch();
        }

    }

    public static void writeToFile(String Words) {
        
        BufferedWriter out = null;
        String home = System.getProperty("user.home");




        try {

            File f = new File((home + "/jSiteSites/" + "wordBuffer.txt"));
            f.createNewFile();

            FileWriter fstream = new FileWriter(f);
            out = new BufferedWriter(fstream);
            out.write("\n" + Words);


        } catch (IOException ex) {
            Logger.getLogger(findNewUrls.class.getName()).log(Level.SEVERE, null, ex);

        } finally {
            if (out != null) {
                try {
                    out.close();


                } catch (IOException ex) {
                    Logger.getLogger(findNewUrls.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    private static void createFolder(String urlStr, String content) throws IOException {
        String home = System.getProperty("user.home");
        BufferedWriter out = null;
       
        
        /*the urls must be sanitized to use them as folder names*/
       
        urlStr = urlStr.replaceAll("[^a-zA-Z]+", "");
        File file = new File(home + "/jSiteSites/uploadFiles/" + urlStr);
        boolean success = (file.mkdirs());
    
        if (success) {
            System.out.println("create: " + home + "/jSiteSites/uploadFiles/" + urlStr);
            FileWriter fstream = new FileWriter(home + "/jSiteSites/uploadFiles/" + urlStr + "/index.html", true); //true tells to append data.
            out = new BufferedWriter(fstream);
            out.write("\n" + content);
          } else {
            /*if there as exception*/
            System.err.print("noSave*createFolder*findNewUrls");
        }
  }

    /*helper function for create a random string for searching*/
    private static String createRandomString(int i) {
        String randS = "";
        Random r = new Random();
        for (int j = 0; j < i; j++) {
            char c = (char) (r.nextInt(26) + 'a');
            randS += c;
        }


        return randS;
    }
}
