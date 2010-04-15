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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import javax.jcr.AccessDeniedException;
import javax.jcr.RepositoryException;
import javax.security.auth.login.CredentialExpiredException;
import nl.msd.jdots.JD_Taglist;
import org.jecars.CARS_ActionContext;
import org.jecars.CARS_Factory;
import org.jecars.CARS_Main;
import org.jecars.client.JC_Clientable;
import org.jecars.client.JC_RESTComm;

/**
 *
 * @author weert
 */
public class JC_LocalRESTComm extends JC_RESTComm {

  private final JC_LocalClient mLocalClient;

  /** JC_LocalRESTComm
   *
   * @param pClient
   */
  public JC_LocalRESTComm( final JC_Clientable pClient ) {
    super( pClient );
    mLocalClient = (JC_LocalClient)pClient;
    return;
  }

  /** _processHttpHeaders
   *
   * @param pAC
   * @param pResponse
   */
  private void _processHttpHeaders( final CARS_ActionContext pAC, final JD_Taglist pTags ) throws RepositoryException {
    pTags.replaceData( "Jecars-Version", CARS_Main.VERSION_ID );
    if (pAC.getContentsLength()!=-1) {
      pTags.replaceData( "ContentLength", (int)pAC.getContentsLength() );
    }
    if (pAC.canBeCachedResult()) {
      pTags.replaceData( "ETag", pAC.getETag() );
    }
    return;
  }


    /** resultToOutput
     *
     * @param pResult
     * @param pResponse
     */
    private void resultToOutput( final Object pResult, final JD_Taglist pTags, final boolean pSendData ) { //throws Exception {
      if (pResult instanceof StringBuilder) {
        pTags.replaceData( "ContentLength", ((StringBuilder)pResult).length() );
        if (pSendData) {
          final ByteArrayInputStream bais = new ByteArrayInputStream( ((StringBuilder)pResult).toString().getBytes() );
          pTags.replaceData( INPUTSTREAM, bais );
        }
      } else if ((pResult instanceof InputStream) && (pSendData)) {
        pTags.replaceData( INPUTSTREAM, (InputStream)pResult );
      }
      return;
    }


  /** createActionContext
   *
   * @param pRequest
   * @param pResponse
   * @return
   * @throws java.lang.Exception
   */
  protected CARS_ActionContext createActionContext( final JC_LocalURLEmul pURLEmul ) throws MalformedURLException, AccessDeniedException {
//    final CARS_ActionContext ac = CARS_ActionContext.createActionContext( mLocalClient.getUsername(), mLocalClient.getPassword() );
//    return ac;

    final JC_LocalRESTComm comm = (JC_LocalRESTComm)mLocalClient.getRESTComm();
    final String auth;
    CARS_ActionContext ac;
    if (comm.getGoogleLoginAuth()==null) {
      auth = mLocalClient.getUsername();
      ac = CARS_ActionContext.createActionContext( auth, mLocalClient.getPassword() );
    } else {
      auth = comm.getGoogleLoginAuth();
      ac = CARS_ActionContext.createActionContext( comm.getGoogleLoginAuth() );
    }

    final URL url = new URL( pURLEmul.getMURL() );
    final String pathInfo = url.getPath();
    if ((auth==null) && (pathInfo!=null)) {
      if ("/accounts/login".equals( pathInfo )) {
        // **** Not allowed, so report (s)he's unauthorized
        throw new AccessDeniedException( pathInfo );
      } else {
        if (pathInfo.startsWith( "/accounts/" )) {
          ac = CARS_ActionContext.createActionContext( "anonymous", "anonymous".toCharArray() );
        } else {
          throw new AccessDeniedException( pathInfo );
        }
      }
    } else {
      if (pathInfo==null) {
        return null;
      }
    }

    // **** Fill in attributes
//    if (ac!=null) {
//      ac.setIfModifiedSince( pRequest.getHeader( "If-Modified-Since" ) );
//    }

    return ac;
  }




  /** createHttpConnection
   *
   * @param pURL
   * @return
   * @throws MalformedURLException
   * @throws IOException
   */
  @Override
  public HttpURLConnection createHttpConnection( final String pURL ) throws MalformedURLException, IOException {
    final JC_LocalURLEmul lue = new JC_LocalURLEmul( null );
    if (getGoogleLoginAuth()!=null) {
      String url = pURL;
      if (url.indexOf( '?' )!=-1) {
        url += "&GOOGLELOGIN_AUTH=" + getGoogleLoginAuth();
      } else {
        url += "?GOOGLELOGIN_AUTH=" + getGoogleLoginAuth();
      }
      lue.setMURL( url );
    } else {
      lue.setMURL( pURL );
    }
    return lue;
  }

