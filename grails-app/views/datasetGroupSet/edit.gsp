

<%@ page import="org.sagres.sampleSet.DatasetGroupSet" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="groupSetMain" />
        <g:set var="entityName" value="${message(code: 'datasetGroupSet.label', default: 'DatasetGroupSet')}" />
        <title><g:message code="default.edit.label" args="[entityName]" /></title>
    </head>
    <body>
        <div class="sampleset-container">
            <h1><g:message code="default.edit.label" args="[entityName]" /></h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <g:hasErrors bean="${datasetGroupSetInstance}">
            <div class="errors">
                <g:renderErrors bean="${datasetGroupSetInstance}" as="list" />
            </div>
            </g:hasErrors>
            <g:form method="post" >
                <g:hiddenField name="id" value="${datasetGroupSetInstance?.id}" />
                <g:hiddenField name="version" value="${datasetGroupSetInstance?.version}" />
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
                        <!-- 
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
                                  <label for="groups"><g:message code="datasetGroupSet.groups.label" default="Groups" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: datasetGroupSetInstance, field: 'groups', 'errors')}">
                                    
<ul>
<g:each in="${datasetGroupSetInstance?.groups?}" var="g">
    <li><g:link controller="datasetGroup" action="show" id="${g.id}">${g?.encodeAsHTML()}</g:link></li>
</g:each>
</ul>
<g:link controller="datasetGroup" action="create" params="['datasetGroupSet.id': datasetGroupSetInstance?.id]">${message(code: 'default.add.label', args: [message(code: 'datasetGroup.label', default: 'DatasetGroup')])}</g:link>

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
                        -->
                        </tbody>
                    </table>
                </div>
                <sec:ifAnyGranted roles="ROLE_SETAPPROVAL">
                <div class="buttons">
                    <g:actionSubmit class="save" action="update" value="${message(code: 'default.button.update.label', default: 'Update')}" />
                    <!--  <g:actionSubmit class="delete" action="delete" value="${message(code: 'default.button.delete.label', default: 'Delete')}" onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" /> -->
                </div>
                </sec:ifAnyGranted>
            </g:form>
        </div>
    </body>
</html>
