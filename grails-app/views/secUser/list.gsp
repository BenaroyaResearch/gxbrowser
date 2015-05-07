<%@ page import="common.SecUserSecRole; common.SecUser" %>
<!doctype html>
<html>
<head>
  <meta name="layout" content="main">
  <title>Users</title>
</head>

<body>

<div id="list-secUser" class="container" role="main">

  <h2>Users</h2>
  <g:if test="${flash.message}">
    <div class="alert-message info" role="status">${flash.message}</div>
  </g:if>

  <table class="pretty-table">
    <thead>
    <tr>

      <g:sortableColumn property="username" title="${message(code: 'secUser.username.label', default: 'Username')}"/>



      <g:sortableColumn property="accountExpired"
                        title="${message(code: 'secUser.accountExpired.label', default: 'Account Expired')}"/>

      <g:sortableColumn property="accountLocked"
                        title="${message(code: 'secUser.accountLocked.label', default: 'Account Locked')}"/>

      <g:sortableColumn property="enabled" title="${message(code: 'secUser.enabled.label', default: 'Enabled')}"/>

      <g:sortableColumn property="passwordExpired"
                        title="${message(code: 'secUser.passwordExpired.label', default: 'Password Expired')}"/>

      <th>Roles</th>

    </tr>
    </thead>
    <tbody>
    <g:each in="${secUserInstanceList}" status="i" var="secUserInstance">
      <tr class="${(i % 2) == 0 ? 'even' : 'odd'}">

        <td><g:link action="show"
                    id="${secUserInstance.id}">${fieldValue(bean: secUserInstance, field: "username")}</g:link></td>


        <td><span class="ui-icon-<g:formatBoolean boolean="${secUserInstance.accountExpired}" true="cross" false=""/>"></span></td>

        <td><span class="ui-icon-<g:formatBoolean boolean="${secUserInstance.accountLocked}" true="lock" false=""/>"></span></td>

        <td><span class="ui-icon-<g:formatBoolean boolean="${secUserInstance.enabled}" true="tick" false="cross"/>"></span></td>

        <td><span class="ui-icon-<g:formatBoolean boolean="${secUserInstance.passwordExpired}" true="cross" false=""/>"></span></td>

        <td>
          <g:join in="${SecUserSecRole.findAllBySecUser(secUserInstance).secRole}" delimiter=", "/>
        </td>

      </tr>
    </g:each>
    </tbody>
  </table>

  <div style="text-align: center;">
    <g:link action="create" class="btn primary large">Create New User</g:link>
  </div>

</div>
</body>
</html>
