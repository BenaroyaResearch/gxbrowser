<%@ page import="common.SecRole; common.SecUserSecRole; common.SecUser" %>
<!doctype html>
<html>
<head>
  <meta name="layout" content="main">
  <title>Edit User</title>
</head>

<body>
<div id="show-secUser" class="container" role="main">
  <h2>Edit User</h2>
  <g:if test="${flash.message}">
    <div class="alert-message info" role="status">${flash.message}</div>
  </g:if>
  <g:form action="update" id="${secUserInstance.id}">
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
        <label for="enabled">Enabled</label>
        <div class="input">
          <ul class="inputs-list">
            <li>
              <g:checkBox name="enabled" checked="${secUserInstance.enabled}"/>
            </li>
          </ul>
        </div>
      </div>
      <div class="clearfix">
        <label for="accountLocked">Account Locked</label>
        <div class="input">
          <ul class="inputs-list">
            <li><g:checkBox name="accountLocked" checked="${secUserInstance.accountLocked}"/></li>
          </ul>
        </div>
      </div>
      <div class="clearfix">
        <label>Roles</label>
        <div class="input">
          <ul class="inputs-list">
            <g:set var="userRoles" value="${SecUserSecRole.findAllBySecUser(secUserInstance).secRole.id.toList()}"/>
            <g:each in="${SecRole.list()}" var="role">
              <li><label>
                <g:checkBox name="optionsRoles" value="${role.id}" checked="${userRoles.contains(role.id)}"/>
                <span>${role.authority}</span>
              </label></li>
            </g:each>
          </ul>
        </div>
      </div>
    </fieldset>
    <div class="actions">
      <button class="btn primary">Update</button>
      <g:link action="show" id="${secUserInstance.id}" class="btn">Cancel</g:link>
    </div>
  </g:form>
</div>
</body>
</html>
