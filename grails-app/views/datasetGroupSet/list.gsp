<%@ page import="org.sagres.sampleSet.DatasetGroupSet" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="groupSetMain" />
        <g:set var="entityName" value="${message(code: 'datasetGroupSet.label', default: 'DatasetGroupSet')}" />
        <title><g:message code="default.list.label" args="[entityName]" /></title>
    <g:javascript src="jquery-min.js"/>
  	<g:javascript src="jquery-ui-min.js"/>
   	<g:javascript src="jquery.tablesorter.min.js"/>
  	<g:javascript src="jquery.tablesorter.pager.js"/>
  	<g:javascript>
  	var isIE = $.browser.msie;
  	var textExtractor = function(node) {
      var childNode = node.getElementsByTagName("span")[0];
      if (!childNode)
      {
      	childNode = node.getElementsByTagName("a")[0];
      }
      if (childNode)
      {
        var nodeName = childNode.nodeName;
        if (nodeName === "SPAN")
        {
          return childNode.title;
        }
        else if (nodeName === "A")
        {
          return childNode.innerHTML;
        }
      }
      if (isIE) {
        return node.innerText;
      } else {
        return node.textContent;
      }
    };
    $(document).ready(function() {
      $("table#groupset").tablesorter({
     	sortList: [[0,0]],
        textExtraction: textExtractor
      })
      .tablesorterPager({container: $("#pager"), size: 20});
    });
	</g:javascript>
        
    </head>
    <body>
        <div class="sampleset-container">
            <h1><g:message code="default.list.label" args="[entityName]" /></h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <div class="list">
                <table id="groupset" class="zebra-striped pretty-table groupset-list">
                    <thead>
                        <tr>              
                            <th>${message(code: 'datasetGroupSet.id.label', default: 'Id')}</th>                   
                            <th>${message(code: 'datasetGroupSet.name.label', default: 'Name')}</th>
                            <!--  <th><g:message code="datasetGroupSet.defaultRankList.label" default="Default Rank List" /></th> -->
                            <th style="text-align: left;"><g:message code="datasetGroupSet.sampleSet.label" default="Sample Set" /></th>
                        </tr>
                    </thead>
                    <tbody>
                    <g:each in="${datasetGroupSetList}" status="i" var="setList">
                        <tr>
                            <td><g:link action="show" id="${setList.datasetGroupSet?.id}">${setList.datasetGroupSet?.id}</g:link></td>
                            <td><g:link action="show" id="${setList.datasetGroupSet?.id}">${setList.datasetGroupSet?.name}</g:link></td>
                            <!-- <td>${setList.datasetGroupSet?.defaultRankList}</td> -->
                            <td style="text-align: left;"><g:link controller="sampleSet" action="show" id="${setList.sampleSet?.id}">${setList.sampleSet?.name}</g:link></td>
                        </tr>
                    </g:each>
                    </tbody>
                    <tfoot>
              <tr>
                <td colspan="9">
	              <div id="pager" class="pager">
	                <span class="pagination">
		              <ul>
		                <li class="prev"><a href="#">&laquo; Previous</a></li>
          				<li class="next"><a href="#">Next &raquo;</a></li>
		              </ul>
	                </span>
      				<div class="samplesets-pages">
           				<span class="view-xy">You're viewing loaded chips <strong><span class="startItem">x</span> - <span class="endItem">y</span></strong> of <strong> ${datasetGroupSetList.size()}</strong>.</span>
           				<span class="view-amount">View</span>
      					<select class="pagesize small">
        					<option value="5">5</option>
        					<option value="10">10</option>
        					<option selected="selected" value="20">20</option>
        					<option value="50">50</option>
      					</select>
         				groupsets per page
      				</div>
	              </div>
                </td>
              </tr>
            </tfoot>  
                </table>
            </div>
        </div>
    </body>
</html>