  @Override
  public HttpURLConnection createHttpConnection(String pURL, String pUsername, char[] pPassword) throws MalformedURLException, IOException {
    return null;
  }

  /** sendMessageDELETE
   *
   * @param pClient
   * @param pConn
   * @return
   * @throws MalformedURLException
   */
  @Override
  public JD_Taglist sendMessageDELETE( final JC_Clientable pClient, final HttpURLConnection pConn ) throws MalformedURLException {
    final JC_LocalURLEmul lue = (JC_LocalURLEmul)pConn;
    final JD_Taglist tags = new JD_Taglist();
    final URL url = new URL( lue.getMURL() );
    final CARS_Factory  factory = new CARS_Factory();
    CARS_ActionContext ac = null;
    try {
      ac = createActionContext( lue );
      ac.setContextPath( "" );
      ac.setPathInfo( url.getPath() );
      ac.setQueryString( url.getQuery() );
      ac.setParameterMap( null );
      ac.setBaseURL( JC_LocalClient.JECARSLOCAL );
      factory.performDeleteAction( ac );
      tags.replaceData( "ContentType", ac.getContentType() );
      tags.replaceData( "ResponseCode", ac.getErrorCode() );
      resultToOutput( ac.getResult(), tags, true );
      tags.replaceData( "ResponseCode", ac.getErrorCode() );
      tags.replaceData( "ResponseException", ac.getError() );
    } catch( AccessDeniedException ade ) {
      // **** Not allowed, so report (s)he's unauthorized
  //    pResponse.setHeader( "WWW-Authenticate", "BASIC realm=\"JeCARS\"" );
      tags.replaceData( "ResponseCode", HttpURLConnection.HTTP_UNAUTHORIZED );
    } catch (CredentialExpiredException cee) {
      tags.replaceData( "Location", cee.getMessage() );
      tags.replaceData( "ResponseCode", HttpURLConnection.HTTP_MOVED_TEMP );
    } catch( Exception e ) {
  //    pResponse.setHeader( "WWW-Authenticate", "BASIC realm=\"JeCARS\"" );
      tags.replaceData( "ResponseCode", HttpURLConnection.HTTP_UNAUTHORIZED );
    } finally {
      if (ac!=null) {
        ac.destroy();
      }
    }
    return tags;
  }


  /** sendMessagePUT
   *
   * @param pClient
   * @param pConn
   * @param pContents
   * @param pContentsType
   * @param pContentsLength
   * @return
   * @throws MalformedURLException
   */
  @Override
  public JD_Taglist sendMessagePUT( final JC_Clientable pClient, final HttpURLConnection pConn,
                                    final InputStream pContents, final String pContentsType,
                                    final long pContentsLength) throws MalformedURLException {
    final JC_LocalURLEmul lue = (JC_LocalURLEmul)pConn;
    final JD_Taglist tags = new JD_Taglist();
    final URL url = new URL( lue.getMURL() );
    final CARS_Factory  factory = new CARS_Factory();
    CARS_ActionContext ac = null;
    try {
      ac = createActionContext( lue );
      ac.setContextPath( "" );
      ac.setPathInfo( url.getPath() );
      ac.setQueryString( url.getQuery() );
      ac.setParameterMap( null );
      ac.setBaseURL( JC_LocalClient.JECARSLOCAL );
      ac.setBodyStream( pContents, pContentsType );
      factory.performPutAction( ac, null );
      tags.replaceData( "ContentType", ac.getContentType() );
      tags.replaceData( "ResponseCode", ac.getErrorCode() );
      resultToOutput( ac.getResult(), tags, true );
      _processHttpHeaders( ac, tags );
      tags.replaceData( "ResponseCode", ac.getErrorCode() );
      tags.replaceData( "ResponseException", ac.getError() );
    } catch( AccessDeniedException ade ) {
      // **** Not allowed, so report (s)he's unauthorized
  //    pResponse.setHeader( "WWW-Authenticate", "BASIC realm=\"JeCARS\"" );
      tags.replaceData( "ResponseCode", HttpURLConnection.HTTP_UNAUTHORIZED );
    } catch (CredentialExpiredException cee) {
      tags.replaceData( "Location", cee.getMessage() );
      tags.replaceData( "ResponseCode", HttpURLConnection.HTTP_MOVED_TEMP );
    } catch( Exception e ) {
  //    pResponse.setHeader( "WWW-Authenticate", "BASIC realm=\"JeCARS\"" );
      tags.replaceData( "ResponseCode", HttpURLConnection.HTTP_UNAUTHORIZED );
    } finally {
      if (ac!=null) {
        ac.destroy();
      }
    }
    return tags;
  }


