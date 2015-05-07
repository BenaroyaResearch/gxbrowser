
<%@ page import="org.sagres.labkey.LabkeyReport" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <g:set var="entityName" value="${message(code: 'labkeyReport.label', default: 'LabkeyReport')}" />
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
                        
                            <g:sortableColumn property="id" title="${message(code: 'labkeyReport.id.label', default: 'Id')}" />
                        
                            <g:sortableColumn property="sampleSetId" title="${message(code: 'labkeyReport.sampleSetId.label', default: 'Sample Set Id')}" />
                        
                            <g:sortableColumn property="category" title="${message(code: 'labkeyReport.category.label', default: 'Category')}" />
                        
                            <g:sortableColumn property="reportURL" title="${message(code: 'labkeyReport.reportURL.label', default: 'Report URL')}" />
                        
                            <g:sortableColumn property="dateLastLoaded" title="${message(code: 'labkeyReport.dateLastLoaded.label', default: 'Date Last Loaded')}" />
													<g:sortableColumn property="enabled" title="${message(code: 'labkeyReport.enabled.label', default: 'enabled')}" />
                        
                        </tr>
                    </thead>
                    <tbody>
                    <g:each in="${labkeyReportInstanceList}" status="i" var="labkeyReportInstance">
                        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                        
                            <td><g:link action="show" id="${labkeyReportInstance.id}">${fieldValue(bean: labkeyReportInstance, field: "id")}</g:link></td>
                        
                            <td>${fieldValue(bean: labkeyReportInstance, field: "sampleSetId")}</td>
                        
                            <td>${fieldValue(bean: labkeyReportInstance, field: "category")}</td>
                        
                            <td>${fieldValue(bean: labkeyReportInstance, field: "reportURL")}</td>
                        
                            <td><g:formatDate date="${labkeyReportInstance.dateLastLoaded}" /></td>
													<td>${fieldValue(bean: labkeyReportInstance, field: "enabled")}</td>
                        
                        </tr>
                    </g:each>
                    </tbody>
                </table>
            </div>
            <div class="paginateButtons">
                <g:paginate total="${labkeyReportInstanceTotal}" />
            </div>
        </div>
    </body>
</html>
