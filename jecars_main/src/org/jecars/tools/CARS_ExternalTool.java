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
package org.jecars.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import javax.jcr.Binary;
import javax.jcr.Node;
import javax.jcr.Property;
import org.jecars.CARS_Utils;

/**
 *
 * @author weert
 */
public class CARS_ExternalTool extends CARS_DefaultToolInterface {

  static final public String WORKINGDIRECTORY               = "jecars:WorkingDirectory";
  static final public String GENERATEUNIQUEWORKINGDIRECTORY = "jecars:GenerateUniqueWorkingDirectory";

  private final transient List<File> mPreRunFiles = new ArrayList<File>();

  private final transient List<File> mInputs = new ArrayList<File>();

  private transient File mWorkingDirectory = null;

  /** getWorkingDirectory
   *
   * @return
   */
  protected File getWorkingDirectory() {
    return mWorkingDirectory;
  }

  /** toolInit
   * 
   * @throws Exception
   */
  @Override
  protected void toolInit() throws Exception {
    CARS_ToolSignalManager.addToolSignalListener( this );
    super.toolInit();
    return;
  }

  /** toolFinally
   *
   */
  @Override
  protected void toolFinally() {
    CARS_ToolSignalManager.removeToolSignalListener( this );
    super.toolFinally();
    return;
  }




  /** toolInput
   *
   * @throws Exception
   */
  @Override
  protected void toolInput() throws Exception {
    Node config = getConfigNode();

    // **** Copy the config node to the current tool
    if (!hasConfigNode()) {
      copyConfigNodeToTool( config );
    }
    config = getConfigNode();

    if (config.hasProperty( WORKINGDIRECTORY )) {

      // ******************************
      // **** Get the working directory
      mWorkingDirectory = new File( config.getProperty( WORKINGDIRECTORY ).getString() );
      final boolean unique = "true".equals( config.getProperty( GENERATEUNIQUEWORKINGDIRECTORY ).getValue().getString());
      if (unique) {
        final long id = System.currentTimeMillis();
        getTool().setProperty( "jecars:Id", id );
        mWorkingDirectory = new File( mWorkingDirectory, "wd_" + id );
        if (!mWorkingDirectory.mkdirs()) {
          throw new IOException( "Cannot create directory: " + mWorkingDirectory.getAbsolutePath() );
        }
      }

      // *****************************************************
      // **** Copy the Inputs* object to the working directory
      final Collection<InputStream> inputs = (Collection<InputStream>)getInputsAsObject( InputStream.class, null );
      int i = 1;
      for (InputStream inputStream : inputs) {
        final File inputF = new File( mWorkingDirectory, "input" + i + ".txt" );
        final FileOutputStream fos = new FileOutputStream( inputF );
        try {
          CARS_Utils.sendInputStreamToOutputStream( 10000, inputStream, fos );
          mInputs.add( inputF );
        } finally {
          fos.close();
        }
        i++;
      }

      // **************************************************************************
      // **** Copy the input resource of the template tool to the working directory
      final Node templateTool = getToolTemplate( getTool() );
      if (templateTool!=null) {
        final List<Node> inputRes = getInputResources( templateTool );
        for ( final Node input : inputRes ) {
          InputStream       is = null;
          FileOutputStream fos = null;
          try {
            final Binary bin = input.getProperty( "jcr:data" ).getBinary();
            is = bin.getStream();
//            is = input.getProperty( "jcr:data" ).getStream();
            final File inputResFile = new File( mWorkingDirectory, input.getName() );
            fos = new FileOutputStream( inputResFile );
            CARS_Utils.sendInputStreamToOutputStream( 10000, is, fos );
          } finally {
            if (fos!=null) {
              fos.close();
            }
            if (is!=null) {
              is.close();
            }
          }
        }
      }

    }
    super.toolInput();
    return;
  }


