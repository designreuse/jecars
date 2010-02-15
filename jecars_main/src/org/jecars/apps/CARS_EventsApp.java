/*
 * Copyright 2010 NLR - National Aerospace Laboratory
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

import java.io.UnsupportedEncodingException;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.ConstraintViolationException;
import nl.msd.jdots.JD_Taglist;
import org.jecars.CARS_ActionContext;
import org.jecars.CARS_EventManager;
import org.jecars.CARS_Factory;
import org.jecars.CARS_Main;
import org.jecars.CARS_Utils;

/**
 * CARS_EventsApp

 */
public class CARS_EventsApp extends CARS_DefaultInterface {
    
  /** Creates a new instance of CARS_AdminApp
   */
  public CARS_EventsApp() {
    super();
  }
 
  
  /** getVersion
   * 
   * @return
   */
  @Override
  public String getVersion() {
    return getClass().getName() + ": " + CARS_Main.PRODUCTNAME + " version=" + CARS_Main.VERSION_ID + " CARS_EventsApp";
  }

  /** Add a node to the repository
   * @param pMain the CARS_Main object
   * @param pInterfaceNode the Node which defines the application source or NULL
   * @param pParentNode the node under which the object must be added
   * @param pName the node name
   * @param pPrimType the node type
   * @param pParams list of parameters
   */
  @Override
  public Node addNode( final CARS_Main pMain, final Node pInterfaceNode, final Node pParentNode, final String pName, final String pPrimType, final JD_Taglist pParams ) throws RepositoryException, UnsupportedEncodingException {
    final Node realEventNode;
    if ("jecars:Event".equals( pPrimType )) {
//      final String eventPath = pMain.getContext().getParameterStringFromMap( "X-EventPath" );
      JD_Taglist paramsTL = pMain.getContext().getQueryPartsAsTaglist();
      paramsTL = pMain.getContext().getParameterMapAsTaglist( paramsTL );
      final String eventPath = (String)paramsTL.getData( "jecars:X-EventPath" );
// paramsTL.print();
      if (eventPath==null) {
        throw new ConstraintViolationException( "X-EventPath not given" );
      } else {
        final Session appSession = CARS_Factory.getSystemApplicationSession();
        final CARS_ActionContext ac = pMain.getContext();
        final CARS_EventManager eventManager = CARS_Factory.getEventManager();
        synchronized( appSession ) {
//          if (!pParentNode.hasNode( "events" )) {
//            final Node pn = appSession.getRootNode().getNode( pParentNode.getPath().substring(1) );
//            pn.addNode( "events", "jecars:EventsFolder" );
//            CARS_Utils.setCurrentModificationDate( pn );
//            pn.save();
//          }
          final Node eventPathFolder   = appSession.getRootNode().getNode( eventPath.substring(1) );
          final Node loginUserAsSystem = appSession.getRootNode().getNode( pMain.getLoginUser().getPath().substring(1) );
          // **** An event object will be created
          realEventNode = eventManager.addEvent( eventPathFolder, ac.getMain(),
                      loginUserAsSystem, pParentNode,
//                      pMain.getLoginUser(), pParentNode,
                      eventPath,
                      (String)paramsTL.getData( "jecars:Category" ),
                      (String)paramsTL.getData( "jecars:Type" ),
                      (String)paramsTL.getData( "jecars:Title" ),
                      pPrimType,
                      (String)paramsTL.getData( "jecars:Body" ) );
          appSession.save();
        }
  //      final Node eventFolder = pParentNode.getNode( "events" );
//        eventNode = eventManager.addEvent( pParentNode, ac.getMain(), pMain.getLoginUser(), pParentNode,
//                      "events",
//                      "APP", "READ", "", pPrimType, "" );
//        CARS_Utils.setExpireDate( eventNode, 5 );
        pParentNode.save();
      }
    } else {
      throw new ConstraintViolationException( "Nodetype " + pPrimType + " isn't allowed\n" +
              "Parameters;\n" +
              "(Mandatory) jcr:primaryType=jecars:Event\n" +
              "(Mandatory) X-EventPath={the event path where the event will be created}\n"
              );
    }
    return realEventNode;
  }


  
    
}
