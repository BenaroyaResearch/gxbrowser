
<%@ page import="org.sagres.rankList.RankListParams" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <g:set var="entityName" value="${message(code: 'rankListParams.label', default: 'RankListParams')}" />
        <title><g:message code="default.list.label" args="[entityName]" /></title>
    </head>
    <body>
        <div class="nav">
            <span class="menuButton"><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a></span>
            <span class="menuButton"><g:link class="create" action="create"><g:message code="default.new.label" args="[entityName]" /></g:link></span>
        </div>
        <div class="body">
            <h1><g:message code="default.list.label" args="[entityName]" /></h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <div class="list">
                <table>
                    <thead>
                        <tr>
                        
                            <g:sortableColumn property="id" title="${message(code: 'rankListParams.id.label', default: 'Id')}" />
                        
                            <g:sortableColumn property="userName" title="${message(code: 'rankListParams.userName.label', default: 'User Name')}" />
                        
                            <g:sortableColumn property="runDate" title="${message(code: 'rankListParams.runDate.label', default: 'Run Date')}" />
                        
                            <g:sortableColumn property="rankListName" title="${message(code: 'rankListParams.rankListName.label', default: 'Rank List Name')}" />
                        
                        </tr>
                    </thead>
                    <tbody>
                    <g:each in="${rankListParamsInstanceList}" status="i" var="rankListParamsInstance">
                        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                        
                            <td><g:link action="show" id="${rankListParamsInstance.id}">${fieldValue(bean: rankListParamsInstance, field: "id")}</g:link></td>
                        
                            <td>${fieldValue(bean: rankListParamsInstance, field: "userName")}</td>
                        
                            <td><g:formatDate date="${rankListParamsInstance.runDate}" /></td>
                        
                            <td>${fieldValue(bean: rankListParamsInstance, field: "rankListName")}</td>
                        
                        </tr>
                    </g:each>
                    </tbody>
                </table>
            </div>
            <div class="paginateButtons">
                <g:paginate total="${rankListParamsInstanceTotal}" />
            </div>
        </div>
    </body>
</html>
