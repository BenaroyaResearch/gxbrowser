<%@ page import="org.sagres.sampleSet.SampleSetLink" %>
<!doctype html>
<html>
<head>
  <meta name="layout" content="sampleSetMain">
  <title>Sample Set Link - Create</title>
</head>

<body>
<div id="create-sampleSetLink" class="sampleset-container" role="main">
  <h2>Create Sample Set Link</h2>
  <g:if test="${flash.message}">
    <div class="alert-message error" role="status">${flash.message}</div>
  </g:if>
  <g:hasErrors bean="${sampleSetLinkInstance}">
    <g:eachError bean="${sampleSetLinkInstance}" var="error">
      <div class="alert-message error"><g:message error="${error}"/></div>
    </g:eachError>
  </g:hasErrors>
  <g:form>
    <fieldset>
      <div class="clearfix">
        <label for="name">Name</label>
        <div class="input"><g:textField name="name"/></div>
      </div>
      <div class="clearfix">
        <label for="displayName">Display Name</label>
        <div class="input"><g:textField name="displayName"/></div>
      </div>
      <div class="clearfix">
        <label for="baseUrl">Base URL</label>
        <div class="input"><g:textField name="baseUrl"/></div>
      </div>
      <div class="clearfix">
        <label for="icon">Icon</label>
        <div class="input"><g:textField name="icon"/></div>
      </div>
    </fieldset>
    <div class="actions">
      <button class="btn primary" type="submit" name="_action_save">Add Link</button>
      <button class="btn" type="submit" name="_action_cancel">Cancel</button>
    </div>
  </g:form>
</div>
</body>
</html>
