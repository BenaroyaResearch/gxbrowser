<html>
<head>
  <meta name="layout" content="main">
  <title>Reset Password Sent</title>
  <style type="text/css">
    .large-font { font-size: 16px; color: #333333; padding: 10px 0; }
  </style>
</head>

<body>
<div class="container">
  <h2>Reset Password Sent!</h2>
  <g:if test="${flash.message}">
    <div class="alert-message error" role="status">${flash.message}</div>
  </g:if>
  <div class="large-font">
    An email with a link to reset your password has been sent. Check your mailbox and follow the instructions.
  </div>
</div>
</body>
</html>
