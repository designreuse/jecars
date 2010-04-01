/*
 * Copyright 2007 NLR - National Aerospace Laboratory
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

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import javax.jcr.Credentials;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import org.apache.jackrabbit.core.TransientRepository;

/**
 * CARS_Repository
 * @version $Id: CARS_Repository.java,v 1.1 2007/11/09 16:06:00 weertj Exp $
 */
public class CARS_Repository extends TransientRepository {

  public CARS_Repository() throws IOException {
    super();
  }
  
  @Override
  public Session login( Credentials pCreds, String pPropsFile ) throws RepositoryException {
    try {
      FileInputStream fis = new FileInputStream( pPropsFile );
      Properties props = new Properties();
      props.load( fis );
      fis.close();
      TransientRepository rep = new TransientRepository(
              props.getProperty( "configFile" ), props.getProperty( "repHome" ) );
      Session ses = rep.login( pCreds, props.getProperty( "workspace" ));
      String[] ns = CARS_Factory.gRepNamespaces.split( "," );
      CARS_Factory.getLastFactory().addNamespaces( ses.getWorkspace().getNamespaceRegistry(), ns );
      String[] cnds = CARS_Factory.gRepCNDFiles.split( "," );
      CARS_Factory.getLastFactory().addNodeTypesDefinitions( ses, cnds );
      return ses;
    } catch (Exception e) {
      throw new RepositoryException(e);
    }
  }
  
}
