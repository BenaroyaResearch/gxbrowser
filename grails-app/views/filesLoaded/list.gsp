
<%@ page import="org.sagres.FilesLoaded" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <g:set var="entityName" value="${message(code: 'filesLoaded.label', default: 'FilesLoaded')}" />
        <title><g:message code="default.list.label" args="[entityName]" /></title>
    </head>
    <body>
        <div class="nav">
            <span class="menuButton"><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a></span>
            <span class="menuButton"><g:link class="create" action="create"><g:message code="default.new.label" args="[entityName]" /></g:link></span>
	        <g:link class="Button"
			        action="showUploadSoftAnnotationForm">
		      Load Soft Annotation File
	        </g:link>
	        <g:link class="Button"
			        action="showUploadGeoAnnotationTableForm">
		      Load GEO Annotation Table
	        </g:link>
	        <g:link class="Button"
			        action="showUploadChipAnnotationForm">
		      Load Chip Annotation File
	        </g:link>
	        <g:link class="Button"
			        action="showUploadFluidigmDesignFileForm">
		      Load Fluidigm Design File
	        </g:link>
	        <g:link class="Button"
			        action="showUploadNcbiGeneForm">
		      Load NCBI Gene File
	        </g:link>
        </div>

        <div class="body">
            <h1><g:message code="default.list.label" args="[entityName]" /></h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <div class="list">
                <table>
                    <thead>
                        <tr>
                        
                            <g:sortableColumn property="id" title="${message(code: 'filesLoaded.id.label', default: 'Id')}" />
                        
                            <g:sortableColumn property="filename" title="${message(code: 'filesLoaded.filename.label', default: 'Filename')}" />
                        
                            <th><g:message code="filesLoaded.loadStatus.label" default="File Load Status" /></th>
                        
                            <g:sortableColumn property="dateStarted" title="${message(code: 'filesLoaded.dateStarted.label', default: 'Date Started')}" />
                        
                            <g:sortableColumn property="dateEnded" title="${message(code: 'filesLoaded.dateEnded.label', default: 'Date Ended')}" />
                        
                            <g:sortableColumn property="notes" title="${message(code: 'filesLoaded.notes.label', default: 'Notes')}" />
                        
                        </tr>
                    </thead>
                    <tbody>
                    <g:each in="${filesLoadedInstanceList}" status="i" var="filesLoadedInstance">
                        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                        
                            <td><g:link action="show" id="${filesLoadedInstance.id}">${fieldValue(bean: filesLoadedInstance, field: "id")}</g:link></td>
                        
                            <td>${fieldValue(bean: filesLoadedInstance, field: "filename")}</td>
                        
                            <td>${fieldValue(bean: filesLoadedInstance, field: "loadStatus")}</td>
                        
                            <td><g:formatDate date="${filesLoadedInstance.dateStarted}" /></td>
                        
                            <td><g:formatDate date="${filesLoadedInstance.dateEnded}" /></td>
                        
                            <td>${fieldValue(bean: filesLoadedInstance, field: "notes")}</td>
                        
                        </tr>
                    </g:each>
                    </tbody>
                </table>
            </div>
            <div class="paginateButtons">
                <g:paginate total="${filesLoadedInstanceTotal}" />
            </div>
        </div>
    </body>
</html>
