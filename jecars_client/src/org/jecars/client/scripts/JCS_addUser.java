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
public class JCS_addUser extends JCS_defaultScript {

  public JCS_addUser( String[] args ) {
    super();
    parseArguments( args );
  }

  /** create
   * 
   * 
   * @throws java.lang.Exception
   */
  public void create() throws Exception {
    JC_Clientable client = getClient();
    JC_Nodeable usersN  = client.getNode( "/JeCARS/default/Users" );
    JC_Nodeable groupsN = client.getNode( "/JeCARS/default/Groups" );
    JC_UsersNode  users  = (JC_UsersNode)usersN.morphToNodeType();
    JC_GroupsNode groups = (JC_GroupsNode)groupsN.morphToNodeType();
    JC_GroupNode group = groups.getGroup( mGroup );

    // **** Add user
    JC_UserNode user;
    if (!users.hasUser( mUser )) {
      user = users.addUser( mUser, null, mUserPassword.toCharArray(), JC_PermissionNode.RS_ALLRIGHTS );
      user.save();
    }
    user = users.getUser( mUser );
    group.addUser( user );
    group.save();
    return;
  }


}
