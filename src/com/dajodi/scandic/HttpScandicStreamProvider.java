package com.dajodi.scandic;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.CharArrayBuffer;
import org.apache.http.util.EntityUtils;

public class HttpScandicStreamProvider {

    private final HttpClient httpClient;

    public HttpScandicStreamProvider(HttpClient client) {
        httpClient = client;
    }


    
    
    public static String toString(
            final InputStream instream) throws IOException, ParseException {
        
        Reader reader = new InputStreamReader(instream, HTTP.ISO_8859_1);
        CharArrayBuffer buffer = new CharArrayBuffer(10 * 1024); 
        try {
            char[] tmp = new char[1024];
            int l;
            while((l = reader.read(tmp)) != -1) {
                buffer.append(tmp, 0, l);
            }
        } finally {
            reader.close();
        }
        return buffer.toString();
    }
}
