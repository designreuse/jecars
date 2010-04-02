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

package org.jecars;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
//import org.apache.jackrabbit.core.id.ItemId;
import org.apache.jackrabbit.core.security.*;

import javax.jcr.AccessDeniedException;
import javax.jcr.ItemNotFoundException;
import javax.jcr.LoginException;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.security.auth.Subject;
import org.apache.jackrabbit.core.HierarchyManager;
import org.apache.jackrabbit.core.ItemImpl;
import org.apache.jackrabbit.core.ItemManager;
import org.apache.jackrabbit.core.NodeImpl;
import org.apache.jackrabbit.core.SessionImpl;
import org.apache.jackrabbit.core.security.authorization.AccessControlProvider;
import org.apache.jackrabbit.core.security.authorization.Permission;
import org.apache.jackrabbit.core.security.authorization.WorkspaceAccessManager;
import org.apache.jackrabbit.spi.Path;

/**
 * CARS_AccessManager
 *
 * 
 */
public class CARS_AccessManager extends DefaultAccessManager implements AccessManager {
    
    static final private Logger LOG = Logger.getLogger( "org.jecars" );

    static public final String P_GETPROPERTY = "get_property";
    static public final String P_SETPROPERTY = "set_property";
    static public final String P_READ        = "read";
    static public final String P_ADDNODE     = "add_node";
    static public final String P_REMOVE      = "remove";
    
    static public final String gAccountKeysPath  = "accounts/ClientLogin";
    static public final String gUsersPath        = CARS_Main.MAINFOLDER + "/default/Users";
    static public final String gGroupsPath       = CARS_Main.MAINFOLDER + "/default/Groups";
    static public final String gPasswordProperty = CARS_Main.DEFAULTNS + "Password_crypt";
    static public final String gSuperuserName    = "Superuser";
    static public final String gActions          = CARS_Main.DEFAULTNS + "Actions";
    static public final String gPrincipal        = CARS_Main.DEFAULTNS + "Principal";
    static public final String gPrincipalSQL     = " OR " + gPrincipal + "='";
    static public final String gDelegate         = CARS_Main.DEFAULTNS + "Delegate";
    static public final String gSelectGroups     = "SELECT * FROM jecars:groupable WHERE jecars:GroupMembers='";
//    static public final String gSelectPermission = "SELECT * FROM jecars:permissionable WHERE (jecars:Principal='";
    static public final String gSelectPermission = "SELECT * FROM jecars:Permission WHERE (jecars:Principal='";

    static public final String gUSERNAME_GRANTALL = "Administrator";

    final static public Object  EXCLUSIVE_CONTROL = new Object();
    /** When RUNNING_IN_EXCLUSIVE_MODE is true the cache isn't cleared. The "setter" of this
     *  mode MUST have sychronized on EXCLUSIVE_CONTROL
     */
//    static public volatile boolean RUNNING_IN_EXCLUSIVE_MODE = false;
    
    static private final Node[] _NODE = new Node[0];

    static private final int X_NOTCLEARCACHE = 1024;

    /**
     * Subject whose access rights this AccessManager should reflect
     */
//    protected Subject mSubject;
//    protected Node    mLoggedInUser = null;
    transient protected String  mLoggedInUsername = null;
    transient protected String  mLoggedInPath = null;

    /**
     * hierarchy manager used for ACL-based access control model
     */
    private HierarchyManager mHierMgr;

    private boolean initialized;

    protected boolean system;
    protected boolean anonymous;

    private boolean mInTheManager = false;
    
    // ***** CACHING members
    transient static Node[]                    mLastAllRightGivers     = null;
    transient static private String            mLastAllRightGiversUUID = null;
    transient static private HashSet<String>   mReadPathCache          = new HashSet<String>();
    transient static private HashSet<String>   mDenyReadPathCache      = new HashSet<String>();
    transient static private HashSet<String>   mWritePathCache         = new HashSet<String>();
    transient private QueryManager      mQueryManager           = null;
    transient private String            mLastPermDeleg_Select   = null;
    transient private QueryResult       mLastPermDeleg_Result   = null;
    
    transient private SessionImpl      mSASession = null;
    transient private ItemManager      mItemManager = null;
//    transient private NamePathResolver mResolver = null;
    
    /**
     * Empty constructor
     */
    public CARS_AccessManager() {
      super();
      initialized = false;
      anonymous = false;
      system = false;
      return;
    }

