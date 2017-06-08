package com.rivetlogic.translator.api;

public class TranslatorException extends Exception {

    /**
     * @author joseross
     */
    private static final long serialVersionUID = 1L;

    public TranslatorException(String message, Throwable throwable) {
        super(message, throwable);
    }
    
    public TranslatorException(String message) {
        this(message, null);
    }
    
}
