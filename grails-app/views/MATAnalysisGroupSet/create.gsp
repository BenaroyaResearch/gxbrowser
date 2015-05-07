

<%@ page import="org.sagres.mat.MATAnalysisGroupSet" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <g:set var="entityName" value="${message(code: 'MATAnalysisGroupSet.label', default: 'MATAnalysisGroupSet')}" />
        <title><g:message code="default.create.label" args="[entityName]" /></title>
    </head>
    <body>
        <div class="nav">
            <span class="menuButton"><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a></span>
            <span class="menuButton"><g:link class="list" action="list"><g:message code="default.list.label" args="[entityName]" /></g:link></span>
        </div>
        <div class="body">
            <h1><g:message code="default.create.label" args="[entityName]" /></h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <g:hasErrors bean="${MATAnalysisGroupSetInstance}">
            <div class="errors">
                <g:renderErrors bean="${MATAnalysisGroupSetInstance}" as="list" />
            </div>
            </g:hasErrors>
            <g:form action="save" >
                <div class="dialog">
                    <table>
                        <tbody>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="groupName"><g:message code="MATAnalysisGroupSet.groupName.label" default="Group Name" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: MATAnalysisGroupSetInstance, field: 'groupName', 'errors')}">
                                    <g:textField name="groupName" value="${MATAnalysisGroupSetInstance?.groupName}" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="analysisId"><g:message code="MATAnalysisGroupSet.analysisId.label" default="Analysis Id" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: MATAnalysisGroupSetInstance, field: 'analysisId', 'errors')}">
                                    <g:textField name="analysisId" value="${fieldValue(bean: MATAnalysisGroupSetInstance, field: 'analysisId')}" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="groupId"><g:message code="MATAnalysisGroupSet.groupId.label" default="Group Id" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: MATAnalysisGroupSetInstance, field: 'groupId', 'errors')}">
                                    <g:textField name="groupId" value="${fieldValue(bean: MATAnalysisGroupSetInstance, field: 'groupId')}" />
                                </td>
                            </tr>
                        
                        </tbody>
                    </table>
                </div>
                <div class="buttons">
                    <span class="button"><g:submitButton name="create" class="save" value="${message(code: 'default.button.create.label', default: 'Create')}" /></span>
                </div>
            </g:form>
        </div>
    </body>
</html>