  static private class BooleanHolder {
    private boolean mValue = false;
  }


 /** setSession
   * 
   * @param pSession
   */
  public void setSession( SessionImpl pSession ) throws RepositoryException {
    mSASession = pSession;
    if (mSASession!=null) {
      mQueryManager = mSASession.getWorkspace().getQueryManager();
    }
    return;
  }
  
  /** getQueryManager
   * 
   * @return
   * @throws javax.jcr.RepositoryException
   */
  final private QueryManager getQueryManager() throws RepositoryException {
    if (mQueryManager==null) {
      if (mSASession!=null) {
        mQueryManager = mSASession.getWorkspace().getQueryManager();
      }
    }
    return mQueryManager;
  }
   
  /** getSession
   * 
   * @return
   * @throws javax.jcr.LoginException
   * @throws javax.jcr.RepositoryException
   */
  final public SessionImpl getSession() throws LoginException, RepositoryException {
    return mSASession;
  }    
    
  @Override
  public void init( AMContext context, AccessControlProvider acProvider,
                    WorkspaceAccessManager wspAccessMgr) throws AccessDeniedException, Exception {
    init( context );
    return;
  }


    //--------------------------------------------------------< AccessManager >
    /**
     * {@inheritDoc}
     */
    @Override
    public void init( final AMContext context)
            throws AccessDeniedException, Exception {
      if (initialized) {
        throw new IllegalStateException("already initialized");
      }
//      mResolver = context.getNamePathResolver();
      Subject subject = context.getSubject();
      SessionImpl xses = (SessionImpl)context.getSession();
      final String userId = xses.getUserID();
      mSASession = (SessionImpl)CARS_Factory.getSystemAccessSession();
      Session ses = mSASession;
      if (ses!=null) {
        synchronized( ses ) {
          try {
            mItemManager = mSASession.getItemManager();
            if (!gSuperuserName.equals( userId )) {
              Node n = ses.getRootNode().getNode( gUsersPath );
              if (n.hasNode( userId )) {
                Node user = n.getNode( userId );
                mLoggedInUsername = userId;
                mLoggedInPath     = user.getPath();
              } else {
                throw new AccessDeniedException( "Access denied for user: " + userId );
              }
            }
          } catch (NullPointerException ne) {
            throw new AccessDeniedException(ne);
          }
        }
      }
      mHierMgr = context.getHierarchyManager();
      anonymous = !subject.getPrincipals(AnonymousPrincipal.class).isEmpty();
      system = !subject.getPrincipals(SystemPrincipal.class).isEmpty();
      // @todo check permission to access given workspace based on principals
      initialized = true;
      return;
    }

    /** checkInitialized
     * 
     */
    @Override
    protected void checkInitialized() {
      if (initialized==false) {
        throw new IllegalStateException( "not initialized" );
      }
    }



    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void close() throws Exception {
      if (!initialized) {
        throw new IllegalStateException("not initialized");
      }
      initialized = false;
      return;
    }

    /** gClearCache
     * 
     */
    static public void gClearCache() {
      mLastAllRightGiversUUID = null;
      mLastAllRightGivers = null;
      clearPathCache();
      return;
    }
    
    /** clearCache
     */
    public void clearCache() {
//      if (RUNNING_IN_EXCLUSIVE_MODE==false) {
        mLastAllRightGiversUUID = null;
        mLastAllRightGivers = null;
//      mLastPermQuery = null;
//        mReadPathCache.clear();
//        mDenyReadPathCache.clear();
        clearPathCache();
        mLastPermDeleg_Select = null;
        mLastPermDeleg_Result = null;
//      }
      return;
    }
    
    /** clearPathCache
     * @param pPrefix
     */
    static public void clearPathCache( final String pPrefix ) {
      synchronized( EXCLUSIVE_CONTROL ) {
        Iterator<String> it = mReadPathCache.iterator();
        String p, cp = pPrefix.substring(pPrefix.indexOf('/'));
        while( it.hasNext() ) {
          p = it.next();
          if (p.substring(p.indexOf('/')).startsWith( cp )) {
//            System.out.println( "---- REMOVE mReadPathCache " + p );
            it.remove();
          }
        }
        it = mDenyReadPathCache.iterator();
        while( it.hasNext() ) {
          p = it.next();
          if (p.substring(p.indexOf('/')).startsWith( cp )) {
            it.remove();
          }
        }
        it = mWritePathCache.iterator();
        while( it.hasNext() ) {
          p = it.next();
          if (p.substring(p.indexOf('/')).startsWith( cp )) {
//          System.out.println( "---- REMOVE mWritePathCache " + p );
            it.remove();
          }
        }
      }
      return;
    }
    
