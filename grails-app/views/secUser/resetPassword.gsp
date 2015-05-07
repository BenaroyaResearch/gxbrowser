<html>
<head>
  <meta name="layout" content="main">
  <title>Reset Password</title>
  <style type="text/css">
    .large-font { font-size: 16px; color: #333333; padding: 10px 0; }
  </style>
</head>

<body>
<div class="container">
  <h2>Reset Your Password</h2>
  <g:if test="${flash.message}">
    <div class="alert-message error" role="status">${flash.message}</div>
  </g:if>
  <div>
    <g:form action="resetPassword">
      <g:hiddenField name="secretKey" value="${params.resetkey}"/>
      <fieldset>
      <div class="clearfix">
        <label>Username</label>
        <div class="input">
          <g:textField name="username" value="${params.resetuser}"/>
        </div>
      </div>
      <div class="clearfix">
        <label>New Password</label>
        <div class="input">
          <g:passwordField name="newPassword"/>
        </div>
      </div>
      <div class="clearfix">
        <label>New Password Again</label>
        <div class="input">
          <g:passwordField name="newPassword2"/>
        </div>
      </div>
      <div class="clearfix">
        <div class="input">
          <button type="submit" class="btn primary large">Reset</button>
        </div>
      </div>
      </fieldset>
    </g:form>
  </div>
</div>
</body>
</html>
