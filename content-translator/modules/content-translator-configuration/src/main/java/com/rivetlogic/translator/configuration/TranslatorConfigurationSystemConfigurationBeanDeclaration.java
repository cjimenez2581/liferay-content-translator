package com.rivetlogic.translator.configuration;

import com.liferay.portal.kernel.settings.definition.ConfigurationBeanDeclaration;

public class TranslatorConfigurationSystemConfigurationBeanDeclaration implements ConfigurationBeanDeclaration {
	@Override
	public Class<?> getConfigurationBeanClass() {
		return TranslatorConfiguration.class;
	}

}
