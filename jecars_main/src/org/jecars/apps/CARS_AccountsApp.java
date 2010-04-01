/*
 * Copyright 2007-2010 NLR - National Aerospace Laboratory
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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.Calendar;
import java.util.UUID;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import nl.msd.jdots.JD_Taglist;
import org.jecars.CARS_AccessManager;
import org.jecars.CARS_ActionContext;
import org.jecars.CARS_CustomException;
import org.jecars.CARS_Factory;
import org.jecars.CARS_Main;
import org.jecars.CARS_Utils;
import org.jecars.jaas.CARS_PasswordService;
import org.jecars.support.BASE64Encoder;
import org.jecars.support.Base64;

/**
 * CARS_AccountsApp
 *
 * @version $Id: CARS_AccountsApp.java,v 1.7 2009/06/17 07:31:53 weertj Exp $
 */
public class CARS_AccountsApp extends CARS_DefaultInterface {

  static final public String AUTHKEY_PREFIX = "AUTHKEY_";

  static private long gKeyValidInHours = 1;
    
  /** Creates a new instance of CARS_AccountsApp
   */
  public CARS_AccountsApp() {
    super();
    return;
  }

  /** getVersion
   * 
   * @return
   */
  @Override
  public String getVersion() {
    return getClass().getName() + ": JeCARS version=" + CARS_Main.VERSION_ID + " $Id: CARS_AccountsApp.java,v 1.7 2009/06/17 07:31:53 weertj Exp $";
  }

  /** setKeyValidInHours
   * 
   * @param pValid
   */
  static public void setKeyValidInHours( final long pValid ) {
    gKeyValidInHours = pValid;
    return;
  }

  /** getNodes
   * @param pMain
   * @param pInterfaceNode
   * @param pParentNode
   * @param pLeaf
   * @throws java.lang.Exception
   */
  @Override
  public void getNodes( final CARS_Main pMain, final Node pInterfaceNode, final Node pParentNode, final String pLeaf ) throws Exception {
//    System.out.println( "Must put the nodes under: " + pParentNode.getPath() );
//    System.out.println( "The leaf is (fullpath): " + pLeaf );
    
    // **** sys* nodes have all rights. TODO
    final Session appSession = CARS_Factory.getSystemApplicationSession();
    synchronized( appSession ) {
      final Node sysParentNode = appSession.getRootNode().getNode( pParentNode.getPath().substring(1) );
      if (sysParentNode.isNodeType( "jecars:CARS_Interface" )) {
        // **** Hey!.... it the root....
        if (!sysParentNode.hasNode( "login")) {
          sysParentNode.addNode( "login", "jecars:root" );        
        }
      }
      sysParentNode.save();
    }
    return;
  }

  /** createAuthKey
   * @param pUsername
   * @param pEncryptPwd
   * @param pService
   * @return
   * @throws java.lang.Exception
   */
  static final public String createAuthKey( String pUsername, String pEncryptPwd, String pService, Calendar pExpireDate ) throws Exception {
    final CARS_PasswordService ps = CARS_PasswordService.getInstance();
    final UUID uuid = UUID.randomUUID();
    final String authKeyS = BASE64Encoder.encodeBuffer(
            ps.encrypt(pUsername + "$!$" + pEncryptPwd + pService + "!$!" + uuid.toString() ).getBytes(),
            Base64.DONT_BREAK_LINES );
//    String authKeyS = CARS_Utils.encode(ps.encrypt( pUsername + "$!$" + pEncryptPwd + pService + "!$!" + pExpireDate.getTimeInMillis() ));
    return authKeyS;
  }