  /** sendMessagePOST
   *
   * @param pClient
   * @param pConn
   * @param pContents
   * @param pContentsType
   * @param pContentsLength
   * @return
   * @throws MalformedURLException
   */
  @Override
  public JD_Taglist sendMessagePOST( final JC_Clientable pClient, final HttpURLConnection pConn,
                                     final InputStream pContents, final String pContentsType, final long pContentsLength ) throws MalformedURLException {
    final JC_LocalURLEmul lue = (JC_LocalURLEmul)pConn;
    final JD_Taglist tags = new JD_Taglist();
    final URL url = new URL( lue.getMURL() );
    final CARS_Factory  factory = new CARS_Factory();
    CARS_ActionContext ac = null;
    try {
      ac = createActionContext( lue );
      ac.setContextPath( "" );
      ac.setPathInfo( url.getPath() );
      ac.setQueryString( url.getQuery() );
      ac.setParameterMap( null );
      ac.setBaseURL( JC_LocalClient.JECARSLOCAL );
      ac.setBodyStream( pContents, pContentsType );
      factory.performPostAction( ac );
      tags.replaceData( "ContentType", ac.getContentType() );
      final String createdNodePath = ac.getCreatedNodePath();
      if (createdNodePath!=null) {
        tags.replaceData( "Location", ac.getBaseContextURL() + createdNodePath );
      }
      tags.replaceData( "ResponseCode", ac.getErrorCode() );
      resultToOutput( ac.getResult(), tags, true );
      _processHttpHeaders( ac, tags );
      tags.replaceData( "ResponseCode", ac.getErrorCode() );
      tags.replaceData( "ResponseException", ac.getError() );
    } catch( AccessDeniedException ade ) {
      // **** Not allowed, so report (s)he's unauthorized
  //    pResponse.setHeader( "WWW-Authenticate", "BASIC realm=\"JeCARS\"" );
      tags.replaceData( "ResponseCode", HttpURLConnection.HTTP_UNAUTHORIZED );
    } catch (CredentialExpiredException cee) {
      tags.replaceData( "Location", cee.getMessage() );
      tags.replaceData( "ResponseCode", HttpURLConnection.HTTP_MOVED_TEMP );
    } catch( Exception e ) {
  //    pResponse.setHeader( "WWW-Authenticate", "BASIC realm=\"JeCARS\"" );
      tags.replaceData( "ResponseCode", HttpURLConnection.HTTP_UNAUTHORIZED );
    } finally {
      if (ac!=null) {
        ac.destroy();
      }
    }
    return tags;
  }


  /** sendMessageHEAD
   *
   * @param pClient
   * @param pConn
   * @return
   * @throws ProtocolException
   * @throws IOException
   */
  @Override
  public JD_Taglist sendMessageHEAD( final JC_Clientable pClient, final HttpURLConnection pConn) throws ProtocolException, IOException {
    final JD_Taglist tags = new JD_Taglist();
    final JC_LocalURLEmul lue = (JC_LocalURLEmul)pConn;
    final URL url = new URL( lue.getMURL() );
    final CARS_Factory  factory = new CARS_Factory();
    CARS_ActionContext ac = null;
    try {
      ac = createActionContext( lue );
      ac.setContextPath( "" );
      ac.setPathInfo( url.getPath() );
      ac.setQueryString( url.getQuery() );
      ac.setParameterMap( null );
      ac.setBaseURL( JC_LocalClient.JECARSLOCAL );
      factory.performHeadAction( ac );
      tags.replaceData( "ContentType", ac.getContentType() );
      tags.replaceData( "ResponseCode", ac.getErrorCode() );
      if (ac.getLastModified()!=0) {
        tags.replaceData( "LastModified", ac.getLastModified() );
      }
      _processHttpHeaders( ac, tags );
      tags.replaceData( "ResponseCode", ac.getErrorCode() );
      tags.replaceData( "ResponseException", ac.getError() );
    } catch( AccessDeniedException ade ) {
      // **** Not allowed, so report (s)he's unauthorized
  //    pResponse.setHeader( "WWW-Authenticate", "BASIC realm=\"JeCARS\"" );
      tags.replaceData( "ResponseCode", HttpURLConnection.HTTP_UNAUTHORIZED );
    } catch (CredentialExpiredException cee) {
      tags.replaceData( "Location", cee.getMessage() );
      tags.replaceData( "ResponseCode", HttpURLConnection.HTTP_MOVED_TEMP );
    } catch( Exception e ) {
  //    pResponse.setHeader( "WWW-Authenticate", "BASIC realm=\"JeCARS\"" );
      tags.replaceData( "ResponseCode", HttpURLConnection.HTTP_UNAUTHORIZED );
    } finally {
      if (ac!=null) {
        ac.destroy();
      }
    }
    return tags;
  }



