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
package org.jecars.tools;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;
import java.util.logging.Level;

/** CARS_SimpleCalcTool
 * 
 * @version $Id: CARS_SimpleCalcTool.java,v 1.5 2009/03/25 13:20:32 weertj Exp $
 */
public class CARS_SimpleCalcTool extends CARS_DefaultToolInterface {

  private Collection<Double> mResults = new ArrayList<Double>();
  
  /** toolInit
   * @throws java.lang.Exception
   */
  @Override
  protected void toolInit() throws Exception {
    System.out.println( " SIMPLE CALC TOOL INIT" );
    mResults.clear();
    super.toolInit();
    return;
  }
  
  /** toolRun
   * @throws java.lang.Exception
   */
  @Override
  protected void toolRun() throws Exception {
      
    System.out.println( " SIMPLE CALC TOOL RUN" );
    
    Collection<InputStream> inputs = (Collection<InputStream>)getInputsAsObject( InputStream.class, null );
    if (inputs.size()==0) {
      throw new Exception( "No INPUT" );
    }
    for (InputStream inputStream : inputs) {
      Properties props = new Properties();
      props.load( inputStream );
      double no1 = Double.parseDouble( props.getProperty( "No1" ) );
      double no2 = Double.parseDouble( props.getProperty( "No2" ) );
      
      double compl = 10;

//      while( (compl<=100) && (!getFuture().isCancelled()) ) {
      while( (compl<=100) && (!STATEREQUEST_STOP.equals(getStateRequest())) ) {
        reportMessage( Level.INFO, "Tool completion = " + compl + "%\n", false );
        reportOutput( "(OUTPUT) Tool completion = " + compl + "%\n" );
        reportProgress( compl/100.0 );
        getTool().setProperty( "jecars:Body", "<HTML>Een simpele test tool: " + compl + "% completed\n</HTML>" );
        getTool().save();
        Thread.sleep( 1000 );
        compl += 10;
      }

      if (STATEREQUEST_STOP.equals(getStateRequest())) {
        // **** We are cancelled
        reportMessage( Level.WARNING, "Tool is cancelled", false );
        setState( STATE_OPEN_ABORTING );
      }


/*
      reportMessage( Level.INFO, "Tool completion = " + compl + "%\n", false );
      reportOutput( "(OUTPUT) Tool completion = " + compl + "%\n" );
      reportProgress( compl/100.0 );
      getTool().setProperty( "jecars:Body", "<HTML>Een simpele test tool: " + compl + "% completed\n</HTML>" );      
      getTool().save();
      Thread.sleep( 1000 );
      compl += 10;
      reportMessage( Level.INFO, "Tool completion = " + compl + "%\n", false );
      reportOutput( "(OUTPUT) Tool completion = " + compl + "%\n" );
      reportProgress( compl/100.0 );
      getTool().setProperty( "jecars:Body", "<HTML>Een simpele test tool: " + compl + "% completed\n</HTML>" );      
      getTool().save();
      Thread.sleep( 1000 );
      compl += 10;
      reportMessage( Level.INFO, "Tool completion = " + compl + "%\n", false );
      reportOutput( "(OUTPUT) Tool completion = " + compl + "%\n" );
      reportProgress( compl/100.0 );
      getTool().setProperty( "jecars:Body", "<HTML>Een simpele test tool: " + compl + "% completed\n</HTML>" );      
      getTool().save();
      Thread.sleep( 1000 );
      compl += 10;
      reportMessage( Level.INFO, "Tool completion = " + compl + "%\n", false );
      reportOutput( "(OUTPUT) Tool completion = " + compl + "%\n" );
      reportProgress( compl/100.0 );
      getTool().setProperty( "jecars:Body", "<HTML>Een simpele test tool: " + compl + "% completed\n</HTML>" );      
      getTool().save();
      Thread.sleep( 1000 );
      compl += 10;
      reportMessage( Level.INFO, "Tool completion = " + compl + "%\n", false );
      reportOutput( "(OUTPUT) Tool completion = " + compl + "%\n" );
      reportProgress( compl/100.0 );
      getTool().setProperty( "jecars:Body", "<HTML>Een simpele test tool: " + compl + "% completed\n</HTML>" );      
      getTool().save();
      Thread.sleep( 1000 );
      compl += 10;
      reportMessage( Level.INFO, "Tool completion = " + compl + "%\n", false );
      reportOutput( "(OUTPUT) Tool completion = " + compl + "%\n" );
      reportProgress( compl/100.0 );
      getTool().setProperty( "jecars:Body", "<HTML>Een simpele test tool: " + compl + "% completed\n</HTML>" );      
      getTool().save();
      Thread.sleep( 1000 );
      compl += 10;
      reportMessage( Level.INFO, "Tool completion = " + compl + "%\n", false );
      reportOutput( "(OUTPUT) Tool completion = " + compl + "%\n" );
      reportProgress( compl/100.0 );
      getTool().setProperty( "jecars:Body", "<HTML>Een simpele test tool: " + compl + "% completed\n</HTML>" );      
      getTool().save();
      Thread.sleep( 1000 );
      compl += 10;
      reportMessage( Level.INFO, "Tool completion = " + compl + "%\n", false );
      reportOutput( "(OUTPUT) Tool completion = " + compl + "%\n" );
      reportProgress( compl/100.0 );
      getTool().setProperty( "jecars:Body", "<HTML>Een simpele test tool: " + compl + "% completed\n</HTML>" );      
      getTool().save();
      Thread.sleep( 1000 );
      compl += 10;
      reportMessage( Level.INFO, "Tool completion = " + compl + "%\n", false );
      reportOutput( "(OUTPUT) Tool completion = " + compl + "%\n" );
      reportProgress( compl/100.0 );
      getTool().setProperty( "jecars:Body", "<HTML>Een simpele test tool: " + compl + "% completed\n</HTML>" );      
      getTool().save();
      Thread.sleep( 1000 );
      reportMessage( Level.INFO, "Tool completion = " + compl + "%\n", false );
      reportOutput( "(OUTPUT) Tool completion = " + compl + "%\n" );
      reportProgress( compl/100.0 );
      System.out.println( "CALC " + no1 + " + " + no2 + " = " + (no1+no2) );
      mResults.add( (no1+no2) );
      compl += 10;
      getTool().setProperty( "jecars:Body", "<HTML>Een simpele test tool: " + compl + "% completed\n</HTML>" );      
      reportMessage( Level.INFO, "Tool completion = " + compl + "%\n", false );
      reportOutput( "(OUTPUT) Tool completion = " + compl + "%\n" );
      reportProgress( compl/100.0 );
      getTool().save();
 */
    }
    super.toolRun();
    return;
  }

  /** Superclass must implement this method to actually process the outputs for the tool
   * @throws java.lang.Exception
   */
  @Override
  protected void toolOutput() throws Exception {
    System.out.println( " SIMPLE CALC TOOL OUTPUT" );
    clearOutputs();    
    for (Double d : mResults ) {
      addOutput( d.toString() );
    }
    super.toolOutput();
    return;
  }
  
    
}
