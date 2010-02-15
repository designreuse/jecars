/*
 * Copyright 2007-2008 NLR - National Aerospace Laboratory
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

import java.util.Calendar;
import java.util.logging.Level;
import javax.jcr.*;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.query.*;
import org.apache.jackrabbit.core.SessionImpl;
import org.apache.jackrabbit.core.data.GarbageCollector;
import org.apache.jackrabbit.core.state.NoSuchItemStateException;
import org.apache.jackrabbit.util.ISO8601;
import org.jecars.tools.CARS_DefaultToolInterface;

/** CARS_ExpireManager
 *
 * @version $Id: CARS_ExpireManager.java,v 1.32 2009/06/05 14:42:38 weertj Exp $
 */
public class CARS_ExpireManager extends CARS_DefaultToolInterface {

  private long    mLastExpireCheck    = 0L;
  private long    mCheckEvery         = 30000L; // **** 30 seconds
  private long    mPurgeSizeLimit     = 1000000L;
  private int     mDataStoreGCTimes   = 10;     // **** Datastore garbage collect every (10*30) seconds
  private int     mDataStoreGCCurrent = 0;      // **** Datastore garbage collect every (10*30) seconds
  private Session mSession            = null;
  static final private Object    LOCK = new Object();

  /** Superclass must implement this method to actually start the tool
   */
  @Override
  protected void toolRun() throws Exception {
    super.toolRun();
    try {
      Node n = getTool();
      mSession = n.getSession().getRepository().login( new SimpleCredentials( CARS_AccessManager.gSuperuserName, "".toCharArray() ));
      purge();
    } catch (ConstraintViolationException cve) {
       cve.printStackTrace();
    } finally {
      mSession.logout();
      mSession = null;
    }
    return;
  }
  
  /** Creates a new instance of CARS_ExpireManager
   */
  public CARS_ExpireManager() {
    return;
  }
  
  /** shutdown the expire manager
   */
  public void shutdown() {
    try {
      setStateRequest( STATEREQUEST_ABORT );
    } catch (Exception e) {
      gLog.log( Level.WARNING, null, e );
    }
    if (mSession!=null) {        
      mSession.logout();
      mSession = null;
    }
    return;
  }
  
  /** getExpireNodes
   * @param pQM
   * @param pTime
   * @return
   * @throws javax.jcr.query.InvalidQueryException
   * @throws javax.jcr.RepositoryException
   */
  public NodeIterator getExpireNodes( QueryManager pQM, Calendar pTime ) throws InvalidQueryException, RepositoryException {
    String qu = "SELECT * FROM jecars:root WHERE jecars:ExpireDate<= TIMESTAMP '" +
                 ISO8601.format(pTime) + "'";
    Query q = pQM.createQuery( qu, Query.SQL );        
    QueryResult qr = q.execute();
    return qr.getNodes();    
  }
  