  /** sendMessageGET
   *
   * @param pClient
   * @param pConn
   * @return
   * @throws ProtocolException
   * @throws IOException
   */
  @Override
  public JD_Taglist sendMessageGET( final JC_Clientable pClient, final HttpURLConnection pConn ) throws ProtocolException, IOException {
    final JC_LocalURLEmul lue = (JC_LocalURLEmul)pConn;
    final URL url = new URL( lue.getMURL() );

    // **** Check the X-HTTP-Method-Override flags
    final String q = url.getQuery();
    if (q!=null) {
      if (q.indexOf( "X-HTTP-Method-Override=" )!=-1) {
        if (q.indexOf( "X-HTTP-Method-Override=DELETE")!=-1) {
          return sendMessageDELETE( pClient, pConn );
        }
        if (q.indexOf( "X-HTTP-Method-Override=PUT")!=-1) {
          return sendMessagePUT( pClient, pConn, null, null, -1L );
        }
        if (q.indexOf( "X-HTTP-Method-Override=POST")!=-1) {
          return sendMessagePOST( pClient, pConn, null, null, -1L );
        }
        if (q.indexOf( "X-HTTP-Method-Override=HEAD")!=-1) {
          return sendMessageHEAD( pClient, pConn );
        }
      }
    }

    final JD_Taglist tags = new JD_Taglist();
    final CARS_Factory  factory = new CARS_Factory();
    CARS_ActionContext ac = null;
    try {
      ac = createActionContext( lue );
      ac.setContextPath( "" );
      ac.setPathInfo( url.getPath() );
      ac.setQueryString( q );
      ac.setParameterMap( null );
      ac.setBaseURL( JC_LocalClient.JECARSLOCAL );
      factory.performGetAction( ac, null );
      final long lastMod = ac.getLastModified();
      boolean getResult = true;
      if ((ac.canBeCachedResult()) && (lastMod!=0) && (ac.getIfModifiedSince()!=null)) {
      // **** Check for if modified since
        if (ac.getIfModifiedSince().getValue()/1000>=(lastMod/1000)) {
          // **** Not changed
          tags.replaceData( "ResponseCode", HttpURLConnection.HTTP_NOT_MODIFIED );
          getResult = false;
        }
      }
      if (getResult) {
        final Object result = ac.getResult();
        tags.replaceData( "ContentType", ac.getContentType() );
        tags.replaceData( "ResponseCode", ac.getErrorCode() );
        if (lastMod!=0) {
          tags.replaceData( "LastModified", lastMod );
        }
        _processHttpHeaders( ac, tags );
        resultToOutput( result, tags, true );
        tags.replaceData( "ResponseCode", ac.getErrorCode() );
        tags.replaceData( "ResponseException", ac.getError() );
      }
    } catch( AccessDeniedException ade ) {
      // **** Not allowed, so report (s)he's unauthorized
  //    pResponse.setHeader( "WWW-Authenticate", "BASIC realm=\"JeCARS\"" );
      tags.replaceData( "ResponseCode", HttpURLConnection.HTTP_UNAUTHORIZED );
    } catch (CredentialExpiredException cee) {
      tags.replaceData( "Location", cee.getMessage() );
      tags.replaceData( "ResponseCode", HttpURLConnection.HTTP_MOVED_TEMP );
    } catch( Exception e ) {
  //    pResponse.setHeader( "WWW-Authenticate", "BASIC realm=\"JeCARS\"" );
      tags.replaceData( "ResponseCode", HttpURLConnection.HTTP_UNAUTHORIZED );
    } finally {
      if (ac!=null) {
        ac.destroy();
      }
    }
    return tags;
  }


}
