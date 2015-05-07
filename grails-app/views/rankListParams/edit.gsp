

<%@ page import="org.sagres.rankList.RankListParams" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <g:set var="entityName" value="${message(code: 'rankListParams.label', default: 'RankListParams')}" />
        <title><g:message code="default.edit.label" args="[entityName]" /></title>
    </head>
    <body>
        <div class="nav">
            <span class="menuButton"><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a></span>
            <span class="menuButton"><g:link class="list" action="list"><g:message code="default.list.label" args="[entityName]" /></g:link></span>
            <span class="menuButton"><g:link class="create" action="create"><g:message code="default.new.label" args="[entityName]" /></g:link></span>
        </div>
        <div class="body">
            <h1><g:message code="default.edit.label" args="[entityName]" /></h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <g:hasErrors bean="${rankListParamsInstance}">
            <div class="errors">
                <g:renderErrors bean="${rankListParamsInstance}" as="list" />
            </div>
            </g:hasErrors>
            <g:form method="post" >
                <g:hiddenField name="id" value="${rankListParamsInstance?.id}" />
                <g:hiddenField name="version" value="${rankListParamsInstance?.version}" />
                <div class="dialog">
                    <table>
                        <tbody>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="userName"><g:message code="rankListParams.userName.label" default="User Name" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: rankListParamsInstance, field: 'userName', 'errors')}">
                                    <g:textField name="userName" value="${rankListParamsInstance?.userName}" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="runDate"><g:message code="rankListParams.runDate.label" default="Run Date" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: rankListParamsInstance, field: 'runDate', 'errors')}">
                                    <g:datePicker name="runDate" precision="day" value="${rankListParamsInstance?.runDate}" default="none" noSelection="['': '']" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="comparisons"><g:message code="rankListParams.comparisons.label" default="Comparisons" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: rankListParamsInstance, field: 'comparisons', 'errors')}">
                                    <g:select name="comparisons" from="${org.sagres.rankList.RankListComparison.list()}" multiple="yes" optionKey="id" size="5" value="${rankListParamsInstance?.comparisons*.id}" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="rankListName"><g:message code="rankListParams.rankListName.label" default="Rank List Name" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: rankListParamsInstance, field: 'rankListName', 'errors')}">
                                    <g:textField name="rankListName" value="${rankListParamsInstance?.rankListName}" />
                                </td>
                            </tr>
                        
                        </tbody>
                    </table>
                </div>
                <div class="buttons">
                    <span class="button"><g:actionSubmit class="save" action="update" value="${message(code: 'default.button.update.label', default: 'Update')}" /></span>
                    <span class="button"><g:actionSubmit class="delete" action="delete" value="${message(code: 'default.button.delete.label', default: 'Delete')}" onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" /></span>
                </div>
            </g:form>
        </div>
    </body>
</html>
