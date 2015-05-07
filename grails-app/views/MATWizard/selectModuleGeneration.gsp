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

	<h2>Select Module Generation</h2>

	<form action="${createLink(action: 'saveModuleGeneration')}" method="post" class="mat-mainform" id="wizardform">
		<div class="mat-upload-form">
			<g:hiddenField name="step" value="3"/>

			<g:hiddenField name="id" value="${matWizardInstance.id}"/>

			<p>You now need to select the generation of modules that you would like to use for this analysis. For more information on the different generations please consult our module wiki at
				<a href="">http://ourmodulewiki.itn.org</a></p>

			<label class="nopad">Platform type:</label> Illumina Background Subtracted


			<div class="formsection formsection3">
				<label>Generation:</label>
				<g:select from="${generations}" optionKey="key" optionValue="value" name="generationId"/>
				<p style="font-style:italic; padding-left:150px;">
					If you don't know what this means, just leave it on the default selection
				</p>
			</div>

			<div class="prevnext">
        <g:link action="uploadSignalData" id="${matWizardInstance.id}" class="btn small primary prev">Back</g:link>
				<a href="javascript:document.forms['wizardform'].submit();" class="btn small primary next">Next</a>
			</div>
		</div>

	</form>

</div>
</body>
</html>
