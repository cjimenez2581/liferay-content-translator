package com.rivetlogic.translator.util;

/**
 * 
 * @author joseross
 *
 */
public interface WebKeys extends com.liferay.portal.kernel.util.WebKeys {
    
    // Control Panel keys
    public static final String TRANSLATOR_CLIENT_ID = "microsoft.translator.clientid";
    public static final String TRANSLATOR_CLIENT_SECRET = "microsoft.translator.clientsecret";
    public static final String TRANSLATOR_API_URL = "microsoft.translator.apiurl";
    public static final String TRANSLATOR_AUTH_URL = "microsoft.translator.authurl";
    
    // Request keys
    public static final String TRANSLATOR_DISABLE = "translatorDisable";
    public static final String TRANSLATOR_CLOSE = "translatorClose";
    public static final String TRANSLATOR_LANGUAGES = "translatorLanguages";
    
    // Messages keys
    public static final String TRANSLATOR_ERROR_KEY = "translator-error";
    public static final String TRANSLATOR_AUTH_ERROR = "translator-auth-error";

}
