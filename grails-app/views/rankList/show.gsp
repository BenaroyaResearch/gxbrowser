
<%@ page import="org.sagres.rankList.RankList" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <g:set var="entityName" value="${message(code: 'rankList.label', default: 'RankList')}" />
        <title><g:message code="default.show.label" args="[entityName]" /></title>
    </head>
    <body>
        <div class="nav">
            <span class="menuButton"><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a></span>
            <span class="menuButton"><g:link class="list" action="list"><g:message code="default.list.label" args="[entityName]" /></g:link></span>
            <span class="menuButton"><g:link class="create" action="create"><g:message code="default.new.label" args="[entityName]" /></g:link></span>
        </div>
        <div class="body">
            <h1><g:message code="default.show.label" args="[entityName]" /></h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <div class="dialog">
                <table>
                    <tbody>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="rankList.id.label" default="Id" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: rankListInstance, field: "id")}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="rankList.fileLoaded.label" default="File Loaded" /></td>
                            
                            <td valign="top" class="value">
                              <g:if test="${rankListInstance?.fileLoadedId}">
                                <g:link controller="filesLoaded" action="show"
                                        id="${rankListInstance?.fileLoaded?.id}">
                                  ${rankListInstance?.fileLoaded?.encodeAsHTML()}
                                </g:link>
                              </g:if>
                            </td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="rankList.sampleSetId.label" default="Sample Set Id" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: rankListInstance, field: "sampleSetId")}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="rankList.rankListType.label" default="Rank List Type" /></td>
                            
                            <td valign="top" class="value"><g:link controller="rankListType" action="show" id="${rankListInstance?.rankListType?.id}">${rankListInstance?.rankListType?.encodeAsHTML()}</g:link></td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="rankList.description.label" default="Description" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: rankListInstance, field: "description")}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="rankList.numProbes.label" default="Num Probes" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: rankListInstance, field: "numProbes")}</td>
                            
                        </tr>
                    
                    </tbody>
                </table>
            </div>
            <div class="buttons">
                <g:form>
                    <g:hiddenField name="id" value="${rankListInstance?.id}" />
                    <span class="button"><g:actionSubmit class="edit" action="edit" value="${message(code: 'default.button.edit.label', default: 'Edit')}" /></span>
                    <span class="button"><g:actionSubmit class="delete" action="delete" value="${message(code: 'default.button.delete.label', default: 'Delete')}" onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" /></span>
                </g:form>
            </div>
        </div>
    </body>
</html>
