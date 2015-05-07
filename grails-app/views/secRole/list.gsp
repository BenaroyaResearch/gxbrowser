<%@ page import="common.SecRole" %>
<html>
<head>
  <meta name="layout" content="main">
  <title>User Roles</title>
</head>

<body>
<div id="list-secRole" class="container" role="main">
  <h2>User Roles</h2>
  <g:if test="${flash.message}">
    <div class="alert-message info" role="status">${flash.message}</div>
  </g:if>
  <table class="zebra-striped">
    <thead>
    <tr>
      <g:sortableColumn property="authority" title="${message(code: 'secRole.authority.label', default: 'Authority')}"/>
    </tr>
    </thead>
    <tbody>
    <g:each in="${secRoleInstanceList}" status="i" var="secRoleInstance">
      <tr>
      <td>
        <g:link action="show" id="${secRoleInstance.id}">${fieldValue(bean: secRoleInstance, field: "authority")}</g:link>
      </td>
      </tr>
    </g:each>
    </tbody>
  </table>

  <div style="text-align: center;"><g:link action="create" class="btn primary large">Create New Role</g:link></div>

</div>
</body>
</html>
