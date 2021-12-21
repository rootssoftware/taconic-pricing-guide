package be.roots.taconic.pricingguide.util;

/*
   This file is part of the Taconic Pricing Guide generator.  This code will
   generate a full featured PDF Pricing Guide by using using iText
   (http://www.itextpdf.com) based on JSON files.

   Copyright (C) 2015  Roots nv
   Authors: Koen Dehaen (koen.dehaen@roots.be)

   This program is free software: you can redistribute it and/or modify
   it under the terms of the GNU Affero General Public License as
   published by the Free Software Foundation, either version 3 of the
   License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU Affero General Public License for more details.

   You should have received a copy of the GNU Affero General Public License
   along with this program.  If not, see <http://www.gnu.org/licenses/>.

   For more information, please contact Roots nv at this address: support@roots.be
 */

import com.itextpdf.xmp.impl.Base64;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class HttpUtil {

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(HttpUtil.class);
    private static final String REQUEST_METHOD = "GET";
    private static final int CONNECT_TIMEOUT = 10000;

    public static String readString(String urlAsString, String urlBase) {
        return readString(urlAsString, urlBase, null, null);
    }

    public static String readString(String urlAsString, String urlBase, String userName, String password) {

        try {
            final HttpURLConnection con = getInputStreamFor(urlAsString, urlBase, userName, password);
            final BufferedInputStream in = new BufferedInputStream(con.getInputStream());
            final String response = IOUtils.toString(in, StandardCharsets.UTF_8);
            IOUtils.closeQuietly(in);
            return response;
        } catch ( IOException e ) {
            LOGGER.error ( e.getLocalizedMessage(), e );
        }
        return null;

    }

    public static byte[] readByteArray(String urlAsString, String urlBase, String userName, String password) {

        try {
            final HttpURLConnection con = getInputStreamFor(urlAsString, urlBase, userName, password);
            final BufferedInputStream in = new BufferedInputStream(con.getInputStream());
            final byte[] response = IOUtils.toByteArray(in);
            IOUtils.closeQuietly(in);
            return response;
        } catch ( IOException e ) {
            LOGGER.error ( e.getLocalizedMessage(), e );
        }
        return null;
    }

    private static HttpURLConnection getInputStreamFor(String urlAsString, String urlBase, String userName, String password) throws IOException {
        LOGGER.info(REQUEST_METHOD + "ting data from url: " + urlAsString + " as " + userName + " with timeout set to " + CONNECT_TIMEOUT );

        final URL url = new URL(urlAsString.replaceAll("http://localhost", urlBase));
        final HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod(REQUEST_METHOD);
        con.setConnectTimeout(CONNECT_TIMEOUT);

        if ( userName != null || password != null ) {
            final String encoded = Base64.encode(userName + ":" + password);
            con.setRequestProperty("Authorization", "Basic "+encoded);
        }

        final int responseCode = con.getResponseCode();

        LOGGER.info("Response code: " + responseCode);
        return con;
    }

}