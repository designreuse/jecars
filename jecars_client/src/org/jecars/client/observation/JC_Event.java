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
public interface JC_Event {

  static public enum TYPE { NODE_ADDED, NODE_CHANGED, PROPERTY_CHANGED };

  /** getDate
   * Returns the date when the change was persisted that caused this event.
   * 
   * @return
   */
  Calendar getDate();

  /** getIdentifier
   * Returns the identifier associated with this event or null if this event has no associated identifier.
   * @return the identifier associated with this event or null.
   */
  String getIdentifier();

  /** getPath
   * Returns the absolute path associated with this event or null if this event has no associated identifier.
   * @return
   */
  JC_Path getPath();

  /** getType
   * Returns the type of this event: an enum defined by this interface.
   * @return
   */
  TYPE getType();

}
