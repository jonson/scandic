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
