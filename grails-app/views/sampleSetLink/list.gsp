<%@ page import="org.sagres.sampleSet.SampleSetLink" %>
<!doctype html>
<html>
<head>
  <meta name="layout" content="sampleSetMain">
  <title>Sample Set Links</title>
</head>

<body>
<div id="list-sampleSetLink" class="sampleset-container" role="main">
  <h2>Sample Set Links</h2>
  <g:if test="${flash.message}">
    <div class="alert-message info" role="status">${flash.message}</div>
  </g:if>
  <table class="pretty-table zebra-striped">
    <thead>
    <tr>
      <g:sortableColumn property="name" title="${message(code: 'sampleSetLink.name.label', default: 'Name')}"/>

      <g:sortableColumn property="displayName"
                        title="${message(code: 'sampleSetLink.displayName.label', default: 'Display Name')}"/>

      <g:sortableColumn property="baseUrl"
                        title="${message(code: 'sampleSetLink.baseUrl.label', default: 'Base Url')}"/>

      <th>Icon</th>
    </tr>
    </thead>
    <tbody>
    <g:each in="${sampleSetLinkInstanceList}" status="i" var="sampleSetLinkInstance">
      <tr>

        <td><g:link action="show"
                    id="${sampleSetLinkInstance.id}">${fieldValue(bean: sampleSetLinkInstance, field: "name")}</g:link></td>

        <td>${fieldValue(bean: sampleSetLinkInstance, field: "displayName")}</td>

        <td>${fieldValue(bean: sampleSetLinkInstance, field: "baseUrl")}</td>

        <td>${fieldValue(bean: sampleSetLinkInstance, field: "icon")}</td>

      </tr>
    </g:each>
    </tbody>
  </table>

  <div class="pagination">
    <g:paginate total="${sampleSetLinkInstanceTotal}"/>
  </div>
</div>
</body>
</html>
