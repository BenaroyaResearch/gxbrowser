<html>
<head>
  <meta name="layout" content="main">
  <title>Forgot Password</title>
  <style type="text/css">
    .large-font { font-size: 16px; color: #333333; padding: 10px 0; }
  </style>
</head>

<body>
<div class="container">
  <h2>Forgot Password?</h2>
  <g:if test="${flash.message}">
    <div class="alert-message error" role="status">${flash.message}</div>
  </g:if>
  <div class="large-font">No problem!</div>
  <div>Enter your username OR email address to reset your password. A link to reset your password will be sent to email address on your account.</div>
  <div style="margin-top: 10px;">
    <g:form action="sendPasswordReset">
      <fieldset>
      <div class="clearfix">
        <label>Username</label>
        <div class="input">
          <g:textField name="username"/>
        </div>
      </div>
      <div class="clearfix">
        <div class="input">
          OR
        </div>
      </div>
      <div class="clearfix">
        <label>Email</label>
        <div class="input">
          <g:textField name="email"/>
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
