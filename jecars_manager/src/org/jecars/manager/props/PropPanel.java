/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jecars.manager.props;

import javax.swing.JPanel;
import org.jecars.manager.jcrProperty;

/**
 *
 * @author weert
 */
public class PropPanel extends JPanel {

  private final jcrProperty mProp;

  public PropPanel( jcrProperty pProp ) throws Exception {
    mProp = pProp;
    return;
  }

  protected jcrProperty getProp() {
    return mProp;
  }

  public void writeProp( final String pValue ) throws Exception {
    mProp.setValue( pValue );
    mProp.save();
    return;
  }

}
