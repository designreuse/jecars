/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jecars.manager;

import org.jecars.client.JC_Nodeable;
import org.jecars.client.JC_Propertyable;

/**
 *
 * @author weert
 */
public class jecarsProperty extends jcrProperty {

  private final JC_Propertyable mProperty;

  public jecarsProperty( JC_Propertyable pProp ) {
    super( null );
    mProperty = pProp;
    return;
  }

  @Override
  public String getName() throws Exception {
    return mProperty.getName();
  }

  @Override
  public String getValueAsString() throws Exception {
    return mProperty.getValueString();
  }

  @Override
  public void setValue( final String pValue ) throws Exception {
    mProperty.setValue( pValue );
    return;
  }

  @Override
  public void save() throws Exception {
    mProperty.save();
  }


}
