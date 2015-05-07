

<%@ page import="org.sagres.mat.Analysis" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <g:set var="entityName" value="${message(code: 'analysis.label', default: 'Analysis')}" />
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
            <g:hasErrors bean="${analysisInstance}">
            <div class="errors">
                <g:renderErrors bean="${analysisInstance}" as="list" />
            </div>
            </g:hasErrors>
            <g:form method="post" >
                <g:hiddenField name="id" value="${analysisInstance?.id}" />
                <g:hiddenField name="version" value="${analysisInstance?.version}" />
                <div class="dialog">
                    <table>
                        <tbody>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="datasetName"><g:message code="analysis.datasetName.label" default="Dataset Name" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: analysisInstance, field: 'datasetName', 'errors')}">
                                    <g:textField name="datasetName" value="${analysisInstance?.datasetName}" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="runDate"><g:message code="analysis.runDate.label" default="Run Date" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: analysisInstance, field: 'runDate', 'errors')}">
                                    <g:datePicker name="runDate" precision="day" value="${analysisInstance?.runDate}"  />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="inputType"><g:message code="analysis.inputType.label" default="Input Type" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: analysisInstance, field: 'inputType', 'errors')}">
                                    <g:select name="inputType" from="${analysisInstance.constraints.inputType.inList}" value="${analysisInstance?.inputType}" valueMessagePrefix="analysis.inputType"  />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="signalPattern"><g:message code="analysis.signalPattern.label" default="Signal Pattern" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: analysisInstance, field: 'signalPattern', 'errors')}">
                                    <g:textField name="signalPattern" value="${analysisInstance?.signalPattern}" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="designDataFile"><g:message code="analysis.designDataFile.label" default="Design Data File" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: analysisInstance, field: 'designDataFile', 'errors')}">
                                    <g:textField name="designDataFile" value="${analysisInstance?.designDataFile}" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="moduleFunctionFile"><g:message code="analysis.moduleFunctionFile.label" default="Module Function File" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: analysisInstance, field: 'moduleFunctionFile', 'errors')}">
                                    <g:textField name="moduleFunctionFile" value="${analysisInstance?.moduleFunctionFile}" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="moduleVersionFile"><g:message code="analysis.moduleVersionFile.label" default="Module Version File" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: analysisInstance, field: 'moduleVersionFile', 'errors')}">
                                    <g:textField name="moduleVersionFile" value="${analysisInstance?.moduleVersionFile}" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="variables"><g:message code="analysis.variables.label" default="Variables" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: analysisInstance, field: 'variables', 'errors')}">
                                    <g:textField name="variables" value="${analysisInstance?.variables}" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="colorGroups"><g:message code="analysis.colorGroups.label" default="Color Groups" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: analysisInstance, field: 'colorGroups', 'errors')}">
                                    <g:textField name="colorGroups" value="${analysisInstance?.colorGroups}" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="orderModules"><g:message code="analysis.orderModules.label" default="Order Modules" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: analysisInstance, field: 'orderModules', 'errors')}">
                                    <g:textField name="orderModules" value="${analysisInstance?.orderModules}" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="plotWidth"><g:message code="analysis.plotWidth.label" default="Plot Width" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: analysisInstance, field: 'plotWidth', 'errors')}">
                                    <g:textField name="plotWidth" value="${fieldValue(bean: analysisInstance, field: 'plotWidth')}" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="plotHeight"><g:message code="analysis.plotHeight.label" default="Plot Height" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: analysisInstance, field: 'plotHeight', 'errors')}">
                                    <g:textField name="plotHeight" value="${fieldValue(bean: analysisInstance, field: 'plotHeight')}" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="deltaType"><g:message code="analysis.deltaType.label" default="Delta Type" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: analysisInstance, field: 'deltaType', 'errors')}">
                                    <g:select name="deltaType" from="${analysisInstance.constraints.deltaType.inList}" value="${analysisInstance?.deltaType}" valueMessagePrefix="analysis.deltaType"  />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="minExpression"><g:message code="analysis.minExpression.label" default="Min Expression" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: analysisInstance, field: 'minExpression', 'errors')}">
                                    <g:textField name="minExpression" value="${fieldValue(bean: analysisInstance, field: 'minExpression')}" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="topModule"><g:message code="analysis.topModule.label" default="Top Module" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: analysisInstance, field: 'topModule', 'errors')}">
                                    <g:textField name="topModule" value="${fieldValue(bean: analysisInstance, field: 'topModule')}" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="deltaCut"><g:message code="analysis.deltaCut.label" default="Delta Cut" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: analysisInstance, field: 'deltaCut', 'errors')}">
                                    <g:textField name="deltaCut" value="${fieldValue(bean: analysisInstance, field: 'deltaCut')}" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="foldCut"><g:message code="analysis.foldCut.label" default="Fold Cut" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: analysisInstance, field: 'foldCut', 'errors')}">
                                    <g:textField name="foldCut" value="${fieldValue(bean: analysisInstance, field: 'foldCut')}" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="zscoreCut"><g:message code="analysis.zscoreCut.label" default="Zscore Cut" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: analysisInstance, field: 'zscoreCut', 'errors')}">
                                    <g:textField name="zscoreCut" value="${fieldValue(bean: analysisInstance, field: 'zscoreCut')}" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="chisqPvalue"><g:message code="analysis.chisqPvalue.label" default="Chisq Pvalue" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: analysisInstance, field: 'chisqPvalue', 'errors')}">
                                    <g:textField name="chisqPvalue" value="${fieldValue(bean: analysisInstance, field: 'chisqPvalue')}" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="minDiff"><g:message code="analysis.minDiff.label" default="Min Diff" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: analysisInstance, field: 'minDiff', 'errors')}">
                                    <g:textField name="minDiff" value="${fieldValue(bean: analysisInstance, field: 'minDiff')}" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="minPercent"><g:message code="analysis.minPercent.label" default="Min Percent" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: analysisInstance, field: 'minPercent', 'errors')}">
                                    <g:textField name="minPercent" value="${fieldValue(bean: analysisInstance, field: 'minPercent')}" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="preprocess"><g:message code="analysis.preprocess.label" default="Preprocess" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: analysisInstance, field: 'preprocess', 'errors')}">
                                    <g:select name="preprocess" from="${analysisInstance.constraints.preprocess.inList}" value="${analysisInstance?.preprocess}" valueMessagePrefix="analysis.preprocess"  />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="zMeasure"><g:message code="analysis.zMeasure.label" default="ZM easure" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: analysisInstance, field: 'zMeasure', 'errors')}">
                                    <g:select name="zMeasure" from="${analysisInstance.constraints.zMeasure.inList}" value="${analysisInstance?.zMeasure}" valueMessagePrefix="analysis.zMeasure"  />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="expressionDateFile"><g:message code="analysis.expressionDateFile.label" default="Expression Date File" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: analysisInstance, field: 'expressionDateFile', 'errors')}">
                                    <g:textField name="expressionDateFile" value="${analysisInstance?.expressionDateFile}" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="moduleVersion"><g:message code="analysis.moduleVersion.label" default="Module Version" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: analysisInstance, field: 'moduleVersion', 'errors')}">
                                    <g:textField name="moduleVersion" value="${analysisInstance?.moduleVersion}" />
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
