package com.rivetlogic.translator.action;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletContext;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.PortletSession;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.liferay.dynamic.data.mapping.model.DDMStructure;
import com.liferay.dynamic.data.mapping.service.DDMStructureLocalServiceUtil;
import com.liferay.dynamic.data.mapping.storage.Field;
import com.liferay.dynamic.data.mapping.storage.Fields;
import com.liferay.dynamic.data.mapping.util.DDMUtil;
import com.liferay.journal.constants.JournalPortletKeys;
import com.liferay.journal.model.JournalArticle;
import com.liferay.journal.service.JournalArticleServiceUtil;
import com.liferay.journal.util.JournalConverter;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.portlet.PortletRequestModel;
import com.liferay.portal.kernel.portlet.bridges.mvc.BaseMVCActionCommand;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCActionCommand;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ServiceContextFactory;
import com.liferay.portal.kernel.servlet.SessionErrors;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.upload.UploadPortletRequest;
import com.liferay.portal.kernel.util.HttpUtil;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.LocalizationUtil;
import com.liferay.portal.kernel.util.MapUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.xml.Document;
import com.liferay.portal.kernel.xml.Element;
import com.liferay.portal.kernel.xml.SAXReaderUtil;
import com.rivetlogic.translator.api.TranslatorException;
import com.rivetlogic.translator.api.YandexTranslatorAPI;
import com.rivetlogic.translator.ui.WebKeys;
import com.rivetlogic.translator.util.AutomaticTranslatorUtil;

@Component(
    immediate = true,
    property = {
		"javax.portlet.name=" + JournalPortletKeys.JOURNAL,
		"mvc.command.name=automaticTranslate"
    },
    service = MVCActionCommand.class
)
public class AutomaticTranslatorAction extends BaseMVCActionCommand {

    private static final Log LOG = LogFactoryUtil.getLog(AutomaticTranslatorAction.class);

