/*
 * Copyright 2007-2009 NLR - National Aerospace Laboratory
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

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.logging.*;
import javax.jcr.LoginException;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import org.jecars.jaas.CARS_Credentials;

/**
 * CARS_EventManager
 *
 * @version $Id: CARS_EventManager.java,v 1.22 2009/06/08 09:55:29 weertj Exp $
 */
public class CARS_EventManager {

  static final protected Logger gLog = Logger.getLogger( "org.jecars" );

 /** - jecars:StoreEventsPer     (String)='HOUR' autocreated < '(NONE|YEAR|MONTH|DAY|HOUR|MINUTE)'
  *
  */
  static final public String SEP_NONE   = "NONE";
  static final public String SEP_YEAR   = "YEAR";
  static final public String SEP_MONTH  = "MONTH";
  static final public String SEP_DAY    = "DAY";
  static final public String SEP_HOUR   = "HOUR";
  static final public String SEP_MINUTE = "MINUTE";

 /**
   *            URL = Generated by URL based access
   *            APP = Generated by direct application access
   *            DIR = Generated by filebased backend
   *            DEF = Default
   *            SYS = System report
   */
  static final public String EVENTCAT_URL    = "URL";
  static final public String EVENTCAT_APP    = "APP";
  static final public String EVENTCAT_DIR    = "DIR";
  static final public String EVENTCAT_DEF    = "DEF";
  static final public String EVENTCAT_SYS    = "SYS";

  static final public String EVENTTYPE_LOGIN    = "LOGIN";
  static final public String EVENTTYPE_LOGOUT   = "LOGOUT";
  static final public String EVENTTYPE_READ     = "READ";
  static final public String EVENTTYPE_QUERY    = "QUERY";
  static final public String EVENTTYPE_WRITE    = "WRITE";
  static final public String EVENTTYPE_CREATE   = "CREATE";
  static final public String EVENTTYPE_MOVE     = "MOVE";
  static final public String EVENTTYPE_COPY     = "COPY";
  static final public String EVENTTYPE_DELETE   = "DELETE";
  static final public String EVENTTYPE_TRASHED  = "THRASHED";
  static final public String EVENTTYPE_CHECKIN  = "CHECKIN";
  static final public String EVENTTYPE_CHECKOUT = "CHECKOUT";
  static final public String EVENTTYPE_RESTORE  = "RESTORE";
  static final public String EVENTTYPE_UPDATE   = "UPDATE";
  static final public String EVENTTYPE_SEVERE   = "SEVERE";
  static final public String EVENTTYPE_WARNING  = "WARNING";
  static final public String EVENTTYPE_INFO     = "INFO";
  static final public String EVENTTYPE_CONFIG   = "CONFIG";
  static final public String EVENTTYPE_FINE     = "FINE";
  static final public String EVENTTYPE_FINER    = "FINER";
  static final public String EVENTTYPE_FINEST   = "FINEST";
  
  private Session           mSession = null;
  private int               mInUse   = 0;

  static final private Object EVENTLOCK = new Object();
  
  static private File              gEVENTLOGFILE  = new File( "jecars.log" );
  static final private Object       EVENTFILELOCK = new Object();
  static private boolean           gENABLELOG     = true;
  static private SimpleDateFormat  gLOGTIMEFORMAT = new SimpleDateFormat( "[dd/MMM/yyyy:HH:mm:ss Z]", Locale.US );


  /** Creates a new instance of CARS_EventManager
   */
  public CARS_EventManager() {
    return;
  }

  /** setEventLogFile
   * 
   * @param pLogFile
   */
  static public void setEventLogFile( final File pLogFile ) {
    gLog.info( "Setting event logfile to: " + pLogFile.getAbsolutePath() );
    gEVENTLOGFILE = pLogFile;
    return;
  }

