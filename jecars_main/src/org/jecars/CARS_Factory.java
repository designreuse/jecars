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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.util.*;
import java.util.logging.*;
import javax.jcr.*;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;
import javax.security.auth.login.AccountLockedException;
import javax.security.auth.login.CredentialExpiredException;
import nl.msd.jdots.JD_Taglist;
import org.apache.jackrabbit.core.RepositoryImpl;
import org.jecars.jaas.CARS_PasswordService;
import org.apache.jackrabbit.core.TransientRepository;
import org.apache.jackrabbit.core.state.CacheManager;
import org.apache.log4j.PropertyConfigurator;
import org.jecars.apps.CARS_AccountsApp;
import org.jecars.apps.CARS_AdminApp;
import org.jecars.apps.CARS_Interface;

/**
 * CARS_Factory
 *
 * @version $Id: CARS_Factory.java,v 1.37 2009/06/21 20:58:48 weertj Exp $
 */
public class CARS_Factory {
  
  static final public int REPOSITORYTYPE_TRANSIENT = 1;

//  static private HashMap<String, Session> gSessionPool = new HashMap<String, Session>();
  
  static final public String     JECARSPROPERTIESNAME = "jecars.properties";
  // **** If not null then gJecarsPropertiesPath overrules the properties location
  static       public String     gJecarsPropertiesPath = null;
  static final public Properties gJecarsProperties = new Properties();
  static public String        gConfigFile       = "";
  static public String        gRepHome          = "";
  static public String        gRepLogHome       = "";
  static public String        gRepNamespaces    = "";
  static public String        gRepCNDFiles      = "";
  static public Level         gLogLevel   = Level.FINE;
  static private CARS_Factory gLastFactory = null;

  static final protected Logger gLog = Logger.getLogger( "org.jecars" );
  
  static private   SimpleCredentials    gSysCreds                 = null;
  static protected TransientRepository  gRepository               = null;
  static private   Session              gSystemCarsSession        = null;
  static private   Session              gSystemAccessSession      = null;
  static private   Session              gSystemLoginSession       = null;
  static private   Session              gSystemApplicationSession = null;
  static private   Session              gSystemToolSession        = null;
  static private   Session              gObservationSession       = null;
  static protected CARS_EventManager    gEventManager             = null;
  static private   Object               gServletContext           = null;
  static final private Calendar         gStartTime                = Calendar.getInstance();
  static private   boolean              gEnableFET                = true;
  
  /** Creates a new instance of CARS_Factory */
  public CARS_Factory() {
    gLastFactory   = this;
    return;
  }

  /** setJecarsPropertiesPath
   * 
   * @param pPath
   */
  static public void setJecarsPropertiesPath( final String pPath ) {
    gJecarsPropertiesPath = pPath;
    return;
  }

  /** setEnableFET
   * 
   * @param pEnable
   */
  static public void setEnableFET( final boolean pEnable ) {
    gEnableFET = pEnable;
    return;
  }

  /** getJeCARSStartTime
   * 
   * @return
   */
  static public Calendar getJeCARSStartTime() {
    return gStartTime;
  }

  /** setServletContext
   * 
   * @param pSC
   */
  static public void setServletContext( final Object pSC ) {
    gServletContext = pSC;
    return;
  }

  /** getServletContext
   *
   * @return
   */
  static public Object getServletContext() {
    return gServletContext;
  }

  /** getSessionPool
   * 
   * @return
   */
//  static HashMap<String, Session>getSessionPool() {
//    return gSessionPool;
//  }
 
  /** isInSessionPool
   * 
   * @param pSession
   * @return
   */
//  static boolean isInSessionPool( Session pSession ) {
//    return (gSessionPool.containsValue( pSession ));
//  }
  
  /** Get the last created factory
   */
  static public CARS_Factory getLastFactory() {
    return gLastFactory;
  }

  /** initJeCARSProperties
   *
   */
  static private void initJeCARSProperties() {
    try {
//      if (gJecarsProperties.isEmpty()) {
        if (gJecarsPropertiesPath==null) {
          gLog.log( Level.INFO, "Trying to read jecars properties as system resource " + JECARSPROPERTIESNAME );
          final InputStream sis = ClassLoader.getSystemResourceAsStream( "/" + JECARSPROPERTIESNAME );
          if (sis!=null) {
            gJecarsProperties.load( sis );
            sis.close();
          } else {
            // **** Read jecars property file
            final File f = new File( JECARSPROPERTIESNAME );
            gLog.log( Level.INFO, "Trying to read file: " + f.getCanonicalPath() );
            if (f.exists()) {
              final FileInputStream fis = new FileInputStream(f);
              try {
                gJecarsProperties.load( fis );
              } finally {
                fis.close();
              }
            } else {
              gLog.log( Level.SEVERE, "Cannot find " + f.getCanonicalPath()  );
            }
          }
        } else {
          gLog.log( Level.INFO, "Trying to read jecars properties from path " + gJecarsPropertiesPath );
          final FileInputStream jfis = new FileInputStream( gJecarsPropertiesPath );
          try {
            gJecarsProperties.load( jfis );
          } finally {
            jfis.close();
          }
        }
//      }
      gLog.log( Level.INFO, "Config file = " + gJecarsProperties.getProperty( "jecars.ConfigFile", "<null>" ) );
      gConfigFile    = gJecarsProperties.getProperty( "jecars.ConfigFile", "<null>" );
      gRepHome       = gJecarsProperties.getProperty( "jecars.RepHome",    "<null>" );
      gLog.log( Level.INFO, "Repository home = " + gRepHome );
      gRepLogHome    = gJecarsProperties.getProperty( "jecars.RepLogHome", "<null>" );
      gLog.log( Level.INFO, "Log home = " + gRepLogHome );
      gRepNamespaces = gJecarsProperties.getProperty( "jecars.Namespaces", "jecars,http://jecars.org" );
      gLog.log( Level.INFO, "Namespaces = " + gRepNamespaces );
      gRepCNDFiles   = gJecarsProperties.getProperty( "jecars.CNDFiles",   "/org/jecars/jcr/nodetype/jecars.cnd,jecars.cnd" );
      gLog.log( Level.INFO, "CND files = " + gRepCNDFiles );
    } catch (IOException e) {
      gLog.log( Level.SEVERE, null, e );
    }      
  }

  
  static public CARS_EventManager getEventManager() {
    return gEventManager;
  }

