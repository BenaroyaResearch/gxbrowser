<%@ page import="org.sagres.geneList.GeneListCategory" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <g:set var="entityName" value="${message(code: 'geneListCategory.label', default: 'Gene List Category')}" />
        <title><g:message code="default.show.label" args="[entityName]" /></title>
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
            	<li><g:link class="list" action="list"><g:message code="default.list.label" args="[entityName]" /></g:link></li>
              	<sec:ifAnyGranted roles="ROLE_GENELISTS">
	            	<li><g:link class="create" action="create"><g:message code="default.new.label" args="[entityName]" /></g:link></li>
	            </sec:ifAnyGranted>
        	</ul>

        <div class="body">
            <h1><g:message code="default.show.label" args="[entityName]" /></h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <div class="dialog">
                <table>
                    <tbody>
                                        
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="geneListCategory.name.label" default="Name" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: geneListCategoryInstance, field: "name")}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="geneListCategory.description.label" default="Description" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: geneListCategoryInstance, field: "description")}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="geneListCategory.geneLists.label" default="Gene Lists" /></td>
                            
                            <td valign="top" style="text-align: left;" class="value">
                                <ul>
                                <g:each in="${geneListCategoryInstance.geneLists}" var="geneListItem">
                                    <li><g:link controller="geneList" action="show" id="${geneListItem.id}">${geneListItem?.name}</g:link></li>
                                </g:each>
                                </ul>
                                <sec:ifAnyGranted roles="ROLE_GENELISTS">
	                                <g:link controller="geneList" action="create" params="['geneListCategory.id': geneListCategoryInstance?.id]">${message(code: 'default.add.label', args: [message(code: 'geneList.label', default: 'Gene List')])}</g:link>
                                </sec:ifAnyGranted>
                                
                            </td>
                            
                        </tr>
                    
                    </tbody>
                </table>
            </div>
            <sec:ifAnyGranted roles="ROLE_GENELISTS">
            <div class="buttons">
                <g:form>
                    <g:hiddenField name="id" value="${geneListCategoryInstance?.id}" />
                    <span class="button"><g:actionSubmit class="edit" action="edit" value="${message(code: 'default.button.edit.label', default: 'Edit')}" /></span>
                    <span class="button"><g:actionSubmit class="delete" action="delete" value="${message(code: 'default.button.delete.label', default: 'Delete')}" onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" /></span>
                </g:form>
            </div>
            </sec:ifAnyGranted>
        </div>
    </body>
</html>
