package de.todesbaum.jsite.main;

import freenet.pluginmanager.FredPlugin;
import freenet.pluginmanager.PluginRespirator;
import java.util.logging.Logger;





/*this is the start class for the freenet Jsite-Automator plugin*/

public class aPlugin implements FredPlugin {

    private final static Logger LOGGER = Logger.getLogger(aPlugin.class.getName());
    private PluginRespirator pr;
    private Main jsite;

    @Override
    public void runPlugin(PluginRespirator pr) {
        this.pr = pr;
        /*jump to jsite*/
        jsite = new Main();
    }


    
    @Override
    public void terminate() {
        jsite=null;

    }

    public static void main(String[] args) {
    	System.out.println("Plugin started");
    }
}
