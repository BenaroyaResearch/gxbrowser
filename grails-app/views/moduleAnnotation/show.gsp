<%@ page import="org.sagres.mat.ModuleGeneration" %>
<html>
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
  <meta name="layout" content="matmain">
  <link rel="stylesheet" href="${resource(dir: 'css', file: 'sampleset-main.css')}"/>
  <title>Module Annotation</title>
</head>

<body>
<div class="mat-container">
  <h2>${moduleAnnotationInstance.annotation}</h2>
  <div class="well" style="width:400px;margin-top:5px;">
    <h3>Abbreviation</h3>
    <p>${moduleAnnotationInstance.abbreviation ?: "None"}</p>
    <h3>Color</h3>
    <p>
      <span style="background-color:${moduleAnnotationInstance.hexColor};padding-left:20px;margin-right:5px;"></span>
            ${moduleAnnotationInstance.hexColor.toUpperCase()}
    </p>
    <h3>Module</h3>
    <p>${moduleAnnotationInstance.moduleName}</p>
    <hr/>
    <p>This is a <b>Generation ${moduleAnnotationInstance.generation}</b> module annotation</p>
  </div>
  <g:link class="btn primary" controller="moduleAnnotation" action="edit" id="${params.id}">Edit</g:link>
  %{--<g:form action="delete" style="display:inline-block;" id="${params.id}">--}%
    %{--<button class="btn danger" type="submit">Delete</button> <small>This is going to delete the annotation for ALL modules</small>--}%
  %{--</g:form>--}%
</div>

</body>
</html>