  /** Purge, search for objects which can be expired 
   */
  private void purge() throws Exception {
    if (mSession==null) return;
    if ((System.currentTimeMillis()-mCheckEvery)>mLastExpireCheck) {
      try {
      synchronized( LOCK ) {
        if ((++mDataStoreGCCurrent)==mDataStoreGCTimes) {
          try {
            GarbageCollector gc = ((SessionImpl)mSession).createDataStoreGarbageCollector();
            gc.scan();
            gc.stopScan();
            int du = gc.deleteUnused();
            if (du>0) {
              gLog.info( " *** EXPIRING Datastore " + du + " objects" );
            }
            // **** Check accessmanager cache size
            long cs = CARS_AccessManager.getCacheSize();
  //          gLog.info( " *** Accessmanager cache size = " + cs );
            if (cs>1000000) {
              CARS_AccessManager.clearPathCache();
              gLog.info( " *** Accessmanager CLEAR" );
            }
//          mGarbageCollector = null;
            mDataStoreGCCurrent = 0;
          } catch( NullPointerException npe) {
            gLog.log( Level.WARNING, "Garbage Collector", npe );
          }
        }
          
        CARS_Main main = null;
        mLastExpireCheck = System.currentTimeMillis();      
        Calendar c = Calendar.getInstance();
        QueryManager qm = mSession.getWorkspace().getQueryManager();
        long purgeSize = mPurgeSizeLimit+1, purgeTodo = -1;
        NodeIterator ni = null;
        boolean started = true;
        int timeDiff = -24;
        while( (purgeSize>mPurgeSizeLimit) && (timeDiff!=0) ) {
          ni = getExpireNodes( qm, c );
          purgeSize = ni.getSize();
//  System.out.println( "ExpireManager: d1 -  = " + purgeSize );
          if (purgeTodo==-1) purgeTodo = purgeSize;
          if (started==true) {
//            System.out.println( " *** EXPIRING (TODO) " + ni.getSize() + " OBJECTS " );
            if (purgeSize>4) {
              gLog.info( " *** EXPIRING (TODO) " + purgeSize + " OBJECTS " );
            } else {
              return;
            }
          }
          if ((purgeSize==0) && (started==true)) return;
          started = false;
          if (purgeSize<=4) {
            if (timeDiff==-1) {
              while( purgeSize<mPurgeSizeLimit ) {
                c.add( Calendar.MINUTE, 1  );
                ni = getExpireNodes( qm, c );
                purgeSize = ni.getSize();
//     System.out.println( "d - " + sdf.format( c.getTime() ) + " = " + purgeSize );
                if (purgeSize>purgeTodo) {
                  c.add( Calendar.MINUTE, -1 );
                  ni = getExpireNodes( qm, c );
                  break;
                }
              }
              break;
            } else {
              if (timeDiff==1) {
                timeDiff = -1;                
              } else {
                if (timeDiff<0) {
                  timeDiff = -(timeDiff/2);
                } else {
                  timeDiff = (timeDiff/2);                  
                }
              }
              c.add( Calendar.HOUR_OF_DAY, timeDiff  );
              purgeSize = mPurgeSizeLimit+1;
            }
          } else {
            c.add( Calendar.HOUR_OF_DAY, timeDiff  );
            if (c.after( Calendar.getInstance() )) {
              timeDiff = -(timeDiff/2);              
            }
//         SimpleDateFormat sdf = new SimpleDateFormat();
//         System.out.println( "time diff = " + sdf.format( c.getTime()) + " :: " + timeDiff );
          }
        }
//        if ((ni!=null) && (ni.getSize()>4)) {
        if (ni!=null) {
          long expObjs = ni.getSize();
          gLog.info( " *** EXPIRING " + expObjs + " OBJECTS " );
          Node en;
          while( ni.hasNext() ) {
            en = ni.nextNode();
            try {
              if (en.getReferences().getSize()==0) {
                if (main==null) {
                  main = CARS_Factory.getLastFactory().createMain( mSession );
                }
                removeNodes( main, en );
//                main.removeNode( en.getPath(), null );
              } else {
                // **** There are still references
              }
            } catch( NoSuchItemStateException nsise ) {
              // **** The node is already removed
            } catch( InvalidItemStateException iise ) {
              // **** The node is already removed
//            } catch( NoSuchItemStateException nsise ) {              
              // **** 
            } catch( ItemNotFoundException infe ) {
              // ****
            } catch( Exception e ) {
// e.printStackTrace();                
              // **** Catch general exception
              gLog.log( Level.WARNING, "ExpireManager", e );
            }
          }
          if (expObjs>100) gLog.info( " *** READY EXPIRING " + expObjs + " OBJECTS " );
//          getTool().setProperty( "jecars:LastAccessed", Calendar.getInstance() );
//          mSession.save();
        }
      }
//      } catch( Exception e ) {
//        // **** Catch general exception
//        gLog.log( Level.WARNING, "ExpireManager", e );
      } finally {
        try {
          Node tool = getTool();
          if (tool!=null) {
            tool.save();
            tool.setProperty( CARS_ActionContext.gDefLastAccessed, Calendar.getInstance() );
            if (tool.hasProperty( CARS_ActionContext.gDefExpireDate )==true) {
              tool.setProperty( CARS_ActionContext.gDefExpireDate, (Calendar)null );              
            }
            tool.getSession().save();
//            tool.save();
          }
        } catch( InvalidItemStateException iise ) {
          gLog.log( Level.WARNING, iise.getMessage(), iise );
        }
//        mSession.save();
//        mSession.refresh( false );
//        mSession.getRootNode().getNode( "JeCARS" ).unlock();        
      }
    }
    return;
  }

  /** removeNodes
   *
   * @param pMain
   * @param pNode
   * @throws java.lang.Exception
   */
  private final void removeNodes( final CARS_Main pMain, final Node pNode ) throws Exception {
    if (pNode.hasNodes()) {
      Node n;
      final NodeIterator ni = pNode.getNodes();
      while (ni.hasNext()) {
        n = ni.nextNode();
        removeNodes( pMain, n );
      }
    }
    try {
//   System.out.println("REMOVE " + pNode.getPath() );
      pMain.removeNode( pNode.getPath(), null );
    } catch( PathNotFoundException pe ) {
      try {
        // **** Try a normal remove
        Node parent = pNode.getParent();
        pNode.remove();
        parent.setProperty( CARS_ActionContext.gDefModified, Calendar.getInstance() );
        parent.save();
      } catch( RepositoryException re) {
        re.printStackTrace();
      }
    }
    return;
  }

  @Override
  public boolean isScheduledTool() {
    return true;
  }

  @Override
  public long getDelayInSecs() {
//    return mCheckEverySecs;
    return mCheckEvery/1000;
  }

  /** createToolSession
   *
   * @return
   * @throws java.lang.Exception
   */
  @Override
  public SessionImpl createToolSession() throws Exception {
    return (SessionImpl)getTool().getSession().getRepository().login( new SimpleCredentials( CARS_AccessManager.gSuperuserName, "".toCharArray() ));
  }


}
