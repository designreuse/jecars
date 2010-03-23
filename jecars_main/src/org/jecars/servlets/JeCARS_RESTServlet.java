/*
 * Copyright 2007-2010 NLR - National Aerospace Laboratory
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

package org.jecars.servlets;

import com.google.gdata.data.DateTime;
import com.google.gdata.util.ParseException;
import java.io.*;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jcr.AccessDeniedException;
import javax.jcr.RepositoryException;

import javax.security.auth.login.CredentialExpiredException;
import javax.servlet.*;
import javax.servlet.http.*;
import org.jecars.CARS_ActionContext;
import org.jecars.CARS_Factory;
import org.jecars.CARS_Main;
import org.jecars.support.BASE64Decoder;
import org.jecars.tools.CARS_DefaultToolInterface;

/**
 * JeCARS_RESTServlet
 *
 * @version $Id: JeCARS_RESTServlet.java,v 1.6 2009/06/05 14:43:05 weertj Exp $
 */
public class JeCARS_RESTServlet extends HttpServlet {
      
  protected static final Logger gLog = Logger.getLogger( JeCARS_RESTServlet.class.getPackage().getName() );

  /** The maximum number of concurrent threads running before replying with a SERVICE UNAVAILABLE
   */
  static protected int        MAXTHREADCOUNT  = 20;
  static private volatile int mThreadCount    = 0;
  static private CARS_Factory mCARSFactory    = null;

  static private JeCARS_WebDAVServlet gWebdav = new JeCARS_WebDAVServlet();


  /** init
   * 
   * @throws javax.servlet.ServletException
   */
  @Override
  public void init() throws ServletException {
    gLog.log( Level.INFO, "Using " + CARS_Main.PRODUCTNAME + " version: " + CARS_Main.VERSION );
    try {
      mCARSFactory = new CARS_Factory();
      CARS_Factory.setServletContext( getServletContext() );

      gLog.log( Level.INFO, "Trying to read /WEB-INF/classes/" + CARS_Factory.JECARSPROPERTIESNAME );
      InputStream is = null;
      is = getServletContext().getResourceAsStream( "/WEB-INF/classes/" + CARS_Factory.JECARSPROPERTIESNAME );
      if (is!=null) {
        gLog.log( Level.INFO, "Reading /WEB-INF/classes/" + CARS_Factory.JECARSPROPERTIESNAME );
        CARS_Factory.gJecarsProperties.load( is );
        is.close();
      } else {
        gLog.log( Level.INFO, "/WEB-INF/classes/" + CARS_Factory.JECARSPROPERTIESNAME + " not found" );
      }
      mCARSFactory.init( null, false );
      gWebdav.init();
      gWebdav.init( getServletContext(), this );
    } catch (Exception e) {
      gLog.log( Level.SEVERE, null, e );
    }
    return;
  }

  /** getResourcePathPrefix
   * 
   * @return
   */
  protected String getResourcePathPrefix() {
    return null;
  }

