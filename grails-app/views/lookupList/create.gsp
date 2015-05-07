<html>
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
  <meta name="layout" content="sampleSetMain"/>

  <title>Lookup List :: Create</title>

</head>

<body>
<div class="sampleset-container">
  <div class="page-header">
    <h2>Create Lookup List</h2>
  </div>
    <g:form class="form-stacked">
        <g:hasErrors bean="${lookupListInstance}">
          <g:eachError bean="${lookupListInstance}">
            <div class="error-message">
              <g:message error="${it}"/><br/>
            </div>
          </g:eachError>
        </g:hasErrors>
        <fieldset>
          <div class="clearfix">
            <label for="name"><h4>Name</h4></label>
            <div class="input">
          <g:textField name="name" value="${lookupListInstance?.name}" class="xxlarge"/>
              </div>
          </div>


        <div class="clearfix">
          <label for="name"><h4>Description</h4></label>
          <div class="input">
          <g:textArea name="description" value="${lookupListInstance?.description}" rows="10" class="xxlarge"/>
            </div>
        </div>
      </fieldset>

        <div class="actions">
          <button class="btn primary" type="submit" name="_action_save">Save</button>
          <button class="btn" type="submit" name="_action_list">Cancel</button>
        </div>

    </g:form>
  </div>
</body>
</html>