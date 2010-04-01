/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jecars.client.scripts;

import org.jecars.client.gui.UsersManagerFrame;

/**
 *
 * @author weert
 */
public class JCS_scriptClient {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
//      args = new String[7];
//      args[0] = "addUser";
//      args[1] = "-s=http://localhost:8080/cars";
//      args[2] = "-u=Administrator";
//      args[3] = "-p=admin";
//      args[4] = "-user=test1";
//      args[5] = "-userpassword=test";
//      args[6] = "-group=Test_Normal";
      if (args.length<2) {
        System.err.println( "Usage: jecarsClient <initJeCARS|addUser|suspendUser|listUsers> [-s=servername] [-u=loginname] [-p=loginpassword]" );
        System.exit( -1 );
      }
      try {
        if ("initJeCARS".equals( args[0] )) {
          JCS_initJeCARS init = new JCS_initJeCARS();
          JCS_defaultScript ds = new JCS_defaultScript( args );
          init.mJeCARSServer = ds.mJeCARSServer;
          init.mUsername     = ds.mUsername;
          init.mPassword     = ds.mPassword;
          init.startInit();
        }
        if ("addUser".equals( args[0] )) {
          JCS_addUser jif = new JCS_addUser( args );
          jif.create();
        }
        if ("suspendUser".equals( args[0] )) {
          JCS_suspendUser jif = new JCS_suspendUser( args );
          jif.suspend();
        }
        if ("listUsers".equals( args[0] )) {
          JCS_listUsers jif = new JCS_listUsers( args );
          jif.list();
        }
        if ("usersManager".equals( args[0] )) {
          UsersManagerFrame.main( args );
        }
      } catch( Exception e ) {
        e.printStackTrace();
      }
    }

}
