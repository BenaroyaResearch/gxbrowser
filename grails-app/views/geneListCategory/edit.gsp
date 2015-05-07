<%@ page import="org.sagres.geneList.GeneListCategory" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <g:set var="entityName" value="${message(code: 'geneListCategory.label', default: 'Gene List Category')}" />
        <title><g:message code="default.edit.label" args="[entityName]" /></title>
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
        	</ul>

        <div class="body">
            <h1><g:message code="default.edit.label" args="[entityName]" /></h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <g:hasErrors bean="${geneListCategoryInstance}">
            <div class="errors">
                <g:renderErrors bean="${geneListCategoryInstance}" as="list" />
            </div>
            </g:hasErrors>
            <g:form method="post" >
                <g:hiddenField name="id" value="${geneListCategoryInstance?.id}" />
                <g:hiddenField name="version" value="${geneListCategoryInstance?.version}" />
                <div class="dialog">
                    <table>
                        <tbody>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="name"><g:message code="geneListCategory.name.label" default="Name" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: geneListCategoryInstance, field: 'name', 'errors')}">
                                    <g:textField name="name" value="${geneListCategoryInstance?.name}" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="description"><g:message code="geneListCategory.description.label" default="Description" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: geneListCategoryInstance, field: 'description', 'errors')}">
                                    <g:textField name="description" value="${geneListCategoryInstance?.description}" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="geneLists"><g:message code="geneListCategory.geneLists.label" default="Gene Lists" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: geneListCategoryInstance, field: 'geneLists', 'errors')}">
                                    
								<ul>
									<g:each in="${geneListCategoryInstance?.geneLists?}" var="geneList">
    									<li><g:link controller="geneList" action="show" id="${geneList.id}">${geneList?.name}</g:link></li>
									</g:each>
								</ul>
								<g:link controller="geneList" action="create" params="['geneListCategory.id': geneListCategoryInstance?.id]">${message(code: 'default.add.label', args: [message(code: 'geneList.label', default: 'GeneList')])}</g:link>

                                </td>
                            </tr>
                        
                        </tbody>
                    </table>
                </div>
                <div class="buttons">
                    <span class="button"><g:actionSubmit class="save" action="update" value="${message(code: 'default.button.update.label', default: 'Update')}" /></span>
                    <span class="button"><g:actionSubmit class="delete" action="delete" value="${message(code: 'default.button.delete.label', default: 'Delete')}" onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" /></span>
                </div>
            </g:form>
        </div>
    </body>
</html>