  /** setRepository
   *
   * @param pRepType
   * @throws IOException
   */
  static public void setRepository( final int pRepType ) throws IOException {
    switch( pRepType ) {
      case REPOSITORYTYPE_TRANSIENT: {
        setRepository( new TransientRepository() );
        return;
      }      
    }
    gLog.log( Level.SEVERE, "Unknown repository type: " + pRepType );
    return;
  }

  /** setRepository
   * 
   * @param pRep
   */
  static public void setRepository( TransientRepository pRep ) {
    gRepository = pRep;
    return;
  }
  
  static public Repository getRepository() {
    return gRepository;
  }
  
  /** Add namespaces
   */
  protected void addNamespaces( NamespaceRegistry pNSR, String[] pNS ) throws RepositoryException {
    int i = 0;
    while( i<pNS.length ) {
      try {
        gLog.log( Level.INFO, "Add namespace: " + pNS[i] + " = " +  pNS[i+1] );
        CARS_ActionContext.addPublicNamespace( pNS[i] );
        pNSR.registerNamespace( pNS[i], pNS[i+1] );
      } catch (NamespaceException ne) {
        // **** Jackrabbit init ready
      }
      i += 2;
    }
    return;
  }
  
  /** Add nodetypes definitions
   * @param pSession
   * @param pCNDS
   * @throws java.io.IOException
   * @throws org.apache.jackrabbit.core.nodetype.compact.ParseException
   * @throws javax.jcr.RepositoryException
   */
// v2.0
//  protected void addNodeTypesDefinitions( final Session pSession, final String[] pCNDS ) throws IOException, RepositoryException, org.apache.jackrabbit.core.nodetype.compact.ParseException {
  protected void addNodeTypesDefinitions( Session pSession, String[] pCNDS ) throws IOException, RepositoryException, org.apache.jackrabbit.commons.cnd.ParseException {
    int i = 0;
    while( i<pCNDS.length ) {
      gLog.log( Level.INFO, "Process CND file: " + pCNDS[i] + " = " +  pCNDS[i+1] );
      final InputStream is = CARS_Factory.class.getResourceAsStream( pCNDS[i] );
      if (is!=null) {
        final InputStreamReader isr = new InputStreamReader( is );
// v2.0
        org.apache.jackrabbit.commons.cnd.CndImporter.registerNodeTypes( isr, pSession );

//        final org.apache.jackrabbit.core.nodetype.compact.CompactNodeTypeDefReader cndReader =
//                    new org.apache.jackrabbit.core.nodetype.compact.CompactNodeTypeDefReader( isr, pCNDS[i+1] );
//        final List ntdList = cndReader.getNodeTypeDefs();
//        final NodeTypeManagerImpl ntmgr =(NodeTypeManagerImpl)pSession.getWorkspace().getNodeTypeManager();
//        final NodeTypeRegistry ntreg = ntmgr.getNodeTypeRegistry();
//        for (final Iterator it = ntdList.iterator(); it.hasNext();) {
//          final org.apache.jackrabbit.core.nodetype.NodeTypeDef ntd = (org.apache.jackrabbit.core.nodetype.NodeTypeDef)it.next();
//          try {
//            ntreg.registerNodeType(ntd);
//          } catch (InvalidNodeTypeDefException de) {
//            LOG.log( Level.INFO, de.getMessage() );
//          } catch (RepositoryException re) {
//            LOG.log( Level.INFO, re.getMessage() );
//          }
//        }
        isr.close();
        is.close();
      } else {
        gLog.log( Level.SEVERE, "Cannot find CND file: " + pCNDS[i] + " - " + pCNDS[i+1] );
      }
      i += 2;
    }
    return;
  }
  
  /** Init the JCR repository and register the nodetypes
   *
   * @param pCreds
   * @throws java.lang.Exception
   */
  private void initJCR( final SimpleCredentials pCreds ) throws Exception {
    initJeCARSProperties();
    // **** init log handler
    initLogging();
    gLog.log( Level.INFO, "Create repository using: " + gConfigFile + " and " + gRepHome );
    gLog.log( Level.INFO, "Config file abspath: " + new File(gConfigFile).getAbsolutePath() );
    gLog.log( Level.INFO, "Repository home abspath: " + new File(gRepHome).getAbsolutePath() );

    try {
      CARS_LoginModule.gSuperuserAllowed = true;
      if (gRepository==null) setRepository( new TransientRepository( gConfigFile, gRepHome ) );

      gLog.info( "JeCARS version: " + CARS_Main.VERSION );
      gLog.info( "JCR version: " + gRepository.getDescriptor( Repository.SPEC_VERSION_DESC ) + " (" + gRepository.getDescriptor( Repository.SPEC_NAME_DESC ) + ")" );
      gLog.info( "Repository version: " + gRepository.getDescriptor( Repository.REP_VERSION_DESC ) + " (" + gRepository.getDescriptor( Repository.SPEC_NAME_DESC ) + ")" );
      gLog.info( "Repository vendor: " + gRepository.getDescriptor( Repository.REP_VENDOR_DESC ));

      gEventManager = new CARS_EventManager();
      gSystemLoginSession       = gRepository.login( pCreds );
      gSystemAccessSession      = gRepository.login( pCreds );
      gSystemApplicationSession = gRepository.login( pCreds );
      gSystemToolSession        = gRepository.login( pCreds );
      gSystemCarsSession        = gRepository.login( pCreds );
      gObservationSession       = gRepository.login( pCreds );

      CacheManager cache = ((RepositoryImpl)gSystemApplicationSession.getRepository()).getCacheManager();
      cache.setMaxMemory( 16*1024*1024 );
//      cache.setMaxMemory( 1 );
      cache.setMaxMemoryPerCache( 4*1024*1024 );
      cache.setMinMemoryPerCache( 1*1024*1024 );
      
      NamespaceRegistry nsReg = gSystemCarsSession.getWorkspace().getNamespaceRegistry();
      String[] ns = gRepNamespaces.split( "," );
      addNamespaces( nsReg, ns );
      String[] cnds = gRepCNDFiles.split( "," );
      addNodeTypesDefinitions( gSystemCarsSession, cnds );
    } finally {
//      CARS_LoginModule.gSuperuserAllowed = false;
    }
    
//    CARS_AdminApp.autoStartTools();
    return;
  }

