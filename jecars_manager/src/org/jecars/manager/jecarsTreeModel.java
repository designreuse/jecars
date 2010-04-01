/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jecars.manager;

import javax.swing.tree.TreeNode;
import org.jecars.client.JC_Clientable;
import org.jecars.client.JC_Exception;
import org.jecars.client.JC_Factory;

/**
 *
 * @author weert
 */
public class jecarsTreeModel extends jcrTreeModel {

  private final JC_Clientable mClient;
  
  /** jecarsTreeModel
   * 
   * @param pServer
   * @param pUsername
   * @param pPassword
   * @throws JC_Exception
   */
  public jecarsTreeModel( final String pServer, final String pUsername, final char[] pPassword ) throws JC_Exception {
    super();
    mClient = JC_Factory.createClient( pServer );
    mClient.setCredentials( pUsername, pPassword );
    return;
  }

  @Override
  public void disconnect() {
    return;
  }

  @Override
  public jcrTreeNode getRootTreeNode() throws Exception {
    jecarsTreeNode tn = new jecarsTreeNode( mClient.getRootNode() );
    return tn;
  }




}
