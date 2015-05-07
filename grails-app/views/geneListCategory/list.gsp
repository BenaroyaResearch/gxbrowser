<%@ page import="org.sagres.geneList.GeneList; org.sagres.geneList.GeneListCategory" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <g:set var="entityName" value="${message(code: 'geneListCategory.label', default: 'Gene List Category')}" />
        <g:set var="geneList" value="${message(code: 'geneList.label', default: 'Gene List')}" />
        <title><g:message code="default.list.label" args="[entityName]" /></title>
    </head>
    <body>
 
 <div class="topbar">
	<div class="topbar-inner itn-fill">
	%{--<div class="topbar-inner bri-fill">--}%
    <div class="sampleset-container">
			<!--<img src="../images/itn_logo_2.png" style="margin-top: 7px; margin-right: 5px; float: left;"/>    -->
      <h3 style="display: inline"><g:link controller="geneBrowser" action="list"><strong>GXB</strong></g:link></h3>
      <ul class="nav secondary-nav">
        <li><g:link controller="sampleSet" action="list" target="_blank">Annotation Tool</g:link></li>
      </ul>
    </div>
  </div><!-- /fill -->
</div><!-- /topbar -->
       	<ul class="nav secondary-nav pills">
           	  <li><g:link controller="geneList" class="list" action="list"><g:message code="default.list.label" args="[geneList]" /></g:link></li>
       	      <li><g:link controller="geneList" class="create" action="create"><g:message code="default.new.label" args="[geneList]" /></g:link></li>
              <sec:ifAnyGranted roles="ROLE_GENELISTS">
               <li><g:link class="create" action="create"><g:message code="default.new.label" args="[entityName]" /></g:link></li>
              </sec:ifAnyGranted>
              
        	</ul>

        <div class="body">
            <h1><g:message code="default.list.label" args="[entityName]" /></h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <div class="list">
                <table>
                    <thead>
                        <tr>
    						<sec:ifAnyGranted roles="ROLE_GENELISTS">
        						<g:sortableColumn property="edit" title="Operation"/>
							</sec:ifAnyGranted>
                            <g:sortableColumn property="name" title="${message(code: 'geneListCategory.name.label', default: 'Name')}" />
                            <g:sortableColumn property="description" title="${message(code: 'geneListCategory.description.label', default: 'Description')}" />
                            <g:sortableColumn property="list count" title="List Count"/>
                        
                        </tr>
                    </thead>
                    <tbody>
                    <g:each in="${geneListCategoryInstanceList}" status="i" var="geneListCategoryInstance">
                        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                     		  <sec:ifAnyGranted roles="ROLE_GENELISTS">
          								<td><g:link action="edit" id="${geneListCategoryInstance.id}">Edit</g:link></td>
          						  </sec:ifAnyGranted>
                            <td><g:link action="show" id="${geneListCategoryInstance.id}">${fieldValue(bean: geneListCategoryInstance, field: "name")}</g:link></td>
                            <td>${fieldValue(bean: geneListCategoryInstance, field: "description")}</td>
                            <td>${GeneList.countByGeneListCategory(geneListCategoryInstance)}</td>
                        
                        </tr>
                    </g:each>
                    </tbody>
                </table>
            </div>
            <div class="paginateButtons">
                <g:paginate total="${geneListCategoryInstanceTotal}" />
            </div>
        </div>
    </body>
</html>
