

<%@ page import="org.sagres.labkey.LabkeyReport" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <g:set var="entityName" value="${message(code: 'labkeyReport.label', default: 'LabkeyReport')}" />
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
            <g:hasErrors bean="${labkeyReportInstance}">
            <div class="errors">
                <g:renderErrors bean="${labkeyReportInstance}" as="list" />
            </div>
            </g:hasErrors>
            <g:form method="post" >
                <g:hiddenField name="id" value="${labkeyReportInstance?.id}" />
                <g:hiddenField name="version" value="${labkeyReportInstance?.version}" />
                <div class="dialog">
                    <table>
                        <tbody>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="sampleSetId"><g:message code="labkeyReport.sampleSetId.label" default="Sample Set Id" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: labkeyReportInstance, field: 'sampleSetId', 'errors')}">
                                    <g:textField name="sampleSetId" value="${fieldValue(bean: labkeyReportInstance, field: 'sampleSetId')}" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="category"><g:message code="labkeyReport.category.label" default="Category" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: labkeyReportInstance, field: 'category', 'errors')}">
                                    <g:textField name="category" value="${labkeyReportInstance?.category}" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="reportURL"><g:message code="labkeyReport.reportURL.label" default="Report URL" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: labkeyReportInstance, field: 'reportURL', 'errors')}">
                                    <g:textField name="reportURL" value="${labkeyReportInstance?.reportURL}" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="dateLastLoaded"><g:message code="labkeyReport.dateLastLoaded.label" default="Date Last Loaded" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: labkeyReportInstance, field: 'dateLastLoaded', 'errors')}">
                                    <g:datePicker name="dateLastLoaded" precision="day" value="${labkeyReportInstance?.dateLastLoaded}" default="none" noSelection="['': '']" />
                                </td>
                            </tr>


												     <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="enabled"><g:message code="labkeyReport.enabled.label" default="Enabled" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: labkeyReportInstance, field: 'enabled', 'errors')}">
																	<g:checkBox name="enabled" value="${!(labkeyReportInstance?.enabled == false)}"/>
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
