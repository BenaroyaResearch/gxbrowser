<html>
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
  <meta name="layout" content="matmain">
  <link rel="stylesheet" href="${resource(dir: 'css', file: 'sampleset-main.css')}"/>
  <title>Upload Annotations</title>
</head>

<body>
<div class="mat-container">
  <h2>New Annotation Set</h2>
  <g:form enctype="multipart/form-data" action="updateAnnotations">
    <fieldset>
      <div class="clearfix">
        <label for="generation">Generation</label>
        <div class="input">
          <g:select name="generation" from="${generations}"/>
        </div>
      </div>
      <div class="clearfix">
        <label for="annotationFile">Annotation Set File</label>
        <div class="input">
          <input type="file" name="annotationFile" id="annotationFile"/>
        </div>
      </div>
    </fieldset>

    <div class="actions">
      <button class="btn primary" type="submit">Update Annotation Set</button>
    </div>
  </g:form>
</div>
</body>
</html>