  /** setEnableFileLog
   *
   * @param pEnable
   */
  static public void setEnableFileLog( final boolean pEnable ) {
    gLog.info( "Setting enable logfile: " + pEnable );
    gENABLELOG = pEnable;
    return;
  }

  /** setSession
   * 
   * @param pSession
   */
  public void setSession( Session pSession ) {
    mSession = pSession;
    return;
  }
   
  /** sessionLogout
   * 
   */
//  final public void sessionLogout() {
//    Session ses = mSession;
//    if (ses!=null) {
//      synchronized( ses ) {
//        mSession = null;
//        ses.logout();
//      }
//    }
//    return;
//  }

  /** returnSession
   * 
   * @param pSession
   */
  final private void returnSession( Session pSession ) {    
/*
      if (mSession!=null) {
      synchronized( mSession ) {
        mInUse--;
        if (mInUse<=0) {
          mInUse = 0;
          System.out.println( "Event session LOGOUT." );
          mSession.logout();
          mSession = null;
        }
      }
    }
 */
    return;
  }
  
  /** getSession
   * 
   * @return
   * @throws javax.jcr.LoginException
   * @throws javax.jcr.RepositoryException
   */
  final public Session getSession() throws LoginException, RepositoryException {
    if (mSession==null) {
      try {
        setSession( CARS_Factory.getRepository().login( new CARS_Credentials( CARS_AccessManager.gSuperuserName, "".toCharArray(), null )));
//        System.out.println( "Event session login..." + mInUse );
      } catch( LoginException e ) {
        e.printStackTrace();
      }
    }
    mInUse++;
    return mSession;
  }
  
  /** addEventReference
   * 
   * @param pEvent
   * @param pFolder
   * @return
   * @throws java.lang.Exception
   */
  public Node addEventReference( Node pEvent, String pFolder ) throws Exception {
    Node event;
    Session ses = getSession();
    if (ses==null) return null;
    try {
      synchronized( EVENTLOCK ) {
        Node ef = ses.getRootNode().getNode( "JeCARS/default/Events" );
        if (pFolder==null) {
          ef = ef.getNode( "System" );
        } else {
          ef = ef.getNode( pFolder );
        }
        long count = ef.getProperty( CARS_Main.DEFAULTNS + "EventsCount" ).getLong();
        event = CARS_DefaultMain.addNode( ef, pEvent.getName() + "_" + count, CARS_Main.DEFAULTNS + "EventReference" );
        ef.setProperty( CARS_Main.DEFAULTNS + "EventsCount", count+1 );
        event.setProperty( CARS_Main.DEFAULTNS + "EventPath", pEvent.getPath() );
        if (pEvent.hasProperty( CARS_Main.DEFAULTNS + "ExpireDate" )) {
          event.setProperty( CARS_Main.DEFAULTNS + "ExpireDate", pEvent.getProperty( CARS_Main.DEFAULTNS + "ExpireDate" ).getValue() );
        }
        ses.save();
  //      mSession.refresh( false );
      }
    } finally {
      returnSession( ses );
    }
    return event;
  }


