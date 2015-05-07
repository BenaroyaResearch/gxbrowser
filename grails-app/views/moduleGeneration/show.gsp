<%@ page import="org.sagres.mat.ModuleGeneration" %>
<html>
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
  <meta name="layout" content="matmain">
  <link rel="stylesheet" href="${resource(dir: 'css', file: 'sampleset-main.css')}"/>
  <title>Module Generation Version: ${moduleGeneration.versionName}</title>
</head>

<body>
<div class="mat-container">
  <h2>${moduleGeneration.versionName}</h2>
  <h4>Generation ${moduleGeneration.generation}, ${chipType.name}</h4>
  <g:if test="${flash.message}">
    <div class="message">${flash.message}</div>
  </g:if>
  <g:if test="${modules.isEmpty()}">
    <p style="margin: 10px 0;">No modules founds for this module generation version.</p>
  </g:if>
  <g:else>
    <table class="zebra-striped">
      <thead><tr><th>Module</th><th>Annotation</th><th>Color</th><th># Probes</th></tr></thead>
      <tbody>
      <g:each in="${modules}" var="m">
        <tr>
          <td><g:link controller="module" action="show" id="${m.id}">${m.name}</g:link></td>
          <td>
            <g:if test="${m.annotation_name}">
              <g:link controller="module" action="editAnnotation" id="${m.id}">${m.annotation_name}</g:link>
            </g:if>
            <g:else>
              <g:link controller="moduleAnnotation" action="create" params="[moduleName:m.name]">Create</g:link>
            </g:else>
            OR
            <g:link controller="module" action="setAnnotation" id="${m.id}">Set</g:link>
          </td>
          <td>
            <g:if test="${m.color}">
            <span style="background-color:${m.color};padding-left:20px;margin-right:5px;"></span>
            ${m.color.toUpperCase()}
            </g:if>
          </td>
          <td>${m.probes}</td>
        </tr>
      </g:each>
      </tbody>
    </table>
  </g:else>

</div>
</body>
</html>
