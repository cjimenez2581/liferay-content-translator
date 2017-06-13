package com.rivetlogic.translator.api;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.osgi.service.component.annotations.Component;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.rivetlogic.translator.api.TranslatorException.Type;

/**
 * @author emmanuelabarca
 */
@Component(
		service = YandexTranslatorAPI.class,
		immediate = true
)
public class YandexTranslatorAPI {
	private static final String ENCODING = "UTF-8";
	private static final String BASE_URL = "https://translate.yandex.net/api/v1.5/tr.json";
	private static final String GET_LANGUAGES_PATH = "/getLangs";
	private static final String TRANSLATE_PATH = "/translate";

	protected String apiKey;
	private final Supplier<Set<String>> translationsMap =
			Suppliers.memoizeWithExpiration(translationsListSupplier(), 12, TimeUnit.HOURS);

	/**
	 * A map where the key represents the source language and value the target language
	 * @return null if there was an error
	 */
	public Set<String> getTranslationsList(){
		return translationsMap.get();
	}

	private Supplier<Set<String>> translationsListSupplier(){
		return new Supplier<Set<String>>() {
			@Override
			public Set<String> get() {
				try {
					URI getLanguagesURI = new URIBuilder(BASE_URL + GET_LANGUAGES_PATH)
							.build()
							.normalize();
					// POST arguments
					List<NameValuePair> arguments = new ArrayList<NameValuePair>();
					arguments.add( new BasicNameValuePair( "key", getApiKey() ) );
					arguments.add( new BasicNameValuePair( "ui", "en" ) );
					JSONObject response = postRequest(getLanguagesURI, arguments);
					JSONArray translationPairsJSON = (JSONArray) response.get("dirs");
					Multimap<String, String> translations = HashMultimap.create();
					Set<String> languages = new HashSet<>();
					for(Object obj : translationPairsJSON){
						String str = (String) obj;
						String[] parts = str.split("-");
						translations.put( parts[0], parts[1] );
					}
					
					JSONObject languagesJSON = (JSONObject) response.get("langs");
					for( Object obj : languagesJSON.keySet() ){
						String str = (String) obj;
						languages.add( str );
					}

					return languages;
				} catch (URISyntaxException e) {
					e.printStackTrace();
					return null;
				}
			}
		};
	}

	/**
	 * Translate the text from source language to target language.
	 * @param text Text to be translated.
	 * @param from Source language.
	 * @param to Target language.
	 * @return The contents of the InputStream as a String.
	 * @throws Exception on error.
	 */
	public String translate(String text, String from, String to) throws TranslatorException{
		if(text==null)
			return null;
		if(text.trim().isEmpty())
			return text;
		try {
			URI translateURI = new URIBuilder(BASE_URL + TRANSLATE_PATH)
					.build()
					.normalize();
			// POST arguments
			List<NameValuePair> arguments = new ArrayList<NameValuePair>();
			arguments.add( new BasicNameValuePair( "key", getApiKey() ) );
			arguments.add( new BasicNameValuePair( "lang", from+"-"+to ) );
			arguments.add( new BasicNameValuePair( "text", text ) );
			arguments.add( new BasicNameValuePair( "format", "html" ) );
			JSONObject response = postRequest(translateURI, arguments);
			if(response.containsKey("code")) {
				long code = (long) response.get("code");
				if( code==401 ) {
					throw new TranslatorException(Type.API_AUTH, "[yandex-translator-api] "+code+": "+ response.getOrDefault("message", "Probably api key auth error.").toString() );
				}else if( code!=200 ){
					throw new TranslatorException(Type.UNSUPPORTED, "[yandex-translator-api] "+code+": Error retrieving translation, probably the specified translation direction is not supported. ");
				}
			}
			JSONArray translations = (JSONArray) response.get("text");

			return (String) translations.get(0);
		} catch (URISyntaxException ex) {
			throw new TranslatorException(Type.BAD_FORMAT, "[yandex-translator-api] Error retrieving translation : " + ex.getMessage(), ex);
		}
	}
	
	public boolean canTranslateTo(String targetLang, String[] baseLanguages){
		Set<String> translationMap = getTranslationsList();
    	/*for (String lang : baseLanguages) {
			if(translationMap.containsEntry(lang, targetLang)) {
				return true;
			}
		}
    	return false;*/
		return translationMap.contains(targetLang);
    }

	protected JSONObject postRequest(URI uri, List<NameValuePair> arguments){
		HttpClient client = HttpClientBuilder.create().build();
		HttpPost post = new HttpPost(uri);
		post.addHeader("Content-Type","application/x-www-form-urlencoded; charset=" + ENCODING);
		post.addHeader("Accept-Charset", ENCODING);

		String response = "";
		try {
			post.setEntity(new UrlEncodedFormEntity(arguments, ENCODING));
			HttpResponse httpResponse = client.execute(post);

			response = EntityUtils.toString(httpResponse.getEntity());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return (JSONObject) JSONValue.parse( response );
	}

	public void setApiKey(String apiKey){ this.apiKey = apiKey; }
	public String getApiKey(){ return this.apiKey; }
}