  /** Retrieve and/or create the event store folder
   * @param pWhen
   * @param pExpire
   * @param pFolder
   * @return
   * @throws javax.jcr.RepositoryException
   */
  private Node getEventStoreFolder( final Calendar pWhen, final Calendar pExpire, final Node pFolder ) throws RepositoryException {
    Node storeEvents = pFolder;
    if (storeEvents.hasProperty( "jecars:StoreEventsPer" )) {
        final Calendar cal = Calendar.getInstance();
        final String storePer = storeEvents.getProperty( "jecars:StoreEventsPer" ).getString();
        if (SEP_YEAR.equals( storePer ) || SEP_MONTH.equals( storePer ) || SEP_DAY.equals( storePer ) || SEP_HOUR.equals( storePer ) || SEP_MINUTE.equals( storePer )) {
          final String path = String.valueOf( pWhen.get( Calendar.YEAR  )  );
          if (!storeEvents.hasNode( path )) {
            storeEvents.addNode( path, "jecars:EventsStoreFolder" );
            storeEvents.setProperty( CARS_ActionContext.DEF_MODIFIED, cal );
          }
          storeEvents = storeEvents.getNode( path );
          if (pExpire!=null) {
            if (storeEvents.hasProperty( CARS_ActionContext.gDefExpireDate )==false) {
              storeEvents.setProperty( CARS_ActionContext.gDefExpireDate, pExpire );
            } else {
              Calendar scal = storeEvents.getProperty( CARS_ActionContext.gDefExpireDate ).getDate();
              if (scal.before( pExpire )) storeEvents.setProperty( CARS_ActionContext.gDefExpireDate, pExpire );
            }
          }
        }
        if (SEP_MONTH.equals( storePer ) || SEP_DAY.equals( storePer ) || SEP_HOUR.equals( storePer ) || SEP_MINUTE.equals( storePer )) {
          final String path = String.valueOf( pWhen.get( Calendar.MONTH  )+1 );
          if (storeEvents.hasNode( path )==false) {
            storeEvents.addNode( path, "jecars:EventsStoreFolder" );
            storeEvents.setProperty( CARS_ActionContext.DEF_MODIFIED, cal );
          }
          storeEvents = storeEvents.getNode( path );
          if (pExpire!=null) {
            if (storeEvents.hasProperty( CARS_ActionContext.gDefExpireDate )==false) {
              storeEvents.setProperty( CARS_ActionContext.gDefExpireDate, pExpire );
            } else {
              Calendar scal = storeEvents.getProperty( CARS_ActionContext.gDefExpireDate ).getDate();
              if (scal.before( pExpire )) storeEvents.setProperty( CARS_ActionContext.gDefExpireDate, pExpire );
            }
          }
        }
        if (SEP_DAY.equals( storePer ) || SEP_HOUR.equals( storePer ) || SEP_MINUTE.equals( storePer )) {
          final String path = String.valueOf( pWhen.get( Calendar.DAY_OF_MONTH  )  );
          if (!storeEvents.hasNode( path )) {
            storeEvents.addNode( path, "jecars:EventsStoreFolder" );
            storeEvents.setProperty( CARS_ActionContext.DEF_MODIFIED, cal );
          }
          storeEvents = storeEvents.getNode( path );
          if (pExpire!=null) {
            if (storeEvents.hasProperty( CARS_ActionContext.gDefExpireDate )==false) {
              storeEvents.setProperty( CARS_ActionContext.gDefExpireDate, pExpire );
            } else {
              final Calendar scal = storeEvents.getProperty( CARS_ActionContext.gDefExpireDate ).getDate();
              if (scal.before( pExpire )) storeEvents.setProperty( CARS_ActionContext.gDefExpireDate, pExpire );
            }
          }
        }
        if (SEP_HOUR.equals( storePer ) || SEP_MINUTE.equals( storePer )) {
          final String path = String.valueOf( pWhen.get( Calendar.HOUR_OF_DAY )  );
          if (!storeEvents.hasNode( path )) {
            storeEvents.addNode( path, "jecars:EventsStoreFolder" );
            storeEvents.setProperty( CARS_ActionContext.DEF_MODIFIED, cal );
          }
          storeEvents = storeEvents.getNode( path );
          if (pExpire!=null) {
            if (storeEvents.hasProperty( CARS_ActionContext.gDefExpireDate )==false) {
              storeEvents.setProperty( CARS_ActionContext.gDefExpireDate, pExpire );
            } else {
              final Calendar scal = storeEvents.getProperty( CARS_ActionContext.gDefExpireDate ).getDate();
              if (scal.before( pExpire )) storeEvents.setProperty( CARS_ActionContext.gDefExpireDate, pExpire );
            }
          }
        }
        if (SEP_MINUTE.equals( storePer )) {
          final String path = String.valueOf( pWhen.get( Calendar.MINUTE )  );
          if (!storeEvents.hasNode( path )) {
            storeEvents.addNode( path, "jecars:EventsStoreFolder" );
            storeEvents.setProperty( CARS_ActionContext.DEF_MODIFIED, cal );
          }
          storeEvents = storeEvents.getNode( path );
          if (pExpire!=null) {
            if (storeEvents.hasProperty( CARS_ActionContext.gDefExpireDate )==false) {
              storeEvents.setProperty( CARS_ActionContext.gDefExpireDate, pExpire );
            } else {
              final Calendar scal = storeEvents.getProperty( CARS_ActionContext.gDefExpireDate ).getDate();
              if (scal.before( pExpire )) storeEvents.setProperty( CARS_ActionContext.gDefExpireDate, pExpire );
            }
          }
        }
      }
     return storeEvents;
   }

