<%@ page import="org.sagres.mat.Version" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <g:set var="entityName" value="${message(code: 'version.label', default: 'Version')}" />
        <title><g:message code="default.list.label" args="[entityName]" /></title>
    </head>
    <body>
        <div class="nav">
            <span class="menuButton"><g:link class="create" action="create"><g:message code="default.new.label" args="[entityName]" /></g:link></span>
        </div>
        <div class="body">
            <h1><g:message code="default.list.label" args="[entityName]" /></h1>
            <div><g:link action="associate" >Associate Versions with chips</g:link></div>
					<br/>
					<br/>
            <div class="list">
                <table>
                    <thead>
                        <tr>
                        
                            <g:sortableColumn property="id" title="${message(code: 'version.id.label', default: 'Id')}" />
                        
                            <g:sortableColumn property="binaryFileFunction" title="${message(code: 'version.binaryFileFunction.label', default: 'Binary File Function')}" />
                        
                            <g:sortableColumn property="binaryFileVersion" title="${message(code: 'version.binaryFileVersion.label', default: 'Binary File Version')}" />
                        
                            <g:sortableColumn property="dateCreated" title="${message(code: 'version.dateCreated.label', default: 'Date Created')}" />
                        
                            <g:sortableColumn property="lastUpdated" title="${message(code: 'version.lastUpdated.label', default: 'Last Updated')}" />
                        
                            <g:sortableColumn property="versionName" title="${message(code: 'version.versionName.label', default: 'Version 2 Name')}" />



                        </tr>
                    </thead>
                    <tbody>
                    <g:each in="${moduleVersionFileInstanceList}" status="i" var="versionInstance">
                        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                        
                            <td><g:link action="show" id="${versionInstance.id}">${fieldValue(bean: versionInstance, field: "id")}</g:link></td>
                        
                            <td><g:link action="show" id="${versionInstance.id}">${fieldValue(bean: versionInstance, field: "binaryFileFunction")}</g:link></td>
                        
                            <td><g:link action="show" id="${versionInstance.id}">${fieldValue(bean: versionInstance, field: "binaryFileVersion")}</g:link></td>
                        
                            <td><g:link action="show" id="${versionInstance.id}"><g:formatDate date="${versionInstance.dateCreated}" /></g:link></td>
                        
                            <td><g:link action="show" id="${versionInstance.id}"><g:formatDate date="${versionInstance.lastUpdated}" /></g:link></td>
                        
                            <td><g:link action="show" id="${versionInstance.id}">${fieldValue(bean: versionInstance, field: "versionName")}</g:link></td>


                        </tr>
                    </g:each>
                    </tbody>
                </table>
            </div>
            <div class="paginateButtons">
                <g:paginate total="${moduleVersionFileInstanceTotal}" />
            </div>
        </div>
    </body>
</html>
