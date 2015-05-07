<%@ page import="org.sagres.geneList.GeneList; org.sagres.geneList.GeneListDetail" %>
<html>
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
  <meta name="layout" content="main"/>
  <g:set var="entityName" value="${message(code: 'geneList.label', default: 'Gene List')}"/>
  <title>Gene List: ${geneListInstance.name}</title>
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
        <li><g:link class="list" action="list"><g:message code="default.list.label" args="[entityName]"/></g:link></li>
    <sec:ifAnyGranted roles="ROLE_USER">
  	  <li><g:link class="create" action="create"><g:message code="default.new.label" args="[entityName]"/></g:link></li>
    </sec:ifAnyGranted>
  </ul>


<div class="list">
  <h2>${geneListInstance.name} <small>- ${geneListInstance.description}, Category: ${geneListInstance.geneListCategory.name}, Count: ${GeneListDetail.countByGeneList(geneListInstance)} genes</small></h2>
		<g:if test="${flash.message}">
			<div class="message">
				${flash.message}
			</div>
		</g:if>
		<g:hasErrors bean="${feedback}">
			<div class="errors">
				<g:renderErrors bean="${feedback}" as="list" />
			</div>
		</g:hasErrors>
  <div>
    <table class="zebra-striped">
      <thead>
        <tr>
          <th>Symbol</th>
          <th>Gene ID</th>
          <th>Official Full Name</th>
        </tr>
      </thead>
      <tbody>
        <g:each in="${geneListDetails}" var="gene">
          <tr>
            <td>${gene.symbol}</td>
            <td>${gene.geneId}</td>
            <td>${gene.name}</td>
          </tr>
        </g:each>
      </tbody>
    </table>
  </div>
</div>
</body>
</html>
