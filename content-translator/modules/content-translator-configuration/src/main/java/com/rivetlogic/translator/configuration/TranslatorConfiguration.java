package com.rivetlogic.translator.configuration;

import aQute.bnd.annotation.metatype.Meta;

/**
 * @author emmanuelabarca
 */
@Meta.OCD(id = "com.rivetlogic.Translator")
public interface TranslatorConfiguration {

    @Meta.AD(
        required = true,
        name = "Yandex API Key",
        description = "It can be obtained here https://translate.yandex.com/developers/keys"
    )
    public String yandexApiKey();
}