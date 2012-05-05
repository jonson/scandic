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

/**
 * Triggered when a fatal error occurs while parsing the html.
 * 
 * This will likely be cause by Scandic updating the html on the site.
 * 
 * @author jon
 *
 */
public class ScandicHtmlException extends RuntimeException {

	public ScandicHtmlException() {
	}

	public ScandicHtmlException(String detailMessage) {
		super(detailMessage);
	}

	public ScandicHtmlException(Throwable throwable) {
		super(throwable);
	}

	public ScandicHtmlException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}

}
