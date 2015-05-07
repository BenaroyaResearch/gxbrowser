<%@ page import="org.sagres.project.Project" %>
<!doctype html>
<html>
	<head>
		<meta name="layout" content="projectMain">
		<g:set var="entityName" value="${message(code: 'project.label', default: 'Project')}" />
		<title><g:message code="default.edit.label" args="[entityName]" /></title>
		<g:javascript src="jquery.simple-color.js"/>
		
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
		<g:javascript>
		  	var closeModal = function(modalId) {
    			$("#"+modalId).modal("hide");
  			};
		
			var taskShift = function() {
				var args = {
					type:		$("#shiftType").val(),
				  	pid:		$("#shiftProject").val(),
  					tid:		$("#shiftTask").val(),
 					amount:		$("#shiftAmount").val(),
 					units:		$("#shiftUnits").val(),
 					direction:	$("#shiftDirect").val(),
 					adjust:		$("#shiftEnds").val()
 				};
	
				$.getJSON(getBase()+"/project/taskShift", args, function(json) {
					window.location.reload(true);
				});
			};
			
			$(document).ready(function() {
			
			   $('.simple_color').simpleColor({
								    cellWidth: 20,
    								cellHeight: 20,
    								border: '1px solid #660033',
								    displayColorCode: true,
    								livePreview: true});
			    
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
		<div id="edit-project" class="content scaffold-edit" role="main">
			<h1>Edit: <g:fieldValue bean="${projectInstance}" field="name"/> - <g:fieldValue bean="${projectInstance}" field="title"/> </h1>
			<g:if test="${flash.message}">
			<div class="message" role="status">${flash.message}</div>
			</g:if>
			<g:hasErrors bean="${projectInstance}">
			<ul class="errors" role="alert">
				<g:eachError bean="${projectInstance}" var="error">
				<li <g:if test="${error in org.springframework.validation.FieldError}">data-field-id="${error.field}"</g:if>><g:message error="${error}"/></li>
				</g:eachError>
			</ul>
			</g:hasErrors>
			<g:if test="${!projectInstance.locked || projectInstance.owner.id == currentUser?.id}">
			<g:form method="post" >
				<g:hiddenField name="id" value="${projectInstance?.id}" />
				<g:hiddenField name="version" value="${projectInstance?.version}" />
				<fieldset class="form">
					<g:render template="form"/>
				</fieldset>
				<fieldset class="buttons">
					<g:actionSubmit class="button save" action="update" value="${message(code: 'default.button.update.label', default: 'Update')}" />
					<g:link class="button" action="show" id="${projectInstance.id}">${message(code: 'default.button.show.label', default: 'Cancel')}</g:link>
					<g:actionSubmit class="button delete" action="delete" value="${message(code: 'default.button.delete.label', default: 'Delete')}" formnovalidate="" onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" />
				</fieldset>
			</g:form>
			</g:if>
		</div>
	</body>
</html>
