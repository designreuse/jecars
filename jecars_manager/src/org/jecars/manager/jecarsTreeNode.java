/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jecars.manager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.jecars.client.JC_Exception;
import org.jecars.client.JC_Nodeable;
import org.jecars.client.JC_Propertyable;

/**
 *
 * @author weert
 */
public class jecarsTreeNode extends jcrTreeNode {

  private final JC_Nodeable mJCNode;

  public jecarsTreeNode( final JC_Nodeable pNode ) {
    mJCNode = pNode;
    return;
  }

//  public JC_Nodeable getJCNode() {
//    return mJCNode;
//  }

  @Override
  protected void updateNodeChildren() {
    try {
      Collection<JC_Nodeable> nodes = mJCNode.getNodes();
      for (JC_Nodeable node : nodes) {
        add( new jecarsTreeNode( node ) );
      }
    } catch( Exception e ) {
      reportError( e );
    }
    return;
  }

  @Override
  public String getNodeName() throws Exception {
    return mJCNode.getName();
  }

  @Override
  public List<jcrProperty> getProperties() throws Exception {
    List<jcrProperty> props = new ArrayList<jcrProperty>();
    Collection<JC_Propertyable> pi = mJCNode.getProperties();
    for (JC_Propertyable p : pi) {
      props.add( new jecarsProperty( p ));
    }
    return Collections.unmodifiableList( props );
  }

  @Override
  public void refresh() throws Exception {
    mJCNode.refresh();
    return;
  }

  @Override
  public void delete() throws JC_Exception {
    mJCNode.removeNode();
    mJCNode.save();
    return;
  }

}
