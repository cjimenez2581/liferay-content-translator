package com.rivetlogic.translator.configuration;

import com.liferay.portal.configuration.metatype.annotations.ExtendedObjectClassDefinition;

import aQute.bnd.annotation.metatype.Meta;

/**
 * @author emmanuelabarca
 */
@ExtendedObjectClassDefinition(
		category = "other",
		scope=ExtendedObjectClassDefinition.Scope.SYSTEM
)
@Meta.OCD(
		id = "com.rivetlogic.translator.configuration.TranslatorConfiguration",
		localization = "content/Language", name = "translator-configuration-name"
)
public interface TranslatorConfiguration {

    @Meta.AD(
        required = true,
        name = "translator.configuration.yandex.api.name",
        description = "translator.configuration.yandex.api.desc"
    )
    public String yandexApiKey();
}