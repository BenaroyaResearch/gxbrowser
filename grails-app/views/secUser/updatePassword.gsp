<html>
<head>
  <meta name="layout" content="main">
  <title>Change Password</title>
</head>

<body>
<div class="container">
  <h2>Change Your Password</h2>
  <g:if test="${flash.message}">
    <div class="alert-message error" role="status">${flash.message}</div>
  </g:if>
  <g:form controller="secUser" action="updatePassword">
    <fieldset>
      <div class="clearfix">
        <label>Username</label>
        <div class="input">
          <label><div style="text-align: left;"><sec:username/></div></label>
        </div>
      </div>
      <div class="clearfix">
        <label for="password">Current Password</label>
        <div class="input">
          <g:passwordField name="password"/>
        </div>
      </div>
      <div class="clearfix">
        <label for="password_new">New Password</label>
        <div class="input">
          <g:passwordField name="password_new"/>
        </div>
      </div>
      <div class="clearfix">
        <label for="password_new2">New Password Again</label>
        <div class="input">
          <g:passwordField name="password_new2"/>
        </div>
      </div>
    </fieldset>
    <div class="actions">
      <button class="btn primary" type="submit">Change Password</button>
      <g:link class="btn" action="account">Cancel</g:link>
    </div>
  </g:form>
</div>
</body>
</html>
