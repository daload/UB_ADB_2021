package org.adbs.dbxic.utils;

public class DBxicException extends Exception {

    /**
     * Constructor: Generates a new exception.
     *
     * @param msg this server exception's message.
     */
    public DBxicException(String msg) {
        super(msg);
    } // DBxicRunException()


    /**
     * Constructor: generates a new throwable exception.
     *
     * @param msg the error message.
     * @param e the originating throwable.
     */
    public DBxicException(String msg, Throwable e) {
        super(msg, e);
    } // DBxicRunException()

} // DBxicRunException
