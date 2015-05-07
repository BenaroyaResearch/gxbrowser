<%@ page import="org.sagres.project.Project" %>
<!doctype html>
<html>
	<head>
		<meta name="layout" content="projectMain">
		<g:set var="entityName" value="${message(code: 'project.label', default: 'TG2 Import')}" />
		<title><g:message code="default.create.label" args="[entityName]" /></title>
		<style type="text/css">
			.property-label { display: inline-block; text-align: left; width: 100px; }
			

			table td.checklist {
				line-height: 0px;
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
			<div id="import-project" class="content scaffold-create" role="main">
			<h1><g:message code="default.create.label" args="[entityName]" /></h1>
			<g:if test="${flash.message}">
				${flash.message}
			</g:if>
			<g:hasErrors bean="${projectInstance}">
			<ul class="errors" role="alert">
				<g:eachError bean="${projectInstance}" var="error">
				<li <g:if test="${error in org.springframework.validation.FieldError}">data-field-id="${error.field}"</g:if>><g:message error="${error}"/></li>
				</g:eachError>
			</ul>
			</g:hasErrors>
			<g:form action="tgImportSave" >
				<fieldset class="form">
						<label class="property-label" for="tg2Reference">
							<g:message code="project.tg2Reference.label" default="Tg2 Project" />:
						</label>
						<g:select name="tg2id" noSelection="${['null':'Select One...']}" from="${tg2ProjectList}" optionValue="title" optionKey="id" required=""/>
				</fieldset>
				<fieldset class="buttons">
					<g:submitButton name="import" class="button save" value="${message(code: 'default.button.import.label', default: 'Import')}" />
					<g:link class="button" action="list">${message(code: 'default.button.cancel.label', default: 'Cancel')}</g:link>
				</fieldset>
			</g:form>
		</div>
	</body>
</html>
