<%@ page import="org.sagres.gene.GeneRif" %>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<meta name="layout" content="main"/>
	<g:set var="entityName" value="${message(code: 'geneRif.label', default: 'GeneRif')}"/>
	<title><g:message code="default.list.label" args="[entityName]"/></title>
</head>

<body>
<div class="nav">
	<span class="menuButton"><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a>
	</span>
	<span class="menuButton"><g:link class="create" action="create"><g:message code="default.new.label"
	                                                                           args="[entityName]"/></g:link></span>
</div>

<div class="body">
	<h1><g:message code="default.list.label" args="[entityName]"/></h1>
	<g:if test="${flash.message}">
		<div class="message">${flash.message}</div>
	</g:if>
	<div class="list">
		<table>
			<thead>
			<tr>

				<g:sortableColumn property="id" title="${message(code: 'geneRif.id.label', default: 'Id')}"/>

				<g:sortableColumn property="geneID" title="${message(code: 'geneRif.geneID.label', default: 'Gene ID')}"/>

				<g:sortableColumn property="geneRIF" title="${message(code: 'geneRif.geneRIF.label', default: 'Gene RIF')}"/>

				<g:sortableColumn property="lastUpdated"
				                  title="${message(code: 'geneRif.lastUpdated.label', default: 'Last Updated')}"/>

				<g:sortableColumn property="pubmedID" title="${message(code: 'geneRif.pubmedID.label', default: 'Pubmed ID')}"/>

			</tr>
			</thead>
			<tbody>
			<g:each in="${geneRifInstanceList}" status="i" var="geneRifInstance">
				<tr class="${(i % 2) == 0 ? 'odd' : 'even'}">

					<td><g:link action="show"
					            id="${geneRifInstance.id}">${fieldValue(bean: geneRifInstance, field: "id")}</g:link></td>

					<td>${fieldValue(bean: geneRifInstance, field: "geneID")}</td>

					<td>${fieldValue(bean: geneRifInstance, field: "geneRIF")}</td>

					<td>${fieldValue(bean: geneRifInstance, field: "lastUpdated")}</td>

					<td>${fieldValue(bean: geneRifInstance, field: "pubmedID")}</td>

				</tr>
			</g:each>
			</tbody>
		</table>
	</div>

	<div class="paginateButtons">
		<g:paginate total="${geneRifInstanceTotal}"/>
	</div>
</div>
</body>
</html>
