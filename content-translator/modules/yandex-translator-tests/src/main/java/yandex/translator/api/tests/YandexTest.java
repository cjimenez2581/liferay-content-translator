package yandex.translator.api.tests;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Set;

import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.collect.Multimap;
import com.rivetlogic.translator.api.TranslatorException;
import com.rivetlogic.translator.api.YandexTranslatorAPI;

public class YandexTest {
	private static String apiKey;

	@Test
	public void testNullAPIKey() {
		assertNotNull("Null API key", apiKey);
	}
	
	@Test
	public void testGetLanguages(){
		YandexTranslatorAPI yandex = new YandexTranslatorAPI();
		yandex.setApiKey(apiKey);
		//Multimap<String, String> translations = yandex.getTranslationsList();
		//assertTrue("English to German translation doesn't appear", translations.containsEntry("en", "de") );
		Set<String> languages = yandex.getTranslationsList();
		assertTrue("English doesn't appear", languages.contains("en") );
		assertTrue("German doesn't appear", languages.contains("de") );
		assertTrue("Hebrew doesn't appear", languages.contains("he") );
		assertTrue("Chinese doesn't appear", languages.contains("zh") );
	}
	
	@Test
	public void testTranslateEnDeHTML(){
		YandexTranslatorAPI yandex = new YandexTranslatorAPI();
		yandex.setApiKey(apiKey);
		try {
			String translation = yandex.translate("<section>\n<span><a>milk</a></span>\n</section>", "en", "de");
			assertEquals("Failed translating english to german with HTML", "<section>\n<span><a>Milch</a></span>\n</section>", translation);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test (expected = TranslatorException.class)
	public void testUnavailable() throws TranslatorException{
		YandexTranslatorAPI yandex = new YandexTranslatorAPI();
		yandex.setApiKey(apiKey);
		
		@SuppressWarnings("unused")
		String translation = yandex.translate("text", "en", "xyz");
	}

	@BeforeClass
	public static void setUp(){
		if(apiKey == null){
			System.out.println("Please type your Yandex API key");
			BufferedReader buffer=new BufferedReader(new InputStreamReader(System.in));
			try {
				apiKey = buffer.readLine();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}