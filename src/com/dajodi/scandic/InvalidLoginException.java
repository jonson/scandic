package com.dajodi.scandic;

/**
 * Triggered when a login attempt fails. 
 * 
 * @author jon
 *
 */
public class InvalidLoginException extends ScandicHtmlException {

    public InvalidLoginException() {
    }

    public InvalidLoginException(String detailMessage) {
        super(detailMessage);
    }

    public InvalidLoginException(Throwable throwable) {
        super(throwable);
    }

    public InvalidLoginException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }
}
