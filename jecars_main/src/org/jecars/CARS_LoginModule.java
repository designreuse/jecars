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
package org.jecars;

import java.util.logging.Logger;
import javax.jcr.Credentials;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.SimpleCredentials;
import javax.security.auth.*;
import javax.security.auth.callback.*;
import javax.security.auth.login.*;
import javax.security.auth.spi.LoginModule;
import java.util.*;
import javax.jcr.Session;
import org.jecars.jaas.CARS_PasswordService;
import org.apache.jackrabbit.core.security.AnonymousPrincipal;
import org.apache.jackrabbit.core.security.SecurityConstants;
import org.apache.jackrabbit.core.security.UserPrincipal;
import org.apache.jackrabbit.core.security.authentication.CredentialsCallback;

/**
 * CARS_LoginModule
 *
 * @version $Id: CARS_LoginModule.java,v 1.10 2009/05/26 09:21:41 weertj Exp $
 */
public class CARS_LoginModule implements LoginModule {

  private static Logger gLog = Logger.getLogger( "org.jecars" );

  private static final String OPT_ANONYMOUS = "anonymousId";
  private static final String OPT_DEFAULT = "defaultUserId";
  private static final String DEFAULT_ANONYMOUS_ID = "anonymous";

  private Subject         mSubject;
  private CallbackHandler mCallbackHandler;
//  private Map             mSharedState;
//  private Map             mOptions;

  private final Set       mPrincipals = new HashSet();
  private String          mAnonymousUserId = DEFAULT_ANONYMOUS_ID;
  private String          mDefaultUserId = null;

  static public boolean gSuperuserAllowed = false;
  
  public CARS_LoginModule() {
  }

  /**
   * Returns the anonymous user id.
   *
   * @return anonymous user id
   */
  public String getAnonymousId() {
    return mAnonymousUserId;
  }

  /**
   * Sets the default user id to be used when no login credentials
   * are presented.
   * 
   * @param pAnonymousId user id
   */
  public void setAnonymousId( String pAnonymousId ) {
    mAnonymousUserId = pAnonymousId;
    return;
  }

  /**
   * Returns the default user id.
   *
   * @return default user id
   */
  public String getDefaultUserId() {
    return mDefaultUserId;
  }

