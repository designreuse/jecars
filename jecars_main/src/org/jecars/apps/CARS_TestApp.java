/*
 * Copyright 2007 NLR - National Aerospace Laboratory
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jecars.apps;

import java.util.Calendar;
import javax.jcr.Node;
import org.jecars.CARS_Main;

/**
 * CARS_TestApp
 *
 * @version $Id: CARS_TestApp.java,v 1.2 2008/09/26 13:37:12 weertj Exp $
 */
public class CARS_TestApp extends CARS_DefaultInterface implements CARS_Interface {
    
  /** Creates a new instance of CARS_TestApp */
  public CARS_TestApp() {
  }
  
  /** getVersion
   * 
   * @return
   */
  @Override
  public String getVersion() {
    return getClass().getName() + ": JeCARS version=" + CARS_Main.VERSION_ID + " $Id: CARS_TestApp.java,v 1.2 2008/09/26 13:37:12 weertj Exp $";
  }

  
  public void getNodes( CARS_Main pMain, Node pInterfaceNode, Node pParentNode, String pLeaf ) throws Exception {
    System.out.println( "Must put the nodes under: " + pParentNode.getPath() );
    System.out.println( "The leaf is (fullpath): " + pLeaf );
    
    if (pLeaf.equals( "/testApp" )) {
      // **** Hey!.... it the root....
      if (pParentNode.hasNode( "test1" )==false) {
        pParentNode.addNode( "test1", "nt:unstructured" );
      }
      if (pParentNode.hasNode( "thisSystem" )==false) {
        pParentNode.addNode( "thisSystem", "nt:unstructured" );
      }
    }
    
    if (pLeaf.equals( "/testApp/thisSystem" )) {
      // **** Recreate the properties.
      if (pParentNode.hasProperty( "os.version" )==false) {
        pParentNode.setProperty( "os.version", System.getProperty( "os.version" ) );
      }
      pParentNode.setProperty( "currentTime", Calendar.getInstance() );
    }
    pParentNode.save();
    return;
  }

  
}
