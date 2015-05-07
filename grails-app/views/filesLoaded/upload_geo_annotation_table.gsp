<%@ page import="org.sagres.FilesLoaded" %>
<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="layout" content="main" />
    <g:set var="entityName" value="${message(code: 'rankList.label', default: 'RankList')}" />
    <title>Load GEO Annotation Table</title>
  </head>
  <body>

    <div class="nav">
      <span class="menuButton"><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a></span>
    </div>

    <div class="body">
      <h1>Load GEO Annotation Table</h1>
      <g:if test="${flash.message}">
        <div class="message">${flash.message}</div>
      </g:if>

	  <g:uploadForm action="uploadGeoAnnotationTable">
		
		<label for="annotationFile">
		  Filename
		</label>
		<input type="file" name="annotationFile" />
		<br />

		<g:checkBox name="runNow" value="${false}" checked="true" />
		Run Now
		<br />

		<!--Cancel button!!!-->
		<g:submitButton name="upload" value="Upload" />
		
	  </g:uploadForm>

    </div>
  </body>
</html>
	  
