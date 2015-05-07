<%@ page import="org.sagres.mat.Version" %>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<meta name="layout" content="main"/>
	<g:set var="entityName" value="${message(code: 'version.label', default: 'Version')}"/>
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
	<g:hasErrors bean="${moduleVersionFileInstance}">
		<div class="errors">
			<g:renderErrors bean="${moduleVersionFileInstance}" as="list"/>
		</div>
	</g:hasErrors>
	<g:form action="save" enctype="multipart/form-data" >
		<div class="dialog">
			<table>
				<tbody>

				<tr class="prop">
					<td valign="top" class="name">
						<label for="versionName"><g:message code="moduleVersionFile.versionName.label"
																								default="Version Name"/></label>
					</td>
					<td valign="top" class="value ${hasErrors(bean: moduleVersionFileInstance, field: 'versionName', 'errors')}">
						<g:textField name="versionName" value="${moduleVersionFileInstance?.versionName}"/>
					</td>
				</tr>


				<tr class="prop">
					<td valign="top" class="name">Upload Version File:</td>
					<td valign="top" class="name">
						<input type="file" id="versionFile" name="versionFile"/>
					</td>
				</tr>

				<tr class="prop">
					<td valign="top" class="name">Upload Function File:</td>
					<td valign="top" class="name">
						<input type="file" id="functionFile" name="functionFile"/>
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
