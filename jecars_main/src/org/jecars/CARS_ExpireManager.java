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

  static private final int   MIN_REMOVEDOBJECTS_FOR_LOG = 4;
  static private final long  MAX_ACCESSMANAGER_CACHE    = 1000000L;
  static private final long  CHECKEVERY                 = 30000L; // **** 30 seconds
  static private final int   DATASTORE_GC_TIMES         = 10;     // **** Datastore garbage collect every (10*30) seconds

  static private final Object    LOCK = new Object();

  private transient long        mLastExpireCheck    = 0L;
  private transient int         mDataStoreGCCurrent = 0;      // **** Datastore garbage collect every (10*30) seconds
  private transient Session     mSession            = null;

  /** Superclass must implement this method to actually start the tool
   */
  @Override
  protected void toolRun() throws Exception {
    super.toolRun();
    try {
      final Node n = getTool();
      mSession = n.getSession().getRepository().login( new SimpleCredentials( CARS_AccessManager.gSuperuserName, "".toCharArray() ));
      purge();
    } catch (ConstraintViolationException cve) {
      cve.printStackTrace();
      LOG.log( Level.SEVERE, cve.getMessage(), cve );
    } finally {
      mSession.logout();
      mSession = null;
    }
    return;
  }
  
  /** Creates a new instance of CARS_ExpireManager
   */
  public CARS_ExpireManager() {
    super();
    return;
  }
  
  /** shutdown the expire manager
   */
  public void shutdown() {
    try {
      setStateRequest( STATEREQUEST_ABORT );
    } catch (Exception e) {
      LOG.log( Level.WARNING, null, e );
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
  public NodeIterator getExpireNodes( final QueryManager pQM, final Calendar pTime ) throws InvalidQueryException, RepositoryException {
    final String qu = "SELECT * FROM jecars:root WHERE jecars:ExpireDate<= TIMESTAMP '" +
                 ISO8601.format(pTime) + "'";
    final Query q = pQM.createQuery( qu, Query.SQL );
    final QueryResult qr = q.execute();
    return qr.getNodes();    
  }
  
  /** Purge, search for objects which can be expired 
   *
   * @throws RepositoryException
   */
  private void purge() throws RepositoryException {
    if (mSession==null) {
      return;
    }
    if ((System.currentTimeMillis()-CHECKEVERY)>mLastExpireCheck) {
      try {
        synchronized( LOCK ) {
          if ((++mDataStoreGCCurrent)==DATASTORE_GC_TIMES) {
            try {
              final GarbageCollector gc = ((SessionImpl)mSession).createDataStoreGarbageCollector();
              gc.mark();
              final int du = gc.sweep();
              if (du>0) {
                LOG.info( "ExpireManager: Ready removing " + du + " datastore objects" );
              }
              // **** Check accessmanager cache size
              final long cs = CARS_AccessManager.getCacheSize();
              if (cs>MAX_ACCESSMANAGER_CACHE) {
                CARS_AccessManager.clearPathCache();
                LOG.info( "ExpireManager: Accessmanager cache CLEAR" );
              }
              mDataStoreGCCurrent = 0;
            } catch( NullPointerException npe) {
              LOG.log( Level.WARNING, "Garbage Collector", npe );
            }
          }
          
          CARS_Main main = null;
          mLastExpireCheck = System.currentTimeMillis();
          final Calendar c = Calendar.getInstance();
          final QueryManager qm = mSession.getWorkspace().getQueryManager();
          final NodeIterator ni = getExpireNodes( qm, c );
          if ((ni!=null) && (ni.hasNext())) {
            int totremoved = 0;
            while( ni.hasNext() ) {
              final Node en = ni.nextNode();
              try {
                if (en.getReferences().getSize()==0) {
                  if (main==null) {
                    main = CARS_Factory.getLastFactory().createMain( mSession );
                  }
                  final int removed = removeNodes( main, en, 0 );
                  if (removed>MIN_REMOVEDOBJECTS_FOR_LOG) {
                    LOG.log( Level.INFO, "ExpireManager: " + removed + " removed" );
                  }
                  totremoved += removed;
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
                // **** Catch general exception
                LOG.log( Level.WARNING, "ExpireManager", e );
              }
            }
            if (totremoved>MIN_REMOVEDOBJECTS_FOR_LOG) {
              LOG.log( Level.INFO, "ExpireManager: Ready removing " + totremoved + " objects." );
            }
          }
        }
      } finally {
      try {
        final Node tool = getTool();
        if (tool!=null) {
          tool.save();
          tool.setProperty( CARS_ActionContext.gDefLastAccessed, Calendar.getInstance() );
          if (tool.hasProperty( CARS_ActionContext.gDefExpireDate )) {
            tool.setProperty( CARS_ActionContext.gDefExpireDate, (Calendar)null );              
          }
          tool.getSession().save();
        }
      } catch( InvalidItemStateException iise ) {
        LOG.log( Level.WARNING, iise.getMessage(), iise );
      }
    }
    }
    return;
  }

  /** removeNodes
   *
   * @param pMain
   * @param pNode
   * @param pRemoved
   * @return
   * @throws Exception
   */
  private final int removeNodes( final CARS_Main pMain, final Node pNode, final int pRemoved ) throws Exception {
    int removed = pRemoved;
    if (pNode.hasNodes()) {
      Node n;
      final NodeIterator ni = pNode.getNodes();
      while (ni.hasNext()) {
        n = ni.nextNode();
        removed += removeNodes( pMain, n, pRemoved );
      }
    }
    try {
      pMain.removeNode( pNode.getPath(), null );
      removed++;
    } catch( PathNotFoundException pe ) {
      try {
        // **** Try a normal remove
        final Node parent = pNode.getParent();
        pNode.remove();
        parent.setProperty( CARS_ActionContext.DEF_MODIFIED, Calendar.getInstance() );
        parent.save();
        removed++;
      } catch( RepositoryException re) {
        re.printStackTrace();
      }
    }
    return removed;
  }

  /** isScheduledTool
   *
   * @return
   */
  @Override
  public boolean isScheduledTool() {
    return true;
  }

  /** getDelayInSecs
   *
   * @return
   */
  @Override
  public long getDelayInSecs() {
    return CHECKEVERY/1000;
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