  /** Init the logging files, the directories
   */
  public void initLogging() throws Exception {
    File f = new File( gRepLogHome );
    if (!f.exists()) {
      if (!f.mkdirs()) {
        throw new Exception( "Cannot create directory: " + f.getCanonicalPath() );
      }
    }
    Handler fh = new FileHandler( f.getAbsolutePath() + "/jecars.log", false );
    fh.setFormatter( new SimpleFormatter() );
    fh.setLevel( gLogLevel );
    gLog.addHandler( fh );
    Properties p = System.getProperties();
    p.put( "derby.stream.error.file", f.getAbsolutePath() + "/derby.log" );
//    org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger( "A1" );
    Properties props = new Properties();
    props.setProperty( "log4j.logger.org.apache.jackrabbit.core", "INFO, A1" );
    props.setProperty( "log4j.appender.A1", "org.apache.log4j.FileAppender" );
    props.setProperty( "log4j.appender.A1.file", f.getAbsolutePath() + "/jackrabbit.log" );
    props.setProperty( "log4j.appender.A1.layout", "org.apache.log4j.PatternLayout" );
    PropertyConfigurator.configure( props );
    return;
  }
  
  /** Init, the initial object framework will be created
   * 
   * @param pCreds
   * @param pReinit
   * @throws java.lang.Exception
   */
  public void init( SimpleCredentials pCreds, final boolean pReinit ) throws Exception {
    
    final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger( "org.apache.jackrabbit.core.TransientRepository" );
    logger.setLevel( org.apache.log4j.Level.ERROR );
      
    if (pCreds==null) {
      pCreds = new SimpleCredentials( "Superuser", "pw".toCharArray() );
    }
    gSysCreds = pCreds;
    initJCR( gSysCreds );
    final Node rootNode = gSystemCarsSession.getRootNode();
    Node n = gSystemCarsSession.getRootNode();
    if (pReinit) {
      if (n.hasNode( CARS_Main.MAINFOLDER )) {
        n.getNode( CARS_Main.MAINFOLDER ).remove();
      }
      n.save();
    }
    final Calendar cal = Calendar.getInstance();
    if (!n.hasNode( CARS_Main.MAINFOLDER )) {
      n.addNode( CARS_Main.MAINFOLDER, "jecars:main" );
    }
    n = n.getNode( CARS_Main.MAINFOLDER );
    n.setProperty( "jecars:Started", cal );

    Node internalSource;
//    if (n.hasNode( "Trashcans" )==false) {
//      Node trashcans = n.addNode( "Trashcans", "jecars:Trashcan" );
//      trashcans.setProperty( "jecars:Created", cal );
//      Node gt = trashcans.addNode( "General", "jecars:Trashcan" );
//      gt.setProperty( "jecars:Body", "General default trashcan" );
//    }
    
    if (!rootNode.hasNode( "accounts" )) {
      final Node ext = rootNode.addNode( "accounts", "jecars:CARS_Interface" );
      ext.setProperty( "jecars:InterfaceClass", "org.jecars.apps.CARS_AccountsApp" );
//      ext.addNode( "login", "jecars:root" );
    }
    final Node accountsNode = rootNode.getNode( "accounts" );
    
    if (!n.hasNode( "UserSources" )) {
      final Node ext = n.addNode( "UserSources", "jecars:UserSources" );
      ext.addNode( "rest", "jecars:UserSource" );
      internalSource = ext.addNode( "internal", "jecars:UserSource" );
    }
    internalSource = n.getNode( "UserSources/internal" );
    if (!n.hasNode( "GroupSources" )) {
      final Node ext = n.addNode( "GroupSources", "jecars:GroupSources" );
      Node internalGroup = ext.addNode( "internal", "jecars:GroupSource" );
    }
    Node def;
    if (!n.hasNode( "default" )) {
      def = n.addNode( "default", "jecars:workspace" );
    }
    def = n.getNode( "default" );
    if (!def.hasNode( "Users" )) {
      def.addNode( "Users", "jecars:Users" );
    }
    final Node users = def.getNode( "Users" );
    if (!users.hasNode( "Superuser" )) {
      // **** Superuser
      Node su = users.addNode( "Superuser", "jecars:User" );
      su.setProperty( "jecars:Fullname", "Superuser" );
      su.setProperty( "jecars:Source", internalSource.getPath() );
      su.setProperty( "jecars:Password_crypt", CARS_PasswordService.getInstance().encrypt("pw") );
    }
    if (!users.hasNode( "Administrator" )) {
      // **** Administrator
      final Node admin = users.addNode( "Administrator" );
      admin.setProperty( "jecars:Fullname", "Administrator" );
      admin.setProperty( "jecars:Source", internalSource.getPath() );
      admin.setProperty( "jecars:Password_crypt", CARS_PasswordService.getInstance().encrypt("admin") );
    }
    if (!users.hasNode( "anonymous" )) {
      // **** anonymous
      final Node anon = users.addNode( "anonymous" );
      anon.setProperty( "jecars:Fullname", "anonymous" );
      anon.setProperty( "jecars:Source", internalSource.getPath() );
      CARS_DefaultMain.setCryptedProperty( anon, "jecars:Password_crypt", "anonymous" );
//      anon.setProperty( "jecars:Password_crypt", CARS_PasswordService.getInstance().encrypt("anonymous") );
    }

    if (!accountsNode.hasNode( "jecars:P_anonymous" )) {
      Node p = accountsNode.addNode( "jecars:P_anonymous", "jecars:Permission" );
      final String[] r = {"read","add_node","get_property"};
      p.setProperty( "jecars:Actions", r );
      final Node anon = users.getNode( "anonymous" );
      final Value[] anons = {gSystemCarsSession.getValueFactory().createValue( anon.getPath() )};
      p.setProperty( "jecars:Principal", anons );
      p = rootNode.addNode( "jecars:P_anonymous", "jecars:Permission" );
      final String[] rr = {"read"};
      p.setProperty( "jecars:Actions", rr );
      p.setProperty( "jecars:Principal", anons );
    }
    
    if (!def.hasNode( "Data" )) {
      Node df = def.addNode( "Data", "jecars:datafolder" );
    }
    if (!def.hasNode( "Events" )) {
      Node df = def.addNode( "Events", "jecars:EventsFolder" );
    }
    final Node events = def.getNode( "Events" );
    if (!events.hasNode( "Applications" )) {
      Node df = events.addNode( "Applications", "jecars:EventsFolder" );
      df = df.addNode( "Directory", "jecars:EventsFolder" );
    }
    if (!events.hasNode( "System" )) {
      Node df = events.addNode( "System", "jecars:SystemEventsFolder" );
    }
    
    final Node su    = users.getNode( "Superuser" );
    final Node admin = users.getNode( "Administrator" );
    
    if (!def.hasNode( "Groups" )) {
      // **** Groups
      def.addNode( "Groups", "jecars:Groups" );
    }

    final Node groups = def.getNode( "Groups" );
    if (!groups.hasNode( "World" )) {
      // **** World
      final Node world = groups.addNode( "World", "jecars:Group" );
      world.setProperty( "jecars:Fullname", "World (all users)" );
      world.setProperty( "jecars:Source", "/JeCARS/GroupSources/internal" );
      final ArrayList<Value> l = new ArrayList<Value>();
      Value v = world.getSession().getValueFactory().createValue( su.getPath() );
      l.add( v );
      v = world.getSession().getValueFactory().createValue( admin.getPath() );
      l.add( v );
      world.setProperty( "jecars:GroupMembers",l.toArray(new Value[0]) );
    }
    if (!groups.hasNode( "Admins" )) {
      // **** Admins
      final Node admins = groups.addNode( "Admins", "jecars:Group" );
      admins.setProperty( "jecars:Fullname", "Administrators" );
      admins.setProperty( "jecars:Source", "/JeCARS/GroupSources/internal" );
      final ArrayList<Value> l = new ArrayList<Value>();
      Value v = admins.getSession().getValueFactory().createValue( su.getPath() );
      l.add( v );
      v = admins.getSession().getValueFactory().createValue( admin.getPath() );
      l.add( v );
      admins.setProperty( "jecars:GroupMembers",l.toArray(new Value[0]) );
    }
    if (!groups.hasNode( "EventsAppUsers" )) {
      // **** EventsAppUsers
      final Node eau = groups.addNode( "EventsAppUsers", "jecars:Group" );
      eau.setProperty( "jecars:Fullname", "Members of this group can use the EventsApp application" );
      eau.setProperty( "jecars:Source", "/JeCARS/GroupSources/internal" );
      CARS_Utils.setCurrentModificationDate( groups );
    }
    final Node eau    = def.getNode( "Groups/EventsAppUsers" );
    final Node world  = def.getNode( "Groups/World" );
    final Node admins = def.getNode( "Groups/Admins" );
    if (!rootNode.hasNode( "jecars:P_Admins" )) {
      final Node p = rootNode.addNode( "jecars:P_Admins", "jecars:Permission" );
      p.setProperty( "jecars:Delegate", true );
      final String[] r = {"read","add_node","set_property","get_property","remove","acl_read","acl_edit"};
      p.setProperty( "jecars:Actions", r );
      final Value[] adminsV = {gSystemCarsSession.getValueFactory().createValue( admins.getPath() )};
      p.setProperty( "jecars:Principal", adminsV );
    }
    
    // **** Application sources
    n = gSystemCarsSession.getRootNode().getNode( "JeCARS" );
    if (!n.hasNode( "ApplicationSources" )) {
      final Node ext = n.addNode( "ApplicationSources", "jecars:ApplicationSources" );
      Node appext = ext.addNode( "AdminApp", "jecars:CARS_Interface" );
      appext.setProperty( "jecars:InterfaceClass", "org.jecars.apps.CARS_AdminApp" );
      appext = ext.addNode( "ToolsApp", "jecars:CARS_Interface" );
      appext.setProperty( "jecars:InterfaceClass", "org.jecars.apps.CARS_ToolsApp" );
      CARS_Utils.setCurrentModificationDate( ext );
    }
    final Node asn = n.getNode( "ApplicationSources" );
    // **** Always rebuild eventsapp
    if (asn.hasNode( "EventsApp" )) {
      asn.getNode( "EventsApp" ).remove();
      asn.save();
    }
    if (!asn.hasNode( "EventsApp" )) {
      // **** Event application
      final Node appext = asn.addNode( "EventsApp", "jecars:CARS_Interface" );
      appext.setProperty( "jecars:InterfaceClass", "org.jecars.apps.CARS_EventsApp" );
      CARS_Utils.setCurrentModificationDate( asn );
    }
    final Node eventsApp = asn.getNode( "EventsApp" );
    if (!eventsApp.hasNode( "jecars:P_EventsAppUsers" )) {
      CARS_Utils.addPermission( eventsApp, "EventsAppUsers", null, "read,get_property,add_node" );
    }

    // **** Always rebuild infoapp
    if (asn.hasNode( "InfoApp" )) {
      asn.getNode( "InfoApp" ).remove();
      asn.save();
    }
    if (!asn.hasNode( "InfoApp" )) {
      // **** Info application
      final Node appext = asn.addNode( "InfoApp", "jecars:CARS_Interface" );
      appext.setProperty( "jecars:InterfaceClass", "org.jecars.apps.CARS_InfoApp" );
      CARS_Utils.setCurrentModificationDate( asn );
      try {
        CARS_Utils.addPermission( appext, "DefaultReadGroup", null, "read,get_property,delegate" );
      } catch( PathNotFoundException pnfe ) {
        gLog.log( Level.WARNING, pnfe.getMessage() + " not found, InfoApp will be added after initialization of JeCARS" );
        appext.remove();
      }
    }

    gSystemCarsSession.save();
    
    // **** Check user/password
    if (def.hasNode( "Users/" + pCreds.getUserID() )) {
      final Node user = def.getNode( "Users/" + pCreds.getUserID() );
      if (!user.getProperty( "jecars:Password_crypt" ).getString().equals(
              CARS_PasswordService.getInstance().encrypt(new String(pCreds.getPassword()) ))) {
        throw new Exception( "Invalid password" );        
      }
    } else {
      throw new Exception( "User unknown: " + pCreds.getUserID() );
    }
    

    // **** Add logger for JeCARS events
    final Handler jecarsH = new CARS_LogHandler();
    final Logger globalLogger = Logger.getLogger( "" );
    globalLogger.addHandler( jecarsH );

    // **** Init the application sources
//    initApplicationSources( gSystemCarsSession );
    initApplicationSources( null );
    CARS_AdminApp.autoStartTools();

    return;
  }
 
