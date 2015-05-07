<%@ page import="org.sagres.project.Project" %>
<!doctype html>
<html>
	<head>
		<meta name="layout" content="projectMain">
		<g:set var="entityName" value="${message(code: 'project.label', default: 'Project')}" />
		<title><g:message code="default.list.label" args="[entityName]" /></title>
	</head>
	<body>
	<div class="container">

      <!-- Main hero unit for a primary marketing message or call to action -->
      <div class="hero-unit">
        <h1>Project Tracker</h1>
        <p>
        	Keep track of sample preparation, laboratory analysis, bioinformatics analysis, and other aspects of public projects.
        	Also track personal projects without exposing them to the world.
        	Add tasks and resources to your projects, and view projects and resources in a Gantt chart.
        </p>
      </div>

      <!-- Example row of columns -->
      <div class="row-fluid">
        <div class="span4">
          <h2>Public Projects</h2>
          <p>View all Public Projects.</p>
          <p><g:link class="btn primary" action="list">Public Projects &raquo;</g:link></p>
        </div>
        <div class="span4">
          <h2>Private Projects</h2>
          <p>View your Private Projects.</p>
          <sec:ifLoggedIn>
	  		<p><g:link class="btn primary" action="privateList">Private Projects &raquo;</g:link></p>
  		  </sec:ifLoggedIn>
  		  <sec:ifNotLoggedIn>
  			<p><a class="btn primary" href="${createLink(controller:'login', params:[ 'lastVisitedPage':request.forwardURI.encodeAsURL() ])}">Log in &raquo;</a></p>
  		  </sec:ifNotLoggedIn>
       </div>
        <div class="span4">
          <h2>Gantt Chart</h2>
          <p>View charts for projects or resources.</p>
          <p><g:link class="btn primary" action="gantt">Gantt Charts &raquo;</g:link></p>
        </div>
      </div>
      <footer>
        <p>&copy; Benaroya Research Institute 2013</p>
      </footer>
    </div> <!-- /container -->
	</body>
</html>
