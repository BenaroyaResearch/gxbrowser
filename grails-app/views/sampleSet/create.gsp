<%@ page import="org.sagres.sampleSet.SampleSet" %>
<html>
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
  <meta name="layout" content="sampleSetMain"/>
  <title>Sample Set :: Create</title>
</head>

<body>
<div class="sampleset-container">
  <div class="page-header">
    <h2>Create Sample Set</h2>
  </div>
    <g:form class="form-stacked">
        <g:hasErrors bean="${sampleSet}">
          <g:eachError bean="${sampleSet}">
            <div class="error-message">
              <g:message error="${it}"/><br/>
            </div>
          </g:eachError>
        </g:hasErrors>
      <fieldset>
        <div class="clearfix">
          <label for="name"><h4>Title</h4></label>
          <div class="input">
            <g:textField name="name" value="${sampleSet?.name}" class="xxlarge"/>
            </div>
      </div>
        <div class="clearfix">
          <label for="description"><h4>Description</h4></label>
        <div class="input">
          <g:textArea name="description" value="${sampleSet?.description}" rows="10" class="xxlarge"/>
          </div>
          </div>
      </fieldset>

        <div class="actions">
        <button class="btn primary" type="submit" name="_action_save">Create Sample Set</button>
        <button class="btn" type="submit" name="_action_cancel">Cancel</button>
        </div>

    </g:form>
</div>

</body>
</html>
