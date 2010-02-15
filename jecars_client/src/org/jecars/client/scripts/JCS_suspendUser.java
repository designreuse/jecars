/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jecars.client.scripts;

import java.util.logging.Level;
import org.jecars.client.JC_Clientable;
import org.jecars.client.JC_Nodeable;
import org.jecars.client.nt.JC_GroupNode;
import org.jecars.client.nt.JC_GroupsNode;
import org.jecars.client.nt.JC_PermissionNode;
import org.jecars.client.nt.JC_UserNode;
import org.jecars.client.nt.JC_UsersNode;

/**
 *
 * @author weert
 */
public class JCS_suspendUser extends JCS_defaultScript {

  public JCS_suspendUser( String[] args ) {
    super( args );
  }

  /** create
   * 
   * 
   * @throws java.lang.Exception
   */
  public void suspend() throws Exception {
    final JC_Clientable client = getClient();
    final JC_Nodeable usersN  = client.getNode( "/JeCARS/default/Users" );
    final JC_UsersNode  users  = (JC_UsersNode)usersN.morphToNodeType();

    // **** Suspend/unsuspend user
    if (users.hasUser( mUser )) {
      final JC_UserNode user = users.getUser( mUser );
      user.setSuspended( Boolean.parseBoolean(mBoolOption) );
      user.save();
    } else {
      mErrOutput.println( "User: " + users.getPath() + "/" + mUser + " not found" );
    }
    return;
  }
  

}
