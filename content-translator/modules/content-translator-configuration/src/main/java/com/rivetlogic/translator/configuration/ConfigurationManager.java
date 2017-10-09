package com.rivetlogic.translator.configuration;

import java.util.Map;

import com.liferay.portal.configuration.metatype.bnd.util.ConfigurableUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;

@Component(
		configurationPid = "com.rivetlogic.translator.configuration.TranslatorConfiguration",
		service = ConfigurationManager.class,
		immediate = true
)
public class ConfigurationManager {
	private static final Log LOG = LogFactoryUtil.getLog(ConfigurationManager.class);
			
	public String getYandexApiKey() {
		return _configuration.yandexApiKey();
	}

	@Activate
	@Modified
	protected void activate(Map<String, Object> properties) {
		_configuration = ConfigurableUtil.createConfigurable(
				TranslatorConfiguration.class, properties);
		LOG.info("Configuration change");
	}

	private volatile TranslatorConfiguration _configuration;
}