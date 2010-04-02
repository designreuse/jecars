/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jecars.tools;

import java.util.ArrayList;
import java.util.List;
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

public final class CARS_ToolSignalManager {

  static private final List<CARS_ToolSignalListener> TOOLSIGNALLISTENERS = new ArrayList<CARS_ToolSignalListener>();

  /** CARS_ToolSignalManager
   *
   */
  private CARS_ToolSignalManager() {
    return;
  }

  /** addToolEventListener
   *
   * @param pTEL
   */
  static public void addToolSignalListener( final CARS_ToolSignalListener pTEL ) {
    synchronized( TOOLSIGNALLISTENERS ) {
      if (!TOOLSIGNALLISTENERS.contains( pTEL )) {
        TOOLSIGNALLISTENERS.add( pTEL );
      }
    }
    return;
  }

  /** removeToolSignalListener
   *
   * @param pTEL
   */
  static public void removeToolSignalListener( final CARS_ToolSignalListener pTEL ) {
    synchronized( TOOLSIGNALLISTENERS ) {
      TOOLSIGNALLISTENERS.remove( pTEL );
    }
    return;
  }

  /** sendSignal
   *
   * @param pToolPath
   * @param pSignal
   */
  static public void sendSignal( final String pToolPath, final CARS_ToolSignal pSignal ) {
    synchronized( TOOLSIGNALLISTENERS ) {
      for( final CARS_ToolSignalListener tel : TOOLSIGNALLISTENERS ) {
        tel.signal( pToolPath, pSignal );
      }
    }
  }

}