  /** Init all Application sources (jecars:CARS_Interface)
   * @param pMain CARS_Main
   */
  public void initApplicationSources( final CARS_Main pMain ) throws RepositoryException {
    final String query = "SELECT * FROM jecars:interfaceclass WHERE jecars:InterfaceClass<>''";
    final Session ses = getSystemCarsSession();
    synchronized( ses ) {
      final Query q = ses.getWorkspace().getQueryManager().createQuery( query, Query.SQL );
      final QueryResult qr = q.execute();
      final NodeIterator ni = qr.getNodes();
      while( ni.hasNext() ) {
        final Node apps = ni.nextNode();
        try {
          final String clss = apps.getProperty( "jecars:InterfaceClass" ).getString();
          final CARS_Interface ic = (CARS_Interface)Class.forName( clss ).newInstance();
          ic.init( null, apps );
        } catch (Exception e) {
          gLog.log( Level.SEVERE, "initApplicationSources", e );
        }
      }
    }
      
    return;
  }
  
  /** closeAllSessions
   * 
   */
  static public void closeAllSessions() {
//    getEventManager().sessionLogout();
    
    Session ses = gSystemCarsSession;
    if (ses!=null) {
      synchronized( ses ) {
        gSystemCarsSession = null;
        ses.logout();
      }
    }
    try {
      gSystemLoginSession.refresh( false );
    } catch( Exception e ) {
      e.printStackTrace();
    }
    try {
      gSystemAccessSession.refresh( false );
    } catch( Exception e ) {
      e.printStackTrace();
    }
    try {
      gSystemApplicationSession.refresh( false );
    } catch( Exception e ) {
      e.printStackTrace();
    }
    try {
      gSystemToolSession.refresh( false );
    } catch( Exception e ) {
      e.printStackTrace();
    }
    
    return;
  }

