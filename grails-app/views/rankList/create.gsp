

<%@ page import="org.sagres.rankList.RankList" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <g:set var="entityName" value="${message(code: 'rankList.label', default: 'RankList')}" />
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
            <g:hasErrors bean="${rankListInstance}">
            <div class="errors">
                <g:renderErrors bean="${rankListInstance}" as="list" />
            </div>
            </g:hasErrors>
            <g:form action="save" >
                <div class="dialog">
                    <table>
                        <tbody>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="fileLoaded"><g:message code="rankList.fileLoaded.label" default="File Loaded" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: rankListInstance, field: 'fileLoaded', 'errors')}">
                                    <g:select name="fileLoaded.id" from="${org.sagres.FilesLoaded.list()}" optionKey="id" value="${rankListInstance?.fileLoaded?.id}"  />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="sampleSetId"><g:message code="rankList.sampleSetId.label" default="Sample Set Id" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: rankListInstance, field: 'sampleSetId', 'errors')}">
                                    <g:textField name="sampleSetId" value="${fieldValue(bean: rankListInstance, field: 'sampleSetId')}" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="rankListType"><g:message code="rankList.rankListType.label" default="Rank List Type" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: rankListInstance, field: 'rankListType', 'errors')}">
                                    <g:select name="rankListType.id" from="${org.sagres.rankList.RankListType.list()}" optionKey="id" value="${rankListInstance?.rankListType?.id}"  />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="description"><g:message code="rankList.description.label" default="Description" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: rankListInstance, field: 'description', 'errors')}">
                                    <g:textField name="description" value="${rankListInstance?.description}" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="numProbes"><g:message code="rankList.numProbes.label" default="Num Probes" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: rankListInstance, field: 'numProbes', 'errors')}">
                                    <g:textField name="numProbes" value="${fieldValue(bean: rankListInstance, field: 'numProbes')}" />
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
