/*
 * Copyright 2008-2010 NLR - National Aerospace Laboratory
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

package org.jecars.client;

import java.net.URL;
import org.jecars.client.local.JC_LocalClient;


/**
 * JC_Factory
 *
 * @version $Id: JC_Factory.java,v 1.8 2009/05/06 14:11:13 weertj Exp $
 */
public class JC_Factory {

  /** createClient
   *
   * @param pServerPath
   * @return JC_Clientable
   * @throws org.jecars.client.JC_Exception
   */
  static public JC_Clientable createClient( final String pServerPath ) throws JC_Exception {
    final JC_DefaultClient c;
    if (JC_LocalClient.JECARSLOCAL.equals( pServerPath )) {
      try {
        c = new JC_LocalClient();
      } catch( Exception e ) {
        throw new JC_Exception( e );
      }
    } else {
      c = new JC_DefaultClient();
    }
    c.registerNodeClass( "[root]", "org.jecars.client.JC_RootNode" );
    c.registerNodeClass( "*", "org.jecars.client.JC_DefaultNode" );
    c.registerNodeClass( "jecars:Prefs", "org.jecars.client.nt.JC_PrefsNode" );
    c.registerNodeClass( "jecars:User", "org.jecars.client.nt.JC_UserNode" );
    c.registerNodeClass( "jecars:Users", "org.jecars.client.nt.JC_UsersNode" );
    c.registerNodeClass( "jecars:GroupLevel", "org.jecars.client.nt.JC_GroupNode" );
    c.registerNodeClass( "jecars:Group", "org.jecars.client.nt.JC_GroupNode" );
    c.registerNodeClass( "jecars:Groups", "org.jecars.client.nt.JC_GroupsNode" );
    c.registerNodeClass( "jecars:Permission", "org.jecars.client.nt.JC_PermissionNode" );
    c.registerNodeClass( "jecars:Tool", "org.jecars.client.nt.JC_ToolNode" );
    c.registerNodeClass( "jecars:ToolEvent", "org.jecars.client.nt.JC_ToolEventNode" );
    c.registerNodeClass( "jecars:ToolEventException", "org.jecars.client.nt.JC_ToolEventExceptionNode" );
    c.registerNodeClass( "jecars:parameterdata", "org.jecars.client.nt.JC_ParameterDataNode" );
    c.registerNodeClass( "jecars:Event", "org.jecars.client.nt.JC_EventNode" );
    c.setServerPath( pServerPath );

    c.setDefaultParams( JC_RESTComm.GET,    c.createParams( JC_RESTComm.GET ) );
    c.setDefaultParams( JC_RESTComm.HEAD,   c.createParams( JC_RESTComm.HEAD ) );
    c.setDefaultParams( JC_RESTComm.POST,   c.createParams( JC_RESTComm.POST ) );
    c.setDefaultParams( JC_RESTComm.PUT,    c.createParams( JC_RESTComm.PUT ) );
    c.setDefaultParams( JC_RESTComm.DELETE, c.createParams( JC_RESTComm.DELETE ) );

    return c;
  }
  
  /** createClient
   * 
   * @param pURL
   * @return JC_Clientable
   * @throws org.jecars.client.JC_Exception
   */
  static public JC_Clientable createClient( final URL pURL ) throws JC_Exception {
    return createClient( pURL.toExternalForm() );
  }

  /** createLocalClient
   * 
   * @return
   * @throws JC_Exception
   */
  static public JC_Clientable createLocalClient() throws JC_Exception {
    JC_Clientable client = createClient( JC_LocalClient.JECARSLOCAL );
    return client;
  }

}
