<%@ page import="org.sagres.mat.Analysis" %>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<meta name="layout" content="MATWizard"/>
	<g:set var="entityName" value="${message(code: 'analysis.label', default: 'Analysis')}"/>
	<title><g:message code="default.create.label" args="[entityName]"/></title>

</head>

<body>

		<g:javascript>
		function checkFileStatus() {

				$.ajax({
				type: "GET",
				url: "${createLink(action:'fileStatus', id:matWizardInstance.id)}",
				async: true,
				cache: false,
				timeout: 20000,
				success: function(data) {
					//check status
					if ("${matWizardInstance.fileProcessStates[2]}" == data.status ||
					   "${matWizardInstance.fileProcessStates[3]}"  == data.status ) {
					   $("#pageContent").load("${createLink(action: 'signalData', id: matWizardInstance.id)}");
							//	window.location="${createLink(action:'moduleAnalysisResults', id:matWizardInstance.id)}";
					   } else {
							setTimeout(
								'checkFileStatus()',
								10000 //1 minute
							);
					}
				},
				error: function(XMLHttpRequest, textStatus, errorThrown) {
					setTimeout(
						'checkFileStatus()',
						10000 //1 minute
					);
				}
			});
		};


	</g:javascript>

<g:javascript>
	$(document).ready(function() {
		  //alert('testing');
			checkFileStatus();
		});
</g:javascript>
<div id="pageContent">

<div class="mat-container">
	<h1>Module Analysis Wizard</h1>
		<h2>Processing Signal Data: </h2>

		<g:if test="${flash.message}">
		<div class="alert-message matwiz-message error">${flash.message}</div>
	</g:if>
	<g:hasErrors bean="${wizardInstance}">
    <div class="alert-message matwiz-message error">
			<g:renderErrors bean="${wizardInstance}" as="list"/>
		</div>
	</g:hasErrors>
			<form action="${createLink(action: 'saveModuleGeneration')}" method="post" class="mat-mainform" name="wizardForm">
			<div class="mat-upload-form">
				<p class="">
					It takes a short time for us to format the data to run Module Analysis on and verify that it's what we expect. When the verification is complete, this page will display
					a short summary of what we have found.
				</p>
				<div class="waiting"><img src="${resource(dir: 'images', file: 'mat-verifying.gif')}" alt="building modules..." /></div>
			</div>
			</form>
	</div>
</div>

</body>
</html>
