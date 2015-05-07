<%@ page import="org.sagres.mat.ModuleAnnotation" %>
<html>
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
  <meta name="layout" content="matmain">
  <link rel="stylesheet" href="${resource(dir: 'css', file: 'sampleset-main.css')}"/>
  <title>Module Annotations</title>
</head>

<body>
<div class="mat-container">
  <div class="page-header"><h2>Annotations</h2></div>
  <g:form action="list">
    Generation: <g:select name="generation" from="${generations}"/> <button class="btn primary" type="submit">Update</button>
  </g:form>
  <table id="moduleannotation" class="zebra-striped pretty-table">
    <thead>
    <tr>
      <th>Module Name</th>
      <th>Annotation</th>
      <th>Hex Color</th>
      <th>Abbreviation</th>
    </tr>
    </thead>
    <tbody>
    <g:each in="${annotations}" status="i" var="annot">
      <tr>
        <td>${annot.moduleName}</td>
        <td><g:link action="show" id="${annot.id}">${annot.annotation}</g:link></td>
        <td>${annot.hexColor}</td>
        <td>${annot.abbreviation}</td>
      </tr>
    </g:each>
    </tbody>
  </table>
</div>

</body>
</html>