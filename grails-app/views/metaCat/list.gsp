<%@ page import="org.sagres.mat.MetaCat" %>
<!doctype html>
<html>
	<head>
		<meta name="layout" content="metacatMain">
		<g:set var="entityName" value="${message(code: 'metaCat.label', default: 'MetaCat Collections')}" />
		<title><g:message code="default.list.label" args="[entityName]" /></title>
	</head>
	<body>
		<div class="body">
		<g:if test="${!metaCatInstanceList.isEmpty()}">
		<div id="list">
			<h1><g:message code="default.list.label" args="[entityName]" /></h1>
			<g:if test="${flash.message}">
			<div class="message" role="status">${flash.message}</div>
			</g:if>
			<table>
				<thead>
					<tr>
						<td></td>
						<g:sortableColumn property="displayName" title="${message(code: 'metaCat.displayName.label', default: 'Name')}" />
						<g:sortableColumn property="disease" title="${message(code: 'metaCat.disease.label', default: 'Disease')}" />
						<g:sortableColumn property="generation" title="${message(code: 'metaCat.generation.label', default: 'Generation')}" />
						<g:sortableColumn property="analyses" title="Analyses" />
						<g:sortableColumn property="samples" title="${message(code: 'metaCat.noSamples.label', default: 'Samples')}" />
						<g:sortableColumn property="cases" title="${message(code: 'metaCat.noCases.label', default: 'Cases')}" />
						<g:sortableColumn property="user" title="Owner" />
						<td>Module Plot</td>
						<td>Scatter Plot</td>
					</tr>
				</thead>
				<tbody>
				<g:each in="${metaCatInstanceList}" status="i" var="metaCatInstance">
					<tr class="${(i % 2) == 0 ? 'even' : 'odd'}">
						<td><g:link class="btn primary small" action="show" id="${metaCatInstance.id}">Show</g:link></td>
						<td>${fieldValue(bean: metaCatInstance, field: "displayName")}</td>
						<td>${fieldValue(bean: metaCatInstance, field: "disease")}</td>
						<td>${fieldValue(bean: metaCatInstance, field: "generation")}</td>
						<td>${metaCatInstance.analyses.size()}</td>
						<td>${metaCatInstance.noSamples}</td>
						<td>${metaCatInstance.noCases}</td>
						<td>${metaCatInstance.user?.username}</td>
						<td><g:link controller="analysis" action="metaCompare" id="${metaCatInstance.id}">${fieldValue(bean: metaCatInstance, field: "displayName")}</g:link></td>
						<td><g:link controller="analysis" action="metaScatter" id="${metaCatInstance.id}">${fieldValue(bean: metaCatInstance, field: "displayName")}</g:link></td>
					</tr>
				</g:each>
				</tbody>
			</table>
		</div>
		<div class="paginateButtons">
			<g:paginate total="${metaCatInstanceTotal}" />
		</div>
		</g:if>
		<g:else>
			<div class="none-found">No collections found.</div>
		</g:else>
		</div>	
	</body>
</html>
