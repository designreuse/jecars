/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jecars.wb;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.geom.Dimension2D;
import javax.swing.UIManager;
import org.jecars.client.JC_Clientable;
import org.jecars.client.scripts.JCS_defaultScript;

/**
 *
 * @author weert
 */
public class startUserFrame extends JCS_defaultScript {

  /** start
   *
   * @throws Exception
   */
  public void start() throws Exception {
    UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
    final JC_Clientable client = getClient();
    final WB_UserFrame uf = new WB_UserFrame();
    if (uf.setDefaultScript( this )) {
      final Toolkit tk = Toolkit.getDefaultToolkit();
      final Dimension screenSize = tk.getScreenSize();
      final int screenHeight = screenSize.height;
      final int screenWidth = screenSize.width;
      uf.setLocation(screenWidth / 4, screenHeight / 4);
      uf.setVisible( true );
    }
    return;
  }

  /**
   * @param args the command line arguments
   */
  public static void main( final String[] pArgs ) {
    try {
      final startUserFrame suf = new startUserFrame();
      suf.parseArguments( pArgs );
      suf.start();
    } catch( Exception e ) {
      e.printStackTrace();
    }
    return;
  }

}