  /** getAuthKeyFromNode
   * 
   * @param pNode
   * @return
   * @throws javax.jcr.RepositoryException
   */
  static final public String getAuthKeyFromNode( final Node pNode ) throws RepositoryException {
    String auth = pNode.getName();
    if (auth.startsWith( AUTHKEY_PREFIX )) {
      auth = auth.substring( AUTHKEY_PREFIX.length() );
    } else {
      throw new RepositoryException( "Node " + pNode.getPath() + " is not a authkey node" );
    }
    return auth;
  }
  
  
  /** addNode
   * @param pMain
   * @param pInterfaceNode
   * @param pParentNode
   * @param pName
   * @param pPrimType
   * @param pParams
   * @return
   * @throws java.lang.Exception
   */
  @Override
  synchronized public Node addNode( CARS_Main pMain, Node pInterfaceNode, Node pParentNode, String pName, String pPrimType, JD_Taglist pParams ) throws Exception {
    Node newNode = null;
    
    // **** sys* nodes have all rights. TODO
    final Session appSession = CARS_Factory.getSystemApplicationSession();
    synchronized( appSession ) {
      final Node sysParentNode = appSession.getRootNode().getNode( pParentNode.getPath().substring(1) );
      if (pName.equals( "ClientLogin" )) {

        final CARS_ActionContext ac = pMain.getContext();

        // **** ClientLogin
        if (!sysParentNode.hasNode( pName )) {
          newNode = sysParentNode.addNode( pName, "jecars:unstructured" );
          newNode.addMixin( "jecars:permissionable" );
          final String[] prin = {"/JeCARS/default/Groups/DefaultReadGroup"};
          newNode.setProperty( "jecars:Principal", prin );
          final String[] acts = {"read","get_property"};
          newNode.setProperty( "jecars:Actions", acts );
          sysParentNode.save();
//          newNode.setProperty( "jecars:KeyValidForHours", gKeyValidInHours );
        } else {
          newNode = sysParentNode.getNode( pName );          
        }

        final String email   = (String)pParams.getData( "jecars:Email" );
        final String pwd     = (String)pParams.getData( "jecars:Passwd" );
        final String service = "JeCARS";
        if ((email!=null) && (pwd!=null)) {
          // **** Check the user account        
          Node user = CARS_Factory.getSystemApplicationSession().getRootNode().getNode( CARS_AccessManager.gUsersPath );
          if (user.hasNode( email )) {
            user = user.getNode( email );
            final CARS_PasswordService ps = CARS_PasswordService.getInstance();
            if (user.hasProperty( CARS_AccessManager.gPasswordProperty )) {
              if (user.getProperty( CARS_AccessManager.gPasswordProperty ).getString().equals( ps.encrypt(pwd) ) ) {
                // **** Is ok
//                newNode.setProperty( "jecars:Email",        email );
//                newNode.setProperty( "jecars:Passwd",       pwd );
//                newNode.setProperty( "jecars:source",       (String)pParams.getData( "jecars:source" ) );
//                newNode.setProperty( "jecars:service",      (String)pParams.getData( "jecars:service" ) );
//                newNode.setProperty( "jecars:accountType",  (String)pParams.getData( "jecars:accountType" ) );
                pParams.clear();
                final StringBuilder result = new StringBuilder();
//                String authKeyS = CARS_Utils.encode(ps.encrypt( email + "$!$" + ps.encrypt( pwd ) + service ));
                boolean clash = true;
                int i = 0;
                while( clash ) {
                  Calendar c = Calendar.getInstance();
                  c.add( Calendar.HOUR_OF_DAY, (int)gKeyValidInHours );
                  final String authKeyS = createAuthKey( email, ps.encrypt( pwd ), service, c );
                  result.append( "Auth=" ).append( authKeyS ).append( "\n" );
                  final String nodeAuthKey = AUTHKEY_PREFIX + authKeyS;
                  Node authKey;
                  if (!newNode.hasNode( nodeAuthKey )) {
                    authKey = newNode.addNode( nodeAuthKey, "jecars:root" );
                    authKey.setProperty( CARS_ActionContext.gDefTitle, email );
                    authKey.setProperty( CARS_ActionContext.gDefExpireDate, c );
                    CARS_Utils.setCurrentModificationDate( newNode );
                    newNode = authKey;
                    break;
                  } else {
                    i++;
                    if (i>20) {
                      ac.setContentsResultStream( new ByteArrayInputStream( "Error=ServiceUnavailable\n".getBytes() ), "text/plain" );
                      ac.setErrorCode( HttpURLConnection.HTTP_FORBIDDEN );
                      throw new CARS_CustomException( "Key clash" );
                    }
                  }
                }
  //              authKey = newNode.getNode( nodeAuthKey );
                ac.setContentsResultStream( new ByteArrayInputStream( result.toString().getBytes() ), "text/plain" );
              } else {
                authError( ac );
              }
            } else {
              authError( ac );
            }
          } else {
            authError( ac );
          }
        } else {
          authError( ac );
        }
      }
      sysParentNode.save();
    }    
    return newNode;
  }

  /** _authError
   * 
   * @param pActionContext
   * @throws org.jecars.CARS_CustomException
   */
  private final void authError( final CARS_ActionContext pActionContext ) throws CARS_CustomException {
    pActionContext.setContentsResultStream( new ByteArrayInputStream( "Error=BadAuthentication\n".getBytes() ), "text/plain" );
    pActionContext.setErrorCode( HttpURLConnection.HTTP_FORBIDDEN );
    throw new CARS_CustomException( "Auth error" );
  }

  /** nodeAdded
   * @param pMain
   * @param pInterfaceNode
   * @param pNewNode
   * @param pBody
   * @throws java.lang.Exception
   */
  @Override   
  public void nodeAdded( final CARS_Main pMain, final Node pInterfaceNode, final Node pNewNode, final InputStream pBody )  throws Exception {
    super.nodeAdded( pMain, pInterfaceNode, pNewNode, pBody);
    pMain.getContext().setErrorCode( HttpURLConnection.HTTP_OK );
    return;
  }

  
    
}