    /** clearPathCache
     */
    static public void clearPathCache() {
      synchronized( EXCLUSIVE_CONTROL ) {
        mReadPathCache.clear();
        mDenyReadPathCache.clear();
        mWritePathCache.clear();
      }
      return;
    }
    
    static public HashSet<String> getReadPathCache() {
      return mReadPathCache;
    }

    static public HashSet<String> getWritePathCache() {
      return mWritePathCache;
    }

    static public HashSet<String> getDenyReadPathCache() {
      return mDenyReadPathCache;
    }

    
    /** getCacheSize
     * @return
     */
    static public long getCacheSize() {
      long size = 0;
      for (String c : mReadPathCache) {
        size += c.length();
      }
      for (String c : mDenyReadPathCache) {
        size += c.length();
      }
      for (String c : mWritePathCache) {
        size += c.length();
      }
      return size;
    }
    
    /** fillPrincipalsForGroupMembers
     *      "SELECT * FROM jecars:groupable WHERE jecars:GroupMembers='"
     * @param pQM
     * @param pAL
     * @param pUUID
     * @throws java.lang.Exception
     */
    static protected void fillPrincipalsForGroupMembers( final QueryManager pQM, final List<Node> pAL, final String pUUID ) throws RepositoryException  {
      final Query q = pQM.createQuery( gSelectGroups + pUUID + "'", Query.SQL );
// System.out.println( "FILL PRINC " + gSelectGroups + pUUID + "'" );
      final QueryResult qr = q.execute();
      final NodeIterator ni = qr.getNodes();
      while( ni.hasNext() ) {
        final Node rn = ni.nextNode();
        if (!pAL.contains( rn ))
          pAL.add( rn );
        try {
          Node rnn = rn;
          while( (rnn=rnn.getParent())!=null ) {
            if (rnn.isNodeType( gPrincipal ))
              if (!pAL.contains( rnn ))
                pAL.add( rnn );
          }
        } catch (ItemNotFoundException e) {            
        }
        fillPrincipalsForGroupMembers( pQM, pAL, rn.getPath() );
      }
      return;
    }

    /** getAllRightGiversForUser
     * 
     * @param pUsername
     * @return
     * @throws java.lang.Exception
     */
    private Node[] getAllRightGiversForUser( final String pUsername ) throws RepositoryException {
      return getAllRightGivers( "/JeCARS/default/Users/" + pUsername );
    }
    
    /** getAllRightGivers
     *
     * @param pUUID
     * @return
     * @throws java.lang.Exception
     */
    private Node[] getAllRightGivers( final String pUUID ) throws RepositoryException  {
//    System.out.println( "get all rights for -- " + mLastAllRightGiversUUID + " --- " + pUUID );
      if ((mLastAllRightGiversUUID!=null) && (mLastAllRightGiversUUID.equals(pUUID))) {
        return mLastAllRightGivers;
      }
//  System.out.println( "---a-a--- clear CACHE " + pUUID + " cache size = " + getCacheSize() );
      //clearCache();
      final Session ses = getSession();
//      QueryManager qm = mSASession.getWorkspace().getQueryManager();
      final List<Node> nodes = new ArrayList<Node>();
      nodes.add( ses.getRootNode().getNode( pUUID.substring(1) ) );
      fillPrincipalsForGroupMembers( getQueryManager(), nodes, pUUID );
      mLastAllRightGiversUUID = pUUID;
      mLastAllRightGivers = nodes.toArray( _NODE );
      return mLastAllRightGivers;
    }
    
    /** checkReadCache
     * @param pPath
     * @return
     */
    private boolean checkReadCache( final String pPath ) {
      return mReadPathCache.contains( pPath );
    }
    
    /** addReadCache
     * @param pPath
     */
    private void addReadCache( final String pPath ) {
      synchronized( EXCLUSIVE_CONTROL ) {
        mReadPathCache.add( pPath );
      }
      return;
    }

    /** addWriteCache
     * @param pPath
     */
    final private void addWriteCache( final String pPath ) {
      synchronized( EXCLUSIVE_CONTROL ) {
        mWritePathCache.add( pPath );
      }
      return;
    }

