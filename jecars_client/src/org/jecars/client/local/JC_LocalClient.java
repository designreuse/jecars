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

package org.jecars.client.local;

import javax.jcr.AccessDeniedException;
import javax.jcr.SimpleCredentials;
import org.jecars.CARS_ActionContext;
import org.jecars.CARS_Factory;
import org.jecars.CARS_Main;
import org.jecars.client.JC_DefaultClient;
import org.jecars.client.JC_Exception;
import org.jecars.client.JC_Factory;
import org.jecars.client.JC_RESTComm;

/** JC_LocalClient
 * $LastChangedDate$
 * $Id: JC_LocalClient.java 133 2010-01-04 15:58:21Z weertj $
 */
public class JC_LocalClient extends JC_DefaultClient {

  static private CARS_Factory gFactory = null;

  private CARS_Main              mMain = null;
  private final JC_LocalRESTComm mRESTComm;

  static public final String JECARSLOCAL = "http://jecarslocal";

  /** JC_LocalClient
   * 
   * @throws Exception
   */
  public JC_LocalClient() throws Exception {
    super();
    initJeCARS();
    mRESTComm = new JC_LocalRESTComm( this );
    return;
  }

  /** initJeCARS
   *
   * @throws Exception
   */
  private final void initJeCARS() throws Exception {
    if (gFactory==null) {
      gFactory = new CARS_Factory();
      gFactory.init( null, false );
      CARS_ActionContext.gUntransport = false; // **** No decode because of local use
    }
    return;
  }


  /** getRESTComm
   *
   * @return
   */
  @Override
  public JC_RESTComm getRESTComm() {
    return mRESTComm;
  }

  /** isLocalClient
   *
   * @return
   */
  @Override
  public boolean isLocalClient() {
    return true;
  }

  /** getMain
   *
   * @return
   * @throws AccessDeniedException
   */
  protected CARS_Main getMain() throws AccessDeniedException {
    if (mMain==null) {
      mMain = gFactory.createMain( new SimpleCredentials( mRESTComm.getUsername(), mRESTComm.getPassword() ), "" );
    }
    return mMain;
  }

}
