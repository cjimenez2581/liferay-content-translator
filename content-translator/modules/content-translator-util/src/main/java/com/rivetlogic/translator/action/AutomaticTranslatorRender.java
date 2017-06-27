package com.rivetlogic.translator.action;

import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.liferay.journal.constants.JournalPortletKeys;
import com.liferay.journal.model.JournalArticle;
import com.liferay.journal.service.JournalArticleLocalServiceUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCRenderCommand;
import com.liferay.portal.kernel.servlet.SessionErrors;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.rivetlogic.translator.ui.WebKeys;
import com.rivetlogic.translator.util.AutomaticTranslatorUtil;

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
        
        return "/content-translator/translate.jsp";
	}

    @Reference(unbind = "-")
	protected void setAutomaticTranslatorUtil(AutomaticTranslatorUtil automaticTranslatorUtil) {
		this.automaticTranslatorUtil = automaticTranslatorUtil;
	}
	
	private AutomaticTranslatorUtil automaticTranslatorUtil;
}
