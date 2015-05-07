
<%@ page import="org.sagres.mat.MetaCat" %>
<!doctype html>
<html>
	<head>
		<meta name="layout" content="metacatMain">
		<g:set var="entityName" value="${message(code: 'metaCat.label', default: 'MetaCat Collection')}" />
		<title><g:message code="default.show.label" args="[entityName]" /></title>
	</head>
	<body>
		<div id="show-metaCat" class="content scaffold-show" role="main">
			<h1>${entityName} #${metaCatInstance?.id}</h1>
			<g:if test="${flash.message}">
			<div class="message" role="status">${flash.message}</div>
			</g:if>
			<ul class="property-list metaCat">
				<g:if test="${metaCatInstance?.displayName}">
				<li class="fieldcontain">
					<span id="displayName-label" class="property-label"><g:message code="metaCat.displayName.label" default="Name" />: </span>
						<span class="property-value" aria-labelledby="displayName-label"><g:fieldValue bean="${metaCatInstance}" field="displayName"/></span>
				</li>
				</g:if>

				<g:if test="${metaCatInstance?.disease}">
				<li class="fieldcontain">
					<span id="disease-label" class="property-label"><g:message code="metaCat.disease.label" default="Disease" />: </span>
						<span class="property-value" aria-labelledby="disease-label"><g:fieldValue bean="${metaCatInstance}" field="disease"/></span>
				</li>
				</g:if>

				<g:if test="${metaCatInstance?.generation}">
				<li class="fieldcontain">
					<span id="generation-label" class="property-label"><g:message code="metaCat.generation.label" default="Generation" />: </span>
						<span class="property-value" aria-labelledby="generation-label"><g:fieldValue bean="${metaCatInstance}" field="generation"/></span>
				</li>
				</g:if>
				<g:if test="${metaCatInstance?.noSamples}">
				<li class="fieldcontain">
					<span id="noSamples-label" class="property-label"><g:message code="metaCat.noSamples.label" default="Samples" />: </span>
						<span class="property-value" aria-labelledby="noSamples-label"><g:fieldValue bean="${metaCatInstance}" field="noSamples"/></span>
				</li>
				</g:if>
				<g:if test="${metaCatInstance?.noCases}">
				<li class="fieldcontain">
					<span id="noCases-label" class="property-label"><g:message code="metaCat.noCases.label" default="Cases" />: </span>
						<span class="property-value" aria-labelledby="noCases-label"><g:fieldValue bean="${metaCatInstance}" field="noCases"/></span>
				</li>
				</g:if>
				<g:if test="${metaCatInstance?.user}">
				<li class="fieldcontain">
					<span id="user-label" class="property-label"><g:message code="project.user.label" default="Owner" />: </span>
					
						<span class="property-value" aria-labelledby="user-label">${metaCatInstance?.user?.encodeAsHTML()}</span>
					
				</li>
				</g:if>
			
				<g:if test="${metaCatInstance?.analyses}">
				<li class="fieldcontain">
					<span id="analyses-label" class="property-label"><g:message code="project.analyses.label" default="Analyses" />: </span>
						<p>
						<g:each in="${metaCatInstance.analyses}" status="d" var="analysisInstance">
				            	<span class="analyses-value" aria-labelledby="analyses-label">#${analysisInstance?.id} <g:link controller="analysis" action="show" id="${analysisInstance?.id}" target="_blank">${analysisInstance?.displayName}</g:link></span><br/>
				         </g:each>
				         </p>
				</li>
				
<%--				<li class="fieldcontain">--%>
<%--					<span id="analyses-label" class="property-label"><g:message code="metaCat.analyses.label" default="Analyses" /></span>--%>
<%--					--%>
<%--						<g:each in="${metaCatInstance.analyses}" var="a">--%>
<%--						<span class="property-value" aria-labelledby="analyses-label"><g:link controller="analysis" action="show" id="${a.id}">${a?.encodeAsHTML()}</g:link></span>--%>
<%--						</g:each>--%>
<%--					--%>
<%--				</li>--%>
				</g:if>
			
			
			</ul>
			<g:if test="${currentUser == metaCatInstance?.user || currentUser?.authorities?.any { it.authority == 'ROLE_ADMIN' }}">
			<g:form>
				<fieldset class="buttons">
					<g:hiddenField name="id" value="${metaCatInstance?.id}" />
					<g:link class="edit" action="edit" id="${metaCatInstance?.id}"><g:message code="default.button.edit.label" default="Edit" /></g:link>
					<g:actionSubmit class="delete" action="delete" value="${message(code: 'default.button.delete.label', default: 'Delete')}" onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" />
				</fieldset>
			</g:form>
			</g:if>
		</div>
	</body>
</html>
