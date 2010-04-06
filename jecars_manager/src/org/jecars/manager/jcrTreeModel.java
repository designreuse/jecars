/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jecars.manager;

import java.io.IOException;
import javax.jcr.LoginException;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import org.apache.jackrabbit.core.TransientRepository;

/**
 *
 * @author weert
 */
public class jcrTreeModel {

  private Repository mJRRepository = null;
  private Session    mSession;

  public jcrTreeModel() {
    return;
  }

  /** jcrTreeModel
   *
   * @param pRepDirectory
   * @throws IOException
   * @throws LoginException
   * @throws RepositoryException
   */
  public jcrTreeModel( final String pRepDirectory ) throws IOException, LoginException, RepositoryException {
    mJRRepository = new TransientRepository( "repository.xml", pRepDirectory );
    mSession = mJRRepository.login( new SimpleCredentials( "Administrator", "admin".toCharArray() ));
    return;
  }



  public void disconnect() {
    return;
  }

  /** getRootTreeNode
   *
   * @return
   * @throws RepositoryException
   */
  public jcrTreeNode getRootTreeNode() throws Exception {
    final jcrTreeNode tn = new jcrTreeNode( mSession.getRootNode() );
    return tn;
  }

}
