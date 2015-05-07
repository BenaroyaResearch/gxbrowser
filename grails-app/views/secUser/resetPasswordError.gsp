<html>
<head>
  <meta name="layout" content="main">
  <title>Reset Password Error</title>
  <style type="text/css">
    .large-font { font-size: 16px; color: #333333; padding: 10px 0; }
  </style>
</head>

<body>
<div class="container">
  <h2>Reset Password Error</h2>
  <g:if test="${flash.message}">
    <div class="alert-message error" role="status">${flash.message}</div>
  </g:if>
  <div class="large-font">
    There was a problem with resetting your password.
  </div>
</div>
</body>
</html>