  /** addEvent
   * 
   * deprecated replace with addEvent( CARS_Main, ...... )
   * 
   */
  @Deprecated
  public Node addEvent( Node pUser, Node pSource, String pApplication, String pCategory, String pType, String pMessage ) throws Exception {
    return addEvent( null, pUser, pSource, pApplication, pCategory, pType, pMessage );
  }
  
  /** Add an event to the JeCARS repository
   *
   * @param pMain
   * @param pUser the logged in user which generates the event or null
   * @param pSource the "source" node, subject of the event or null
   * @param pApplication The folder under which the event will be added.
   *                     if null then "System/jecars:Events" + pType will be used
   * @param pCategory
   *            URL  = Generated by URL based access
   *            APP  = Generated by direct application access
   *            DIR  = Generated by filebased backend
   *            DEF  = Default
   *            SYS  = System report
   *            TOOL = Generate by a tool
   * @param pType the event type;
   *            LOGIN = Login event
   *            LOGOUT = Logout event
   *            READ = Read access (e.g. GET HTTP calls)
   *            QUERY = Query calls
   *            WRITE = Node binary write operation (streams)
   *            CREATE = Node creation (e.g. POST HTTP calls)
   *            MOVE = Node move operations
   *            COPY = Node copy operations
   *            DELETE = Deletions (e.g. DELETE HTTP calls)
   *            TRASHED = Node trashing
   *            CHECKIN = Node version checkin
   *            CHECKOUT = Node version checkout
   *            RESTORE = Node version restore operations
   *            UPDATE = Node updates (e.g. PUT HTTP calls)
   *            SEVERE,WARNING,INFO,CONFIG,FINE,FINER,FINEST = Level.* log events
   * @param pMessage the event message
   * @param pEventType the type of the node (eg. jecars:Event)
   * @return the create event node
   */
  public Node addEvent( final CARS_Main pMain,   final Node pUser,   final Node pSource, final String pApplication,
                        final String pCategory,  final String pType, final String pMessage,
                        final String pEventType, final String pBody ) throws RepositoryException, UnsupportedEncodingException {
    final Node event;
    synchronized( EVENTLOCK ) {
      final Session ses = getSession();
      if (ses==null) return null;
      try {
        final Node ef = ses.getRootNode().getNode( "JeCARS/default/Events" );
        event = addEvent( ef, pMain, pUser, pSource, pApplication, pCategory, pType, pMessage, pEventType, pBody );
        ses.save();
        ses.refresh( false );
      } finally {
        returnSession( ses );
      }
    }
    return event;
  }

  /** addEvent
   *
   * @param pEventFolder
   * @param pMain
   * @param pUser
   * @param pSource
   * @param pApplication
   * @param pCategory
   * @param pType
   * @param pMessage
   * @param pEventType
   * @param pBody
   * @return
   * @throws RepositoryException
   * @throws UnsupportedEncodingException
   */
  public Node addEvent( final Node pEventFolder, final CARS_Main pMain, final Node pUser, final Node pSource,
                        final String pApplication, final String pCategory, final String pType,
                        final String pMessage, final String pEventType, final String pBody ) throws RepositoryException, UnsupportedEncodingException  {
    final Node event = createEventNode( pMain, pEventFolder, pUser, pSource, pApplication, pCategory, pType, pMessage, pEventType );
    if (pBody!=null) {
      event.setProperty( "jecars:Body", pBody );
    }

    addLogEntry( pMain, pUser, pSource, pApplication, pCategory, pType, null, pMessage );
    return event;
  }