  /** shutdown
   *
   */
  @SuppressWarnings("empty-statement")
  static public void shutdown() {
    System.out.println( "CARS_Factory: shutdown" );
    gLastFactory = null;
    if (gObservationSession!=null) {
      try {
        gObservationSession.save();
        gObservationSession.refresh( false );
      } catch( RepositoryException re ) {};
      gObservationSession.logout();
      gObservationSession = null;
    }
    if (gSystemAccessSession!=null) {
      try {
        gSystemAccessSession.save();
        gSystemAccessSession.refresh( false );
      } catch( RepositoryException re ) {};
      gSystemAccessSession.logout();
      gSystemAccessSession = null;
    }
    if (gSystemApplicationSession!=null) {
      try {
        gSystemApplicationSession.save();
        gSystemApplicationSession.refresh( false );
      } catch( RepositoryException re ) {};
      gSystemApplicationSession.logout();
      gSystemApplicationSession = null;
    }
    if (gSystemLoginSession!=null) {
      try {
        gSystemLoginSession.save();
        gSystemLoginSession.refresh( false );
      } catch( RepositoryException re ) {};
      gSystemLoginSession.logout();
      gSystemLoginSession = null;
    }
    if (gSystemToolSession!=null) {
      try {
        gSystemToolSession.save();
        gSystemToolSession.refresh( false );
      } catch( RepositoryException re ) {};
      gSystemToolSession.logout();
      gSystemToolSession = null;
    }
    if (gSystemCarsSession!=null) {
      try {
        gSystemCarsSession.save();
        gSystemCarsSession.refresh( false );
      } catch( RepositoryException re ) {};
      gSystemCarsSession.logout();
      gRepository.shutdown();
    }
    return;
  }

  /** getSystemCarsSession
   * 
   * @return
   */
  static protected Session getSystemCarsSession() {
    if (gSystemCarsSession==null) {
      try {
        gSystemCarsSession = gRepository.login( new SimpleCredentials( CARS_AccessManager.gUSERNAME_GRANTALL, "".toCharArray() ));
        gLog.info( "System jecars session login: " + CARS_AccessManager.gUSERNAME_GRANTALL );
      } catch( Exception e ) {
        e.printStackTrace();
      }
    }
    return gSystemCarsSession;
  }

  /** getSystemAccessSession
   * 
   * @return
   */
  static protected Session getSystemAccessSession() {
    return gSystemAccessSession;
  }

  /** getSystemLoginSession
   *
   * @return
   */
  static protected Session getSystemLoginSession() {
    return gSystemLoginSession;
  }

  /** getSystemApplicationSession
   *
   * @return
   */
  static public Session getSystemApplicationSession() {
    return gSystemApplicationSession;
  }

  /** getObservationSession
   *
   * @return
   */
  static public Session getObservationSession() {
    return gObservationSession;
  }
   
  /** getSystemToolsSession
   */
  static public Session getSystemToolsSession() throws LoginException, RepositoryException {
    return gSystemToolSession;
  }
  
  /** createMain
   * @param pSession
   * @param pFactory
   * @return
   * @throws java.lang.Exception
   */
  static public CARS_Main createMain( final Session pSession, final CARS_Factory pFactory ) throws RepositoryException {
    return new CARS_DefaultMain( pSession, pFactory );
  }
  
  /** createMain
   * Create a CARS_Main interface
   * @param pCreds use these credentials
   * @param pWhat not used, use "default"
   * @return the CARS_Main interface
   * @throws AccessDeniedException when the actions isn't allowed
   */
  public CARS_Main createMain( final SimpleCredentials pCreds, final String pWhat ) throws AccessDeniedException {
    CARS_Main m;
    try {
      final Session ses = gRepository.login( pCreds );
      m = createMain( ses );
    } catch (Exception e) {
      throw new AccessDeniedException( e );
    }
    return m;
  }

