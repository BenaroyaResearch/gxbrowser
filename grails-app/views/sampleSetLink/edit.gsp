<%@ page import="org.sagres.sampleSet.SampleSetLink" %>
<html>
<head>
  <meta name="layout" content="sampleSetMain">
  <title>Sample Set Link - Edit</title>
</head>

<body>
<div id="edit-sampleSetLink" class="sampleset-container" role="main">
  <h2>Edit ${sampleSetLinkInstance.displayName}</h2>
  <g:if test="${flash.message}">
    <div class="alert-message info" role="status">${flash.message}</div>
  </g:if>
  <g:hasErrors bean="${sampleSetLinkInstance}">
    <g:eachError bean="${sampleSetLinkInstance}" var="error">
      <div class="alert-message error"><g:message error="${error}"/></div>
    </g:eachError>
  </g:hasErrors>
  <g:form method="post">
    <g:hiddenField name="id" value="${sampleSetLinkInstance?.id}"/>
    <g:hiddenField name="version" value="${sampleSetLinkInstance?.version}"/>
    <fieldset class="form">
      <div class="clearfix">
        <label for="name">Name</label>
        <div class="input">
          <g:textField name="name" value="${sampleSetLinkInstance.name}"/>
        </div>
      </div>
      <div class="clearfix">
        <label for="displayName">Display Name</label>
        <div class="input">
          <g:textField name="displayName" value="${sampleSetLinkInstance.displayName}"/>
        </div>
      </div>
      <div class="clearfix">
        <label for="baseUrl">Base URL</label>
        <div class="input">
          <g:textField name="baseUrl" value="${sampleSetLinkInstance.baseUrl}"/>
        </div>
      </div>
      <div class="clearfix">
        <label for="icon">Icon</label>
        <div class="input">
          <g:textField name="icon" value="${sampleSetLinkInstance.icon}"/>
        </div>
      </div>
      <div class="clearfix">
        <label for="visible">Visible</label>
        <div class="input">
          <ul class="inputs-list">
            <li>
              <g:checkBox name="visible" value="1" checked="${sampleSetLinkInstance.visible}"/>
            </li>
          </ul>
        </div>
      </div>
    </fieldset>
    <div class="actions">
      <g:actionSubmit class="btn primary" action="update"
                      value="${message(code: 'default.button.update.label', default: 'Update')}"/>
      <g:actionSubmit class="btn danger" action="delete"
                      value="${message(code: 'default.button.delete.label', default: 'Delete')}" formnovalidate=""
                      onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');"/>
      <g:link action="show" id="${sampleSetLinkInstance.id}" class="btn">Cancel</g:link>
    </div>
  </g:form>
</div>
</body>
</html>
