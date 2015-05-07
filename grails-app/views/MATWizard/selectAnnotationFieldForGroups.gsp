<%@ page import="org.sagres.mat.Analysis" %>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<meta name="layout" content="MATWizard"/>
	<g:set var="entityName" value="${message(code: 'analysis.label', default: 'Analysis')}"/>

	<title><g:message code="default.create.label" args="[entityName]"/></title>
	<link href="https://ajax.googleapis.com/ajax/libs/jqueryui/1.8/themes/base/jquery-ui.css" rel="stylesheet"
				type="text/css"/>
	<script src="https://ajax.googleapis.com/ajax/libs/jquery/1.5/jquery.min.js"></script>
	<script src="https://ajax.googleapis.com/ajax/libs/jqueryui/1.8/jquery-ui.min.js"></script>
</head>

<body>

<g:javascript>
</g:javascript>
<div class="mat-container">
	<h1>Module Analysis Wizard</h1>

	<h2>Select Groups</h2>


	<form action="${createLink(action: 'saveAnnotationField')}" method="post" class="mat-mainform">
		<div class="mat-upload-form matform" >
			<g:hiddenField name="id" value="${matWizardInstance.id}"/>
			<input type="hidden" name="step" value="3.5" />


			<p>Since you have provided an annotation file, we need to select what grouping you wish to use to in the analysis
			</p>


			<p class="last">We found multiple group sets in your annotation. Please pick one for your analysis from the list below.
      </p>
      <ul class="full">
<g:each in="${groupInfo}" status="i" var="group">
	 <li><input type="radio" name="groupSetName" value="${group.key}"/><label class="nowidth"> ${group.key}
		 <span class="cat_number"> (${group.value.size()} groups)</span></label></li>
</g:each>
				<li><input type="radio" name="groupSetName" value="none" selected>
					 <label class="nowidth"> None of the above
		 <span class="cat_number"> (You will create your own grouping)</span></label>
				</li>
       </ul>
			<div class="prevnext">
        <g:link action="signalData" id="${matWizardInstance.id}" class="btn small primary prev">Back</g:link>
				<g:submitButton name="Next" value="Next" class="btn small primary next"/>
			</div>
		</div>
	</form>

</div>
</body>
</html>
