

<%@ page import="org.sagres.sampleSet.DatasetGroupSet" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <g:set var="entityName" value="${message(code: 'datasetGroupSet.label', default: 'DatasetGroupSet')}" />
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
            <g:hasErrors bean="${datasetGroupSetInstance}">
            <div class="errors">
                <g:renderErrors bean="${datasetGroupSetInstance}" as="list" />
            </div>
            </g:hasErrors>
            <g:form action="save" >
                <div class="dialog">
                    <table>
                        <tbody>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="name"><g:message code="datasetGroupSet.name.label" default="Name" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: datasetGroupSetInstance, field: 'name', 'errors')}">
                                    <g:textField name="name" value="${datasetGroupSetInstance?.name}" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="defaultRankList"><g:message code="datasetGroupSet.defaultRankList.label" default="Default Rank List" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: datasetGroupSetInstance, field: 'defaultRankList', 'errors')}">
                                    <g:select name="defaultRankList.id" from="${org.sagres.rankList.RankList.list()}" optionKey="id" value="${datasetGroupSetInstance?.defaultRankList?.id}" noSelection="['null': '']" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="sampleSet"><g:message code="datasetGroupSet.sampleSet.label" default="Sample Set" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: datasetGroupSetInstance, field: 'sampleSet', 'errors')}">
                                    <g:select name="sampleSet.id" from="${org.sagres.sampleSet.SampleSet.list()}" optionKey="id" value="${datasetGroupSetInstance?.sampleSet?.id}"  />
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
