/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jecars.client.scripts;

import java.util.Collection;
import org.jecars.client.JC_Clientable;
import org.jecars.client.JC_Filter;
import org.jecars.client.JC_Nodeable;
import org.jecars.client.nt.JC_UserNode;

/**
 *
 * @author weert
 */
public class JCS_listUsers extends JCS_defaultScript {

  public JCS_listUsers( String[] args ) {
    super();
    parseArguments( args );
  }

  /** create
   * 
   * 
   * @throws java.lang.Exception
   */
  public void list() throws Exception {
    final JC_Clientable client = getClient();
    final JC_Nodeable usersN  = client.getNode( "/JeCARS/default/Users" );

    JC_Filter filter = JC_Filter.createFilter();
    filter.addCategory( "User" );
    Collection<JC_Nodeable> users = usersN.getNodes( null, filter, null );
    for (JC_Nodeable user : users) {
      JC_UserNode un = (JC_UserNode)user;
      if (un.getSuspended()) {
        mStdOutput.println( "User: " + un.getName() + " (SUSPENDED)" );
      } else {
        mStdOutput.println( "User: " + un.getName()  );
      }
    }

    return;
  }
  

}
