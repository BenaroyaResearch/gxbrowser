

<%@ page import="org.sagres.FilesLoaded" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <g:set var="entityName" value="${message(code: 'filesLoaded.label', default: 'FilesLoaded')}" />
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
            <g:hasErrors bean="${filesLoadedInstance}">
            <div class="errors">
                <g:renderErrors bean="${filesLoadedInstance}" as="list" />
            </div>
            </g:hasErrors>
            <g:form method="post" >
                <g:hiddenField name="id" value="${filesLoadedInstance?.id}" />
                <g:hiddenField name="version" value="${filesLoadedInstance?.version}" />
                <div class="dialog">
                    <table>
                        <tbody>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="filename"><g:message code="filesLoaded.filename.label" default="Filename" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: filesLoadedInstance, field: 'filename', 'errors')}">
                                    <g:textField name="filename" value="${filesLoadedInstance?.filename}" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="fileLoadStatus"><g:message code="filesLoaded.fileLoadStatus.label" default="File Load Status" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: filesLoadedInstance, field: 'fileLoadStatus', 'errors')}">
                                    <g:select name="fileLoadStatus.id" from="${org.sagres.FileLoadStatus.list()}" optionKey="id" value="${filesLoadedInstance?.fileLoadStatus?.id}"  />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="dateStarted"><g:message code="filesLoaded.dateStarted.label" default="Date Started" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: filesLoadedInstance, field: 'dateStarted', 'errors')}">
                                    <g:datePicker name="dateStarted" precision="day" value="${filesLoadedInstance?.dateStarted}"  />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="dateEnded"><g:message code="filesLoaded.dateEnded.label" default="Date Ended" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: filesLoadedInstance, field: 'dateEnded', 'errors')}">
                                    <g:datePicker name="dateEnded" precision="day" value="${filesLoadedInstance?.dateEnded}" default="none" noSelection="['': '']" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="notes"><g:message code="filesLoaded.notes.label" default="Notes" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: filesLoadedInstance, field: 'notes', 'errors')}">
                                    <g:textArea name="notes" cols="40" rows="5" value="${filesLoadedInstance?.notes}" />
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
