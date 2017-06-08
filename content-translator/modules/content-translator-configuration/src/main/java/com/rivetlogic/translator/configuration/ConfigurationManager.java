package com.rivetlogic.translator.configuration;

import java.util.Map;

import com.liferay.portal.configuration.metatype.bnd.util.ConfigurableUtil;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;

@Component(
		configurationPid = "com.rivetlogic.Translator",
		service = ConfigurationManager.class,
		immediate = true
)
public class ConfigurationManager {
	public String getYandexApiKey() {
		return _configuration.yandexApiKey();
	}

	@Activate
	@Modified
	protected void activate(Map<String, Object> properties) {
		_configuration = ConfigurableUtil.createConfigurable(
				TranslatorConfiguration.class, properties);
	}

	private volatile TranslatorConfiguration _configuration;
}