

<%@ page import="org.sagres.mat.MATWizard" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <g:set var="entityName" value="${message(code: 'MATWizard.label', default: 'MATWizard')}" />
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
            <g:hasErrors bean="${MATWizardInstance}">
            <div class="errors">
                <g:renderErrors bean="${MATWizardInstance}" as="list" />
            </div>
            </g:hasErrors>
            <g:form method="post" >
                <g:hiddenField name="id" value="${MATWizardInstance?.id}" />
                <g:hiddenField name="version" value="${MATWizardInstance?.version}" />
                <div class="dialog">
                    <table>
                        <tbody>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="fileFormat"><g:message code="MATWizard.fileFormat.label" default="File Format" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: MATWizardInstance, field: 'fileFormat', 'errors')}">
                                    <g:select name="fileFormat" from="${MATWizardInstance.constraints.fileFormat.inList}" value="${MATWizardInstance?.fileFormat}" valueMessagePrefix="MATWizard.fileFormat"  />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="annotationFile"><g:message code="MATWizard.annotationFile.label" default="Annotation File" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: MATWizardInstance, field: 'annotationFile', 'errors')}">
                                    <g:textField name="annotationFile" value="${MATWizardInstance?.annotationFile}" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="annotationInfo"><g:message code="MATWizard.annotationInfo.label" default="Annotation Info" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: MATWizardInstance, field: 'annotationInfo', 'errors')}">
                                    <g:textField name="annotationInfo" value="${MATWizardInstance?.annotationInfo}" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="signalData"><g:message code="MATWizard.signalData.label" default="Signal Data" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: MATWizardInstance, field: 'signalData', 'errors')}">
                                    <g:textField name="signalData" value="${MATWizardInstance?.signalData}" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="step"><g:message code="MATWizard.step.label" default="Step" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: MATWizardInstance, field: 'step', 'errors')}">
                                    <g:textField name="step" value="${MATWizardInstance?.step}" />
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
