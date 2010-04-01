/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jecars.manager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 *
 * @author weert
 */
public class jcrTreeNode extends DefaultMutableTreeNode {

  private boolean mChildrenUpdated = false;
  private Node    mNode;

  public jcrTreeNode() {
  }

  public jcrTreeNode( Node pNode ) {
    mNode = pNode;
    return;
  }

//  public Node getJCRNode() {
//    return mNode;
//  }

  protected void updateNodeChildren() {
    try {
      NodeIterator ni = mNode.getNodes();
      for ( ; ni.hasNext();) {
        Node n = ni.nextNode();
        add( new jcrTreeNode( n ));
      }
    } catch( Exception e ) {
      reportError( e );
    }
    return;
  }

  @Override
  public String toString() {
    try {
      return getNodeName();
    } catch( Exception re ) {
      return re.getMessage();
    }
  }

  @Override
  public int getChildCount() {
    if (!mChildrenUpdated) {
      mChildrenUpdated = true;
      updateNodeChildren();
    }
    return super.getChildCount();
  }

  protected void reportError( Throwable pE ) {
    pE.printStackTrace();
    return;
  }

  public String getNodeName() throws Exception {
    return mNode.getName();
  }


  public jcrProperty getPropertyByName( final String pName ) throws PathNotFoundException, RepositoryException {
    return new jcrProperty( mNode.getProperty( pName ) );
  }

  public List<jcrProperty> getProperties() throws Exception {
    List<jcrProperty> props = new ArrayList<jcrProperty>();
    PropertyIterator pi = mNode.getProperties();
    while( pi.hasNext() ) {
      props.add( new jcrProperty( pi.nextProperty() ));
    }
    return Collections.unmodifiableList( props );
  }

  public void refresh() throws Exception {
    mNode.refresh( false );
    return;
  }


}
