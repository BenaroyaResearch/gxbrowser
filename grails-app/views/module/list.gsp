<%@ page import="org.sagres.mat.Module" %>
<html>
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
  <meta name="layout" content="main"/>
  <g:set var="entityName" value="${message(code: 'module.label', default: 'Module')}"/>
  <title><g:message code="default.list.label" args="[entityName]"/></title>
</head>

<body>
<div class="nav">
  <span class="menuButton"><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a>
  </span>
  <span class="menuButton"><g:link class="create" action="create"><g:message code="default.new.label"
                                                                             args="[entityName]"/></g:link></span>
</div>

<div class="body">
  <h1><g:message code="default.list.label" args="[entityName]"/></h1>
  <g:if test="${flash.message}">
    <div class="message">${flash.message}</div>
  </g:if>
  <div class="list">
    <table>
      <thead>
      <tr>

        <g:sortableColumn property="id" title="${message(code: 'module.id.label', default: 'Id')}"/>

        <g:sortableColumn property="moduleName"
                          title="${message(code: 'module.moduleName.label', default: 'Module Name')}"/>

        <g:sortableColumn property="moduleAnnotationId"
                          title="${message(code: 'module.moduleAnnotationId.label', default: 'Module Annotation Id')}"/>

        <g:sortableColumn property="moduleGenerationId"
                          title="${message(code: 'module.moduleGenerationId.label', default: 'Module Generation Id')}"/>

        <g:sortableColumn property="probeCount"
                          title="${message(code: 'module.probeCount.label', default: 'Probe Count')}"/>

      </tr>
      </thead>
      <tbody>
      <g:each in="${moduleInstanceList}" status="i" var="moduleInstance">
        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">

          <td><g:link action="show"
                      id="${moduleInstance.id}">${fieldValue(bean: moduleInstance, field: "id")}</g:link></td>

          <td>${fieldValue(bean: moduleInstance, field: "moduleName")}</td>

          <td>${fieldValue(bean: moduleInstance, field: "moduleAnnotationId")}</td>

          <td>${fieldValue(bean: moduleInstance, field: "moduleGenerationId")}</td>

          <td>${fieldValue(bean: moduleInstance, field: "probeCount")}</td>

        </tr>
      </g:each>
      </tbody>
    </table>
  </div>

  <div class="paginateButtons">
    <g:paginate total="${moduleInstanceTotal}"/>
  </div>
</div>
</body>
</html>