  /** addEvent
   * 
   * @param pMain
   * @param pUser
   * @param pSource
   * @param pApplication
   * @param pCategory
   * @param pType
   * @param pMessage
   * @return
   * @throws java.lang.Exception
   */
  public Node addEvent( CARS_Main pMain, Node pUser, Node pSource, String pApplication, String pCategory, String pType, String pMessage ) throws Exception {
    return addEvent( pMain, pUser, pSource, pApplication, pCategory, pType, pMessage, CARS_Main.DEFAULTNS + "Event", null );
  }

  /** addEvent
   * 
   * @param pMain
   * @param pUser
   * @param pSource
   * @param pApplication
   * @param pCategory
   * @param pType
   * @param pMessage
   * @param pEventType
   * @return
   * @throws java.lang.Exception
   */
  public Node addEvent( final CARS_Main pMain, final Node pUser, final Node pSource, final String pApplication, final String pCategory, final String pType, final String pMessage, final String pEventType ) throws Exception {
    return addEvent( pMain, pUser, pSource, pApplication, pCategory, pType, pMessage, pEventType, null );
  }

  /** Create the event object
   * helper method
   *
   * @param pMain
   * @param pFolder
   * @param pUser
   * @param pSource
   * @param pApplication
   * @param pCategory
   * @param pType
   * @param pMessage
   * @param pEventType
   * @return
   * @throws RepositoryException
   * @throws UnsupportedEncodingException
   */
  public Node createEventNode( final CARS_Main pMain, Node pFolder, final Node pUser, final Node pSource, final String pApplication, final String pCategory, final String pType, String pMessage,
                               final String pEventType ) throws RepositoryException, UnsupportedEncodingException {
      Node event;
      synchronized( EVENTLOCK ) {
        if (pApplication==null) {
          pFolder = pFolder.getNode( "System/jecars:Events" + pType );
        } else {
          if (pApplication.startsWith( "/" )) {
            pFolder = pFolder.getSession().getRootNode().getNode( pApplication.substring(1) );
          } else {
            pFolder = pFolder.getNode( pApplication );
          }
        }
        final Calendar now = Calendar.getInstance();
        Calendar cal = Calendar.getInstance();
        final long expireValue = pFolder.getProperty( CARS_Main.DEFAULTNS + "ExpireHour" + pType ).getLong();
        if (expireValue<0) {
          // **** No expire date
          cal = null;
        } else {
          cal.add( Calendar.HOUR, (int)expireValue );
        }
        final Node storeEvents = getEventStoreFolder( now, cal, pFolder );
        final long count = pFolder.getProperty( CARS_Main.DEFAULTNS + "EventsCount" ).getLong();
        if (pEventType==null) {
          event = CARS_DefaultMain.addNode( storeEvents, pCategory + "_" + pType + "_" + count, CARS_Main.DEFAULTNS + "Event" );
        } else {
          event = CARS_DefaultMain.addNode( storeEvents, pCategory + "_" + pType + "_" + count, pEventType );    // **** Tracker 2542920
        }
        pFolder.setProperty( CARS_Main.DEFAULTNS + "EventsCount", count+1 );
        event.setProperty( CARS_Main.DEFAULTNS + "Category", pCategory );
        if (pSource!=null) event.setProperty( CARS_Main.DEFAULTNS + "Source", pSource.getPath() );
        if (pUser!=null)   event.setProperty( CARS_Main.DEFAULTNS + "User", pUser.getPath() );
        event.setProperty( CARS_Main.DEFAULTNS + "Type", pType );
        event.setProperty( CARS_Main.DEFAULTNS + "ExpireDate", cal );
        if (pMessage!=null) {
          event.setProperty( CARS_Main.DEFAULTNS + "Title", pMessage );
          event.setProperty( CARS_Main.DEFAULTNS + "Body", pMessage );
        }
        if (pMain!=null) {
          final CARS_ActionContext ac = pMain.getContext();
          if (ac!=null) {
            final String id = ac.getQueryValueResolved( "jecars:EventCollectionID" );
            if (id!=null) {
              event.setProperty( "jecars:EventCollectionID", id ); // **** First char is a '='
            }
          }
        }
      }
    return event;
  }

