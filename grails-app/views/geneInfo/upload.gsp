<%@ page import="common.GeneInfo" %>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<meta name="layout" content="main"/>
	<g:set var="entityName" value="${message(code: 'geneInfo.label', default: 'GeneInfo')}"/>
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
<h3>Import Gene Info File</h3>
	<g:uploadForm action="importFile">
		<label for="theFile">File: </label><input type="file" name="theFile" />
		<br/>
		<g:submitButton name="Upload File" value="Upload File"/>
	</g:uploadForm>
</div>
</body>
</html>
