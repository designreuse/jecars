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

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * @author weert
 */
public class JC_LocalURLEmul extends HttpURLConnection {

    private String mURL = "";

    public JC_LocalURLEmul( final URL u ) {
      super( u );
    }

    /** setMURL
     *
     * @param pURL
     */
    public void setMURL( final String pURL ) {
      mURL = pURL;
      return;
    }

    public String getMURL() {
      return mURL;
    }



    @Override
    public void disconnect() {
      return;
    }

    @Override
    public boolean usingProxy() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void connect() throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
