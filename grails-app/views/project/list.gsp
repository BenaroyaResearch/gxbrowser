<%@ page import="org.sagres.project.Project" %>
<!doctype html>
<html>
	<head>
		<meta name="layout" content="projectMain">
		<g:set var="entityName" value="${message(code: 'project.label', default: 'Project')}" />
		<title><g:message code="default.list.label" args="[entityName]" /></title>
	</head>
	<body>
<%--  		TODO: Add jquery tablesort, and functions.--%>
<%--    	TODO: Add delete button--%>
<%--    	TODO: Add live edit on the main screen for status?, link to sample set--%>
<%--    	TODO: Add slidein modal to connect to sample set.--%>
		<div class="body">
			<h1><g:message code="default.list.label" args="[entityName]" /></h1>
			<g:if test="${flash.message}">
			<div class="message" role="status">${flash.message}</div>
			</g:if>
			<g:if test="${!projectInstanceList.isEmpty()}">
			<div class="list">
			<table>
				<thead>
					<tr>
						<g:sortableColumn property="name" title="${message(code: 'project.name.label', default: 'Name')}" />
						<g:sortableColumn property="title" title="${message(code: 'project.title.label', default: 'Title')}" />
						<g:sortableColumn property="title" title="${message(code: 'project.status.label', default: 'Status')}" />
<%--						<g:sortableColumn property="owner" title="${message(code: 'project.owner.label', default: 'Owner')}" />--%>
						<g:sortableColumn property="analyst" title="${message(code: 'project.analyst.label', default: 'Analyst')}" />
						<g:sortableColumn property="client" title="${message(code: 'project.client.label', default: 'Client; Contact')}" />
						<g:sortableColumn property="samples" title="${message(code: 'project.samples.label', default: 'Sample Count')}" />
						<g:sortableColumn property="contactDate" title="${message(code: 'project.contactDate.label', default: 'Begin Date')}" />
						<g:sortableColumn property="analysisDate" title="${message(code: 'project.analysisDate.label', default: 'Analysis Date')}" />
						<g:sortableColumn property="deliveryDate" title="${message(code: 'project.deliveryDate.label', default: 'Delivery Date')}" />
						<g:sortableColumn property="technology" title="${message(code: 'project.contact.label', default: 'Technology')}" />
						<g:sortableColumn style="width: 10%;" property="deliverables" title="${message(code: 'project.deliverables.label', default: 'Deliverables')}" />
						<th style="width: 2%;">Links</th>
						<g:sortableColumn style="width: 20%;" property="comments" title="${message(code: 'project.comments.label', default: 'Comments')}" />
					</tr>
				</thead>
				<tbody>
				<g:each in="${projectInstanceList}" status="i" var="projectInstance">
					<tr class="${(i % 2) == 0 ? 'even' : 'odd'}">
						<td><g:link action="show" id="${projectInstance.id}">${fieldValue(bean: projectInstance, field: "name")}</g:link></td>
						<td>${fieldValue(bean: projectInstance, field: "title")}
						</td>
						<td>${fieldValue(bean: projectInstance, field: "status")}</td>
<%--						<td><a href="mailto:${projectInstance?.owner?.email}?subject=${projectInstance?.title}">${fieldValue(bean: projectInstance, field: "owner")}</a></td>--%>
						<td><a href="mailto:${projectInstance?.analyst?.email}?subject=${projectInstance?.title}">${fieldValue(bean: projectInstance, field: "analyst")}</a></td>
						<td>${fieldValue(bean: projectInstance, field: "client")}
							<g:if test="${projectInstance.contact}">
								; ${fieldValue(bean: projectInstance, field: "contact")}
							</g:if>
							</td>
						<td>${fieldValue(bean: projectInstance, field: "sampleCount")}
						<td><g:formatDate format="yyyy-MM-dd" date="${projectInstance.contactDate}" /></td>
						<td><g:formatDate format="yyyy-MM-dd" date="${projectInstance.analysisDate}" /></td>
						<td><g:formatDate format="yyyy-MM-dd" date="${projectInstance.deliveryDate}" /></td>
						<td>${fieldValue(bean: projectInstance, field: "technology")}</td>
						<td>
							<div class="span2">
								<g:each in="${deliverableInstanceList}" status="d" var="deliverableInstance">
									<g:if test="${projectInstance?.deliverables.find{ s->s.id == deliverableInstance.id}}">
						      		<label class="checkbox" >
						            	<g:checkBox style="float: left;" name="deliverables_${deliverableInstance.id}" value="${deliverableInstance.id}" checked="${projectInstance?.deliverables.find{ s->s.id == deliverableInstance.id} }" disabled="true"/> <span class="text" style="float: left; margin-left: 5px;"  ><g:message code="${deliverableInstance?.name}.label" default="${deliverableInstance?.name}" /></span>
						           </label>
						           </g:if>
						         </g:each>
						    </div>
						</td>
						<td>
						<g:if test="${projectInstance?.tg2id}">
							<a href="http://srvdm:8080/TG2/project/projectDetail?tab=0&tab2=1&id=${projectInstance?.tg2id}" title="${tg2ProjectList.find({it.id == projectInstance?.tg2id})?.title}">TG2</a>
						</g:if>
						<g:if test="${projectInstance?.sampleSet}">
							<g:link controller="sampleSet" action="show" title="${projectInstance?.sampleSet?.name}" id="${projectInstance?.sampleSet?.id}">SSAT</g:link>
							<g:link controller="geneBrowser" action="show" title="${projectInstance?.sampleSet?.name}" id="${projectInstance?.sampleSet?.id}">GXB</g:link>
						</g:if>
						<g:if test="${!projectInstance?.sampleSet || !projectInstance?.tg2id}">
							<g:if test="${!projectInstance.locked || projectInstance.owner.id == currentUser?.id}">
								<g:link action="edit" id="${projectInstance?.id}">Link</g:link>
							</g:if>
						</g:if>
						</td>
						<td>${fieldValue(bean: projectInstance, field: "comments")}</td>
					</tr>
				</g:each>
				</tbody>
			</table>
			</div>
			<div class="paginateButtons">
				<g:paginate action="${params.action}" total="${projectInstanceTotal}" />
			</div>
			</g:if>
			<g:else>
				<div class="none-found">No projects found.</div>
			</g:else>
		</div>
	</body>
</html>
