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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
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
import org.jecars.client.JC_Clientable;
import org.jecars.client.JC_Exception;
import org.jecars.client.JC_Factory;
import org.jecars.client.JC_GDataAuth;
import org.jecars.client.JC_InfoApp;
import org.jecars.jaas.CARS_PasswordService;
import org.jecars.support.BASE64Encoder;
import org.jecars.support.Base64;

/**
 * CARS_AccountsApp
 *
 * @version $Id: CARS_AccountsApp.java,v 1.7 2009/06/17 07:31:53 weertj Exp $
 */
public class CARS_AccountsApp extends CARS_DefaultInterface {

  static final public Logger LOG = Logger.getLogger( "org.jecars.apps" );

  static final public String AUTHKEY_PREFIX = "AUTHKEY_";

  static private long gKeyValidInHours = 1;

  static private File               gCIRCLE_OF_TRUST = null;
  static private Properties         propCIRCLE_OF_TRUST = new Properties();

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

  /** setCircleOfTrustFile
   * 
   * @param pCotf
   */
  static public void setCircleOfTrustFile( final File pCotf ) throws FileNotFoundException, IOException {
    gCIRCLE_OF_TRUST = pCotf;
    propCIRCLE_OF_TRUST.clear();
    if (pCotf!=null) {
      final FileInputStream fis = new FileInputStream( pCotf );
      try {
        propCIRCLE_OF_TRUST.load( fis );
      } finally {
        fis.close();
      }
    }
    return;
  }

  /** getTrustedServers
   *
   * @return
   */
  static public List<String> getTrustedServers() {
    if (propCIRCLE_OF_TRUST==null) {
      return Collections.EMPTY_LIST;
    } else {
      final List<String> l = new ArrayList<String>();
      final Enumeration<Object> keys = propCIRCLE_OF_TRUST.keys();
      while( keys.hasMoreElements() ) {
        final String key = keys.nextElement().toString();
        if (key.startsWith( "JeCARS.Server" )) {
          l.add( propCIRCLE_OF_TRUST.getProperty( key ) );
        }
      }
      return l;
    }
  }

  /** checkCircleOfTrust
   *
   * @param pAuth
   * @return
   */
  static public String checkCircleOfTrust( final String pAuth ) throws RepositoryException {
    String un = null;
    final List<String> tss = getTrustedServers();
    for( final String ts : tss ) {
      try {
        final JC_Clientable client = JC_Factory.createClient( ts );
        client.setCredentials( JC_GDataAuth.create( pAuth ));
        final JC_InfoApp info = new JC_InfoApp( client );
        un = info.whoAmI();
        if (un!=null) {

          // **** Copy the key to the current jecars
          final Session appSession = CARS_Factory.getSystemApplicationSession();
          synchronized( appSession ) {
            final Node clientLogin = appSession.getNode( "/accounts/ClientLogin" );
            final String nodeAuthKey = AUTHKEY_PREFIX + pAuth;
            if (!clientLogin.hasNode( nodeAuthKey )) {
              clientLogin.addNode( nodeAuthKey, "jecars:root" );
            }
            final Node authKey = clientLogin.getNode( nodeAuthKey );
            final Calendar c = Calendar.getInstance();
            c.add( Calendar.MINUTE, 15 );
            authKey.setProperty( CARS_ActionContext.gDefTitle, un );
            authKey.setProperty( CARS_ActionContext.gDefExpireDate, c );
            authKey.setProperty( CARS_ActionContext.gDefBody, "Key is copied from " + client.getServerPath() );
            CARS_Utils.setCurrentModificationDate( authKey.getParent() );
            appSession.save();
          }

          break;
        }
      } catch(JC_Exception je) {
        LOG.log( Level.WARNING, je.getMessage(), je );
      }
    }
    return un;
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
    
    // **** sys* nodes have all rights.
    final Session appSession = CARS_Factory.getSystemApplicationSession();
    synchronized( appSession ) {
      final Node sysParentNode = appSession.getRootNode().getNode( pParentNode.getPath().substring(1) );
      if (sysParentNode.isNodeType( "jecars:CARS_Interface" )) {
        if (!sysParentNode.hasNode( "login")) {
          sysParentNode.addNode( "login", "jecars:root" );        
        }
      }
      sysParentNode.save();
    }
    return;
  }

