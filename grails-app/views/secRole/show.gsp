<%@ page import="common.SecUserSecRole; common.SecRole" %>
<html>
<head>
  <meta name="layout" content="main">
  <title>View Role - ${secRoleInstance.authority}</title>
</head>

<body>
<div id="show-secRole" class="container" role="main">
  <h2>${secRoleInstance.authority} <small><g:link action="list">&laquo; Back to Roles</g:link></small></h2>
  <g:if test="${flash.message}">
    <div class="alert-message info" role="status">${flash.message}</div>
  </g:if>
  <table class="zebra-striped pretty-table">
    <thead><tr>
      <th>User</th>
      <th>Account Expired</th>
      <th>Account Locked</th>
      <th>Enabled</th>
      <th>Password Expired</th>
    </tr></thead>
    <tbody>
    <g:each in="${SecUserSecRole.findAllBySecRole(secRoleInstance).secUser}" var="user">
      <tr>
        <td><g:link controller="secUser" action="show" id="${user.id}">${user.username}</g:link></td>
        <td><span class="ui-icon-<g:formatBoolean boolean="${user.accountExpired}" true="cross" false=""/>"></span></td>
        <td><span class="ui-icon-<g:formatBoolean boolean="${user.accountLocked}" true="lock" false=""/>"></span></td>
        <td><span class="ui-icon-<g:formatBoolean boolean="${user.enabled}" true="tick" false="cross"/>"></span></td>
        <td><span class="ui-icon-<g:formatBoolean boolean="${user.passwordExpired}" true="cross" false=""/>"></span></td>
      </tr>
    </g:each>
    </tbody>
  </table>

  <g:form>
    <fieldset style="text-align: center;">
      <g:hiddenField name="id" value="${secRoleInstance?.id}"/>
      <g:actionSubmit class="btn primary" action="delete"
                      value="Delete Role"
                      onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');"/>
    </fieldset>
  </g:form>
</div>
</body>
</html>