	@Override
	protected void doProcessAction(ActionRequest actionRequest, ActionResponse actionResponse) throws Exception {
		ThemeDisplay themeDisplay = (ThemeDisplay) actionRequest.getAttribute(WebKeys.THEME_DISPLAY);
		PortletRequestModel portletRequestModel = new PortletRequestModel(actionRequest, actionResponse);
		UploadPortletRequest uploadPortletRequest =
                PortalUtil.getUploadPortletRequest(actionRequest);
        LOG.debug("Translate article " + MapUtil.toString(uploadPortletRequest.getParameterMap()));
        
        
        String redirect = ParamUtil.getString(actionRequest, "redirect");
        String portletId = HttpUtil.getParameter(redirect, "p_p_id", false);
        String namespace = PortalUtil.getPortletNamespace(portletId);
        ServiceContext serviceContext = ServiceContextFactory.getInstance(
                JournalArticle.class.getName(), uploadPortletRequest);
        
        String articleId = ParamUtil.getString(actionRequest, "articleId");
        long groupId = ParamUtil.getLong(actionRequest, "groupId");
        double version = ParamUtil.getDouble(uploadPortletRequest, "version");
        String[] toLanguageIds = ParamUtil.getParameterValues(uploadPortletRequest, "selectedLanguages");
        
        JournalArticle article = JournalArticleServiceUtil.getArticle(
                groupId, articleId, version);
        String structureId = ParamUtil.getString(uploadPortletRequest, "structureId");
        LOG.debug("Structure key: "+structureId);
        DDMStructure ddmStructure = null;
        
        String defaultLanguageId = article.getDefaultLanguageId();
        System.out.println("Debugging");
        Locale defaultLocale = LocaleUtil.fromLanguageId(defaultLanguageId);
        System.out.println(defaultLanguageId);
        System.out.println(defaultLocale);
        //String content = article.getContentByLocale(defaultLanguageId);
        String content = JournalArticleServiceUtil.getArticleContent(groupId, articleId, version, defaultLanguageId, portletRequestModel, themeDisplay);
        System.out.println(content);
        String title = article.getTitle(defaultLocale);
        String description = article.getDescription(defaultLocale);
        LOG.debug("Title: " + title);
        LOG.debug("Description: " + description);
        LOG.debug("Content: " + content);
        
        Map<String, byte[]> images = new HashMap<String, byte[]>();
        YandexTranslatorAPI translateUtil = automaticTranslatorUtil.getTranslateAPI();
        LOG.debug(defaultLanguageId);
        LOG.debug(Arrays.toString(toLanguageIds));
        try {
            for(String languageId : toLanguageIds) {
                Locale locale = LocaleUtil.fromLanguageId(languageId);
                LOG.debug("Version: " + version);
                
                LOG.debug("Translate: " + title + " from: " + defaultLocale.getLanguage() + " to: " + locale.getLanguage());
                String newTitle = translateUtil.translate(title, defaultLocale.getLanguage(), locale.getLanguage());
                LOG.debug("Result: " + newTitle);
                
                LOG.debug("Translate: " + description + " from: " + defaultLocale.getLanguage() + " to: " + locale.getLanguage());
                String newDescription = translateUtil.translate(description, defaultLocale.getLanguage(), locale.getLanguage());
                LOG.debug("Result: " + newDescription);
                
                if (Validator.isNull(structureId)) {
                    LOG.debug("structureId is null");
                    if (!article.isTemplateDriven()) {
                        String curContent = StringPool.BLANK;
                        
                        Document document = SAXReaderUtil.read(content);
    
                        Element rootElement = document.getRootElement();
                        List<Element> staticContentElements = rootElement.elements(
                                "static-content");
                        
                        Element staticContentElement = staticContentElements.get(0);
                        curContent = article.getContent();
                        LOG.debug("Translate: " + staticContentElement.getText() + " from: " + defaultLocale.getLanguage() + " to: " + locale.getLanguage());
                        String newContent = translateUtil.translate(staticContentElement.getText(), defaultLocale.getLanguage(), locale.getLanguage());
                        LOG.debug("Result: " + newContent);
                        
                        content = LocalizationUtil.updateLocalization(
                                curContent, "static-content", newContent, languageId,
                                defaultLanguageId, true, true);
                        LOG.debug("New content: " + content);
    
                    }
                } else {
                    ddmStructure = DDMStructureLocalServiceUtil.getStructure(
                    		PortalUtil.getSiteGroupId(groupId),
                            PortalUtil.getClassNameId(JournalArticle.class), structureId,
                            true);
                
                    if (article.isTemplateDriven()) {
                        Fields newFields = DDMUtil.getFields(
                            ddmStructure.getStructureId(), serviceContext);
        
                        Fields existingFields = _journalConverter.getDDMFields(
                            ddmStructure, article.getContent());
                        LOG.debug(serviceContext);
                        LOG.debug("NewFields: "+newFields.getNames()); // TODO Empty
                        LOG.debug("ExistingFields: "+existingFields.getNames());
                        LOG.debug("NewFields num: "+newFields.getNames().size());
                        
                        Fields mergedFields = existingFields;
                        if( newFields.getNames().size()>0 ) try {
                        	mergedFields = DDMUtil.mergeFields( //TODO current exception
                        		newFields, existingFields);
                        } catch(Exception e){
                        	LOG.warn("Error merging fields", e);
                        }
        
                        content = _journalConverter.getContent(
                            ddmStructure, mergedFields);
                        
                        LOG.debug("Old Content: " + content);
                        
                        for(Field field : mergedFields) {
                            LOG.debug(field.getValue(defaultLocale));
                            if(field.getType().contains("text")) {
                                String currValue = String.valueOf(field.getValue(defaultLocale));
                                LOG.debug("Field Value: " + currValue);
                                String newValue = translateUtil.translate(currValue, defaultLocale.getLanguage(), locale.getLanguage());
                                field.setValue(locale, newValue);
                                LOG.debug("Field new Value: " + newValue);
                            }
                        }
                        
                        content = _journalConverter.getContent(
                                ddmStructure, mergedFields);
                        LOG.debug("New Content: " + content);
                        
                    }
                }
                
                article = JournalArticleServiceUtil.updateArticleTranslation(
                        groupId, articleId, version, locale, newTitle, newDescription,
                        content, images, serviceContext);
                serviceContext.setFormDate(new Date());
                version = article.getVersion();
                LOG.debug("New version: " + version);
            }
            
            actionResponse.setRenderParameter(WebKeys.TRANSLATOR_CLOSE, "true");
            actionResponse.setRenderParameter("mvcPath", "/content-translator/translate.jsp");
        } catch(TranslatorException e) {
            SessionErrors.add(actionRequest, WebKeys.TRANSLATOR_ERROR_KEY);
            LOG.error(e.getMessage());
        }
		
		
        System.out.println("Translated");
		// Render
		PortletSession portletSession =
				actionRequest.getPortletSession();
		PortletContext portletContext =
				portletSession.getPortletContext();
		PortletRequestDispatcher portletRequestDispatcher =
				portletContext.getRequestDispatcher("/content-translator/translate.jsp");

		portletRequestDispatcher.include(actionRequest, actionResponse);
	}

	
	@Reference(unbind = "-")
	protected void setAutomaticTranslatorUtil(AutomaticTranslatorUtil automaticTranslatorUtil) {
		this.automaticTranslatorUtil = automaticTranslatorUtil;
	}
	
	@Reference(unbind = "-")
	protected void setJournalConverter(JournalConverter journalConverter) {
		_journalConverter = journalConverter;
	}
	
	private AutomaticTranslatorUtil automaticTranslatorUtil;
	private JournalConverter _journalConverter;
}
