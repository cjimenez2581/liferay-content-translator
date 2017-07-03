package com.rivetlogic.translator.util;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.rivetlogic.translator.api.TranslatorException;
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
    @SuppressWarnings("unused")
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
        return api;
    }
    
    /**
     * 
     * @param text
     * @param from
     * @param to Hebrew will be corrected from "iw" to "he", as liferay uses "iw"
     * @return
     * @throws TranslatorException
     */
    public String translate(String text, String from, String to) throws TranslatorException{
    	if( to.equalsIgnoreCase("iw") )
    		to = "he";
    	return api.translate(text, from, to);
    }
    
    /**
     * 
     * @param targetLang
     * @param baseLanguages Receives baseLanguages as an array of Liferay's Language IDs
     * @return
     */
    public boolean canTranslateTo(String targetLang, String[] baseLanguages){
    	if( targetLang.equalsIgnoreCase("iw") )
    		targetLang = "he";
    	for (int ii = 0; ii < baseLanguages.length; ii++) {
			baseLanguages[ii] = baseLanguages[ii].split("_")[0];
		}
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
    	LOG.info("Config is set "+this.config);
    	refreshApiKey();
    }
    
    public synchronized void unsetConfig(ConfigurationManager config){
    	if (this.config == config) {
            this.config = null;
            LOG.info("Config is unset "+this.config);
        }
    }
    
    @Reference(
    		service = YandexTranslatorAPI.class,
    		cardinality = ReferenceCardinality.MANDATORY,
    		policy = ReferencePolicy.STATIC,
    		unbind = "unsetYandexTranslatorAPI"
	)
    public synchronized void setYandexTranslatorAPI(YandexTranslatorAPI api){
    	this.api = api;
    	LOG.info("Yandex API is set "+this.api);
    	refreshApiKey();
    }
    
    public synchronized void unsetYandexTranslatorAPI(YandexTranslatorAPI api){
    	if (this.api == api) {
            this.api = null;
            LOG.info("Yandex API is unset "+this.api);
        }
    }
    
    public synchronized void refreshApiKey() {
    	if( this.api!=null && this.config != null){
			this.api.setApiKey( this.config.getYandexApiKey() );
			//LOG.info("Yandex API key="+this.api.getApiKey());
		}
    }
    
    private YandexTranslatorAPI api;
    private ConfigurationManager config;
}
