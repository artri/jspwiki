<%--
    Licensed to the Apache Software Foundation (ASF) under one
    or more contributor license agreements.  See the NOTICE file
    distributed with this work for additional information
    regarding copyright ownership.  The ASF licenses this file
    to you under the Apache License, Version 2.0 (the
    "License"); you may not use this file except in compliance
    with the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on an
    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied.  See the License for the
    specific language governing permissions and limitations
    under the License.
--%>

<%@ page import="org.apache.commons.lang3.time.StopWatch" %>
<%@ page import="org.apache.wiki.WatchDog" %>
<%@ page import="org.apache.wiki.api.core.*" %>
<%@ page import="org.apache.wiki.api.Wiki" %>
<%@ page import="org.apache.wiki.auth.AuthorizationManager" %>
<%@ page import="org.apache.wiki.preferences.Preferences" %>
<%@ page import="org.apache.wiki.ui.TemplateManager" %>
<%@ page import="org.apache.wiki.util.*" %>
<%@ page import="org.apache.wiki.util.WikiLogger" %>
<%@ page errorPage="/Error.jsp" %>
<%@ taglib uri="http://jspwiki.apache.org/tags" prefix="wiki" %>
<%!
    WikiLogger log = WikiLogger.getLogger(Wiki.PLATFORM_NAME);
%>

<%
    Engine wiki = Wiki.engine().find( getServletConfig() );
    // Create wiki context and check for authorization
    Context wikiContext = Wiki.context().create( wiki, request, ContextEnum.PAGE_VIEW.getRequestContext() );
    if( !wiki.getManager( AuthorizationManager.class ).hasAccess( wikiContext, response ) ) return;
    String pagereq = wikiContext.getName();

    // Redirect if request was for a special page
    String redirect = wikiContext.getRedirectURL( );
    if( redirect != null )
    {
        response.sendRedirect( redirect );
        return;
    }

    StopWatch sw = new StopWatch();
    sw.start();
    WatchDog w = WatchDog.getCurrentWatchDog( wiki );
    try {
        w.enterState("Generating VIEW response for "+wikiContext.getPage(),60);

        // Set the content type and include the response content
        response.setContentType("text/html; charset="+wiki.getContentEncoding() );
        String contentPage = wiki.getManager( TemplateManager.class ).findJSP( pageContext, wikiContext.getTemplate(), "ViewTemplate.jsp" );

%><wiki:Include page="<%=contentPage%>" /><%
    }
    finally
    {
        sw.stop();
        if( log.isDebugEnabled() ) log.debug("Total response time from server on page "+pagereq+": "+sw);
        w.exitState();
    }
%>

