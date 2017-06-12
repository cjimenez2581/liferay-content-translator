package com.rivetlogic.translator.action;

import com.liferay.journal.constants.JournalPortletKeys;
import com.liferay.journal.model.JournalArticle;
import com.liferay.journal.service.JournalArticleLocalServiceUtil;
import com.liferay.journal.util.JournalConverter;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCActionCommand;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCRenderCommand;
import com.liferay.portal.kernel.servlet.SessionErrors;
import com.liferay.portal.kernel.struts.BaseStrutsPortletAction;
import com.liferay.portal.kernel.struts.StrutsPortletAction;
import com.liferay.portal.kernel.upload.UploadPortletRequest;
import com.liferay.portal.kernel.util.HttpUtil;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.LocalizationUtil;
import com.liferay.portal.kernel.util.MapUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.kernel.xml.Document;
import com.liferay.portal.kernel.xml.Element;
import com.liferay.portal.kernel.xml.SAXReaderUtil;
import com.rivetlogic.translator.api.TranslatorException;
import com.rivetlogic.translator.ui.WebKeys;
import com.rivetlogic.translator.util.AutomaticTranslatorUtil;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletConfig;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(
	    immediate = true,
	    property = {
			"javax.portlet.name=" + JournalPortletKeys.JOURNAL,
			"mvc.command.name=automaticTranslate"
	    },
	    service = MVCRenderCommand.class
	)
public class AutomaticTranslatorRender implements MVCRenderCommand {
    private static final Log LOG = LogFactoryUtil.getLog(AutomaticTranslatorRender.class);
    
    @Override
	public String render(RenderRequest renderRequest, RenderResponse renderResponse) throws PortletException {
    	String articleId = ParamUtil.getString(renderRequest, "articleId");
        long groupId = ParamUtil.getLong(renderRequest, "groupId");
        int status = ParamUtil.getInteger(renderRequest, "status", WorkflowConstants.STATUS_ANY);
		try {
			JournalArticle article = JournalArticleLocalServiceUtil.getLatestArticle(groupId, articleId, status);
			
			renderRequest.setAttribute(WebKeys.JOURNAL_ARTICLE, article);
		} catch (PortalException e) {
			SessionErrors.add(renderRequest, WebKeys.TRANSLATOR_ERROR_KEY);
            LOG.error(e.getMessage());
			e.printStackTrace();
		}
        
        renderRequest.setAttribute(WebKeys.TRANSLATOR_DISABLE, false);
        // Should we get the available languages from the API each time the translator is rendered?
        //renderRequest.setAttribute(WebKeys.TRANSLATOR_LANGUAGES, Languages.LANGUAGES);

        if(!automaticTranslatorUtil.validateCredentials()) {
            SessionErrors.add(renderRequest, WebKeys.TRANSLATOR_AUTH_ERROR);
            renderRequest.setAttribute(WebKeys.TRANSLATOR_DISABLE, true);
        }
        
        System.out.println("***");
        System.out.println("Rendering");
        System.out.println("***");
        return "/content-translator/translate.jsp";
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
