<%@ page import="org.sagres.sampleSet.component.OverviewComponent; org.sagres.sampleSet.SampleSet" %>
<html>
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
  <meta name="layout" content="sampleSetMain"/>
  <title>Admin :: Annotation Components</title>
</head>

<body>
<div class="sampleset-container">
  <div class="page-header">
    <h2>Create Overview Component</h2>
  </div>
  <g:form class="form" action="createOverviewComponent">
    <fieldset>
      <div class="clearfix">
        <label for="name">Name</label>
        <div class="input">
          <g:textField name="name" class="large"/>
        </div>
      </div>
      <div class="clearfix">
        <label for="tooltip">Tooltip</label>
        <div class="input">
          <g:textField name="tooltip" class="large"/>
        </div>
      </div>
      <div class="clearfix">
        <label for="componentType">Component Type</label>
        <div class="input">
          <g:select name="componentType" from="['textarea','text']"/>
        </div>
      </div>
      <div class="clearfix">
        <label for="annotationName">Annotation Name</label>
        <div class="input">
          <g:textField name="annotationName" class="large"/>
        </div>
      </div>
      <div class="clearfix">
        <label for="password">Password</label>
        <div class="input">
          <input type="password" name="password" id="password" class="large"/>
        </div>
      </div>
      <div class="clearfix">
        <div class="input">
          <button class="btn primary" type="submit">Create</button>
        </div>
      </div>
    </fieldset>
  </g:form>

  <div class="page-header">
    <h2>Default Sets</h2>
  </div>
  <g:each in="${defaultSets.keySet()}" var="defaultSet">
    <h4>Default Set #${defaultSet}</h4>
    <ul>
    <g:each in="${defaultSets.get(defaultSet)}" var="component">
      <li>${OverviewComponent.get(component.componentId).name}</li>
    </g:each>
    </ul>
    <g:form class="form" action="updateSampleSets">
      <fieldset>
        <legend>Update Sample Sets</legend>
        <div class="clearfix">
          <label for="updatePassword">Password</label>
          <div class="input">
            <input type="password" name="updatePassword" id="updatePassword" class="large"/>
          </div>
        </div>
        <div class="clearfix">
          <div class="input">
            <button class="btn primary" type="submit">Update</button>
          </div>
        </div>
      </fieldset>
    </g:form>
    <g:form class="form" action="addComponentToDefaultSet" id="${defaultSet}">
      <fieldset>
        <legend>Add Overview Component</legend>
        <div class="clearfix">
          <label for="component">Component</label>
          <div class="input">
            <g:select from="${overviewComponents}" name="component" optionKey="id" optionValue="name"/>
          </div>
        </div>
        <div class="clearfix">
          <label for="addPassword">Password</label>
          <div class="input">
            <input type="password" name="addPassword" id="addPassword" class="large"/>
          </div>
        </div>
        <div class="clearfix">
          <div class="input">
            <button class="btn primary" type="submit">Add Component</button>
          </div>
        </div>
      </fieldset>
  </g:form>
  </g:each>
</div>
</body>
</html>
