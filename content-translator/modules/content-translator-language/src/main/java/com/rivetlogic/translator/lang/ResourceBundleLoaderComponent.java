package com.rivetlogic.translator.lang;

import com.liferay.portal.kernel.util.AggregateResourceBundleLoader;
import com.liferay.portal.kernel.util.CacheResourceBundleLoader;
import com.liferay.portal.kernel.util.ClassResourceBundleLoader;
import com.liferay.portal.kernel.util.ResourceBundleLoader;

import java.util.Locale;
import java.util.ResourceBundle;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(
		immediate = true,
		property = {
			"bundle.symbolic.name=com.liferay.journal.lang",
			"resource.bundle.base.name=content.Language",
			"servlet.context.name=journal-web"
		}
	)
public class ResourceBundleLoaderComponent implements ResourceBundleLoader {

	@Override
	public ResourceBundle loadResourceBundle(String languageId) {
		return _resourceBundleLoader.loadResourceBundle(languageId);
	}
	
	public ResourceBundle loadResourceBundle(Locale locale) {
		 return _resourceBundleLoader.loadResourceBundle(locale.toString());
	}


	@Reference(target = "(&(bundle.symbolic.name=com.liferay.journal.lang)(!(component.name=com.rivetlogic.translator.lang.ResourceBundleLoaderComponent)))")
	public void setResourceBundleLoader(
		ResourceBundleLoader resourceBundleLoader) {

		_resourceBundleLoader = new AggregateResourceBundleLoader(
			new CacheResourceBundleLoader(
				new ClassResourceBundleLoader(
					"content.Language",
					ResourceBundleLoaderComponent.class.getClassLoader())),
			resourceBundleLoader);
	}

	private AggregateResourceBundleLoader _resourceBundleLoader;

}