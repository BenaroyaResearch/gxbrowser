<html>
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
  <meta name="layout" content="matmain">
  <link rel="stylesheet" href="${resource(dir: 'css', file: 'sampleset-main.css')}"/>
  <title>Edit Module's Annotation</title>
</head>

<body>
<div class="mat-container">
  <h2>${moduleAnnotationInstance.moduleName}: Editing Annotation - ${moduleAnnotationInstance.annotation}</h2>
  <g:form action="update" id="${moduleAnnotationInstance.id}">
    <fieldset>
      <div class="clearfix">
        <label for="name">Name</label>
        <div class="input">
          <g:textField name="name" class="xlarge" value="${moduleAnnotationInstance.annotation}"/>
        </div>
      </div>
      <div class="clearfix">
        <label for="color">Hex Color</label>
        <div class="input">
          <g:textField name="color" class="xlarge" value="${moduleAnnotationInstance.hexColor}"/>
        </div>
      </div>
      <div class="clearfix">
        <label for="abbrev">Abbreviation</label>
        <div class="input">
          <g:textField name="abbrev" class="xlarge" value="${moduleAnnotationInstance.abbreviation}"/>
        </div>
      </div>
      <div class="clearfix">
        <div class="input">
          <g:checkBox name="applyToAll" checked="false" value="apply"/> Apply to all module generation annotations of the same annotation name
        </div>
      </div>
    </fieldset>
    <div class="actions">
      <button class="btn primary" type="submit">Update</button>
    </div>
  </g:form>
</div>

</body>
</html>