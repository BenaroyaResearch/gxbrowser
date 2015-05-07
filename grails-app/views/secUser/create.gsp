<%@ page import="common.SecRole; common.SecUser" %>
<html>
<head>
  <meta name="layout" content="main">
  <title>Add User</title>
</head>

<body>

<div id="create-secUser" class="container" role="main">
  <h2>Add User</h2>
  <g:if test="${flash.message}">
    <div class="alert-message info" role="status">${flash.message}</div>
  </g:if>
  <g:hasErrors bean="${secUserInstance}">
    <g:eachError bean="${secUserInstance}" var="error">
      <div class="alert-message error"><g:message error="${error}"/></div>
    </g:eachError>
  </g:hasErrors>
  <g:form>
    <fieldset class="form">
      <div class="clearfix fieldcontain required">
        <label for="username">Username<span class="required-indicator">*</span></label>
        <div class="input">
          <g:textField name="username"/>
        </div>
      </div>
      <div class="clearfix fieldcontain required">
        <label for="password">Password<span class="required-indicator">*</span></label>
        <div class="input">
          <g:passwordField name="password"/>
        </div>
      </div>
      <div class="clearfix fieldcontain required">
        <label for="email">Email<span class="required-indicator">*</span></label>
        <div class="input">
          <g:textField name="email"/>
        </div>
      </div>
      <div class="clearfix">
        <label for="roles">Roles</label>
        <div class="input">
          <g:select name="roles" from="${SecRole.list()}" optionKey="id" optionValue="authority" multiple="true" style="height: 5em;"/>
        </div>
      </div>
    </fieldset>

    <div class="actions">
      <button class="btn primary" type="submit" name="_action_save">Add User</button>
      <button class="btn" type="submit" name="_action_cancel">Cancel</button>
    </div>
  </g:form>
</div>
</body>
</html>
