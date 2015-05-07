<%@ page import="org.sagres.sampleSet.SampleSetLink" %>
<html>
<head>
  <meta name="layout" content="sampleSetMain">
  <title>Sample Set Link - Show</title>
  <style type="text/css">
    .showTable tr td:first-child { text-align: right; width: 150px; }
  </style>
</head>

<body>
<div id="show-sampleSetLink" class="sampleset-container" role="main">
  <h2>${sampleSetLinkInstance.displayName} <small><g:link action="list">&laquo; Back to Sample Set Links</g:link> </small></h2>
  <g:if test="${flash.message}">
    <div class="alert-message info" role="status">${flash.message}</div>
  </g:if>
  <table class="showTable">
    <tbody>
      <tr><td>Name</td><td>${sampleSetLinkInstance.name}</td></tr>
      <tr><td>Display Name</td><td>${sampleSetLinkInstance.displayName}</td></tr>
      <tr><td>Base URL</td><td>${sampleSetLinkInstance.baseUrl}</td></tr>
      <tr><td>Icon</td><td><span class="ui-icon-${sampleSetLinkInstance.icon}"></span></td></tr>
      <tr><td>Visible</td><td><span class="ui-icon-${sampleSetLinkInstance.visible == 1 ? "tick" : "cross"}"></span></td></tr>
    </tbody>
  </table>
   <g:form>
    <div class="actions">
      <g:hiddenField name="id" value="${sampleSetLinkInstance?.id}"/>
      <g:link class="btn primary" action="edit" id="${sampleSetLinkInstance?.id}">Edit</g:link>
      <g:actionSubmit class="btn" action="delete"
                      value="${message(code: 'default.button.delete.label', default: 'Delete')}"
                      onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');"/>
    </div>
  </g:form>
</div>
</body>
</html>
