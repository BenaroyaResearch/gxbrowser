<%@ page import="org.sagres.project.Task" %>
<!doctype html>
<html>
	<head>
		<meta name="layout" content="projectMain">
		<g:set var="entityName" value="${message(code: 'task.label', default: 'Task')}" />
		<title><g:message code="default.list.label" args="[entityName]" /></title>
	</head>
	<body>
		<div id="list-task" class="content scaffold-list" role="main">
			<h1><g:message code="default.list.label" args="[entityName]" /></h1>
			<g:if test="${flash.message}">
			<div class="message" role="status">${flash.message}</div>
			</g:if>
			<table>
				<thead>
					<tr>
						<g:sortableColumn property="name" title="${message(code: 'task.name.label', default: 'Name')}" />					
						<th><g:message code="task.type.label" default="Type" /></th>
						<g:sortableColumn property="project" title="${message(code: 'task.project.label', default: 'Project')}" />
						<th><g:message code="task.resource.label" default="Resource" /></th>
						<g:sortableColumn property="beginDate" title="${message(code: 'task.beginDate.label', default: 'Begin Date')}" />
						<g:sortableColumn property="endDate" title="${message(code: 'task.endDate.label', default: 'End Date')}" />
						<g:sortableColumn property="comments" title="${message(code: 'task.comments.label', default: 'Comments')}" />
					</tr>
				</thead>
				<tbody>
					<g:each in="${taskInstanceList}" status="i" var="taskInstance">
						<tr class="${(i % 2) == 0 ? 'even' : 'odd'}">
							<td>${fieldValue(bean: taskInstance, field: "name")}</td>
							<td><g:link action="show" id="${taskInstance.id}">${fieldValue(bean: taskInstance, field: "type")}</g:link></td>
							<td><g:link controller="project" action="show" id="${taskInstance.project.id}">${fieldValue(bean: taskInstance, field: "project")}</g:link></td>
							<td>${fieldValue(bean: taskInstance, field: "resource")}</td>
							<td><g:formatDate format='yyyy-MM-dd' date="${taskInstance.beginDate}" /></td>
							<td><g:formatDate format='yyy-MM-dd' date="${taskInstance.endDate}" /></td>
							<td>${fieldValue(bean: taskInstance, field: "comments")}</td>
						</tr>
					</g:each>
				</tbody>
			</table>
			<div class="pagination">
				<g:paginate total="${taskInstanceTotal}" />
			</div>
		</div>
	</body>
</html>