  /** createMain
   * Create a CARS_Main interface using only the context
   * @param pContext the action context
   * @return the CARS_Main interface
   * @throws javax.jcr.AccessDeniedException when the actions isn't allowed
   * @throws javax.security.auth.login.AccountLockedException
   * @throws javax.security.auth.login.CredentialExpiredException
   */
  public CARS_Main createMain( final CARS_ActionContext pContext ) throws AccessDeniedException, CredentialExpiredException {
    CARS_Main m;
    try {
      final Session ses;
      if (pContext.getAuthKey()==null) {
        final String userName = pContext.getUsername();
        if (userName.equals( CARS_AccessManager.gSuperuserName)) throw new AccessDeniedException();
        final SimpleCredentials creds = new SimpleCredentials( userName, pContext.getPassword() );
        ses = gRepository.login( creds );
      } else {
        final SimpleCredentials creds = new SimpleCredentials( CARS_AccountsApp.AUTHKEY_PREFIX + pContext.getAuthKey(), "".toCharArray() );
        ses = gRepository.login( creds );
      }
      m = createMain( ses );
      m.addContext( pContext );
      final String userId = m.getSession().getUserID();
      final Session loginSession = CARS_Factory.getSystemLoginSession();
      synchronized( loginSession ) {
        final Node users = loginSession.getNode( "/JeCARS/default/Users" );
        final Node user = users.getNode( userId );
//        final Node user = m.getLoginUser();
        if ((user.hasProperty( "jecars:PasswordMustChange" )) && (user.getProperty( "jecars:PasswordMustChange" ).getBoolean())) {
          // **** Password must change
          if (user.hasProperty( "jecars:Source" )) {
//            final Session sas = getSystemAccessSession();
//            synchronized(sas) {
//            // **** Refresh after x times
//            if (gRefreshSystemAccessSession<=0) {
//              sas.refresh( false );
//              gRefreshSystemAccessSession = gRefreshSystemAccessSession_Times;
//            }
//            gRefreshSystemAccessSession--;
              final Node source = loginSession.getNode( user.getProperty( "jecars:Source" ).getString().substring(1) );
              if (source.hasProperty( "jecars:ChangePasswordURL" )) {
                throw new CredentialExpiredException( source.getProperty( "jecars:ChangePasswordURL" ).getString() );
              }
//            }
          }
          throw new CredentialExpiredException();
        }
      }
//    } catch (AccountLockedException ale) {
//      throw ale;
    } catch (CredentialExpiredException cee) {
      throw cee;
    } catch (Exception e) {
//    e.printStackTrace();
      throw new AccessDeniedException( e );
    }
    return m;
  }

  /** Create a CARS_Main interface
   *
   * @param pSession
   * @return
   * @throws RepositoryException
   */
  public CARS_Main createMain( final Session pSession ) throws RepositoryException {
    return new CARS_DefaultMain( pSession, this );
  }

  /** _getFET
   * 
   * @param pContext
   * @return
   */
  private String _getFET( final CARS_ActionContext pContext ) {
    if (gEnableFET) return pContext.getQueryValue( "FET=" );
    return null;
  }

  /** Do HEAD
   *
   * @param pContext
   * @throws CredentialExpiredException
   * @throws AccessDeniedException
   */
  public void performHeadAction( final CARS_ActionContext pContext ) throws CredentialExpiredException, AccessDeniedException {
    CARS_Main main = null;
    try {
      String fet = _getFET( pContext );
      pContext.setAction( CARS_ActionContext.gDefActionGET );
      main = createMain( pContext );
      pContext.setMain( main );
      if ((fet==null) || (fet.indexOf( "READ" )==-1)) {
        if (pContext.getQueryString()==null) {
          gEventManager.addEvent( main, main.getLoginUser(), null, null, "URL", "READ",
                "HEAD " + pContext.getPathInfo() );
        } else {
          gEventManager.addEvent( main, main.getLoginUser(), null, null, "URL", "READ",
                "HEAD " + pContext.getPathInfo() + "?" + CARS_ActionContext.untransportString(pContext.getQueryString())  );
        }
      }
      Node cnode = main.getNode( pContext.getPathInfo(), null, true );
      pContext.setThisNode( cnode );
      pContext.prepareResult(); // **** This will cache the result so we can close the connection
    } catch (AccountLockedException ale) {
      pContext.setErrorCode( HttpURLConnection.HTTP_UNAUTHORIZED );
      pContext.setError( ale );
    } catch (CredentialExpiredException cee) {
      throw cee;
    } catch (AccessDeniedException ade) {
      // TODO
//      gEventManager.addException( main.getLoginUser(), main.getCurrentViewNode(), null, "SYS", "READ", ade, pContext.getPathInfo() );
      gEventManager.addException( main, null, null, null, "SYS", "LOGIN", ade, pContext.getPathInfo() );
      throw ade;
    } catch (PathNotFoundException pnfe) {
      gEventManager.addException( main, main.getLoginUser(), main.getCurrentViewNode(), null, "SYS", "READ", pnfe, pContext.getPathInfo() );
      pContext.setErrorCode( HttpURLConnection.HTTP_NOT_FOUND );
      pContext.setError( pnfe );
    } catch (Exception e) {
      gEventManager.addException( main, main.getLoginUser(), main.getCurrentViewNode(), null, "SYS", "READ", e, null );
      pContext.setErrorCode( HttpURLConnection.HTTP_INTERNAL_ERROR );
      pContext.setError( e );
//    } finally {
    }
    return;
  }

  /** performGetAction
   * 
   * @param pContext
   * @throws CredentialExpiredException
   * @throws AccessDeniedException
   */
  public void performGetAction( final CARS_ActionContext pContext ) throws CredentialExpiredException, AccessDeniedException  {
    performGetAction( pContext, null );
    return;
  }
  
