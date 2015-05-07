<html>
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
  <meta name="layout" content="matmain">
  <link rel="stylesheet" href="${resource(dir: 'css', file: 'sampleset-main.css')}"/>
  <title>${module.moduleName}: Edit Annotation</title>
</head>

<body>
<div class="mat-container">
  <g:form action="deleteModuleAnnotation" id="${module.id}">
    <button class="btn danger" type="submit">Delete Annotation For This Module</button>
  </g:form>
  <h2>${module.moduleName}: Edit Annotation</h2>
  <h4>Changes made to this annotation will only apply to module ${module.moduleName}.</h4>
  <g:form action="updateModuleAnnotation" id="${module.id}">
    <fieldset>
      <div class="clearfix">
        <label for="name">Name</label>
        <div class="input">
          <g:textField name="name" value="${annotation?.annotation}"/>
        </div>
      </div>
      <div class="clearfix">
        <label for="name">Hex Color</label>
        <div class="input">
          <g:textField name="color" value="${annotation?.hexColor}"/>
        </div>
      </div>
      <div class="clearfix">
        <label for="name">Abbreviation</label>
        <div class="input">
          <g:textField name="abbrev" value="${annotation?.abbreviation}"/>
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