    /** checkWriteCache
     * @param pPath
     * @return
     */
    final private boolean checkWriteCache( final String pPath ) {
      return mWritePathCache.contains( pPath );
    }
    
    /** checkDenyReadCache
     * @param pPath
     * @return
     */
    private boolean checkDenyReadCache( final String pPath ) {
      return mDenyReadPathCache.contains( pPath );
    }

    /** addDenyReadCache
     * @param pPath
     */
    private void addDenyReadCache( final String pPath ) {
      synchronized( EXCLUSIVE_CONTROL ) {
        mDenyReadPathCache.add( pPath );
      }
      return;
    }
          
    /** canRead
     *
     * @param pItemPath
     * @return
     * @throws javax.jcr.RepositoryException
     */
    @Override
    public boolean canRead( final Path pItemPath ) throws RepositoryException {
      return isGranted( pItemPath, Permission.READ );
    }

    /** checkPermission
     *
     * @param id
     * @param permissions
     * @throws javax.jcr.AccessDeniedException
     * @throws javax.jcr.ItemNotFoundException
     * @throws javax.jcr.RepositoryException
     */
// v2.0
    @Override
    public void checkPermission( final org.apache.jackrabbit.core.id.ItemId id, final int permissions) throws AccessDeniedException, ItemNotFoundException, RepositoryException {
//    public void checkPermission( final org.apache.jackrabbit.core.ItemId id, final int permissions) throws AccessDeniedException, ItemNotFoundException, RepositoryException {
      if (!isGranted( id, permissions|X_NOTCLEARCACHE )) {
        throw new AccessDeniedException("Not sufficient privileges for permissions : " + permissions + " on " + id);
      }
    }


    /**
     * @see AccessManager#isGranted(ItemId, int)
     */
// v2.0
    @Override
    public boolean isGranted( final org.apache.jackrabbit.core.id.ItemId id, final int actions )
            throws ItemNotFoundException, RepositoryException {
//    public boolean isGranted( final org.apache.jackrabbit.core.ItemId id, final int actions )
//            throws ItemNotFoundException, RepositoryException {
        checkInitialized();
        if (mInTheManager) return true;
        int perm = 0;
        if ((actions & READ) == READ) {
          perm |= Permission.READ;
        }
        if ((actions & WRITE) == WRITE) {
          if (id.denotesNode()) {
            perm |= Permission.SET_PROPERTY;
            perm |= Permission.ADD_NODE;
          } else {
            perm |= Permission.SET_PROPERTY;
          }
        }
        if ((actions & REMOVE) == REMOVE) {
          perm |= (id.denotesNode()) ? Permission.REMOVE_NODE : Permission.REMOVE_PROPERTY;
        }
        if ((actions & X_NOTCLEARCACHE)==X_NOTCLEARCACHE) {
          perm |= X_NOTCLEARCACHE;
        }
        final Path path = mHierMgr.getPath(id);
        return isGranted(path, perm);
    }

    /** _resolveNodePath
     *
     * @param pPath
     * @return
     * @throws javax.jcr.RepositoryException
     */
    final private NodeImpl _resolveNodePath( final Path pPath, final BooleanHolder pIsProperty ) throws RepositoryException {
//      if (pPath.denotesRoot()) {
//        return mItemManager.getNode( pPath );
//      }
      try {
        return (NodeImpl)mItemManager.getNode( pPath );
      } catch( PathNotFoundException pe ) {
        pIsProperty.mValue = true;
        return _resolveNodePath( pPath.subPath( 0, pPath.getLength()-1 ), pIsProperty );
      }
    }