  /** toolRun
   *
   * @throws Exception
   */
  @Override
  protected void toolRun() throws Exception {

    // **** file snapshot
    final File workDir = getWorkingDirectory();
    final File[] files = workDir.listFiles();
    for( final File file : files ) {
      mPreRunFiles.add( file );
    }


    final Node config = getConfigNode();
    if (config.hasProperty( "jecars:ExecPath" )) {
      final String execPath = config.getProperty( "jecars:ExecPath" ).getString();
      reportMessage( Level.CONFIG, "ExecPath=" + execPath, false );
      final String cmdParam = getParameterString( "commandLine", 0 );
      final ProcessBuilder pb;
      final List<String> commands = new ArrayList<String>();
      commands.add( execPath );
      if (cmdParam!=null) {
        commands.add( cmdParam );
      }
      for(final File input : mInputs ) {
        commands.add( input.getAbsolutePath() );
      }
      pb = new ProcessBuilder( commands );
      if (config.hasProperty( WORKINGDIRECTORY )) {
        pb.directory( mWorkingDirectory );
      }
      final Process process = pb.start();
      pb.redirectErrorStream( true );
      final BufferedReader outputReader = new BufferedReader(new InputStreamReader( process.getInputStream() ) );
//      final BufferedReader errorReader = new BufferedReader(new InputStreamReader( process.getErrorStream() ) );
//      final OutputStream ostream = process.getOutputStream();
//      ostream.write( "hihia hahahajhj\n".getBytes() );
//      ostream.flush();
      String line;
      final StringBuilder output = new StringBuilder();
      while( (line = outputReader.readLine()) != null) {
//          System.out.println("outputp " + line );
        output.append( line ).append( LF );
        replaceOutput( "stdout", output.toString() );
      }
//      addOutput( output.toString() );
      reportStatusMessage( "External tool is ending" );
      outputReader.close();
      process.destroy();
    } else {
      throw new InvalidParameterException( "No execpath" );
    }
    super.toolRun();
  }

  /** toolOutput
   *
   * @throws Exception
   */
  @Override
  protected void toolOutput() throws Exception {
    super.toolOutput();
    scanOutputFiles( true, false );
    return;
  }

  /** scanOutputFiles
   *
   * @param pCopyOutput
   * @throws FileNotFoundException
   * @throws Exception
   */
  private synchronized void scanOutputFiles( final boolean pCopyOutput, final boolean pPartial ) throws FileNotFoundException, Exception {
    final File workDir = getWorkingDirectory();
    if ((workDir!=null) && (workDir.exists())) {
        reportStatusMessage( "Scan output files [Copy output=" + pCopyOutput + "] [Partial=" + pPartial + "]" );
        final File[] files = workDir.listFiles();
        final boolean outputLink;
        final Property outputAsLink = getResolvedToolProperty( getTool(), "jecars:OutputAsLink" );
        if (outputAsLink==null) {
          outputLink = false;
        } else {
          outputLink = outputAsLink.getBoolean();
        }
        for( final File file : files ) {
          if (!mPreRunFiles.contains(file)) {
            if ((!outputLink) && (pCopyOutput)) {
              // **** New output file... copy it
              reportStatusMessage( "Copy output file " + file.getName() );
              final FileInputStream fis = new FileInputStream( file );
              try {
                final Node output = addOutput( fis, file.getName() );
                if (output!=null) {
                  output.setProperty( "jecars:IsLink", outputLink );
                  output.setProperty( "jecars:ContentLength", file.length() );
                  output.save();
                }
              } finally {
                fis.close();
              }
            } else {
              final Node output = addOutput( null, file.getName() );
              if (output!=null) {
                  final long len = file.length();
                  reportStatusMessage( "Set output file length " + len );
                  output.setProperty( "jecars:IsLink", outputLink );
                  output.setProperty( "jecars:ContentLength", len );
                  output.setProperty( "jecars:Partial", pPartial );
                  output.save();
              }
            }
          }
        }
    }
    return;
  }


  /** signal
   *
   * @param pToolPath
   * @param pSignal
   */
  @Override
  public void signal( final String pToolPath, final CARS_ToolSignal pSignal ) {
    switch( pSignal ) {

      /** REFRESH_OUTPUTS
       *
       */
      case REFRESH_OUTPUTS: {
        try {
          if (STATE_OPEN_RUNNING.equals( getState() )) {
            scanOutputFiles( false, true );
          }
        } catch( Exception e ) {
          LOG.log( Level.WARNING, e.getMessage(), e );
        }
        break;
      }
    }
    super.signal(pToolPath, pSignal);
  }



}
