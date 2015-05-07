<html>
<head>
  <meta name="layout" content="main">
  <title>Account Management: <sec:username/></title>
  <style type="text/css">
    .large-font { font-size: 16px; color: #333333; padding: 10px 0; }
  </style>
</head>

<body>
<div class="container">
  <h2>Welcome, <sec:username/></h2>
  <br/><br/>
  <g:if test="${flash.message}">
    <div class="alert-message info" role="status">${flash.message}</div>
  </g:if>
  <div class="large-font">Your username is <strong><sec:username/></strong></div>
  <div class="large-font">Your email is <strong>${user.email}</strong></div>
  <div class="large-font">
    <g:link action="updatePassword" class="btn">Change my password</g:link>
    <g:link action="sendPasswordReset" class="btn primary">I forgot my password</g:link>
  </div>
  <div style="margin-top: 50px;">
  <g:link controller="logout" class="btn primary large">Logout</g:link>
  </div>
</div>
</body>
</html>
