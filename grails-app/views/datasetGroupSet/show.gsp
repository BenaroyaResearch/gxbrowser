
<%@ page import="org.sagres.sampleSet.DatasetGroupSet" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="groupSetMain" />
        <g:set var="entityName" value="${message(code: 'datasetGroupSet.label', default: 'DatasetGroupSet')}" />
        <title><g:message code="default.show.label" args="[entityName]" /></title>
    </head>
    <body>
        <div class="sampleset-container">
            <h1><g:message code="default.show.label" args="[entityName]" /></h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <div class="dialog">
                <table>
                    <tbody>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="datasetGroupSet.id.label" default="Id" /></td>
                            
                            <td valign="top" class="value">${datasetGroupSetInstance.datasetGroupSet.id}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="datasetGroupSet.name.label" default="Name" /></td>
                            
                            <td valign="top" class="value">${datasetGroupSetInstance.datasetGroupSet.name}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="datasetGroupSet.groups.label" default="Groups" /></td>
                            
                            <td valign="top" style="text-align: left;" class="value">
                                <ul>
                                <g:each in="${datasetGroupSetInstance.datasetGroupSet.groups}" var="g">
                                    <li>${g?.encodeAsHTML()}</li>
                                </g:each>
                                </ul>
                            </td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="datasetGroupSet.sampleSet.label" default="Sample Set" /></td>
	                    	<g:if test="${datasetGroupSetInstance?.sampleSet?.id}">
                            	<td valign="top" class="value"><g:link controller="sampleSet" action="show" id="${datasetGroupSetInstance?.sampleSet?.id}">${datasetGroupSetInstance?.sampleSet?.name.encodeAsHTML()}</g:link></td>
                    		</g:if>
                    		<g:else>
                    			<td valign="top" class="value">(orphan: none)</td>
                    		</g:else>                            
                        </tr>

                    </tbody>
                </table>
            </div>
            <sec:ifAnyGranted roles="ROLE_SETAPPROVAL">
			<div class="buttons">
                <g:form>
                    <g:hiddenField name="id" value="${datasetGroupSetInstance?.datasetGroupSet.id}" />
                    <g:actionSubmit class="edit" action="edit" value="${message(code: 'default.button.edit.label', default: 'Edit')}" />
                    <g:actionSubmit class="delete" action="delete" value="${message(code: 'default.button.delete.label', default: 'Delete')}" onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" />
                </g:form>
			</div>
            </sec:ifAnyGranted>
        </div>
    </body>
</html>
