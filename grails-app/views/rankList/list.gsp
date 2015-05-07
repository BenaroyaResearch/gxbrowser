<%@ page import="org.sagres.rankList.RankList" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <g:set var="entityName" value="${message(code: 'rankList.label', default: 'RankList')}" />
        <title><g:message code="default.list.label" args="[entityName]" /></title>
        <g:javascript src="jquery.tablesorter.min.js"/>
  		<g:javascript src="jquery.tablesorter.pager.js"/>
        
    	<g:javascript>
  		var markRankListForDeletion = function(elt, rankListId, deleteState, colspan) {
	  		// alert("about to mark for delete: " + deleteState);
    		var it = $(elt);
    		$.post(getBase()+"/rankList/markForDeletion", {id: rankListId, deleteState: deleteState, colspan: colspan}, function(json) {
	      		// do something with message
      			if (json.error)
      			{
	       	 		$("div#delete-error").show().delay(3000).fadeOut(2000);
      			}
      			else if (deleteState == 1)
      			{
	        		it.closest("tr").html(json.message);
        			//it.closest("tr").html('<td colspan="6">' + json.message + '</td>');
      			} else { // restore the page.
	      			window.location.reload();
      			}
    		});
 	 	};
    $(document).ready(function() {
      var textExtractor = function(node) {
        var childNode = node.childNodes[0];
        if (childNode)
        {
          var nodeName = childNode.nodeName.toLowerCase();
          if (nodeName === "span")
          {
            return childNode.title;
          }
          else if (nodeName === "a")
          {
            return childNode.innerHTML;
          }
        }
        return node.textContent;
      };
      $("table#ranklists").tablesorter({
        sortList: [[0,0]],
        textExtraction: textExtractor
      }).tablesorterPager({
      	container: $("#pager"),
      	size: 20
      });
    });
		</g:javascript>
    </head>
    <body>
<%--        <div class="nav">--%>
<%--            <span class="menuButton"><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a></span>--%>
<%--            <span class="menuButton"><g:link class="create" action="create"><g:message code="default.new.label" args="[entityName]" /></g:link></span>--%>
<%--	        <g:link class="Button"--%>
<%--			        action="showUploadStdRankListForm">--%>
<%--		      Load Std Rank List--%>
<%--	        </g:link>--%>
<%--	        <g:link class="Button"--%>
<%--			        action="showUploadGenericRankListForm">--%>
<%--		      Load Generic Rank List--%>
<%--	        </g:link>--%>
<%--        </div>--%>

        <div class="body">
            <h1><g:message code="default.list.label" args="[entityName]" /></h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <div class="list">
                <table id="ranklists" class="zebra-striped pretty-table">
                    <thead>
                        <tr>
                        
                            <g:sortableColumn property="id" title="${message(code: 'rankList.id.label', default: 'Id')}" />
                        
                            <th><g:message code="rankList.fileLoaded.label" default="File Loaded" /></th>
                        
                            <g:sortableColumn property="sampleSetId" title="${message(code: 'rankList.sampleSetId.label', default: 'Sample Set Id')}" />
                        
                            <th><g:message code="rankList.rankListType.label" default="Rank List Type" /></th>
                        
                            <g:sortableColumn property="description" title="${message(code: 'rankList.description.label', default: 'Description')}" />
                        
                            <g:sortableColumn property="numProbes" title="${message(code: 'rankList.numProbes.label', default: 'Num Probes')}" />
                            <th>Delete</th>
                        
                        </tr>
                    </thead>
                    <tbody>
                    <g:each in="${rankListInstanceList}" status="i" var="rankListInstance">
                        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                        
<%--                            <td><g:link action="show" id="${rankListInstance.id}">${fieldValue(bean: rankListInstance, field: "id")}</g:link></td>--%>
                            <td><g:link action="show" id="${rankListInstance.id}">${rankListInstance.id}</g:link></td>
                        
                            <td>${rankListInstance?.fileLoaded ? fieldValue(bean: rankListInstance, field: "fileLoaded") : ""}</td>
                        
                            <td>${fieldValue(bean: rankListInstance, field: "sampleSetId")}</td>
                        
                            <td>${rankListInstance?.rankListType ? fieldValue(bean: rankListInstance, field: "rankListType") : ""}</td>
                        
                            <td>${fieldValue(bean: rankListInstance, field: "description")}</td>
                        
                            <td>${fieldValue(bean: rankListInstance, field: "numProbes")}</td>
                            <td style="text-align:center;">
                        		<button class="ui-icon-cross" title="Mark this ranklist for deletion" onclick="javascript:markRankListForDeletion(this,${rankListInstance.id}, 1, 6);"/>
                    		</td>
                        </tr>
                    </g:each>
                    </tbody>
                         <tfoot>
        <tr>
          <td colspan="7">
            <div id="pager" class="pager">
              <span class="pagination">
                <ul>
                  <li class="prev"><a href="#">&laquo; Previous</a></li>
                  <li class="next"><a href="#">Next &raquo;</a></li>
                </ul>
              </span>
              <div class="samplesets-pages">
                  <span class="view-xy">You're viewing rank lists <strong><span class="startItem"></span> - <span class="endItem"></span></strong> of <strong> ${rankListInstanceList.size()}</strong>.</span>

                  <span class="view-amount">View</span>
                    <select class="pagesize small">
                        <option value="5">5</option>
                        <option value="10">10</option>
                        <option selected="selected" value="20">20</option>
                        <option value="50">50</option>
                    </select>
                    rank lists per page
              </div>
            </div>
          </td>
        </tr>
      </tfoot>
                    
                </table>
<%--            <div class="paginateButtons">--%>
<%--                <g:paginate controller="rankList" action="list" params="${['sampleSetId': params.sampleSetId]}" total="${rankListInstanceTotal}" />--%>
<%--            </div>--%>
<%--                --%>
            </div>
        </div>
    </body>
</html>
