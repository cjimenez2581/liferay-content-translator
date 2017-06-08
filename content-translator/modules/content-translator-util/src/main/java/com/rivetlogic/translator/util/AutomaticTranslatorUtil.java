package com.rivetlogic.translator.util;

import java.util.Arrays;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.rivetlogic.translator.api.YandexTranslatorAPI;
import com.rivetlogic.translator.configuration.ConfigurationManager;;

/**
 * Utility class to get the credentials from the control panel.
 * @author joseross
 * @author emmanuelabarca
 *
 */
@Component(
		service = AutomaticTranslatorUtil.class,
		immediate = true
)
public class AutomaticTranslatorUtil {
    private static final Log LOG = LogFactoryUtil.getLog(AutomaticTranslatorUtil.class);

    /**
     * Used to check if the values of the credentials in the control panel are valid.
     */
    public boolean validateCredentials() {
        String apiKey = getAPIKey();
        return !apiKey.equals(StringPool.BLANK) && !apiKey.equals("CLIENT_ID");
    }
    
    /**
     * Used to get an instance of TranslatorAPI, using the credentials from the control panel.
     */
    public YandexTranslatorAPI getTranslateAPI() {
        LOG.debug("Getting preferences from control panel");
        
        YandexTranslatorAPI translator = new YandexTranslatorAPI();
        translator.setApiKey(
        		config.getYandexApiKey() );
        return translator;
    }
    
    /**
     * 
     * @param targetLang
     * @param baseLanguages Receives baseLanguages as an array of Liferay's Language IDs
     * @return
     */
    public boolean canTranslateTo(String targetLang, String[] baseLanguages){
    	for (int ii = 0; ii < baseLanguages.length; ii++) {
			baseLanguages[ii] = baseLanguages[ii].split("_")[0];
		}
    	System.out.println("Can translate to "+targetLang+" from "+Arrays.toString(baseLanguages)+"?\n"
    			+ getTranslateAPI().canTranslateTo(targetLang, baseLanguages));
    	return getTranslateAPI().canTranslateTo(targetLang, baseLanguages);
    }
    
    /**
     * Get the value for ClientId from the control panel
     */
    private String getAPIKey() {
        return config.getYandexApiKey();
    }
    
    @Reference(
    		service = ConfigurationManager.class,
    		cardinality = ReferenceCardinality.MANDATORY,
    		policy = ReferencePolicy.STATIC,
    		unbind = "unsetConfig"
	)
    public synchronized void setConfig(ConfigurationManager config){
    	this.config = config;
    }
    
    public synchronized void unsetConfig(ConfigurationManager config){
    	if (this.config == config) {
            this.config = null;
        }
    }
    
    private ConfigurationManager config;
}
