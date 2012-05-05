/*
 * Copyright 2012 - Jon DeYoung
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.dajodi.scandic;

import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;


public enum Singleton {

	
	INSTANCE;
	
	/**
	 * Indicates whether or not we are in production mode.
	 * In production mode, no tracking is done.
	 */
	private final boolean production = true;
	
	private final DefaultHttpClient httpClient;
	private final HtmlScraper scraper;
	
	private Singleton() {
		// should use the default driver if possible
		this.httpClient = creatHttpClient();
		
//		java.util.logging.Logger.getLogger("org.apache.http.wire").setLevel(java.util.logging.Level.FINEST);
//		java.util.logging.Logger.getLogger("org.apache.http.headers").setLevel(java.util.logging.Level.FINEST);

//		System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.SimpleLog");
//		System.setProperty("org.apache.commons.logging.simplelog.showdatetime", "true");
//		System.setProperty("org.apache.commons.logging.simplelog.log.httpclient.wire", "warn");
//		System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.http", "debug");
//		System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.http.headers", "debug");
//		System.setProperty("org.apache.http.client.protocol.RequestAddCookies", "debug");
//		System.setProperty("org.apache.http.client.protocol.ResponseProcessCookies", "debug");

		scraper = new JSoupScraper();
	}
	
	private DefaultHttpClient creatHttpClient() {
		HttpParams parms = new BasicHttpParams();
		SchemeRegistry registry = new SchemeRegistry();
		registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
		registry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
		SingleClientConnManager manager = new SingleClientConnManager(parms, registry);
		DefaultHttpClient client = new DefaultHttpClient(manager, parms);
		return client;
	}
	
	public DefaultHttpClient getHttpClient() {
		return this.httpClient;
	}
	
	public HtmlScraper getScraper() {
		return this.scraper;
	}
	
}