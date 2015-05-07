<%@ page import="org.sagres.mat.ModuleAnnotation; org.sagres.mat.Module" %>
<html>
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
  <meta name="layout" content="main"/>
  <g:set var="entityName" value="${message(code: 'module.label', default: 'Module')}"/>
  <title><g:message code="default.edit.label" args="[entityName]"/></title>
</head>

<body>
<div class="nav">
  <span class="menuButton"><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a>
  </span>
  <span class="menuButton"><g:link class="list" action="list"><g:message code="default.list.label"
                                                                         args="[entityName]"/></g:link></span>
  <span class="menuButton"><g:link class="create" action="create"><g:message code="default.new.label"
                                                                             args="[entityName]"/></g:link></span>
</div>

<div class="body">
  <h1><g:message code="default.edit.label" args="[entityName]"/></h1>
  <g:if test="${flash.message}">
    <div class="message">${flash.message}</div>
  </g:if>
  <g:hasErrors bean="${moduleInstance}">
    <div class="errors">
      <g:renderErrors bean="${moduleInstance}" as="list"/>
    </div>
  </g:hasErrors>
  <g:form method="post">
    <g:hiddenField name="id" value="${moduleInstance?.id}"/>
    <g:hiddenField name="version" value="${moduleInstance?.version}"/>
    <div class="dialog">
      <table>
        <tbody>

        <tr class="prop">
          <td valign="top" class="name">
            <label for="moduleName"><g:message code="module.moduleName.label" default="Module Name"/></label>
          </td>
          <td valign="top" class="value ${hasErrors(bean: moduleInstance, field: 'moduleName', 'errors')}">
            <g:textField name="moduleName" value="${moduleInstance?.moduleName}"/>
          </td>
        </tr>

        <tr class="prop">
          <td valign="top" class="name">
            <label for="moduleAnnotationId"><g:message code="module.moduleAnnotationId.label"
                                                       default="Module Annotation Id"/></label>
          </td>
          <td valign="top" class="value ${hasErrors(bean: moduleInstance, field: 'moduleAnnotationId', 'errors')}">
            <g:select from="${annotations}" name="moduleAnnotationId" optionKey="id" optionValue="annotation"
                      value="${fieldValue(bean: moduleInstance, field: 'moduleAnnotationId')}"/>
            %{--<g:textField name="moduleAnnotationId"--}%
                         %{--value="${fieldValue(bean: moduleInstance, field: 'moduleAnnotationId')}"/>--}%
          </td>
        </tr>

        <tr class="prop">
          <td valign="top" class="name">
            <label for="moduleGenerationId"><g:message code="module.moduleGenerationId.label"
                                                       default="Module Generation Id"/></label>
          </td>
          <td valign="top" class="value ${hasErrors(bean: moduleInstance, field: 'moduleGenerationId', 'errors')}">
            <g:textField name="moduleGenerationId"
                         value="${fieldValue(bean: moduleInstance, field: 'moduleGenerationId')}"/>
          </td>
        </tr>

        <tr class="prop">
          <td valign="top" class="name">
            <label for="probeCount"><g:message code="module.probeCount.label" default="Probe Count"/></label>
          </td>
          <td valign="top" class="value ${hasErrors(bean: moduleInstance, field: 'probeCount', 'errors')}">
            <g:textField name="probeCount" value="${fieldValue(bean: moduleInstance, field: 'probeCount')}"/>
          </td>
        </tr>

        </tbody>
      </table>
    </div>

    <div class="buttons">
      <span class="button"><g:actionSubmit class="save" action="update"
                                           value="${message(code: 'default.button.update.label', default: 'Update')}"/></span>
      <span class="button"><g:actionSubmit class="delete" action="delete"
                                           value="${message(code: 'default.button.delete.label', default: 'Delete')}"
                                           onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');"/></span>
    </div>
  </g:form>
</div>
</body>
</html>
