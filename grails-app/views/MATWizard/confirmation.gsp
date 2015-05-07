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
			<h2>Confirmation</h2>
	<g:if test="${flash.message}">
		<div class="alert-message matwiz-message error">${flash.message}</div>
	</g:if>
	<g:hasErrors bean="${wizardInstance}">
    <div class="alert-message matwiz-message error">
			<g:renderErrors bean="${wizardInstance}" as="list"/>
		</div>
	</g:hasErrors>
				<form action="${createLink(action: 'saveConfirmation')}" method="post" class="mat-mainform" name="wizardForm">
				<div class="mat-upload-form">
					<g:hiddenField name="id" value="${matWizardInstance.id}"/>
					<g:hiddenField name="step" value="7"/>



					<p class="last">You are about to generate the module analysis for your data. Please confirm the information
		below and click on start.</p>

					 <p>
								<label class="nopad">Signal Data:</label> ${matWizardInstance?.signalData?: "Missing"}
					</p>


					<p>
						<label class="nopad">File format type:</label> ${matWizardInstance?.fileType?:"Missing"}

					</p>

					<p>
						<label class="nopad">Number of samples:</label> ${matWizardInstance?.sampleCount?:"Missing"}

					</p>



					<p>
						<label class="nopad">Control group:</label> ${matWizardInstance?.controlLabel?:"Missing"}

					</p>

					<p class="last">
						<label class="nopad">Case group:</label> ${matWizardInstance?.caseLabel?:"Missing"}

					</p>

					<p>If you would like to receive an email confirmation when the analysis is finished please enter your email address below.</p>
					<p><label>Email Address:</label><g:textField name="userEmail" value="${matWizardInstance.userEmail?:''}"/></p>


					<div class="prevnext">
            <g:link action="setParameters" id="${matWizardInstance.id}" class="btn small primary prev">Cancel</g:link>
						<g:submitButton name="Next" value="Next" class="btn small primary next"/>
					</div>




				</div>

				</form>





	</div>
</body>
</html>
