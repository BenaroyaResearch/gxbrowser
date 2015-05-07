<%@ page import="org.sagres.mat.ModuleGeneration" %>
<html>
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
  <meta name="layout" content="matmain">
  <link rel="stylesheet" href="${resource(dir: 'css', file: 'sampleset-main.css')}"/>
  <title>Edit Module Generation</title>
</head>

<body>
<div class="mat-container">
  <h2>Edit Module Generation</h2>
  <g:if test="${flash.message}">
    <div class="message">${flash.message}</div>
  </g:if>
  <g:hasErrors bean="${moduleGeneration}">
    <div class="errors">
      <g:renderErrors bean="${moduleGeneration}" as="list"/>
    </div>
  </g:hasErrors>
  <g:form enctype="multipart/form-data" action="update" id="${moduleGeneration.id}">
    <fieldset>
      <div class="clearfix">
        <label for="versionName">Version Name</label>
        <div class="input">
          <g:textField name="versionName" value="${moduleGeneration.versionName}" class="medium"/>
        </div>
      </div>
      <div class="clearfix">
        <label for="generation">Generation</label>
        <div class="input">
          <g:textField name="generation" value="${moduleGeneration.generation}" class="small"/> (1,2,3,...)
        </div>
      </div>
    </fieldset>
    <div class="actions">
      <button class="btn primary" type="submit">Update Module Generation</button>
      <button class="btn" type="submit" name="_action_cancel">Cancel</button>
    </div>
    </div>
  </g:form>
</div>
</body>
</html>