  /** Do GET
   * 
   * @param pContext
   * @throws java.lang.Exception
   */
  public void performGetAction( final CARS_ActionContext pContext, final CARS_Main pMain ) throws CredentialExpiredException, AccessDeniedException  {
    CARS_Main main = pMain;
    try {
//      main = createMain( new SimpleCredentials( pContext.getUsername(), pContext.getPassword() ), "default" );
      final String fet = _getFET( pContext );
      pContext.setAction( CARS_ActionContext.gDefActionGET );
      if (main==null) {
        main = createMain( pContext );
        pContext.setMain( main );
      }
      if ((fet==null) || (fet.indexOf( "READ" )==-1)) {
        if (pContext.getQueryString()==null) {
          gEventManager.addEvent( main, main.getLoginUser(), null, null, "URL", "READ",
                "GET " + pContext.getPathInfo() );
        } else {
          gEventManager.addEvent( main, main.getLoginUser(), null, null, "URL", "READ",
                "GET " + pContext.getPathInfo() + "?" + CARS_ActionContext.untransportString(pContext.getQueryString())  );
        }
      }
      pContext.setCanBeCachedResult( pContext.getQueryString()==null );
      final Node cnode = main.getNode( pContext.getPathInfo(), null, false );
      pContext.setThisNode( cnode );
      pContext.prepareResult(); // **** This will cache the result so we can close the connection
    } catch (AccountLockedException ale) {
      pContext.setErrorCode( HttpURLConnection.HTTP_UNAUTHORIZED );
      pContext.setError( ale );
    } catch (CredentialExpiredException cee) {
      throw cee;
    } catch (AccessDeniedException ade) {
      // TODO
//      gEventManager.addException( main.getLoginUser(), main.getCurrentViewNode(), null, "SYS", "READ", ade, pContext.getPathInfo() );
      gEventManager.addException( main, null, null, null, "SYS", "LOGIN", ade, pContext.getPathInfo() );
      throw ade;
    } catch (PathNotFoundException pnfe) {
      pContext.setErrorCode( HttpURLConnection.HTTP_NOT_FOUND );
      pContext.setError( pnfe );
      gEventManager.addException( main, main.getLoginUser(), main.getCurrentViewNode(), null, "SYS", "READ", pnfe, pContext.getPathInfo() );
    } catch (Exception e) {
//      LOG.log( Level.INFO, null, e );
      pContext.setErrorCode( HttpURLConnection.HTTP_INTERNAL_ERROR );
      pContext.setError( e );
      gEventManager.addException( main, main.getLoginUser(), main.getCurrentViewNode(), null, "SYS", "READ", e, null );
    } finally {
//      if (main!=null) {
//        try {
//          main.getSession().save();
//        } catch( Exception e ) {          
//        }
//      }
    }
    return;
  }

  
  /** Do POST
   *
   * @param pContext
   * @throws AccessDeniedException
   * @throws CredentialExpiredException
   */
  public void performPostAction( final CARS_ActionContext pContext ) throws AccessDeniedException, CredentialExpiredException {
    CARS_Main main = null;
    try {
      final String fet = _getFET( pContext );
      pContext.setAction( CARS_ActionContext.gDefActionPOST );
      main = createMain( pContext );      
      pContext.setMain( main );
      final String pathinfo = pContext.getPathInfo();
      if ((fet==null) || (fet.indexOf( "WRITE" )==-1)) {
        gEventManager.addEvent( main, main.getLoginUser(), null, null, "URL", "WRITE", "POST " + pathinfo );
      }
      if (pathinfo.lastIndexOf( '/' )==-1) {
        throw new PathNotFoundException( pathinfo );
      } else {
        // **** Store the given parameters
        JD_Taglist paramsTL = pContext.getQueryPartsAsTaglist();
        paramsTL = pContext.getParameterMapAsTaglist( paramsTL );
        final Node cnode = main.addNode( pathinfo, paramsTL, pContext.getBodyStream(), pContext.getBodyContentType() );
        pContext.setCreatedNode( cnode );
      }      
    } catch (CARS_CustomException cce) {
      gEventManager.addException( main, main.getLoginUser(), main.getCurrentViewNode(), null, CARS_EventManager.EVENTCAT_SYS, CARS_EventManager.EVENTTYPE_CREATE, cce, null );
    } catch (ItemExistsException iee) {
      pContext.setError( iee );
      pContext.setErrorCode( 1300 );
      gEventManager.addException( main, main.getLoginUser(), main.getCurrentViewNode(), null, CARS_EventManager.EVENTCAT_SYS, CARS_EventManager.EVENTTYPE_CREATE, iee, null );
    } catch (AccountLockedException ale) {
      pContext.setErrorCode( HttpURLConnection.HTTP_UNAUTHORIZED );
      pContext.setError( ale );
    } catch (AccessDeniedException ade) {
      gEventManager.addException( main, null, null, null, CARS_EventManager.EVENTCAT_SYS, CARS_EventManager.EVENTTYPE_LOGIN, ade, pContext.getPathInfo() );
      pContext.setErrorCode( HttpURLConnection.HTTP_FORBIDDEN );
      throw ade;
    } catch (CredentialExpiredException cee) {
      throw cee;
    } catch (PathNotFoundException pnfe) {
      pContext.setErrorCode( HttpURLConnection.HTTP_NOT_FOUND );
      pContext.setError( pnfe );
      gEventManager.addException( main, main.getLoginUser(), main.getCurrentViewNode(), null, CARS_EventManager.EVENTCAT_SYS, CARS_EventManager.EVENTTYPE_CREATE, pnfe, null );
    } catch (NoSuchNodeTypeException nsnte) {
      pContext.setErrorCode( HttpURLConnection.HTTP_NOT_FOUND );
      pContext.setError( nsnte );
      gEventManager.addException( main, main.getLoginUser(), main.getCurrentViewNode(), null, CARS_EventManager.EVENTCAT_SYS, CARS_EventManager.EVENTTYPE_CREATE, nsnte, null );
    } catch (ConstraintViolationException cve) {
      pContext.setErrorCode( HttpURLConnection.HTTP_NOT_ACCEPTABLE );
      pContext.setError( cve );
      gEventManager.addException( main, main.getLoginUser(), main.getCurrentViewNode(), null, CARS_EventManager.EVENTCAT_SYS, CARS_EventManager.EVENTTYPE_CREATE, cve, null );
    } catch (Exception e) {
//   e.printStackTrace();
      pContext.setErrorCode( HttpURLConnection.HTTP_INTERNAL_ERROR );
      gEventManager.addException( main, main.getLoginUser(), main.getCurrentViewNode(), null, CARS_EventManager.EVENTCAT_SYS, CARS_EventManager.EVENTTYPE_CREATE, e, null );
      pContext.setError( e );
//    } finally {
//      if (main!=null) {
//        try {
//          main.getSession().save();
//        } catch( Exception e ) {          
//        }
//      }
    }
    return;
  }
  
  
  /** Do PUT
   *
   * @param pContext
   * @param pMain
   * @throws Exception
   */
  public void performPutAction( final CARS_ActionContext pContext, final CARS_Main pMain ) throws AccessDeniedException, CredentialExpiredException {
    CARS_Main main = pMain;
    try {
//      main = createMain( new SimpleCredentials( pContext.getUsername(), pContext.getPassword() ), "default" );
      final String fet = _getFET( pContext );
      pContext.setAction( CARS_ActionContext.gDefActionPUT );
      if (main==null) {
        main = createMain( pContext );
        pContext.setMain( main );
      }
      final String pathinfo = pContext.getPathInfo();
      if ((fet==null) || (fet.indexOf( "WRITE" )==-1)) {
        gEventManager.addEvent( main, main.getLoginUser(), null, null, "URL", "WRITE", "PUT " + pathinfo );
      }
      if (pathinfo.lastIndexOf( '/' )!=-1) {
        // **** Store the given parameters
        final JD_Taglist paramsTL = pContext.getQueryPartsAsTaglist();
        final Node cnode = main.updateNode( pathinfo, paramsTL, pContext.getBodyStream(), pContext.getBodyContentType() );
        pContext.setThisNode( cnode );
      } else {
        throw new PathNotFoundException( pathinfo );
      }
    } catch (AccessDeniedException ade) {
      gEventManager.addException( main, null, null, null, "SYS", "LOGIN", ade, pContext.getPathInfo() );
      pContext.setErrorCode( HttpURLConnection.HTTP_FORBIDDEN );
      throw ade;
    } catch (AccountLockedException ale) {
      pContext.setErrorCode( HttpURLConnection.HTTP_UNAUTHORIZED );
      pContext.setError( ale );      
    } catch (CredentialExpiredException cee) {
      throw cee;
    } catch (PathNotFoundException pnfe) {
      gEventManager.addException( main, main.getLoginUser(), main.getCurrentViewNode(), null, "SYS", "UPDATE", pnfe, null );
      pContext.setError( pnfe );
      pContext.setErrorCode( HttpURLConnection.HTTP_NOT_FOUND );
    } catch (RepositoryException re) {
      gEventManager.addException( main, main.getLoginUser(), main.getCurrentViewNode(), null, "SYS", "UPDATE", re, null );
      pContext.setError( re );
      pContext.setErrorCode( HttpURLConnection.HTTP_INTERNAL_ERROR );
    } catch (Exception e) {
      gEventManager.addException( main, main.getLoginUser(), main.getCurrentViewNode(), null, "SYS", "UPDATE", e, null );
//      LOG.log( Level.INFO, null, e );
      pContext.setError( e );
      pContext.setErrorCode( HttpURLConnection.HTTP_INTERNAL_ERROR );
    } finally {
//      if (main!=null) {
//        try {
//          main.getSession().save();
//        } catch( Exception e ) {          
//        }
//     }
    }
    return;
  }
  
