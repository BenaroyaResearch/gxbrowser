
<%@ page import="common.chipInfo.ChipsLoaded" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="chipsLoadedMain" />
        <g:set var="entityName" value="${message(code: 'chipsLoaded.label', default: 'ChipsLoaded')}" />
        <title><g:message code="default.show.label" args="[entityName]" /></title>
    </head>

    <body>

    <div class="chipsloaded-container">

        <div class="matcreate-wrapper">

            <h1><g:message code="default.show.label" args="[entityName]" /></h1>

             %{--<ul class="pills chips-pills">--}%
            %{--<li><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a></li>--}%
            %{--<li><g:link class="list" action="list"><g:message code="default.list.label" args="[entityName]" /></g:link></li>--}%
            %{--<li><g:link class="create" action="create"><g:message code="default.new.label" args="[entityName]" /></g:link></li>--}%
        %{--</ul>--}%

            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>

     %{--<div sty--}%%{--le="border: 1px solid red; clear: both;"><label><g:message code="chipsLoaded.id.label" default="Id" /></label>--}%

             %{--<span class="span3"> ${fieldValue(bean: chipsLoadedInstance, field: "id")}</span>--}%
         %{--</div>--}%

            <div class="centered-gray">
                <table class="zebra-striped pretty-table">
                    <tbody>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="chipsLoaded.id.label" default="Id" /></td>

                            <td valign="top" class="value">${fieldValue(bean: chipsLoadedInstance, field: "id")}</td>

                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="chipsLoaded.filename.label" default="Filename" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: chipsLoadedInstance, field: "filename")}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="chipsLoaded.chipType.label" default="Chip Type" /></td>
                            
                            <td valign="top" class="value"><g:link controller="chipType" action="show" id="${chipsLoadedInstance?.chipType?.id}">${chipsLoadedInstance?.chipType?.encodeAsHTML()}</g:link></td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="chipsLoaded.noSamples.label" default="No Samples" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: chipsLoadedInstance, field: "noSamples")}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="chipsLoaded.noProbes.label" default="No Probes" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: chipsLoadedInstance, field: "noProbes")}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="chipsLoaded.loadStatus.label" default="Load Status" /></td>
                            
                            <td valign="top" class="value"><g:link controller="chipLoadStatus" action="show" id="${chipsLoadedInstance?.loadStatus?.id}">${chipsLoadedInstance?.loadStatus?.encodeAsHTML()}</g:link></td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="chipsLoaded.notes.label" default="Notes" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: chipsLoadedInstance, field: "notes")}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="chipsLoaded.dateStarted.label" default="Date Started" /></td>
                            
                            <td valign="top" class="value"><g:formatDate date="${chipsLoadedInstance?.dateStarted}" /></td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="chipsLoaded.dateEnded.label" default="Date Ended" /></td>
                            
                            <td valign="top" class="value"><g:formatDate date="${chipsLoadedInstance?.dateEnded}" /></td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="chipsLoaded.genomicDataSource.label" default="Data Source" /></td>
                            
                            <td valign="top" class="value"><g:link controller="genomicDataSource" action="show" id="${chipsLoadedInstance?.genomicDataSource?.id}">${chipsLoadedInstance?.genomicDataSource?.name}</g:link></td>
                            
                        </tr>
                    
                    </tbody>
                </table>

            %{--<div>--}%
                %{--<g:form>--}%
                    %{--<g:hiddenField name="id" value="${chipsLoadedInstance?.id}" />--}%
                    %{--<g:actionSubmit class="edit btn primary" action="edit" value="${message(code: 'default.button.edit.label', default: 'Edit')}" />--}%
                    %{--<g:actionSubmit class="delete btn danger" action="delete" value="${message(code: 'default.button.delete.label', default: 'Delete')}" onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" />--}%
                %{--</g:form>--}%
            %{--</div>--}%

               </div><!--end centered gray-->

        </div>

        </div>

    </body>
</html>
