<%@ page import="org.sagres.mat.ModuleGeneration" %>
<html>
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
  <meta name="layout" content="matmain">
  <link rel="stylesheet" href="${resource(dir: 'css', file: 'sampleset-main.css')}"/>
  <title>Module: ${module.moduleName}</title>
</head>

<body>
<div class="mat-container">
  <h2>
    ${module.moduleName}
  </h2>
  <g:if test="${annotation}">
  <h3>
      <g:link action="editAnnotation" id="${module.id}">${annotation.annotation}</g:link>
       (${annotation.abbreviation}) :
      <span style="background-color:${annotation.hexColor};padding-left:40px;margin-right:5px;"></span>
            ${annotation.hexColor.toUpperCase()}

  </h3>
    </g:if>
  <h4>Generation ${generation.generation}, ${chip.name}, <g:if test="${probes}">${probes.size()} probes</g:if></h4>
  <g:if test="${flash.message}">
    <div class="message">${flash.message}</div>
  </g:if>
  <g:if test="${probes.empty}">
    <p style="margin: 10px 0;">No probes founds for this module.</p>
  </g:if>
  <g:else>
    <table class="zebra-striped">
      <thead><tr><th>Probe</th><th>Gene Symbol</th></thead>
      <tbody>
      <g:each in="${probes}" var="p">
        <tr>
          <td>${p.probeId}</td>
          <td>${p.geneSymbol}</td>
        </tr>
      </g:each>
      </tbody>
    </table>
  </g:else>

</div>
</body>
</html>
