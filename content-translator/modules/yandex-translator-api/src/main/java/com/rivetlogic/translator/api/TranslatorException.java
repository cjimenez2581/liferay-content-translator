package com.rivetlogic.translator.api;

public class TranslatorException extends Exception {
	public static enum Type{
		UNKNOWN, UNSUPPORTED, API_AUTH, BAD_FORMAT;
	}

    /**
     * @author joseross
     */
    private static final long serialVersionUID = 1L;
    private Type type = Type.UNKNOWN;

    public TranslatorException(Type type, String message, Throwable throwable) {
        super(message, throwable);
        this.type = type;
    }
    
    public TranslatorException(Type type, String message) {
        this(type, message, null);
    }
    
    public Type getType(){ return type; }
}
