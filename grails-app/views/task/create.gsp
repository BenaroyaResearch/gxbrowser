<%@ page import="org.sagres.project.Task" %>
<!doctype html>
<html>
	<head>
		<meta name="layout" content="projectMain">
		<g:set var="entityName" value="${message(code: 'task.label', default: 'Task')}" />
		<title><g:message code="default.create.label" args="[entityName]" /></title>
		<style type="text/css">
			.property-label { display: inline-block; text-align: left; width: 100px; }
			.fieldcontain { margin: 2px 2px 2px 2px; }
			
			table td.checklist {
				border-bottom: none;
				padding: 2px 2px 2px;
			}
			
		</style>
		<g:javascript>
			$(document).ready(function() {
			      $(".ui-icon-date").click(function() {
        				var textField = $(this).closest(".input-append").find("input:text");
        				if (!textField.is(":disabled"))
       					{
        					textField.datepicker("show");
        					textField.datepicker("widget").position({
	          					my: "left top",
          						at: "left bottom",
          						of: textField
        					});
      					}
     			});
				$('.datepicker').datepicker({
      				changeMonth: true,
      				changeYear: true,
      				dateFormat: 'yy-mm-dd'
     			}).focus(function() {
     	 			$('.datepicker').datepicker("widget").position({
	        			my: "left top",
        				at: "left bottom",
        				of: this
      				});
     			});
     		});
     	</g:javascript>
		
	</head>
	<body>
		<div id="create-task" class="content scaffold-create" role="main">
			<h1><g:message code="default.create.label" args="[entityName]" /></h1>
			<g:if test="${flash.message}">
			<div class="message" role="status">${flash.message}</div>
			</g:if>
			<g:hasErrors bean="${taskInstance}">
			<ul class="errors" role="alert">
				<g:eachError bean="${taskInstance}" var="error">
				<li <g:if test="${error in org.springframework.validation.FieldError}">data-field-id="${error.field}"</g:if>><g:message error="${error}"/></li>
				</g:eachError>
			</ul>
			</g:hasErrors>
			<g:form action="save" >
				<fieldset class="form">
					<g:render template="form"/>
				</fieldset>
				<fieldset class="buttons">
					<g:submitButton name="create" class="save" value="${message(code: 'default.button.create.label', default: 'Create')}" />
				</fieldset>
			</g:form>
		</div>
	</body>
</html>
