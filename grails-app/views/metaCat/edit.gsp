<%@ page import="org.sagres.mat.MetaCat" %>
<!doctype html>
<html>
	<head>
		<meta name="layout" content="metacatMain">
		<g:set var="entityName" value="${message(code: 'metaCat.label', default: 'MetaCat Collection')}" />
		<title><g:message code="default.edit.label" args="[entityName]" /></title>
		<style type="text/css">
			.property-label { display: inline-block; text-align: left; width: 100px; }
			.fieldcontain { margin: 2px 2px 2px 2px; }
			
			table td.checklist {
				border-bottom: none;
				padding: 2px 2px 2px;
			}
			
			.simpleColorDisplay {
    			font-family: Helvetica;
   				 margin: 10px 100px;
  			}
  			.simpleColorChooser {
   				 background-color: #fff;
  			}
			
		</style>
		
	</head>
	<body>
		<div id="edit-metaCat" class="content scaffold-edit" role="main">
			<h1><g:message code="default.edit.label" args="[entityName]" /></h1>
			<g:if test="${flash.message}">
			<div class="message" role="status">${flash.message}</div>
			</g:if>
			<g:hasErrors bean="${metaCatInstance}">
			<ul class="errors" role="alert">
				<g:eachError bean="${metaCatInstance}" var="error">
				<li <g:if test="${error in org.springframework.validation.FieldError}">data-field-id="${error.field}"</g:if>><g:message error="${error}"/></li>
				</g:eachError>
			</ul>
			</g:hasErrors>
			<g:form method="post" >
				<g:hiddenField name="id" value="${metaCatInstance?.id}" />
				<g:hiddenField name="version" value="${metaCatInstance?.version}" />
				<fieldset class="form">
					<g:render template="form"/>
				</fieldset>
				<fieldset class="buttons">
					<g:actionSubmit class="save" action="update" value="${message(code: 'default.button.update.label', default: 'Update')}" />
					<g:actionSubmit class="delete" action="delete" value="${message(code: 'default.button.delete.label', default: 'Delete')}" formnovalidate="" onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" />
				</fieldset>
			</g:form>
		</div>
	</body>
</html>
