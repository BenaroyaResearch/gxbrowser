<%@ page import="common.GeneInfo" %>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<meta name="layout" content="main"/>
	<g:set var="entityName" value="${message(code: 'geneInfo.label', default: 'GeneInfo')}"/>
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
	<g:hasErrors bean="${geneInfoInstance}">
		<div class="errors">
			<g:renderErrors bean="${geneInfoInstance}" as="list"/>
		</div>
	</g:hasErrors>
	<g:form action="save">
		<div class="dialog">
			<table>
				<tbody>

				<tr class="prop">
					<td valign="top" class="name">
						<label for="dbXref"><g:message code="geneInfo.dbXref.label" default="Db Xref"/></label>
					</td>
					<td valign="top" class="value ${hasErrors(bean: geneInfoInstance, field: 'dbXref', 'errors')}">
						<g:textField name="dbXref" value="${geneInfoInstance?.dbXref}"/>
					</td>
				</tr>

				<tr class="prop">
					<td valign="top" class="name">
						<label for="description"><g:message code="geneInfo.description.label" default="Description"/></label>
					</td>
					<td valign="top" class="value ${hasErrors(bean: geneInfoInstance, field: 'description', 'errors')}">
						<g:textField name="description" value="${geneInfoInstance?.description}"/>
					</td>
				</tr>

				<tr class="prop">
					<td valign="top" class="name">
						<label for="geneID"><g:message code="geneInfo.geneID.label" default="Gene ID"/></label>
					</td>
					<td valign="top" class="value ${hasErrors(bean: geneInfoInstance, field: 'geneID', 'errors')}">
						<g:textField name="geneID" value="${fieldValue(bean: geneInfoInstance, field: 'geneID')}"/>
					</td>
				</tr>

				<tr class="prop">
					<td valign="top" class="name">
						<label for="symbol"><g:message code="geneInfo.symbol.label" default="Symbol"/></label>
					</td>
					<td valign="top" class="value ${hasErrors(bean: geneInfoInstance, field: 'symbol', 'errors')}">
						<g:textField name="symbol" value="${geneInfoInstance?.symbol}"/>
					</td>
				</tr>

				<tr class="prop">
					<td valign="top" class="name">
						<label for="synonyms"><g:message code="geneInfo.synonyms.label" default="Synonyms"/></label>
					</td>
					<td valign="top" class="value ${hasErrors(bean: geneInfoInstance, field: 'synonyms', 'errors')}">
						<g:textField name="synonyms" value="${geneInfoInstance?.synonyms}"/>
					</td>
				</tr>

				<tr class="prop">
					<td valign="top" class="name">
						<label for="taxID"><g:message code="geneInfo.taxID.label" default="Tax ID"/></label>
					</td>
					<td valign="top" class="value ${hasErrors(bean: geneInfoInstance, field: 'taxID', 'errors')}">
						<g:textField name="taxID" value="${fieldValue(bean: geneInfoInstance, field: 'taxID')}"/>
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
