# automaton
Freenet plugin

The automaton searches for the words in bing.com, downloads the result, stores them in the jSiteSites folder in the home directory of the node, and uploads to the Freenet. After uploading to the Freenet, the process starts anew. It is built upon jSite, an open source software package for uploading information onto the Freenet storage system. jSite was tweaked in such a way that it would upload given information with no additional human interaction with the program. 

jSite's main class, the Main.java, is manipulated in order to automate its own functionality and implement new automaton functions. Additional classes: automate.java, CopyDirectory.java, findNewUrls.java, and aPlugin.java. 
