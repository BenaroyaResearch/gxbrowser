<%@ page import="common.SecUserSecRole; common.SecUser" %>
<!doctype html>
<html>
<head>
  <meta name="layout" content="main">
  <title>View User</title>
</head>

<body>
<div id="show-secUser" class="container" role="main">
  <h2>User: ${secUserInstance.username} <small><g:link action="list">&laquo; Back to Users</g:link></small></h2>
  <g:if test="${flash.message}">
    <div class="alert-message info" role="status">${flash.message}</div>
  </g:if>
  <g:form>
    <fieldset>
      <div class="clearfix">
        <label for="username">Username</label>
        <div class="input">
          <label style="text-align: left;"><g:fieldValue bean="${secUserInstance}" field="username"/></label>
        </div>
      </div>
      <div class="clearfix">
        <label for="email">Email</label>
        <div class="input">
          <label style="text-align: left;"><g:fieldValue bean="${secUserInstance}" field="email"/></label>
        </div>
      </div>
      <div class="clearfix">
        <label>Account Expired</label>
        <div class="input">
          <label style="text-align: left;"><span class="ui-icon-<g:formatBoolean boolean="${secUserInstance.accountExpired}" true="cross" false="tick"/>"></span></label>
        </div>
      </div>
      <div class="clearfix">
        <label>Account Locked</label>
        <div class="input">
          <label style="text-align: left;"><span class="ui-icon-<g:formatBoolean boolean="${secUserInstance.accountLocked}" true="lock" false="tick"/>"></span></label>
        </div>
      </div>
      <div class="clearfix">
        <label>Enabled</label>
        <div class="input">
          <label style="text-align: left;"><span class="ui-icon-<g:formatBoolean boolean="${secUserInstance.enabled}" true="tick" false="cross"/>"></span></label>
        </div>
      </div>
      <div class="clearfix">
        <label>Password Expired</label>
        <div class="input">
          <label style="text-align: left;"><span class="ui-icon-<g:formatBoolean boolean="${secUserInstance.passwordExpired}" true="cross" false="tick"/>"></span></label>
        </div>
      </div>
      <div class="clearfix">
        <label>Roles</label>
        <div class="input">
          <ul class="inputs-list unstyled">
            <g:each in="${SecUserSecRole.findAllBySecUser(secUserInstance).secRole}" var="role">
              <li>${role.authority}</li>
            </g:each>
          </ul>
        </div>
      </div>
    </fieldset>
    <div class="actions">
      <g:hiddenField name="id" value="${secUserInstance?.id}"/>
      <g:link class="btn primary" action="edit" id="${secUserInstance.id}">Edit</g:link>
      <g:actionSubmit class="btn danger" action="delete"
                      value="${message(code: 'default.button.delete.label', default: 'Delete')}"
                      onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');"/>
    </div>
  </g:form>
</div>
</body>
</html>
