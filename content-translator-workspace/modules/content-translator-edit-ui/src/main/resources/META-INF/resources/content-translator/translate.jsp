<%@page import="com.liferay.portal.kernel.util.HtmlUtil"%>
<%@page import="com.liferay.portal.kernel.util.ParamUtil"%>

<%@page import="java.util.Arrays" %>
<%@page import="java.util.List" %>

<%@ include file="/init.jsp" %>

<%
String articleId = ParamUtil.getString(request, "articleId");
long groupId = ParamUtil.getLong(request, "groupId");
double version = ParamUtil.getDouble(request, "version", JournalArticleConstants.VERSION_DEFAULT);

JournalArticle article = JournalArticleLocalServiceUtil.getArticle(groupId, articleId, version);
String structureId = BeanParamUtil.getString(article, request, "structureId");
String redirect = ParamUtil.getString(request, "redirect");
int status = BeanParamUtil.getInteger(article, request, "status");
pageContext.setAttribute("pns", renderResponse.getNamespace());

boolean close = ParamUtil.getBoolean(request, "translatorClose");
boolean disable = ParamUtil.getBoolean(request, "translatorDisable");
String[] availableTranslations = (String[]) request.getAttribute("translatorLanguages");

%>

<portlet:actionURL var="translateArticleActionURL" windowState="<%= WindowState.MAXIMIZED.toString() %>">
    <portlet:param name="struts_action" value="/journal/translate" />
    <portlet:param name="redirect" value="<%= redirect %>" />
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
        if (!ArrayUtil.contains(article.getAvailableLanguageIds(), LocaleUtil.toLanguageId(iiLocale)) ) {
            
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
    </aui:field-wrapper>
    
    <aui:button-row>
	    <aui:button name="saveButton" type="submit" value="continue" disabled="<%= disable %>" />
	    <aui:button href="<%= redirect %>" type="cancel" />
    </aui:button-row>
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