  /** Do DELETE
   * 
   * @param pContext
   * @throws java.lang.Exception
   */
  public void performDeleteAction( CARS_ActionContext pContext ) throws Exception {    
    CARS_Main main = null;
    try {
//      main = createMain( new SimpleCredentials( pContext.getUsername(), pContext.getPassword() ), "default" );
      pContext.setAction( CARS_ActionContext.gDefActionDELETE );
      main = createMain( pContext );      
      pContext.setMain( main );
      String pathinfo = pContext.getPathInfo();
      gEventManager.addEvent( main, main.getLoginUser(), null, null, "URL", "DELETE", "DELETE " + pathinfo );
      if (pathinfo.lastIndexOf( '/' )!=-1) {
        // **** Store the given parameters
        JD_Taglist paramsTL = pContext.getQueryPartsAsTaglist();
        main.removeNode( pathinfo, paramsTL );
      } else {
        throw new PathNotFoundException( pathinfo );
      }
    } catch (ReferentialIntegrityException rie) {
      gEventManager.addException( main, main.getLoginUser(), null, null, "SYS", "DELETE", rie, null );
      pContext.setError( rie );
      pContext.setErrorCode( HttpURLConnection.HTTP_FORBIDDEN );
    } catch (AccessDeniedException ade) {        
      gEventManager.addException( main, main.getLoginUser(), null, null, "SYS", "DELETE", ade, null );
      pContext.setErrorCode( HttpURLConnection.HTTP_FORBIDDEN );
      throw ade;
    } catch (AccountLockedException ale) {
      pContext.setErrorCode( HttpURLConnection.HTTP_UNAUTHORIZED );
      pContext.setError( ale );      
    } catch (CredentialExpiredException cee) {
      throw cee;
    } catch (PathNotFoundException pnfe) {
      gEventManager.addException( main, main.getLoginUser(), null, null, "SYS", "DELETE", pnfe, null );
      pContext.setErrorCode( HttpURLConnection.HTTP_NOT_FOUND );      
      pContext.setError( pnfe );
    } catch (Exception e) {
      gEventManager.addException( main, main.getLoginUser(), main.getCurrentViewNode(), null, "SYS", "DELETE", e, null );
//      LOG.log( Level.INFO, null, e );
      pContext.setError( e );
      pContext.setErrorCode( HttpURLConnection.HTTP_INTERNAL_ERROR );
    } finally {
//      if (main!=null) {
//        try {
//          main.getSession().save();
//          main.destroy();
//        } catch( Exception e ) {          
//        }
//      }
    }
    return;
  }
  
  
  
}
