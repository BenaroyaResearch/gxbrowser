<%@ page import="org.sagres.project.Task" %>
<!doctype html>
<html>
	<head>
		<meta name="layout" content="projectMain">
		<g:set var="entityName" value="${message(code: 'task.label', default: 'Task')}" />
		<title><g:message code="default.show.label" args="[entityName]" /></title>
				
		<style type="text/css">
			span.property-label { display: inline-block; width: 100px; }
			table td {
				line-height: 0px;
				border-bottom: none;
				padding: 2px 2px 2px;
			}
			li p{width: 800px; margin-left: 100px}
		</style>
		
	</head>
	<body>
		<div id="show-task" class="content scaffold-show" role="main">
			<h1><g:message code="default.show.label" args="[entityName]" /></h1>
			<g:if test="${flash.message}">
			<div class="message" role="status">${flash.message}</div>
			</g:if>
			<ul class="property-list task">
			
				<g:if test="${taskInstance?.name}">
				<li class="fieldcontain">
					<span id="name-label" class="property-label"><g:message code="task.name.label" default="Name" /></span>
					
						<span class="property-value" aria-labelledby="name-label"><g:fieldValue bean="${taskInstance}" field="name"/></span>
					
				</li>
				</g:if>
			
				<g:if test="${taskInstance?.project}">
				<li class="fieldcontain">
					<span id="project-label" class="property-label"><g:message code="task.project.label" default="Project" /></span>
					
						<span class="property-value" aria-labelledby="project-label"><g:link controller="project" action="show" id="${taskInstance?.project?.id}">${taskInstance?.project?.encodeAsHTML()}</g:link></span>
					
				</li>
				</g:if>
			
			
				<g:if test="${taskInstance?.type}">
				<li class="fieldcontain">
					<span id="type-label" class="property-label"><g:message code="task.type.label" default="Type" /></span>
					
						<span class="property-value" aria-labelledby="type-label"><g:link controller="lookupList" action="show" id="${taskInstance?.type?.lookupList.id}">${taskInstance?.type?.encodeAsHTML()}</g:link></span>
					
				</li>
				</g:if>
			
				<g:if test="${taskInstance?.resource}">
				<li class="fieldcontain">
					<span id="resource-label" class="property-label"><g:message code="task.resource.label" default="Resource" /></span>
					
						<span class="property-value" aria-labelledby="resource-label"><g:link controller="lookupList" action="show" id="${taskInstance?.resource?.lookupList.id}">${taskInstance?.resource?.encodeAsHTML()}</g:link></span>
					
				</li>
				</g:if>
			
				<g:if test="${taskInstance?.beginDate}">
				<li class="fieldcontain">
					<span id="beginDate-label" class="property-label"><g:message code="task.beginDate.label" default="Begin Date" /></span>
					
						<span class="property-value" aria-labelledby="beginDate-label"><g:formatDate format="yyyy-MM-dd" date="${taskInstance?.beginDate}" /></span>
					
				</li>
				</g:if>
			
				<g:if test="${taskInstance?.endDate}">
				<li class="fieldcontain">
					<span id="endDate-label" class="property-label"><g:message code="task.endDate.label" default="End Date" /></span>
					
						<span class="property-value" aria-labelledby="endDate-label"><g:formatDate format="yyyy-MM-dd" date="${taskInstance?.endDate}" /></span>
					
				</li>
				</g:if>
			
				<g:if test="${taskInstance?.comments}">
				<li class="fieldcontain">
					<span id="comments-label" class="property-label"><g:message code="task.comments.label" default="Comments" /></span>
					
						<span class="property-value" aria-labelledby="comments-label"><g:fieldValue bean="${taskInstance}" field="comments"/></span>
					
				</li>
				</g:if>
			
			</ul>
			<g:form>
				<fieldset class="buttons">
					<g:hiddenField name="id" value="${taskInstance?.id}" />
					<g:link class="edit" action="edit" id="${taskInstance?.id}"><g:message code="default.button.edit.label" default="Edit" /></g:link>
					<g:actionSubmit class="delete" action="delete" value="${message(code: 'default.button.delete.label', default: 'Delete')}" onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" />
				</fieldset>
			</g:form>
		</div>
	</body>
</html>
