<%@ page import="org.sagres.mat.ModuleGeneration" %>
<html>
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
  <meta name="layout" content="matmain">
  <link rel="stylesheet" href="${resource(dir: 'css', file: 'sampleset-main.css')}"/>
  <title>New Module Generation</title>
</head>

<body>
<div class="mat-container">
<h2>New Module Generation</h2>
<g:if test="${flash.message}">
  <div class="message">${flash.message}</div>
</g:if>
<g:hasErrors bean="${moduleGenerationInstance}">
  <div class="errors">
    <g:renderErrors bean="${moduleGenerationInstance}" as="list"/>
  </div>
</g:hasErrors>
<g:form enctype="multipart/form-data">
  <fieldset>
    <div class="clearfix">
      <label for="versionName">Generation Name</label>
      <div class="input">
        <g:textField name="versionName" value="${moduleGenerationInstance?.versionName}" class="medium"/>
      </div>
    </div>
    <div class="clearfix">
      <label for="generation">Generation</label>
      <div class="input">
        <g:textField name="generation" value="${moduleGenerationInstance?.generation}" class="small"/> (1-4)
      </div>
    </div>
    <div class="clearfix">
      <label for="versionFile">Version File</label>
      <div class="input">
        <input type="file" name="versionFile" id="versionFile"/>
      </div>
    </div>
    <div class="clearfix">
      <label for="functionFile">Function File</label>
      <div class="input">
        <input type="file" name="functionFile" id="functionFile"/>
      </div>
    </div>
    <div class="clearfix">
      <label for="chipTypeId">Original Chip Type</label>
      <div class="input">
        <g:select from="${chipTypes}" name="chipTypeId" optionKey="id" optionValue="name"/> (This is for mapping the probe IDs to gene symbols)
      </div>
    </div>
  </fieldset>

  <div class="actions">
    <button class="btn primary" type="submit" name="_action_save">Create Module Generation</button>
    <button class="btn" type="submit" name="_action_cancel">Cancel</button>
  </div>
  </div>
</g:form>
</div>
</body>
</html>
