<%@ page import="common.SecRole" %>
<html>
<head>
  <meta name="layout" content="main">
  <title>Create Role</title>
</head>

<body>
<div id="create-secRole" class="container" role="main">
  <h2>Create Role</h2>
  <g:if test="${flash.message}">
    <div class="alert-message info" role="status">${flash.message}</div>
  </g:if>
  <g:hasErrors bean="${secRoleInstance}">
    <g:eachError bean="${secRoleInstance}" var="error">
      <div class="alert-message error"><g:message error="${error}"/></div>
    </g:eachError>
  </g:hasErrors>
  <g:form action="save">
    <fieldset>
      <div class="clearfix">
        <label for="authority">Authority</label>
        <div class="input">
          <g:textField name="authority"/>
        </div>
      </div>
    </fieldset>
    <div class="actions">
      <g:submitButton name="create" class="btn primary"
                      value="${message(code: 'default.button.create.label', default: 'Create')}"/>
      <g:link action="cancel" class="btn">Cancel</g:link>
    </fieldset>
  </g:form>
</div>
</body>
</html>