  /** destroy
   */
  @Override
  public void destroy() {
    gLog.log( Level.INFO, "Servlet destroy request " );
    CARS_DefaultToolInterface.destroy();
    CARS_Factory.shutdown();
    return;
  }
  
    
    /** Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     */
    protected void processRequest( final HttpServletRequest request, final HttpServletResponse response )
                                   throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        final PrintWriter out = response.getWriter();
        out.println("<html>");
        out.println("<head>");
        out.println("<title>Servlet JeCARS_RESTServlet</title>");
        out.println("</head>");
        out.println("<body>");
        out.println("<h1>Servlet JeCARS_RESTServlet at " + request.getPathInfo() + "</h1>");
        out.println("</body>");
        out.println("</html>");
        out.close();
        return;
    }

    /** createActionContext
     *
     * @param pRequest
     * @param pResponse
     * @return
     * @throws java.lang.Exception
     */
    protected CARS_ActionContext createActionContext( final HttpServletRequest pRequest, final HttpServletResponse pResponse ) throws IOException, ParseException {
      CARS_ActionContext ac = null;
      // **** Get Authorization header
      String auth = pRequest.getHeader( "Authorization" );
      if (auth!=null) {
        String username = null, password = null;
        if (auth.toUpperCase().startsWith("BASIC ")) {
          // **** Get encoded user and password, comes after "BASIC "
          final String userpassEncoded = auth.substring(6);
          final String encoding = new String( BASE64Decoder.decodeBuffer( userpassEncoded ));
          // **** Check our user list to see if that user and password are "allowed"
          final int in = encoding.indexOf( ':' );
//          username = CARS_Utils.encode(encoding.substring( 0, in));
          username = encoding.substring( 0, in);
          password = encoding.substring( in+1 );          
          ac = CARS_ActionContext.createActionContext( username, password.toCharArray() );
        }
        if (auth.toUpperCase().startsWith("GOOGLELOGIN AUTH=")) {
          final String key = auth.substring( 17 );
          ac = CARS_ActionContext.createActionContext( key );          
        }
      }
      
      if (auth==null) {          
        final String q = pRequest.getQueryString();
        if (q!=null) {
          final int ix = q.indexOf( "GOOGLELOGIN_AUTH=" );
          if (ix!=-1) {
            auth = q.substring( ix + "GOOGLELOGIN_AUTH=".length() );
            if (auth.indexOf( '&' )!=-1) {
              auth = auth.substring( 0, auth.indexOf( '&' ));
            }
            ac = CARS_ActionContext.createActionContext( auth );          
          }
        }
      }

      final String pathInfo = pRequest.getPathInfo();
      if ((auth==null) && (pathInfo!=null)) {
        if (pathInfo.equals( "/accounts/login" )) {
//        // **** Not allowed, so report (s)he's unauthorized
          pResponse.setHeader( "WWW-Authenticate", "BASIC realm=\"JeCARS\"" );
          pResponse.sendError( pResponse.SC_UNAUTHORIZED );
        } else {
          if (pathInfo.startsWith( "/accounts/" )) {
            ac = CARS_ActionContext.createActionContext( "anonymous", "anonymous".toCharArray() );
          } else {
            pResponse.setHeader( "WWW-Authenticate", "BASIC realm=\"JeCARS\"" );
            pResponse.sendError( pResponse.SC_UNAUTHORIZED );
          }
        }
      } else {
        if (pathInfo==null) {
          return null;
        }
      }

      // **** Fill in attributes
      if (ac!=null) {
        ac.setIfModifiedSince( pRequest.getHeader( "If-Modified-Since" ) );
        ac.setIfNoneMatch(     pRequest.getHeader( "If-None-Match"     ) );
      }

      return ac;
    }

    /** resultToOutput
     *
     * @param pResult
     * @param pResponse
     * @param pSendData
     */
    private void resultToOutput( final Object pResult, final HttpServletResponse pResponse, final boolean pSendData ) {
      PrintWriter outp = null;
      OutputStream os  = null;
      try {
        if (pResult instanceof StringBuilder) {
          pResponse.setContentLength( ((StringBuilder)pResult).length() );
          if (pSendData) {
            outp = pResponse.getWriter();
            outp.print( ((StringBuilder)pResult).toString() );
          }
        } else if (pResult instanceof InputStream) {
          if (pSendData) {
            os = pResponse.getOutputStream();
            final BufferedInputStream  bis = new BufferedInputStream((InputStream)pResult);
            final BufferedOutputStream bos = new BufferedOutputStream(os);
            try {
              final byte[] buff = new byte[200000];
//            long sended = 0;
              int bytesRead;
              while(-1 != (bytesRead = bis.read(buff, 0, buff.length))) {
                bos.write(buff, 0, bytesRead);
                bos.flush();
//              sended += bytesRead;
  //            System.out.println( "--- " + sended );
              }
            } finally {
              if (bis!=null) bis.close();
              if (bos!=null) bos.close();
            }
          }
        }
      } catch (IOException ioe ) {
        if ((ioe.getCause()!=null) && (ioe.getCause().getMessage()!=null)) {
          if (ioe.getCause().getMessage().indexOf( "Connection reset by peer" )==-1) {
            gLog.log( Level.WARNING, null, ioe );     // **** Tracker [1822777]
          }
        } else {
         gLog.log( Level.WARNING, null, ioe );
        }
      } catch (Exception e) {
        gLog.log( Level.WARNING, null, e );
      } finally {
        if (outp!=null) {
          outp.flush();
          outp.close();
        }
        if (os!=null) {
          try {
            os.flush();
            os.close();
          } catch (IOException ioe) {
            if ((ioe.getCause()!=null) && (ioe.getCause().getMessage()!=null)) {
              if (ioe.getCause().getMessage().indexOf( "Connection reset by peer" )==-1) {
                gLog.log( Level.WARNING, null, ioe );     // **** Tracker [1822777]
              }
            } else {
             gLog.log( Level.WARNING, null, ioe );
            }
          }
        }          
      }
      return;
    }

    /** _processHttpHeaders
     *
     * @param pAC
     * @param pResponse
     * @throws RepositoryException
     */
    private void _processHttpHeaders( final CARS_ActionContext pAC, final HttpServletResponse pResponse ) throws RepositoryException {
      final Map<String, ArrayList<String>> headers = pAC.getHttpHeaders();
      if (headers!=null) {
        for (String key : headers.keySet()) {
          final List<String> values = headers.get(key);
          if (values!=null) {
            for( String value : values ) {
              pResponse.setHeader( key, value );
            }
          }
        }
      }
      pResponse.setHeader( "Jecars-Version", CARS_Main.VERSION_ID );
      if (pAC.canBeCachedResult()) {
        pResponse.setHeader( "ETag", pAC.getETag() );
      }
      if (pAC.getContentsLength()!=-1) pResponse.setContentLength( (int)pAC.getContentsLength() );
      return;
    }




    private void debugPrintRequestHeader( HttpServletRequest pRequest ) {
      System.out.println( " --- HttpServletRequest --- " + pRequest.getPathInfo() + " / " + pRequest.getQueryString() );
      Enumeration e = pRequest.getHeaderNames();
      String h;
      while (e.hasMoreElements()) {
        h = (String) e.nextElement();
        if (h!=null) {
          System.out.println( h + "\t= " + pRequest.getHeader(h) );
        }
      }
      return;
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /** Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     */     
    @Override
    protected void doGet( final HttpServletRequest pRequest, final HttpServletResponse pResponse ) throws ServletException, IOException {

//      debugPrintRequestHeader( pRequest );

      // **** Check the X-HTTP-Method-Override flags
      final String q = pRequest.getQueryString();
      if (q!=null) {
        if (q.indexOf( "X-HTTP-Method-Override=" )!=-1) {
          if (q.indexOf( "X-HTTP-Method-Override=DELETE")!=-1) {
            doDelete( pRequest, pResponse );
            return;
          }
          if (q.indexOf( "X-HTTP-Method-Override=PUT")!=-1) {
            doPut( pRequest, pResponse );
            return;
          }
          if (q.indexOf( "X-HTTP-Method-Override=POST")!=-1) {
            doPost( pRequest, pResponse );
            return;
          }
          if (q.indexOf( "X-HTTP-Method-Override=HEAD")!=-1) {
            doHead( pRequest, pResponse );
            return;
          }
        }
      }
// long time = System.currentTimeMillis();
//  System.out.println( "--- " + mThreadCount + " GET " + pRequest.getPathInfo() );
      try {
        if (mThreadCount>MAXTHREADCOUNT) {
          pResponse.sendError( pResponse.SC_SERVICE_UNAVAILABLE );
          gLog.log( Level.WARNING, "SC_SERVICE_UNAVAILABLE", (Exception)null );
          return;
        }
        mThreadCount++;
        final CARS_ActionContext ac = createActionContext( pRequest, pResponse );
        if (ac!=null) {
          try {
            ac.setContextPath( pRequest.getContextPath() + pRequest.getServletPath() );
            ac.setPathInfo( new String(pRequest.getPathInfo().getBytes( "ISO-8859-1" ), "UTF-8" ) );
            ac.setQueryString( q );
            ac.setParameterMap( pRequest.getParameterMap() );
            ac.setBaseURL( pRequest.getScheme() + "://" + pRequest.getServerName()  + ':' + pRequest.getServerPort() );
            mCARSFactory.performGetAction( ac );
            final long lastMod = ac.getLastModified();
            boolean getResult = true;
            if (ac.getIfNoneMatch()!=null) {
              // **** Check for if the ETag is the same
              if (ac.getIfNoneMatch().compareTo( ac.getETag() )==0) {
                // **** Not changed
                pResponse.setStatus( HttpURLConnection.HTTP_NOT_MODIFIED );
                pResponse.setHeader( "ETag", ac.getETag() );
                getResult = false;
              }
            }
            if ((ac.canBeCachedResult()) && (lastMod!=0) && (ac.getIfModifiedSince()!=null)) {
              // **** Check for if modified since
              if (ac.getIfModifiedSince().getValue()/1000>=(lastMod/1000)) {
                // **** Not changed
                pResponse.setStatus( HttpURLConnection.HTTP_NOT_MODIFIED );
                pResponse.setHeader( "ETag", ac.getETag() );
                getResult = false;
              }
            }
            if (getResult) {
              final Object result = ac.getResult();
              pResponse.setContentType( ac.getContentType() );
              pResponse.setStatus( ac.getErrorCode() );
//              if (lastMod!=0) {
//                DateTime dtime = new DateTime( ac.getLastModified() );
              //    pResponse.setHeader( "Last-Modified", dtime.toStringRfc822() );
//                pResponse.setHeader( "ETag", ac.getETag() );
//              }
//              pResponse.setHeader( "ETag", ac.getETag() );
              _processHttpHeaders( ac, pResponse );
              resultToOutput( result, pResponse, true );
              pResponse.setStatus( ac.getErrorCode() );
            }
          } catch( AccessDeniedException ade ) {
            // **** Not allowed, so report (s)he's unauthorized
            pResponse.setHeader( "WWW-Authenticate", "BASIC realm=\"JeCARS\"" );
            pResponse.sendError( pResponse.SC_UNAUTHORIZED );            
          } catch (CredentialExpiredException cee) {
            pResponse.setHeader( "Location", cee.getMessage() );
            pResponse.setStatus( HttpURLConnection.HTTP_MOVED_TEMP );
          } finally {
            ac.destroy();
          }
        }
      } catch (Exception e) {
        gLog.log( Level.WARNING, null, e );
        pResponse.sendError( pResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage() );
      } finally {
        mThreadCount--;
      }
      return;
    }
    
    /** doHead
     * 
     * @param pRequest
     * @param pResponse
     * @throws javax.servlet.ServletException
     * @throws java.io.IOException
     */
    @Override
    protected void doHead( final HttpServletRequest pRequest, final HttpServletResponse pResponse )  throws ServletException, IOException {
      try {
        if (mThreadCount>MAXTHREADCOUNT) {
          pResponse.sendError( pResponse.SC_SERVICE_UNAVAILABLE );
          gLog.log( Level.WARNING, "SC_SERVICE_UNAVAILABLE", (Exception)null );
          return;
        }
        mThreadCount++;
        final CARS_ActionContext ac = createActionContext( pRequest, pResponse );
        if (ac!=null) {
          try {
            ac.setContextPath( pRequest.getContextPath() + pRequest.getServletPath() );
            ac.setPathInfo( new String(pRequest.getPathInfo().getBytes( "ISO-8859-1" ), "UTF-8" ) );
            ac.setQueryString( pRequest.getQueryString() );
            ac.setBaseURL( pRequest.getScheme() + "://" + pRequest.getServerName()  + ":" + pRequest.getServerPort() );
            mCARSFactory.performHeadAction( ac );
//            Object result = ac.getResult();
            pResponse.setContentType( ac.getContentType() );
            pResponse.setStatus( ac.getErrorCode() );
            if (ac.getLastModified()!=0) {
              final DateTime dtime = new DateTime( ac.getLastModified() );
              pResponse.setHeader( "Last-Modified", dtime.toStringRfc822() );
            }
//            resultToOutput( result, pResponse, false );
            _processHttpHeaders( ac, pResponse );
            pResponse.setStatus( ac.getErrorCode() );
          } catch( AccessDeniedException ade ) {
            // **** Not allowed, so report (s)he's unauthorized
            pResponse.setHeader( "WWW-Authenticate", "BASIC realm=\"JeCARS\"" );
            pResponse.sendError( pResponse.SC_UNAUTHORIZED );            
          } catch (CredentialExpiredException cee) {
            pResponse.setHeader( "Location", cee.getMessage() );
            pResponse.setStatus( HttpURLConnection.HTTP_MOVED_TEMP );
          } finally {
            ac.destroy();
          }
        }
      } catch (Exception e) {
        gLog.log( Level.WARNING, null, e );
      } finally {
        mThreadCount--;
      }
      return;
    }
            
    /** Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     */     
    @Override
    protected void doPost( HttpServletRequest pRequest, HttpServletResponse pResponse ) throws ServletException, IOException {

      try {
        if (mThreadCount>MAXTHREADCOUNT) {
          pResponse.sendError( pResponse.SC_SERVICE_UNAVAILABLE );
          return;
        }
        mThreadCount++;
        CARS_ActionContext ac = createActionContext( pRequest, pResponse );
        ServletInputStream sis = null;
        if (ac!=null) {
          try {
            ac.setContextPath( pRequest.getContextPath() + pRequest.getServletPath() );
            String pathInfo = new String(pRequest.getPathInfo().getBytes( "ISO-8859-1" ), "UTF-8" );
            if (pRequest.getHeader( "Slug" )!=null) {
              if (pathInfo.endsWith( "/" )) {
                pathInfo += pRequest.getHeader( "Slug" );
              } else {
                pathInfo += "/" + pRequest.getHeader( "Slug" );                
              }
            }
            ac.setPathInfo(     pathInfo );
            ac.setQueryString(  pRequest.getQueryString() );
            if ((pRequest.getContentType()==null) || pRequest.getContentType().equals( "application/x-www-form-urlencoded" )) {
              ac.setParameterMap( pRequest.getParameterMap() );
            }
            sis = pRequest.getInputStream();
            ac.setBaseURL( pRequest.getScheme() + "://" + pRequest.getServerName()  + ":" + pRequest.getServerPort() );
            ac.setBodyStream( sis, pRequest.getContentType() );
            mCARSFactory.performPostAction( ac );
            pResponse.setContentType( ac.getContentType() );
            final String createdNodePath = ac.getCreatedNodePath();
            if (createdNodePath!=null) {
              pResponse.setHeader( "Location", ac.getBaseContextURL() + createdNodePath );
            }
//            Node createdNode = ac.getCreatedNode();
//            if (createdNode!=null) {
//              pResponse.setHeader( "Location", ac.getBaseContextURL() + createdNode.getPath() );
//            }
            pResponse.setStatus( ac.getErrorCode() );
            resultToOutput( ac.getResult(), pResponse, true );
            _processHttpHeaders( ac, pResponse );
            pResponse.setStatus( ac.getErrorCode() );
          } catch( AccessDeniedException ade ) {
            // **** Not allowed, so report (s)he's unauthorized
            pResponse.setHeader( "WWW-Authenticate", "BASIC realm=\"JeCARS\"" );
            pResponse.sendError( pResponse.SC_UNAUTHORIZED );            
          } finally {
            if (sis!=null) sis.close();
            ac.destroy();
          }
        }
      } catch (Exception e) {
        gLog.log( Level.WARNING, null, e );
      } finally {
        mThreadCount--;
      }
      return;
    }
    
    /** doPut
     *
     * @param pRequest
     * @param pResponse
     * @throws javax.servlet.ServletException
     * @throws java.io.IOException
     */
    @Override
    protected void doPut( final HttpServletRequest pRequest, final HttpServletResponse pResponse )  throws ServletException, IOException {
      try {
        if (mThreadCount>MAXTHREADCOUNT) {
          pResponse.sendError( pResponse.SC_SERVICE_UNAVAILABLE );
          return;
        }
        mThreadCount++;
        final CARS_ActionContext ac = createActionContext( pRequest, pResponse );
        ServletInputStream sis = null;
        if (ac!=null) {
          try {
            ac.setContextPath( pRequest.getContextPath() + pRequest.getServletPath() );
            ac.setPathInfo( new String(pRequest.getPathInfo().getBytes( "ISO-8859-1" ), "UTF-8" ) );
            ac.setQueryString( pRequest.getQueryString() );
            sis = pRequest.getInputStream();
            ac.setBaseURL( pRequest.getScheme() + "://" + pRequest.getServerName()  + ":" + pRequest.getServerPort() );
            ac.setBodyStream( sis, pRequest.getContentType() );
            mCARSFactory.performPutAction( ac );
            pResponse.setContentType( ac.getContentType() );
            pResponse.setStatus( ac.getErrorCode() );
            resultToOutput( ac.getResult(), pResponse, true );
            _processHttpHeaders( ac, pResponse );
            pResponse.setStatus( ac.getErrorCode() );
          } catch( AccessDeniedException ade ) {
            // **** Not allowed, so report (s)he's unauthorized
            pResponse.setHeader( "WWW-Authenticate", "BASIC realm=\"JeCARS\"" );
            pResponse.sendError( pResponse.SC_UNAUTHORIZED );            
          } finally {
            ac.destroy();
            if (sis!=null) {
              sis.close();
            }
          }
        }
      } catch (Exception e) {
        gLog.log( Level.WARNING, null, e );
      } finally {
        mThreadCount--;
      }
      return;
    }


    /** doDelete
     *
     * @param pRequest
     * @param pResponse
     * @throws javax.servlet.ServletException
     * @throws java.io.IOException
     */
    @Override
    protected void doDelete( HttpServletRequest pRequest, HttpServletResponse pResponse )  throws ServletException, IOException {
      try {
        if (mThreadCount>MAXTHREADCOUNT) {
          pResponse.sendError( pResponse.SC_SERVICE_UNAVAILABLE );
          return;
        }
        mThreadCount++;
        CARS_ActionContext ac = createActionContext( pRequest, pResponse );
        if (ac!=null) {
          PrintWriter outp = pResponse.getWriter();
          try {
            ac.setContextPath( pRequest.getContextPath() + pRequest.getServletPath() );
            ac.setPathInfo( new String(pRequest.getPathInfo().getBytes( "ISO-8859-1" ), "UTF-8" ) );
            ac.setQueryString( pRequest.getQueryString() );
            ac.setBaseURL( pRequest.getScheme() + "://" + pRequest.getServerName()  + ":" + pRequest.getServerPort() );
            mCARSFactory.performDeleteAction( ac );
            pResponse.setContentType( ac.getContentType() );
            pResponse.setStatus( ac.getErrorCode() );
            resultToOutput( ac.getResult(), pResponse, true );
            pResponse.setStatus( ac.getErrorCode() );
          } catch( AccessDeniedException ade ) {
            // **** Not allowed, so report (s)he's unauthorized
            pResponse.setHeader( "WWW-Authenticate", "BASIC realm=\"JeCARS\"" );
            pResponse.sendError( pResponse.SC_UNAUTHORIZED );            
          } finally {
            outp.flush();
            outp.close();
            ac.destroy();
          }
        }
      } catch (Exception e) {
        gLog.log( Level.WARNING, null, e );
      } finally {
        mThreadCount--;
      }
      return;
    }

 /*
  @Override
    protected void service( HttpServletRequest pReq, HttpServletResponse pResp ) throws ServletException, IOException {
      if (pReq.getPathInfo().startsWith( "/webdav" )==true) {
        JeCARS_WebDAVServlet webdav = new JeCARS_WebDAVServlet();
        try {
          webdav.service( pReq, pResp, null, null );
        } catch( Exception e ) {
          e.printStackTrace();
        }
        return;
      }
      super.service( pReq, pResp );
      return;
    }
*/

    /** service
     *
     * @param pRequest
     * @param pResponse
     * @throws javax.servlet.ServletException
     * @throws java.io.IOException
     */
    @Override
    protected void service( HttpServletRequest pRequest, HttpServletResponse pResponse ) throws ServletException, IOException {
      final String pathInfo = pRequest.getPathInfo();
      if ((pathInfo!=null) && (pathInfo.startsWith( "/webdav" ))) {

//        debugPrintRequestHeader( pRequest );
        try {
          if (mThreadCount>MAXTHREADCOUNT) {
            pResponse.sendError( pResponse.SC_SERVICE_UNAVAILABLE );
            gLog.log( Level.WARNING, "SC_SERVICE_UNAVAILABLE", (Exception)null );
            return;
          }
          mThreadCount++;
          CARS_ActionContext ac = createActionContext( pRequest, pResponse );
          if (ac!=null) {
            try {
              ac.setContextPath( pRequest.getContextPath() + pRequest.getServletPath() );
              String path = new String( pathInfo.getBytes( "ISO-8859-1" ), "UTF-8" );
//              String path = new String( pathInfo.substring( "/webdav".length() ).getBytes( "ISO-8859-1" ), "UTF-8" );
              ac.setPathInfo( path );
              ac.setQueryString( pRequest.getQueryString() );
              ac.setBaseURL( pRequest.getScheme() + "://" + pRequest.getServerName()  + ":" + pRequest.getServerPort() );
  //            JeCARS_WebDAVServlet webdav = new JeCARS_WebDAVServlet();
              gWebdav.service( pRequest, pResponse, ac, mCARSFactory );
            } catch( AccessDeniedException ade ) {
              // **** Not allowed, so report (s)he's unauthorized
              pResponse.setHeader( "WWW-Authenticate", "BASIC realm=\"JeCARS\"" );
              pResponse.sendError( pResponse.SC_UNAUTHORIZED );
            } catch (CredentialExpiredException cee) {
              pResponse.setHeader( "Location", cee.getMessage() );
              pResponse.setStatus( HttpURLConnection.HTTP_MOVED_TEMP );
            } finally {
              ac.destroy();
            }
          } else {
          }
        } catch (Exception e) {
          gLog.log( Level.WARNING, null, e );
        } finally {
          mThreadCount--;
        }
        return;
      }
      super.service( pRequest, pResponse );
      return;
    }

    
    
    @Override   
    protected long getLastModified( HttpServletRequest req ) {
      return super.getLastModified( req );
    }
    
    
    /** Returns a short description of the servlet.
     */
     
    @Override
    public String getServletInfo() {
      return "JeCARS REST webservice servlet";
    }
    // </editor-fold>
}
