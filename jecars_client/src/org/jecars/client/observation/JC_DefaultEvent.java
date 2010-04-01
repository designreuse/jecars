/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jecars.client.observation;

import java.util.Calendar;
import org.jecars.client.JC_Path;

/**
 *
 * @author weert
 */
public class JC_DefaultEvent implements JC_Event {

  private final Calendar mDate;
  private final String   mId;
  private final JC_Path  mPath;
  private final TYPE     mType;

  /** JC_DefaultEvent
   *
   * @param pDate
   * @param pId
   * @param pPath
   * @param pType
   */
  public JC_DefaultEvent( final Calendar pDate, final String pId, final JC_Path pPath, final TYPE pType) {
    mDate = pDate;
    mId   = pId;
    mPath = pPath;
    mType = pType;
    return;
  }



  @Override
  public Calendar getDate() {
    return mDate;
  }

  @Override
  public String getIdentifier() {
    return mId;
  }

  @Override
  public JC_Path getPath() {
    return mPath;
  }

  @Override
  public TYPE getType() {
    return mType;
  }

  @Override
  public String toString() {
    return "Event: ID=" + mId + " at " + mPath + " on " + mDate.getTime() + " of type " + mType;
  }


}
