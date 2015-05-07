

<%@ page import="org.sagres.labkey.LabkeyReport" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <g:set var="entityName" value="${message(code: 'labkeyReport.label', default: 'LabkeyReport')}" />
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
            <g:hasErrors bean="${labkeyReportInstance}">
            <div class="errors">
                <g:renderErrors bean="${labkeyReportInstance}" as="list" />
            </div>
            </g:hasErrors>
            <g:form action="save" >
                <div class="dialog">
                    <table>
                        <tbody>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="sampleSetId"><g:message code="labkeyReport.sampleSetId.label" default="Sample Set Id" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: labkeyReportInstance, field: 'sampleSetId', 'errors')}">
																	<g:select id="sampleSetId" from="${sampleSets}" optionKey="key" name="sampleSetId"
								optionValue="value" />
                                </td>
                            </tr>

												<tr class="prop">
                                <td valign="top" class="name">
                                    <label for="reportName"><g:message code="labkeyReport.reportName.label" default="Report Name" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: labkeyReportInstance, field: 'reportName', 'errors')}">
                                    <g:textField name="reportName" value="${fieldValue(bean: labkeyReportInstance, field: 'reportName')}" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="category"><g:message code="labkeyReport.category.label" default="Category" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: labkeyReportInstance, field: 'category', 'errors')}">
																	<g:select id="category" from="${grailsApplication.config.dm3.labkeyTabs}" optionKey="key" name="category"
								optionValue="value" />
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