  /** createAuthKey
   *
   * @param pUsername
   * @param pEncryptPwd
   * @param pService
   * @param pExpireDate
   * @return
   * @throws NoSuchAlgorithmException
   * @throws UnsupportedEncodingException
   */
  static final public String createAuthKey( final String pUsername, final String pEncryptPwd, final String pService, final Calendar pExpireDate ) throws NoSuchAlgorithmException, UnsupportedEncodingException {
    final CARS_PasswordService ps = CARS_PasswordService.getInstance();
    final UUID uuid = UUID.randomUUID();
    final String authKeyS = BASE64Encoder.encodeBuffer(
            ps.encrypt(pUsername + "$!$" + pEncryptPwd + pService + "!$!" + uuid.toString() ).getBytes(),
            Base64.DONT_BREAK_LINES );
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
      throw new RepositoryException( "Node " + pNode.getPath() + " is not an authkey node" );
    }
    return auth;
  }
  
  
  /** addNode
   *
   * @param pMain
   * @param pInterfaceNode
   * @param pParentNode
   * @param pName
   * @param pPrimType
   * @param pParams
   * @return
   * @throws RepositoryException
   * @throws NoSuchAlgorithmException
   * @throws UnsupportedEncodingException
   * @throws CARS_CustomException
   */
  @Override
  synchronized public Node addNode( final CARS_Main pMain, final Node pInterfaceNode, final Node pParentNode,
                                    final String pName, final String pPrimType, final JD_Taglist pParams ) throws RepositoryException, NoSuchAlgorithmException, UnsupportedEncodingException, CARS_CustomException {
    Node newNode = null;
    
    // **** sys* nodes have all rights.
    final Session appSession = CARS_Factory.getSystemApplicationSession();
    synchronized( appSession ) {
      final Node sysParentNode = appSession.getNode( pParentNode.getPath() );
      if ("ClientLogin".equals( pName )) {

        final CARS_ActionContext ac = pMain.getContext();

        // **** ClientLogin
        if (sysParentNode.hasNode( pName )) {
          newNode = sysParentNode.getNode( pName );
        } else {
          newNode = sysParentNode.addNode( pName, "jecars:unstructured" );
          newNode.addMixin( "jecars:permissionable" );
          final String[] prin = {"/JeCARS/default/Groups/DefaultReadGroup"};
          newNode.setProperty( "jecars:Principal", prin );
          final String[] acts = {"read","get_property"};
          newNode.setProperty( "jecars:Actions", acts );
          sysParentNode.save();
//          newNode.setProperty( "jecars:KeyValidForHours", gKeyValidInHours );
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
                final boolean clash = true;
                int i = 0;
                while( clash ) {
                  final Calendar c = Calendar.getInstance();
                  c.add( Calendar.HOUR_OF_DAY, (int)gKeyValidInHours );
                  final String authKeyS = createAuthKey( email, ps.encrypt( pwd ), service, c );
                  result.append( "Auth=" ).append( authKeyS ).append( "\n" );
                  final String nodeAuthKey = AUTHKEY_PREFIX + authKeyS;
                  Node authKey;
                  if (newNode.hasNode( nodeAuthKey )) {
                    i++;
                    if (i>20) {
                      ac.setContentsResultStream( new ByteArrayInputStream( "Error=ServiceUnavailable\n".getBytes() ), "text/plain" );
                      ac.setErrorCode( HttpURLConnection.HTTP_FORBIDDEN );
                      throw new CARS_CustomException( "Key clash" );
                    }
                  } else {
                    authKey = newNode.addNode( nodeAuthKey, "jecars:root" );
                    authKey.setProperty( CARS_ActionContext.gDefTitle, email );
                    authKey.setProperty( CARS_ActionContext.gDefExpireDate, c );
                    CARS_Utils.setCurrentModificationDate( newNode );
                    newNode = authKey;
                    break;
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
