<%@ page import="org.sagres.mat.ModuleGeneration" %>
<html>
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
  <meta name="layout" content="matmain">
  <link rel="stylesheet" href="${resource(dir: 'css', file: 'sampleset-main.css')}"/>
  <title>Module Annotation: Create</title>
</head>

<body>
<div class="mat-container">
  <h2>Create Module Annotation</h2>
  <g:form action="save">
    <fieldset>
      <div class="clearfix">
        <label for="generation">Generation</label>
        <div class="input">
          <g:select from="${generations}" name="generation" onchange="populateModules();"/>
        </div>
      </div>
      <div class="clearfix">
        <label for="module">Module</label>
        <div class="input">
          <g:select name="module" from="${modules}" value="${moduleName}"/>
        </div>
      </div>
      <div class="clearfix">
        <label for="name">Annotation</label>
        <div class="input">
          <g:textField name="name" class="xlarge"/>
        </div>
      </div>
      <div class="clearfix">
        <label for="color">Hex Color</label>
        <div class="input">
          <g:textField name="color" class="xlarge"/>
        </div>
      </div>
      <div class="clearfix">
        <label for="abbrev">Abbreviation</label>
        <div class="input">
          <g:textField name="abbrev" class="xlarge"/>
        </div>
      </div>
    </fieldset>

    <div class="actions">
      <button class="btn primary" type="submit">Save</button>
    </div>
  </g:form>

</div>
<g:javascript>
  var populateModules = function() {
    var id = $("select#generation").val();
    $.getJSON(getBase()+"/moduleGeneration/getModulesForGeneration", { id:id }, function(json) {
      if (json.modules) {
        var options = "";
        $.each(json.modules, function(i,m) {
          options += '<option value="'+m+'">'+m+'</option>';
        });
        $("select#module").html(options);
      }
    });
  };
</g:javascript>

</body>
</html>
