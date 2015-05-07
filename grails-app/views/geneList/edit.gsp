<%@ page import="org.sagres.geneList.GeneList; org.sagres.geneList.GeneListCategory" %>
<html>
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
  <meta name="layout" content="main"/>
  <g:set var="entityName" value="${message(code: 'geneList.label', default: 'Gene List')}"/>
  <title><g:message code="default.edit.label" args="[entityName]"/></title>
</head>

<body>
<div class="topbar">
	<div class="topbar-inner itn-fill">
	%{--<div class="topbar-inner bri-fill">--}%
    <div class="sampleset-container">
			<!--<img src="../images/itn_logo_2.png" style="margin-top: 7px; margin-right: 5px; float: left;"/>    -->
      <h3 style="display: inline"><g:link controller="geneBrowser" action="list"><strong>GXB</strong></g:link></h3>
      <ul class="nav secondary-nav">
        <li><g:link controller="sampleSet" action="list" target="_blank">Annotation Tool</g:link></li>
      </ul>
    </div>
  </div><!-- /fill -->
</div><!-- /topbar -->

   <ul class="nav secondary-nav pills">
    <li><g:link class="list" action="list"><g:message code="default.list.label" args="[entityName]"/></g:link></li>
    <sec:ifAnyGranted roles="ROLE_USER">
    	<li><g:link class="create" action="create"><g:message code="default.new.label" args="[entityName]"/></g:link></li>
    </sec:ifAnyGranted>
  </ul>


<div class="body">
  <h2><g:message code="default.edit.label" args="[entityName]"/></h2>
  <g:if test="${flash.message}">
    <div class="message">${flash.message}</div>
  </g:if>
  <g:hasErrors bean="${geneListInstance}">
    <div class="errors">
      <g:renderErrors bean="${geneListInstance}" as="list"/>
    </div>
  </g:hasErrors>
  <g:form method="post">
    <g:hiddenField name="id" value="${geneListInstance?.id}"/>
    <g:hiddenField name="version" value="${geneListInstance?.version}"/>
    <div class="dialog">
      <table>
        <tbody>

        <tr class="prop">
          <td valign="top" class="name">
            <label for="name"><g:message code="geneList.name.label" default="Name"/></label>
          </td>
          <td valign="top" class="value ${hasErrors(bean: geneListInstance, field: 'name', 'errors')}">
            <g:textField name="name" value="${geneListInstance?.name}"/>
          </td>
        </tr>
       <tr class="prop">
          <g:set var="coption" value="${geneListCategory?.id ?: geneListInstance?.geneListCategory?.id}" />
          <td valign="top" class="category">
            <label for="name"><g:message code="geneListCategory.name.label" default="Category"/></label>
          </td>
          <td valign="top" class="value ${hasErrors(bean: geneListInstance, field: 'geneListCategory', 'errors')}">
            <g:if test="${editable == false}">
      			<g:set var="coption" value="${GeneListCategory.findByName('User Defined').id}"/>
   				<input type="hidden" name="geneListCategory.id" value="${coption}">
      		</g:if>	
       		<g:select name="GeneListCategory.id" from="${GeneListCategory.list()}"
      			optionKey="id" optionValue="name" value="${coption}" disabled="${!editable}"
      			noSelection="['':'-Choose a Category-']"/>
          </td>
        </tr>
        <tr class="prop">
          <td valign="top" class="name">
            <label for="description"><g:message code="geneList.description.label" default="Description"/></label>
          </td>
          <td valign="top" class="value ${hasErrors(bean: geneListInstance, field: 'description', 'errors')}">
            <g:textField name="description" value="${geneListInstance?.description}"/>
          </td>
        </tr>

        </tbody>
      </table>
    </div>

    <div class="buttons">
      <span class="button"><g:actionSubmit class="save" action="update"
                                           value="${message(code: 'default.button.update.label', default: 'Update')}"/></span>
      <span class="button"><g:actionSubmit class="delete" action="delete"
                                           value="${message(code: 'default.button.delete.label', default: 'Delete')}"
                                           onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');"/></span>
    </div>
  </g:form>
</div>
</body>
</html>