  /**
   * Sets the default user id to be used when no login credentials
   * are presented.
   * 
   * @param pDefaultUserId default user id
   */
  public void setDefaultUserId( String pDefaultUserId ) {
    mDefaultUserId = pDefaultUserId;
    return;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void initialize( Subject pSubject, CallbackHandler pCallbackHandler,
                           Map pSharedState, Map pOptions) {
    mSubject         = pSubject;
    mCallbackHandler = pCallbackHandler;
//    mSharedState     = pSharedState;
//    mOptions         = pOptions;

//    String userId = (String)mOptions.get(OPT_ANONYMOUS);
    String userId = (String)pOptions.get(OPT_ANONYMOUS);
    if (userId != null) {
      mAnonymousUserId = userId;
    }
    if (pOptions.containsKey(OPT_DEFAULT)) {
      mDefaultUserId = (String)pOptions.get(OPT_DEFAULT);
//      mDefaultUserId = (String)mOptions.get(OPT_DEFAULT);
    }
    return;
  }


  /** Get all current active keys of the give username
   * @param pUsername
   * @return Collection of keys (nodes)
   */
//  static protected Collection<Node> getUserKeys( String pUsername ) throws RepositoryException {
//    Collection<Node> col = new ArrayList<Node>();
//    Node authsPath = CARS_Factory.getSystemCarsSession().getRootNode().getNode( CARS_AccessManager.gAccountKeysPath );
//    NodeIterator ni = authsPath.getNodes();
//    Node key;
//    while( ni.hasNext() ) {
//      key = ni.nextNode();
//      if (key.getProperty( CARS_ActionContext.gDefTitle ).getString().equals( pUsername )==true) {
//        col.add( key );
//      }
//    }
//    return col;
//  }

  /** getKeyNode
   * @param pKey
   * @return
   * @throws javax.jcr.RepositoryException
   */
  static protected Node getKeyNode( String pKey ) throws RepositoryException {
    Session s = CARS_Factory.getSystemCarsSession();
//    s.refresh( false );
    return s.getRootNode().getNode( CARS_AccessManager.gAccountKeysPath ).getNode( pKey );
  }

  
    /**
     * {@inheritDoc}
     */
   @Override
   synchronized public boolean login() throws LoginException {
      if (mCallbackHandler == null) {
        throw new LoginException( "No CallbackHandler available" );
      }
      boolean authenticated = false;
      mPrincipals.clear();
      try {
        // **** Get credentials using a JAAS callback
        final CredentialsCallback ccb = new CredentialsCallback();
        mCallbackHandler.handle(new Callback[] { ccb });
        final Credentials creds = ccb.getCredentials();
        if (creds != null) {
           if (creds instanceof SimpleCredentials) {
              UserPrincipal userP = null;
              final SimpleCredentials sc = (SimpleCredentials) creds;
              String userName = CARS_Utils.decode(sc.getUserID());
              final Object attr = sc.getAttribute(SecurityConstants.IMPERSONATOR_ATTRIBUTE);
              if (attr != null && attr instanceof Subject) {
//                Subject impersonator = (Subject) attr;
              } else {
                // **** Check username password
//                if (CARS_Factory.gSystemCarsSession!=null) {
                  // **** Superuser isn't allowed for remote login
                if (!userName.equals( CARS_AccessManager.gSuperuserName )) {
                  Session loginSession = CARS_Factory.getSystemLoginSession();
                  if (loginSession!=null) {
                    synchronized( loginSession ) {
//                      loginSession.refresh( false );
                      if (userName.startsWith( "AUTHKEY_" )) {
//                      System.out.println( " CHECK KEY = " + userName );
                        final String authKey = userName;
                        final Node keyNode = getKeyNode( authKey );
                        userName = keyNode.getProperty( CARS_ActionContext.gDefTitle ).getString();
                        final Node users = CARS_Factory.getSystemLoginSession().getRootNode().getNode( CARS_AccessManager.gUsersPath );
                        if (users.hasNode( userName )) {
                          final Node user = users.getNode( userName );
                          if (user.hasProperty( CARS_AccessManager.gPasswordProperty )) {
                            // **** The username is a AUTHKEY
                            userP = new UserPrincipal( userName );
                            authenticated = true;
                          }                        
                        }
                      } else {
                        // **** Normal username/password
                        final Node users = CARS_Factory.getSystemLoginSession().getRootNode().getNode( CARS_AccessManager.gUsersPath );
                        if (users.hasNode( userName )) {
                          final Node user = users.getNode( userName );
                          if ((user.hasProperty( "jecars:Suspended" )) && (user.getProperty( "jecars:Suspended").getBoolean())) {
                            // **** The user is suspended
                            throw new AccountLockedException( userName + " is suspended" );
                          }
//                          if (userName.equals( CARS_AccessManager.gUSERNAME_GRANTALL )==true) {
//                            authenticated = true;
//                          } else if (user.hasProperty( CARS_AccessManager.gPasswordProperty )) {
                          if (user.hasProperty( CARS_AccessManager.gPasswordProperty )) {
                            if (user.getProperty( CARS_AccessManager.gPasswordProperty ).getString().equals(
                              CARS_PasswordService.getInstance().encrypt(new String(sc.getPassword()) ) )) {
                              authenticated = true;
                            }
                          }
                        }
                      }
                    }
                  }
                } else {
                  // **** Is Superuser
//                  authenticated = true;
                  authenticated = gSuperuserAllowed;
                }
              }
              if (mAnonymousUserId.equals(userName)) {
                mPrincipals.add(new AnonymousPrincipal());
              } else {
                if (userP==null) {
                  mPrincipals.add(new UserPrincipal(userName));
                } else {
                  mPrincipals.add( userP );
                }
              }
//              authenticated = true;
            }
          } else if (mDefaultUserId==null) {
            mPrincipals.add(new AnonymousPrincipal());
            authenticated = true;
          } else {
            mPrincipals.add(new UserPrincipal(mDefaultUserId));
            authenticated = true;
          }
        } catch (RepositoryException re) {
            throw new LoginException(re.getMessage());
        } catch (java.io.IOException ioe) {
            throw new LoginException(ioe.toString());
        } catch (UnsupportedCallbackException uce) {
            throw new LoginException(uce.getCallback().toString() + " not available");
        } catch (AccountLockedException ale) {
          throw ale;
        } catch (Exception ne) {
            throw new LoginException(ne.getMessage());
        }

        if (authenticated) {
          return !mPrincipals.isEmpty();
        } else {
          mPrincipals.clear();
          throw new FailedLoginException();
        }
    }

  /**
   * {@inheritDoc}
   */
   @Override
  public boolean commit() throws LoginException {
    if (mPrincipals.isEmpty()) {
      return false;
    } else {
      // **** add a principals (authenticated identities) to the Subject
      mSubject.getPrincipals().addAll( mPrincipals );
      return true;
    }
  }

    /**
     * {@inheritDoc}
     */
  @Override
  public boolean abort() throws LoginException {
    if (mPrincipals.isEmpty()) {
      return false;
    } else {
      logout();
    }
    return true;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean logout() throws LoginException {
    mSubject.getPrincipals().removeAll(mPrincipals);
    mPrincipals.clear();
    return true;
  }

}