    /** _isGranted
     *
     * @param pPath
     * @param pName
     * @param pPermissions
     * @return
     * @throws javax.jcr.ItemNotFoundException
     * @throws javax.jcr.RepositoryException
     */
    @Override
    public boolean isGranted( final Path pPath, final int pPermissions) throws RepositoryException {
        if (mInTheManager) return true;
        if (pPermissions==Permission.NODE_TYPE_MNGMT) return true;  // ***** TODO activate with Jackrabbit v1.6
//        if (pPermissions==128) return true;
        
        mInTheManager = true;
        try {
          if (mLoggedInUsername!=null) {

            if (mLoggedInUsername.equals( gUSERNAME_GRANTALL )) {
              if ((pPermissions&Permission.READ)==0) {
                clearCache();
              }
              return true;
            }

            boolean clearCache = true;
            NodeImpl n = null;
            String perm = null;
            final BooleanHolder isProperty = new BooleanHolder();
            if (((pPermissions&Permission.SET_PROPERTY)!=0) ||
                ((pPermissions&Permission.REMOVE_PROPERTY)!=0)) {
                final Path np = pPath.subPath( 0, pPath.getLength()-1 );
                try {
                  n = _resolveNodePath( np, isProperty );
                  isProperty.mValue = true;
                } catch( PathNotFoundException pe ) {
                  return false;
                }
            } else {
              try {
                n = _resolveNodePath( pPath, isProperty );
              } catch( PathNotFoundException pe ) {
                return false;
              }
            }
            final String fp = n.getPath();
//              System.out.println("  fp = " + fp);
            if ((pPermissions&X_NOTCLEARCACHE)!=0) {
              clearCache = false;
            }
            if ((pPermissions&Permission.READ)!=0) {
              if (isProperty.mValue) {
                perm = P_GETPROPERTY;
              } else {
                perm = P_READ;
              }
            } else if (((pPermissions&Permission.SET_PROPERTY)!=0) || ((pPermissions&Permission.LOCK_MNGMT)!=0))    //***** TODO activate with Jackrabbit v1.6
//            else if ((pPermissions&Permission.SET_PROPERTY)!=0)
              perm = P_SETPROPERTY;
            else if ((pPermissions&Permission.ADD_NODE)!=0) {
              perm = P_ADDNODE;
            } else if ((pPermissions&Permission.REMOVE_NODE)!=0) {
              n = (NodeImpl)n.getParent();
              perm = P_REMOVE;
            } else if ((pPermissions&Permission.REMOVE_PROPERTY)!=0) {
              n = (NodeImpl)n.getParent();
              perm = P_REMOVE;
            }
            try {
              final String uuidfp = mLoggedInUsername+fp;
//      if (propId==null) {
//        System.out.println( "--- " + pPath.getString() + " : " + pPermissions + " : " + pPermissions + " : " + perm );
//        System.out.println( "--- " + uuidfp + " : " + pPermissions + " : " + pPermissions + " : " + perm );
//      } else {
//        System.out.println( "-P- " + uuidfp + "/" + propId.getName() + " : " + pPermissions + " : " + perm );
//      }
              if (perm.equals(P_READ)) {
                // **** READ Cache
                if (checkReadCache(     uuidfp )) return true;
                if (checkDenyReadCache( uuidfp )) return false;
              }
              if (perm.equals(P_ADDNODE)) {
                if (checkWriteCache( uuidfp )) return true;
              }
              // **** getAllRightGivers will clear the cache when an other user is detected
              final Node rn[]     = getAllRightGiversForUser( mLoggedInUsername );

              // **** Only the permissions on the same level
              // **** "SELECT * FROM jecars:permissionable WHERE (jecars:Principal='"
              final StringBuilder qu = new StringBuilder( gSelectPermission );
              qu.append( mLoggedInPath ).append( "'" );
              for ( int i=0; i<rn.length; i++ ) {
                qu.append( gPrincipalSQL ).append( rn[i].getPath() ).append( "'" );
              }
              qu.append( ") AND " ).append( gActions ).append( "='" ).append( perm ).append( "'" );
              final StringBuilder qu2 = new StringBuilder( qu );

              // **** New check the permission with delegate=true
              qu.append( " AND (jecars:Delegate='true')" );
              final String qus = qu.toString();
              if ((mLastPermDeleg_Result==null) || (!qus.equals(mLastPermDeleg_Select))) {
                mLastPermDeleg_Select = qus;
                mLastPermDeleg_Result = getQueryManager().createQuery( qus, Query.SQL ).execute();
              }
              NodeIterator ni = mLastPermDeleg_Result.getNodes();
              while( ni.hasNext() ) {
                final Node cn = ni.nextNode();
                String chPath = cn.getParent().getPath();
//                if (!chPath.endsWith( "/" )) {
                if (chPath.charAt( chPath.length()-1 )!='/') {
                  chPath += "/";
//                    System.out.println("ends with -= " + chPath );
                }
                if (fp.startsWith( chPath )) {
                  if (perm.equals( P_ADDNODE )) {
                    // **** Tracker #1916760
                    if (n.isNodeType( "jecars:permissionable" )) {
                      if (clearCache) clearCache();
                    } else {
                      addWriteCache( uuidfp );
                    }
                  } else if (perm.equals( P_REMOVE )) {
                    // **** Tracker #1916760
                    if (n.isNodeType( "jecars:permissionable" )) {
                      // **** Only clear the cache when the removed node is a permission object
                      if (clearCache) clearCache();
                    } else {
                      // **** Remove this path and all children
                      if (clearCache) clearPathCache( uuidfp );
                    }
                  } else {
                    addReadCache( uuidfp );
                  }
                  return true;
                }
              }

              // *********************************************
              // **** Handle the permission without delegation
              String chpath = n.getPath();
              if ("/".equals( chpath)) chpath = "";
              qu2.append( " AND (jcr:path LIKE '" ).append( chpath ).append(
                      "/%' AND NOT jcr:path LIKE '" ).append( chpath ).append( "/%/%')" );
              final QueryResult qr = getQueryManager().createQuery( qu2.toString(), Query.SQL ).execute();
              ni = qr.getNodes();
              while( ni.hasNext() ) {
                final Node cn = ni.nextNode();
                if (fp.equals( cn.getParent().getPath() )) {
                  if (perm.equals( P_ADDNODE )) {
                    if (cn.isNodeType( "jecars:permissionable" )) {
                      if (clearCache) clearCache();
                    } else {
                      addWriteCache( uuidfp );
                    }
                  } else if (perm.equals( P_REMOVE )) {
                    if (cn.isNodeType( "jecars:permissionable" )) {
                      // **** Only clear the cache when the removed node is a permission object
                      if (clearCache) clearCache();
                    } else {                      // **** Remove this path and all children
                      if (clearCache) clearPathCache( uuidfp );
                    }
                  } else {
                    addReadCache( uuidfp );
                  }
                  return true;
                }
              }

              // **** Check node itself
              if ((n.isNodeType( "jecars:permissionable" )) && (!n.isNodeType( "jecars:Permission" ))) {
                if ((n.hasProperty( "jecars:Actions" )) && (n.hasProperty( "jecars:Principal" ))) {
                  final Property prin = n.getProperty( "jecars:Principal" );
                  Value[] vals = prin.getValues();
                  String rg = null;
                  for (Value value : vals) {
                    for( Node rgiver : rn ) {
                      if (rgiver.getPath().equals( value.getString() )) {
                        rg = rgiver.getPath();
                        break;
                      }
                    }
                  }
                  if (rg!=null) {
                    final Property act = n.getProperty( "jecars:Actions" );
                    vals = act.getValues();
                    for (Value value : vals) {
                      if (value.getString().equals( P_READ ) && (perm.equals(P_READ))) {
                        addReadCache( uuidfp );
                        return true;
                      } else if (value.getString().equals( P_GETPROPERTY ) && (perm.equals(P_GETPROPERTY))) {
                        addReadCache( uuidfp );
                        return true;
                      } else if (value.getString().equals( P_ADDNODE ) && (perm.equals(P_ADDNODE))) {
                        addWriteCache( uuidfp );
                        return true;
                      } else if (value.getString().equals( P_SETPROPERTY ) && (perm.equals(P_SETPROPERTY))) {
                        addWriteCache( uuidfp );
                        return true;
                      } else if (value.getString().equals( P_REMOVE ) && (perm.equals(P_REMOVE))) {
                        if (clearCache) clearCache();
                      }
                    }
                  }
                }
              }
              if (perm.equals(P_READ)) {
                addDenyReadCache( uuidfp );
              }
            } catch (Exception e) {
              throw new RepositoryException(e);
            }
//              System.out.println("NOT ALLOWED!!! ");
            return false;
          }
        } catch (ItemNotFoundException infe) {
          return true;
        } catch (RepositoryException re) {
          re.printStackTrace();
          return false;
        } finally {
          mInTheManager = false;
        }
        if (system) {
            // system has always all permissions
            return true;
        } else if (anonymous) {
            // anonymous is always denied WRITE & REMOVE permissions
            if (((pPermissions & Permission.ADD_NODE) == Permission.ADD_NODE) ||
                ((pPermissions & Permission.REMOVE_NODE) == Permission.REMOVE_NODE) ||
                ((pPermissions & Permission.REMOVE_PROPERTY) == Permission.REMOVE_PROPERTY) ) {
                return false;
            }
        }
  //    }
      return true;
    }




}
