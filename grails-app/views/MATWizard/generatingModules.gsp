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

			<g:javascript>
		function checkWizardStatus() {
			$.ajax({
				type: "GET",
				url: "${createLink(action:'wizardStatus', id:matWizardInstance.id)}",
				async: true,
				cache: false,
				timeout: 20000,
				success: function(data) {
					//check status
					if ("${matWizardInstance.wizardAvailableStatus[2]}" == data.status ||
					  "${matWizardInstance.wizardAvailableStatus[3]}"   == data.status ||
					   "${matWizardInstance.wizardAvailableStatus[4]}"  == data.status ) {
								window.location="${createLink(action:'moduleAnalysisResults', id:matWizardInstance.id)}";
					   } else {
							setTimeout(
								'checkWizardStatus()',
								60000 //1 minute
							);
					}
				},
				error: function(XMLHttpRequest, textStatus, errorThrown) {
					setTimeout(
						'checkWizardStatus()',
						60000 //1 minute
					);
				}
			});
		};


	</g:javascript>

<g:javascript>
	$(document).ready(function() {
			checkWizardStatus();
		});

</g:javascript>
<h1>Module Analysis Wizard</h1>
  <h2>Generating Modules...</h2>


    <form action="${createLink(action:'saveEmailAddress')}" method="post" class="mat-mainform">
		<div class="mat-upload-form">
			<g:hiddenField name="step" value="8"/>
			<g:hiddenField name="id" value="${matWizardInstance.id}"/>
			<g:hiddenField name="analysisId" value="${matWizardInstance.analysisId}"/>

          <div class="waiting"><img src="${resource(dir: 'images', file: 'waiting.gif')}" alt="building modules..." /></div>

			<p class="last">Depending on the size of your dataset, the analysis may take anywhere between a few minutes and several hours.  If you remain on this page
			it will show you when the analysis is complete and provide you a link to take you to the results.  Alternatively, you can provide your email address and we will
			notify you when the analysis is done and email you a link to the analysis.

			<g:if test="${matWizardInstance.userEmail == null || matWizardInstance.userEmail.length() < 5}">
			<p>Or enter your email address and we will email you once the results are ready.</p>
			<p><label>Email Address:</label><g:textField name="userEmail" value="${matWizardInstance.userEmail?:''}"/>
				<button class="btn primary small match">OK</button></p>
			</g:if>




		</div>

    </form>



	</div>
</body>
</html>
