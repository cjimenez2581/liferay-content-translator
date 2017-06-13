<%@page import="com.liferay.portal.kernel.util.HtmlUtil"%>
<%@page import="com.liferay.portal.kernel.util.ParamUtil"%>
<%@page import="org.osgi.framework.BundleContext"%>
<%@page import="org.osgi.framework.FrameworkUtil"%>
<%@page import="org.osgi.framework.ServiceReference"%>
<%@page import="com.rivetlogic.translator.util.AutomaticTranslatorUtil"%>

<%@page import="java.util.Arrays" %>
<%@page import="java.util.List" %>

<%@ include file="/init.jsp" %>

<%
String articleId = ParamUtil.getString(request, "articleId");
long groupId = ParamUtil.getLong(request, "groupId");
double version = ParamUtil.getDouble(request, "version", JournalArticleConstants.VERSION_DEFAULT);

// JournalArticle article = JournalArticleLocalServiceUtil.getArticle(groupId, articleId);
JournalArticle article = journalDisplayContext.getArticle();
String structureId = BeanParamUtil.getString(article, request, "structureId");
String redirect = ParamUtil.getString(request, "redirect");
int status = BeanParamUtil.getInteger(article, request, "status");
if(renderResponse!=null) {
	pageContext.setAttribute("pns", 
			renderResponse.getNamespace());
}

boolean close = ParamUtil.getBoolean(request, com.rivetlogic.translator.ui.WebKeys.TRANSLATOR_CLOSE);
boolean disable = ParamUtil.getBoolean(request, com.rivetlogic.translator.ui.WebKeys.TRANSLATOR_DISABLE);
String[] articleLanguages = new String[0];
if(article!=null){
	article.getAvailableLanguageIds();
} else {
%>
<p><liferay-ui:message key="save-a-version-first" /></p>
<%
	return;
}

BundleContext bundleContext = FrameworkUtil.getBundle(AutomaticTranslatorUtil.class).getBundleContext();
ServiceReference<AutomaticTranslatorUtil> utilReference = bundleContext.getServiceReference(AutomaticTranslatorUtil.class );
AutomaticTranslatorUtil automaticTranslatorUtil = (AutomaticTranslatorUtil) bundleContext.getService( utilReference );
%>

<portlet:actionURL var="translateArticleActionURL" name="automaticTranslate" windowState="<%= WindowState.MAXIMIZED.toString() %>">
	<portlet:param name="articleId" value="<%=articleId%>" />
	<portlet:param name="groupId" value="<%=String.valueOf(groupId)%>" />
	<portlet:param name="version" value="<%=String.valueOf(version)%>" />
</portlet:actionURL>

<liferay-ui:error key="translator-error" message="translator-error-message"/>
<liferay-ui:error key="translator-auth-error" message="translator-auth-error-message"/>

<aui:form action="<%= translateArticleActionURL %>" cssClass="lfr-dynamic-form" enctype="multipart/form-data" method="post" name="fm1">
    <aui:input name="articleId" type="hidden" value="<%= articleId %>" />
    <aui:input name="groupId" type="hidden" value="<%= groupId %>" />
    <aui:input name="version" type="hidden" value="<%= ((article == null) || article.isNew()) ? version : article.getVersion() %>" />
    <aui:input name="status" type="hidden" value="<%= status %>" />
    <aui:input name="structureId" type="hidden" value="<%= structureId %>" />
    
    <aui:field-wrapper name="selectedLanguages" label="journal.article.form.translate.select.language">
    <%
    Set<Locale> locales = LanguageUtil.getAvailableLocales(themeDisplay.getSiteGroupId());
	
    for (Locale iiLocale : locales) {
        if (!ArrayUtil.contains(article.getAvailableLanguageIds(), LocaleUtil.toLanguageId(iiLocale)) &&
        		 automaticTranslatorUtil.canTranslateTo( LocaleUtil.toLanguageId(iiLocale).split("_")[0], articleLanguages) ) {
            
    %>
        <div>
	        <aui:input name="selectedLanguages" type="checkbox" inlineLabel="right" label="" value="<%=LocaleUtil.toLanguageId(iiLocale) %>"/>
	        <liferay-ui:icon
	            image='<%= "../language/" + LocaleUtil.toLanguageId(iiLocale) %>'
	            message="<%= iiLocale.getDisplayName(locale) %>"
	        />
	        <span class="lfr-icon-menu-text"><%= iiLocale.getDisplayName(locale) %></span>
        </div>
    <%
        }
    }
    %>
    
		<aui:button-row>
			<aui:button name="saveButton" type="submit" value="continue" disabled="<%= disable %>" />
		    <aui:button href="<%= redirect %>" type="cancel" />
		</aui:button-row>
		<span>Powered by <a href="http://translate.yandex.com/">Yandex.Translate</a></span>
    </aui:field-wrapper>
</aui:form>

<% if(close) {  %>
<aui:script use="aui-base">
	Liferay.fire('closeWindow',
	{
		id: '<portlet:namespace/>automatic-translate',
		redirect: '<%= HtmlUtil.escapeJS(redirect) %>'
	});
</aui:script>
<% } %>
