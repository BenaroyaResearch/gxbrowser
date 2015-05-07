<%@ page import="org.sagres.geneList.GeneList; org.sagres.geneList.GeneListCategory; org.sagres.geneList.GeneListDetail; org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils" %>
<html>
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
  <meta name="layout" content="gxbmain"/>
  <g:set var="entityName" value="${message(code: 'geneList.label', default: 'Gene List')}"/>
  <g:set var="categoryName" value="${message(code: 'geneListCategory.label', default: 'Gene List Category')}"/>
  <title><g:message code="default.list.label" args="[entityName]"/></title>
  
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
      $("table#genelists").tablesorter({
        sortList: [[0,2]],
        textExtraction: textExtractor, widthFixed: true
      }).tablesorterPager({container: $("#pager"), size: 20});
    });
      // LOGIN stuff
   var showLoginForm = function() {
    $("div.login-content").parent().toggle();
    $("div.login-content").find("#username").focus();
  };
    
  </g:javascript>
  
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

<div class="sampleset-container">
 <ul class="nav secondary-nav pills">
  <sec:ifAnyGranted roles="ROLE_USER">
	  <li><g:link class="create" action="create"><g:message code="default.new.label" args="[entityName]"/></g:link></li>
  </sec:ifAnyGranted>
  <sec:ifAnyGranted roles="ROLE_GENELISTS">
  	<li><g:link controller="geneListCategory" class="create" action="create"> <g:message code="default.new.label" args="[categoryName]"/> </g:link>
  </li>
  </sec:ifAnyGranted>
  </ul>

  <div class="page-header">
  <h2><g:message code="default.list.label" args="[entityName]"/></h2>
  </div>
  <g:if test="${flash.message}">
    <div class="message">${flash.message}</div>
  </g:if>
  <g:hasErrors bean="${feedback}">
	<div class="errors">
		<g:renderErrors bean="${feedback}" as="list" />
	</div>
  </g:hasErrors>
 
    <table id="genelists" class="zebra-striped pretty-table">
      <thead>
      <tr>
      	<sec:ifLoggedIn>
    		<th>Operation</th>
    	</sec:ifLoggedIn>
        <th>Name</th>
		<th>Category</th>
        <th>Description</th>
 		<th>Owner</th>
        <th>Gene Count</th>
      </tr>
      </thead>
      <tbody>
      <g:each in="${geneListInstanceList}" status="i" var="geneListInstance">
        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
             <sec:ifLoggedIn>
                 <g:if test="${editable == true || user?.id == geneListInstance?.user?.id}">
          			<td><g:link action="edit" id="${geneListInstance.id}">Edit</g:link></td>
          		</g:if>
          		<g:else>
          			<td>(Edit)</td>
          		</g:else>	
          	</sec:ifLoggedIn>
          <td><g:link action="show" id="${geneListInstance.id}">${fieldValue(bean: geneListInstance, field: "name")}</g:link></td>
		  <td><g:link controller="geneListCategory" action="show" id="${geneListInstance.geneListCategory.id}">${fieldValue(bean: geneListInstance, field: "geneListCategory.name") }</g:link></td>
          <td>${fieldValue(bean: geneListInstance, field: "description")}</td>
		  <td>${fieldValue(bean: geneListInstance, field: "user.username")}</td>
		  <td>${GeneListDetail.countByGeneList(geneListInstance)}</td>
        </tr>
      </g:each>
      </tbody>
      <tfoot>
         <tr><td colspan="6">
          <div id="pager" class="pager">
      <span class="pagination">
        <ul>
          <li class="prev"><a href="#">&laquo; Previous</a></li>
          <li class="next"><a href="#">Next &raquo;</a></li>
        </ul>
      </span>
      <div style="padding-top: 5px;">
        <span style="padding-left: 10px;">You're viewing gene lists <strong><span class="startItem">x</span> - <span class="endItem">y</span></strong> of <strong> ${geneListInstanceList.size()}</strong>.</span>
        <span style="padding-left: 10px;">
        View
          </span>
      <select class="pagesize small">
        <option value="5">5</option>
        <option value="10">10</option>
        <option selected="selected" value="20">20</option>
        <option value="50">50</option>
      </select>
      gene lists per page
      </div>
    </div></td></tr>
      </tfoot>
      
    </table>
  </div>

</div>
</body>
</html>
