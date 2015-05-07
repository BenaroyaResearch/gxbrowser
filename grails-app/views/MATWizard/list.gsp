
<%@ page import="org.sagres.mat.MATWizard" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <g:set var="entityName" value="${message(code: 'MATWizard.label', default: 'MATWizard')}" />
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
                        
                            <g:sortableColumn property="id" title="${message(code: 'MATWizard.id.label', default: 'Id')}" />
                        
                            <g:sortableColumn property="fileFormat" title="${message(code: 'MATWizard.fileFormat.label', default: 'File Format')}" />
                        
                            <g:sortableColumn property="annotationFile" title="${message(code: 'MATWizard.annotationFile.label', default: 'Annotation File')}" />
                        
                            <g:sortableColumn property="annotationInfo" title="${message(code: 'MATWizard.annotationInfo.label', default: 'Annotation Info')}" />
                        
                            <g:sortableColumn property="signalData" title="${message(code: 'MATWizard.signalData.label', default: 'Signal Data')}" />
                        
                            <g:sortableColumn property="step" title="${message(code: 'MATWizard.step.label', default: 'Step')}" />
                        
                        </tr>
                    </thead>
                    <tbody>
                    <g:each in="${MATWizardInstanceList}" status="i" var="MATWizardInstance">
                        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                        
                            <td><g:link action="show" id="${MATWizardInstance.id}">${fieldValue(bean: MATWizardInstance, field: "id")}</g:link></td>
                        
                            <td>${fieldValue(bean: MATWizardInstance, field: "fileFormatId")}</td>
                        
                            <td>${fieldValue(bean: MATWizardInstance, field: "annotationFile")}</td>
                        
                            <td>${fieldValue(bean: MATWizardInstance, field: "annotationInfo")}</td>
                        
                            <td>${fieldValue(bean: MATWizardInstance, field: "signalData")}</td>
                        
                            <td>${fieldValue(bean: MATWizardInstance, field: "step")}</td>
                        
                        </tr>
                    </g:each>
                    </tbody>
                </table>
            </div>
            <div class="paginateButtons">
                <g:paginate total="${MATWizardInstanceTotal}" />
            </div>
        </div>
    </body>
</html>
