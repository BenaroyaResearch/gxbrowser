<%@ page import="org.sagres.gene.GeneRif" %>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<meta name="layout" content="main"/>
	<g:set var="entityName" value="${message(code: 'geneRif.label', default: 'GeneRif')}"/>
	<title><g:message code="default.create.label" args="[entityName]"/></title>
</head>

<body>
<div class="nav">
	<span class="menuButton"><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a>
	</span>
	<span class="menuButton"><g:link class="list" action="list"><g:message code="default.list.label"
	                                                                       args="[entityName]"/></g:link></span>
</div>

<div class="body">
	<h1><g:message code="default.create.label" args="[entityName]"/></h1>
	<g:if test="${flash.message}">
		<div class="message">${flash.message}</div>
	</g:if>
	<g:hasErrors bean="${geneRifInstance}">
		<div class="errors">
			<g:renderErrors bean="${geneRifInstance}" as="list"/>
		</div>
	</g:hasErrors>
	<g:form action="save">
		<div class="dialog">
			<table>
				<tbody>

				<tr class="prop">
					<td valign="top" class="name">
						<label for="geneID"><g:message code="geneRif.geneID.label" default="Gene ID"/></label>
					</td>
					<td valign="top" class="value ${hasErrors(bean: geneRifInstance, field: 'geneID', 'errors')}">
						<g:textField name="geneID" value="${fieldValue(bean: geneRifInstance, field: 'geneID')}"/>
					</td>
				</tr>

				<tr class="prop">
					<td valign="top" class="name">
						<label for="geneRIF"><g:message code="geneRif.geneRIF.label" default="Gene RIF"/></label>
					</td>
					<td valign="top" class="value ${hasErrors(bean: geneRifInstance, field: 'geneRIF', 'errors')}">
						<g:textField name="geneRIF" value="${geneRifInstance?.geneRIF}"/>
					</td>
				</tr>

				<tr class="prop">
					<td valign="top" class="name">
						<label for="pubmedID"><g:message code="geneRif.pubmedID.label" default="Pubmed ID"/></label>
					</td>
					<td valign="top" class="value ${hasErrors(bean: geneRifInstance, field: 'pubmedID', 'errors')}">
						<g:textField name="pubmedID" value="${geneRifInstance?.pubmedID}"/>
					</td>
				</tr>

				</tbody>
			</table>
		</div>

		<div class="buttons">
			<span class="button"><g:submitButton name="create" class="save"
			                                     value="${message(code: 'default.button.create.label', default: 'Create')}"/></span>
		</div>
	</g:form>
</div>
</body>
</html>