  /** addException
   *
   * deprecated replace with addException( CARS_Main, ...... )
   * 
   */
  @Deprecated
  public void addException( Node pUser, Node pSource, String pApplication, String pCategory, String pType, Throwable pThrow, String pMessage ) {
    addException( null, pUser, pSource, pApplication, pCategory, pType, pThrow, pMessage );
  }
  
  /** Add an exception to the JeCARS repository
   *
   * @param pMain the main context
   * @param pUser the logged in user which generates the event or null
   * @param pSource the "source" node, subject of the event or null
   * @param pApplication The folder under which the event will be added.
   *                     if null then "System/jecars:Events" + pType will be used
   * @param pCategory
   *            URL = Generated by URL based access
   *            APP = Generated by direct application access
   *            DIR = Generated by filebased backend
   *            DEF = Default
   *            SYS = System report
   * @param pType the event type;
   *            LOGIN = Login event
   *            LOGOUT = Logout event
   *            READ = Read access (e.g. GET HTTP calls)
   *            QUERY = Query calls
   *            WRITE = Node binary write operation (streams)
   *            CREATE = Node creation (e.g. POST HTTP calls)
   *            MOVE = Node move operations
   *            COPY = Node copy operations
   *            DELETE = Deletions (e.g. DELETE HTTP calls)
   *            TRASHED = Node trashing
   *            CHECKIN = Node version checkin
   *            CHECKOUT = Node version checkout
   *            RESTORE = Node version restore operations
   *            UPDATE = Node updates (e.g. PUT HTTP calls)
   *            SEVERE,WARNING,INFO,CONFIG,FINE,FINER,FINEST = Level.* log events
   * @param pThrowable the throwable
   * @param pMessage the event message
   * @return the create event node
   */  
  public void addException( CARS_Main pMain, Node pUser, Node pSource, String pApplication, String pCategory, String pType, Throwable pThrow, String pMessage ) {
//  System.out.println( "ADD EXCEPT: " + pType + " :: " + pMessage );
      try {
        Session ses = getSession();
        if (ses==null) return;
        synchronized( EVENTLOCK ) {
          try {
            Node ef = ses.getRootNode().getNode( "JeCARS/default/Events" );
            Node event = createEventNode( pMain, ef, pUser, pSource, pApplication, pCategory, pType, pMessage, CARS_Main.DEFAULTNS + "Exception" );
            if (pThrow!=null) {
              if (pMessage==null) event.setProperty( CARS_Main.DEFAULTNS + "Title", pThrow.getMessage() );
              StringWriter sw = new StringWriter();
              PrintWriter pw = new PrintWriter(sw);
              pThrow.printStackTrace(pw);
              event.setProperty( CARS_Main.DEFAULTNS + "Body", sw.getBuffer().toString() );
              ByteArrayOutputStream dos = new ByteArrayOutputStream();
              ObjectOutputStream oos = new ObjectOutputStream( dos );
              oos.writeObject( pThrow );
              oos.close();
              dos.close();
              ByteArrayInputStream bais = new ByteArrayInputStream( dos.toByteArray() );
              event.setProperty( CARS_Main.DEFAULTNS + "Exception", bais );
              bais.close();
            }
            ses.save();

            addLogEntry( pMain, pUser, pSource, pApplication, pCategory, pType, pThrow, pMessage );

          } catch( Exception e ) {
            ses.refresh( false );
          } finally {
            returnSession( ses );
          }
        }
      } catch (Exception e) {
        if (CARS_Factory.getLastFactory()!=null) {
          e.printStackTrace();
        }
//      LOG.log( Level.SEVERE, null, e );
      }
    return;
  }

