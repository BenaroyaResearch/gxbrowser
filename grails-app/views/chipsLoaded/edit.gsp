

<%@ page import="common.chipInfo.ChipsLoaded" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <g:set var="entityName" value="${message(code: 'chipsLoaded.label', default: 'ChipsLoaded')}" />
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
            <g:hasErrors bean="${chipsLoadedInstance}">
            <div class="errors">
                <g:renderErrors bean="${chipsLoadedInstance}" as="list" />
            </div>
            </g:hasErrors>
            <g:form method="post" >
                <g:hiddenField name="id" value="${chipsLoadedInstance?.id}" />
                <g:hiddenField name="version" value="${chipsLoadedInstance?.version}" />
                <div class="dialog">
                    <table>
                        <tbody>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="filename"><g:message code="chipsLoaded.filename.label" default="Filename" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: chipsLoadedInstance, field: 'filename', 'errors')}">
                                    <g:textField name="filename" value="${chipsLoadedInstance?.filename}" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="chipType"><g:message code="chipsLoaded.chipType.label" default="Chip Type" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: chipsLoadedInstance, field: 'chipType', 'errors')}">
                                    <g:select name="chipType.id" from="${common.chipInfo.ChipType.list()}" optionKey="id" value="${chipsLoadedInstance?.chipType?.id}"  />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="noSamples"><g:message code="chipsLoaded.noSamples.label" default="No Samples" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: chipsLoadedInstance, field: 'noSamples', 'errors')}">
                                    <g:textField name="noSamples" value="${fieldValue(bean: chipsLoadedInstance, field: 'noSamples')}" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="noProbes"><g:message code="chipsLoaded.noProbes.label" default="No Probes" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: chipsLoadedInstance, field: 'noProbes', 'errors')}">
                                    <g:textField name="noProbes" value="${fieldValue(bean: chipsLoadedInstance, field: 'noProbes')}" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="loadStatus"><g:message code="chipsLoaded.loadStatus.label" default="Load Status" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: chipsLoadedInstance, field: 'loadStatus', 'errors')}">
                                    <g:select name="loadStatus.id" from="${common.chipInfo.ChipLoadStatus.list()}" optionKey="id" value="${chipsLoadedInstance?.loadStatus?.id}"  />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="notes"><g:message code="chipsLoaded.notes.label" default="Notes" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: chipsLoadedInstance, field: 'notes', 'errors')}">
                                    <g:textArea name="notes" cols="40" rows="5" value="${chipsLoadedInstance?.notes}" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="dateStarted"><g:message code="chipsLoaded.dateStarted.label" default="Date Started" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: chipsLoadedInstance, field: 'dateStarted', 'errors')}">
                                    <g:datePicker name="dateStarted" precision="day" value="${chipsLoadedInstance?.dateStarted}"  />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="dateEnded"><g:message code="chipsLoaded.dateEnded.label" default="Date Ended" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: chipsLoadedInstance, field: 'dateEnded', 'errors')}">
                                    <g:datePicker name="dateEnded" precision="day" value="${chipsLoadedInstance?.dateEnded}" default="none" noSelection="['': '']" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="genomicDataSource"><g:message code="chipsLoaded.genomicDataSource.label" default="Data Source" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: chipsLoadedInstance, field: 'genomicDataSource', 'errors')}">
								  <g:select name="genomicDataSource.id"
											from="${common.chipInfo.GenomicDataSource.list()}"
											optionKey="id"
											optionValue="name"
											noSelection="[ '': '--Data Source--' ]"
											value="${chipsLoadedInstance?.genomicDataSource?.id}" />
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
