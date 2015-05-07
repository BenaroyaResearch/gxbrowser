<%@ page import="org.sagres.mat.Analysis" %>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<meta name="layout" content="MATWizard"/>
	<g:set var="entityName" value="${message(code: 'analysis.label', default: 'Analysis')}"/>
	<title><g:message code="default.create.label" args="[entityName]"/></title>
</head>

<body>
	<div class="mat-container">


<h1>Module Analysis Wizard</h1>
  <h2>Completed Generating Modules...</h2>


    <form action="/MAT/MATAnalysisGroupSet/index" method="post" class="mat-mainform">
		<div class="mat-upload-form">

          <div class="waiting"><img src="${resource(dir: 'images', file: 'finished.png')}" alt="" style="margin-bottom: 40px;" />

		  <p><a href="${createLink(controller: 'analysis', action: 'show', id: matWizardInstance.analysisId)}" class="btn primary large">View Results</a></p>
		  </div><!---end waiting-->


		</div><!--end mat-uploadform-div-->

    </form>




	</div>
</body>
</html>
