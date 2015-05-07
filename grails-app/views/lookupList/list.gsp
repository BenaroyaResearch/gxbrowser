<html>
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
  <meta name="layout" content="sampleSetMain"/>

  <title>Lookup Lists</title>

  <g:javascript src="jquery.tablesorter.min.js"/>
  <g:javascript>
    $(document).ready(function() {
      $("table#lookupLists").tablesorter({ sortList: [[1,0]] });
    });
  </g:javascript>

</head>

<body>
<div class="sampleset-container">
  <div class="page-header">
    <h2>Lookup Lists</h2>
  </div>
  <table id="lookupLists" class="zebra-striped">
      <thead>
      <tr>
        <th>Name</th>
        <th>Description</th>
        <th>Details</th>
      </tr>
      </thead>
      <tbody>
      <g:each in="${lookupListInstanceList}" status="i" var="lookupListInstance">
        <tr>
          <td width="150px"><g:link controller="lookupList" action="show"
                      id="${lookupListInstance.id}">${fieldValue(bean: lookupListInstance, field: "name")}</g:link></td>
          <td>${abbreviate(maxLength: '150', value: fieldValue(bean: lookupListInstance, field: "description"), hint: "--")}</td>
          <td width="250px">${fieldValue(bean: lookupListInstance, field: "lookupDetails")}</td>
        </tr>
      </g:each>
      </tbody>
    </table>
  </div>
</body>
</html>