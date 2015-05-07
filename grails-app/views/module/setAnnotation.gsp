<html>
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
  <meta name="layout" content="matmain">
  <link rel="stylesheet" href="${resource(dir: 'css', file: 'sampleset-main.css')}"/>
  <title>Module: Set Annotation</title>
</head>

<body>
<div class="mat-container">
  <h2>${module.moduleName}: Set Module Annotation</h2>
  <g:form action="setAnnotation" id="${module.id}">
    <fieldset>
      <div class="clearfix">
        <label for="annotation">Annotation</label>
        <div class="input">
          <g:select name="annotation" from="${annotations}" value="${moduleAnnotation}"/>
        </div>
      </div>
    </fieldset>
    <div class="actions">
      <button class="btn primary" type="submit">Save</button>
      <g:link class="btn" controller="moduleGeneration" action="show" id="${module.moduleGenerationId}">Cancel</g:link>
    </div>
  </g:form>
</div>
</body>
</html>
