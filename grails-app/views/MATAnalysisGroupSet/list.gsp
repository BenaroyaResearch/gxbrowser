
<%@ page import="org.sagres.mat.MATAnalysisGroupSet" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <g:set var="entityName" value="${message(code: 'MATAnalysisGroupSet.label', default: 'MATAnalysisGroupSet')}" />
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
                        
                            <g:sortableColumn property="id" title="${message(code: 'MATAnalysisGroupSet.id.label', default: 'Id')}" />
                        
                            <g:sortableColumn property="groupName" title="${message(code: 'MATAnalysisGroupSet.groupName.label', default: 'Group Name')}" />
                        
                            <g:sortableColumn property="analysisId" title="${message(code: 'MATAnalysisGroupSet.analysisId.label', default: 'Analysis Id')}" />
                        
                            <g:sortableColumn property="groupId" title="${message(code: 'MATAnalysisGroupSet.groupId.label', default: 'Group Id')}" />
                        
                        </tr>
                    </thead>
                    <tbody>
                    <g:each in="${MATAnalysisGroupSetInstanceList}" status="i" var="MATAnalysisGroupSetInstance">
                        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                        
                            <td><g:link action="show" id="${MATAnalysisGroupSetInstance.id}">${fieldValue(bean: MATAnalysisGroupSetInstance, field: "id")}</g:link></td>
                        
                            <td>${fieldValue(bean: MATAnalysisGroupSetInstance, field: "groupName")}</td>
                        
                            <td>${fieldValue(bean: MATAnalysisGroupSetInstance, field: "analysisId")}</td>
                        
                            <td>${fieldValue(bean: MATAnalysisGroupSetInstance, field: "groupId")}</td>
                        
                        </tr>
                    </g:each>
                    </tbody>
                </table>
            </div>
            <div class="paginateButtons">
                <g:paginate total="${MATAnalysisGroupSetInstanceTotal}" />
            </div>
        </div>
    </body>
</html>