  /** processLogRequest
   *
   * @param pRequest
   * @return
   */
  static private String processLogRequest( final String pRequest ) {
    if ((pRequest!=null) &&
        ((pRequest.startsWith( "GET " )) ||
         (pRequest.startsWith( "HEAD " )) ||
         (pRequest.startsWith( "PUT " )) ||
         (pRequest.startsWith( "DELETE " )) ||
         (pRequest.startsWith( "POST " )))) {
      return pRequest + " HTTP/1.0";
    }
    return  "GET " + pRequest + " HTTP/1.0";
  }

  /** addLogEntry
   *
   * @param pMain
   * @param pUser
   * @param pSource
   * @param pApplication
   * @param pCategory
   * @param pType
   * @param pThrow
   * @param pMessage
   * @throws RepositoryException
   */
  public void addLogEntry( final CARS_Main pMain, final Node pUser, final Node pSource,
                        final String pApplication, final String pCategory, final String pType,
                        final Throwable pThrow, final String pMessage ) throws RepositoryException {
    if (gENABLELOG) {
      try {
        final String rh, userAgent, referer;
        final int code;
        final CARS_ActionContext ac;
        if (pMain==null) {
          ac = null;
        } else {
          ac = pMain.getContext();
        }
        if (ac==null) {
          referer = "-";
          userAgent = "-";
          rh = "jecars.org";
          code = 200;
        } else {
          referer = ac.getReferer();
          userAgent = ac.getUserAgent();
          rh   = ac.getRemoteHost();
          code = ac.getErrorCode();
        }
        if (pUser==null) {
          if (pSource==null) {
            addLogEntry( rh, "-", "-", processLogRequest( pMessage ), code, 0, referer, userAgent );
          } else {
            addLogEntry( rh, "-", "-", processLogRequest( pMessage ), code, 0, referer, userAgent );
          }
        } else {
          if (pSource==null) {
            addLogEntry( rh, "-", pUser.getName(), processLogRequest( pMessage ), code, 0, referer, userAgent );
          } else {
            addLogEntry( rh, "-", pUser.getName(), processLogRequest( pMessage ), code, 0, referer, userAgent );
          }
        }
      } catch(IOException ie) {
      }
    }
    return;
  }

  /** addLogEntry
   *
   * @param pClient
   * @param pIdentd
   * @param pUserId
   * @param pRequest
   * @param pResponse
   * @param pSize
   * @param pReferrer
   * @param pUserAgent
   * @throws IOException
   */
  static public void addLogEntry(
                        final String pClient,   final String pIdentd,  final String pUserId,
                        final String pRequest,  final int pResponse,   final int pSize,
                        final String pReferrer, final String pUserAgent ) throws IOException {
    synchronized( EVENTFILELOCK ) {
      FileOutputStream fos = null;
      try {
        fos = new FileOutputStream( gEVENTLOGFILE, true );
        final String line = pClient + " " + pIdentd + " " + pUserId + " " +
                      gLOGTIMEFORMAT.format(new Date()) + " " +
                      "\"" + pRequest + "\" " + pResponse + " " + pSize + " " +
                      "\"" + pReferrer + "\" " + "\"" + pUserAgent + "\"\n";
        fos.write( line.getBytes() );
        fos.flush();
      } catch (Exception e) {
        gLog.log( Level.SEVERE, null, e );
      } finally {
        if (fos!=null) {
          fos.close();
        }
      }
    }
    return;
  }



}
