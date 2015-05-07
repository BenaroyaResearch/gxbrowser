<%@ page import="org.sagres.mat.ModuleGeneration" %>
<html>
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
  <meta name="layout" content="matmain">
  <link rel="stylesheet" href="${resource(dir: 'css', file: 'sampleset-main.css')}"/>
  <title>Module Generation Versions</title>
</head>

<body>
<div class="mat-container">
  <g:link class="btn primary large" action="create">New Module Generation Version</g:link>
  <g:link class="btn large" action="upload">Upload Annotation Set</g:link>
  <br/><br/>
  <h2>Module Generation Versions</h2>
  <g:if test="${flash.message}">
    <div class="message">${flash.message}</div>
  </g:if>
  %{--<div class="list">--}%
    <g:if test="${moduleGenerations.isEmpty()}">
      <p style="margin: 10px 0;">No module generation versions found.</p>
    </g:if>
    <g:else>
      <table class="zebra-striped">
        <thead>
        <tr>
          <th style="width:25px;"></th>
          <th>Version Name</th>
          <th>Generation</th>
          <th>Chip Type</th>
          <th># Modules</th>
          <th># Probes</th>
        </tr>
        </thead>
        <tbody>
          <g:each in="${moduleGenerations}" status="i" var="mg">
            <tr>
              <td style="text-align:center;"><g:link action="delete" id="${mg.id}"><span class="ui-icon-delete"></span></g:link></td>
              <td><g:link controller="moduleGeneration" action="show" id="${mg.id}">${mg.name}</g:link></td>
              <td>${mg.gen}</td>
              <td>${mg.chiptype}</td>
              <td>${mg.modules}</td>
              <td>${mg.probes}</td>
            </tr>
          </g:each>
        </tbody>
      </table>
    </g:else>
  %{--</div>--}%

</div>
</body>
</html>
