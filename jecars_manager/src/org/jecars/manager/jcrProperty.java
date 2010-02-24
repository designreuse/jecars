/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jecars.manager;

import javax.jcr.Property;
import javax.jcr.ValueFormatException;

/**
 *
 * @author weert
 */
public class jcrProperty {

  private final Property mProperty;

  public jcrProperty(Property pProperty) {
    mProperty = pProperty;
    return;
  }

  public String getName() throws Exception {
    return mProperty.getName();
  }

  public String getValueAsString() throws Exception {
    try {
      return mProperty.getString();
    } catch( Throwable vfe ) {
      return vfe.getMessage();
    }
  }

  public void setValue( final String pValue ) throws Exception {
    mProperty.setValue( pValue );
    return;
  }

  public void save() throws Exception {
    mProperty.save();
  }

}
